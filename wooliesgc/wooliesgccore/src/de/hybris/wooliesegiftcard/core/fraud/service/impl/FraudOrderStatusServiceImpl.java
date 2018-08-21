/**
 *
 */
package de.hybris.wooliesegiftcard.core.fraud.service.impl;

import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import de.hybris.wooliesegiftcard.core.constants.WooliesgcCoreConstants;
import de.hybris.wooliesegiftcard.core.fraud.dao.impl.FraudOrderStatusDaoImpl;
import de.hybris.wooliesegiftcard.core.fraud.service.FraudOrderStatusService;
import de.hybris.wooliesegiftcard.core.utility.WooliesCoreCustomerUtility;
import de.hybris.wooliesegiftcard.facades.dto.FraudErrorResponse;
import de.hybris.wooliesegiftcard.facades.dto.FraudOrderStatus;
import de.hybris.wooliesegiftcard.facades.dto.FraudOrderStatusRequest;
import de.hybris.wooliesegiftcard.facades.dto.FraudRequestCron;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.annotation.Resource;
import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Required;



/**
 * @author 669567
 *
 */
public class FraudOrderStatusServiceImpl implements FraudOrderStatusService
{
	private static final Logger LOG = Logger.getLogger(FraudOrderStatusServiceImpl.class.getName());

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;

	@Resource(name = "modelService")
	private ModelService modelService;

	private FraudOrderStatusDaoImpl fraudOrderStatusDao;

	private BaseStoreService baseStoreService;





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

	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
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

	protected BaseStoreService getBaseStoreService()
	{
		return baseStoreService;
	}

	@Required
	public void setBaseStoreService(final BaseStoreService service)
	{
		this.baseStoreService = service;
	}

	/**
	 * @param dateFormat
	 * @param zone
	 * @param localDate
	 * @return String
	 */
	@Override
	public String getTimeWithZone(final String dateFormat, final String zone, final Date localDate)
	{

		final SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
		final TimeZone timeZone = TimeZone.getTimeZone(zone);
		formatter.setTimeZone(timeZone);
		return formatter.format(localDate);
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.wooliesgiftcard.core.fraud.service.FraudOrderStatusService#createFraudCronRequest(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public FraudRequestCron createFraudCronRequest(final String startTimeInString, final String currentTime)
	{
		final FraudRequestCron fraudCronRequest = new FraudRequestCron();
		final String[] startDates = startTimeInString.split(",");
		final String[] endDates = currentTime.split(",");
		fraudCronRequest.setStartDate(startDates[0]);
		fraudCronRequest.setStartTime(startDates[1]);
		fraudCronRequest.setEndDate(endDates[0]);
		fraudCronRequest.setEndTime(endDates[1]);
		return fraudCronRequest;
	}



	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.wooliesgiftcard.core.fraud.service.FraudOrderStatusService#sendFraudRequestApigee(de.hybris.
	 * wooliesegiftcard.facades.dto.FraudRequestCron)
	 */
	@Override
	public boolean sendFraudRequestApigee(final FraudRequestCron fraudRequestCron) throws KeyManagementException,
			NoSuchAlgorithmException
	{
		try
		{
			final String targetURL = configurationService.getConfiguration().getString(WooliesgcCoreConstants.FRAUD_STATUS_API_URL,
					WooliesgcCoreConstants.FRAUD_STATUS_API_URL_DUMMY);
			final String xApiKey = configurationService.getConfiguration().getString(WooliesgcCoreConstants.FRAUD_STATUS_API_KEY,
					WooliesgcCoreConstants.FRAUD_STATUS_API_KEY_DUMMY);
			final int connTimeOut = configurationService.getConfiguration().getInt(WooliesgcCoreConstants.FRAUD_CONN_TIMEOUT,
					WooliesgcCoreConstants.FRAUD_CONN_TIMEOUT_DUMMY);
			final int readTimeOut = configurationService.getConfiguration().getInt(WooliesgcCoreConstants.FRAUD_READ_TIMEOUT,
					WooliesgcCoreConstants.FRAUD_READ_TIMEOUT_DUMMY);
			final String env = configurationService.getConfiguration().getString(WooliesgcCoreConstants.ENV_HYBRIS_FRAUD,
					WooliesgcCoreConstants.ENV_LOCAL);
			final HttpsURLConnection httpConnection = WooliesCoreCustomerUtility.postApigee(targetURL, xApiKey, env, connTimeOut,
					readTimeOut, fraudRequestCron);
			if (httpConnection.getResponseCode() == 200)
			{
				final StringBuilder builder = WooliesCoreCustomerUtility.successfulResponse(httpConnection);
				final FraudOrderStatusRequest fraudOrderResponse = (FraudOrderStatusRequest) createFraudResponseStatus(
						builder.toString(), WooliesgcCoreConstants.SUCCESS_CODE);

				if (null != fraudOrderResponse && CollectionUtils.isNotEmpty(fraudOrderResponse.getOrders()))
				{
					saveOrderStatus(fraudOrderResponse);
				}
				else
				{
					LOG.info("Empty orders in response from APIGEE of Fraud Order Cron");
				}
				return true;
			}
			else
			{
				final StringBuilder builder = getErrorResponse(httpConnection);
				final FraudErrorResponse fraudErrorResponse = (FraudErrorResponse) createFraudResponseStatus(builder.toString(),
						WooliesgcCoreConstants.FAILURE_CODE);
				logError(fraudErrorResponse);
				return false;
			}
		}
		catch (final MalformedURLException e)
		{
			LOG.error("URL Malformed Exception" + e);
			return false;
		}
		catch (final SocketException e)
		{
			LOG.error("Socket Exception" + e);
			return false;
		}
		catch (final IOException e)
		{
			LOG.error("Error in converting response from APIGEE" + e);
			return false;
		}
	}



	/**
	 * This method is used to save order status
	 * 
	 * @param fraudOrderResponse
	 */
	private void saveOrderStatus(final FraudOrderStatusRequest fraudOrderResponse)
	{
		for (final FraudOrderStatus fraudOrderStatus : fraudOrderResponse.getOrders())
		{
			final BaseStoreModel baseStoreModel = getBaseStoreService().getCurrentBaseStore();
			final OrderModel order = fraudOrderStatusDao.findOrderByCodeAndStore(fraudOrderStatus.getMerchantReferenceNumber(),
					baseStoreModel);
			saveOrderStatus(fraudOrderStatus, order);
		}
	}

	/**
	 * @param httpConnection
	 * @return StringBuilder
	 * @throws IOException
	 */
	private static StringBuilder getErrorResponse(final HttpsURLConnection httpConnection) throws IOException
	{
		final BufferedReader responseBuffer = new BufferedReader(new InputStreamReader((httpConnection.getErrorStream())));

		final StringBuilder builder = new StringBuilder();
		String output;
		LOG.debug("Output from Server:\n");
		while ((output = responseBuffer.readLine()) != null)
		{
			builder.append(output);
		}
		LOG.debug("Failure response" + builder);
		responseBuffer.close();
		httpConnection.disconnect();
		return builder;
	}






	/**
	 * @param fraudOrderStatus
	 * @param order
	 */
	private void saveOrderStatus(final FraudOrderStatus fraudOrderStatus, final OrderModel order)
	{
		if (null != order)
		{
			final OrderStatus status = order.getStatus();
			if (status == OrderStatus.ON_VALIDATION
					&& fraudOrderStatus.getOriginalDecision().equalsIgnoreCase(WooliesgcCoreConstants.FRAUD_REVIEW))
			{
				if (fraudOrderStatus.getNewDecision().equalsIgnoreCase(WooliesgcCoreConstants.FRAUD_ACCEPTED))
				{
					order.setStatus(OrderStatus.CHECKED_VALID);
				}
				else if (fraudOrderStatus.getNewDecision().equalsIgnoreCase(WooliesgcCoreConstants.FRAUD_REJECTED))
				{
					order.setStatus(OrderStatus.CHECKED_INVALID);
				}
				modelService.save(order);
			}
			else
			{
				LOG.error("Order Mismatch");
				LOG.error("Order Status for order number " + fraudOrderStatus.getMerchantReferenceNumber() + "from APIGEE is "
						+ fraudOrderStatus.getOriginalDecision());
				LOG.error("Order Status for order number " + fraudOrderStatus.getMerchantReferenceNumber() + "in HYBRIS is "
						+ order.getStatus());
			}
		}
		else
		{

			LOG.info("No Orders available in Hybris System with order number " + fraudOrderStatus.getMerchantReferenceNumber());
		}

	}


	/**
	 * @param fraudResponse
	 * @param code
	 * @return Object
	 * @throws IOException
	 */
	private Object createFraudResponseStatus(final String fraudResponse, final String code) throws IOException
	{
		final ObjectMapper mapper = new ObjectMapper();
		FraudOrderStatusRequest fraudOrderResponse = null;
		FraudErrorResponse fraudErrorResponse = null;

		if (WooliesgcCoreConstants.SUCCESS_CODE.equalsIgnoreCase(code))
		{
			fraudOrderResponse = mapper.readValue(fraudResponse, FraudOrderStatusRequest.class);
			return fraudOrderResponse;
		}
		else
		{
			fraudErrorResponse = mapper.readValue(fraudResponse, FraudErrorResponse.class);
			return fraudErrorResponse;
		}

	}

	/**
	 * @param fraudErrorResponse
	 */
	private void logError(final FraudErrorResponse fraudErrorResponse)
	{
		LOG.error("Error from APIGEE for fraud order Status Cron");
		LOG.error("HttpStatus Code " + fraudErrorResponse.getHttpStatusCode());
		LOG.error("Error Code " + fraudErrorResponse.getErrorCode());
		LOG.error("Error Message " + fraudErrorResponse.getErrorMessage());
		LOG.error("Error Details " + fraudErrorResponse.getErrorDetail());
	}


}
