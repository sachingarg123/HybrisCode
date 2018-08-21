/**
 *
 */
package com.woolies.webservices.rest.exceptions;

import de.hybris.platform.commerceservices.order.CommerceCartModificationException;


/**
 * @author 264343 This class is used to throw cart modification exception
 */
public class WoolliesCartModificationException extends CommerceCartModificationException
{

	/**
	 * @param errorCode
	 */
	public WoolliesCartModificationException(final String errorCode)
	{
		super(errorCode);
	}

	/**
	 * @param errorCode
	 * @param cause
	 */
	public WoolliesCartModificationException(final String errorCode, final Throwable cause)
	{
		super(errorCode, cause);
	}

}
