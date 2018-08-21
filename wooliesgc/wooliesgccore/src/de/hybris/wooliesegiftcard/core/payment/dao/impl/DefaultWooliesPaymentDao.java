/**
 *
 */
package de.hybris.wooliesegiftcard.core.payment.dao.impl;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.wooliesegiftcard.core.payment.dao.WooliesPaymentDao;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * @author 653930
 *
 */
public class DefaultWooliesPaymentDao extends AbstractItemDao implements WooliesPaymentDao
{
	private static final Logger LOG = Logger.getLogger(DefaultWooliesPaymentDao.class.getName());
	static final String STATUS_CHECKED_VALID = "CHECKED_VALID";

	@Override
	public Collection<OrderModel> getOrderByStatusandVersion()
	{
		final Map<String, Object> params = new HashMap();
		final StringBuilder query = new StringBuilder(" SELECT {o." + OrderModel.PK + "} FROM ");
		query.append(" {" + OrderModel._TYPECODE + " AS o ");
		query.append(" JOIN OrderStatus AS os ON {o.status}={os.pk} } ");
		query.append(" WHERE {os.code}=?status");
		params.put("status", STATUS_CHECKED_VALID);
		LOG.info("getOrderByStatusandVersionQry==" + query);
		LOG.info("getOrderByStatusandVersionQryParam==" + params);

		final SearchResult<OrderModel> searchRes = search(query.toString(), params);
		return searchRes.getResult();
	}



}
