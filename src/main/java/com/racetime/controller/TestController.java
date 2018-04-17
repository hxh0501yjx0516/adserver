package com.racetime.controller;

import com.alibaba.fastjson.JSON;
import com.racetime.service.ITestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;



/**
 * @author hu_xuanhua_hua
 * @ClassName: TestController
 * @Description: TODO
 * @date 2018-04-17 9:53
 * @versoin 1.0
 **/
@RestController
public class TestController {
    @Autowired
    private ITestService testService;
//    private Logger logger = LoggerFactory.getLogger(TestController.class);

    @GetMapping("/go")
    public String go() {
        return JSON.toJSONString(testService.getMaterial());

    }
}
