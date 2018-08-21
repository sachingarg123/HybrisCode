/*
 * [y] hybris Platform
 *
 * Copyright (c) 2017 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package com.woolies.webservices.rest.v2.controller;

import de.hybris.model.PersonalisationEGiftCardModel;
import de.hybris.platform.b2b.model.B2BOrderThresholdPermissionModel;
import de.hybris.platform.b2b.model.B2BPermissionModel;
import de.hybris.platform.basecommerce.enums.StockLevelStatus;
import de.hybris.platform.cmsfacades.data.MediaData;
import de.hybris.platform.cmssmarteditwebservices.dto.MediaWsDTO;
import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.SaveCartFacade;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.CartModificationData;
import de.hybris.platform.commercefacades.order.data.DeliveryModesData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.data.PromotionResultData;
import de.hybris.platform.commercefacades.product.data.StockData;
import de.hybris.platform.commercefacades.promotion.CommercePromotionRestrictionFacade;
import de.hybris.platform.commercefacades.storelocator.data.PointOfServiceData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commercefacades.voucher.VoucherFacade;
import de.hybris.platform.commercefacades.voucher.exceptions.VoucherOperationException;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.commerceservices.order.CommerceCartMergingException;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commerceservices.order.CommerceCartRestorationException;
import de.hybris.platform.commerceservices.promotion.CommercePromotionRestrictionException;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commercewebservicescommons.dto.order.CartListWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.CartModificationWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.CartWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.DeliveryModeListWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.DeliveryModeWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.OrderEntryListWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.OrderEntryWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.product.PromotionResultListWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.user.AddressWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.user.UserSignUpWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.voucher.VoucherListWsDTO;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.CartAddressException;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.CartEntryException;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.CartException;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.LowStockException;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.ProductLowStockException;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.RequestParameterException;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.StockSystemException;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.price.DiscountModel;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.media.impl.DefaultMediaDao;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.DiscountValue;
import de.hybris.platform.webservicescommons.cache.CacheControl;
import de.hybris.platform.webservicescommons.cache.CacheControlDirective;
import de.hybris.platform.webservicescommons.errors.exceptions.WebserviceValidationException;
import de.hybris.wooliesegiftcard.core.enums.BulkOrderStatus;
import de.hybris.wooliesegiftcard.core.enums.UserDataType;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BCustomerModel;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BUnitModel;
import de.hybris.wooliesegiftcard.core.model.WWBulkOrderDataModel;
import de.hybris.wooliesegiftcard.core.model.WWBulkOrderItemsDataModel;
import de.hybris.wooliesegiftcard.exception.WooliesB2BUserException;
import de.hybris.wooliesegiftcard.exceptions.WooliesFacadeLayerException;
import de.hybris.wooliesegiftcard.facade.ProfileSummeryFacade;
import de.hybris.wooliesegiftcard.facade.WooliesGuestUserFacade;
import de.hybris.wooliesegiftcard.facade.WooliesPaymentFacade;
import de.hybris.wooliesegiftcard.facades.WooliesDefaultCustomerFacade;
import de.hybris.wooliesegiftcard.facades.constants.WooliesgcFacadesConstants;
import de.hybris.wooliesegiftcard.facades.dto.PersonalisationMediaWsDTO;
import de.hybris.wooliesegiftcard.facades.impl.DefaultWooliesCheckoutFacade;
import de.hybris.wooliesegiftcard.facades.order.impl.WooliesDefaultCartFacade;
import de.hybris.wooliesegiftcard.facades.product.data.PersonalisationMediaData;
import de.hybris.wooliesgiftcard.core.bulkorder.service.WooliesBulkOrderService;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.woolies.webservices.constants.WooliesgcWebServicesConstants;
import com.woolies.webservices.dto.PaymentDetails;
import com.woolies.webservices.dto.PaymentInfoDetails;
import com.woolies.webservices.dto.eGiftCardWsDTO;
import com.woolies.webservices.rest.cart.impl.CommerceWebServicesCartFacade;
import com.woolies.webservices.rest.exceptions.InvalidPaymentInfoException;
import com.woolies.webservices.rest.exceptions.NoCheckoutCartException;
import com.woolies.webservices.rest.exceptions.UnsupportedDeliveryModeException;
import com.woolies.webservices.rest.exceptions.UnsupportedRequestException;
import com.woolies.webservices.rest.exceptions.WooliesWebserviceException;
import com.woolies.webservices.rest.exceptions.WoolliesCartModificationException;
import com.woolies.webservices.rest.order.data.CartDataList;
import com.woolies.webservices.rest.order.data.OrderEntryDataList;
import com.woolies.webservices.rest.product.data.PromotionResultDataList;
import com.woolies.webservices.rest.request.support.impl.PaymentProviderRequestSupportedStrategy;
import com.woolies.webservices.rest.stock.CommerceStockFacade;
import com.woolies.webservices.rest.swagger.ApiBaseSiteIdAndUserIdParam;
import com.woolies.webservices.rest.swagger.ApiBaseSiteIdParam;
import com.woolies.webservices.rest.swagger.ApiBaseSiteIdUserIdAndCartIdParam;
import com.woolies.webservices.rest.voucher.data.VoucherDataList;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.Authorization;


@Controller
@RequestMapping(value = "/{baseSiteId}/users/{userId}/carts")
@CacheControl(directive = CacheControlDirective.NO_CACHE)
@Api(tags = "Carts")
public class CartsController extends BaseCommerceController
{
	private static final Logger LOG = Logger.getLogger(BaseCommerceController.class);
	private static final long DEFAULT_PRODUCT_QUANTITY = 1;
	@Resource(name = "commercePromotionRestrictionFacade")
	private CommercePromotionRestrictionFacade commercePromotionRestrictionFacade;
	@Resource(name = "commerceStockFacade")
	private CommerceStockFacade commerceStockFacade;
	@Resource(name = "customerFacade")
	private CustomerFacade customerFacade;
	@Resource(name = "pointOfServiceValidator")
	private Validator pointOfServiceValidator;
	@Resource(name = "orderEntryCreateValidator")
	private Validator orderEntryCreateValidator;
	@Resource(name = "orderEntryUpdateValidator")
	private Validator orderEntryUpdateValidator;
	@Resource(name = "orderEntryReplaceValidator")
	private Validator orderEntryReplaceValidator;
	@Resource(name = "greaterThanZeroValidator")
	private Validator greaterThanZeroValidator;
	@Resource(name = "paymentProviderRequestSupportedStrategy")
	private PaymentProviderRequestSupportedStrategy paymentProviderRequestSupportedStrategy;
	@Resource(name = "saveCartFacade")
	private SaveCartFacade saveCartFacade;
	@Resource(name = "voucherFacade")
	private VoucherFacade voucherFacade;
	@Resource(name = "wooliesDefaultCartFacade")
	private WooliesDefaultCartFacade wooliesDefaultCartFacade;
	@Resource(name = "wooliesGuestUserFacade")
	private WooliesGuestUserFacade wooliesGuestUserFacade;
	@Resource(name = "wooliesCustomerFacade")
	private WooliesDefaultCustomerFacade wooliesCustomerFacade;

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;

	@Resource(name = "wooliesPaymentFacade")
	private WooliesPaymentFacade wooliesPaymentFacade;

	@Resource(name = "profileSummeryFacade")
	ProfileSummeryFacade profileSummeryFacade;
	@Resource(name = "wooliesCheckoutFacade")
	private DefaultWooliesCheckoutFacade wooliesCheckoutFacade;
	@Resource(name = "cartFacade")
	private CartFacade cartFacade;

	@Resource(name = "wooliesBulkOrderService")
	private WooliesBulkOrderService wooliesBulkOrderService;

	@Autowired
	private UserService userService;

	@Resource(name = "wooliesMediaModelConverter")
	private Converter<MediaModel, MediaData> wooliesMediaModelConverter;

	@Resource(name = "mediaModelConverter")
	private Converter<MediaModel, MediaData> mediaModelConverter;

	@Resource(name = "cartService")
	private CartService cartService;

	@Resource(name = "mediaDao")
	private DefaultMediaDao mediaDao;


	/**
	 * This method is used to merge cart modification data between the cart objects
	 *
	 * @param CartModifiData1
	 * @param CartModifiData2
	 * @return CartModificationData
	 */
	protected static CartModificationData mergeCartModificationData(final CartModificationData CartModifiData1,
			final CartModificationData CartModifiData2)

	{
		if ((CartModifiData1 == null) && (CartModifiData2 == null))
		{
			return new CartModificationData();
		}
		if (CartModifiData1 == null)
		{
			return CartModifiData2;
		}
		if (CartModifiData2 == null)
		{
			return CartModifiData1;
		}
		final CartModificationData cartModificationData = new CartModificationData();
		cartModificationData.setDeliveryModeChanged(Boolean.valueOf(Boolean.TRUE.equals(CartModifiData1.getDeliveryModeChanged())
				|| Boolean.TRUE.equals(CartModifiData2.getDeliveryModeChanged())));
		cartModificationData.setEntry(CartModifiData2.getEntry());
		cartModificationData.setQuantity(CartModifiData2.getQuantity());
		cartModificationData.setQuantityAdded(CartModifiData1.getQuantityAdded() + CartModifiData2.getQuantityAdded());
		cartModificationData.setStatusCode(CartModifiData2.getStatusCode());
		return cartModificationData;
	}

	/**
	 * This method is used to get cart entry for the given entry number
	 *
	 * @param cart
	 * @param number
	 * @return
	 * @throws CartEntryException
	 */
	protected static OrderEntryData getCartEntryForNumber(final CartData cart, final long number) throws CartEntryException //NOSONAR
	{
		final List<OrderEntryData> orderEntries = cart.getEntries();
		if (orderEntries != null && !orderEntries.isEmpty())
		{
			final Integer requestedEntryNumber = Integer.valueOf((int) number);
			for (final OrderEntryData entry : orderEntries)
			{
				if (entry != null && requestedEntryNumber.equals(entry.getEntryNumber()))
				{
					return entry;
				}
			}
		}
		throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ENTRYNOTFOUND,
				WooliesgcWebServicesConstants.ERRMSG_ENTRYNOTFOUND, WooliesgcWebServicesConstants.ERRRSN_ENTRYNOTFOUND);
	}

	/**
	 * This method is used to get cart entry for the given entry number
	 *
	 * @param cart
	 * @param productCode
	 * @param pickupStore
	 * @return
	 */
	protected static OrderEntryData getCartEntry(final CartData cart, final String productCode, final String pickupStore)
	{
		for (final OrderEntryData oed : cart.getEntries())
		{
			if (oed.getProduct().getCode().equals(productCode))
			{
				if (pickupStore == null && oed.getDeliveryPointOfService() == null)
				{
					return oed;
				}
				else if (pickupStore != null && oed.getDeliveryPointOfService() != null
						&& pickupStore.equals(oed.getDeliveryPointOfService().getName()))
				{
					return oed;
				}
			}
		}
		return null;
	}

	/**
	 * This method is used to validate ambiguous positions
	 *
	 * @param currentCart
	 * @param currentEntry
	 * @param newPickupStore
	 * @throws CommerceCartModificationException
	 */
	protected static void validateForAmbiguousPositions(final CartData currentCart, final OrderEntryData currentEntry,
			final String newPickupStore) throws CommerceCartModificationException
	{
		final OrderEntryData entryToBeModified = getCartEntry(currentCart, currentEntry.getProduct().getCode(), newPickupStore);
		if (entryToBeModified != null && !entryToBeModified.equals(currentEntry))
		{
			throw new CartEntryException(
					"Ambiguous cart entries! Entry number " + currentEntry.getEntryNumber()
							+ " after change would be the same as entry " + entryToBeModified.getEntryNumber(),
					CartEntryException.AMBIGIOUS_ENTRY, entryToBeModified.getEntryNumber().toString());
		}
	}

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get all customer carts.", notes = "Lists all customer carts.")
	@ApiBaseSiteIdAndUserIdParam
	public CartListWsDTO getCarts(
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields,
			@ApiParam(value = "Optional parameter. If the parameter is provided and its value is true only saved carts are returned.") @RequestParam(required = false, defaultValue = "false") final boolean savedCartsOnly,
			@ApiParam(value = "Optional pagination parameter in case of savedCartsOnly == true. Default value 0.") @RequestParam(required = false, defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
			@ApiParam(value = "Optional {@link PaginationData} parameter in case of savedCartsOnly == true. Default value 20.") @RequestParam(required = false, defaultValue = DEFAULT_PAGE_SIZE) final int pageSize,
			@ApiParam(value = "Optional sort criterion in case of savedCartsOnly == true. No default value.") @RequestParam(required = false) final String sort)
	{
		if (getUserFacade().isAnonymousUser())
		{
			throw new AccessDeniedException("Access is denied");
		}

		final CartDataList cartDataList = new CartDataList();

		final PageableData pageableData = new PageableData();
		pageableData.setCurrentPage(currentPage);
		pageableData.setPageSize(pageSize);
		pageableData.setSort(sort);
		final List<CartData> allCarts = new ArrayList<>(
				saveCartFacade.getSavedCartsForCurrentUser(pageableData, null).getResults());
		if (!savedCartsOnly)
		{
			allCarts.addAll(getCartFacade().getCartsForCurrentUser());
		}
		cartDataList.setCarts(allCarts);

		return getDataMapper().map(cartDataList, CartListWsDTO.class, fields);
	}

	@RequestMapping(value =
	{ "/{cartId}/getCartDetails", "/getCartDetails" }, method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get a cart with a given identifier.", notes = "Returns the cart with a given identifier.")
	@ApiBaseSiteIdUserIdAndCartIdParam
	public CartWsDTO getCart(
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		// CartMatchingFilter sets current cart based on cartId, so we can return cart from the session
		return getDataMapper().map(getSessionCart(), CartWsDTO.class, fields);
	}

	/**
	 * This method is used to create cart for a user
	 *
	 * @param oldCartId
	 * @param toMergeCartGuid
	 * @param fields
	 * @return cartDTO
	 */
	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	@ApiOperation(value = "Creates or restore a cart for a user.", notes = "Creates a new cart or restores an anonymous cart as a user's cart (if an old Cart Id is given in the request).")
	@ApiBaseSiteIdAndUserIdParam
	public CartWsDTO createCart(@ApiParam(value = "Anonymous cart GUID.") @RequestParam(required = false) final String oldCartId,
			@ApiParam(value = "User's cart GUID to merge anonymous cart to.") @RequestParam(required = false) final String toMergeCartGuid,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("createCart");
		}

		String evaluatedToMergeCartGuid = toMergeCartGuid;

		if (StringUtils.isNotEmpty(oldCartId))
		{
			if (getUserFacade().isAnonymousUser())
			{
				throw new CartException("Anonymous user is not allowed to copy cart!");
			}

			if (!isCartAnonymous(oldCartId))
			{
				throw new CartException("Cart is not anonymous", CartException.CANNOT_RESTORE, oldCartId);
			}

			if (StringUtils.isEmpty(evaluatedToMergeCartGuid))
			{
				evaluatedToMergeCartGuid = getSessionCart().getGuid();
			}
			else
			{
				if (!isUserCart(evaluatedToMergeCartGuid))
				{
					throw new CartException("Cart is not current user's cart", CartException.CANNOT_RESTORE, evaluatedToMergeCartGuid);
				}
			}

			try
			{
				getCartFacade().restoreAnonymousCartAndMerge(oldCartId, evaluatedToMergeCartGuid);
				return getDataMapper().map(getSessionCart(), CartWsDTO.class, fields);
			}
			catch (final CommerceCartMergingException e)
			{
				throw new CartException("Couldn't merge carts", CartException.CANNOT_MERGE, e);
			}
			catch (final CommerceCartRestorationException e)
			{
				throw new CartException("Couldn't restore cart", CartException.CANNOT_RESTORE, e);
			}
		}
		else
		{
			if (StringUtils.isNotEmpty(evaluatedToMergeCartGuid))
			{
				if (!isUserCart(evaluatedToMergeCartGuid))
				{
					throw new CartException("Cart is not current user's cart", CartException.CANNOT_RESTORE, evaluatedToMergeCartGuid);
				}

				try
				{
					getCartFacade().restoreSavedCart(evaluatedToMergeCartGuid);
					return getDataMapper().map(getSessionCart(), CartWsDTO.class, fields);
				}
				catch (final CommerceCartRestorationException e)
				{
					throw new CartException("Couldn't restore cart", CartException.CANNOT_RESTORE, oldCartId, e);
				}

			}
			return getDataMapper().map(getSessionCart(), CartWsDTO.class, fields);
		}
	}

	/**
	 * This method check given cart is current user cart or not
	 *
	 * @param toMergeCartGuid
	 * @return
	 */
	protected boolean isUserCart(final String toMergeCartGuid)
	{
		if (getCartFacade() instanceof CommerceWebServicesCartFacade)
		{
			final CommerceWebServicesCartFacade commerceWebServicesCartFacade = (CommerceWebServicesCartFacade) getCartFacade();
			return commerceWebServicesCartFacade.isCurrentUserCart(toMergeCartGuid);
		}
		return true;
	}

	/**
	 * This metod checks given cart is anonymous user cart or not
	 *
	 * @param cartGuid
	 * @return isCartAnonymous
	 */
	protected boolean isCartAnonymous(final String cartGuid)
	{
		if (getCartFacade() instanceof CommerceWebServicesCartFacade)
		{
			final CommerceWebServicesCartFacade commerceWebServicesCartFacade = (CommerceWebServicesCartFacade) getCartFacade();
			return commerceWebServicesCartFacade.isAnonymousUserCart(cartGuid);
		}
		return true;
	}

	/**
	 * This method is used to delete the cart
	 */
	@RequestMapping(value =
	{ "/{cartId}/deleteCart", "/deleteCart" }, method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Deletes a cart with a given cart id.", notes = "Deletes a cart with a given cart id.")
	@ApiBaseSiteIdUserIdAndCartIdParam
	public void deleteCart()
	{
		getCartFacade().removeSessionCart();
	}

	/**
	 * This method is used for guest user login
	 *
	 * @param email
	 * @throws DuplicateUidException
	 */
	@Secured(
	{ "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT" })
	@RequestMapping(value = "/{cartId}/email", method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Assigns an email to the cart.", notes = "Assigns an email to the cart. This step is required to make a guest checkout.")
	@ApiBaseSiteIdUserIdAndCartIdParam
	public void guestLogin(
			@ApiParam(value = "Email of the guest user. It will be used during checkout process.", required = true) @RequestParam final String email)
			throws DuplicateUidException
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("createGuestUserForAnonymousCheckout: email=" + sanitize(email));
		}

		if (!EmailValidator.getInstance().isValid(email))
		{
			throw new RequestParameterException("Email [" + sanitize(email) + "] is not a valid e-mail address!",
					RequestParameterException.INVALID, "login");
		}

		customerFacade.createGuestUserForAnonymousCheckout(email, "guest");
	}

	/**
	 * This method is used to get cart entries
	 *
	 * @param fields
	 * @return OrderEntryListWsDTO
	 */
	@RequestMapping(value = "/{cartId}/entries", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get cart entries.", notes = "Returns cart entries.")
	@ApiBaseSiteIdUserIdAndCartIdParam
	public OrderEntryListWsDTO getCartEntries(
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("getCartEntries");
		}
		final OrderEntryDataList dataList = new OrderEntryDataList();
		dataList.setOrderEntries(getSessionCart().getEntries());
		return getDataMapper().map(dataList, OrderEntryListWsDTO.class, fields);
	}

	/**
	 * This method adds a product to the cart
	 *
	 * @param baseSiteId
	 * @param code
	 * @param qty
	 * @param pickupStore
	 * @param fields
	 * @return CartModificationWsDTO
	 * @throws CommerceCartModificationException
	 * @throws WebserviceValidationException
	 * @throws ProductLowStockException
	 * @throws StockSystemException
	 */
	@RequestMapping(value = "/{cartId}/entries", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(hidden = true, value = "Adds a product to the cart.", notes = "Adds a product to the cart.")
	@ApiBaseSiteIdUserIdAndCartIdParam
	public CartModificationWsDTO addCartEntry(@ApiParam(value = "Base site identifier.") @PathVariable final String baseSiteId,
			@ApiParam(value = "Code of the product to be added to cart. Product look-up is performed for the current product catalog version.") @RequestParam(required = true) final String code,
			@ApiParam(value = "Quantity of product.") @RequestParam(required = false, defaultValue = "1") final long qty,
			@ApiParam(value = "Name of the store where product will be picked. Set only if want to pick up from a store.") @RequestParam(required = false) final String pickupStore,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws CommerceCartModificationException, WebserviceValidationException, ProductLowStockException, StockSystemException //NOSONAR
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("addCartEntry: " + logParam("code", code) + ", " + logParam("qty", qty) + ", "
					+ logParam("pickupStore", pickupStore));
		}

		if (StringUtils.isNotEmpty(pickupStore))
		{
			validate(pickupStore, "pickupStore", pointOfServiceValidator);
		}

		return addCartEntryInternal(baseSiteId, code, qty, pickupStore, fields);
	}

	/**
	 * This method adds cart entry
	 *
	 * @param baseSiteId
	 * @param code
	 * @param qty
	 * @param pickupStore
	 * @param fields
	 * @return
	 * @throws CommerceCartModificationException
	 */
	protected CartModificationWsDTO addCartEntryInternal(final String baseSiteId, final String code, final long qty,
			final String pickupStore, final String fields) throws CommerceCartModificationException
	{
		final CartModificationData cartModificationData;
		if (StringUtils.isNotEmpty(pickupStore))
		{
			validateIfProductIsInStockInPOS(baseSiteId, code, pickupStore, null);
			cartModificationData = getCartFacade().addToCart(code, qty, pickupStore);
		}
		else
		{
			validateIfProductIsInStockOnline(baseSiteId, code, null);
			cartModificationData = getCartFacade().addToCart(code, qty);
		}
		return getDataMapper().map(cartModificationData, CartModificationWsDTO.class, fields);
	}

	/**
	 * This method is used to add a product to the cart
	 *
	 * @param baseSiteId
	 * @param entry
	 * @param fields
	 * @return
	 * @throws CommerceCartModificationException
	 * @throws WebserviceValidationException
	 * @throws ProductLowStockException
	 * @throws StockSystemException
	 */
	@RequestMapping(value = "/{cartId}/entries", method = RequestMethod.POST, consumes =
	{ MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseBody
	@ApiOperation(value = "Adds a product to the cart.", notes = "Adds a product to the cart.")
	@ApiBaseSiteIdUserIdAndCartIdParam
	public CartModificationWsDTO addCartEntry(@ApiParam(value = "Base site identifier") @PathVariable final String baseSiteId,
			@ApiParam(value = "Request body parameter (DTO in xml or json format) which contains details like : "
					+ "product code (product.code), quantity of product (quantity), pickup store name (deliveryPointOfService.name)", required = true) @RequestBody final OrderEntryWsDTO entry,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws CommerceCartModificationException, WebserviceValidationException, ProductLowStockException, StockSystemException //NOSONAR
	{
		if (entry.getQuantity() == null)
		{
			entry.setQuantity(Long.valueOf(DEFAULT_PRODUCT_QUANTITY));
		}

		validate(entry, "entry", orderEntryCreateValidator);

		final String pickupStore = entry.getDeliveryPointOfService() == null ? null : entry.getDeliveryPointOfService().getName();
		return addCartEntryInternal(baseSiteId, entry.getProduct().getCode(), entry.getQuantity().longValue(), pickupStore, fields);
	}

	/**
	 * This method is used to get the details of the cart entries
	 *
	 * @param entryNumber
	 * @param fields
	 * @return
	 */
	@RequestMapping(value = "/{cartId}/entries/{entryNumber}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get the details of the cart entries.", notes = "Returns the details of the cart entries.")
	@ApiBaseSiteIdUserIdAndCartIdParam
	public OrderEntryWsDTO getCartEntry(
			@ApiParam(value = "Entry number. Zero-based numbering.", required = true) @PathVariable final long entryNumber,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("getCartEntry: " + logParam("entryNumber", entryNumber)); //NOSONAR
		}
		final OrderEntryData orderEntry = getCartEntryForNumber(getSessionCart(), entryNumber);
		return getDataMapper().map(orderEntry, OrderEntryWsDTO.class, fields);
	}

	/**
	 * This method is used to set the cart entry
	 *
	 * @param baseSiteId
	 * @param entryNumber
	 * @param qty
	 * @param pickupStore
	 * @param fields
	 * @return
	 * @throws CommerceCartModificationException
	 */
	@RequestMapping(value = "/{cartId}/entries/{entryNumber}", method = RequestMethod.PUT)
	@ResponseBody
	@ApiOperation(hidden = true, value = "Set quantity and store details of a cart entry.", notes = "Updates the quantity of a single cart entry and details of the store where the cart "
			+ "entry will be picked. Attributes not provided in request will be defined again (set to null or default)")
	@ApiBaseSiteIdUserIdAndCartIdParam
	public CartModificationWsDTO setCartEntry(@ApiParam(value = "Base site identifier.") @PathVariable final String baseSiteId,
			@ApiParam(value = "Entry number. Zero-based numbering.") @PathVariable final long entryNumber,
			@ApiParam(value = "Quantity of product.") @RequestParam(required = true) final Long qty,
			@ApiParam(value = "Name of the store where product will be picked. Set only if want to pick up from a store.") @RequestParam(required = false) final String pickupStore,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws CommerceCartModificationException
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("setCartEntry: " + logParam("entryNumber", entryNumber) + ", " + logParam("qty", qty) + ", "
					+ logParam("pickupStore", pickupStore));
		}
		final CartData cart = getSessionCart();

		final OrderEntryData orderEntry = getCartEntryForNumber(cart, entryNumber);
		if (!StringUtils.isEmpty(pickupStore))
		{
			validate(pickupStore, "pickupStore", pointOfServiceValidator);
		}

		return updateCartEntryInternal(baseSiteId, cart, orderEntry, qty, pickupStore, fields, true);
	}

	/**
	 * This method is used to update the cart entry
	 *
	 * @param baseSiteId
	 * @param cart
	 * @param orderEntry
	 * @param qty
	 * @param pickupStore
	 * @param fields
	 * @param putMode
	 * @return
	 * @throws CommerceCartModificationException
	 */
	protected CartModificationWsDTO updateCartEntryInternal(final String baseSiteId, final CartData cart,
			final OrderEntryData orderEntry, final Long qty, final String pickupStore, final String fields, final boolean putMode)
			throws CommerceCartModificationException
	{
		final long entryNumber = orderEntry.getEntryNumber().longValue();
		final String productCode = orderEntry.getProduct().getCode();
		final PointOfServiceData currentPointOfService = orderEntry.getDeliveryPointOfService();

		CartModificationData cartModificationData1 = null;
		CartModificationData cartModificationData2 = null;

		if (!StringUtils.isEmpty(pickupStore))
		{
			if (currentPointOfService == null || !currentPointOfService.getName().equals(pickupStore))
			{
				//was 'shipping mode' or store is changed
				validateForAmbiguousPositions(cart, orderEntry, pickupStore);
				validateIfProductIsInStockInPOS(baseSiteId, productCode, pickupStore, Long.valueOf(entryNumber));
				cartModificationData1 = getCartFacade().updateCartEntry(entryNumber, pickupStore);
			}
		}
		else if (putMode && currentPointOfService != null)
		{
			//was 'pickup in store', now switch to 'shipping mode'
			validateForAmbiguousPositions(cart, orderEntry, pickupStore);
			validateIfProductIsInStockOnline(baseSiteId, productCode, Long.valueOf(entryNumber));
			cartModificationData1 = getCartFacade().updateCartEntry(entryNumber, pickupStore);
		}

		if (qty != null)
		{
			cartModificationData2 = getCartFacade().updateCartEntry(entryNumber, qty.longValue());
		}

		return getDataMapper().map(mergeCartModificationData(cartModificationData1, cartModificationData2),
				CartModificationWsDTO.class, fields);
	}

	/**
	 * This method is used to Set quantity and store details of a cart entry
	 *
	 * @param baseSiteId
	 * @param entryNumber
	 * @param entry
	 * @param fields
	 * @return
	 * @throws CommerceCartModificationException
	 */
	@RequestMapping(value = "/{cartId}/entries/{entryNumber}", method = RequestMethod.PUT, consumes =
	{ MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseBody
	@ApiOperation(value = "Set quantity and store details of a cart entry.", notes = "Updates the quantity of a single cart entry and details "
			+ "of the store where the cart entry will be picked. Attributes not provided in request will be defined again (set to null or default)")
	@ApiBaseSiteIdUserIdAndCartIdParam
	public CartModificationWsDTO setCartEntry(@ApiParam(value = "Base site identifier.") @PathVariable final String baseSiteId,
			@ApiParam(value = "Entry number. Zero-based numbering.", required = true) @PathVariable final long entryNumber,
			@ApiParam(value = "Request body parameter (DTO in xml or json format) which contains details like : quantity of product (quantity), pickup store name (deliveryPointOfService.name)", required = true) @RequestBody final OrderEntryWsDTO entry,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws CommerceCartModificationException
	{
		final CartData cart = getSessionCart();
		final OrderEntryData orderEntry = getCartEntryForNumber(cart, entryNumber);
		final String pickupStore = entry.getDeliveryPointOfService() == null ? null : entry.getDeliveryPointOfService().getName();

		validateCartEntryForReplace(orderEntry, entry);

		return updateCartEntryInternal(baseSiteId, cart, orderEntry, entry.getQuantity(), pickupStore, fields, true);
	}

	/**
	 * This method is used to validate cart entry
	 *
	 * @param oryginalEntry
	 * @param entry
	 */
	protected void validateCartEntryForReplace(final OrderEntryData oryginalEntry, final OrderEntryWsDTO entry)
	{
		final String productCode = oryginalEntry.getProduct().getCode();
		final Errors errors = new BeanPropertyBindingResult(entry, "entry");
		if (entry.getProduct() != null && entry.getProduct().getCode() != null && !entry.getProduct().getCode().equals(productCode))
		{
			errors.reject("cartEntry.productCodeNotMatch");
			throw new WebserviceValidationException(errors);
		}

		validate(entry, "entry", orderEntryReplaceValidator);
	}

	/**
	 * This method is used to update quantity and store details of a cart entry
	 *
	 * @param baseSiteId
	 * @param entryNumber
	 * @param qty
	 * @param pickupStore
	 * @param fields
	 * @return
	 * @throws CommerceCartModificationException
	 */
	@RequestMapping(value = "/{cartId}/entries/{entryNumber}", method = RequestMethod.PATCH)
	@ResponseBody
	@ApiOperation(hidden = true, value = "Update quantity and store details of a cart entry.", notes = "Updates the quantity of a single cart entry and details of the store where the cart entry will be picked.")
	@ApiBaseSiteIdUserIdAndCartIdParam
	public CartModificationWsDTO updateCartEntry(@ApiParam(value = "Base site identifier.") @PathVariable final String baseSiteId,
			@ApiParam(value = "Entry number. Zero-based numbering.", required = true) @PathVariable final long entryNumber,
			@ApiParam(value = "Quantity of product.") @RequestParam(required = false) final Long qty,
			@ApiParam(value = "Name of the store where product will be picked. Set only if want to pick up from a store.") @RequestParam(required = false) final String pickupStore,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws CommerceCartModificationException
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("updateCartEntry: " + logParam("entryNumber", entryNumber) + ", " + logParam("qty", qty) + ", "
					+ logParam("pickupStore", pickupStore));
		}

		final CartData cart = getSessionCart();
		final OrderEntryData orderEntry = getCartEntryForNumber(cart, entryNumber);

		if (qty == null && StringUtils.isEmpty(pickupStore))
		{
			throw new RequestParameterException("At least one parameter (qty,pickupStore) should be set!",
					RequestParameterException.MISSING);
		}

		if (qty != null)
		{
			validate(qty, "quantity", greaterThanZeroValidator);
		}

		if (pickupStore != null)
		{
			validate(pickupStore, "pickupStore", pointOfServiceValidator);
		}

		return updateCartEntryInternal(baseSiteId, cart, orderEntry, qty, pickupStore, fields, false);
	}

	/**
	 * This method is used to update quantity and store details of a cart entry
	 *
	 * @param baseSiteId
	 * @param entryNumber
	 * @param entry
	 * @param fields
	 * @return
	 * @throws CommerceCartModificationException
	 */
	@RequestMapping(value =
	{ "/{cartId}/entries/{entryNumber}", "/entries/{entryNumber}" }, method = RequestMethod.PATCH, consumes =
	{ MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseBody
	@ApiOperation(value = "Update quantity and store details of a cart entry.", notes = "Updates the quantity of a single cart entry and details of the store where the cart entry will be picked.")
	@ApiBaseSiteIdUserIdAndCartIdParam
	public CartWsDTO updateCartEntry(@ApiParam(value = "Base site identifier.") @PathVariable final String baseSiteId,
			@ApiParam(value = "Entry number. Zero-based numbering.", required = true) @PathVariable final long entryNumber,
			@ApiParam(value = "Request body parameter (DTO in xml or json format) which contains details like : quantity of product (quantity), pickup store name (deliveryPointOfService.name)", required = true) @RequestBody final OrderEntryWsDTO entry,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws CommerceCartModificationException
	{
		final CartData cart = getSessionCart();
		final OrderEntryData orderEntry = getCartEntryForNumber(cart, entryNumber);

		final String productCode = orderEntry.getProduct().getCode();
		final Errors errors = new BeanPropertyBindingResult(entry, "entry");
		if (entry.getProduct() != null && entry.getProduct().getCode() != null && !entry.getProduct().getCode().equals(productCode))
		{
			throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_UPDATECARTFORENTRY,
					WooliesgcWebServicesConstants.ERRMSG_UPDATECARTFORENTRY, WooliesgcWebServicesConstants.ERRRSN_UPDATECARTFORENTRY);
		}

		if (entry.getQuantity() == null)
		{
			entry.setQuantity(orderEntry.getQuantity());
		}
		if (entry.getCustomerPrice() != null)
		{
			if (entry.getCustomerPrice() < 0)
			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_CUSTOMERPRICECHECK,
						WooliesgcWebServicesConstants.ERRMSG_CUSTOMERPRICECHECK,
						WooliesgcWebServicesConstants.ERRRSN_CUSTOMERPRICECHECK);
			}
			if (!(entry.getCustomerPrice().doubleValue() >= configurationService.getConfiguration().getLong("minimum.card.value", 5)
					&& entry.getCustomerPrice().doubleValue() <= configurationService.getConfiguration().getLong("maximum.card.value",
							500)))
			{

				final StringBuilder errMsg = new StringBuilder(WooliesgcWebServicesConstants.ERRMSG_CARDLIMIT);
				errMsg.append(configurationService.getConfiguration().getLong("minimum.card.value", 5)).append(" and ")
						.append(configurationService.getConfiguration().getLong("maximum.card.value", 500));
				final StringBuilder errRsn = new StringBuilder(WooliesgcWebServicesConstants.ERRRSN_CARDLIMIT);
				errRsn.append(configurationService.getConfiguration().getLong("minimum.card.value", 5)).append(" and ")
						.append(configurationService.getConfiguration().getLong("maximum.card.value", 500));
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_CARDLIMIT, errMsg.toString(),
						errRsn.toString());
			}
		}

		validate(entry, "entry", orderEntryUpdateValidator);

		final String pickupStore = entry.getDeliveryPointOfService() == null ? null : entry.getDeliveryPointOfService().getName();
		final UserModel userModel = wooliesCustomerFacade.getUser();
		final CustomerModel customerModel = (CustomerModel) userModel;
		if (customerModel.getCustomerType() == UserDataType.B2B)
		{
			if (entry.getQuantity().longValue() > configurationService.getConfiguration().getLong("cart.limit.for.B2B.user", 9999))
			{
				final StringBuilder errMsg = new StringBuilder();
				errMsg.append(configurationService.getConfiguration().getLong("cart.limit.for.B2B.user", 9999));
				errMsg.append(WooliesgcWebServicesConstants.ERRMSG_CARTLIMITFORB2B);
				final StringBuilder errRsn = new StringBuilder();
				errRsn.append(configurationService.getConfiguration().getLong("cart.limit.for.B2B.user", 9999));
				errRsn.append(WooliesgcWebServicesConstants.ERRRSN_CARTLIMITFORB2B);
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_CARTLIMITFORB2B, errMsg.toString(),
						errRsn.toString());
			}
			try
			{

				final CorporateB2BCustomerModel corporateb2bcustomerModel = (CorporateB2BCustomerModel) customerModel;
				final Set<PrincipalGroupModel> allGroups = corporateb2bcustomerModel.getAllGroups();
				if (userService.getUserGroupForUID(WooliesgcFacadesConstants.B2BADMINGROUP) != null)
				{
					BigDecimal thresholdValue = null;
					final CorporateB2BUnitModel corporateb2BUnit = (CorporateB2BUnitModel) corporateb2bcustomerModel
							.getDefaultB2BUnit();
					final Set<B2BPermissionModel> b2bPermissionModel = corporateb2BUnit.getPermissions();
					if (!b2bPermissionModel.isEmpty())
					{
						for (final B2BPermissionModel b2bPermission : b2bPermissionModel)
						{
							if (b2bPermission.getCode().equalsIgnoreCase(corporateb2bcustomerModel.getUid()))
							{
								final B2BOrderThresholdPermissionModel b2bOrderPermissionModel = (B2BOrderThresholdPermissionModel) b2bPermission;
								thresholdValue = BigDecimal.valueOf(b2bOrderPermissionModel.getThreshold());
							}
						}
					}
					if (allGroups.contains(userService.getUserGroupForUID(WooliesgcFacadesConstants.B2BADMINGROUP)))
					{
						updateCartEntryInternal(baseSiteId, cart, orderEntry, entry.getQuantity(), pickupStore, fields, false,
								entry.getCustomerPrice());
					}

					else if (thresholdValue != null)
					{
						if (thresholdValue.doubleValue() == 0)
						{
							updateCartEntryInternal(baseSiteId, cart, orderEntry, entry.getQuantity(), pickupStore, fields, false,
									entry.getCustomerPrice());
						}
						final CartModel cartModel = wooliesDefaultCartFacade.getCart();
						double totalPrice = 0;
						for (final AbstractOrderEntryModel orderEntryModel : cartModel.getEntries())
						{
							if (orderEntryModel.getEntryNumber().longValue() == entryNumber)
							{
								final List<DiscountValue> discountApplied = orderEntryModel.getDiscountValues();
								double discount = 0;
								for (final DiscountValue discountValue : discountApplied)
								{
									discount += discountValue.getValue();
								}
								if (entry.getCustomerPrice() != null
										&& entry.getCustomerPrice().doubleValue() != orderEntryModel.getBasePrice().doubleValue())
								{
									totalPrice += (entry.getCustomerPrice().doubleValue() - discount) * entry.getQuantity().longValue();
								}
								else
								{
									totalPrice += (orderEntryModel.getBasePrice().doubleValue() - discount)
											* entry.getQuantity().longValue();
								}
							}
							else
							{
								totalPrice += orderEntryModel.getTotalPrice();
							}
						}
						final List<DiscountModel> globalDiscounts = cartModel.getDiscounts();
						for (final DiscountModel discountModel : globalDiscounts)
						{
							totalPrice -= discountModel.getValue().doubleValue();
						}

						if (totalPrice <= thresholdValue.doubleValue())
						{
							updateCartEntryInternal(baseSiteId, cart, orderEntry, entry.getQuantity(), pickupStore, fields, false,
									entry.getCustomerPrice());
						}
						else if (totalPrice > thresholdValue.doubleValue())
						{
							throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ORDERLIMITFORBUYER,
									WooliesgcWebServicesConstants.ERRMSG_ORDERLIMITFORBUYER + thresholdValue,
									WooliesgcWebServicesConstants.ERRRSN_ORDERLIMITFORBUYER + thresholdValue);
						}
					}
				}
			}
			catch (final WoolliesCartModificationException exception)
			{
				final StringBuilder errMsg = new StringBuilder();
				errMsg.append(configurationService.getConfiguration().getLong("cart.limit.for.B2B.user", 9999));
				errMsg.append(WooliesgcWebServicesConstants.ERRMSG_CARTLIMITFORB2B);
				final StringBuilder errRsn = new StringBuilder();
				errRsn.append(configurationService.getConfiguration().getLong("cart.limit.for.B2B.user", 9999));
				errRsn.append(WooliesgcWebServicesConstants.ERRRSN_CARTLIMITFORB2B);
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_CARTLIMITFORB2B, errMsg.toString(),
						errRsn.toString());
			}
		}
		else
		{
			try
			{
				updateCartEntryInternal(baseSiteId, cart, orderEntry, entry.getQuantity(), pickupStore, fields, false,
						entry.getCustomerPrice());
			}
			catch (final WoolliesCartModificationException exception)
			{
				if (exception.getMessage().equals(WooliesgcWebServicesConstants.ERRCODE_CARTLIMIT))
				{
					final StringBuilder errMsg = new StringBuilder();
					errMsg.append(configurationService.getConfiguration().getLong("cart.limit.for.user", 2));
					errMsg.append(WooliesgcWebServicesConstants.ERRMSG_CARTLIMIT);
					final StringBuilder errRsn = new StringBuilder();
					errRsn.append(configurationService.getConfiguration().getLong("cart.limit.for.user", 2));
					errRsn.append(WooliesgcWebServicesConstants.ERRRSN_CARTLIMIT);
					throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_CARTLIMIT, errMsg.toString(),
							errRsn.toString());
				}
				else if (exception.getMessage().equals("40023"))
				{
					final StringBuilder errMsg = new StringBuilder();
					errMsg.append(configurationService.getConfiguration().getLong("cart.limit.for.B2B.user", 9999));
					errMsg.append(WooliesgcWebServicesConstants.ERRMSG_CARTLIMITFORB2B);
					final StringBuilder errRsn = new StringBuilder();
					errRsn.append(configurationService.getConfiguration().getLong("cart.limit.for.B2B.user", 9999));
					errRsn.append(WooliesgcWebServicesConstants.ERRRSN_CARTLIMITFORB2B);
					throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_CARTLIMITFORB2B, errMsg.toString(),
							errRsn.toString());
				}
			}
			catch (final IllegalStateException exception)
			{
				if (exception.getMessage().contains(WooliesgcWebServicesConstants.ERRCODE_MULTIPRICE))
				{
					throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_MULTIPRICE,
							WooliesgcWebServicesConstants.ERRMSG_MULTIPRICE, WooliesgcWebServicesConstants.ERRRSN_MULTIPRICE);
				}
			}
		}
		return getDataMapper().map(getSessionCart(), CartWsDTO.class, "GETCART");
	}

	/**
	 * This method is used to remove cart entry
	 *
	 * @param entryNumber
	 * @return
	 * @throws CommerceCartModificationException
	 */
	@RequestMapping(value =
	{ "/{cartId}/entries/{entryNumber}", "/entries/{entryNumber}" }, method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@ApiOperation(value = "Deletes cart entry.", notes = "Deletes cart entry.")
	@ApiBaseSiteIdUserIdAndCartIdParam
	public CartWsDTO removeCartEntry(
			@ApiParam(value = "Entry number. Zero-based numbering.", required = true) @PathVariable final long entryNumber)
			throws CommerceCartModificationException
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("removeCartEntry: " + logParam("entryNumber", entryNumber));
		}

		final CartData cart = getSessionCart();
		getCartEntryForNumber(cart, entryNumber);
		getCartFacade().updateCartEntry(entryNumber, 0);
		final CartModel cartModel = wooliesDefaultCartFacade.getCart();
		final List<String> appliedCoupons = cart.getAppliedVouchers();
		if (cartModel != null && cartModel.getEntries() != null && cartModel.getEntries().isEmpty() && !appliedCoupons.isEmpty())
		{
			for (final String coupon : appliedCoupons)
			{
				try
				{
					getVoucherFacade().releaseVoucher(coupon);
				}
				catch (final VoucherOperationException e)
				{
					throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_VOUCHER_COUPON,
							WooliesgcWebServicesConstants.ERRCODE_VOUCHER_COUPON_MESSAGE,
							WooliesgcWebServicesConstants.ERRCODE_VOUCHER_EXCEPTION);
				}
			}
		}
		return getDataMapper().map(getSessionCart(), CartWsDTO.class, "GETCART");
	}


	/**
	 * This method is used to Create a delivery address for the cart
	 *
	 * @param request
	 * @param fields
	 * @return
	 * @throws WebserviceValidationException
	 * @throws NoCheckoutCartException
	 */
	@Secured(
	{ "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT" })
	@RequestMapping(value = "/{cartId}/addresses/delivery", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	@ApiOperation(hidden = true, value = "Creates a delivery adress for the cart.", notes = "Creates an address and assigns it to the cart as the delivery address.")
	@ApiBaseSiteIdUserIdAndCartIdParam
	public AddressWsDTO createAndSetAddress(final HttpServletRequest request,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws WebserviceValidationException, NoCheckoutCartException //NOSONAR
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("createAddress");
		}
		final AddressData addressData = super.createAddressInternal(request);
		final String addressId = addressData.getId();
		super.setCartDeliveryAddressInternal(addressId);
		return getDataMapper().map(addressData, AddressWsDTO.class, fields);
	}

	/**
	 * This method is used to Create a delivery address for the cart
	 *
	 * @param address
	 * @param fields
	 * @return
	 * @throws WebserviceValidationException
	 * @throws NoCheckoutCartException
	 */
	@Secured(
	{ "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT" })
	@RequestMapping(value = "/{cartId}/addresses/delivery", method = RequestMethod.POST, consumes =
	{ MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@ApiOperation(value = "Creates a delivery adress for the cart.", notes = "Creates an address and assigns it to the cart as the delivery address.")
	@ApiBaseSiteIdUserIdAndCartIdParam
	public AddressWsDTO createAndSetAddress(
			@ApiParam(value = "Request body parameter (DTO in xml or json format) which contains details like : Customer's first name(firstName), Customer's last name(lastName), Customer's title code(titleCode), "
					+ "country(country.isocode), first part of address(line1) , second part of address(line2), town (town), postal code(postalCode), region (region.isocode)", required = true) @RequestBody final AddressWsDTO address,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws WebserviceValidationException, NoCheckoutCartException //NOSONAR
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("createAddress");
		}
		AddressData addressData = getDataMapper().map(address, AddressData.class,
				"address1,address2,postalCode,city,state,country(isocode),defaultAddress,addressType");
		boolean newAddress = false;
		validate(address, "address", getAddressDTOValidator());
		if (address.getAddressID() == null)
		{
			addressData = createAddressInternal(addressData);
			newAddress = true;
			if (addressData.getAddressID() == null)
			{
				throw new CartAddressException("Address Limit reached. cannot update address");
			}

		}
		else
		{
			addressData.setAddressID(address.getAddressID());
		}
		setCartDeliveryAddressInternal(addressData, newAddress);
		return getDataMapper().map(addressData, AddressWsDTO.class, fields);
	}

	/**
	 * This method is used to create address object
	 *
	 * @param addressData
	 */
	@Override
	protected AddressData createAddressInternal(final AddressData addressData)
	{
		getUserFacade().addAddress(addressData);
		return addressData;
	}

	/**
	 * This method is used to set CartDeliveryAddress
	 *
	 * @param addressData
	 * @param newAddress
	 * @return
	 * @throws NoCheckoutCartException
	 */
	protected CartData setCartDeliveryAddressInternal(final AddressData addressData, final boolean newAddress)
			throws NoCheckoutCartException
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("setCartDeliveryAddressInternal: " + logParam("addressId", addressData.getAddressID()));
		}

		if (wooliesDefaultCartFacade.setDeliveryAddress(addressData, newAddress))
		{
			return getSessionCart();
		}
		throw new CartAddressException(
				"Address given by id " + sanitize(addressData.getAddressID()) + " cannot be set as delivery address in this cart");
	}

	/**
	 * This method is used to set CartDeliveryAddress
	 *
	 * @param addressId
	 * @throws NoCheckoutCartException
	 */
	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT" })
	@RequestMapping(value = "/{cartId}/addresses/delivery", method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Sets a delivery address for the cart.", notes = "Sets a delivery address for the cart. The address country must be placed among the delivery countries of the current base store.")
	@ApiBaseSiteIdUserIdAndCartIdParam
	public void setCartDeliveryAddress(
			@ApiParam(value = "Address identifier", required = true) @RequestParam(required = true) final String addressId)
			throws NoCheckoutCartException
	{
		super.setCartDeliveryAddressInternal(addressId);
	}

	/**
	 * This method is used to remove CartDeliveryAddress
	 */
	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT" })
	@RequestMapping(value = "/{cartId}/addresses/delivery", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Delete the delivery address from the cart.", notes = "Removes the delivery address from the cart.")
	@ApiBaseSiteIdUserIdAndCartIdParam
	public void removeCartDeliveryAddress()
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("removeDeliveryAddress");
		}
		if (!getCheckoutFacade().removeDeliveryAddress())
		{
			throw new CartException("Cannot reset address!", CartException.CANNOT_RESET_ADDRESS);
		}
	}

	/**
	 * This method is used to set get the delivery mode selected for the cart
	 *
	 * @param fields
	 * @return DeliveryModeWsDTO
	 */
	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT" })
	@RequestMapping(value = "/{cartId}/deliverymode", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get the delivery mode selected for the cart.", notes = "Returns the delivery mode selected for the cart.")
	@ApiBaseSiteIdUserIdAndCartIdParam
	public DeliveryModeWsDTO getCartDeliveryMode(
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("getCartDeliveryMode");
		}
		return getDataMapper().map(getSessionCart().getDeliveryMode(), DeliveryModeWsDTO.class, fields);
	}

	/**
	 * This method is used to set the delivery mode for a cart
	 *
	 * @param deliveryModeId
	 * @param fields
	 * @return
	 * @throws UnsupportedDeliveryModeException
	 */
	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT" })
	@RequestMapping(value =
	{ "/{cartId}/deliverymode", "/deliverymode" }, method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@ApiOperation(value = "Sets the delivery mode for a cart.", notes = "Sets the delivery mode with a given identifier for the cart.")
	@ApiBaseSiteIdUserIdAndCartIdParam
	public CartWsDTO setCartDeliveryMode(
			@ApiParam(value = "Delivery mode identifier (code)", required = true) @RequestParam(required = true) final String deliveryModeId,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws UnsupportedDeliveryModeException
	{
		try
		{
			final CartData cartData = super.setCartDeliveryModeInternal(deliveryModeId);
			return getDataMapper().map(cartData, CartWsDTO.class, "GETCART");
		}
		catch (final UnsupportedDeliveryModeException ex)
		{
			throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_DELIVERYMODENOTSUPPORT,
					WooliesgcWebServicesConstants.ERRMSG_DELIVERYMODENOTSUPPORT,
					WooliesgcWebServicesConstants.ERRRSN_DELIVERYMODENOTSUPPORT);
		}

	}

	/**
	 * This method deletet he delivery mode from the cart
	 */
	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT" })
	@RequestMapping(value =
	{ "/{cartId}/deliverymode", "/deliverymode" }, method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Delete the delivery mode from the cart.", notes = "Removes the delivery mode from the cart.")
	@ApiBaseSiteIdUserIdAndCartIdParam
	public void removeDeliveryMode()
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("removeDeliveryMode");
		}
		if (!getCheckoutFacade().removeDeliveryMode())
		{
			throw new CartException("Cannot reset delivery mode!", CartException.CANNOT_RESET_DELIVERYMODE);
		}
	}

	/**
	 * Get all delivery modes for the current store and delivery address
	 *
	 * @param fields
	 * @return
	 */
	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT" })
	@RequestMapping(value = "/{cartId}/deliverymodes", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get all delivery modes for the current store and delivery address.", notes = "Returns all delivery modes supported for the current "
			+ "base store and cart delivery address. A delivery address must be set for the cart, otherwise an empty list will be returned.")
	@ApiBaseSiteIdUserIdAndCartIdParam
	public DeliveryModeListWsDTO getSupportedDeliveryModes(
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("getSupportedDeliveryModes");
		}
		final DeliveryModesData deliveryModesData = new DeliveryModesData();
		deliveryModesData.setDeliveryModes(getCheckoutFacade().getSupportedDeliveryModes());

		return getDataMapper().map(deliveryModesData, DeliveryModeListWsDTO.class, fields);
	}

	/**
	 * This method used to add payment details
	 *
	 * @param request
	 * @param fields
	 * @return
	 * @throws WebserviceValidationException
	 * @throws InvalidPaymentInfoException
	 * @throws NoCheckoutCartException
	 * @throws UnsupportedRequestException
	 */
	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT" })
	@RequestMapping(value = "/{cartId}/paymentdetails", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	@ApiOperation(hidden = true, value = "Defines and assigns details of a new credit card payment to the cart.", notes = "Defines details of a new credit card payment details and assigns the payment to the cart.")
	@ApiBaseSiteIdUserIdAndCartIdParam
	public PaymentDetailsWsDTO addPaymentDetails(final HttpServletRequest request, //NOSONAR
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws WebserviceValidationException, InvalidPaymentInfoException, NoCheckoutCartException, UnsupportedRequestException //NOSONAR
	{
		paymentProviderRequestSupportedStrategy.checkIfRequestSupported("addPaymentDetails");
		final CCPaymentInfoData paymentInfoData = super.addPaymentDetailsInternal(request).getPaymentInfo();
		return getDataMapper().map(paymentInfoData, PaymentDetailsWsDTO.class, fields);
	}

	/**
	 * This method is used to add PaymentDetails
	 *
	 * @param paymentDetails
	 * @param fields
	 * @return PaymentDetailsWsDTO
	 * @throws WebserviceValidationException
	 * @throws InvalidPaymentInfoException
	 * @throws NoCheckoutCartException
	 * @throws UnsupportedRequestException
	 */
	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT" })
	@RequestMapping(value = "/{cartId}/paymentdetails", method = RequestMethod.POST, consumes =
	{ MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	@ApiOperation(value = "Defines and assigns details of a new credit card payment to the cart.", notes = "Defines details of a new credit card payment details and assigns the payment to the cart.")
	@ApiBaseSiteIdUserIdAndCartIdParam
	public PaymentDetailsWsDTO addPaymentDetails(
			@ApiParam(value = "Request body parameter (DTO in xml or json format) which contains details like : Name on card (accountHolderName), card number(cardNumber), card type (cardType.code), "
					+ "Month of expiry date (expiryMonth), Year of expiry date (expiryYear), if payment details should be saved (saved), if if the payment details should be used as default (defaultPaymentInfo), "
					+ "billing address (billingAddress.firstName, billingAddress.lastName, billingAddress.titleCode, billingAddress.country.isocode, billingAddress.line1, billingAddress.line2, "
					+ "billingAddress.town, billingAddress.postalCode, billingAddress.region.isocode)", required = true) @RequestBody final PaymentDetailsWsDTO paymentDetails, //NOSONAR
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws WebserviceValidationException, InvalidPaymentInfoException, NoCheckoutCartException, UnsupportedRequestException //NOSONAR
	{
		paymentProviderRequestSupportedStrategy.checkIfRequestSupported("addPaymentDetails");
		validatePayment(paymentDetails);
		final String copiedfields = "accountHolderName,cardNumber,cardType,cardTypeData(code),expiryMonth,expiryYear,issueNumber,startMonth,startYear,subscriptionId,defaultPaymentInfo,saved,"
				+ "billingAddress(titleCode,firstName,lastName,line1,line2,town,postalCode,country(isocode),region(isocode),defaultAddress)";
		CCPaymentInfoData paymentInfoData = getDataMapper().map(paymentDetails, CCPaymentInfoData.class, copiedfields);
		paymentInfoData = addPaymentDetailsInternal(paymentInfoData).getPaymentInfo();
		return getDataMapper().map(paymentInfoData, PaymentDetailsWsDTO.class, fields);
	}

	/**
	 * This method is used to validate payment
	 *
	 * @param paymentDetails
	 * @throws NoCheckoutCartException
	 */
	protected void validatePayment(final PaymentDetailsWsDTO paymentDetails) throws NoCheckoutCartException
	{
		if (!getCheckoutFacade().hasCheckoutCart())
		{
			throw new NoCheckoutCartException("Cannot add PaymentInfo. There was no checkout cart created yet!");
		}
		validate(paymentDetails, "paymentDetails", getPaymentDetailsDTOValidator());
	}

	/**
	 * This method is used to set payment details
	 *
	 * @param paymentDetailsId
	 * @throws InvalidPaymentInfoException
	 */
	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT" })
	@RequestMapping(value = "/{cartId}/paymentdetails", method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Sets credit card payment details for the cart.", notes = "Sets credit card payment details for the cart.")
	@ApiBaseSiteIdUserIdAndCartIdParam
	public void setPaymentDetails(
			@ApiParam(value = "Payment details identifier.", required = true) @RequestParam(required = true) final String paymentDetailsId)
			throws InvalidPaymentInfoException
	{
		super.setPaymentDetailsInternal(paymentDetailsId);
	}

	/**
	 * This method is used to information about promotions applied on cart
	 *
	 * @param fields
	 * @return
	 */
	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT" })
	@RequestMapping(value = "/{cartId}/promotions", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get information about promotions applied on cart.", notes = "Return information about promotions applied on cart. Requests pertaining "
			+ "to promotions have been developed for the previous version of promotions and vouchers and therefore some of them are currently not compatible with the new promotion engine.")
	@ApiBaseSiteIdUserIdAndCartIdParam
	public PromotionResultListWsDTO getPromotions(
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("getPromotions");
		}
		final List<PromotionResultData> appliedPromotions = new ArrayList<>();
		final List<PromotionResultData> orderPromotions = getSessionCart().getAppliedOrderPromotions();
		final List<PromotionResultData> productPromotions = getSessionCart().getAppliedProductPromotions();
		appliedPromotions.addAll(orderPromotions);
		appliedPromotions.addAll(productPromotions);

		final PromotionResultDataList dataList = new PromotionResultDataList();
		dataList.setPromotions(appliedPromotions);
		return getDataMapper().map(dataList, PromotionResultListWsDTO.class, fields);
	}

	/**
	 * This method is used to get promotion n cart
	 *
	 * @param promotionId
	 * @param fields
	 * @return PromotionResultListWsDTO
	 */
	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT" })
	@RequestMapping(value = "/{cartId}/promotions/{promotionId}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get information about promotions applied on cart.", notes = "Return information about promotion with given id, applied on cart. Requests pertaining to promotions "
			+ "have been developed for the previous version of promotions and vouchers and therefore some of them are currently not compatible with the new promotion engine.")
	@ApiBaseSiteIdUserIdAndCartIdParam
	public PromotionResultListWsDTO getPromotion(
			@ApiParam(value = "Promotion identifier (code)", required = true) @PathVariable final String promotionId,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("getPromotion: promotionId = " + sanitize(promotionId));
		}
		final List<PromotionResultData> appliedPromotions = new ArrayList<PromotionResultData>();
		final List<PromotionResultData> orderPromotions = getSessionCart().getAppliedOrderPromotions();
		final List<PromotionResultData> productPromotions = getSessionCart().getAppliedProductPromotions();
		for (final PromotionResultData prd : orderPromotions)
		{
			if (prd.getPromotionData().getCode().equals(promotionId))
			{
				appliedPromotions.add(prd);
			}
		}
		for (final PromotionResultData prd : productPromotions)
		{
			if (prd.getPromotionData().getCode().equals(promotionId))
			{
				appliedPromotions.add(prd);
			}
		}

		final PromotionResultDataList dataList = new PromotionResultDataList();
		dataList.setPromotions(appliedPromotions);
		return getDataMapper().map(dataList, PromotionResultListWsDTO.class, fields);
	}

	/**
	 * This method is used to apply and enable promotion for current cart
	 *
	 * @param promotionId
	 * @throws CommercePromotionRestrictionException
	 */
	@Secured(
	{ "ROLE_TRUSTED_CLIENT" })
	@RequestMapping(value = "/{cartId}/promotions", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Enables promotions based on the promotionsId of the cart.", notes = "Enables the promotion for the order based on the promotionId defined for the cart. "
			+ "Requests pertaining to promotions have been developed for the previous version of promotions and vouchers and therefore some of them are currently not compatible with the new promotion engine.", authorizations =
	{ @Authorization(value = "oauth2_client_credentials") })
	@ApiBaseSiteIdUserIdAndCartIdParam
	public void applyPromotion(
			@ApiParam(value = "Promotion identifier (code)", required = true) @RequestParam(required = true) final String promotionId)
			throws CommercePromotionRestrictionException
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("applyPromotion: promotionId = " + sanitize(promotionId));
		}
		commercePromotionRestrictionFacade.enablePromotionForCurrentCart(promotionId);
	}

	/**
	 * This method is used to remove promotion fro the cart
	 *
	 * @param promotionId
	 * @throws CommercePromotionRestrictionException
	 * @throws NoCheckoutCartException
	 */
	@Secured(
	{ "ROLE_TRUSTED_CLIENT" })
	@RequestMapping(value = "/{cartId}/promotions/{promotionId}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Disables the promotion based on the promotionsId of the cart.", notes = "Disables the promotion for the order based on the promotionId defined for the cart. "
			+ "Requests pertaining to promotions have been developed for the previous version of promotions and vouchers and therefore some of them are currently not compatible with the new promotion engine.", authorizations =
	{ @Authorization(value = "oauth2_client_credentials") })
	@ApiBaseSiteIdUserIdAndCartIdParam
	public void removePromotion(
			@ApiParam(value = "Promotion identifier (code)", required = true) @PathVariable final String promotionId) //NOSONAR
			throws CommercePromotionRestrictionException, NoCheckoutCartException //NOSONAR
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("removePromotion: promotionId = " + sanitize(promotionId));
		}
		commercePromotionRestrictionFacade.disablePromotionForCurrentCart(promotionId);
	}

	/**
	 * This method is used to
	 *
	 * @param fields
	 * @return
	 */
	@Secured(
	{ "ROLE_CLIENT", "ROLE_CUSTOMERGROUP", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_GUEST" })
	@RequestMapping(value = "/{cartId}/vouchers", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get a list of vouchers applied to the cart.", notes = "Returns list of vouchers applied to the cart.")
	@ApiBaseSiteIdUserIdAndCartIdParam
	public VoucherListWsDTO getVouchers(
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("getVouchers");
		}
		final VoucherDataList dataList = new VoucherDataList();
		dataList.setVouchers(voucherFacade.getVouchersForCart());
		return getDataMapper().map(dataList, VoucherListWsDTO.class, fields);
	}

	@Secured(
	{ "ROLE_CLIENT", "ROLE_CUSTOMERGROUP", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_GUEST" })
	@RequestMapping(value =
	{ "/{cartId}/vouchers", "/vouchers" }, method = RequestMethod.POST, consumes =
	{ MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Applies a voucher based on the voucherId defined for the cart.", notes = "Applies a voucher based on the voucherId defined for the cart.")
	@ApiBaseSiteIdUserIdAndCartIdParam
	@SuppressWarnings("squid:S1160")
	public CartWsDTO applyVoucherForCart(
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields,
			@ApiParam(value = "Voucher identifier (code)", required = true) @RequestParam(required = true) final String voucherId) //NOSONAR
			throws NoCheckoutCartException, VoucherOperationException //NOSONAR
	{
		super.applyVoucherForCartInternal(voucherId);
		return getDataMapper().map(getSessionCart(), CartWsDTO.class, "GETCART");
	}






	@Secured(
	{ "ROLE_CLIENT", "ROLE_CUSTOMERGROUP", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_GUEST" })
	@RequestMapping(value =
	{ "/{cartId}/vouchers/{voucherId}", "/vouchers/{voucherId}" }, method = RequestMethod.DELETE)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Delete a voucher defined for the current cart.", notes = "Removes a voucher based on the voucherId defined for the current cart.")
	@ApiBaseSiteIdUserIdAndCartIdParam
	@SuppressWarnings("squid:S1160")
	public CartWsDTO releaseVoucherFromCart(
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields,
			@ApiParam(value = "Voucher identifier (code)", required = true) @PathVariable final String voucherId) //NOSONAR
			throws NoCheckoutCartException, VoucherOperationException //NOSONAR
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("release voucher : voucherCode = " + sanitize(voucherId));
		}
		if (!getCheckoutFacade().hasCheckoutCart())
		{
			throw new NoCheckoutCartException("Cannot realese voucher. There was no checkout cart created yet!");
		}
		getVoucherFacade().releaseVoucher(voucherId);
		return getDataMapper().map(getSessionCart(), CartWsDTO.class, "GETCART");
	}


	protected void validateIfProductIsInStockInPOS(final String baseSiteId, final String productCode, final String storeName,
			final Long entryNumber)
	{
		if (!commerceStockFacade.isStockSystemEnabled(baseSiteId))
		{
			throw new StockSystemException("Stock system is not enabled on this site", StockSystemException.NOT_ENABLED, baseSiteId);
		}
		final StockData stock = commerceStockFacade.getStockDataForProductAndPointOfService(productCode, storeName);
		if (stock != null && stock.getStockLevelStatus().equals(StockLevelStatus.OUTOFSTOCK))
		{
			if (entryNumber != null)
			{
				throw new LowStockException("Product [" + sanitize(productCode) + "] is currently out of stock", //NOSONAR
						LowStockException.NO_STOCK, String.valueOf(entryNumber));
			}
			else
			{
				throw new ProductLowStockException("Product [" + sanitize(productCode) + "] is currently out of stock",
						LowStockException.NO_STOCK, productCode);
			}
		}
		else if (stock != null && stock.getStockLevelStatus().equals(StockLevelStatus.LOWSTOCK))
		{
			if (entryNumber != null)
			{
				throw new LowStockException("Not enough product in stock", LowStockException.LOW_STOCK, String.valueOf(entryNumber));
			}
			else
			{
				throw new ProductLowStockException("Not enough product in stock", LowStockException.LOW_STOCK, productCode);
			}
		}
	}

	protected void validateIfProductIsInStockOnline(final String baseSiteId, final String productCode, final Long entryNumber)
	{
		if (!commerceStockFacade.isStockSystemEnabled(baseSiteId))
		{
			throw new StockSystemException("Stock system is not enabled on this site", StockSystemException.NOT_ENABLED, baseSiteId);
		}
		final StockData stock = commerceStockFacade.getStockDataForProductAndBaseSite(productCode, baseSiteId);
		if (stock != null && stock.getStockLevelStatus().equals(StockLevelStatus.OUTOFSTOCK))
		{
			if (entryNumber != null)
			{
				throw new LowStockException("Product [" + sanitize(productCode) + "] cannot be shipped - out of stock online",
						LowStockException.NO_STOCK, String.valueOf(entryNumber));
			}
			else
			{
				throw new ProductLowStockException("Product [" + sanitize(productCode) + "] cannot be shipped - out of stock online",
						LowStockException.NO_STOCK, productCode);
			}
		}
	}

	@RequestMapping(value =
	{ "/{cartId}/cartEntries", "/cartEntries" }, method = RequestMethod.POST, consumes =
	{ MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseBody
	public CartModificationWsDTO addCartEntries(@PathVariable final String baseSiteId, @RequestBody final OrderEntryWsDTO entry,
			@RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws CommerceCartModificationException, WebserviceValidationException, ProductLowStockException, StockSystemException,
			WooliesB2BUserException//NOSONAR
	{
		if (entry.getQuantity() == null)
		{
			entry.setQuantity(Long.valueOf(DEFAULT_PRODUCT_QUANTITY));
		}
		validate(entry, "entry", orderEntryCreateValidator);

		final String pickupStore = entry.getDeliveryPointOfService() == null ? null : entry.getDeliveryPointOfService().getName();
		final UserModel userModel = wooliesCustomerFacade.getUser();
		final CustomerModel customerModel = (CustomerModel) userModel;
		BigDecimal thresholdValue = null;
		try
		{
			if (entry.getCustomerPrice() != null)
			{
				if (entry.getCustomerPrice() < 0)
				{
					throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_CUSTOMERPRICECHECK,
							WooliesgcWebServicesConstants.ERRMSG_CUSTOMERPRICECHECK,
							WooliesgcWebServicesConstants.ERRRSN_CUSTOMERPRICECHECK);
				}
				if (entry.getCustomerPrice().doubleValue() >= configurationService.getConfiguration().getLong("minimum.card.value", 5)
						&& entry.getCustomerPrice().doubleValue() <= configurationService.getConfiguration()
								.getLong("maximum.card.value", 500))
				{
					if (customerModel.getCustomerType() == UserDataType.B2B)
					{

						if (entry.getQuantity().longValue() > configurationService.getConfiguration().getLong("cart.limit.for.B2B.user",
								9999))
						{
							final StringBuilder errMsg = new StringBuilder();
							errMsg.append(configurationService.getConfiguration().getLong("cart.limit.for.B2B.user", 9999));
							errMsg.append(WooliesgcWebServicesConstants.ERRMSG_CARTLIMITFORB2B);
							final StringBuilder errRsn = new StringBuilder();
							errRsn.append(configurationService.getConfiguration().getLong("cart.limit.for.B2B.user", 9999));
							errRsn.append(WooliesgcWebServicesConstants.ERRRSN_CARTLIMITFORB2B);
							throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_CARTLIMITFORB2B,
									errMsg.toString(), errRsn.toString());
						}
						final CorporateB2BCustomerModel corporateb2bcustomerModel = (CorporateB2BCustomerModel) customerModel;
						try
						{

							final Set<PrincipalGroupModel> allGroups = corporateb2bcustomerModel.getAllGroups();
							if (userService.getUserGroupForUID(WooliesgcFacadesConstants.B2BADMINGROUP) != null)
							{

								final CorporateB2BUnitModel corporateb2BUnit = (CorporateB2BUnitModel) corporateb2bcustomerModel
										.getDefaultB2BUnit();
								final Set<B2BPermissionModel> b2bPermissionModel = corporateb2BUnit.getPermissions();
								if (!b2bPermissionModel.isEmpty())
								{
									for (final B2BPermissionModel b2bPermission : b2bPermissionModel)
									{
										if (b2bPermission.getCode().equalsIgnoreCase(corporateb2bcustomerModel.getUid()))
										{
											final B2BOrderThresholdPermissionModel b2bOrderPermissionModel = (B2BOrderThresholdPermissionModel) b2bPermission;
											thresholdValue = BigDecimal.valueOf(b2bOrderPermissionModel.getThreshold());
										}
									}
								}
								if (allGroups.contains(userService.getUserGroupForUID(WooliesgcFacadesConstants.B2BADMINGROUP)))
								{
									final CartModificationWsDTO cartModi = addCartEntryInternal(baseSiteId, entry.getProduct().getCode(),
											entry.getQuantity().longValue(), pickupStore, fields, entry.getCustomerPrice());

									return cartModi;
								}

								else if (null != thresholdValue)
								{
									if (thresholdValue.doubleValue() == 0)
									{
										final CartModificationWsDTO cartModi = addCartEntryInternal(baseSiteId,
												entry.getProduct().getCode(), entry.getQuantity().longValue(), pickupStore, fields,
												entry.getCustomerPrice());
										cartModi.setCartTotal(wooliesDefaultCartFacade.getCart().getTotalPrice());

										//cartModi.setTotalDiscounts(getPriceDataFactory().create(PriceDataType.BUY, totalDiscounts, source.getCurrency().getIsocode()));
										return cartModi;
									}
									final CartModel cartModel = wooliesDefaultCartFacade.getCart();
									final Double totalPrice = cartModel.getTotalPrice();

									if ((totalPrice.doubleValue()
											+ (entry.getCustomerPrice().doubleValue() * entry.getQuantity().longValue())) <= thresholdValue
													.doubleValue())
									{
										final CartModificationWsDTO cartModi = addCartEntryInternal(baseSiteId,
												entry.getProduct().getCode(), entry.getQuantity().longValue(), pickupStore, fields,
												entry.getCustomerPrice());
										cartModi.setCartTotal(wooliesDefaultCartFacade.getCart().getTotalPrice());
										//cartModi.setDiscount(wooliesDefaultCartFacade.getCart().getTotalDiscounts());
										return cartModi;
									}

									else if ((totalPrice.doubleValue()
											+ (entry.getCustomerPrice().doubleValue() * entry.getQuantity().doubleValue())) >= thresholdValue
													.doubleValue())
									{
										throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ORDERLIMITFORBUYER,
												WooliesgcWebServicesConstants.ERRMSG_ORDERLIMITFORBUYER + thresholdValue,
												WooliesgcWebServicesConstants.ERRRSN_ORDERLIMITFORBUYER + thresholdValue);
									}
								}


							}

						}
						catch (final WoolliesCartModificationException exception)
						{
							final StringBuilder errMsg = new StringBuilder();
							errMsg.append(configurationService.getConfiguration().getLong("cart.limit.for.B2B.user", 9999));
							errMsg.append(WooliesgcWebServicesConstants.ERRMSG_CARTLIMITFORB2B);
							final StringBuilder errRsn = new StringBuilder();
							errRsn.append(configurationService.getConfiguration().getLong("cart.limit.for.B2B.user", 9999));
							errRsn.append(WooliesgcWebServicesConstants.ERRRSN_CARTLIMITFORB2B);
							throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_CARTLIMITFORB2B,
									errMsg.toString(), errRsn.toString());
						}
					}
					else
					{
						final CartModificationWsDTO cartModi = addCartEntryInternal(baseSiteId, entry.getProduct().getCode(),
								entry.getQuantity().longValue(), pickupStore, fields, entry.getCustomerPrice());
						cartModi.setCartTotal(wooliesDefaultCartFacade.getCart().getTotalPrice());
						return cartModi;
					}
				}
				else
				{
					final StringBuilder errMsg = new StringBuilder(WooliesgcWebServicesConstants.ERRMSG_CARDLIMIT);
					errMsg.append(configurationService.getConfiguration().getLong("minimum.card.value", 5)).append(" and ")
							.append(configurationService.getConfiguration().getLong("maximum.card.value", 500));
					final StringBuilder errRsn = new StringBuilder(WooliesgcWebServicesConstants.ERRRSN_CARDLIMIT);
					errRsn.append(configurationService.getConfiguration().getLong("minimum.card.value", 5)).append(" and ")
							.append(configurationService.getConfiguration().getLong("maximum.card.value", 500));
					throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_CARDLIMIT, errMsg.toString(),
							errRsn.toString());
				}
			}
			else
			{
				final CartModificationWsDTO cartModi = addCartEntryInternal(baseSiteId, entry.getProduct().getCode(),
						entry.getQuantity().longValue(), pickupStore, fields, 0.0);
				cartModi.setCartTotal(wooliesDefaultCartFacade.getCart().getTotalPrice());
				return cartModi;
			}


		}
		catch (final WoolliesCartModificationException exception)
		{
			if (exception.getMessage().equals("40013"))
			{
				final StringBuilder errMsg = new StringBuilder();
				errMsg.append(configurationService.getConfiguration().getLong("cart.limit.for.user", 2));
				errMsg.append(WooliesgcWebServicesConstants.ERRMSG_CARTLIMIT);
				final StringBuilder errRsn = new StringBuilder();
				errRsn.append(configurationService.getConfiguration().getLong("cart.limit.for.user", 2));
				errRsn.append(WooliesgcWebServicesConstants.ERRRSN_CARTLIMIT);
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_CARTLIMIT, errMsg.toString(),
						errRsn.toString());
			}
			else if (exception.getMessage().equals("40023"))
			{
				final StringBuilder errMsg = new StringBuilder();
				errMsg.append(configurationService.getConfiguration().getLong("cart.limit.for.B2B.user", 9999));
				errMsg.append(WooliesgcWebServicesConstants.ERRMSG_CARTLIMITFORB2B);
				final StringBuilder errRsn = new StringBuilder();
				errRsn.append(configurationService.getConfiguration().getLong("cart.limit.for.B2B.user", 9999));
				errRsn.append(WooliesgcWebServicesConstants.ERRRSN_CARTLIMITFORB2B);
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_CARTLIMITFORB2B, errMsg.toString(),
						errRsn.toString());
			}
		}
		catch (final IllegalStateException exception)
		{
			if (exception.getMessage().contains(WooliesgcWebServicesConstants.ERRCODE_CUSTOMERPRICE))
			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_CUSTOMERPRICE,
						WooliesgcWebServicesConstants.ERRMSG_CUSTOMERPRICE, WooliesgcWebServicesConstants.ERRRSN_CUSTOMERPRICE);
			}
			if (exception.getMessage().contains(WooliesgcWebServicesConstants.ERRCODE_ORDERLIMITFORBUYER))
			{

				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ORDERLIMITFORBUYER,
						WooliesgcWebServicesConstants.ERRMSG_ORDERLIMITFORBUYER + thresholdValue,
						WooliesgcWebServicesConstants.ERRRSN_ORDERLIMITFORBUYER + thresholdValue);
			}
			if (exception.getMessage().contains(WooliesgcWebServicesConstants.ERRCODE_MULTIPRICE))
			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_MULTIPRICE,
						WooliesgcWebServicesConstants.ERRMSG_MULTIPRICE, WooliesgcWebServicesConstants.ERRRSN_MULTIPRICE);
			}
		}
		return getDataMapper().map(getSessionCart(), CartModificationWsDTO.class, fields);
	}

	/**
	 * @param baseSiteId
	 * @param code
	 * @param longValue
	 * @param pickupStore
	 * @param fields
	 * @param priceGivenByCustomer
	 * @return
	 * @throws CommerceCartModificationException
	 */
	private CartModificationWsDTO addCartEntryInternal(final String baseSiteId, final String code, final long qty,
			final String pickupStore, final String fields, final Double priceGivenByCustomer)
			throws CommerceCartModificationException
	{
		final CartModificationData cartModificationData;
		if (StringUtils.isNotEmpty(pickupStore))
		{
			validateIfProductIsInStockInPOS(baseSiteId, code, pickupStore, null);
			cartModificationData = wooliesDefaultCartFacade.addToCart(code, qty, pickupStore, priceGivenByCustomer);
		}
		else
		{
			validateIfProductIsInStockOnline(baseSiteId, code, null);
			cartModificationData = wooliesDefaultCartFacade.addToCart(code, qty, priceGivenByCustomer);
		}
		return getDataMapper().map(cartModificationData, CartModificationWsDTO.class, fields);
	}

	/**
	 * @param baseSiteId
	 * @param cart
	 * @param orderEntry
	 * @param quantity
	 * @param pickupStore
	 * @param fields
	 * @param b
	 * @param customerPrice
	 * @return
	 * @throws CommerceCartModificationException
	 */
	private CartModificationWsDTO updateCartEntryInternal(final String baseSiteId, final CartData cart,
			final OrderEntryData orderEntry, final Long quantity, final String pickupStore, final String fields,
			final boolean putMode, final Double customerPrice) throws CommerceCartModificationException
	{

		final long entryNumber = orderEntry.getEntryNumber().longValue();
		final String productCode = orderEntry.getProduct().getCode();
		final PointOfServiceData currentPointOfService = orderEntry.getDeliveryPointOfService();

		CartModificationData cartModificationData1 = null;
		CartModificationData cartModificationData2 = null;

		if (!StringUtils.isEmpty(pickupStore))
		{
			if (currentPointOfService == null || !currentPointOfService.getName().equals(pickupStore))
			{
				//was 'shipping mode' or store is changed
				validateForAmbiguousPositions(cart, orderEntry, pickupStore);
				validateIfProductIsInStockInPOS(baseSiteId, productCode, pickupStore, Long.valueOf(entryNumber));
				cartModificationData1 = getCartFacade().updateCartEntry(entryNumber, pickupStore);
			}
		}
		else if (putMode && currentPointOfService != null)
		{
			//was 'pickup in store', now switch to 'shipping mode'
			validateForAmbiguousPositions(cart, orderEntry, pickupStore);
			validateIfProductIsInStockOnline(baseSiteId, productCode, Long.valueOf(entryNumber));
			cartModificationData1 = getCartFacade().updateCartEntry(entryNumber, pickupStore);
		}

		if (quantity != null || customerPrice != null)
		{
			cartModificationData2 = wooliesDefaultCartFacade.updateCartEntry(entryNumber, quantity.longValue(), customerPrice);
		}

		return getDataMapper().map(mergeCartModificationData(cartModificationData1, cartModificationData2),
				CartModificationWsDTO.class, fields);

	}

	// for capture Guest Customer Details

	@Secured(
	{ "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value =
	{ "/{cartId}/guestUser", "/guestUser" }, method = RequestMethod.POST, consumes =
	{ MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@SuppressWarnings("squid:S1160")
	@ApiOperation(value = " Guest customer", notes = "Guest customer.  It is used to convert a guest to a customer. In this case the required parameters are: guid, password.")
	@ApiBaseSiteIdParam
	public void guestUser(@ApiParam(value = "User's object.", required = true) @RequestBody final UserSignUpWsDTO user,
			final HttpServletRequest httpRequest, final HttpServletResponse httpResponse)
			throws DuplicateUidException, UnknownIdentifierException, //NOSONAR
			IllegalArgumentException, WebserviceValidationException, UnsupportedEncodingException //NOSONAR
	{
		if (user.getAddresses() == null)
		{
			throw new RequestParameterException("Address is missing for the user", RequestParameterException.MISSING, "addresses");
		}
		else
		{
			final List<AddressData> addresses = user.getAddresses();
			boolean havingBillingAddress = false;
			boolean havingShippingAddress = false;
			for (final AddressData addressData : addresses)
			{
				if (addressData.getIsBilling().booleanValue())
				{
					havingBillingAddress = true;
				}
				else if (addressData.getIsShipping().booleanValue())
				{
					havingShippingAddress = true;
				}
			}
			if (!havingBillingAddress || !havingShippingAddress)
			{
				throw new RequestParameterException("Shipping or billing address is missing for the user",
						RequestParameterException.MISSING, "addresses");

			}
		}
		final CustomerData guestCustomerData = getDataMapper().map(user, CustomerData.class);
		guestCustomerData.setUid(user.getUid());
		wooliesGuestUserFacade.createGuestUserForAnonymousCheckout(guestCustomerData, "guest", cartService.getSessionCart());
	}


	// for capture Guest Customer Details

	/*
	 * @Secured( { "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	 */
	@RequestMapping(value =
	{ "/{cartId}/paymentOpt", "/paymentOpt" }, method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.OK)
	@ResponseBody
	@SuppressWarnings("squid:S1160")
	@ApiOperation(value = "Payment Options", notes = "Payment options for muultiple users")
	@ApiBaseSiteIdParam
	public ResponseEntity<Object> getPaymentOptions(
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws DuplicateUidException, UnknownIdentifierException, //NOSONAR
			IllegalArgumentException, WebserviceValidationException, UnsupportedEncodingException //NOSONAR
	{

		final CartData cart = getSessionCart();
		final List<PaymentDetails> paymentDetails = new ArrayList<>();
		final UserModel userModel = wooliesCustomerFacade.getUser();
		final boolean isAnonymousUser = wooliesCustomerFacade.isAnonymousUser(userModel);


		PaymentInfoDetails paymentInfoDetails = new PaymentInfoDetails();
		try
		{
			paymentInfoDetails = wooliesPaymentFacade.getPaymentDetails(userModel, isAnonymousUser, cart.getTotalPrice().getValue());
		}
		catch (final WooliesFacadeLayerException e)
		{
			throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_PAYMENT,
					WooliesgcWebServicesConstants.ERRRSN_PAYMENT_MSG, WooliesgcWebServicesConstants.ERRMSG_PAYMENT_DESC);
		}

		return new ResponseEntity<Object>(paymentInfoDetails, HttpStatus.OK);
	}



	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value =
	{ "/{cartID}/deliveryOptions", "/deliveryOptions" }, method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get customer profile", notes = "Returns customer profile.")
	@ApiBaseSiteIdAndUserIdParam
	public DeliveryModeListWsDTO getDeliveryOptions(
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL, LOGINCHECKOUT") @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws UnsupportedDeliveryModeException
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Get delivery modes for the cart");
		}
		final DeliveryModesData deliveryModesData = new DeliveryModesData();
		deliveryModesData.setDeliveryModes(wooliesDefaultCartFacade.getDeliveryMode());
		if (deliveryModesData.getDeliveryModes().isEmpty())
		{
			throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_DELIVERYMODE,
					WooliesgcWebServicesConstants.ERRMSG_DELIVERYMODE, WooliesgcWebServicesConstants.ERRRSN_DELIVERYMODE);
		}
		return getDataMapper().map(deliveryModesData, DeliveryModeListWsDTO.class, fields);
	}

	@RequestMapping(value =
	{ "/{cartId}/entries/{entryNumber}/PID", "/entries/{entryNumber}/PID" }, method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Returns the PID for the eGiftCards.", notes = "Returns the PID for the eGiftCards")
	@ApiBaseSiteIdUserIdAndCartIdParam
	public OrderEntryWsDTO getPID(@ApiParam("EntryNumber") @PathVariable final int entryNumber,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws WooliesWebserviceException
	{
		final CartModel cartModel = wooliesDefaultCartFacade.getCart();
		if (cartModel.getEntries().size() - 1 < entryNumber)
		{
			throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ENTRYNOTFOUND,
					WooliesgcWebServicesConstants.ERRMSG_ENTRYNOTFOUND, WooliesgcWebServicesConstants.ERRRSN_ENTRYNOTFOUND);
		}
		else if (!cartModel.getEntries().get(entryNumber).getProduct().getIsEGiftCard())
		{
			throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ISNOTEGIFTCARD,
					WooliesgcWebServicesConstants.ERRMSG_ISNOTEGIFTCARD, WooliesgcWebServicesConstants.ERRRSN_ISNOTEGIFTCARD);
		}
		final OrderEntryData orderData = wooliesDefaultCartFacade.generatePIDForeGiftCard(cartModel.getEntries().get(entryNumber));
		return getDataMapper().map(orderData, OrderEntryWsDTO.class, fields);
	}

	@RequestMapping(value =
	{ "/{cartId}/entries/{entryNumber}/PID/{PIDNo}", "/entries/{entryNumber}/PID/{PIDNo}" }, method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdUserIdAndCartIdParam
	public OrderEntryWsDTO addPersonalisationForEgiftCard(@ApiParam("EntryNumber") @PathVariable final int entryNumber,
			@ApiParam("PIDNo") @PathVariable final int PIDNo, @RequestBody final eGiftCardWsDTO eGiftCardCustomization,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws WooliesWebserviceException
	{
		final CartModel cartModel = wooliesDefaultCartFacade.getCart();
		if (cartModel.getEntries().size() - 1 < entryNumber)
		{
			throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ENTRYNOTFOUND,
					WooliesgcWebServicesConstants.ERRMSG_ENTRYNOTFOUND, WooliesgcWebServicesConstants.ERRRSN_ENTRYNOTFOUND);
		}
		else if (!cartModel.getEntries().get(entryNumber).getProduct().getIsEGiftCard())
		{
			throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ISNOTEGIFTCARD,
					WooliesgcWebServicesConstants.ERRMSG_ISNOTEGIFTCARD, WooliesgcWebServicesConstants.ERRRSN_ISNOTEGIFTCARD);
		}
		else if (!cartModel.getEntries().get(entryNumber).getProduct().getIsEGiftCard())
		{
			throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ISNOTEGIFTCARD,
					WooliesgcWebServicesConstants.ERRMSG_ISNOTEGIFTCARD, WooliesgcWebServicesConstants.ERRRSN_ISNOTEGIFTCARD);
		}
		else
		{
			final Collection<PersonalisationEGiftCardModel> eGiftCards = cartModel.getEntries().get(entryNumber)
					.getPersonalisationDetail();
			boolean isPIDExist = false;
			for (final PersonalisationEGiftCardModel personalisationEGiftCardModel : eGiftCards)
			{
				if (personalisationEGiftCardModel.getPid().intValue() == PIDNo)
				{
					isPIDExist = true;
					break;
				}
			}
			if (!isPIDExist)
			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ISNOTPIDEXIST,
						WooliesgcWebServicesConstants.ERRMSG_ISNOTPIDEXIST, WooliesgcWebServicesConstants.ERRRSN_ISNOTPIDEXIST);
			}
		}
		OrderEntryData orderData = null;
		try
		{
			orderData = wooliesDefaultCartFacade.applyPersnalisationForeGiftCard(cartModel, cartModel.getEntries().get(entryNumber),
					PIDNo, eGiftCardCustomization);
		}
		catch (final IllegalArgumentException ex)
		{
			if (ex.getMessage().equals("40012"))
			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_PIDIMAGENOTFOUND,
						WooliesgcWebServicesConstants.ERRMSG_PIDIMAGENOTFOUND, WooliesgcWebServicesConstants.ERRRSN_PIDIMAGENOTFOUND);
			}
		}
		if (null != orderData)
		{
			return getDataMapper().map(orderData, OrderEntryWsDTO.class, fields);
		}
		else
		{
			return getDataMapper().map(null, OrderEntryWsDTO.class, fields);
		}
	}

	/**
	 * @param entryNumber
	 *           This is used for remove PID on cartLineIntem
	 */
	@RequestMapping(value =
	{ "/{cartId}/entries/{entryNumber}/removePID", "/entries/{entryNumber}/removePID" }, method = RequestMethod.DELETE)
	@ResponseStatus(value = HttpStatus.OK)
	@ApiBaseSiteIdUserIdAndCartIdParam
	public void removePersonalisationForEgiftCard(@ApiParam("EntryNumber") @PathVariable final int entryNumber)

	{
		final CartModel cartModel = wooliesDefaultCartFacade.getCart();
		if (cartModel.getEntries().size() - 1 < entryNumber)
		{
			throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ENTRYNOTFOUND,
					WooliesgcWebServicesConstants.ERRMSG_ENTRYNOTFOUND, WooliesgcWebServicesConstants.ERRRSN_ENTRYNOTFOUND);
		}
		else if (!cartModel.getEntries().get(entryNumber).getProduct().getIsEGiftCard())
		{
			throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ISNOTEGIFTCARD,
					WooliesgcWebServicesConstants.ERRMSG_ISNOTEGIFTCARD, WooliesgcWebServicesConstants.ERRRSN_ISNOTEGIFTCARD);
		}
		else
		{
			try
			{
				wooliesDefaultCartFacade.removePersonalisationForEgiftCard(cartModel, entryNumber);
			}
			catch (final WooliesFacadeLayerException e)
			{
				throw new WooliesWebserviceException(e.getErrorCode(), e.getMessage(), e.getErrorReason());
			}
		}
	}

	@RequestMapping(value =
	{ "/{cartId}/entries/{entryNumber}/PID/{PIDNo}/imageData",
			"/entries/{entryNumber}/PID/{PIDNo}/imageData" }, method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "Saves image data for customer.", notes = "Saves image data for customer")
	@ApiBaseSiteIdUserIdAndCartIdParam
	public PersonalisationMediaWsDTO getImageData(@ApiParam("EntryNumber") @PathVariable final int entryNumber,
			@ApiParam("PIDNo") @PathVariable final int PIDNo, @RequestBody final MediaWsDTO media,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL, MEDIA") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws WooliesWebserviceException
	{
		final CartModel cartModel = wooliesDefaultCartFacade.getCart();
		if (cartModel.getEntries().size() - 1 < entryNumber)
		{
			throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ENTRYNOTFOUND,
					WooliesgcWebServicesConstants.ERRMSG_ENTRYNOTFOUND, WooliesgcWebServicesConstants.ERRRSN_ENTRYNOTFOUND);
		}
		else if (!cartModel.getEntries().get(entryNumber).getProduct().getIsEGiftCard())
		{
			throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ISNOTEGIFTCARD,
					WooliesgcWebServicesConstants.ERRMSG_ISNOTEGIFTCARD, WooliesgcWebServicesConstants.ERRRSN_ISNOTEGIFTCARD);
		}
		else
		{
			final Collection<PersonalisationEGiftCardModel> eGiftCards = cartModel.getEntries().get(entryNumber)
					.getPersonalisationDetail();
			boolean isPIDExist = false;
			for (final PersonalisationEGiftCardModel personalisationEGiftCardModel : eGiftCards)
			{
				if (personalisationEGiftCardModel.getPid().intValue() == PIDNo)
				{
					isPIDExist = true;
				}
			}
			if (!isPIDExist)
			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ISNOTPIDEXIST,
						WooliesgcWebServicesConstants.ERRMSG_ISNOTPIDEXIST, WooliesgcWebServicesConstants.ERRRSN_ISNOTPIDEXIST);
			}

		}
		final PersonalisationMediaData mediaData = wooliesDefaultCartFacade.saveImageForCustomer(PIDNo, media.getUrl());
		return getDataMapper().map(mediaData, PersonalisationMediaWsDTO.class, fields);
	}

	@RequestMapping(value =
	{ "/{cartId}/entries/{entryNumber}/coBrandID/{coBrandID}",
			"/entries/{entryNumber}/coBrandID/{coBrandID}" }, method = RequestMethod.PATCH)
	@ResponseBody
	@ApiOperation(value = "Get a cart with a given identifier.", notes = "Returns the cart with a given identifier.")
	@ApiBaseSiteIdUserIdAndCartIdParam
	public CartWsDTO getCoBrandCart(@ApiParam("EntryNumber") @PathVariable final int entryNumber,
			@ApiParam("CoBrandID") @PathVariable final String coBrandID,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		final CartModel cartModel = wooliesDefaultCartFacade.getCart();
		if (cartModel.getEntries().size() - 1 < entryNumber)
		{
			throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ENTRYNOTFOUND,
					WooliesgcWebServicesConstants.ERRMSG_ENTRYNOTFOUND, WooliesgcWebServicesConstants.ERRRSN_ENTRYNOTFOUND);
		}
		final boolean isUpdated = wooliesDefaultCartFacade.updateCoBrandData(cartModel, entryNumber, coBrandID);
		if (isUpdated)
		{
			return getDataMapper().map(getSessionCart(), CartWsDTO.class, fields);
		}
		else
		{
			throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_COBRANDNOTFOUND,
					WooliesgcWebServicesConstants.ERRMSG_COBRANDNOTFOUND, WooliesgcWebServicesConstants.ERRRSN_COBRANDNOTFOUND);
		}
	}

	@RequestMapping(value =
	{ "/{cartId}/entries/{entryNumber}/removeCoBrand", "/entries/{entryNumber}/removeCoBrand" }, method = RequestMethod.PATCH)
	@ResponseBody
	@ApiOperation(value = "Get a cart with a given identifier.", notes = "Returns the cart with a given identifier.")
	@ApiBaseSiteIdUserIdAndCartIdParam
	public CartWsDTO removeCoBrandForCart(@ApiParam("EntryNumber") @PathVariable final int entryNumber,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		final CartModel cartModel = wooliesDefaultCartFacade.getCart();
		if (cartModel.getEntries().size() - 1 < entryNumber)
		{
			throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ENTRYNOTFOUND,
					WooliesgcWebServicesConstants.ERRMSG_ENTRYNOTFOUND, WooliesgcWebServicesConstants.ERRRSN_ENTRYNOTFOUND);
		}
		wooliesDefaultCartFacade.removeCoBrandData(cartModel, entryNumber);
		return getDataMapper().map(getSessionCart(), CartWsDTO.class, fields);
	}

	@RequestMapping(value =
	{ "/{cartId}/entries/{entryNumber}/PID/{PIDNo}", "/entries/{entryNumber}/PID/{PIDNo}" }, method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdUserIdAndCartIdParam
	public eGiftCardWsDTO getPersonalisationForEgiftCard(@ApiParam("EntryNumber") @PathVariable final int entryNumber,
			@ApiParam("PIDNo") @PathVariable final int PIDNo,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws WooliesWebserviceException
	{
		final CartModel cartModel = wooliesDefaultCartFacade.getCart();
		if (cartModel.getEntries().size() - 1 < entryNumber)
		{
			throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ENTRYNOTFOUND,
					WooliesgcWebServicesConstants.ERRMSG_ENTRYNOTFOUND, WooliesgcWebServicesConstants.ERRRSN_ENTRYNOTFOUND);
		}
		else if (!cartModel.getEntries().get(entryNumber).getProduct().getIsEGiftCard())
		{
			throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ISNOTEGIFTCARD,
					WooliesgcWebServicesConstants.ERRMSG_ISNOTEGIFTCARD, WooliesgcWebServicesConstants.ERRRSN_ISNOTEGIFTCARD);
		}
		else if (!cartModel.getEntries().get(entryNumber).getProduct().getIsEGiftCard())
		{
			throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ISNOTEGIFTCARD,
					WooliesgcWebServicesConstants.ERRMSG_ISNOTEGIFTCARD, WooliesgcWebServicesConstants.ERRRSN_ISNOTEGIFTCARD);
		}
		else
		{
			final Collection<PersonalisationEGiftCardModel> eGiftCards = cartModel.getEntries().get(entryNumber)
					.getPersonalisationDetail();
			boolean isPIDExist = false;
			for (final PersonalisationEGiftCardModel personalisationEGiftCardModel : eGiftCards)
			{
				if (personalisationEGiftCardModel.getPid().intValue() == PIDNo)
				{
					isPIDExist = true;
					final eGiftCardWsDTO giftCardWsDTO = new eGiftCardWsDTO();
					if (null != personalisationEGiftCardModel.getFromName())
					{
						giftCardWsDTO.setFromName(personalisationEGiftCardModel.getFromName());
					}
					if (null != personalisationEGiftCardModel.getToName())
					{
						giftCardWsDTO.setToName(personalisationEGiftCardModel.getToName());
					}
					if (null != personalisationEGiftCardModel.getCustomerImage())
					{
						giftCardWsDTO.setCustomImage(
								getDataMapper().map(wooliesMediaModelConverter.convert(personalisationEGiftCardModel.getCustomerImage()),
										MediaWsDTO.class, "code,url"));
					}
					if (null != personalisationEGiftCardModel.getMessage())
					{
						giftCardWsDTO.setMessage(personalisationEGiftCardModel.getMessage());
					}
					if (null != personalisationEGiftCardModel.getToEmail())
					{
						giftCardWsDTO.setToEmail(personalisationEGiftCardModel.getToEmail());
					}
					if (cartModel.getEntries().get(entryNumber).getProduct().getThumbnail() != null)
					{
						final String absolutePath = configurationService.getConfiguration()
								.getString("website.woolworths.giftcards.https");
						final MediaData productBanner = mediaModelConverter
								.convert(cartModel.getEntries().get(entryNumber).getProduct().getThumbnail());
						productBanner.setUrl(absolutePath + productBanner.getUrl());
						giftCardWsDTO.setProductBannerImage(getDataMapper().map(productBanner, MediaWsDTO.class, "code,url"));
					}
					final String defaultImage = configurationService.getConfiguration()
							.getString(WooliesgcWebServicesConstants.DEFAULTIMAGEPROPERTY, WooliesgcWebServicesConstants.DEFAULTIMAGE);
					final List<MediaModel> mediaModel = mediaDao
							.findMediaByCode(cartModel.getEntries().get(entryNumber).getProduct().getCatalogVersion(), defaultImage);
					if (CollectionUtils.isNotEmpty(mediaModel))
					{
						final String absolutePath = configurationService.getConfiguration()
								.getString("website.woolworths.giftcards.https");
						final MediaData defaultImage1 = mediaModelConverter.convert(mediaModel.get(0));
						defaultImage1.setUrl(absolutePath + defaultImage1.getUrl());
						giftCardWsDTO.setDefaultImage(
								getDataMapper().map(defaultImage1, MediaWsDTO.class, WooliesgcWebServicesConstants.MEDIADATA));
					}
					giftCardWsDTO.setPID(personalisationEGiftCardModel.getPid());
					return giftCardWsDTO;
				}
			}
			if (!isPIDExist)
			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ISNOTPIDEXIST,
						WooliesgcWebServicesConstants.ERRMSG_ISNOTPIDEXIST, WooliesgcWebServicesConstants.ERRRSN_ISNOTPIDEXIST);
			}
		}
		return new eGiftCardWsDTO();
	}


	@RequestMapping(value =
	{ "/bulkUploadCartEntries/{referenceNumber}" }, method = RequestMethod.POST, consumes =
	{ MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseBody
	public CartWsDTO addBulkOrderCartEntries(@PathVariable final String baseSiteId, @PathVariable final String referenceNumber,
			@RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws CommerceCartModificationException, WebserviceValidationException, WooliesB2BUserException//NOSONAR
	{
		final int qnty = 1;
		final WWBulkOrderDataModel wwStatusOfBulkOrderData = wooliesBulkOrderService.getStatusOfBulkOrder(referenceNumber);
		if (null != wwStatusOfBulkOrderData)
		{
			if (wwStatusOfBulkOrderData.getBulkOrderStatus().equals(BulkOrderStatus.VALIDATE_SUCCESS))
			{
				final Collection<WWBulkOrderItemsDataModel> bulkOrderDataCollection = wooliesBulkOrderService
						.getBulkOrderSavedData(referenceNumber);

				if (null != bulkOrderDataCollection && !bulkOrderDataCollection.isEmpty())
				{

					wooliesDefaultCartFacade.addToCartforBulkOrder(wooliesBulkOrderService, bulkOrderDataCollection,
							wwStatusOfBulkOrderData, qnty);
				}
				else
				{
					final StringBuilder errMsg = new StringBuilder(WooliesgcWebServicesConstants.ERRMSG_ADDTOCART);
					errMsg.append(wwStatusOfBulkOrderData.getBulkOrderStatus());
					final StringBuilder errRsn = new StringBuilder(WooliesgcWebServicesConstants.ERRRSN_ADDTOCART);
					errRsn.append(wwStatusOfBulkOrderData.getBulkOrderStatus());
					throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ADDTOCART, errMsg.toString(),
							errRsn.toString());
				}
			}
			else

			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_REFERENCENUMNOTFOUND,
						WooliesgcWebServicesConstants.ERRMSG_REFERENCENUMNOTFOUND,
						WooliesgcWebServicesConstants.ERRRSN_REFERENCENUMNOTFOUND);
			}
		}
		return getDataMapper().map(getSessionCart(), CartWsDTO.class, fields);

	}

	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT" })
	@RequestMapping(value =
	{ "/{cartId}/minimalPackage/{isMinimalPackage}",
			"/minimalPackage/{isMinimalPackage}" }, method = RequestMethod.PATCH, consumes =
	{ MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseBody
	@ApiOperation(value = "Update MinimalPackage of cart", notes = "Updates the quantity of a single cart entry and details of the store where the cart entry will be picked.")
	@ApiBaseSiteIdUserIdAndCartIdParam
	@ApiResponse(code = 200, message = "minimal package")
	public ResponseEntity<Object> minimalPackage(@ApiParam(value = "Base site identifier.") @PathVariable final String baseSiteId,
			@ApiParam(value = "isMinimalPackage from Gigiya.", required = true) @PathVariable final Boolean isMinimalPackage,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "GETCART") final String fields,
			final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) throws CommerceCartModificationException


	{
		wooliesDefaultCartFacade.setMinimalPackaging(isMinimalPackage);
		return new ResponseEntity<Object>(getDataMapper().map(getSessionCart(), CartWsDTO.class, fields), HttpStatus.OK);


	}
}