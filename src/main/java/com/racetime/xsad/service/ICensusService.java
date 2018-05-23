package com.racetime.xsad.service;

import java.util.List;
import java.util.Map;

/**
 * @author hu_xuanhua_hua
 * @ClassName: ICensusService
 * @Description: TODO
 * @date 2018-05-03 14:17
 * @versoin 1.0
 **/
public interface ICensusService {
    /**
     * 扫描日志，统计日志
     */
    void collectReport();

    void handleReport();

    void getCustomer_id();

    void selectChannel_id();

    /**
     * 投放计数
     */
    public void launcCcount();
}
