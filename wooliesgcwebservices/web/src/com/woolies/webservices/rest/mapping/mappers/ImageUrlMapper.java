package com.woolies.webservices.rest.mapping.mappers;

import de.hybris.platform.commercefacades.product.data.ImageData;
import de.hybris.platform.commercewebservicescommons.dto.product.ImageWsDTO;
import de.hybris.platform.webservicescommons.mapping.mappers.AbstractCustomMapper;

import com.woolies.webservices.rest.constants.YcommercewebservicesConstants;

import ma.glasnost.orika.MappingContext;


/**
 * This class image url mapper
 *
 */
public class ImageUrlMapper extends AbstractCustomMapper<ImageData, ImageWsDTO>
{

	/**
	 * This method is used to map image data from A to B object
	 * 
	 * @param a
	 * @param b
	 * @param context
	 */
	@Override
	public void mapAtoB(final ImageData a, final ImageWsDTO b, final MappingContext context)
	{
		// other fields are mapped automatically

		context.beginMappingField("url", getAType(), a, "url", getBType(), b);
		try
		{
			if (shouldMap(a, b, context))
			{
				final StringBuilder url = new StringBuilder(YcommercewebservicesConstants.V2_ROOT_CONTEXT).append(a.getUrl());
				b.setUrl(url.toString());
			}
		}
		finally
		{
			context.endMappingField();
		}
	}
}
