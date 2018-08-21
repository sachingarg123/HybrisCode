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
import de.hybris.platform.acceleratorservices.order.strategies.impl.CustomerServiceUncollectedConsignmentStrategy;
import de.hybris.platform.acceleratorservices.process.email.context.AbstractEmailContext;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commercefacades.order.data.ConsignmentData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commerceservices.enums.CustomerType;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.i18n.FormatFactory;

import java.text.DateFormat;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * Velocity context for a Consignment Collection reminder email.
 */
public class ConsignmentCollectionReminderEmailContext extends AbstractEmailContext<ConsignmentProcessModel>
{
	private static final Logger LOG = Logger.getLogger(ConsignmentCollectionReminderEmailContext.class);//NOPMD

	private Converter<ConsignmentModel, ConsignmentData> consignmentConverter;
	private CustomerServiceUncollectedConsignmentStrategy customerServiceUncollectedConsignmentStrategy;
	private FormatFactory formatFactory;
	private ConsignmentData consignmentData;
	private String orderCode;
	private String orderGuid;
	private String pickUpDate;
	private boolean guest;
	private Converter<OrderModel, OrderData> orderConverter;
	private OrderData orderData;

	/**
	 * To initiate Consignment Collection reminder email
	 *
	 * @param consignmentProcessModel
	 * @param emailPageModel
	 */
	@Override
	public void init(final ConsignmentProcessModel consignmentProcessModel, final EmailPageModel emailPageModel)
	{
		super.init(consignmentProcessModel, emailPageModel);
		orderCode = consignmentProcessModel.getConsignment().getOrder().getCode();
		orderGuid = consignmentProcessModel.getConsignment().getOrder().getGuid();
		consignmentData = getConsignmentConverter().convert(consignmentProcessModel.getConsignment());
		guest = CustomerType.GUEST.equals(getCustomer(consignmentProcessModel).getType());
		final DateFormat dateFormat = getFormatFactory().createDateTimeFormat(DateFormat.MEDIUM, -1);
		pickUpDate = dateFormat.format(DateUtils.addHours(consignmentProcessModel.getConsignment().getShippingDate(),
				getCustomerServiceUncollectedConsignmentStrategy().getTimeThreshold().intValue()));
		orderData = getOrderConverter().convert((OrderModel) consignmentProcessModel.getConsignment().getOrder());
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Initialized with order code " + orderCode);
		}
	}

	/**
	 * To get site
	 *
	 * @param consignmentProcessModel
	 * @return
	 */
	@Override
	protected BaseSiteModel getSite(final ConsignmentProcessModel consignmentProcessModel)
	{
		return consignmentProcessModel.getConsignment().getOrder().getSite();
	}

	/**
	 * To get customer
	 *
	 * @param consignmentProcessModel
	 * @return
	 */
	@Override
	protected CustomerModel getCustomer(final ConsignmentProcessModel consignmentProcessModel)
	{
		return (CustomerModel) consignmentProcessModel.getConsignment().getOrder().getUser();
	}

	/**
	 * To get email language
	 *
	 * @param consignmentProcessModel
	 * @return
	 */
	@Override
	protected LanguageModel getEmailLanguage(final ConsignmentProcessModel consignmentProcessModel)
	{
		if (consignmentProcessModel.getConsignment().getOrder() instanceof OrderModel)
		{
			return ((OrderModel) consignmentProcessModel.getConsignment().getOrder()).getLanguage();
		}

		return null;
	}

	/**
	 * To get consignment
	 *
	 * @return consignmentData
	 */
	public ConsignmentData getConsignment()
	{
		return consignmentData;
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
	 * To get order Guid
	 *
	 * @return orderGuid
	 */
	public String getOrderGuid()
	{
		return orderGuid;
	}

	/**
	 * Checks guest or not
	 *
	 * @return guest
	 */
	public boolean isGuest()
	{
		return guest;
	}

	/**
	 * To get pickupdate
	 *
	 * @return pickUpDate
	 */
	public String getPickUpDate()
	{
		return pickUpDate;
	}

	/**
	 * To get Consignment Converter
	 *
	 * @return consignmentConverter
	 */
	protected Converter<ConsignmentModel, ConsignmentData> getConsignmentConverter()
	{
		return consignmentConverter;
	}

	/**
	 * To set Consignment Converter
	 * 
	 * @param consignmentConverter
	 */
	@Required
	public void setConsignmentConverter(final Converter<ConsignmentModel, ConsignmentData> consignmentConverter)
	{
		this.consignmentConverter = consignmentConverter;
	}

	/**
	 * To get CustomerServiceUncollectedConsignmentStrategy
	 * 
	 * @return customerServiceUncollectedConsignmentStrategy
	 */
	protected CustomerServiceUncollectedConsignmentStrategy getCustomerServiceUncollectedConsignmentStrategy()
	{
		return customerServiceUncollectedConsignmentStrategy;
	}

	/**
	 * To set CustomerServiceUncollectedConsignmentStrategy
	 * 
	 * @param customerServiceUncollectedConsignmentStrategy
	 */
	@Required
	public void setCustomerServiceUncollectedConsignmentStrategy(
			final CustomerServiceUncollectedConsignmentStrategy customerServiceUncollectedConsignmentStrategy)
	{
		this.customerServiceUncollectedConsignmentStrategy = customerServiceUncollectedConsignmentStrategy;
	}

	/**
	 * To get format factory
	 * 
	 * @return formatFactory
	 */
	protected FormatFactory getFormatFactory()
	{
		return formatFactory;
	}

	/**
	 * To set format factory
	 * 
	 * @param formatFactory
	 */
	@Required
	public void setFormatFactory(final FormatFactory formatFactory)
	{
		this.formatFactory = formatFactory;
	}

	/**
	 * To get OrderConverter
	 * 
	 * @return orderConverter
	 */
	protected Converter<OrderModel, OrderData> getOrderConverter()
	{
		return orderConverter;
	}

	/**
	 * To set OrderConverter
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
}
