/**
 *
 */
package de.hybris.wooliesegiftcard.core.apigee.service.impl;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.wooliesegiftcard.core.apigee.service.ApigeeOrderDetailsService;
import de.hybris.wooliesegiftcard.core.constants.WooliesgcCoreConstants;
import de.hybris.wooliesegiftcard.core.exceptions.WooliesServiceLayerException;
import de.hybris.wooliesegiftcard.core.utility.WooliesCoreCustomerUtility;
import de.hybris.wooliesegiftcard.facades.dto.CancelPaymentError;
import de.hybris.wooliesegiftcard.facades.dto.CancelPaymentRequest;
import de.hybris.wooliesegiftcard.facades.dto.CancelPaymentResponse;
import de.hybris.wooliesegiftcard.facades.dto.VoidRequests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.net.ssl.HttpsURLConnection;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;


/**
 * @author 687679
 *
 */
public class ApigeeOrderDetailsServiceImpl implements ApigeeOrderDetailsService
{

	private static final Logger LOG = Logger.getLogger(ApigeeOrderDetailsServiceImpl.class);
	@Resource(name = "configurationService")
	private ConfigurationService configurationService;


	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.wooliesegiftcard.core.apigee.service.ApigeeOrderDetailsService#sendPaymentCancelRequest(de.hybris.
	 * platform.core.model.order.OrderModel)
	 */
	@Override
	public boolean sendPaymentCancelRequest(final OrderModel orderModel) throws WooliesServiceLayerException
	{
		try
		{
			final String targetURL = configurationService.getConfiguration().getString(WooliesgcCoreConstants.PAYMENT_CANCEL_URL,
					WooliesgcCoreConstants.PAYMENT_CANCEL_URL_DUMMY);
			final String xApiKey = configurationService.getConfiguration().getString(WooliesgcCoreConstants.CANCEL_API_KEY,
					WooliesgcCoreConstants.CANCEL_API_KEY_DUMMY);

			final int connTimeOut = configurationService.getConfiguration().getInt(WooliesgcCoreConstants.CONN_TIMEOUT,
					WooliesgcCoreConstants.CONN_TIMEOUT_DUMMY);
			final int readTimeOut = configurationService.getConfiguration().getInt(WooliesgcCoreConstants.READ_TIMEOUT,
					WooliesgcCoreConstants.READ_TIMEOUT_DUMMY);
			final String env = configurationService.getConfiguration().getString(WooliesgcCoreConstants.ENV_HYBRIS,
					WooliesgcCoreConstants.ENV_LOCAL_COMPLETION);
			final CancelPaymentRequest cancelPaymentRequest = setPayloadforCancel(orderModel);

			final HttpsURLConnection httpConnection = WooliesCoreCustomerUtility.postApigee(targetURL, xApiKey, env, connTimeOut,
					readTimeOut, cancelPaymentRequest);

			if (httpConnection.getResponseCode() == 200)
			{
				final StringBuilder builder = WooliesCoreCustomerUtility.successfulResponse(httpConnection);

				final CancelPaymentResponse cancelPaymentResponse = (CancelPaymentResponse) createCancelPaymentResponse(
						builder.toString(), WooliesgcCoreConstants.SUCCESS_CODE);
				LOG.info("Cancel Payment Request for Order:" + orderModel.getCode());
				LOG.info("Transaction Reciept of order is" + cancelPaymentResponse.getTransactionReceipt());
				LOG.info("PaymentTransactionReferenNumber is" + cancelPaymentResponse.getTransactionReceipt());
				return true;

			}
			else
			{
				final BufferedReader responseBuffer = new BufferedReader(new InputStreamReader((httpConnection.getErrorStream())));

				final StringBuilder builder = new StringBuilder();
				String output;
				LOG.info("Output from Server(APIGEE):\n");
				while ((output = responseBuffer.readLine()) != null)
				{
					builder.append(output);
				}
				LOG.info("Failure response" + builder);
				responseBuffer.close();
				httpConnection.disconnect();
				return false;
			}
		}
		catch (final KeyManagementException e)
		{
			LOG.error(e);
			throw new WooliesServiceLayerException(WooliesgcCoreConstants.ERR_CODE_URL_MISMATCH,
					WooliesgcCoreConstants.ERRMSG_URL_MISMATCH_DESC, WooliesgcCoreConstants.ERRSN_URL_MISMATCH_MSG);
		}

		catch (final NoSuchAlgorithmException e)
		{
			LOG.error(e);
			throw new WooliesServiceLayerException(WooliesgcCoreConstants.ERR_CODE_URL_MISMATCH,
					WooliesgcCoreConstants.ERRMSG_URL_MISMATCH_DESC, WooliesgcCoreConstants.ERRSN_URL_MISMATCH_MSG);
		}

		catch (final MalformedURLException e)
		{
			LOG.error(e);
			throw new WooliesServiceLayerException(WooliesgcCoreConstants.ERR_CODE_URL_MISMATCH,
					WooliesgcCoreConstants.ERRMSG_URL_MISMATCH_DESC, WooliesgcCoreConstants.ERRSN_URL_MISMATCH_MSG);
		}
		catch (final SocketException e)
		{
			LOG.error(e);
			throw new WooliesServiceLayerException(WooliesgcCoreConstants.ERR_CODE_SOCKETEXCEPTION,
					WooliesgcCoreConstants.ERRMSG_SOCKETEXCEPTION, WooliesgcCoreConstants.ERRRSN_SOCKETEXCEPTION);
		}
		catch (final IOException e)
		{
			LOG.error(e);
			throw new WooliesServiceLayerException(WooliesgcCoreConstants.ERR_CODE_IOEXCEPTION,
					WooliesgcCoreConstants.ERRMSG_IOEXCEPTION_DESC, WooliesgcCoreConstants.ERRRSN_IOEXCEPTION_MSG);
		}

	}

	/**
	 * @param String
	 *           for the cancelpaymentResponse
	 * @param successCode
	 *           the response code we get
	 * @return Object as a response
	 * @throws WooliesServiceLayerException
	 *            throws this exception in case of any error
	 */
	private Object createCancelPaymentResponse(final String cancelpaymentResponse, final String successCode)
			throws WooliesServiceLayerException
	{
		final ObjectMapper mapper = new ObjectMapper();
		CancelPaymentResponse cancelPaymentResponseObject = null;
		CancelPaymentError cancelPaymentError = null;
		try
		{
			if (WooliesgcCoreConstants.SUCCESS_CODE.equalsIgnoreCase(successCode))
			{
				cancelPaymentResponseObject = mapper.readValue(cancelpaymentResponse, CancelPaymentResponse.class);
				return cancelPaymentResponseObject;
			}
			else
			{
				cancelPaymentError = mapper.readValue(cancelpaymentResponse, CancelPaymentError.class);
				return cancelPaymentError;
			}
		}
		catch (final IOException e)
		{
			LOG.error(e);
			throw new WooliesServiceLayerException(WooliesgcCoreConstants.ERR_CODE_JSON_RESPONSE,
					WooliesgcCoreConstants.ERRMSG_JSON_RESPONSE_DESC, WooliesgcCoreConstants.ERRRSN_JSON_RESPONSE_MSG);
		}


	}

	/**
	 * @param orderModel
	 *           the Order for which Cancellation has to happen
	 * @return CancelPaymentRequest to be sent to APIGEE
	 */
	private CancelPaymentRequest setPayloadforCancel(final OrderModel orderModel)
	{
		final CancelPaymentRequest cancelPaymentRequest = new CancelPaymentRequest();
		final VoidRequests voids = new VoidRequests();
		cancelPaymentRequest.setClientReference(orderModel.getCode());
		cancelPaymentRequest.setOrderNumber(orderModel.getCode());
		voids.setPaymentTransactionRef(orderModel.getPaymentInfo().getPaymentTransactionRef());
		final List<VoidRequests> voidsList = new ArrayList<>();
		voidsList.add(voids);
		cancelPaymentRequest.setVoids(voidsList);
		return cancelPaymentRequest;
	}
}
