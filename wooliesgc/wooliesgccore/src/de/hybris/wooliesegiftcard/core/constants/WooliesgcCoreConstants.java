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
package de.hybris.wooliesegiftcard.core.constants;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;

import org.springframework.beans.factory.annotation.Autowired;


/**
 * Global class for all WooliesgcCore constants. You can add global constants for your extension into this class.
 */
public final class WooliesgcCoreConstants extends GeneratedWooliesgcCoreConstants
{
	public static final String EXTENSIONNAME = "wooliesgccore";


	private WooliesgcCoreConstants()
	{
		//empty
	}

	@Autowired
	private static ConfigurationService configurationService;
	// implement here constants used by this extension
	public static final String QUOTE_BUYER_PROCESS = "quote-buyer-process";
	public static final String QUOTE_SALES_REP_PROCESS = "quote-salesrep-process";
	public static final String QUOTE_USER_TYPE = "QUOTE_USER_TYPE";
	public static final String QUOTE_SELLER_APPROVER_PROCESS = "quote-seller-approval-process";
	public static final String QUOTE_TO_EXPIRE_SOON_EMAIL_PROCESS = "quote-to-expire-soon-email-process";
	public static final String QUOTE_EXPIRED_EMAIL_PROCESS = "quote-expired-email-process";
	public static final String QUOTE_POST_CANCELLATION_PROCESS = "quote-post-cancellation-process";
	public static final String PAYMENT_AUTH_KEY = "paymentAuth.url";
	public static final String AUTH_API_KEY = "paymentAuth.apiKey";
	public static final String CONN_TIMEOUT = "apigee.conn.timeout";
	public static final int CONN_TIMEOUT_DUMMY = 5000;
	public static final String READ_TIMEOUT = "apigee.read.timeout";
	public static final int READ_TIMEOUT_DUMMY = 45000;
	public static final String ENV_HYBRIS = "env.hybris";
	public static final String ENV_LOCAL = "local";
	public static final String ENV_HYBRIS_COMPLETION = "env.hybris.completion";
	public static final String ENV_LOCAL_COMPLETION = "local";
	public static final String SUCCESS_CODE = "SUCCESS_CODE";
	public static final String FAILURE_CODE = "FAILURE_CODE";
	public static final String ERR_CODE_URL_MISMATCH = "ERR_30003";
	public static final String ERRMSG_URL_MISMATCH_DESC = "APIGEE URL is not correctly defined";
	public static final String ERRSN_URL_MISMATCH_MSG = "APIGEE url malformed exception";
	public static final String ERR_CODE_SOCKETEXCEPTION = "ERR_30011";
	public static final String ERRMSG_SOCKETEXCEPTION = "Socket Connection Exception";
	public static final String ERRRSN_SOCKETEXCEPTION = "Timeout error from APIGEE";
	public static final String ERR_CODE_IOEXCEPTION = "ERR_30004";
	public static final String ERRMSG_IOEXCEPTION_DESC = "IO Exception while connecting with APIGEE";
	public static final String ERRRSN_IOEXCEPTION_MSG = "IO exception";
	public static final String ERR_CODE_JSON_RESPONSE = "ERR_30002";
	public static final String ERRMSG_JSON_RESPONSE_DESC = "Error in converting response from APIGEE";
	public static final String ERRRSN_JSON_RESPONSE_MSG = "Error in converting response from APIGEE";
	public static final String PAYMENT_AUTH_URL = "https://dev.mobile-api.woolworths.com.au/wow/v1/pay/payments";
	public static final String METHOD_POST = "POST";
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String APPLICATION_JSON = "application/json";
	public static final String API_KEY = "X-Api-Key";
	public static final String AUTHORIZATION = "Authorization";
	public static final String BEARER = "Bearer";
	public static final String PROXY_URL = configurationService.getConfiguration().getString("proxyURL", "10.155.103.186"); // "10.155.103.186";
	public static final String TLS = "TLSv1.2";
	public static final String PAYMENT_CANCEL_URL_DUMMY = "https://dev.mobile-api.woolworths.com.au/wow/v1/pay/voids";
	public static final String PAYMENT_CANCEL_URL = "paymentCancel.url";
	public static final String ENV_DEV = "dev";
	public static final String ENV_UAT = "uat";
	public static final String AUTH_API_KEY_DUMMY = "BcUvzMlA4VTbjbFEtnUgA5wXA42uRPhi";
	public static final String CANCEL_API_KEY = "paymentCancel.apiKey";
	public static final String CANCEL_API_KEY_DUMMY = "BcUvzMlA4VTbjbFEtnUgA5wXA42uRPhi";
	public static final String ERR_CODE_JSON_COVERSION = "ERR_30002";
	public static final String PAYMENT_COMPLETION_KEY = "paymentCompletion.url";
	public static final String PAYMENT_COMPLETION_URL = "https://dev.mobile-api.woolworths.com.au/wow/v1/pay/completions";
	public static final String COMPLETION_API_KEY = "paymentCompletion.apiKey";
	public static final String COMPLETION_API_KEY_DUMMY = "BcUvzMlA4VTbjbFEtnUgA5wXA42uRPhi";
	public static final String DATE_FORMAT = "yyyy-MM-dd,HH:mm:ss";
	public static final String TIMEZONE_UTC = "UTC";

	public static final String FRAUD_ACCEPTED = "ACCEPT";
	public static final String FRAUD_REVIEW = "REVIEW";
	public static final String FRAUD_REJECTED = "REJECT";

	//Fraud Request Cron API
	public static final String FRAUD_STATUS_API_URL = "fraud.status.api.url";
	public static final String FRAUD_STATUS_API_KEY = "fraud.status.apiKey";
	public static final String FRAUD_STATUS_API_URL_DUMMY = "https://uat.mobile-api.woolworths.com.au/wow/v1/gcp/fraud/status";
	public static final String FRAUD_STATUS_API_KEY_DUMMY = "BcUvzMlA4VTbjbFEtnUgA5wXA42uRPhi";

	public static final String FRAUD_CONN_TIMEOUT = "fraud.apigee.conn.timeout";
	public static final int FRAUD_CONN_TIMEOUT_DUMMY = 5000;
	public static final String FRAUD_READ_TIMEOUT = "fraud.apigee.read.timeout";
	public static final int FRAUD_READ_TIMEOUT_DUMMY = 45000;

	public static final String ENV_HYBRIS_FRAUD = "env.hybris.fraud";

	public enum PAYMENT_OPTIONS
	{
		PAY_1001, PAY_1002, PAY_1003;
	}

	public static final String FIND_ORDERS_BY_CODE_STORE_QUERY = "SELECT {" + OrderModel.PK + "}, {" + OrderModel.CREATIONTIME
			+ "}, {" + OrderModel.CODE + "} FROM {" + OrderModel._TYPECODE + "} WHERE {" + OrderModel.CODE + "} = ?code AND {"
			+ OrderModel.VERSIONID + "} IS NULL AND {" + OrderModel.STORE + "} = ?store";

}
