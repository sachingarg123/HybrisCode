/**
 *
 */
package de.hybris.wooliesegiftcard.core.exceptions;

/**
 * @author 669567
 *
 */
public class WooliesServiceLayerException extends Exception
{

	private final String errorCode;
	private final String errorReason;

	/**
	 * @return the errorReason
	 */
	public String getErrorReason()
	{
		return errorReason;
	}

	/**
	 * @return the errorCode
	 */
	public String getErrorCode()
	{
		return errorCode;
	}

	public WooliesServiceLayerException()
	{
		super("Woolies Facade Layer Exception");
		errorCode = null;
		errorReason = null;
	}

	public WooliesServiceLayerException(final String msg)
	{
		super(msg);
		errorCode = null;
		errorReason = null;
	}





	/**
	 * @param message
	 */
	public WooliesServiceLayerException(final String errorCode, final String message, final String errorReason)
	{
		super(message);
		this.errorCode = errorCode;
		this.errorReason = errorReason;
	}


}
