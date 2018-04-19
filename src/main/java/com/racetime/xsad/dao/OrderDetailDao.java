package com.racetime.xsad.dao;

import java.util.List;

import com.racetime.xsad.model.OrderDetail;
import com.racetime.xsad.model.Paramaters;


public interface OrderDetailDao {

	public List<OrderDetail> getOrderDetail(Paramaters paramaters);
	
	public int getTotalNum(Paramaters paramaters);
}
