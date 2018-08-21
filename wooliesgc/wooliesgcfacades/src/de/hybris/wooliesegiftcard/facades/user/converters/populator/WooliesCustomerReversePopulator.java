/**
 *
 */
package de.hybris.wooliesegiftcard.facades.user.converters.populator;

import de.hybris.platform.commercefacades.user.converters.populator.CustomerReversePopulator;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.wooliesegiftcard.utility.WooliesCustomerUtility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.util.Assert;


/**
 * @author 669567 This class populate customer reverse
 */
public class WooliesCustomerReversePopulator extends CustomerReversePopulator
{
	private Converter<AddressData, AddressModel> addressReverseConverter;
	private static final Logger LOG = Logger.getLogger(WooliesCustomerReversePopulator.class);
	private WooliesCustomerUtility wooliesCustomerUtility;


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
	 * This method is used to populate customer reverse
	 *
	 * @param source
	 *           CustomerData as source
	 * @param target
	 *           CustomerModel as Target
	 * @throws ConversionException
	 *            throws this Exception
	 */
	@Override
	public void populate(final CustomerData source, final CustomerModel target) throws ConversionException
	{
		Assert.notNull(source, "Parameter source cannot be null.");
		Assert.notNull(target, "Parameter target cannot be null.");
		if (source.getDob() != null)
		{
			try
			{
				final SimpleDateFormat formatter = new SimpleDateFormat("dd/mm/yyyy");
				target.setDob(formatter.parse(source.getDob()));

			}
			catch (final ParseException e)
			{
				LOG.error("Exception occurred", e);
			}
		}
		if (null != source.getPhone())
		{
			target.setPhone(source.getPhone());
		}
		if (null != source.getCardNo())
		{
			target.setCardNo(source.getCardNo());
		}
		final List<AddressModel> customerAddresses = convertAddress(source, target);
		target.setAddresses(customerAddresses);
		target.setName(getCustomerNameStrategy().getName(source.getFirstName(), source.getLastName()));
		setUid(source, target);
	}

	/**
	 * @param source
	 *           The CustomerData as Source
	 * @param target
	 *           The CustomerModel as target
	 * @return returns List<AddressModel>
	 */
	private List<AddressModel> convertAddress(final CustomerData source, final CustomerModel target)
	{
		final List<AddressModel> customerAddresses = new ArrayList<>();
		customerAddresses.addAll(target.getAddresses());
		for (final AddressData addressdata : source.getAddresses())
		{
			if (null != addressdata && !defaultAddressExist(addressdata, customerAddresses))
			{
				if (!ifAddressExist(addressdata, customerAddresses))
				{
					final AddressModel address = getModelService().create(AddressModel.class);
					addressReverseConverter.convert(addressdata, address);
					address.setOwner(target);
					getModelService().save(address);
					getModelService().refresh(address);
					customerAddresses.add(address);

				}
				else
				{
					LOG.error("Address Already Exist in System.Updated the existing Address");
				}
			}
			else
			{
				throw new ConversionException("Default Address Already Available");
			}
		}
		return customerAddresses;
	}

	/**
	 * This method is used to check whether address is exist or not
	 *
	 * @param addressdata
	 *           the Address data for the User
	 * @param customerAddresses
	 *           List of Customer Addresses
	 * @return isAddressExist a boolean value
	 */
	private boolean ifAddressExist(final AddressData addressdata, final List<AddressModel> customerAddresses)
	{
		boolean isAddressExist = false;
		for (final AddressModel addressModel : customerAddresses)
		{
			if (null != addressModel)
			{
				if (null != addressdata.getAddressID()
						&& addressdata.getAddressID().equalsIgnoreCase(addressModel.getPk().toString()))
				{
					addressReverseConverter.convert(addressdata, addressModel);
					getModelService().save(addressModel);
					getModelService().refresh(addressModel);
					isAddressExist = true;
					break;
				}
				else
				{
					isAddressExist = false;
				}
			}

		}
		return isAddressExist;
	}

	/**
	 * This method is used to check whether default address exist or not
	 *
	 * @param addressdata
	 *           the Address data for the User
	 * @param customerAddresses
	 *           List of Customer Addresses
	 * @return isDefaulAddressExist a boolean value
	 */
	private boolean defaultAddressExist(final AddressData addressdata, final List<AddressModel> customerAddresses)
	{
		boolean isDefaulAddressExist = false;
		for (final AddressModel addressModel : customerAddresses)
		{
			if (null != addressModel && addressdata.isDefaultAddress())
			{
				if (addressModel.getBillingAddress().booleanValue() || addressModel.getShippingAddress().booleanValue())
				{
					isDefaulAddressExist = true;
					break;
				}
			}
			else
			{
				isDefaulAddressExist = false;
				break;
			}

		}
		return isDefaulAddressExist;
	}




}
