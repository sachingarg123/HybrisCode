/**
 *
 */
package de.hybris.wooliesegiftcard.exception;

import de.hybris.platform.servicelayer.exceptions.BusinessException;


/**
 * @author 264343 This class to throw is user defined order history exception
 */
public class WoolliesOrderHistoryException extends Exception
{

	/**
	 * @param message
	 */
	public WoolliesOrderHistoryException(final String message)
	{
		super(message);
	}

	public WoolliesOrderHistoryException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

}
