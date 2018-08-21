/**
 *
 */
package de.hybris.wooliesegiftcard.dao;

import de.hybris.model.EGiftCardModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.List;


/**
 * @author 653154
 *
 */
public interface WooliesOrderHistoryDao
{
	List<OrderModel> getOrderDetailsForOrderNo(final String code, final UserModel user);

	SearchResult<OrderModel> getOrderHistoryRange(UserModel user, String fromDate, String toDate, int startIndex, int pageSize);

	SearchResult<OrderModel> getOrderHistoryForStatus(UserModel user, int startIndex, int pageSize, List<String> orderStatus);

	SearchResult<OrderModel> getAllOrderHistoryForStatus(UserModel user, int startIndex, int pageSize);

	SearchResult<OrderModel> getOrderHistoryForRangeAndStatus(UserModel user, String fromDate, String toDate, int startIndex,
			int pageSize, List<String> orderStatus);

	EGiftCardModel getEgiftCardDetails(final String eToken);
}
