/**
 *
 */
package de.hybris.wooliesegiftcard.facades.order.converters.populator;

import de.hybris.platform.commercefacades.order.converters.populator.CommerceCartParameterBasicPopulator;
import de.hybris.platform.commercefacades.order.data.AddToCartParams;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;


/**
 * @author 668982 This calss is used to populate commerce cart parameter
 */
public class WooliesCommerceCartParameterBasicPopulator extends CommerceCartParameterBasicPopulator
{
	/**
	 * This method is to populate commerce cart
	 * 
	 * @param addToCartParams
	 * @param parameter
	 * @throws ConversionException
	 */
	@Override
	public void populate(final AddToCartParams addToCartParams, final CommerceCartParameter parameter) throws ConversionException
	{
		if (addToCartParams.getCustomerPrice() != null)
		{

			parameter.setCustomerPrice(addToCartParams.getCustomerPrice());
		}
		if (addToCartParams.getMessage() != null)
		{
			parameter.setMessage(addToCartParams.getMessage());
		}
		if (addToCartParams.getFromName() != null)
		{
			parameter.setFromName(addToCartParams.getFromName());
		}
		if (addToCartParams.getToName() != null)
		{
			parameter.setToName(addToCartParams.getToName());
		}
		if (addToCartParams.getImage() != null)
		{
			parameter.setImage(addToCartParams.getImage());
		}
		parameter.setCustomizeForAlleCards(addToCartParams.getCustomizeForAlleCards());
	}
}
