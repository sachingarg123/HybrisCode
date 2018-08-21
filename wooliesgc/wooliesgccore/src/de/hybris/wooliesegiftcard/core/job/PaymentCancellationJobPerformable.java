/**
 *
 */
package de.hybris.wooliesegiftcard.core.job;


import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.enums.PaymentStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.wooliesegiftcard.core.apigee.service.impl.ApigeeOrderDetailsServiceImpl;
import de.hybris.wooliesegiftcard.core.exceptions.WooliesServiceLayerException;
import de.hybris.wooliesegiftcard.core.genric.dao.impl.DefaultWooliesGenericDao;
import de.hybris.wooliesegiftcard.core.model.OrderRetryCountModel;

import java.util.Collection;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author 687679
 *
 */
public class PaymentCancellationJobPerformable extends AbstractJobPerformable<CronJobModel>
{
	private static final Logger LOG = Logger.getLogger(PaymentCancellationJobPerformable.class.getName());
	@Autowired
	private DefaultWooliesGenericDao defaultWooliesGenericDao;
	@Autowired
	private ApigeeOrderDetailsServiceImpl apigeeOrderDetailsService;

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;
	boolean paymentCancellationFlag = false;

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



	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable#perform(de.hybris.platform.cronjob.model.
	 * CronJobModel)
	 */
	@Override
	public PerformResult perform(final CronJobModel arg0)
	{
		final Collection<OrderModel> orders = defaultWooliesGenericDao.getOrdersForPaymentCancellation();
		final int maxRetryCount = configurationService.getConfiguration().getInt("payment.cancel.retry.count");

		if (null != orders && !orders.isEmpty())
		{
			for (final OrderModel order : orders)
			{
				if ("PAY_1001".equals(order.getPaymentInfo().getPaymentOption().getCode()))
				{
					orderAndPaymentStatus(order, maxRetryCount);
				}
			}
		}
		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}


	void orderAndPaymentStatus(final OrderModel order, final int maxRetryCount)
	{

		try
		{
			int retryCount = 0;
			if (null != order.getRetryCount())
			{
				final OrderRetryCountModel orderRetryCountModel = order.getRetryCount();
				retryCount = orderRetryCountModel.getPaymentCancelCount().intValue();
				orderRetryCountModel.setPaymentCancelCount(Integer.valueOf(retryCount + 1));
				getModelService().save(orderRetryCountModel);
				order.setRetryCount(orderRetryCountModel);
				getModelService().save(order);
				retryCount = order.getRetryCount().getPaymentCancelCount().intValue();
			}
			else
			{
				final OrderRetryCountModel orderRetryCountModel = getModelService().create(OrderRetryCountModel.class);
				orderRetryCountModel.setPaymentCancelCount(Integer.valueOf(retryCount + 1));
				getModelService().save(orderRetryCountModel);
				order.setRetryCount(orderRetryCountModel);
				getModelService().save(order);
				getModelService().save(orderRetryCountModel);
				retryCount = order.getRetryCount().getPaymentCancelCount().intValue();
			}
			if (retryCount <= maxRetryCount)
			{
				paymentCancellationFlag = apigeeOrderDetailsService.sendPaymentCancelRequest(order);

				if (paymentCancellationFlag)
				{
					order.setStatus(OrderStatus.CANCELLED);
					order.setPaymentStatus(PaymentStatus.NOTPAID);
					LOG.info("Data Posted Successfully to APIGEE");
				}
				else
				{
					order.setPaymentStatus(PaymentStatus.ERROR_PAYMENT_CANCELLATION);
					LOG.info("Error response from Apigee for this Order");
				}
			}
			if (retryCount == maxRetryCount)
			{
				order.setStatus(OrderStatus.PROCESSING_ERROR);
				order.setPaymentStatus(PaymentStatus.ERROR_PAYMENT_CANCELLATION);
			}
			LOG.info("OrderStatus==" + order.getStatus());
			LOG.info("PaymentStatus==" + order.getPaymentStatus());
			getModelService().save(order);
			getModelService().refresh(order);
		}
		catch (final WooliesServiceLayerException e)
		{
			LOG.error(e);
			try
			{
				throw new WooliesServiceLayerException(e.getErrorCode(), e.getMessage(), e.getErrorReason());
			}
			catch (final WooliesServiceLayerException e1)
			{
				LOG.error(e1);
			}
		}
		catch (final Exception e)
		{
			LOG.error(e);
		}
	}
}
