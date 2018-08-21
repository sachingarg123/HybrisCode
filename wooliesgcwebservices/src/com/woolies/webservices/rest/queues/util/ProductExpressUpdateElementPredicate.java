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
package com.woolies.webservices.rest.queues.util;


import com.woolies.webservices.rest.queues.data.ProductExpressUpdateElementData;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Predicate;

/**
 *
 * This class is used to predicate ProductExpressUpdateElement
 *
 */
public class ProductExpressUpdateElementPredicate implements Predicate<ProductExpressUpdateElementData>
{
	private ProductExpressUpdateElementData productExpressUpdateElementData;

	public ProductExpressUpdateElementPredicate()
	{
		super();
	}

	/**
	 *
	 * @param productExpressUpdateElementData
	 */
	public ProductExpressUpdateElementPredicate(final ProductExpressUpdateElementData productExpressUpdateElementData)
	{
		super();
		this.productExpressUpdateElementData = productExpressUpdateElementData;
	}

	/**
	 * This method is used to apply ProductExpressUpdateElement
	 *
	 * @param input
	 * @return
	 */
	@Override
	public boolean apply(@Nullable final ProductExpressUpdateElementData input)
	{

		return areElementsEqual(productExpressUpdateElementData, input);
	}

	/**
	 * This method is used to compare the ProductExpressUpdateElementData objects
	 * 
	 * @param element1
	 * @param element2
	 * @return gives the elements are equal or not
	 */
	protected boolean areElementsEqual(final ProductExpressUpdateElementData element1,
			final ProductExpressUpdateElementData element2)
	{
		if (element1 == element2) //NOSONAR
		{
			return true;
		}

		if (element1 == null || element2 == null)
		{
			return false;
		}

		if (!StringUtils.equals(element1.getCode(), element2.getCode()))
		{
			return false;
		}

		if (!StringUtils.equals(element1.getCatalogVersion(), element2.getCatalogVersion()))
		{
			return false;
		}

		if (!StringUtils.equals(element1.getCatalogId(), element2.getCatalogId()))
		{
			return false;
		}

		return true;
	}

	/**
	 * This method is used to get ProductExpressUpdateElementData object
	 * 
	 * @return productExpressUpdateElementData
	 */
	public ProductExpressUpdateElementData getProductExpressUpdateElementData()
	{
		return productExpressUpdateElementData;
	}

	/**
	 * This method is used to set ProductExpressUpdateElementData object
	 * 
	 * @param productExpressUpdateElementData
	 */
	public void setProductExpressUpdateElementData(final ProductExpressUpdateElementData productExpressUpdateElementData)
	{
		this.productExpressUpdateElementData = productExpressUpdateElementData;
	}

}
