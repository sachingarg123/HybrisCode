/**
 *
 */
package de.hybris.wooliesegiftcard.core.payment.dao;

import de.hybris.platform.core.model.order.OrderModel;

import java.util.Collection;


/**
 * @author 653930
 *
 */
public interface WooliesPaymentDao
{
	public Collection<OrderModel> getOrderByStatusandVersion();

}
