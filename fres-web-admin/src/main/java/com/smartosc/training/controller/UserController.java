package com.smartosc.training.controller;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.smartosc.training.dto.RoleDTO;
import com.smartosc.training.dto.UserDTO;
import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.service.RestService;
import com.smartosc.training.utils.FileUtil;
import com.smartosc.training.utils.JWTUtil;
import com.smartosc.training.utils.RestPageImpl;
import com.smartosc.training.utils.StringHandler;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.awt.Color;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
public class UserController {

    @Autowired
    private RestService restService;

    @Autowired
    private ServletContext context;

    @Autowired
    private JWTUtil jwtTokenUtil;

    @Value("${api.url}")
    private String url;

    @Value("${prefix.user}")
    private String prefixUrl;

    private static final String TEMP_EXPORT_DATA_DIRECTORY = "\\resources\\reports";
    private static final String EXPORT_DATA_REPORT_FILE_NAME = "customer";

    @PostMapping("users")
    public String getListAllUserBySearchValue(
            @RequestParam("table_search") String searchValue) {
        return "redirect:/users?searchValue=" + searchValue;
    }

    @RequestMapping(path = "users", method = {RequestMethod.GET, RequestMethod.POST})
    public String getListAllUserPage(Model model,
                                     @RequestParam(defaultValue = "", required = false) String searchValue,
                                     @RequestParam(defaultValue = "0", required = false) Integer pageNo,
                                     @RequestParam(defaultValue = "5", required = false) Integer pageSize,
                                     @RequestParam(defaultValue = "userId", required = false) String sortBy) {
        APIResponse<RestPageImpl<UserDTO>> responseData = getAllUsers(searchValue, pageNo, pageSize, sortBy);
        RestPageImpl<UserDTO> users = null;
        if (responseData.getStatus() == 200) {
            users = responseData.getData();
        }
        model.addAttribute("data", users);
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("searchValue", searchValue);
        return "customerManagement";
    }

    @PostMapping("user/toggle-block")
    public String changeUserStatus(
            @RequestParam(defaultValue = "", required = false) String searchValue,
            @RequestParam Integer id,
            @RequestParam Integer status) {
        String authToken = jwtTokenUtil.getJwtTokenFromSecurityContext();
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(authToken);
        Map<String, Object> values = new HashMap<>();
        values.put("id", id);
        values.put("status", status);
        restService.execute(
                url + prefixUrl + "/toggle-block?id={id}&status={status}",
                HttpMethod.POST,
                header,
                null,
                new ParameterizedTypeReference<APIResponse<UserDTO>>() {
                },
                values);
        return "redirect:/users?searchValue=" + searchValue;
    }


    private APIResponse<RestPageImpl<UserDTO>> getAllUsers(
            String searchValue, Integer pageNo, Integer pageSize, String sortBy) {
        Map<String, Object> values = new HashMap<>();
        values.put("searchValue", searchValue);
        values.put("pageNo", pageNo);
        values.put("pageSize", pageSize);
        values.put("sortBy", sortBy);
        return restService.execute(
                url + "users" +
                        "?searchValue={searchValue}&page={pageNo}&size={pageSize}&sortBy={sortBy}",
                HttpMethod.GET,
                null,
                null,
                new ParameterizedTypeReference<APIResponse<RestPageImpl<UserDTO>>>() {
                },
                values);
    }

    @PostMapping("users/import")
    public String importUserDataFromFile(Model model, @RequestParam("file") MultipartFile file) {
        boolean isFlag = false;
        List<UserDTO> users;
        List<String> errors = new ArrayList<>();
        if (file.isEmpty()) {
            errors.add("File can't be empty!");
            return "redirect:/users?errorMsg=" + errors.get(0);
        }

        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (extension == null)
            extension = "";
        if (extension.equalsIgnoreCase("xlsx") || extension.equalsIgnoreCase("xls")) {
            users = readDataFromExcel(file, extension, errors);
        } else if (extension.equalsIgnoreCase("csv") || extension.equalsIgnoreCase("txt")) {
            users = readDataFromCsv(file, errors);
        } else {
            errors.add("File extension isn't correct!");
            return "redirect:/users?errorMsg=" + errors.get(0);
        }
        if (!users.isEmpty()) {
            HttpHeaders header = new HttpHeaders();
            header.setBearerAuth(jwtTokenUtil.getJwtTokenFromSecurityContext());
            APIResponse<Boolean> result = restService.execute(
                    url + "users/save_all",
                    HttpMethod.POST,
                    header,
                    users,
                    new ParameterizedTypeReference<APIResponse<Boolean>>() {
                    },
                    new HashMap<>()
            );
            isFlag = result.getData();
        }
        if (!errors.isEmpty()) {
            APIResponse<RestPageImpl<UserDTO>> responseData =
                    getAllUsers("", 0, 5, "userId");
            RestPageImpl<UserDTO> userResult = null;
            if (responseData.getStatus() == 200) {
                userResult = responseData.getData();
            }
            model.addAttribute("errors", errors);
            model.addAttribute("data", userResult);
            model.addAttribute("currentPage", 0);
            model.addAttribute("searchValue", "");
            return "customerManagement";
        }

        if (!isFlag) {
            errors.add("Something went wrong when update data!");
            return "redirect:/users?errorMsg=" + errors.get(0);
        }
        return "redirect:/users";
    }

    private List<UserDTO> readDataFromCsv(MultipartFile file, List<String> errors) {
        try {
            boolean isDataValid;
            List<UserDTO> users = new ArrayList<>();
            List<UserDTO> customers = findAllCustomer();
            InputStreamReader reader = new InputStreamReader(file.getInputStream());
            CSVParser csvParser = new CSVParserBuilder().withSeparator(';').build();
            CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(csvParser).withSkipLines(1).build();
            List<String[]> rows = csvReader.readAll();
            for (int i = 0; i < rows.size(); i++) {
                isDataValid = true;
                String[] row = rows.get(i);
                int rowLine = i + 1;
                if (row.length == 10) {
                    UserDTO user = new UserDTO();

                    //CELL USERNAME VALIDATION
                    if (StringUtils.isBlank(row[1])) {
                        errors.add("Line " + rowLine + ": username - is empty");
                        isDataValid = false;
                    }
                    if (StringHandler.isExist(row[1],
                            customers.stream().map(UserDTO::getUserName).collect(Collectors.toList()))) {
                        errors.add("Line " + rowLine + ": username - already exist");
                        isDataValid = false;
                    }
                    if (StringHandler.isExist(row[1],
                            users.stream().map(UserDTO::getUserName).collect(Collectors.toList()))) {
                        errors.add("Line " + rowLine + ": username - duplicate in file");
                        isDataValid = false;
                    }
                    user.setUserName(row[1]);

                    //CELL EMAIL VALIDATION
                    if (StringUtils.isBlank(row[2])) {
                        errors.add("Line " + rowLine + ": email - is empty");
                        isDataValid = false;
                    }
                    if (!StringHandler.isEmail(row[2])) {
                        errors.add("Line " + rowLine + ": email - not email format");
                        isDataValid = false;
                    }
                    if (StringHandler.isExist(row[2],
                            customers.stream().map(UserDTO::getEmail).collect(Collectors.toList()))) {
                        errors.add("Line " + rowLine + ": email - already exist");
                        isDataValid = false;
                    }
                    if (StringHandler.isExist(row[2],
                            users.stream().map(UserDTO::getEmail).collect(Collectors.toList()))) {
                        errors.add("Line " + rowLine + ": email - duplicate in Excel");
                        isDataValid = false;
                    }
                    user.setEmail(row[2]);

                    //CELL PASSWORD VALIDATION
                    if (StringUtils.isBlank(row[3])) {
                        errors.add("Line " + rowLine + ": password - is empty");
                        isDataValid = false;
                    } else {
                        user.setPassword(row[3]);
                    }

                    //CELL FULL NAME VALIDATION
                    if (StringUtils.isBlank(row[4])) {
                        errors.add("Line " + rowLine + ": fullName - is empty");
                        isDataValid = false;
                    } else {
                        user.setFullName(row[4]);
                    }

                    //CELL ENABLED VALIDATION
                    if (StringUtils.isBlank(row[5])) {
                        errors.add("Line " + rowLine + ": enabled - is empty");
                        isDataValid = false;
                    } else {
                        user.setEnabled(row[5].equalsIgnoreCase("true"));
                    }

                    //CELL STATUS VALIDATION
                    if (StringUtils.isNumeric(row[6].trim())) {
                        user.setStatus(Integer.parseInt(row[6].trim().equals("1") ? row[6].trim() : "0"));
                    } else {
                        errors.add("Line " + rowLine + ": status - Not NUMERIC type");
                        isDataValid = false;
                    }

                    //CELL CREATED AT VALIDATION
                    if (StringUtils.isBlank(row[8])) {
                        errors.add("Line " + rowLine + ": createAt - is empty");
                        isDataValid = false;
                    } else if (!StringHandler.isDateTime(row[8], "dd/MM/yyyy HH:mm:ss")) {
                        errors.add("Line " + rowLine + ": createAt - Not DATE type");
                        isDataValid = false;
                    } else {
                        user.setCreatedAt(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(row[8]));
                    }

                    //CELL UPDATED AT VALIDATION
                    if (StringUtils.isBlank(row[9])) {
                        errors.add("Line " + rowLine + ": updatedAt - is empty");
                        isDataValid = false;
                    } else if (!StringHandler.isDateTime(row[9], "dd/MM/yyyy HH:mm:ss")) {
                        errors.add("Line " + rowLine + ": updatedAt - Not DATE type");
                        isDataValid = false;
                    } else {
                        user.setUpdatedAt(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(row[9]));
                    }

                    if (isDataValid) {
                        users.add(user);
                    }
                } else {
                    errors.add("Line " + rowLine + ": Numbers of data isn't correct!");
                }
            }
            return users;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<UserDTO> readDataFromExcel(MultipartFile file, String extension, List<String> errors) {
        Workbook workbook = null;
        try {
            boolean isDataValid;
            List<UserDTO> users = new ArrayList<>();
            List<UserDTO> customers = findAllCustomer();
            workbook = getWorkbook(file, extension);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            //ignore header
            rows.next();
            while (rows.hasNext()) {
                isDataValid = true;
                Row row = rows.next();
                int rowLine = row.getRowNum() + 1;
                UserDTO user = new UserDTO();
                if (this.isRowValid(row)) {
                    Cell userNameCell = row.getCell(1);
                    Cell emailCell = row.getCell(2);
                    Cell passwordCell = row.getCell(3);
                    Cell fullNameCell = row.getCell(4);
                    Cell enabledCell = row.getCell(5);
                    Cell statusCell = row.getCell(6);
                    Cell createdAtCell = row.getCell(8);
                    Cell updatedAtCell = row.getCell(9);

                    //CELL USERNAME VALIDATION
                    if (userNameCell.getCellTypeEnum() == CellType.STRING
                            || userNameCell.getCellTypeEnum() == CellType.FORMULA) {
                        if (StringUtils.isBlank(userNameCell.getStringCellValue())) {
                            errors.add("Line " + rowLine + ": username - is empty");
                            isDataValid = false;
                        }
                        if (StringHandler.isExist(userNameCell.getStringCellValue(),
                                customers.stream().map(UserDTO::getUserName).collect(Collectors.toList()))) {
                            errors.add("Line " + rowLine + ": username - already exist");
                            isDataValid = false;
                        }
                        if (StringHandler.isExist(userNameCell.getStringCellValue(),
                                users.stream().map(UserDTO::getUserName).collect(Collectors.toList()))) {
                            errors.add("Line " + rowLine + ": username - duplicate in excel");
                            isDataValid = false;
                        }
                        user.setUserName(userNameCell.getStringCellValue());
                    } else {
                        errors.add("Line " + rowLine + ": username - Not STRING type");
                        isDataValid = false;
                    }

                    //CELL EMAIL VALIDATION
                    if (emailCell.getCellTypeEnum() == CellType.STRING
                            || userNameCell.getCellTypeEnum() == CellType.FORMULA) {
                        if (StringUtils.isBlank(emailCell.getStringCellValue())) {
                            errors.add("Line " + rowLine + ": email - is empty");
                            isDataValid = false;
                        }
                        if (!StringHandler.isEmail(emailCell.getStringCellValue())) {
                            errors.add("Line " + rowLine + ": email - not email format");
                            isDataValid = false;
                        }
                        if (StringHandler.isExist(emailCell.getStringCellValue(),
                                customers.stream().map(UserDTO::getEmail).collect(Collectors.toList()))) {
                            errors.add("Line " + rowLine + ": email - already exist");
                            isDataValid = false;
                        }
                        if (StringHandler.isExist(emailCell.getStringCellValue(),
                                users.stream().map(UserDTO::getEmail).collect(Collectors.toList()))) {
                            errors.add("Line " + rowLine + ": email - duplicate in Excel");
                            isDataValid = false;
                        }
                        user.setEmail(emailCell.getStringCellValue());
                    } else {
                        errors.add("Line " + rowLine + ": email - Not STRING type");
                        isDataValid = false;
                    }

                    //CELL PASSWORD VALIDATION
                    if (passwordCell.getCellTypeEnum() == CellType.STRING
                            || userNameCell.getCellTypeEnum() == CellType.FORMULA) {
                        if (StringUtils.isBlank(passwordCell.getStringCellValue())) {
                            errors.add("Line " + rowLine + ": password - is empty");
                            isDataValid = false;
                        } else {
                            user.setPassword(passwordCell.getStringCellValue());
                        }
                    } else {
                        errors.add("Line " + rowLine + ": password - Not STRING type");
                        isDataValid = false;
                    }

                    //CELL FULL NAME VALIDATION
                    if (fullNameCell.getCellTypeEnum() == CellType.STRING
                            || userNameCell.getCellTypeEnum() == CellType.FORMULA) {
                        if (StringUtils.isBlank(fullNameCell.getStringCellValue())) {
                            errors.add("Line " + rowLine + ": fullName - is empty");
                            isDataValid = false;
                        } else {
                            user.setFullName(fullNameCell.getStringCellValue());
                        }
                    } else {
                        errors.add("Line " + rowLine + ": fullName - Not STRING type");
                        isDataValid = false;
                    }

                    //CELL ENABLED VALIDATION
                    if (enabledCell.getCellTypeEnum() == CellType.BOOLEAN) {
                        user.setEnabled(enabledCell.getBooleanCellValue());
                    } else {
                        errors.add("Line " + rowLine + ": enabled - Not BOOLEAN type");
                        isDataValid = false;
                    }

                    //CELL STATUS VALIDATION
                    if (statusCell.getCellTypeEnum() == CellType.NUMERIC) {
                        user.setStatus((int) Math.round(statusCell.getNumericCellValue()));
                    } else {
                        errors.add("Line " + rowLine + ": status - Not NUMERIC type");
                        isDataValid = false;
                    }

                    //CELL CREATED AT VALIDATION
                    if (createdAtCell.getCellTypeEnum() == CellType.NUMERIC) {
                        user.setCreatedAt(createdAtCell.getDateCellValue());
                    } else {
                        errors.add("Line " + rowLine + ": createAt - Not DATE type");
                        isDataValid = false;
                    }

                    //CELL UPDATED AT VALIDATION
                    if (updatedAtCell.getCellTypeEnum() == CellType.NUMERIC) {
                        user.setUpdatedAt(updatedAtCell.getDateCellValue());
                    } else {
                        errors.add("Line " + rowLine + ": updatedAt - Not DATE type");
                        isDataValid = false;
                    }
                } else {
                    errors.add("Line " + (rowLine) + ": not correct list data");
                    isDataValid = false;
                }

                if (isDataValid) {
                    users.add(user);
                }
            }
            return users;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (workbook != null) {
                try {
                    if (workbook != null) {
                        workbook.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
        } finally {
            try {
                if (workbook != null) {
                    workbook.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @GetMapping("users/export/excel")
    public void exportUserDataToExcelFile(HttpServletResponse response) {
        List<UserDTO> result = findAllCustomer();
        String fullPath = this.generateUserExcel(result, context, EXPORT_DATA_REPORT_FILE_NAME);
        if (fullPath != null) {
            FileUtil.fileDownload(fullPath, response, EXPORT_DATA_REPORT_FILE_NAME,
                    "xlsx", context);
        }
    }

    @GetMapping("users/export/report/{type}")
    public void exportUserDataToReportFile(@PathVariable("type") String type, HttpServletResponse response)
            throws JRException {
        String fullPath = this.generateUserReport(type, context, EXPORT_DATA_REPORT_FILE_NAME);
        if (fullPath != null) {
            FileUtil.fileDownload(fullPath, response, EXPORT_DATA_REPORT_FILE_NAME, type, context);
        }
    }

    private String generateUserExcel(List<UserDTO> users, ServletContext context, String fileName) {
        String filePath = context.getRealPath(TEMP_EXPORT_DATA_DIRECTORY);
        File file = new File(filePath);
        if (!file.exists()) {
            new File(filePath).mkdirs();
        }
        try {
            FileOutputStream fos = new FileOutputStream(file + "\\" + fileName + ".xlsx");
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet worksheet = workbook.createSheet("Customer");
            worksheet.setDefaultColumnWidth(20);

            XSSFRow headerRow = worksheet.createRow(0);

            XSSFCellStyle headerCellStyle = workbook.createCellStyle();
            XSSFFont font = workbook.createFont();
            font.setFontName(XSSFFont.DEFAULT_FONT_NAME);
            font.setColor(new XSSFColor(Color.WHITE));
            headerCellStyle.setFont(font);
            headerCellStyle.setFillForegroundColor(new XSSFColor(new Color(135, 206, 250)));
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFCell userId = headerRow.createCell(0);
            userId.setCellValue("ID");
            userId.setCellStyle(headerCellStyle);

            XSSFCell username = headerRow.createCell(1);
            username.setCellValue("Username");
            username.setCellStyle(headerCellStyle);

            XSSFCell email = headerRow.createCell(2);
            email.setCellValue("Email");
            email.setCellStyle(headerCellStyle);

            XSSFCell password = headerRow.createCell(3);
            password.setCellValue("Password");
            password.setCellStyle(headerCellStyle);

            XSSFCell fullName = headerRow.createCell(4);
            fullName.setCellValue("Full Name");
            fullName.setCellStyle(headerCellStyle);

            XSSFCell enabled = headerRow.createCell(5);
            enabled.setCellValue("Enabled");
            enabled.setCellStyle(headerCellStyle);

            XSSFCell status = headerRow.createCell(6);
            status.setCellValue("Status");
            status.setCellStyle(headerCellStyle);

            XSSFCell roleName = headerRow.createCell(7);
            roleName.setCellValue("Role");
            roleName.setCellStyle(headerCellStyle);

            XSSFCell createdAt = headerRow.createCell(8);
            createdAt.setCellValue("Created At");
            createdAt.setCellStyle(headerCellStyle);

            XSSFCell updatedAt = headerRow.createCell(9);
            updatedAt.setCellValue("Updated At");
            updatedAt.setCellStyle(headerCellStyle);

            if (!users.isEmpty()) {
                List<RoleDTO> roles;
                for (int i = 0; i < users.size(); i++) {
                    UserDTO user = users.get(i);
                    roles = findRoleByUsername(user.getUserName());
                    XSSFRow bodyRow = worksheet.createRow(i + 1);
                    XSSFCellStyle bodyCellStyle = workbook.createCellStyle();
                    bodyCellStyle.setFillForegroundColor(new XSSFColor(Color.WHITE));

                    XSSFCell userIdValue = bodyRow.createCell(0);
                    userIdValue.setCellValue(i + 1);
                    userIdValue.setCellStyle(bodyCellStyle);

                    XSSFCell usernameValue = bodyRow.createCell(1);
                    usernameValue.setCellValue(user.getUserName());
                    usernameValue.setCellStyle(bodyCellStyle);

                    XSSFCell emailValue = bodyRow.createCell(2);
                    emailValue.setCellValue(user.getEmail());
                    emailValue.setCellStyle(bodyCellStyle);

                    XSSFCell passwordValue = bodyRow.createCell(3);
                    passwordValue.setCellValue(user.getPassword());
                    passwordValue.setCellStyle(bodyCellStyle);

                    XSSFCell fullNameValue = bodyRow.createCell(4);
                    fullNameValue.setCellValue(user.getFullName());
                    fullNameValue.setCellStyle(bodyCellStyle);

                    XSSFCell enabledValue = bodyRow.createCell(5);
                    enabledValue.setCellValue(user.isEnabled());
                    enabledValue.setCellStyle(bodyCellStyle);

                    XSSFCell statusValue = bodyRow.createCell(6);
                    statusValue.setCellValue(user.getStatus());
                    statusValue.setCellStyle(bodyCellStyle);

                    XSSFCell roleNameValue = bodyRow.createCell(7);
                    roleNameValue.setCellValue(
                            roles.stream().map(RoleDTO::getName)
                                    .collect(Collectors.joining(",")));
                    roleNameValue.setCellStyle(bodyCellStyle);

                    CreationHelper creationHelper = workbook.getCreationHelper();
                    CellStyle cellStyle = workbook.createCellStyle();
                    cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("dd/MM/yyyy HH:mm:ss"));

                    XSSFCell createdAtValue = bodyRow.createCell(8);
                    createdAtValue.setCellValue(user.getCreatedAt());
                    createdAtValue.setCellStyle(cellStyle);

                    XSSFCell updatedAtValue = bodyRow.createCell(9);
                    updatedAtValue.setCellValue(user.getUpdatedAt());
                    updatedAtValue.setCellStyle(cellStyle);
                }
            }
            workbook.write(fos);
            fos.flush();
            fos.close();

            return file + "\\" + fileName + ".xlsx";
        } catch (Exception e) {
            return null;
        }
    }

    private String generateUserReport(String type, ServletContext context, String fileName)
            throws JRException {
        String tempFileName = null;
        List<UserDTO> users = findAllCustomer();
        String filePath = context.getRealPath(TEMP_EXPORT_DATA_DIRECTORY);
        File file = new File(filePath);
        if (!file.exists()) {
            new File(filePath).mkdirs();
        }
        InputStream resourceStream = getClass().getResourceAsStream("/customer.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(resourceStream);
        try {
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(users);
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperReport, null, dataSource);
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
        }
        return tempFileName;
    }

    private List<RoleDTO> findRoleByUsername(String username) {
        Map<String, Object> values = new HashMap<>();
        values.put("username", username);
        APIResponse<List<RoleDTO>> result = restService.execute(
                url + "role/username/{username}",
                HttpMethod.GET,
                null,
                null,
                new ParameterizedTypeReference<APIResponse<List<RoleDTO>>>() {
                },
                values
        );

        return result.getData();
    }

    private List<UserDTO> findAllCustomer() {
        APIResponse<List<UserDTO>> result = restService.execute(
                url + "users/all",
                HttpMethod.GET,
                null,
                null,
                new ParameterizedTypeReference<APIResponse<List<UserDTO>>>() {
                },
                new HashMap<>()
        );
        return result.getData();
    }

    private boolean isRowValid(Row row) {
        int rowCell = 0;
        Cell cell;
        if (row == null) {
            return false;
        } else if (row.getLastCellNum() <= 0) {
            return false;
        }

        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            cell = row.getCell(i);
            if (cell != null && cell.getCellTypeEnum() != CellType.BLANK
                    && StringUtils.isNotBlank(cell.toString())) {
                rowCell++;
            }
        }
        return rowCell == 10;
    }
}
