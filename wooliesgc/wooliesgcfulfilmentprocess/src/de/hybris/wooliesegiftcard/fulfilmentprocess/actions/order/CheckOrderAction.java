/*
 * [y] hybris Platform
 *
 * Copyright (c) 2017 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.wooliesegiftcard.fulfilmentprocess.actions.order;

import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.task.RetryLaterException;
import de.hybris.wooliesegiftcard.core.constants.WooliesgcCoreConstants.PAYMENT_OPTIONS;
import de.hybris.wooliesegiftcard.fulfilmentprocess.CheckOrderService;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * This example action checks the order for required data in the business process. Skipping this action may result in
 * failure in one of the subsequent steps of the process. The relation between the order and the business process is
 * defined in basecommerce extension through item OrderProcess. Therefore if your business process has to access the
 * order (a typical case), it is recommended to use the OrderProcess as a parentClass instead of the plain
 * BusinessProcess.
 */
public class CheckOrderAction extends AbstractOrderAction<OrderProcessModel>
{
	private static final Logger LOG = Logger.getLogger(CheckOrderAction.class);

	private CheckOrderService checkOrderService;

	public enum Transition
	{
		OK, NOK, SENDTOWEX, PAYMENTCOMPLETION;

		public static Set<String> getStringValues()
		{
			final Set<String> res = new HashSet<String>();
			for (final Transition transitions : Transition.values())
			{
				res.add(transitions.toString());
			}
			return res;
		}
	}

	@Override
	public Set<String> getTransitions()
	{
		return Transition.getStringValues();
	}


	public Transition executeAction(final OrderProcessModel process)
	{
		final String status = OrderStatus.CHECKED_VALID.toString();
		final String CREDIT_CARD = PAYMENT_OPTIONS.PAY_1001.toString();
		final OrderModel order = process.getOrder();

		if (order.getStatus().getCode().equalsIgnoreCase(status)
				&& order.getPaymentInfo().getPaymentOption().getCode().equalsIgnoreCase(CREDIT_CARD))
		{

			return Transition.PAYMENTCOMPLETION;
		}
		if (order.getStatus() == OrderStatus.APPROVED)
		{
			return Transition.SENDTOWEX;
		}

		if ((CREDIT_CARD).equalsIgnoreCase(order.getPaymentInfo().getPaymentOption().getCode())
				&& order.getStatus() == OrderStatus.CHECKED_INVALID)
		{
			LOG.error("Payment Cancellation Initiated for the Order");
			return Transition.NOK;
		}
		else
		{
			return Transition.OK;
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


	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.hybris.platform.processengine.spring.Action#execute(de.hybris.platform.processengine.model.BusinessProcessModel
	 * )
	 */
	@Override
	public final String execute(final OrderProcessModel process) throws RetryLaterException, Exception
	{
		return executeAction(process).toString();
	}
}
