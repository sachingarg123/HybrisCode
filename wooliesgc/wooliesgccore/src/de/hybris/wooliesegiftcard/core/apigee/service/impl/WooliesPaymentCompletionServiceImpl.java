/**
 *
 */
package de.hybris.wooliesegiftcard.core.apigee.service.impl;

import de.hybris.platform.commercefacades.order.impl.DefaultCheckoutFacade;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.wooliesegiftcard.core.apigee.service.WooliesPaymentCompletionService;
import de.hybris.wooliesegiftcard.core.constants.WooliesgcCoreConstants;
import de.hybris.wooliesegiftcard.core.exceptions.WooliesServiceLayerException;
import de.hybris.wooliesegiftcard.core.utility.WooliesCoreCustomerUtility;
import de.hybris.wooliesegiftcard.facades.dto.CompletionResponses;
import de.hybris.wooliesegiftcard.facades.dto.Completions;
import de.hybris.wooliesegiftcard.facades.dto.PaymentCompletionError;
import de.hybris.wooliesegiftcard.facades.dto.PaymentCompletionRequest;
import de.hybris.wooliesegiftcard.facades.dto.PaymentCompletionResponse;

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

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;


/**
 * @author 653930
 *
 */
public class WooliesPaymentCompletionServiceImpl extends DefaultCheckoutFacade implements WooliesPaymentCompletionService
{
	private static final Logger LOG = Logger.getLogger(WooliesPaymentCompletionServiceImpl.class);
	@Resource(name = "configurationService")
	private ConfigurationService configurationService;

	@Resource
	private TypeService typeService;

	@Override
	public boolean sendPaymentCompletionRequest(final OrderModel order) throws WooliesServiceLayerException
	{

		try
		{
			final String targetURL = configurationService.getConfiguration().getString(WooliesgcCoreConstants.PAYMENT_COMPLETION_KEY,
					WooliesgcCoreConstants.PAYMENT_COMPLETION_URL);
			final String xApiKey = configurationService.getConfiguration().getString(WooliesgcCoreConstants.COMPLETION_API_KEY,
					WooliesgcCoreConstants.COMPLETION_API_KEY_DUMMY);
			final int connTimeOut = configurationService.getConfiguration().getInt(WooliesgcCoreConstants.CONN_TIMEOUT,
					WooliesgcCoreConstants.CONN_TIMEOUT_DUMMY);
			final int readTimeOut = configurationService.getConfiguration().getInt(WooliesgcCoreConstants.READ_TIMEOUT,
					WooliesgcCoreConstants.READ_TIMEOUT_DUMMY);

			final PaymentCompletionRequest payLoad = setPaymentCompletionPayload(order);
			final String env = configurationService.getConfiguration().getString(WooliesgcCoreConstants.ENV_HYBRIS_COMPLETION,
					WooliesgcCoreConstants.ENV_LOCAL_COMPLETION);
			final HttpsURLConnection httpConnection = WooliesCoreCustomerUtility.postApigee(targetURL, xApiKey, env, connTimeOut,
					readTimeOut, payLoad);


			if (httpConnection.getResponseCode() == 200)
			{
				final BufferedReader responseBuffer = new BufferedReader(new InputStreamReader((httpConnection.getInputStream())));
				final StringBuilder builder = new StringBuilder();
				String output;
				LOG.debug("Output from Server:\n");
				while ((output = responseBuffer.readLine()) != null)
				{
					builder.append(output);
				}
				LOG.debug("Sucess response" + builder);
				responseBuffer.close();
				httpConnection.disconnect();

				final PaymentInfoModel paymentInfoModel = setPmentcompletionResponse(order, builder);
				order.setPaymentInfo(paymentInfoModel);
				getModelService().save(order);
				return true;

			}
			else
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
				return false;
			}
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

		catch (final KeyManagementException e)
		{
			LOG.error(e);
			throw new WooliesServiceLayerException(WooliesgcCoreConstants.ERR_CODE_IOEXCEPTION,
					WooliesgcCoreConstants.ERRMSG_IOEXCEPTION_DESC, WooliesgcCoreConstants.ERRRSN_IOEXCEPTION_MSG);
		}

		catch (final NoSuchAlgorithmException e)
		{
			LOG.error(e);
			throw new WooliesServiceLayerException(WooliesgcCoreConstants.ERR_CODE_IOEXCEPTION,
					WooliesgcCoreConstants.ERRMSG_IOEXCEPTION_DESC, WooliesgcCoreConstants.ERRRSN_IOEXCEPTION_MSG);
		}
	}

	PaymentInfoModel setPmentcompletionResponse(final OrderModel order, final StringBuilder builder)
			throws WooliesServiceLayerException
	{
		final PaymentCompletionResponse paymentcompletionResponse = (PaymentCompletionResponse) createPaymentCompletionResponse(
				builder.toString(), WooliesgcCoreConstants.SUCCESS_CODE);

		LOG.debug("Transaction Reciept of order is" + paymentcompletionResponse.getTransactionReceipt());
		LOG.debug("PaymentTransactionReferenNumber is" + paymentcompletionResponse.getTransactionReceipt());
		final PaymentInfoModel paymentInfoModel = order.getPaymentInfo();

		if (CollectionUtils.isNotEmpty(paymentcompletionResponse.getCompletionResponses()))
		{
			for (final CompletionResponses completionResponses : paymentcompletionResponse.getCompletionResponses())
			{
				paymentInfoModel.setPaymentTransactionRef(completionResponses.getPaymentTransactionRef());
			}
		}
		return paymentInfoModel;
	}

	private PaymentCompletionRequest setPaymentCompletionPayload(final OrderModel order) throws IOException
	{
		final PaymentCompletionRequest paymentcompltRequest = new PaymentCompletionRequest();
		final Completions completionsData = new Completions();

		paymentcompltRequest.setClientReference(order.getCode());
		paymentcompltRequest.setOrderNumber(order.getCode());
		completionsData.setAmount(order.getTotalPrice());
		completionsData.setPaymentTransactionRef(order.getPaymentInfo().getPaymentTransactionRef());

		final List<Completions> paymentCompletionsDataList = new ArrayList<>();
		paymentCompletionsDataList.add(completionsData);
		paymentcompltRequest.setCompletions(paymentCompletionsDataList);

		return paymentcompltRequest;
	}

	public Object createPaymentCompletionResponse(final String paymentResponse, final String code)
			throws WooliesServiceLayerException
	{
		final ObjectMapper mapper = new ObjectMapper();
		PaymentCompletionResponse paymentResponseObject = null;
		PaymentCompletionError paymentcompletionError = null;
		try
		{
			if (WooliesgcCoreConstants.SUCCESS_CODE.equalsIgnoreCase(code))
			{
				paymentResponseObject = mapper.readValue(paymentResponse, PaymentCompletionResponse.class);
				return paymentResponseObject;
			}
			else
			{
				paymentcompletionError = mapper.readValue(paymentResponse, PaymentCompletionError.class);
				return paymentcompletionError;
			}
		}
		catch (final IOException e)
		{
			LOG.error(e);
			throw new WooliesServiceLayerException(WooliesgcCoreConstants.ERR_CODE_JSON_RESPONSE,
					WooliesgcCoreConstants.ERRMSG_JSON_RESPONSE_DESC, WooliesgcCoreConstants.ERRRSN_JSON_RESPONSE_MSG);
		}


	}

}
