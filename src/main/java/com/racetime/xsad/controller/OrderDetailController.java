package com.racetime.xsad.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.racetime.xsad.model.Page;
import com.racetime.xsad.model.Paramaters;
import com.racetime.xsad.service.OrderDetailService;

/**
 * 
 * @author skg
 * 
 */
@RestController
@RequestMapping("/orderDetail")
public class OrderDetailController {

	@Autowired
	private OrderDetailService orderDetailService;

	/**
	 * 终端请求广告
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@PostMapping("/getOrderDetail")
	public String getOrderDetail(HttpServletRequest request,
			HttpServletResponse response, Paramaters paramaters) {
		Page page = orderDetailService.getOrderDetails(paramaters);
		
		String json = new Gson().toJson(page);
		response.setContentType("application/json;charset=utf-8");
		try {
			response.getWriter().println(json);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
