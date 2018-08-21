/**
 *
 */
package de.hybris.wooliesegiftcard.facades;

import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.commercefacades.catalog.CatalogOption;

import java.util.Set;


/**
 * @author 653930 This interface is used to get category details
 */
public interface WooilesCategoryFacade
{
	/**
	 * This method is used to get the root categories
	 *
	 * @param catalogversion
	 *           the Catalog version associated
	 * @param catalogId
	 *           the Catalog ID
	 * @param opts
	 *           the CatalogOption
	 * @return categoryModel It returns the Category Model
	 */
	CategoryModel getRootCategories(String catalogversion, String catalogId, Set<CatalogOption> opts);

}
