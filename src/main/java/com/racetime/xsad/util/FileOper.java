package com.racetime.xsad.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 压缩工具类 
 */
public class FileOper {
	
	
	
	//copy file
	public static void copyFile(File sourceFile,String targetFile) throws IOException{
		File target = new File(targetFile);
		if(target.exists()){
			target.delete();  
		}else{
			target.createNewFile();
		}
	    FileChannel inputChannel = null;    
        FileChannel outputChannel = null;    
	    try {
	        inputChannel = new FileInputStream(sourceFile).getChannel();
	        outputChannel = new FileOutputStream(target).getChannel();
	        outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
	    } finally {
	        inputChannel.close();
	        outputChannel.close();
	    }
	}
	//解压文件加
	 public static void unZipFiles(File zipFile, String descDir) throws IOException {  
		 	ZipFile zip = new ZipFile(zipFile,Charset.forName("GBK"));//解决中文文件夹乱码  
	        String name = zip.getName().substring(zip.getName().lastIndexOf('\\')+1, zip.getName().lastIndexOf('.'));  
	        File pathFile = new File(descDir+name);  
	        if (!pathFile.exists()) {  
	            pathFile.mkdirs();  
	        }  
	        for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements();) {  
	            ZipEntry entry = (ZipEntry) entries.nextElement();  
	            String zipEntryName = entry.getName();  
	            InputStream in = zip.getInputStream(entry);  
	            String outPath = (descDir + name +"/"+ zipEntryName).replaceAll("\\*", "/");  
	              
	            // 判断路径是否存在,不存在则创建文件路径  
	            File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));  
	            if (!file.exists()) {  
	                file.mkdirs();  
	            }  
	            // 判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压  
	            if (new File(outPath).isDirectory()) {  
	                continue;  
	            }  
	            // 输出文件路径信息  
//	          System.out.println(outPath);  
	  
	            FileOutputStream out = new FileOutputStream(outPath);  
	            byte[] buf1 = new byte[1024];  
	            int len;  
	            while ((len = in.read(buf1)) > 0) {  
	                out.write(buf1, 0, len);  
	            }  
	            in.close();  
	            out.close();  
	        }
	        
	        
	        System.out.println("******************解压完毕********************");  
	    }  
	    //测试  
	    public static void main(String[] args) {  
	        try {  
	            unZipFiles(new File("E:/Study/Java.zip"), "E:/Study/abc/");  
	        } catch (IOException e) {  
	            e.printStackTrace();  
	        }  
	    } 
	
	
	    //删处文件夹
	    public static boolean deleteDir(File dir) {
	        if (dir.isDirectory()) {
	            String[] children = dir.list();
	            for (int i=0; i<children.length; i++) {
	                boolean success = deleteDir(new File(dir, children[i]));
	                if (!success) {
	                    return false;
	                }
	            }
	        }
	        // 目录此时为空，可以删除
	        return dir.delete();
	    }
		//获取路径中的Excel
	    public static String getFileDirExcel(String filePath){
	    	File fileDir=new File(filePath);
	    	if (fileDir.isDirectory()) {
	    		String[] children = fileDir.list();
	    		for (int i = 0; i < children.length; i++) {
					if(children[i].endsWith("xlsx")||children[i].endsWith("xls")){
						return children[i];
					}
				}
	    	}
	    	return null;
	    }
	    
	   //判断文件夹下是否包含此文件名称
	   public static boolean validateFile(String filePath,String fileName){
		   File fileDir=new File(filePath);
	    	if (fileDir.isDirectory()) {
	    		String[] children = fileDir.list();
	    		for (int i = 0; i < children.length; i++) {
					if(children[i].equals(fileName)){
						return true;
					}
				}
	    	}
		   return false;
	   }
	   /**
	     * 获取文件长度
	     * @param file
	     */
	    public static long getFileSize(File file) {
	        long fileSize = 0;
	    	if (file.exists() && file.isFile()) {
	            fileSize = file.length()/1024;
	        }
	    	return fileSize;
	    }
	    
	    
	
	
	
}
