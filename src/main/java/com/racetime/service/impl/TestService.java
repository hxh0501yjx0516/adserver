package com.racetime.service.impl;

import com.racetime.dao.TestDao;
import com.racetime.service.ITestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author hu_xuanhua_hua
 * @ClassName: TestService
 * @Description: TODO
 * @date 2018-04-17 10:09
 * @versoin 1.0
 **/
@Service
public class TestService implements ITestService {
    @Autowired
    private TestDao testDao;

    @Override
    public List getMaterial() {
        List<String> materialList = testDao.getMaterial();
        return materialList;
    }
}
