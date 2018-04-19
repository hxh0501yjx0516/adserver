package com.racetime.xsad.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.racetime.xsad.dao.OrderDetailDao;
import com.racetime.xsad.model.OrderDetail;
import com.racetime.xsad.model.Page;
import com.racetime.xsad.model.Paramaters;
import com.racetime.xsad.service.OrderDetailService;

@Service
@Transactional
public class OrderDetailServiceImpl implements OrderDetailService{

	@Autowired(required=true)
	private OrderDetailDao orderDetailDao;
	
	@Override
	public Page getOrderDetails(Paramaters paramaters) {
		if(paramaters.getPage() != null && paramaters.getPageSize() !=null)
			paramaters.setStartSize((paramaters.getPage()-1)*paramaters.getPageSize());
		List<OrderDetail> orderDetails = orderDetailDao.getOrderDetail(paramaters);
		int totalNum = orderDetailDao.getTotalNum(paramaters);
		
		Page page = new Page();
		page.setList(orderDetails);
		page.setTotalNum(totalNum);
		if(paramaters.getPage() != null)
			page.setPage(paramaters.getPage());
		Integer pageSize = paramaters.getPageSize();
		if(paramaters.getPageSize() != null){
			page.setPageSize(pageSize);
			page.setTotalPage(totalNum / pageSize + ((totalNum % pageSize == 0) ? 0 : 1));
		}
		return page;
	}

}
