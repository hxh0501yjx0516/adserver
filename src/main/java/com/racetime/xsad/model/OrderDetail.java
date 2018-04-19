package com.racetime.xsad.model;

public class OrderDetail extends Paramaters{

	private String order_detail_id = "";
	
	private String order_type = "";

	private String adslot_name = "";
	
	private String city_name = "";
	
	private String channel_name = "";
	
	private String scene_name = "";
	
	private String customer_name = "";
	
	private int estimate_pv;
	
	private int estimate_uv;
	
	private int reality_pv;
	
	private int reality_uv;
	
	private String monitor_url = "";

	private String put_time = "";
	
	public String getOrder_detail_id() {
		return order_detail_id;
	}

	public void setOrder_detail_id(String order_detail_id) {
		this.order_detail_id = order_detail_id;
	}

	public String getAdslot_name() {
		return adslot_name;
	}

	public void setAdslot_name(String adslot_name) {
		this.adslot_name = adslot_name;
	}

	public String getCity_name() {
		return city_name;
	}

	public void setCity_name(String city_name) {
		this.city_name = city_name;
	}

	public String getChannel_name() {
		return channel_name;
	}

	public void setChannel_name(String channel_name) {
		this.channel_name = channel_name;
	}

	public String getScene_name() {
		return scene_name;
	}

	public void setScene_name(String scene_name) {
		this.scene_name = scene_name;
	}

	public String getCustomer_name() {
		return customer_name;
	}

	public void setCustomer_name(String customer_name) {
		this.customer_name = customer_name;
	}

	public int getEstimate_pv() {
		return estimate_pv;
	}

	public void setEstimate_pv(int estimate_pv) {
		this.estimate_pv = estimate_pv;
	}

	public int getEstimate_uv() {
		return estimate_uv;
	}

	public void setEstimate_uv(int estimate_uv) {
		this.estimate_uv = estimate_uv;
	}

	public int getReality_pv() {
		return reality_pv;
	}

	public void setReality_pv(int reality_pv) {
		this.reality_pv = reality_pv;
	}

	public int getReality_uv() {
		return reality_uv;
	}

	public void setReality_uv(int reality_uv) {
		this.reality_uv = reality_uv;
	}

	public String getMonitor_url() {
		return monitor_url;
	}

	public void setMonitor_url(String monitor_url) {
		this.monitor_url = monitor_url;
	}

	public String getPut_time() {
		return put_time;
	}

	public void setPut_time(String put_time) {
		this.put_time = put_time;
	}

	public String getOrder_type() {
		return order_type;
	}

	public void setOrder_type(String order_type) {
		this.order_type = order_type;
	}

}
