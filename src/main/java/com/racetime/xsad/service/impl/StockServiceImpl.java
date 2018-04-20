package com.racetime.xsad.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.racetime.xsad.dao.StockDaoMapper;
import com.racetime.xsad.model.PmpResource;
import com.racetime.xsad.service.StockService;

/**
 *库存管理业务类
 */
@Service
public class StockServiceImpl implements StockService{
	@Autowired
	private StockDaoMapper stockDaoMapper;
	
	@Override
	public String getStockInfo(Map<String, Object> param) {
		List<Map<String,Object>> json = new ArrayList<>();
		List<Map<String,Object>> deviceInfo = stockDaoMapper.getPmpResource(param);
		if(deviceInfo != null){
			for (int i = 0; i < deviceInfo.size(); i++) {
				Map<String,Object> value = new HashMap<>();
				value.put("app_id", deviceInfo.get(i).get("ssp_app_id"));
				value.put("adslot_id", deviceInfo.get(i).get("ssp_adslot_id"));
				value.put("scene_id", deviceInfo.get(i).get("scene_id"));
				value.put("name", deviceInfo.get(i).get("name"));
				value.put("city_code", deviceInfo.get(i).get("city_code"));
				//获取该资源组在订单中的时间段
				Map<String,Object> orderInfo = stockDaoMapper.getOrderInfo(deviceInfo.get(i).get("id").toString());
				//获取该该资源组下的库存和投放时间
				List<Map<String,Object>> stockInfo = stockDaoMapper.getPmpResouceStock(deviceInfo.get(i).get("id").toString());
				Map<String,Object> mdate = new TreeMap<String,Object>();
				//组装mdate
				for (int j = 0; j < stockInfo.size(); j++) {
					PmpResource source = new PmpResource();
					source.setId(Integer.parseInt(stockInfo.get(j).get("id").toString()));
					source.setTotal(Integer.parseInt(stockInfo.get(j).get("stock").toString()));
					//验证是否有投放量
					if(orderInfo != null){
						long datetime = Long.valueOf(stockInfo.get(i).get("mdate").toString().replaceAll("-",""));
						long end_time = Long.valueOf(orderInfo.get("end_time").toString().replaceAll("-",""));
						long start_time = Long.valueOf(orderInfo.get("start_time").toString().replaceAll("-",""));
						if(datetime>= start_time && datetime<=end_time){
							source.setUsed(Integer.parseInt(orderInfo.get("stock").toString()));
						}else{
							source.setUsed(0);
						}
					}else{
						source.setUsed(0);
					}
					mdate.put(stockInfo.get(j).get("mdate").toString(),source);
				}
				value.put("mdate", mdate);
				json.add(value);
			}
		}else{
			return "0";
		}
		System.out.println(new Gson().toJson(json));
		return new Gson().toJson(json);
	}

	@Override
	public String getAppStockInfo() {
		List<Map<String,Object>> json = new ArrayList<>();
		List<Map<String,Object>> app_info = stockDaoMapper.getAllAppStock();
		if(app_info.size()>0){
			for (int i = 0; i < app_info.size(); i++) {
				//Map<String,Object> app_key = new HashMap<>();
				Map<String,Object> appInfo = new HashMap<>();
				appInfo.put("total", app_info.get(i).get("total"));
				appInfo.put("app_id",app_info.get(i).get("app_id"));
				int remain = 0;
				Map<String,Object> orderAppStock = stockDaoMapper.getOderAppStock(app_info.get(i).get("app_id").toString());
				if(orderAppStock != null){
					if(orderAppStock.get("sdnum") == null){
						appInfo.put("lock", 0);
					}else{
						appInfo.put("lock", orderAppStock.get("sdnum"));
						remain += Integer.parseInt(orderAppStock.get("sdnum").toString());
					}
					if(orderAppStock.get("ydnum") == null){
						appInfo.put("reserve", 0);
					}else{
						appInfo.put("reserve", orderAppStock.get("ydnum"));
						remain += Integer.parseInt(orderAppStock.get("ydnum").toString());
					}
					appInfo.put("remain",Integer.parseInt(app_info.get(i).get("total").toString())-remain);
				}else{
					appInfo.put("lock", 0);
					appInfo.put("reserve", 0);
					appInfo.put("app_id", 0);
					appInfo.put("remain", 0);
				}
				json.add(appInfo);
			}
			
		}else{
			return "0";
		}
		System.out.println(new Gson().toJson(json));
		return new Gson().toJson(json);
	}

	@Override
	@Transactional
	public int updatePmpResouceStock(String ids, String stock) {
		List<String> paramIds = new ArrayList<>();
		Map<String,Object> param = new HashMap<>();
		List<String> pmpResourceIds = Arrays.asList(ids.split("-"));
		param.put("list",pmpResourceIds);
		param.put("stock", stock);
		List<Map<String,Object>> resouceDateInfo = stockDaoMapper.getPmpResouceDatas(param);
		for (int i = 0; i < resouceDateInfo.size(); i++) {
			//获取该资源组ID和日期
			Map<String,Object> orderInfo = stockDaoMapper.getOrderInfo(resouceDateInfo.get(i).get("pmp_resource_id").toString());
			if(orderInfo != null){
				//该资源组投放总使用数
				int total = Integer.parseInt(orderInfo.get("stock").toString());
				long datetime = Long.valueOf(resouceDateInfo.get(i).get("mdate").toString().replaceAll("-",""));
				//该资源组投放日期
				long end_time = Long.valueOf(orderInfo.get("end_time").toString().replaceAll("-",""));
				long start_time = Long.valueOf(orderInfo.get("start_time").toString().replaceAll("-",""));
				if(datetime>= start_time && datetime<=end_time && Integer.parseInt(stock) > total){
					paramIds.add(resouceDateInfo.get(i).get("pmp_resource_stock_id").toString());
				}
			}else{
				paramIds.add(resouceDateInfo.get(i).get("pmp_resource_stock_id").toString());
			}
		}
		if(paramIds.size()>0){
			Map<String,Object> updateParam = new HashMap<>();
			updateParam.put("list", paramIds);
			updateParam.put("stock", stock);
			return stockDaoMapper.updatePmpResouceStock(updateParam);
		}
		//System.out.println(k);
		return 0;
	}

}
