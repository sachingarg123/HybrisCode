/**
 *
 */
package com.woolies.webservices.order.impl;

import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.order.strategies.CartValidator;
import de.hybris.platform.order.strategies.impl.DefaultCreateOrderFromCartStrategy;
import de.hybris.platform.order.strategies.ordercloning.CloneAbstractOrderStrategy;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * @author 653154 This class is used to maintain order from cart strategy
 */
public class DefaultWooliesCreateOrderFromCartStrategy extends DefaultCreateOrderFromCartStrategy
{
	private static final Logger LOG = Logger.getLogger(DefaultWooliesCreateOrderFromCartStrategy.class);
	private CartValidator cartValidator;
	private CloneAbstractOrderStrategy cloneAbstractOrderStrategy;

	/**
	 * This method is used to create order from cart
	 * 
	 * @param cart
	 * @return OrderModel
	 * @throws InvalidCartException
	 */
	@Override
	public OrderModel createOrderFromCart(final CartModel cart) throws InvalidCartException
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("DefaultWooliesCreateOrderFromCartStrategy : createOrderFromCart ");
		}
		if (cartValidator != null)
		{
			cartValidator.validateCart(cart);
		}
		return cloneAbstractOrderStrategy.clone(null, null, cart, generateOrderCode(cart), OrderModel.class, OrderEntryModel.class);
	}

	/**
	 * Generate a code for created order. Default implementation use {@link KeyGenerator}.
	 *
	 * @param cart
	 *           You can use a cart to generate new code for order.
	 */
	@Override
	protected String generateOrderCode(final CartModel cart)
	{
		return cart.getOrderId();
	}

	/**
	 * This method is used to set cart validator object
	 * 
	 * @param cartValidator
	 */
	@Override
	@Required
	public void setCartValidator(final CartValidator cartValidator)
	{
		this.cartValidator = cartValidator;
	}

	/**
	 * This method is used to set CloneAbstractOrderStrategy object
	 * 
	 * @param cloneAbstractOrderStrategy
	 */
	@Override
	@Required
	public void setCloneAbstractOrderStrategy(final CloneAbstractOrderStrategy cloneAbstractOrderStrategy)
	{
		this.cloneAbstractOrderStrategy = cloneAbstractOrderStrategy;
	}
}
