/**
 *
 */
package de.hybris.wooliesegiftcard.facades.user.converters.populator;

import de.hybris.platform.commercewebservicescommons.dto.order.B2BCustomerListWsDTO;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BCustomerModel;

import java.util.ArrayList;
import java.util.List;


/**
 * @author 648156 This class is used to populate b2b customer list
 */
public class WooliesB2BCustomerListPopulator implements Populator<List<CorporateB2BCustomerModel>, B2BCustomerListWsDTO>
{

	/**
	 * This method populates b2b customer list
	 * 
	 * @param source
	 * @param target
	 * @throws ConversionException
	 */
	@Override
	public void populate(final List<CorporateB2BCustomerModel> source, final B2BCustomerListWsDTO target)
			throws ConversionException
	{
		final List<String> emails = new ArrayList<>();
		for (final CorporateB2BCustomerModel corporateb2bCustomerModel : source)
		{
			emails.add(corporateb2bCustomerModel.getEmail());
		}
		target.setEmail(emails);
	}


}
