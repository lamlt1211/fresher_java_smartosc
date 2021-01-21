package com.smartosc.training.utils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

public class ExportUtils {

    public static void fileDownload(String fullPath, HttpServletResponse response,
                                    String fileName, ServletContext context) {
        File file = new File(fullPath);
        FileInputStream fis = null;
        if (file.exists()) {
            try {
                fis = new FileInputStream(file);
                String mimeType = context.getMimeType(fullPath);
                response.setContentType(mimeType);
                response.setHeader("content-disposition", "attachment; filename=" + fileName);
                OutputStream os = response.getOutputStream();
                byte[] buffer = new byte[4096];
                int bytesRead = -1;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                fis.close();
                os.close();
                file.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public static void fileDownload(String fullPath, HttpServletResponse response,
                                    String fileName, ServletContext context,String type) {
        File file = new File(fullPath);
        FileInputStream fis = null;
        if (file.exists()) {
            try {
                fis = new FileInputStream(file);
                String mimeType = context.getMimeType(fullPath);
                response.setContentType(mimeType);
                response.setHeader("content-disposition", "attachment; filename=" + fileName+"."+type);
                OutputStream os = response.getOutputStream();
                byte[] buffer = new byte[4096];
                int bytesRead = -1;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                fis.close();
                os.close();
                file.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Workbook  getWorkbook(MultipartFile file, String extension) {
        Workbook workbook = null;
        try {
            if(extension.equalsIgnoreCase("xls")) {
                workbook = new HSSFWorkbook(file.getInputStream());
            } else if(extension.equalsIgnoreCase("xlsx")) {
                workbook = new XSSFWorkbook(file.getInputStream());
            }
            return workbook;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
