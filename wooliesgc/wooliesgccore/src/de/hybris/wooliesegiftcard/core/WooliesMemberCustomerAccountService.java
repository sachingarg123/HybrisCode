/**
 *
 */
package de.hybris.wooliesegiftcard.core;

import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.wooliesegiftcard.core.model.MemberCustomerModel;


/**
 * @author 669567 This interface is used for customer Account service
 *
 */
public interface WooliesMemberCustomerAccountService extends CustomerAccountService
{
	/**
	 * Register a user with given parameters
	 *
	 * @param memberCustomer
	 *           the member data the user will be registered with
	 * @throws DuplicateUidException
	 *            if the login is not unique
	 */
	void register(final MemberCustomerModel memberCustomer) throws DuplicateUidException;

	/**
	 * Register a customer with given parameters
	 *
	 * @param memberCustomer
	 *           the member data the user will be registered wit
	 * @throws DuplicateUidException
	 *            if the login is not unique
	 */
	void registerCustomer(final MemberCustomerModel memberCustomer) throws DuplicateUidException;

	/**
	 * To save customer details
	 *
	 * @param memberCustomer
	 *           the member data to save
	 * @throws DuplicateUidException
	 *            if the login is not unique
	 */
	void internalSaveCustomer(final MemberCustomerModel memberCustomer) throws DuplicateUidException;

	/**
	 * To update the member details
	 *
	 * @param memberCustomer
	 *           the member data to update
	 */
	void updateMemberCustomer(final MemberCustomerModel memberCustomer);
}
