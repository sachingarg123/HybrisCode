/**
 *
 */
package de.hybris.wooliesegiftcard.core.job;

import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.wooliesegiftcard.core.model.OrderRetryCountModel;
import de.hybris.wooliesegiftcard.core.wex.dao.impl.WexOrderDetailsDaoImpl;
import de.hybris.wooliesegiftcard.facades.dto.WexIntegrationError;
import de.hybris.wooliesegiftcard.facades.dto.WoolworthsOrderRequest;
import de.hybris.wooliesgiftcard.core.wex.service.impl.WexOrderDetailsServiceImpl;

import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


/**
 * @author 416910 This is cronjob that will be executed on 15 mins to send order details to wex in case invoice number
 *         not generated after palcing the order
 */
public class OrderDetailsWexPerformable extends AbstractJobPerformable<CronJobModel>
{
	private static final Logger LOG = Logger.getLogger(OrderDetailsWexPerformable.class.getName());

	private WexOrderDetailsDaoImpl wexOrderDetails;
	private ConfigurationService configurationService;
	private WexOrderDetailsServiceImpl wexOrderDetailsService;

	private ModelService modelService;

	/**
	 * @return the wexOrderDetails
	 */
	public WexOrderDetailsDaoImpl getWexOrderDetails()
	{
		return wexOrderDetails;
	}



	/**
	 * @param wexOrderDetails
	 *           the wexOrderDetails to set
	 */
	public void setWexOrderDetails(final WexOrderDetailsDaoImpl wexOrderDetails)
	{
		this.wexOrderDetails = wexOrderDetails;
	}



	/**
	 * @return the configurationService
	 */
	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}



	/**
	 * @param configurationService
	 *           the configurationService to set
	 */
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}



	/**
	 * @return the modelService
	 */
	public ModelService getModelService()
	{
		return modelService;
	}



	/**
	 * @param modelService
	 *           the modelService to set
	 */
	@Override
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}



	@Override
	public PerformResult perform(final CronJobModel job)
	{
		final int maxRetryCount = configurationService.getConfiguration().getInt("wex.retry.count", 3);
		WoolworthsOrderRequest woolworthsOrderRequest = new WoolworthsOrderRequest();
		List<WoolworthsOrderRequest> woolworthsOrderRequestList = new ArrayList<WoolworthsOrderRequest>();
		List<String> cardType = new ArrayList<String>();
		final List<OrderModel> approvedOrderList = wexOrderDetails.getOrderDetailsForWex();
		for (final OrderModel orderData : approvedOrderList)
		{
			cardType = checkForMixedOrder(orderData);
			if (cardType.contains("P") && cardType.contains("E"))
			{
				woolworthsOrderRequestList = wexOrderDetailsService.createWexRequestPayloadForMixOrder(orderData);
			}
			else
			{
				woolworthsOrderRequest = wexOrderDetailsService.createWexRequestPayload(orderData);
			}

			final List<WexIntegrationError> wexIntegrationErrorList = new ArrayList<WexIntegrationError>();
			if (cardType.contains("P") && cardType.contains("E"))
			{
				retryForMixedOrderDetailsToWex(orderData, woolworthsOrderRequestList, maxRetryCount, wexIntegrationErrorList);
			}
			else
			{
				retryForSingleOrderDetailsToWex(orderData, woolworthsOrderRequest, maxRetryCount, wexIntegrationErrorList);
			}
		}

		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
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

	public List<String> checkForMixedOrder(final OrderModel orderData)
	{
		final List<String> cardType = new ArrayList<String>();
		for (final AbstractOrderEntryModel orderEntry : orderData.getEntries())
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
		return cardType;
	}

	public void retryForMixedOrderDetailsToWex(final OrderModel orderData,
			final List<WoolworthsOrderRequest> woolworthsOrderRequestList, final int maxRetryCount,
			final List<WexIntegrationError> wexIntegrationErrorList)
	{
		try
		{
			int retryCount = 0;
			if (null != orderData.getRetryCount())
			{
				final OrderRetryCountModel orderRetryCountModel = orderData.getRetryCount();
				retryCount = orderRetryCountModel.getWexCount().intValue();
				orderRetryCountModel.setWexCount(Integer.valueOf(retryCount + 1));
				getModelService().save(orderRetryCountModel);
				orderData.setRetryCount(orderRetryCountModel);
				getModelService().save(orderData);
				retryCount = orderData.getRetryCount().getWexCount().intValue();
			}
			else
			{
				final OrderRetryCountModel orderRetryCountModel = getModelService().create(OrderRetryCountModel.class);
				orderRetryCountModel.setWexCount(Integer.valueOf(retryCount + 1));
				getModelService().save(orderRetryCountModel);
				orderData.setRetryCount(orderRetryCountModel);
				getModelService().save(orderData);
				retryCount = orderData.getRetryCount().getWexCount().intValue();
			}

			if (retryCount <= maxRetryCount)
			{
				for (final WoolworthsOrderRequest woolworthsOrderRequestMix : woolworthsOrderRequestList)
				{
					final boolean finalResponseStatus = wexOrderDetailsService.sendOrderDetailsToWex(woolworthsOrderRequestMix,
							wexIntegrationErrorList, orderData);
					checkForOrderStatusSharedToWex(finalResponseStatus, retryCount, maxRetryCount, orderData);
				}
			}

		}
		catch (KeyManagementException | NoSuchAlgorithmException e)
		{
			LOG.error(e.getMessage());
			try
			{
				throw e;
			}
			catch (final GeneralSecurityException e1)
			{
				LOG.error(e1);
			}
		}
	}

	public void retryForSingleOrderDetailsToWex(final OrderModel orderData, final WoolworthsOrderRequest woolworthsOrderRequest,
			final int maxRetryCount, final List<WexIntegrationError> wexIntegrationErrorList)
	{
		try
		{
			int retryCount = 0;
			if (null != orderData.getRetryCount())
			{
				final OrderRetryCountModel orderRetryCountModel = orderData.getRetryCount();
				retryCount = orderRetryCountModel.getWexCount().intValue();
				orderRetryCountModel.setWexCount(Integer.valueOf(retryCount + 1));
				getModelService().save(orderRetryCountModel);
				orderData.setRetryCount(orderRetryCountModel);
				getModelService().save(orderData);
				retryCount = orderData.getRetryCount().getWexCount().intValue();
			}
			else
			{
				final OrderRetryCountModel orderRetryCountModel = getModelService().create(OrderRetryCountModel.class);
				orderRetryCountModel.setWexCount(Integer.valueOf(retryCount + 1));
				getModelService().save(orderRetryCountModel);
				orderData.setRetryCount(orderRetryCountModel);
				getModelService().save(orderData);
				retryCount = orderData.getRetryCount().getWexCount().intValue();
			}

			if (retryCount <= maxRetryCount)
			{
				final boolean finalResponseStatus = wexOrderDetailsService.sendOrderDetailsToWex(woolworthsOrderRequest,
						wexIntegrationErrorList, orderData);
				checkForOrderStatusSharedToWex(finalResponseStatus, retryCount, maxRetryCount, orderData);
			}

		}
		catch (KeyManagementException | NoSuchAlgorithmException e)
		{
			LOG.error(e.getMessage());
			try
			{
				throw e;
			}
			catch (final GeneralSecurityException e1)
			{
				LOG.error(e1);
			}
		}

	}

	public void checkForOrderStatusSharedToWex(final boolean finalResponseStatus, final int retryCount, final int maxRetryCount,
			final OrderModel orderData)
	{
		if (finalResponseStatus)
		{
			LOG.info("POSTED SUCCESSFULLY TO WEX");
		}
		else
		{
			if (retryCount == maxRetryCount)
			{
				orderData.setStatus(OrderStatus.ERROR_SENDING_WEX);

			}
		}
		LOG.info("OrderStatus==" + orderData.getStatus());
		modelService.save(orderData);
		modelService.refresh(orderData);

	}

}
