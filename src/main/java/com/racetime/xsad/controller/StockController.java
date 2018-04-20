package com.racetime.xsad.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.racetime.xsad.model.ResponseJson;
import com.racetime.xsad.service.StockService;

/**
 * 库存管理接口
 * @author xk
 *
 */
@Controller
@RequestMapping("/stock")
public class StockController {
	@Autowired
	private StockService stockService;
	
	@ResponseBody 
	@RequestMapping("/getStockInfo")
	public String getStockInfo(String app_id,String adslot_id,String city_code,String sdate,String edate,String scene_id){
		Map<String,Object> param = new HashMap<>();
		param.put("ssp_adslot_id", adslot_id);
		param.put("ssp_app_id", app_id);
		if(city_code !=null && !city_code.equals("")){
			param.put("city_code",  Arrays.asList(city_code.split(",")));
		}
		if(scene_id !=null && !scene_id.equals("")){
			param.put("scene_id",  Arrays.asList(scene_id.split(",")));
		}
		param.put("sdate", sdate);
		param.put("edate", edate);
		return stockService.getStockInfo(param);
	}
	/**
	 * 返回所有应用下库存信息
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getAppStock")
	public String getAppStock(){
		//stockService.getAppStockInfo();
		return stockService.getAppStockInfo();
	}
	
	/**
	 * 批量更新库存
	 * @param ids
	 * @param stock
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/updateStock")
	public String updateStock(String ids,String stock){
		ResponseJson json  = new ResponseJson();
		
		if(ids == null || ids.equals("")||stock == null || stock.equals("")){
			json.setCode(400);
			json.setMsg("");
			json.setData("");
			return new Gson().toJson(json);
		}
		json = stockService.updatePmpResouceStock(ids, stock);
		return new Gson().toJson(json) ;
		
	}
	
	
	
	
	
	
}
