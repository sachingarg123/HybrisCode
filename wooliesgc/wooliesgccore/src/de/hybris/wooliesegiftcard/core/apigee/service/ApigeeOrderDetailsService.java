/**
 *
 */
package de.hybris.wooliesegiftcard.core.apigee.service;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.wooliesegiftcard.core.exceptions.WooliesServiceLayerException;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;


/**
 * @author 687679
 *
 */
public interface ApigeeOrderDetailsService
{
	/**
	 * Method send the payment cancellation request.Also used in cronjob for retry mechanism. While doing the
	 * cancellation from AEM side, it will come to hybris to cancel the payment made by customer.
	 *
	 * @param orderModel
	 *           The Cancel payment request is for this Order
	 * @throws WooliesServiceLayerException
	 *            throws this Exception in case of any error
	 * @throws KeyManagementException
	 *            throws this Exception in case of any error
	 * @throws NoSuchAlgorithmException
	 *            throws this Exception in case of any error
	 * @throws CalculationException
	 *            throws this Exception in case of any error
	 * @return boolean It returns Boolean value based on the response
	 */
	public boolean sendPaymentCancelRequest(final OrderModel orderModel) throws WooliesServiceLayerException;

}
