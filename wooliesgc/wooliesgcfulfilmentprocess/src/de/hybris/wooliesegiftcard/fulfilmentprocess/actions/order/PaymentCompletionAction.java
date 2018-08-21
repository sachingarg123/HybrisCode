/**
 *
 */
package de.hybris.wooliesegiftcard.fulfilmentprocess.actions.order;

import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.enums.PaymentStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.wooliesegiftcard.core.apigee.service.WooliesPaymentCompletionService;
import de.hybris.wooliesegiftcard.core.constants.WooliesgcCoreConstants.PAYMENT_OPTIONS;
import de.hybris.wooliesegiftcard.core.exceptions.WooliesServiceLayerException;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

import org.apache.log4j.Logger;


/**
 * @author 653930
 *
 */
public class PaymentCompletionAction extends AbstractSimpleDecisionAction<OrderProcessModel>
{
	private static final Logger LOG = Logger.getLogger(CheckOrderAction.class);

	private WooliesPaymentCompletionService wooliesPaymentCompletionService;

	@Override
	public Transition executeAction(final OrderProcessModel process) throws KeyManagementException, NoSuchAlgorithmException,
			WooliesServiceLayerException, ParseException, CalculationException
	{
		final OrderModel order = process.getOrder();
		final String status = OrderStatus.CHECKED_VALID.toString();

		final String CREDIT_CARD = PAYMENT_OPTIONS.PAY_1001.toString();
		boolean paymentCompletionFlag = false;
		if (order.getStatus().getCode().equalsIgnoreCase(status)
				&& order.getPaymentInfo().getPaymentOption().getCode().equalsIgnoreCase(CREDIT_CARD))
		{

			try
			{
				paymentCompletionFlag = getWooliesPaymentCompletionService().sendPaymentCompletionRequest(order);
			}
			catch (final WooliesServiceLayerException e)
			{
				LOG.info(e);
				throw new WooliesServiceLayerException(e.getErrorCode(), e.getMessage(), e.getErrorReason());
			}
			if (paymentCompletionFlag)
			{
				order.setStatus(OrderStatus.APPROVED);
				order.setPaymentStatus(PaymentStatus.PAID);
				getModelService().save(order);
				LOG.info("OrderStatus==" + order.getStatus());
				LOG.info("PaymentStatus==" + order.getPaymentStatus());
				return Transition.OK;
			}
			else
			{
				LOG.info("OrderStatus==" + order.getStatus());
				LOG.info("PaymentStatus==" + order.getPaymentStatus());
				return Transition.NOK;
			}
		}

		else
		{
			LOG.error("No Payment Cancellation");
			return Transition.NOK;
		}
	}



	/**
	 * @return the wooliesPaymentCompletionService
	 */
	public WooliesPaymentCompletionService getWooliesPaymentCompletionService()
	{
		return wooliesPaymentCompletionService;
	}

	/**
	 * @param wooliesPaymentCompletionService
	 *           the wooliesPaymentCompletionService to set
	 */
	public void setWooliesPaymentCompletionService(final WooliesPaymentCompletionService wooliesPaymentCompletionService)
	{
		this.wooliesPaymentCompletionService = wooliesPaymentCompletionService;
	}
}
