/**
 *
 */
package de.hybris.wooliesegiftcard.facades.order.converters.populator;

import de.hybris.platform.commercefacades.user.converters.populator.AddressPopulator;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.user.UserService;

import javax.annotation.Resource;


/**
 * @author 668982 This class is used to populate the address of the user
 */
public class WooliesAddressPopulator extends AddressPopulator
{
	@Resource(name = "userService")
	private UserService userService;

	/**
	 * This method is used to populate address
	 * 
	 * @param source
	 * @param target
	 */
	@Override
	public void populate(final AddressModel source, final AddressData target)
	{
		target.setIsBilling(source.getBillingAddress());
		target.setIsShipping(source.getShippingAddress());
		target.setFirstName(source.getFirstname());
		target.setLastName(source.getLastname());
		target.setPhone(source.getPhone1());
		target.setAddress1(source.getStreetname());
		target.setAddress2(source.getStreetnumber());
		target.setState(source.getDistrict());
		target.setCity(source.getTown());
		target.setAddressID(source.getPk().toString());
	}
}
