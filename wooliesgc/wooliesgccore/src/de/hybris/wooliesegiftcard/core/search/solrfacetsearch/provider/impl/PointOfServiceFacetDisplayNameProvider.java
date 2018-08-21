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
package de.hybris.wooliesegiftcard.core.search.solrfacetsearch.provider.impl;

import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.provider.impl.AbstractFacetValueDisplayNameProvider;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.storelocator.model.PointOfServiceModel;
import de.hybris.platform.storelocator.pos.PointOfServiceService;

import org.springframework.beans.factory.annotation.Required;


/**
 *
 * @author Cognizant
 *
 *         This class is to display the name tof the service provider
 *
 */
public class PointOfServiceFacetDisplayNameProvider extends AbstractFacetValueDisplayNameProvider
{
	private PointOfServiceService pointOfServiceService;

	/**
	 * Displays facet value for the service provider
	 * 
	 * @param query
	 * @param property
	 * @param facetValue
	 * @return facetValue
	 */
	@Override
	public String getDisplayName(final SearchQuery query, final IndexedProperty property, final String facetValue)
	{
		final PointOfServiceModel posModel = getPointOfServiceService().getPointOfServiceForName(facetValue);
		if (posModel != null)
		{
			return posModel.getName();
		}
		return facetValue;
	}

	/**
	 * To get point of service object
	 * 
	 * @return
	 */
	protected PointOfServiceService getPointOfServiceService()
	{
		return pointOfServiceService;
	}

	/**
	 * To set point of service object
	 * 
	 * @param pointOfServiceService
	 */
	@Required
	public void setPointOfServiceService(final PointOfServiceService pointOfServiceService)
	{
		this.pointOfServiceService = pointOfServiceService;
	}
}
