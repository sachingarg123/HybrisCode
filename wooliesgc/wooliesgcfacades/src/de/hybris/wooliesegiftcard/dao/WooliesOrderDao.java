/**
 *
 */
package de.hybris.wooliesegiftcard.dao;

import de.hybris.platform.core.model.order.OrderModel;

import java.util.List;


/**
 * @author 648156
 *
 */
public interface WooliesOrderDao
{
	/**
	 * this method is used to get order model based on order token
	 * 
	 * @param orderToken
	 * @return OrderModel
	 */
	public List<OrderModel> getOrderDetailsWithDecryptKey(final String orderToken);
}
