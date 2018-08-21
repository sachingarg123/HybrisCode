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
 * @author 648156 This class is used to validate number
 *
 */
public class NumberValidator implements Validator
{
	private String fieldPath;
	private int maxLength;
	private String errorMessageId;
	private final String mobilePattern = "[0-9]{10}";

	@Override
	public boolean supports(final Class clazz)
	{
		return true;
	}

	/**
	 * This method is used to validate number
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
		final String resultErrorMessageId1 = this.errorMessageId != null ? this.errorMessageId : "field.phoneNumberValidation";
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

		else if (!fieldValue.matches(mobilePattern))
		{
			errors.rejectValue(this.fieldPath, resultErrorMessageId, new String[]
			{ String.valueOf(this.maxLength) }, "This field is required and must to be between 1 and {0} characters long.");

		}

	}

	/**
	 * This method is used to get field
	 *
	 * @return fieldPath
	 */
	public String getFieldPath()
	{
		return this.fieldPath;
	}

	/**
	 * This method is used to set field
	 *
	 * @param fieldPath
	 */
	@Required
	public void setFieldPath(final String fieldPath)
	{
		this.fieldPath = fieldPath;
	}

	/**
	 * This method is used to get max length
	 *
	 * @return maxLength
	 */
	public int getMaxLength()
	{
		return this.maxLength;
	}


	@Required
	public void setMaxLength(final int maxLength)
	{
		this.maxLength = maxLength;
	}

	/**
	 * This method is used to get ErrorMessageId
	 *
	 * @return
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