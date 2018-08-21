/**
 *
 */
package de.hybris.wooliesegiftcard.facades.order.impl;

import de.hybris.model.PersonalisationEGiftCardModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commercefacades.order.data.AddToCartParams;
import de.hybris.platform.commercefacades.order.data.CartModificationData;
import de.hybris.platform.commercefacades.order.data.CartRestorationData;
import de.hybris.platform.commercefacades.order.data.DeliveryModeData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.order.data.ZoneDeliveryModeData;
import de.hybris.platform.commercefacades.order.impl.DefaultCartFacade;
import de.hybris.platform.commercefacades.product.data.PriceDataType;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.delivery.DeliveryService;
import de.hybris.platform.commerceservices.order.CommerceCartModification;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commerceservices.order.CommerceCartRestoration;
import de.hybris.platform.commerceservices.order.CommerceCartRestorationException;
import de.hybris.platform.commerceservices.order.CommerceCheckoutService;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.deliveryzone.model.ZoneDeliveryModeModel;
import de.hybris.platform.deliveryzone.model.ZoneDeliveryModeValueModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.wooliesegiftcard.core.enums.BulkOrderStatus;
import de.hybris.wooliesegiftcard.core.model.WWBulkOrderDataModel;
import de.hybris.wooliesegiftcard.core.model.WWBulkOrderItemsDataModel;
import de.hybris.wooliesegiftcard.exceptions.WooliesFacadeLayerException;
import de.hybris.wooliesegiftcard.facades.constants.WooliesgcFacadesConstants;
import de.hybris.wooliesegiftcard.facades.order.WooliesCartFacade;
import de.hybris.wooliesegiftcard.facades.product.data.PersonalisationMediaData;
import de.hybris.wooliesegiftcard.service.WooliesCartService;
import de.hybris.wooliesegiftcard.service.impl.WooliesDefaultCartService;
import de.hybris.wooliesgiftcard.core.bulkorder.service.WooliesBulkOrderService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.woolies.webservices.dto.eGiftCardWsDTO;


/**
 * @author 668982 This class is used to maintain cart details
 */
public class WooliesDefaultCartFacade extends DefaultCartFacade implements WooliesCartFacade
{
	private Converter<ZoneDeliveryModeModel, ZoneDeliveryModeData> zoneDeliveryModeConverter;
	private ConfigurationService configurationService;
	private CommonI18NService commonI18NService;
	private WooliesDefaultCartService cartService;
	private DeliveryService deliveryService;
	private CommerceCheckoutService commerceCheckoutService;
	private Converter<AddressData, AddressModel> addressReverseConverter;
	private Converter<AbstractOrderEntryModel, OrderEntryData> orderEntryConverter;
	@Autowired
	private WooliesCartService wooliesCartService;
	@Resource
	private ModelService modelService;

	/**
	 * @return the orderEntryConverter
	 */
	@Override
	public Converter<AbstractOrderEntryModel, OrderEntryData> getOrderEntryConverter()
	{
		return orderEntryConverter;
	}

	/**
	 * @param orderEntryConverter
	 *           the orderEntryConverter to set
	 */
	@Override
	public void setOrderEntryConverter(final Converter<AbstractOrderEntryModel, OrderEntryData> orderEntryConverter)
	{
		this.orderEntryConverter = orderEntryConverter;
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
	 * @return the deliveryService
	 */
	@Override
	public DeliveryService getDeliveryService()
	{
		return deliveryService;
	}

	/**
	 * @param deliveryService
	 *           the deliveryService to set
	 */
	@Override
	public void setDeliveryService(final DeliveryService deliveryService)
	{
		this.deliveryService = deliveryService;
	}

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
	 * @return the zoneDeliveryModeConverter
	 */
	public Converter<ZoneDeliveryModeModel, ZoneDeliveryModeData> getZoneDeliveryModeConverter()
	{
		return zoneDeliveryModeConverter;
	}

	/**
	 * @param zoneDeliveryModeConverter
	 *           the zoneDeliveryModeConverter to set
	 */
	public void setZoneDeliveryModeConverter(final Converter<ZoneDeliveryModeModel, ZoneDeliveryModeData> zoneDeliveryModeConverter)
	{
		this.zoneDeliveryModeConverter = zoneDeliveryModeConverter;
	}

	/**
	 * @return the cartService
	 */
	@Override
	public WooliesDefaultCartService getCartService()
	{
		return cartService;
	}

	/**
	 * @param cartService
	 *           the cartService to set
	 */
	public void setCartService(final WooliesDefaultCartService cartService)
	{
		this.cartService = cartService;
	}

	/**
	 *
	 * This method is used add cart
	 *
	 * @param code
	 *           product code
	 * @param quantity
	 *           quantity of item
	 * @param priceGivenByCustomer
	 *           customer Price
	 * @return CartModificationData data
	 * @throws CommerceCartModificationException
	 *            throws CommerceCartModificationException
	 */
	@Override
	public CartModificationData addToCart(final String code, final long quantity, final Double priceGivenByCustomer)
			throws CommerceCartModificationException
	{
		final AddToCartParams params = new AddToCartParams();
		params.setProductCode(code);
		params.setQuantity(quantity);
		if (priceGivenByCustomer != null)
		{
			params.setCustomerPrice(priceGivenByCustomer);
		}
		return addToCart(params);
	}

	/**
	 * This method is used add cart
	 *
	 * @param code
	 *           product code
	 * @param qty
	 *           quantity of item
	 * @param pickupStore
	 *           pickupStore
	 * @param priceGivenByCustomer
	 *           customer Price
	 * @return CartModificationData data
	 * @throws CommerceCartModificationException
	 *            throws CommerceCartModificationException
	 */
	@Override
	public CartModificationData addToCart(final String code, final long qty, final String pickupStore,
			final Double priceGivenByCustomer) throws CommerceCartModificationException
	{
		final AddToCartParams params = new AddToCartParams();
		params.setProductCode(code);
		params.setQuantity(qty);
		params.setStoreId(pickupStore);
		if (priceGivenByCustomer != null)
		{
			params.setCustomerPrice(priceGivenByCustomer);
		}

		return addToCart(params);
	}

	/**
	 * This method is used to update cart
	 *
	 * @param entryNumber
	 *           entry Number
	 * @param customerPrice
	 *           customer Price
	 * @param quantity
	 *           quantity of item
	 * @return CartModificationData data
	 * @throws CommerceCartModificationException
	 *            throws CommerceCartModificationException
	 */
	@Override
	public CartModificationData updateCartEntry(final long entryNumber, final long quantity, final Double customerPrice)
			throws CommerceCartModificationException
	{

		final AddToCartParams dto = new AddToCartParams();
		dto.setQuantity(quantity);
		dto.setCustomerPrice(customerPrice);
		final CommerceCartParameter parameter = getCommerceCartParameterConverter().convert(dto);
		parameter.setEnableHooks(true);
		parameter.setEnableHooks(true);
		parameter.setEntryNumber(entryNumber);
		final CommerceCartModification modification = getCommerceCartService().updateQuantityForCartEntry(parameter);
		return getCartModificationConverter().convert(modification);
	}

	/**
	 * To get deliver mode
	 *
	 * @return the deliver data
	 */
	@Override
	public List<DeliveryModeData> getDeliveryMode()
	{
		final List<DeliveryModeData> result = new ArrayList<>();

		if (hasSessionCart())
		{
			final CartModel cart = getCartService().getSessionCart();
			getDeliveryModeData(result, cart);
		}
		return result;
	}

	/**
	 * This method is used to get delivery mode data
	 *
	 * @param result
	 *           List of DeliveryModeData
	 * @param cart
	 *           cart Model
	 */
	public void getDeliveryModeData(final List<DeliveryModeData> result, final CartModel cart)
	{
		for (final ZoneDeliveryModeValueModel deliveryModeModel : getCartService().getdeliveryOptionsForCart(cart))
		{
			final ZoneDeliveryModeData zoneDeliveryModeData = getZoneDeliveryModeConverter().convert(
					deliveryModeModel.getDeliveryMode());
			deliveryCostCalculation(deliveryModeModel, zoneDeliveryModeData);
			if (deliveryModeModel.getDeliveryMode().getCode().startsWith("EGC"))
			{
				zoneDeliveryModeData.setDeliveryType("eGift");
				zoneDeliveryModeData.setSelectedFlag(Boolean.TRUE);
			}
			else if (cart.getDeliveryMode() != null && cart.getDeliveryMode().getCode().equals(zoneDeliveryModeData.getCode()))
			{
				zoneDeliveryModeData.setDeliveryType("Plastic");
				zoneDeliveryModeData.setSelectedFlag(Boolean.TRUE);
			}
			else
			{
				zoneDeliveryModeData.setDeliveryType("Plastic");
				zoneDeliveryModeData.setSelectedFlag(Boolean.FALSE);
			}
			result.add(zoneDeliveryModeData);
		}
	}

	/**
	 * This method is used to calculate the delivery cost
	 *
	 * @param deliveryModeModel
	 *           ZoneDeliveryModeValueModel
	 * @param zoneDeliveryModeData
	 *           zoneDeliveryModeData
	 */
	private void deliveryCostCalculation(final ZoneDeliveryModeValueModel deliveryModeModel,
			final ZoneDeliveryModeData zoneDeliveryModeData)
	{
		final CurrencyModel currency = deliveryModeModel.getCurrency();
		if (deliveryModeModel.getValue() != null && deliveryModeModel.getValue().doubleValue() > 0)
		{
			final double taxCost = getCommonI18NService().roundCurrency(
					deliveryModeModel.getValue().doubleValue()
							/ ((getConfigurationService().getConfiguration().getDouble("tax.deliveryCost", 10)) + 1),
					currency.getDigits().intValue());
			zoneDeliveryModeData.setTaxCost(getPriceDataFactory().create(PriceDataType.BUY, BigDecimal.valueOf(taxCost),
					currency.getIsocode()));
		}
		zoneDeliveryModeData.setDeliveryCost(getPriceDataFactory().create(PriceDataType.BUY,
				BigDecimal.valueOf(deliveryModeModel.getValue().doubleValue()), currency.getIsocode()));

	}

	/**
	 * This method is used to set delivery address
	 *
	 * @param addressData
	 *           AddressData
	 * @param newAddress
	 *           boolean
	 * @return bollean value
	 */
	public boolean setDeliveryAddress(final AddressData addressData, final boolean newAddress)
	{
		final CartModel cartModel = getCartService().getSessionCart();
		if (cartModel != null)
		{
			AddressModel addressModel = null;
			if (addressData != null)
			{
				addressModel = getDeliveryAddressModelForCode(addressData.getAddressID(), cartModel);
			}
			if (addressModel != null)
			{
				if (!newAddress)
				{
					getAddressReverseConverter().convert(addressData, addressModel);
					getModelService().save(addressModel);
					getModelService().refresh(addressModel);
				}
				final CommerceCheckoutParameter parameter = createCommerceCheckoutParameter(cartModel, true);
				parameter.setAddress(addressModel);
				return getCommerceCheckoutService().setDeliveryAddress(parameter);
			}
		}
		return false;
	}

	/**
	 * This method is used to get delivery address for the code
	 *
	 * @param code
	 *           address
	 * @param cartModel
	 *           user cart
	 * @return address AddressModel
	 */
	protected AddressModel getDeliveryAddressModelForCode(final String code, final CartModel cartModel)
	{
		if (cartModel != null && StringUtils.isNotEmpty(code))
		{
			for (final AddressModel address : getDeliveryService().getSupportedDeliveryAddressesForOrder(cartModel, false))
			{
				if (code.equals(address.getPk().toString()))
				{
					return address;
				}
			}
		}
		return null;
	}

	/**
	 * @return the commerceCheckoutService
	 */
	public CommerceCheckoutService getCommerceCheckoutService()
	{
		return commerceCheckoutService;
	}

	/**
	 * @param commerceCheckoutService
	 *           the commerceCheckoutService to set
	 */
	public void setCommerceCheckoutService(final CommerceCheckoutService commerceCheckoutService)
	{
		this.commerceCheckoutService = commerceCheckoutService;
	}

	/**
	 * This method is used to create commerce checkout parameter
	 *
	 * @param cart
	 *           user cart
	 * @param enableHooks
	 *           boolean value
	 * @return parameter CommerceCheckoutParameter
	 */
	protected CommerceCheckoutParameter createCommerceCheckoutParameter(final CartModel cart, final boolean enableHooks)
	{
		final CommerceCheckoutParameter parameter = new CommerceCheckoutParameter();
		parameter.setEnableHooks(enableHooks);
		parameter.setCart(cart);
		return parameter;
	}


	/**
	 * This method is used to get the cart details
	 *
	 * @return the cartModel user cart
	 */
	@Override
	public CartModel getCart()
	{
		if (hasSessionCart())
		{
			return getCartService().getSessionCart();
		}
		return null;
	}

	/**
	 * This method is used to get the pid for gift card for the given entry number
	 *
	 * @param abstractOrderEntryModel
	 *           orderEntry model
	 * @return OrderEntryData
	 */
	@Override
	public OrderEntryData generatePIDForeGiftCard(final AbstractOrderEntryModel abstractOrderEntryModel)
	{
		OrderEntryData orderEntryData = null;
		getCartService().generatePIDForEntryModel(abstractOrderEntryModel);
		orderEntryData = getOrderEntryConverter().convert(abstractOrderEntryModel);
		return orderEntryData;
	}

	/**
	 * This method is used to apply personalisation for gift card
	 *
	 * @param cartModel
	 *           CartModel
	 * @param abstractOrderEntryModel
	 *           AbstractOrderEntryModel
	 * @param PIDNo
	 *           integer value
	 * @param eGiftCardCustomization
	 *           eGiftCardWsDTO
	 * @return OrderEntryData
	 */
	@Override
	public OrderEntryData applyPersnalisationForeGiftCard(final CartModel cartModel,
			final AbstractOrderEntryModel abstractOrderEntryModel, final int PIDNo, final eGiftCardWsDTO eGiftCardCustomization)
	{
		return getCartService().applyPersnalisationForeGiftCard(cartModel, abstractOrderEntryModel, PIDNo, eGiftCardCustomization);
	}

	/**
	 * This method is used to remove PID's
	 *
	 * @param cartModel
	 *           user cart
	 * @param entryNumber
	 *           order entry number
	 * @throws WooliesFacadeLayerException
	 */
	@Override
	public void removePersonalisationForEgiftCard(final CartModel cartModel, final int entryNumber)
			throws WooliesFacadeLayerException
	{
		final AbstractOrderEntryModel entry = cartModel.getEntries().get(entryNumber);
		final List<PersonalisationEGiftCardModel> personalisation = entry.getPersonalisationDetail();
		if (CollectionUtils.isNotEmpty(personalisation))
		{
			entry.setPersonalisationDetail(new ArrayList<PersonalisationEGiftCardModel>());
			getModelService().removeAll(personalisation);
			getModelService().save(entry);
		}
		else
		{
			throw new WooliesFacadeLayerException(WooliesgcFacadesConstants.ERRCODE_PIDNOTAVAILABLE,
					WooliesgcFacadesConstants.ERRMSG_PIDNOTAVAILABLE, WooliesgcFacadesConstants.ERRRSN_PIDNOTAVAILABLE);
		}

	}

	/**
	 * To save image for customer
	 *
	 * @param pIDNo
	 *           pid number
	 * @param url
	 *           url
	 * @return MediaData PersonalisationMediaData
	 */
	@Override
	public PersonalisationMediaData saveImageForCustomer(final int pIDNo, final String url)
	{
		return wooliesCartService.saveImageForCustomer(pIDNo, url);
	}

	//Woolies cart merge functionality
	/**
	 * This method is used to restore anonymous user
	 *
	 * @param guid
	 *           guid
	 * @return cartRestorationData
	 * @throws CommerceCartRestorationException
	 */
	@Override
	public CartRestorationData restoreWooliesAnonymousCartAndTakeOwnership(final String guid)
			throws CommerceCartRestorationException
	{
		final BaseSiteModel currentBaseSite = getBaseSiteService().getCurrentBaseSite();
		final CartModel cart = getCommerceCartService().getCartForGuidAndSiteAndUser(guid, currentBaseSite,
				getUserService().getAnonymousUser());

		if (cart == null)
		{
			throw new CommerceCartRestorationException(String.format("Cart not found for guid %s", guid));
		}
		final CommerceCartParameter parameter = new CommerceCartParameter();
		parameter.setEnableHooks(true);
		parameter.setCart(cart);

		final CommerceCartRestoration commerceCartRestoration = getCommerceCartService().restoreCart(parameter);
		getCartService().changeCurrentCartUser(getUserService().getCurrentUser());
		return getCartRestorationConverter().convert(commerceCartRestoration);
	}

	/**
	 * This method is used to update cob brand data
	 *
	 * @param cartModel
	 *           user cart
	 * @param entryNumber
	 *           order entry
	 * @param coBrandID
	 *           co brand id
	 * @return boolean
	 */
	@Override
	public boolean updateCoBrandData(final CartModel cartModel, final int entryNumber, final String coBrandID)
	{
		return wooliesCartService.updateCoBrandData(cartModel, entryNumber, coBrandID);
	}

	/**
	 * This method is used to remove co brand data
	 *
	 * @param cartModel
	 *           user cart
	 * @param entryNumber
	 *           entry number
	 */
	@Override
	public void removeCoBrandData(final CartModel cartModel, final int entryNumber)
	{
		wooliesCartService.removeCoBrandData(cartModel, entryNumber);

	}

	/**
	 *
	 * @param isMinimalPackaging
	 *           boolean value
	 */
	public void setMinimalPackaging(final boolean isMinimalPackaging)
	{
		final CartModel cartModel = getCart();
		cartModel.setIsMinimalPackage(Boolean.valueOf(isMinimalPackaging));
		getModelService().save(cartModel);
	}

	/**
	 * This method is used to add items in cart for bulk order
	 *
	 * @param wooliesBulkOrderService
	 *           WooliesBulkOrderService
	 * @param bulkOrderDataCollection
	 *           List of WWBulkOrderItemsDataModel
	 * @param wwStatusOfBulkOrderData
	 *           WWBulkOrderDataModel
	 * @param qnty
	 *           quantity
	 * @throws CommerceCartModificationException
	 */
	@Override
	public void addToCartforBulkOrder(final WooliesBulkOrderService wooliesBulkOrderService,
			final Collection<WWBulkOrderItemsDataModel> bulkOrderDataCollection, final WWBulkOrderDataModel wwStatusOfBulkOrderData,
			final int qnty) throws CommerceCartModificationException
	{
		final Map<Integer, WWBulkOrderItemsDataModel> coreItems = new ConcurrentHashMap<>();
		int entryNumber = 0;
		final Iterator itr = bulkOrderDataCollection.iterator();
		final CartModel cartModel = getCart();
		cartModel.setIsBulkOrder(Boolean.TRUE);
		wooliesBulkOrderService.saveModel(cartModel);
		while (itr.hasNext())
		{
			final WWBulkOrderItemsDataModel items = (WWBulkOrderItemsDataModel) itr.next();
			boolean isExistinMap = false;
			if (coreItems.isEmpty())
			{
				coreItems.put(Integer.valueOf(entryNumber), items);
			}
			else
			{
				for (final Map.Entry<Integer, WWBulkOrderItemsDataModel> entry : coreItems.entrySet())
				{
					isExistinMap = checkIfExistsInMap(items, isExistinMap, entry);
				}
				if (!isExistinMap)
				{
					entryNumber++;
					coreItems.put(Integer.valueOf(entryNumber), items);
				}
			}
			addToCart(items.getSkuCode(), qnty, items.getUnitPrice());
		}
		setPersonalisationData(coreItems, cartModel, entryNumber);
		wwStatusOfBulkOrderData.setBulkOrderStatus(BulkOrderStatus.SUCCESS);
		wooliesBulkOrderService.saveModel(wwStatusOfBulkOrderData);

	}

	/**
	 * @param items
	 * @param isExistinMap
	 * @param entry
	 * @return
	 */
	private boolean checkIfExistsInMap(final WWBulkOrderItemsDataModel items, boolean isExistinMap,
			final Map.Entry<Integer, WWBulkOrderItemsDataModel> entry)
	{
		if (items.getSkuCode().equals(entry.getValue().getSkuCode())
				&& Double.compare(items.getUnitPrice().doubleValue(), entry.getValue().getUnitPrice().doubleValue()) == 0)
		{
			isExistinMap = true;
		}
		return isExistinMap;
	}

	/**
	 * This method is used to set Personalization data for Bulk Order
	 *
	 * @param coreItems
	 *           Map<Integer, WWBulkOrderItemsDataModel>
	 * @param cartModel
	 *           user cart
	 * @param entryNumber
	 *           integer number
	 */

	@Override
	public void setPersonalisationData(final Map<Integer, WWBulkOrderItemsDataModel> coreItems, final CartModel cartModel,
			final int entryNumber)
	{
		final eGiftCardWsDTO eGiftCardCustomization = new eGiftCardWsDTO();
		OrderEntryData orderEntry = null;
		for (final Map.Entry<Integer, WWBulkOrderItemsDataModel> entry : coreItems.entrySet())
		{
			orderEntry = generatePIDForeGiftCard(cartModel.getEntries().get(entry.getKey().intValue()));
			eGiftCardCustomization.setFromName(entry.getValue().getFromName());
			eGiftCardCustomization.setToName(entry.getValue().getToName());
			eGiftCardCustomization.setToEmail(entry.getValue().getToEmail());
			eGiftCardCustomization.setMessage(entry.getValue().getMessage());
			if (null != orderEntry.getListOfPID() && !orderEntry.getListOfPID().isEmpty())
			{
				for (final int pid : orderEntry.getListOfPID())
				{
					applyPersnalisationForeGiftCard(cartModel, cartModel.getEntries().get(entry.getKey().intValue()), pid,
							eGiftCardCustomization);
				}
			}
		}

	}
}