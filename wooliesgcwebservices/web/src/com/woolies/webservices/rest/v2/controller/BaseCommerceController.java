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

import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.CheckoutFacade;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.CartModificationData;
import de.hybris.platform.commercefacades.order.data.CartModificationDataList;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.voucher.VoucherFacade;
import de.hybris.platform.commercefacades.voucher.exceptions.VoucherOperationException;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.CartAddressException;
import de.hybris.platform.converters.ConfigurablePopulator;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.webservicescommons.errors.exceptions.WebserviceValidationException;
import de.hybris.platform.webservicescommons.validators.EnumValueValidator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.woolies.webservices.rest.constants.YcommercewebservicesConstants;
import com.woolies.webservices.rest.exceptions.InvalidPaymentInfoException;
import com.woolies.webservices.rest.exceptions.NoCheckoutCartException;
import com.woolies.webservices.rest.exceptions.UnsupportedDeliveryModeException;
import com.woolies.webservices.rest.populator.options.PaymentInfoOption;
import com.woolies.webservices.rest.validator.PlaceOrderCartValidator;


public class BaseCommerceController extends BaseController
{
	private static final Logger LOG = Logger.getLogger(BaseCommerceController.class);
	//TODO change commerceWebServicesCartFacade2 to commerceWebServicesCartFacade after removing it in commercefacades
	@Resource(name = "commerceWebServicesCartFacade2")
	private CartFacade cartFacade;
	@Resource(name = "checkoutFacade")
	private CheckoutFacade checkoutFacade;
	@Resource(name = "voucherFacade")
	private VoucherFacade voucherFacade;
	@Resource(name = "deliveryAddressValidator")
	private Validator deliveryAddressValidator;
	@Resource(name = "httpRequestAddressDataPopulator")
	private Populator<HttpServletRequest, AddressData> httpRequestAddressDataPopulator;
	@Resource(name = "addressValidator")
	private Validator addressValidator;
	@Resource(name = "addressDTOValidator")
	private Validator addressDTOValidator;
	@Resource(name = "userFacade")
	private UserFacade userFacade;
	@Resource(name = "ccPaymentInfoValidator")
	private Validator ccPaymentInfoValidator;
	@Resource(name = "paymentDetailsDTOValidator")
	private Validator paymentDetailsDTOValidator;
	@Resource(name = "httpRequestPaymentInfoPopulator")
	private ConfigurablePopulator<HttpServletRequest, CCPaymentInfoData, PaymentInfoOption> httpRequestPaymentInfoPopulator;
	@Resource(name = "placeOrderCartValidator")
	private PlaceOrderCartValidator placeOrderCartValidator;
	@Resource(name = "orderStatusValueValidator")
	private EnumValueValidator orderStatusValueValidator;

	/**
	 * This method is used to create internal address from the request object
	 *
	 * @param request
	 * @return
	 * @throws WebserviceValidationException
	 */
	protected AddressData createAddressInternal(final HttpServletRequest request) throws WebserviceValidationException //NOSONAR
	{
		final AddressData addressData = new AddressData();
		httpRequestAddressDataPopulator.populate(request, addressData);

		validate(addressData, "addressData", addressValidator);

		return createAddressInternal(addressData);
	}

	/**
	 * This method is used to create internal address from the addressData object
	 *
	 * @param addressData
	 * @return
	 */
	protected AddressData createAddressInternal(final AddressData addressData)
	{
		addressData.setShippingAddress(true);
		addressData.setVisibleInAddressBook(true);
		userFacade.addAddress(addressData);
		if (addressData.isDefaultAddress())
		{
			userFacade.setDefaultAddress(addressData);
		}
		return addressData;
	}

	/**
	 * This method is used to set cart delivery internal address from the addressId object
	 *
	 * @param addressId
	 * @return
	 * @throws NoCheckoutCartException
	 */
	protected CartData setCartDeliveryAddressInternal(final String addressId) throws NoCheckoutCartException
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("setCartDeliveryAddressInternal: " + logParam("addressId", addressId));
		}
		final AddressData address = new AddressData();
		address.setId(addressId);
		final Errors errors = new BeanPropertyBindingResult(address, "addressData");
		deliveryAddressValidator.validate(address, errors);
		if (errors.hasErrors())
		{
			throw new CartAddressException("Address given by id " + sanitize(addressId) + " is not valid",
					CartAddressException.NOT_VALID, addressId);
		}
		if (checkoutFacade.setDeliveryAddress(address))
		{
			return getSessionCart();
		}
		throw new CartAddressException(
				"Address given by id " + sanitize(addressId) + " cannot be set as delivery address in this cart",
				CartAddressException.CANNOT_SET, addressId);
	}

	/**
	 * This method is used to set deliver mode for the given delivery mode id
	 *
	 * @param deliveryModeId
	 * @return the session cart
	 * @throws UnsupportedDeliveryModeException
	 */
	protected CartData setCartDeliveryModeInternal(final String deliveryModeId) throws UnsupportedDeliveryModeException
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("setCartDeliveryModeInternal: " + logParam("deliveryModeId", deliveryModeId));
		}
		if (checkoutFacade.setDeliveryMode(deliveryModeId))
		{
			return getSessionCart();
		}
		throw new UnsupportedDeliveryModeException(deliveryModeId);
	}

	/**
	 * This method is used to apply voucher for cart data
	 *
	 * @param voucherId
	 * @return
	 * @throws NoCheckoutCartException
	 * @throws VoucherOperationException
	 */
	protected CartData applyVoucherForCartInternal(final String voucherId)
			throws NoCheckoutCartException, VoucherOperationException
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("apply voucher: " + logParam("voucherId", voucherId));
		}
		if (!checkoutFacade.hasCheckoutCart())
		{
			throw new NoCheckoutCartException("Cannot apply voucher. There was no checkout cart created yet!");
		}

		voucherFacade.applyVoucher(voucherId);
		return getSessionCart();
	}

	/**
	 * This method is used to add payment details for the given request
	 *
	 * @param request
	 * @return payment details
	 * @throws WebserviceValidationException
	 * @throws InvalidPaymentInfoException
	 * @throws NoCheckoutCartException
	 */
	protected CartData addPaymentDetailsInternal(final HttpServletRequest request)
			throws WebserviceValidationException, InvalidPaymentInfoException, NoCheckoutCartException //NOSONAR
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("addPaymentInfo");
		}
		if (!checkoutFacade.hasCheckoutCart())
		{
			throw new NoCheckoutCartException("Cannot add PaymentInfo. There was no checkout cart created yet!");
		}

		final CCPaymentInfoData paymentInfoData = new CCPaymentInfoData();
		final Errors errors = new BeanPropertyBindingResult(paymentInfoData, "paymentInfoData");

		final Collection<PaymentInfoOption> options = new ArrayList<PaymentInfoOption>();
		options.add(PaymentInfoOption.BASIC);
		options.add(PaymentInfoOption.BILLING_ADDRESS);

		httpRequestPaymentInfoPopulator.populate(request, paymentInfoData, options);
		ccPaymentInfoValidator.validate(paymentInfoData, errors);

		if (errors.hasErrors())
		{
			throw new WebserviceValidationException(errors);
		}

		return addPaymentDetailsInternal(paymentInfoData);
	}

	/**
	 * This method is used to add payment details for the given payment information data
	 *
	 * @param paymentInfoData
	 * @return
	 * @throws InvalidPaymentInfoException
	 */
	protected CartData addPaymentDetailsInternal(final CCPaymentInfoData paymentInfoData) throws InvalidPaymentInfoException
	{
		final boolean emptySavedPaymentInfos = userFacade.getCCPaymentInfos(true).isEmpty();
		final CCPaymentInfoData createdPaymentInfoData = checkoutFacade.createPaymentSubscription(paymentInfoData);

		if (createdPaymentInfoData == null)
		{
			throw new InvalidPaymentInfoException("null");
		}

		if (createdPaymentInfoData.isSaved() && (paymentInfoData.isDefaultPaymentInfo() || emptySavedPaymentInfos))
		{
			userFacade.setDefaultPaymentInfo(createdPaymentInfoData);
		}

		if (checkoutFacade.setPaymentDetails(createdPaymentInfoData.getId()))
		{
			return getSessionCart();
		}
		throw new InvalidPaymentInfoException(createdPaymentInfoData.getId());
	}

	/**
	 * This method is used to save payment details for the given payment details id
	 * 
	 * @param paymentDetailsId
	 * @return
	 * @throws InvalidPaymentInfoException
	 */
	protected CartData setPaymentDetailsInternal(final String paymentDetailsId) throws InvalidPaymentInfoException
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("setPaymentDetailsInternal: " + logParam("paymentDetailsId", paymentDetailsId));
		}
		if (checkoutFacade.setPaymentDetails(paymentDetailsId))
		{
			return getSessionCart();
		}
		throw new InvalidPaymentInfoException(paymentDetailsId);
	}

	/**
	 * This method is used to validate cart for the placeorder
	 * 
	 * @throws NoCheckoutCartException
	 * @throws InvalidCartException
	 * @throws WebserviceValidationException
	 */
	protected void validateCartForPlaceOrder() throws NoCheckoutCartException, InvalidCartException, WebserviceValidationException //NOSONAR
	{
		if (!checkoutFacade.hasCheckoutCart())
		{
			throw new NoCheckoutCartException("Cannot place order. There was no checkout cart created yet!");
		}

		final CartData cartData = getSessionCart();

		final Errors errors = new BeanPropertyBindingResult(cartData, "sessionCart");
		placeOrderCartValidator.validate(cartData, errors);
		if (errors.hasErrors())
		{
			throw new WebserviceValidationException(errors);
		}

		try
		{
			final List<CartModificationData> modificationList = cartFacade.validateCartData();
			if (modificationList != null && !modificationList.isEmpty())
			{
				final CartModificationDataList cartModificationDataList = new CartModificationDataList();
				cartModificationDataList.setCartModificationList(modificationList);
				throw new WebserviceValidationException(cartModificationDataList);
			}
		}
		catch (final CommerceCartModificationException e)
		{
			throw new InvalidCartException(e);
		}
	}

	/**
	 * Gets session cart
	 * 
	 * @return
	 */
	protected CartData getSessionCart()
	{
		return cartFacade.getSessionCart();
	}

	/**
	 * Checks if given statuses are valid
	 *
	 * @param statuses
	 */
	protected void validateStatusesEnumValue(final String statuses)
	{
		if (statuses == null)
		{
			return;
		}

		final String[] statusesStrings = statuses.split(YcommercewebservicesConstants.OPTIONS_SEPARATOR);
		validate(statusesStrings, "", orderStatusValueValidator);
	}

	/**
	 * Gets cartFacade
	 * 
	 * @return cartFacade
	 */

	protected CartFacade getCartFacade()
	{
		return cartFacade;
	}

	/**
	 * Sets the cartFacade
	 * 
	 * @param cartFacade
	 */
	protected void setCartFacade(final CartFacade cartFacade)
	{
		this.cartFacade = cartFacade;
	}

	/**
	 * Get checkout Facade
	 * 
	 * @return checkoutFacade
	 */
	protected CheckoutFacade getCheckoutFacade()
	{
		return checkoutFacade;
	}

	/**
	 * Set the checkout Facade
	 * 
	 * @param checkoutFacade
	 */
	protected void setCheckoutFacade(final CheckoutFacade checkoutFacade)
	{
		this.checkoutFacade = checkoutFacade;
	}

	/**
	 * Get VoucherFacade object
	 * 
	 * @return voucherFacade
	 */
	protected VoucherFacade getVoucherFacade()
	{
		return voucherFacade;
	}

	/**
	 * Set the VoucherFacade object
	 * 
	 * @param voucherFacade
	 */
	protected void setVoucherFacade(final VoucherFacade voucherFacade)
	{
		this.voucherFacade = voucherFacade;
	}

	/**
	 * Get Delivery Address Validator object
	 * 
	 * @return deliveryAddressValidator
	 */
	protected Validator getDeliveryAddressValidator()
	{
		return deliveryAddressValidator;
	}

	/**
	 * This method is used to set delivery address validator object
	 * 
	 * @param deliveryAddressValidator
	 */
	protected void setDeliveryAddressValidator(final Validator deliveryAddressValidator)
	{
		this.deliveryAddressValidator = deliveryAddressValidator;
	}

	/**
	 * This method is used to set delivery address validator object
	 * 
	 * @return
	 */
	protected Populator<HttpServletRequest, AddressData> getHttpRequestAddressDataPopulator()
	{
		return httpRequestAddressDataPopulator;
	}

	/**
	 * This method is used to set HttpRequestAddressDataPopulator object
	 * 
	 * @param httpRequestAddressDataPopulator
	 */
	protected void setHttpRequestAddressDataPopulator(
			final Populator<HttpServletRequest, AddressData> httpRequestAddressDataPopulator)
	{
		this.httpRequestAddressDataPopulator = httpRequestAddressDataPopulator;
	}

	/**
	 * This method is used to get AddressValidator object
	 * 
	 * @return addressValidator
	 */
	protected Validator getAddressValidator()
	{
		return addressValidator;
	}

	/**
	 * This method is used to set AddressValidator object
	 * 
	 * @param addressValidator
	 */
	protected void setAddressValidator(final Validator addressValidator)
	{
		this.addressValidator = addressValidator;
	}

	/**
	 * This method is used to get AddressDTOValidator object
	 * 
	 * @return addressDTOValidator
	 */
	protected Validator getAddressDTOValidator()
	{
		return addressDTOValidator;
	}

	/**
	 * This method is used to set AddressDTOValidator object
	 * 
	 * @param addressDTOValidator
	 */
	protected void setAddressDTOValidator(final Validator addressDTOValidator)
	{
		this.addressDTOValidator = addressDTOValidator;
	}

	/**
	 * This method is used to get UserFacade object
	 * 
	 * @return userFacade
	 */
	protected UserFacade getUserFacade()
	{
		return userFacade;
	}

	/**
	 * This method is used to set UserFacade object
	 * 
	 * @param userFacade
	 */
	protected void setUserFacade(final UserFacade userFacade)
	{
		this.userFacade = userFacade;
	}

	/**
	 * This method is used to get CcPaymentInfoValidator object
	 * 
	 * @return ccPaymentInfoValidator
	 */
	protected Validator getCcPaymentInfoValidator()
	{
		return ccPaymentInfoValidator;
	}

	/**
	 * This method is used to set CcPaymentInfoValidator object
	 * 
	 * @param ccPaymentInfoValidator
	 */
	protected void setCcPaymentInfoValidator(final Validator ccPaymentInfoValidator)
	{
		this.ccPaymentInfoValidator = ccPaymentInfoValidator;
	}

	/**
	 * This method is used to get PaymentDetailsDTOValidator object
	 * 
	 * @return paymentDetailsDTOValidator
	 */
	protected Validator getPaymentDetailsDTOValidator()
	{
		return paymentDetailsDTOValidator;
	}

	/**
	 * This method is used to set PaymentDetailsDTOValidator object
	 * 
	 * @param paymentDetailsDTOValidator
	 */
	protected void setPaymentDetailsDTOValidator(final Validator paymentDetailsDTOValidator)
	{
		this.paymentDetailsDTOValidator = paymentDetailsDTOValidator;
	}

	/**
	 * This method is used to get HttpRequestPaymentInfoPopulator object
	 * 
	 * @return httpRequestPaymentInfoPopulator
	 */
	protected ConfigurablePopulator<HttpServletRequest, CCPaymentInfoData, PaymentInfoOption> getHttpRequestPaymentInfoPopulator()
	{
		return httpRequestPaymentInfoPopulator;
	}

	/**
	 * This method is used to set HttpRequestPaymentInfoPopulator object
	 * 
	 * @param httpRequestPaymentInfoPopulator
	 */
	protected void setHttpRequestPaymentInfoPopulator(
			final ConfigurablePopulator<HttpServletRequest, CCPaymentInfoData, PaymentInfoOption> httpRequestPaymentInfoPopulator)
	{
		this.httpRequestPaymentInfoPopulator = httpRequestPaymentInfoPopulator;
	}

}
