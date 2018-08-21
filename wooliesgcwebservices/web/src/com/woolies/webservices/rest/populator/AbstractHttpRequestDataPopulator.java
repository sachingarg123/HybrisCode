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
package com.woolies.webservices.rest.populator;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;


/**
 * This class is to populate the http request
 *
 */
public abstract class AbstractHttpRequestDataPopulator
{

	/**
	 * This method is used to update string value for the request
	 *
	 * @param request
	 * @param paramName
	 * @param defaultValue
	 * @return
	 */
	protected String updateStringValueFromRequest(final HttpServletRequest request, final String paramName,
			final String defaultValue)
	{
		final String requestParameterValue = getRequestParameterValue(request, paramName);
		if (requestParameterValue == null || requestParameterValue.isEmpty())
		{
			return null;
		}
		return StringUtils.defaultIfBlank(requestParameterValue, defaultValue);
	}

	/**
	 * This method is used to update Boolean value for the request
	 * 
	 * @param request
	 * @param paramName
	 * @param defaultValue
	 * @return
	 */
	protected boolean updateBooleanValueFromRequest(final HttpServletRequest request, final String paramName,
			final boolean defaultValue)
	{
		final String booleanString = updateStringValueFromRequest(request, paramName, null);
		if (booleanString == null)
		{
			return defaultValue;
		}
		return Boolean.parseBoolean(booleanString);
	}

	/**
	 * This method is used to update Double value for the request
	 * 
	 * @param request
	 * @param paramName
	 * @param defaultValue
	 * @return
	 */
	protected Double updateDoubleValueFromRequest(final HttpServletRequest request, final String paramName,
			final Double defaultValue)
	{
		final String booleanString = updateStringValueFromRequest(request, paramName, null);
		if (booleanString == null)
		{
			return defaultValue;
		}
		return Double.valueOf(booleanString);
	}

	/**
	 * This method is used to request parameter value
	 * 
	 * @param request
	 * @param paramName
	 * @return
	 */
	protected String getRequestParameterValue(final HttpServletRequest request, final String paramName)
	{
		return request.getParameter(paramName);
	}
}
