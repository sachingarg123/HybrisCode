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

import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercewebservicescommons.dto.order.OrderEntryWsDTO;
import de.hybris.platform.storelocator.model.PointOfServiceModel;
import de.hybris.platform.storelocator.pos.PointOfServiceService;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


/**
 * Default commerce web services point of service validator. Checks if point of service with given name exist.
 */
public class PointOfServiceValidator implements Validator
{
	private PointOfServiceService pointOfServiceService;
	private String fieldPath;

	@Override
	public boolean supports(final Class<?> clazz)
	{
		return String.class.equals(clazz) || OrderEntryData.class.isAssignableFrom(clazz)
				|| OrderEntryWsDTO.class.isAssignableFrom(clazz);
	}

	/**
	 * This method is used to check if point of service with given name exist.
	 *
	 * @param target
	 * @param errors
	 */
	@Override
	public void validate(final Object target, final Errors errors)
	{
		final String storeName = fieldPath == null ? (String) target : (String) errors.getFieldValue(fieldPath);

		if (!StringUtils.isEmpty(storeName))
		{
			final PointOfServiceModel pointOfServiceModel = getPointOfServiceService().getPointOfServiceForName(storeName);
			if (pointOfServiceModel == null)
			{
				errors.reject("pointOfService.notExists");
			}
		}
	}

	/**
	 * This method is used to get point of service object
	 *
	 * @return pointOfServiceService
	 */
	protected PointOfServiceService getPointOfServiceService()
	{
		return pointOfServiceService;
	}

	/**
	 * This metho is used to set point of service object
	 *
	 * @param pointOfServiceService
	 */
	@Required
	public void setPointOfServiceService(final PointOfServiceService pointOfServiceService)
	{
		this.pointOfServiceService = pointOfServiceService;
	}

	/**
	 * This method is used to get field path
	 *
	 * @return
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

}
