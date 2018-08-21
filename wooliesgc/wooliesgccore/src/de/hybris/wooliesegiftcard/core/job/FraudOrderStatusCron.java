/**
 *
 */
package de.hybris.wooliesegiftcard.core.job;

import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.wooliesegiftcard.core.constants.WooliesgcCoreConstants;
import de.hybris.wooliesegiftcard.core.fraud.dao.impl.FraudOrderStatusDaoImpl;
import de.hybris.wooliesegiftcard.core.fraud.service.impl.FraudOrderStatusServiceImpl;
import de.hybris.wooliesegiftcard.facades.dto.FraudRequestCron;

import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author 416910
 *
 */
public class FraudOrderStatusCron extends AbstractJobPerformable<CronJobModel>
{
	private static final Logger LOG = Logger.getLogger(FraudOrderStatusCron.class.getName());

	private FraudOrderStatusDaoImpl fraudOrderStatusDao;

	private FraudOrderStatusServiceImpl fraudOrderStatusService;

	private ConfigurationService configurationService;




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
	 * @return the fraudOrderStatusDao
	 */
	public FraudOrderStatusDaoImpl getFraudOrderStatusDao()
	{
		return fraudOrderStatusDao;
	}



	/**
	 * @param fraudOrderStatusDao
	 *           the fraudOrderStatusDao to set
	 */
	public void setFraudOrderStatusDao(final FraudOrderStatusDaoImpl fraudOrderStatusDao)
	{
		this.fraudOrderStatusDao = fraudOrderStatusDao;
	}



	/**
	 * @return the fraudOrderStatusService
	 */
	public FraudOrderStatusServiceImpl getFraudOrderStatusService()
	{
		return fraudOrderStatusService;
	}



	/**
	 * @param fraudOrderStatusService
	 *           the fraudOrderStatusService to set
	 */
	public void setFraudOrderStatusService(final FraudOrderStatusServiceImpl fraudOrderStatusService)
	{
		this.fraudOrderStatusService = fraudOrderStatusService;
	}



	/*
	 * perform method
	 *
	 * @param job CronJobModel
	 *
	 * @return PerformResult result
	 */
	@Override
	public PerformResult perform(final CronJobModel job)
	{
		final FraudRequestCron fraudCronRequest;


		//get current time and go bak 24 hours for start date
		final Calendar startTime = Calendar.getInstance();
		final String currentTime = fraudOrderStatusService.getTimeWithZone(WooliesgcCoreConstants.DATE_FORMAT,
				WooliesgcCoreConstants.TIMEZONE_UTC, startTime.getTime());
		if (StringUtils.isEmpty(job.getLastSuccessTime()))
		{
			startTime.add(Calendar.HOUR, -23);
			startTime.add(Calendar.MINUTE, -55);
			final String startTimeInString = fraudOrderStatusService.getTimeWithZone(WooliesgcCoreConstants.DATE_FORMAT,
					WooliesgcCoreConstants.TIMEZONE_UTC, startTime.getTime());
			fraudCronRequest = fraudOrderStatusService.createFraudCronRequest(startTimeInString, currentTime);
		}
		else
		{
			final String startOldTime = job.getLastSuccessTime();
			fraudCronRequest = fraudOrderStatusService.createFraudCronRequest(startOldTime, currentTime);
		}





		boolean responseStatus = false;
		try
		{
			responseStatus = fraudOrderStatusService.sendFraudRequestApigee(fraudCronRequest);
		}
		catch (KeyManagementException | NoSuchAlgorithmException ex)
		{
			LOG.error(ex.getMessage());
			try
			{
				throw ex;
			}
			catch (final GeneralSecurityException e)
			{
				LOG.error(e);
			}
		}
		if (responseStatus)
		{
			LOG.info("Cron executed Successfully");
			job.setLastSuccessTime(currentTime);
			modelService.save(job);
			return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
		}
		else
		{
			LOG.info("Recieved Error response from APIGEE ");
			return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
		}

	}





}
