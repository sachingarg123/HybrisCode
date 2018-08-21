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
package com.woolies.webservices.rest.request.support.impl;

import de.hybris.platform.commerceservices.order.CommercePaymentProviderStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;

import com.woolies.webservices.rest.exceptions.UnsupportedRequestException;
import com.woolies.webservices.rest.request.support.RequestSupportedStrategy;


/**
 * Implementation for {@link RequestSupportedStrategy} which checks if request is supported based on payment provider
 * name
 *
 *
 */
public class PaymentProviderRequestSupportedStrategy implements RequestSupportedStrategy
{
	private CommercePaymentProviderStrategy paymentProviderStrategy;

	private Map<String, List<String>> unsupportedRequestMap = new HashMap();

	/**
	 * This method checks if the quest request is supported or not
	 * 
	 * @param requestId
	 */
	@Override
	public boolean isRequestSupported(final String requestId)
	{
		final String paymentProvider = paymentProviderStrategy.getPaymentProvider();
		if (paymentProvider != null)
		{
			final List<String> unsupportedRequests = unsupportedRequestMap.get(paymentProvider);
			if (unsupportedRequests != null)
			{
				return !unsupportedRequests.contains(requestId);
			}
		}
		return true;
	}

	/**
	 * Check if request is supported or not
	 * 
	 * @param requestId
	 */
	@Override
	public void checkIfRequestSupported(final String requestId) throws UnsupportedRequestException
	{
		final String paymentProvider = paymentProviderStrategy.getPaymentProvider();
		if (paymentProvider != null)
		{
			final List<String> unsupportedRequests = unsupportedRequestMap.get(paymentProvider);
			if (unsupportedRequests != null && unsupportedRequests.contains(requestId))
			{
				throw new UnsupportedRequestException("This request is not supported for payment provider : " + paymentProvider);
			}
		}
	}

	/**
	 * Gets payment provider strategy
	 * 
	 * @return paymentProviderStrategy
	 */
	public CommercePaymentProviderStrategy getPaymentProviderStrategy()
	{
		return paymentProviderStrategy;
	}

	/**
	 * Sets payment provider strategy
	 * 
	 * @param paymentProviderStrategy
	 */
	@Required
	public void setPaymentProviderStrategy(final CommercePaymentProviderStrategy paymentProviderStrategy)
	{
		this.paymentProviderStrategy = paymentProviderStrategy;
	}

	/**
	 * Gets unsupported RequestMap
	 * 
	 * @return
	 */
	public Map<String, List<String>> getUnsupportedRequestMap()
	{
		return unsupportedRequestMap;
	}

	/**
	 * sets Unsupported RequestMap
	 * 
	 * @param unsupportedRequestMap
	 */
	public void setUnsupportedRequestMap(final Map<String, List<String>> unsupportedRequestMap)
	{
		this.unsupportedRequestMap = unsupportedRequestMap;
	}
}
