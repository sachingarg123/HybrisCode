/**
 *
 */
package de.hybris.wooliesegiftcard.facades.order.impl;

import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.order.data.OrderStatusData;
import de.hybris.platform.commercefacades.order.impl.DefaultOrderFacade;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.wooliesegiftcard.exception.WoolliesOrderHistoryException;
import de.hybris.wooliesegiftcard.facades.OrderHistoryFacade;
import de.hybris.wooliesegiftcard.service.WooliesOrderHistoryService;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.woolies.webservices.rest.user.data.OrderListData;


/**
 * @author 264343 This class is used to maintain the order history
 */
public class OrderHistoryFacadeImpl extends DefaultOrderFacade implements OrderHistoryFacade
{
	@Autowired
	private WooliesOrderHistoryService wooliesOrderHistoryService;
	@Autowired
	private UserService userService;
	@Autowired
	private ConfigurationService configurationService;

	private static final Logger LOG = Logger.getLogger(OrderHistoryFacadeImpl.class);

	@Override
	public List<OrderData> getOrderDetailsForOrderNo(final String code, final String userId) throws WoolliesOrderHistoryException
	{
		LOG.info("getOrderDetailsForOrderNo");
		final UserModel user = userService.getUserForUID(userId);
		final List<OrderModel> orderList = wooliesOrderHistoryService.getOrderDetailsForOrderNo(code, user);
		final List<OrderData> list = new ArrayList<OrderData>();
		for (final OrderModel eachModel : orderList)
		{
			final OrderData orderData = getOrderConverter().convert(eachModel);
			list.add(orderData);
		}
		return list;
	}

	@Override
	public OrderListData getOrderHistoryForRange(final String userId, final String fromDate, final String toDate,
			final int startIndex, final int pageSize) throws WoolliesOrderHistoryException
	{
		LOG.info("getOrderHistoryForRange");
		final UserModel user = userService.getUserForUID(userId);
		final SearchResult<OrderModel> orderList = wooliesOrderHistoryService.getOrderHistoryRange(user, fromDate, toDate,
				startIndex, pageSize);
		final List<OrderModel> resultSet = new ArrayList(orderList.getResult());
		final List<OrderData> list = new ArrayList<OrderData>();
		for (final OrderModel eachModel : resultSet)
		{
			final OrderData orderData = getOrderConverter().convert(eachModel);
			list.add(orderData);
		}
		final OrderListData orderListData = populateOrderData(startIndex, pageSize, orderList, list);
		return orderListData;
	}

	@Override
	public OrderListData getOrderHistoryForRangeAndStatus(final String userId, final String fromDate, final String toDate,
			final int startIndex, final int pageSize, final List<String> orderStatus)
	{
		LOG.info("getOrderHistoryForRangeAndStatus");
		final UserModel user = userService.getUserForUID(userId);
		final SearchResult<OrderModel> orderList = wooliesOrderHistoryService.getOrderHistoryForRangeAndStatus(user, fromDate,
				toDate, startIndex, pageSize, orderStatus);
		final List<OrderModel> resultSet = new ArrayList(orderList.getResult());
		final List<OrderData> list = new ArrayList<OrderData>();
		for (final OrderModel eachModel : resultSet)
		{
			final OrderData orderData = getOrderConverter().convert(eachModel);
			list.add(orderData);
		}
		final OrderListData orderListData = populateOrderData(startIndex, pageSize, orderList, list);
		return orderListData;
	}

	@Override
	public OrderListData getOrderHistoryForStatus(final String userId, final int startIndex, final int pageSize,
			final List<String> orderStatus)
	{
		LOG.info("getOrderHistoryForStatus");
		final UserModel user = userService.getUserForUID(userId);
		final SearchResult<OrderModel> orderList = wooliesOrderHistoryService.getOrderHistoryForStatus(user, startIndex, pageSize,
				orderStatus);
		final List<OrderModel> resultSet = new ArrayList(orderList.getResult());
		final List<OrderData> list = new ArrayList<OrderData>();
		for (final OrderModel eachModel : resultSet)
		{
			final OrderData orderData = getOrderConverter().convert(eachModel);
			list.add(orderData);
		}
		final OrderListData orderListData = populateOrderData(startIndex, pageSize, orderList, list);
		return orderListData;
	}



	@Override
	public OrderListData getAllOrderHistory(final String userId, final int startIndex, final int pageSize)
	{
		LOG.info("getAllOrderHistory");
		final UserModel user = userService.getUserForUID(userId);
		final SearchResult<OrderModel> orderList = wooliesOrderHistoryService.getAllOrderHistoryForStatus(user, startIndex,
				pageSize);
		final List<OrderModel> resultSet = new ArrayList(orderList.getResult());
		final List<OrderData> list = new ArrayList<OrderData>();
		for (final OrderModel eachModel : resultSet)
		{
			final OrderData orderData = getOrderConverter().convert(eachModel);
			list.add(orderData);
		}
		final OrderListData orderListData = populateOrderData(startIndex, pageSize, orderList, list);
		return orderListData;
	}

	/**
	 * @param startIndex
	 * @param pageSize
	 * @param orderList
	 * @param list
	 * @return
	 */
	private OrderListData populateOrderData(final int startIndex, final int pageSize, final SearchResult<OrderModel> orderList,
			final List<OrderData> list)
	{
		final OrderListData orderListData = new OrderListData();
		final OrderStatusData orderStatusData = new OrderStatusData();
		orderListData.setOrders(list);
		orderListData.setTotalCount(orderList.getTotalCount());
		orderListData.setCurrentIndex(startIndex + Math.min(pageSize, orderList.getTotalCount()));
		final ArrayList<OrderStatusData> listOrderStatus = new ArrayList<OrderStatusData>();
		for (final OrderData orderData : list)
		{

			if (null != orderData.getOrderStatus()
					&& null != configurationService.getConfiguration().getString(orderData.getOrderStatus()))
			{
				orderStatusData.setName(configurationService.getConfiguration().getString(orderData.getOrderStatus()));
				orderStatusData.setOrderStatusId(orderData.getOrderStatus());
			}
			listOrderStatus.add(orderStatusData);
		}
		orderListData.setOrderStatuses(listOrderStatus);
		return orderListData;
	}


}
