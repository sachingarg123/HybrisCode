/**
 *
 */
package de.hybris.wooliesegiftcard.dao.impl;

import de.hybris.model.EGiftCardModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.wooliesegiftcard.dao.WooliesOrderHistoryDao;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author 653154
 *
 */
public class DefaultWooliesOrderHistoryDao implements WooliesOrderHistoryDao
{
	@Autowired
	private FlexibleSearchService flexibleSearchService;
	private final static String ORDER_STATUS = "orderStatus";
	private static final Logger LOG = Logger.getLogger(DefaultWooliesOrderHistoryDao.class);

	@Override
	public List<OrderModel> getOrderDetailsForOrderNo(final String code, final UserModel user)
	{
		final String query = "SELECT {pk} FROM {order} WHERE {code} = ?code AND {user} = ?user";
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		final Map params = new HashMap<String, Object>();
		params.put("code", code);
		params.put("user", user);
		fQuery.setResultClassList(Collections.singletonList(OrderModel.class));
		fQuery.addQueryParameters(params);
		LOG.info("get Order model based on code and user" + fQuery);
		final SearchResult<OrderModel> searchResult = flexibleSearchService.search(fQuery);
		return searchResult.getResult();
	}

	@Override
	public SearchResult<OrderModel> getOrderHistoryRange(final UserModel users, final String fromDates, final String toDates,
			final int startIndexs, final int pageSizes)
	{
		final long fDate1 = Long.parseLong(fromDates);
		final long tDate1 = Long.parseLong(toDates);
		final Date date1 = new Date(fDate1);
		final Date date2 = new Date(tDate1);
		final DateFormat formats = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		final String formattedFromDate1 = formats.format(date1);
		final String formattedToDate1 = formats.format(date2);

		final String query = "SELECT {pk} FROM {order} WHERE {user} = ?users AND {creationTime} >= ?fDate "
				+ "AND {creationTime} <= ?tDate ORDER BY {creationTime} DESC";
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		fQuery.setStart(startIndexs);
		fQuery.setCount(pageSizes);
		fQuery.setNeedTotal(true);
		final Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("users", users);
		queryParams.put("fDate", formattedFromDate1);
		queryParams.put("tDate", formattedToDate1);
		fQuery.getQueryParameters().putAll(queryParams);
		final SearchResult<OrderModel> searchResult = flexibleSearchService.search(fQuery);
		return searchResult;
	}
	/**
	 * @param fromDate
	 * @return formattedDate
	 */
	private String formatDate(final String fromDate)
	{
		final long fDate = Long.parseLong(fromDate);
		final Date date = new Date(fDate);
		final DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		final String formattedDate = format.format(date);
		return formattedDate;
	}

	@Override
	public SearchResult<OrderModel> getOrderHistoryForRangeAndStatus(final UserModel user, final String fromDate,
			final String toDate, final int startIndex, final int pageSize, final List<String> orderStatus)
	{
		final List status = getOrderStatus(orderStatus).getResult();
		final long fDate = Long.parseLong(fromDate);
		final long tDate = Long.parseLong(toDate);
		final Date date3 = new Date(fDate);
		final Date date4 = new Date(tDate);
		final DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		final String formattedFromDate = format.format(date3);
		final String formattedToDate = format.format(date4);
		final String query = "SELECT {pk} FROM {order} WHERE {user} = ?user AND {creationTime} >= ?fromDate "
				+ "AND {creationTime} <= ?toDate AND {status} IN (?orderStatus) ORDER BY {creationTime} DESC";
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		fQuery.setStart(startIndex);
		fQuery.setCount(pageSize);
		fQuery.setNeedTotal(true);
		final Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("user", user);
		queryParams.put("fromDate", formattedFromDate);
		queryParams.put("toDate", formattedToDate);
		queryParams.put("orderStatus", status);
		fQuery.getQueryParameters().putAll(queryParams);
		final SearchResult<OrderModel> searchResult = flexibleSearchService.search(fQuery);
		return searchResult;
	}

	@Override
	public SearchResult<OrderModel> getOrderHistoryForStatus(final UserModel user, final int startIndex, final int pageSize,
			final List<String> orderStatus)
	{
		final List status = getOrderStatus(orderStatus).getResult();
		final String query = "SELECT {pk} FROM {order} WHERE {user} = ?user AND {status} IN (?orderStatus) ORDER BY {creationTime} DESC";
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		fQuery.setStart(startIndex);
		fQuery.setCount(pageSize);
		fQuery.setNeedTotal(true);
		final Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("user", user);
		queryParams.put(ORDER_STATUS, status);
		fQuery.getQueryParameters().putAll(queryParams);
		LOG.info("getOrderHistoryForStatus against user" + fQuery);
		return flexibleSearchService.search(fQuery);
	}

	@Override
	public SearchResult<OrderModel> getAllOrderHistoryForStatus(final UserModel user, final int startIndex, final int pageSize)
	{
		final String query = "SELECT {pk} FROM {order} WHERE {user} = ?user ORDER BY {creationTime} DESC";
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		fQuery.setStart(startIndex);
		fQuery.setCount(pageSize);
		fQuery.setNeedTotal(true);
		final Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("user", user);
		fQuery.getQueryParameters().putAll(queryParams);
		LOG.info("getAllOrderHistoryForStatus against user" + fQuery);
		return flexibleSearchService.search(fQuery);
	}

	public SearchResult getOrderStatus(final List<String> orderStatus)
	{
		final String query = "SELECT {pk} FROM {orderstatus} WHERE {code} IN (?orderStatus)";
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		final Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put(ORDER_STATUS, orderStatus);
		fQuery.getQueryParameters().putAll(queryParams);
		LOG.info("getOrderStatus" + fQuery);
		return flexibleSearchService.search(fQuery);
	}

	@Override
	public EGiftCardModel getEgiftCardDetails(final String eToken)
	{
		final String query = "SELECT {pk} FROM {EGiftCard} WHERE {giftCardToken} = ?token";
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		final Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("token", eToken);
		fQuery.getQueryParameters().putAll(queryParams);
		LOG.info("getEgiftCardDetails" + fQuery);
		final SearchResult<EGiftCardModel> searchResult = flexibleSearchService.search(fQuery);
		if (CollectionUtils.isNotEmpty(searchResult.getResult()))
		{
			return searchResult.getResult().get(0);
		}
		return null;
	}







}
