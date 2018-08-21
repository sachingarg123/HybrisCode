/**
 *
 */
package de.hybris.wooliesegiftcard.facade.impl;

import de.hybris.model.PersonalisationEGiftCardModel;
import de.hybris.model.PersonalisationMediaModel;
import de.hybris.platform.commercefacades.customer.impl.DefaultCustomerFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.commerceservices.enums.CustomerType;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.wooliesegiftcard.core.enums.UserDataType;
import de.hybris.wooliesegiftcard.core.impl.WooliesDefaultCustomerAccountService;
import de.hybris.wooliesegiftcard.facade.WooliesGuestUserFacade;
import de.hybris.wooliesegiftcard.facades.constants.WooliesgcFacadesConstants;
import de.hybris.wooliesegiftcard.service.impl.WooliesCountryServiceImpl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author 648156 This class is used to maintain details of the guest user
 */
public class WooliesGuestUserFacadeImpl extends DefaultCustomerFacade implements WooliesGuestUserFacade
{
	private Converter<AddressData, AddressModel> addressReverseConverter;
	private WooliesDefaultCustomerAccountService wooliesCustomerAccountService;
	private WooliesCountryServiceImpl wooliesCountryServiceImpl;
	private static final Logger LOG = Logger.getLogger(WooliesGuestUserFacadeImpl.class);

	/**
	 * @return the wooliesCountryServiceImpl
	 */
	public WooliesCountryServiceImpl getWooliesCountryServiceImpl()
	{
		return wooliesCountryServiceImpl;
	}

	/**
	 * @param wooliesCountryServiceImpl
	 *           the wooliesCountryServiceImpl to set
	 */
	public void setWooliesCountryServiceImpl(final WooliesCountryServiceImpl wooliesCountryServiceImpl)
	{
		this.wooliesCountryServiceImpl = wooliesCountryServiceImpl;
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
	 * @return the wooliesCustomerAccountService
	 */
	public WooliesDefaultCustomerAccountService getWooliesCustomerAccountService()
	{
		return wooliesCustomerAccountService;
	}

	/**
	 * @param wooliesCustomerAccountService
	 *           the wooliesCustomerAccountService to set
	 */
	public void setWooliesCustomerAccountService(final WooliesDefaultCustomerAccountService wooliesCustomerAccountService)
	{
		this.wooliesCustomerAccountService = wooliesCustomerAccountService;
	}



	/**
	 * This method is used to create guest user for anonymous checkout for the give customer data
	 *
	 * @param guestCustomerData
	 * @param name
	 * @throws DuplicateUidException
	 */
	@Override
	public void createGuestUserForAnonymousCheckout(final CustomerData guestCustomerData, final String name,
			final CartModel cartModel) throws DuplicateUidException
	{
		final CustomerModel guestCustomer = getModelService().create(CustomerModel.class);
		if (StringUtils.isNotBlank(guestCustomerData.getFirstName()) && StringUtils.isNotBlank(guestCustomerData.getLastName()))
		{
			guestCustomer.setName(getCustomerNameStrategy().getName(guestCustomerData.getFirstName(),
					guestCustomerData.getLastName()));
		}
		if (guestCustomerData.getDob() != null)
		{
			final SimpleDateFormat formatter = new SimpleDateFormat(WooliesgcFacadesConstants.DOB);

			try
			{
				guestCustomer.setDob(formatter.parse(guestCustomerData.getDob()));

			}
			catch (final ParseException e)
			{
				LOG.error("Exception occurred", e);
			}
		}
		guestCustomer.setUid(guestCustomer.getCustomerID());
		guestCustomer.setPhone(guestCustomerData.getPhone());
		guestCustomer.setType(CustomerType.valueOf(CustomerType.GUEST.getCode()));
		guestCustomer.setSessionLanguage(getCommonI18NService().getCurrentLanguage());
		guestCustomer.setSessionCurrency(getCommonI18NService().getCurrentCurrency());
		guestCustomer.setUserEmail(guestCustomerData.getEmail());
		guestCustomer.setCustomerType(UserDataType.B2C);
		wooliesCountryServiceImpl.getCountryForUser(guestCustomerData);
		final List<AddressModel> addresses = getAddressReverseConverter().convertAll(guestCustomerData.getAddresses());
		if (addresses != null && !addresses.isEmpty())
		{
			for (final AddressModel addressModel : addresses)
			{
				addressModel.setFirstname(guestCustomerData.getFirstName());
				addressModel.setLastname(guestCustomerData.getLastName());
				addressModel.setPhone1(guestCustomerData.getPhone());
				wooliesCustomerAccountService.saveAddressesEntry(guestCustomer, addressModel);
			}
		}

		wooliesCustomerAccountService.registerGuestForAnonymousCheckout(guestCustomer);
		updateCartWithGuestForAnonymousCheckout(getCustomerConverter().convert(guestCustomer));
		final List<AbstractOrderEntryModel> entries = cartModel.getEntries();
		final Set<PersonalisationMediaModel> mediaImages = new HashSet<>();
		for (final AbstractOrderEntryModel abstractOrderEntryModel : entries)
		{
			setMediaImages(guestCustomer, mediaImages, abstractOrderEntryModel);

		}
		if (CollectionUtils.isNotEmpty(mediaImages))
		{
			guestCustomer.setGiftCardMedias(mediaImages);
			getModelService().save(guestCustomer);
		}
	}

	/**
	 * This is used to set media images for guest user
	 *
	 * @param guestCustomer
	 * @param mediaImages
	 * @param abstractOrderEntryModel
	 */
	private void setMediaImages(final CustomerModel guestCustomer, final Set<PersonalisationMediaModel> mediaImages,
			final AbstractOrderEntryModel abstractOrderEntryModel)
	{
		if (abstractOrderEntryModel.getProduct().getIsEGiftCard().booleanValue()
				&& CollectionUtils.isNotEmpty(abstractOrderEntryModel.getPersonalisationDetail()))
		{
			final List<PersonalisationEGiftCardModel> personalisationDetail = abstractOrderEntryModel.getPersonalisationDetail();
			for (final PersonalisationEGiftCardModel personalisationEGiftCardModel : personalisationDetail)
			{
				if (personalisationEGiftCardModel.getCustomerImage() != null)
				{
					final PersonalisationMediaModel customImage = personalisationEGiftCardModel.getCustomerImage();
					customImage.setUser(guestCustomer);
					getModelService().save(customImage);
					mediaImages.add(customImage);
				}

			}
		}
	}


}
