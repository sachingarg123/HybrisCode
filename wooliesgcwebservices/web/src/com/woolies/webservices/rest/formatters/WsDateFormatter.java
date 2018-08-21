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
package com.woolies.webservices.rest.formatters;

import java.util.Date;


/**
 * This interface is Webservice Date formatter
 *
 */
public interface WsDateFormatter
{
	/**
	 * This method converts given time stamp to Date format
	 *
	 * @param timestamp
	 * @return
	 */
	Date toDate(String timestamp);

	/**
	 * This method converts give date format to string format
	 *
	 * @param date
	 * @return
	 */
	String toString(Date date);
}
