/**
 *
 */
package de.hybris.wooliesegiftcard.exceptions;

/**
 * @author 669567 This is custom exception for facade layer module
 *
 */
public class WooliesFacadeLayerException extends Exception
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

	public WooliesFacadeLayerException()
	{
		super("Woolies Facade Layer Exception");
		errorCode = null;
		errorReason = null;
	}

	public WooliesFacadeLayerException(final String msg)
	{
		super(msg);
		errorCode = null;
		errorReason = null;
	}





	/**
	 * @param message
	 */
	public WooliesFacadeLayerException(final String errorCode, final String message, final String errorReason)
	{
		super(message);
		this.errorCode = errorCode;
		this.errorReason = errorReason;
	}


}
