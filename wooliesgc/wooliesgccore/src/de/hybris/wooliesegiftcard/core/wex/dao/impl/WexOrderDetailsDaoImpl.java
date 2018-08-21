/**
 * 
 */
package de.hybris.wooliesegiftcard.core.wex.dao.impl;

import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.wooliesegiftcard.core.enums.BulkOrderStatus;
import de.hybris.wooliesegiftcard.core.job.OrderDetailsWexPerformable;
import de.hybris.wooliesegiftcard.core.model.WWBulkOrderDataModel;
import de.hybris.wooliesegiftcard.core.wex.dao.WexOrderDetailsDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author 416910
 *
 */
public class WexOrderDetailsDaoImpl extends AbstractItemDao implements WexOrderDetailsDao
{

	private static final Logger LOG = Logger.getLogger(WexOrderDetailsDaoImpl.class.getName());
	
	private FlexibleSearchService flexibleSearchService;
	private ConfigurationService configurationService;
	
	
	
	/**
	 * @return the flexibleSearchService
	 */
	public FlexibleSearchService getFlexibleSearchService()
	{
		return flexibleSearchService;
	}



	/**
	 * @param flexibleSearchService the flexibleSearchService to set
	 */
	public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}



	/**
	 * @return the configurationService
	 */
	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}



	/**
	 * @param configurationService the configurationService to set
	 */
	public void setConfigurationService(ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}



	@Override
	public List<OrderModel> getOrderDetailsForWex()
	{
		final String query = "select {pk} from {Order} where {status} IN (?status)";
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		final ArrayList al = new ArrayList<OrderStatus>();
		al.add(OrderStatus.APPROVED);
		final Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("status", al);
		fQuery.addQueryParameters(queryParams);
		fQuery.setResultClassList(Collections.singletonList(OrderModel.class));
		final SearchResult<OrderModel> searchResult = flexibleSearchService.search(fQuery);
		LOG.info("Flexi::"+fQuery.toString());
		return searchResult.getResult();
	}

}
