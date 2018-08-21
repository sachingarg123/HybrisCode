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
import de.hybris.platform.commercefacades.order.data.ConsignmentEntryData;
import de.hybris.platform.commercefacades.product.PriceDataFactory;
import de.hybris.platform.commercefacades.product.data.PriceData;
import de.hybris.platform.commerceservices.enums.CustomerType;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Required;


/**
 * Velocity context for email notification about order consignment.
 */
/**
 * @author 347422
 *
 */
public class NotPickedUpConsignmentCanceledEmailContext extends AbstractEmailContext<ConsignmentProcessModel>
{
	private Converter<ConsignmentModel, ConsignmentData> consignmentConverter;
	private PriceDataFactory priceDataFactory;
	private ConsignmentData consignmentData;
	private String orderCode;
	private String orderGuid;
	private boolean guest;
	private PriceData refundAmount;

	/**
	 * To initiate not picked up consignment canceled email
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
		calculateRefundAmount();
	}

	/**
	 * To calcuate refund amount
	 */
	protected void calculateRefundAmount()
	{
		BigDecimal refundAmountValue = BigDecimal.ZERO;
		PriceData totalPrice = null;
		for (final ConsignmentEntryData consignmentEntry : getConsignment().getEntries())
		{
			totalPrice = consignmentEntry.getOrderEntry().getTotalPrice();
			refundAmountValue = refundAmountValue.add(totalPrice.getValue());
		}

		if (totalPrice != null)
		{
			refundAmount = getPriceDataFactory().create(totalPrice.getPriceType(), refundAmountValue, totalPrice.getCurrencyIso());
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
	 * To get Customer
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
	 * To get ConsignmentConverter
	 * 
	 * @return consignmentConverter
	 */
	protected Converter<ConsignmentModel, ConsignmentData> getConsignmentConverter()
	{
		return consignmentConverter;
	}

	/**
	 * To set ConsignmentConverter
	 * 
	 * @param consignmentConverter
	 */
	@Required
	public void setConsignmentConverter(final Converter<ConsignmentModel, ConsignmentData> consignmentConverter)
	{
		this.consignmentConverter = consignmentConverter;
	}

	/**
	 * To get Consignment
	 * 
	 * @return consignmentData
	 */
	public ConsignmentData getConsignment()
	{
		return consignmentData;
	}

	/**
	 * To get PriceDataFactory
	 * 
	 * @return priceDataFactory
	 */
	protected PriceDataFactory getPriceDataFactory()
	{
		return priceDataFactory;
	}

	/**
	 * To set PriceDataFactory
	 * 
	 * @param priceDataFactory
	 */
	@Required
	public void setPriceDataFactory(final PriceDataFactory priceDataFactory)
	{
		this.priceDataFactory = priceDataFactory;
	}

	/**
	 * To get OrderCode
	 * 
	 * @return orderCode
	 */
	public String getOrderCode()
	{
		return orderCode;
	}

	/**
	 * To get OrderGuid
	 * 
	 * @return orderGuid
	 */
	public String getOrderGuid()
	{
		return orderGuid;
	}

	/**
	 *
	 * @return guest
	 */
	public boolean isGuest()
	{
		return guest;
	}

	/**
	 *
	 * @return EmailLanguage
	 */
	public PriceData getRefundAmount()
	{
		return refundAmount;
	}

	/**
	 * To get EmailLanguage
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
