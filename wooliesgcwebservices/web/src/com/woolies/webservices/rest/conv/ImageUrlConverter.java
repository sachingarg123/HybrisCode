package com.woolies.webservices.rest.conv;

import java.util.Optional;

import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.woolies.webservices.rest.constants.YcommercewebservicesConstants;


/**
 *
 * This class is used as image url converter
 *
 */
public class ImageUrlConverter implements SingleValueConverter
{
	@Override
	public String toString(final Object o)
	{
		return Optional.ofNullable(o).filter(String.class::isInstance).map(String.class::cast).map(this::addRootContext)
				.orElseGet(() -> null);
	}

	/**
	 * This method adds root context to the image url
	 * 
	 * @param imageUrl
	 * @return
	 */
	protected String addRootContext(final String imageUrl)
	{
		return new StringBuilder(YcommercewebservicesConstants.V1_ROOT_CONTEXT).append(imageUrl).toString();
	}

	@Override
	public Object fromString(final String s)
	{
		return null;
	}

	@Override
	public boolean canConvert(final Class type)
	{
		return type == String.class;
	}
}
