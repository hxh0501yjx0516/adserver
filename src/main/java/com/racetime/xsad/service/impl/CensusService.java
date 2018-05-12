package com.racetime.xsad.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.racetime.xsad.dao.OrderDao;
import com.racetime.xsad.pojo.*;
import com.racetime.xsad.service.ICensusService;
import com.racetime.xsad.util.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import redis.clients.jedis.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author hu_xuanhua_hua
 * @ClassName: CensusService
 * @Description: 统计日志，产生报表；
 * @date 2018-05-03 14:17
 * @versoin 1.0
 **/
@Service
@PropertySource({"classpath:jdbc.properties"})
public class CensusService implements ICensusService {
    @Autowired
    Environment env;
    @Autowired
    private ShardedJedisPool shardedJedisPool;
    @Autowired
    private OrderDao orderDao;
    private static Map<String, Object> staticMap = new HashMap<>();

    /**
     * 读取日志
     * 将日志放进redis
     */
    @Override
    public void collectReport() {
        Map<String, Set<Object>> map = scanLog();//读取日志
        insertRedis(map);//插入redis

    }

    /**
     * 读取redis
     * 整理日志，删除redis(已经处理过的)
     * 整理报表数据，入库
     */
    @Override
    public void handleReport() {
        Map<String, String> map = getRedis();//读取redis
        List<BDPojo> pdjoList = new ArrayList<>();
        dataReport(map, pdjoList);//整理日志，且删除reids
        insertReport(pdjoList);//整理报表数据，入库
    }

    @Override
    public void getCustomer_id() {
        List<Map<String, Object>> list = orderDao.getCustomer_id();
        for (Map<String, Object> map : list) {
            staticMap.put(map.get("material_url").toString(), map.get("customer_id"));
        }
    }


    private Map<String, String> getRedis() {
        ShardedJedis shardedJedis = shardedJedisPool.getResource();
        try {
            ShardedJedisPipeline sp = shardedJedis.pipelined();
            Collection<Jedis> collection = shardedJedis.getAllShards();
            Iterator<Jedis> jedis = collection.iterator();
            while (jedis.hasNext()) {
                jedis.next().select(0);
            }

            Map<String, String> map = shardedJedis.hgetAll("execute_num");
            return map;
        } finally {
            shardedJedis.close();
        }
    }

    /**
     * @param map
     */
    private void dataReport(Map<String, String> map, List<BDPojo> bdPojoList) {


//        List<BDPojo> bdPojoList = new ArrayList<>();
//        List<PTPojo> ptPojoList = new ArrayList<>();

        String bd_pt = null;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String value = entry.getValue();
            List<Object> list = JSON.parseArray(value, Object.class);
            int listNum = list.size();
            int if_6_or_7 = 0;
            String line;

            for (int i = 0; i < listNum; i++) {
                line = list.get(i).toString();
                JSONObject jsonObject = JSONObject.parseObject(line);
                int type = Integer.parseInt(jsonObject.get("log_type").toString());
                if (type == 3) {
                    continue;
                }
                JSONObject json = JSONObject.parseObject(jsonObject.get("log_data").toString());

                if (type == 6) {
                    bd_pt = jsonObject.get("source_type").toString();
                    if ("1003".equals(json.get("code")) || "1001".equals(json.get("code"))) {
                        if_6_or_7 = 6;
                        break;
                    }
                } else if (type == 7) {
                    if_6_or_7 = 7;

                }
            }
            if (if_6_or_7 == 6) {
                getPojo6(list, bdPojoList);
                //删除redis
            } else if (if_6_or_7 == 7) {
                getPojo7(list, bd_pt, bdPojoList);
                //删除redis
            }
        }

    }

    private void insertReport(List<BDPojo> pojoList) {
        Map<String, BDPojo> bdPojoMap = new HashMap<>();
        Map<String, Integer> adx_request_numMap = new HashMap<>();
        Map<String, Integer> response_bid_success_numMap = new HashMap<>();
        Map<String, Integer> response_bid_fail_numMap = new HashMap<>();
        Map<String, Integer> return_success_numMap = new HashMap<>();
        Map<String, Integer> black_success_numMap = new HashMap<>();
        String md5Key = null;
        if (pojoList != null && pojoList.size() > 0) {
            for (BDPojo bdPojo : pojoList) {
                md5Key = MD5Util.MD5Encode(bdPojo.getSsp_app_id() + bdPojo.getSsp_adslot_id()
                        + bdPojo.getAdx_app_id() + bdPojo.getAdx_adslot_id()
                        + bdPojo.getAd_customer_id() + bdPojo.getAd_city_code()
                        + bdPojo.getAd_channel_id() + bdPojo.getScene_id()
                        + getHour() + getDay());
                if (bdPojoMap.size() == 0 || bdPojoMap.get(md5Key) == null) {
                    BDPojo pojo = new BDPojo();
                    pojo.setSsp_app_id(bdPojo.getSsp_app_id());
                    pojo.setSsp_adslot_id(bdPojo.getSsp_adslot_id());
                    pojo.setAdx_app_id(bdPojo.getAdx_app_id());
                    pojo.setAdx_adslot_id(bdPojo.getAdx_adslot_id());
                    pojo.setAd_customer_id(bdPojo.getAd_customer_id());
                    pojo.setAd_channel_id(bdPojo.getAd_channel_id());
                    pojo.setDate_hour(getHour());
                    pojo.setDate_day(getDay());
                    pojo.setAd_serving_id(bdPojo.getAd_serving_id());
                    pojo.setAd_city_code(bdPojo.getAd_city_code());
                    pojo.setMd5Key(md5Key);
                    bdPojoMap.put(md5Key, pojo);
                }


                if (adx_request_numMap != null && adx_request_numMap.get(md5Key) != null) {
                    adx_request_numMap.put(md5Key, adx_request_numMap.get(md5Key) + bdPojo.getAdx_request_num());
                } else {
                    adx_request_numMap.put(md5Key, bdPojo.getAdx_request_num());
                }
                if (response_bid_success_numMap != null && response_bid_success_numMap.get(md5Key) != null) {
                    response_bid_success_numMap.put(md5Key, response_bid_success_numMap.get(md5Key) + bdPojo.getResponse_bid_success_num());
                } else {
                    response_bid_success_numMap.put(md5Key, bdPojo.getResponse_bid_success_num());
                }
                if (response_bid_fail_numMap != null && response_bid_fail_numMap.get(md5Key) != null) {
                    response_bid_fail_numMap.put(md5Key, response_bid_fail_numMap.get(md5Key) + bdPojo.getResponse_bid_fail_num());
                } else {
                    response_bid_fail_numMap.put(md5Key, bdPojo.getResponse_bid_fail_num());
                }
                if (return_success_numMap != null && return_success_numMap.get(md5Key) != null) {
                    return_success_numMap.put(md5Key, return_success_numMap.get(md5Key) + bdPojo.getReturn_success_num());
                } else {
                    return_success_numMap.put(md5Key, bdPojo.getReturn_success_num());
                }
                if (black_success_numMap != null && black_success_numMap.get(md5Key) != null) {
                    black_success_numMap.put(md5Key, black_success_numMap.get(md5Key) + bdPojo.getBlack_success_num());
                } else {
                    black_success_numMap.put(md5Key, bdPojo.getBlack_success_num());
                }

            }

            /**
             * 拼装入库对象
             */
            List<BDPojo> bdPojoList = new ArrayList<>();
            if (bdPojoMap != null && bdPojoMap.size() > 0) {
                for (Map.Entry<String, BDPojo> entry : bdPojoMap.entrySet()) {
                    String key = entry.getKey();
                    BDPojo bdPojo = entry.getValue();
                    if (adx_request_numMap != null && adx_request_numMap.size() > 0 && adx_request_numMap.get(key) != null) {
                        bdPojo.setAdx_request_num(adx_request_numMap.get(key));
                    }
                    if (response_bid_success_numMap != null && response_bid_success_numMap.size() > 0 && response_bid_success_numMap.get(key) != null) {
                        bdPojo.setResponse_bid_success_num(response_bid_success_numMap.get(key));
                    }
                    if (response_bid_fail_numMap != null && response_bid_fail_numMap.size() > 0 && response_bid_fail_numMap.get(key) != null) {
                        bdPojo.setResponse_bid_fail_num(response_bid_fail_numMap.get(key));
                    }
                    if (return_success_numMap != null && return_success_numMap.size() > 0 && return_success_numMap.get(key) != null) {
                        bdPojo.setReturn_success_num(return_success_numMap.get(key));
                    }
                    if (black_success_numMap != null && black_success_numMap.size() > 0 && black_success_numMap.get(key) != null) {
                        bdPojo.setBlack_success_num(black_success_numMap.get(key));
                    }
                    bdPojoList.add(bdPojo);
                }
            }
            /**
             * 入库
             */
            if (bdPojoList != null && bdPojoList.size() > 0) {
                for (BDPojo pojo : bdPojoList) {
                    orderDao.insertOrUpdateAdx_Report(pojo);
                }
            }

        }
    }

    /**
     * 如果是返回广告失败的情况
     *
     * @param
     * @param
     * @param list
     */
    private void getPojo6(List<Object> list, List<BDPojo> bdPojoLis) {
        String line = null;
        int listNum = list.size();
        BDPojo bdPojo = new BDPojo();
        BDPojo ptPojo = new BDPojo();
        for (int i = 0; i < listNum; i++) {
            line = list.get(i).toString();
            JSONObject jsonObject = JSONObject.parseObject(line);
            int type = Integer.parseInt(jsonObject.get("log_type").toString());
            if (type == 3) {
                continue;
            }
            JSONObject json = JSONObject.parseObject(jsonObject.get("log_data").toString());

            if (type == 1) {
                //获取场景和城市code
                bdPojo.setSsp_app_id(json.get("app_id").toString());
                bdPojo.setSsp_adslot_id(json.get("adslot_id").toString());
                ptPojo.setSsp_app_id(json.get("app_id").toString());
                ptPojo.setSsp_adslot_id(json.get("adslot_id").toString());
            } else if (type == 2) {
                //获取场景和城市code
                bdPojo.setAd_city_code(json.get("city_code").toString());
                bdPojo.setScene_id(json.get("scene_id").toString());
                ptPojo.setAd_city_code(json.get("city_code").toString());
                ptPojo.setScene_id(json.get("scene_id").toString());
            } else if (type == 4) {
                //bd获取app_id和adslot_id
                //pt获取strategy_id和 app_id
                if ("BD".equals(jsonObject.get("source_type"))) {
                    bdPojo.setAdx_app_id(json.get("app_id").toString());
                    JSONObject adslot_id = JSONObject.parseObject(json.get("slot").toString());
                    bdPojo.setAdx_adslot_id(adslot_id.get("adslot_id").toString());
                    ptPojo.setAd_serving_id("0");
                    bdPojo.setAd_channel_id("BD");
                    bdPojo.setAdx_request_num(1);

                } else if ("PT".equals(jsonObject.get("source_type"))) {
                    bdPojo.setAdx_app_id(json.get("app_id").toString());
                    ptPojo.setAdx_adslot_id(json.get("adslot_id").toString());
                    ptPojo.setAd_serving_id(json.get("strategy_id").toString());
                    ptPojo.setAd_channel_id("PT");
                    ptPojo.setAdx_request_num(1);
                }
            } else if (type == 6) {
                bdPojo.setResponse_bid_fail_num(1);
                ptPojo.setResponse_bid_fail_num(1);

            }
            //插入list集合
            if (bdPojo.getAdx_app_id() != null) {
                bdPojoLis.add(bdPojo);
            }
            if (ptPojo.getAd_serving_id() != null) {
                bdPojoLis.add(ptPojo);
            }
        }

    }

    /**
     * 如果是返回广告成功的情况
     *
     * @param
     * @param list
     */
    private void getPojo7(List<Object> list, String bd_pt, List<BDPojo> bdPojoList) {
        String line = null;
        int listNum = list.size();
        BDPojo bdPojo = new BDPojo();
        BDPojo ptPojo = new BDPojo();
        for (int i = 0; i < listNum; i++) {
            line = list.get(i).toString();
            JSONObject jsonObject = JSONObject.parseObject(line);
            int type = Integer.parseInt(jsonObject.get("log_type").toString());
            if (type == 3) {
                continue;
            }
            JSONObject json = JSONObject.parseObject(jsonObject.get("log_data").toString());
            if (type == 1) {
                //获取场景和城市code
                bdPojo.setSsp_app_id(json.get("app_id").toString());
                bdPojo.setSsp_adslot_id(json.get("adslot_id").toString());
                ptPojo.setSsp_app_id(json.get("app_id").toString());
                ptPojo.setSsp_adslot_id(json.get("adslot_id").toString());
            } else if (type == 2) {
                //获取场景和城市code
                bdPojo.setAd_city_code(json.get("city_code").toString());
                bdPojo.setScene_id(json.get("scene_id").toString());
                ptPojo.setAd_city_code(json.get("city_code").toString());
                ptPojo.setScene_id(json.get("scene_id").toString());
            } else if (type == 4) {
                //bd获取app_id和adslot_id
                //pt获取strategy_id和 app_id
                if ("BD".equals(jsonObject.get("source_type"))) {
                    bdPojo.setAdx_app_id(json.get("app_id").toString());
                    JSONObject adslot_id = JSONObject.parseObject(json.get("slot").toString());
                    bdPojo.setAdx_adslot_id(adslot_id.get("adslot_id").toString());
                    bdPojo.setAd_channel_id("BD");
                    bdPojo.setAd_serving_id("0");
                    bdPojo.setAdx_request_num(1);

                } else if ("PT".equals(jsonObject.get("source_type"))) {
                    ptPojo.setAdx_adslot_id(json.get("adslot_id").toString());
                    ptPojo.setAd_serving_id(json.get("strategy_id").toString());
                    ptPojo.setAdx_app_id(json.get("app_id").toString());
                    ptPojo.setAd_channel_id("PT");
                    ptPojo.setAdx_request_num(1);
                }
            } else if (type == 6) {

                String data = json.get("data").toString();
                List<Map> listData = JSONArray.parseArray(data, Map.class);
                String key = listData.get(0).get("ad_url").toString();
                if (staticMap.get(key) != null) {
                    //bd 判断是否成功，失败
                    if ("BD".equals(jsonObject.get("source_type"))) {
                        bdPojo.setResponse_bid_success_num(1);
                        ptPojo.setResponse_bid_fail_num(1);
                        bdPojo.setAd_customer_id(staticMap.get(key).toString());
                        ptPojo.setAd_customer_id("0");
                    } else if ("PT".equals(jsonObject.get("source_type"))) {
                        ptPojo.setResponse_bid_success_num(1);
                        bdPojo.setResponse_bid_fail_num(1);
                        ptPojo.setAd_customer_id(staticMap.get(key).toString());
                        bdPojo.setAd_customer_id("0");


                    }
                }

            } else if (type == 7) {
                if ("BD".equals(bd_pt)) {
                    bdPojo.setReturn_success_num(1);
                    if ("1".equals(json.get("callback_type"))) {
                        bdPojo.setBlack_success_num(1);
                    }
                } else if ("PT".equals(bd_pt)) {
                    ptPojo.setReturn_success_num(1);
                    ptPojo.setBlack_success_num(1);
                }

            }

        }
        //插入list集合
        if (bdPojo.getAdx_app_id() != null) {
            bdPojoList.add(bdPojo);
        }
        if (ptPojo.getAd_serving_id() != null) {
            bdPojoList.add(ptPojo);
        }

    }

    /**
     * 读取文件，将数据封装到map中
     */
    private Map<String, Set<Object>> scanLog() {
        final File[] sourcefiles = getSourceFileAdress().listFiles();//获取源文件
        Map<String, Set<Object>> map = new HashMap<>();//日志的文件进入map集合
        for (int i = 0; i < sourcefiles.length; i++) {
            String fileName = sourcefiles[i].getAbsoluteFile().getName();
            System.err.println(fileName);
            System.err.println(getDatetime());
            BufferedReader bufr = null;
            InputStreamReader isr = null;
            try {
                isr = new InputStreamReader(new FileInputStream(sourcefiles[i]), "UTF-8");
                bufr = new BufferedReader(isr);
                String line = null;
                JSONObject json = null;
                while ((line = bufr.readLine()) != null) {
                    if (!"".equals(line)) {
                        json = JSONObject.parseObject(line);
                        if (map != null && map.size() > 0) {
                            if (map.get(json.getString("request_id")) != null) {
                                map.get(json.getString("request_id")).add(json);
                            } else {
                                Set<Object> set = new HashSet<>();
                                set.add(json);
                                map.put(json.getString("request_id"), set);

                            }
                        } else {
                            Set<Object> set = new HashSet<>();
                            set.add(json);
                            map.put(json.getString("request_id"), set);
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    isr.close();
                    bufr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                sourcefiles.clone();
            }
        }

        return map;

    }


    private void insertRedis(Map<String, Set<Object>> map) {
        ShardedJedis shardedJedis = shardedJedisPool.getResource();
        try {
            ShardedJedisPipeline sp = shardedJedis.pipelined();
            Map<String, Object> redisMap = new HashMap<>();//存储cpt计数
            Collection<Jedis> collection = shardedJedis.getAllShards();
            Iterator<Jedis> jedis = collection.iterator();
            while (jedis.hasNext()) {
                jedis.next().select(0);
            }
            Map<String, String> allMap = shardedJedis.hgetAll("execute_num");
            if (map != null && map.size() > 0) {//放进redis
                for (Map.Entry<String, Set<Object>> entry : map.entrySet()) {
                    String key = entry.getKey();
                    Set<Object> value = entry.getValue();
                    if (allMap.get(key) != null) {//如何已经存在，则融合后再插入
                        String str = allMap.get(key);
                        List<Object> list = JSON.parseArray(str, Object.class);
                        Set<Object> set = new HashSet<>(list);
                        map.get(key).addAll(set);
                        sp.hset("execute_num", key, JSON.toJSONString(value));
                    } else {//不存在，直接插入
                        sp.hset("execute_num", key, JSON.toJSONString(value));
                    }
                }
                sp.sync();
                shardedJedis.hset("heartbeat", "execute_num", "存放广告产生的日志" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            }
        } finally {
            shardedJedis.close();
        }

    }


    /**
     * 将处理过的文件移除
     *
     * @param oldFileName
     * @param newFileName
     * @throws IOException
     */
    private void moveFile(String oldFileName, String newFileName) throws IOException {

        Path fromPath = Paths.get(oldFileName); //   相当于 c:\test\a.txt  a.txt为需要复制的文件
        Path toPath = Paths.get(newFileName);  //    相当于 c:\test1\b.txt 。 b.txt无需存在
        Files.move(fromPath, toPath); //移动文件（即复制并删除源文件）
    }

    /**
     * 获取要去读这个文件以及这个文件以前的时间的文件
     *
     * @return
     */
    private String getDatetime() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -1);//控制分
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
        return sdf.format(cal.getTime());
    }

    private String getDay() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 0);//控制分
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(cal.getTime());
    }

    private String getHour() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 0);//控制分
        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        return sdf.format(cal.getTime());
    }

    /**
     * starttime-endTime >0
     *
     * @param startime
     * @param endTime
     * @return
     */
    private long timeDifference(String startime, String endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
        long result = 0;
        try {
            result = sdf.parse(startime).getTime() - sdf.parse(endTime).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 源文件地址
     */
    private File getSourceFileAdress() {
        String filepath = env.getProperty("filepath");
        File sourcefile = new File(filepath);
        File file5 = null;
//        if (hour().equals("23")) {//临界点时间凌晨一点
//            sourcefile = new File(oldpath + File.separator + yesterday() + File.separator + hour());
//        } else {//正常点的时间
//            sourcefile = new File(oldpath + File.separator + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + File.separator + hour());
//        }
        if (!sourcefile.exists()) {
            sourcefile.mkdirs();
        }
        return sourcefile;
    }

//    public static void main(String[] args) {
////        ReportPojo reportPojo = new ReportPojo();
////        System.err.println(reportPojo.getSsp_app_id());
////        String a = "12321";
////        String b = "12321";
////        System.err.println(a == b);
////        Set<String> set = new HashSet<>();
////        for (int i = 0; i < set.size(); i++) {
//////            System.err.println(set.);
////        }
//
//
////        Map<String, List<Integer>> map = new HashMap<>();
////        String key = "q";
////        for (int i = 0; i < 10; i++) {
////            if (map != null && map.get(key) != null) {
////                map.get(key).add(1);
////            } else {
////                List<Integer> list = new ArrayList<>();
////                map.put(key, list);
////            }
////            String k = "";
////        }
////        System.err.println(map.get(key).size());
//
//        Map<String, Set<String>> map1 = new HashMap<>();
//        Map<String, Set<String>> map2 = new HashMap<>();
//        Set<String> list = new HashSet<>();
//        list.add("value1");
//        map1.put("key", list);
//        list = new HashSet<>();
//        list.add("value2");
//        map2.put("key", list);
//        map1.get("key").addAll(map2.get("key"));
//        String str = JSON.toJSONString(map1);
//        System.err.println(str);
//        JSONObject obj = JSONObject.parseObject(str);
//
//        String kk = obj.get("key").toString();
//
//        Set<String> s1 = new HashSet<>();
//        Set<String> s2 = new HashSet<>();
//        s1.add("1");
//        s1.add("2");
//        s1.add("3");
//        s2.add("2");
//        s1.addAll(s2);
//        System.err.println(s1);
//
//
//        System.err.println(kk);
//
//
//        for (int i = 0; i < 10; i++) {
//            break;
//        }
//
//
//        String str1 = null;
//        System.err.println(str1.equals("BD"));


//    }

    public static void main(String[] args) {
//        /******list集合添加数据********/
//        List<String> list = new ArrayList<>();
//        list.add("美国");
//        list.add("日本");
//        list.add("韩国");
//        /******list集合解析数据********/
//        if (list != null && list.size() > 0) {//判断list集合是否为空
//            int listNum = list.size();//求出list的大小
//
//            for (int i = 0; i < listNum; i++) {//遍历下，并输出
//                System.out.println("fori输出：" + list.get(i));
//            }
//
//            for (String str : list) {
//                System.err.println("foreach输出：" + str);
//            }
//        } else {
//            System.out.println("集合为空，无法解析");
//        }
        Map<String, Integer> map = new HashMap<>();
        System.err.println(map.get("123"));
    }

}
