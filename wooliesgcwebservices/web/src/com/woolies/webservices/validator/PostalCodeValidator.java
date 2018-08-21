/**
 *
 */
package com.woolies.webservices.validator;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


/**
 * @author 648156 This class is used to validate postal code
 *
 */
public class PostalCodeValidator implements Validator
{
	private String fieldPath;
	private int maxLength;
	private String errorMessageId;
	private final String postalCodePattern = "[0-9]{6}";

	@Override
	public boolean supports(final Class clazz)
	{
		return true;
	}

	/**
	 * This method is used to validate postal code
	 *
	 * @param object
	 * @param errors
	 */
	@Override
	public void validate(final Object object, final Errors errors)
	{
		Assert.notNull(errors, "Errors object must not be null");
		final String fieldValue = (String) errors.getFieldValue(this.fieldPath);
		final String resultErrorMessageId = this.errorMessageId != null ? this.errorMessageId : "field.requiredAndNotTooLong";
		final String resultErrorMessageId1 = this.errorMessageId != null ? this.errorMessageId : "field.postalCodeValidation";
		if (StringUtils.isBlank(fieldValue) || StringUtils.length(fieldValue) > this.maxLength)
		{
			errors.rejectValue(this.fieldPath, resultErrorMessageId, new String[]
			{ String.valueOf(this.maxLength) }, "This field is required and must to be between 1 and {0} characters long.");
		}
		else if (!StringUtils.isNumeric(fieldValue))
		{
			errors.rejectValue(this.fieldPath, resultErrorMessageId1, new String[]
			{ String.valueOf(this.fieldPath) }, "must be a numeric.");
		}

		else if (!fieldValue.matches(postalCodePattern))
		{
			errors.rejectValue(this.fieldPath, resultErrorMessageId, new String[]
			{ String.valueOf(this.maxLength) }, "This field is required and must to be between 1 and {0} characters long.");

		}

	}


	/**
	 * This method is used to get Field path
	 *
	 * @return fieldPath
	 */
	public String getFieldPath()
	{
		return this.fieldPath;
	}

	/**
	 * This method is used to set field path
	 *
	 * @param fieldPath
	 */
	@Required
	public void setFieldPath(final String fieldPath)
	{
		this.fieldPath = fieldPath;
	}

	/**
	 * This method is used to get MaxLength
	 *
	 * @return
	 */
	public int getMaxLength()
	{
		return this.maxLength;
	}

	/**
	 * This method is used to set MaxLength
	 *
	 * @param maxLength
	 */
	@Required
	public void setMaxLength(final int maxLength)
	{
		this.maxLength = maxLength;
	}

	/**
	 * This method is used to get ErrorMessageId
	 *
	 * @return errorMessageId
	 */
	public String getErrorMessageId()
	{
		return this.errorMessageId;
	}

	/**
	 * This method is used to set ErrorMessageId
	 *
	 * @param errorMessageId
	 */
	public void setErrorMessageId(final String errorMessageId)
	{
		this.errorMessageId = errorMessageId;
	}
}