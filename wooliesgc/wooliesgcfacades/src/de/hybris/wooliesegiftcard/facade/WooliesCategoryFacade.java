/**
 *
 */
package de.hybris.wooliesegiftcard.facade;

import de.hybris.platform.commercefacades.product.data.CategoryData;
import de.hybris.wooliesegiftcard.core.model.PriceValueCustomerModel;

import java.util.List;


/**
 * @author 653930 This interface is maintain the category details
 */
public interface WooliesCategoryFacade
{
	/**
	 * This method is used to get the root categories based on the catalog id and version
	 *
	 * @param catalogId
	 *           categoryID used as a parameter
	 * @param catalogVersionId
	 *           the ID for catalog version
	 * @return it returns the CategoryData
	 */
	CategoryData getRootCategories(String catalogId, String catalogVersionId);

	/**
	 * This method is used to get the sub categories for the give category id and user id
	 *
	 * @param categoryId
	 *           categoryID used as a parameter
	 * @param userId
	 *           the user ID as a parameter
	 * @return it returns the CategoryData
	 */
	CategoryData getSubCategories(String categoryId, String userId);

	/**
	 * This method is used to get the getMemberPriceWithExclusion
	 *
	 * @param categoryData
	 *           the CategoryData as a parameter
	 * @param priceValueCustomerModel
	 *           it takes list of List<PriceValueCustomerModel> as parameter
	 * @return it returns the CategoryData
	 */
	public CategoryData getMemberPriceWithExclusion(CategoryData categoryData,
			List<PriceValueCustomerModel> priceValueCustomerModel);

	/**
	 * This method is used to get the MemberRootCategories
	 *
	 * @param catalogId
	 *           categoryID used as a parameter
	 * @param catalogVersionId
	 *           the ID for catalog version
	 * @param memberCtegory
	 *           the Member Category to which it belongs
	 * @return it returns the CategoryData
	 */
	public CategoryData getMemberRootCategories(final String catalogId, final String catalogVersionId, final String memberCtegory);

}
