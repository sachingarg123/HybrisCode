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
package com.woolies.webservices.rest.formatters.impl;

import java.util.Date;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.woolies.webservices.rest.formatters.WsDateFormatter;


/**
 * This class is default Web service date formatter
 *
 */
public class DefaultWsDateFormatter implements WsDateFormatter
{
	private final DateTimeFormatter parser = ISODateTimeFormat.dateTimeNoMillis();

	/**
	 * This method converts given time stamp to Date format
	 * 
	 * @param timestamp
	 */
	@Override
	public Date toDate(final String timestamp)
	{
		return parser.parseDateTime(timestamp).toDate();
	}

	/**
	 * This method converts give date format to string format
	 */
	@Override
	public String toString(final Date date)
	{
		return parser.print(date.getTime());
	}

}
