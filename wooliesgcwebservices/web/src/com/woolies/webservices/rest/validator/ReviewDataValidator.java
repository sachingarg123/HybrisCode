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

import static de.hybris.platform.customerreview.model.CustomerReviewModel.*;

import de.hybris.platform.commercefacades.product.data.ReviewData;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;


/**
 * This class is used to validate ReviewData details
 *
 *
 */
@Component("reviewValidator")
public class ReviewDataValidator implements Validator
{
	private static final String FIELD_REQUIRED_MESSAGE_ID = "field.required";
	private static final double RATING_MIN = 1.0d;
	private static final double RATING_MAX = 5.0d;

	@Override
	public boolean supports(final Class clazz)
	{
		return ReviewData.class.equals(clazz);
	}

	/**
	 * This method is used to validate ReviewData details
	 * 
	 * @param target
	 * @param errors
	 */
	@Override
	public void validate(final Object target, final Errors errors)
	{
		ValidationUtils.rejectIfEmpty(errors, HEADLINE, FIELD_REQUIRED_MESSAGE_ID);
		ValidationUtils.rejectIfEmpty(errors, COMMENT, FIELD_REQUIRED_MESSAGE_ID);
		validateRating(errors);
	}

	/**
	 * This method is used to validate Rating details
	 * 
	 * @param errors
	 */
	protected void validateRating(final Errors errors)
	{
		Assert.notNull(errors, "Errors object must not be null");
		final Double rating = (Double) errors.getFieldValue(RATING);

		if (rating == null)
		{
			errors.rejectValue(RATING, FIELD_REQUIRED_MESSAGE_ID);
		}
		else
		{
			if (rating.doubleValue() < RATING_MIN || rating.doubleValue() > RATING_MAX)
			{
				errors.rejectValue(RATING, "review.rating.invalid");
			}
		}
	}

}
