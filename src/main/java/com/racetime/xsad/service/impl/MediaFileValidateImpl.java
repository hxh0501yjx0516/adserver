package com.racetime.xsad.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.racetime.xsad.dao.MediaResDao;
import com.racetime.xsad.service.MediaFileValidate;
import com.racetime.xsad.util.FileOper;
import com.racetime.xsad.util.GetLatAndLngByBaidu;
import com.racetime.xsad.util.POIUtil;

@Service
public class MediaFileValidateImpl implements MediaFileValidate{
	 private Logger log = LoggerFactory.getLogger(MediaFileValidateImpl.class);
	 @Autowired 
	 private MediaResDao mediaResDao;
	
	//验证RTB资源文件是否正确
	@Override
	public List<String> MediaRTBValidate(String filePath) {
		List<String> errorList = new ArrayList<>();
		List<Map<String,Object>> scenes =  mediaResDao.getScene();
		try {
			Map<String,List<String[]>> datas = new HashMap<>();
			datas = POIUtil.readExcel(filePath);
			//验证媒体资源明细
			for (int i = 0; i < datas.get("0").size(); i++) {
				//验证是否存在设备ID
				String[] str = datas.get("0").get(i);
				if(str[0]==""){
					errorList.add("媒体资源明细中,第"+(i+1)+"行,未获取到渠道设备组ID");
				}
				if(str[2]==""){
					errorList.add("媒体资源明细中,第"+(i+1)+"行,未获取到媒体设备组ID");
				}
				if(str[4]==""){
					errorList.add("媒体资源明细中,第"+(i+1)+"行,未获取到媒体设备ID");
				}
				String cityAddress = str[7]+str[10];
				if(GetLatAndLngByBaidu.getCoordinate(cityAddress)==null){
					errorList.add("媒体资源明细中,第"+(i+1)+"行,未获取到该详细地址中的经纬度,请重新核对详细地址");
				}
				//判断二级场景是否符合规范
				if(str[12] == ""){
					errorList.add("媒体资源明细中,第"+(i+1)+"行,未获取到二级场景");
				}
				if(str[12] != ""){
					if(!validateScene(scenes,str[12])){
						errorList.add("媒体资源明细中,第"+(i+1)+"行,请核实二级场景信息");
					}
				}
				
				
			}
			//验证广告位信息是否正确
			//Map<String,Object> param= null;
			for (int j = 0; j < datas.get("1").size(); j++) {
				String [] adslotStr = datas.get("1").get(j);
				if(adslotStr[1]==""){
					errorList.add("媒体广告位信息中,第"+(j+1)+"行,未获取到媒体资源类型ID");
				}
				if(adslotStr[2]==""){
					errorList.add("媒体广告位信息中,第"+(j+1)+"行,未获取到媒体设备组ID");
				}
				if(adslotStr[3]==""){
					errorList.add("媒体广告位信息中,第"+(j+1)+"行,未获取到媒体广告位ID");
				}
				if(adslotStr[5]==""){
					errorList.add("媒体广告位信息中,第"+(j+1)+"行,未获取媒体CPM底价");
				}
				//如果是APP中类型是OTT类型的广告位如果存在就不能在添加
				
				
				//验证该媒体主下面是否包含该广告位
				/*param = new HashMap<String, Object>();
				param.put("company_name", adslotStr[0].toString());
				param.put("app_id", adslotStr[1].toString());
				param.put("ssp_devicegroup_id", adslotStr[2].toString());
				List<String>  adslots =  mediaResDao.getMediaAdslot(param);
				//根据下游确认
				if(!adslots.contains(adslotStr[3])){
					errorList.add("媒体广告位信息,第"+j+1+"行,广告位和设备组及APPID不匹配");
				}*/
			}
			//验证渠道是否正确(渠道)
			int w = 2;
			while(w<4){
				//Map<String,Object> channelParam = null;
				for (int c = 0; c < datas.get(String.valueOf(w)).size(); c++) {
					String [] adxslotStr = datas.get(String.valueOf(w)).get(c);
					String str = "";
					if(w==2){
						str = "百度渠道中";
					}else{
						str = "平台渠道中";
					}
					if(adxslotStr[0]==""){
						errorList.add(str+",第"+(c+1)+"行,未获取APPSID");
					}
					if(adxslotStr[1]==""){
						errorList.add(str+",第"+(c+1)+"行,未获取到渠道设备组ID");
					}
					if(adxslotStr[2]==""){
						errorList.add(str+",第"+(c+1)+"行,未获取系统广告位ID");
					}
					if(adxslotStr[3]==""){
						errorList.add(str+",第"+(c+1)+"行,未获取渠道广告位ID");
					}
					if(adxslotStr[5]==""){
						errorList.add(str+",第"+(c+1)+"行,未获取渠道CPM底价");
					}
					if(adxslotStr[6]==""){
						errorList.add(str+",第"+(c+1)+"行,未获取媒体广告位ID");
					}
					//如果上游广告位是OTT类型则只能对应一次广告位
					if(adxslotStr[2] != ""){
						if(mediaResDao.getOTTCountbyAdslot(adxslotStr[2])>0){
							errorList.add(str+",第"+(c+1)+"行,该OTT广告位ID已经绑定固定下游广告位ID");
						}
					}
					//验证该媒体主下面是否包含该广告位
					/*channelParam = new HashMap<String, Object>();
					channelParam.put("adx_app_id", adxslotStr[0].toString());
					channelParam.put("adx_devicegroup_id", adxslotStr[1].toString());
					channelParam.put("adx_adslot_id", adxslotStr[2].toString());
					List<String>  adxslots =  mediaResDao.getMediaAdxAdslot(channelParam);
					//根据下游确认
					if(!adxslots.contains(adxslotStr[3])){
						if(w == 2){
							errorList.add("百度渠道,第"+c+1+"行,广告位和设备组及APPID不匹配");
						}else{
							errorList.add("平台渠道,第"+c+1+"行,广告位和设备组及APPID不匹配");
						}
					}*/
				}
				w++;
			}

		} catch (Exception e) {
			log.error("验证失败",e);
			e.printStackTrace();
		}
		return errorList;
	}

	@Override
	public List<String> MediaPMPValidate(String filePath) {
		List<String> errorList = new ArrayList<>();
		try {
			Map<String,List<String[]>> datas = new HashMap<>();
			datas = POIUtil.readExcel(filePath);
			//验证资源库存排期
			for (int i = 0; i < datas.get("0").size(); i++) {
				//验证是否存在设备ID
				String[] str = datas.get("0").get(i);
				if(str[2]==""){
					errorList.add("媒体库存排期,第"+(i+1)+"行,未获取到广告位");
				}if(str[0] == ""){
					errorList.add("媒体库存排期,第"+(i+1)+"行,未获取到媒体主信息");
				}if(str[1] == ""){
					errorList.add("媒体库存排期,第"+(i+1)+"行,未获取到媒体类型APPID");
				}if(str[3]==""){
					errorList.add("媒体库存排期,第"+(i+1)+"行,未获取到售卖单元编号");
				}if(str[4]==""){
					errorList.add("媒体库存排期,第"+(i+1)+"行,未获取到售卖单元");
				}if(str[6]==""){
					errorList.add("媒体库存排期,第"+(i+1)+"行,未获取到城市");
				}if(str[10]==""){
					errorList.add("媒体库存排期,第"+(i+1)+"行,未获取到场景信息");
				}if(str[11]==""){
					errorList.add("媒体库存排期,第"+(i+1)+"行,未获取到设备数");
				}if(str[12]==""){
					errorList.add("媒体库存排期,第"+(i+1)+"行,未获取到开始售卖时间");
				}if(str[13]==""){
					errorList.add("媒体库存排期,第"+(i+1)+"行,未获取到结束售卖时间");
				}if(str[14]==""){
					errorList.add("媒体库存排期,第"+(i+1)+"行,未获取到库存份数");
				}if(str[15]==""){
					errorList.add("媒体库存排期,第"+(i+1)+"行,未获取到展现量");
				}if(str[16]==""){
					errorList.add("媒体库存排期,第"+(i+1)+"行,未获取到处达人群");
				}if(str[17]==""){
					errorList.add("媒体库存排期,第"+(i+1)+"行,未获取到CPM");
				}if(str[18] == ""){
					errorList.add("媒体库存排期,第"+(i+1)+"行,未获取到媒体单价");
				}if(str[3] != ""){
					String regEx1 = "^[a-zA-Z0-9_-]{5,}";
					Pattern p = Pattern.compile(regEx1);
					if(!p.matcher(str[3]).matches()){
						errorList.add("媒体库存排期,第"+(i+1)+"行,售卖单元编号不符合格式规则");
					}
				}
			}
			//验证百度渠道
			int w =1;
			while(w<3){
				String str = "";
				if(w==1){
					str = "百度渠道中";
				}else{
					str = "平台渠道中";
				}
				for (int j = 0; j < datas.get("1").size(); j++) {
					//验证是否存在设备ID
					String[] channelStr = datas.get("1").get(j);
					if(channelStr[0] == ""){
						errorList.add(str+",第"+(j+1)+"行,未获取到APPSID");
					}
					if(channelStr[1] == ""){
						errorList.add(str+",第"+(j+1)+"行,未获取到系统广告位ID");
					}
					if(channelStr[2] == ""){
						errorList.add(str+",第"+(j+1)+"行,未获取到"+str+"广告位ID");
					}
					if(channelStr[3]== ""){
						errorList.add(str+",第"+(j+1)+"行,未获取到"+str+"广告位名称");
					}
					if(channelStr[4] == ""){
						errorList.add(str+",第"+(j+1)+"行,未获取到CPM");
					}
					if(channelStr[5] == ""){
						errorList.add(str+",第"+(j+1)+"行,未获取到渠道媒体单价/份/天");
					}
					if(channelStr[6] == ""){
						errorList.add(str+",第"+(j+1)+"行,未获取售卖单元编号");
					}
				}
				w++;
			}
		} catch (IOException e) {
			e.printStackTrace();
			errorList.add("文件解析错误");
			log.error("验证PMP文件出现错误"+e);
			return errorList;
		}
		return errorList;
	}

	@Override
	public List<String> MediaMediaMaterial(String filePath,String descDir) {
		List<String> errorList = new ArrayList<>();
		try {
			Map<String,List<String[]>> datas = new HashMap<>();
			datas = POIUtil.readExcel(filePath);
			//验证资源库存排期
			for (int i = 0; i < datas.get("0").size(); i++) {
				//验证是否存在设备ID
				String[] str = datas.get("0").get(i);
				if(str[0]==""){
					errorList.add("媒体物料备案中,第"+(i+1)+"行,未获取到物料名称");
				}if(str[3] == ""){
					errorList.add("媒体物料备案中,第"+(i+1)+"行,未获取到渠道广告位ID");
				}if(str[4]==""){
					errorList.add("媒体物料备案中,第"+(i+1)+"行,未获取到物料类型");
				}if(str[5]==""){
					errorList.add("媒体物料备案中,第"+(i+1)+"行,未获取到播放时长");
				}if(str[6]==""){
					errorList.add("媒体物料备案中,第"+(i+1)+"行,未获取到图片宽");
				}if(str[7] == ""){
					errorList.add("媒体物料备案中,第"+(i+1)+"行,未获取到图片高");
				}if(str[8] == ""){
					errorList.add("媒体物料备案中,第"+(i+1)+"行,未获取到物料大小");
				}if(str[10] == ""){
					errorList.add("媒体物料备案中,第"+(i+1)+"行,未获取到客户名称");
				}if(str[0] != ""){
					if(!FileOper.validateFile(descDir, str[0])){
						errorList.add("媒体物料备案中,第"+(i+1)+"行,未在压缩包中获取到该物料");
					}
				}if(str[9] != ""){
					if(str[1]==""){
						errorList.add("媒体物料备案中,第"+(i+1)+"行,未获取到物料URL");
					}
				}
				if(!str[3].equals("")&& !str[0].equals("")&& !str[4].equals("")&&!str[5].equals("")&&!str[6].equals("")
						&&!str[7].equals("")&&!str[8].equals("")&&!str[10].equals("")){
					//获取广告位相关属性信息
					Map<String,Object> adslot = mediaResDao.getAdslotInfoById(str[3]);
					if(adslot != null){
						//long fileSize = FileOper.getFileSize(new File(descDir+str[0]));
						//获取该广告位最大物料大小
						String fileExtension = str[0].substring(str[0].lastIndexOf(".")+1).toUpperCase();
						long adslot_filesize = Long.parseLong(adslot.get("max_size").toString());
						int adslot_width = Integer.parseInt(adslot.get("width").toString());
						int adslot_heigth = Integer.parseInt(adslot.get("height").toString());
						String adslot_type = adslot.get("material_type").toString();
						String adslot_expand = adslot.get("expand").toString();
						if(Integer.parseInt(str[8]) > adslot_filesize){
							errorList.add("媒体物料备案中,第"+(i+1)+"行,上传物料文件大于该广告位支持的最大物料大小");
						}
						if(Integer.parseInt(str[6]) > adslot_width){
							errorList.add("媒体物料备案中,第"+(i+1)+"行,上传物料宽大于广告位支持最大宽");
						}
						if(Integer.parseInt(str[7])> adslot_heigth){
							errorList.add("媒体物料备案中,第"+(i+1)+"行,上传物料高大于广告位支持最大高");
						}
						if(!str[4].equals(adslot_type)){
							errorList.add("媒体物料备案中,第"+(i+1)+"行,该广告位不支持此物料类型");
						}
						if(mediaResDao.getCustomerIdByName(str[10]) == null){
							errorList.add("媒体物料备案中,第"+(i+1)+"行,客户名称未在系统中找到");
						}
						if(!adslot_expand.contains(fileExtension)){
							errorList.add("媒体物料备案中,第"+(i+1)+"行,该物料不符合广告位要求文件格式");
						}
					}else{
						errorList.add("媒体物料备案中,第"+(i+1)+"行,广告位未在系统中找到");
					}
					
					//if(mediaResDao.getFileSizeByAdslot(str[3]))
				}
				//
				
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			errorList.add("文件解析错误");
			log.error("验证上传物料出现错误"+e);
			return errorList;
		}
		return errorList;
		
	}
	
	public boolean validateScene(List<Map<String,Object>> scene,String sceneName){
		for(Map<String,Object> map:scene){
			if(map.get("name").equals(sceneName)){
				return true;
			}
		}
		return false;
		
		
		
	}
	
	
	

}
