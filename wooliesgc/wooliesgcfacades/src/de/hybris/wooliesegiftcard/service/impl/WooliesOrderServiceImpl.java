/**
 *
 */
package de.hybris.wooliesegiftcard.service.impl;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.wooliesegiftcard.dao.impl.WooliesOrderDaoImpl;
import de.hybris.wooliesegiftcard.service.WooliesOrderService;

import java.util.List;


/**
 * @author 648156
 *
 */
public class WooliesOrderServiceImpl implements WooliesOrderService
{
	private WooliesOrderDaoImpl wooliesOrderDaoImpl;

	/**
	 * @return the wooliesOrderDaoImpl
	 */
	public WooliesOrderDaoImpl getWooliesOrderDaoImpl()
	{
		return wooliesOrderDaoImpl;
	}

	/**
	 * @param wooliesOrderDaoImpl
	 *           the wooliesOrderDaoImpl to set
	 */
	public void setWooliesOrderDaoImpl(final WooliesOrderDaoImpl wooliesOrderDaoImpl)
	{
		this.wooliesOrderDaoImpl = wooliesOrderDaoImpl;
	}

	/**
	 * This api used for get the orderdetails
	 *
	 * @param orderToken
	 * @return OrderData
	 */
	@Override
	public List<OrderModel> getOrderDetailsWithDecryptKey(final String orderToken)
	{
		return wooliesOrderDaoImpl.getOrderDetailsWithDecryptKey(orderToken);
	}

}
