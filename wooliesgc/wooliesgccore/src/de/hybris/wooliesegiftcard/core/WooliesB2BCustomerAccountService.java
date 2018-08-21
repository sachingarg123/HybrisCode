/**
 *
 */
package de.hybris.wooliesegiftcard.core;

import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BCustomerModel;


/**
 * * @author 648156 This interface is used for B2B customer Account service
 */

public interface WooliesB2BCustomerAccountService extends CustomerAccountService

{
	/**
	 * This method is used while doing registration for B2BCorporate
	 *
	 * @param customerModel
	 *           the parameter value to be used
	 * @throws DuplicateUidException
	 *            used to throw exception
	 */
	public void register(final CorporateB2BCustomerModel customerModel) throws DuplicateUidException;

	/**
	 * This method is used while doing registration for B2BCorporate customer
	 *
	 * @param customerModel
	 *           the parameter value to be used
	 * @throws DuplicateUidException
	 *            used to throw exception
	 */
	public void registerCustomer(final CorporateB2BCustomerModel customerModel) throws DuplicateUidException;

	/**
	 * This method is used while doing registration for B2BCorporate customer.Its Internally called from parent method.
	 *
	 * @param customerModel
	 *           the parameter value to be used
	 * @throws DuplicateUidException
	 *            used to throw exception
	 */
	public void internalSaveCustomer(final CorporateB2BCustomerModel customerModel) throws DuplicateUidException;

	/**
	 * This method is saving the address entry in Address model
	 *
	 * @param customerModel
	 *           the parameter value to be used
	 * @param addressModel
	 *           the parameter value to be used
	 */
	public void saveAddressesEntry(final CorporateB2BCustomerModel customerModel, final AddressModel addressModel);

}
