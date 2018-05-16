package com.racetime.xsad.util;

import java.io.File;  

import java.io.FileInputStream;  
  
import java.io.IOException;  
  
import java.security.MessageDigest;  


public class MD5FileUtil {
	 static char hexdigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };  
	  
     
	  
	    /** 
	 
	     * @funcion ���ļ�ȫ�����MD5ժҪ  
	 
	     * @param file:Ҫ���ܵ��ļ� 
	 
	     * @return MD5ժҪ�� 
	 
	     */  
	  
	    public static String getMD5(File file) {  
	  
	        FileInputStream fis = null;  
	  
	        try {  
	  
	            MessageDigest md = MessageDigest.getInstance("MD5");  
	  
	            fis = new FileInputStream(file);  
	  
	            byte[] buffer = new byte[2048];  
	  
	            int length = -1;  
	  
	            while ((length = fis.read(buffer)) != -1) {  
	  
	                md.update(buffer, 0, length);  
	  
	            }  
	  
	            byte[] b = md.digest();  
	  
	            return byteToHexString(b);  
	  
	        } catch (Exception e) {  
	  
	            e.printStackTrace();  
	  
	            return null;  
	  
	        } finally {  
	  
	            try {  
	  
	                fis.close();  
	  
	            } catch (IOException e) {  
	  
	                e.printStackTrace();  
	  
	            }  
	  
	        }  
	  
	    }   
	  
	    private static String convertString(String str, Boolean beginUp){  
	        char[] ch = str.toCharArray();  
	        StringBuffer sbf = new StringBuffer();  
	        for(int i=0; i< ch.length; i++){  
	        	sbf.append(charToUpperCase(ch[i]));  
	        }  
	        return sbf.toString();  
	    }  
	      
	    /**ת��д**/  
	    private static char charToUpperCase(char ch){  
	        if(ch <= 122 && ch >= 97){  
	            ch -= 32;  
	        }  
	        return ch;  
	    }  
	  
	    /** 
	 
	     * @function ��byte[]����ת����ʮ������ַ��ʾ��ʽ 
	 
	     * @param tmp  Ҫת����byte[] 
	 
	     * @return ʮ������ַ��ʾ��ʽ 
	 
	     */  
	  
	    private static String byteToHexString(byte[] tmp) {  
	  
	        String s;  
	  
	        // ���ֽڱ�ʾ���� 16 ���ֽ�  
	  
	        // ÿ���ֽ��� 16 ���Ʊ�ʾ�Ļ���ʹ�������ַ����Ա�ʾ�� 16 ������Ҫ 32 ���ַ�  
	  
	        // ����һ���ֽ�Ϊ01011011����ʮ������ַ�����ʾ���ǡ�5b��  
	  
	        char str[] = new char[16 * 2];  
	  
	        int k = 0; // ��ʾת������ж�Ӧ���ַ�λ��  
	  
	        for (int i = 0; i < 16; i++) { // �ӵ�һ���ֽڿ�ʼ���� MD5 ��ÿһ���ֽ�ת���� 16 �����ַ��ת��  
	  
	            byte byte0 = tmp[i]; // ȡ�� i ���ֽ�  
	  
	            str[k++] = hexdigits[byte0 >>> 4 & 0xf]; // ȡ�ֽ��и� 4 λ������ת��, >>> Ϊ�߼����ƣ������λһ������  
	  
	            str[k++] = hexdigits[byte0 & 0xf]; // ȡ�ֽ��е� 4 λ������ת��  
	  
	        }  
	  
	   
	  
	        s = new String(str); // ����Ľ��ת��Ϊ�ַ�  
	  
	        return s;  
	  
	    }  
	  
	   
	  
	    public static void main(String arg[]) {  
	    	//848391aba66777eeb0b0d62e6aace2dd

	        String a = getMD5(new File("d:/device.xlsx"));  
	        System.out.println(a);
	        
	  
	    }  
	
	

}
