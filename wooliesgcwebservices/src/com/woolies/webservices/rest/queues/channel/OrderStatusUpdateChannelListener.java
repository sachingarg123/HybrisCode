/*
 * [y] hybris Platform
 *
 * Copyright (c) 2017 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package com.woolies.webservices.rest.queues.channel;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import com.woolies.webservices.rest.queues.UpdateQueue;
import com.woolies.webservices.rest.queues.data.OrderStatusUpdateElementData;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

/**
 *
 * This class is Listener for Order status update channel
 *
 */
public class OrderStatusUpdateChannelListener
{
	private static final Logger LOG = Logger.getLogger(OrderStatusUpdateChannelListener.class);
	private UpdateQueue<OrderStatusUpdateElementData> orderStatusUpdateQueue;
	private Converter<OrderModel, OrderStatusUpdateElementData> orderStatusUpdateElementConverter;

	/**
	 * This method is used to add the order status update element data to the queue
	 * 
	 * @param order
	 */
	public void onMessage(final OrderModel order)
	{
		if (order != null)
		{
			LOG.debug("OrderStatusUpdateChannelListener got new status for order with code " + order.getCode());
			final OrderStatusUpdateElementData orderStatusUpdateElementData = getOrderStatusUpdateElementConverter().convert(order);
			if (orderStatusUpdateElementData != null)
			{
				getOrderStatusUpdateQueue().addItem(orderStatusUpdateElementData);
			}
		}
	}

	/**
	 * This method is used to get order status update queue object
	 * 
	 * @return orderStatusUpdateQueue
	 */
	public UpdateQueue<OrderStatusUpdateElementData> getOrderStatusUpdateQueue()
	{
		return orderStatusUpdateQueue;
	}

	/**
	 * This method is used to set order status update queue object
	 * 
	 * @param orderStatusUpdateQueue
	 */
	@Required
	public void setOrderStatusUpdateQueue(final UpdateQueue<OrderStatusUpdateElementData> orderStatusUpdateQueue)
	{
		this.orderStatusUpdateQueue = orderStatusUpdateQueue;
	}

	public Converter<OrderModel, OrderStatusUpdateElementData> getOrderStatusUpdateElementConverter()
	{
		return orderStatusUpdateElementConverter;
	}

	/**
	 * This method is used to set Order Status Update Element Converter object
	 * 
	 * @param orderStatusUpdateElementConverter
	 */
	@Required
	public void setOrderStatusUpdateElementConverter(
			final Converter<OrderModel, OrderStatusUpdateElementData> orderStatusUpdateElementConverter)
	{
		this.orderStatusUpdateElementConverter = orderStatusUpdateElementConverter;
	}

}
