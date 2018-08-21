/**
 *
 */
package de.hybris.wooliesegiftcard.core.fraud.dao;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.store.BaseStoreModel;



/**
 * @author 669567
 *
 */
public interface FraudOrderStatusDao
{


	/**
	 * @param code
	 * @param store
	 * @return OrderModel
	 */
	public OrderModel findOrderByCodeAndStore(final String code, final BaseStoreModel store);
}
