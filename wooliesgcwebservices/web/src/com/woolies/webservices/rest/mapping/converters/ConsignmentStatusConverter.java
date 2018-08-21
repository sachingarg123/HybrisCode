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

import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.webservicescommons.mapping.WsDTOMapping;

import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;


/**
 * Bidirectional converter between {@link ConsignmentStatus} and String
 */
@WsDTOMapping
public class ConsignmentStatusConverter extends BidirectionalConverter<ConsignmentStatus, String>
{
	/**
	 * This method converts given string object source to ConsignmentStatus object
	 *
	 * @param source
	 * @param destinationType
	 * @param mappingContext
	 * @return
	 */
	@Override
	public ConsignmentStatus convertFrom(final String source, final Type<ConsignmentStatus> destinationType,
			final MappingContext mappingContext)
	{
		return ConsignmentStatus.valueOf(source);
	}

	/**
	 * This method converts given source to string format
	 *
	 * @param source
	 * @param destinationTyp
	 * @param mappingContexte
	 * @return
	 */
	@Override
	public String convertTo(final ConsignmentStatus source, final Type<String> destinationTyp,
			final MappingContext mappingContexte)
	{
		return source.toString();
	}
}
