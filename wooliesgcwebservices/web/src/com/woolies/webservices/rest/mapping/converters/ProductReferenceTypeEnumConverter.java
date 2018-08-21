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


import de.hybris.platform.catalog.enums.ProductReferenceTypeEnum;
import de.hybris.platform.webservicescommons.mapping.WsDTOMapping;

import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;


/**
 * Bidirectional converter between {@link ProductReferenceTypeEnum} and String
 */
@WsDTOMapping
public class ProductReferenceTypeEnumConverter extends BidirectionalConverter<ProductReferenceTypeEnum, String>
{
	/**
	 * This method converts given string object source to ProductReferenceTypeEnum object
	 * 
	 * @param source
	 * @param destinationType
	 * @param mappingContext
	 * @return
	 */
	@Override
	public ProductReferenceTypeEnum convertFrom(final String source, final Type<ProductReferenceTypeEnum> destinationType,
			final MappingContext mappingContext)
	{
		return ProductReferenceTypeEnum.valueOf(source);
	}

	/**
	 * This method converts given ProductReferenceTypeEnum source object to string format
	 * 
	 * @param source
	 * @param destinationType
	 * @param mappingContext
	 * @return
	 */
	@Override
	public String convertTo(final ProductReferenceTypeEnum source, final Type<String> destinationType,
			final MappingContext mappingContext)
	{
		return source.toString();
	}
}
