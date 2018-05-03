package com.racetime.xsad.timer;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author hu_xuanhua_hua
 * @ClassName: Timer
 * @Description: TODO
 * @date 2018-05-03 12:03
 * @versoin 1.0
 **/
@Component
public class Timer {
    @Scheduled(cron="${goTest}")   //每10秒执行一次
    public  void goTest(){
        System.err.println("怎么回事");
    }
}
