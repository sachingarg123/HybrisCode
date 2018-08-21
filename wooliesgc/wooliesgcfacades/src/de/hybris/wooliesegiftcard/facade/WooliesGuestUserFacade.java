/**
 *
 */
package de.hybris.wooliesegiftcard.facade;

import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.core.model.order.CartModel;


/**
 * @author 648156 This interface is used to maintain details of the guest user
 */
public interface WooliesGuestUserFacade extends CustomerFacade
{
	/**
	 * This method is used to create guest user for anonymous checkout for the give customer data
	 *
	 * @param guestCustomerData
	 *           the value to be used
	 * @param name
	 *           the value to be used
	 * @param cartModel
	 *           the value to be used
	 * @throws DuplicateUidException
	 *            used to throw exception
	 */

	public void createGuestUserForAnonymousCheckout(final CustomerData guestCustomerData, final String name, CartModel cartModel)
			throws DuplicateUidException;
}
