/**
 *
 */
package de.hybris.wooliesegiftcard.service.impl;

import de.hybris.model.EGiftCardModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.wooliesegiftcard.dao.WooliesOrderHistoryDao;
import de.hybris.wooliesegiftcard.service.WooliesOrderHistoryService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author 653154
 *
 */
public class DefaultWooliesOrderHistoryService implements WooliesOrderHistoryService
{
	@Autowired
	private WooliesOrderHistoryDao wooliesOrderHistoryDao;

	@Override
	public List<OrderModel> getOrderDetailsForOrderNo(final String code, final UserModel user)
	{
		final List<OrderModel> orderList = wooliesOrderHistoryDao.getOrderDetailsForOrderNo(code, user);
		return orderList;
	}

	@Override
	public SearchResult<OrderModel> getOrderHistoryRange(final UserModel user, final String fromDate, final String toDate,
			final int startIndex, final int pageSize)
	{
		final SearchResult<OrderModel> orderList = wooliesOrderHistoryDao.getOrderHistoryRange(user, fromDate, toDate, startIndex,
				pageSize);
		return orderList;
	}

	@Override
	public SearchResult<OrderModel> getOrderHistoryForRangeAndStatus(final UserModel user, final String fromDate,
			final String toDate, final int startIndex, final int pageSize, final List<String> orderStatus)
	{
		final SearchResult<OrderModel> orderList = wooliesOrderHistoryDao.getOrderHistoryForRangeAndStatus(user, fromDate, toDate,
				startIndex, pageSize, orderStatus);
		return orderList;
	}

	@Override
	public SearchResult<OrderModel> getOrderHistoryForStatus(final UserModel user, final int startIndex, final int pageSize,
			final List<String> orderStatus)
	{
		final SearchResult<OrderModel> orderList = wooliesOrderHistoryDao.getOrderHistoryForStatus(user, startIndex, pageSize,
				orderStatus);
		return orderList;
	}

	@Override
	public SearchResult<OrderModel> getAllOrderHistoryForStatus(final UserModel user, final int startIndex, final int pageSize)
	{
		final SearchResult<OrderModel> orderList = wooliesOrderHistoryDao.getAllOrderHistoryForStatus(user, startIndex, pageSize);
		return orderList;
	}

	@Override
	public EGiftCardModel getEgiftCardDetails(final String eToken)
	{
		return wooliesOrderHistoryDao.getEgiftCardDetails(eToken);
	}

}
