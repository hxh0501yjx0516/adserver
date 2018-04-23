package com.racetime.xsad.dao;

import java.util.List;
import java.util.Map;

public interface StockDaoMapper {
	
	public List<Map<String,Object>> getPmpResource(Map<String,Object> map);
	
	public Map<String,Object> getOrderInfo(String pmp_resource_id);
	
	public List<Map<String,Object>> getPmpResouceStock(Map<String,Object> map);
	
	public List<Map<String,Object>> getAllAppStock();
	
	public Map<String,Object> getOderAppStock(String ad_app_id);
	
	public int updatePmpResouceStock(Map<String,Object> list);
	
	public List<Map<String,Object>> getPmpResouceDatas(Map<String,Object> map);
	

}
