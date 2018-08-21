/**
 *
 */
package de.hybris.wooliesegiftcard.fulfilmentprocess.actions.wex;

import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.wooliesegiftcard.facades.dto.WexIntegrationError;
import de.hybris.wooliesegiftcard.facades.dto.WoolworthsOrderRequest;
import de.hybris.wooliesgiftcard.core.wex.service.impl.WexOrderDetailsServiceImpl;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


/**
 * @author 416910
 *
 */
public class SendOrderDetailsToWexAction extends AbstractSimpleDecisionAction<OrderProcessModel>
{
	private static final Logger LOG = Logger.getLogger(SendOrderDetailsToWexAction.class.getName());
	private WexOrderDetailsServiceImpl wexOrderDetailsService;

	@Override
	public Transition executeAction(final OrderProcessModel process)
	{
		final OrderModel order = process.getOrder();
		WoolworthsOrderRequest woolworthsOrderRequest = new WoolworthsOrderRequest();
		List<WoolworthsOrderRequest> woolworthsOrderRequestList = new ArrayList<WoolworthsOrderRequest>();
		final List<String> cardType = new ArrayList<String>();

		if (order.getStatus() == OrderStatus.APPROVED)
		{
			for (final AbstractOrderEntryModel orderEntry : order.getEntries())
			{
				addCardType(cardType, orderEntry);
			}

			if (cardType.contains("P") && cardType.contains("E"))
			{
				woolworthsOrderRequestList = wexOrderDetailsService.createWexRequestPayloadForMixOrder(order);
			}
			else
			{
				woolworthsOrderRequest = wexOrderDetailsService.createWexRequestPayload(order);
			}


			final List<WexIntegrationError> wexIntegrationErrorList = new ArrayList<WexIntegrationError>();
			sendOrderDetails(order, woolworthsOrderRequest, woolworthsOrderRequestList, cardType, wexIntegrationErrorList);

			return Transition.OK;
		}

		else
		{
			LOG.info("Order Status Is Not APPROVED");
			return Transition.NOK;
		}
	}

	/**
	 * @param order
	 * @param woolworthsOrderRequest
	 * @param woolworthsOrderRequestList
	 * @param cardType
	 * @param wexIntegrationErrorList
	 */
	private void sendOrderDetails(final OrderModel order, WoolworthsOrderRequest woolworthsOrderRequest,
			List<WoolworthsOrderRequest> woolworthsOrderRequestList, final List<String> cardType,
			final List<WexIntegrationError> wexIntegrationErrorList)
	{
		boolean responseStatus = false;

		if (cardType.contains("P") && cardType.contains("E"))
		{
			for (final WoolworthsOrderRequest woolworthsOrderRequestMix : woolworthsOrderRequestList)
			{
				try
				{
					responseStatus = wexOrderDetailsService.sendOrderDetailsToWex(woolworthsOrderRequestMix,
							wexIntegrationErrorList, order);
					logInfo(responseStatus);
				}
				catch (KeyManagementException | NoSuchAlgorithmException e)
				{
					LOG.error(e);
				}
			}
		}
		else
		{
			try
			{
				responseStatus = wexOrderDetailsService.sendOrderDetailsToWex(woolworthsOrderRequest, wexIntegrationErrorList,
						order);
				logInfo(responseStatus);
			}
			catch (KeyManagementException | NoSuchAlgorithmException e)
			{
				LOG.error(e);
			}
		}
	}

	/**
	 * @param responseStatus
	 */
	private static void logInfo(final boolean responseStatus)
	{
		if (responseStatus)
		{
			LOG.info("POSTED SUCCESSFULLY TO WEX");
		}
		else
		{
			LOG.info("Error While Sending Data");
		}
	}

	/**
	 * @param cardType
	 * @param orderEntry
	 */
	private void addCardType(final List<String> cardType, final AbstractOrderEntryModel orderEntry)
	{
		if (orderEntry.getProduct().getIsEGiftCard().booleanValue())
		{
			cardType.add("E");
		}
		else
		{
			cardType.add("P");
		}
	}

	/**
	 * @return the wexOrderDetailsService
	 */
	public WexOrderDetailsServiceImpl getWexOrderDetailsService()
	{
		return wexOrderDetailsService;
	}

	/**
	 * @param wexOrderDetailsService
	 *           the wexOrderDetailsService to set
	 */
	public void setWexOrderDetailsService(final WexOrderDetailsServiceImpl wexOrderDetailsService)
	{
		this.wexOrderDetailsService = wexOrderDetailsService;
	}


}
