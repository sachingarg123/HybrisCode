/**
 *
 */
package de.hybris.wooliesegiftcard.facades.user.converters.populator;

import de.hybris.platform.commercefacades.user.converters.populator.AddressReversePopulator;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.user.UserService;

import javax.annotation.Resource;

import org.apache.commons.lang.BooleanUtils;
import org.springframework.util.Assert;



/**
 * @author 648156 This class is used to populate address reverse
 *
 */
public class WooliesAddressReversePopulator extends AddressReversePopulator
{
	@Resource(name = "userService")
	private UserService userService;

	/**
	 * This method is populate address reverse
	 * 
	 * @param addressData
	 * @param addressModel
	 * @throws ConversionException
	 */
	@Override
	public void populate(final AddressData addressData, final AddressModel addressModel) throws ConversionException
	{
		Assert.notNull(addressData, "Parameter addressData cannot be null.");
		Assert.notNull(addressModel, "Parameter addressModel cannot be null.");

		super.populate(addressData, addressModel);
		addressModel.setFirstname(addressData.getFirstName());
		addressModel.setLastname(addressData.getLastName());
		addressModel.setPhone1(addressData.getPhone());
		addressModel.setTown(addressData.getCity());
		addressModel.setStreetname(addressData.getAddress1());
		addressModel.setStreetnumber(addressData.getAddress2());
		addressModel.setDistrict(addressData.getState());
		if (null != addressData.getIsBilling())
		{
			addressModel.setBillingAddress(BooleanUtils.toBooleanObject(addressData.getIsBilling().booleanValue()));
		}
		else
		{
			addressModel.setBillingAddress(Boolean.TRUE);
		}


		if (null != addressData.getIsShipping())
		{
			addressModel.setShippingAddress(BooleanUtils.toBooleanObject(addressData.getIsShipping().booleanValue()));
		}
		else
		{
			addressModel.setBillingAddress(Boolean.TRUE);
		}
		addressModel.setVisibleInAddressBook(Boolean.valueOf(true));
	}
}
