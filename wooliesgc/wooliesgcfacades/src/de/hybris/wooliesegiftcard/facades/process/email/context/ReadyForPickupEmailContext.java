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
import de.hybris.platform.commercefacades.order.data.ConsignmentData;
import de.hybris.platform.commerceservices.enums.CustomerType;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import org.springframework.beans.factory.annotation.Required;


/**
 * Velocity context for a Ready For Pickup notification email.
 */
public class ReadyForPickupEmailContext extends AbstractEmailContext<ConsignmentProcessModel>
{
	private Converter<ConsignmentModel, ConsignmentData> consignmentConverter;
	private ConsignmentData consignmentData;
	private String orderCode;
	private String orderGuid;
	private boolean guest;

	/**
	 * This method is used to initiate Ready For Pickup notification email
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
	}

	/**
	 * To get site details
	 * 
	 * @param consignmentProcessModel
	 * @return BaseSiteModel
	 */
	@Override
	protected BaseSiteModel getSite(final ConsignmentProcessModel consignmentProcessModel)
	{
		return consignmentProcessModel.getConsignment().getOrder().getSite();
	}

	/**
	 * To get cutomer details
	 * 
	 * @param consignmentProcessModel
	 * @return CustomerModel
	 */
	@Override
	protected CustomerModel getCustomer(final ConsignmentProcessModel consignmentProcessModel)
	{
		return (CustomerModel) consignmentProcessModel.getConsignment().getOrder().getUser();
	}

	/**
	 * To get consignment converter
	 * 
	 * @return consignmentConverter
	 */
	protected Converter<ConsignmentModel, ConsignmentData> getConsignmentConverter()
	{
		return consignmentConverter;
	}

	/**
	 * To set consignment converter
	 * 
	 * @param consignmentConverter
	 */
	@Required
	public void setConsignmentConverter(final Converter<ConsignmentModel, ConsignmentData> consignmentConverter)
	{
		this.consignmentConverter = consignmentConverter;
	}

	/**
	 * To get consignment details
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
	 * To get order guid
	 * 
	 * @return orderGuid
	 */
	public String getOrderGuid()
	{
		return orderGuid;
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

}
