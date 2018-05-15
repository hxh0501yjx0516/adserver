package com.racetime.xsad.timer;

import com.racetime.xsad.service.ICensusService;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private ICensusService censusService;

    @Scheduled(cron = "${collectReport}")
        //每10秒执行一次
    void collectReport() {
        System.err.println("怎么回事");
        censusService.collectReport();
    }

    @Scheduled(cron = "${handleReport}")
    void handleReport() {
        censusService.handleReport();
    }

    @Scheduled(fixedRate = 1000 * 60 * 60 * 12)
    void getgetCustomer_id() {
        censusService.getCustomer_id();
    }

    @Scheduled(cron = "${launcCcount}")
    void launcCcount() {
        censusService.launcCcount();
    }
}
