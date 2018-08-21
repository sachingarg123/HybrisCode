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
package com.woolies.webservices.rest.conv;

import de.hybris.platform.commercefacades.user.data.TitleData;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.woolies.webservices.rest.user.data.TitleDataList;


/**
 * Specific converter for a {@link TitleDataList} object.
 */
public class TitleDataListConverter extends AbstractRedirectableConverter
{
	@Override
	public boolean canConvert(final Class type)
	{
		return type == getConvertedClass();
	}

	/**
	 * This method is used to marshal the given object
	 *
	 * @param source
	 * @param writer
	 * @param context
	 */
	@Override
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context)
	{
		final TitleDataList reviews = (TitleDataList) source;
		for (final TitleData rd : reviews.getTitles())
		{
			writer.startNode("title");
			context.convertAnother(rd);
			writer.endNode();
		}

	}

	/**
	 * This method is used to un marshal the given object
	 *
	 * @param reader
	 * @param context
	 * @return
	 */
	@Override
	public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context)
	{
		return getTargetConverter().unmarshal(reader, context);
	}

	/**
	 * Gets converted class
	 */
	@Override
	public Class getConvertedClass()
	{
		return TitleDataList.class;
	}


}
