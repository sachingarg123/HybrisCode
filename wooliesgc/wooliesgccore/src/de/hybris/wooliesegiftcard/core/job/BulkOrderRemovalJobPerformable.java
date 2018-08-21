/**
 *
 */
package de.hybris.wooliesegiftcard.core.job;

/**
 * @author 676313
 *
 */
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.wooliesegiftcard.core.model.WWBulkOrderDataModel;
import de.hybris.wooliesegiftcard.core.model.WWBulkOrderItemsDataModel;
import de.hybris.wooliesegiftcard.core.workflow.dao.impl.BulkOrderDaoImpl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Date;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;


/**
 * @author 676313
 *
 */
public class BulkOrderRemovalJobPerformable extends AbstractJobPerformable<CronJobModel>
{
	private BulkOrderDaoImpl wooliesBulkOrder;
	private ConfigurationService configurationService;


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
	 * @return the wooliesBulkOrderFacade
	 */


	/**
	 * @return the wooliesBulkOrder
	 */


	/**
	 * @return the wooliesBulkOrder
	 */
	public BulkOrderDaoImpl getWooliesBulkOrder()
	{
		return wooliesBulkOrder;
	}

	/**
	 * @param wooliesBulkOrder
	 *           the wooliesBulkOrder to set
	 */
	public void setWooliesBulkOrder(final BulkOrderDaoImpl wooliesBulkOrder)
	{
		this.wooliesBulkOrder = wooliesBulkOrder;
	}

	/**
	 * @return the bulkOrderRemovalJob
	 */

	private ModelService modelService;

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



	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable#perform(de.hybris.platform.cronjob.model.
	 * CronJobModel)
	 */
	@Override
	public PerformResult perform(final CronJobModel cronJob)
	{
		final Logger LOG = Logger.getLogger(BulkOrderRemovalJobPerformable.class);


		final List<WWBulkOrderDataModel> orderdata = (List<WWBulkOrderDataModel>) wooliesBulkOrder.successAndvalidationfailure();
		

			final List referbnceNumbers = new ArrayList<String>();
			for (final WWBulkOrderDataModel eachOrder : orderdata)
			{
				referbnceNumbers.add(eachOrder.getReferenceNumber());
			}

			LOG.debug("oldorderdata.isEmpty()==" + referbnceNumbers.isEmpty());

			if (CollectionUtils.isNotEmpty(referbnceNumbers))
			{

				final List<WWBulkOrderItemsDataModel> orderitemData = (List<WWBulkOrderItemsDataModel>) wooliesBulkOrder
						.itemsSuccessandvalidationfailure(referbnceNumbers);
				if (!orderitemData.isEmpty())
				{
					getModelService().removeAll(orderitemData);
					getModelService().removeAll(orderdata);
				}

			
			LOG.debug("Remove Old order data Successfully");
		}
		final int hours = getConfigurationService().getConfiguration().getInt("creationtime");
		final Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR_OF_DAY, -hours);
		 final Date oldTimes = calendar.getTime();
		final List<WWBulkOrderDataModel> orderdatas = (List<WWBulkOrderDataModel>) wooliesBulkOrder
				.inProcessandvalidationsuccess(oldTimes);
		



			final List referbnceNumbersa = new ArrayList<String>();
			for (final WWBulkOrderDataModel eachOrder : orderdatas)
			{
				referbnceNumbersa.add(eachOrder.getReferenceNumber());
			}

			LOG.debug("oldorderdata.isEmpty()==" + referbnceNumbersa.isEmpty());

			if (CollectionUtils.isNotEmpty(referbnceNumbersa))
			{

				final List<WWBulkOrderItemsDataModel> orderitemDatas = (List<WWBulkOrderItemsDataModel>) wooliesBulkOrder
						.itemsProcessandvalidationsuccess(referbnceNumbersa, oldTimes);
				if (!orderitemDatas.isEmpty())
				{
					getModelService().removeAll(orderitemDatas);
					getModelService().removeAll(orderdatas);
				}

			
			LOG.debug("Remove Old order data Successfully");
		}

		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);

	}



}
