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
package de.hybris.wooliesegiftcard.core.job;

import de.hybris.platform.commerceservices.enums.QuoteNotificationType;
import de.hybris.platform.commerceservices.event.QuoteToExpireSoonEvent;
import de.hybris.platform.commerceservices.order.dao.CommerceQuoteDao;
import de.hybris.platform.core.enums.QuoteState;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.QuoteModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.time.TimeService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Required;


/**
 * The job finds quotes that qualify for {@link QuoteNotificationType#EXPIRING_SOON} email notification. We can specify
 * when to send the notification by specifying the number of days left to quote expiry
 * {@link QuoteToExpireSoonJobPerformable#DAYS_TO_EXPIRE}. A quote qualifies for the notification if it is in
 * {@link QuoteState#BUYER_OFFER} state and expires in DAYS_TO_EXPIRE days from the current date (Set on
 * {@link QuoteModel#EXPIRATIONTIME}).
 *
 * @since 6.4
 */
public class QuoteToExpireSoonJobPerformable extends AbstractJobPerformable<CronJobModel>
{
	private static final Logger LOG = Logger.getLogger(QuoteToExpireSoonJobPerformable.class);

	protected static final String DAYS_TO_EXPIRE = "quotetoexpiresoonjob.daystoexpire";

	protected static final int DEFAULT_DAYS_TO_EXPIRE = 3;

	private ConfigurationService configurationService;

	private Set<QuoteState> supportedQuoteStatuses;

	private CommerceQuoteDao commerceQuoteDao;

	private EventService eventService;

	private TimeService timeService;

	@Override
	public PerformResult perform(final CronJobModel cronJob)
	{
		final LocalDateTime currentDateTime = getCurrentDateTime();
		final Date expiredAfter = toDate(currentDateTime);
		Date expiredBy = null;

		final int daysToExpire = getConfigurationService().getConfiguration().getInt(DAYS_TO_EXPIRE, DEFAULT_DAYS_TO_EXPIRE);
		if (daysToExpire != -1)
		{
			expiredBy = toDate(currentDateTime.plus(daysToExpire, ChronoUnit.DAYS));
		}

		final SearchResult<QuoteModel> searchResult = getCommerceQuoteDao().findQuotesSoonToExpire(expiredAfter, expiredBy,
				QuoteNotificationType.EXPIRING_SOON, getSupportedQuoteStatuses());

		if (LOG.isDebugEnabled())
		{
			LOG.debug(String.format("Quotes to expire by %s: %s", expiredBy,
					searchResult.getResult().stream().map(AbstractOrderModel::getCode).collect(Collectors.joining(", "))));
		}

		searchResult.getResult().stream().forEach(this::publishQuoteToExpireSoonEvent);

		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}

	/**
	 * To publish quote to expire soon for the event
	 *
	 * @param quoteModel
	 */
	protected void publishQuoteToExpireSoonEvent(final QuoteModel quoteModel)
	{
		final QuoteToExpireSoonEvent quoteToExpireSoonEvent = new QuoteToExpireSoonEvent(quoteModel);

		getEventService().publishEvent(quoteToExpireSoonEvent);
	}

	/**
	 *
	 * @param localDateTime
	 * @return date
	 */
	protected Date toDate(final LocalDateTime localDateTime)
	{
		return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * To get current date time
	 *
	 * @return local time
	 */
	protected LocalDateTime getCurrentDateTime()
	{
		final Date currentDate = getTimeService().getCurrentTime();
		return LocalDateTime.ofInstant(currentDate.toInstant(), ZoneId.systemDefault());
	}

	/**
	 * Get supported quote statuses
	 *
	 * @return supportedQuoteStatuses
	 */
	protected Set<QuoteState> getSupportedQuoteStatuses()
	{
		return supportedQuoteStatuses;
	}

	/**
	 * Set supported quote statuses
	 *
	 * @param supportedQuoteStatuses
	 */
	@Required
	public void setSupportedQuoteStatuses(final Set<QuoteState> supportedQuoteStatuses)
	{
		this.supportedQuoteStatuses = supportedQuoteStatuses;
	}

	/**
	 * Get commerce quote dao
	 * 
	 * @return commerceQuoteDao
	 */
	protected CommerceQuoteDao getCommerceQuoteDao()
	{
		return commerceQuoteDao;
	}

	/**
	 * Set commerce quote dao
	 * 
	 * @param commerceQuoteDao
	 */
	@Required
	public void setCommerceQuoteDao(final CommerceQuoteDao commerceQuoteDao)
	{
		this.commerceQuoteDao = commerceQuoteDao;
	}

	/**
	 * Get event service object
	 * 
	 * @return eventService
	 */
	protected EventService getEventService()
	{
		return eventService;
	}

	/**
	 * Sets event service object
	 * 
	 * @param eventService
	 */
	@Required
	public void setEventService(final EventService eventService)
	{
		this.eventService = eventService;
	}

	/**
	 * Get configuration service object
	 * 
	 * @return configurationService
	 */
	protected ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	/**
	 * Get configuration service object
	 * 
	 * @param configurationService
	 */
	@Required
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	/**
	 * Get time service object
	 * 
	 * @return timeService
	 */
	protected TimeService getTimeService()
	{
		return timeService;
	}

	/**
	 * Set time service object
	 * 
	 * @param timeService
	 */
	@Required
	public void setTimeService(final TimeService timeService)
	{
		this.timeService = timeService;
	}
}
