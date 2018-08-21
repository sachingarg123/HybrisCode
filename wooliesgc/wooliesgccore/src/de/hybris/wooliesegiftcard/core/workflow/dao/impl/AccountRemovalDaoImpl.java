/**
 *
 */
package de.hybris.wooliesegiftcard.core.workflow.dao.impl;

import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.wooliesegiftcard.core.workflow.dao.AccountRemovalDao;

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

public class AccountRemovalDaoImpl extends AbstractItemDao implements AccountRemovalDao
{
	private static final Logger LOG = Logger.getLogger(AccountRemovalDaoImpl.class);

	@Override
	public List<CustomerModel> findAllB2COlderThanSpecifiedDays(final Date oldDate)
	{
		final String query = "SELECT {pk} FROM {Customer} WHERE {loginDisabled}=?flag and {creationtime}<=?oldDate";
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		final Map<String, Object> queryParams = new HashMap();
		queryParams.put("oldDate", oldDate);
		queryParams.put("flag", Boolean.TRUE);
		fQuery.addQueryParameters(queryParams);
		fQuery.setResultClassList(Collections.singletonList(CustomerModel.class));
		LOG.info("findAllB2COlderThanSpecifiedDays" + fQuery);
		final SearchResult<CustomerModel> searchResult = getFlexibleSearchService().search(fQuery);
		return searchResult.getResult();
	}

}
