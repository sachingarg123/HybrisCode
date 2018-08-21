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
package de.hybris.wooliesegiftcard.facades.constants;


import de.hybris.platform.servicelayer.config.ConfigurationService;

import org.springframework.beans.factory.annotation.Autowired;


/**
 * Global class for all WooliesgcFacades constants.
 */
public class WooliesgcFacadesConstants extends GeneratedWooliesgcFacadesConstants
{
	@Autowired
	private static ConfigurationService configurationService;

	public static final String EXTENSIONNAME = "wooliesgcfacades";
	public static final String DATEFORMAT = "dd-MMMM-yyyy";
	public static final String FIRSTNAME = "firstName";
	public static final String LASTNAME = "lastName";
	public static final String PHONE = "phone";
	public static final String UID = "uid";
	public static final String EMAIL = "email";
	public static final String OPTINFORMARKETING = "optInForMarketing";
	public static final String POLICYAGREEMENT = "policyAgreement";
	public static final String PLASTICCARD = "Plastic";
	public static final String EGIFTCARD = "Egift";
	public static final String B2B = "B2B";
	public static final String B2C = "B2C";
	public static final String MEM = "MEM";
	public static final String PAYMENT_OPTION_PAY_NOW_ERROR_CODE = "MSG_101";
	public static final String DUMMY = "DUMMY";
	public static final String OPTIONS_SEPARATOR = ",";
	public static final String FULL_STATUS = "PENDING,APPROVED";
	public static final String PAYMENT_AUTH_URL = "https://dev.mobile-api.woolworths.com.au/wow/v1/pay/payments";
	public static final String PAYMENT_AUTH_KEY = "paymentAuth.url";
	public static final String ENV_HYBRIS = "env.hybris";
	public static final String ENV_LOCAL = "local";
	public static final String ENV_DEV = "dev";
	public static final String ENV_UAT = "uat";
	public static final String AUTH_API_KEY = "paymentAuth.apiKey";
	public static final String AUTH_API_KEY_DUMMY = "BcUvzMlA4VTbjbFEtnUgA5wXA42uRPhi";
	public static final String PRE_AUTH_DATA = "PREAUTH";
	public static final String FRAUD_ACCEPTED = "ACCEPT";
	public static final String FRAUD_REVIEW = "REVIEW";
	public static final String FRAUD_REJECTED = "REJECT";
	public static final String CREDIT_CARD = "CREDIT_CARD";
	public static final String BANK_TRANSFER = "BANK_TRANSFER";
	public static final String ON_ACCOUNT = "ON_ACCOUNT";
	public static final String SUCCESS_CODE = "SUCCESS_CODE";
	public static final String FAILURE_CODE = "FAILURE_CODE";
	public static final String ERR_CODE_JSON_RESPONSE = "ERR_30002";
	public static final String ERRMSG_JSON_RESPONSE_DESC = "Error in converting response from APIGEE";
	public static final String ERRRSN_JSON_RESPONSE_MSG = "Error in converting response from APIGEE";
	public static final String ERR_CODE_URL_MISMATCH = "ERR_30003";
	public static final String ERRMSG_URL_MISMATCH_DESC = "APIGEE URL is not correctly defined";
	public static final String ERRSN_URL_MISMATCH_MSG = "APIGEE url malformed exception";
	public static final String ERR_CODE_IOEXCEPTION = "ERR_30004";
	public static final String ERRMSG_IOEXCEPTION_DESC = "IO Exception while connecting with APIGEE";
	public static final String ERRRSN_IOEXCEPTION_MSG = "IO exception";
	public static final String ERR_CODE_BILLING_ADDRESS = "ERR_50003";
	public static final String ERRMSG_BILLING_ADDRESS = "Address ID is not valid";
	public static final String ERRRSN_BILLING_ADDRESS = "Address ID does not exist in System";
	public static final String ERR_CODE_GUEST_CART = "ERR_80070";
	public static final String DOB = "yyyy-MM-dd";
	public static final String BILLING_TYPE = "Billing";
	public static final String SHIPPING_TYPE = "Shipping";

	public static final String B2BADMINGROUP = "b2badmingroup";
	public static final String START_HHMMSS = "00:00:00";
	public static final String END_HHMMSS = "23:59:59";

	public static final String ERRCODE_ORDERCREDITLIMIT = "ERR_30009";
	public static final String ERRMSG_ORDERCREDITLIMIT = "On account mode is not enabled for the respective unit or order total price exceeded than credit limit";
	public static final String ERRRSN_ORDERCREDITLIMIT = "On account mode is not enabled for the respective unit or order total price exceeded than credit limit";
	public static final String ERR_CODE_SOCKETEXCEPTION = "ERR_30011";
	public static final String ERRMSG_SOCKETEXCEPTION = "Socket Connection Exception";
	public static final String ERRRSN_SOCKETEXCEPTION = "Timeout error from APIGEE";
	public static final String CONN_TIMEOUT = "apigee.conn.timeout";
	public static final int CONN_TIMEOUT_DUMMY = 5000;
	public static final String READ_TIMEOUT = "apigee.read.timeout";
	public static final int READ_TIMEOUT_DUMMY = 45000;
	public static final String FRAUD_VERSION_DEFAULT = "CyberSourceTransaction_1.101";
	public static final String FRAUD_PROVIDER_DEFAULT = "cybersource";
	public static final String FRAUD_PROVIDER = "cybersource.provider";
	public static final String FRAUD_VERSION = "cybersource.version";
	public static final String FRAUD_FORMAT = "cybersource.format.zip";
	public static final String FRAUD_RESPFORMAT = "cybersource.responseFormat.zip";
	public static final String FRAUD_FORMAT_DEFAULT = "ZIP_BASE_64_ENCODED";
	public static final String FRAUD_RESPFORMAT_DEFAULT = "ZIP_BASE_64_ENCODED";
	public static final String FRAUD_FORMAT_XML = "cybersource.format.xml";
	public static final String FRAUD_RESPFORMAT_XML = "cybersource.responseFormat.xml";
	public static final String FRAUD_FORMAT_DEFAULT_XML = "XML";
	public static final String FRAUD_RESPFORMAT_DEFAULT_XML = "XML";
	public static final String MODEL_NOT_FOUND = "40012";

	public static final String ERR_CODE_ORDER_NOT_FOUND = "ERR_30012";
	public static final String ERRMSG_ORDER_NOT_FOUND = "Order not found";
	public static final String ERRRSN_ORDER_NOT_FOUND = "Order not found";

	public static final String ERR_CODE_INCORRECT_PAYLOAD = "ERR_10005";
	public static final String ERRMSG_INCORRECT_PAYLOAD = "Payload is incorrect";
	public static final String ERRRSN_INCORRECT_PAYLOAD = "Payload is incorrect";

	public static final String ERR_CODE_INCORRECT_ORDER_STATUS = "ERR_30013";
	public static final String ERRMSG_INCORRECT_ORDER_STATUS = "Order Status mismatch: API will only update orders which are in Review status";
	public static final String ERRRSN_INCORRECT_ORDER_STATUS = "Original decision in System is ";

	public static final String ERR_CODE_PARSE_FRAUD_XML = "ERR_30014";
	public static final String ERRMSG_PARSE_FRAUD_XML = "Error in parsing Fraud XML";
	public static final String ERRRSN_PARSE_FRAUD_XML = "Error in parsing Fraud XML";

	public static final String ERR_CODE_TRANSFORM_XML = "ERR_30015";
	public static final String ERRMSG_TRANSFORM_XML = "Error in transforming Fraud xml";
	public static final String ERRRSN_TRANSFORM_XML = "Error in transforming Fraud xml";


	//FraudXML Constants
	public static final String ELEMENT_REQUEST = "requestMessage";
	public static final String ELEMENT_REQUEST_ATTIBUTE = "xmlns";
	public static final String ELEMENT_REQUEST_ATTIBUTE_VALUE = "urn:schemas-cybersource-com:transaction-data-1.101";
	public static final String ELEMENT_MERCHANTID = "merchantID";
	public static final String ELEMENT_MERCHANTID_VALUE = "gfs_giftcards";
	public static final String MERCHANTID_DIGIPAY = "digipay.merchantid";
	public static final String ELEMENT_MERCHANT_REF_CODE = "merchantReferenceCode";
	public static final String ELEMENT_BILL_TO = "billTo";
	public static final String ELEMENT_SHIP_TO = "shipTo";
	public static final String ELEMENT_FIRST_NAME = "firstName";
	public static final String ELEMENT_LAST_NAME = "lastName";
	public static final String ELEMENT_STREET = "street1";
	public static final String ELEMENT_CITY = "city";
	public static final String ELEMENT_STATE = "state";
	public static final String ELEMENT_POSTALCODE = "postalCode";
	public static final String ELEMENT_COUNTRY = "country";
	public static final String ELEMENT_PHONE = "phoneNumber";
	public static final String ELEMENT_EMAIL = "email";
	public static final String ELEMENT_IP_ADDRESS = "ipAddress";
	public static final String ELEMENT_CUSTOMER_ID = "customerID";

	public static final String ELEMENT_PURCHASE_TOTALS = "purchaseTotals";
	public static final String ELEMENT_CURRENCY = "currency";
	public static final String ELEMENT_GRAND_TOTAL_AMOUNT = "grandTotalAmount";

	public static final String ELEMENT_ITEM = "item";
	public static final String ELEMENT_UNIT_PRICE = "unitPrice";
	public static final String ELEMENT_QUANTITY = "quantity";
	public static final String ELEMENT_PRODUCT_NAME = "productName";
	public static final String ELEMENT_PRODUCT_SKU = "productSKU";

	public static final String ELEMENT_MERCHANT_DEFINED_DATA = "merchantDefinedData";
	public static final String ELEMENT_MDD_FIELD = "mddField";
	public static final String ELEMENT_AFS_SERVICE = "afsService";
	public static final String ATTRIBUTE_AFS_SERVICE = "run";
	public static final String ATTRIBUTE_AFS_SERVICE_VALUE = "true";
	public static final String ELEMENT_FINGERPRINT = "deviceFingerprintID";
	public static final String FRAUD_DATE_FORMAT = "yyyy-MM-dd HH:mm";
	public static final String AUS_ZONE = "Australia/Sydney";
	public static final String ATTRIBUTE_ID = "id";
	public static final String ATTRIBUTE_ID_VALUE_1 = "1";
	public static final String ATTRIBUTE_ID_VALUE_2 = "2";
	public static final String ATTRIBUTE_ID_VALUE_12 = "12";
	public static final String ATTRIBUTE_ID_VALUE_16 = "16";
	public static final String ATTRIBUTE_ID_VALUE_17 = "17";
	public static final String ATTRIBUTE_ID_VALUE_18 = "18";
	public static final String ATTRIBUTE_ID_VALUE_19 = "19";
	public static final String ATTRIBUTE_ID_VALUE_20 = "20";
	public static final String ATTRIBUTE_ID_VALUE_57 = "57";
	public static final String ATTRIBUTE_ID_VALUE_59 = "59";
	public static final String ATTRIBUTE_ID_VALUE_12_DATA = "NO";
	public static final String ATTRIBUTE_ID_VALUE_16_DATA = "0";
	public static final String ATTRIBUTE_ID_VALUE_20_DATA = "WEB";
	public static final String ATTRIBUTE_ID_VALUE_57_PUB = "PUB";
	public static final String ATTRIBUTE_ID_VALUE_57_COR = "COR";
	public static final String ATTRIBUTE_ID_VALUE_57_MEM = "MEM";
	public static final String ATTRIBUTE_ID_VALUE_59_DATA = "0";
	public static final String ATTRIBUTE_ID_VALUE_DATA_PHYSICAL = "mdd.field19.physical";
	public static final String ATTRIBUTE_ID_VALUE_PHYSICAL_DATA_DEFAULT = "GC_P";
	public static final String ATTRIBUTE_ID_VALUE_DATA_DIGITAL = "mdd.field19.digital";
	public static final String ATTRIBUTE_ID_VALUE_DIGITAL_DATA_DEFAULT = "GC_D";
	public static final String ATTRIBUTE_ID_VALUE_DATA_MIXED = "mdd.field19.mixed";
	public static final String ATTRIBUTE_ID_VALUE_MIXED_DATA_DEFAULT = "GC_P";
	public static final String USER_CREATION_DATE_FORMAT = "dd MM yyyy";
	public static final String PROXY_URL = configurationService.getConfiguration().getString("proxyURL", "10.155.103.186"); // "10.155.103.186";


	public static final String METHOD_POST = "POST";
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String APPLICATION_JSON = "application/json";
	public static final String API_KEY = "X-Api-Key";
	public static final String AUTHORIZATION = "Authorization";
	public static final String BEARER = "Bearer";
	public static final String TLS = "TLSv1.2";
	public static final String XML_OUTPUT_RENDER = "{http://xml.apache.org/xslt}indent-amount";
	public static final String CYBERSOURCE_ACTIVE = "cybersource.active";
	public static final String CYBERSOURCE_ACTIVE_DEFAULT = "ZIP";
	public static final String CYBERSOURCE_ACTIVE_DEFAULT_XML = "XML";

	public static final String ERRCODE_ORDERLIMIT = "50004";
	public static final String ERRMSG_ORDERLIMIT = "orderLimit is mandotory for Buyer ";
	public static final String ERRRSN_ORDERLIMIT = "orderLimit is mandotory for Buyer ";
	public static final String ERRCODE_ISNOTADMIN = "70001";
	public static final String ERRMSG_ISNOTADMIN = "Customer is not a B2B admin user";
	public static final String ERRRSN_ISNOTADMIN = "Customer is not a B2B admin user";
	public static final String ERRCODE_ISNOTATIVE = "70002";
	public static final String ERRMSG_ISNOTATIVE = "Only for Buyer users orderlimit will update";
	public static final String ERRRSN_ISNOTATIVE = "Only for Buyer users orderlimit will update";
	public static final String ERRCODE_ISNOTEXIST = "70003";
	public static final String ERRMSG_ISNOTEXIST = "Customer is not a B2B exist user";
	public static final String ERRRSN_ISNOTEXIST = "Customer is not a B2B exist user";
	public static final String ERRCODE_ADDADMINSANDBUYERS = "70008";
	public static final String ERRMSG_ADDADMINSANDBUYERS = "Only B2B admin only can add buyers and admin ";
	public static final String ERRRSN_ADDADMINSANDBUYERS = "Only B2B admin only can add buyers and admin";
	public static final String ERRCODE_EMAILFORMAT = "80003";
	public static final String ERRMSG_EMAILFORMAT = "please enter valid email format";
	public static final String ERRRSN_EMAILFORMAT = "please enter valid email format";
	public static final String ERRCODE_USER_ALREADY_AVAILABLE = "ERR_40012";
	public static final String ERRCODE_IMAGENOTEXIST = "70005";
	public static final String ERRCODE_PIDNOTAVAILABLE = "40027";
	public static final String ERRMSG_PIDNOTAVAILABLE = "PID is not avilable on this cart";
	public static final String ERRRSN_PIDNOTAVAILABLE = "PID is not avilable on this cart";

	public static final String CURRENCYNOTNULL = "80008";
	public static final String DEFAULTIMAGEPROPERTY = "default.image.for.eGiftCard";
	public static final String DEFAULTIMAGE = "DefaultImageForGiftCard";
	public static final String MEDIADATA = "code,url";

	//Bulk Order
	public static final String BULK_ORDER_VALIDATION_ERROR = "ValidationError";
	public static final String BULK_ORDER_MISSING_VALUE_ERROR = "MissingValue";
	public static final String BULK_ORDER_SKU_CODE = "skuCode";
	public static final String BULK_ORDER_UNIT_PRICE = "unitPrice";
	public static final String BULK_ORDER_NO_VALUE_ERROR = "No value provided for the property";
	public static final String BULK_ORDER_MESSAGE = "message";
	public static final String BULK_ORDER_EMAIL_NOT_VALID = "Email is not valid";
	public static final String IMAGE_DOES_NOT_EXIST_CODE = "40012";

	public static final int TWO = 2;
	public static final int FIVE = 5;
	public static final int NINE = 9;
	public static final int TEN = 10;

	public enum PAYMENT_OPTIONS
	{
		PAY_1001, PAY_1002, PAY_1003;
	}

	private WooliesgcFacadesConstants()
	{
		//empty
	}
}
