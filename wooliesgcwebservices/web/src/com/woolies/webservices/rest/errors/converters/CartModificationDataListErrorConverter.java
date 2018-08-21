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
package com.woolies.webservices.rest.errors.converters;

import de.hybris.platform.commercefacades.order.data.CartModificationData;
import de.hybris.platform.commercefacades.order.data.CartModificationDataList;
import de.hybris.platform.webservicescommons.dto.error.ErrorWsDTO;
import de.hybris.platform.webservicescommons.errors.converters.AbstractErrorConverter;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;


/**
 * Converts {@link CartModificationDataList} to a list of
 * {@link de.hybris.platform.webservicescommons.dto.error.ErrorWsDTO}.
 */
public class CartModificationDataListErrorConverter extends AbstractErrorConverter
{
	private CartModificationDataErrorConverter cartModificationDataErrorConverter;

	/**
	 *
	 * @param clazz
	 * @return
	 */
	@Override
	public boolean supports(final Class clazz)
	{
		return CartModificationDataList.class.isAssignableFrom(clazz);
	}

	/**
	 * This method populate the cart modification data error list
	 * 
	 * @param o
	 * @param webserviceErrorList
	 */
	@Override
	public void populate(final Object o, final List<ErrorWsDTO> webserviceErrorList)
	{
		final CartModificationDataList cartModificationList = (CartModificationDataList) o;
		for (final CartModificationData modificationData : cartModificationList.getCartModificationList())
		{
			getCartModificationDataErrorConverter().populate(modificationData, webserviceErrorList);
		}
	}

	/**
	 * Get CartModification DataError Converter
	 * 
	 * @return
	 */
	public CartModificationDataErrorConverter getCartModificationDataErrorConverter()
	{
		return cartModificationDataErrorConverter;
	}

	/**
	 * Set the CartModification DataError Converter
	 * 
	 * @param cartModificationDataErrorConverter
	 */
	@Required
	public void setCartModificationDataErrorConverter(final CartModificationDataErrorConverter cartModificationDataErrorConverter)
	{
		this.cartModificationDataErrorConverter = cartModificationDataErrorConverter;
	}
}
