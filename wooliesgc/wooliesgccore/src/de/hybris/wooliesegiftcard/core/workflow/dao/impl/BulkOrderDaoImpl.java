/**
 *
 */
package de.hybris.wooliesegiftcard.core.workflow.dao.impl;

/**
 * @author 676313
 *
 */
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.wooliesegiftcard.core.enums.BulkOrderStatus;
import de.hybris.wooliesegiftcard.core.model.WWBulkOrderDataModel;
import de.hybris.wooliesegiftcard.core.model.WWBulkOrderItemsDataModel;
import de.hybris.wooliesegiftcard.core.workflow.dao.BulkOrderDao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * @author 676313
 *
 */
public class BulkOrderDaoImpl extends AbstractItemDao implements BulkOrderDao
{
	private FlexibleSearchService flexibleSearchService;
	private ConfigurationService configurationService;
	private static final Logger LOG = Logger.getLogger(BulkOrderDaoImpl.class);

	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}


	/**
	 * @param configurationService
	 *           the configurationService to set
	 */
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	/**
	 * @return the flexibleSearchService
	 */
	@Override
	public FlexibleSearchService getFlexibleSearchService()
	{
		return flexibleSearchService;
	}

	/**
	 * @param flexibleSearchService
	 *           the flexibleSearchService to set
	 */
	@Override
	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hybris.wooliesegiftcard.core.workflow.dao.BulkOrderDao#inprocessandsuccess()
	 */
	@Override
	public Collection<WWBulkOrderDataModel> successAndvalidationfailure()
	{

		final StringBuilder query = new StringBuilder("Select {pk} from {WWBulkOrderData} where {bulkOrderStatus} IN (?status)");
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		final ArrayList al = new ArrayList<BulkOrderStatus>();
		al.add(BulkOrderStatus.SUCCESS);
		al.add(BulkOrderStatus.VALIDATE_FAILED);
		final Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("status", al);
		fQuery.addQueryParameters(queryParams);
		fQuery.setResultClassList(Collections.singletonList(WWBulkOrderDataModel.class));
		LOG.info("get WWBulkOrderDataModel based on bulkorder status" + fQuery);
		final SearchResult<WWBulkOrderDataModel> searchResult = flexibleSearchService.search(fQuery);
		return searchResult.getResult();




	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hybris.wooliesegiftcard.core.workflow.dao.BulkOrderDao#initemsprocessandsuccess()
	 */
	@Override
	public Collection<WWBulkOrderItemsDataModel> itemsSuccessandvalidationfailure(final List<String> refnumbers)
	{
		//2018-06-07 12:18:45.391


		// YTODO Auto-generated method stub
		final String query = "Select pk from {WWBulkOrderItemsData} where {referenceNumber} IN (?refnumbers)";
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		final Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("refnumbers", refnumbers);
		fQuery.addQueryParameters(queryParams);
		fQuery.setResultClassList(Collections.singletonList(WWBulkOrderItemsDataModel.class));
		LOG.info("get WWBulkOrderItemsDataModel based on referenceNumber" + fQuery);
		final SearchResult<WWBulkOrderItemsDataModel> searchResult = flexibleSearchService.search(fQuery);
		return searchResult.getResult();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hybris.wooliesegiftcard.core.workflow.dao.BulkOrderDao#inProcessandvalidationsuccess()
	 */
	@Override
	public Collection<WWBulkOrderDataModel> inProcessandvalidationsuccess(final Date oldTimes)
	{
		final StringBuilder query = new StringBuilder(
				"Select {pk} from {WWBulkOrderData} where {bulkOrderStatus} IN  (?status) and  {modifiedtime}<=?reqtime");
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		final ArrayList al = new ArrayList<BulkOrderStatus>();
		al.add(BulkOrderStatus.IN_PROCESS);
		al.add(BulkOrderStatus.VALIDATE_SUCCESS);
		final Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("status", al);
		queryParams.put("reqtime", oldTimes);
		fQuery.addQueryParameters(queryParams);
		fQuery.setResultClassList(Collections.singletonList(WWBulkOrderDataModel.class));
		LOG.info("get WWBulkOrderDataModel based on bulkOrderStatus" + fQuery);
		final SearchResult<WWBulkOrderDataModel> searchResult = flexibleSearchService.search(fQuery);
		return searchResult.getResult();




	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hybris.wooliesegiftcard.core.workflow.dao.BulkOrderDao#itemsProcessandvalidationsuccess(java.util.List)
	 */
	@Override
	public Collection<WWBulkOrderItemsDataModel> itemsProcessandvalidationsuccess(final List<String> refnumbers,
			final Date oldTimes)
	{

		final String query = "Select pk from {WWBulkOrderItemsData} where {referenceNumber} IN (?refnumbers) and  {modifiedtime}<=?reqtime";
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		final Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("refnumbers", refnumbers);
		queryParams.put("reqtime", oldTimes);
		fQuery.addQueryParameters(queryParams);
		fQuery.setResultClassList(Collections.singletonList(WWBulkOrderItemsDataModel.class));
		LOG.info("get WWBulkOrderItemsDataModel based on referenceNumber and modifiedtime" + fQuery);
		final SearchResult<WWBulkOrderItemsDataModel> searchResult = flexibleSearchService.search(fQuery);
		return searchResult.getResult();
	}

}