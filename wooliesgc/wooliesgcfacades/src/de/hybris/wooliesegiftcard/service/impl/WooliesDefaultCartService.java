package de.hybris.wooliesegiftcard.service.impl;

import de.hybris.model.PersonalisationEGiftCardModel;
import de.hybris.model.PersonalisationMediaModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.deliveryzone.model.ZoneDeliveryModeValueModel;
import de.hybris.platform.order.CartFactory;
import de.hybris.platform.order.impl.DefaultCartService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.media.impl.DefaultMediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionService.SessionAttributeLoader;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.wooliesegiftcard.core.enums.UserDataType;
import de.hybris.wooliesegiftcard.core.model.CoBrandImageModel;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BCustomerModel;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BUnitModel;
import de.hybris.wooliesegiftcard.dao.impl.WooliesDefaultCartDao;
import de.hybris.wooliesegiftcard.facades.constants.WooliesgcFacadesConstants;
import de.hybris.wooliesegiftcard.facades.product.data.PersonalisationMediaData;
import de.hybris.wooliesegiftcard.service.WooliesCartService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.woolies.webservices.dto.eGiftCardWsDTO;

import sun.misc.BASE64Decoder;


/**
 * @author 668982 This class is to maintain default cart service details
 */
public class WooliesDefaultCartService extends DefaultCartService implements WooliesCartService
{
	private ConfigurationService configurationService;
	private WooliesDefaultCartDao wooliesDefaultCartDao;
	private KeyGenerator keyGenerator;
	private DefaultMediaService mediaService;
	private Converter<AbstractOrderEntryModel, OrderEntryData> orderEntryConverter;
	private static final Logger LOG = Logger.getLogger(WooliesDefaultCartService.class);
	private static final String IMG_PREFIX = "_";
	private UserService userService;
	private Converter<PersonalisationMediaModel, PersonalisationMediaData> wooliesMediaModelConverter;
	private CMSSiteService cmsSiteService;
	private KeyGenerator imageCodeGenerator;
	private transient TypeService typeService;
	@Autowired
	private ModelService modelService;
	@Autowired
	private CartFactory cartFactory;
	private KeyGenerator orderKeyGenerator;

	/**
	 * @return the typeService
	 */
	public TypeService getTypeService()
	{
		return typeService;
	}

	/**
	 * @param typeService
	 *           the typeService to set
	 */
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	/**
	 * @return the wooliesDefaultCartDao
	 */
	public WooliesDefaultCartDao getWooliesDefaultCartDao()
	{
		return wooliesDefaultCartDao;
	}

	/**
	 * @param wooliesDefaultCartDao
	 *           the wooliesDefaultCartDao to set
	 */
	public void setWooliesDefaultCartDao(final WooliesDefaultCartDao wooliesDefaultCartDao)
	{
		this.wooliesDefaultCartDao = wooliesDefaultCartDao;
	}

	/**
	 * @return the keyGenerator
	 */
	public KeyGenerator getKeyGenerator()
	{
		return keyGenerator;
	}

	/**
	 * @param keyGenerator
	 *           the keyGenerator to set
	 */
	public void setKeyGenerator(final KeyGenerator keyGenerator)
	{
		this.keyGenerator = keyGenerator;
	}


	/**
	 * @return the configurationService
	 */
	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	/**
	 * @param configurationService
	 *           the configurationService to set
	 */
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	/**
	 * This method is used to get delivery options for cart
	 *
	 * @param cart
	 * @return deliveryModes
	 */
	@Override
	public Collection<ZoneDeliveryModeValueModel> getdeliveryOptionsForCart(final AbstractOrderModel cart)
	{
		final List<AbstractOrderEntryModel> entries = cart.getEntries();

		boolean hasPlasticCard = false;
		boolean haseGiftCard = false;
		long totalQuantity = 0;
		for (final AbstractOrderEntryModel abstractOrderEntryModel : entries)
		{
			if (!abstractOrderEntryModel.getProduct().getIsEGiftCard().booleanValue())
			{
				hasPlasticCard = true;
				totalQuantity += abstractOrderEntryModel.getQuantity().longValue();
			}
			else
			{
				haseGiftCard = true;
			}
		}
		final Collection<ZoneDeliveryModeValueModel> deliveryModes = new ArrayList();
		if (hasPlasticCard && totalQuantity > 0)
		{

			final Collection<ZoneDeliveryModeValueModel> zoneDeliveryModeValueModel = getWooliesDefaultCartDao()
					.findDeliveryModes(cart, Long.valueOf(totalQuantity));
			if (zoneDeliveryModeValueModel != null && !zoneDeliveryModeValueModel.isEmpty())
			{
				deliveryModes.addAll(zoneDeliveryModeValueModel);
			}
		}
		if (haseGiftCard)
		{
			final Collection<ZoneDeliveryModeValueModel> zoneDeliveryModeValueModel = getWooliesDefaultCartDao()
					.findDeliveryModeForeGiftProduct(cart);
			if (zoneDeliveryModeValueModel != null && !zoneDeliveryModeValueModel.isEmpty())
			{
				deliveryModes.addAll(zoneDeliveryModeValueModel);
			}
		}
		return deliveryModes;
	}

	/**
	 * This method is used to generate pid
	 *
	 * @return CartModel
	 */
	@Override
	public CartModel generatePID()
	{
		final CartModel cartModel = getSessionCart();
		final List<AbstractOrderEntryModel> orderEntryModelList = cartModel.getEntries();
		if (orderEntryModelList != null && !orderEntryModelList.isEmpty())
		{
			for (final AbstractOrderEntryModel abstractOrderEntryModel : orderEntryModelList)
			{
				if (abstractOrderEntryModel.getProduct().getIsEGiftCard().booleanValue())
				{
					generatePIDForEntryModel(abstractOrderEntryModel);
				}
			}
		}

		return cartModel;
	}

	/**
	 * @return the mediaService
	 */
	public DefaultMediaService getMediaService()
	{
		return mediaService;
	}

	/**
	 * @param mediaService
	 *           the mediaService to set
	 */
	public void setMediaService(final DefaultMediaService mediaService)
	{
		this.mediaService = mediaService;
	}

	/**
	 * @return the orderEntryConverter
	 */
	public Converter<AbstractOrderEntryModel, OrderEntryData> getOrderEntryConverter()
	{
		return orderEntryConverter;
	}

	/**
	 * @param orderEntryConverter
	 *           the orderEntryConverter to set
	 */
	public void setOrderEntryConverter(final Converter<AbstractOrderEntryModel, OrderEntryData> orderEntryConverter)
	{
		this.orderEntryConverter = orderEntryConverter;
	}

	/**
	 * This method is used to generate pid for entry model
	 *
	 * @param abstractOrderEntryModel
	 *           parameter to generate PID
	 */
	public void generatePIDForEntryModel(final AbstractOrderEntryModel abstractOrderEntryModel)
	{
		if (CollectionUtils.isEmpty(abstractOrderEntryModel.getPersonalisationDetail()))
		{
			final List<PersonalisationEGiftCardModel> egiftCard = new ArrayList();

			for (long qty = 0; qty < abstractOrderEntryModel.getQuantity().longValue(); qty++)
			{
				final PersonalisationEGiftCardModel eGiftCardModel = getModelService().create(PersonalisationEGiftCardModel.class);
				eGiftCardModel.setPid(Integer.valueOf((String) this.keyGenerator.generate()));
				getModelService().save(eGiftCardModel);
				egiftCard.add(eGiftCardModel);
			}
			abstractOrderEntryModel.setPersonalisationDetail(egiftCard);
			getModelService().save(abstractOrderEntryModel);
			getModelService().refresh(abstractOrderEntryModel);
		}
		else if (abstractOrderEntryModel.getPersonalisationDetail().size() != abstractOrderEntryModel.getQuantity().longValue())
		{
			final List<PersonalisationEGiftCardModel> egiftCard = new ArrayList(abstractOrderEntryModel.getPersonalisationDetail());
			final int sizeOfeGiftCard = egiftCard.size();
			final long entryQuantity = abstractOrderEntryModel.getQuantity().longValue();
			final long difference = entryQuantity - sizeOfeGiftCard;
			if (difference > 0)
			{
				for (long qty = 0; qty < difference; qty++)
				{
					final PersonalisationEGiftCardModel eGiftCardModel = getModelService().create(PersonalisationEGiftCardModel.class);
					eGiftCardModel.setPid(Integer.valueOf((String) this.keyGenerator.generate()));
					getModelService().save(eGiftCardModel);
					egiftCard.add(eGiftCardModel);
				}
				abstractOrderEntryModel.setPersonalisationDetail(egiftCard);
				getModelService().save(abstractOrderEntryModel);
				getModelService().refresh(abstractOrderEntryModel);
			}
			else
			{
				final List<PersonalisationEGiftCardModel> egiftCard1 = new ArrayList<>(
						abstractOrderEntryModel.getPersonalisationDetail());
				int size = egiftCard1.size();
				for (long qty = 0; qty > difference; qty--)
				{
					getModelService().remove(egiftCard1.get(size - 1).getPk());
					egiftCard1.remove(size - 1);
					size--;
				}
				abstractOrderEntryModel.setPersonalisationDetail(egiftCard1);
				getModelService().save(abstractOrderEntryModel);
				getModelService().refresh(abstractOrderEntryModel);
			}
		}
	}



	/**
	 * This method is used to apply personalisation for efift card
	 *
	 * @param cartModel
	 *           cart model as param
	 * @param abstractOrderEntryModel
	 *           for order data
	 * @param pIDNo
	 *           pid number as input
	 * @param eGiftCardCustomization
	 *           DTO of eGiftCard
	 * @return the OrderEntryData
	 */
	public OrderEntryData applyPersnalisationForeGiftCard(final CartModel cartModel,
			final AbstractOrderEntryModel abstractOrderEntryModel, final int pIDNo, final eGiftCardWsDTO eGiftCardCustomization)
	{
		if (eGiftCardCustomization.getCustomizeForAlleCards() != null
				&& eGiftCardCustomization.getCustomizeForAlleCards().booleanValue())
		{
			customiseAlleCards(abstractOrderEntryModel, eGiftCardCustomization);
		}
		else
		{
			customiseeGiftCardPerQunatity(abstractOrderEntryModel, pIDNo, eGiftCardCustomization);
		}
		return getOrderEntryConverter().convert(abstractOrderEntryModel);
	}

	/**
	 * This method is used to customize egiftcard for each quantity
	 *
	 * @param abstractOrderEntryModel
	 * @param pIDNo
	 * @param eGiftCardCustomization
	 */
	private void customiseeGiftCardPerQunatity(final AbstractOrderEntryModel abstractOrderEntryModel, final int pIDNo,
			final eGiftCardWsDTO eGiftCardCustomization)
	{
		final Collection<PersonalisationEGiftCardModel> eGiftCards = abstractOrderEntryModel.getPersonalisationDetail();
		if (eGiftCards != null && !eGiftCards.isEmpty())
		{
			for (final PersonalisationEGiftCardModel eGiftCard : eGiftCards)
			{
				if (eGiftCard.getPid().intValue() == pIDNo)
				{
					setPersonalisationDetail(eGiftCardCustomization, eGiftCard);
					setEgiftCardToEmail(abstractOrderEntryModel, eGiftCardCustomization, eGiftCard);
					setCustomerImage(eGiftCardCustomization, eGiftCard);
					getModelService().save(eGiftCard);
					getModelService().refresh(eGiftCard);
					break;
				}

			}

		}

	}

	/**
	 * @param abstractOrderEntryModel
	 * @param eGiftCardCustomization
	 * @param eGiftCard
	 */
	private void setEgiftCardToEmail(final AbstractOrderEntryModel abstractOrderEntryModel,
			final eGiftCardWsDTO eGiftCardCustomization, final PersonalisationEGiftCardModel eGiftCard)
	{
		if (null != eGiftCardCustomization.getToEmail())
		{
			final CustomerModel customer = (CustomerModel) abstractOrderEntryModel.getOrder().getUser();
			if (customer.getCustomerType() != null && customer.getCustomerType().getCode().equals(UserDataType.B2B.getCode()))
			{
				eGiftCard.setToEmail(eGiftCardCustomization.getToEmail());
			}
		}
		else if (null != eGiftCard.getToEmail())
		{
			eGiftCard.setToEmail(null);
		}
	}

	/**
	 * @param eGiftCardCustomization
	 * @param eGiftCard
	 */
	private void setCustomerImage(final eGiftCardWsDTO eGiftCardCustomization, final PersonalisationEGiftCardModel eGiftCard)
	{
		if (null != eGiftCardCustomization.getCustomImage())
		{
			final List<PersonalisationMediaModel> images = wooliesDefaultCartDao
					.getImageByID(eGiftCardCustomization.getCustomImage().getCode());
			if (images.isEmpty())
			{
				throw new IllegalArgumentException(WooliesgcFacadesConstants.IMAGE_DOES_NOT_EXIST_CODE);
			}
			eGiftCard.setCustomerImage(images.get(0));
		}
		else if (null != eGiftCard.getCustomerImage())
		{
			eGiftCard.setCustomerImage(null);
		}
	}

	/**
	 * This method is used to get customized ecards
	 *
	 * @param abstractOrderEntryModel
	 * @param eGiftCardCustomization
	 */
	private void customiseAlleCards(final AbstractOrderEntryModel abstractOrderEntryModel,
			final eGiftCardWsDTO eGiftCardCustomization)
	{
		final Collection<PersonalisationEGiftCardModel> eGiftCards = abstractOrderEntryModel.getPersonalisationDetail();
		for (final PersonalisationEGiftCardModel eGiftCard : eGiftCards)
		{
			setPersonalisationDetail(eGiftCardCustomization, eGiftCard);
			setCustomerImage(eGiftCardCustomization, eGiftCard);
			setEgiftCardToEmail(abstractOrderEntryModel, eGiftCardCustomization, eGiftCard);
			getModelService().save(eGiftCard);
			getModelService().refresh(eGiftCard);
		}
	}

	/**
	 * @param eGiftCardCustomization
	 * @param eGiftCard
	 */
	private void setPersonalisationDetail(final eGiftCardWsDTO eGiftCardCustomization,
			final PersonalisationEGiftCardModel eGiftCard)
	{
		if (null != eGiftCardCustomization.getMessage())
		{
			eGiftCard.setMessage(eGiftCardCustomization.getMessage());
		}
		else if (null != eGiftCard.getMessage())
		{
			eGiftCard.setMessage(null);
		}
		if (null != eGiftCardCustomization.getFromName())
		{
			eGiftCard.setFromName(eGiftCardCustomization.getFromName());
		}
		else if (null != eGiftCard.getFromName())
		{
			eGiftCard.setFromName(null);
		}
		if (null != eGiftCardCustomization.getToName())
		{
			eGiftCard.setToName(eGiftCardCustomization.getToName());
		}
		else if (null != eGiftCard.getToName())
		{
			eGiftCard.setToName(null);
		}
		if (null != eGiftCardCustomization.getToEmail())
		{
			eGiftCard.setToName(eGiftCardCustomization.getToEmail());
		}
		else if (null != eGiftCard.getToEmail())
		{
			eGiftCard.setToEmail(null);
		}
	}

	/**
	 * @return the userService
	 */
	public UserService getUserService()
	{
		return userService;
	}

	/**
	 * @param userService
	 *           the userService to set
	 */
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}


	/**
	 * This method is used save image for customer
	 *
	 * @param pIDNo
	 * @param url
	 * @return MediaData
	 */
	@Override
	public PersonalisationMediaData saveImageForCustomer(final int pIDNo, final String url)
	{
		PersonalisationMediaData emptyData = new PersonalisationMediaData();
		final UserModel user = getUserService().getCurrentUser();
		final Set<PersonalisationMediaModel> customerMedias = new HashSet(user.getGiftCardMedias());
		if (!getUserService().isAnonymousUser(user))
		{
			final PersonalisationMediaModel eachMedia = getModelService().create(PersonalisationMediaModel.class);
			eachMedia.setMime("images/jpg");
			eachMedia.setRealFileName(user.getUid() + IMG_PREFIX);
			eachMedia.setCode(imageCodeGenerator.generate().toString());
			eachMedia.setCatalogVersion(getCmsSiteService().getCurrentCatalogVersion());
			eachMedia.setUser(user);
			getModelService().save(eachMedia);
			final BASE64Decoder decoder = new BASE64Decoder();
			byte[] decodedBytes = null;
			try
			{
				decodedBytes = decoder.decodeBuffer(url);
			}
			catch (final IOException e)
			{
				LOG.error("Cannot convert Media " + e);
			}
			mediaService.setDataForMedia(eachMedia, decodedBytes);
			eachMedia.setPid(Integer.valueOf(pIDNo));
			customerMedias.add(eachMedia);
			getModelService().save(eachMedia);
			user.setGiftCardMedias(customerMedias);
			getModelService().save(user);
			emptyData = getWooliesMediaModelConverter().convert(eachMedia);
		}

		else
		{

			final PersonalisationMediaModel eachMedias = getModelService().create(PersonalisationMediaModel.class);
			eachMedias.setMime("images/jpg");
			eachMedias.setRealFileName(IMG_PREFIX);
			eachMedias.setCode(imageCodeGenerator.generate().toString());
			eachMedias.setCatalogVersion(getCmsSiteService().getCurrentCatalogVersion());
			eachMedias.setUser(user);
			getModelService().save(eachMedias);
			final BASE64Decoder decoder = new BASE64Decoder();
			byte[] decodedBytes = null;
			try
			{
				decodedBytes = decoder.decodeBuffer(url);
			}
			catch (final IOException e)
			{
				LOG.error("Cannot convert Media " + e);
			}
			mediaService.setDataForMedia(eachMedias, decodedBytes);
			eachMedias.setPid(Integer.valueOf(pIDNo));
			getModelService().save(eachMedias);
			emptyData = getWooliesMediaModelConverter().convert(eachMedias);
		}
		return emptyData;
	}

	/**
	 * @return the cmsSiteService
	 */
	public CMSSiteService getCmsSiteService()
	{
		return cmsSiteService;
	}

	/**
	 * @param cmsSiteService
	 *           the cmsSiteService to set
	 */
	public void setCmsSiteService(final CMSSiteService cmsSiteService)
	{
		this.cmsSiteService = cmsSiteService;
	}

	/**
	 * @return the imageCodeGenerator
	 */
	public KeyGenerator getImageCodeGenerator()
	{
		return imageCodeGenerator;
	}

	/**
	 * @param imageCodeGenerator
	 *           the imageCodeGenerator to set
	 */
	public void setImageCodeGenerator(final KeyGenerator imageCodeGenerator)
	{
		this.imageCodeGenerator = imageCodeGenerator;
	}

	/**
	 * This method is used to create card from absract order
	 *
	 * @param order
	 * @return CartModel
	 */
	@Override
	public CartModel createCartFromAbstractOrder(final AbstractOrderModel order)
	{

		return super.clone(getTypeService().getComposedTypeForClass(CartModel.class),
				getTypeService().getComposedTypeForClass(CartEntryModel.class), order, this.getKeyGenerator().generate().toString());
	}

	/**
	 * This method is used to update co brand data
	 *
	 * @param cartModel
	 * @param entryNumber
	 * @param coBrandID
	 * @return boolean
	 */
	@Override
	public boolean updateCoBrandData(final CartModel cartModel, final int entryNumber, final String coBrandID)
	{
		boolean isUpdated = false;
		final CustomerModel customer = (CustomerModel) cartModel.getUser();
		if (customer.getCustomerType() != null
				&& customer.getCustomerType().getCode().equalsIgnoreCase(UserDataType.B2B.toString()))
		{
			final CorporateB2BCustomerModel corporateb2bCustomer = (CorporateB2BCustomerModel) customer;
			if (CollectionUtils.isNotEmpty(((CorporateB2BUnitModel) corporateb2bCustomer.getDefaultB2BUnit()).getCoBrandImages()))
			{
				final Collection<CoBrandImageModel> list = ((CorporateB2BUnitModel) corporateb2bCustomer.getDefaultB2BUnit())
						.getCoBrandImages();
				for (final CoBrandImageModel coBrandImageModel : list)
				{
					if (coBrandImageModel.getImageID().equalsIgnoreCase(coBrandID))
					{
						isUpdated = saveCobrandImage(cartModel, entryNumber, coBrandID, isUpdated);
						break;
					}
				}
			}
		}
		return isUpdated;
	}

	/**
	 * @param cartModel
	 * @param entryNumber
	 * @param coBrandID
	 * @param isUpdated
	 * @return boolean
	 */
	private boolean saveCobrandImage(final CartModel cartModel, final int entryNumber, final String coBrandID, boolean isUpdated)
	{
		if (CollectionUtils.isNotEmpty(cartModel.getEntries()))
		{
			final AbstractOrderEntryModel eachModel = cartModel.getEntries().get(entryNumber);
			eachModel.setCoBrandID(coBrandID);
			modelService.save(eachModel);
			isUpdated = true;
		}
		return isUpdated;
	}

	/**
	 * This method is used to remove co brand data
	 *
	 * @param cartModel
	 * @param entryNumber
	 */
	@Override
	public void removeCoBrandData(final CartModel cartModel, final int entryNumber)
	{
		final CustomerModel customer = (CustomerModel) cartModel.getUser();
		if (customer.getCustomerType() != null && customer.getCustomerType().getCode().equalsIgnoreCase(UserDataType.B2B.toString())
				&& CollectionUtils.isNotEmpty(cartModel.getEntries()))
		{
			final AbstractOrderEntryModel eachModel = cartModel.getEntries().get(entryNumber);
			eachModel.setCoBrandID(null);
			modelService.save(eachModel);
		}

	}

	/**
	 * @return the wooliesMediaModelConverter
	 */
	public Converter<PersonalisationMediaModel, PersonalisationMediaData> getWooliesMediaModelConverter()
	{
		return wooliesMediaModelConverter;
	}

	/**
	 * @param wooliesMediaModelConverter
	 *           the wooliesMediaModelConverter to set
	 */
	public void setWooliesMediaModelConverter(
			final Converter<PersonalisationMediaModel, PersonalisationMediaData> wooliesMediaModelConverter)

	{
		this.wooliesMediaModelConverter = wooliesMediaModelConverter;
	}

	@Override
	protected CartModel internalGetSessionCart()
	{
		return getSessionService().getOrLoadAttribute(SESSION_CART_PARAMETER_NAME, new SessionAttributeLoader<CartModel>()
		{
			@Override
			public CartModel load()
			{
				final CartModel cartModel = cartFactory.createCart();
				cartModel.setOrderId(generateOrderCode());
				modelService.save(cartModel);
				return cartModel;
			}
		});
	}

	private String generateOrderCode()
	{
		final Object generatedValue = orderKeyGenerator.generate();
		if (generatedValue instanceof String)
		{
			return (String) generatedValue;
		}
		else
		{
			return String.valueOf(generatedValue);
		}
	}

	/**
	 * @return the orderKeyGenerator
	 */
	public KeyGenerator getOrderKeyGenerator()
	{
		return orderKeyGenerator;
	}

	/**
	 * @param orderKeyGenerator
	 *           the orderKeyGenerator to set
	 */
	public void setOrderKeyGenerator(final KeyGenerator orderKeyGenerator)
	{
		this.orderKeyGenerator = orderKeyGenerator;
	}

}
