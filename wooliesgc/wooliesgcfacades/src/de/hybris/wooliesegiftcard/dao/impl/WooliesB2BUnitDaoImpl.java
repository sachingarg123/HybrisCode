/**
 *
 */
package de.hybris.wooliesegiftcard.dao.impl;

import de.hybris.platform.b2b.model.B2BPermissionModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BCustomerModel;
import de.hybris.wooliesegiftcard.dao.WooliesB2bUnitDao;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;





/**
 * @author 648156 This clasess is used for B2B DAO activities
 *
 */
public class WooliesB2BUnitDaoImpl implements WooliesB2bUnitDao
{
	private static final Logger LOG = Logger.getLogger(WooliesB2BUnitDaoImpl.class);

	/**
	 * @return the flexibleSearchService
	 */
	public FlexibleSearchService getFlexibleSearchService()
	{
		return flexibleSearchService;
	}

	/**
	 * @param flexibleSearchService
	 *           the flexibleSearchService to set
	 */
	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}

	private FlexibleSearchService flexibleSearchService;

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.wooliesegiftcard.dao.WooliesB2bUnitDao#getB2BAdmin(java.lang.String) Here we are gitting the
	 * B2BCustomer based on uid
	 *
	 * @param user giving the b2bCustomer uid
	 *
	 * @return its returns list of b2bCustomer models
	 */
	@Override
	public List<CorporateB2BCustomerModel> getB2BAdmin(final String adminUid)
	{
		List<CorporateB2BCustomerModel> listCorporateB2BCustomerModel = null;
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder query = new StringBuilder("SELECT DISTINCT {").append(ItemModel.PK).append("}");
		query.append(" from {").append(CorporateB2BCustomerModel._TYPECODE).append("} where {")
				.append(CorporateB2BCustomerModel.UID);
		query.append("} like ?adminUid");
		params.put("adminUid", adminUid);

		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		fQuery.addQueryParameters(params);

		fQuery.setResultClassList(Collections.singletonList(CorporateB2BCustomerModel.class));
		LOG.info("CorporateB2BCustomerModel based on uid" + fQuery);
		final SearchResult<CorporateB2BCustomerModel> searchResult = flexibleSearchService.search(fQuery);
		if (searchResult != null)
		{
			listCorporateB2BCustomerModel = searchResult.getResult();
			return listCorporateB2BCustomerModel;
		}
		else
		{
			return Collections.emptyList();
		}
	}

	/*
	 * @param user giving the b2bCustomer userId
	 *
	 * @return its returns list of B2BPermissionModel models
	 */

	@Override
	public List<B2BPermissionModel> getOrderLimit(final String userId)
	{
		final StringBuilder flexBuf1 = new StringBuilder();
		final Map<String, Object> queryParams1 = new HashMap<>();
		flexBuf1.append("select {o.pk} from {B2BPermission AS o} where {o.code} = ?uid ");
		queryParams1.put("uid", userId);
		LOG.info("B2BPermissionModel based on uid" + queryParams1);
		final SearchResult<B2BPermissionModel> queryResultCustomer1 = flexibleSearchService.search(flexBuf1.toString(),
				queryParams1);
		return queryResultCustomer1.getResult();
	}

}
