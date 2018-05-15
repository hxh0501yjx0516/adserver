package com.racetime.xsad.model;

import java.util.Date;

/**
 * pmp 库存数据 
 */
public class PmpStock {
	private int id;
	private String pmp_resource_id;
	private Date mdate;
	private String stock;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getPmp_resource_id() {
		return pmp_resource_id;
	}
	public void setPmp_resource_id(String pmp_resource_id) {
		this.pmp_resource_id = pmp_resource_id;
	}
	public Date getMdate() {
		return mdate;
	}
	public void setMdate(Date mdate) {
		this.mdate = mdate;
	}
	public String getStock() {
		return stock;
	}
	public void setStock(String stock) {
		this.stock = stock;
	}
	
	
		
}
