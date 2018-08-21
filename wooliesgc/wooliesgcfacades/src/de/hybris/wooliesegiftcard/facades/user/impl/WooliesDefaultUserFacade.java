/**
 *
 */
package de.hybris.wooliesegiftcard.facades.user.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.impl.DefaultUserFacade;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.wooliesegiftcard.core.impl.WooliesDefaultCustomerAccountService;
import de.hybris.wooliesegiftcard.facade.WooliesUserFacade;
import de.hybris.wooliesegiftcard.facades.user.converters.populator.WooliesAddressReversePopulator;
import de.hybris.wooliesegiftcard.utility.WooliesCustomerUtility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author 668982
 *
 */
public class WooliesDefaultUserFacade extends DefaultUserFacade implements WooliesUserFacade
{
	private Converter<AddressData, AddressModel> addressReverseConverter;
	@Autowired
	private WooliesAddressReversePopulator wooliesAddressReversePopulator;

	private WooliesCustomerUtility wooliesCustomerUtility;
	@Autowired
	private WooliesDefaultCustomerAccountService customerAccountService;

	private static final String ADDRESSDATA = "addressData";

	/**
	 * @return the wooliesCustomerUtility
	 */
	public WooliesCustomerUtility getWooliesCustomerUtility()
	{
		return wooliesCustomerUtility;
	}

	/**
	 * @param wooliesCustomerUtility
	 *           the wooliesCustomerUtility to set
	 */
	public void setWooliesCustomerUtility(final WooliesCustomerUtility wooliesCustomerUtility)
	{
		this.wooliesCustomerUtility = wooliesCustomerUtility;
	}

	/**
	 * @return the addressReverseConverter
	 */
	public Converter<AddressData, AddressModel> getAddressReverseConverter()
	{
		return addressReverseConverter;
	}

	/**
	 * @param addressReverseConverter
	 *           the addressReverseConverter to set
	 */
	public void setAddressReverseConverter(final Converter<AddressData, AddressModel> addressReverseConverter)
	{
		this.addressReverseConverter = addressReverseConverter;
	}

	/**
	 * This method is used to add address
	 *
	 * @param addressData
	 */
	@Override
	public void addAddress(final AddressData addressData)
	{
		validateParameterNotNullStandardMessage(ADDRESSDATA, addressData);

		final CustomerModel currentCustomer = getCurrentUserForCheckout();
		final AddressModel newAddress = getModelService().create(AddressModel.class);
		getAddressReverseConverter().convert(addressData, newAddress);

		// Store the address against the user
		getCustomerAccountService().saveAddressEntry(currentCustomer, newAddress);
		final Collection<AddressModel> addresses = currentCustomer.getAddresses();
		for (final AddressModel address : addresses)
		{
			if ((address.getShippingAddress().booleanValue()) && null == currentCustomer.getDefaultShipmentAddress())
			{
				currentCustomer.setDefaultShipmentAddress(address);
				addressData.setDefaultAddress(true);
				getModelService().save(currentCustomer);
				getModelService().refresh(currentCustomer);
			}
		}
		addressData.setAddressID(newAddress.getPk().toString());

		if (addressData.isDefaultAddress())
		{
			customerAccountService.setDefaultAddressEntry(currentCustomer, newAddress);
		}
	}

	/**
	 * This method is used to edit address
	 *
	 * @param addressData
	 */
	@Override
	public void editAddress(final AddressData addressData)
	{
		validateParameterNotNullStandardMessage(ADDRESSDATA, addressData);
		final CustomerModel currentCustomer = getCurrentUserForCheckout();
		final AddressModel addressModel = getCustomerAccountService().getAddressForCode(currentCustomer, addressData.getId());
		if (addressModel != null)
		{
			addressModel.setRegion(null);
			wooliesAddressReversePopulator.populate(addressData, addressModel);
			getCustomerAccountService().saveAddressEntry(currentCustomer, addressModel);
			if (addressData.isDefaultAddress())
			{
				getCustomerAccountService().setDefaultAddressEntry(currentCustomer, addressModel);
			}
			else if (addressModel.equals(currentCustomer.getDefaultShipmentAddress()))
			{
				customerAccountService.clearDefaultShipmentAddressEntry(currentCustomer);
			}
			else if (addressModel.equals(currentCustomer.getDefaultPaymentAddress()))
			{
				customerAccountService.clearDefaultPaymentAddressEntry(currentCustomer);
			}
		}

	}

	/**
	 * This method is used to get the address model for the given address id
	 *
	 * @param addressId
	 * @return addressData
	 */
	@Override
	public AddressData getAddressModelForAddressId(final String addressId)
	{
		final CustomerModel currentCustomer = (CustomerModel) getUserService().getCurrentUser();
		final AddressModel defaultPaymentAddress = currentCustomer.getDefaultPaymentAddress();
		final AddressModel defaultShipmenttAddress = currentCustomer.getDefaultShipmentAddress();
		final Collection<AddressModel> addresses = currentCustomer.getAddresses();
		if (null != addresses && CollectionUtils.isNotEmpty(addresses))
		{
			for (final AddressModel address : addresses)
			{
				if (address.getPk().toString().equalsIgnoreCase(addressId))
				{
					final AddressData addressData = getAddressConverter().convert(address);
					setDefaultAddress(defaultPaymentAddress, defaultShipmenttAddress, address, addressData);
					return addressData;
				}
			}
		}
		return null;

	}

	/**
	 * @param defaultPaymentAddress
	 * @param defaultShipmenttAddress
	 * @param address
	 * @param addressData
	 */
	private void setDefaultAddress(final AddressModel defaultPaymentAddress, final AddressModel defaultShipmenttAddress,
			final AddressModel address, final AddressData addressData)
	{
		if (address == defaultPaymentAddress || address == defaultShipmenttAddress)
		{
			addressData.setDefaultAddress(true);
		}
	}

	/**
	 * This method is used to get woolies billing address book
	 *
	 * @return the billing addressData
	 */
	@Override
	public List<AddressData> getWooliesBillingAddressBook()
	{
		// Get the current customer's addresses
		final CustomerModel currentUser = (CustomerModel) getUserService().getCurrentUser();
		final Collection<AddressModel> addresses = getCustomerAccountService().getAllAddressEntries(currentUser);
		if (CollectionUtils.isNotEmpty(addresses))
		{
			final List<AddressData> result = new ArrayList<>();
			final AddressData defaultAddress = getDefaultPaymentAddress();

			for (final AddressModel address : addresses)
			{
				if (address.getBillingAddress().booleanValue())
				{
					final AddressData addressData = getAddressConverter().convert(address);

					setShippingAddress(result, defaultAddress, addressData);
				}
			}
			return result;
		}
		return Collections.emptyList();
	}

	/**
	 * This method is used to get woolies shilling address book
	 *
	 * @return the shipping addressData
	 */
	@Override
	public List<AddressData> getWooliesShippingAddressBook()
	{
		// Get the current customer's addresses
		final CustomerModel currentUser = (CustomerModel) getUserService().getCurrentUser();
		final Collection<AddressModel> addresses = getCustomerAccountService().getAllAddressEntries(currentUser);
		if (CollectionUtils.isNotEmpty(addresses))
		{
			final List<AddressData> result = new ArrayList<>();
			final AddressData defaultAddress = getDefaultShipmentAddress();

			for (final AddressModel address : addresses)
			{
				if (address.getShippingAddress().booleanValue())
				{
					final AddressData addressData = getAddressConverter().convert(address);
					setShippingAddress(result, defaultAddress, addressData);
				}
			}
			return result;
		}
		return Collections.emptyList();
	}

	/**
	 * @param result
	 * @param defaultAddress
	 * @param addressData
	 */
	private void setShippingAddress(final List<AddressData> result, final AddressData defaultAddress,
			final AddressData addressData)
	{
		if (defaultAddress != null && defaultAddress.getId() != null && defaultAddress.getId().equals(addressData.getId()))
		{
			addressData.setDefaultAddress(true);
			result.add(0, addressData);
		}
		else
		{
			result.add(addressData);
		}
	}

	/**
	 * This method is used to get woolies address book
	 *
	 * @return the shipping addressData
	 */
	@Override
	public List<AddressData> getWooliesAddressBook()
	{
		final List<AddressData> addresses = new ArrayList<>();
		final List<AddressData> billingAddreses = getWooliesBillingAddressBook();
		final List<AddressData> shippingAddreses = getWooliesShippingAddressBook();
		if (CollectionUtils.isNotEmpty(billingAddreses))
		{
			addresses.addAll(billingAddreses);
		}
		if (CollectionUtils.isNotEmpty(shippingAddreses))
		{
			addresses.addAll(shippingAddreses);
		}
		return addresses;
	}

	/**
	 * This method is used to remove address
	 *
	 * @param addressData
	 */
	@Override
	public void removeAddress(final AddressData addressData)
	{
		validateParameterNotNullStandardMessage(ADDRESSDATA, addressData);
		final CustomerModel currentCustomer = getCurrentUserForCheckout();
		for (final AddressModel addressModel : getCustomerAccountService().getAllAddressEntries(currentCustomer))
		{
			if (addressData.getId().equals(addressModel.getPk().getLongValueAsString()))
			{
				getCustomerAccountService().deleteAddressEntry(currentCustomer, addressModel);
				break;
			}
		}
	}

	/**
	 * This method is used to get the default shipment address
	 *
	 * @return addressData
	 */
	@Override
	public AddressData getDefaultShipmentAddress()
	{
		final CustomerModel currentCustomer = (CustomerModel) getUserService().getCurrentUser();
		AddressData defaultAddressData = null;
		if (null != currentCustomer.getDefaultShipmentAddress())
		{
			final AddressModel defaultAddress = customerAccountService.getDefaultShipmentAddress(currentCustomer);
			if (defaultAddress != null)
			{
				defaultAddressData = getAddressConverter().convert(defaultAddress);
			}
		}
		return defaultAddressData;
	}

	/**
	 * To get default payment address
	 *
	 * @return defaultAddressData
	 */
	public AddressData getDefaultPaymentAddress()
	{
		final CustomerModel currentCustomer = (CustomerModel) getUserService().getCurrentUser();
		AddressData defaultAddressData = null;
		if (null != currentCustomer.getDefaultPaymentAddress())
		{
			final AddressModel defaultAddress = customerAccountService.getDefaultPaymentAddress(currentCustomer);

			if (defaultAddress != null)
			{
				defaultAddressData = getAddressConverter().convert(defaultAddress);
			}
		}
		return defaultAddressData;
	}
}