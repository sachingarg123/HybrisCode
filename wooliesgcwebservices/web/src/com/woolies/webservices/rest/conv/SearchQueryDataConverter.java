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

import de.hybris.platform.commercefacades.search.data.SearchQueryData;
import de.hybris.platform.commerceservices.search.solrfacetsearch.data.SolrSearchQueryData;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.convert.converter.Converter;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;


/**
 * SearchQuery data converter renders a {@link SolrSearchQueryData} as
 *
 * <pre>
 * {@code
 * <currentQuery>a:relevance</currentQuery>
 * }
 * </pre>
 */
public class SearchQueryDataConverter extends AbstractRedirectableConverter
{
	private Converter<SolrSearchQueryData, SearchQueryData> solrSearchQueryEncoder;

	protected Converter<SolrSearchQueryData, SearchQueryData> getSolrSearchQueryEncoder()
	{
		return solrSearchQueryEncoder;
	}

	/**
	 * Sets SolrSearchQueryEncoder
	 * 
	 * @param solrSearchQueryEncoder
	 */
	@Required
	public void setSolrSearchQueryEncoder(final Converter<SolrSearchQueryData, SearchQueryData> solrSearchQueryEncoder)
	{
		this.solrSearchQueryEncoder = solrSearchQueryEncoder;
	}

	/**
	 * Checks given calss is convertible or not
	 * 
	 * @param type
	 * @return
	 */
	@Override
	public boolean canConvert(final Class type)
	{
		return type == SolrSearchQueryData.class;
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
		final String query = getSolrSearchQueryEncoder().convert((SolrSearchQueryData) source).getValue();
		writer.setValue(query);
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
	 * Get converted class
	 */
	@Override
	public Class getConvertedClass()
	{
		return SolrSearchQueryData.class;
	}
}
