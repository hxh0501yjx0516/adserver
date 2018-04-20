package com.racetime.xsad.dao;

import com.sun.org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author hu_xuanhua_hua
 * @ClassName: OrderDao
 * @Description: TODO
 * @date 2018-04-18 18:18
 * @versoin 1.0
 **/
public interface OrderDao {
    /**
     * 查询价格和渠道
     *
     * @param ad_id 广告位
     * @return
     */
    Map<String, String> getChannelAndPrice(@Param("ad_posttion_id") String ad_id);

    /**
     * 查询pv,uv
     *
     * @param list
     * @return
     */
    Map<String, Object> getPvUv(@Param("list") List<String> list, @Param("num") int num,@Param("floor_price") int floor_price,@Param("price") int price);

    /**
     * 已有订单
     *
     * @return
     */
    List<Map<String, Object>> getExistingOrder();

    /**
     * 获取所有库存
     *
     * @return
     */
    List<Map<String, Object>> getPmp_Resource();

    /**
     * 插入订单库
     *
     * @param orderMap
     */
    void InsertOrder(@Param("map") Map<String, Object> orderMap);

    /**
     * 查询库存
     */
    List<Map<String, Object>> getResource(@Param("list") List<String> list);

    /**
     * 插入子订单
     */
    void insertPmp_order_detail(@Param("list") List<Map<String, Object>> list, @Param("orderid") String orderId);


}
