package com.racetime.xsad.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.racetime.xsad.constant.FileUploadConstant;
import com.racetime.xsad.dao.MediaResDao;
import com.racetime.xsad.model.AdxAdslot;
import com.racetime.xsad.model.DeviceModel;
import com.racetime.xsad.model.Material;
import com.racetime.xsad.model.PmpAdslotRelation;
import com.racetime.xsad.model.PmpDevice;
import com.racetime.xsad.model.PmpStock;
import com.racetime.xsad.model.SspAdSlot;
import com.racetime.xsad.service.MediaFileValidate;
import com.racetime.xsad.service.MediaResService;
import com.racetime.xsad.util.DateUtil;
import com.racetime.xsad.util.FileOper;
import com.racetime.xsad.util.GetLatAndLngByBaidu;
import com.racetime.xsad.util.MD5Util;
import com.racetime.xsad.util.POIUtil;
import com.racetime.xsad.util.PropertiesUtil;

@Service
public class MediaResServiceImpl implements MediaResService{
	private Logger log = LoggerFactory.getLogger(MediaResServiceImpl.class);
    @Autowired 
	private MediaResDao mediaResDao;
	//添加备注信息
	@Override
	public boolean insertMediaRes(Map<String, Object> map) {
		if(mediaResDao.insertMediaRecord(map)>0){
			return true;
		}
		return false;
	}

	@Override
	@Transactional(rollbackFor=Exception.class)
	public boolean addRTBFile(String filePath) throws IOException {
		try{
			//获取Excel资源数据
			Map<String,List<String[]>> result = POIUtil.readExcel(filePath);
			//添加资源信息
			List<DeviceModel> resDatas = getMediaResource(result.get("0"));
			if(resDatas.size()>0)
			mediaResDao.insertDevice(resDatas);
			//添加媒体广告位信息
			List<SspAdSlot> sspAdslot = getSSPAdSlot(result.get("1"));
			if(sspAdslot.size()>0)
			mediaResDao.updateSspAdslot(sspAdslot);
			//更新渠道信息
			int w = 2;
			while(w<4){
				List<AdxAdslot> adxAdslot = getAdxAdslot(result.get(String.valueOf(w)));
				for (int k = 0; k < adxAdslot.size(); k++) {
					mediaResDao.updateAdxAdslot(adxAdslot.get(k));
					mediaResDao.insertAdslotReation(adxAdslot.get(k));
					mediaResDao.insertAdx_app_devicegroup(adxAdslot.get(k));
				}
				w++;
			}
			return true;	
		}catch (Exception e) {
			 log.error("添加RTB文件报错",e);
			 e.printStackTrace();
			 TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();   
			 return false;
		}
		
	}

	@Override
	@Transactional
	public boolean addPMPFile(String filePath) {
		try {
			Map<String,List<String[]>> result = POIUtil.readExcel(filePath);
			List<PmpDevice> pmpDevice = getPmpDevice(result.get("0"));
		    if(pmpDevice.size()>0){
		    	//Map<String,Object> param = null;
		    	PmpDevice pmpDeviceBean = null;
		    	for (int i = 0; i < pmpDevice.size(); i++) {
		    		pmpDeviceBean = pmpDevice.get(i);
		    		mediaResDao.insertPmpDevice(pmpDeviceBean);
		    		//组装库存数据
		    		PmpStock stock = null;
		    		//List<PmpStock> list = new ArrayList<>();
		    		for (int j = 0; j < pmpDeviceBean.getTimes().size(); j++) {
		    			stock = new PmpStock();
		    			stock.setPmp_resource_id(pmpDeviceBean.getId());
		    			stock.setStock(pmpDeviceBean.getStock());
		    			stock.setMdate( pmpDeviceBean.getTimes().get(j));
		    			//插入库存
		    			mediaResDao.insertPmpStock(stock);
		    		}
		    		//mediaResDao.insertPmpStock(list);
				}
		    	int num = 1;
		    	while(num<3){
		    		//获取PMP广告位对应关系
		    		List<PmpAdslotRelation> adslotRelation = getPmpAdslotRelationInfo(result.get(String.valueOf(num)));
			    	if(adslotRelation.size() >0){
			    		//PmpAdslotRelation pmpAdslotRelation = null;
			    		for (int j = 0; j < adslotRelation.size(); j++) {
			    			//更新APP和上下游关系表
			    			mediaResDao.insertAdxAppGroup(adslotRelation.get(j));
			    			//更新广告位及渠道关系
			    			mediaResDao.insertPmpAdxAdslotRelation(adslotRelation.get(j));
						}
			    	}
			    	num++;
		    	}
		    	return true;
		    }
		} catch (Exception e) {
			e.printStackTrace();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();   
			return false;
		}
		return false;
	}
	//============================添加物料================================
	@Override
	@Transactional
	public boolean addMediaMaterial(String filePath) {
		try {
			//文件目录
			String imageFilePath = new File(filePath).getParent();
			//目标目录
			String targetDir = PropertiesUtil.getValue("file.mediaMaterialRelease", FileUploadConstant.FILEPATHPROPERTIES)+File.separator;
			Map<String,List<String[]>> result = POIUtil.readExcel(filePath);
			List<Material> material = getMaterial(result.get("0"));
			if(material.size()>0)
			mediaResDao.insertMaterial(material);
			//copy file
			for (int i = 0; i < material.size(); i++) {
				//System.out.println(targetDir+File.separator);
				FileOper.copyFile(new File(imageFilePath+File.separator+material.get(i).getName()),targetDir+File.separator+material.get(i).getName());
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return false;
		}
	}
		
	@Override
	public List<DeviceModel> getMediaResource(List<String[]> list) throws Exception{
		//获取城市编码
		List<Map<String,Object>> cityc_code = mediaResDao.getCityInfo();
		//获取场景编码
		List<Map<String,Object>> ad_scene = mediaResDao.getScene();
		//组装设备组对象
		List<DeviceModel> deviceModels = new ArrayList<>();
		DeviceModel deviceModel = null;
		for (int i = 0; i < list.size(); i++) {
			String[] str = list.get(i);
			deviceModel = new DeviceModel();
			deviceModel.setAdx_devicegroup_id(str[0]);
			deviceModel.setSsp_devicegroup_id(str[2]);
			deviceModel.setDevice_id(str[4]);
			deviceModel.setScreen_num(str[5]);
			for (Map<String,Object> map:cityc_code) {
				if(map.get("name").equals(str[6])){
					deviceModel.setCity_code(map.get("code").toString());
				}
			}
			deviceModel.setPoi(str[9]);
			deviceModel.setAddress(str[10]);
			for(Map<String,Object> scene:ad_scene){
				if(scene.get("name").equals(str[12])){
					deviceModel.setScene_id(scene.get("id").toString());
				}
			}
			Object[] o = GetLatAndLngByBaidu.getCoordinate(str[7]+str[10]);
			deviceModel.setLon(o[0].toString());
			deviceModel.setLat(o[1].toString());
			deviceModels.add(deviceModel);
			
		}
		return deviceModels;
	}

	@Override
	public List<SspAdSlot> getSSPAdSlot(List<String[]> list) {
		//错误信息存储
		List<SspAdSlot> ssp_adslot = new ArrayList<>();
		SspAdSlot adslot = null;
		for (int i = 0; i < list.size(); i++) {
			adslot = new SspAdSlot();
			String[] str = list.get(i);
			adslot.setApp_id(str[1]);
			adslot.setMedia_name(str[0]);
			adslot.setSsp_devicegroup_id(str[3]);
			adslot.setName(str[4]);
			adslot.setPrice(Double.parseDouble(str[5]));
			adslot.setSsp_adslot_id(str[3]);
			ssp_adslot.add(adslot);
		}
		return ssp_adslot;
	}

	@Override
	public List<AdxAdslot> getAdxAdslot(List<String[]> list) {
		List<AdxAdslot> adx_adslot = new ArrayList<>();
		AdxAdslot adslot = null;
		for (int i = 0; i < list.size(); i++) {
			adslot = new AdxAdslot();
			String[] str = list.get(i);
			adslot.setAdx_devicegroup_id(str[1]);
			adslot.setAdx_adslot_id(str[2]);
			adslot.setSsp_adslot_id(str[6]);
			adslot.setChannel_name(str[4]);
			adslot.setAdx_adslot_price(Double.parseDouble(str[5]));
			//插入ADX_APPSID_ID
			adslot.setAdx_app_id(str[0]);
			adx_adslot.add(adslot);
		}
		return adx_adslot;
	}
	
	public List<PmpDevice> getPmpDevice(List<String[]> list)throws Exception{
		List<PmpDevice> pmpDevices = new ArrayList<>();
			//获取城市编码
			List<Map<String,Object>> cityc_code = mediaResDao.getCityInfo();
			//获取场景编码
			List<Map<String,Object>> ad_scene = mediaResDao.getScene();
			PmpDevice pmpDevice = null;
			for (int i = 0; i < list.size(); i++) {
				String[] str = list.get(i);
				pmpDevice = new PmpDevice();
				pmpDevice.setSsp_app_id(str[1]);
				pmpDevice.setSsp_adslot_id(str[2]);
				pmpDevice.setId(str[3]);
				pmpDevice.setName(str[4]);
				for (Map<String,Object> map:cityc_code) {
					if(map.get("name").equals(str[6])){
						pmpDevice.setCity_code(map.get("code").toString());
					}
				}
				pmpDevice.setPoi(str[7]);
				pmpDevice.setAddress(str[8]);
				for(Map<String,Object> scene:ad_scene){
					if(scene.get("name").equals(str[10])){
						pmpDevice.setScene_id(scene.get("id").toString());
					}
				}
				pmpDevice.setDevice_num(str[11]);
				//获取开始投放日期和截止日期中所有日期
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
				Date dBegin = sdf.parse(str[12].substring(0,4)+"-"+str[12].substring(4,6)+"-"+str[12].substring(6,8));  
				Date dEnd = sdf.parse(str[13].substring(0,4)+"-"+str[13].substring(4,6)+"-"+str[13].substring(6,8));  
				List<Date> date = DateUtil.findDates(dBegin,dEnd);
				pmpDevice.setTimes(date);
				pmpDevice.setStock(str[14]);
				pmpDevice.setPv(str[15]);
				pmpDevice.setUv(str[16]);
				//pmpDevice.setCpm(Double.parseDouble(str[17]));
				pmpDevice.setCpm(Double.parseDouble(str[17]));
				pmpDevices.add(pmpDevice);
			}
		return pmpDevices;
	}
	//获取PMP广告位配置信息
	public List<PmpAdslotRelation> getPmpAdslotRelationInfo(List<String[]> list) throws Exception{
		List<PmpAdslotRelation> adx_adslot = new ArrayList<>();
			PmpAdslotRelation adslot = null;
			for (int i = 0; i < list.size(); i++) {
				adslot = new PmpAdslotRelation();
				String[] str = list.get(i);
				adslot.setAdx_app_id(str[0]);
				adslot.setAdx_adslot_id(str[1]);
				adslot.setChannel_name(str[3]);
				adslot.setCpm(Double.parseDouble(str[4]));//媒体单价
				//根据售卖单元获取媒体资源主键
				adslot.setPmp_resource_id(str[5]);
				adx_adslot.add(adslot);
			}
		return adx_adslot;
	}
	//组装物料集合
	
	public List<Material> getMaterial(List<String[]> list) throws Exception{
		List<Material> material = new ArrayList<>();
		Material materialBean = null;
		for (int i = 0; i < list.size(); i++) {
			String[] str = list.get(i);
			materialBean = new Material();
			materialBean.setName(str[0]);
			materialBean.setMaterial_url(str[1]);
			materialBean.setTitle(str[2]);
			materialBean.setAdx_adslot_id(str[3]);
			materialBean.setType(str[4]);//物料类型
			if(str[5].equals("L屏广告位")){
				materialBean.setDic_adslot_id("1");
			}else if(str[5].equals("大屏广告位")){
				materialBean.setDic_adslot_id("2");
			}else if(str[5].equals("户外大屏广告位")){
				materialBean.setDic_adslot_id("3");
			}
			//获取资源类型
			List<Map<String,Object>> resformat = mediaResDao.getResFormat();
			for(Map<String,Object> res:resformat){
				if(res.get("value").equals(str[6])){
					materialBean.setDic_resformat_id(res.get("id").toString());
				}
			}
			materialBean.setVideo_duration(str[7]);
			materialBean.setAd_width(str[8]);
			materialBean.setAd_height(str[9]);
			materialBean.setAd_size(str[10]);
			materialBean.setMd5(MD5Util.MD5Encode(str[0]+str[4]));
			materialBean.setRemark(str[11]);
			material.add(materialBean);
		}
		return material;
	}
	public static void main(String[] args) throws ParseException {
		  String string = "20161024";
		  String year = string.substring(0,4);
		  String moth = string.substring(4,6);
		  String date = string.substring(6,8);
		  
		  System.out.println(year+"-"+moth+"-"+date);
		  System.out.println(string.substring(0,4)+"-"+string.substring(4,6)+"-"+string.substring(6,8));
		   SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		   System.out.println(sdf.parse(string));
	}
	
	
	
	

}
