package com.smartosc.training.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.smartosc.training.dto.CategoryDTO;
import com.smartosc.training.dto.ProductDTO;
import com.smartosc.training.dto.ProductFormDTO;
import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.service.RestService;
import com.smartosc.training.utils.ExportUtils;
import com.smartosc.training.utils.JWTUtil;
import com.smartosc.training.utils.RestPageImpl;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/")
public class ProductController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private RestService restService;

    @Autowired
    private ServletContext context;

    @Autowired
    private JWTUtil jwtTokenUtil;

    @Value("${api.url}")
    private String url;

    @Value("${prefix.product}")
    private String prefixUrl;

    @Value("${spring.datasource.url}")
    private  String datasource;

    private String path="\\";

    @Value("${spring.datasource.username}")
    private String user;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${cloudinary.cloud_name}")
    private String cloudName;

    @Value("${cloudinary.api_key}")
    private String apiKey;

    private String xlsx = ".xlsx";

    @Value("${cloudinary.api_secret}")
    private String apiSecret;

    private static final int BUFFER_SIZE = 4096;

    private static final String TEMP_EXPORT_DATA_DIRECTORY = "\\resources\\reports";
    private static final String EXPORT_DATA_REPORT_FILE_NAME = "products";
    private static final String CLOUDINARY_DIRECTORY_DEFAULT = "mm_images/profile/";

    @PostMapping("products")
    public String getListAllProductBySearchValue(@RequestParam("table_search") String searchValue) {
        return "redirect:/products?searchValue=" + searchValue;
    }

    @GetMapping("products")
    public String getListAllProductPage(Model model,
                                        @RequestParam(defaultValue = "", required = false) String searchValue,
                                        @RequestParam(defaultValue = "0", required = false) Integer pageNo,
                                        @RequestParam(defaultValue = "10", required = false) Integer pageSize,
                                        @RequestParam(defaultValue = "productId", required = false) String sortBy) {
        APIResponse<RestPageImpl<ProductDTO>> responseData = getAllProducts(searchValue, pageNo, pageSize, sortBy);
        RestPageImpl<ProductDTO> products = null;
        if (responseData.getStatus() == HttpStatus.OK.value()) {
            products = responseData.getData();
        }
        model.addAttribute("data", products.getContent());
        model.addAttribute("dataPageImpl", products);
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("searchValue", searchValue);
        return "products";
    }

    @PostMapping("products/import")
    public String mapReapExcel(@RequestParam("file") MultipartFile reapExcelDataFile) {
        String authToken = jwtTokenUtil.getJwtTokenFromSecurityContext();
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(authToken);

        List<ProductDTO> listProduct = restService.execute(url + "products/export", HttpMethod.GET, header, null,
                new ParameterizedTypeReference<APIResponse<List<ProductDTO>>>() {
                }).getData();

        Map<Integer, String> mapProduct = listProduct.stream().collect(Collectors.toMap(ProductDTO::getProductId, ProductDTO::getProductName));

        List<ProductDTO> productDTOS = null;
        String extension = FilenameUtils.getExtension(reapExcelDataFile.getOriginalFilename());
        if (extension.equalsIgnoreCase("xlsx") || extension.equalsIgnoreCase("xls")) {
            productDTOS = readDataFromExcel(reapExcelDataFile, extension, mapProduct);
        } else if (extension.equalsIgnoreCase("csv") || extension.equalsIgnoreCase("txt")) {
            productDTOS = readDataFromCsv(reapExcelDataFile, mapProduct);
        } else {
            //do nothing
        }
        if (productDTOS != null) {
            restService.execute(
                    new StringBuilder(url).append("products/import").toString(),
                    HttpMethod.POST,
                    header,
                    productDTOS,
                    new ParameterizedTypeReference<APIResponse<Boolean>>() {
                    },
                    new HashMap<>()
            );
        }
        return "redirect:/products";
    }


    private List<ProductDTO> readDataFromCsv(MultipartFile file, Map<Integer, String> mapProduct) {
        CSVReader csvReader = null;
        try(InputStreamReader reader = new InputStreamReader(file.getInputStream());) {
            List<ProductDTO> productDTOS = new ArrayList<>();
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yy:HH:mm:ss");
            CSVParser csvParser = new CSVParserBuilder().withSeparator(';').build();
            csvReader = new CSVReaderBuilder(reader).withCSVParser(csvParser).withSkipLines(1).build();
            List<String[]> rows = csvReader.readAll();

            for (int i = 0; i < rows.size(); i++) {
                String[] row = rows.get(i);
                String productName = row[0];
                if (row.length > 1 && !mapProduct.containsValue(productName)) {
                    ProductDTO productDTO = new ProductDTO();
                    productDTO.setProductName(row[0]);
                    productDTO.setImage(row[1]);
                    productDTO.setDescription(row[2]);
                    productDTO.setPrice(Double.parseDouble(row[3]));
                    productDTO.setStatus(Integer.parseInt(row[4]));
                    String[] listcategory = row[5].split(",");
                    List<CategoryDTO> categoryDTOS = new ArrayList<>();
                    Arrays.asList(listcategory).forEach(c -> {
                        CategoryDTO categoryDTO = new CategoryDTO();
                        categoryDTO.setCategoryName(c);
                        categoryDTOS.add(categoryDTO);
                    });
                    productDTO.setCategories(categoryDTOS);
                    productDTO.setCreatedAt(dateFormat.parse(row[6]));
                    productDTO.setUpdatedAt(dateFormat.parse(row[7]));
                    productDTOS.add(productDTO);
                } else {
                    return new ArrayList<>();
                }
            }
            return productDTOS;
        } catch (Exception e) {
            LOGGER.error("Can't read data from CSV, detail: {}", e.getMessage());
        } finally {
            try {
                if(csvReader != null)
                    csvReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }


    private List<ProductDTO> readDataFromExcel(MultipartFile file, String extension, Map<Integer, String> mapProduct) {
        Workbook workbook = null;
        try {
            List<ProductDTO> products = new ArrayList<>();
            workbook = ExportUtils.getWorkbook(file, extension);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            rows.next();
            while (rows.hasNext()) {
                Row row = rows.next();
                ProductDTO productDTO = new ProductDTO();
                if (row.getPhysicalNumberOfCells() == 8) {
                    List<CategoryDTO> listCategoryDTO = new ArrayList<>();

                    String productName = row.getCell(0).getStringCellValue().trim();
                    if (!mapProduct.containsValue(productName)) {

                        productDTO.setProductName(productName);
                        productDTO.setImage(row.getCell(1).getStringCellValue());
                        productDTO.setDescription(row.getCell(2).getStringCellValue());
                        productDTO.setPrice(row.getCell(3).getNumericCellValue());
                        productDTO.setStatus((int) row.getCell(4).getNumericCellValue());

                        String[] s1 = row.getCell(5).getStringCellValue().split(",");

                        Arrays.asList(s1).forEach(l -> {
                            CategoryDTO categoryDTO = new CategoryDTO();
                            categoryDTO.setCategoryName(l);
                            listCategoryDTO.add(categoryDTO);
                        });
                        productDTO.setCategories(listCategoryDTO);
                    } else {
                        return new ArrayList<>();
                    }
                }
                products.add(productDTO);
            }
            return products;
        } catch (Exception e) {
            LOGGER.error("Can't read data from Excel, detail: {}", e.getMessage());
        } finally {
            try {
                if(workbook != null) {
                    workbook.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    @GetMapping("products/export/excel")
    public void exportProductDataToExcelFile(HttpServletResponse response) {
        List<ProductDTO> result = findAllProducts();
        String fullPath = this.generateProductExcel(result, context, EXPORT_DATA_REPORT_FILE_NAME);
        if (fullPath != null) {
            this.fileDownload(fullPath, response, EXPORT_DATA_REPORT_FILE_NAME, "xlsx");
        }
    }

    @GetMapping("products/export/report/{type}")
    public void exportProductDataToReportFile(@PathVariable("type") String type, HttpServletResponse response)
            throws JRException {
        List<ProductDTO> result = findAllProducts();
        String fullPath = this.generateProductReport(result, type, context, EXPORT_DATA_REPORT_FILE_NAME);
        if (fullPath != null) {
            this.fileDownload(fullPath, response, EXPORT_DATA_REPORT_FILE_NAME, type);
        }
    }

    private void fileDownload(String fullPath, HttpServletResponse response, String fileName, String type) {
        File file = new File(fullPath);
        if (file.exists()) {
            OutputStream os = null;
            try(FileInputStream fis = new FileInputStream(file);) {
                String mimeType = context.getMimeType(fullPath);
                response.setContentType(mimeType);
                response.setHeader("content-disposition", "attachment; filename=" + fileName + "." + type);
                os = response.getOutputStream();
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead = -1;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                Files.delete(file.toPath());
            } catch (Exception e) {
                LOGGER.error("Can't download file, detail: {}", e.getMessage());
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

    private String generateProductExcel(List<ProductDTO> products, ServletContext context, String fileName) {
        String filePath = context.getRealPath(TEMP_EXPORT_DATA_DIRECTORY);
        File file = new File(filePath);
        if (!file.exists()) {
            new File(filePath).mkdirs();
        }
        try(FileOutputStream fos = new FileOutputStream(file + "\\" + fileName + xlsx);
            XSSFWorkbook workbook = new XSSFWorkbook();) {

            XSSFSheet worksheet = workbook.createSheet("Product");
            worksheet.setDefaultColumnWidth(20);

            XSSFRow headerRow = worksheet.createRow(0);

            XSSFCellStyle headerCellStyle = workbook.createCellStyle();
            XSSFFont font = workbook.createFont();
            font.setFontName(XSSFFont.DEFAULT_FONT_NAME);
            font.setColor(new XSSFColor(java.awt.Color.WHITE));
            headerCellStyle.setFont(font);
            headerCellStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(135, 206, 250)));
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFCell productId = headerRow.createCell(0);
            productId.setCellValue("ID");
            productId.setCellStyle(headerCellStyle);

            XSSFCell productName = headerRow.createCell(1);
            productName.setCellValue("productName");
            productName.setCellStyle(headerCellStyle);

            XSSFCell productImage = headerRow.createCell(2);
            productImage.setCellValue("productImage");
            productImage.setCellStyle(headerCellStyle);

            XSSFCell productDescription = headerRow.createCell(3);
            productDescription.setCellValue("productDescription");
            productDescription.setCellStyle(headerCellStyle);

            XSSFCell price = headerRow.createCell(4);
            price.setCellValue("Price");
            price.setCellStyle(headerCellStyle);

            XSSFCell status = headerRow.createCell(5);
            status.setCellValue("Status");
            status.setCellStyle(headerCellStyle);

            XSSFCell createdAt = headerRow.createCell(7);
            createdAt.setCellValue("Created At");
            createdAt.setCellStyle(headerCellStyle);

            XSSFCell updatedAt = headerRow.createCell(8);
            updatedAt.setCellValue("Updated At");
            updatedAt.setCellStyle(headerCellStyle);

            XSSFCell category = headerRow.createCell(6);
            category.setCellValue("Category");
            category.setCellStyle(headerCellStyle);

            if (!products.isEmpty()) {
                List<CategoryDTO> categoryDTOS;
                for (int i = 0; i < products.size(); i++) {
                    ProductDTO productDTO = products.get(i);
                    categoryDTOS = findCateByProductName(productDTO.getProductName());
                    XSSFRow bodyRow = worksheet.createRow(i + 1);
                    XSSFCellStyle bodyCellStyle = workbook.createCellStyle();
                    bodyCellStyle.setFillForegroundColor(new XSSFColor(java.awt.Color.WHITE));

                    XSSFCell productIDValue = bodyRow.createCell(0);
                    productIDValue.setCellValue(i + 1f);
                    productIDValue.setCellStyle(bodyCellStyle);

                    XSSFCell productNameValue = bodyRow.createCell(1);
                    productNameValue.setCellValue(productDTO.getProductName());
                    productNameValue.setCellStyle(bodyCellStyle);

                    XSSFCell productDescriptionValue = bodyRow.createCell(2);
                    productDescriptionValue.setCellValue(productDTO.getDescription());
                    productDescriptionValue.setCellStyle(bodyCellStyle);

                    XSSFCell productImageValue = bodyRow.createCell(3);
                    productImageValue.setCellValue(productDTO.getImage());
                    productImageValue.setCellStyle(bodyCellStyle);

                    XSSFCell productPriceValue = bodyRow.createCell(4);
                    productPriceValue.setCellValue(productDTO.getPrice());
                    productPriceValue.setCellStyle(bodyCellStyle);

                    XSSFCell productStatusValue = bodyRow.createCell(5);
                    productStatusValue.setCellValue(productDTO.getStatus());
                    productStatusValue.setCellStyle(bodyCellStyle);

                    XSSFCell roleNameValue = bodyRow.createCell(6);
                    roleNameValue.setCellValue(
                            categoryDTOS.stream().map(CategoryDTO::getCategoryName)
                                    .collect(Collectors.joining(",")));
                    roleNameValue.setCellStyle(bodyCellStyle);

                    CreationHelper creationHelper = workbook.getCreationHelper();
                    CellStyle cellStyle = workbook.createCellStyle();
                    cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("dd/MM/yyyy HH:mm:ss"));

                    XSSFCell createdAtValue = bodyRow.createCell(7);
                    createdAtValue.setCellValue(productDTO.getCreatedAt());
                    createdAtValue.setCellStyle(cellStyle);

                    XSSFCell updatedAtValue = bodyRow.createCell(8);
                    updatedAtValue.setCellValue(productDTO.getUpdatedAt());
                    updatedAtValue.setCellStyle(cellStyle);
                }
            }
            workbook.write(fos);
            return file + "\\" + fileName +xlsx ;
        } catch (Exception e) {
            return null;
        }
    }


    private List<CategoryDTO> findCateByProductName(String productName) {
        Map<String, Object> values = new HashMap<>();
        values.put("productName", productName);
        APIResponse<List<CategoryDTO>> result = restService.execute(
                url + "categories/productName/{productName}",
                HttpMethod.GET,
                null,
                null,
                new ParameterizedTypeReference<APIResponse<List<CategoryDTO>>>() {
                },
                values
        );

        return result.getData();
    }

    private String generateProductReport(List<ProductDTO> users, String type, ServletContext context, String fileName)
            throws JRException {
        Connection conn = null;
        String tempFileName = null;
        String filePath = context.getRealPath(TEMP_EXPORT_DATA_DIRECTORY);
        File file = new File(filePath);
        if (!file.exists()) {
            new File(filePath).mkdirs();
        }
        InputStream resourceStream = getClass().getResourceAsStream("/product.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(resourceStream);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(users, false);
        Map<String, Object> values = new HashMap<>();
        values.put("createdBy", "VegetFood");
        try {
            conn = DriverManager.getConnection(datasource, user, password);
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperReport, values, conn);
            if (type.equalsIgnoreCase("pdf")) {
                tempFileName = file + path + fileName + ".pdf";
                JasperExportManager.exportReportToPdfFile(jasperPrint, tempFileName);
            } else if (type.equalsIgnoreCase("xlsx")) {
                tempFileName = file + path + fileName + xlsx;
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
            LOGGER.error("Can't generate file excel, detail: {}", e.getMessage());
        } finally {
            try {
                if(conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return tempFileName;
    }

    @GetMapping("products/{productId}")
    public String detail(@PathVariable("productId") Integer productId, Model model) {
        String authToken = jwtTokenUtil.getJwtTokenFromSecurityContext();
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(authToken);
        ProductDTO product = restService.execute(url + "products/" + productId, HttpMethod.GET, header, null,
                new ParameterizedTypeReference<APIResponse<ProductDTO>>() {
                }).getData();
        if (product != null) {
            model.addAttribute("product", product);
        }
        return "product-detail";
    }

    @PostMapping("products/create")
    public String save(@ModelAttribute ProductFormDTO productRequest) {
        ProductDTO product = new ProductDTO();
        product.setProductName(productRequest.getProductName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setCategoryIds(productRequest.getCategoryIds());

        MultipartFile file = productRequest.getImageFile();
        String imageUrl = productRequest.getImageURL();
        if (file.getSize() != 0 || isImage(imageUrl)) {
            imageUrl = this.getImageUrlFromCloudinary(file, imageUrl);
            product.setImage(imageUrl);
        }

        String authToken = jwtTokenUtil.getJwtTokenFromSecurityContext();
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(authToken);
        restService.execute(
                url + "products",
                HttpMethod.POST,
                header,
                product,
                new ParameterizedTypeReference<APIResponse<ProductDTO>>() {
                },
                new HashMap<String, Object>());

        return "redirect:/products";
    }

    private APIResponse<RestPageImpl<ProductDTO>> getAllProducts(String searchValue, Integer pageNo, Integer pageSize, String sortBy) {
        Map<String, Object> values = new HashMap<>();
        values.put("searchValue", searchValue);
        values.put("pageNo", pageNo);
        values.put("pageSize", pageSize);
        values.put("sortBy", sortBy);
        return restService.execute(
                new StringBuilder(url).append("products")
                        .append("?searchValue={searchValue}&page={pageNo}&size={pageSize}&sortBy={sortBy}").toString(),
                HttpMethod.GET,
                null,
                null,
                new ParameterizedTypeReference<APIResponse<RestPageImpl<ProductDTO>>>() {
                },
                values);
    }

    @GetMapping("products/create")
    public String show(@ModelAttribute("productRequest") ProductFormDTO productRequest, Model model) {
        List<CategoryDTO> listCategory = restService
                .execute(new StringBuilder(url).append("categories/categories").toString(), HttpMethod.GET, null, null,
                        new ParameterizedTypeReference<APIResponse<List<CategoryDTO>>>() {
                        })
                .getData();
        model.addAttribute("listCategory", listCategory);
        model.addAttribute("productRequest", productRequest);
        return "add-product";
    }

    @GetMapping("products/edit/{productId}")
    public String edit(@PathVariable("productId") Integer productId, Model model) {
        ProductDTO productDTO = new ProductDTO();
        APIResponse<ProductDTO> responseData = restService.execute(
                url + "products/" + productId,
                HttpMethod.GET,
                null,
                null,
                new ParameterizedTypeReference<APIResponse<ProductDTO>>() {
                });
        if(responseData.getStatus() == HttpStatus.OK.value()) {
            productDTO = responseData.getData();
        }
        //convert ProductDTO to ProductFormDTO for upload new image
        ProductFormDTO productForm = new ProductFormDTO();
        productForm.setProductId(productDTO.getProductId());
        productForm.setProductName(productDTO.getProductName());
        productForm.setDescription(productDTO.getDescription());
        productForm.setImage(productDTO.getImage());
        productForm.setPrice(productDTO.getPrice());
        productForm.setImageURL(productDTO.getImage());

        List<Integer> selectedCategories = productDTO.getCategories().stream().map(CategoryDTO::getCategoryId)
                .collect(Collectors.toList());

        List<CategoryDTO> listCategory = restService
                .execute(new StringBuilder(url).append("categories/categories").toString(), HttpMethod.GET, null, null,
                        new ParameterizedTypeReference<APIResponse<List<CategoryDTO>>>() {
                        })
                .getData();
        model.addAttribute("listCategory", listCategory);
        model.addAttribute("selectedCategories", selectedCategories);
        model.addAttribute("product", productForm);
        return "edit-product";
    }

    @PostMapping("products/edit/{productId}")
    public String editO(@ModelAttribute("product") ProductFormDTO productRequest,
                        @PathVariable("productId") Integer productId) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductName(productRequest.getProductName());
        productDTO.setDescription(productRequest.getDescription());
        productDTO.setPrice(productRequest.getPrice());
        productDTO.setCategoryIds(productRequest.getCategoryIds());

        MultipartFile file = productRequest.getImageFile();
        String imageUrl = productRequest.getImageURL();
        String imageStoredUrl = productRequest.getImage();
        //if image already exist, delete it immediately
        if (!file.isEmpty()) {
            deleteImageFromCloudinary(productRequest.getImage());
            productRequest.setImage("");
            imageUrl = this.getImageUrlFromCloudinary(file, imageUrl);
            productDTO.setImage(imageUrl);
        } else {
            if (!imageUrl.equalsIgnoreCase(imageStoredUrl) && imageUrl.length() > 0) {
                deleteImageFromCloudinary(productRequest.getImage());
                productRequest.setImage("");
                imageUrl = this.getImageUrlFromCloudinary(file, imageUrl);
                productDTO.setImage(imageUrl);
            } else {
                productDTO.setImage(imageUrl);
            }
        }

        String authToken = jwtTokenUtil.getJwtTokenFromSecurityContext();
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(authToken);
        header.setContentType(MediaType.APPLICATION_JSON);
        restService.execute(url + "products/" + productId, HttpMethod.PUT, header, productDTO,
                new ParameterizedTypeReference<APIResponse<List<ProductDTO>>>() {
                });
        return "redirect:/products";
    }

    @GetMapping("products/delete/{productId}")
    public String delete(@PathVariable("productId") Integer productId, ProductDTO product) {
        String authToken = jwtTokenUtil.getJwtTokenFromSecurityContext();

        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(authToken);
        header.setContentType(MediaType.APPLICATION_JSON);
        restService.execute(url + "products/" + productId, HttpMethod.DELETE, header, product,
                new ParameterizedTypeReference<APIResponse<ProductDTO>>() {
                });
        return "redirect:/products";
    }

    private Cloudinary getCloudinaryClient() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true));
    }

    private Map<String, Object> uploadToCloudinaryByFile(
            Cloudinary cloudinary, MultipartFile sourceFile, String fileName) {
        Map<String, Object> cloudinaryURL = null;
        Map params = ObjectUtils.asMap("public_id",
                CLOUDINARY_DIRECTORY_DEFAULT + fileName.split("\\.", 3)[0]);
        try {
            Map<String, Object> result = cloudinary.uploader()
                    .upload(sourceFile.getBytes(), params);
            cloudinaryURL = result;
        } catch (IOException e) {
            LOGGER.error("Can't not upload image to cloudinary by File, detail: {}", e.getMessage());
        }
        return cloudinaryURL;
    }

    private Map<String, Object> uploadToCloudinaryByUrl(
            Cloudinary cloudinary, String imagePath, String fileName) {
        Map<String, Object> cloudinaryURL = null;
        Map params = ObjectUtils.asMap("public_id",
                CLOUDINARY_DIRECTORY_DEFAULT + fileName.split("\\.", 3)[0]);
        try {
            Map<String, Object> result = cloudinary.uploader().upload(imagePath, params);
            cloudinaryURL = result;
        } catch (IOException e) {
            LOGGER.error("Can't not upload image to cloudinary by Url, detail: {}", e.getMessage());
        }
        return cloudinaryURL;
    }

    private String getImageUrlFromCloudinary(MultipartFile file, String imagePath) {
        Cloudinary cloudinary = getCloudinaryClient();
        String fileName = "";
        String imageUrl = null;
        if (file.getSize() != 0) {
            fileName = file.getOriginalFilename();
            uploadToCloudinaryByFile(cloudinary, file, fileName);
        } else {
            fileName = FilenameUtils.getName(imagePath);
            uploadToCloudinaryByUrl(cloudinary, imagePath, fileName);
        }
        imageUrl = cloudinary.url().format("jpg")
                .transformation(new Transformation().width(450).height(450).crop("fit"))
                .generate(CLOUDINARY_DIRECTORY_DEFAULT + fileName.split("\\.", 3)[0]);
        return imageUrl;
    }

    private void deleteImageFromCloudinary(String imagePath) {
        Cloudinary cloudinary = getCloudinaryClient();
        try {
            cloudinary.uploader().destroy(CLOUDINARY_DIRECTORY_DEFAULT + FilenameUtils.getBaseName(imagePath), null);
        } catch (IOException e) {
            LOGGER.error("Can't not delete image to cloudinary by File, detail: {}", e.getMessage());
        }
    }

    private boolean isImage(String imagePath) {
        try {
            BufferedImage image = ImageIO.read(new URL(imagePath));
            return image != null;
        } catch (MalformedURLException e) {
            LOGGER.error("URL error with image, detail: {}", e.getMessage());
            return false;
        } catch (IOException e) {
            LOGGER.error("IO error with image, detail: {}", e.getMessage());
            return false;
        }
    }

    private List<ProductDTO> findAllProducts() {
        APIResponse<List<ProductDTO>> result = restService.execute(
                new StringBuilder(url).append("products/all").toString(),
                HttpMethod.GET,
                null,
                null,
                new ParameterizedTypeReference<APIResponse<List<ProductDTO>>>() {
                },
                new HashMap<>()
        );
        return result.getData();
    }

}
