/**
 *
 */
package de.hybris.wooliesgiftcard.core.wex.service;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.wooliesegiftcard.facades.dto.WexIntegrationError;
import de.hybris.wooliesegiftcard.facades.dto.WoolworthsOrderRequest;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;


/**
 * @author 416910
 *
 */
public interface WexOrderDetailsService
{
	/**
	 * This method is used to createWexRequestPayload
	 *
	 * @param orderModel
	 *           the value will used
	 * @return the WoolworthsOrderRequest
	 */
	public WoolworthsOrderRequest createWexRequestPayload(OrderModel orderModel);

	/**
	 * This method is used to sendOrderDetailsToWex
	 *
	 * @param woolworthsOrderRequest
	 *           the value will used
	 * @param wexIntegrationErrorList
	 *           the value will used
	 * @param orderModel
	 *           the value will used
	 * @return the boolean value
	 * @throws KeyManagementException
	 *            used to throw exception
	 * @throws NoSuchAlgorithmException
	 *            used to throw exception
	 */
	public boolean sendOrderDetailsToWex(final WoolworthsOrderRequest woolworthsOrderRequest,
			final List<WexIntegrationError> wexIntegrationErrorList, final OrderModel orderModel) throws KeyManagementException,
			NoSuchAlgorithmException;

	/**
	 * This method is used to createWexRequestPayloadForMixOrder
	 *
	 * @param orderModel
	 *           value will be used
	 * @return the list of WoolworthsOrderRequest
	 */
	public List<WoolworthsOrderRequest> createWexRequestPayloadForMixOrder(final OrderModel orderModel);

}
