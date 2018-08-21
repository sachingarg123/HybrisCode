/**
 *
 */
package de.hybris.wooliesegiftcard.facades.user.converters.populator;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.wooliesegiftcard.facades.UserProfileData;


/**
 * @author 648156 This class is used to populate Customer profile
 */
public class WooliesCustomerProfilePopulator implements Populator<UserModel, UserProfileData>
{
	/**
	 * This method is used to populate CustomerData
	 * 
	 * @param source
	 * @param target
	 * @throws ConversionException
	 */
	@Override
	public void populate(final UserModel source, final UserProfileData target) throws ConversionException
	{
		if (null != ((CustomerModel) source).getCustomerType())
		{
			final CustomerModel customerModel = (CustomerModel) source;
			target.setFirstName(customerModel.getFirstName());
			target.setLastName(customerModel.getLastName());
			target.setAccountType(((CustomerModel) source).getCustomerType().toString());
			target.setUid(customerModel.getUid());
			target.setCustomerId(customerModel.getCustomerID());
			target.setEmail(customerModel.getUserEmail());
		}

	}

}
