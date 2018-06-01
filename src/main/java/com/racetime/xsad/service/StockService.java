package com.racetime.xsad.service;

import java.util.Map;

import com.racetime.xsad.model.ResponseJson;

public interface StockService {
	public String getStockInfoByAdx(Map<String,Object> param );
	public String getAppStockInfo(Map<String,Object> param );
	public ResponseJson updatePmpResouceStock(String ids,String stock);
	public String getStockInfoSsp(Map<String,Object> param);
	

}
