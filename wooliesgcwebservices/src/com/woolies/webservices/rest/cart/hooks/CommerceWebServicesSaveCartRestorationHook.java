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
package com.woolies.webservices.rest.cart.hooks;

import de.hybris.platform.commerceservices.order.CommerceCartRestorationException;
import de.hybris.platform.commerceservices.order.hook.CommerceSaveCartRestorationMethodHook;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.util.Config;

import org.apache.log4j.Logger;


/**
 * Hook to set the saveTime as null before performing the restore. By default it is enabled.
 */
public class CommerceWebServicesSaveCartRestorationHook implements CommerceSaveCartRestorationMethodHook
{
	private static final Logger LOG = Logger.getLogger(CommerceWebServicesSaveCartRestorationHook.class);

	/**
	 * This method is to Hook to set the saveTime as null before performing the restore
	 * 
	 * @param commerceCartParameter
	 * @throws CommerceCartRestorationException
	 */
	@Override
	public void beforeRestoringCart(final CommerceCartParameter commerceCartParameter) throws CommerceCartRestorationException
	{

		if (Config.getBoolean("wooliesgcwebservices.commercesavecart.restoration.savetime.hook.enabled", true))
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Setting saveTime as null on the cart to be restored");
			}
			final CartModel cartModel = commerceCartParameter.getCart();
			//Convert save cart to active cart
			cartModel.setSaveTime(null);
		}
	}

	@Override
	public void afterRestoringCart(final CommerceCartParameter commerceCartParameter) throws CommerceCartRestorationException
	{
		// Auto-generated method stub
	}
}
