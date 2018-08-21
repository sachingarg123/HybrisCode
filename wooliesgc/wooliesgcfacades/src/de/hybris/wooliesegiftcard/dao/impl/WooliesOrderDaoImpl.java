/**
 *
 */
package de.hybris.wooliesegiftcard.dao.impl;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.wooliesegiftcard.dao.WooliesOrderDao;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author 648156
 *
 */

public class WooliesOrderDaoImpl implements WooliesOrderDao
{
	private static final Logger LOG = Logger.getLogger(WooliesOrderDaoImpl.class);

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

	@Autowired
	private FlexibleSearchService flexibleSearchService;


	/**
	 * This api used for get the orderdetails
	 *
	 * @param orderToken
	 * @return OrderData
	 */
	@Override
	public List<OrderModel> getOrderDetailsWithDecryptKey(final String orderToken)
	{
		List<OrderModel> orderModel = null;
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder query = new StringBuilder("SELECT DISTINCT {").append(ItemModel.PK).append("}");
		query.append(" from {").append(OrderModel._TYPECODE).append("} where {").append(OrderModel.ORDERTOKEN);
		query.append("} = ?orderToken");
		params.put("orderToken", orderToken);

		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		fQuery.addQueryParameters(params);

		fQuery.setResultClassList(Collections.singletonList(OrderModel.class));
		LOG.info("getOrderDetailsWithDecryptKey" + fQuery);
		final SearchResult<OrderModel> searchResult = flexibleSearchService.search(fQuery);
		if (searchResult != null)
		{
			orderModel = searchResult.getResult();
			return orderModel;
		}
		else
		{
			return Collections.emptyList();
		}

	}


}
