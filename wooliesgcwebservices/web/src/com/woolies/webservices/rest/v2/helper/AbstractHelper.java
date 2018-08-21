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
package com.woolies.webservices.rest.v2.helper;

import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.webservicescommons.mapping.DataMapper;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;


/**
 * This class is abstract helper
 *
 */
@Component
public abstract class AbstractHelper
{
	@Resource(name = "dataMapper")
	private DataMapper dataMapper;

	/**
	 * This method is used to create page data
	 *
	 * @param currentPage
	 * @param pageSize
	 * @param sort
	 * @return
	 */
	protected PageableData createPageableData(final int currentPage, final int pageSize, final String sort)
	{
		final PageableData pageable = new PageableData();
		pageable.setCurrentPage(currentPage);
		pageable.setPageSize(pageSize);
		pageable.setSort(sort);
		return pageable;
	}

	/**
	 * Get data mapper object
	 * 
	 * @return dataMapper
	 */
	protected DataMapper getDataMapper()
	{
		return dataMapper;
	}

	/**
	 * Set the data mapper object
	 * 
	 * @param dataMapper
	 */
	protected void setDataMapper(final DataMapper dataMapper)
	{
		this.dataMapper = dataMapper;
	}
}
