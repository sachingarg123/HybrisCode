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

import de.hybris.platform.commercefacades.order.data.OrderEntryData;

import java.util.List;



/**
 * Velocity context for email about partially order cancellation. This class is used to maintain Order Partially
 * Canceled Email Context
 */
public class OrderPartiallyCanceledEmailContext extends OrderPartiallyModifiedEmailContext
{

	/**
	 * This method is used to get canceled enteries
	 * 
	 * @return
	 */
	public List<OrderEntryData> getCanceledEntries()
	{
		return super.getModifiedEntries();
	}
}
