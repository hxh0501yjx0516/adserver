package com.racetime.xsad.service.impl;

import com.alibaba.druid.support.json.JSONUtils;
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
import java.text.SimpleDateFormat;
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
//        Map<String, Object> map = new HashMap<>();
//        map.put("order_name", "test");
//        map.put("app_id", "1");
//        map.put("ad_id", "1");
//        map.put("customer", "1");
//        map.put("num", "2");
//        map.put("material", "1,2,3");
//        map.put("edate", "2018-12-29");
//        map.put("sdate", "2018-11-29");
//        map.put("detail", "1,2");
//        System.err.println(gson.toJson(map));
        Map<String, Object> orderMap = gson.fromJson(order_name, Map.class);
//        System.err.println(orderMap.get("detail"));
/**
 * 获取价格
 */
        Map<String, String> channelAndPriceMap = orderDao.getChannelAndPrice(orderMap.get("ad_id").toString());

        String detail = orderMap.get("detail").toString();
        String[] details = detail.split(",");
        List<String> detaillist = new ArrayList<>();
        for (String str : details) {
            detaillist.add(str);
        }
        Map<String, Object> pvuvmap = orderDao.getPvUv(detaillist);
        int pv = Integer.parseInt(pvuvmap.get("pv").toString());
        int uv = Integer.parseInt(pvuvmap.get("uv").toString());


        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("pv", pv);
        resultMap.put("uv", uv);
        resultMap.put("price", channelAndPriceMap.get("price"));
        return gson.toJson(resultMap);

    }

    @Override
    public String generatingOrder(String order_name) {
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        Map<String, Object> map = new HashMap<>();
        map.put("order_name", "test");
        map.put("app_id", "1");
        map.put("ad_id", "1");
        map.put("customer", "1");
        map.put("num", "40");
        map.put("material", "1,2,3");
        map.put("edate", "2018-12-29");
        map.put("sdate", "2018-11-29");
        map.put("detail", "1,2");
//        System.err.println(gson.toJson(map));
        Map<String, Object> orderMap = gson.fromJson(order_name, Map.class);
//        System.err.println(orderMap.get("detail"));
//        Map<String, Object> detailMap = (Map<String, Object>) orderMap.get("detail");
        /**
         * 要入库的订单
         */
//        Map<String, Object> dateOrderMap = new HashMap<>();
//        for (Map.Entry<String, Object> entry : detailMap.entrySet()) {
//            String key = entry.getKey();
//            String value = entry.getValue().toString();
//            String[] strs = value.split(",");
//            if (strs.length > 0) {
//                for (String str : strs) {
//                    if (dateOrderMap.get(str) != null) {
//                        dateOrderMap.put(key + "," + str, orderMap.get("num"));
//                    }
//
//                }
//            }
//        }
        /**
         * 库存订单
         */
        List<Map<String, Object>> pmpResourceList = orderDao.getPmp_Resource();
        Map<String, Object> pmpResourceMap = new HashMap<>();
        int pmpResourceListSize = pmpResourceList.size();
        for (int i = 0; i < pmpResourceListSize; i++) {
            pmpResourceMap.put(pmpResourceList.get(i).get("id").toString(), pmpResourceList.get(i).get("num"));
        }

        /**
         * 已有订单
         */
        List<Map<String, Object>> existingOrderList = orderDao.getExistingOrder();
        Map<String, Object> existingOrderMap = new HashMap<>();
        if (existingOrderList != null || existingOrderList.size() > 0) {
            int existingOrderListSize = existingOrderList.size();
            for (int i = 0; i < existingOrderListSize; i++) {
                existingOrderMap.put(existingOrderList.get(i).get("id").toString(), existingOrderList.get(i).get("num"));
            }
        }

        /**
         * 判断库存是否是否满足生成新订单
         */
        String detail = orderMap.get("detail").toString();
        String[] details = detail.split(",");
        List<String> detaillist = new ArrayList<>();
        for (String str : details) {
            detaillist.add(str);
        }
        int num = Integer.parseInt(orderMap.get("num").toString());
        for (int i = 0; i < detaillist.size(); i++) {
            int resourceNum = 0;
            int existingOrderNum = 0;
            if (pmpResourceMap.get(detaillist.get(i)) != null) {
                resourceNum =  Integer.parseInt(pmpResourceMap.get(detaillist.get(i)).toString());
            }
            if (existingOrderMap != null && existingOrderMap.get(detaillist.get(i)) != null) {
                existingOrderNum = Integer.parseInt(existingOrderMap.get(detaillist.get(i)).toString());
            }
            if (num > resourceNum - existingOrderNum) {
                Map<String, String> resultmap = new HashMap<>();
                resultmap.put("success", "false");
                return JSONUtils.toJSONString(resultmap);
            }

    }

    /**
     * 生成订单
     */
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    String orderId = simpleDateFormat.format(new Date());
    Map<String, Object> insertOrderMap = new HashMap<>();
        insertOrderMap.put("orderId",orderId);
        insertOrderMap.put("pv",orderMap.get("pv"));
        insertOrderMap.put("uv",orderMap.get("uv"));
        insertOrderMap.put("ad_app_id",orderMap.get("app_id"));
        insertOrderMap.put("ad_position_id",orderMap.get("ad_id"));
        insertOrderMap.put("order_name",orderMap.get("order_name"));
        insertOrderMap.put("ad_customer_id",orderMap.get("customer"));
        insertOrderMap.put("num",orderMap.get("num"));
        insertOrderMap.put("money",orderMap.get("money"));
        insertOrderMap.put("material_id",orderMap.get("material"));
        insertOrderMap.put("sdate",orderMap.get("sdate"));
        insertOrderMap.put("edate",orderMap.get("edate"));
        insertOrderMap.put("ad_channel_id",1);
        insertOrderMap.put("city_code",orderMap.get("city_code"));
        orderDao.InsertOrder(insertOrderMap);
    /**
     * 生成详情订单
     */
    String resourceStockId = orderMap.get("detail").toString();
    String[] resourceStockIds = resourceStockId.split(",");
    List<String> resourceStockList = new ArrayList<>();
        for(
    String str :resourceStockIds)

    {
        resourceStockList.add(str);
    }

    List<Map<String, Object>> resourceList = orderDao.getResource(resourceStockList);
        orderDao.insertPmp_order_detail(resourceList,orderId);

        Map<String, String> resultmap = new HashMap<>();
        resultmap.put("success", "true");
        return JSONUtils.toJSONString(resultmap);
}

    //    public static void main(String[] args) {
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
//        Map<String, Object> orderMap = gson.fromJson(gson.toJson(map), Map.class);
//        System.err.println(orderMap.get("detail"));
//        Map<String, Object> detailMap = (Map<String, Object>) orderMap.get("detail");
//        Map<String, List<String>> getpvuvMap = new HashMap<>();
//        for (Map.Entry<String, Object> entry : detailMap.entrySet()) {
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
//
//                    for (String str : strs) {
//                        List<String> list = new ArrayList<>();
//                        list.add(key);
//                        getpvuvMap.put(str, list);
//                    }
//                }
//            }
//
//        }
//        System.err.println();
//    }
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
//        Map<String, Object> orderMap = gson.fromJson(gson.toJson(map), Map.class);
//        System.err.println(orderMap.get("detail"));
//        Map<String, Object> detailMap = (Map<String, Object>) orderMap.get("detail");
//        Map<String, Object> dateOrderMap = new HashMap<>();
//        for (Map.Entry<String, Object> entry : detailMap.entrySet()) {
//            String key = entry.getKey();
//            String value = entry.getValue().toString();
//            String[] strs = value.split(",");
//            if (strs.length > 0) {
//                for (String str : strs) {
//                    if (dateOrderMap.get(str) != null) {
//                        dateOrderMap.put(key + "," + str, orderMap.get("num"));
//                    }
//
//                }
//                System.err.println();
//            }
//        }
//
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
//        String orderId = simpleDateFormat.format(new Date());
//        Map<String, Object> InsertOrderMap = new HashMap<>();
//        InsertOrderMap.put("orderId", orderId);
//        InsertOrderMap.put("pv", orderMap.get("pv"));
//        InsertOrderMap.put("uv", orderMap.get("uv"));
//        InsertOrderMap.put("ad_app_id", orderMap.get("app_id"));
//        InsertOrderMap.put("ad_position_id", orderMap.get("ad_id"));
//        InsertOrderMap.put("order_name", orderMap.get("order_name"));
//        InsertOrderMap.put("ad_customer_id", orderMap.get("customer"));
//        InsertOrderMap.put("num", orderMap.get("num"));
//        InsertOrderMap.put("money", orderMap.get("money"));
//        InsertOrderMap.put("material_id", orderMap.get("material"));
//        InsertOrderMap.put("sdate", orderMap.get("sdate"));
//        InsertOrderMap.put("sdate", orderMap.get("sdate"));
//        InsertOrderMap.put("ad_channel_id", orderMap.get("uv"));


    }
}
