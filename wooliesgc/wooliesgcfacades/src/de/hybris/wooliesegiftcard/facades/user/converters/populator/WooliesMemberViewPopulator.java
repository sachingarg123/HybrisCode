/**
 *
 */
package de.hybris.wooliesegiftcard.facades.user.converters.populator;

import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.wooliesegiftcard.core.model.MemberCustomerModel;

/**
 * @author 676313 This class is used to populate member view
 */
public class WooliesMemberViewPopulator implements Populator<UserModel, CustomerData>
{

	/**
	 * This method is used to populate member view
	 * 
	 * @param source
	 * @param target
	 * @throws ConversionException
	 */
	@Override
	public void populate(final UserModel source, final CustomerData target) throws ConversionException
	{

		target.setFirstName(((MemberCustomerModel) source).getFirstName());
		target.setLastName(((MemberCustomerModel) source).getLastName());
		target.setPhone(((MemberCustomerModel) source).getPhone());
		target.setEmail(((MemberCustomerModel) source).getEmail());
		target.setUid(((MemberCustomerModel) source).getUid());
			target.setCustomerId(((MemberCustomerModel) source).getCustomerID());
		target.setEmail(((MemberCustomerModel) source).getEmail());
		target.setOptInForMarketing(((MemberCustomerModel) source).getOptInForMarketing());
	}
}



