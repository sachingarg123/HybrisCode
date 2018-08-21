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
package com.woolies.webservices.rest.cronjob;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commerceservices.order.dao.CommerceCartDao;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.time.TimeService;
import de.hybris.platform.servicelayer.user.UserService;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Required;

import com.woolies.webservices.rest.model.OldCartRemovalCronJobModel;


/**
 * A Cron Job to clean up old carts.
 */
public class OldCartRemovalJob extends AbstractJobPerformable<OldCartRemovalCronJobModel>
{
	private static final Logger LOG = Logger.getLogger(OldCartRemovalJob.class);

	private CommerceCartDao commerceCartDao;
	private TimeService timeService;
	private UserService userService;

	private static final int DEFAULT_CART_MAX_AGE = 2419200;
	private static final int DEFAULT_ANONYMOUS_CART_MAX_AGE = 1209600;

	/**
	 * This method is a Cron Job to clean up old carts.
	 * 
	 * @param oldCartRemovalCronJob
	 * @return
	 */
	@Override
	public PerformResult perform(final OldCartRemovalCronJobModel oldCartRemovalCronJob)
	{
		try
		{
			if (oldCartRemovalCronJob.getSites() == null || oldCartRemovalCronJob.getSites().isEmpty())
			{
				LOG.warn("There is no sites defined for " + oldCartRemovalCronJob.getCode());
				return new PerformResult(CronJobResult.FAILURE, CronJobStatus.FINISHED);
			}

			final int cartAge = oldCartRemovalCronJob.getCartRemovalAge() != null
					? oldCartRemovalCronJob.getCartRemovalAge().intValue() : DEFAULT_CART_MAX_AGE;
			final int anonymousCartAge = oldCartRemovalCronJob.getAnonymousCartRemovalAge() != null
					? oldCartRemovalCronJob.getAnonymousCartRemovalAge().intValue() : DEFAULT_ANONYMOUS_CART_MAX_AGE;

			for (final BaseSiteModel site : oldCartRemovalCronJob.getSites())
			{
				for (final CartModel oldCart : getCommerceCartDao().getCartsForRemovalForSiteAndUser(
						new DateTime(getTimeService().getCurrentTime()).minusSeconds(cartAge).toDate(), site, null))
				{
					getModelService().remove(oldCart);
				}

				for (final CartModel oldCart : getCommerceCartDao().getCartsForRemovalForSiteAndUser(
						new DateTime(getTimeService().getCurrentTime()).minusSeconds(anonymousCartAge).toDate(), site,
						getUserService().getAnonymousUser()))
				{
					getModelService().remove(oldCart);
				}
			}

			return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
		}
		catch (final Exception e)
		{
			LOG.error("Exception occurred during cart cleanup", e);
			return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
		}
	}

	/**
	 * This method is used to get commerce cart Dao data
	 * 
	 * @return commerceCartDao
	 */
	protected CommerceCartDao getCommerceCartDao()
	{
		return commerceCartDao;
	}

	/**
	 * This method is used to set commerce cart Dao data
	 * 
	 * @param commerceCartDao
	 */
	@Required
	public void setCommerceCartDao(final CommerceCartDao commerceCartDao)
	{
		this.commerceCartDao = commerceCartDao;
	}

	/**
	 * This method is used to get time service object
	 * 
	 * @return
	 */
	protected TimeService getTimeService()
	{
		return timeService;
	}

	/**
	 * This method is used to set time service object
	 * 
	 * @param timeService
	 */
	@Required
	public void setTimeService(final TimeService timeService)
	{
		this.timeService = timeService;
	}

	/**
	 * This method is used to get ModelService object
	 * 
	 * @return modelService
	 */
	protected ModelService getModelService()
	{
		return modelService;
	}

	/**
	 * This method is used to get UserServic object
	 * 
	 * @return userService
	 */
	protected UserService getUserService()
	{
		return userService;
	}

	/**
	 * This method is used to set UserServic object
	 * 
	 * @param userService
	 */
	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}
}
