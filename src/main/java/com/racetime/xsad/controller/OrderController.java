package com.racetime.xsad.controller;

import com.alibaba.druid.support.json.JSONUtils;
import com.racetime.xsad.service.IOrderService;
import com.sun.org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hu_xuanhua_hua
 * @ClassName: OrderController
 * @Description: 订单业务，查询业务，生成订单
 * @date 2018-04-18 18:11
 * @versoin 1.0
 **/
@RestController
@RequestMapping(value = "/order")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    /**
     * 获取pv,uv,金额
     *
     * @param order_name
     * @return
     */
    @RequestMapping(value = "/getNum",method = { RequestMethod.POST, RequestMethod.GET })
    public String getNum(String order_name) {

        System.err.println("========"+order_name);
//        return order_name;
        return orderService.getNum(order_name);
    }


    /**
     * 生成订单
     *
     * @param order_name
     * @return
     */
    @PostMapping(value = "/generatingOrder")
    public String generatingOrder(String order_name) {
        System.err.println("========"+order_name);

        return orderService.generatingOrder(order_name);
    }
}
