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
package com.woolies.webservices.rest.validator;

import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;

import java.util.Calendar;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;



/**
 * Validates instances of {@link PaymentDetailsWsDTO}. This class is used as validator for the PaymentDetailsDTO
 *
 */
public class PaymentDetailsDTOValidator implements Validator
{
	private static final String FIELD_REQUIRED_MESSAGE_ID = "field.required";
	private Validator paymentAddressValidator;

	@Override
	public boolean supports(final Class clazz)
	{
		return PaymentDetailsWsDTO.class.isAssignableFrom(clazz);
	}

	/**
	 * This method is used as validator for the PaymentDetailsDTO
	 * 
	 * @param target
	 * @param errors
	 */
	@Override
	public void validate(final Object target, final Errors errors)
	{
		final PaymentDetailsWsDTO paymentDetails = (PaymentDetailsWsDTO) target;

		if (StringUtils.isNotBlank(paymentDetails.getStartMonth()) && StringUtils.isNotBlank(paymentDetails.getStartYear())
				&& StringUtils.isNotBlank(paymentDetails.getExpiryMonth()) && StringUtils.isNotBlank(paymentDetails.getExpiryYear()))
		{
			final Calendar start = Calendar.getInstance();
			start.set(Calendar.DAY_OF_MONTH, 0);
			start.set(Calendar.MONTH, Integer.parseInt(paymentDetails.getStartMonth()) - 1);
			start.set(Calendar.YEAR, Integer.parseInt(paymentDetails.getStartYear()) - 1);

			final Calendar expiration = Calendar.getInstance();
			expiration.set(Calendar.DAY_OF_MONTH, 0);
			expiration.set(Calendar.MONTH, Integer.parseInt(paymentDetails.getExpiryMonth()) - 1);
			expiration.set(Calendar.YEAR, Integer.parseInt(paymentDetails.getExpiryYear()) - 1);

			if (start.after(expiration))
			{
				errors.rejectValue("startMonth", "payment.startDate.invalid");
			}
		}

		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "accountHolderName", FIELD_REQUIRED_MESSAGE_ID);
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "cardType.code", FIELD_REQUIRED_MESSAGE_ID);
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "cardNumber", FIELD_REQUIRED_MESSAGE_ID);
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "expiryMonth", FIELD_REQUIRED_MESSAGE_ID);
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "expiryYear", FIELD_REQUIRED_MESSAGE_ID);

		paymentAddressValidator.validate(paymentDetails, errors);
	}

	public Validator getPaymentAddressValidator()
	{
		return paymentAddressValidator;
	}

	/**
	 * This method is used to set PaymentAddressValidator
	 * 
	 * @param paymentAddressValidator
	 */
	@Required
	public void setPaymentAddressValidator(final Validator paymentAddressValidator)
	{
		this.paymentAddressValidator = paymentAddressValidator;
	}
}
