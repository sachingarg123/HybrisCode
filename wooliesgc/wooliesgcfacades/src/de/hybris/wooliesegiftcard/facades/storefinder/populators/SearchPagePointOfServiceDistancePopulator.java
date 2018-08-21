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
package de.hybris.wooliesegiftcard.facades.storefinder.populators;

import de.hybris.platform.commercefacades.storelocator.data.PointOfServiceData;
import de.hybris.platform.commerceservices.storefinder.data.PointOfServiceDistanceData;
import de.hybris.platform.commerceservices.storefinder.data.StoreFinderSearchPageData;
import de.hybris.platform.converters.Converters;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.util.UriComponentsBuilder;


/**
 *
 * @author This method is used to populate search page point of service
 * @param <SOURCE>
 * @param <TARGET>
 */
public class SearchPagePointOfServiceDistancePopulator<SOURCE extends StoreFinderSearchPageData<PointOfServiceDistanceData>, TARGET extends StoreFinderSearchPageData<PointOfServiceData>>
		implements Populator<SOURCE, TARGET>
{
	private Converter<PointOfServiceDistanceData, PointOfServiceData> pointOfServiceDistanceConverter;

	/**
	 * To get PointOfServiceDistanceConverter
	 * 
	 * @return pointOfServiceDistanceConverter
	 */
	protected Converter<PointOfServiceDistanceData, PointOfServiceData> getPointOfServiceDistanceConverter()
	{
		return pointOfServiceDistanceConverter;
	}

	/**
	 * To set PointOfServiceDistanceConverter
	 * 
	 * @param pointOfServiceDistanceConverter
	 */
	@Required
	public void setPointOfServiceDistanceConverter(
			final Converter<PointOfServiceDistanceData, PointOfServiceData> pointOfServiceDistanceConverter)
	{
		this.pointOfServiceDistanceConverter = pointOfServiceDistanceConverter;
	}

	/**
	 * This method is to populate search page point of service
	 * 
	 * @param source
	 * @param target
	 */
	@Override
	public void populate(final SOURCE source, final TARGET target)
	{
		target.setPagination(source.getPagination());
		target.setSorts(source.getSorts());
		target.setResults(Converters.convertAll(source.getResults(), getPointOfServiceDistanceConverter()));

		target.setLocationText(source.getLocationText());
		target.setSourceLatitude(source.getSourceLatitude());
		target.setSourceLongitude(source.getSourceLongitude());
		target.setBoundNorthLatitude(source.getBoundNorthLatitude());
		target.setBoundSouthLatitude(source.getBoundSouthLatitude());
		target.setBoundWestLongitude(source.getBoundWestLongitude());
		target.setBoundEastLongitude(source.getBoundEastLongitude());

		if (target.getResults() != null && !target.getResults().isEmpty())
		{
			final String urlPrefix = "/store/";
			final String urlQueryParams = getStoreUrlQueryParams(target);

			for (final PointOfServiceData pointOfService : target.getResults())
			{
				pointOfService.setUrl(urlPrefix + pointOfService.getName() + urlQueryParams);
			}
		}
	}

	/**
	 * To get StoreUrlQueryParams
	 * 
	 * @param target
	 * @return
	 */
	protected String getStoreUrlQueryParams(final TARGET target)
	{
		if (target.getLocationText() != null && !target.getLocationText().isEmpty())
		{
			// Build URL to position query
			return UriComponentsBuilder.fromPath("").queryParam("lat", Double.valueOf(target.getSourceLatitude()))
					.queryParam("long", Double.valueOf(target.getSourceLongitude())).queryParam("q", target.getLocationText()).build()
					.toString();
		}
		// Build URL to position query
		return UriComponentsBuilder.fromPath("").queryParam("lat", Double.valueOf(target.getSourceLatitude()))
				.queryParam("long", Double.valueOf(target.getSourceLongitude())).build().toString();
	}
}
