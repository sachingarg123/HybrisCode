/**
 *
 */
package de.hybris.wooliesegiftcard.fulfilmentprocess.actions.order;

import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.enums.PaymentStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.wooliesegiftcard.core.apigee.service.impl.ApigeeOrderDetailsServiceImpl;
import de.hybris.wooliesegiftcard.core.exceptions.WooliesServiceLayerException;
import de.hybris.wooliesegiftcard.fulfilmentprocess.CheckOrderService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * @author 687679
 *
 */
public class PaymentCancelAction extends AbstractSimpleDecisionAction<OrderProcessModel>
{
	private static final Logger LOG = Logger.getLogger(CheckOrderAction.class);

	private CheckOrderService checkOrderService;
	private ApigeeOrderDetailsServiceImpl apigeeOrderDetailsService;

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.platform.processengine.action.AbstractSimpleDecisionAction#executeAction(de.hybris.platform.
	 * processengine.model.BusinessProcessModel)
	 */
	@Override
	public Transition executeAction(final OrderProcessModel process) throws WooliesServiceLayerException
	{
		final OrderModel order = process.getOrder();
		boolean paymentCancelFlag = false;
		if (order.getStatus() == OrderStatus.CHECKED_INVALID
				&& order.getPaymentInfo().getPaymentOption().getCode().equals("PAY_1001"))
		{

			paymentCancelFlag = apigeeOrderDetailsService.sendPaymentCancelRequest(order);
			if (paymentCancelFlag)
			{
				order.setStatus(OrderStatus.CANCELLED);
				order.setPaymentStatus(PaymentStatus.NOTPAID);
				getModelService().save(order);
				return Transition.OK;
			}
			else
			{
				order.setPaymentStatus(PaymentStatus.ERROR_PAYMENT_CANCELLATION);
				getModelService().save(order);
				LOG.error("No Payment Cancellation");
				return Transition.NOK;
			}
		}
		else
		{
			LOG.error("No Payment Cancellation");
			return Transition.NOK;
		}
	}

	protected CheckOrderService getCheckOrderService()
	{
		return checkOrderService;
	}

	@Required
	public void setCheckOrderService(final CheckOrderService checkOrderService)
	{
		this.checkOrderService = checkOrderService;
	}

	/**
	 * @return the apigeeOrderDetailsService
	 */
	public ApigeeOrderDetailsServiceImpl getApigeeOrderDetailsService()
	{
		return apigeeOrderDetailsService;
	}

	/**
	 * @param apigeeOrderDetailsService
	 *           the apigeeOrderDetailsService to set
	 */
	public void setApigeeOrderDetailsService(final ApigeeOrderDetailsServiceImpl apigeeOrderDetailsService)
	{
		this.apigeeOrderDetailsService = apigeeOrderDetailsService;
	}

}
