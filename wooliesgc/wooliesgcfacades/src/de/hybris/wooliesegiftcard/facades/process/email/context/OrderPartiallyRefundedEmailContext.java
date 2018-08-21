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
import de.hybris.platform.acceleratorservices.orderprocessing.model.OrderModificationProcessModel;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.data.PriceData;

import java.math.BigDecimal;
import java.util.List;



/**
 * Velocity context for email about partially order refund
 */
public class OrderPartiallyRefundedEmailContext extends OrderPartiallyModifiedEmailContext
{
	private PriceData refundAmount;

	/**
	 * This meethod initiates email partially order refund
	 */
	@Override
	public void init(final OrderModificationProcessModel orderProcessModel, final EmailPageModel emailPageModel)
	{
		super.init(orderProcessModel, emailPageModel);
		calculateRefundAmount();
	}

	/**
	 * This method is used to calculate refunded amount
	 */
	protected void calculateRefundAmount()
	{
		BigDecimal refundAmountValue = BigDecimal.ZERO;
		PriceData totalPrice = null;
		if (getRefundedEntries() != null && !getRefundedEntries().isEmpty())
		{
			for (final OrderEntryData entryData : getRefundedEntries())
			{
				totalPrice = entryData.getTotalPrice();
				refundAmountValue = refundAmountValue.add(totalPrice.getValue());
			}
		}

		if (totalPrice != null)
		{
			refundAmount = getPriceDataFactory().create(totalPrice.getPriceType(), refundAmountValue, totalPrice.getCurrencyIso());
		}
	}

	/**
	 * To get refunded entries
	 * 
	 * @return OrderEntryData list
	 */
	public List<OrderEntryData> getRefundedEntries()
	{
		return super.getModifiedEntries();
	}

	/**
	 * To get refund amount
	 * 
	 * @return refundAmount
	 */
	public PriceData getRefundAmount()
	{
		return refundAmount;
	}
}
