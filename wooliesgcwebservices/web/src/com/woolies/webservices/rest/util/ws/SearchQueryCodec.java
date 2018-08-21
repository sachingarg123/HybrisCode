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
package com.woolies.webservices.rest.util.ws;

/**
 * This interface used to decode and encode the search query
 */
public interface SearchQueryCodec<QUERY>
{
	/**
	 * This method is used to decode the give query
	 * 
	 * @param query
	 * @return
	 */
	QUERY decodeQuery(String query);

	/**
	 * This method is used to encode the give query
	 * 
	 * @param query
	 * @return
	 */
	String encodeQuery(QUERY query);
}
