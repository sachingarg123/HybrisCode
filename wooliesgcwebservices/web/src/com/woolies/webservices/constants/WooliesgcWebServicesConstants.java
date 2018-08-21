/**
 *
 */
package com.woolies.webservices.constants;

/**
 * @author 668982 This class is used to maintain constants for Wooliesgc Webservice
 */
public class WooliesgcWebServicesConstants
{
	public static final String ERRCODE_CARDLIMIT = "40001";
	public static final StringBuilder ERRMSG_CARDLIMIT = new StringBuilder("Please enter a value between ");
	public static final StringBuilder ERRRSN_CARDLIMIT = new StringBuilder("Price for Card must be between ");

	public static final String ERRCODE_CUSTOMERPRICE = "40002";
	public static final String ERRMSG_CUSTOMERPRICE = "Please provide customer price for entry";
	public static final String ERRRSN_CUSTOMERPRICE = "Customer prce is required for the card, as price for the product is 0 in database";

	public static final String ERRCODE_ORDERCONFIRMATION_ORDER_NOT_FOUND = "50011";
	public static final String ERRMSG_ORDERCONFIRMATION_ORDER_NOT_FOUND = "Order not found in current BaseStore";

	public static final String ERRCODE_DELIVERYMODE = "40003";
	public static final String ERRMSG_DELIVERYMODE = "No Delivery modes are supported for the current cart";
	public static final String ERRRSN_DELIVERYMODE = "No Delivery modes are supported for the current cart";

	public static final String ERRCODE_ENTRYNOTFOUND = "40004";
	public static final String ERRMSG_ENTRYNOTFOUND = "Entry not found";
	public static final String ERRRSN_ENTRYNOTFOUND = "Provided entry is not found for the given cart";

	public static final String ERRCODE_ISNOTEGIFTCARD = "40005";
	public static final String ERRMSG_ISNOTEGIFTCARD = "Product is not an e-gift card";
	public static final String ERRRSN_ISNOTEGIFTCARD = "PID will be generated for only e-gift card line items.";

	public static final String ERRCODE_ISNOTPIDEXIST = "40006";
	public static final String ERRMSG_ISNOTPIDEXIST = "PID doesn't exist for the mentioned line item";
	public static final String ERRRSN_ISNOTPIDEXIST = "PID doesn't exist for the mentioned line item";


	public static final String ERRCODE_ENTRYPRODUCTNOTSAME = "40007";
	public static final String ERRMSG_ENTRYPRODUCTNOTSAME = "Product code is not same";
	public static final String ERRRSN_ENTRYPRODUCTNOTSAME = "Product code is not same";

	public static final String ERRCODE_ISNOTISOEXIST = "40008";
	public static final String ERRMSG_ISNOTISOEXIST = "ISO code doesnot Exist";
	public static final String ERRRSN_ISNOTISOEXIST = "ISO code doesnot Exist";

	public static final String ERRCODE_ISNOTCATEGORYEXIST = "40009";
	public static final String ERRMSG_ISNOTCATEGORYEXIST = "Category  doesnot Exist for this Catalog";
	public static final String ERRRSN_ISNOTCATEGORYEXIST = "Category  doesnot Exist for this Catalog";

	public static final String ERRCODE_ISNOTUSEREXIST = "40010";
	public static final String ERRMSG_ISNOTUSEREXIST = "Cannot find user with this uid";
	public static final String ERRRSN_ISNOTUSEREXIST = "Cannot find user with this uid";

	public static final String ISOCODE_AU = "AU";
	public static final String ISOCODE_NZ = "NZ";

	public static final String CATALOG_AU = "auWooliesProductCatalog";
	public static final String CATALOG_NZ = "nzWooliesProductCatalog";
	public static final String CATALOGVERSION_ONLINE = "Online";


	public static final String ERRCODE_PAYMENT = "ERR_30001";
	public static final String ERRMSG_PAYMENT_DESC = "Available Credit limit is not set in backOffice";
	public static final String ERRRSN_PAYMENT_MSG = "Available Credit limit is null";

	public static final String ERR_CODE_URL_MISMATCH = "ERR_30003";
	public static final String ERRMSG_URL_MISMATCH_DESC = "APIGEE URL is not correctly defined";
	public static final String ERRSN_URL_MISMATCH_MSG = "APIGEE url malformed exception";

	public static final String ERR_CODE_IOEXCEPTION = "ERR_30004";
	public static final String ERRMSG_IOEXCEPTION_DESC = "IO Exception while connecting with APIGEE";
	public static final String ERRRSN_IOEXCEPTION_MSG = "IO exception";


	public static final String ERR_CODE_ACCESS_TOKEN = "ERR_30005";
	public static final String ERR_CODE_STEPUP_TOKEN = "ERR_30006";
	public static final String ERR_CODE_PAYMENT_INSTRUMENT_ID = "ERR_30007";
	public static final String ERR_CODE_PAYMENT_OPTIONS = "ERR_30008";
	public static final String ERRMSG_PAYMENT_OPTIONS = "Payment option is incorrect";
	public static final String ERRRSN_PAYMENT_OPTIONS = "Payment option must be";



	public static final String ERR_CODE_JSON_RESPONSE = "ERR_30002";
	public static final String ERRMSG_JSON_RESPONSE_DESC = "Error in converting response from APIGEE";
	public static final String ERRRSN_JSON_RESPONSE_MSG = "Error in converting response from APIGEE";

	public static final String ERRCODE_DELIVERYMODENOTSUPPORT = "40011";
	public static final String ERRMSG_DELIVERYMODENOTSUPPORT = "Either delivery mode is not supported for the current cart or it doesn't exist";
	public static final String ERRRSN_DELIVERYMODENOTSUPPORT = "Either delivery mode is not supported for the current cart or it doesn't exist";

	public static final String ERRCODE_ORDER_PLACED = "ERR_60001";
	public static final String ERRMSG_ORDER_PLACED_DESC = "Error Order Placed Payload Structure";
	public static final String ERRRSN_ORDER_PLACED_MSG = "Order Place Error";

	public static final String ERRCODE_REORDER = "40020";
	public static final String ERRMSG_REORDER = "Order doesn't exist nor belong to this user.";
	public static final String ERRRSN_REORDER = "Order doesn't exist nor belong to this user.";

	public static final String ERRCODE_USER_ALREADY_AVAILABLE = "ERR_40012";
	public static final String ERRMSG_USER_ALREADY_AVAILABLE = "Duplicate User";
	public static final String ERRRSN_USER_ALREADY_AVAILABLE = "User already available";

	public static final String ERRCODE_MEMBER_TOKEN_NOT_AVAILABLE = "ERR_40013";
	public static final String ERRMSG_MEMBER_TOKEN_NOT_AVAILABLE = "Member token not available";
	public static final String ERRRSN_MEMBER_TOKEN_NOT_AVAILABLE = "Member tpken is null in Payload";

	public static final String ERRCODE_MEMBER_ACCOUNT_NOT_AVAILABLE = "ERR_40014";
	public static final String ERRMSG_MEMBER_ACCOUNT_NOT_AVAILABLE = "Member account not available";
	public static final String ERRRSN_MEMBER_ACCOUNT_NOT_AVAILABLE = "Member account is not available";

	public static final String ERRCODE_MEMBER_TOKEN_DATE_NOT_VALID = "ERR_40015";
	public static final String ERRMSG_MEMBER_TOKEN_DATE_NOT_VALID = "Member Token date is not valid";
	public static final String ERRRSN_MEMBER_TOKEN_DATE_NOT_VALID = "Member Token date is not valid";


	public static final String ERRCODE_MEMBER_TOKEN_TIME_LIMIT_ERROR = "ERR_40016";
	public static final String ERRMSG_MEMBER_TOKEN_TIME_LIMIT_ERROR = "Member Token date expired";
	public static final String ERRRSN_MEMBER_TOKEN_TIME_LIMIT_ERROR = "Member Token date expired";

	public static final String ERRCODE_MEMEBER_ACCOUNT_NOT_VALID = "ERR_40017";
	public static final String ERRMSG_MEMEBER_ACCOUNT_NOT_VALID = "User is not associated with input memberID";
	public static final String ERRRSN_MEMEBER_ACCOUNT_NOT_VALID = "User is not associated with input memberID";

	public static final String PAYMENT_OPTION_PAY_NOW = "CREDIT CARD";
	public static final String PAYMENT_OPTION_BANK_TRANSFER = "BANK TRANSFER";
	public static final String PAYMENT_OPTION_ON_ACCOUNT = "ON ACCOUNT";

	public static final String ERRCODE_PIDIMAGENOTFOUND = "40019";
	public static final String ERRMSG_PIDIMAGENOTFOUND = "Image doesn't exist";
	public static final String ERRRSN_PIDIMAGENOTFOUND = "Image doesn't exist";

	public static final String ERRCODE_CRNNOTMACHED = "50003";
	public static final String ERRMSG_NNNOTMACHED = "CRN number does not match for current user";
	public static final String ERRRSN_NNNOTMACHED = "CRN number Should match with current User";

	public static final String ERRCODE_ISNOTADMIN = "70001";
	public static final String ERRMSG_ISNOTADMIN = "Customer is not a B2B admin user";
	public static final String ERRRSN_ISNOTADMIN = "Customer is not a B2B admin user";

	public static final String ERRCODE_ISNOTATIVE = "70002";
	public static final String ERRMSG_ISNOTATIVE = "Only for Buyer users orderlimit will update";
	public static final String ERRRSN_ISNOTATIVE = "Only for Buyer users orderlimit will update";

	public static final String ERRCODE_ISNOTEXIST = "70003";
	public static final String ERRMSG_ISNOTEXIST = "Customer is not a B2B exist user";
	public static final String ERRRSN_ISNOTEXIST = "Customer is not a B2B exist user";

	public static final String ERRCODE_ORDERLIMITFORBUYER = "50002";
	public static final String ERRMSG_ORDERLIMITFORBUYER = "Current Order limit for buyer is exceeded. OrderLimit=";
	public static final String ERRRSN_ORDERLIMITFORBUYER = "Current Order limit for buyer is exceeded. OrderLimit=";

	public static final String ERRCODE_IMAGEIDNOTEXIST = "70004";
	public static final String ERRMSG_IMAGEIDNOTEXIST = "No media found";
	public static final String ERRRSN_IMAGEIDNOTEXIST = "No media exists for given status";

	public static final String ERRCODE_IMAGENOTEXIST = "70005";
	public static final String ERRMSG_IMAGENOTEXIST = "No Image exists for give ID";
	public static final String ERRRSN_IMAGENOTEXIST = "No Image exists for give ID";


	public static final String ERRCODE_CARTLIMIT = "40013";
	public static final String ERRMSG_CARTLIMIT = " cards can be added to cart";
	public static final String ERRRSN_CARTLIMIT = " cards can be added to cart";

	public static final String ERRCODE_NOSTATUS = "70006";
	public static final String ERRMSG_NOSTATUS = "Invalid image status";

	public static final String ERRCODE_CURRENCYCHECK = "70007";
	public static final String ERRMSG_CURRENCYCHECK = "currency code and value are required";
	public static final String ERRRSN_CURRENCYCHECK = "currency code and value are required";
	public static final String ERRRSN_NOSTATUS = "Only PENDING/APPROVED/APPROVED,PENDING are allowed";

	public static final String ERRCODE_ORDERLIMIT = "50004";
	public static final String ERRMSG_ORDERLIMIT = "orderLimit is mandotory for Buyer ";
	public static final String ERRRSN_ORDERLIMIT = "orderLimit is mandotory for Buyer ";

	public static final String ERRCODE_ADDADMINSANDBUYERS = "70008";
	public static final String ERRMSG_ADDADMINSANDBUYERS = "Only B2B admin only can add buyers and admin ";
	public static final String ERRRSN_ADDADMINSANDBUYERS = "Only B2B admin only can add buyers and admin";

	public static final String ERRCODE_ADMINCANREGISTER = "70009";
	public static final String ERRMSG_ADMINCANREGISTER = "Only B2B admin only can register ";
	public static final String ERRRSN_ADMINCANREGISTER = "Only B2B admin only can register";

	public static final String ERRCODE_FIELDMISSING = "80001";
	public static final String ERRMSG_FIELDMISSING = "field is missing";
	public static final String ERRRSN_FIELDMISSING = "field is missing";

	public static final String ERRCODE_ADDADDRESS = "50005";
	public static final String ERRMSG_ADDADDRESS = "Billing and Shipping flag cannot be same while adding a new Address";
	public static final String ERRRSN_ADDADDRESS = "Billing and Shipping flag cannot be same while adding a new Address";

	public static final String ERRCODE_DELETEADDRESS = "50007";
	public static final String ERRMSG_DELETEADDRESS = "Address with given addressID doesn't exist or belong to another user";
	public static final String ERRRSN_DELETEADDRESS = "Address with given addressID doesn't exist or belong to another user";

	public static final String ERRCODE_ADDRESSTYPE = "ERR_50009";
	public static final String ERRMSG_ADDRESSTYPE = "AddressType not Valid for the User: Only 'Billing' and 'Shipping' addressTypes are allowed ";
	public static final String ERRRSN_ADDRESSTYPE = "AddressType not Valid for the User";

	public static final String BILLINGTYPE = "Billing";
	public static final String SHIPPINGTYPE = "Shipping";

	public static final String ERRCODE_CREDIT_CARD_SECURITY_CHECK_MISSING = "90001";
	public static final String ERRMSG_CREDIT_CARD_SECURITY_CHECK_MISSING = "Security Check Status is Missing";
	public static final String ERRRSN_CREDIT_CARD_SECURITY_CHECK_MISSING = "Security Check Status is Missing";

	public static final String CITY = "city";
	public static final String ADDRESS1 = "address1";
	public static final String STATE = "state";
	public static final String POSTALCODE = "postalCode";
	public static final String FIRSTNAME = "firstName";
	public static final String LASTNAME = "lastName";
	public static final String PHONE = "phone";

	public static final String ERRCODE_ORDER_HISTORY = "ERR_80001";
	public static final String ERRMSG_ORDER_HISTORY_DESC = "Order History Not Found in Back End";
	public static final String ERRRSN_ORDER_HISTORY_MSG = "Order History is Null";

	public static final String PAYMENT_INSTRUMENT_ID = "paymentInstrumentId";
	public static final String STEP_UP_TOKEN = "stepUpToken";
	public static final String ACCESS_TOKEN = "accessToken";

	public static final String ERRCODE_EMAILFORMAT = "80003";
	public static final String ERRMSG_EMAILFORMAT = "please enter valid email format";
	public static final String ERRRSN_EMAILFORMAT = "please enter valid email format";


	public static final String ERR_CODE_BILLING_ADDRESS = "ERR_50003";
	public static final String ERRMSG_BILLING_ADDRESS = "Address ID is not valid";
	public static final String ERRRSN_BILLING_ADDRESS = "Address ID does not exist in System";

	public enum PAYMENT_OPTIONS
	{
		PAY_1001, PAY_1002, PAY_1003;
	}

	public static final String ERRCODE_COBRANDNOTFOUND = "40018";
	public static final String ERRMSG_COBRANDNOTFOUND = "Incorrect coBrand ID";
	public static final String ERRRSN_COBRANDNOTFOUND = "coBrand doesn't exist";


	public static final String ERRCODE_ONLYADMINCANVIEW = "70010";
	public static final String ERRMSG_ONLYADMINCANVIEW = "Only B2B admin can see the list of users ";
	public static final String ERRRSN_ONLYADMINCANVIEW = "Only B2B admin can see the list of users";

	public static final String ERRCODE_USERALREADYEXIST = "70011";
	public static final String ERRMSG_USERALREADYEXIST = "UID is already exist";
	public static final String ERRRSN_USERALREADYEXIST = "UID is already exist";

	public static final String ERRCODE_POLICYAGREEMENT = "80004";
	public static final String ERRMSG_POLICYAGREEMENT = "policy agreement should be true";
	public static final String ERRRSN_POLICYAGREEMENT = "policy agreement should be true";

	public static final String ERRCODE_UPDATECARTFORENTRY = "40022";
	public static final String ERRMSG_UPDATECARTFORENTRY = "Product code from request body object doesn't match to product code from updated entry (product code cannot be changed)";
	public static final String ERRRSN_UPDATECARTFORENTRY = "Product code from request body object doesn't match to product code from updated entry (product code cannot be changed)";

	public static final String ERRCODE_CARTLIMITFORB2B = "40023";
	public static final String ERRMSG_CARTLIMITFORB2B = " cards can be added to cart, for B2B user";
	public static final String ERRRSN_CARTLIMITFORB2B = " cards can be added to cart, for B2B user";

	public static final String ERRCODE_CUSTOMERPRICECHECK = "40021";
	public static final String ERRMSG_CUSTOMERPRICECHECK = "Customer price must be greater than 0";
	public static final String ERRRSN_CUSTOMERPRICECHECK = "Customer price must be greater than 0";


	public static final String ERRCODE_REFERENCENUMNOTFOUND = "40024";
	public static final String ERRMSG_REFERENCENUMNOTFOUND = "Incorrect Reference No of Bulk Order";
	public static final String ERRRSN_REFERENCENUMNOTFOUND = "Incorrect Reference No of Bulk Order";

	public static final String ERRCODE_ADDTOCART = "40025";
	public static final String ERRMSG_ADDTOCART = "Bulk Order Status is now for this reference Number is ";
	public static final String ERRRSN_ADDTOCART = "Bulk Order Status is now for this reference Number is";

	public static final String ERRCODE_IMAGEIDEXCEEDED = "70012";
	public static final String ERRMSG_IMAGEIDEXCEEDED = "First image ID's list should be less than page size";
	public static final String ERRRSN_IMAGEIDEXCEEDED = "First image ID's list should be less than page size";

	public static final String ERRCODE_REMOVEACCOUNT = "70012";
	public static final String ERRMSG_REMOVEACCOUNT = "Admin user can not delete him/her self";
	public static final String ERRRSN_REMOVEACCOUNT = "Admin user can not delete him/her self";

	public static final String ERRCODE_PHONENOTVALID = "80007";
	public static final String ERRMSG_PHONENOTVALID = "please enter valid phone number";
	public static final String ERRRSN_PHONENOTVALID = "please enter valid phone number";

	public static final String ERRCODE_ORDERCREDITLIMIT = "ERR_30009";
	public static final String ERRMSG_ORDERCREDITLIMIT = "On account mode is not enabled for the respective unit or order total price exceeded than credit limit";
	public static final String ERRRSN_ORDERCREDITLIMIT = "On account mode is not enabled for the respective unit or order total price exceeded than credit limit";

	public static final String ERRCODE_PAYMENT_NOT_ALLOWED = "ERR_30010";
	public static final String ERRMSG_PAYMENT_NOT_ALLOWED = "Only B2B user allowed for PAY_002 and PAY_003 Payment options";
	public static final String ERRRSN_PAYMENT_NOT_ALLOWED = "Only B2B user allowed for Bank Transfer and On Account Options";

	public static final String ERRCODE_CATEGORY_NOT_ASSOCIATED = "ERR_40026";
	public static final String ERRMSG_CATEGORY_NOT_ASSOCIATED = "Category not associated with this user";
	public static final String ERRRSN_CATEGORY_NOT_ASSOCIATED = "Category not associated with this user";

	public static final String B2B = "B2B";
	public static final String B2C = "B2C";
	public static final String MEM = "MEM";
	public static final String ANONYMOUS = "anonymous";
 
    public static final String ERR_CODE_SOCKETEXCEPTION = "ERR_30011";
	public static final String ERRMSG_SOCKETEXCEPTION = "Socket Connection Exception";
	public static final String ERRRSN_SOCKETEXCEPTION = "Timeout error from APIGEE";
	
	public static final String DEFAULTIMAGEPROPERTY = "default.image.for.eGiftCard";
	public static final String DEFAULTIMAGE = "DefaultImageForGiftCard";
	public static final String MEDIADATA = "code,url";
	

	public static final String ERRCODE_CURRENCYNOTNULL = "80008";
	public static final String ERRMSG_CURRENCYNOTNULL = "please enter currency code";
	public static final String ERRRSN_CURRENCYNOTNULL = "please enter currency code";

	public static final String ERRCODE_VOUCHER_COUPON = "800011";
	public static final String ERRCODE_VOUCHER_COUPON_MESSAGE = "Failed to remove the Coupons for this Cart";
	public static final String ERRCODE_VOUCHER_EXCEPTION = "Failed to remove the Coupons for this Cart";
	
	public static final String ERRCODE_MULTIPRICE = "50012";
	public static final String ERRMSG_MULTIPRICE = "Provided price is not available";
	public static final String ERRRSN_MULTIPRICE = "Provided price is not available";

	public static final String ERR_CODE_GUEST_CART = "ERR_80070";
	public static final String ERRMSG_GUEST_CART_PROFILE = "Cannot find cart for a given guid";
	public static final String ERRRSN_GUEST_CART_PROFILE = "Cannot find cart for a given guid";

    public static final String ERRCODE_NOTEGIFTCARD = "40030";
	public static final String ERRMSG_NOTEGIFTCARD = "No GiftCard found for given value";
	public static final String ERRRSN_NOTEGIFTCARD = "No GiftCard found for given value.";
   
    public static final String ERRCODE_EGIFTCARDEMPTYTOKEN = "40031";
	public static final String ERRMSG_EGIFTCARDEMPTYTOKEN = "No GiftCard found for given value";
	public static final String ERRRSN_EGIFTCARDEMPTYTOKEN = "No GiftCard found for given value.";

	public static final int TWO = 2;
	public static final int FIVE = 5;
	public static final int NINE = 9;

public static final String ERRCODE_INVALID_ORDER_TOKEN = "ERR_60002";
	public static final String ERRMSG_INVALID_ORDER_TOKEN = "Order token is invalid";
	public static final String ERRRSN_INVALID_ORDER_TOKEN = "Order token is invalid";
	
	public static final String CART_LIMIT_B2B = "cart.limit.for.B2B.user";
}
