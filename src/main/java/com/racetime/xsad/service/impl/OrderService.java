package com.racetime.xsad.service.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.racetime.xsad.dao.OrderDao;
import com.racetime.xsad.service.IOrderService;
import com.sun.corba.se.spi.ior.ObjectKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * @author hu_xuanhua_hua
 * @ClassName: OrderService
 * @Description: TODO
 * @date 2018-04-18 18:17
 * @versoin 1.0
 **/
@Service
public class OrderService implements IOrderService {
    @Autowired
    private OrderDao orderDao;

    @Override
    public String getNum(String order_name) {
//        order_name={
//                "order_name":"test",
//                "app_id":"1",
//                "ad_id":"1",
//                "customer":"1",
//                "num":2,
//                "material":{
//            1,2,3
//        }
//        "edate":"2018-11-29",
//                "sdate":"2018-12-19",
//                "detail":{
//            "2018-11-12":"{'1','2'}",
//                    "2018-11-13":"{'1','2'}",
//                    "2018-11-13":"{'1','2'}",
//                    "2018-11-13":"{'1','2'}",
//                    "2018-11-13":"{'1','2'}"
//        }
//}

        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
//        Map<String, String> map2 = new HashMap<>();
//        map2.put("2018-11-29", "1,2,3");
//        map2.put("2018-12-01", "1,2,3");
//        map2.put("2018-12-02", "1,2,3");
//        map2.put("2018-12-03", "1,2,3");
//        Map<String, Object> map = new HashMap<>();
//        map.put("order_name", "test");
//        map.put("app_id", "1");
//        map.put("ad_id", "1");
//        map.put("customer", "1");
//        map.put("num", "2");
//        map.put("material", "1,2,3");
//        map.put("edate", "2018-12-29");
//        map.put("sdate", "2018-11-29");
//        map.put("detail", map2);
//        System.err.println(gson.toJson(map));
        Map<String, Object> map3 = gson.fromJson(gson.toJson(order_name), Map.class);
        System.err.println(map3.get("detail"));
        Map<String, Object> map4 = (Map<String, Object>) map3.get("detail");
        Map<String, List<String>> getpvuvMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : map4.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().toString();
            String[] strs = value.split(",");
            if (strs.length > 0) {
                if (getpvuvMap != null && getpvuvMap.size() > 0) {
                    for (String str : strs) {
                        if (getpvuvMap.get(str) != null) {
                            getpvuvMap.get(str).add(key);
                        } else {
                            List<String> list = new ArrayList<>();
                            list.add(key);
                            getpvuvMap.put(str, list);
                        }
                    }
                } else {
                    List<String> list = new ArrayList<>();
                    list.add(key);
                    for (String str : strs) {
                        getpvuvMap.put(str, list);
                    }
                }
            }

        }
/**
 * 获取价格
 */
        Map<String, String> channelAndPriceMap = orderDao.getChannelAndPrice(map3.get("ad_id").toString());
        int pv = 0;
        int uv = 0;
        for (Map.Entry<String, List<String>> entry : getpvuvMap.entrySet()) {
            String key = entry.getKey();
            List<String> value = entry.getValue();
            Map<String, Object> pvuvmap = orderDao.getPvUv(value, key);
            pv = Integer.parseInt(pvuvmap.get("pv").toString()) + pv;
            uv = Integer.parseInt(pvuvmap.get("uv").toString()) + uv;

        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("pv", pv);
        resultMap.put("uv", uv);
        resultMap.put("price", channelAndPriceMap.get("price"));
        return gson.toJson(resultMap);
    }

    @Override
    public String generatingOrder(String order_name) {
        return null;
    }

    public static void main(String[] args) {


//        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
//        Map<String, String> map2 = new HashMap<>();
//        map2.put("2018-11-29", "1,2,3");
//        map2.put("2018-12-01", "1,2,3");
//        map2.put("2018-12-02", "1,2,3");
//        map2.put("2018-12-03", "1,2,3");
//        Map<String, Object> map = new HashMap<>();
//        map.put("order_name", "test");
//        map.put("app_id", "1");
//        map.put("ad_id", "1");
//        map.put("customer", "1");
//        map.put("num", "2");
//        map.put("material", "1,2,3");
//        map.put("edate", "2018-12-29");
//        map.put("sdate", "2018-11-29");
//        map.put("detail", map2);
//        System.err.println(gson.toJson(map));
//        Map<String, Object> map3 = gson.fromJson(gson.toJson(map), Map.class);
//        System.err.println(map3.get("detail"));
//        Map<String, Object> map4 = (Map<String, Object>) map3.get("detail");
//        Map<String, List<String>> getpvuvMap = new HashMap<>();
//        for (Map.Entry<String, Object> entry : map4.entrySet()) {
//            String key = entry.getKey();
//            String value = entry.getValue().toString();
//            String[] strs = value.split(",");
//            if (strs.length > 0) {
//                if (getpvuvMap != null && getpvuvMap.size() > 0) {
//                    for (String str : strs) {
//                        if (getpvuvMap.get(str) != null) {
//                            getpvuvMap.get(str).add(key);
//                        } else {
//                            List<String> list = new ArrayList<>();
//                            list.add(key);
//                            getpvuvMap.put(str, list);
//                        }
//                    }
//                } else {
//                    List<String> list = new ArrayList<>();
//                    list.add(key);
//                    for (String str : strs) {
//                        getpvuvMap.put(str, list);
//                    }
//                }
//            }
//
//        }
        System.err.println();
    }
}
