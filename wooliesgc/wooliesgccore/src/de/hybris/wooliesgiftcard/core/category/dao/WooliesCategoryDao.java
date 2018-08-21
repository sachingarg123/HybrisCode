/**
 *
 */
package de.hybris.wooliesgiftcard.core.category.dao;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserGroupModel;

import java.util.Collection;
import java.util.Set;


/**
 * @author 653930 This interface is used to get the categories and products from database
 */
public interface WooliesCategoryDao
{

	public boolean isCategoryExist(String categoryId);

	public Collection<CategoryModel> getSubCategoriesByCategory(CategoryModel categoryModel, final Set<UserGroupModel> userGroups);

	public Collection<CategoryModel> getSubCategoriesByCategory(CategoryModel categoryModel);

	public Collection<ProductModel> getProductsByCategory(CategoryModel categoryModel);

	public Collection<CategoryModel> getMemberRootCategory(CatalogVersionModel catalogVersion,
			final Collection<ProductModel> products);


	public Collection<CategoryModel> getSuperCategoriesByCategory(final CatalogVersionModel catalogVersion,
			final Collection<CategoryModel> category);



}
