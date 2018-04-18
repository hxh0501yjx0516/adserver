package com.racetime.xsad.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.racetime.xsad.service.TestService;

/**
 * 
* 项目名称：busonline-gd   
* 类名称：GdController   
* 类描述：   gd广告排期控制类
* 创建人：skg 
* 创建时间：2017-10-24 下午1:38:31   
* @version    
*
 */
@RestController
@RequestMapping("/test")
public class TestController {
	

	@Autowired
	private TestService testService;
	
	/**
	 * 终端请求广告
	 * @param request
	 * @param response
	 * @return
	 */
	@GetMapping("/hello")
	public String getGd(HttpServletRequest request, HttpServletResponse response) {
		System.out.println("TestController");
		testService.testService();
		return null;
	}

}
