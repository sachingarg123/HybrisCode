/**
 *
 */
package de.hybris.wooliesgiftcard.core.wex.service.impl;

import de.hybris.platform.core.enums.ExportStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.wooliesegiftcard.core.constants.WooliesgcCoreConstants;
import de.hybris.wooliesegiftcard.core.enums.UserDataType;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BCustomerModel;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BUnitModel;
import de.hybris.wooliesegiftcard.facades.dto.CorporateAccountDetail;
import de.hybris.wooliesegiftcard.facades.dto.DeliveryDetail;
import de.hybris.wooliesegiftcard.facades.dto.Order;
import de.hybris.wooliesegiftcard.facades.dto.PublicPurchaserDetail;
import de.hybris.wooliesegiftcard.facades.dto.WexIntegrationError;
import de.hybris.wooliesegiftcard.facades.dto.WoolworthsOrderRequest;
import de.hybris.wooliesegiftcard.facades.dto.WoolworthsOrderResponse;
import de.hybris.wooliesgiftcard.core.wex.service.WexOrderDetailsService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author 416910
 *
 */
public class WexOrderDetailsServiceImpl implements WexOrderDetailsService
{
	private static final Logger LOG = Logger.getLogger(WexOrderDetailsServiceImpl.class.getName());

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;

	@Resource(name = "modelService")
	private ModelService modelService;

	@Autowired
	private BusinessProcessService businessProcessService;

	private static final String CUSTOMER_NAME = "Test User";

	private static final String WEX_INVOICE_EMAIL_ADDRESS = "wex.taxinvoice.email.address";

	private static final String WEX_INVOICE_EMAIL_ADDRESS_VALUE = "taxinvoice@giftcards.com.au";

	private static final String LOCAL_ENVIRONMENT = "local_env";

	public WoolworthsOrderRequest createWexRequestPayload(final OrderModel orderModel)
	{
		final CustomerModel customer = (CustomerModel) orderModel.getUser();
		LOG.info("Customer Info" + customer.getUid());

		final WoolworthsOrderRequest woolworthsOrderRequest = new WoolworthsOrderRequest();


		final DeliveryDetail deliveryDetail = new DeliveryDetail();

		woolworthsOrderRequest.setRequestMessageId(orderModel.getCode() != null ? orderModel.getCode() : "");
		woolworthsOrderRequest.setPurchaseOrderReference(orderModel.getCode() != null ? orderModel.getCode() : "");
		woolworthsOrderRequest.setDeliveryMethod("1");
		woolworthsOrderRequest.setStoreId("123456");
		woolworthsOrderRequest.setCorporateAccountId("0");
		String paymentTypeId = "";
		String paymentDetails = "";
		if (orderModel.getPaymentInfo() != null)
		{
			if ("PAY_1001".equals(orderModel.getPaymentInfo().getPaymentOption().getCode()))
			{
				paymentDetails = "CREDIT CARD";
				paymentTypeId = "1";
			}
			if ("PAY_1002".equals(orderModel.getPaymentInfo().getPaymentOption().getCode()))
			{
				paymentDetails = "PAY LATER";
				paymentTypeId = "2";
			}
			if ("PAY_1003".equals(orderModel.getPaymentInfo().getPaymentOption().getCode()))
			{
				paymentDetails = "ON ACCOUNT";
				paymentTypeId = "4";
			}
		}
		woolworthsOrderRequest.setPaymentTypeId(paymentTypeId);
		woolworthsOrderRequest.setPaymentDetail(paymentDetails);
		woolworthsOrderRequest.setFreightFee(new Double(orderModel.getDeliveryCost()));
		boolean deliveryOption = false;

		if (orderModel.getDeliveryMode() != null)
		{
			deliveryOption = setDeliveryModeValue(orderModel);
		}
		woolworthsOrderRequest.setIsRegisteredPost(deliveryOption);
		woolworthsOrderRequest.setDiscountTypeId("10");
		final Double discountPercentage = new Double(10);
		woolworthsOrderRequest.setDiscountPercentage(discountPercentage);
		woolworthsOrderRequest.setDiscountAuthNumber("");
		woolworthsOrderRequest.setOrganisationNumber("");
		final boolean isBulkOrder = true;
		woolworthsOrderRequest.setIsBulkOrder(isBulkOrder);
		woolworthsOrderRequest.setSpecialInstruction("");
		woolworthsOrderRequest.setOrderType("2");
		if (customer.getCustomerType() == UserDataType.B2B)
		{
			woolworthsOrderRequest.setPurchaserType("1");
		}
		else
		{
			woolworthsOrderRequest.setPurchaserType("2");
		}



		if (customer.getCustomerType() == UserDataType.B2B)
		{
			final CorporateAccountDetail corporateAccountDetail = new CorporateAccountDetail();
			final CorporateB2BCustomerModel corporateb2bcustomerModel = (CorporateB2BCustomerModel) orderModel.getUser();
			final CorporateB2BUnitModel corporateb2BUnit = (CorporateB2BUnitModel) corporateb2bcustomerModel.getDefaultB2BUnit();

			corporateAccountDetail.setAccountName(corporateb2BUnit.getName() != null ? corporateb2BUnit.getName() : "");

			String firstName = "";
			String lastName = "";
			String address1 = "";
			String address2 = "";
			String address3 = "";
			String suburb = "";
			String state = "";
			String postCode = "";

			if (CollectionUtils.isNotEmpty(corporateb2bcustomerModel.getAddresses()))
			{
				for (final AddressModel address : corporateb2bcustomerModel.getAddresses())
				{
					firstName = address.getFirstname();
					lastName = address.getLastname();
					address1 = address.getStreetname();
					address2 = address.getStreetnumber();
					address3 = address.getStreetname();
					suburb = address.getTown();
					state = address.getDistrict();
					postCode = address.getPostalcode();
				}
			}

			corporateAccountDetail.setFirstName(corporateb2bcustomerModel.getFirstName() != null ? corporateb2bcustomerModel
					.getFirstName() : CUSTOMER_NAME);
			corporateAccountDetail.setLastName(corporateb2bcustomerModel.getLastName() != null ? corporateb2bcustomerModel
					.getLastName() : CUSTOMER_NAME);
			corporateAccountDetail.setTitle("");
			final String taxInvoiceEmail = configurationService.getConfiguration().getString(WEX_INVOICE_EMAIL_ADDRESS,
					WEX_INVOICE_EMAIL_ADDRESS_VALUE);
			corporateAccountDetail.setEmail(taxInvoiceEmail);
			corporateAccountDetail
					.setPhone(corporateb2bcustomerModel.getPhone() != null ? corporateb2bcustomerModel.getPhone() : "");
			corporateAccountDetail.setFax("");

			corporateAccountDetail.setAddress1(address1);
			corporateAccountDetail.setAddress2(address2);
			corporateAccountDetail.setSuburb(suburb);
			corporateAccountDetail.setState(state);
			corporateAccountDetail.setPostCode(postCode);
			corporateAccountDetail.setCountryId("106");
			woolworthsOrderRequest.setCorporateAccountDetail(corporateAccountDetail);

			deliveryDetail.setRecipientFirstName(firstName);
			deliveryDetail.setRecipientLastName(lastName);
			deliveryDetail.setRecipientTitle(" ");
			deliveryDetail.setRecipientEmail(taxInvoiceEmail);
			deliveryDetail.setRecipientPhone(corporateb2bcustomerModel.getPhone() != null ? corporateb2bcustomerModel.getPhone()
					: "");
			deliveryDetail.setRecipientFax(" ");
			deliveryDetail.setRecipientAddress1(address1);
			deliveryDetail.setRecipientAddress2(address2);
			deliveryDetail.setRecipientAddress3(address3);
			deliveryDetail.setRecipientSuburb(suburb);
			deliveryDetail.setRecipientState(state);
			deliveryDetail.setRecipientPostCode(postCode);
			deliveryDetail.setRecipientCountryId("106");
			deliveryDetail.setRecipientCompanyName(corporateb2BUnit.getName() != null ? corporateb2BUnit.getName() : "");
		}
		else
		{
			final PublicPurchaserDetail publicPurchaserDetail = new PublicPurchaserDetail();

			String firstName = "";
			String lastName = "";
			String address1 = "";
			String address2 = "";
			String address3 = "";
			String suburb = "";
			String state = "";
			String postCode = "";

			if (CollectionUtils.isNotEmpty(customer.getAddresses()))
			{
				for (final AddressModel address : customer.getAddresses())
				{
					firstName = address.getFirstname();
					lastName = address.getLastname();
					address1 = address.getStreetname();
					address2 = address.getStreetnumber();
					address3 = address.getStreetname();
					suburb = address.getTown();
					state = address.getDistrict();
					postCode = address.getPostalcode();
				}
			}
			publicPurchaserDetail.setFirstName(customer.getFirstName() != null ? customer.getFirstName() : CUSTOMER_NAME);
			publicPurchaserDetail.setLastName(customer.getLastName() != null ? customer.getLastName() : CUSTOMER_NAME);
			publicPurchaserDetail.setTitle(" ");
			final String taxInvoiceEmail = configurationService.getConfiguration().getString(WEX_INVOICE_EMAIL_ADDRESS,
					WEX_INVOICE_EMAIL_ADDRESS_VALUE);
			publicPurchaserDetail.setEmail(taxInvoiceEmail);
			publicPurchaserDetail.setPhone(customer.getPhone() != null ? customer.getPhone() : "");
			publicPurchaserDetail.setFax(" ");

			publicPurchaserDetail.setAddress1(address1);
			publicPurchaserDetail.setAddress2(address2);
			publicPurchaserDetail.setSuburb(suburb);
			publicPurchaserDetail.setState(state);
			publicPurchaserDetail.setPostCode(postCode);
			publicPurchaserDetail.setCountryId("106");
			woolworthsOrderRequest.setPublicPurchaserDetail(publicPurchaserDetail);

			deliveryDetail.setRecipientFirstName(firstName);
			deliveryDetail.setRecipientLastName(lastName);
			deliveryDetail.setRecipientTitle(" ");
			deliveryDetail.setRecipientEmail(taxInvoiceEmail);
			deliveryDetail.setRecipientPhone(customer.getPhone() != null ? customer.getPhone() : "");
			deliveryDetail.setRecipientFax(" ");
			deliveryDetail.setRecipientAddress1(address1);
			deliveryDetail.setRecipientAddress2(address2);
			deliveryDetail.setRecipientAddress3(address3);
			deliveryDetail.setRecipientSuburb(suburb);
			deliveryDetail.setRecipientState(state);
			deliveryDetail.setRecipientPostCode(postCode);
			deliveryDetail.setRecipientCountryId("106");
			deliveryDetail.setRecipientCompanyName("B2C");
		}

		final List<Order> orderList = new ArrayList<Order>();
		for (final AbstractOrderEntryModel orderEntry : orderModel.getEntries())
		{
			final Order orderData = new Order();
			orderData.setOrderReferenceId(orderEntry.getEntryNumber().toString() != null ? orderEntry.getEntryNumber().toString()
					: "");
			orderData.setVoucherProductCode(orderEntry.getProduct() != null ? orderEntry.getProduct().getCode() : "");
			orderData.setAmount(orderEntry.getBasePrice());
			orderData.setQuantity(new Double(orderEntry.getQuantity()));
			final Double additionalDiscountVoucher = new Double(0);
			orderData.setAdditionalDiscountVouchers(additionalDiscountVoucher);
			orderData.setIncludeCarrier(true);
			orderData.setCardDescriptor(" ");
			orderData.setValidFrom(" ");
			orderData.setValidTo(" ");
			orderData.setExternalLogo(orderEntry.getCoBrandID() != null ? orderEntry.getCoBrandID() : "");
			orderList.add(orderData);
		}

		woolworthsOrderRequest.setDeliveryDetail(deliveryDetail);
		woolworthsOrderRequest.setOrders(orderList);
		return woolworthsOrderRequest;
	}

	public List<WoolworthsOrderRequest> createWexRequestPayloadForMixOrder(final OrderModel orderModel)
	{
		final CustomerModel customer = (CustomerModel) orderModel.getUser();
		LOG.info("Customer Info" + customer.getUid());
		final List<WoolworthsOrderRequest> woolworthsMixOrderList = new ArrayList<WoolworthsOrderRequest>();

		for (final AbstractOrderEntryModel ordreEntryModel : orderModel.getEntries())
		{
			final WoolworthsOrderRequest woolworthsOrderRequest = new WoolworthsOrderRequest();
			final DeliveryDetail deliveryDetail = new DeliveryDetail();
			if (ordreEntryModel.getProduct().getIsEGiftCard().booleanValue())
			{
				woolworthsOrderRequest.setRequestMessageId(orderModel.getCode() != null ? "E_" + orderModel.getCode() : "");
			}
			else
			{
				woolworthsOrderRequest.setRequestMessageId(orderModel.getCode() != null ? "P_" + orderModel.getCode() : "");
			}
			woolworthsOrderRequest.setPurchaseOrderReference(orderModel.getCode() != null ? orderModel.getCode() : "");
			woolworthsOrderRequest.setDeliveryMethod("1");
			woolworthsOrderRequest.setStoreId("123456");
			woolworthsOrderRequest.setCorporateAccountId("0");
			String paymentTypeId = "";
			String paymentDetails = "";
			if (orderModel.getPaymentInfo() != null)
			{
				if ("PAY_1001".equals(orderModel.getPaymentInfo().getPaymentOption().getCode()))
				{
					paymentDetails = "CREDIT CARD";
					paymentTypeId = "1";
				}
				if ("PAY_1002".equals(orderModel.getPaymentInfo().getPaymentOption().getCode()))
				{
					paymentDetails = "PAY LATER";
					paymentTypeId = "2";
				}
				if ("PAY_1003".equals(orderModel.getPaymentInfo().getPaymentOption().getCode()))
				{
					paymentDetails = "ON ACCOUNT";
					paymentTypeId = "4";
				}
			}
			woolworthsOrderRequest.setPaymentTypeId(paymentTypeId);
			woolworthsOrderRequest.setPaymentDetail(paymentDetails);
			woolworthsOrderRequest.setFreightFee(new Double(orderModel.getDeliveryCost()));
			boolean deliveryOption = false;

			if (orderModel.getDeliveryMode() != null)
			{
				deliveryOption = setDeliveryModeValue(orderModel);
			}
			woolworthsOrderRequest.setIsRegisteredPost(deliveryOption);
			woolworthsOrderRequest.setDiscountTypeId("10");
			final Double discountPercentage = new Double(10);
			woolworthsOrderRequest.setDiscountPercentage(discountPercentage);
			woolworthsOrderRequest.setDiscountAuthNumber("");
			woolworthsOrderRequest.setOrganisationNumber("");
			final boolean isBulkOrder = true;
			woolworthsOrderRequest.setIsBulkOrder(isBulkOrder);
			woolworthsOrderRequest.setSpecialInstruction("");
			woolworthsOrderRequest.setOrderType("2");
			if (customer.getCustomerType() == UserDataType.B2B)
			{
				woolworthsOrderRequest.setPurchaserType("1");
			}
			else
			{
				woolworthsOrderRequest.setPurchaserType("2");
			}



			if (customer.getCustomerType() == UserDataType.B2B)
			{
				final CorporateAccountDetail corporateAccountDetail = new CorporateAccountDetail();
				final CorporateB2BCustomerModel corporateb2bcustomerModel = (CorporateB2BCustomerModel) orderModel.getUser();
				final CorporateB2BUnitModel corporateb2BUnit = (CorporateB2BUnitModel) corporateb2bcustomerModel.getDefaultB2BUnit();
				corporateAccountDetail.setAccountName(corporateb2BUnit.getName() != null ? corporateb2BUnit.getName() : "");
				setCorporatePersonalInfo(woolworthsOrderRequest, corporateAccountDetail, deliveryDetail, corporateb2BUnit,
						corporateb2bcustomerModel);
			}
			else
			{
				final PublicPurchaserDetail publicPurchaserDetail = new PublicPurchaserDetail();
				setPublicPurchaserPersonalInfo(woolworthsOrderRequest, publicPurchaserDetail, customer, deliveryDetail);
			}

			final List<Order> orderList = new ArrayList<Order>();
			final Order orderData = new Order();
			orderData.setOrderReferenceId(ordreEntryModel.getEntryNumber().toString() != null ? ordreEntryModel.getEntryNumber()
					.toString() : "");
			orderData.setVoucherProductCode(ordreEntryModel.getProduct() != null ? ordreEntryModel.getProduct().getCode() : "");
			orderData.setAmount(ordreEntryModel.getBasePrice());
			orderData.setQuantity(new Double(ordreEntryModel.getQuantity()));
			final Double additionalDiscountVoucher = new Double(0);
			orderData.setAdditionalDiscountVouchers(additionalDiscountVoucher);
			orderData.setIncludeCarrier(true);
			orderData.setCardDescriptor(" ");
			orderData.setValidFrom(" ");
			orderData.setValidTo(" ");
			orderData.setExternalLogo(ordreEntryModel.getCoBrandID() != null ? ordreEntryModel.getCoBrandID() : "");
			orderList.add(orderData);

			woolworthsOrderRequest.setDeliveryDetail(deliveryDetail);
			woolworthsOrderRequest.setOrders(orderList);
			woolworthsMixOrderList.add(woolworthsOrderRequest);
		}
		return woolworthsMixOrderList;
	}

	@Override
	public boolean sendOrderDetailsToWex(final WoolworthsOrderRequest woolworthsOrderRequest,
			final List<WexIntegrationError> wexIntegrationErrorList, final OrderModel orderModel) throws KeyManagementException,
			NoSuchAlgorithmException
	{
		try
		{
			final String targetURL = configurationService.getConfiguration().getString("wex.endpoint.url",
					"https://dev.mobile-api.woolworths.com.au/wow/v2/giftcards/orders");

			final String xApiKey = configurationService.getConfiguration().getString("wex.endpoint.apikey",
					"BcUvzMlA4VTbjbFEtnUgA5wXA42uRPhi");

			final int connTimeOut = configurationService.getConfiguration().getInt("wex.connection.time.out", 5000);
			final int readTimeOut = configurationService.getConfiguration().getInt("wex.read.time.out", 50000);
			final String env = configurationService.getConfiguration().getString("wex.env.hybris", LOCAL_ENVIRONMENT);

			final HttpsURLConnection httpConnection = postOrderDataToWex(targetURL, xApiKey, env, connTimeOut, readTimeOut,
					woolworthsOrderRequest);

			if (httpConnection.getResponseCode() == 200)
			{
				final BufferedReader responseBuffer = new BufferedReader(new InputStreamReader((httpConnection.getInputStream())));
				final StringBuilder builder = new StringBuilder();
				String output;
				LOG.info("OUT From WEX...");
				while ((output = responseBuffer.readLine()) != null)
				{
					builder.append(output);
				}
				LOG.info(builder);
				responseBuffer.close();
				httpConnection.disconnect();

				final WoolworthsOrderResponse wexIntegrationResponse = (WoolworthsOrderResponse) createWexResponse(
						builder.toString(), "SUCCESS_CODE");
				if (wexIntegrationResponse != null)
				{
					populateInvoiceNumber(orderModel, wexIntegrationResponse);
					orderModel.setExportStatus(ExportStatus.EXPORTED);
					modelService.save(orderModel);
					final OrderProcessModel orderProcessModel = businessProcessService.createProcess("invoiceTaxEmailProcess-"
							+ orderModel.getCode() + "-" + System.currentTimeMillis(), "invoiceTaxEmailProcess");
					orderProcessModel.setOrder(orderModel);
					modelService.save(orderProcessModel);
					businessProcessService.startProcess(orderProcessModel);
				}
				return true;

			}
			else
			{
				final BufferedReader responseBuffer = new BufferedReader(new InputStreamReader((httpConnection.getErrorStream())));

				final StringBuilder builder = new StringBuilder();
				String output;
				LOG.info("Out Put From Server");
				while ((output = responseBuffer.readLine()) != null)
				{
					builder.append(output);
				}
				LOG.info(builder);
				responseBuffer.close();
				httpConnection.disconnect();

				final WexIntegrationError wexIntegrationError = (WexIntegrationError) createWexResponse(builder.toString(),
						"FAILURE_CODE");
				wexIntegrationErrorList.add(wexIntegrationError);
				return false;
			}
		}
		catch (final MalformedURLException e)
		{
			LOG.error(e);
		}
		catch (final IOException e)
		{
			LOG.error(e);
		}
		return false;
	}

	public static Object createWexResponse(final String wexResponse, final String code)
	{
		final ObjectMapper mapper = new ObjectMapper();
		WoolworthsOrderResponse wexResponseObject = null;
		WexIntegrationError wexIntegrationError = null;
		try
		{
			if ("SUCCESS_CODE".equalsIgnoreCase(code))
			{
				wexResponseObject = mapper.readValue(wexResponse, WoolworthsOrderResponse.class);
				return wexResponseObject;
			}
			else
			{
				wexIntegrationError = mapper.readValue(wexResponse, WexIntegrationError.class);
				return wexIntegrationError;
			}
		}
		catch (final IOException e)
		{

			LOG.error(e);
		}
		return wexResponseObject;
	}

	public static HttpsURLConnection postOrderDataToWex(final String targetURL, final String xApiKey, final String env,
			final int connTimeOut, final int readTimeOut, final Object payLoad) throws IOException, NoSuchAlgorithmException,
			KeyManagementException
	{
		HttpsURLConnection httpConnection = null;
		final URL targetUrl = new URL(targetURL);
		final ObjectMapper mapper = new ObjectMapper();

		if (LOCAL_ENVIRONMENT.equalsIgnoreCase(env))
		{
			final String cognizantProxyIpAddress = WooliesgcCoreConstants.PROXY_URL;
			final InetSocketAddress proxyInet = new InetSocketAddress(cognizantProxyIpAddress, 6050);
			final Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyInet);
			httpConnection = (HttpsURLConnection) targetUrl.openConnection(proxy);
		}
		else
		{
			httpConnection = (HttpsURLConnection) targetUrl.openConnection();
		}
		httpConnection.setConnectTimeout(connTimeOut);
		httpConnection.setReadTimeout(readTimeOut);
		httpConnection.setDoOutput(true);
		httpConnection.setRequestMethod("POST");
		httpConnection.setRequestProperty("content-type", "application/json");

		httpConnection.setRequestProperty("x-api-key", xApiKey);
		if (LOCAL_ENVIRONMENT.equalsIgnoreCase(env))
		{
			// Set up a Trust all manager
			final TrustManager[] trustAllCerts = new TrustManager[]
			{ new X509TrustManager()
			{

				public java.security.cert.X509Certificate[] getAcceptedIssuers()
				{
					return new java.security.cert.X509Certificate[0];
				}

				@Override
				public void checkClientTrusted(final java.security.cert.X509Certificate[] certs, final String authType)
				{
					//
				}

				@Override
				public void checkServerTrusted(final java.security.cert.X509Certificate[] certs, final String authType)
				{
					//
				}
			} };
			final SSLContext sc = SSLContext.getInstance("TLSv1.2");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			httpConnection.setSSLSocketFactory(sc.getSocketFactory());
			final HostnameVerifier allHostsValid = new HostnameVerifier()
			{

				public boolean verify(final String hostname, final SSLSession session)
				{
					return true;
				}
			};
			httpConnection.setHostnameVerifier(allHostsValid);

		}

		final String input = mapper.writeValueAsString(payLoad);
		LOG.info("REQUEST PAYLOAD FOR WEX::" + input);
		OutputStream outputStream = null;
		try
		{
			outputStream = httpConnection.getOutputStream();
			outputStream.write(input.getBytes());
			outputStream.flush();
		}
		finally
		{
			if (null != outputStream)
			{
				outputStream.close();
			}
		}
		return httpConnection;

	}

	public boolean setDeliveryModeValue(final OrderModel orderModel)
	{
		boolean deliveryOption = false;
		if (orderModel.getDeliveryMode().getCode() != null)
		{
			if ("PGC1002".equals(orderModel.getDeliveryMode().getCode()))
			{
				deliveryOption = false;
			}
			else
			{
				deliveryOption = true;
			}
		}
		else
		{
			deliveryOption = false;
		}
		return deliveryOption;
	}

	public void setCorporatePersonalInfo(final WoolworthsOrderRequest woolworthsOrderRequest,
			final CorporateAccountDetail corporateAccountDetail, final DeliveryDetail deliveryDetail,
			final CorporateB2BUnitModel corporateb2BUnit, final CorporateB2BCustomerModel corporateb2bcustomerModel)
	{
		String firstName = "";
		String lastName = "";
		String address1 = "";
		String address2 = "";
		String address3 = "";
		String suburb = "";
		String state = "";
		String postCode = "";

		if (CollectionUtils.isNotEmpty(corporateb2bcustomerModel.getAddresses()))
		{
			for (final AddressModel address : corporateb2bcustomerModel.getAddresses())
			{
				firstName = address.getFirstname();
				lastName = address.getLastname();
				address1 = address.getStreetname();
				address2 = address.getStreetnumber();
				address3 = address.getStreetname();
				suburb = address.getTown();
				state = address.getDistrict();
				postCode = address.getPostalcode();
			}
		}

		corporateAccountDetail.setFirstName(corporateb2bcustomerModel.getFirstName() != null ? corporateb2bcustomerModel
				.getFirstName() : CUSTOMER_NAME);
		corporateAccountDetail.setLastName(corporateb2bcustomerModel.getLastName() != null ? corporateb2bcustomerModel
				.getLastName() : CUSTOMER_NAME);
		corporateAccountDetail.setTitle("");
		final String taxInvoiceEmail = configurationService.getConfiguration().getString(WEX_INVOICE_EMAIL_ADDRESS,
				WEX_INVOICE_EMAIL_ADDRESS_VALUE);
		corporateAccountDetail.setEmail(taxInvoiceEmail);
		corporateAccountDetail.setPhone(corporateb2bcustomerModel.getPhone() != null ? corporateb2bcustomerModel.getPhone() : "");
		corporateAccountDetail.setFax("");

		corporateAccountDetail.setAddress1(address1);
		corporateAccountDetail.setAddress2(address2);
		corporateAccountDetail.setSuburb(suburb);
		corporateAccountDetail.setState(state);
		corporateAccountDetail.setPostCode(postCode);
		corporateAccountDetail.setCountryId("106");
		woolworthsOrderRequest.setCorporateAccountDetail(corporateAccountDetail);

		deliveryDetail.setRecipientFirstName(firstName);
		deliveryDetail.setRecipientLastName(lastName);
		deliveryDetail.setRecipientTitle(" ");
		deliveryDetail.setRecipientEmail(taxInvoiceEmail);
		deliveryDetail.setRecipientPhone(corporateb2bcustomerModel.getPhone() != null ? corporateb2bcustomerModel.getPhone() : "");
		deliveryDetail.setRecipientFax(" ");
		deliveryDetail.setRecipientAddress1(address1);
		deliveryDetail.setRecipientAddress2(address2);
		deliveryDetail.setRecipientAddress3(address3);
		deliveryDetail.setRecipientSuburb(suburb);
		deliveryDetail.setRecipientState(state);
		deliveryDetail.setRecipientPostCode(postCode);
		deliveryDetail.setRecipientCountryId("106");
		deliveryDetail.setRecipientCompanyName(corporateb2BUnit.getName() != null ? corporateb2BUnit.getName() : "");
	}

	public void setPublicPurchaserPersonalInfo(final WoolworthsOrderRequest woolworthsOrderRequest,
			final PublicPurchaserDetail publicPurchaserDetail, final CustomerModel customer, final DeliveryDetail deliveryDetail)
	{
		String firstName = "";
		String lastName = "";
		String address1 = "";
		String address2 = "";
		String address3 = "";
		String suburb = "";
		String state = "";
		String postCode = "";

		if (CollectionUtils.isNotEmpty(customer.getAddresses()))
		{
			for (final AddressModel address : customer.getAddresses())
			{
				firstName = address.getFirstname();
				lastName = address.getLastname();
				address1 = address.getStreetname();
				address2 = address.getStreetnumber();
				address3 = address.getStreetname();
				suburb = address.getTown();
				state = address.getDistrict();
				postCode = address.getPostalcode();
			}
		}
		publicPurchaserDetail.setFirstName(customer.getFirstName() != null ? customer.getFirstName() : CUSTOMER_NAME);
		publicPurchaserDetail.setLastName(customer.getLastName() != null ? customer.getLastName() : CUSTOMER_NAME);
		publicPurchaserDetail.setTitle(" ");
		final String taxInvoiceEmail = configurationService.getConfiguration().getString(WEX_INVOICE_EMAIL_ADDRESS,
				WEX_INVOICE_EMAIL_ADDRESS_VALUE);
		publicPurchaserDetail.setEmail(taxInvoiceEmail);
		publicPurchaserDetail.setPhone(customer.getPhone() != null ? customer.getPhone() : "");
		publicPurchaserDetail.setFax(" ");

		publicPurchaserDetail.setAddress1(address1);
		publicPurchaserDetail.setAddress2(address2);
		publicPurchaserDetail.setSuburb(suburb);
		publicPurchaserDetail.setState(state);
		publicPurchaserDetail.setPostCode(postCode);
		publicPurchaserDetail.setCountryId("106");
		woolworthsOrderRequest.setPublicPurchaserDetail(publicPurchaserDetail);

		deliveryDetail.setRecipientFirstName(firstName);
		deliveryDetail.setRecipientLastName(lastName);
		deliveryDetail.setRecipientTitle(" ");
		deliveryDetail.setRecipientEmail(taxInvoiceEmail);
		deliveryDetail.setRecipientPhone(customer.getPhone() != null ? customer.getPhone() : "");
		deliveryDetail.setRecipientFax(" ");
		deliveryDetail.setRecipientAddress1(address1);
		deliveryDetail.setRecipientAddress2(address2);
		deliveryDetail.setRecipientAddress3(address3);
		deliveryDetail.setRecipientSuburb(suburb);
		deliveryDetail.setRecipientState(state);
		deliveryDetail.setRecipientPostCode(postCode);
		deliveryDetail.setRecipientCountryId("106");
		deliveryDetail.setRecipientCompanyName("B2C");
	}

	public void populateInvoiceNumber(final OrderModel orderModel, final WoolworthsOrderResponse wexIntegrationResponse)
	{
		if (orderModel.getInvoiceNumber() != null)
		{
			orderModel.setInvoiceNumber(orderModel.getInvoiceNumber() + "|" + wexIntegrationResponse.getInvoiceNo());
		}
		else
		{
			orderModel.setInvoiceNumber(wexIntegrationResponse.getInvoiceNo());
		}
	}



}
