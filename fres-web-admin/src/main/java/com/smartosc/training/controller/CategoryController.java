package com.smartosc.training.controller;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.smartosc.training.dto.CategoryDTO;
import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.service.RestService;
import com.smartosc.training.utils.FileUtil;
import com.smartosc.training.utils.JWTUtil;
import com.smartosc.training.utils.RestPageImpl;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.awt.Color;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @implNote export category report
 * @author thaotp
 */

@Controller
@RequestMapping(value = "/")
public class CategoryController {
    public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    @Autowired
    private RestService restService;

    @Autowired
    private JWTUtil jwtTokenUtil;

    @Value("${api.url}")
    private String url;

    @Value("${prefix.category}")
    private String prefixUrl;

    @Autowired
    private ServletContext context;

    private static final String TEMP_EXPORT_DATA_DIRECTORY = "\\resources\\reports";
    private static final String EXPORT_DATA_REPORT_FILE_NAME = "category";

    public List<CategoryDTO> getAllCategories(){
        HttpHeaders header = new HttpHeaders();
        List<CategoryDTO> categoryDTOList = restService.execute(url + "categories/categories",
                HttpMethod.GET,
                header,
                null,
                new ParameterizedTypeReference<APIResponse<List<CategoryDTO>>>() {
                }).getData();
        return categoryDTOList;
    }
    @PostMapping("categories")
    public String getListAllCategoryBySearchValue(@RequestParam("table_search") String searchValue) {
        return "redirect:/categories?searchValue=" + searchValue;
    }

    @GetMapping("categories")
    public String getListAllCategoryPage(Model model,
                                         @RequestParam(defaultValue = "", required = false) String searchValue,
                                         @RequestParam(defaultValue = "0", required = false) Integer pageNo,
                                         @RequestParam(defaultValue = "10", required = false) Integer pageSize,
                                         @RequestParam(defaultValue = "categoryId", required = false) String sortBy) {
        APIResponse<RestPageImpl<CategoryDTO>> responseData = getAllCategories(searchValue, pageNo, pageSize, sortBy);
        RestPageImpl<CategoryDTO> categories = null;
        if (responseData.getStatus() == 200) {
            categories = responseData.getData();
        }
        model.addAttribute("data", categories);
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("searchValue", searchValue);
        return "category";
    }

    private APIResponse<RestPageImpl<CategoryDTO>> getAllCategories(String searchValue, Integer pageNo, Integer pageSize, String sortBy) {
        Map<String, Object> values = new HashMap<>();
        values.put("searchValue", searchValue);
        values.put("pageNo", pageNo);
        values.put("pageSize", pageSize);
        values.put("sortBy", sortBy);
        return restService.execute(
                new StringBuilder(url).append("categories")
                        .append("?searchValue={searchValue}&page={pageNo}&size={pageSize}&sortBy={sortBy}").toString(),
                HttpMethod.GET,
                null,
                null,
                new ParameterizedTypeReference<APIResponse<RestPageImpl<CategoryDTO>>>() {
                },
                values);
    }

    @GetMapping("categories/{categoryId}")
    public String detail(@PathVariable("categoryId") Integer categoryId, Model model) {
        String authToken = jwtTokenUtil.getJwtTokenFromSecurityContext();
        HttpHeaders header = new HttpHeaders();
        CategoryDTO categoryDTO = restService.execute(
                url + "categories/" + categoryId,
                HttpMethod.GET,
                header,
                null,
                new ParameterizedTypeReference<APIResponse<CategoryDTO>>() {
                }).getData();
        if (categoryDTO != null) {
            model.addAttribute("category", categoryDTO);
        }
        return "category-detail";
    }

    @PostMapping("categories/import")
    public String importFile(@RequestParam("file") MultipartFile file) throws IOException {
//        String authToken = jwtTokenUtil.getJwtTokenFromSecurityContext();
        HttpHeaders header = new HttpHeaders();
        try {
            List<CategoryDTO> categoryDTOList = null;
            List<CategoryDTO> categories = restService.execute(url + "categories/categories", HttpMethod.GET, header, null, new ParameterizedTypeReference<APIResponse<List<CategoryDTO>>>() {
            }).getData();

            Map<Integer, String> mapCategory = categories.stream().collect(Collectors.toMap(CategoryDTO::getCategoryId, CategoryDTO::getCategoryName));

            if (file.isEmpty()) {

            }
            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
            if (extension.equalsIgnoreCase("xlsx") || extension.equalsIgnoreCase("xls")) {
                categoryDTOList = readDataFromExcel(file, extension);
            }
            if (extension.equalsIgnoreCase("csv") || extension.equalsIgnoreCase("txt")) {
                categoryDTOList = readDataFromCsv(file, mapCategory);
            }
            if (!categoryDTOList.isEmpty()) {
                header.setBearerAuth(jwtTokenUtil.getJwtTokenFromSecurityContext());
                boolean check = restService.execute(
                        new StringBuilder(url).append("categories/import").toString(),
                        HttpMethod.POST,
                        header,
                        categoryDTOList,
                        new ParameterizedTypeReference<APIResponse<Boolean>>() {
                }).getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return "redirect:/categories";
    }

    @PostMapping("categories/create")
    public String save(@ModelAttribute("categoryRequest") CategoryDTO categoryRequest) {
        String authToken = jwtTokenUtil.getJwtTokenFromSecurityContext();
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(authToken);
        header.setContentType(MediaType.APPLICATION_JSON);
        restService.execute(
                url + "categories",
                HttpMethod.POST,
                header,
                categoryRequest,
                new ParameterizedTypeReference<APIResponse<CategoryDTO>>() {
                });
        return "redirect:/categories";
    }

    private List<CategoryDTO> readDataFromExcel(MultipartFile file, String extension) {
        Workbook workbook = null;
        try {
            List<CategoryDTO> categoryDTOList = new ArrayList<>();
            workbook = getWorkbook(file, extension);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            rows.next();
            while (rows.hasNext()) {
                Row row = rows.next();
                CategoryDTO categoryDTO = new CategoryDTO();
                categoryDTO.setCategoryName(row.getCell(1).getStringCellValue());
                categoryDTO.setDescription(row.getCell(2).getStringCellValue());
                categoryDTO.setImage(row.getCell(3).getStringCellValue());
                categoryDTO.setStatus((int) row.getCell(4).getNumericCellValue());
                //            categoryDTO.setCreatedAt(row.getCell(5).getDateCellValue());
                //            categoryDTO.setUpdatedAt(row.getCell(6).getDateCellValue());
                categoryDTOList.add(categoryDTO);
            }
            return categoryDTOList;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private List<CategoryDTO> readDataFromCsv(MultipartFile file, Map<Integer, String> mapCategories) throws IOException, ParseException {
        try {
            List<CategoryDTO> categoryDTOList = new ArrayList<>();
            InputStreamReader reader = new InputStreamReader(file.getInputStream());
            CSVParser csvParser = new CSVParserBuilder().withSeparator(';').build();
            CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(csvParser).withSkipLines(1).build();
            List<String[]> rows = csvReader.readAll();
            for (int i = 0; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length <= 1) {
                    //DO NOTHING
                } else if (row.length > 1 && row.length <= 6) {
                    CategoryDTO categoryDTO = new CategoryDTO();
                    if (mapCategories.containsValue(row[0])) {
                        System.out.println("Trùng tên rồi!!!!");
                    } else {
                        categoryDTO.setCategoryName(row[0]);
                    }
                    categoryDTO.setDescription(row[1]);
                    categoryDTO.setImage(row[2]);
                    categoryDTO.setStatus(Integer.parseInt(row[3].trim()));
                    categoryDTO.setCreatedAt(simpleDateFormat.parse(row[4]));
                    categoryDTO.setUpdatedAt(simpleDateFormat.parse(row[5]));
                    categoryDTOList.add(categoryDTO);
                }
            }
            return categoryDTOList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Workbook getWorkbook(MultipartFile file, String extension) {
        Workbook workbook = null;
        try {
            if (extension.equalsIgnoreCase("xls")) {
                workbook = new HSSFWorkbook(file.getInputStream());
            } else if (extension.equalsIgnoreCase("xlsx")) {
                workbook = new XSSFWorkbook(file.getInputStream());
            }
            return workbook;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("categories/create")
    public String show(@ModelAttribute("categoryRequest") CategoryDTO categoryRequest, Model model) {
        model.addAttribute("categoryRequest", categoryRequest);
        return "add-category";
    }

    @GetMapping("categories/edit/{categoryId}")
    public String edit(@PathVariable("categoryId") Integer categoryId, Model model) {
        String authToken = jwtTokenUtil.getJwtTokenFromSecurityContext();
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(authToken);
        if (categoryId == null) {
            return "Error";
        }
        CategoryDTO category = restService.execute(
                url + "categories/" + categoryId,
                HttpMethod.GET,
                header,
                null,
                new ParameterizedTypeReference<APIResponse<CategoryDTO>>() {
                }).getData();
        model.addAttribute("category", category);
        return "edit-category";
    }

    @PostMapping("categories/edit/{categoryId}")
    public String editO(@ModelAttribute("category") CategoryDTO categoryDTO, @PathVariable("categoryId") Integer categoryId) {
        String authToken = jwtTokenUtil.getJwtTokenFromSecurityContext();
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(authToken);
        header.setContentType(MediaType.APPLICATION_JSON);
        restService.execute(
                url + "categories/" + categoryId,
                HttpMethod.PUT,
                header,
                categoryDTO,
                new ParameterizedTypeReference<APIResponse<CategoryDTO>>() {
                });
        return "redirect:/categories";
    }

    @PostMapping("categories/delete")
    public String delete(Model model,
                         @RequestParam(defaultValue = "", required = false) String searchValue,
                         @RequestParam Integer id) {
        String authToken = jwtTokenUtil.getJwtTokenFromSecurityContext();
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(authToken);
        Map<String, Object> values = new HashMap<>();
        values.put("id", id);
        restService.execute(
                new StringBuilder(url).append("categories").append("?id={id}").toString(),
                HttpMethod.DELETE,
                header,
                null,
                new ParameterizedTypeReference<APIResponse<CategoryDTO>>() {
                },
                values);
        return "redirect:/categories?searchValue=" + searchValue;
    }

    @GetMapping("categories/export/excel")
    public void exportCategoryToExcelFile(HttpServletResponse response) throws FileNotFoundException, JRException {
        List<CategoryDTO> result = getAllCategories();
        String fullPath = this.exportReport(result, context, EXPORT_DATA_REPORT_FILE_NAME);
        if(fullPath != null){
            FileUtil.fileDownload(fullPath, response, EXPORT_DATA_REPORT_FILE_NAME, "xlsx", context);
        }
    }

    public String exportReport(List<CategoryDTO> categoryDTOList, ServletContext context, String fileName) throws FileNotFoundException, JRException {
        String path = context.getRealPath(TEMP_EXPORT_DATA_DIRECTORY);
        File file = new File(path);
        if (!file.exists()) {
            new File(path).mkdirs();
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file + "\\" + fileName + ".xlsx");
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet worksheet = workbook.createSheet("Category");
            worksheet.setDefaultColumnWidth(20);

            XSSFRow headerRow = worksheet.createRow(0);
            XSSFCellStyle headerCellStyle = workbook.createCellStyle();
            XSSFFont font = workbook.createFont();
            font.setFontName(XSSFFont.DEFAULT_FONT_NAME);
            font.setColor(new XSSFColor(Color.WHITE));
            headerCellStyle.setFont(font);
            headerCellStyle.setFillForegroundColor(new XSSFColor(new Color(135, 206, 250)));
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFCell categoryId = headerRow.createCell(0);
            categoryId.setCellValue("ID");
            categoryId.setCellStyle(headerCellStyle);

            XSSFCell name = headerRow.createCell(1);
            name.setCellValue("Name");
            name.setCellStyle(headerCellStyle);

            XSSFCell description = headerRow.createCell(2);
            description.setCellValue("Description");
            description.setCellStyle(headerCellStyle);

            XSSFCell status = headerRow.createCell(3);
            status.setCellValue("Status");
            status.setCellStyle(headerCellStyle);

            XSSFCell createdAt = headerRow.createCell(4);
            createdAt.setCellValue("createdAt");
            createdAt.setCellStyle(headerCellStyle);

            XSSFCell updatedAt = headerRow.createCell(5);
            updatedAt.setCellValue("updatedAt");
            updatedAt.setCellStyle(headerCellStyle);

            if (!categoryDTOList.isEmpty()) {
                for (int i = 0; i < categoryDTOList.size(); i++) {
                    CategoryDTO categoryDTO = categoryDTOList.get(i);
                    XSSFRow bodyRow = worksheet.createRow(i + 1);
                    XSSFCellStyle bodyCellStyle = workbook.createCellStyle();
                    bodyCellStyle.setFillForegroundColor(new XSSFColor(Color.WHITE));

                    XSSFCell categoryIdValue = bodyRow.createCell(0);
                    categoryIdValue.setCellValue(i + 1);
                    categoryIdValue.setCellStyle(bodyCellStyle);

                    XSSFCell nameValue = bodyRow.createCell(1);
                    nameValue.setCellValue(categoryDTO.getCategoryName());
                    nameValue.setCellStyle(bodyCellStyle);

                    XSSFCell descriptionValue = bodyRow.createCell(2);
                    descriptionValue.setCellValue(categoryDTO.getDescription());
                    descriptionValue.setCellStyle(bodyCellStyle);

                    XSSFCell statusValue = bodyRow.createCell(3);
                    statusValue.setCellValue(categoryDTO.getStatus());
                    statusValue.setCellStyle(bodyCellStyle);

                    CreationHelper creationHelper = workbook.getCreationHelper();
                    CellStyle cellStyle = workbook.createCellStyle();
                    cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("dd/MM/yyyy HH:mm:ss"));

                    XSSFCell createdAtValue = bodyRow.createCell(4);
                    createdAtValue.setCellValue(categoryDTO.getCreatedAt());
                    createdAtValue.setCellStyle(cellStyle);

                    XSSFCell updatedAtValue = bodyRow.createCell(5);
                    updatedAtValue.setCellValue(categoryDTO.getUpdatedAt());
                    updatedAtValue.setCellStyle(cellStyle);
                }
            }
            workbook.write(fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

            return file + "\\" + fileName + ".xlsx";

        } catch (Exception e) {
            return null;
        }
    }

    @GetMapping("categories/export/report/{type}")
    public void exportUserDataToReportFile(@PathVariable("type") String type, HttpServletResponse response)
            throws JRException {
        String fullPath = this.generateCategoryJasperReport(type, context, EXPORT_DATA_REPORT_FILE_NAME);
        if (fullPath != null) {
            FileUtil.fileDownload(fullPath, response, EXPORT_DATA_REPORT_FILE_NAME, type, context);
        }
    }

//    using jasper report
    private String generateCategoryJasperReport(String type, ServletContext context, String fileName) throws JRException {
        String tempFileName = null;
        List<CategoryDTO> categoryDTOList = getAllCategories();

        String filePath = context.getRealPath((TEMP_EXPORT_DATA_DIRECTORY));
        File file = new File(filePath);

        if(!file.exists()){
            new File(filePath).mkdirs();
        }

        InputStream resourceStream = getClass().getResourceAsStream("/categories.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(resourceStream);
        try{
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(categoryDTOList);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, null, dataSource);
            if(type.equalsIgnoreCase("pdf")){
                tempFileName = file + "\\"+fileName+".pdf";
                JasperExportManager.exportReportToPdfFile(jasperPrint, tempFileName);
            } else if(type.equalsIgnoreCase("xlsx")){
                tempFileName = file+"\\"+fileName+".xlsx";
                JRXlsExporter exporter = new JRXlsExporter();
                exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(tempFileName));

                SimpleXlsxReportConfiguration config = new SimpleXlsxReportConfiguration();
                config.setDetectCellType(true);
                config.setRemoveEmptySpaceBetweenColumns(true);
                exporter.setConfiguration(config);
                exporter.exportReport();
            } else {
                throw new FileNotFoundException("This file is not supported");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return tempFileName;
    }
}
