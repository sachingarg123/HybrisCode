/**
 *
 */
package de.hybris.wooliesegiftcard.core.apigee.service;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.wooliesegiftcard.core.exceptions.WooliesServiceLayerException;


/**
 * @author 653930
 *
 */
public interface WooliesPaymentCompletionService
{
	/**
	 * Method send the payment completion request.Also used in cronjob for retry mechanism. While doing the payment
	 * completion from DIGIPAY, it will come to hybris to change the payment status at hybris.
	 *
	 * @param order
	 *           the parameter value to be used
	 * @return the sendPaymentCompletionRequest the parameter used to return
	 * @throws WooliesServiceLayerException
	 *            used to throw exception
	 */
	public boolean sendPaymentCompletionRequest(final OrderModel order) throws WooliesServiceLayerException;
}
