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
package com.woolies.webservices.rest.stock.impl;

import de.hybris.platform.basecommerce.enums.StockLevelStatus;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commercefacades.product.data.StockData;
import de.hybris.platform.commerceservices.stock.CommerceStockService;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.storelocator.model.PointOfServiceModel;
import de.hybris.platform.storelocator.pos.PointOfServiceService;
import de.hybris.platform.webservicescommons.util.YSanitizer;

import org.springframework.beans.factory.annotation.Required;

import com.woolies.webservices.rest.stock.CommerceStockFacade;
import com.woolies.webservices.rest.strategies.BaseStoreForSiteSelectorStrategy;


/**
 * Default implementation of {@link com.woolies.webservices.rest.stock.CommerceStockFacade}<br/>
 * when moving to commercelayer:<br/>
 * please add validation to the default implementation of BaseSiteService and throw exceptions from there</br>
 * ideally get rid of createStockData()
 */
public class DefaultCommerceStockFacade implements CommerceStockFacade
{
	private BaseSiteService baseSiteService;
	private CommerceStockService commerceStockService;
	private ProductService productService;
	private PointOfServiceService pointOfServiceService;
	private BaseStoreForSiteSelectorStrategy baseStoreForSiteSelectorStrategy;

	/**
	 * Indicates if stock system is enabled for given base store
	 *
	 * @param baseSiteId
	 *           to be checked
	 * @return true if stock system is enabled
	 * @throws UnknownIdentifierException
	 *            the unknown identifier exception when no base site with given id was found
	 */
	@Override
	public boolean isStockSystemEnabled(final String baseSiteId) throws UnknownIdentifierException //NOSONAR
	{
		// it's not checked in the service layer (!) :
		ServicesUtil.validateParameterNotNull(baseSiteId, "Parameter baseSiteId must not be null");
		final BaseSiteModel baseSiteModel = getBaseSiteService().getBaseSiteForUID(baseSiteId);
		if (baseSiteModel == null)
		{
			throw new UnknownIdentifierException("Base site with uid '" + YSanitizer.sanitize(baseSiteId) + "' not found!");
		}

		return getCommerceStockService().isStockSystemEnabled(getBaseStoreForSiteSelectorStrategy().getBaseStore(baseSiteModel));
	}

	/**
	 * Returns stock data for combination of given product and base site
	 *
	 * @param productCode
	 * @param baseSiteId
	 * @return {@link StockData} information
	 * @throws UnknownIdentifierException
	 *            the unknown identifier exception when no base site or product with given id was found
	 * @throws IllegalArgumentException
	 *            the illegal argument exception when any one parameter is null
	 * @throws AmbiguousIdentifierException
	 *            the ambiguous identifier exception when there is more than one product with given code
	 */
	@Override
	public StockData getStockDataForProductAndBaseSite(final String productCode, final String baseSiteId)
			throws UnknownIdentifierException, IllegalArgumentException, AmbiguousIdentifierException //NOSONAR
	{
		StockData stockData = null;
		// it's not checked in the service layer (!) :
		ServicesUtil.validateParameterNotNull(baseSiteId, "Parameter baseSiteId must not be null");
		final BaseSiteModel baseSiteModel = getBaseSiteService().getBaseSiteForUID(baseSiteId);
		if (baseSiteModel == null)
		{
			throw new UnknownIdentifierException("Base site with uid '" + YSanitizer.sanitize(baseSiteId) + "' not found!");
		}

		final ProductModel productModel = getProductService().getProductForCode(productCode);
		if (productModel != null)
		{
			final StockLevelStatus stockLevelStatus = getCommerceStockService().getStockLevelStatusForProductAndBaseStore(
					productModel, getBaseStoreForSiteSelectorStrategy().getBaseStore(baseSiteModel));
			final Long stockLevelForProduct = getCommerceStockService().getStockLevelForProductAndBaseStore(productModel,
					getBaseStoreForSiteSelectorStrategy().getBaseStore(baseSiteModel));
			if (stockLevelStatus != null && stockLevelForProduct != null)
			{
				stockData = createStockData(stockLevelStatus, stockLevelForProduct);
			}
			if (stockData != null)
			{
				return stockData;
			}
			else
			{
				return null;

			}
		}
		else
		{
			return null;
		}
	}

	/**
	 * Returns stock data for given product and point of service (that also indicates warehouse)
	 *
	 * @param productCode
	 * @param storeName
	 * @return {@link StockData} information
	 * @throws UnknownIdentifierException
	 *            the unknown identifier exception when no store or product with given id was found
	 * @throws IllegalArgumentException
	 *            the illegal argument exception when any one parameter is null
	 * @throws AmbiguousIdentifierException
	 *            the ambiguous identifier exception when there is more than one product with given code
	 */
	@Override
	public StockData getStockDataForProductAndPointOfService(final String productCode, final String storeName)
			throws UnknownIdentifierException, IllegalArgumentException, AmbiguousIdentifierException //NOSONAR
	{
		StockData stockData = null;
		final ProductModel productModel = getProductService().getProductForCode(productCode);
		final PointOfServiceModel pointOfServiceModel = getPointOfServiceService().getPointOfServiceForName(storeName);

		final StockLevelStatus stockLevelStatus = getCommerceStockService()
				.getStockLevelStatusForProductAndPointOfService(productModel, pointOfServiceModel);
		final Long stockLevel = getCommerceStockService().getStockLevelForProductAndPointOfService(productModel,
				pointOfServiceModel);
		if (stockLevelStatus != null && stockLevel != null)
		{
			stockData = createStockData(stockLevelStatus, stockLevel);
		}
		if (stockData != null)
		{
			return stockData;
		}
		else
		{
			return null;
		}

	}

	/**
	 * This method is used here instead of regular populator beacause {@link CommerceStockService} returns all values
	 * separately.<br/>
	 * Ideally would be to improve it someday by returning StockLevelModel.
	 *
	 * @param stockLevelStatus
	 *           stock level status
	 * @param stockLevel
	 *           stock level
	 * @return stockData
	 */
	protected StockData createStockData(final StockLevelStatus stockLevelStatus, final Long stockLevel)
	{
		final StockData stockData = new StockData();
		stockData.setStockLevelStatus(stockLevelStatus);
		stockData.setStockLevel(stockLevel);
		return stockData;
	}

	/**
	 * This method is used to get CommerceStockService object
	 *
	 * @return commerceStockService
	 */
	public CommerceStockService getCommerceStockService()
	{
		return commerceStockService;
	}

	/**
	 * This method is used to set CommerceStockService object
	 * 
	 * @param commerceStockService
	 */
	@Required
	public void setCommerceStockService(final CommerceStockService commerceStockService)
	{
		this.commerceStockService = commerceStockService;
	}

	/**
	 * This method is used to get baseSiteService object
	 * 
	 * @return baseSiteService
	 */
	public BaseSiteService getBaseSiteService()
	{
		return baseSiteService;
	}

	/**
	 * This method is used to set baseSiteService object
	 * 
	 * @param baseSiteService
	 */
	@Required
	public void setBaseSiteService(final BaseSiteService baseSiteService)
	{
		this.baseSiteService = baseSiteService;
	}

	/**
	 * This method is used to get tBaseStoreForSiteSelectorStrategy object
	 * 
	 * @return baseStoreForSiteSelectorStrategy
	 */
	public BaseStoreForSiteSelectorStrategy getBaseStoreForSiteSelectorStrategy()
	{
		return baseStoreForSiteSelectorStrategy;
	}

	/**
	 * This method is used to set tBaseStoreForSiteSelectorStrategy object
	 * 
	 * @param baseStoreForSiteSelectorStrategy
	 */
	@Required
	public void setBaseStoreForSiteSelectorStrategy(final BaseStoreForSiteSelectorStrategy baseStoreForSiteSelectorStrategy)
	{
		this.baseStoreForSiteSelectorStrategy = baseStoreForSiteSelectorStrategy;
	}

	public ProductService getProductService()
	{
		return productService;
	}

	/**
	 * This method is used to set ProductService object
	 * 
	 * @param productService
	 */
	@Required
	public void setProductService(final ProductService productService)
	{
		this.productService = productService;
	}

	/**
	 * This method is used to get PointOfServiceService object
	 * 
	 * @return pointOfServiceService
	 */
	public PointOfServiceService getPointOfServiceService()
	{
		return pointOfServiceService;
	}

	/**
	 * This method is used to set PointOfServiceService object
	 * 
	 * @param pointOfServiceService
	 */
	@Required
	public void setPointOfServiceService(final PointOfServiceService pointOfServiceService)
	{
		this.pointOfServiceService = pointOfServiceService;
	}
}
