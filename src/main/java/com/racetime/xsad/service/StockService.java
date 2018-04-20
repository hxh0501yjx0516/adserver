package com.racetime.xsad.service;

import java.util.Map;

public interface StockService {
	public String getStockInfo(Map<String,Object> param );
	public String getAppStockInfo();
	public int updatePmpResouceStock(String ids,String stock);
	

}
