/**
 *
 */
package com.woolies.webservices.rest.errors.converters;

import de.hybris.platform.webservicescommons.dto.error.ErrorWsDTO;
import de.hybris.platform.webservicescommons.errors.converters.AbstractErrorConverter;

import java.util.List;

import org.springframework.core.NestedRuntimeException;

import com.woolies.webservices.rest.exceptions.WooliesWebserviceException;


/**
 * @author 668982 This class is Woolies Webservice Exception Converter
 *
 */
public class WooliesWebserviceExceptionConverter extends AbstractErrorConverter
{

	@Override
	public boolean supports(final Class clazz)
	{
		return WooliesWebserviceException.class.isAssignableFrom(clazz);
	}

	/**
	 * This method is used to populate the web service error list
	 *
	 * @param o
	 * @param webserviceErrorList
	 */
	@Override
	public void populate(final Object o, final List<ErrorWsDTO> webserviceErrorList)
	{
		final WooliesWebserviceException ex = (WooliesWebserviceException) o;
		final ErrorWsDTO error = new ErrorWsDTO();
		error.setErrorMessage(this.filterExceptionMessage(ex));
		error.setErrorDescription(ex.getErrorReason());
		error.setErrorCode(ex.getErrorCode());
		webserviceErrorList.add(error);
	}

	/**
	 * This method is used to filter the exception message
	 *
	 * @param t
	 * @return
	 */
	protected String filterExceptionMessage(final Throwable t)
	{
		final String message = t.getMessage();
		if (NestedRuntimeException.class.isAssignableFrom(t.getClass()))
		{
			final int index = message.indexOf("; nested exception is ");
			if (index > 0)
			{
				return message.substring(0, index);
			}
		}

		return t.getMessage();
	}
}