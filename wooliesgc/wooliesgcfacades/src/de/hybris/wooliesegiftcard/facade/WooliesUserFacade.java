/**
 *
 */
package de.hybris.wooliesegiftcard.facade;

import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;

import java.util.List;


/**
 * @author 687679 This interface is used to maintain user data
 */
public interface WooliesUserFacade extends UserFacade
{
	/**
	 * This method is used to get the address model for the given address id
	 *
	 * @param addressId
	 *           This Method fetches the AddressData for the addressId passed in the Argument
	 * @return addressData Results gives the Address Data for the particular addressId
	 */
	AddressData getAddressModelForAddressId(String addressId);

	/**
	 * This method is used to get woolies billing address book
	 *
	 * @return the biling addressData
	 */
	List<AddressData> getWooliesBillingAddressBook();

	/**
	 * This method is used to get woolies shilling address book
	 *
	 * @return the shipping addressData
	 */
	List<AddressData> getWooliesShippingAddressBook();

	/**
	 * This method is used to remove address
	 *
	 * @param addressData
	 *           removes this Address Data from the Address list of particular User
	 */
	void removeAddress(AddressData addressData);

	/**
	 * This method is used to get the default shipment address
	 *
	 * @return addressData
	 */
	AddressData getDefaultShipmentAddress();

	/**
	 * This method is used to get woolies address book
	 *
	 * @return the AddressData list
	 */
	List<AddressData> getWooliesAddressBook();

}
