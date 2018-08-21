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
package com.woolies.webservices.rest.queues.channel;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import com.woolies.webservices.rest.queues.UpdateQueue;
import com.woolies.webservices.rest.queues.data.ProductExpressUpdateElementData;
import com.woolies.webservices.rest.queues.util.ProductExpressUpdateElementPredicate;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.base.Predicate;


/**
 *
 * This class is listener for Producat Experess update channel
 *
 */
public class ProductExpressUpdateChannelListener
{
	private static final Logger LOG = Logger.getLogger(ProductExpressUpdateChannelListener.class);
	private final ProductExpressUpdateElementPredicate productExpressUpdateElementPredicate = new ProductExpressUpdateElementPredicate();
	private UpdateQueue<ProductExpressUpdateElementData> productExpressUpdateQueue;
	private Converter<ProductModel, ProductExpressUpdateElementData> productExpressUpdateElementConverter;

	/**
	 * This method is used to add the product express update element data to the queue
	 *
	 * @param product
	 */
	public void onMessage(final ProductModel product)
	{
		if (product != null)
		{
			LOG.debug("ProductExpressUpdateChannelListener got product with code " + product.getCode());
			final ProductExpressUpdateElementData productExpressUpdateElementData = getProductExpressUpdateElementConverter()
					.convert(product);
			if (productExpressUpdateElementData != null)
			{
				getProductExpressUpdateQueue().removeItems(getPredicate(productExpressUpdateElementData));
				getProductExpressUpdateQueue().addItem(productExpressUpdateElementData);
			}
		}
	}

	/**
	 * Method return object which will be used to determine if element is equal to productExpressUpdateElementData
	 * parameter.
	 *
	 * @param productExpressUpdateElementData
	 *           - element data for comparison
	 * @return object implementing Predicate interface which should return true from apply method if element is equal to
	 *         productExpressUpdateElementData parameter
	 */
	protected Predicate<ProductExpressUpdateElementData> getPredicate(
			final ProductExpressUpdateElementData productExpressUpdateElementData)
	{
		productExpressUpdateElementPredicate.setProductExpressUpdateElementData(productExpressUpdateElementData);
		return productExpressUpdateElementPredicate;
	}

	/**
	 * This method is used to get ProductExpressUpdateQueue object data
	 * 
	 * @return productExpressUpdateQueue
	 */
	public UpdateQueue<ProductExpressUpdateElementData> getProductExpressUpdateQueue()
	{
		return productExpressUpdateQueue;
	}

	/**
	 * This method is used to set ProductExpressUpdateQueue object data
	 * 
	 * @param productExpressUpdateQueue
	 */
	@Required
	public void setProductExpressUpdateQueue(final UpdateQueue<ProductExpressUpdateElementData> productExpressUpdateQueue)
	{
		this.productExpressUpdateQueue = productExpressUpdateQueue;
	}

	/**
	 * This method is used to get ProductExpressUpdateElement data
	 * 
	 * @return
	 */
	public Converter<ProductModel, ProductExpressUpdateElementData> getProductExpressUpdateElementConverter()
	{
		return productExpressUpdateElementConverter;
	}

	/**
	 * This method is used to set ProductExpressUpdateElement data
	 * 
	 * @param productExpressUpdateElementConverter
	 */
	@Required
	public void setProductExpressUpdateElementConverter(
			final Converter<ProductModel, ProductExpressUpdateElementData> productExpressUpdateElementConverter)
	{
		this.productExpressUpdateElementConverter = productExpressUpdateElementConverter;
	}

}
