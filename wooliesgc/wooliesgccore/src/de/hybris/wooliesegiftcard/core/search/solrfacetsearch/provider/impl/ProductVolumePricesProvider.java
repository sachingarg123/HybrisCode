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
package de.hybris.wooliesegiftcard.core.search.solrfacetsearch.provider.impl;

import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.europe1.jalo.PriceRow;
import de.hybris.platform.jalo.order.price.PriceInformation;
import de.hybris.platform.product.PriceService;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.provider.FieldValue;
import de.hybris.platform.solrfacetsearch.provider.FieldValueProvider;
import de.hybris.platform.solrfacetsearch.provider.impl.AbstractPropertyFieldValueProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Required;


/**
 * Provides value for volumePrices flag. "true" if product has volume prices, "false" otherwise.
 */
public class ProductVolumePricesProvider extends AbstractPropertyFieldValueProvider implements FieldValueProvider
{
	private FieldNameProvider fieldNameProvider;
	private PriceService priceService;

	/**
	 * To provide field value for ProductVolumePricesProvider
	 *
	 * @param indexConfig
	 * @param indexedProperty
	 * @param model
	 * @return fieldValues
	 * @throws FieldValueProviderException
	 */
	@Override
	public Collection<FieldValue> getFieldValues(final IndexConfig indexConfig, final IndexedProperty indexedProperty,
			final Object model) throws FieldValueProviderException
	{
		final ProductModel product = (ProductModel) model;//this provider shall only be used with products
		final Collection<FieldValue> fieldValues = new ArrayList<FieldValue>();
		final CurrencyModel sessionCurrency = i18nService.getCurrentCurrency();
		try
		{
			for (final CurrencyModel currency : indexConfig.getCurrencies())
			{
				i18nService.setCurrentCurrency(currency);
				final List<PriceInformation> prices = getPriceService().getPriceInformationsForProduct(product);
				if (prices != null && !prices.isEmpty())
				{
					addFieldValues(indexedProperty, product, fieldValues, currency);
				}
			}
		}
		finally
		{
			i18nService.setCurrentCurrency(sessionCurrency);
		}
		return fieldValues;
	}

	/**
	 * To add field values for the given product
	 *
	 * @param indexedProperty
	 * @param product
	 * @param fieldValues
	 * @param currency
	 */
	private void addFieldValues(final IndexedProperty indexedProperty, final ProductModel product,
			final Collection<FieldValue> fieldValues, final CurrencyModel currency)
	{
		final Collection<String> fieldNames = getFieldNameProvider().getFieldNames(indexedProperty,
				currency.getIsocode().toLowerCase());
		final Boolean hasVolumePrices = hasVolumePrices(product);
		if (fieldNames != null)
		{
			for (final String fieldName : fieldNames)
			{
				fieldValues.add(new FieldValue(fieldName, hasVolumePrices));
			}
		}
	}

	/**
	 * To check volume prices for the product
	 *
	 * @param product
	 * @return has volume prices or not
	 */
	private Boolean hasVolumePrices(final ProductModel product)
	{
		final Set<Long> volumes = new HashSet<Long>();
		final List<PriceInformation> priceInfos = getPriceService().getPriceInformationsForProduct(product);
		if (priceInfos != null)
		{
			for (final PriceInformation priceInfo : priceInfos)
			{
				if (priceInfo.getQualifiers().containsKey(PriceRow.MINQTD))
				{
					final Long volume = (Long) priceInfo.getQualifiers().get(PriceRow.MINQTD);
					volumes.add(volume);
				}
			}
		}
		if (volumes.size() > 1)//one volume price (probably with minqt=1) is not taken into account
		{
			return Boolean.TRUE;
		}
		else
		{
			return Boolean.FALSE;
		}
	}

	/**
	 * To get price service
	 *
	 * @return priceService
	 */
	protected PriceService getPriceService()
	{
		return priceService;
	}

	/**
	 * Sets price service
	 * 
	 * @param priceService
	 */
	@Required
	public void setPriceService(final PriceService priceService)
	{
		this.priceService = priceService;
	}

	/**
	 * Get field name provider
	 * 
	 * @return fieldNameProvider
	 */
	protected FieldNameProvider getFieldNameProvider()
	{
		return fieldNameProvider;
	}

	/**
	 * Set field name provider
	 * 
	 * @param fieldNameProvider
	 */
	@Required
	public void setFieldNameProvider(final FieldNameProvider fieldNameProvider)
	{
		this.fieldNameProvider = fieldNameProvider;
	}
}