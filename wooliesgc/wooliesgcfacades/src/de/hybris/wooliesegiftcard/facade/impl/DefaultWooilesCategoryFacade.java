/**
 *
 */
package de.hybris.wooliesegiftcard.facade.impl;

import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.commercefacades.product.data.CategoryData;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.wooliesegiftcard.core.model.PriceValueCustomerModel;
import de.hybris.wooliesegiftcard.facade.WooliesCategoryFacade;
import de.hybris.wooliesegiftcard.facades.category.populators.WooliesCategoryPopulator;
import de.hybris.wooliesegiftcard.facades.constants.WooliesgcFacadesConstants;
import de.hybris.wooliesgiftcard.core.category.service.impl.DefaultWooliesCategoryService;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;


/**
 * @author 653930 This class is maintain the category details
 */
public class DefaultWooilesCategoryFacade implements WooliesCategoryFacade
{
	private static final String CATEGORY_NOT_FOUND_FOR_ISOCODE = "Category with catalog %s not found for current BaseStore";
	private static final String CATEGORY_NOT_FOUND = "Category with  %s not found for current BaseStore";
	private static final String USER_TYPE_B2C = "B2C";
	private ConfigurationService configurationService;
	private DefaultWooliesCategoryService wooliesCategoryService;
	private WooliesCategoryPopulator wooliesCategoryPopulator;

	@Resource(name = "userService")
	private UserService userService;

	/**
	 * This method is used to get the root categories based on the catalog id and version
	 *
	 * @param catalogId
	 * @param catalogVersionId
	 * @return the categoryData
	 */
	@Override
	public CategoryData getRootCategories(final String catalogId, final String catalogVersionId)
	{
		final CategoryData data = new CategoryData();

		final CategoryModel category = getWooliesCategoryService().findRootCategoriesByCatalogVersion(catalogVersionId, catalogId);
		if (category == null)
		{
			throw new UnknownIdentifierException(String.format(CATEGORY_NOT_FOUND_FOR_ISOCODE, catalogId));
		}

		getWooliesCategoryPopulator().populate(category, data);
		return data;
	}

	/**
	 * This method is used to get the sub categories for the give category id and user id
	 *
	 * @param catalogId
	 * @param catalogVersionId
	 * @return categoryData
	 */
	@Override
	public CategoryData getMemberRootCategories(final String catalogId, final String catalogVersionId, final String memberCategory)
	{
		final CategoryData data = new CategoryData();

		final CategoryModel category = getWooliesCategoryService().findMemberRootCategoriesByCatalogVersion(catalogVersionId,
				catalogId, memberCategory);
		if (category == null)
		{
			throw new UnknownIdentifierException(String.format(CATEGORY_NOT_FOUND_FOR_ISOCODE, catalogId));
		}

		getWooliesCategoryPopulator().populate(category, data);
		return data;
	}

	/**
	 * This method is used to findMemberCategoriesandProduct
	 *
	 * @param catalogVersionName
	 *           catalogVersionName
	 * @param catalogId
	 *           catalogId
	 * @param memberCategory
	 *           memberCategory
	 * @param categoryId
	 *           categoryId
	 * @return the CategoryData
	 * @throws UnknownIdentifierException
	 *            used to throw exception
	 */
	public CategoryData findMemberCategoriesandProduct(final String catalogVersionName, final String catalogId,
			final String memberCategory, final String categoryId) throws UnknownIdentifierException
	{
		String minQntyArg;
		String maxQntyArg;
		final CategoryData data = new CategoryData();

		final CategoryModel category = getWooliesCategoryService().findMemberCategoriesandProduct(catalogVersionName, catalogId,
				memberCategory, categoryId);
		if (category == null)
		{
			throw new UnknownIdentifierException(String.format(CATEGORY_NOT_FOUND_FOR_ISOCODE, catalogId));
		}

		getWooliesCategoryPopulator().populate(category, data);

		minQntyArg = "user.MEM.card.min.qnty";
		maxQntyArg = "user.MEM.card.max.qnty";
		final Integer minQntry = getConfigurationService().getConfiguration().getInteger(minQntyArg, Integer.valueOf(1));
		final Integer maxQnty = getConfigurationService().getConfiguration().getInteger(maxQntyArg, Integer.valueOf(0));

		if (null != data.getProducts() && !data.getProducts().isEmpty())
		{
			for (final ProductData product : data.getProducts())
			{
				setProductMinAndMaxQty(minQntry, maxQnty, product);
			}
		}
		return data;

	}

	@Override
	public CategoryData getSubCategories(final String categoryId, final String userId)
	{
		final CategoryData data = new CategoryData();
		String userType = "";
		String minQntyArg;
		String maxQntyArg;

		final CategoryModel category = getWooliesCategoryService().getSubCategoriesByCategory(categoryId, userId);
		if (category == null)
		{
			throw new UnknownIdentifierException(String.format(CATEGORY_NOT_FOUND, categoryId));
		}
		getWooliesCategoryPopulator().populate(category, data);

		if (null != userId && !userId.isEmpty())
		{
			final CustomerModel customer = (CustomerModel) userService.getUserForUID(userId);

			if (null != customer)
			{
				userType = customer.getCustomerType().toString();
			}
		}
		else
		{
			userType = USER_TYPE_B2C;
		}
		if (WooliesgcFacadesConstants.B2B.equals(userType))
		{
			minQntyArg = "user.B2B.card.min.qnty";
			maxQntyArg = "user.B2B.card.max.qnty";
		}
		else if (WooliesgcFacadesConstants.MEM.equals(userType))
		{
			minQntyArg = "user.MEM.card.min.qnty";
			maxQntyArg = "user.MEM.card.max.qnty";
		}
		else
		{
			minQntyArg = "user.B2C.card.min.qnty";
			maxQntyArg = "user.B2C.card.max.qnty";
		}

		final Integer minQntry = getConfigurationService().getConfiguration().getInteger(minQntyArg, Integer.valueOf(1));


		final Integer maxQnty = getConfigurationService().getConfiguration().getInteger(maxQntyArg, Integer.valueOf(2));

		if (null != data.getProducts() && !data.getProducts().isEmpty())
		{
			for (final ProductData product : data.getProducts())
			{
				setProductMinAndMaxQty(minQntry, maxQnty, product);
			}
		}
		return data;
	}

	/**
	 * This method is used to setProductMinAndMaxQty in to product data
	 *
	 * @param minQntry
	 *           minQntry
	 * @param maxQnty
	 *           maxQnty
	 * @param product
	 *           product
	 */
	private void setProductMinAndMaxQty(final Integer minQntry, final Integer maxQnty, final ProductData product)
	{
		if (minQntry != null)
		{
			product.setMinQty(minQntry);
		}
		if (maxQnty != null)
		{
			product.setMaxQty(maxQnty);
		}
	}

	@Override
	public CategoryData getMemberPriceWithExclusion(final CategoryData categoryData,
			final List<PriceValueCustomerModel> priceValueCustomerModel)
	{
		List<Double> personasPrice = null;
		final List<ProductData> productList = new ArrayList<>();
		final List<Double> zeroPrice = new ArrayList<>();
		final double zeroValue = 0;
		zeroPrice.add(Double.valueOf(zeroValue));
		for (final ProductData product : categoryData.getProducts())
		{
			for (final PriceValueCustomerModel priceValueEntry : priceValueCustomerModel)
			{
				if (priceValueEntry.getProduct().getCode().equals(product.getSkuCode()))
				{
					personasPrice = priceExclusionFilter(product.getUnitPrices(), priceValueEntry.getPersonasBasedPriceCollection());
					personasPrice.removeAll(zeroPrice);
					product.setUnitPrices(personasPrice);
				}
			}
			productList.add(product);
		}
		categoryData.setProducts(productList);
		return categoryData;
	}

	/**
	 * This method is used to getSingleProductPrice
	 *
	 * @param categoryData
	 *           categoryData
	 * @return the CategoryData
	 */
	public CategoryData getSingleProductPrice(final CategoryData categoryData)
	{
		final List<ProductData> productList = new ArrayList<>();
		final List<Double> zeroPrice = new ArrayList<>();
		final double zeroValue = 0;
		zeroPrice.add(Double.valueOf(zeroValue));
		if (null != categoryData.getProducts() && !categoryData.getProducts().isEmpty())
		{
			for (final ProductData product : categoryData.getProducts())
			{
				if (product.getUnitPrices().size() > 1)
				{
					product.setUnitPrices(zeroPrice);
				}
				productList.add(product);
			}
			categoryData.setProducts(productList);
		}
		return categoryData;
	}

	/**
	 * This method is used to get priceExclusionFilter
	 *
	 * @param productPriceList
	 *           productPriceList
	 * @param excludedPriceList
	 *           excludedPriceList
	 * @return the double value
	 */
	public List<Double> priceExclusionFilter(final List<Double> productPriceList, final List<Integer> excludedPriceList)
	{
		final List<Double> excludedPrice = new ArrayList<>();
		final List<Double> productPriceMinusExcludedPrice = new ArrayList<>(productPriceList);
		for (final Integer price : excludedPriceList)
		{
			excludedPrice.add(Double.valueOf(price.doubleValue()));
		}
		productPriceMinusExcludedPrice.removeAll(excludedPrice);
		return productPriceMinusExcludedPrice;
	}



	/**
	 * @return the wooliesCategoryService
	 */
	public DefaultWooliesCategoryService getWooliesCategoryService()
	{
		return wooliesCategoryService;
	}

	/**
	 * @param wooliesCategoryService
	 *           the wooliesCategoryService to set
	 */
	public void setWooliesCategoryService(final DefaultWooliesCategoryService wooliesCategoryService)
	{
		this.wooliesCategoryService = wooliesCategoryService;
	}

	/**
	 * @return the wooliesCategoryPopulator
	 */
	public WooliesCategoryPopulator getWooliesCategoryPopulator()
	{
		return wooliesCategoryPopulator;
	}

	/**
	 * @param wooliesCategoryPopulator
	 *           the wooliesCategoryPopulator to set
	 */
	public void setWooliesCategoryPopulator(final WooliesCategoryPopulator wooliesCategoryPopulator)
	{
		this.wooliesCategoryPopulator = wooliesCategoryPopulator;
	}

	/**
	 * @return the configurationService
	 */
	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	/**
	 * @param configurationService
	 *           the configurationService to set
	 */
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}




}
