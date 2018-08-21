/**
 *
 */
package de.hybris.wooliesgiftcard.core.category.service.impl;

import de.hybris.platform.catalog.impl.DefaultCatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.CategoryService;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.commercefacades.customergroups.CustomerGroupFacade;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.wooliesgiftcard.core.category.dao.impl.DefaultWooliesCategoryDao;
import de.hybris.wooliesgiftcard.core.category.service.WooliesCategoryService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;


/**
 * @author 653930 This class is a default category service
 */
public class DefaultWooliesCategoryService implements WooliesCategoryService
{
	private static final String ERRCODE_CATEGORY_NOT_ASSOCIATED = "ERR_40026";
	private static final String CUSTOMERGROUP = "customergroup";
	private static final String CARD_CLASSIFICATION_TYPE = "TYPE";
	private static final String MEMBER_PREFIX = "MEM";
	private DefaultWooliesCategoryDao wooliesCategoryDao;
	private DefaultCatalogVersionService catalogVersionService;
	private CategoryService categoryService;
	ModelService modelService;
	@Resource(name = "userService")
	private UserService userService;
	@Resource(name = "customerGroupFacade")
	private CustomerGroupFacade customerGroupFacade;

	/**
	 * This method is used to get sub categories for the given category and user id
	 *
	 * @param categoryId
	 * @param userId
	 * @return subcategory
	 */
	@Override
	public CategoryModel getSubCategoriesByCategory(final String categoryId, final String userId)
	{
		Collection<CategoryModel> categories = null;
		final List<CategoryModel> allcategories = new ArrayList<>();
		List<ProductModel> products = null;
		final CategoryModel category = getCategoryService().getCategoryForCode(categoryId);
		Set<UserGroupModel> userGroups = new HashSet();
		final Collection<CategoryModel> categoryList = getWooliesCategoryDao().findCategoriesByCode(categoryId);
		if (categoryList != null && !categoryList.isEmpty())
		{
			if (null != userId && !userId.isEmpty())
			{
				final UserModel user = userService.getUserForUID(userId);
				userGroups = userService.getAllUserGroupsForUser(user);
			}
			else
			{
				userGroups.add(userService.getUserGroupForUID(CUSTOMERGROUP));
			}
			categories = getWooliesCategoryDao().getSubCategoriesByCategory(categoryList.iterator().next(), userGroups);
			products = getProductsByCategory(categoryList.iterator().next());
		}
		setCategoriesandProduct(category, categories, products, allcategories);
		return category;
	}

	void setCategoriesandProduct(final CategoryModel category, final Collection<CategoryModel> categories,
			final List<ProductModel> products, final List<CategoryModel> allcategories)
	{

		if (CARD_CLASSIFICATION_TYPE.equalsIgnoreCase(category.getClassification()))
		{
			if (categories != null && !categories.isEmpty())
			{
				for (final CategoryModel categoryForProduct : categories)
					{
						final List<ProductModel> allproducts = getProductsByCategory(categoryForProduct);
setAllCategories(allcategories, categoryForProduct, allproducts);
					}
			}
			category.setCategories(new ArrayList<CategoryModel>(allcategories));
			if (null != products && !products.isEmpty())
			{
				category.setProducts(products);
			}

		}
		else
		{
			setCategoriesClassification(category, categories, products);
		}
	}

	/**
	 * this is used to set all categories
	 * 
	 * @param allcategories
	 * @param categoryForProduct
	 * @param allproducts
	 */
	private void setAllCategories(final List<CategoryModel> allcategories, final CategoryModel categoryForProduct,
			final List<ProductModel> allproducts)
	{
		if (null != allproducts && !allproducts.isEmpty())
		{
			allcategories.add(categoryForProduct);
		}
	}
	
	void setCategoriesClassification(final CategoryModel category, final Collection<CategoryModel> categories,
			final List<ProductModel> products)
	{
		if (null != categories && !categories.isEmpty())
		{
			category.setCategories(new ArrayList<CategoryModel>(categories));
		}
		else
		{
			category.setCategories(null);
		}
		if (null != products && !products.isEmpty())
		{
			category.setProducts(products);
		}
		else
		{
			category.setProducts(null);
		}
	}


	@Override
	public List<ProductModel> getProductsByCategory(final CategoryModel category)
	{
		Collection<ProductModel> products = new ArrayList<>();
		final Collection<CategoryModel> categoryList = getWooliesCategoryDao().findCategoriesByCode(category.getCode());
		if (categoryList != null && !categoryList.isEmpty())
		{
			products = getWooliesCategoryDao().getProductsByCategory(categoryList.iterator().next());
		}

		return new ArrayList<>(products);
	}

	/**
	 * This method is used to find root categories for the given catalogversion and catalog id
	 *
	 * @param catalogVersionName
	 * @param catalogId
	 * @return
	 */
	@Override
	public CategoryModel findRootCategoriesByCatalogVersion(final String catalogVersionName, final String catalogId)
	{
		final CategoryModel category = getModelService().create(CategoryModel.class);
		final List<CategoryModel> rootCategories = new ArrayList<>();
		final CatalogVersionModel catalogVersion = getCatalogVersionService().getCatalogVersion(catalogId, catalogVersionName);
		final Collection<CategoryModel> categoriesCollection = getWooliesCategoryDao()
				.findRootCategoriesByCatalogVersion(catalogVersion);
		final Iterator<CategoryModel> itr = categoriesCollection.iterator();
		while (itr.hasNext())
		{
			final CategoryModel elem = itr.next();
			if (elem.getCode().indexOf(MEMBER_PREFIX) < 0)
			{
				rootCategories.add(elem);
			}
		}
		category.setCategories(rootCategories);
		return category;
	}

	@Override
	public CategoryModel findMemberRootCategoriesByCatalogVersion(final String catalogVersionName, final String catalogId,
			final String memberCtegory)
	{
		final CategoryModel category = getModelService().create(CategoryModel.class);
		Collection<CategoryModel> memberRootCategories = null;
		final Collection<CategoryModel> categories = findCategorieswithOutMemCategoryByProduct(memberCtegory, catalogVersionName,
				catalogId);
		final CatalogVersionModel catalogVersion = getCatalogVersionService().getCatalogVersion(catalogId, catalogVersionName);
		Collection<CategoryModel> superCategories = getWooliesCategoryDao().getSuperCategoriesByCategory(catalogVersion,
				categories);
		do
		{
			memberRootCategories = new HashSet<>();
			memberRootCategories.addAll(superCategories);
			superCategories = getWooliesCategoryDao().getSuperCategoriesByCategory(catalogVersion, superCategories);
		}
		while (null != superCategories && !superCategories.isEmpty());
		category.setCategories(new ArrayList<CategoryModel>(memberRootCategories));
		return category;
	}

	final Collection<CategoryModel> findCategorieswithOutMemCategoryByProduct(final String memberCtegory,
			final String catalogVersionName, final String catalogId)
	{
		Collection<ProductModel> products = new ArrayList<>();
		final Collection<CategoryModel> superCategoriesByProduct = new HashSet<>();
		final Collection<CategoryModel> afetrDelMemCategories = new HashSet<>();
		final Collection<CategoryModel> categoryModel = getWooliesCategoryDao().findCategoriesByCode(memberCtegory);
		if (null != categoryModel && !categoryModel.isEmpty())
		{
			products = getWooliesCategoryDao().getProductsByCategory(categoryModel.iterator().next());
		}
		final CatalogVersionModel catalogVersion = getCatalogVersionService().getCatalogVersion(catalogId, catalogVersionName);
		for (final ProductModel product : products)
		{
			superCategoriesByProduct
					.addAll(getWooliesCategoryDao().findCategoriesByCatalogVersionAndProduct(catalogVersion, product));
		}


		final Iterator<CategoryModel> itr = superCategoriesByProduct.iterator();
		while (itr.hasNext())
		{
			final CategoryModel elem = itr.next();
			if (elem.getCode().indexOf(MEMBER_PREFIX) < 0)
			{
				afetrDelMemCategories.add(elem);
			}
		}
		return afetrDelMemCategories;
	}

	@Override
	public CategoryModel findMemberCategoriesandProduct(final String catalogVersionName, final String catalogId,
			final String memberCategory, final String categoryId)
	{

		final Collection<CategoryModel> memberCategories = new HashSet<>();
		List<CategoryModel> superCategories = new ArrayList<>(
				findCategorieswithOutMemCategoryByProduct(memberCategory, catalogVersionName, catalogId));
		final Collection<CategoryModel> userInputCategory = getWooliesCategoryDao().findCategoriesByCode(categoryId);
		final CategoryModel category = getCategoryService().getCategoryForCode(categoryId);
		final CatalogVersionModel catalogVersion = getCatalogVersionService().getCatalogVersion(catalogId, catalogVersionName);
		category.setCategories(new ArrayList<CategoryModel>(userInputCategory));
		final List<CategoryModel> subCategories = new ArrayList<>(
				getWooliesCategoryDao().getSubCategoriesByCategory(userInputCategory.iterator().next()));
		List<CategoryModel> filteredCategory = null;
		if (!subCategories.isEmpty())
		{
			while (null == filteredCategory || filteredCategory.isEmpty())
			{
				filteredCategory = superCategories.stream()
						.filter(superCat -> subCategories.stream().anyMatch(subCat -> subCat.getCode().equals(superCat.getCode())))
						.collect(Collectors.toList());

				if (null == filteredCategory || filteredCategory.isEmpty())
				{
					superCategories = new ArrayList<>(
							getWooliesCategoryDao().getSuperCategoriesByCategory(catalogVersion, superCategories));
				}
			}
			memberCategories.addAll(filteredCategory);
			category.setCategories(new ArrayList<CategoryModel>(memberCategories));
		}

		else
		{
			final Collection<ProductModel> memberProducts = new HashSet<>();
			final List<CategoryModel> memCategoryModel = new ArrayList<>(
					getWooliesCategoryDao().findCategoriesByCode(memberCategory));
			final List<ProductModel> products = new ArrayList<>(
					getWooliesCategoryDao().getProductsByCategory(userInputCategory.iterator().next()));

			final List<ProductModel> memProducts = new ArrayList<>(
					getWooliesCategoryDao().getProductsByCategory(memCategoryModel.iterator().next()));

			final List<ProductModel> filteredProduct = products.stream()
					.filter(product -> memProducts.stream().anyMatch(memProd -> memProd.getCode().equals(product.getCode())))
					.collect(Collectors.toList());
			if (filteredProduct.isEmpty())
			{
				throw new UnknownIdentifierException(ERRCODE_CATEGORY_NOT_ASSOCIATED);
			}
			memberProducts.addAll(filteredProduct);
			category.setCategories(new ArrayList<CategoryModel>(userInputCategory));
			category.setProducts(new ArrayList<ProductModel>(memberProducts));
		}
		return category;

	}

	/**
	 * @return the wooliesCategoryDao
	 */
	public DefaultWooliesCategoryDao getWooliesCategoryDao()
	{
		return wooliesCategoryDao;
	}

	/**
	 * @param wooliesCategoryDao
	 *           the wooliesCategoryDao to set
	 */
	public void setWooliesCategoryDao(final DefaultWooliesCategoryDao wooliesCategoryDao)
	{
		this.wooliesCategoryDao = wooliesCategoryDao;
	}

	/**
	 * @return the catalogVersionService
	 */
	public DefaultCatalogVersionService getCatalogVersionService()
	{
		return catalogVersionService;
	}

	/**
	 * @param catalogVersionService
	 *           the catalogVersionService to set
	 */
	public void setCatalogVersionService(final DefaultCatalogVersionService catalogVersionService)
	{
		this.catalogVersionService = catalogVersionService;
	}

	/**
	 * @return the categoryService
	 */
	public CategoryService getCategoryService()
	{
		return categoryService;
	}

	/**
	 * @param categoryService
	 *           the categoryService to set
	 */
	public void setCategoryService(final CategoryService categoryService)
	{
		this.categoryService = categoryService;
	}

	/**
	 * @return the modelService
	 */
	public ModelService getModelService()
	{
		return modelService;
	}

	/**
	 * @param modelService
	 *           the modelService to set
	 */
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

}
