package com.zeek.mybatis3.mapper;

import java.util.List;

import com.zeek.mybatis3.po.Orders;
import com.zeek.mybatis3.po.OrdersCustom;

/**
 * 
 * <p>Title: OrdersMapperCustom</p>
 * <p>Description: 订单mapper</p>
 * @date	2015-4-23上午10:28:43
 * @version 1.0
 */
public interface OrdersMapperCustom {
	
	//查询订单关联查询用户信息
	public List<OrdersCustom> findOrdersUser()throws Exception;
	
	//查询订单关联查询用户信息(使用resultMap)
	public List<Orders> findOrdersUserResultMap() throws Exception;

}
