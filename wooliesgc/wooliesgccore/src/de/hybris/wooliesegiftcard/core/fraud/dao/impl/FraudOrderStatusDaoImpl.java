/**
 *
 */
package de.hybris.wooliesegiftcard.core.fraud.dao.impl;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.wooliesegiftcard.core.constants.WooliesgcCoreConstants;
import de.hybris.wooliesegiftcard.core.fraud.dao.FraudOrderStatusDao;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;



/**
 * @author 669567
 *
 */
public class FraudOrderStatusDaoImpl extends AbstractItemDao implements FraudOrderStatusDao
{
	private static final Logger LOG = Logger.getLogger(FraudOrderStatusDaoImpl.class);
	private FlexibleSearchService flexibleSearchService1;
	private ConfigurationService configurationService;


	/**
	 * @return the flexibleSearchService
	 */
	@Override
	public FlexibleSearchService getFlexibleSearchService()
	{
		return flexibleSearchService1;
	}



	/**
	 * @param flexibleSearchService
	 *           the flexibleSearchService to set
	 */
	@Override
	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService1 = flexibleSearchService;
	}



	/**
	 * @return the configurationService
	 */
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


	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hybris.wooliesegiftcard.core.fraud.dao.FraudOrderStatusDao#findOrderByCodeAndStore(java.lang.String,
	 * de.hybris.platform.store.BaseStoreModel)
	 */
	@Override
	public OrderModel findOrderByCodeAndStore(final String code, final BaseStoreModel store)
	{
		final Map<String, Object> queryParams = new HashMap();
		queryParams.put("code", code);
		queryParams.put("store", store);
		LOG.info("findOrderByCodeAndStore" + queryParams);
		return getFlexibleSearchService().searchUnique(
				new FlexibleSearchQuery(WooliesgcCoreConstants.FIND_ORDERS_BY_CODE_STORE_QUERY, queryParams));

	}


}
