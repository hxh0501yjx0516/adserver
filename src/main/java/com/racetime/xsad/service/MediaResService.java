package com.racetime.xsad.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.racetime.xsad.model.AdxAdslot;
import com.racetime.xsad.model.DeviceModel;
import com.racetime.xsad.model.SspAdSlot;

public interface MediaResService {
	
	public boolean insertMediaRes(Map<String,Object> map);
	//验证平台RTB文件
	public boolean addRTBFile(String filePath)throws IOException;
	public List<DeviceModel> getMediaResource(List<String[]> list)throws Exception;
	public List<SspAdSlot> getSSPAdSlot(List<String[]> list);
	public List<AdxAdslot> getAdxAdslot(List<String[]> list);
	
	//====================PMP=========================
	public boolean addPMPFile(String filePath);
	//====================物料==========================
	
	public boolean addMediaMaterial(String filePath);



}
