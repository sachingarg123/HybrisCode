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
package com.woolies.webservices.rest.mapping.converters;

import de.hybris.platform.basecommerce.enums.StockLevelStatus;
import de.hybris.platform.webservicescommons.mapping.WsDTOMapping;

import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;


/**
 * Bidirectional converter between {@link StockLevelStatus} and String
 */
@WsDTOMapping
public class StockLevelStatusConverter extends BidirectionalConverter<StockLevelStatus, String>
{
	/**
	 * This method converts given StockLevelStatus source object to string format
	 * 
	 * @param source
	 * @param destinationType
	 * @param mappingContext
	 * @return
	 */
	@Override
	public String convertTo(final StockLevelStatus source, final Type<String> destinationType, final MappingContext mappingContext)
	{
		return source.toString();
	}

	/**
	 * This method converts given string source object to StockLevelStatus format
	 * 
	 * @param source
	 * @param destinationType
	 * @param mappingContext
	 * @return
	 */
	@Override
	public StockLevelStatus convertFrom(final String source, final Type<StockLevelStatus> destinationType,
			final MappingContext mappingContext)
	{
		return StockLevelStatus.valueOf(source);
	}
}
