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

import de.hybris.platform.b2b.model.B2BOrderThresholdPermissionModel;
import de.hybris.platform.b2b.model.B2BPermissionModel;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.OrderFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.order.data.OrderHistoriesData;
import de.hybris.platform.commercefacades.order.data.OrderStatusData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commercewebservicescommons.dto.order.CartWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.OrderHistoryListWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.OrderWsDTO;
import de.hybris.platform.commercewebservicescommons.strategies.CartLoaderStrategy;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.order.impl.DefaultCalculationService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.webservicescommons.cache.CacheControl;
import de.hybris.platform.webservicescommons.cache.CacheControlDirective;
import de.hybris.platform.webservicescommons.dto.error.ErrorListWsDTO;
import de.hybris.platform.webservicescommons.dto.error.ErrorWsDTO;
import de.hybris.platform.webservicescommons.errors.exceptions.WebserviceValidationException;
import de.hybris.wooliesegiftcard.core.enums.UserDataType;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BCustomerModel;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BUnitModel;
import de.hybris.wooliesegiftcard.exception.WooliesB2BUserException;
import de.hybris.wooliesegiftcard.exception.WoolliesOrderHistoryException;
import de.hybris.wooliesegiftcard.exceptions.WooliesFacadeLayerException;
import de.hybris.wooliesegiftcard.facade.WooliesMemCustomerFacade;
import de.hybris.wooliesegiftcard.facade.impl.WooliesPaymentFacadeImpl;
import de.hybris.wooliesegiftcard.facades.OrderHistoryFacade;
import de.hybris.wooliesegiftcard.facades.OrderPlacedFacade;
import de.hybris.wooliesegiftcard.facades.dto.BulkOrderRequestDTO;
import de.hybris.wooliesegiftcard.facades.dto.BulkOrderResponseDTO;
import de.hybris.wooliesegiftcard.facades.dto.FraudOrderStatusRequest;
import de.hybris.wooliesegiftcard.facades.dto.OrderDetailsRequestDTO;
import de.hybris.wooliesegiftcard.facades.dto.OrderHistoryListResponseDTO;
import de.hybris.wooliesegiftcard.facades.dto.PaymentAuthError;
import de.hybris.wooliesegiftcard.facades.dto.PaymentRequestDTO;
import de.hybris.wooliesegiftcard.facades.dto.PlaceOrderRequestDTO;
import de.hybris.wooliesegiftcard.facades.impl.DefaultWooliesCheckoutFacade;
import de.hybris.wooliesegiftcard.facades.impl.WooliesBulkOrderFacadeImpl;
import de.hybris.wooliesegiftcard.core.EncryptionUtils;
import de.hybris.wooliesegiftcard.facades.WooliesDefaultOrderFacade;
import de.hybris.wooliesegiftcard.facades.dto.DecryptOrderTokenDTO;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.woolies.webservices.constants.WooliesgcWebServicesConstants;
import com.woolies.webservices.rest.exceptions.NoCheckoutCartException;
import com.woolies.webservices.rest.exceptions.PaymentAuthorizationException;
import com.woolies.webservices.rest.exceptions.WooliesWebserviceException;
import com.woolies.webservices.rest.strategies.OrderCodeIdentificationStrategy;
import com.woolies.webservices.rest.swagger.ApiBaseSiteIdAndUserIdParam;
import com.woolies.webservices.rest.swagger.ApiBaseSiteIdParam;
import com.woolies.webservices.rest.user.data.OrderListData;
import com.woolies.webservices.rest.v2.helper.OrdersHelper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.Authorization;


/**
 * Web Service Controller for the ORDERS resource. Most methods check orders of the user. Methods require authentication
 * and are restricted to https channel.
 */


@Controller
@RequestMapping(value = "/{baseSiteId}")
@Api(tags = "Orders")
public class OrdersController extends BaseCommerceController
{
	private static final Logger LOG = Logger.getLogger(OrdersController.class);

	private static final String GET_BULKORDER = "SELECT {PK} FROM {WWBulkOrderData} where referencenumber ";
	@Autowired
	private FlexibleSearchService flexibleSearchService;

	@Resource(name = "orderFacade")
	private OrderFacade orderFacade;
	@Resource(name = "orderCodeIdentificationStrategy")
	private OrderCodeIdentificationStrategy orderCodeIdentificationStrategy;
	@Resource(name = "cartLoaderStrategy")
	private CartLoaderStrategy cartLoaderStrategy;
	@Resource(name = "ordersHelper")
	private OrdersHelper ordersHelper;

	@Resource(name = "modelService")
	private ModelService modelService;

	@Resource(name = "orderHistoryFacade")
	private OrderHistoryFacade orderHistoryFacade;
	OrderListData orderListData = null;

	@Resource(name = "orderPlacedFacade")
	private OrderPlacedFacade orderPlacedFacade;

	@Resource(name = "wooliesCheckoutFacade")
	private DefaultWooliesCheckoutFacade wooliesCheckoutFacade;
	@Resource(name = "cartFacade")
	private CartFacade cartFacade;

	@Resource(name = "wooliesPaymentFacade")
	private WooliesPaymentFacadeImpl wooliesPaymentFacade;


	@Resource(name = "userService")
	private UserService userService;

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;


	@Resource(name = "wooliesBulkOrderFacade")
	private WooliesBulkOrderFacadeImpl wooliesBulkOrderFacade;

	@Resource(name = "wooliesMemCustomerFacade")
	private WooliesMemCustomerFacade wooliesMemCustomerFacade;

	OrderStatusData orderStatusData = null;

	List<OrderStatusData> listOrderStatus;

	@Resource(name = "paymentDetailsCustomDTOValidator")
	private Validator paymentDetailsCustomDTOValidator;

	@Autowired
	private Converter<CartModel, CartData> cartConverter;

	@Autowired
	private DefaultCalculationService calculationService;
	@Autowired
	private WooliesDefaultOrderFacade wooliesDefaultOrderFacade;

	@Secured("ROLE_TRUSTED_CLIENT")
	@RequestMapping(value = "/orders/{code}", method = RequestMethod.GET)
	@CacheControl(directive = CacheControlDirective.PUBLIC, maxAge = 120)
	@Cacheable(value = "orderCache", key = "T(de.hybris.platform.commercewebservicescommons.cache.CommerceCacheKeyGenerator).generateKey(false,true,'getOrder',#code,#fields)")
	@ResponseBody
	@ApiOperation(value = "Get a order", notes = "Returns details of a specific order based on order GUID (Globally Unique Identifier) or order CODE. The response contains a detailed order information.", authorizations =
	{ @Authorization(value = "oauth2_client_credentials") })
	@ApiBaseSiteIdParam
	public OrderWsDTO getOrder(
			@ApiParam(value = "Order GUID (Globally Unique Identifier) or order CODE", required = true) @PathVariable final String code,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		OrderData orderData;
		if (orderCodeIdentificationStrategy.isID(code))
		{
			orderData = orderFacade.getOrderDetailsForGUID(code);
		}
		else
		{
			orderData = orderFacade.getOrderDetailsForCodeWithoutUser(code);
		}

		return getDataMapper().map(orderData, OrderWsDTO.class, fields);
	}





	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@CacheControl(directive = CacheControlDirective.PUBLIC, maxAge = 120)
	@RequestMapping(value = "/users/{userId}/orders", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get order history for user", notes = "Returns order history data for all orders placed by the specific user for the specific base store. Response contains orders search result displayed in several pages if needed.")
	@ApiBaseSiteIdAndUserIdParam
	public OrderHistoryListWsDTO getOrdersForUser(
			@ApiParam(value = "Filters only certain order statuses. It means: statuses=CANCELLED,CHECKED_VALID would only return orders with status CANCELLED or CHECKED_VALID.") @RequestParam(required = false) final String statuses,
			@ApiParam(value = "The current result page requested.") @RequestParam(required = false, defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
			@ApiParam(value = "The number of results returned per page.") @RequestParam(required = false, defaultValue = DEFAULT_PAGE_SIZE) final int pageSize,
			@ApiParam(value = "Sorting method applied to the return results.") @RequestParam(required = false) final String sort,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
			final HttpServletResponse response)
	{
		validateStatusesEnumValue(statuses);

		final OrderHistoryListWsDTO orderHistoryList = ordersHelper.searchOrderHistory(statuses, currentPage, pageSize, sort,
				addPaginationField(fields));

		// X-Total-Count header
		if (orderHistoryList != null)
		{
			setTotalCountHeader(response, orderHistoryList.getPagination());
		}
		return orderHistoryList;
	}

	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/users/{userId}/order/{code}", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdParam
	public ResponseEntity<Object> getOrderForUserByCode(
			@ApiParam(value = "Order GUID (Globally Unique Identifier) or order CODE", required = true) @PathVariable final String code,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws UnknownIdentifierException, ConversionException, ModelNotFoundException
	{
		OrderData orderData = null;
		final ErrorWsDTO errorDetails = new ErrorWsDTO();
		final ErrorListWsDTO errorListDto = new ErrorListWsDTO();
		try
		{
			if (null != code)
			{
				orderData = orderFacade.getOrderDetailsForCode(code);
			}

			else
			{
				orderData = orderFacade.getOrderDetailsForCodeWithoutUser(code);
			}
		}
		catch (final ModelNotFoundException ue)
		{
			errorDetails.setErrorCode(WooliesgcWebServicesConstants.ERRCODE_ORDERCONFIRMATION_ORDER_NOT_FOUND);
			errorDetails.setErrorDescription(WooliesgcWebServicesConstants.ERRMSG_ORDERCONFIRMATION_ORDER_NOT_FOUND);
			errorDetails.setErrorMessage(WooliesgcWebServicesConstants.ERRMSG_ORDERCONFIRMATION_ORDER_NOT_FOUND);
			final List<ErrorWsDTO> errorDetailList = new ArrayList<>();
			errorDetailList.add(errorDetails);
			errorListDto.setErrors(errorDetailList);
			return new ResponseEntity<Object>(errorListDto, HttpStatus.INTERNAL_SERVER_ERROR);


		}
		catch (final UnknownIdentifierException ue)
		{
			errorDetails.setErrorCode(WooliesgcWebServicesConstants.ERRCODE_ORDERCONFIRMATION_ORDER_NOT_FOUND);
			errorDetails.setErrorDescription(ue.getMessage());
			errorDetails.setErrorMessage(ue.getMessage());
			final List<ErrorWsDTO> errorDetailList = new ArrayList<>();
			errorDetailList.add(errorDetails);
			errorListDto.setErrors(errorDetailList);

			return new ResponseEntity<Object>(errorListDto, HttpStatus.INTERNAL_SERVER_ERROR);

		}
		return new ResponseEntity<Object>(getDataMapper().map(orderData, OrderWsDTO.class, fields), HttpStatus.OK);
	}

	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/users/{userId}/orders", method = RequestMethod.HEAD)
	@ResponseBody
	@ApiOperation(value = "Get total number of orders", notes = "Returns X-Total-Count header with a total number of results (orders history for all orders placed by the specific user for the specific base store).")
	@ApiBaseSiteIdAndUserIdParam
	public void getCountOrdersForUser(
			@ApiParam(value = "Filters only certain order statuses. It means: statuses=CANCELLED,CHECKED_VALID would only return orders with status CANCELLED or CHECKED_VALID.") @RequestParam(required = false) final String statuses,
			final HttpServletResponse response)
	{
		final OrderHistoriesData orderHistoriesData = ordersHelper.searchOrderHistory(statuses, 0, 1, null);

		if (orderHistoriesData != null)
		{
			setTotalCountHeader(response, orderHistoriesData.getPagination());
		}
	}


	@RequestMapping(value = "/users/{userId}/orders/placeOrder", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	@ApiOperation(value = "Post a order", notes = "Authorizes cart and places the order. Response contains the new order data.")
	@ApiBaseSiteIdAndUserIdParam
	@SuppressWarnings("squid:S1160")
	public ResponseEntity<Object> placeOrder(
			@ApiParam(value = "User's object.", required = true) @RequestBody final PlaceOrderRequestDTO placeOrderRequestDTO,
			@ApiParam(value = "User id", required = true) @PathVariable final String userId, //NOSONAR
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws PaymentAuthorizationException, InvalidCartException, WebserviceValidationException, NoCheckoutCartException //NOSONAR
			, CalculationException, KeyManagementException, NoSuchAlgorithmException, WooliesFacadeLayerException, ParseException
	{

		if (LOG.isDebugEnabled())
		{
			LOG.info("placeOrder");
		}
		CartModel cartModel = new CartModel();
		if (placeOrderRequestDTO.getGuid() == null)
		{
			if (orderPlacedFacade.getCart(userId) == null)
			{
				throw new WooliesWebserviceException("101", "Cart is not there for current user",
						"Cart is not there for current user");
			}
			cartModel = orderPlacedFacade.getCart(userId);
			cartLoaderStrategy.loadCart(cartModel.getCode());
		}
		else
		{
			if (orderPlacedFacade.getCartForAnonymous(placeOrderRequestDTO.getGuid()) == null)
			{
				throw new WooliesWebserviceException("101", "Cart is not there for current user",
						"Cart is not there for current user");
			}
			try
			{
				cartModel = wooliesCheckoutFacade.createAnonymousUser(placeOrderRequestDTO);
			}
			catch (final IllegalArgumentException ex)
			{
				if (ex.getMessage().equals("103"))
				{
					throw new WooliesWebserviceException("103",
							"First Name, Last Name, Email ID, Phone Number are mandatory for guest user creation",
							"Phone number is mandatory for guest user creation");
				}
				if (ex.getMessage().equals("104"))
				{
					throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_USER_ALREADY_AVAILABLE,
							WooliesgcWebServicesConstants.ERRMSG_USER_ALREADY_AVAILABLE,
							WooliesgcWebServicesConstants.ERRRSN_USER_ALREADY_AVAILABLE);
				}
			}

			cartLoaderStrategy.loadCart(placeOrderRequestDTO.getGuid());
		}
		if (placeOrderRequestDTO.getBillingAddress() == null)
		{
			throw new WooliesWebserviceException("100", "Billing address is missing", "Billing address is missing");
		}
		else if (placeOrderRequestDTO.getShippingAddress() == null)
		{
			final List<AbstractOrderEntryModel> entries = cartModel.getEntries();

			for (final AbstractOrderEntryModel abstractOrderEntryModel : entries)
			{
				if (!abstractOrderEntryModel.getProduct().getIsEGiftCard().booleanValue())
				{
					throw new WooliesWebserviceException("100", "Shipping address is missing", "Shipping address is missing");

				}
			}
		}
		OrderData orderData = new OrderData();
		final PaymentRequestDTO paymentInfo = placeOrderRequestDTO.getPayment();
		if (null != paymentInfo)
		{
			final UserModel user = userService.getUserForUID(userId);
			validatePaymentOptions(paymentInfo.getPaymentOption());
			if (paymentInfo.getPaymentOption().equalsIgnoreCase(WooliesgcWebServicesConstants.PAYMENT_OPTIONS.PAY_1001.name()))
			{
				validatePayment(paymentInfo, "paymentInfo", paymentDetailsCustomDTOValidator);
				final List<PaymentAuthError> authErrors = new ArrayList<>();
				boolean isAuth = false;
				try
				{
					wooliesPaymentFacade.savePaymentInfo(placeOrderRequestDTO,
							getDataMapper().map(placeOrderRequestDTO.getBillingAddress(), AddressData.class, fields), cartModel);
					cartConverter.convert(cartModel);
					validateCartForPlaceOrder();
					isAuth = wooliesPaymentFacade.doAuthorize(placeOrderRequestDTO, cartModel, authErrors, new Date());
				}
				catch (final WooliesFacadeLayerException e)
				{

					throw new WooliesWebserviceException(e.getErrorCode(), e.getMessage(), e.getErrorReason());

				}
				if (isAuth)
				{
					orderPlacedFacade.saveShippingAddress(
							getDataMapper().map(placeOrderRequestDTO.getShippingAddress(), AddressData.class, fields), cartModel);
					calculationService.calculateTotals(cartModel, true);
					orderData = getCheckoutFacade().placeOrder();
					final AddressData billingAddressData = getDataMapper().map(placeOrderRequestDTO.getBillingAddress(),
							AddressData.class);
					final AddressData shippingAddressData = getDataMapper().map(placeOrderRequestDTO.getShippingAddress(),
							AddressData.class);

					if (((CustomerModel) user).getCustomerType() == UserDataType.MEM)
					{
						wooliesMemCustomerFacade.saveMemberProfile(placeOrderRequestDTO, billingAddressData, shippingAddressData,
								userId);
					}
				}
				else
				{
					if (null != authErrors && authErrors.size() > 0)
					{
						for (final PaymentAuthError paymentAuthError : authErrors)
						{
							if (null != paymentAuthError.getCreditCards() && paymentAuthError.getCreditCards().size() > 0)
							{
								return new ResponseEntity(paymentAuthError.getCreditCards(),
										HttpStatus.valueOf(paymentAuthError.getHttpStatusCode()));
							}
							else if (null != paymentAuthError.getUnknown() && paymentAuthError.getUnknown().size() > 0)
							{
								return new ResponseEntity(paymentAuthError.getUnknown(),
										HttpStatus.valueOf(paymentAuthError.getHttpStatusCode()));
							}
							else
							{
								return new ResponseEntity(paymentAuthError, HttpStatus.valueOf(paymentAuthError.getHttpStatusCode()));
							}
						}
					}
				}
			}
			else if (paymentInfo.getPaymentOption().equalsIgnoreCase(WooliesgcWebServicesConstants.PAYMENT_OPTIONS.PAY_1002.name())
					|| paymentInfo.getPaymentOption().equalsIgnoreCase(WooliesgcWebServicesConstants.PAYMENT_OPTIONS.PAY_1003.name()))
			{
				if (((CustomerModel) user).getCustomerType() == UserDataType.B2B)
				{
					try
					{
						wooliesPaymentFacade.savePaymentInfo(placeOrderRequestDTO,
								getDataMapper().map(placeOrderRequestDTO.getBillingAddress(), AddressData.class, fields), cartModel);
					}
					catch (final WooliesFacadeLayerException e)
					{
						throw new WooliesWebserviceException(e.getErrorCode(), e.getMessage(), e.getErrorReason());
					}
					validateCartForPlaceOrder();
					orderPlacedFacade.saveShippingAddress(
							getDataMapper().map(placeOrderRequestDTO.getShippingAddress(), AddressData.class, fields), cartModel);
					calculationService.calculateTotals(cartModel, true);
					orderData = getCheckoutFacade().placeOrder();
					final AddressData billingAddressData = getDataMapper().map(placeOrderRequestDTO.getBillingAddress(),
							AddressData.class);
					final AddressData shippingAddressData = getDataMapper().map(placeOrderRequestDTO.getShippingAddress(),
							AddressData.class);
				}
				else
				{
					throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_PAYMENT_NOT_ALLOWED,
							WooliesgcWebServicesConstants.ERRMSG_PAYMENT_NOT_ALLOWED,
							WooliesgcWebServicesConstants.ERRRSN_PAYMENT_NOT_ALLOWED);
				}
			}
		}
		return new ResponseEntity(getDataMapper().map(orderData, OrderWsDTO.class, fields), HttpStatus.OK);
	}


	@RequestMapping(value = "/orders/bulkUpload", method = RequestMethod.POST, consumes =
	{ MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@SuppressWarnings("squid:S1160")
	@ApiOperation(value = " Bulk Upload", notes = "Bulk Upload Orders")
	@ApiBaseSiteIdParam
	public ResponseEntity<Object> bulkUpload(
			@ApiParam(value = "User's object.", required = true) @RequestBody final BulkOrderRequestDTO bulkData)
			throws DuplicateUidException, UnknownIdentifierException, //NOSONAR
			IllegalArgumentException, WebserviceValidationException, UnsupportedEncodingException //NOSONAR
			, WooliesB2BUserException
	{

		LOG.info("Entered into bulkorder, starting stopwatch");
		final StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		LOG.debug("Stopwatch time: " + stopWatch);
		final BulkOrderResponseDTO bulkOrderResponseDTO = wooliesBulkOrderFacade.saveBulkOrder(bulkData);

		stopWatch.stop();
		LOG.debug("Stopwatch time after saving data: " + stopWatch);
		if (null != bulkOrderResponseDTO.getErrors() && bulkOrderResponseDTO.getErrors().size() > 0)
		{
			return new ResponseEntity(bulkOrderResponseDTO, HttpStatus.BAD_REQUEST);
		}
		else
		{
			return new ResponseEntity(bulkOrderResponseDTO, HttpStatus.CREATED);
		}
	}

	// create new validation api
	@RequestMapping(value = "/orders/bulkOrderValidation/refNumber/{refNumber}", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	@ResponseBody
	@ApiOperation(value = " Bulk order validation", notes = "Bulk order validation")
	@ApiBaseSiteIdParam
	public ResponseEntity<Object> bulkOrderValidation(
			@ApiParam(value = "referenceNumber.", required = true) @PathVariable final String refNumber)
			throws DuplicateUidException, UnknownIdentifierException, //NOSONAR
			IllegalArgumentException, WebserviceValidationException, UnsupportedEncodingException //NOSONAR
			, WooliesB2BUserException
	{
		LOG.info("Entered into bulkorder, starting stopwatch");
		final StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		BulkOrderResponseDTO bulkOrderResponseDTO;
		try
		{
			bulkOrderResponseDTO = wooliesBulkOrderFacade.validateBulkOrder(refNumber);
			LOG.debug("Stopwatch time for validation: " + stopWatch);
			stopWatch.stop();
			LOG.debug("Stopwatch time after validation data: " + stopWatch);
			if (null != bulkOrderResponseDTO.getErrors() && bulkOrderResponseDTO.getErrors().size() > 0)
			{
				return new ResponseEntity(bulkOrderResponseDTO, HttpStatus.BAD_REQUEST);
			}
			else
			{
				return new ResponseEntity(bulkOrderResponseDTO, HttpStatus.OK);
			}
		}
		catch (final WooliesFacadeLayerException e)
		{
			final ErrorWsDTO errorDetails = new ErrorWsDTO();
			final ErrorListWsDTO errorListDto = new ErrorListWsDTO();
			errorDetails.setErrorCode("ERR_90001");
			errorDetails.setErrorDescription("No Order available in Hybris");
			errorDetails.setErrorMessage("Orders Not found");
			final List<ErrorWsDTO> errorDetailList = new ArrayList<>();
			errorDetailList.add(errorDetails);
			errorListDto.setErrors(errorDetailList);
			return new ResponseEntity<Object>(errorListDto, HttpStatus.INTERNAL_SERVER_ERROR);
		}


	}


	@RequestMapping(value = "/orders/orderHistory", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "Get order history for user", notes = "Returns order history data for all orders placed by the specific user for the specific base store. Response contains orders search result displayed in several pages if needed.")
	@ApiBaseSiteIdAndUserIdParam
	@ApiResponse(code = 200, message = "Order History Details")
	public ResponseEntity<Object> getOrdersHistoryForUser(
			@ApiParam(value = "User's object.", required = true) @RequestBody final OrderDetailsRequestDTO orderDetailsRequestDTO,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
			final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) throws WoolliesOrderHistoryException
	{
		final String code = orderDetailsRequestDTO.getOrderNumber();
		final String userId = orderDetailsRequestDTO.getUid();
		final List<String> orderStatus = orderDetailsRequestDTO.getOrderStatus();
		final String toDate = orderDetailsRequestDTO.getToDate();
		final String fromDate = orderDetailsRequestDTO.getFromDate();
		final int startIndex = orderDetailsRequestDTO.getStartIndex();
		final int pageSize = orderDetailsRequestDTO.getPageSize();

		if (StringUtils.isNotEmpty(userId))
		{
			if (StringUtils.isNotEmpty(code))
			{
				final List<OrderData> resultList = orderHistoryFacade.getOrderDetailsForOrderNo(code, userId);
				if (CollectionUtils.isNotEmpty(resultList))
				{
					orderListData = new OrderListData();
					orderListData.setOrders(resultList);
					return new ResponseEntity<Object>(getDataMapper().map(orderListData, OrderHistoryListResponseDTO.class, fields),
							HttpStatus.OK);
				}
				else
				{
					throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ORDER_HISTORY,
							WooliesgcWebServicesConstants.ERRRSN_ORDER_HISTORY_MSG,
							WooliesgcWebServicesConstants.ERRMSG_ORDER_HISTORY_DESC);
				}
			}
			else
			{
				if (StringUtils.isNotEmpty(fromDate) && StringUtils.isNotEmpty(toDate))
				{
					if (CollectionUtils.isEmpty(orderStatus))
					{
						orderListData = orderHistoryFacade.getOrderHistoryForRange(userId, fromDate, toDate, startIndex, pageSize);
						return new ResponseEntity<Object>(getDataMapper().map(orderListData, OrderHistoryListResponseDTO.class, fields),
								HttpStatus.OK);
					}
					else if (CollectionUtils.isNotEmpty(orderStatus))
					{
						orderListData = orderHistoryFacade.getOrderHistoryForRangeAndStatus(userId, fromDate, toDate, startIndex,
								pageSize, orderStatus);
						return new ResponseEntity<Object>(getDataMapper().map(orderListData, OrderHistoryListResponseDTO.class, fields),
								HttpStatus.OK);
					}
				}
				else if (StringUtils.isEmpty(fromDate) && StringUtils.isEmpty(toDate))
				{
					if (CollectionUtils.isNotEmpty(orderStatus))
					{
						orderListData = orderHistoryFacade.getOrderHistoryForStatus(userId, startIndex, pageSize, orderStatus);
						return new ResponseEntity<Object>(getDataMapper().map(orderListData, OrderHistoryListResponseDTO.class, fields),
								HttpStatus.OK);
					}
					else if (CollectionUtils.isEmpty(orderStatus))
					{
						orderListData = orderHistoryFacade.getAllOrderHistory(userId, startIndex, pageSize);
						return new ResponseEntity<Object>(getDataMapper().map(orderListData, OrderHistoryListResponseDTO.class, fields),
								HttpStatus.OK);
					}
				}
				else
				{
					throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ORDER_HISTORY,
							WooliesgcWebServicesConstants.ERRRSN_ORDER_HISTORY_MSG,
							WooliesgcWebServicesConstants.ERRMSG_ORDER_HISTORY_DESC);
				}
			}
		}
		else
		{
			throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ORDER_HISTORY,
					WooliesgcWebServicesConstants.ERRRSN_ORDER_HISTORY_MSG, WooliesgcWebServicesConstants.ERRMSG_ORDER_HISTORY_DESC);
		}

		return null;

	}

	@Secured("ROLE_TRUSTED_CLIENT")
	@RequestMapping(value = "/orders/{code}/orderConfirmation", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdParam
	public ResponseEntity<Object> getOrderForDeliveryDateEstimation(
			@ApiParam(value = "Order GUID (Globally Unique Identifier) or order CODE", required = true) @PathVariable final String code,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL,ORDERSUBMISSION") @RequestParam(defaultValue = "ORDERSUBMISSION") final String fields)
			throws UnknownIdentifierException, ConversionException, ModelNotFoundException

	{
		OrderData orderData = null;
		final ErrorWsDTO errorDetails = new ErrorWsDTO();
		final ErrorListWsDTO errorListDto = new ErrorListWsDTO();
		try
		{
			if (orderCodeIdentificationStrategy.isID(code))
			{
				orderData = orderFacade.getOrderDetailsForGUID(code);
			}
			else
			{
				orderData = orderFacade.getOrderDetailsForCodeWithoutUser(code);
			}
		}
		catch (final ModelNotFoundException ue)
		{
			errorDetails.setErrorCode(WooliesgcWebServicesConstants.ERRCODE_ORDERCONFIRMATION_ORDER_NOT_FOUND);
			errorDetails.setErrorDescription(WooliesgcWebServicesConstants.ERRMSG_ORDERCONFIRMATION_ORDER_NOT_FOUND);
			errorDetails.setErrorMessage(WooliesgcWebServicesConstants.ERRMSG_ORDERCONFIRMATION_ORDER_NOT_FOUND);
			final List<ErrorWsDTO> errorDetailList = new ArrayList<>();
			errorDetailList.add(errorDetails);
			errorListDto.setErrors(errorDetailList);
			return new ResponseEntity<Object>(errorListDto, HttpStatus.INTERNAL_SERVER_ERROR);

		}
		catch (final UnknownIdentifierException ue)
		{
			errorDetails.setErrorCode(WooliesgcWebServicesConstants.ERRCODE_ORDERCONFIRMATION_ORDER_NOT_FOUND);
			errorDetails.setErrorDescription(ue.getMessage());
			errorDetails.setErrorMessage(ue.getMessage());
			final List<ErrorWsDTO> errorDetailList = new ArrayList<>();
			errorDetailList.add(errorDetails);
			errorListDto.setErrors(errorDetailList);
			return new ResponseEntity<Object>(errorListDto, HttpStatus.INTERNAL_SERVER_ERROR);

		}
		return new ResponseEntity<Object>(getDataMapper().map(orderData, OrderWsDTO.class, fields), HttpStatus.OK);
	}

	@RequestMapping(value = "/users/{userId}/reorder", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "Get a order", notes = "Returns details of a specific order based on order GUID (Globally Unique Identifier) or order CODE. The response contains a detailed order information.", authorizations =
	{ @Authorization(value = "oauth2_client_credentials") })
	@ApiBaseSiteIdParam
	public CartWsDTO reOrder(@RequestParam(value = "orderCode") final String code) throws CommerceCartModificationException
	{
		try
		{
			wooliesCheckoutFacade.createCartFromOrder(code);
		}
		catch (final IllegalArgumentException ex)
		{
			if (ex.getMessage().equals("40012"))
			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_REORDER,
						WooliesgcWebServicesConstants.ERRMSG_REORDER, WooliesgcWebServicesConstants.ERRRSN_REORDER);
			}
			else if (ex.getMessage().equals(WooliesgcWebServicesConstants.ERRCODE_ORDERLIMITFORBUYER))
			{
				final UserModel currentUser = userService.getCurrentUser();
				final CorporateB2BCustomerModel corporateb2bCustomer = (CorporateB2BCustomerModel) currentUser;
				Double thresholdValue = null;

				final CorporateB2BUnitModel corporateb2BUnit = (CorporateB2BUnitModel) corporateb2bCustomer.getDefaultB2BUnit();
				final Set<B2BPermissionModel> b2bPermissionModel = corporateb2BUnit.getPermissions();
				if (!b2bPermissionModel.isEmpty())
				{
					for (final B2BPermissionModel b2bPermission : b2bPermissionModel)
					{
						if (b2bPermission.getCode().equalsIgnoreCase(corporateb2bCustomer.getUid()))
						{
							final B2BOrderThresholdPermissionModel b2bOrderPermissionModel = (B2BOrderThresholdPermissionModel) b2bPermission;
							thresholdValue = b2bOrderPermissionModel.getThreshold();
						}
					}
				}
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ORDERLIMITFORBUYER,
						WooliesgcWebServicesConstants.ERRMSG_ORDERLIMITFORBUYER + thresholdValue,
						WooliesgcWebServicesConstants.ERRRSN_ORDERLIMITFORBUYER + thresholdValue);
			}
			else if (ex.getMessage().equals(WooliesgcWebServicesConstants.ERRCODE_CARTLIMIT))
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
		}
		// validate for stock and availability
		cartFacade.validateCartData();
		final CartWsDTO cartwsdto = new CartWsDTO();
		getDataMapper().map(getSessionCart(), cartwsdto, "cartGiftCardCount, totalPrice, cartItemCount", true);
		return cartwsdto;
	}

	private void validatePayment(final Object object, final String objectName, final Validator validator)
	{
		final Errors errors = new BeanPropertyBindingResult(object, objectName);
		validator.validate(object, errors);
		if (errors.hasErrors())
		{
			final List<FieldError> fieldErrors = errors.getFieldErrors();
			for (final FieldError fieldError : fieldErrors)
			{
				if (fieldError.getField().equalsIgnoreCase(WooliesgcWebServicesConstants.PAYMENT_INSTRUMENT_ID))
				{
					throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERR_CODE_PAYMENT_INSTRUMENT_ID,
							fieldError.getField() + WooliesgcWebServicesConstants.ERRMSG_FIELDMISSING,
							fieldError.getField() + WooliesgcWebServicesConstants.ERRRSN_FIELDMISSING);
				}
				else if (fieldError.getField().equalsIgnoreCase(WooliesgcWebServicesConstants.ACCESS_TOKEN))
				{
					throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERR_CODE_ACCESS_TOKEN,
							fieldError.getField() + WooliesgcWebServicesConstants.ERRMSG_FIELDMISSING,
							fieldError.getField() + WooliesgcWebServicesConstants.ERRRSN_FIELDMISSING);
				}
			}

		}
	}

	private void validatePaymentOptions(final String paymentOptions)
	{
		final WooliesgcWebServicesConstants.PAYMENT_OPTIONS PAY_1001 = WooliesgcWebServicesConstants.PAYMENT_OPTIONS.PAY_1001;
		final WooliesgcWebServicesConstants.PAYMENT_OPTIONS PAY_1002 = WooliesgcWebServicesConstants.PAYMENT_OPTIONS.PAY_1002;
		final WooliesgcWebServicesConstants.PAYMENT_OPTIONS PAY_1003 = WooliesgcWebServicesConstants.PAYMENT_OPTIONS.PAY_1003;

		if ((!paymentOptions.equalsIgnoreCase(PAY_1001.name())) && (!paymentOptions.equalsIgnoreCase(PAY_1002.name()))
				&& (!paymentOptions.equalsIgnoreCase(PAY_1003.name())))
		{
			throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERR_CODE_PAYMENT_OPTIONS,
					WooliesgcWebServicesConstants.ERRMSG_PAYMENT_OPTIONS, WooliesgcWebServicesConstants.ERRRSN_PAYMENT_OPTIONS
							+ PAY_1001.name() + " ," + PAY_1002.name() + " ," + PAY_1003.name());
		}

	}



	@RequestMapping(value = "/orders/updateOrderStatus", method = RequestMethod.PATCH)
	@ResponseStatus(value = HttpStatus.OK)
	@ResponseBody
	@ApiOperation(value = "Update order Status", notes = "Update order Status")
	@ApiBaseSiteIdParam
	public void updateOrderStatus(
			@ApiParam(value = "Update Order Request.", required = true) @RequestBody final FraudOrderStatusRequest fraudRequest)
	{
		LOG.info("Entered into updateOrderStatus API");
		try
		{
			orderPlacedFacade.setOrderStatus(fraudRequest);
		}
		catch (final WooliesFacadeLayerException e)
		{
			throw new WooliesWebserviceException(e.getErrorCode(), e.getMessage(), e.getErrorReason());
		}

	}
	
		@RequestMapping(value = "orders/decryptToken/orderDetails", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@ApiOperation(value = "decryptOrderDetails", notes = "decryptOrderDetails")
	@ApiBaseSiteIdAndUserIdParam
	public ResponseEntity<Object> decryptOrderDetails(
			@ApiParam(value = "User's object.", required = true) @RequestBody final DecryptOrderTokenDTO decryptOrderTokenDTO,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws UnsupportedEncodingException
	{
		OrderData orderData = null;
		final String encryptedKey = configurationService.getConfiguration().getString("encryption.key");
		final String characterEncoding = "UTF-8";
		final byte[] b2 = encryptedKey.getBytes(characterEncoding);
		final String decyptStr = EncryptionUtils.decrypt(decryptOrderTokenDTO.getEncryptedOrderToken(), b2);
		try
		{
			orderData = wooliesDefaultOrderFacade.getOrderDetailsWithDecryptKey(decyptStr);
		}
		catch (final IllegalArgumentException e)
		{
			throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_INVALID_ORDER_TOKEN,
					WooliesgcWebServicesConstants.ERRMSG_INVALID_ORDER_TOKEN,
					WooliesgcWebServicesConstants.ERRRSN_INVALID_ORDER_TOKEN);
		}
		return new ResponseEntity<Object>(getDataMapper().map(orderData, OrderWsDTO.class, fields), HttpStatus.OK);
	}
}
