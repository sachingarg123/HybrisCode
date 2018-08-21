/**
 *
 */
package de.hybris.wooliesegiftcard.core.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.commerceservices.customer.impl.DefaultCustomerAccountService;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.impl.UniqueAttributesInterceptor;
import de.hybris.wooliesegiftcard.core.WooliesCustomerAccountService;
import de.hybris.wooliesegiftcard.core.genric.dao.impl.DefaultWooliesGenericDao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;



/**
 * @author 648156 This class is used maintain customer Account service
 */
public class WooliesDefaultCustomerAccountService extends DefaultCustomerAccountService implements WooliesCustomerAccountService
{
	@Autowired
	private DefaultWooliesGenericDao defaultWooliesGenericDao;
	private static final String CUSTOMER_NOT_NULL = "Customer model cannot be null";

	/**
	 * Register a user with given parameters
	 *
	 * @param customerModel
	 *           the user data the user will be registered with
	 * @throws DuplicateUidException
	 *            if the login is not unique
	 */
	@Override
	public void register(final CustomerModel customerModel) throws DuplicateUidException
	{
		registerCustomer(customerModel);
	}

	@Override
	public void registerGuestForAnonymousCheckout(final CustomerModel customerModel) throws DuplicateUidException
	{
		registerCustomer(customerModel);
	}

	/**
	 * Register a customer with given parameters
	 *
	 * @param customerModel
	 *           the user data the user will be registered with
	 * @throws DuplicateUidException
	 *            if the login is not unique
	 */
	@Override
	public void registerCustomer(final CustomerModel customerModel) throws DuplicateUidException
	{
		internalSaveCustomer(customerModel);
	}

	/**
	 * To save customer details
	 *
	 * @param customerModel
	 *           the user data to save
	 * @throws DuplicateUidException
	 *            if the login is not unique
	 */
	@Override
	public void internalSaveCustomer(final CustomerModel customerModel) throws DuplicateUidException
	{

		try
		{
			getModelService().save(customerModel);
		}
		catch (final ModelSavingException modelSavingException)
		{
			if (modelSavingException.getCause() instanceof InterceptorException
					&& ((InterceptorException) modelSavingException.getCause()).getInterceptor().getClass()
							.equals(UniqueAttributesInterceptor.class))
			{
				throw new DuplicateUidException(customerModel.getUid(), modelSavingException);
			}
			else
			{
				throw modelSavingException;
			}
		}
		catch (final AmbiguousIdentifierException ambigIdentifyException)
		{
			throw new DuplicateUidException(customerModel.getUid(), ambigIdentifyException);
		}
	}

	/**
	 * To save Address Entry
	 *
	 * @param customerModel
	 *           the user data to save
	 * @param addressModel
	 *           if the login is not unique
	 */
	@Override
	public void saveAddressesEntry(final CustomerModel customerModel, final AddressModel addressModel)
	{
		final List<AddressModel> customerAddresses = new ArrayList<>();
		if (customerModel.getAddresses() != null)
		{
			customerAddresses.addAll(customerModel.getAddresses());
			if (!customerAddresses.contains(addressModel))
			{
				addressModel.setBillingAddress(Boolean.valueOf(true));
				addressModel.setOwner(customerModel);
				getModelService().save(addressModel);
				getModelService().refresh(addressModel);
				customerAddresses.add(addressModel);
			}

		}
		else
		{
			addressModel.setBillingAddress(Boolean.valueOf(true));
			addressModel.setOwner(customerModel);
			getModelService().save(addressModel);
			getModelService().refresh(addressModel);
			customerAddresses.add(addressModel);
		}
		customerModel.setAddresses(customerAddresses);
	}

	/**
	 * To set default address for the customer
	 *
	 * @param customerModel
	 *           the customer data
	 * @param addressModel
	 *           the address data
	 */
	@Override
	public void setDefaultAddressEntry(final CustomerModel customerModel, final AddressModel addressModel)
	{
		validateParameterNotNull(customerModel, CUSTOMER_NOT_NULL);
		validateParameterNotNull(addressModel, "Address model cannot be null");
		if (customerModel.getAddresses().contains(addressModel))
		{
			if (addressModel.getBillingAddress().booleanValue())
			{
				customerModel.setDefaultPaymentAddress(addressModel);
			}
			else if (addressModel.getShippingAddress().booleanValue())
			{
				customerModel.setDefaultShipmentAddress(addressModel);
			}
		}
		else
		{
			final AddressModel clone = getModelService().clone(addressModel);
			clone.setOwner(customerModel);
			getModelService().save(clone);
			final List<AddressModel> customerAddresses = new ArrayList<>();
			customerAddresses.addAll(customerModel.getAddresses());
			customerAddresses.add(clone);
			customerModel.setAddresses(customerAddresses);
			if (addressModel.getBillingAddress().booleanValue())
			{
				customerModel.setDefaultPaymentAddress(clone);
			}
			else if (addressModel.getShippingAddress().booleanValue())
			{
				customerModel.setDefaultShipmentAddress(clone);
			}
		}
		getModelService().save(customerModel);
		getModelService().refresh(customerModel);
	}

	/**
	 * This method is used to get the address of customer for the given code
	 *
	 * @param customerModel
	 * @param code
	 * @return addressModel
	 */
	@Override
	public AddressModel getAddressForCode(final CustomerModel customerModel, final String code)
	{
		validateParameterNotNull(customerModel, CUSTOMER_NOT_NULL);

		for (final AddressModel addressModel : getAllAddressEntries(customerModel))
		{
			if (addressModel.getPk().getLongValueAsString().equals(code))
			{
				return addressModel;
			}
		}
		return null;
	}

	/**
	 * Used to get address for the given address id
	 *
	 * @return addressModel
	 */
	@Override
	public AddressModel getaddressforAddressId(final String addressId)
	{
		final List<AddressModel> list = defaultWooliesGenericDao.getAddressModelByAddressId(addressId);
		if (!CollectionUtils.isEmpty(list))
		{
			return list.get(0);
		}
		else
		{
			return null;
		}
	}

	/**
	 * To get address details of the customer
	 *
	 * @param customerModel
	 *           the customer data
	 * @return the address details
	 */
	@Override
	public List<AddressModel> getAllAddressEntries(final CustomerModel customerModel)
	{
		validateParameterNotNull(customerModel, CUSTOMER_NOT_NULL);
		final List<AddressModel> addressModels = new ArrayList<>();
		addressModels.addAll(customerModel.getAddresses());
		return addressModels;
	}

	/**
	 * This method is used to delete address entry of customer for the given address and customer details
	 *
	 * @param customerModel
	 *           the customerModel
	 * @param addressModel
	 *           the addressModel
	 */
	@Override
	public void deleteAddressEntry(final CustomerModel customerModel, final AddressModel addressModel)
	{
		validateParameterNotNull(customerModel, CUSTOMER_NOT_NULL);
		validateParameterNotNull(addressModel, "Address model cannot be null");



		if (customerModel.getAddresses().contains(addressModel))
		{
			if (addressModel.getBillingAddress().booleanValue())
			{
				deleteBillingAddress(customerModel, addressModel);
			}
			else if (addressModel.getShippingAddress().booleanValue())
			{
				deleteShippingAddress(customerModel, addressModel);
			}
		}
		else
		{
			throw new IllegalArgumentException("Address " + addressModel + " does not belong to the customer " + customerModel
					+ " and will not be removed.");
		}
	}

	/**
	 * This used to deleteShippingAddress
	 *
	 * @param customerModel
	 * @param addressModel
	 */
	private void deleteShippingAddress(final CustomerModel customerModel, final AddressModel addressModel)
	{
		final boolean changeDefaultShipmentAddress = addressModel.equals(getDefaultShipmentAddress(customerModel));
		getModelService().remove(addressModel);
		getModelService().refresh(customerModel);
		final Collection<AddressModel> addresses = customerModel.getAddresses();
		final ArrayList<AddressModel> shippingAddresses = new ArrayList<>();

		for (final AddressModel address : addresses)
		{

			if (address.getShippingAddress().booleanValue())
			{
				shippingAddresses.add(address);

			}
		}
		if (changeDefaultShipmentAddress && !CollectionUtils.isEmpty(shippingAddresses))
		{
			setDefaultAddressEntry(customerModel, shippingAddresses.get(0));
		}
	}

	/**
	 * This used to deleteShippingAddress
	 * 
	 * @param customerModel
	 * @param addressModel
	 */
	private void deleteBillingAddress(final CustomerModel customerModel, final AddressModel addressModel)
	{
		final boolean changeDefaultPaymentAddress = addressModel.equals(getDefaultPaymentAddress(customerModel));
		getModelService().remove(addressModel);
		getModelService().refresh(customerModel);
		final Collection<AddressModel> addresses = customerModel.getAddresses();
		final ArrayList<AddressModel> billingAddresses = new ArrayList<>();

		for (final AddressModel address : addresses)
		{

			if (address.getBillingAddress().booleanValue())
			{
				billingAddresses.add(address);

			}
		}
		if (changeDefaultPaymentAddress && !CollectionUtils.isEmpty(billingAddresses))
		{
			setDefaultAddressEntry(customerModel, billingAddresses.get(0));
		}

	}

	/**
	 * This method is used to clear the default payment address of the customer
	 *
	 * @param customerModel
	 */

	@Override
	public void clearDefaultPaymentAddressEntry(final CustomerModel customerModel)
	{
		validateParameterNotNull(customerModel, CUSTOMER_NOT_NULL);
		customerModel.setDefaultPaymentAddress(null);
		getModelService().save(customerModel);
		getModelService().refresh(customerModel);
	}

	/**
	 * This method is used to clear the default shipment address of the customer
	 *
	 * @param customerModel
	 */
	@Override
	public void clearDefaultShipmentAddressEntry(final CustomerModel customerModel)
	{
		validateParameterNotNull(customerModel, CUSTOMER_NOT_NULL);
		customerModel.setDefaultShipmentAddress(null);
		getModelService().save(customerModel);
		getModelService().refresh(customerModel);
	}

	/**
	 * This method is used to get the default payment address of the customer
	 *
	 * @param customerModel
	 */
	@Override
	public AddressModel getDefaultPaymentAddress(final CustomerModel customerModel)
	{
		return customerModel.getDefaultPaymentAddress();
	}

	/**
	 * This method is used to get the default shipment address of the customer
	 *
	 * @param customerModel
	 */
	@Override
	public AddressModel getDefaultShipmentAddress(final CustomerModel customerModel)
	{
		return customerModel.getDefaultShipmentAddress();
	}

}
