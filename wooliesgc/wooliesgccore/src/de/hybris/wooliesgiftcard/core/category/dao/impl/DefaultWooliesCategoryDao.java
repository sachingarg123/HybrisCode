/**
 *
 */
package de.hybris.wooliesgiftcard.core.category.dao.impl;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.daos.impl.DefaultCategoryDao;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.wooliesgiftcard.core.category.dao.WooliesCategoryDao;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;


/**
 * @author 653930 This class is used to get the categories and products
 */
public class DefaultWooliesCategoryDao extends DefaultCategoryDao implements WooliesCategoryDao
{
	private static final Logger LOG = Logger.getLogger(DefaultWooliesCategoryDao.class);

	/**
	 * This method is used to check whether category exist or not
	 *
	 * @param categoryId
	 * @return category exist or not
	 */
	@Override
	public boolean isCategoryExist(final String categoryId)
	{
		boolean isExist = false;
		final Collection<CategoryModel> categories = findCategoriesByCode(categoryId);
		if (!categories.isEmpty())
		{
			isExist = true;
		}
		return isExist;
	}

	/**
	 * This method is used to get sub categories of the give category
	 *
	 * @param categoryModel
	 * @return sub catergories
	 */
	@Override
	public Collection<CategoryModel> getSubCategoriesByCategory(final CategoryModel categoryModel,
			final Set<UserGroupModel> userGroups)
	{
		final Map<String, Object> params = new HashMap<>();
		final StringBuilder query = new StringBuilder(" SELECT {sub." + CategoryModel.PK + "} ");
		query.append(" FROM {" + CategoryModel._TYPECODE + " AS sub ");
		query.append(" JOIN " + CategoryModel._CATEGORYCATEGORYRELATION + " AS rel ON {sub.PK}={rel.target}  ");
		query.append("	JOIN Category2PrincipalRelation ").append(" AS cpl ");
		query.append(" ON {cpl:source}={sub:pk} }");
		query.append(" WHERE {rel.source} = ?category");
		query.append(" AND {sub." + CategoryModel.CATALOGVERSION + "} = ?catalogversion");
		query.append(" AND {cpl.target} IN ( ?userGroups )");
		params.put("category", categoryModel.getPk());
		params.put("catalogversion", categoryModel.getCatalogVersion());
		params.put("userGroups", userGroups);
		LOG.info("GetMemberRootCategory==" + query + "Params==" + params);
		final SearchResult<CategoryModel> searchRes = search(query.toString(), params);
		return searchRes.getResult();
	}

	/**
	 * This method is used to get products for the given category based on user groups
	 *
	 * @param categoryModel
	 * @param userGroups
	 * @return products
	 */
	@Override
	public Collection<CategoryModel> getSubCategoriesByCategory(final CategoryModel categoryModel)
	{
		final Map<String, Object> params = new HashMap<>();
		final StringBuilder query = new StringBuilder(" SELECT {sub." + CategoryModel.PK + "} ");
		query.append(" FROM {" + CategoryModel._TYPECODE + " AS sub ");
		query.append(" JOIN " + CategoryModel._CATEGORYCATEGORYRELATION + " AS rel ON {sub.PK}={rel.target} } ");
		query.append(" WHERE {rel.source} = ?category");
		query.append(" AND {sub." + CategoryModel.CATALOGVERSION + "} = ?catalogversion");
		params.put("category", categoryModel.getPk());
		params.put("catalogversion", categoryModel.getCatalogVersion());
		LOG.info("GetMemberRootCategory==" + query + "Params==" + params);
		final SearchResult<CategoryModel> searchRes = search(query.toString(), params);
		return searchRes.getResult();
	}

	@Override
	public Collection<ProductModel> getProductsByCategory(final CategoryModel categoryModel)
	{
		final Map<String, Object> params = new HashMap<>();
		final StringBuilder queryString = new StringBuilder();

		queryString.append("SELECT {p:").append(ProductModel.PK).append("} ");
		queryString.append("	FROM {").append(ProductModel._TYPECODE).append(" AS p ");
		queryString.append("	JOIN CATEGORYPRODUCTRELATION ").append(" AS l ");
		queryString.append(" ON {l:target}={p:pk}}");
		queryString.append("	WHERE {l:source}");
		queryString.append(" IN ( ?cat ) ");
		params.put("cat", categoryModel);
		LOG.info("GetProductsByCategoryQry==" + queryString + "Param==" + params);
		final SearchResult<ProductModel> searchRes = search(queryString.toString(), params);
		return searchRes.getResult();
	}

	@Override
	public Collection<CategoryModel> getMemberRootCategory(final CatalogVersionModel catalogVersion,
			final Collection<ProductModel> products)
	{
		final Map<String, Object> params = new HashMap<>();
		final StringBuilder queryString = new StringBuilder();

		queryString.append("SELECT {cat:").append(CategoryModel.PK).append("} ");
		queryString.append("	FROM {").append(CategoryModel._TYPECODE).append(" AS cat");
		queryString.append("	JOIN Category2PrincipalRelation ").append(" AS cpl ");
		queryString.append(" ON {cpl:source}={cat:pk}}");
		queryString.append(" WHERE {cat:" + CategoryModel.CATALOGVERSION + "} = ?catalogversion");
		queryString.append(" AND {cpl:target} IN ( ?userGroups )");
		params.put("catalogversion", catalogVersion);
		LOG.info("GetMemberRootCategory==" + queryString + "Param==" + params);
		final SearchResult<CategoryModel> searchRes = search(queryString.toString(), params);
		return searchRes.getResult();
	}

	@Override
	public Collection<CategoryModel> getSuperCategoriesByCategory(final CatalogVersionModel catalogVersion,
			final Collection<CategoryModel> categories)
	{
		final Map<String, Object> params = new HashMap<>();
		final StringBuilder queryString = new StringBuilder();

		queryString.append("SELECT DISTINCT({super:").append(CategoryModel.PK).append("}) ");
		queryString.append("	FROM {").append(CategoryModel._TYPECODE).append(" AS super");
		queryString.append("	JOIN CATEGORYCATEGORYRELATION ").append(" AS rel ");
		queryString.append(" ON {super.PK}={rel.source}}");
		queryString.append(" WHERE {super:" + CategoryModel.CATALOGVERSION + "} = ?catalogversion");
		queryString.append(" AND {rel:target} IN ( ?categories )");
		params.put("catalogversion", catalogVersion);
		params.put("categories", categories);
		LOG.info("GetSuperCategoriesByCategory==" + queryString + "Param==" + params);
		final SearchResult<CategoryModel> searchRes = search(queryString.toString(), params);
		return searchRes.getResult();
	}
}
