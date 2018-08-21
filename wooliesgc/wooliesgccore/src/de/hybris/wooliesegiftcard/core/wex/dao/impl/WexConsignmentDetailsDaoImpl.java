/**
 *
 */
package de.hybris.wooliesegiftcard.core.wex.dao.impl;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.wooliesegiftcard.core.wex.dao.WexConsignmentDetailsDao;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author 676313
 *
 */
public class WexConsignmentDetailsDaoImpl extends AbstractItemDao implements WexConsignmentDetailsDao
{
	@Autowired
	private ModelService modelService;
	@Autowired
	private FlexibleSearchService flexibleSearchService;
	private static final Logger LOG = Logger.getLogger(WexConsignmentDetailsDaoImpl.class);

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.wooliesegiftcard.core.wex.dao.WexOrderDetailsDao#getOrderDetailsForWex()
	 */


	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.wooliesegiftcard.core.wex.dao.WexConsignmentDetailsDao#getconsignmentDetailsForWex()
	 */
	@Override
	public OrderModel getconsignmentDetailsForWex(final String invoiceNumber)
	{
		final String query = "SELECT  {pk} FROM {order} WHERE {invoiceNumber}=?invoiceNumber";
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		final Map params = new HashMap<String, Object>();
		params.put("invoiceNumber", invoiceNumber);
		fQuery.setResultClassList(Collections.singletonList(OrderModel.class));
		fQuery.addQueryParameters(params);
		LOG.info("getconsignmentDetailsForWex" + fQuery);
		final SearchResult<OrderModel> searchResult = flexibleSearchService.search(fQuery);
		if (CollectionUtils.isNotEmpty(searchResult.getResult()))
		{
			return searchResult.getResult().get(0);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.hybris.wooliesegiftcard.core.wex.dao.WexConsignmentDetailsDao#getconsignmentDetailsForWex(java.lang.String)
	 */

}
