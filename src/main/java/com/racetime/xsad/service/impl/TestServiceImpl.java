package com.racetime.xsad.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.racetime.xsad.dao.TestDao;
import com.racetime.xsad.service.TestService;

@Service
@Transactional
public class TestServiceImpl implements TestService {

	@Autowired
	private TestDao testDao;

	@Override
	public void testService() {
		System.out.println("testService");
		
	}
	
	
}
