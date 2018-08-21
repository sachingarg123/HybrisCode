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
package com.woolies.webservices.rest.v2.controller;

import de.hybris.platform.commercefacades.product.ProductExportFacade;
import de.hybris.platform.commercefacades.product.ProductOption;
import de.hybris.platform.commercefacades.product.data.ProductResultData;
import de.hybris.platform.commercewebservicescommons.dto.product.ProductListWsDTO;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.RequestParameterException;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.woolies.webservices.rest.constants.YcommercewebservicesConstants;
import com.woolies.webservices.rest.formatters.WsDateFormatter;
import com.woolies.webservices.rest.product.data.ProductDataList;
import com.woolies.webservices.rest.swagger.ApiBaseSiteIdParam;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;


/**
 * Web Services Controller to expose the functionality of the
 * {@link de.hybris.platform.commercefacades.product.ProductFacade} and SearchFacade.
 */
@Controller
@RequestMapping(value = "/{baseSiteId}/export/products")
@Api(tags = "Export")
public class ExportController extends BaseController
{
	private static final Set<ProductOption> OPTIONS;
	private static final String DEFAULT_PAGE_VALUE = "0";
	private static final String MAX_INTEGER = "20";
	@Resource(name = "cwsProductExportFacade")
	private ProductExportFacade productExportFacade;
	@Resource(name = "wsDateFormatter")
	private WsDateFormatter wsDateFormatter;

	static
	{
		String productOptions = "";

		for (final ProductOption option : ProductOption.values())
		{
			productOptions = productOptions + option.toString() + " ";
		}
		productOptions = productOptions.trim().replace(" ", YcommercewebservicesConstants.OPTIONS_SEPARATOR);
		OPTIONS = extractOptions(productOptions);
	}

	/**
	 * This method is uset to get extract options
	 *
	 * @param options
	 * @return opts
	 */
	protected static Set<ProductOption> extractOptions(final String options)
	{
		final String[] optionsStrings = options.split(YcommercewebservicesConstants.OPTIONS_SEPARATOR);

		final Set<ProductOption> opts = new HashSet<ProductOption>();
		for (final String option : optionsStrings)
		{
			opts.add(ProductOption.valueOf(option));
		}
		return opts;
	}

	/**
	 * This method is used to get a list of product exports
	 *
	 * @param fields
	 * @param currentPage
	 * @param pageSize
	 * @param catalog
	 * @param version
	 * @param timestamp
	 * @return
	 */
	@Secured("ROLE_TRUSTED_CLIENT")
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get a list of product exports.", notes = "Used for product export. Depending on the timestamp parameter, it can return all products or only products modified after the given time.", authorizations =
	{ @Authorization(value = "oauth2_client_credentials") })
	@ApiBaseSiteIdParam
	public ProductListWsDTO exportProducts(
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields,
			@ApiParam(value = "The current result page requested.") @RequestParam(required = false, defaultValue = DEFAULT_PAGE_VALUE) final int currentPage,
			@ApiParam(value = "The number of results returned per page.") @RequestParam(required = false, defaultValue = MAX_INTEGER) final int pageSize,
			@ApiParam(value = "Catalog from which get products. Must be provided along with version.") @RequestParam(required = false) final String catalog,
			@ApiParam(value = "Catalog version. Must be provided along with catalog.") @RequestParam(required = false) final String version,
			@ApiParam(value = "When this parameter is set, only products modified after given time will be returned. This parameter should be in ISO-8601 format.") @RequestParam(required = false) final String timestamp)
	{
		if (StringUtils.isEmpty(catalog) && !StringUtils.isEmpty(version))
		{
			throw new RequestParameterException("Both 'catalog' and 'version' parameters have to be provided or ignored.",
					RequestParameterException.MISSING, catalog == null ? "catalog" : "version");
		}

		if (StringUtils.isEmpty(version) && !StringUtils.isEmpty(catalog))
		{
			throw new RequestParameterException("Both 'catalog' and 'version' parameters have to be provided or ignored.",
					RequestParameterException.MISSING, catalog == null ? "catalog" : "version");
		}

		if (StringUtils.isEmpty(timestamp))
		{
			return fullExport(fields, currentPage, pageSize, catalog, version);
		}
		else
		{
			return incrementalExport(fields, currentPage, pageSize, catalog, version, timestamp);
		}
	}

	/**
	 * This method is used for incrementalExport
	 *
	 * @param fields
	 * @param currentPage
	 * @param pageSize
	 * @param catalog
	 * @param version
	 * @param timestamp
	 * @return ProductListWsDTO
	 */
	protected ProductListWsDTO incrementalExport(final String fields, final int currentPage, final int pageSize,
			final String catalog, final String version, final String timestamp)
	{
		final Date timestampDate;
		try
		{
			timestampDate = wsDateFormatter.toDate(timestamp);
		}
		catch (final IllegalArgumentException e)
		{
			throw new RequestParameterException("Wrong time format. The only accepted format is ISO-8601.",
					RequestParameterException.INVALID, "timestamp", e);
		}

		final ProductResultData modifiedProducts = productExportFacade.getOnlyModifiedProductsForOptions(catalog, version,
				timestampDate, OPTIONS, currentPage, pageSize);

		return getDataMapper().map(convertResultset(currentPage, pageSize, catalog, version, modifiedProducts),
				ProductListWsDTO.class, fields);
	}

	/**
	 * This method is used for full export
	 * 
	 * @param fields
	 * @param currentPage
	 * @param pageSize
	 * @param catalog
	 * @param version
	 * @return
	 */
	protected ProductListWsDTO fullExport(final String fields, final int currentPage, final int pageSize, final String catalog,
			final String version)
	{
		final ProductResultData products = productExportFacade.getAllProductsForOptions(catalog, version, OPTIONS, currentPage,
				pageSize);

		return getDataMapper().map(convertResultset(currentPage, pageSize, catalog, version, products), ProductListWsDTO.class,
				fields);
	}

	/**
	 * This method is used to covert result set to ProductDataList object
	 * 
	 * @param page
	 * @param pageSize
	 * @param catalog
	 * @param version
	 * @param modifiedProducts
	 * @return
	 */
	protected ProductDataList convertResultset(final int page, final int pageSize, final String catalog, final String version,
			final ProductResultData modifiedProducts)
	{
		final ProductDataList result = new ProductDataList();
		result.setProducts(modifiedProducts.getProducts());
		if (pageSize > 0)
		{
			result.setTotalPageCount((modifiedProducts.getTotalCount() % pageSize == 0) ? modifiedProducts.getTotalCount() / pageSize
					: modifiedProducts.getTotalCount() / pageSize + 1);
		}
		result.setCurrentPage(page);
		result.setTotalProductCount(modifiedProducts.getTotalCount());
		result.setCatalog(catalog);
		result.setVersion(version);
		return result;
	}
}
