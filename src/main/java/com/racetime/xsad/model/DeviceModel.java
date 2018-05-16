package com.racetime.xsad.model;

/**
 * 设备详情 
 */
public class DeviceModel {
	private String adx_devicegroup_id; //渠道设备组ID
	private String ssp_devicegroup_id; //媒体设备组ID
	private String device_id;  		   //设备ID
	private String screen_num; 		   //屏幕数量
	private String scene_id;		  //二级场景
	private String city_code;		 //城市CODE
	private String lat;				 //精度
	private String lon;				 //维度
	private String poi;				 //POI点
	private String address;			 //详细地址
	public String getAdx_devicegroup_id() {
		return adx_devicegroup_id;
	}
	public void setAdx_devicegroup_id(String adx_devicegroup_id) {
		this.adx_devicegroup_id = adx_devicegroup_id;
	}
	public String getSsp_devicegroup_id() {
		return ssp_devicegroup_id;
	}
	public void setSsp_devicegroup_id(String ssp_devicegroup_id) {
		this.ssp_devicegroup_id = ssp_devicegroup_id;
	}
	public String getDevice_id() {
		return device_id;
	}
	public void setDevice_id(String device_id) {
		this.device_id = device_id;
	}
	public String getScreen_num() {
		return screen_num;
	}
	public void setScreen_num(String screen_num) {
		this.screen_num = screen_num;
	}
	public String getScene_id() {
		return scene_id;
	}
	public void setScene_id(String scene_id) {
		this.scene_id = scene_id;
	}
	public String getCity_code() {
		return city_code;
	}
	public void setCity_code(String city_code) {
		this.city_code = city_code;
	}
	public String getLat() {
		return lat;
	}
	public void setLat(String lat) {
		this.lat = lat;
	}
	public String getLon() {
		return lon;
	}
	public void setLon(String lon) {
		this.lon = lon;
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
	
	
	
	

}
