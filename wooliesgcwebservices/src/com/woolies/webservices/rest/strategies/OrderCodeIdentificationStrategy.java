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
package com.woolies.webservices.rest.strategies;


/**
 * Strategy for identifying is given string is GUID.
 */
public interface OrderCodeIdentificationStrategy
{

	/**
	 * This method is used to check if given potential is ID or not
	 *
	 * @param potentialId
	 * @return
	 */
	boolean isID(String potentialId);
}
