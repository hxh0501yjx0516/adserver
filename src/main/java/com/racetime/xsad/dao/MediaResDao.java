package com.racetime.xsad.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.racetime.xsad.model.AdxAdslot;
import com.racetime.xsad.model.DeviceModel;
import com.racetime.xsad.model.Material;
import com.racetime.xsad.model.PmpAdslotRelation;
import com.racetime.xsad.model.PmpDevice;
import com.racetime.xsad.model.PmpStock;
import com.racetime.xsad.model.SspAdSlot;

public interface MediaResDao {
	public int insertMediaRecord(Map<String,Object> map);
	
	//==============================公共部分==============================
	public List<Map<String,Object>> getCityInfo();
	
	public List<Map<String,Object>> getScene();
	
	public List<Map<String,Object>> getResFormat();
	
	
	//================================RTB===============================
	public List<String> getMediaAdslot(Map<String,Object> map);
	
	public List<String> getMediaAdxAdslot(Map<String,Object> map);
	
	public int insertDevice(List<DeviceModel> list);
	
	public int updateSspAdslot(List<SspAdSlot> list);
	
	public int updateAdxAdslot(AdxAdslot ad);
	
	public String getAdslotRelation(AdxAdslot ad);
	
	public String getAppGroupRelaltion(AdxAdslot ad);
	
	public int insertAdslotReation(AdxAdslot ad);
	
	public int insertAdx_app_devicegroup(AdxAdslot ad);
	
	
	public int getOTTCountbyAdslot(String adx_adslot_id);
	
	//===============================PMP==================================
	
	public int insertPmpDevice(PmpDevice pmp);
	
	public int insertPmpStock(PmpStock list);
	
	public String getPmpResourceIdBySellNum(String sell_num);
	
	public int insertAdxAppGroup(PmpAdslotRelation pmpAdslotRelation);
	
	public int insertPmpAdxAdslotRelation(PmpAdslotRelation pmpAdslotRelation);
	
	public int insertPmpResourceCity(List<Map<String,Object>> map);
	
	public int insertPmpResourceSecene(List<Map<String,Object>> map);
	
	
	//================================material===============================
	
	public int insertMaterial(List<Material> list);
	
	public Map<String,Object> getAdslotInfoById(String adx_adslot_id);
	
	public String getCustomerIdByName(String name);
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
