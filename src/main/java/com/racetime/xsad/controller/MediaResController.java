package com.racetime.xsad.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.racetime.xsad.constant.FileUploadConstant;
import com.racetime.xsad.model.ResponseJson;
import com.racetime.xsad.service.MediaFileValidate;
import com.racetime.xsad.service.MediaResService;
import com.racetime.xsad.util.ExcelFileUtil;
import com.racetime.xsad.util.FileOper;
import com.racetime.xsad.util.MD5FileUtil;
import com.racetime.xsad.util.PropertiesUtil;

/**
 * 媒体资源管理
 * @author xk
 *
 */
@RequestMapping("/mediaRes")
@RestController
public class MediaResController {
	private Logger log = LoggerFactory.getLogger(MediaResController.class);
	@Autowired
	private MediaResService mediaResService;
	@Autowired
	private MediaFileValidate mediaFileValidate;

	//上传物料信息zip
	@SuppressWarnings("finally")
	@ResponseBody
	@RequestMapping(value="/material", method = RequestMethod.POST)
	public ResponseJson uploadMediaMaterialRes(@RequestParam(value="file") MultipartFile file,
            HttpServletRequest request,HttpServletResponse response) throws IOException{
		boolean flag = false;
		ResponseJson json = new ResponseJson();
		//判断文件是否为空
		if(!ExcelFileUtil.validateFile(file,"ZIP")){
			json = getResponse(400,"","文件上传格式不匹配.");
			return json;
		}
		String name=file.getOriginalFilename();
        //System.out.println(file.getSize());
		try{
			if(ExcelFileUtil.uploadLocal("file.mediaMaterialzip",file.getInputStream(), name)){
	        	//压缩包位置
	        	String filePath = PropertiesUtil.getValue("file.mediaMaterialzip", FileUploadConstant.FILEPATHPROPERTIES)+File.separator+name;
	        	//解压目录
	        	String descDir = PropertiesUtil.getValue("file.mediaMaterialunzip", FileUploadConstant.FILEPATHPROPERTIES)+File.separator;
	        	//清空解压路径
	        	FileOper.deleteDir(new File(descDir+File.separator));
	        	//解压到固定文件路径
	        	FileOper.unZipFiles(new File(filePath), descDir);
	        	//获取压缩文件中的Excel路径
	        	//压缩文件
	        	String zipPath = descDir+name.substring(name.lastIndexOf('\\')+1, name.lastIndexOf('.'));
	        	String excelName = FileOper.getFileDirExcel(zipPath);
	        	if(excelName !=null){
	        		//验证模板
	        		if(!ExcelFileUtil.fileValidate(zipPath+File.separator+excelName, "material")){
	        			json = getResponse(500,"","文件上传文件模板不正确");
	        			return json;
	        		}
	        		List<String> errorList = mediaFileValidate.MediaMediaMaterial(zipPath+File.separator+excelName,zipPath);
	            	if(errorList.size()>0){
	     				json = getResponse(500,errorList,"文件内容不符合格式要求");
	     				return json;
	            	}else{
	            		flag = mediaResService.addMediaMaterial(zipPath+File.separator+excelName);
	            	}
	        	}
	        }
		}catch(Exception e){
			log.error("物料提交出现错误", e);
		}finally{
			if(flag){
				json = getResponse(200,"","文件上传成功.");
	        }else{
	        	json = getResponse(500,"","上传失败.");
	        }
	        return json;
		}
	
	}
	
	@SuppressWarnings("finally")
	@ResponseBody
	@RequestMapping(value="/rtb", method = RequestMethod.POST)
	public ResponseJson addRTBMediaRes(@RequestParam(value="file") MultipartFile file,
            HttpServletRequest request,HttpServletResponse response){
		ResponseJson json = new ResponseJson();
		boolean flag = false;
		//判断文件是否为空
		if(!ExcelFileUtil.validateFile(file,"EXCEL")){
			json = getResponse(400,"","文件上传格式不匹配.");
			return json;
		}
		String name=file.getOriginalFilename();
        try{
        	if(ExcelFileUtil.uploadLocal("file.platform",file.getInputStream(), name)){
        		String filePath = PropertiesUtil.getValue("file.platform", FileUploadConstant.FILEPATHPROPERTIES)+File.separator+name;
        		if(!ExcelFileUtil.fileValidate(filePath, "rtb")){
        			json = getResponse(500,"","文件上传文件模板不正确");
        			return json;
        		}
        		List<String>  errorList = mediaFileValidate.MediaRTBValidate(filePath);
    			if(errorList.size()>0){
    				json = getResponse(500,errorList,"文件内容不符合格式要求");
    				return json;
    			}else{
    				flag = mediaResService.addRTBFile(filePath);
    			}
        	}
        }catch(Exception e){
        	e.printStackTrace();
        	log.error("上传RTB资源报错", e);
        	
        }finally {
        	if(flag == true){
    			json = getResponse(200,"","scucess");
        	}else{
        		json = getResponse(500,"","上传失败");
        	}
    		return json;
		}
        
	}
	@SuppressWarnings("finally")
	@ResponseBody
	@RequestMapping(value="/pmp", method = RequestMethod.POST)
	public ResponseJson addPMPMediaRes(@RequestParam(value="file") MultipartFile file,
            HttpServletRequest request,HttpServletResponse response){
		ResponseJson json = new ResponseJson();
		boolean flag = false;
		//判断文件是否为空
		if(!ExcelFileUtil.validateFile(file,"EXCEL")){
			json = getResponse(400,"","文件上传格式不匹配.");
			return json;
		}
		//创建文件名称
		Calendar cal = Calendar.getInstance();
		Integer year = cal.get(cal.YEAR);
		Integer moth = cal.get(cal.MONTH)+1;
		Integer day = cal.get(cal.DAY_OF_MONTH);
		String name= year+""+moth+""+day;
		//String name=file.getOriginalFilename();
        try{
        	if(ExcelFileUtil.uploadLocal("file.platform",file.getInputStream(), name)){
        		String filePath = PropertiesUtil.getValue("file.platform", FileUploadConstant.FILEPATHPROPERTIES)+File.separator+name;
        		if(!ExcelFileUtil.fileValidate(filePath, "pmp")){
        			json = getResponse(500,"","文件上传文件模板不正确");
        			return json;
        		}
        		//导入文件验证是否满足需求
        		 List<String> errorList = mediaFileValidate.MediaPMPValidate(filePath);
	   			 if(errorList.size()>0){
	    				json = getResponse(500,errorList,"文件内容不符合格式要求");
	    				return json;
	   			 }else{
	   				 flag = mediaResService.addPMPFile(filePath);
	   			 }
        	}
        	
        }catch(Exception e){
        	e.printStackTrace();
        	log.error("PMP导入出错", e);
        }finally {
        	if(flag == true){
    			json = getResponse(200,"","scucess");
        	}else{
        		json = getResponse(500,"","上传失败");
        	}
    		return json;
		}
        
	}
	
	//组装返回错误信息
	public ResponseJson getResponse(int code,Object data,String msg){
		ResponseJson json = new ResponseJson();
		json.setCode(code);
		json.setData(data);
		json.setMsg(msg);
		return json;
	}
	
	
	
	
}
