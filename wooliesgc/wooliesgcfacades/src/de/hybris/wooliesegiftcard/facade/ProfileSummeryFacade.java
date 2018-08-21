/**
 *
 */
package de.hybris.wooliesegiftcard.facade;

import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.wooliesegiftcard.exceptions.WooliesFacadeLayerException;
import de.hybris.wooliesegiftcard.facades.GuestCartProfileData;
import de.hybris.wooliesegiftcard.facades.UserProfileData;


/**
 * @author 648156 This interface is used to profile details of customer and have the methods
 *         getCurrentCustomer,getCurrentB2BCustomer and getuser
 */
public interface ProfileSummeryFacade
{
	/**
	 * This method is used to get the current customer details
	 *
	 * @return the UserProfileData for the current customer
	 * @throws WooliesFacadeLayerException
	 *            exception thrown in case of failure
	 */
	UserProfileData getCurrentCustomer() throws WooliesFacadeLayerException;

	/**
	 * This method is used to get the current customer details for the given code
	 *
	 * @param code
	 *           code
	 * @return the UserProfileData
	 * @throws WooliesFacadeLayerException
	 *            WooliesFacadeLayerException
	 */
	UserProfileData getCurrentCustomer(final String code) throws WooliesFacadeLayerException;

	/**
	 * This method is used to get cureent b2b custmer details
	 *
	 * @return the customer data
	 */
	CustomerData getCurrentB2BCustomer();

	/**
	 * This method is used to get the use details
	 *
	 * @return the customer data
	 */
	CustomerData getuser();

	/**
	 * * This method is used to get the Anonymous User Cart details
	 *
	 * @param guid
	 *           of the Cart
	 * @return UserProfileData for the anonymous customer
	 * @throws WooliesFacadeLayerException
	 *            exception for cart restoration
	 */
	GuestCartProfileData getCartDetailsForAnonymousUser(String guid) throws WooliesFacadeLayerException;

}
