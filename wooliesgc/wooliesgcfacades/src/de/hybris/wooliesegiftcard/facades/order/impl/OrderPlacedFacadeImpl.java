/**
 *
 */
package de.hybris.wooliesegiftcard.facades.order.impl;

import de.hybris.model.EGiftCardModel;
import de.hybris.platform.commercefacades.order.impl.DefaultOrderFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import de.hybris.wooliesegiftcard.core.impl.WooliesDefaultCustomerAccountService;
import de.hybris.wooliesegiftcard.dao.impl.WooliesDefaultCartDao;
import de.hybris.wooliesegiftcard.exceptions.WooliesFacadeLayerException;
import de.hybris.wooliesegiftcard.facades.GiftCardResponseData;
import de.hybris.wooliesegiftcard.facades.OrderPlacedFacade;
import de.hybris.wooliesegiftcard.facades.constants.WooliesgcFacadesConstants;
import de.hybris.wooliesegiftcard.facades.dto.FraudOrderStatus;
import de.hybris.wooliesegiftcard.facades.dto.FraudOrderStatusRequest;
import de.hybris.wooliesegiftcard.service.WooliesOrderHistoryService;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author 264343 This class is used to maintain placed order
 */
public class OrderPlacedFacadeImpl extends DefaultOrderFacade implements OrderPlacedFacade
{

	@Autowired
	private WooliesDefaultCartDao wooliesDefaultCartDao;
	@Resource
	private BaseStoreService baseStoreService;
	@Resource
	protected ModelService modelService;
	CountryModel shippingCountryModel;
	@Autowired
	private Converter<AddressData, AddressModel> addressReverseConverter;
	@Autowired
	private WooliesDefaultCustomerAccountService customerAccountService;
	@Autowired
	private WooliesOrderHistoryService wooliesOrderHistoryService;
	@Autowired
	private Converter<EGiftCardModel, GiftCardResponseData> giftCardConverter;

	/**
	 * This method is used to save shipping address
	 *
	 * @param shippingAddress
	 *           address data
	 * @param cartModel
	 *           user cart
	 * @throws WooliesFacadeLayerException
	 */
	@Override
	public void saveShippingAddress(final AddressData shippingAddress, final CartModel cartModel)
			throws WooliesFacadeLayerException
	{
		final CustomerModel customerModel = (CustomerModel) cartModel.getUser();
		if (shippingAddress.getAddressID() != null)
		{
			final AddressModel shippingAddressModel = customerAccountService.getaddressforAddressId(shippingAddress.getAddressID());
			if (shippingAddressModel != null)
			{
				cartModel.setDeliveryAddress(modelService.clone(shippingAddressModel));
			}
			else
			{
				throw new WooliesFacadeLayerException("Shipping address is missing");
			}
		}
		else
		{
			final AddressModel shippingAddressModel = addressReverseConverter.convert(shippingAddress);
			shippingAddressModel.setShippingAddress(Boolean.TRUE);
			shippingAddressModel.setShippingAddress(Boolean.valueOf(true));
			shippingAddressModel.setBillingAddress(Boolean.valueOf(false));
			shippingAddressModel.setOwner(customerModel);
			cartModel.setDeliveryAddress(shippingAddressModel);
			if (shippingAddress.getSaveToProfile() != null && shippingAddress.getSaveToProfile().booleanValue())
			{
				final List<AddressModel> customerAddresses = new ArrayList(customerModel.getAddresses());
				modelService.save(shippingAddressModel);
				modelService.refresh(shippingAddressModel);
				customerAddresses.add(shippingAddressModel);
				customerModel.setAddresses(customerAddresses);
				if (null == customerModel.getDefaultShipmentAddress())
				{
					customerModel.setDefaultShipmentAddress(shippingAddressModel);
				}
				modelService.save(customerModel);
			}
		}
		modelService.save(cartModel);
	}

	/**
	 * This method is used to get the cart for the given uid
	 *
	 * @param uid
	 *           cart id
	 * @return CartModel
	 */
	@Override
	public CartModel getCart(final String uid)
	{
		final List<CartModel> carts = wooliesDefaultCartDao.getCartByUID(uid, getBaseStoreService().getCurrentBaseStore());
		if (CollectionUtils.isNotEmpty(carts))
		{
			return carts.get(0);
		}
		else
		{
			return null;
		}
	}

	/**
	 * This method is used to get the cart details for anonymous guest uid
	 *
	 * @param guid
	 *           String
	 * @return cartModel
	 */
	@Override
	public CartModel getCartForAnonymous(final String guid)
	{
		final List<CartModel> carts = wooliesDefaultCartDao.getCartByGUID(guid);
		if (CollectionUtils.isNotEmpty(carts))
		{
			return carts.get(0);
		}
		else
		{
			return null;
		}
	}

	/**
	 * set status to order
	 *
	 * @param fraudOrderRequest
	 *           FraudOrderStatusRequest
	 * @throws WooliesFacadeLayerException
	 */
	@Override
	public void setOrderStatus(final FraudOrderStatusRequest fraudOrderRequest) throws WooliesFacadeLayerException
	{
		if (null == fraudOrderRequest)
		{
			throw new WooliesFacadeLayerException(WooliesgcFacadesConstants.ERR_CODE_INCORRECT_PAYLOAD,
					WooliesgcFacadesConstants.ERRMSG_INCORRECT_PAYLOAD, WooliesgcFacadesConstants.ERRRSN_INCORRECT_PAYLOAD);
		}
		final List<FraudOrderStatus> fraudOrderStatusList = fraudOrderRequest.getOrders();
		if (null != fraudOrderStatusList && CollectionUtils.isNotEmpty(fraudOrderStatusList))
		{
			for (final FraudOrderStatus fraudOrderStatus : fraudOrderStatusList)
			{
				setOrderStatusForFraud(fraudOrderStatus);
			}
		}
		else
		{
			throw new WooliesFacadeLayerException(WooliesgcFacadesConstants.ERR_CODE_INCORRECT_PAYLOAD,
					WooliesgcFacadesConstants.ERRMSG_INCORRECT_PAYLOAD, WooliesgcFacadesConstants.ERRRSN_INCORRECT_PAYLOAD);
		}
	}

	/**
	 * set status to order
	 *
	 * @param fraudOrderStatus
	 *           FraudOrderStatus
	 * @throws WooliesFacadeLayerException
	 */
	private void setOrderStatusForFraud(final FraudOrderStatus fraudOrderStatus) throws WooliesFacadeLayerException
	{
		final String orderNumber = fraudOrderStatus.getMerchantReferenceNumber();
		final BaseStoreModel baseStoreModel = getBaseStoreService().getCurrentBaseStore();

		OrderModel orderModel = null;

		try
		{
			orderModel = getCustomerAccountService().getOrderForCode(orderNumber, baseStoreModel);
		}
		catch (final ModelNotFoundException e)
		{
			throw new WooliesFacadeLayerException(WooliesgcFacadesConstants.ERR_CODE_ORDER_NOT_FOUND,
					WooliesgcFacadesConstants.ERRMSG_ORDER_NOT_FOUND, WooliesgcFacadesConstants.ERRRSN_ORDER_NOT_FOUND);
		}


		if (orderModel == null)
		{
			throw new WooliesFacadeLayerException(WooliesgcFacadesConstants.ERR_CODE_ORDER_NOT_FOUND,
					WooliesgcFacadesConstants.ERRMSG_ORDER_NOT_FOUND, WooliesgcFacadesConstants.ERRRSN_ORDER_NOT_FOUND);
		}
		else
		{
			final OrderStatus status = orderModel.getStatus();
			if (status == OrderStatus.ON_VALIDATION
					&& fraudOrderStatus.getOriginalDecision().equalsIgnoreCase(WooliesgcFacadesConstants.FRAUD_REVIEW))
			{
				setOrderStatus(fraudOrderStatus, orderModel);
			}
			else
			{
				final String orderStatus = getOrderStatus(status);
				throw new WooliesFacadeLayerException(WooliesgcFacadesConstants.ERR_CODE_INCORRECT_ORDER_STATUS,
						WooliesgcFacadesConstants.ERRMSG_INCORRECT_ORDER_STATUS,
						WooliesgcFacadesConstants.ERRRSN_INCORRECT_ORDER_STATUS + orderStatus
								+ " but received original decision from Cybersoource as " + fraudOrderStatus.getOriginalDecision());
			}
		}
	}

	/**
	 * set gift card response
	 *
	 * @param eToken
	 *           String
	 * @return GiftCardResponseData
	 */
	@Override
	public GiftCardResponseData getEgiftCardDetails(final String eToken)
	{
		final EGiftCardModel giftCard = wooliesOrderHistoryService.getEgiftCardDetails(eToken);
		if (giftCard != null)
		{
			GiftCardResponseData giftCardData = new GiftCardResponseData();
			giftCardData = giftCardConverter.convert(giftCard, giftCardData);
			return giftCardData;
		}
		return null;
	}

	/**
	 * set status to order
	 *
	 * @param fraudOrderStatus
	 *           FraudOrderStatus
	 * @param orderModel
	 *           OrderModel
	 */
	private void setOrderStatus(final FraudOrderStatus fraudOrderStatus, final OrderModel orderModel)
	{
		if (fraudOrderStatus.getNewDecision().equalsIgnoreCase(WooliesgcFacadesConstants.FRAUD_ACCEPTED))
		{
			orderModel.setStatus(OrderStatus.CHECKED_VALID);
		}
		else if (fraudOrderStatus.getNewDecision().equalsIgnoreCase(WooliesgcFacadesConstants.FRAUD_REJECTED))
		{
			orderModel.setStatus(OrderStatus.CHECKED_INVALID);
		}
		modelService.save(orderModel);
	}

	/**
	 * set status to order
	 *
	 * @param status
	 * @return String
	 */
	private String getOrderStatus(final OrderStatus status)
	{
		String orderStatus = "";
		if (status == OrderStatus.CHECKED_INVALID)
		{
			orderStatus = WooliesgcFacadesConstants.FRAUD_REJECTED;
		}
		else if (status == OrderStatus.CHECKED_VALID)
		{
			orderStatus = WooliesgcFacadesConstants.FRAUD_ACCEPTED;
		}
		else if (status == OrderStatus.ON_VALIDATION)
		{
			orderStatus = WooliesgcFacadesConstants.FRAUD_REVIEW;
		}
		else
		{
			orderStatus = status.getCode();
		}
		return orderStatus;
	}

}
