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
package com.woolies.webservices.rest.mapping.mappers;

import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.order.data.CardTypeData;
import de.hybris.platform.commercewebservicescommons.dto.order.CardTypeWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import de.hybris.platform.webservicescommons.mapping.mappers.AbstractCustomMapper;

import ma.glasnost.orika.MappingContext;


/**
 * This classs is CC paymet info data mapper
 */
public class CCPaymentInfoDataMapper extends AbstractCustomMapper<CCPaymentInfoData, PaymentDetailsWsDTO>
{
	@Override
	public void mapAtoB(final CCPaymentInfoData a, final PaymentDetailsWsDTO b, final MappingContext context)
	{
		// other fields are mapped automatically
		mapCartTypeAtoB(a, b, context);
		mapdDefaultPaymentAtoB(a, b, context);
	}

	/**
	 * This method maps CartTypeA to cartypeB
	 *
	 * @param a
	 * @param b
	 * @param context
	 */
	protected void mapCartTypeAtoB(final CCPaymentInfoData a, final PaymentDetailsWsDTO b, final MappingContext context)
	{
		context.beginMappingField("cardType", getAType(), a, "cardType", getBType(), b);
		try
		{
			if (shouldMap(a, b, context))
			{
				if (a.getCardTypeData() != null && a.getCardTypeData().getCode() != null)
				{
					b.setCardType(mapperFacade.map(a.getCardTypeData(), CardTypeWsDTO.class, context));
				}
				else if (a.getCardType() != null)
				{
					final CardTypeWsDTO cardType = new CardTypeWsDTO();
					cardType.setCode(a.getCardType());
					b.setCardType(cardType);
				}
			}
		}
		finally
		{
			context.endMappingField();
		}
	}

	/**
	 * This method is used to map default payment from A to B
	 * 
	 * @param a
	 * @param b
	 * @param context
	 */
	protected void mapdDefaultPaymentAtoB(final CCPaymentInfoData a, final PaymentDetailsWsDTO b, final MappingContext context)
	{
		context.beginMappingField("defaultPaymentInfo", getAType(), a, "defaultPayment", getBType(), b);
		try
		{
			if (shouldMap(a, b, context))
			{
				if (a.isDefaultPaymentInfo())
				{
					b.setDefaultPayment(Boolean.TRUE);
				}
				else
				{
					b.setDefaultPayment(Boolean.FALSE);
				}
			}
		}
		finally
		{
			context.endMappingField();
		}
	}

	/**
	 * This method is used to map payment details from B to A
	 * 
	 * @param b
	 * @param a
	 * @param context
	 */
	@Override
	public void mapBtoA(final PaymentDetailsWsDTO b, final CCPaymentInfoData a, final MappingContext context)
	{
		// other fields are mapped automatically

		mapCartTypeBtoA(b, a, context);
		mapDefaultPaymentBtoA(b, a, context);
	}

	/**
	 * This method is used to map cart type from A to B
	 * 
	 * @param b
	 * @param a
	 * @param context
	 */
	protected void mapCartTypeBtoA(final PaymentDetailsWsDTO b, final CCPaymentInfoData a, final MappingContext context)
	{
		context.beginMappingField("cardType", getBType(), b, "cardType", getAType(), a);
		try
		{
			if (shouldMap(b, a, context) && b.getCardType() != null)
			{
				a.setCardType(b.getCardType().getCode());
				a.setCardTypeData(mapperFacade.map(b.getCardType(), CardTypeData.class, context));
			}
		}
		finally
		{
			context.endMappingField();
		}
	}

	/**
	 * This method is used to map default payment from B to A
	 * 
	 * @param b
	 * @param a
	 * @param context
	 */
	protected void mapDefaultPaymentBtoA(final PaymentDetailsWsDTO b, final CCPaymentInfoData a, final MappingContext context)
	{
		context.beginMappingField("defaultPayment", getBType(), b, "defaultPaymentInfo", getAType(), a);
		try
		{
			if (shouldMap(b, a, context) && b.getDefaultPayment() != null)
			{
				a.setDefaultPaymentInfo(b.getDefaultPayment().booleanValue());
			}
		}
		finally
		{
			context.endMappingField();
		}
	}
}
