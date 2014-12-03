package com.chu.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chu.dao.domain.Test;
import com.chu.dao.domain.TestExample;
import com.chu.dao.mapper.TestMapper;

@Service
public class TestService {

	@Autowired
	private TestMapper testMapper;
	
	public Test findById(Integer id) {
		TestExample c = new TestExample();
		TestExample.Criteria cr = c.createCriteria();
		cr.andIdEqualTo(id);
		List<Test> tests = testMapper.selectByExample(c);
		Test test = tests.get(0);
		return test;
	}
	
	@Transactional
	public int save(Test test) {
		int i = testMapper.insertSelective(test);
		return i;
	}
}
