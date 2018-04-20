package com.racetime.xsad.dao;

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
     * @param date
     * @param resourceId
     * @return
     */
    Map<String, Object> getPvUv(@Param("ad_posttion_id") List<String> date, @Param("pmp_resource_id") String resourceId);
}
