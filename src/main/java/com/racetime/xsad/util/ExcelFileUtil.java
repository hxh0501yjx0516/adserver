package com.racetime.xsad.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.racetime.xsad.constant.FileUploadConstant;

public class ExcelFileUtil {
	private static final Logger log = LoggerFactory.getLogger(ExcelFileUtil.class);
	
	/**
     * 根据输入流存入本地
     * @param inputStream fileName
     * @return 文件名称 
     */
    public static boolean uploadLocal(String filePathName,InputStream inputStream, String fileName) {
    	//清理临时文件
    	OutputStream os = null;
        try {
            String path = PropertiesUtil.getValue(filePathName, FileUploadConstant.FILEPATHPROPERTIES);
            byte[] bs = new byte[1024];
            // 读取到的数据长度
            int len;
            // 输出的文件流保存到本地文件
            File tempFile = new File(path);
            if (!tempFile.exists()) {
                tempFile.mkdirs();
            }
            os = new FileOutputStream(tempFile.getPath() + File.separator + fileName);
            // 开始读取
            while ((len = inputStream.read(bs)) != -1) {
                os.write(bs, 0, len);
            }
        }catch (Exception e) {
        	log.error("上传excel文件存储本地失败",e);
            e.printStackTrace();
            return false;
        }finally {
            // 完毕，关闭所有链接
            try {
                os.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
    //验证上传文件是否符合格式
    public static boolean validateFile(MultipartFile file,String type){
    	//判断文件是否为空
        if(file==null) return false;
        //获取文件名
        String name=file.getOriginalFilename();
        //进一步判断文件是否为空（即判断其大小是否为0或其名称是否为null）
        long size=file.getSize();
        if(name==null || ("").equals(name) && size==0) return false;
        if(type.equals("EXCEL")){
        	if(!(WDWUtil.isExcel2003(name) || WDWUtil.isExcel2007(name))){
            	return false;
            }
        }else if(type.equals("ZIP")){
        	if(!name.endsWith(".zip")){
        		return false;
        	}
        }
    	return true;
    }
    
    //写入文件
    public boolean excelWrite(String filePath,List<String> list){
    	HSSFWorkbook wb = new HSSFWorkbook();  
    	HSSFSheet sheet = wb.createSheet("错误信息");  
    	HSSFRow row = sheet.createRow(0); 
    	HSSFCellStyle  style = wb.createCellStyle();
    	HSSFCell cell = row.createCell(0);
    	cell.setCellValue("错误信息列表");
    	cell.setCellStyle(style);
    	for (int i = 0; i < list.size(); i++) {
    		row = sheet.createRow(i+1);
    		row.createCell(0).setCellValue(list.get(i)); 
		}
    	 try {  
             FileOutputStream fout = new FileOutputStream(filePath);  
             wb.write(fout);  
             fout.close();  
         } catch (IOException e) {  
             e.printStackTrace();
             log.error("生成错误信息excel文件存储本地失败",e);
             return false;
         }
         System.out.println("Excel文件生成成功...");  
    	return true;
    }
    
    
    

}
