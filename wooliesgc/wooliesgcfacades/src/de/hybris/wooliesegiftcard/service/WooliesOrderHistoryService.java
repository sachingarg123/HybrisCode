/**
 *
 */
package de.hybris.wooliesegiftcard.service;

import de.hybris.model.EGiftCardModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.List;


/**
 * @author 653154
 *
 */
public interface WooliesOrderHistoryService
{
	/**
	 * This method is used to get the getOrderDetailsForOrderNo
	 *
	 * @param code
	 *           the parameter value to be used
	 * @param user
	 *           the parameter value to be used
	 * @return OrderModel
	 * 
	 */
	List<OrderModel> getOrderDetailsForOrderNo(final String code, final UserModel user);

	/**
	 * This method is used to get the getOrderHistoryRange of user
	 *
	 * @param user
	 *           the parameter value to be used
	 * @param fromDate
	 *           the parameter value to be used
	 * @param toDate
	 *           the parameter value to be used
	 * @param startIndex
	 *           the parameter value to be used
	 * @param pageSize
	 *           the parameter value to be used
	 * @return OrderModel
	 * 
	 */
	SearchResult<OrderModel> getOrderHistoryRange(UserModel user, String fromDate, String toDate, int startIndex, int pageSize);

	/**
	 * This method is used to get the getOrderHistoryForRangeAndStatus of user
	 *
	 * @param user
	 *           the parameter value to be used
	 * @param fromDate
	 *           the parameter value to be used
	 * @param toDate
	 *           the parameter value to be used
	 * @param startIndex
	 *           the parameter value to be used
	 * @param pageSize
	 *           the parameter value to be used
	 * @param orderStatus
	 *           the parameter value to be used
	 * @return OrderModel
	 * 
	 */

	SearchResult<OrderModel> getOrderHistoryForRangeAndStatus(UserModel user, String fromDate, String toDate, int startIndex,
			int pageSize, List<String> orderStatus);

	/**
	 * This method is used to get the getOrderHistoryForStatus of user
	 *
	 * @param user
	 *           the parameter value to be used
	 * @param startIndex
	 *           the parameter value to be used the parameter value to be used
	 * @param pageSize
	 *           the parameter value to be used
	 * @param orderStatus
	 *           the parameter value to be used
	 * @return OrderModel
	 * 
	 */

	SearchResult<OrderModel> getOrderHistoryForStatus(UserModel user, int startIndex, int pageSize, List<String> orderStatus);

	/**
	 * This method is used to get the getOrderHistoryForStatus of user
	 *
	 * @param user
	 *           the parameter value to be used
	 * @param startIndex
	 *           the parameter value to be used the parameter value to be used
	 * @param pageSize
	 *           the parameter value to be used
	 * @return OrderModel
	 * 
	 */
	SearchResult<OrderModel> getAllOrderHistoryForStatus(UserModel user, int startIndex, int pageSize);

	/**
	 * This method is used to get the getOrderHistoryForStatus of user
	 *
	 * @param eToken
	 *           the parameter value to be used
	 * 
	 * @return EGiftCardModel
	 * 
	 */
	EGiftCardModel getEgiftCardDetails(String eToken);

}
