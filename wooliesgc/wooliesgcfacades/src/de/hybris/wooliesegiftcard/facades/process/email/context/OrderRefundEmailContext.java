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
package de.hybris.wooliesegiftcard.facades.process.email.context;

import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel;
import de.hybris.platform.acceleratorservices.process.email.context.AbstractEmailContext;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.product.data.PriceData;
import de.hybris.platform.commerceservices.enums.CustomerType;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import org.springframework.beans.factory.annotation.Required;


/**
 * Velocity context for a order refund email.
 */
public class OrderRefundEmailContext extends AbstractEmailContext<OrderProcessModel>
{
	private Converter<OrderModel, OrderData> orderConverter;
	private OrderData orderData;
	private String orderCode;
	private String orderGuid;
	private boolean guest;
	private String storeName;
	private PriceData refundAmount;

	/**
	 * This method is used to initiate order refund email
	 * 
	 * @param orderProcessModel
	 * @param emailPageModel
	 */
	@Override
	public void init(final OrderProcessModel orderProcessModel, final EmailPageModel emailPageModel)
	{
		super.init(orderProcessModel, emailPageModel);
		orderData = getOrderConverter().convert(orderProcessModel.getOrder());
		orderCode = orderProcessModel.getOrder().getCode();
		orderGuid = orderProcessModel.getOrder().getGuid();
		guest = CustomerType.GUEST.equals(getCustomer(orderProcessModel).getType());
		storeName = orderProcessModel.getOrder().getStore().getName();
		orderData = getOrderConverter().convert(orderProcessModel.getOrder());
		refundAmount = orderData.getTotalPrice();
	}

	/**
	 * To get site detials
	 * 
	 * @param orderProcessModel
	 * @return
	 */
	@Override
	protected BaseSiteModel getSite(final OrderProcessModel orderProcessModel)
	{
		return orderProcessModel.getOrder().getSite();
	}

	/**
	 * To get customer details
	 * 
	 * @param orderProcessModel
	 * @return CustomerModel
	 */
	@Override
	protected CustomerModel getCustomer(final OrderProcessModel orderProcessModel)
	{
		return (CustomerModel) orderProcessModel.getOrder().getUser();
	}

	/**
	 * To get order converter
	 * 
	 * @return orderConverter
	 */
	protected Converter<OrderModel, OrderData> getOrderConverter()
	{
		return orderConverter;
	}

	/**
	 * To set order converter
	 * 
	 * @param orderConverter
	 */
	@Required
	public void setOrderConverter(final Converter<OrderModel, OrderData> orderConverter)
	{
		this.orderConverter = orderConverter;
	}

	/**
	 * To get order
	 * 
	 * @return orderData
	 */
	public OrderData getOrder()
	{
		return orderData;
	}

	/**
	 * To get Email language
	 * 
	 * @param orderProcessModel
	 * @return
	 */
	@Override
	protected LanguageModel getEmailLanguage(final OrderProcessModel orderProcessModel)
	{
		return orderProcessModel.getOrder().getLanguage();
	}

	/**
	 * To get order data
	 * 
	 * @return orderData
	 */
	public OrderData getOrderData()
	{
		return orderData;
	}

	/**
	 * To set order data
	 * 
	 * @param orderData
	 */
	public void setOrderData(final OrderData orderData)
	{
		this.orderData = orderData;
	}

	/**
	 * To get order code
	 * 
	 * @return orderCode
	 */
	public String getOrderCode()
	{
		return orderCode;
	}

	/**
	 * To set order code
	 * 
	 * @param orderCode
	 */
	public void setOrderCode(final String orderCode)
	{
		this.orderCode = orderCode;
	}

	/**
	 * To get order guid
	 * 
	 * @return orderGuid
	 */
	public String getOrderGuid()
	{
		return orderGuid;
	}

	/**
	 * To set order guid
	 * 
	 * @param orderGuid
	 */
	public void setOrderGuid(final String orderGuid)
	{
		this.orderGuid = orderGuid;
	}

	/**
	 * To check guest
	 * 
	 * @return guest
	 */
	public boolean isGuest()
	{
		return guest;
	}

	/**
	 * To set guest
	 * 
	 * @param guest
	 */
	public void setGuest(final boolean guest)
	{
		this.guest = guest;
	}

	/**
	 * To store name
	 * 
	 * @return storeName
	 */
	public String getStoreName()
	{
		return storeName;
	}

	/**
	 * To set store name
	 * 
	 * @param storeName
	 */
	public void setStoreName(final String storeName)
	{
		this.storeName = storeName;
	}

	/**
	 * To refund amount
	 * 
	 * @return refundAmount
	 */
	public PriceData getRefundAmount()
	{
		return refundAmount;
	}
}
