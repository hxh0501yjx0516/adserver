package com.racetime.xsad.service;

import java.util.List;
import java.util.Map;

/**
 * @author hu_xuanhua_hua
 * @ClassName: IOrderService
 * @Description: TODO
 * @date 2018-04-18 18:17
 * @versoin 1.0
 **/
public interface IOrderService {

    String getNum(String order_name);

    String generatingOrder(String order_name);
}
