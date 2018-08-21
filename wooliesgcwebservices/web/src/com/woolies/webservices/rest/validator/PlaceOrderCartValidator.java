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
package com.woolies.webservices.rest.validator;

import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;

import java.util.List;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


/**
 * Default commerce web services cart validator. Checks if cart is calculated and if needed values are filled.
 */
public class PlaceOrderCartValidator implements Validator
{
	@Override
	public boolean supports(final Class<?> clazz)
	{
		return CartData.class.equals(clazz);
	}

	/**
	 * This method is used to Check if cart is calculated and if needed values are filled
	 *
	 * @param target
	 * @param errors
	 */
	@Override
	public void validate(final Object target, final Errors errors)
	{
		final CartData cart = (CartData) target;

		if (!cart.isCalculated())
		{
			errors.reject("cart.notCalculated");
		}

		final List<OrderEntryData> cartEntries = cart.getEntries();
		for (final OrderEntryData orderEntryData : cartEntries)
		{
			if (!orderEntryData.getIsEGiftCard() && cart.getDeliveryMode() == null)
			{
				errors.reject("cart.deliveryModeNotSet");
				break;
			}
		}

	}
}
