/**
 *
 */
package de.hybris.wooliesegiftcard.facades.order.converters.populator;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import de.hybris.model.EGiftCardModel;
import de.hybris.model.PersonalisationEGiftCardModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.cmsfacades.data.MediaData;
import de.hybris.platform.commercefacades.product.data.PriceData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.media.impl.DefaultMediaDao;
import de.hybris.wooliesegiftcard.core.EncryptionUtils;
import de.hybris.wooliesegiftcard.core.constants.GeneratedWooliesgcCoreConstants.Enumerations.ImageApprovalStatus;
import de.hybris.wooliesegiftcard.facades.GiftCardResponseData;
import de.hybris.wooliesegiftcard.facades.eGiftCardData;
import de.hybris.wooliesegiftcard.facades.constants.WooliesgcFacadesConstants;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author 653154
 *
 */
public class WooliesGiftCardPopulator implements Populator<EGiftCardModel, GiftCardResponseData>
{
	private static final Logger LOG = Logger.getLogger(WooliesGiftCardPopulator.class);
	@Autowired
	private ConfigurationService configurationService;
	private static final String CHARACTERENCODING = "UTF-8";
	@Autowired
	private Converter<MediaModel, MediaData> wooliesMediaModelConverter;
	@Autowired
	private Converter<MediaModel, MediaData> mediaModelConverter;
	@Autowired
	private DefaultMediaDao mediaDao;
	@Autowired
	private CommonI18NService commonI18NService;


	@Override
	public void populate(final EGiftCardModel source, final GiftCardResponseData target) throws ConversionException
	{
		validateParameterNotNullStandardMessage("source", source);
		final String encypKey = configurationService.getConfiguration().getString("encryption.key");
		final String absolutePath = configurationService.getConfiguration().getString("website.ycomdev-giftcards.https");
		try
		{
			final byte[] encryptionKey = encypKey.getBytes(CHARACTERENCODING);
			if (source.getGiftCardNumber() != null)
			{
				target.setGiftCardNumber(source.getGiftCardNumber());
			}
			if (source.getPin() != null)
			{
				target.setPin(EncryptionUtils.decrypt(source.getPin(), encryptionKey));
			}
			if (source.getGiftCardNumber() != null)
			{
				target.setInStoreCardNumber(formatString(source.getGiftCardNumber()));
			}
			if (source.getPersonalisationGiftCard() != null
					&& source.getPersonalisationGiftCard().getOrderEntry().getProduct().getPicture() != null)
			{
				target.setImageURL(
						absolutePath + source.getPersonalisationGiftCard().getOrderEntry().getProduct().getPicture().getURL());
			}

			final PriceData amount = new PriceData();
			amount.setValue(BigDecimal.valueOf(source.getGiftCardValue().doubleValue()));
			amount.setCurrencyIso(commonI18NService.getCurrentCurrency().getIsocode());
			target.setAmount(amount);
			final PersonalisationEGiftCardModel pGiftCard = source.getPersonalisationGiftCard();
			if (pGiftCard != null)
			{
				setPersonalisationDetails(source, target, absolutePath, pGiftCard);
			}
		}
		catch (final UnsupportedEncodingException e)
		{
			LOG.error("Exception in WooliesGiftCardPopulator", e);
		}
	}

	/**
	 * @param source
	 * @param target
	 * @param absolutePath
	 * @param pGiftCard
	 */
	private void setPersonalisationDetails(final EGiftCardModel source, final GiftCardResponseData target,
			final String absolutePath, final PersonalisationEGiftCardModel pGiftCard)
	{
		final eGiftCardData giftCardData = new eGiftCardData();
		if (null != pGiftCard.getFromName())
		{
			giftCardData.setFromName(pGiftCard.getFromName());
		}
		if (null != pGiftCard.getToName())
		{
			giftCardData.setToName(pGiftCard.getToName());
		}
		if (null != pGiftCard.getMessage())
		{
			giftCardData.setMessage(pGiftCard.getMessage());
		}
		if (null != pGiftCard.getToEmail())
		{
			giftCardData.setToEmail(pGiftCard.getToEmail());
		}
		if (null != pGiftCard.getCustomerImage())
		{
			if (pGiftCard.getCustomerImage().getImageApprovalStatus().getCode().equalsIgnoreCase(ImageApprovalStatus.APPROVED))
			{
				giftCardData.setCustomImage(wooliesMediaModelConverter.convert(pGiftCard.getCustomerImage()));
			}
			else
			{
				loadImage(source, giftCardData, absolutePath);
			}
		}
		else
		{
			loadImage(source, giftCardData, absolutePath);
		}
		target.setPersonalisationGiftCard(giftCardData);
	}

	/**
	 * @param source
	 * @param giftCardData
	 */
	private void loadImage(final EGiftCardModel source, final eGiftCardData giftCardData, final String absolutePath)
	{
		if (source.getPersonalisationGiftCard() != null)
		{
			final CatalogVersionModel cVersion = source.getPersonalisationGiftCard().getOrderEntry().getProduct()
					.getCatalogVersion();
			final String defaultImage = configurationService.getConfiguration()
					.getString(WooliesgcFacadesConstants.DEFAULTIMAGEPROPERTY, WooliesgcFacadesConstants.DEFAULTIMAGE);
			final List<MediaModel> mediaModel = mediaDao.findMediaByCode(cVersion, defaultImage);
			if (CollectionUtils.isNotEmpty(mediaModel))
			{
				final MediaData defaultMedia = mediaModelConverter.convert(mediaModel.get(0));
				defaultMedia.setUrl(absolutePath + defaultMedia.getUrl());
				giftCardData.setDefaultImage(defaultMedia);
			}
		}
	}

	/**
	 * @param giftCardNumber
	 * @return String
	 */
	private static String formatString(final Long giftCardNumber)
	{
		String str = giftCardNumber.toString();
		str = str.substring(str.length() - WooliesgcFacadesConstants.TEN);
		final StringBuilder sb = new StringBuilder(str);
		sb.insert(WooliesgcFacadesConstants.TWO, "-");
		sb.insert(WooliesgcFacadesConstants.FIVE, "-");
		sb.insert(WooliesgcFacadesConstants.NINE, "-");
		return str;
	}

}
