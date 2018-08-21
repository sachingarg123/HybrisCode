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
package de.hybris.wooliesegiftcard.facades.populators;

import de.hybris.platform.commercefacades.product.converters.populator.ProductPricePopulator;
import de.hybris.platform.commercefacades.product.data.PriceData;
import de.hybris.platform.commercefacades.product.data.PriceDataType;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;


/**
 * Populate the product data with the price information
 */
public class WooliesProductPricePopulator<SOURCE extends ProductModel, TARGET extends ProductData>
		extends ProductPricePopulator<SOURCE, TARGET>
{
	/**
	 * product data with the price information
	 * 
	 * @param productModel
	 * @param productData
	 * @throws ConversionException
	 */
	@Override
	public void populate(final SOURCE productModel, final TARGET productData) throws ConversionException
	{
		final List<Double> priceList = new ArrayList<Double>();
		final List<Double> zeroPrice = new ArrayList<Double>();
		final double zeroValue=0;
		
		final PriceData priceData = getPriceDataFactory().create(PriceDataType.BUY,
				BigDecimal.valueOf(productModel.getEurope1Prices().iterator().next().getPrice().doubleValue()),
				productModel.getEurope1Prices().iterator().next().getCurrency().getIsocode());

		zeroPrice.add(new Double(zeroValue));
		productData.setPrice(priceData);
		Collection<PriceRowModel> multiPriceList=productModel.getEurope1Prices();
		
		if(multiPriceList !=null && CollectionUtils.isNotEmpty(multiPriceList) && multiPriceList.size()>1)
		{
			for(PriceRowModel price:multiPriceList)
			{
				priceList.add(new Double(price.getPrice().doubleValue()));
			}
			priceList.removeAll(zeroPrice);
		}
		else
		{
			priceList.add(new Double(productModel.getEurope1Prices().iterator().next().getPrice().doubleValue()));
		}

		productData.setUnitPrices(priceList);
	}
}
