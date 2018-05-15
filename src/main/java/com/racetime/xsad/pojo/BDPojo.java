package com.racetime.xsad.pojo;

/**
 * @author hu_xuanhua_hua
 * @ClassName: BDPoJo
 * @Description: TODO
 * @date 2018-05-09 17:08
 * @versoin 1.0
 **/
public class BDPojo {
    /************md5Key开始*******/
    private String ssp_app_id;
    private String ssp_adslot_id;
    private String adx_app_id;
    private String adx_adslot_id;
    private String ad_customer_id;
    private String ad_city_code;
    private String ad_channel_id;
    private String date_hour;
    private String date_day;
    private String scene_id;
    /************md5Key结束*******/


    private int adx_request_num = 0;
    private int response_bid_success_num = 0;
    private int response_bid_fail_num = 0;
    private int return_success_num = 0;
    private int black_success_num = 0;


    private String createtime;
    private String ad_serving_id;
    private String md5Key;

    public String getAdx_app_id() {
        return adx_app_id;
    }

    public void setAdx_app_id(String adx_app_id) {
        this.adx_app_id = adx_app_id;
    }

    public String getAdx_adslot_id() {
        return adx_adslot_id;
    }

    public void setAdx_adslot_id(String adx_adslot_id) {
        this.adx_adslot_id = adx_adslot_id;
    }


    public String getAd_customer_id() {
        return ad_customer_id;
    }

    public void setAd_customer_id(String ad_customer_id) {
        this.ad_customer_id = ad_customer_id;
    }

    public String getAd_city_code() {
        return ad_city_code;
    }

    public void setAd_city_code(String ad_city_code) {
        this.ad_city_code = ad_city_code;
    }

    public String getAd_channel_id() {
        return ad_channel_id;
    }

    public void setAd_channel_id(String ad_channel_id) {
        this.ad_channel_id = ad_channel_id;
    }

    public String getDate_hour() {
        return date_hour;
    }

    public void setDate_hour(String date_hour) {
        this.date_hour = date_hour;
    }

    public String getDate_day() {
        return date_day;
    }

    public void setDate_day(String date_day) {
        this.date_day = date_day;
    }

    public String getScene_id() {
        return scene_id;
    }

    public void setScene_id(String scene_id) {
        this.scene_id = scene_id;
    }


    public int getAdx_request_num() {
        return adx_request_num;
    }

    public void setAdx_request_num(int adx_request_num) {
        this.adx_request_num = adx_request_num;
    }

    public int getResponse_bid_success_num() {
        return response_bid_success_num;
    }

    public void setResponse_bid_success_num(int response_bid_success_num) {
        this.response_bid_success_num = response_bid_success_num;
    }

    public int getResponse_bid_fail_num() {
        return response_bid_fail_num;
    }

    public void setResponse_bid_fail_num(int response_bid_fail_num) {
        this.response_bid_fail_num = response_bid_fail_num;
    }

    public int getReturn_success_num() {
        return return_success_num;
    }

    public void setReturn_success_num(int return_success_num) {
        this.return_success_num = return_success_num;
    }

    public int getBlack_success_num() {
        return black_success_num;
    }

    public void setBlack_success_num(int black_success_num) {
        this.black_success_num = black_success_num;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public String getAd_serving_id() {
        return ad_serving_id;
    }

    public void setAd_serving_id(String ad_serving_id) {
        this.ad_serving_id = ad_serving_id;
    }

    public String getMd5Key() {
        return md5Key;
    }

    public void setMd5Key(String md5Key) {
        this.md5Key = md5Key;
    }

    public String getSsp_app_id() {
        return ssp_app_id;
    }

    public void setSsp_app_id(String ssp_app_id) {
        this.ssp_app_id = ssp_app_id;
    }

    public String getSsp_adslot_id() {
        return ssp_adslot_id;
    }

    public void setSsp_adslot_id(String ssp_adslot_id) {
        this.ssp_adslot_id = ssp_adslot_id;
    }
}
