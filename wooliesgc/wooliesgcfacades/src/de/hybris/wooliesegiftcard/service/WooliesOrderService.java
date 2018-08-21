/**
 *
 */
package de.hybris.wooliesegiftcard.service;

import de.hybris.platform.core.model.order.OrderModel;

import java.util.List;


/**
 * @author 648156
 *
 */
public interface WooliesOrderService
{

	/**
	 * This API used for getOrderDetails
	 *
	 * @param orderToken
	 *           the parameter value to be used
	 * @return OrderModel the parameter used to return
	 */
	public List<OrderModel> getOrderDetailsWithDecryptKey(final String orderToken);

}
