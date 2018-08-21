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

import java.util.Collection;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;


public class VoucherConverter extends AbstractRedirectableConverter
{
	/**
	 * Gets converted class
	 *
	 * @param type
	 * @return
	 */
	@Override
	public boolean canConvert(final Class type)
	{
		return getConvertedClass().isAssignableFrom(type);
	}

	/**
	 * This method is used to marshal the given object
	 *
	 * @param source
	 * @param writerOrig
	 * @param context
	 */
	@Override
	public void marshal(final Object source, final HierarchicalStreamWriter writerOrig, final MarshallingContext context)
	{
		final Collection<String> vouchersCode = (Collection) source;
		final ExtendedHierarchicalStreamWriter writer = (ExtendedHierarchicalStreamWriter) writerOrig.underlyingWriter();

		if (vouchersCode != null && !vouchersCode.isEmpty())
		{
			writer.startNode("appliedVouchers", String.class);
			for (final String voucherCode : vouchersCode)
			{
				writer.startNode("voucherCode", String.class);
				writer.setValue(voucherCode);
				writer.endNode();
			}
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
	 * Get the converted class
	 */
	@Override
	public Class getConvertedClass()
	{
		return Collection.class;
	}
}
