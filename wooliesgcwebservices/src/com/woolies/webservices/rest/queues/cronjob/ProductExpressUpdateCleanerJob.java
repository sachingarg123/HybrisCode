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
package com.woolies.webservices.rest.queues.cronjob;

import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;

import java.util.Date;

import com.woolies.webservices.rest.model.expressupdate.cron.ProductExpressUpdateCleanerCronJobModel;
import com.woolies.webservices.rest.queues.impl.ProductExpressUpdateQueue;


/**
 * A Cron Job for cleaning up {@link ProductExpressUpdateQueue}.
 */
public class ProductExpressUpdateCleanerJob extends AbstractJobPerformable<ProductExpressUpdateCleanerCronJobModel>
{
	private ProductExpressUpdateQueue productExpressUpdateQueue;

	/**
	 * This method is used to perform the cleaner job forProductExpress update
	 * 
	 * @param cronJob
	 * @return PerformResult
	 */
	@Override
	public PerformResult perform(final ProductExpressUpdateCleanerCronJobModel cronJob)
	{
		final Date timestamp = new Date(System.currentTimeMillis() - (cronJob.getQueueTimeLimit().intValue() * 60 * 1000));
		productExpressUpdateQueue.removeItems(timestamp);
		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}

	/**
	 * This method is used to set ProductExpress update queue object
	 * 
	 * @param productExpressUpdateQueue
	 */
	public void setProductExpressUpdateQueue(final ProductExpressUpdateQueue productExpressUpdateQueue)
	{
		this.productExpressUpdateQueue = productExpressUpdateQueue;
	}
}
