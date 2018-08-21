/**
 *
 */
package de.hybris.wooliesegiftcard.facade.impl;

import de.hybris.platform.b2b.model.B2BBudgetModel;
import de.hybris.platform.b2b.model.B2BCostCenterModel;
import de.hybris.platform.commercefacades.order.impl.DefaultCheckoutFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercewebservicescommons.dto.user.AddressWsDTO;
import de.hybris.platform.core.Constants;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.enums.PaymentStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.order.payment.PaymentModeModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.order.impl.DefaultCalculationService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.wooliesegiftcard.core.enums.PaymentOptions;
import de.hybris.wooliesegiftcard.core.enums.UserDataType;
import de.hybris.wooliesegiftcard.core.impl.WooliesDefaultCustomerAccountService;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BCustomerModel;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BUnitModel;
import de.hybris.wooliesegiftcard.core.model.WWHtmlLookUpModel;
import de.hybris.wooliesegiftcard.exceptions.WooliesFacadeLayerException;
import de.hybris.wooliesegiftcard.facade.WooliesPaymentFacade;
import de.hybris.wooliesegiftcard.facades.constants.WooliesgcFacadesConstants;
import de.hybris.wooliesegiftcard.facades.dto.CreditCardsResponse;
import de.hybris.wooliesegiftcard.facades.dto.FraudPayload;
import de.hybris.wooliesegiftcard.facades.dto.FraudResponse;
import de.hybris.wooliesegiftcard.facades.dto.Payment;
import de.hybris.wooliesegiftcard.facades.dto.PaymentAuthError;
import de.hybris.wooliesegiftcard.facades.dto.PaymentAuthRequest;
import de.hybris.wooliesegiftcard.facades.dto.PaymentAuthResponse;
import de.hybris.wooliesegiftcard.facades.dto.PaymentRequestDTO;
import de.hybris.wooliesegiftcard.facades.dto.PlaceOrderRequestDTO;
import de.hybris.wooliesegiftcard.facades.dto.TransactionType;
import de.hybris.wooliesegiftcard.service.impl.WooliesPaymentServiceImpl;
import de.hybris.wooliesegiftcard.utility.WooliesCustomerUtility;
import de.hybris.wooliesegiftcard.utility.WooliesEncoderDecoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.woolies.webservices.dto.PaymentDetails;
import com.woolies.webservices.dto.PaymentInfoDetails;


/**
 * @author 669567 This class is used to maintain payment details
 */
public class WooliesPaymentFacadeImpl extends DefaultCheckoutFacade implements WooliesPaymentFacade
{
	private static final Logger LOG = Logger.getLogger(WooliesPaymentFacadeImpl.class);

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;
	@Resource
	private TypeService typeService;
	@Autowired
	private DefaultCalculationService calculationService;
	@Autowired
	private WooliesDefaultCustomerAccountService customerAccountService;
	private Converter<AddressData, AddressModel> addressReverseConverter;
	private WooliesPaymentServiceImpl wooliesPaymentServiceImpl;

	/**
	 * @return the wooliesPaymentServiceImpl
	 */
	public WooliesPaymentServiceImpl getWooliesPaymentServiceImpl()
	{
		return wooliesPaymentServiceImpl;
	}

	/**
	 * @param wooliesPaymentServiceImpl
	 *           the wooliesPaymentServiceImpl to set
	 */
	public void setWooliesPaymentServiceImpl(final WooliesPaymentServiceImpl wooliesPaymentServiceImpl)
	{
		this.wooliesPaymentServiceImpl = wooliesPaymentServiceImpl;
	}

	/**
	 * @return the addressReverseConverter
	 */
	public Converter<AddressData, AddressModel> getAddressReverseConverter()
	{
		return addressReverseConverter;
	}

	/**
	 * @param addressReverseConverter
	 *           the addressReverseConverter to set
	 */
	public void setAddressReverseConverter(final Converter<AddressData, AddressModel> addressReverseConverter)
	{
		this.addressReverseConverter = addressReverseConverter;
	}

	/**
	 * This method is sued to get the payment details
	 *
	 * @param userModel
	 * @param isAnonymousUser
	 * @param cartTotalPrice
	 * @return the paymentInfoDetails
	 * @throws WooliesFacadeLayerException
	 */
	@Override
	public PaymentInfoDetails getPaymentDetails(final UserModel userModel, final boolean isAnonymousUser,
			final BigDecimal cartTotalPrice) throws WooliesFacadeLayerException
	{
		final PaymentInfoDetails paymentInfoDetails = new PaymentInfoDetails();
		final List<PaymentDetails> paymentDetails = new ArrayList<>();
		final CustomerModel customerModel = (CustomerModel) userModel;
		final List<WWHtmlLookUpModel> paymentOptionsList = wooliesPaymentServiceImpl.getPaymentOptions();
		boolean isB2BCustomer = true;
		if (isAnonymousUser || customerModel.getCustomerType() == UserDataType.B2C
				|| customerModel.getCustomerType() == UserDataType.MEM)
		{
			isB2BCustomer = false;
		}
		if (!isB2BCustomer && (null != paymentOptionsList && !paymentOptionsList.isEmpty()))
		{
			for (final WWHtmlLookUpModel paymentOption : paymentOptionsList)
			{
				if (paymentOption.getId().getCode().equalsIgnoreCase(WooliesgcFacadesConstants.PAYMENT_OPTIONS.PAY_1001.toString()))
				{
					setPaymentDetails(paymentDetails, paymentOption, true);
				}

			}
		}
		else if (customerModel.getCustomerType() == UserDataType.B2B)
		{
			setB2BCustomerPaymentDetails(userModel, cartTotalPrice, paymentDetails, paymentOptionsList);
		}
		paymentInfoDetails.setPaymentDetails(paymentDetails);
		return paymentInfoDetails;
	}

	/**
	 * @param userModel
	 * @param cartTotalPrice
	 * @param paymentDetails
	 * @param paymentOptionsList
	 * @throws WooliesFacadeLayerException
	 */
	private void setB2BCustomerPaymentDetails(final UserModel userModel, final BigDecimal cartTotalPrice,
			final List<PaymentDetails> paymentDetails, final List<WWHtmlLookUpModel> paymentOptionsList)
			throws WooliesFacadeLayerException
	{
		final CorporateB2BCustomerModel corporateb2bcustomerModel = (CorporateB2BCustomerModel) userModel;
		final CorporateB2BUnitModel corporateb2BUnit = (CorporateB2BUnitModel) corporateb2bcustomerModel.getDefaultB2BUnit();
		BigDecimal b2bBudget = null;
		if (paymentOptionsList != null && !paymentOptionsList.isEmpty())
		{
			for (final WWHtmlLookUpModel paymentOption : paymentOptionsList)
			{
				final List<B2BCostCenterModel> b2bCostCenters = corporateb2BUnit.getCostCenters();
				b2bBudget = initialiseBudgetModel(b2bBudget, b2bCostCenters);
				if (CollectionUtils.isNotEmpty(b2bCostCenters) && b2bCostCenters.get(0).getActive().booleanValue())
				{
					setPaymentDetailsOnAccountEnabled(cartTotalPrice, paymentDetails, b2bBudget, paymentOption);
				}
				else
				{
					setPaymentDetailsOnAccountDisabled(paymentDetails, paymentOption);
				}
			}
		}
	}

	/**
	 * @param paymentDetails
	 * @param paymentOption
	 */
	private void setPaymentDetailsOnAccountDisabled(final List<PaymentDetails> paymentDetails,
			final WWHtmlLookUpModel paymentOption)
	{
		if (!(paymentOption.getId().getCode().equalsIgnoreCase(WooliesgcFacadesConstants.PAYMENT_OPTIONS.PAY_1003.toString())))
		{
			setPaymentDetails(paymentDetails, paymentOption, true);
		}
	}

	/**
	 * @param cartTotalPrice
	 * @param paymentDetails
	 * @param b2bBudget
	 * @param paymentOption
	 * @throws WooliesFacadeLayerException
	 */
	private void setPaymentDetailsOnAccountEnabled(final BigDecimal cartTotalPrice, final List<PaymentDetails> paymentDetails,
			final BigDecimal b2bBudget, final WWHtmlLookUpModel paymentOption) throws WooliesFacadeLayerException
	{
		if (paymentOption.getId().getCode().equalsIgnoreCase(WooliesgcFacadesConstants.PAYMENT_OPTIONS.PAY_1003.toString()))
		{
			if (!checkCreditLimit(b2bBudget, cartTotalPrice))
			{
				setPaymentDetails(paymentDetails, paymentOption, false);
			}
			else
			{
				setPaymentDetails(paymentDetails, paymentOption, true);
			}
		}
		else
		{
			setPaymentDetails(paymentDetails, paymentOption, true);
		}
	}

	/**
	 * @param b2bBudget
	 * @param b2bCostCenters
	 * @return
	 */
	private BigDecimal initialiseBudgetModel(BigDecimal b2bBudget, final List<B2BCostCenterModel> b2bCostCenters)
	{
		if (!b2bCostCenters.isEmpty())
		{
			for (final B2BCostCenterModel b2bCostCenterModel : b2bCostCenters)
			{
				final Set<B2BBudgetModel> currentBudget = b2bCostCenterModel.getBudgets();
				for (final B2BBudgetModel b2bBudgetModel : currentBudget)
				{
					b2bBudget = b2bBudgetModel.getBudget();
				}
			}
		}
		return b2bBudget;
	}

	/**
	 * To set the payment details
	 *
	 * @param paymentDetails
	 * @param payment
	 */
	private void setPaymentDetails(final List<PaymentDetails> paymentDetails, final WWHtmlLookUpModel payment,
			final boolean isCreditLimitAvailable)
	{
		final PaymentDetails paymentDetail = new PaymentDetails();
		paymentDetail.setPaymentCode(payment.getId().name());
		paymentDetail.setPaymentDescription(payment.getDescription());
		paymentDetail.setPaymentDetails(payment.getDetails());
		paymentDetail.setIsActive(true);
		if (!isCreditLimitAvailable)
		{
			paymentDetail.setIsActive(false);
			paymentDetail.setErrorCode(WooliesgcFacadesConstants.PAYMENT_OPTION_PAY_NOW_ERROR_CODE);
		}
		paymentDetails.add(paymentDetail);
	}

	/**
	 * This method is used to check the credit limit
	 *
	 * @param availableCreditLimit
	 * @param totalPrice
	 * @return true or false
	 * @throws WooliesFacadeLayerException
	 */
	private boolean checkCreditLimit(final BigDecimal b2bBudget, final BigDecimal totalPrice) throws WooliesFacadeLayerException
	{
		if (null != b2bBudget)
		{
			if (totalPrice.compareTo(b2bBudget) > 0)
			{
				return false;
			}
		}
		else
		{
			throw new WooliesFacadeLayerException("No Credit Limit Set in BackOffice");
		}
		return true;
	}

	/**
	 * This method is used to save payment information
	 *
	 * @param placeOrderRequestDTO
	 * @param addressData
	 * @param userModel
	 * @return payment details saved or not
	 * @throws CalculationException
	 * @throws WooliesFacadeLayerException
	 */
	@Override
	public boolean savePaymentInfo(final PlaceOrderRequestDTO placeOrderRequestDTO, final AddressData addressData,
			final CartModel cartModel) throws CalculationException, WooliesFacadeLayerException
	{
		final UserModel userModel = cartModel.getUser();
		final CustomerModel customerModel = (CustomerModel) userModel;
		PaymentInfoModel paymentInfoModel = null;
		paymentInfoModel = cartModel.getPaymentInfo();
		final String code = customerModel.getUid() + "_" + UUID.randomUUID();
		if (null != paymentInfoModel)
		{
			getModelService().remove(paymentInfoModel);
		}
		paymentInfoModel = getModelService().create(PaymentInfoModel.class);
		paymentInfoModel.setUser(customerModel);
		paymentInfoModel.setPaymentOption(PaymentOptions.valueOf(placeOrderRequestDTO.getPayment().getPaymentOption()));
		paymentInfoModel.setCode(code);
		if (addressData.getAddressID() != null)
		{
			final AddressModel billingAddressModel = customerAccountService.getaddressforAddressId(addressData.getAddressID());
			if (billingAddressModel != null)
			{
				paymentInfoModel.setBillingAddress(getModelService().clone(billingAddressModel));
				getModelService().save(paymentInfoModel);
				getModelService().refresh(paymentInfoModel);
			}
			else
			{
				throw new WooliesFacadeLayerException(WooliesgcFacadesConstants.ERR_CODE_BILLING_ADDRESS,
						WooliesgcFacadesConstants.ERRMSG_BILLING_ADDRESS, WooliesgcFacadesConstants.ERRRSN_BILLING_ADDRESS);
			}
		}
		else
		{
			setBillingAddress(addressData, customerModel, paymentInfoModel);
		}
		if (placeOrderRequestDTO.getPayment().getPaymentOption()
				.equalsIgnoreCase(WooliesgcFacadesConstants.PAYMENT_OPTIONS.PAY_1002.name()))
		{
			setBankTransferPaymentMode(cartModel, code);
		}
		else if (placeOrderRequestDTO.getPayment().getPaymentOption()
				.equalsIgnoreCase(WooliesgcFacadesConstants.PAYMENT_OPTIONS.PAY_1003.name()))
		{
			final UserModel user = cartModel.getUser();
			final BigDecimal b2bBudget = null;

			if (user instanceof CorporateB2BCustomerModel)
			{
				setB2BcostCentreUpdate(cartModel, code, user, b2bBudget);
			}
		}
		if (null != placeOrderRequestDTO.getPoReference() && customerModel.getCustomerType() == UserDataType.B2B)
		{
			cartModel.setPurchaseOrderNumber(placeOrderRequestDTO.getPoReference());
		}
		cartModel.setPaymentInfo(paymentInfoModel);
		getModelService().save(cartModel);
		calculationService.calculateTotals(cartModel, true);
		return true;
	}

	/**
	 * @param cartModel
	 * @param code
	 * @param user
	 * @param b2bBudget
	 * @throws WooliesFacadeLayerException
	 */
	private void setB2BcostCentreUpdate(final CartModel cartModel, final String code, final UserModel user, BigDecimal b2bBudget)
			throws WooliesFacadeLayerException
	{
		final CorporateB2BCustomerModel b2buser = (CorporateB2BCustomerModel) user;
		final CorporateB2BUnitModel b2bUnit = (CorporateB2BUnitModel) b2buser.getDefaultB2BUnit();
		final List<B2BCostCenterModel> b2bCostCenters = b2bUnit.getCostCenters();
		if (CollectionUtils.isNotEmpty(b2bCostCenters) && b2bCostCenters.get(0).getActive().booleanValue())
		{
			for (final B2BCostCenterModel b2bCostCenterModel : b2bCostCenters)
			{
				final Set<B2BBudgetModel> currentBudget = b2bCostCenterModel.getBudgets();
				for (final B2BBudgetModel b2bBudgetModel : currentBudget)
				{
					b2bBudget = b2bBudgetModel.getBudget();
				}
				if (b2bBudget != null && (b2bBudget.doubleValue() >= cartModel.getTotalPrice().doubleValue()))
				{
					setPaymentMode(cartModel, code);
				}
				else
				{
					throw new WooliesFacadeLayerException(WooliesgcFacadesConstants.ERRCODE_ORDERCREDITLIMIT,
							WooliesgcFacadesConstants.ERRMSG_ORDERCREDITLIMIT, WooliesgcFacadesConstants.ERRRSN_ORDERCREDITLIMIT);
				}
			}
		}
		else
		{
			throw new WooliesFacadeLayerException(WooliesgcFacadesConstants.ERRCODE_ORDERCREDITLIMIT,
					WooliesgcFacadesConstants.ERRMSG_ORDERCREDITLIMIT, WooliesgcFacadesConstants.ERRRSN_ORDERCREDITLIMIT);
		}
	}

	/**
	 * @param cartModel
	 * @param code
	 */
	private void setBankTransferPaymentMode(final CartModel cartModel, final String code)
	{
		PaymentModeModel paymentModeCredit;
		paymentModeCredit = getModelService().create(PaymentModeModel.class);
		paymentModeCredit.setActive(Boolean.TRUE);
		paymentModeCredit.setCode(code);
		paymentModeCredit.setName(WooliesgcFacadesConstants.BANK_TRANSFER);
		paymentModeCredit.setPaymentInfoType(typeService.getComposedTypeForCode(Constants.TYPES.InvoicePaymentInfo));
		cartModel.setPaymentMode(paymentModeCredit);
		cartModel.setPaymentStatus(PaymentStatus.NOTPAID);
		cartModel.setStatus(OrderStatus.APPROVED);
	}

	/**
	 * @param cartModel
	 * @param code
	 */
	private void setPaymentMode(final CartModel cartModel, final String code)
	{
		PaymentModeModel paymentModeCredit;
		paymentModeCredit = getModelService().create(PaymentModeModel.class);
		paymentModeCredit.setActive(Boolean.TRUE);
		paymentModeCredit.setCode(code);
		paymentModeCredit.setName(WooliesgcFacadesConstants.ON_ACCOUNT);
		paymentModeCredit.setPaymentInfoType(typeService.getComposedTypeForCode(Constants.TYPES.InvoicePaymentInfo));
		cartModel.setPaymentMode(paymentModeCredit);
		cartModel.setPaymentStatus(PaymentStatus.PAID);
		cartModel.setStatus(OrderStatus.APPROVED);
	}

	/**
	 * @param addressData
	 * @param customerModel
	 * @param paymentInfoModel
	 */
	private void setBillingAddress(final AddressData addressData, final CustomerModel customerModel,
			final PaymentInfoModel paymentInfoModel)
	{
		final AddressModel billingAddressModel = addressReverseConverter.convert(addressData);
		billingAddressModel.setBillingAddress(Boolean.valueOf(true));
		if (addressData.getSaveToProfile() != null && addressData.getSaveToProfile().booleanValue())
		{
			final List<AddressModel> customerAddresses = new ArrayList(customerModel.getAddresses());
			billingAddressModel.setOwner(customerModel);
			customerAddresses.add(billingAddressModel);
			customerModel.setAddresses(customerAddresses);
			if (null == customerModel.getDefaultPaymentAddress())
			{
				customerModel.setDefaultPaymentAddress(billingAddressModel);
			}
			getModelService().save(customerModel);
		}
		else
		{
			billingAddressModel.setOwner(paymentInfoModel);
		}
		paymentInfoModel.setBillingAddress(getModelService().clone(billingAddressModel));
		getModelService().save(paymentInfoModel);
		getModelService().refresh(paymentInfoModel);
	}

	/**
	 * This method is used to deAuthorize
	 *
	 * @param placeOrderRequestDTO
	 * @param cartModel
	 * @param authErrors
	 * @return doAuthorized or not
	 * @throws WooliesFacadeLayerException
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 * @throws CalculationException
	 */
	@Override
	public boolean doAuthorize(final PlaceOrderRequestDTO placeOrderRequestDTO, final CartModel cartModel,
			final List<PaymentAuthError> authErrors, final Date orderDate) throws WooliesFacadeLayerException,
			KeyManagementException, NoSuchAlgorithmException, CalculationException, ParseException
	{
		boolean authResponse = false;
		final PaymentRequestDTO paymentRequestDTO = placeOrderRequestDTO.getPayment();
		if (null != paymentRequestDTO.getIsTest() && paymentRequestDTO.getIsTest().booleanValue())
		{
			final PaymentAuthResponse paymentResponse = new PaymentAuthResponse();
			final FraudResponse fraudResponse = new FraudResponse();
			paymentResponse.setFraudResponse(fraudResponse);
			saveCartModel(cartModel, paymentResponse);
			authResponse = true;
		}
		else
		{
			final String xml = generateXML(placeOrderRequestDTO, cartModel, orderDate);
			LOG.debug("XML generated" + xml);
			authResponse = sendAuthRequest(xml, paymentRequestDTO, cartModel, authErrors);
		}
		return authResponse;
	}

	/**
	 *
	 * @param placeOrderRequestDTO
	 * @param cartModel
	 * @return
	 */

	private String generateXML(final PlaceOrderRequestDTO placeOrderRequestDTO, final CartModel cartModel, final Date orderDate)
			throws ParseException, WooliesFacadeLayerException
	{
		final StringWriter writer = new StringWriter();
		final UserModel userModel = cartModel.getUser();
		final CustomerModel customerModel = (CustomerModel) userModel;
		try
		{
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final Document doc = builder.newDocument();
			final Element rootElement = doc.createElement(WooliesgcFacadesConstants.ELEMENT_REQUEST);
			rootElement.setAttribute(WooliesgcFacadesConstants.ELEMENT_REQUEST_ATTIBUTE,
					WooliesgcFacadesConstants.ELEMENT_REQUEST_ATTIBUTE_VALUE);
			final Element merchantID = doc.createElement(WooliesgcFacadesConstants.ELEMENT_MERCHANTID);
			merchantID.setTextContent(configurationService.getConfiguration().getString(WooliesgcFacadesConstants.MERCHANTID_DIGIPAY,
					WooliesgcFacadesConstants.ELEMENT_MERCHANTID_VALUE));
			final Element merchantReferenceCode = doc.createElement(WooliesgcFacadesConstants.ELEMENT_MERCHANT_REF_CODE);
			merchantReferenceCode.setTextContent(cartModel.getOrderId());
			//set billTo details
			boolean hasPlasticCard = false;
			for (final AbstractOrderEntryModel abstractOrderEntryModel : cartModel.getEntries())
			{
				if (!abstractOrderEntryModel.getProduct().getIsEGiftCard().booleanValue())
				{
					hasPlasticCard = true;
				}
			}
			final Element billTo = setBillingInfo(placeOrderRequestDTO, doc, userModel);
			//set shipTo details
			if (hasPlasticCard)
			{
				final Element shipTo = setShippingInfo(placeOrderRequestDTO, doc, userModel);
				rootElement.appendChild(shipTo);
			}
			// set item info
			final List<Element> itemInfos = createItems(cartModel.getEntries(), doc);
			//set purchaseTotals
			final Element purchaseTotals = setPurchaseTotals(cartModel, doc);
			//set merchantDefined Data
			final Element merchantDefinedData = setMerchantDefinedData(doc, placeOrderRequestDTO, hasPlasticCard, customerModel,
					cartModel, orderDate);
			final Element afsService = doc.createElement(WooliesgcFacadesConstants.ELEMENT_AFS_SERVICE);
			afsService.setAttribute(WooliesgcFacadesConstants.ATTRIBUTE_AFS_SERVICE,
					WooliesgcFacadesConstants.ATTRIBUTE_AFS_SERVICE_VALUE);
			final Element deviceFingerprintID = doc.createElement(WooliesgcFacadesConstants.ELEMENT_FINGERPRINT);
			deviceFingerprintID.setTextContent(cartModel.getOrderId());
			rootElement.appendChild(merchantID);
			rootElement.appendChild(merchantReferenceCode);
			rootElement.appendChild(billTo);

			for (final Element itemInfo : itemInfos)
			{
				rootElement.appendChild(itemInfo);
			}
			rootElement.appendChild(purchaseTotals);
			rootElement.appendChild(merchantDefinedData);
			rootElement.appendChild(afsService);
			rootElement.appendChild(deviceFingerprintID);
			doc.appendChild(rootElement);
			final TransformerFactory tFactory = TransformerFactory.newInstance();
			final Transformer transformer = tFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(WooliesgcFacadesConstants.XML_OUTPUT_RENDER, "4");
			final Source s = new DOMSource(doc);
			final Result res = new StreamResult(writer);
			transformer.transform(s, res);
			LOG.info("XML File Created Succesfully");
			LOG.debug("XML IN String format is: \n" + writer.toString());
			return writer.toString();
		}
		catch (final ParserConfigurationException e)
		{
			LOG.info(e);
			throw new WooliesFacadeLayerException(WooliesgcFacadesConstants.ERR_CODE_PARSE_FRAUD_XML,
					WooliesgcFacadesConstants.ERRMSG_PARSE_FRAUD_XML, WooliesgcFacadesConstants.ERRRSN_PARSE_FRAUD_XML);
		}
		catch (final TransformerException e)
		{
			LOG.info(e);
			throw new WooliesFacadeLayerException(WooliesgcFacadesConstants.ERR_CODE_TRANSFORM_XML,
					WooliesgcFacadesConstants.ERRMSG_TRANSFORM_XML, WooliesgcFacadesConstants.ERRRSN_TRANSFORM_XML);
		}
	}

	/**
	 * This method is used to set the billing information
	 *
	 * @param placeOrderRequestDTO
	 * @param doc
	 * @return biilTo the billing element
	 */
	private Element setBillingInfo(final PlaceOrderRequestDTO placeOrderRequestDTO, final Document doc, final UserModel user)
	{
		final CustomerModel customer = (CustomerModel) user;
		final AddressWsDTO addressData = placeOrderRequestDTO.getBillingAddress();
		final Element biilTo = doc.createElement(WooliesgcFacadesConstants.ELEMENT_BILL_TO);
		if (addressData.getAddressID() != null)
		{
			final AddressModel billingAddressModel = customerAccountService.getaddressforAddressId(addressData.getAddressID());
			final Element firstName = doc.createElement(WooliesgcFacadesConstants.ELEMENT_FIRST_NAME);
			firstName.setTextContent(billingAddressModel.getFirstname());
			final Element lastName = doc.createElement(WooliesgcFacadesConstants.ELEMENT_LAST_NAME);
			lastName.setTextContent(billingAddressModel.getLastname());
			final Element street1 = doc.createElement(WooliesgcFacadesConstants.ELEMENT_STREET);
			street1.setTextContent(billingAddressModel.getStreetname() != null ? billingAddressModel.getStreetname() : "");
			final Element city = doc.createElement(WooliesgcFacadesConstants.ELEMENT_CITY);
			city.setTextContent(billingAddressModel.getTown() != null ? billingAddressModel.getTown() : "");
			final Element state = doc.createElement(WooliesgcFacadesConstants.ELEMENT_STATE);
			state.setTextContent(billingAddressModel.getDistrict() != null ? billingAddressModel.getDistrict() : "");
			final Element postalCode = doc.createElement(WooliesgcFacadesConstants.ELEMENT_POSTALCODE);
			postalCode.setTextContent(billingAddressModel.getPostalcode() != null ? billingAddressModel.getPostalcode() : "");
			final Element country = doc.createElement(WooliesgcFacadesConstants.ELEMENT_COUNTRY);
			country.setTextContent(
					billingAddressModel.getCountry().getIsocode() != null ? billingAddressModel.getCountry().getIsocode() : "");
			final Element phoneNumber = doc.createElement(WooliesgcFacadesConstants.ELEMENT_PHONE);
			phoneNumber.setTextContent(placeOrderRequestDTO.getPhoneNumber());
			final Element email = doc.createElement(WooliesgcFacadesConstants.ELEMENT_EMAIL);
			email.setTextContent(customer.getUserEmail());
			final Element ipAddress = doc.createElement(WooliesgcFacadesConstants.ELEMENT_IP_ADDRESS);
			ipAddress.setTextContent(placeOrderRequestDTO.getClientIP());
			final Element customerID = doc.createElement(WooliesgcFacadesConstants.ELEMENT_CUSTOMER_ID);
			customerID.setTextContent(customer.getCustomerID());
			biilTo.appendChild(firstName);
			biilTo.appendChild(lastName);
			biilTo.appendChild(street1);
			biilTo.appendChild(city);
			biilTo.appendChild(state);
			biilTo.appendChild(postalCode);
			biilTo.appendChild(country);
			biilTo.appendChild(phoneNumber);
			biilTo.appendChild(email);
			biilTo.appendChild(ipAddress);
			biilTo.appendChild(customerID);
		}
		else
		{
			final Element firstName = doc.createElement(WooliesgcFacadesConstants.ELEMENT_FIRST_NAME);
			firstName.setTextContent(addressData.getFirstName());
			final Element lastName = doc.createElement(WooliesgcFacadesConstants.ELEMENT_LAST_NAME);
			lastName.setTextContent(addressData.getLastName());
			final Element street1 = doc.createElement(WooliesgcFacadesConstants.ELEMENT_STREET);
			street1.setTextContent(addressData.getAddress1());
			final Element city = doc.createElement(WooliesgcFacadesConstants.ELEMENT_CITY);
			city.setTextContent(addressData.getCity());
			final Element state = doc.createElement(WooliesgcFacadesConstants.ELEMENT_STATE);
			state.setTextContent(addressData.getState());
			final Element postalCode = doc.createElement(WooliesgcFacadesConstants.ELEMENT_POSTALCODE);
			postalCode.setTextContent(addressData.getPostalCode());
			final Element country = doc.createElement(WooliesgcFacadesConstants.ELEMENT_COUNTRY);
			country.setTextContent(addressData.getCountry().getIsocode());
			final Element phoneNumber = doc.createElement(WooliesgcFacadesConstants.ELEMENT_PHONE);
			phoneNumber.setTextContent(placeOrderRequestDTO.getPhoneNumber());
			final Element email = doc.createElement(WooliesgcFacadesConstants.ELEMENT_EMAIL);
			email.setTextContent(customer.getUserEmail());
			final Element ipAddress = doc.createElement(WooliesgcFacadesConstants.ELEMENT_IP_ADDRESS);
			ipAddress.setTextContent(placeOrderRequestDTO.getClientIP());
			final Element customerID = doc.createElement(WooliesgcFacadesConstants.ELEMENT_CUSTOMER_ID);
			customerID.setTextContent(customer.getCustomerID());
			biilTo.appendChild(firstName);
			biilTo.appendChild(lastName);
			biilTo.appendChild(street1);
			biilTo.appendChild(city);
			biilTo.appendChild(state);
			biilTo.appendChild(postalCode);
			biilTo.appendChild(country);
			biilTo.appendChild(phoneNumber);
			biilTo.appendChild(email);
			biilTo.appendChild(ipAddress);
			biilTo.appendChild(customerID);
		}
		return biilTo;
	}

	/**
	 * This method is used to set shipping information
	 *
	 * @param placeOrderRequestDTO
	 * @param doc
	 * @return shipTo
	 */
	private Element setShippingInfo(final PlaceOrderRequestDTO placeOrderRequestDTO, final Document doc, final UserModel user)
	{
		final CustomerModel customer = (CustomerModel) user;
		final AddressWsDTO addressData = placeOrderRequestDTO.getShippingAddress();
		final Element shipTo = doc.createElement(WooliesgcFacadesConstants.ELEMENT_SHIP_TO);
		if (addressData.getAddressID() != null)
		{
			final AddressModel shippingAddressModel = customerAccountService.getaddressforAddressId(addressData.getAddressID());
			final Element firstName = doc.createElement(WooliesgcFacadesConstants.ELEMENT_FIRST_NAME);
			firstName.setTextContent(shippingAddressModel.getFirstname());
			final Element lastName = doc.createElement(WooliesgcFacadesConstants.ELEMENT_LAST_NAME);
			lastName.setTextContent(shippingAddressModel.getLastname());
			final Element address1 = doc.createElement(WooliesgcFacadesConstants.ELEMENT_STREET);
			address1.setTextContent(shippingAddressModel.getStreetname() != null ? shippingAddressModel.getStreetname() : "");
			final Element city = doc.createElement(WooliesgcFacadesConstants.ELEMENT_CITY);
			city.setTextContent(shippingAddressModel.getTown() != null ? shippingAddressModel.getTown() : "");
			final Element state = doc.createElement(WooliesgcFacadesConstants.ELEMENT_STATE);
			state.setTextContent(shippingAddressModel.getDistrict() != null ? shippingAddressModel.getDistrict() : "");
			final Element postalCode = doc.createElement(WooliesgcFacadesConstants.ELEMENT_POSTALCODE);
			postalCode.setTextContent(shippingAddressModel.getPostalcode() != null ? shippingAddressModel.getPostalcode() : "");
			final Element country = doc.createElement(WooliesgcFacadesConstants.ELEMENT_COUNTRY);
			country.setTextContent(
					shippingAddressModel.getCountry().getIsocode() != null ? shippingAddressModel.getCountry().getIsocode() : "");
			final Element phoneNumber = doc.createElement(WooliesgcFacadesConstants.ELEMENT_PHONE);
			phoneNumber.setTextContent(placeOrderRequestDTO.getPhoneNumber());
			final Element email = doc.createElement(WooliesgcFacadesConstants.ELEMENT_EMAIL);
			email.setTextContent(customer.getUserEmail());
			shipTo.appendChild(firstName);
			shipTo.appendChild(lastName);
			shipTo.appendChild(address1);
			shipTo.appendChild(city);
			shipTo.appendChild(state);
			shipTo.appendChild(postalCode);
			shipTo.appendChild(country);
			shipTo.appendChild(phoneNumber);
			shipTo.appendChild(email);
		}
		else
		{
			final Element firstName = doc.createElement(WooliesgcFacadesConstants.ELEMENT_FIRST_NAME);
			firstName.setTextContent(addressData.getFirstName());
			final Element lastName = doc.createElement(WooliesgcFacadesConstants.ELEMENT_LAST_NAME);
			lastName.setTextContent(addressData.getLastName());
			final Element address1 = doc.createElement(WooliesgcFacadesConstants.ELEMENT_STREET);
			address1.setTextContent(addressData.getAddress1());
			final Element city = doc.createElement(WooliesgcFacadesConstants.ELEMENT_CITY);
			city.setTextContent(addressData.getCity());
			final Element state = doc.createElement(WooliesgcFacadesConstants.ELEMENT_STATE);
			state.setTextContent(addressData.getState());
			final Element postalCode = doc.createElement(WooliesgcFacadesConstants.ELEMENT_POSTALCODE);
			postalCode.setTextContent(addressData.getPostalCode());
			final Element country = doc.createElement(WooliesgcFacadesConstants.ELEMENT_COUNTRY);
			country.setTextContent(addressData.getCountry().getIsocode());
			final Element phoneNumber = doc.createElement(WooliesgcFacadesConstants.ELEMENT_PHONE);
			phoneNumber.setTextContent(placeOrderRequestDTO.getPhoneNumber());
			final Element email = doc.createElement(WooliesgcFacadesConstants.ELEMENT_EMAIL);
			email.setTextContent(customer.getUserEmail());
			shipTo.appendChild(firstName);
			shipTo.appendChild(lastName);
			shipTo.appendChild(address1);
			shipTo.appendChild(city);
			shipTo.appendChild(state);
			shipTo.appendChild(postalCode);
			shipTo.appendChild(country);
			shipTo.appendChild(phoneNumber);
			shipTo.appendChild(email);
		}
		return shipTo;
	}

	/**
	 * This method is used to set the purchase totals
	 *
	 * @param cartModel
	 * @param doc
	 * @return purchaseTotals
	 */
	private Element setPurchaseTotals(final CartModel cartModel, final Document doc)
	{
		final Element purchaseTotals = doc.createElement(WooliesgcFacadesConstants.ELEMENT_PURCHASE_TOTALS);
		final Element currency = doc.createElement(WooliesgcFacadesConstants.ELEMENT_CURRENCY);
		currency.setTextContent(cartModel.getCurrency().getIsocode());
		final Element grandTotalAmount = doc.createElement(WooliesgcFacadesConstants.ELEMENT_GRAND_TOTAL_AMOUNT);
		grandTotalAmount.setTextContent(cartModel.getTotalPrice().toString());
		purchaseTotals.appendChild(currency);
		purchaseTotals.appendChild(grandTotalAmount);
		return purchaseTotals;
	}

	/**
	 * This method is used to create items give model data and document
	 *
	 * @param abstractModel
	 * @param doc
	 * @return itemInfos
	 */
	private List<Element> createItems(final List<AbstractOrderEntryModel> abstractModel, final Document doc)
	{
		final List<Element> itemInfos = new ArrayList();
		for (final AbstractOrderEntryModel abstractEntryModel : abstractModel)
		{
			final Element item = doc.createElement(WooliesgcFacadesConstants.ELEMENT_ITEM);
			item.setAttribute(WooliesgcFacadesConstants.ATTRIBUTE_ID, abstractEntryModel.getEntryNumber().toString());
			final Element unitPrice = doc.createElement(WooliesgcFacadesConstants.ELEMENT_UNIT_PRICE);
			unitPrice.setTextContent(abstractEntryModel.getCustomPrice().toString());
			final Element quantity = doc.createElement(WooliesgcFacadesConstants.ELEMENT_QUANTITY);
			quantity.setTextContent(abstractEntryModel.getQuantity().toString());
			final Element productName = doc.createElement(WooliesgcFacadesConstants.ELEMENT_PRODUCT_NAME);
			productName.setTextContent(abstractEntryModel.getProduct().getName());
			final Element productSKU = doc.createElement(WooliesgcFacadesConstants.ELEMENT_PRODUCT_SKU);
			productSKU.setTextContent(abstractEntryModel.getProduct().getCode());
			item.appendChild(unitPrice);
			item.appendChild(quantity);
			item.appendChild(productName);
			item.appendChild(productSKU);
			itemInfos.add(item);
		}
		return itemInfos;
	}

	/**
	 * This method is used to set merchant defined data for the given doc
	 *
	 * @param doc
	 * @return merchantDefinedData
	 */
	private Element setMerchantDefinedData(final Document doc, final PlaceOrderRequestDTO placeOrderrequestDTO,
			final boolean hasPlasticCard, final CustomerModel customer, final CartModel cartModel, final Date orderDate)
			throws ParseException
	{
		final Element merchantDefinedData = doc.createElement(WooliesgcFacadesConstants.ELEMENT_MERCHANT_DEFINED_DATA);
		final Element mddField1 = doc.createElement(WooliesgcFacadesConstants.ELEMENT_MDD_FIELD);
		mddField1.setAttribute(WooliesgcFacadesConstants.ATTRIBUTE_ID, WooliesgcFacadesConstants.ATTRIBUTE_ID_VALUE_1);
		final String ausCurrentTime = WooliesCustomerUtility.getTimeWithZone(WooliesgcFacadesConstants.FRAUD_DATE_FORMAT,
				WooliesgcFacadesConstants.AUS_ZONE, orderDate);
		final Element mddField7 = doc.createElement(WooliesgcFacadesConstants.ELEMENT_MDD_FIELD);
		mddField7.setAttribute(WooliesgcFacadesConstants.ATTRIBUTE_ID, WooliesgcFacadesConstants.ATTRIBUTE_ID_VALUE_19);
		final int plasticCount = 0;
		final int egiftCardCount = 0;
		final Calendar cal = Calendar.getInstance();
		cal.setTime(orderDate);
		if (cartModel.getEntries().size() > 1)
		{
			setContextForCards(cartModel, mddField1, mddField7, plasticCount, egiftCardCount, cal);
		}
		else
		{
			for (final AbstractOrderEntryModel abstractOrderEntryModel : cartModel.getEntries())
			{
				if (abstractOrderEntryModel.getProduct().getIsEGiftCard().booleanValue())
				{
					mddField7.setTextContent(
							configurationService.getConfiguration().getString(WooliesgcFacadesConstants.ATTRIBUTE_ID_VALUE_DATA_DIGITAL,
									WooliesgcFacadesConstants.ATTRIBUTE_ID_VALUE_DIGITAL_DATA_DEFAULT));
					cal.add(Calendar.HOUR, 6);
					final String dateInAest = WooliesCustomerUtility.getTimeWithZone(WooliesgcFacadesConstants.FRAUD_DATE_FORMAT,
							WooliesgcFacadesConstants.AUS_ZONE, cal.getTime());
					mddField1.setTextContent(dateInAest);
				}
				else
				{
					mddField7.setTextContent(
							configurationService.getConfiguration().getString(WooliesgcFacadesConstants.ATTRIBUTE_ID_VALUE_DATA_PHYSICAL,
									WooliesgcFacadesConstants.ATTRIBUTE_ID_VALUE_PHYSICAL_DATA_DEFAULT));
					cal.add(Calendar.HOUR, 6);
					final String dateInAest = WooliesCustomerUtility.getTimeWithZone(WooliesgcFacadesConstants.FRAUD_DATE_FORMAT,
							WooliesgcFacadesConstants.AUS_ZONE, cal.getTime());
					mddField1.setTextContent(dateInAest);
				}
			}
		}
		final Element mddField2 = doc.createElement(WooliesgcFacadesConstants.ELEMENT_MDD_FIELD);
		mddField2.setAttribute(WooliesgcFacadesConstants.ATTRIBUTE_ID, WooliesgcFacadesConstants.ATTRIBUTE_ID_VALUE_2);
		if (hasPlasticCard)
		{
			mddField2.setTextContent(placeOrderrequestDTO.getShippingAddress().getState());
		}
		else
		{
			mddField2.setTextContent(null);
		}
		final Element mddField3 = doc.createElement(WooliesgcFacadesConstants.ELEMENT_MDD_FIELD);
		mddField3.setAttribute(WooliesgcFacadesConstants.ATTRIBUTE_ID, WooliesgcFacadesConstants.ATTRIBUTE_ID_VALUE_12);
		mddField3.setTextContent(WooliesgcFacadesConstants.ATTRIBUTE_ID_VALUE_12_DATA);
		final Element mddField4 = doc.createElement(WooliesgcFacadesConstants.ELEMENT_MDD_FIELD);
		mddField4.setAttribute(WooliesgcFacadesConstants.ATTRIBUTE_ID, WooliesgcFacadesConstants.ATTRIBUTE_ID_VALUE_16);
		if (null != placeOrderrequestDTO.getGuid())
		{
			mddField4.setTextContent(WooliesgcFacadesConstants.ATTRIBUTE_ID_VALUE_16_DATA);
		}
		else
		{
			setDate(customer, mddField4);
		}
		final Element mddField5 = doc.createElement(WooliesgcFacadesConstants.ELEMENT_MDD_FIELD);
		mddField5.setAttribute(WooliesgcFacadesConstants.ATTRIBUTE_ID, WooliesgcFacadesConstants.ATTRIBUTE_ID_VALUE_17);
		final Date epochDate = new Date((Long.valueOf(placeOrderrequestDTO.getLocalDate())).longValue());
		final String localTime = WooliesCustomerUtility.getTimeWithZone(WooliesgcFacadesConstants.FRAUD_DATE_FORMAT,
				WooliesgcFacadesConstants.AUS_ZONE, epochDate);
		mddField5.setTextContent(localTime);
		final Element mddField6 = doc.createElement(WooliesgcFacadesConstants.ELEMENT_MDD_FIELD);
		mddField6.setAttribute(WooliesgcFacadesConstants.ATTRIBUTE_ID, WooliesgcFacadesConstants.ATTRIBUTE_ID_VALUE_18);
		mddField6.setTextContent(ausCurrentTime);
		final Element mddField8 = doc.createElement(WooliesgcFacadesConstants.ELEMENT_MDD_FIELD);
		mddField8.setAttribute(WooliesgcFacadesConstants.ATTRIBUTE_ID, WooliesgcFacadesConstants.ATTRIBUTE_ID_VALUE_20);
		mddField8.setTextContent(WooliesgcFacadesConstants.ATTRIBUTE_ID_VALUE_20_DATA);
		final Element mddField9 = doc.createElement(WooliesgcFacadesConstants.ELEMENT_MDD_FIELD);
		mddField9.setAttribute(WooliesgcFacadesConstants.ATTRIBUTE_ID, WooliesgcFacadesConstants.ATTRIBUTE_ID_VALUE_57);
		if (customer.getCustomerType() == UserDataType.B2C)
		{
			mddField9.setTextContent(WooliesgcFacadesConstants.ATTRIBUTE_ID_VALUE_57_PUB);
		}
		else if (customer.getCustomerType() == UserDataType.B2B)
		{
			mddField9.setTextContent(WooliesgcFacadesConstants.ATTRIBUTE_ID_VALUE_57_COR);
		}
		else if (customer.getCustomerType() == UserDataType.MEM)
		{
			mddField9.setTextContent(WooliesgcFacadesConstants.ATTRIBUTE_ID_VALUE_57_MEM);
		}
		final Element mddField10 = doc.createElement(WooliesgcFacadesConstants.ELEMENT_MDD_FIELD);
		mddField10.setAttribute(WooliesgcFacadesConstants.ATTRIBUTE_ID, WooliesgcFacadesConstants.ATTRIBUTE_ID_VALUE_59);
		mddField10.setTextContent(WooliesgcFacadesConstants.ATTRIBUTE_ID_VALUE_59_DATA);
		merchantDefinedData.appendChild(mddField1);
		merchantDefinedData.appendChild(mddField2);
		merchantDefinedData.appendChild(mddField3);
		merchantDefinedData.appendChild(mddField4);
		merchantDefinedData.appendChild(mddField5);
		merchantDefinedData.appendChild(mddField6);
		merchantDefinedData.appendChild(mddField7);
		merchantDefinedData.appendChild(mddField8);
		merchantDefinedData.appendChild(mddField9);
		merchantDefinedData.appendChild(mddField10);
		return merchantDefinedData;
	}

	/**
	 * @param customer
	 * @param mddField4
	 * @throws ParseException
	 */
	private void setDate(final CustomerModel customer, final Element mddField4) throws ParseException
	{
		final Date date = customer.getCreationtime();
		final Date currentDate = new Date();
		final SimpleDateFormat dateFormat = new SimpleDateFormat(WooliesgcFacadesConstants.USER_CREATION_DATE_FORMAT);
		final String createdDate = dateFormat.format(date);
		final String currentDateString = dateFormat.format(currentDate);
		final Date oldDate = dateFormat.parse(createdDate);
		final Date newDate = dateFormat.parse(currentDateString);
		final int diffInDays = (int) ((newDate.getTime() - oldDate.getTime()) / (1000 * 60 * 60 * 24));
		mddField4.setTextContent(Integer.toString(diffInDays));
	}

	/**
	 * @param cartModel
	 * @param mddField1
	 * @param mddField7
	 * @param plasticCount
	 * @param egiftCardCount
	 * @param cal
	 */
	private void setContextForCards(final CartModel cartModel, final Element mddField1, final Element mddField7, int plasticCount,
			int egiftCardCount, final Calendar cal)
	{
		for (final AbstractOrderEntryModel abstractOrderEntryModel : cartModel.getEntries())
		{
			if (abstractOrderEntryModel.getProduct().getIsEGiftCard().booleanValue())
			{
				egiftCardCount++;
			}
			else
			{
				plasticCount++;
			}
		}

		if (egiftCardCount > 0 && plasticCount == 0)
		{
			mddField7.setTextContent(
					configurationService.getConfiguration().getString(WooliesgcFacadesConstants.ATTRIBUTE_ID_VALUE_DATA_DIGITAL,
							WooliesgcFacadesConstants.ATTRIBUTE_ID_VALUE_DIGITAL_DATA_DEFAULT));
			cal.add(Calendar.HOUR, 6);

			final String dateInAest = WooliesCustomerUtility.getTimeWithZone(WooliesgcFacadesConstants.FRAUD_DATE_FORMAT,
					WooliesgcFacadesConstants.AUS_ZONE, cal.getTime());
			mddField1.setTextContent(dateInAest);
		}
		else if (plasticCount > 0 && egiftCardCount == 0)
		{
			mddField7.setTextContent(
					configurationService.getConfiguration().getString(WooliesgcFacadesConstants.ATTRIBUTE_ID_VALUE_DATA_PHYSICAL,
							WooliesgcFacadesConstants.ATTRIBUTE_ID_VALUE_PHYSICAL_DATA_DEFAULT));
			cal.add(Calendar.HOUR, 24);
			final String dateInAest = WooliesCustomerUtility.getTimeWithZone(WooliesgcFacadesConstants.FRAUD_DATE_FORMAT,
					WooliesgcFacadesConstants.AUS_ZONE, cal.getTime());
			mddField1.setTextContent(dateInAest);
		}
		else if (plasticCount > 0 && egiftCardCount > 0)
		{
			mddField7.setTextContent(
					configurationService.getConfiguration().getString(WooliesgcFacadesConstants.ATTRIBUTE_ID_VALUE_DATA_MIXED,
							WooliesgcFacadesConstants.ATTRIBUTE_ID_VALUE_MIXED_DATA_DEFAULT));
			cal.add(Calendar.HOUR, 6);
			final String dateInAest = WooliesCustomerUtility.getTimeWithZone(WooliesgcFacadesConstants.FRAUD_DATE_FORMAT,
					WooliesgcFacadesConstants.AUS_ZONE, cal.getTime());
			mddField1.setTextContent(dateInAest);
		}
	}

	/**
	 * This method is used to set the auth request
	 *
	 * @param xml
	 * @param paymentDTO
	 * @param cartModel
	 * @param authErrors
	 * @return
	 * @throws WooliesFacadeLayerException
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 * @throws CalculationException
	 */
	private boolean sendAuthRequest(final String xml, final PaymentRequestDTO paymentDTO, final CartModel cartModel,
			final List<PaymentAuthError> authErrors)
			throws WooliesFacadeLayerException, KeyManagementException, NoSuchAlgorithmException, CalculationException
	{
		try
		{
			final String targetURL = configurationService.getConfiguration().getString(WooliesgcFacadesConstants.PAYMENT_AUTH_KEY,
					WooliesgcFacadesConstants.PAYMENT_AUTH_URL);
			final String xApiKey = configurationService.getConfiguration().getString(WooliesgcFacadesConstants.AUTH_API_KEY,
					WooliesgcFacadesConstants.AUTH_API_KEY_DUMMY);
			final int connTimeOut = configurationService.getConfiguration().getInt(WooliesgcFacadesConstants.CONN_TIMEOUT,
					WooliesgcFacadesConstants.CONN_TIMEOUT_DUMMY);
			final int readTimeOut = configurationService.getConfiguration().getInt(WooliesgcFacadesConstants.READ_TIMEOUT,
					WooliesgcFacadesConstants.READ_TIMEOUT_DUMMY);
			final String accessToken = paymentDTO.getAccessToken();
			final PaymentAuthRequest payLoad = setPayload(paymentDTO, cartModel.getOrderId(), cartModel.getTotalPrice().toString(),
					xml);
			final String env = configurationService.getConfiguration().getString(WooliesgcFacadesConstants.ENV_HYBRIS,
					WooliesgcFacadesConstants.ENV_LOCAL);
			final HttpsURLConnection httpConnection = WooliesCustomerUtility.postApigee(targetURL, xApiKey, env, connTimeOut,
					readTimeOut, payLoad, accessToken);
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

				final PaymentAuthResponse paymentResponse = (PaymentAuthResponse) createPaymentResponse(builder.toString(),
						WooliesgcFacadesConstants.SUCCESS_CODE);

				LOG.debug("Transaction Reciept of order is" + paymentResponse.getTransactionReceipt());
				LOG.debug("PaymentTransactionReferenNumber is" + paymentResponse.getTransactionReceipt());
				saveCartModel(cartModel, paymentResponse);
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
				final PaymentAuthError paymentAuthError = (PaymentAuthError) createPaymentResponse(builder.toString(),
						WooliesgcFacadesConstants.FAILURE_CODE);
				authErrors.add(paymentAuthError);
				return false;
			}
		}
		catch (final MalformedURLException e)
		{
			LOG.info(e);
			throw new WooliesFacadeLayerException(WooliesgcFacadesConstants.ERR_CODE_URL_MISMATCH,
					WooliesgcFacadesConstants.ERRMSG_URL_MISMATCH_DESC, WooliesgcFacadesConstants.ERRSN_URL_MISMATCH_MSG);
		}
		catch (final SocketException e)
		{
			LOG.info(e);
			throw new WooliesFacadeLayerException(WooliesgcFacadesConstants.ERR_CODE_SOCKETEXCEPTION,
					WooliesgcFacadesConstants.ERRMSG_SOCKETEXCEPTION, WooliesgcFacadesConstants.ERRRSN_SOCKETEXCEPTION);
		}
		catch (final IOException e)
		{
			LOG.info(e);
			throw new WooliesFacadeLayerException(WooliesgcFacadesConstants.ERR_CODE_IOEXCEPTION,
					WooliesgcFacadesConstants.ERRMSG_IOEXCEPTION_DESC, WooliesgcFacadesConstants.ERRRSN_IOEXCEPTION_MSG);
		}
	}

	/**
	 * This method is used to set the payload
	 *
	 * @param paymentDTO
	 * @param cartId
	 * @param totalPrice
	 * @return paymentAuthRequest
	 */
	private PaymentAuthRequest setPayload(final PaymentRequestDTO paymentDTO, final String orderId, final String totalPrice,
			final String xml) throws IOException
	{
		final PaymentAuthRequest paymentAuthRequest = new PaymentAuthRequest();
		final FraudPayload fraudPayload = new FraudPayload();
		final Payment paymentData = new Payment();
		final TransactionType transactionType = new TransactionType();
		paymentAuthRequest.setClientReference(orderId);
		paymentAuthRequest.setOrderNumber(orderId);
		paymentData.setAmount(new Double(totalPrice));
		paymentData.setPaymentInstrumentId(paymentDTO.getPaymentInstrumentId());
		paymentData.setStepUpToken(paymentDTO.getStepUpToken());
		final List<Payment> paymentDataList = new ArrayList<>();
		paymentDataList.add(paymentData);
		paymentAuthRequest.setPayments(paymentDataList);
		transactionType.setCreditCard(WooliesgcFacadesConstants.PRE_AUTH_DATA);
		paymentAuthRequest.setTransactionType(transactionType);

		if (configurationService.getConfiguration()
				.getString(WooliesgcFacadesConstants.CYBERSOURCE_ACTIVE, WooliesgcFacadesConstants.CYBERSOURCE_ACTIVE_DEFAULT)
				.equalsIgnoreCase(WooliesgcFacadesConstants.CYBERSOURCE_ACTIVE_DEFAULT))
		{
			fraudPayload.setProvider(configurationService.getConfiguration().getString(WooliesgcFacadesConstants.FRAUD_PROVIDER,
					WooliesgcFacadesConstants.FRAUD_PROVIDER_DEFAULT));
			fraudPayload.setVersion(configurationService.getConfiguration().getString(WooliesgcFacadesConstants.FRAUD_VERSION,
					WooliesgcFacadesConstants.FRAUD_VERSION_DEFAULT));
			fraudPayload.setFormat(configurationService.getConfiguration().getString(WooliesgcFacadesConstants.FRAUD_FORMAT,
					WooliesgcFacadesConstants.FRAUD_FORMAT_DEFAULT));
			fraudPayload.setResponseFormat(configurationService.getConfiguration()
					.getString(WooliesgcFacadesConstants.FRAUD_RESPFORMAT, WooliesgcFacadesConstants.FRAUD_RESPFORMAT_DEFAULT));
			final String encodedData = WooliesEncoderDecoder.encode(xml);
			fraudPayload.setMessage(encodedData);
			paymentAuthRequest.setFraudPayload(fraudPayload);
		}
		else if (configurationService.getConfiguration()
				.getString(WooliesgcFacadesConstants.CYBERSOURCE_ACTIVE, WooliesgcFacadesConstants.CYBERSOURCE_ACTIVE_DEFAULT_XML)
				.equalsIgnoreCase(WooliesgcFacadesConstants.CYBERSOURCE_ACTIVE_DEFAULT_XML))
		{
			fraudPayload.setProvider(configurationService.getConfiguration().getString(WooliesgcFacadesConstants.FRAUD_PROVIDER,
					WooliesgcFacadesConstants.FRAUD_PROVIDER_DEFAULT));
			fraudPayload.setVersion(configurationService.getConfiguration().getString(WooliesgcFacadesConstants.FRAUD_VERSION,
					WooliesgcFacadesConstants.FRAUD_VERSION_DEFAULT));
			fraudPayload.setFormat(configurationService.getConfiguration().getString(WooliesgcFacadesConstants.FRAUD_FORMAT_XML,
					WooliesgcFacadesConstants.FRAUD_FORMAT_DEFAULT_XML));
			fraudPayload.setResponseFormat(configurationService.getConfiguration().getString(
					WooliesgcFacadesConstants.FRAUD_RESPFORMAT_XML, WooliesgcFacadesConstants.FRAUD_RESPFORMAT_DEFAULT_XML));
			fraudPayload.setMessage(xml);
			paymentAuthRequest.setFraudPayload(fraudPayload);
		}
		return paymentAuthRequest;
	}

	/**
	 * This method is used to create payment response
	 *
	 * @param paymentResponse
	 * @param code
	 * @return
	 * @throws WooliesFacadeLayerException
	 */
	private Object createPaymentResponse(final String paymentResponse, final String code) throws WooliesFacadeLayerException
	{
		final ObjectMapper mapper = new ObjectMapper();
		PaymentAuthResponse paymentResponseObject = null;
		PaymentAuthError paymentAuthError = null;
		try
		{
			if (WooliesgcFacadesConstants.SUCCESS_CODE.equalsIgnoreCase(code))
			{
				paymentResponseObject = mapper.readValue(paymentResponse, PaymentAuthResponse.class);
				return paymentResponseObject;
			}
			else
			{
				paymentAuthError = mapper.readValue(paymentResponse, PaymentAuthError.class);
				return paymentAuthError;
			}
		}
		catch (final IOException e)
		{
			LOG.info(e);
			throw new WooliesFacadeLayerException(WooliesgcFacadesConstants.ERR_CODE_JSON_RESPONSE,
					WooliesgcFacadesConstants.ERRMSG_JSON_RESPONSE_DESC, WooliesgcFacadesConstants.ERRRSN_JSON_RESPONSE_MSG);
		}
	}


	/**
	 * This method is used to save the cart model details
	 *
	 * @param cartModel
	 * @param paymentResponse
	 * @throws CalculationException
	 */
	private void saveCartModel(final CartModel cartModel, final PaymentAuthResponse paymentResponse) throws CalculationException
	{

		final String fraudResponseDecision = paymentResponse.getFraudResponse().getDecision();

		if (null == fraudResponseDecision || WooliesgcFacadesConstants.FRAUD_ACCEPTED.equalsIgnoreCase(fraudResponseDecision))
		{
			cartModel.setStatus(OrderStatus.CHECKED_VALID);
		}
		else if (WooliesgcFacadesConstants.FRAUD_REVIEW.equalsIgnoreCase(fraudResponseDecision))
		{
			cartModel.setStatus(OrderStatus.ON_VALIDATION);
		}
		else
		{
			cartModel.setStatus(OrderStatus.CHECKED_INVALID);
		}
		final PaymentModeModel paymentModeCredit = getModelService().create(PaymentModeModel.class);
		paymentModeCredit.setActive(Boolean.TRUE);
		paymentModeCredit.setCode(UUID.randomUUID().toString());
		paymentModeCredit.setName(WooliesgcFacadesConstants.CREDIT_CARD);
		paymentModeCredit.setPaymentInfoType(typeService.getComposedTypeForCode(Constants.TYPES.CreditCardTypeType));

		cartModel.setPaymentMode(paymentModeCredit);

		final PaymentInfoModel paymentInfoModel = cartModel.getPaymentInfo();
		if (CollectionUtils.isNotEmpty(paymentResponse.getCreditCards()))
		{
			for (final CreditCardsResponse creditCardResponse : paymentResponse.getCreditCards())
			{
				paymentInfoModel.setSchema(creditCardResponse.getReceiptData().getScheme());
				paymentInfoModel.setCcNumber(creditCardResponse.getReceiptData().getCardSuffix());
				paymentInfoModel.setPaymentTransactionRef(creditCardResponse.getPaymentTransactionRef());
			}
		}
		cartModel.setPaymentInfo(paymentInfoModel);
		getModelService().save(cartModel);
		calculationService.calculateTotals(cartModel, true);

	}
}