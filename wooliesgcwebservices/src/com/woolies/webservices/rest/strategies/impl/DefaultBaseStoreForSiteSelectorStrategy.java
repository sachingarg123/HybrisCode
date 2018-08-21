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
package com.woolies.webservices.rest.strategies.impl;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.store.BaseStoreModel;

import com.woolies.webservices.rest.strategies.BaseStoreForSiteSelectorStrategy;


/**
 * Default implementation of {@link BaseStoreForSiteSelectorStrategy} which returns first base store from collection.
 */
public class DefaultBaseStoreForSiteSelectorStrategy implements BaseStoreForSiteSelectorStrategy
{

	/**
	 * This method is used to first get base store object param BaseStoreModel
	 */
	@Override
	public BaseStoreModel getBaseStore(final BaseSiteModel baseSiteModel)
	{
		return baseSiteModel.getStores().get(0);
	}
}
