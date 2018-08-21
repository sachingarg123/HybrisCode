/**
 *
 */
package de.hybris.wooliesegiftcard.facades.order.converters.populator;

import de.hybris.model.PersonalisationEGiftCardModel;
import de.hybris.model.PersonalisationMediaModel;
import de.hybris.platform.cmsfacades.data.MediaData;
import de.hybris.platform.commercefacades.order.converters.populator.OrderEntryPopulator;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.data.PriceDataType;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.media.impl.DefaultMediaDao;
import de.hybris.platform.util.DiscountValue;
import de.hybris.wooliesegiftcard.core.enums.UserDataType;
import de.hybris.wooliesegiftcard.core.model.CoBrandImageModel;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BCustomerModel;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BUnitModel;
import de.hybris.wooliesegiftcard.facades.eGiftCardData;
import de.hybris.wooliesegiftcard.facades.product.data.CoBrandImageData;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author 668982 This class is used to populate order entry
 */
public class WooliesOrderEntryPopulator extends OrderEntryPopulator
{
	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private Converter<MediaModel, MediaData> wooliesMediaModelConverter;
	@Autowired
	private Converter<MediaModel, MediaData> mediaModelConverter;

	@Autowired
	private DefaultMediaDao mediaDao;


	/**
	 * This method is used to populate order entry for the give data
	 *
	 * @param source
	 * @param target
	 */
	@Override
	public void populate(final AbstractOrderEntryModel source, final OrderEntryData target)
	{
		final String absolutePath = configurationService.getConfiguration().getString("website.woolworths.giftcards.https");
		final List<DiscountValue> discounts = source.getDiscountValues();
		double totalDiscounts = 0;
		if (!CollectionUtils.isEmpty(discounts))
		{
			for (final DiscountValue discountValue : discounts)
			{
				totalDiscounts += discountValue.getValue();
			}
		}
		if (source.getBasePrice() != null)
		{
			target.setSalesPrice(getPriceDataFactory().create(PriceDataType.BUY,
					BigDecimal.valueOf(source.getBasePrice().doubleValue() - totalDiscounts), source.getOrder().getCurrency()));
		}
		if (source.getProduct().getIsEGiftCard().booleanValue())
		{
			setPersonalisationEGiftCard(source, target, absolutePath);
		}
		else
		{
			setCoBrandImage(source, target, absolutePath);
		}
		target.setIsEGiftCard(source.getProduct().getIsEGiftCard());
		target.setCanPersonalize(source.getProduct().getIsEGiftCard());
	}

	/**
	 * @param source
	 * @param target
	 * @param absolutePath
	 */
	private void setCoBrandImage(final AbstractOrderEntryModel source, final OrderEntryData target, final String absolutePath)
	{
		final CustomerModel customer = (CustomerModel) source.getOrder().getUser();
		final List<CoBrandImageData> coBrandMedia = new ArrayList<>();
		if (customer.getCustomerType() != null
				&& customer.getCustomerType().getCode().equalsIgnoreCase(UserDataType.B2B.toString()))
		{
			if (customer instanceof CorporateB2BCustomerModel)
			{
				final CorporateB2BCustomerModel corporateb2bCustomer = (CorporateB2BCustomerModel) customer;

				if (CollectionUtils.isNotEmpty(((CorporateB2BUnitModel) corporateb2bCustomer.getDefaultB2BUnit()).getCoBrandImages()))
				{
					final Collection<CoBrandImageModel> coBrandImages = ((CorporateB2BUnitModel) corporateb2bCustomer
							.getDefaultB2BUnit()).getCoBrandImages();
					final String code = source.getProduct().getCode();
					setImages(source, absolutePath, coBrandMedia, coBrandImages, code);
				}
			}
			target.setCoBrandMedia(coBrandMedia);
		}
	}

	/**
	 * @param source
	 * @param absolutePath
	 * @param coBrandMedia
	 * @param coBrandImages
	 * @param code
	 */
	private void setImages(final AbstractOrderEntryModel source, final String absolutePath,
			final List<CoBrandImageData> coBrandMedia, final Collection<CoBrandImageModel> coBrandImages, final String code)
	{
		for (final CoBrandImageModel model : coBrandImages)
		{
			if (code.equalsIgnoreCase(model.getProduct().getCode()))
			{
				final CoBrandImageData data = new CoBrandImageData();
				data.setImageID(model.getImageID());
				data.setUrl(absolutePath + model.getURL());
				data.setCode(model.getCode());
				if (source.getCoBrandID() != null && StringUtils.isNotEmpty(source.getCoBrandID())
						&& source.getCoBrandID().equalsIgnoreCase(model.getImageID()))
				{
					data.setSelected(Boolean.TRUE);
				}
				else
				{
					data.setSelected(Boolean.FALSE);
				}
				coBrandMedia.add(data);
			}

		}
	}

	/**
	 * @param source
	 * @param target
	 * @param absolutePath
	 */
	private void setPersonalisationEGiftCard(final AbstractOrderEntryModel source, final OrderEntryData target,
			final String absolutePath)
	{
		final List<eGiftCardData> egiftCardData = new ArrayList();
		final List<Integer> pidList = new ArrayList();
		if (source.getPersonalisationDetail() != null)
		{
			final Collection<PersonalisationEGiftCardModel> giftCards = source.getPersonalisationDetail();
			for (final PersonalisationEGiftCardModel personalisationEGiftCardModel : giftCards)
			{
				final eGiftCardData giftCardData = new eGiftCardData();
				if (personalisationEGiftCardModel.getPid() != null)
				{
					giftCardData.setPID(personalisationEGiftCardModel.getPid());
					pidList.add(personalisationEGiftCardModel.getPid());
				}
				if (personalisationEGiftCardModel.getMessage() != null)
				{
					giftCardData.setMessage(personalisationEGiftCardModel.getMessage());
				}
				if (personalisationEGiftCardModel.getFromName() != null)
				{
					giftCardData.setFromName(personalisationEGiftCardModel.getFromName());
				}
				final String defaultImage = configurationService.getConfiguration().getString("default.image.for.eGiftCard",
						"DefaultImageForGiftCard");
				final List<MediaModel> mediaModel = mediaDao.findMediaByCode(source.getProduct().getCatalogVersion(), defaultImage);
				if (CollectionUtils.isNotEmpty(mediaModel))
				{
					final MediaData defaultImage1 = mediaModelConverter.convert(mediaModel.get(0));
					defaultImage1.setUrl(absolutePath + defaultImage1.getUrl());
					giftCardData.setDefaultImage(defaultImage1);
				}
				setPersonalisationDetails(source, absolutePath, egiftCardData, personalisationEGiftCardModel, giftCardData);
			}
			target.setListOfPID(pidList);
			target.setCustomizeEGiftCards(egiftCardData);
		}
	}

	/**
	 * @param source
	 * @param absolutePath
	 * @param egiftCardData
	 * @param personalisationEGiftCardModel
	 * @param giftCardData
	 */
	private void setPersonalisationDetails(final AbstractOrderEntryModel source, final String absolutePath,
			final List<eGiftCardData> egiftCardData, final PersonalisationEGiftCardModel personalisationEGiftCardModel,
			final eGiftCardData giftCardData)
	{
		if (personalisationEGiftCardModel.getToName() != null)
		{
			giftCardData.setToName(personalisationEGiftCardModel.getToName());
		}
		if (personalisationEGiftCardModel.getCustomerImage() != null)
		{
			final PersonalisationMediaModel imageModel = personalisationEGiftCardModel.getCustomerImage();
			giftCardData.setCustomImage(wooliesMediaModelConverter.convert(imageModel));
		}
		if (personalisationEGiftCardModel.getToEmail() != null)
		{
			giftCardData.setToEmail(personalisationEGiftCardModel.getToEmail());
		}
		if (giftCardData.getPID() != null)
		{
			egiftCardData.add(giftCardData);
		}
		if (source.getProduct().getThumbnail() != null)
		{
			final MediaData productBanner = mediaModelConverter.convert(source.getProduct().getThumbnail());
			productBanner.setUrl(absolutePath + source.getProduct().getThumbnail().getUrl());
			giftCardData.setProductBannerImage(productBanner);
		}
	}

}
