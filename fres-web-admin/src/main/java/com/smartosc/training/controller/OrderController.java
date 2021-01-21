package com.smartosc.training.controller;

import java.io.*;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.cloudinary.utils.StringUtils;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.smartosc.training.dto.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.service.RestService;
import com.smartosc.training.utils.JWTUtil;
import com.smartosc.training.utils.RestPageImpl;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(value = "/order")
public class OrderController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private RestService restService;

    @Autowired
    private ServletContext context;

    @Autowired
    private JWTUtil jwtTokenUtil;

    @Value("${api.url}")
    private String url;

    private static final String DATE_PATTERN = "dd/MM/yyyy HH:mm:ss";

    @PostMapping
    public String getListAllProductBySearchValue(@RequestParam("table_search") String searchValue) {
        return "redirect:/order?searchValue=" + searchValue;
    }

    @GetMapping
    public String list(Model model,
                       @RequestParam(defaultValue = "", required = false) String searchValue,
                       @RequestParam(defaultValue = "0", required = false) Integer pageNo,
                       @RequestParam(defaultValue = "5", required = false) Integer pageSize,
                       @RequestParam(defaultValue = "orderId", required = false) String sortBy) {
        String authToken = jwtTokenUtil.getJwtTokenFromSecurityContext();
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(authToken);
        APIResponse<RestPageImpl<OrdersDTO>> responseData = getAllOrders(searchValue, pageNo, pageSize, sortBy);
        RestPageImpl<OrdersDTO> orders = null;
        if (responseData.getStatus() == 200) {
            orders = responseData.getData();
        }
        model.addAttribute("data", orders);
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("searchValue", searchValue);
        return "orders";
    }

    @GetMapping("/export/excel")
    public void export(HttpServletResponse response) {
        String authToken = jwtTokenUtil.getJwtTokenFromSecurityContext();
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(authToken);
        List<OrdersDTO> listOrdersDTO = restService.execute(url + "order/export", HttpMethod.GET, header, null,
                new ParameterizedTypeReference<APIResponse<List<OrdersDTO>>>() {
                }).getData();

        String fullPath = this.generateOrderExcel(listOrdersDTO, context);
        if (fullPath != null) {
            this.fileDownload(fullPath, response, "orders", "xlsx");
        }
    }

    @GetMapping("/export/report/{type}")
    public void exportProductDataToReportFile(@PathVariable("type") String type, HttpServletResponse response)
            throws FileNotFoundException, JRException {
        String authToken = jwtTokenUtil.getJwtTokenFromSecurityContext();
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(authToken);
        List<OrdersDTO> listOrdersDTO = restService.execute(url + "order/export", HttpMethod.GET, header, null,
                new ParameterizedTypeReference<APIResponse<List<OrdersDTO>>>() {
                }).getData();
        String fullPath = this.generateOrderPDF(listOrdersDTO, type, context, "orders");
        if (fullPath != null) {
            this.fileDownload(fullPath, response, "orders", type);
        }


    }

    private String generateOrderPDF(List<OrdersDTO> listOrdersDTO, String type, ServletContext context, String fileName)
            throws FileNotFoundException, JRException {
        Connection conn = null;
        String tempFileName = null;
        String filePath = context.getRealPath("\\resources\\reports");
        File file = new File(filePath);
        if (!file.exists()) {
            new File(filePath).mkdirs();
        }
        InputStream resourceStream = getClass().getResourceAsStream("/order.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(resourceStream);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(listOrdersDTO, false);
        Map<String, Object> values = new HashMap<>();
        values.put("createdBy", "VegetFood");
        try {
            conn = DriverManager.getConnection("jdbc:mysql://192.168.101.28:3306/group2?serverTimezone=UTC", "group2", "admin");
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperReport, values, conn);
            if (type.equalsIgnoreCase("pdf")) {
                tempFileName = file + "\\" + fileName + ".pdf";
                JasperExportManager.exportReportToPdfFile(jasperPrint, tempFileName);
            } else if (type.equalsIgnoreCase("xlsx")) {
                tempFileName = file + "\\" + fileName + ".xlsx";
                JRXlsxExporter exporter = new JRXlsxExporter();
                exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(tempFileName));
                SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
                configuration.setDetectCellType(true);
                configuration.setCollapseRowSpan(false);
                exporter.setConfiguration(configuration);
                exporter.exportReport();
            } else {
                throw new FileNotFoundException("This file extension is not support!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return tempFileName;
    }


    private String generateOrderExcel(List<OrdersDTO> listOrdersDTO, ServletContext context) {
        String filePath = context.getRealPath("/resources/reports");
        File file = new File(filePath);
        if (!file.exists()) {
            new File(filePath).mkdirs();
        }
        try(
                FileOutputStream fos = new FileOutputStream(file + "/Order.xlsx");
                XSSFWorkbook workbook = new XSSFWorkbook();) {
            XSSFSheet sheet = workbook.createSheet("Order_Info");
            int rowNum = 0;
            Row firstRow = sheet.createRow(rowNum++);
            Cell firstCell = firstRow.createCell(0);
            firstCell.setCellValue("List Order");

            //Create Heading
            Row rowHeading = sheet.createRow(0);
            rowHeading.createCell(0).setCellValue("Full name");
            rowHeading.createCell(1).setCellValue("User name");
            rowHeading.createCell(2).setCellValue("Total price");
            rowHeading.createCell(3).setCellValue("Status");
            rowHeading.createCell(4).setCellValue("Price");
            rowHeading.createCell(5).setCellValue("Quantity");
            rowHeading.createCell(6).setCellValue("Product name");
            rowHeading.createCell(7).setCellValue("Created at");
            rowHeading.createCell(8).setCellValue("Updated at");


            for (OrdersDTO ordersDTO : listOrdersDTO) {
                Row row = sheet.createRow(rowNum++);

                List<OrderdetailDTO> listCategory = ordersDTO.getOrderDetailEntities();
                String result0 = listCategory.stream()
                        .map(n -> String.valueOf(n.getProductDTO().getProductName()))
                        .collect(Collectors.joining(","));

                String result1 = listCategory.stream()
                        .map(n -> String.valueOf(n.getProductDTO().getPrice()))
                        .collect(Collectors.joining(","));

                String result2 = listCategory.stream()
                        .map(n -> String.valueOf(n.getQuantity()))
                        .collect(Collectors.joining(","));

                Cell cell0 = row.createCell(0);
                cell0.setCellValue(ordersDTO.getUserDTO().getFullName());

                Cell cell1 = row.createCell(1);
                cell1.setCellValue(ordersDTO.getUserDTO().getUserName());

                Cell cell2 = row.createCell(2);
                cell2.setCellValue(ordersDTO.getTotalPrice());

                Cell cell3 = row.createCell(3);
                cell3.setCellValue(ordersDTO.getStatus());

                Cell cell4 = row.createCell(4);
                cell4.setCellValue(result1);

                Cell cell5 = row.createCell(5);
                cell5.setCellValue(result2);

                Cell cell6 = row.createCell(6);
                cell6.setCellValue(result0);

                CellStyle cellStyle = workbook.createCellStyle();
                CreationHelper createHelper = workbook.getCreationHelper();
                cellStyle.setDataFormat(createHelper.createDataFormat().getFormat(DATE_PATTERN));
                Cell cell7 = row.createCell(7);
                cell7.setCellValue(ordersDTO.getCreatedAt());
                cell7.setCellStyle(cellStyle);

                Cell cell8 = row.createCell(8);
                cell8.setCellValue(ordersDTO.getUpdatedAt());
                cell8.setCellStyle(cellStyle);
            }
            //Autofit
            for(int i = 0; i < 6; i++){
                sheet.autoSizeColumn(i);
            }

            workbook.write(fos);
            return file + "\\Order.xlsx";
        } catch (Exception e) {
            return null;
        }
    }

    @PostMapping("/import")
    public String mapReapExcelDatatoDB(@RequestParam("file") MultipartFile reapExcelDataFile) {

        String authToken = jwtTokenUtil.getJwtTokenFromSecurityContext();
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(authToken);

        List<OrdersDTO> ordersDTOS = null;
        String extension = FilenameUtils.getExtension(reapExcelDataFile.getOriginalFilename());
        if (extension.equalsIgnoreCase("xlsx") || extension.equalsIgnoreCase("xls")) {
            ordersDTOS = readDataFromExcel(reapExcelDataFile, extension);
        } else if (extension.equalsIgnoreCase("csv") || extension.equalsIgnoreCase("txt")) {
            ordersDTOS = readDataFromCsv(reapExcelDataFile);
        }
        if (ordersDTOS != null) {

            restService.execute(url + "order/import", HttpMethod.POST, header, ordersDTOS,
                    new ParameterizedTypeReference<APIResponse<Boolean>>() {
                    });
        }
        return "redirect:/order";
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
            LOGGER.error("Can't get workbook");
        }
        return null;
    }

    private void fileDownload(String fullPath, HttpServletResponse response, String fileName, String type) {
        File file = new File(fullPath);
        OutputStream os = null;
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file);) {
                String mimeType = context.getMimeType(fullPath);
                response.setContentType(mimeType);
                response.setHeader("content-disposition", "attachment; filename=" + fileName+"."+type);
                os = response.getOutputStream();
                byte[] buffer = new byte[4096];
                int bytesRead = -1;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                Files.delete(file.toPath());
            } catch (Exception e) {
                LOGGER.error("Can't export file: {}", e.getMessage());
            } finally {
                if(os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static boolean isRowEmpty(Row row) {
        boolean isEmpty = true;
        DataFormatter dataFormatter = new DataFormatter();

        if (row != null) {
            for (Cell cell : row) {
                if (dataFormatter.formatCellValue(cell).trim().length() > 0) {
                    isEmpty = false;
                    break;
                }
            }
        }

        return isEmpty;
    }


    private List<OrdersDTO> readDataFromExcel(MultipartFile file, String extension) {
        try {

            List<OrdersDTO> ordersDTOS = new ArrayList<>();
            Workbook workbook = getWorkbook(file, extension);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            rows.next();
            while (rows.hasNext()) {
                Row row = rows.next();
                if (!isRowEmpty(row)) {
                    OrdersDTO ordersDTO = new OrdersDTO();

                    String fullname = row.getCell(0).getStringCellValue().trim();
                    String username = row.getCell(1).getStringCellValue().trim();

                    UserDTO userDTO = new UserDTO();
                    userDTO.setFullName(fullname);
                    userDTO.setUserName(username);

                    ordersDTO.setUserDTO(userDTO);
                    ordersDTO.setTotalPrice(row.getCell(2).getNumericCellValue());
                    ordersDTO.setStatus((int) row.getCell(3).getNumericCellValue());

                    String[] price = row.getCell(4).getStringCellValue().split(";");
                    String[] quantity = row.getCell(5).getStringCellValue().split(";");
                    String[] productName = row.getCell(6).getStringCellValue().split(";");

                    List<OrderdetailDTO> orderdetailDTOS = new ArrayList<>();

                    for (int j = 0; j < price.length; j++) {
                        OrderdetailDTO orderdetailDTO = new OrderdetailDTO();
                        ProductDTO productDTO = new ProductDTO();
                        orderdetailDTO.setPrice(Double.parseDouble(price[j]));
                        orderdetailDTO.setQuantity(Integer.parseInt(quantity[j]));
                        productDTO.setProductName(productName[j]);
                        orderdetailDTO.setProductDTO(productDTO);

                        orderdetailDTOS.add(orderdetailDTO);
                    }

                    ordersDTO.setOrderDetailEntities(orderdetailDTOS);
                    ordersDTOS.add(ordersDTO);
                }

            }
            return ordersDTOS;
        } catch (Exception e) {
            LOGGER.error("Read file Excel has occurred error: {}", e.getMessage());
        }
        return null;
    }


    private List<OrdersDTO> readDataFromCsv(MultipartFile file) {
        List<OrdersDTO> ordersDTOS = new ArrayList<>();
        try {
            InputStreamReader reader = new InputStreamReader(file.getInputStream());
            CSVParser csvParser = new CSVParserBuilder().withSeparator(';').build();
            CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(csvParser).withSkipLines(1).build();
            List<String[]> rows = csvReader.readAll();

            for (int i = 0; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length > 1) {
                    OrdersDTO ordersDTO = new OrdersDTO();
                    UserDTO userDTO = new UserDTO();
                    userDTO.setFullName(row[0]);
                    userDTO.setUserName(row[1]);
                    ordersDTO.setUserDTO(userDTO);

                    ordersDTO.setTotalPrice(Double.parseDouble(row[2]));
                    ordersDTO.setStatus(Integer.parseInt(row[3]));

                    String[] price= row[4].split(",");
                    String[] quantity = row[5].split(",");
                    String[] productname = row[6].split(",");
                    List<OrderdetailDTO> orderdetailDTOS = new ArrayList<>();
                    for(int j =0;j<price.length;j++) {
                        OrderdetailDTO orderdetailDTO = new OrderdetailDTO();
                        ProductDTO productDTO = new ProductDTO();

                        orderdetailDTO.setPrice(Double.parseDouble(price[j]));
                        orderdetailDTO.setQuantity(Integer.parseInt(quantity[j]));
                        productDTO.setProductName(productname[j]);

                        orderdetailDTO.setProductDTO(productDTO);

                        orderdetailDTOS.add(orderdetailDTO);
                    }

                    ordersDTO.setOrderDetailEntities(orderdetailDTOS);

                    ordersDTO.setCreatedAt(new SimpleDateFormat(DATE_PATTERN).parse(row[7]));
                    ordersDTO.setUpdatedAt(new SimpleDateFormat(DATE_PATTERN).parse(row[8]));
                    ordersDTOS.add(ordersDTO);
                }
                else {
                    return new ArrayList<>();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Read file CSV has occurred error: {}", e.getMessage());
        }
        return ordersDTOS;
    }


    @GetMapping("/status")
    public String changeStatus(@RequestParam("id") Integer id, @RequestParam("status") Integer status) {
        OrdersDTO ordersDTO = new OrdersDTO();
        ordersDTO.setOrdersId(id);
        ordersDTO.setStatus(status);
        String authToken = jwtTokenUtil.getJwtTokenFromSecurityContext();
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(authToken);
        header.setContentType(MediaType.APPLICATION_JSON);
        restService.execute(
                url + "order/",
                HttpMethod.PUT,
                header,
                ordersDTO,
                new ParameterizedTypeReference<APIResponse<OrdersDTO>>() {
                });
        return "redirect:/order";
    }

    private APIResponse<RestPageImpl<OrdersDTO>> getAllOrders(String searchValue, Integer pageNo, Integer pageSize,
                                                              String sortBy) {
        Map<String, Object> values = new HashMap<>();
        values.put("searchValue", searchValue);
        values.put("pageNo", pageNo);
        values.put("pageSize", pageSize);
        values.put("sortBy", sortBy);
        return restService.execute(
                new StringBuilder(url).append("order")
                        .append("?searchValue={searchValue}&page={pageNo}&size={pageSize}&sortBy={sortBy}").toString(),
                HttpMethod.GET,
                null,
                null,
                new ParameterizedTypeReference<APIResponse<RestPageImpl<OrdersDTO>>>() {
                }, values);
    }

}
