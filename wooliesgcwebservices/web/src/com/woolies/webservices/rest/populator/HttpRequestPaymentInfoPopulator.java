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
package com.woolies.webservices.rest.populator;

import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.converters.ConfigurablePopulator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import java.util.Collection;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.woolies.webservices.rest.populator.options.PaymentInfoOption;




/**
 * Populates {@link CCPaymentInfoData} instance from the {@link HttpServletRequest} parameters:<br>
 * <ul>
 * <li>accountHolderName</li>
 * <li>cardNumber</li>
 * <li>cardType</li>
 * <li>expiryMonth</li>
 * <li>expiryYear</li>
 * <li>issueNumber</li>
 * <li>startMonth</li>
 * <li>startYear</li>
 * <li>subscriptionId</li>
 * <li>saved</li>
 * <li>defaultPaymentInfo</li>
 * </ul>
 *
 * If populator's options contains {@link PaymentInfoOption#BILLING_ADDRESS}, it uses
 * {@link HttpRequestAddressDataPopulator} with prefix 'billingAddress' to populate also the billing address data from
 * http request request parameters.
 *
 *
 *
 */
@Component("httpRequestPaymentInfoPopulator")
public class HttpRequestPaymentInfoPopulator extends AbstractHttpRequestDataPopulator
		implements ConfigurablePopulator<HttpServletRequest, CCPaymentInfoData, PaymentInfoOption>
{

	private static final String ACCOUNT_HOLDER_NAME = "accountHolderName";
	private static final String CARD_NUMBER = "cardNumber";
	private static final String CARD_TYPE = "cardType";
	private static final String EXPIRY_MONTH = "expiryMonth";
	private static final String EXPIRY_YEAR = "expiryYear";
	private static final String ISSUE_NUMBER = "issueNumber";
	private static final String START_MONTH = "startMonth";
	private static final String START_YEAR = "startYear";
	private static final String SUBSCRIPTION_ID = "subscriptionId";
	private static final String DEFAULT_PAYMENT_INFO = "defaultPaymentInfo";
	private static final String SAVED = "saved";
	private static final String BILLING_ADDRESS = "billingAddress";

	@Resource(name = "httpRequestAddressDataPopulator")
	private HttpRequestAddressDataPopulator httpRequestAddressDataPopulator;

	/**
	 * This method used to populate payment information
	 * 
	 * @param request
	 * @param target
	 * @param options
	 * @throws ConversionException
	 */
	@Override
	public void populate(final HttpServletRequest request, final CCPaymentInfoData target,
			final Collection<PaymentInfoOption> options) throws ConversionException //NOSONAR
	{

		target.setAccountHolderName(updateStringValueFromRequest(request, ACCOUNT_HOLDER_NAME, target.getAccountHolderName()));
		target.setCardNumber(updateStringValueFromRequest(request, CARD_NUMBER, target.getCardNumber()));
		target.setCardType(updateStringValueFromRequest(request, CARD_TYPE, target.getCardType()));
		target.setIssueNumber(updateStringValueFromRequest(request, ISSUE_NUMBER, target.getIssueNumber()));

		target.setExpiryMonth(updateStringValueFromRequest(request, EXPIRY_MONTH, target.getExpiryMonth()));
		target.setExpiryYear(updateStringValueFromRequest(request, EXPIRY_YEAR, target.getExpiryYear()));
		target.setStartMonth(updateStringValueFromRequest(request, START_MONTH, target.getStartMonth()));
		target.setStartYear(updateStringValueFromRequest(request, START_YEAR, target.getStartYear()));
		target.setSubscriptionId(updateStringValueFromRequest(request, SUBSCRIPTION_ID, target.getSubscriptionId()));
		target.setDefaultPaymentInfo(updateBooleanValueFromRequest(request, DEFAULT_PAYMENT_INFO, target.isDefaultPaymentInfo()));
		target.setSaved(updateBooleanValueFromRequest(request, SAVED, target.isSaved()));

		if (options.contains(PaymentInfoOption.BILLING_ADDRESS))
		{
			final AddressData billingAddress = target.getBillingAddress() == null ? new AddressData() : target.getBillingAddress();
			final HttpRequestAddressDataPopulator billingAddressPopulator = getAddressPopulator();
			billingAddressPopulator.setAddressPrefix(BILLING_ADDRESS);
			billingAddressPopulator.populate(request, billingAddress);
			target.setBillingAddress(billingAddress);
		}
	}

	/**
	 * populates Address details
	 * 
	 * @return
	 */
	protected HttpRequestAddressDataPopulator getAddressPopulator()
	{
		return httpRequestAddressDataPopulator;
	}
}
