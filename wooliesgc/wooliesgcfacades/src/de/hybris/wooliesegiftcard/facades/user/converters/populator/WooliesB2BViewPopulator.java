/**
 *
 */
package de.hybris.wooliesegiftcard.facades.user.converters.populator;

import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BCustomerModel;


/**
 * @author 676313 This class is used to populate B2B view profile
 */
public class WooliesB2BViewPopulator implements Populator<UserModel, CustomerData>
{

	/**
	 * This method is used to populate B2B view
	 * 
	 * @param source
	 * @param target
	 * @throws ConversionException
	 */
	@Override
	public void populate(final UserModel source, final CustomerData target) throws ConversionException
	{
		target.setFirstName(((CorporateB2BCustomerModel) source).getFirstName());
		target.setLastName(((CorporateB2BCustomerModel) source).getLastName());
		target.setUid(((CorporateB2BCustomerModel) source).getUid());
		target.setPhone(((CorporateB2BCustomerModel) source).getPhone());
		target.setCustomerId(((CorporateB2BCustomerModel) source).getCustomerID());
		target.setEmail(((CorporateB2BCustomerModel) source).getEmail());
		target.setOptInForMarketing(((CorporateB2BCustomerModel) source).getOptInForMarketing());
	}
}