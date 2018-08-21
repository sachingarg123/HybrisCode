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

import de.hybris.platform.commercewebservicescommons.dto.user.PrincipalWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.user.UserGroupWsDTO;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * This class is used as validator for PrincipalListDTO
 * 
 *
 *
 */
public class PrincipalListDTOValidator implements Validator
{
	private UserService userService;
	private String fieldPath;
	private boolean canBeEmpty = true;

	@Override
	public boolean supports(final Class clazz)
	{
		return List.class.isAssignableFrom(clazz) || UserGroupWsDTO.class.isAssignableFrom(clazz);
	}

	/**
	 * This method is used as validator for PrincipalListDTO
	 * 
	 * @param target
	 * @param errors
	 */
	@Override
	public void validate(final Object target, final Errors errors)
	{
		final List<PrincipalWsDTO> list = (List<PrincipalWsDTO>) (fieldPath == null ? target : errors.getFieldValue(fieldPath));
		final String uidFieldName = fieldPath == null ? "uid" : fieldPath + ".uid";

		if (list == null || list.isEmpty())
		{
			if (!canBeEmpty)
			{
				errors.reject("field.required");
			}
		}
		else
		{
			for (final PrincipalWsDTO principal : list)
			{
				if (StringUtils.isEmpty(principal.getUid()))
				{
					errors.reject("field.withName.required", new String[]
					{ uidFieldName }, "Field {0} is required");
					break;
				}
				else
				{
					if (!userService.isUserExisting(principal.getUid()))
					{
						errors.reject("user.doesnt.exist", new String[]
						{ principal.getUid() }, "User {0} doesn''t exist or you have no privileges");
						break;
					}
				}
			}
		}
	}

	/**
	 * This method is used to get field path
	 *
	 * @return fieldPath
	 */
	public String getFieldPath()
	{
		return fieldPath;
	}

	/**
	 * This method is used to set field path
	 *
	 * @param fieldPath
	 */
	public void setFieldPath(final String fieldPath)
	{
		this.fieldPath = fieldPath;
	}

	/**
	 * This method is used to check CanBeEmpty
	 *
	 * @return
	 */
	public boolean getCanBeEmpty()
	{
		return canBeEmpty;
	}

	/**
	 * This method is used to set CanBeEmpty
	 *
	 * @param canBeEmpty
	 */
	public void setCanBeEmpty(final boolean canBeEmpty)
	{
		this.canBeEmpty = canBeEmpty;
	}

	/**
	 * This method is used to get UserService
	 * 
	 * @return UserService
	 */
	public UserService getUserService()
	{
		return userService;
	}

	/**
	 * This method is used to set UserService
	 * 
	 * @param userService
	 */
	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

}
