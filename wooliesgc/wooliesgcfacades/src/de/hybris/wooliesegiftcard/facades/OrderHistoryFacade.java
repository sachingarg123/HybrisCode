/**
 *
 */
package de.hybris.wooliesegiftcard.facades;

import de.hybris.platform.commercefacades.order.OrderFacade;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.wooliesegiftcard.exception.WoolliesOrderHistoryException;

import java.util.List;

import com.woolies.webservices.rest.user.data.OrderListData;


/**
 * @author 264343 This interface is used to maintain the order history
 */
public interface OrderHistoryFacade extends OrderFacade
{

	/**
	 * This method is used to get the getOrderDetails For OrderNo
	 *
	 * @param code
	 *           the Order code
	 * @param email
	 *           the Email of the Customer
	 * @return list of Order data
	 * @throws WoolliesOrderHistoryException
	 *            throws this exception
	 */
	List<OrderData> getOrderDetailsForOrderNo(String code, String email) throws WoolliesOrderHistoryException;

	/**
	 * This method is used to get the get OrderHistory For Range
	 *
	 * @param userId
	 *           the User ID of the Customer
	 * @param fromDate
	 *           the Date from which indexing should happen
	 * @param toDate
	 *           the Date to which indexing should happen
	 * @param startIndex
	 *           starting Index of the page
	 * @param pageSize
	 *           the page size
	 * @return OrderListData
	 * @throws WoolliesOrderHistoryException
	 *            throws this exception
	 */
	OrderListData getOrderHistoryForRange(String userId, String fromDate, String toDate, int startIndex, int pageSize)
			throws WoolliesOrderHistoryException;

	/**
	 * This method is used to get the get OrderHistory For Range And Status
	 *
	 * @param userId
	 *           the User ID of the Customer
	 * @param fromDate
	 *           the Date from which indexing should happen
	 * @param toDate
	 *           the Date to which indexing should happen
	 * @param startIndex
	 *           starting Index of the page
	 * @param pageSize
	 *           the page size
	 * @param orderStatus
	 *           getOrderHistoryForStatus
	 * @return OrderHistoryFacade OrderListData
	 */
	OrderListData getOrderHistoryForRangeAndStatus(String userId, String fromDate, String toDate, int startIndex, int pageSize,
			List<String> orderStatus);

	/**
	 * This method is used to get the get OrderHistory For Status
	 * 
	 * @param userId
	 *           the User ID of the Customer
	 * @param startIndex
	 *           starting Index of the page
	 * @param pageSize
	 *           the page size
	 * @param orderStatus
	 *           the Status of the Orders
	 * @return list of OrderListData
	 */
	OrderListData getOrderHistoryForStatus(String userId, int startIndex, int pageSize, List<String> orderStatus);

	/**
	 * This method is used to get the get OrderHistory of user
	 *
	 * @param userId
	 *           the User ID of the Customer
	 * @param startIndex
	 *           starting Index of the page
	 * @param pageSize
	 *           the page size
	 * @return list of OrderListData
	 */
	OrderListData getAllOrderHistory(String userId, int startIndex, int pageSize);

}
