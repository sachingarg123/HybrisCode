/**
 *
 */
package de.hybris.wooliesegiftcard.exception;

/**
 * @author 648156 This is user defined B2B user exception class
 */
public class WooliesB2BUserException extends Exception
{


	/**
	 * @param message
	 */
	public WooliesB2BUserException(final String message)
	{
		super(message);
	}

	/**
	 *
	 * @param message
	 * @param cause
	 */
	public WooliesB2BUserException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

}
