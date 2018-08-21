/**
 *
 */
package de.hybris.wooliesegiftcard.core.wex.dao;

import de.hybris.platform.core.model.order.OrderModel;

import java.util.List;


/**
 * @author 416910
 *
 */
public interface WexOrderDetailsDao
{
	public List<OrderModel> getOrderDetailsForWex();
}
