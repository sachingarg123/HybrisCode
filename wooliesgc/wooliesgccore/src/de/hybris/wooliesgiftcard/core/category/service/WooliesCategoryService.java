/**
 *
 */
package de.hybris.wooliesgiftcard.core.category.service;

import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.product.ProductModel;

import java.util.List;


/**
 * @author 653930 This interface is used for category service
 */
public interface WooliesCategoryService
{
	/**
	 * This method is used to get sub categories for the given category and user id
	 *
	 * @param categoryId
	 *           Used to get sub-category for this Category ID
	 * @param userId
	 *           Gets the sub-category for User-ID
	 * @return subcategory returns the sub-category
	 */
	CategoryModel getSubCategoriesByCategory(String categoryId, String userId);


	/**
	 * This method is used to get List of Products for the given category
	 *
	 * @param category
	 *           returns the List of Product Models for this CategoryModel
	 * @return List<ProductModel> returns the List of Product Models
	 */

	public List<ProductModel> getProductsByCategory(CategoryModel category);


	/**
	 * This method is used to find root categories for the given catalogversion and catalog id
	 *
	 * @param catalogVersionName
	 *           the catalogVersionName
	 * @param catalogId
	 *           finds the Category by Catalog version for the catalogId
	 * @return CategoryModel by this Method
	 */
	CategoryModel findRootCategoriesByCatalogVersion(String catalogVersionName, String catalogId);

	/**
	 * This method is used to find root categories for the given catalogversion , catalog id and memberCtegory
	 *
	 * @param catalogVersionName
	 *           the catalogVersionName
	 * @param catalogId
	 *           finds the Category by Catalog version for the catalogId
	 * @param memberCtegory
	 *           Member category used as a parameter
	 * @return CategoryModel by this Method
	 */

	public CategoryModel findMemberRootCategoriesByCatalogVersion(final String catalogVersionName, final String catalogId,
			final String memberCtegory);


	/**
	 * This method is used to find root categories for the given catalogversion , catalog id , memberCtegory and
	 * findMemberCategoriesandProduct
	 *
	 * @param catalogVersionName
	 *           the catalogVersionName
	 * @param catalogId
	 *           finds the Category by Catalog version for the catalogId
	 * @param categoryId
	 *           categoryID used as a parameter
	 *
	 * @param findMemberCategoriesandProduct
	 *           findMemberCategoriesandProduct used as a parameter
	 *
	 * @return CategoryModel by this Method
	 */

	public CategoryModel findMemberCategoriesandProduct(final String catalogVersionName, final String catalogId,
			final String findMemberCategoriesandProduct, String categoryId);





}
