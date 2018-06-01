package com.racetime.xsad.service.impl;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.racetime.xsad.dao.OrderDao;
import com.racetime.xsad.service.IOrderService;
import org.omg.CORBA.OBJ_ADAPTER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

//        System.err.println("==========" + order_name);
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        try {
//            Map<String, Object> map1 = new HashMap<>();
//            map1.put("channel_id", "666");
//            map1.put("app_id", "XAP600002");
//            map1.put("order_name", "444");
//            map1.put("ad_id", "XAD100004");
//            map1.put("customer", "5");
//            map1.put("material", "1,2,3");
//            map1.put("num", "2");
//            map1.put("edate", "2018-12-29");
//            map1.put("sdate", "2018-11-29");
//            List<Map<String, String>> list1 = new ArrayList<>();
//            Map<String, String> gomap = new HashMap<>();
//            gomap.put("XAD100004", "1091,1092,1093");
//            list1.add(gomap);
//            map1.put("detail", list1);
//
//            System.err.println(gson.toJson(map1));
//            order_name = JSON.toJSONString(map1);

            JSONObject jsonObject = JSONObject.parseObject(order_name);
            List<Map> mapList = JSON.parseArray(jsonObject.get("detail").toString(), Map.class);


            Map<String,Object> resultMap =  getPvUvPrice(mapList,jsonObject);
            return gson.toJson(resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> resultmap = new HashMap<>();
            resultmap.put("success", 400);
            return JSONUtils.toJSONString(resultmap);
        }

    }

    /**
     * 寻量
     *
     * @param mapList
     * @param jsonObject
     */
    private Map<String,Object> getPvUvPrice(List<Map> mapList, JSONObject jsonObject) {
        int pv = 0;
        int uv = 0;
        double price = 0;
        double floor_price = 0;
        for (Map map : mapList) {
            Iterator entries = map.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry entry = (Map.Entry) entries.next();
                String key = entry.getKey().toString();
                String value = entry.getValue().toString();
                String[] ids = value.split(",");
                List<String> resourcestockIds = new ArrayList<>();
                for (String id : ids) {
                    resourcestockIds.add(id);
                }
                Map<String, Object> pVandUVandPRICEMap = orderDao.getPVandUVandPRICE(resourcestockIds, jsonObject.getString("num"), key);
                pv = pv + Integer.parseInt(pVandUVandPRICEMap.get("pv").toString());
                uv = uv + Integer.parseInt(pVandUVandPRICEMap.get("uv").toString());
                price = price + Double.parseDouble(pVandUVandPRICEMap.get("price").toString());
                floor_price = floor_price + Double.parseDouble(pVandUVandPRICEMap.get("floor_price").toString());
            }
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("pv", pv);
        resultMap.put("uv", uv);
        resultMap.put("success", 200);
        resultMap.put("price", price);
        resultMap.put("floor_price", floor_price);
        return  resultMap;
    }


    @Override
    public String generatingOrder(String order_name) {
        try {
//            Map<String, Object> map1 = new HashMap<>();
//            map1.put("channel_id", "666");
//            map1.put("app_id", "XAP600002");
//            map1.put("order_name", "444");
//            map1.put("ad_id", "XAD100004");
//            map1.put("customer", "5");
//            map1.put("material", "1,2,3");
//            map1.put("num", "2");
//            map1.put("edate", "2018-12-29");
//            map1.put("sdate", "2018-11-29");
//            map1.put("floor_price", 7199.280000000001);
//            map1.put("price", 10795.68);
//            map1.put("uv", 12000000);
//            map1.put("pv", 6480);
//            List<Map<String, String>> list1 = new ArrayList<>();
//            Map<String, String> gomap = new HashMap<>();
//            gomap.put("XAD100004", "1091,1092,1093");
//            list1.add(gomap);
//            map1.put("detail", list1);
//
//            order_name = JSON.toJSONString(map1);
            JSONObject jsonObject = JSONObject.parseObject(order_name);

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
            List<String> resourcestockIds = new ArrayList<>();
            List<Map> mapList = JSON.parseArray(jsonObject.get("detail").toString(), Map.class);
            for (Map map : mapList) {
                 Iterator entries = map.entrySet().iterator();
                while (entries.hasNext()) {
                    Map.Entry entry = (Map.Entry) entries.next();
                    String key = entry.getKey().toString();
                    String value = entry.getValue().toString();
                    String[] ids = value.split(",");
                    for (String id : ids) {
                        resourcestockIds.add(id);
                     }
                }
            }
            int num = Integer.parseInt(jsonObject.get("num").toString());

            for (int i = 0; i < resourcestockIds.size(); i++) {
                int resourceNum = 0;
                int existingOrderNum = 0;
                if (pmpResourceMap.get(resourcestockIds.get(i)) != null) {
                    resourceNum = Integer.parseInt(pmpResourceMap.get(resourcestockIds.get(i)).toString());
                }
                if (existingOrderMap != null && existingOrderMap.get(resourcestockIds.get(i)) != null) {
                    existingOrderNum = Integer.parseInt(existingOrderMap.get(resourcestockIds.get(i)).toString());
                }
                if (num > resourceNum - existingOrderNum) {
                    Map<String, Object> resultmap = new HashMap<>();
                    resultmap.put("success", 300);
                    return JSONUtils.toJSONString(resultmap);
                }

            }




            /**
             * 生成订单
             */
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            String orderId = simpleDateFormat.format(new Date());
            Map<String, Object> insertOrderMap = new HashMap<>();
            insertOrderMap.put("orderId", orderId);
            insertOrderMap.put("pv", jsonObject.get("pv"));
            insertOrderMap.put("uv", jsonObject.get("uv"));
            insertOrderMap.put("ad_app_id", jsonObject.get("app_id"));
            insertOrderMap.put("ad_position_id", jsonObject.get("ad_id"));
            insertOrderMap.put("order_name", jsonObject.get("order_name"));
            insertOrderMap.put("ad_customer_id", jsonObject.get("customer"));
            insertOrderMap.put("num", jsonObject.get("num"));
            insertOrderMap.put("floor_price", jsonObject.get("floor_price"));
            insertOrderMap.put("money", jsonObject.get("price"));
            insertOrderMap.put("material_id", jsonObject.get("material"));
            insertOrderMap.put("sdate", jsonObject.get("sdate"));
            insertOrderMap.put("edate", jsonObject.get("edate"));
            insertOrderMap.put("ad_channel_id", jsonObject.get("channel_id"));
            orderDao.InsertOrder(insertOrderMap);
            /**
             * 生成详情订单
             */

            List<Map> mapList1 = JSON.parseArray(jsonObject.get("detail").toString(), Map.class);
            for (Map map : mapList1) {
                List<String> listid = new ArrayList<>();
                Iterator entries = map.entrySet().iterator();
                while (entries.hasNext()) {
                    Map.Entry entry = (Map.Entry) entries.next();
                    String key = entry.getKey().toString();
                    String value = entry.getValue().toString();
                    String[] ids = value.split(",");
                    for (String id : ids) {
                        listid.add(id);
                    }
                    List<String> resourceIdList = orderDao.getResourceId(listid);
                    String scene_ids = orderDao.getScene_id(resourceIdList);
                    String city_code = orderDao.getCity_code(resourceIdList);
                    List<Map<String, Object>> resourceList = orderDao.getResource(listid, Integer.parseInt(jsonObject.get("num").toString()),key);
                    orderDao.insertPmp_order_detail(resourceList, orderId,scene_ids,city_code);

                }
            }

            Map<String, Object> resultmap = new HashMap<>();
            resultmap.put("success", 200);
            return JSONUtils.toJSONString(resultmap);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> resultmap = new HashMap<>();
            resultmap.put("success", 400);
            return JSONUtils.toJSONString(resultmap);
        }
    }

    public static void main(String[] args) {
        Map jsonObject = new HashMap();
        jsonObject.put("XAD100004", "1091,1092,1093");

    }
}
