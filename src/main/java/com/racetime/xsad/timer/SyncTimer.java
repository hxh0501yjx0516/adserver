package com.racetime.xsad.timer;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.racetime.xsad.dao.PriceSnapshootDao;


@Component
public class SyncTimer {

	public static Logger logger = LoggerFactory.getLogger(SyncTimer.class);  
	
	@Autowired
	private PriceSnapshootDao priceSnapshootDao;
	
	@SuppressWarnings("unused")
	@Scheduled(cron = "0 5 0 * * ? ")
	private void execute() {
		priceSnapshootDao.execute();
		priceSnapshootDao.pmpExecute();
		logger.info(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+"-->价格快照同步完成！");
	}
}
