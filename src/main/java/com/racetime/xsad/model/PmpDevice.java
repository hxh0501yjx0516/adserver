package com.racetime.xsad.model;

import java.util.Date;
import java.util.List;

/**
 * pmp 售卖单元 
 */
public class PmpDevice {
	private String id;
	private String media_name;
	private String ssp_app_id;
	private String ssp_adslot_id;
	private String sell_num;
	private String name;
	private String city_code;
	private String poi;
	private String address;
	private String scene_id;
	private String device_num;
	private List<Date> times;
	private String stock;
	private String uv;
	private String pv;
	private double cpm;
	private double price;
	private int count;
	
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getMedia_name() {
		return media_name;
	}
	public void setMedia_name(String media_name) {
		this.media_name = media_name;
	}
	public String getSsp_app_id() {
		return ssp_app_id;
	}
	public void setSsp_app_id(String ssp_app_id) {
		this.ssp_app_id = ssp_app_id;
	}
	public String getSsp_adslot_id() {
		return ssp_adslot_id;
	}
	public void setSsp_adslot_id(String ssp_adslot_id) {
		this.ssp_adslot_id = ssp_adslot_id;
	}
	public String getSell_num() {
		return sell_num;
	}
	public void setSell_num(String sell_num) {
		this.sell_num = sell_num;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCity_code() {
		return city_code;
	}
	public void setCity_code(String city_code) {
		this.city_code = city_code;
	}
	public String getPoi() {
		return poi;
	}
	public void setPoi(String poi) {
		this.poi = poi;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getScene_id() {
		return scene_id;
	}
	public void setScene_id(String scene_id) {
		this.scene_id = scene_id;
	}
	public String getDevice_num() {
		return device_num;
	}
	public void setDevice_num(String device_num) {
		this.device_num = device_num;
	}
	public String getStock() {
		return stock;
	}
	public void setStock(String stock) {
		this.stock = stock;
	}
	public String getUv() {
		return uv;
	}
	public void setUv(String uv) {
		this.uv = uv;
	}
	public String getPv() {
		return pv;
	}
	public void setPv(String pv) {
		this.pv = pv;
	}
	public double getCpm() {
		return cpm;
	}
	public void setCpm(double cpm) {
		this.cpm = cpm;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public List<Date> getTimes() {
		return times;
	}
	public void setTimes(List<Date> times) {
		this.times = times;
	}
	
	
	
	
	
	
	
	
	
	
}
