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
package com.woolies.webservices.rest.mapping.mappers;

import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercewebservicescommons.dto.user.AddressValidationWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.user.AddressWsDTO;
import de.hybris.platform.webservicescommons.mapping.mappers.AbstractCustomMapper;

import com.woolies.webservices.rest.user.data.AddressDataList;
import com.woolies.webservices.rest.validation.data.AddressValidationData;

import ma.glasnost.orika.MappingContext;


/**
 * This class is address validation data mapper
 */
public class AddressValidationDataMapper extends AbstractCustomMapper<AddressValidationData, AddressValidationWsDTO>
{
	@Override
	public void mapAtoB(final AddressValidationData a, final AddressValidationWsDTO b, final MappingContext context)
	{
		// other fields are mapped automatically

		context.beginMappingField("suggestedAddressesList", getAType(), a, "suggestedAddresses", getBType(), b);
		try
		{
			if (shouldMap(a, b, context) && a.getSuggestedAddressesList() != null)
			{
				b.setSuggestedAddresses(
						mapperFacade.mapAsList(a.getSuggestedAddressesList().getAddresses(), AddressWsDTO.class, context));
			}
		}
		finally
		{
			context.endMappingField();
		}
	}

	/**
	 * This class maps AddressValidation data b to AddressValidation data a
	 * 
	 * @param b
	 * @param a
	 * @param context
	 */
	@Override
	public void mapBtoA(final AddressValidationWsDTO b, final AddressValidationData a, final MappingContext context)
	{
		// other fields are mapped automatically

		context.beginMappingField("suggestedAddresses", getBType(), b, "suggestedAddressesList", getAType(), a);
		try
		{
			if (shouldMap(b, a, context) && b.getSuggestedAddresses() != null)
			{
				final AddressDataList addressDataList = new AddressDataList();
				addressDataList.setAddresses(mapperFacade.mapAsList(b.getSuggestedAddresses(), AddressData.class, context));
				a.setSuggestedAddressesList(addressDataList);
			}
		}
		finally
		{
			context.endMappingField();
		}

	}
}
