package com.zeek.mybatis.mapper;

import java.io.InputStream;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

import com.zeek.mybatis3.mapper.OrdersMapperCustom;
import com.zeek.mybatis3.mapper.UserMapper;
import com.zeek.mybatis3.po.Orders;
import com.zeek.mybatis3.po.OrdersCustom;
import com.zeek.mybatis3.po.User;

public class OrdersMapperCustomTest {

	private SqlSessionFactory sqlSessionFactory;

	// 此方法是在执行testFindUserById之前执行
	@Before
	public void setUp() throws Exception {
		// 创建sqlSessionFactory

		// mybatis配置文件
		String resource = "SqlMapConfig.xml";
		// 得到配置文件流
		InputStream inputStream = Resources.getResourceAsStream(resource);

		// 创建会话工厂，传入mybatis的配置文件信息
		sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
	}

	@Test
	public void testFindOrdersUser() throws Exception {

		SqlSession sqlSession = sqlSessionFactory.openSession();
		// 创建代理对象
		OrdersMapperCustom ordersMapperCustom = sqlSession
				.getMapper(OrdersMapperCustom.class);

		// 调用maper的方法
		List<OrdersCustom> list = ordersMapperCustom.findOrdersUser();

		System.out.println(list);
		System.out.println(list.size());

		sqlSession.close();
	}
	
	@Test
	public void testFindOrdersUserResultMap() throws Exception {

		SqlSession sqlSession = sqlSessionFactory.openSession();
		// 创建代理对象
		OrdersMapperCustom ordersMapperCustom = sqlSession
				.getMapper(OrdersMapperCustom.class);

		// 调用maper的方法
		List<Orders> list = ordersMapperCustom.findOrdersUserResultMap();

		System.out.println(list);
		System.out.println(list.size());

		sqlSession.close();
	}
	
	@Test
	public void testFindOrdersAndOrderDetailResultMap() throws Exception {

		SqlSession sqlSession = sqlSessionFactory.openSession();
		// 创建代理对象
		OrdersMapperCustom ordersMapperCustom = sqlSession
				.getMapper(OrdersMapperCustom.class);

		// 调用maper的方法
		List<Orders> list = ordersMapperCustom
				.findOrdersAndOrderDetailResultMap();

		System.out.println(list);

		sqlSession.close();
	}
	
	@Test
	public void testFindUserAndItemsResultMap() throws Exception {

		SqlSession sqlSession = sqlSessionFactory.openSession();
		// 创建代理对象
		OrdersMapperCustom ordersMapperCustom = sqlSession
				.getMapper(OrdersMapperCustom.class);

		// 调用maper的方法
		List<User> list = ordersMapperCustom.findUserAndItemsResultMap();

		System.out.println(list);

		sqlSession.close();
	}

	// 查询订单关联查询用户，用户信息使用延迟加载
	@Test
	public void testFindOrdersUserLazyLoading() throws Exception {
		SqlSession sqlSession = sqlSessionFactory.openSession();// 创建代理对象
		OrdersMapperCustom ordersMapperCustom = sqlSession
				.getMapper(OrdersMapperCustom.class);
		// 查询订单信息（单表）
		List<Orders> list = ordersMapperCustom.findOrdersUserLazyLoading();

		// 遍历上边的订单列表
		for (Orders orders : list) {
			// 执行getUser()去查询用户信息，这里实现按需加载
			User user = orders.getUser();
			System.out.println(user);
		}

	}
	
	// 一级缓存测试
	@Test
	public void testCache1() throws Exception {
		SqlSession sqlSession = sqlSessionFactory.openSession();// 
		UserMapper userMapper = sqlSession.getMapper(UserMapper.class);

		// 下边查询使用一个SqlSession
		// 第一次发起请求，查询id为1的用户
		User user1 = userMapper.findUserById(1);
		System.out.println(user1);

		// 如果sqlSession去执行commit操作（执行插入、更新、删除），清空SqlSession中的一级缓存，这样做的目的为了让缓存中存储的是最新的信息，避免脏读。
		// 更新user1的信息
		 user1.setUsername("王五3");
		 userMapper.updateUser(user1);//此处的更新只是更新了一级缓存中的相应的数据信息
		// 执行commit操作去清空缓存
		sqlSession.commit();

		// 第二次发起请求，查询id为1的用户
		User user2 = userMapper.findUserById(1);//由于sqlSession调用的是commit操作而不是close方法（若是调用close方法话，会将数据放置到二级缓存当中）
												//还是会发送sql到数据库中进行
		System.out.println(user2);

		sqlSession.close();

	}
	
	// 二级缓存测试
	@Test
	public void testCache2() throws Exception {
		SqlSession sqlSession1 = sqlSessionFactory.openSession();
		SqlSession sqlSession2 = sqlSessionFactory.openSession();
		SqlSession sqlSession3 = sqlSessionFactory.openSession();
		// 创建代理对象
		UserMapper userMapper1 = sqlSession1.getMapper(UserMapper.class);
		// 第一次发起请求，查询id为1的用户
		User user1 = userMapper1.findUserById(1);
		System.out.println(user1);//username值为 【张明明2】
		
		//这里执行关闭操作，将sqlsession中的数据写到二级缓存区域
		sqlSession1.close();
		
		
			//使用sqlSession3执行commit()操作
			UserMapper userMapper3 = sqlSession3.getMapper(UserMapper.class);
			User user  = userMapper3.findUserById(1);
			user.setUsername("张明明3");
			userMapper3.updateUser(user);
			//执行提交，清空UserMapper下边的二级缓存
			sqlSession3.commit();//此处提交的事务，数据库中的值为 【张明明3】
			sqlSession3.close();
		
		

		UserMapper userMapper2 = sqlSession2.getMapper(UserMapper.class);
		// 第二次发起请求，查询id为1的用户
		User user2 = userMapper2.findUserById(1);//由于在UserMapper.xml中 id为updateUser的statement设置了flushCache="false"，即
												 //<update id="updateUser" parameterType="com.zeek.mybatis3.po.User" flushCache="false">(在配置文件中没有设置此属性，若需要测试的话，需要再加上)
												 //更新用户信息时，不更新相应的二级缓存的信息，此时二级缓存中的信息还为【张明明2】，因此此时userMapper2查询出的id为1的用户的信息的名称
												 //的值还是为 【张明明2】，出现脏数据
		System.out.println(user2);

		sqlSession2.close();

	}
}
