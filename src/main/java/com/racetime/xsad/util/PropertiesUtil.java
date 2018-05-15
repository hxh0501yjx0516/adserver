package com.racetime.xsad.util;
import java.io.FileInputStream;  
import java.io.FileNotFoundException;  
import java.io.IOException;  
import java.io.InputStreamReader;  
import java.util.Properties;


/**
 * properties工具类
 */
public class PropertiesUtil {
	
	 public static String getValue(String keyName,String fileName){  
	        String value = "";  
	        Properties p = new Properties();  
	        try{
	            p.load(new InputStreamReader(new FileInputStream(PropertiesUtil.class.getResource("/"+fileName).getPath()),"UTF-8"));  
	            value = p.getProperty(keyName,"获取失败");  
	        } catch (FileNotFoundException e) {  
	            e.printStackTrace();  
	        } catch (IOException e) {  
	            e.printStackTrace();  
	        }  
	        return value;  
	    }

}
