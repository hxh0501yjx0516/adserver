package com.racetime.xsad.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.racetime.xsad.dao.OrderDao;
import com.racetime.xsad.pojo.*;
import com.racetime.xsad.service.ICensusService;
import com.racetime.xsad.util.MD5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private Logger log = LoggerFactory.getLogger(CensusService.class);

    @Autowired
    Environment env;
    @Autowired
    private ShardedJedisPool shardedJedisPool;
    @Autowired
    private OrderDao orderDao;
    private static Map<String, Object> staticMap = new HashMap<>();
    private static Map<String, Object> staticChannelMap = new HashMap<>();

    @Override
    public void launcCcount() {
//        Map<String, Object> map1 = new HashedMap();
//        map1.put("strategy_id", "37");
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("request_id", "60be8f5e743b45bfb12605e16677dbca");
//        jsonObject.put("log_data", map1);


        ShardedJedis shardedJedis = shardedJedisPool.getResource();
        try {
            Collection<Jedis> collection = shardedJedis.getAllShards();
            Iterator<Jedis> jedis = collection.iterator();
            while (jedis.hasNext()) {
                jedis.next().select(0);
            }
            boolean isLive = shardedJedis.exists("callback");
            Set<String> launcCcountSet = new HashSet<>();
            if (isLive) {
                launcCcountSet = shardedJedis.spop("callback", 1000);
            }
            Map<String, Integer> insertRedisMap = new HashMap<>();
            for (String launcCcount : launcCcountSet) {//遍历redis中取出的数据处理整合
                log.info("计数===="+launcCcount);
                JSONObject obj = JSONObject.parseObject(launcCcount);
                String log_data = obj.get("log_data").toString();
                JSONObject log_dataJSON = JSONObject.parseObject(log_data);
                String strategy_id = log_dataJSON.get("strategy_id").toString();
                if (insertRedisMap != null && insertRedisMap.size() > 0 && insertRedisMap.get("strategy_id") != null) {
                    insertRedisMap.put(strategy_id, insertRedisMap.get(strategy_id) + 1);
                } else {
                    insertRedisMap.put(strategy_id, 1);
                }
            }
            /**
             * 往redis中仍计数
             */
            if (insertRedisMap != null && insertRedisMap.size() > 0) {
                for (Map.Entry<String, Integer> entry : insertRedisMap.entrySet()) {
                    String key = entry.getKey();
                    Integer value = entry.getValue();

                    if (shardedJedis.hget("execute_num", key) != null) {
                        int count = Integer.parseInt(shardedJedis.hget("execute_num", key));
                        shardedJedis.hset("execute_num", key, String.valueOf(value + count));
                    } else {
                        shardedJedis.hset("execute_num", key, value.toString());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally

        {
            shardedJedis.close();
        }

    }


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
        List<Pojo> pdjoList = new ArrayList<>();
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

    @Override
    public void selectChannel_id() {
        List<Map<String, Object>> list = orderDao.selectChannel_id();
        for (Map<String, Object> map : list) {
            staticChannelMap.put(map.get("adx_app_id").toString(), map.get("channel_id"));
        }
    }


    private Map<String, String> getRedis() {
        ShardedJedis shardedJedis = shardedJedisPool.getResource();
        try {
            Collection<Jedis> collection = shardedJedis.getAllShards();
            Iterator<Jedis> jedis = collection.iterator();
            while (jedis.hasNext()) {
                jedis.next().select(0);
            }
            Map<String, String> map = shardedJedis.hgetAll("report_log");
            return map;
        } finally {
            shardedJedis.close();
        }
    }

    /**
     * @param map
     */
    private void dataReport(Map<String, String> map, List<Pojo> bdPojoList) {

        ShardedJedis shardedJedis = shardedJedisPool.getResource();
        try {
            ShardedJedisPipeline sp = shardedJedis.pipelined();
            Collection<Jedis> collection = shardedJedis.getAllShards();
            Iterator<Jedis> jedis = collection.iterator();
            while (jedis.hasNext()) {
                jedis.next().select(0);
            }


            String bd_pt = null;
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                List<Object> list = JSON.parseArray(value, Object.class);
                int listNum = list.size();
                int if_6_or_7 = 0;
                String line;

                for (int i = 0; i < listNum; i++) {
                    line = list.get(i).toString();
                    JSONObject jsonObject = JSONObject.parseObject(line);
                    int type = Integer.parseInt(jsonObject.get("log_type").toString());
                    if (listNum == 1 && type == 7) {//如果只有7，直接扔掉，垃圾数据
                        sp.hdel("report_log", key);
                        break;
                    }

                    if (type == 3) {
                        continue;
                    }
                    JSONObject json = null;
                    if (!(jsonObject.get("log_data") == null && "".equals(jsonObject.get("log_data").toString().trim()))) {
                        json = JSONObject.parseObject(jsonObject.get("log_data").toString());

                    }

                    if (type == 6) {
                        bd_pt = jsonObject.get("source_type").toString();
                        if (json == null || "1003".equals(json.get("code")) || "1001".equals(json.get("code"))) {
                            if_6_or_7 = 7;
                            break;
                        } else if ("1002".equals(json.get("code"))) {
                            String start = jsonObject.get("create_time").toString();
                            int minute = getMinute(start, getDateTime());
                            if (minute > 40) {
                                if_6_or_7 = 7;
                                break;
                            }
                        }
                    } else if (type == 7) {
                        if_6_or_7 = 7;

                    }
                }
                if (if_6_or_7 == 7) {
                    getPojo(list, bd_pt, bdPojoList);
                    //删除redis
                    sp.hdel("report_log", key);

                }
            }
            sp.sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shardedJedis.close();
        }

    }

    private void insertReport(List<Pojo> pojoList) {
        Map<String, Pojo> bdPojoMap = new HashMap<>();
        Map<String, Integer> adx_request_numMap = new HashMap<>();
        Map<String, Integer> response_bid_success_numMap = new HashMap<>();
        Map<String, Integer> response_bid_fail_numMap = new HashMap<>();
        Map<String, Integer> return_success_numMap = new HashMap<>();
        Map<String, Integer> black_success_numMap = new HashMap<>();
        String md5Key = null;
        if (pojoList != null && pojoList.size() > 0) {
            for (Pojo pojomd5 : pojoList) {
                md5Key = MD5Util.MD5Encode(pojomd5.getSsp_app_id() + pojomd5.getSsp_adslot_id()
                        + pojomd5.getAdx_app_id() + pojomd5.getAdx_adslot_id()
                        + pojomd5.getAd_customer_id() + pojomd5.getAd_city_code()
                        + staticChannelMap.get(pojomd5.getAdx_app_id()) + pojomd5.getScene_id()
                        + pojomd5.getDate_hour() + pojomd5.getDate_day());
                if (bdPojoMap.size() == 0 || bdPojoMap.get(md5Key) == null) {
                    Pojo pojo = new Pojo();
                    pojo.setSsp_app_id(pojomd5.getSsp_app_id());
                    pojo.setSsp_adslot_id(pojomd5.getSsp_adslot_id());
                    pojo.setAdx_app_id(pojomd5.getAdx_app_id());
                    pojo.setAdx_adslot_id(pojomd5.getAdx_adslot_id());
                    pojo.setAd_customer_id(pojomd5.getAd_customer_id());
                    pojo.setAd_channel_id(staticChannelMap.get(pojomd5.getAdx_app_id()).toString());
                    pojo.setDate_hour(pojomd5.getDate_hour());
                    pojo.setDate_day(pojomd5.getDate_day());
                    pojo.setAd_serving_id(pojomd5.getAd_serving_id());
                    pojo.setAd_city_code(pojomd5.getAd_city_code());
                    pojo.setMd5Key(md5Key);
                    pojo.setScene_id(pojomd5.getScene_id());
                    bdPojoMap.put(md5Key, pojo);
                }


                if (adx_request_numMap != null && adx_request_numMap.get(md5Key) != null) {
                    adx_request_numMap.put(md5Key, adx_request_numMap.get(md5Key) + pojomd5.getAdx_request_num());
                } else {
                    adx_request_numMap.put(md5Key, pojomd5.getAdx_request_num());
                }
                if (response_bid_success_numMap != null && response_bid_success_numMap.get(md5Key) != null) {
                    response_bid_success_numMap.put(md5Key, response_bid_success_numMap.get(md5Key) + pojomd5.getResponse_bid_success_num());
                } else {
                    response_bid_success_numMap.put(md5Key, pojomd5.getResponse_bid_success_num());
                }
                if (response_bid_fail_numMap != null && response_bid_fail_numMap.get(md5Key) != null) {
                    response_bid_fail_numMap.put(md5Key, response_bid_fail_numMap.get(md5Key) + pojomd5.getResponse_bid_fail_num());
                } else {
                    response_bid_fail_numMap.put(md5Key, pojomd5.getResponse_bid_fail_num());
                }
                if (return_success_numMap != null && return_success_numMap.get(md5Key) != null) {
                    return_success_numMap.put(md5Key, return_success_numMap.get(md5Key) + pojomd5.getReturn_success_num());
                } else {
                    return_success_numMap.put(md5Key, pojomd5.getReturn_success_num());
                }
                if (black_success_numMap != null && black_success_numMap.get(md5Key) != null) {
                    black_success_numMap.put(md5Key, black_success_numMap.get(md5Key) + pojomd5.getBlack_success_num());
                } else {
                    black_success_numMap.put(md5Key, pojomd5.getBlack_success_num());
                }

            }

            /**
             * 拼装入库对象
             */
            List<Pojo> bdPojoList = new ArrayList<>();
            if (bdPojoMap != null && bdPojoMap.size() > 0) {
                for (Map.Entry<String, Pojo> entry : bdPojoMap.entrySet()) {
                    String key = entry.getKey();
                    Pojo bdPojo = entry.getValue();
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
                for (Pojo pojo : bdPojoList) {
                    orderDao.insertOrUpdateAdx_Report(pojo);
                }
            }

        }
    }


    /**
     * 处理完整日志的逻辑
     *
     * @param
     * @param list
     */
    private void getPojo(List<Object> list, String bd_pt, List<Pojo> bdPojoList) {
        String line = null;
        int listNum = list.size();
        Pojo bdPojo = new Pojo();
        Pojo ptPojo = new Pojo();
        for (int i = 0; i < listNum; i++) {
            line = list.get(i).toString();
            JSONObject jsonObject = JSONObject.parseObject(line);
            int type = Integer.parseInt(jsonObject.get("log_type").toString());
            if (type == 3) {
                continue;
            }
            JSONObject json = null;
            if (!(jsonObject.get("log_data") == null && "".equals(jsonObject.get("log_data").toString().trim()))) {
                json = JSONObject.parseObject(jsonObject.get("log_data").toString());

            }
            if (type == 1) {
                //获取场景和城市code
                bdPojo.setSsp_app_id(json.get("app_id").toString());
                bdPojo.setSsp_adslot_id(json.get("adslot_id").toString());
                ptPojo.setSsp_app_id(json.get("app_id").toString());
                ptPojo.setSsp_adslot_id(json.get("adslot_id").toString());
            } else if (type == 2) {
                if (json != null) {
                    //获取场景和城市code
                    bdPojo.setAd_city_code(json.get("city_code").toString());
                    bdPojo.setScene_id(json.get("scene_id").toString());
                    ptPojo.setAd_city_code(json.get("city_code").toString());
                    ptPojo.setScene_id(json.get("scene_id").toString());
                } else {
                    bdPojo.setAd_city_code("");
                    bdPojo.setScene_id("");
                    ptPojo.setAd_city_code("");
                    ptPojo.setScene_id("");
                }
            } else if (type == 4) {
                //bd获取app_id和adslot_id
                //pt获取strategy_id和 app_id
                if ("BD".equals(jsonObject.get("source_type"))) {
                    bdPojo.setAdx_app_id(json.get("app_id").toString());
                    JSONObject adslot_id = JSONObject.parseObject(json.get("slot").toString());
                    bdPojo.setAdx_adslot_id(adslot_id.get("adslot_id").toString());
                    bdPojo.setAd_serving_id("0");
                    bdPojo.setAdx_request_num(1);

                } else if ("PT".equals(jsonObject.get("source_type"))) {
                    if (json != null) {
                        ptPojo.setAdx_adslot_id(json.get("adslot_id").toString());
                        ptPojo.setAd_serving_id(json.get("strategy_id").toString());
                        ptPojo.setAdx_app_id(json.get("app_id").toString());
                        ptPojo.setAdx_request_num(1);
                    }

                }
            } else if (type == 6) {

//                String data = json.get("data").toString();
//                List<Map> listData = JSONArray.parseArray(data, Map.class);
//                String key = listData.get(0).get("ad_url").toString();
//                if (staticMap.get(key) != null) {
                //bd 判断是否成功，失败
                if ("BD".equals(jsonObject.get("source_type"))) {
                    String datetime = updateFormat(jsonObject.getString("create_time"));
                    String[] date = datetime.split(" ");
                    bdPojo.setDate_day(date[0]);
                    bdPojo.setDate_hour(date[1]);
                    if (json == null || "1003".equals(json.get("code")) || "1001".equals(json.get("code"))) {
                        bdPojo.setResponse_bid_fail_num(1);

                    } else if ("1002".equals(json.get("code"))) {
                        String data = json.get("data").toString();
                        List<Map> listData = JSONArray.parseArray(data, Map.class);
                        String key = listData.get(0).get("ad_url").toString();
                        bdPojo.setResponse_bid_success_num(1);
                        ptPojo.setResponse_bid_fail_num(1);
                        ptPojo.setAd_customer_id("0");
                        if (staticMap.get(key) != null) {
                            bdPojo.setAd_customer_id(staticMap.get(key).toString());

                        }
                    }
                } else if ("PT".equals(jsonObject.get("source_type"))) {
                    String datetime = updateFormat(jsonObject.getString("create_time"));
                    String[] date = datetime.split(" ");
                    ptPojo.setDate_day(date[0]);
                    ptPojo.setDate_hour(date[1]);
                    if (json == null || "1003".equals(json.get("code")) || "1001".equals(json.get("code"))) {
                        ptPojo.setResponse_bid_fail_num(1);

                    } else if ("1002".equals(json.get("code"))) {
                        String data = json.get("data").toString();
                        List<Map> listData = JSONArray.parseArray(data, Map.class);
                        String key = listData.get(0).get("ad_url").toString();
                        ptPojo.setResponse_bid_success_num(1);
                        bdPojo.setResponse_bid_fail_num(1);
                        bdPojo.setAd_customer_id("0");
                        if (staticMap.get(key) != null) {
                            ptPojo.setAd_customer_id(staticMap.get(key).toString());
                        }

                    }

                }
//                }

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
            String path = sourcefiles[i].getAbsolutePath();
            String fileName = sourcefiles[i].getAbsoluteFile().getName();
            String ileDayAndHour = getFileDayAndHour(fileName);
            String dayAndHour = getDayAndHour();
            long time = timeDifference(dayAndHour, ileDayAndHour);
            if (time > 0) {
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
                try {
                    moveFile(path, fileName, env.getProperty("newpath"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
            Map<String, String> allMap = shardedJedis.hgetAll("report_log");
            if (map != null && map.size() > 0) {//放进redis
                for (Map.Entry<String, Set<Object>> entry : map.entrySet()) {
                    String key = entry.getKey();
                    Set<Object> value = entry.getValue();
                    if (allMap.get(key) != null) {//如何已经存在，则融合后再插入
                        String str = allMap.get(key);
                        List<Object> list = JSON.parseArray(str, Object.class);
                        Set<Object> set = new HashSet<>(list);
                        map.get(key).addAll(set);
                        sp.hset("report_log", key, JSON.toJSONString(value));
                    } else {//不存在，直接插入
                        sp.hset("report_log", key, JSON.toJSONString(value));
                    }
                }
                sp.sync();
                shardedJedis.hset("heartbeat", "report_log", "存放广告产生的日志" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            }
        } finally {
            shardedJedis.close();
        }

    }


    /**
     * 将处理过的文件移除
     *
     * @param path
     * @param oldFileName
     * @param newFilePath
     * @throws IOException
     */
    private void moveFile(String path, String oldFileName, String newFilePath) throws IOException {

        Path fromPath = Paths.get(path); //   相当于 c:\test\a.txt  a.txt为需要复制的文件
        File storeFile = new File(newFilePath + File.separator + new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        if (!storeFile.exists()) {
            storeFile.mkdirs();
        }
        Path toPath = Paths.get(storeFile + File.separator + oldFileName);  //    相当于 c:\test1\b.txt 。 b.txt无需存在
        Files.move(fromPath, toPath); //移动文件（即复制并删除源文件）
    }

    /**
     * 获取要去读这个文件以及这个文件以前的时间的文件
     *
     * @return
     */
    private String getDayAndHour() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 0);//控制分
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

    private String getDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    /**
     * 分钟差
     *
     * @param start
     * @param end
     * @return
     */
    private int getMinute(String start, String end) {
        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long from = 0;
        long to = 0;
        int minutes = 0;
        try {
            from = simpleFormat.parse(start).getTime();
            to = simpleFormat.parse(end).getTime();
            minutes = (int) ((to - from) / (1000 * 60));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return minutes;
    }

    private String updateFormat(String time) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            cal.setTime(sf.parse(time));//开始时间
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat retrunsf = new SimpleDateFormat("yyyy-MM-dd HH");
        String datetime = retrunsf.format(cal.getTime());
        return datetime;
    }

    /**
     * starttime-endTime >0
     * 判断要拿的文件
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

    private String getFileDayAndHour(String fileName) {
        String[] files = fileName.split("_");
        return files[2].replace(".log", "");
    }

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
//        Map<String, Integer> map = new HashMap<>();
//        System.err.println(map.get("123"));
//        String end = "2018-05-01 12:33:55";
//        String start = "2018-05-01 12:00:21";
//        System.err.println(getMinute(start, end));
//        System.err.println(getDateTime());

//        String fileName = "adapi_192.168.10.140_2018-05-08-18-28.log";
//        String[] files = fileName.split("_");
//        System.err.println(files[2].replace(".log", ""));

    }

}
