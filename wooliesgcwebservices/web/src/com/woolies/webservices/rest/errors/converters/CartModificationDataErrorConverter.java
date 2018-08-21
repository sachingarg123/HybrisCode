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
import de.hybris.platform.commerceservices.i18n.CommerceCommonI18NService;
import de.hybris.platform.commerceservices.order.CommerceCartModificationStatus;
import de.hybris.platform.webservicescommons.dto.error.ErrorWsDTO;
import de.hybris.platform.webservicescommons.errors.converters.AbstractLocalizedErrorConverter;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Required;


/**
 * Converts {@link CartModificationData} to a list of
 * {@link de.hybris.platform.webservicescommons.dto.error.ErrorWsDTO}.
 */
public class CartModificationDataErrorConverter extends AbstractLocalizedErrorConverter
{
	public static final String TYPE = "InsufficientStockError";
	public static final String SUBJECT_TYPE = "entry";
	public static final String NO_STOCK = "noStock";
	public static final String LOW_STOCK = "lowStock";
	public static final String NO_STOCK_MESSAGE = "cart.noStock";
	public static final String LOW_STOCK_MESSAGE = "cart.noStock";
	private CommerceCommonI18NService commerceCommonI18NService;

	@Override
	public boolean supports(final Class clazz)
	{
		return CartModificationData.class.isAssignableFrom(clazz);
	}

	/**
	 * This method is used to populate web service error list
	 *
	 * @param o
	 * @param webserviceErrorList
	 */
	@Override
	public void populate(final Object o, final List<ErrorWsDTO> webserviceErrorList)
	{
		final CartModificationData cartModification = (CartModificationData) o;
		final Locale currentLocale = commerceCommonI18NService.getCurrentLocale();

		final ErrorWsDTO errorDto = createTargetElement();
		errorDto.setType(TYPE);
		errorDto.setSubjectType(SUBJECT_TYPE);
		errorDto.setSubject(cartModification.getEntry().getEntryNumber().toString());

		if (CommerceCartModificationStatus.NO_STOCK.equals(cartModification.getStatusCode()))
		{
			errorDto.setReason(NO_STOCK);
			errorDto.setMessage(getNoStockMessage(cartModification, currentLocale));
		}
		else
		{
			errorDto.setReason(LOW_STOCK);
			errorDto.setMessage(getLowStockMessage(cartModification, currentLocale));
		}

		webserviceErrorList.add(errorDto);
	}

	/**
	 * Get no stock message
	 * 
	 * @param cartModification
	 * @param locale
	 * @return
	 */
	protected String getNoStockMessage(final CartModificationData cartModification, final Locale locale)
	{
		final Object[] args = new Object[]
		{ cartModification.getEntry().getProduct().getCode(), cartModification.getEntry().getEntryNumber() };
		return getMessage(NO_STOCK_MESSAGE, args, locale);
	}

	/**
	 * Get low stock message
	 * 
	 * @param cartModification
	 * @param locale
	 * @return
	 */
	protected String getLowStockMessage(final CartModificationData cartModification, final Locale locale)
	{
		final Object[] args = new Object[]
		{ cartModification.getEntry().getProduct().getCode(), cartModification.getEntry().getEntryNumber(),
				Long.valueOf(cartModification.getQuantityAdded()) };
		return getMessage(LOW_STOCK_MESSAGE, args, locale);
	}

	/**
	 * set the CommerceCommonI18NService
	 * 
	 * @param commerceCommonI18NService
	 */
	@Required
	public void setCommerceCommonI18NService(final CommerceCommonI18NService commerceCommonI18NService)
	{
		this.commerceCommonI18NService = commerceCommonI18NService;
	}
}
