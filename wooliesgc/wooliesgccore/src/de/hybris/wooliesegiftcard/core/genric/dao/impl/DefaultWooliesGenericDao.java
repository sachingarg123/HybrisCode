/**
 *
 */
package de.hybris.wooliesegiftcard.core.genric.dao.impl;

import de.hybris.model.EGiftCardModel;
import de.hybris.model.PersonalisationMediaModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.wooliesegiftcard.core.enums.ImageApprovalStatus;
import de.hybris.wooliesegiftcard.core.genric.dao.WooliesGenericDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * @author 687679
 *
 *         This class is used to maintain the generic data which needs to fetch from the database
 *
 */
public class DefaultWooliesGenericDao extends AbstractItemDao implements WooliesGenericDao
{
	private static final Logger LOG = Logger.getLogger(DefaultWooliesGenericDao.class.getName());
	static final String STATUS_CHECKED_INVALID = "CHECKED_INVALID";
	static final String STATUS_GENERATEGIFTCARD = "GENERATEGIFTCARD";

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.wooliesegiftcard.dao.WooliesCustomerDao#getCountriesByName(java.lang.String) Here we are getting
	 * the countries from hybris db based on the country name
	 *
	 * @param user giving the country name
	 *
	 * @return its returns list of country models
	 *
	 */

	@Override
	public List<AddressModel> getAddressModelByAddressId(final String addressId)
	{
		final Map<String, Object> params = new HashMap<>();
		final StringBuilder query = new StringBuilder("SELECT DISTINCT {").append(ItemModel.PK).append("}");
		query.append(" from {").append(AddressModel._TYPECODE).append("} where {").append(AddressModel.PK);
		query.append("} like ?addressId");
		params.put("addressId", addressId);

		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		fQuery.addQueryParameters(params);

		fQuery.setResultClassList(Collections.singletonList(AddressModel.class));

		final SearchResult<AddressModel> searchResult = getFlexibleSearchService().search(fQuery);

		return searchResult.getResult();
	}

	/**
	 * @return List<PersonalisationMediaModel
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hybris.wooliesegiftcard.core.genric.dao.WooliesGenericDao#getImagesforDelete()
	 */
	@Override
	public List<PersonalisationMediaModel> getImagesforDelete()
	{
		final String query = "SELECT DISTINCT x.PK FROM (({{ SELECT {PK} AS PK FROM {PersonalisationMedia} WHERE "
				+ "{imageApprovalStatus} IN (?pendingStatus) AND {hasOrder} = ?hasOrder AND {creationTime} < (sysdate - INTERVAL '28' DAY) }}) "
				+ "UNION ALL "
				+ "({{ SELECT {PK} AS PK FROM {PersonalisationMedia} WHERE {imageApprovalStatus} IN (?deleteStatus) }})) AS x ";
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		final Map params = new HashMap<String, Object>();
		params.put("hasOrder", Boolean.FALSE);
		final List deleteStatus = new ArrayList<ImageApprovalStatus>();
		deleteStatus.add(ImageApprovalStatus.DELETE);
		deleteStatus.add(ImageApprovalStatus.REJECT);
		params.put("deleteStatus", deleteStatus);
		final List pendingStatus = new ArrayList<ImageApprovalStatus>();
		pendingStatus.add(ImageApprovalStatus.PENDING);
		params.put("pendingStatus", pendingStatus);
		fQuery.setResultClassList(Collections.singletonList(PersonalisationMediaModel.class));
		fQuery.addQueryParameters(params);
		final SearchResult<PersonalisationMediaModel> searchResult = getFlexibleSearchService().search(fQuery);
		return searchResult.getResult();
	}

	/**
	 * @return
	 */
	@Override
	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.wooliesegiftcard.core.genric.dao.WooliesGenericDao#getOrdersForPaymentCancellation()
	 */
	@SuppressWarnings("deprecation")
	public List<OrderModel> getOrdersForPaymentCancellation()
	{
		final Map<String, Object> params = new HashMap();
		final StringBuilder query = new StringBuilder(" SELECT {o." + OrderModel.PK + "} FROM ");
		query.append(" {" + OrderModel._TYPECODE + " AS o ");
		query.append(" JOIN OrderStatus AS os ON {o.status}={os.pk} } ");
		query.append(" WHERE {os.code}=?status");
		params.put("status", STATUS_CHECKED_INVALID);
		LOG.info("Query==" + query);
		LOG.info("Params==" + params);

		final SearchResult<OrderModel> searchRes = search(query.toString(), params);
		return searchRes.getResult();

	}


	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.wooliesegiftcard.core.genric.dao.WooliesGenericDao#getOrdersForEmailNotification()
	 */
	@Override
	public List<OrderModel> getOrdersForEmailNotification()
	{
		final Map<String, Object> params = new HashMap();
		final StringBuilder query = new StringBuilder(" SELECT {o." + OrderModel.PK + "} FROM ");
		query.append(" {" + OrderModel._TYPECODE + " AS o ");
		query.append(" JOIN OrderStatus AS os ON {o.status}={os.pk} } ");
		query.append(" WHERE {os.code}=?status");
		params.put("status", STATUS_GENERATEGIFTCARD);
		LOG.info("Query==" + query);
		LOG.info("Params==" + params);

		final SearchResult<OrderModel> searchRes = search(query.toString(), params);
		return searchRes.getResult();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.wooliesegiftcard.core.genric.dao.WooliesGenericDao#getEgiftCardDetails(java.lang.String)
	 */
	@Override
	public List<EGiftCardModel> getEgiftCardDetails(final String orderID)
	{
		final FlexibleSearchQuery query = new FlexibleSearchQuery(
				"SELECT {e.pk} FROM  {EGiftCard AS e} where {e.orderID}=?orderID");
		query.addQueryParameter("orderID", orderID);
		final SearchResult<EGiftCardModel> result = getFlexibleSearchService().search(query);
		return result.getResult();
	}

}
