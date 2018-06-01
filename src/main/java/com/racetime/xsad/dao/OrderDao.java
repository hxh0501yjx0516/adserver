package com.racetime.xsad.dao;

import com.racetime.xsad.pojo.Pojo;
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
    Map<String, Object> getChannelAndPrice(@Param("thrid_position_id") String ad_id);

    /**
     * 查询pv,uv
     *
     * @param list
     * @return
     */
    Map<String, Object> getPvUv(@Param("list") List<String> list, @Param("num") int num, @Param("price") int price);

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
    List<Map<String, Object>> getResource(@Param("list") List<String> list, @Param("num") int num, @Param("adx_adslot_id") String adx_adslot_id);

    /**
     * 插入子订单
     */
    void insertPmp_order_detail(@Param("list") List<Map<String, Object>> list, @Param("orderid") String orderId, @Param("scene_ids") String scene_ids, @Param("city_code") String city_code);

    /**
     * 查询pv, uv，price
     *
     * @param list
     * @param num
     * @param adx_adslot_id
     * @return
     */
    Map<String, Object> getPVandUVandPRICE(@Param("list") List<String> list, @Param("num") String num, @Param("adx_adslot_id") String adx_adslot_id);

    /**
     * 查询物料url，所对应的客户信息
     *
     * @return
     */
    List<Map<String, Object>> getCustomer_id();

    void insertOrUpdateAdx_Report(@Param("pojo") Pojo pojo);

    List<Map<String, Object>> selectChannel_id();

    List<String> getResourceId(@Param("list") List<String> list);

    String getScene_id(@Param("list") List<String> list);

    String getCity_code(@Param("list") List<String> list);


}
