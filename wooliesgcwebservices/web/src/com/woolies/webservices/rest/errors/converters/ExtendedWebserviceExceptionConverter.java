/**
 *
 */
package com.woolies.webservices.rest.errors.converters;

import de.hybris.platform.webservicescommons.errors.converters.WebserviceExceptionConverter;
import de.hybris.platform.webservicescommons.errors.exceptions.WebserviceException;

import com.woolies.webservices.rest.exceptions.WooliesWebserviceException;


/**
 * This is Extended Web service Exception Converter
 *
 */
public class ExtendedWebserviceExceptionConverter extends WebserviceExceptionConverter
{
	@Override
	public boolean supports(final Class clazz)
	{
		return WebserviceException.class.isAssignableFrom(clazz) && !WooliesWebserviceException.class.isAssignableFrom(clazz);
	}
}
