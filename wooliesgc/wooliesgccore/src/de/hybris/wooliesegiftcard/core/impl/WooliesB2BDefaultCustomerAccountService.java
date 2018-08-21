/**
 *
 */
package de.hybris.wooliesegiftcard.core.impl;

import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.commerceservices.customer.impl.DefaultCustomerAccountService;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.impl.UniqueAttributesInterceptor;
import de.hybris.wooliesegiftcard.core.WooliesB2BCustomerAccountService;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BCustomerModel;

import java.util.ArrayList;
import java.util.List;


/**
 * @author 648156 This class is used to provide the B2B customer account details.
 */
public class WooliesB2BDefaultCustomerAccountService extends DefaultCustomerAccountService implements
		WooliesB2BCustomerAccountService
{
	/**
	 * Used for customer registration
	 *
	 * @param customerModel
	 * @throws DuplicateUidException
	 *            if the login is not unique
	 */
	@Override
	public void register(final CorporateB2BCustomerModel customerModel) throws DuplicateUidException
	{
		registerCustomer(customerModel);
	}

	/**
	 * Used for customer registration
	 *
	 * @param customerModel
	 * @throws DuplicateUidException
	 *            if the login is not unique
	 */
	@Override
	public void registerCustomer(final CorporateB2BCustomerModel customerModel) throws DuplicateUidException
	{
		internalSaveCustomer(customerModel);
	}

	/**
	 * Used for saving customer details
	 *
	 * @param customerModel
	 *           customerModel
	 * @throws DuplicateUidException
	 *            if the login is not unique
	 */
	@Override
	public void internalSaveCustomer(final CorporateB2BCustomerModel b2bcustomerModel) throws DuplicateUidException
	{
		try
		{
			getModelService().save(b2bcustomerModel);
		}
		catch (final ModelSavingException modelSavingException)
		{
			if (modelSavingException.getCause() instanceof InterceptorException
					&& ((InterceptorException) modelSavingException.getCause()).getInterceptor().getClass()
							.equals(UniqueAttributesInterceptor.class))
			{
				throw new DuplicateUidException(b2bcustomerModel.getUid(), modelSavingException);
			}
			else
			{
				throw modelSavingException;
			}
		}
		catch (final AmbiguousIdentifierException ambigIdentifyException)
		{
			throw new DuplicateUidException(b2bcustomerModel.getUid(), ambigIdentifyException);
		}
	}

	/**
	 * Used to save address details of customer
	 *
	 * @param customerModel
	 *           customerModel
	 * @param addressModel
	 *           addressModel
	 */
	@Override
	public void saveAddressesEntry(final CorporateB2BCustomerModel b2bcustomerModel, final AddressModel addressModel)
	{
		final List<AddressModel> customerAddresses = new ArrayList<>();
		if (b2bcustomerModel.getAddresses() != null)
		{
			customerAddresses.addAll(b2bcustomerModel.getAddresses());
			if (!customerAddresses.contains(addressModel))
			{
				addressModel.setBillingAddress(Boolean.valueOf(true));
				addressModel.setOwner(b2bcustomerModel);
				getModelService().save(addressModel);
				getModelService().refresh(addressModel);
				customerAddresses.add(addressModel);
			}
		}
		else
		{
			addressModel.setBillingAddress(Boolean.valueOf(true));
			addressModel.setOwner(b2bcustomerModel);
			getModelService().save(addressModel);
			getModelService().refresh(addressModel);
			customerAddresses.add(addressModel);
		}
		b2bcustomerModel.setAddresses(customerAddresses);
	}



}
