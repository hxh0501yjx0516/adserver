package com.racetime.xsad.model;

/**
 * PMP 渠道广告位对应关系
 */
public class PmpAdslotRelation {
	private int id;
	private String adx_app_id;
	private String adx_adslot_id;
	private String channel_name;
	private double cpm;
	private String sell_num;
	private String pmp_resource_id;
	private double sailed_cpm;// 渠道售卖单价
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getAdx_app_id() {
		return adx_app_id;
	}
	public void setAdx_app_id(String adx_app_id) {
		this.adx_app_id = adx_app_id;
	}
	public String getAdx_adslot_id() {
		return adx_adslot_id;
	}
	public void setAdx_adslot_id(String adx_adslot_id) {
		this.adx_adslot_id = adx_adslot_id;
	}
	public String getChannel_name() {
		return channel_name;
	}
	public void setChannel_name(String channel_name) {
		this.channel_name = channel_name;
	}
	public double getCpm() {
		return cpm;
	}
	public void setCpm(double cpm) {
		this.cpm = cpm;
	}
	public String getSell_num() {
		return sell_num;
	}
	public void setSell_num(String sell_num) {
		this.sell_num = sell_num;
	}
	public String getPmp_resource_id() {
		return pmp_resource_id;
	}
	public void setPmp_resource_id(String pmp_resource_id) {
		this.pmp_resource_id = pmp_resource_id;
	}
	public double getSailed_cpm() {
		return sailed_cpm;
	}
	public void setSailed_cpm(double sailed_cpm) {
		this.sailed_cpm = sailed_cpm;
	}
	
	
	
	
}
