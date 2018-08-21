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
import de.hybris.wooliesegiftcard.core.apigee.service.WooliesPaymentCompletionService;
import de.hybris.wooliesegiftcard.core.exceptions.WooliesServiceLayerException;
import de.hybris.wooliesegiftcard.core.model.OrderRetryCountModel;
import de.hybris.wooliesegiftcard.core.payment.dao.WooliesPaymentDao;

import java.util.Collection;

import javax.annotation.Resource;

import org.apache.log4j.Logger;


/**
 * @author 653930
 *
 */
public class PaymentCompletionRetryPerformable extends AbstractJobPerformable<CronJobModel>
{
	private static final Logger LOG = Logger.getLogger(PaymentCompletionRetryPerformable.class.getName());
	boolean paymentCompletionFlag = false;
	private ModelService modelService;
	private WooliesPaymentDao wooliesPaymentDao;
	private WooliesPaymentCompletionService wooliesPaymentCompletionService;
	@Resource(name = "configurationService")
	private ConfigurationService configurationService;


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
		final Collection<OrderModel> orders = getWooliesPaymentDao().getOrderByStatusandVersion();
		final int maxRetryCount = configurationService.getConfiguration()
				.getInteger("payment.completion.retry.count.max", Integer.valueOf(3)).intValue();

		if (null != orders && !orders.isEmpty())
		{
			for (final OrderModel order : orders)
			{
				saveOrderandPaymentStatus(order, maxRetryCount);
			}
		}
		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}


	void saveOrderandPaymentStatus(final OrderModel order, final int maxRetryCount)
	{
		if (null == order.getRetryCount())
		{
			final OrderRetryCountModel orderRetryCountModel = getModelService().create(OrderRetryCountModel.class);
			order.setRetryCount(orderRetryCountModel);
		}
		final int retryCount = order.getRetryCount().getPaymentCompletionCount().intValue() + 1;
		final OrderRetryCountModel orderRetryCount = order.getRetryCount();
		orderRetryCount.setPaymentCompletionCount(Integer.valueOf(retryCount));
		getModelService().save(orderRetryCount);
		order.setRetryCount(orderRetryCount);
		if (retryCount <= maxRetryCount)
		{
			try
			{
				paymentCompletionFlag = getWooliesPaymentCompletionService().sendPaymentCompletionRequest(order);
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
		if (paymentCompletionFlag)
		{
			order.setStatus(OrderStatus.APPROVED);
			order.setPaymentStatus(PaymentStatus.PAID);
		}
		else
		{
			if (retryCount == maxRetryCount)
			{
				order.setStatus(OrderStatus.PROCESSING_ERROR);
				order.setPaymentStatus(PaymentStatus.ERROR_PAYMENT_COMPLETION);
			}
		}

		LOG.info("OrderStatus==" + order.getStatus());
		LOG.info("PaymentStatus==" + order.getPaymentStatus());
		getModelService().save(order);
		getModelService().refresh(order);

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



	/**
	 * @return the wooliesPaymentDao
	 */
	public WooliesPaymentDao getWooliesPaymentDao()
	{
		return wooliesPaymentDao;
	}



	/**
	 * @param wooliesPaymentDao
	 *           the wooliesPaymentDao to set
	 */
	public void setWooliesPaymentDao(final WooliesPaymentDao wooliesPaymentDao)
	{
		this.wooliesPaymentDao = wooliesPaymentDao;
	}

}
