package com.racetime.xsad.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;  
import java.io.IOException;  
import java.io.InputStream;  
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;  
import org.apache.poi.ss.usermodel.Cell;  
import org.apache.poi.ss.usermodel.Row;  
import org.apache.poi.ss.usermodel.Sheet;  
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;  

public class POIUtil {
	 private static Logger logger  = LoggerFactory.getLogger(POIUtil.class);
	
	 //判断文件类型是否EXCEL
	 public static void checkFile(MultipartFile file) throws IOException{  
	        //判断文件是否存在  
	        if(null == file){  
	            logger.error("文件不存在！");  
	            throw new FileNotFoundException("文件不存在！");  
	        }  
	        //获得文件名  
	        String fileName = file.getOriginalFilename();  
	        //判断文件是否是excel文件  
	        if(!fileName.endsWith("xls") && !fileName.endsWith("xlsx")){  
	            logger.error(fileName + "不是excel文件");  
	            throw new IOException(fileName + "不是excel文件");  
	        }  
	 }  
	 public static List<String[]> readExcel(String filePath,int sheetNum) throws IOException{  
	        //获得Workbook工作薄对象  
	        Workbook workbook = null;
	        InputStream is = new FileInputStream(filePath);
			//读取Excel
			if(WDWUtil.isExcel2003(filePath)){
				workbook = new HSSFWorkbook(is);
			}else{
				workbook = new XSSFWorkbook(is);
			}
	        //创建返回对象，把每行中的值作为一个数组，所有行作为一个集合返回  
	        List<String[]> list = new ArrayList<String[]>();  
	        if(workbook != null){  
                //获得当前sheet工作表  
                Sheet sheet = workbook.getSheetAt(sheetNum);  
                //获得当前sheet的开始行  
                int firstRowNum  = sheet.getFirstRowNum();  
                //获得当前sheet的结束行  
                int lastRowNum = sheet.getLastRowNum();  
                //循环除了第一行的所有行  
                for(int rowNum = firstRowNum+1;rowNum <= lastRowNum;rowNum++){  
                    //获得当前行  
                    Row row = sheet.getRow(rowNum);  
                    if(row == null){  
                        continue;  
                    }  
                    //获得当前行的开始列  
                    int firstCellNum = row.getFirstCellNum();  
                    //获得当前行的列数  
                    int lastCellNum = row.getPhysicalNumberOfCells();  
                    String[] cells = new String[row.getPhysicalNumberOfCells()];  
                    //循环当前行  
                    for(int cellNum = firstCellNum; cellNum < lastCellNum;cellNum++){  
                        Cell cell = row.getCell(cellNum);  
                        //cells[cellNum] = getCellValue(cell);  
                        //判断是否是合并单元格
	                     if(isMergedRegion(sheet,rowNum,cellNum)){
	                    	 cells[cellNum] = getMergedRegionValue(sheet,rowNum,cellNum);
	                     }else{
	                    	 cells[cellNum] = getCellValue(cell);
	                     }
                    }  
                    list.add(cells);  
                }
	           
	        }
	        if(is != null)
	        is.close();
	        return list;  
	    }  
	 public static Map<String,List<String[]>> readExcel(String filePath) throws IOException{  
	        
		 Map<String,List<String[]>> result = new HashMap<String, List<String[]>>();
		 //获得Workbook工作薄对象  
	        Workbook workbook = null;
	        InputStream is = new FileInputStream(filePath);
			//读取Excel
			if(WDWUtil.isExcel2003(filePath)){
				workbook = new HSSFWorkbook(is);
			}else{
				workbook = new XSSFWorkbook(is);
			}
	        //创建返回对象，把每行中的值作为一个数组，所有行作为一个集合返回  
	        List<String[]> list = null;  
	        if(workbook != null){  
	         for (int k = 0; k < workbook.getNumberOfSheets(); k++) {
	        	 list = new ArrayList<String[]>();
	        	 //获得当前sheet工作表  
	             Sheet sheet = workbook.getSheetAt(k);  
	             //获得当前sheet的开始行  
	             int firstRowNum  = sheet.getFirstRowNum();  
	             //获得当前sheet的结束行  
	             int lastRowNum = sheet.getLastRowNum();  
	             //循环除了第一行的所有行  
	             for(int rowNum = firstRowNum+1;rowNum <= lastRowNum;rowNum++){  
	                 //获得当前行  
	                 Row row = sheet.getRow(rowNum);  
	                 if(row == null){  
	                     continue;  
	                 }  
	                 //获得当前行的开始列  
	                 int firstCellNum = row.getFirstCellNum();  
	                 //获得当前行的列数  
	                 //int lastCellNum = row.getPhysicalNumberOfCells();  
	                 //String[] cells = new String[row.getPhysicalNumberOfCells()]; 
	                 int lastCellNum = row.getLastCellNum();  
	                 String[] cells = new String[row.getLastCellNum()]; 
	                 //循环当前行  
	                 for(int cellNum = firstCellNum; cellNum < lastCellNum;cellNum++){  
	                     Cell cell = row.getCell(cellNum); 
	                     //判断是否是合并单元格
	                     if(isMergedRegion(sheet,rowNum,cellNum)){
	                    	 cells[cellNum] = getMergedRegionValue(sheet,rowNum,cellNum);
	                     }else{
	                    	 cells[cellNum] = getCellValue(cell);
	                     }
	                       
	                 }  
	                 list.add(cells);  
	             }
	             result.put(String.valueOf(k), list);
		        }
			}
	        if(is != null)
	        is.close();
	        return result;  
	    }
	 
	 /** 
	     * 判断指定的单元格是否是合并单元格 
	     *  
	     * @param sheet 
	     *            工作表 
	     * @param row 
	     *            行下标 
	     * @param column 
	     *            列下标 
	     * @return 
	     */  
	    private static boolean isMergedRegion(Sheet sheet, int row, int column) {  
	        int sheetMergeCount = sheet.getNumMergedRegions();  
	        for (int i = 0; i < sheetMergeCount; i++) {  
	            CellRangeAddress range = sheet.getMergedRegion(i);  
	            int firstColumn = range.getFirstColumn();  
	            int lastColumn = range.getLastColumn();  
	            int firstRow = range.getFirstRow();  
	            int lastRow = range.getLastRow();  
	            if (row >= firstRow && row <= lastRow) {  
	                if (column >= firstColumn && column <= lastColumn) {  
	                    return true;  
	                }  
	            }  
	        }  
	        return false;  
	    }
	    
	    /**
	     * 返回合并单元格得值
	     * @param sheet
	     * @param row
	     * @param column
	     * @return
	     */
	    private static String getMergedRegionValue(Sheet sheet, int row, int column) {  
	        int sheetMergeCount = sheet.getNumMergedRegions();  
	        for (int i = 0; i < sheetMergeCount; i++){  
	            CellRangeAddress ca = sheet.getMergedRegion(i);  
	            int firstColumn = ca.getFirstColumn();  
	            int lastColumn = ca.getLastColumn();  
	            int firstRow = ca.getFirstRow();  
	            int lastRow = ca.getLastRow();  
	            if (row >= firstRow && row <= lastRow) {  
	  
	                if (column >= firstColumn && column <= lastColumn) {  
	                    Row fRow = sheet.getRow(firstRow);  
	                    Cell fCell = fRow.getCell(firstColumn);  
	                    return getCellValue(fCell);  
	                }  
	            }  
	        }  
	        return "";  
	    }  
    public static String getCellValue(Cell cell){  
        String cellValue = "";  
        if(cell == null){  
            return cellValue;  
        }  
        //把数字当成String来读，避免出现1读成1.0的情况  
        if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC){  
            cell.setCellType(Cell.CELL_TYPE_STRING);  
        }  
        //判断数据的类型  
        switch (cell.getCellType()){  
            case Cell.CELL_TYPE_NUMERIC: //数字  
                cellValue = String.valueOf(cell.getNumericCellValue());  
                break;  
            case Cell.CELL_TYPE_STRING: //字符串  
                cellValue = String.valueOf(cell.getStringCellValue());  
                break;  
            case Cell.CELL_TYPE_BOOLEAN: //Boolean  
                cellValue = String.valueOf(cell.getBooleanCellValue());  
                break;  
            case Cell.CELL_TYPE_FORMULA: //公式  
                cellValue = String.valueOf(cell.getCellFormula());  
                break;  
            case Cell.CELL_TYPE_BLANK: //空值   
                cellValue = "";  
                break;  
            case Cell.CELL_TYPE_ERROR: //故障  
                cellValue = "非法字符";  
                break;  
            default:  
                cellValue = "未知类型";  
                break;  
        }  
        return cellValue;  
    }
    public static String getExtensionName(String filename) {   
        if ((filename != null) && (filename.length() > 0)) {   
            int dot = filename.lastIndexOf('.');   
            if ((dot >-1) && (dot < (filename.length() - 1))) {   
                return filename.substring(dot);   
            }   
        }   
        return filename;   
    } 
    
    
    public static void main(String[] args) throws IOException {
    	/*List<String[]> list = POIUtil.readExcel("D:\\平台模板-PMP资源备案.xlsx", 4);
    	for (int i = 0; i < list.size() ; i++) {
			String [] str = list.get(i);
			for (int j = 0; j < str.length; j++) {
				System.out.println(str[j]);
			}
		}*/
    	System.out.println(POIUtil.getExtensionName("D:\\平台模板-PMP资源备案.xlsx"));
    
    
    }
}
