/**
 *
 */
package com.woolies.webservices.rest.exceptions;

import de.hybris.platform.webservicescommons.errors.exceptions.WebserviceException;


/**
 * @author 668982 This class is Woolies sWebservice Exception
 *
 */
public class WooliesWebserviceException extends WebserviceException
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


	/**
	 * @param errorCode
	 * @param message
	 * @param errorReason
	 */
	public WooliesWebserviceException(final String errorCode, final String message, final String errorReason)
	{
		super(message);
		this.errorCode = errorCode;
		this.errorReason = errorReason;
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.platform.webservicescommons.errors.exceptions.WebserviceException#getType()
	 */
	@Override
	public String getType()
	{
		// XXX Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.platform.webservicescommons.errors.exceptions.WebserviceException#getSubjectType()
	 */
	@Override
	public String getSubjectType()
	{
		// XXX Auto-generated method stub
		return null;
	}


}


