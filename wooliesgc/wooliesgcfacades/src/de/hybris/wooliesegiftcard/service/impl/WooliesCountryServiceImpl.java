/**
 *
 */
package de.hybris.wooliesegiftcard.service.impl;

import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commercefacades.user.data.RegisterData;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.wooliesegiftcard.dao.impl.WooliesDefaultCustomerDao;
import de.hybris.wooliesegiftcard.service.WooliesCountryService;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;


/**
 * @author 648156 This class is used to maintain country service
 */
public class WooliesCountryServiceImpl implements WooliesCountryService
{
	private WooliesDefaultCustomerDao wooliesDefaultCustomerDao;
	private CommonI18NService commonI18NService;
	private ModelService modelService;
	private static final String FOUND = "found.";

	/**
	 * @return the commonI18NService
	 */
	public CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	/**
	 * @param commonI18NService
	 *           the commonI18NService to set
	 */
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	/**
	 * @return the modelService
	 */
	public ModelService getModelService()
	{
		return modelService;
	}

	/**
	 * @param modelService
	 *           the modelService to set
	 */
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	@Override
	public List<CountryModel> getCountry(final String countryName)

	{
		return wooliesDefaultCustomerDao.getCountriesByName(countryName);
	}

	/**
	 * @return the wooliesDefaultCustomerDao
	 */
	public WooliesDefaultCustomerDao getWooliesDefaultCustomerDao()
	{
		return wooliesDefaultCustomerDao;
	}

	/**
	 * @param wooliesDefaultCustomerDao
	 *           the wooliesDefaultCustomerDao to set
	 */
	public void setWooliesDefaultCustomerDao(final WooliesDefaultCustomerDao wooliesDefaultCustomerDao)
	{
		this.wooliesDefaultCustomerDao = wooliesDefaultCustomerDao;
	}

	/**
	 * This method is used to get country for the user
	 *
	 * @param object
	 */
	@Override
	public void getCountryForUser(final Object object)//(final Object object)
	{
		final AddressModel addressModel1 = getModelService().create(AddressModel.class);
		final List<AddressData> addressData = new ArrayList<>();
		populateAddressData(object, addressData);
		for (final AddressData address : addressData)
		{

			if (address.getCountry() != null)
			{
				if (address.getCountry().getIsocode() != null && address.getCountry().getName() == null)
				{

					getCountryByAddress(address, addressModel1);
				}
				else if (address.getCountry().getIsocode() == null && address.getCountry().getName() != null)
				{
					getIsocodeByCountryName(address, addressModel1);

				}
				else if (address.getCountry().getIsocode() != null && address.getCountry().getName() != null)
				{
					getCountryByAddress(address, addressModel1);
				}
			}
		}
	}

	/**
	 * @param object
	 * @param addressData
	 */
	private void populateAddressData(final Object object, final List<AddressData> addressData)
	{
		if (object instanceof CustomerData)
		{
			addressData.addAll(((CustomerData) object).getAddresses());
		}
		else if (object instanceof RegisterData)
		{
			addressData.addAll(((RegisterData) object).getAddresses());
		}
	}

	/**
	 * @param address
	 * @param addressModel1
	 */
	private void getIsocodeByCountryName(final AddressData address, final AddressModel addressModel1)
	{
		final String name = address.getCountry().getName();
		try
		{
			final List<CountryModel> country = getCountry(name);
			if (CollectionUtils.isNotEmpty(country))
			{
				addressModel1.setCountry(country.get(0));
				address.getCountry().setIsocode(country.get(0).getIsocode());
			}
		}
		catch (final UnknownIdentifierException e)
		{
			throw new ConversionException("No country with the name " + name + FOUND, e);
		}
		catch (final AmbiguousIdentifierException e)
		{
			throw new ConversionException("More than one country with the name " + name + FOUND, e);
		}

	}

	/**
	 * @param address
	 * @param addressModel1
	 */
	private void getCountryByAddress(final AddressData address, final AddressModel addressModel1)
	{
		final String isocode = address.getCountry().getIsocode();
		try
		{

			final CountryModel countryModel = getCommonI18NService().getCountry(isocode);
			addressModel1.setCountry(countryModel);
		}
		catch (final UnknownIdentifierException e)
		{
			throw new ConversionException("No country with the code " + isocode + FOUND, e);
		}
		catch (final AmbiguousIdentifierException e)
		{
			throw new ConversionException("More than one country with the code " + isocode + FOUND, e);
		}
	}

}
