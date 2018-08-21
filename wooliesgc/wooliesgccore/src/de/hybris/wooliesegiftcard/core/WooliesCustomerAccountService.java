/**
 *
 */
package de.hybris.wooliesegiftcard.core;

import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;

import java.util.List;



/**
 * @author 648156 This interface is used for customer Account service
 *
 */
public interface WooliesCustomerAccountService extends CustomerAccountService
{
	/**
	 * This method is used to register a B2C customer, while calling Registration API
	 *
	 * @param customerModel
	 *           the parameter value to be used
	 * @throws DuplicateUidException
	 *            used to throw exception
	 */
	public void register(final CustomerModel customerModel) throws DuplicateUidException;

	/**
	 * This method is used to register a B2C customer, while calling Registration API.Also it will check for duplicate
	 * customer registration
	 *
	 * @param customerModel
	 *           the parameter value to be used
	 * @throws DuplicateUidException
	 *            used to throw exception
	 */
	public void registerCustomer(final CustomerModel customerModel) throws DuplicateUidException;

	/**
	 * This method is used to register a Guest customer, while calling Registration API.
	 *
	 * @param customerModel
	 *           the parameter value to be used
	 * @throws DuplicateUidException
	 *            used to throw exception
	 */
	public void registerGuestForAnonymousCheckout(final CustomerModel customerModel) throws DuplicateUidException;

	/**
	 * This method is used to register a B2C customer, while calling Registration API.Calling this method internally to
	 * do registration at Hybris end.
	 *
	 * @param customerModel
	 *           the parameter value to be used
	 * @throws DuplicateUidException
	 *            used to throw exception
	 */
	public void internalSaveCustomer(final CustomerModel customerModel) throws DuplicateUidException;

	/**
	 * This method is come into picture while saving the address of customer during registration process.
	 *
	 * @param customerModel
	 *           the parameter value to be used
	 * @param addressModel
	 *           the parameter value to be used
	 */
	public void saveAddressesEntry(final CustomerModel customerModel, final AddressModel addressModel);

	/**
	 * During checkout process,if an existing customer will login and go for second time checkout, he/she will get an
	 * option to choose existing address by selecting addressID
	 *
	 * @param addressId
	 *           the parameter value to be used
	 * @return AddressModel the parameter value to be used
	 */
	public AddressModel getaddressforAddressId(final String addressId);

	/**
	 * During Registration process,it will set the default address
	 *
	 * @param customerModel
	 *           the parameter value to be used
	 * @param addressModel
	 *           the parameter value to be used
	 */
	public void setDefaultAddressEntry(final CustomerModel customerModel, final AddressModel addressModel);

	/**
	 * Method will provide the all addresses which are saved at Hybris DB.
	 *
	 * @param customerModel
	 *           the parameter value to be used
	 * @return list of addressModel
	 */
	public List<AddressModel> getAllAddressEntries(final CustomerModel customerModel);

	/**
	 * Required to call this method while removing the default shipping address.
	 *
	 * @param customerModel
	 *           the parameter value to be used
	 */
	void clearDefaultPaymentAddressEntry(CustomerModel customerModel);

	/**
	 * Required to call this method while removing the default shipping address.
	 *
	 * @param customerModel
	 *           the parameter value to be used
	 */
	void clearDefaultShipmentAddressEntry(CustomerModel customerModel);

	/**
	 * Method is responsible to fetch the default payment address.The same address will be used for billing Address.
	 *
	 * @param customerModel
	 *           the parameter value to be used
	 * @return AddressModel the parameter value to be used
	 */
	AddressModel getDefaultPaymentAddress(CustomerModel customerModel);

	/**
	 * Method will provides the default shipping address of the customer.The same address will be used during the
	 * registration time.
	 *
	 * @param cusstomerModel
	 *           the parameter value to be used
	 * @return AddressModel the parameter value to be used
	 */
	AddressModel getDefaultShipmentAddress(CustomerModel cusstomerModel);
}
