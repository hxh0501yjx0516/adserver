package com.racetime.xsad.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
	@Autowired
	private MediaResService mediaResService;
	@Autowired
	private MediaFileValidate mediaFileValidate;

	//上传物料信息zip
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
        System.out.println(file.getSize());
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
        		List<String> errorList = mediaFileValidate.MediaMediaMaterial(zipPath+File.separator+excelName,zipPath);
            	if(errorList.size()>0){
     				json = getResponse(500,errorList,"文件内容不符合格式要求");
     				return json;
            	}else{
            		flag = mediaResService.addMediaMaterial(zipPath+File.separator+excelName);
            	
            	}
        	}
        }
        if(flag){
			json = getResponse(200,"","文件上传成功.");
        }else{
        	json = getResponse(500,"","上传失败.");
        }
        return json;
	
	
	}
	
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
        		List<String>  errorList = mediaFileValidate.MediaRTBValidate(filePath);
    			if(errorList.size()>0){
    				json = getResponse(500,errorList,"文件内容不符合格式要求");
    				return json;
    			}else{
    				flag = mediaResService.addRTBFile(filePath);
    			}
        	}
        	if(flag == true){
				json = getResponse(200,"","scucess");
        	}else{
        		json = getResponse(500,"","上传失败");
        	}
        }catch(Exception e){
        	e.printStackTrace();
        }
		return json;
	}
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
		String name=file.getOriginalFilename();
        try{
        	if(ExcelFileUtil.uploadLocal("file.platform",file.getInputStream(), name)){
        		String filePath = PropertiesUtil.getValue("file.platform", FileUploadConstant.FILEPATHPROPERTIES)+File.separator+name;
        		//判断上传文件是否满足导入条件
        		//导入文件验证是否满足需求
        		 List<String> errorList = mediaFileValidate.MediaPMPValidate(filePath);
	   			 if(errorList.size()>0){
	    				json = getResponse(500,errorList,"文件内容不符合格式要求");
	    				return json;
	   			 }else{
	   				 flag = mediaResService.addPMPFile(filePath);
	   			 }
        	}
        	if(flag == true){
				json = getResponse(200,"","scucess");
        	}else{
        		json = getResponse(500,"","上传失败");
        	}
        }catch(Exception e){
        	e.printStackTrace();
        }
		return json;
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
