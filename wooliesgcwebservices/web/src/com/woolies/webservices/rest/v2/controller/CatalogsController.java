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


import de.hybris.platform.category.CategoryService;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.commercefacades.catalog.CatalogFacade;
import de.hybris.platform.commercefacades.catalog.CatalogOption;
import de.hybris.platform.commercefacades.catalog.PageOption;
import de.hybris.platform.commercefacades.catalog.data.CatalogData;
import de.hybris.platform.commercefacades.catalog.data.CatalogVersionData;
import de.hybris.platform.commercefacades.catalog.data.CatalogsData;
import de.hybris.platform.commercefacades.catalog.data.CategoryHierarchyData;
import de.hybris.platform.commercefacades.product.data.CategoryData;
import de.hybris.platform.commercewebservicescommons.dto.catalog.CatalogListWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.catalog.CatalogVersionWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.catalog.CatalogWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.catalog.CategoryHierarchyWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.product.CategoryWsDTO;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.webservicescommons.dto.error.ErrorListWsDTO;
import de.hybris.platform.webservicescommons.dto.error.ErrorWsDTO;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.mapping.FieldSetBuilder;
import de.hybris.platform.webservicescommons.mapping.impl.FieldSetBuilderContext;
import de.hybris.platform.wooliesgcfacades.category.dto.CategoryDetailsRequestDTO;
import de.hybris.wooliesegiftcard.core.model.PriceValueCustomerModel;
import de.hybris.wooliesegiftcard.facade.impl.DefaultWooilesCategoryFacade;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.woolies.webservices.constants.WooliesgcWebServicesConstants;
import com.woolies.webservices.rest.exceptions.WooliesWebserviceException;
import com.woolies.webservices.rest.swagger.ApiBaseSiteIdParam;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;


@Controller
@RequestMapping(value = "/{baseSiteId}/catalogs")
@Api(tags = "Catalogs")
public class CatalogsController extends BaseController
{
	private static final Set<CatalogOption> OPTIONS;
	private static final String CATALOG_NOT_FOUND_FOR_ISOCODE = "Catalog not found for this  isocode for current BaseStore";
	static
	{
		OPTIONS = getOptions();
	}
	@Resource(name = "configurationService")
	private ConfigurationService configurationService;
	@Resource(name = "cwsCatalogFacade")
	private CatalogFacade catalogFacade;
	@Resource(name = "wooilesCategoryFacade")
	private DefaultWooilesCategoryFacade wooilesCategoryFacade;
	@Resource(name = "fieldSetBuilder")
	private FieldSetBuilder fieldSetBuilder;
	@Resource(name = "userService")
	private UserService userService;
	@Resource(name = "categoryService")
	private CategoryService categoryService;

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get a list of catalogs", notes = "Returns all catalogs with versions defined for the base store.")
	@ApiBaseSiteIdParam
	public CatalogListWsDTO getCatalogs(
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		final List<CatalogData> catalogDataList = catalogFacade.getAllProductCatalogsForCurrentSite(OPTIONS);
		final CatalogsData catalogsData = new CatalogsData();
		catalogsData.setCatalogs(catalogDataList);

		final FieldSetBuilderContext context = new FieldSetBuilderContext();
		context.setRecurrencyLevel(countRecurrecyLevel(catalogDataList));
		final Set<String> fieldSet = fieldSetBuilder.createFieldSet(CatalogListWsDTO.class, DataMapper.FIELD_PREFIX, fields,
				context);

		return getDataMapper().map(catalogsData, CatalogListWsDTO.class, fieldSet);
	}



	@RequestMapping(value = "/{catalogId}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get a catalog", notes = "Returns a information about a catalog based on its ID, along with versions defined for the current base store.")
	@ApiBaseSiteIdParam
	public CatalogWsDTO getCatalog(@ApiParam(value = "Catalog identifier", required = true) @PathVariable final String catalogId,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		final CatalogData catalogData = catalogFacade.getProductCatalogForCurrentSite(catalogId, OPTIONS);

		final FieldSetBuilderContext context = new FieldSetBuilderContext();
		context.setRecurrencyLevel(countRecurrencyForCatalogData(catalogData));
		final Set<String> fieldSet = fieldSetBuilder.createFieldSet(CatalogWsDTO.class, DataMapper.FIELD_PREFIX, fields, context);

		return getDataMapper().map(catalogData, CatalogWsDTO.class, fieldSet);
	}


	@RequestMapping(value = "/{catalogId}/{catalogVersionId}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get information about catalog version", notes = "Returns information about catalog version that exists for the current base store.")
	@ApiBaseSiteIdParam
	public CatalogVersionWsDTO getCatalogVersion(
			@ApiParam(value = "Catalog identifier", required = true) @PathVariable final String catalogId,
			@ApiParam(value = "Catalog version identifier", required = true) @PathVariable final String catalogVersionId,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		final CatalogVersionData catalogVersionData = catalogFacade.getProductCatalogVersionForTheCurrentSite(catalogId,
				catalogVersionId, OPTIONS);

		final FieldSetBuilderContext context = new FieldSetBuilderContext();
		context.setRecurrencyLevel(countRecurrencyForCatalogVersionData(catalogVersionData));
		final Set<String> fieldSet = fieldSetBuilder.createFieldSet(CatalogVersionWsDTO.class, DataMapper.FIELD_PREFIX, fields,
				context);

		return getDataMapper().map(catalogVersionData, CatalogVersionWsDTO.class, fieldSet);
	}


	@RequestMapping(value = "/{catalogId}/{catalogVersionId}/categories/{categoryId}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get information about catagory in a catalog version", notes = "Returns information about category that exists in a catalog version available for the current base store.")
	@ApiBaseSiteIdParam
	public CategoryHierarchyWsDTO getCategories(
			@ApiParam(value = "Catalog identifier", required = true) @PathVariable final String catalogId,
			@ApiParam(value = "Catalog version identifier", required = true) @PathVariable final String catalogVersionId,
			@ApiParam(value = "Category identifier", required = true) @PathVariable final String categoryId,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(defaultValue = "DEFAULT") final String fields)
	{
		final PageOption page = PageOption.createForPageNumberAndPageSize(0, 10);
		final CategoryHierarchyData categoryHierarchyData = catalogFacade.getCategoryById(catalogId, catalogVersionId, categoryId,
				page, OPTIONS);

		final FieldSetBuilderContext context = new FieldSetBuilderContext();
		context.setRecurrencyLevel(countRecurrencyForCategoryHierarchyData(1, categoryHierarchyData));
		final Set<String> fieldSet = fieldSetBuilder.createFieldSet(CategoryHierarchyWsDTO.class, DataMapper.FIELD_PREFIX, fields,
				context);

		return getDataMapper().map(categoryHierarchyData, CategoryHierarchyWsDTO.class, fieldSet);
	}

	@RequestMapping(value = "/categories", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "Get information about catagory in a catalog version", notes = "Returns information about category that exists in a catalog version available for the current base store.")
	@ApiBaseSiteIdParam
	@ApiResponse(code = 200, message = "Category Details")
	//	@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
	public ResponseEntity<Object> getCategories(
			@ApiParam(value = "Category's object.", required = true) @RequestBody final CategoryDetailsRequestDTO categoryDetailsRequestDTO,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)

	{

		String userId = categoryDetailsRequestDTO.getUid();
		final String isoCode = categoryDetailsRequestDTO.getCountryISOCode();
		final String categoryId = categoryDetailsRequestDTO.getCategoryId();
		List<PriceValueCustomerModel> priceValueCustomerModel = null;
		CategoryData categoryData = null;
		String userType = "";
		final String catalogId = setCatalogbyIso(isoCode);
		userId = setandCheckUser(userId);
		if (null != userId && !userId.isEmpty())
		{
			final CustomerModel customer = (CustomerModel) userService.getUserForUID(userId);

			if (null != customer)
			{
				userType = customer.getCustomerType().toString();
			}
		}
		if (null != categoryId && !categoryId.isEmpty())
		{
			try
			{
				categoryService.getCategoryForCode(categoryId);
			}
			catch (final UnknownIdentifierException ue)
			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ISNOTCATEGORYEXIST,
						WooliesgcWebServicesConstants.ERRMSG_ISNOTCATEGORYEXIST,
						WooliesgcWebServicesConstants.ERRMSG_ISNOTCATEGORYEXIST);
			}

			if (WooliesgcWebServicesConstants.MEM.equals(userType))
			{
				try
				{
					final String memberCategory = getMemberCategory(userId);
					priceValueCustomerModel = getPriceValue(memberCategory);
					categoryData = wooilesCategoryFacade.findMemberCategoriesandProduct(
							WooliesgcWebServicesConstants.CATALOGVERSION_ONLINE, catalogId, memberCategory, categoryId);
				}
				catch (final UnknownIdentifierException ue)
				{
					throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_CATEGORY_NOT_ASSOCIATED,
							WooliesgcWebServicesConstants.ERRMSG_CATEGORY_NOT_ASSOCIATED,
							WooliesgcWebServicesConstants.ERRRSN_CATEGORY_NOT_ASSOCIATED);
				}
			}
			else
			{
				priceValueCustomerModel = getPriceValue(categoryId);
				categoryData = wooilesCategoryFacade.getSubCategories(categoryId, userId);
			}
		}
		else
		{
			if (WooliesgcWebServicesConstants.MEM.equals(userType))
			{
				categoryData = getMemberRootCategories(userId, catalogId);
			}
			else
			{
				categoryData = wooilesCategoryFacade.getRootCategories(catalogId,
						WooliesgcWebServicesConstants.CATALOGVERSION_ONLINE);
			}

		}

		if (null != categoryData.getProducts() && !categoryData.getProducts().isEmpty() && null != priceValueCustomerModel
				&& CollectionUtils.isNotEmpty(priceValueCustomerModel))
		{
			categoryData = wooilesCategoryFacade.getMemberPriceWithExclusion(categoryData, priceValueCustomerModel);
		}
		else
		{
			if (userType.equals("B2C") || userType.equals("B2B") || userType.equals(""))
			{
				categoryData = wooilesCategoryFacade.getSingleProductPrice(categoryData);
			}
		}

		return new ResponseEntity<>(getDataMapper().map(categoryData, CategoryWsDTO.class, fields), HttpStatus.OK);
	}

	CategoryData getMemberRootCategories(final String userId, final String catalogId)
	{
		CategoryData categoryData = null;
		final String memberCtegory = getMemberCategory(userId);
		categoryData = wooilesCategoryFacade.getMemberRootCategories(catalogId, WooliesgcWebServicesConstants.CATALOGVERSION_ONLINE,
				memberCtegory);

		if (null == categoryData || null == categoryData.getCategories())
		{
			throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_CATEGORY_NOT_ASSOCIATED,
					WooliesgcWebServicesConstants.ERRMSG_CATEGORY_NOT_ASSOCIATED,
					WooliesgcWebServicesConstants.ERRRSN_CATEGORY_NOT_ASSOCIATED);

		}
		return categoryData;
	}

	String getMemberCategory(final String userId)
	{
		final String MEM = "MEM_";
		String memberCtegory = "";
		final String seperator = configurationService.getConfiguration().getString("member.unitcustomerid.seperator", "");
		final StringTokenizer tokenizer = new StringTokenizer(userId, seperator);
		final String memberUnit = (String) tokenizer.nextElement();
		memberCtegory = MEM.concat(memberUnit);

		return memberCtegory;
	}

	String setCatalogbyIso(final String isoCode)
	{
		String catalogId = null;
		if (isoCode.equalsIgnoreCase(WooliesgcWebServicesConstants.ISOCODE_AU))
		{
			catalogId = WooliesgcWebServicesConstants.CATALOG_AU;
		}
		else if (isoCode.equalsIgnoreCase(WooliesgcWebServicesConstants.ISOCODE_NZ))
		{
			catalogId = WooliesgcWebServicesConstants.CATALOG_NZ;
		}
		else
		{
			throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ISNOTISOEXIST,
					WooliesgcWebServicesConstants.ERRMSG_ISNOTISOEXIST, WooliesgcWebServicesConstants.ERRRSN_ISNOTISOEXIST);
		}
		return catalogId;
	}

	String setandCheckUser(String userId)
	{
		if (null != userId && !userId.isEmpty())
		{
			if (!userService.isUserExisting(userId))
			{

				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ISNOTUSEREXIST,
						WooliesgcWebServicesConstants.ERRMSG_ISNOTUSEREXIST, WooliesgcWebServicesConstants.ERRRSN_ISNOTUSEREXIST);
			}
		}
		else
		{
			userId = "";
		}
		return userId;
	}

	List<PriceValueCustomerModel> getPriceValue(final String categoryId)
	{
		List<PriceValueCustomerModel> priceValueCustomerModel = null;
		if (categoryId.contains("MEM_"))
		{
			final CategoryModel categoryModel = categoryService.getCategoryForCode(categoryId);
			if (categoryModel.getPricevaluecustomer() != null && CollectionUtils.isNotEmpty(categoryModel.getPricevaluecustomer()))
			{
				priceValueCustomerModel = categoryModel.getPricevaluecustomer();
			}
		}
		return priceValueCustomerModel;
	}


	@RequestMapping(value = "/{isoCode}/users/{userId}/", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get information about catagory in a catalog version", notes = "Returns information about category that exists in a catalog version available for the current base store.")
	@ApiBaseSiteIdParam
	public ResponseEntity<Object> getSubCategoriesOrProductByCategory(
			@ApiParam(value = "Catalog identifier", required = true) @PathVariable final String isoCode,
			@ApiParam(value = "Catalog identifier", required = true) @PathVariable final String userId,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL,ROOTCATEGORIES") @RequestParam(defaultValue = "ROOTCATEGORIES") final String fields)
	{
		CategoryData categoryData = null;
		String catalogId = null;
		final ErrorWsDTO errorDetails = new ErrorWsDTO();
		final ErrorListWsDTO errorListDto = new ErrorListWsDTO();
		try
		{
			if (isoCode.equalsIgnoreCase(WooliesgcWebServicesConstants.ISOCODE_AU))
			{

				catalogId = WooliesgcWebServicesConstants.CATALOG_AU;
			}


			else if (isoCode.equalsIgnoreCase(WooliesgcWebServicesConstants.ISOCODE_NZ))
			{
				catalogId = WooliesgcWebServicesConstants.CATALOG_NZ;
			}

			else
			{
				errorDetails.setErrorCode(WooliesgcWebServicesConstants.ERRCODE_ISNOTISOEXIST);
				errorDetails.setErrorDescription(CATALOG_NOT_FOUND_FOR_ISOCODE);
				errorDetails.setErrorMessage(CATALOG_NOT_FOUND_FOR_ISOCODE);
				final List<ErrorWsDTO> errorDetailList = new ArrayList<>();
				errorDetailList.add(errorDetails);
				return new ResponseEntity<>(errorDetailList, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			categoryData = wooilesCategoryFacade.getRootCategories(catalogId, WooliesgcWebServicesConstants.CATALOGVERSION_ONLINE);
		}
		catch (final UnknownIdentifierException ue)
		{

			errorDetails.setErrorCode(WooliesgcWebServicesConstants.ERRCODE_ISNOTCATEGORYEXIST);
			errorDetails.setErrorDescription(ue.getMessage());
			errorDetails.setErrorMessage(ue.getMessage());
			final List<ErrorWsDTO> errorDetailList = new ArrayList<>();
			errorDetailList.add(errorDetails);
			errorListDto.setErrors(errorDetailList);
			return new ResponseEntity<>(errorListDto, HttpStatus.INTERNAL_SERVER_ERROR);

		}
		return new ResponseEntity<>(getDataMapper().map(categoryData, CategoryWsDTO.class, fields), HttpStatus.OK);
	}

	protected static Set<CatalogOption> getOptions()
	{
		final Set<CatalogOption> opts = new HashSet<>();
		opts.add(CatalogOption.BASIC);
		opts.add(CatalogOption.CATEGORIES);
		opts.add(CatalogOption.SUBCATEGORIES);
		opts.add(CatalogOption.PRODUCTS);
		return opts;
	}

	protected int countRecurrecyLevel(final List<CatalogData> catalogDataList)
	{
		int recurrencyLevel = 1;
		int value;
		for (final CatalogData catalog : catalogDataList)
		{
			value = countRecurrencyForCatalogData(catalog);
			if (value > recurrencyLevel)
			{
				recurrencyLevel = value;
			}
		}
		return recurrencyLevel;
	}

	protected int countRecurrencyForCatalogData(final CatalogData catalog)
	{
		int retValue = 1;
		int value;
		for (final CatalogVersionData version : catalog.getCatalogVersions())
		{
			value = countRecurrencyForCatalogVersionData(version);
			if (value > retValue)
			{
				retValue = value;
			}
		}
		return retValue;
	}

	protected int countRecurrencyForCatalogVersionData(final CatalogVersionData catalogVersion)
	{
		int retValue = 1;
		int value;
		for (final CategoryHierarchyData hierarchy : catalogVersion.getCategoriesHierarchyData())
		{
			value = countRecurrencyForCategoryHierarchyData(1, hierarchy);
			if (value > retValue)
			{
				retValue = value;
			}
		}
		return retValue;
	}

	protected int countRecurrencyForCategoryHierarchyData(final int currentValue, final CategoryHierarchyData hierarchy)
	{
		int calculatedValue = currentValue + 1;
		int subcategoryRecurrencyValue;
		for (final CategoryHierarchyData subcategory : hierarchy.getSubcategories())
		{
			subcategoryRecurrencyValue = countRecurrencyForCategoryHierarchyData(calculatedValue, subcategory);
			if (subcategoryRecurrencyValue > calculatedValue)
			{
				calculatedValue = subcategoryRecurrencyValue;
			}
		}
		return calculatedValue;
	}



	/**
	 * @return the wooilesCategoryFacade
	 */
	public DefaultWooilesCategoryFacade getWooilesCategoryFacade()
	{
		return wooilesCategoryFacade;
	}



	/**
	 * @param wooilesCategoryFacade
	 *           the wooilesCategoryFacade to set
	 */
	public void setWooilesCategoryFacade(final DefaultWooilesCategoryFacade wooilesCategoryFacade)
	{
		this.wooilesCategoryFacade = wooilesCategoryFacade;
	}
}
