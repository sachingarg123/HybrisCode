/**
 *
 */
package de.hybris.wooliesegiftcard.facades.user.converters.populator;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BCustomerModel;
import de.hybris.wooliesegiftcard.facades.UserProfileData;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.wooliesegiftcard.facades.constants.WooliesgcFacadesConstants;
import java.util.Set;


/**
 * @author 648156 This class is used to populate B2B customer profile
 */
public class WooliesB2BCustomerProfilePopulator implements Populator<UserModel, UserProfileData>
{
	private UserService userService;
	/**
	 * @return the userService
	 */
	public UserService getUserService()
	{
		return userService;
	}

	/**
	 * @param userService
	 *           the userService to set
	 */
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}
	/**
	 * This method is used populate B2B customer profile
	 * 
	 * @param source
	 * @param target
	 * @throws ConversionException
	 */
	@Override
	public void populate(final UserModel source, final UserProfileData target) throws ConversionException
	{
		final CustomerModel customerModel = (CustomerModel) source;
		target.setFirstName(customerModel.getFirstName());
		target.setLastName(customerModel.getLastName());
		target.setAccountType(customerModel.getCustomerType().getCode());
		final Set<PrincipalGroupModel> allGroups = customerModel.getAllGroups();
		if (allGroups.contains(userService.getUserGroupForUID(WooliesgcFacadesConstants.B2BADMINGROUP)))
		{
			target.setIsCorpAdminUser(Boolean.TRUE);
		}
		else
		{
			target.setIsCorpAdminUser(Boolean.FALSE);
		}
		target.setAccountId(((CorporateB2BCustomerModel) source).getDefaultB2BUnit().getUid());
		target.setUid(customerModel.getUid());
		target.setCustomerId(customerModel.getCustomerID());
			target.setEmail(((CorporateB2BCustomerModel) customerModel).getUserEmail());
	}

}
