/*
 * [y] hybris Platform
 *
 * Copyright (c) 2017 SAP SE or an SAP affiliate company.  All  rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */

package com.woolies.webservices.rest.v2.controller;

import de.hybris.platform.b2bcommercefacades.company.data.B2BUnitData;
import de.hybris.platform.cmsfacades.data.MediaData;
import de.hybris.platform.commercefacades.address.AddressVerificationFacade;
import de.hybris.platform.commercefacades.address.data.AddressVerificationResult;
import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.customergroups.CustomerGroupFacade;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoDatas;
import de.hybris.platform.commercefacades.order.data.OrderHistoriesData;
import de.hybris.platform.commercefacades.order.data.OrderHistoryData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commercefacades.user.data.RegisterData;
import de.hybris.platform.commercefacades.user.data.UserGroupDataList;
import de.hybris.platform.commercefacades.user.exceptions.PasswordMismatchException;
import de.hybris.platform.commerceservices.address.AddressVerificationDecision;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.commerceservices.order.CommerceCartRestorationException;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsListWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.product.PriceWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.user.AddressListWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.user.AddressValidationWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.user.AddressWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.user.UserGroupListWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.user.UserSignUpWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.user.UserWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.user.WooliesPaginationWsDTO;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.RequestParameterException;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.PK.PKException;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.webservicescommons.cache.CacheControl;
import de.hybris.platform.webservicescommons.cache.CacheControlDirective;
import de.hybris.platform.webservicescommons.dto.error.ErrorListWsDTO;
import de.hybris.platform.webservicescommons.dto.error.ErrorWsDTO;
import de.hybris.platform.webservicescommons.errors.exceptions.WebserviceValidationException;
import de.hybris.wooliesegiftcard.core.constants.GeneratedWooliesgcCoreConstants.Enumerations.ImageApprovalStatus;
import de.hybris.wooliesegiftcard.core.enums.UserDataType;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BUnitModel;
import de.hybris.wooliesegiftcard.core.model.MemberUnitModel;
import de.hybris.wooliesegiftcard.exception.WooliesB2BUserException;
import de.hybris.wooliesegiftcard.exception.WoolliesMediaImagesException;
import de.hybris.wooliesegiftcard.exceptions.WooliesFacadeLayerException;
import de.hybris.wooliesegiftcard.facade.CorporateDetailsFacade;
import de.hybris.wooliesegiftcard.facade.ProfileSummeryFacade;
import de.hybris.wooliesegiftcard.facade.WooliesB2BUnitFacade;
import de.hybris.wooliesegiftcard.facade.WooliesMediaFacade;
import de.hybris.wooliesegiftcard.facade.WooliesMemCustomerFacade;
import de.hybris.wooliesegiftcard.facade.WooliesUserFacade;
import de.hybris.wooliesegiftcard.facades.CorporateDetailsDTO;
import de.hybris.wooliesegiftcard.facades.CustomerDataList;
import de.hybris.wooliesegiftcard.facades.CustomerDataListDTO;
import de.hybris.wooliesegiftcard.facades.CustomerDetailDTO;
import de.hybris.wooliesegiftcard.facades.GuestCartProfileData;
import de.hybris.wooliesegiftcard.facades.GuestCartProfileWsDTO;
import de.hybris.wooliesegiftcard.facades.PersonalisationMediaDataDataListDTO;
import de.hybris.wooliesegiftcard.facades.PersonalisationMediaDataList;
import de.hybris.wooliesegiftcard.facades.RemoveCustomerDTO;
import de.hybris.wooliesegiftcard.facades.UserProfileData;
import de.hybris.wooliesegiftcard.facades.WooliesDefaultCustomerFacade;
import de.hybris.wooliesegiftcard.facades.WoolliesApprovedImagesMediaFacade;
import de.hybris.wooliesegiftcard.facades.constants.WooliesgcFacadesConstants;
import de.hybris.wooliesegiftcard.facades.dto.ResponseDTO;
import de.hybris.wooliesegiftcard.facades.order.impl.WooliesDefaultCartFacade;
import de.hybris.wooliesegiftcard.facades.user.converters.populator.WooliesB2BCustomerListPopulator;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.util.UriUtils;

import com.woolies.webservices.constants.WooliesgcWebServicesConstants;
import com.woolies.webservices.rest.constants.YcommercewebservicesConstants;
import com.woolies.webservices.rest.exceptions.WooliesWebserviceException;
import com.woolies.webservices.rest.populator.HttpRequestCustomerDataPopulator;
import com.woolies.webservices.rest.populator.options.PaymentInfoOption;
import com.woolies.webservices.rest.swagger.ApiBaseSiteIdAndUserIdParam;
import com.woolies.webservices.rest.swagger.ApiBaseSiteIdParam;
import com.woolies.webservices.rest.user.data.AddressDataList;
import com.woolies.webservices.rest.validation.data.AddressValidationData;


@Controller
@RequestMapping(value = "/{baseSiteId}/users")
@CacheControl(directive = CacheControlDirective.PRIVATE)
@Api(tags = "Users")
public class UsersController extends BaseCommerceController
{
	private static final String ADDRESS_DATA = "addressData";
	private static final Logger LOG = Logger.getLogger(UsersController.class);
	@Resource(name = "customerFacade")
	private CustomerFacade customerFacade;

	@Resource(name = "userService")
	private UserService userService;
	@Resource(name = "modelService")
	private ModelService modelService;
	@Resource(name = "customerGroupFacade")
	private CustomerGroupFacade customerGroupFacade;
	@Resource(name = "addressVerificationFacade")
	private AddressVerificationFacade addressVerificationFacade;
	@Resource(name = "httpRequestCustomerDataPopulator")
	private HttpRequestCustomerDataPopulator httpRequestCustomerDataPopulator;
	@Resource(name = "HttpRequestUserSignUpDTOPopulator")
	private Populator<HttpServletRequest, UserSignUpWsDTO> httpRequestUserSignUpDTOPopulator;
	@Resource(name = "addressDataErrorsPopulator")
	private Populator<AddressVerificationResult<AddressVerificationDecision>, Errors> addressDataErrorsPopulator;
	@Resource(name = "validationErrorConverter")
	private Converter<Object, List<ErrorWsDTO>> validationErrorConverter;
	@Resource(name = "putUserDTOValidator")
	private Validator putUserDTOValidator;
	@Resource(name = "userSignUpDTOValidator")
	private Validator userSignUpDTOValidator;
	@Resource(name = "guestConvertingDTOValidator")
	private Validator guestConvertingDTOValidator;
	@Resource(name = "passwordStrengthValidator")
	private Validator passwordStrengthValidator;

	@Resource(name = "minimalAddressValidator")
	private Validator minimalAddressValidator;
	@Resource(name = "corporateNameValidator")
	private Validator corporateNameValidator;
	@Resource(name = "userManagementValidator")
	private Validator userManagementValidator;

	@Resource(name = "wooliesB2BUnitFacade")
	private WooliesB2BUnitFacade wooliesB2BUnitFacade;

	@Resource(name = "woolliesApprovedImagesMediaFacade")
	WoolliesApprovedImagesMediaFacade woolliesApprovedImagesMediaFacade;
	@Resource(name = "wooliesB2BCustomerListPopulator")
	WooliesB2BCustomerListPopulator wooliesB2BCustomerListPopulator;

	@Resource(name = "profileSummeryFacade")
	ProfileSummeryFacade profileSummeryFacade;
	@Resource(name = "corporateDetailsFacade")
	CorporateDetailsFacade corporateDetailsFacade;
	@Resource(name = "wooliesUserFacade")
	private WooliesUserFacade wooliesUserFacade;
	@Resource(name = "wooliesDefaultCustomerFacade")
	private WooliesDefaultCustomerFacade wooliesDefaultCustomerFacade;
	@Resource(name = "wooliesDefaultCustomerFacades")
	private WooliesDefaultCustomerFacade wooliesDefaultCustomerFacades;

	@Resource(name = "httpRequestAddressDataPopulator")
	private Populator<HttpServletRequest, AddressData> httpRequestAddressDataPopulator;
	@Resource(name = "wooliesMediaFacade")
	private WooliesMediaFacade wooliesMediaFacade;
	@Resource(name = "wooliesDefaultCartFacade")
	private WooliesDefaultCartFacade wooliesDefaultCartFacade;
	@Resource(name = "wooliesMemCustomerFacade")
	private WooliesMemCustomerFacade wooliesMemCustomerFacade;
	@Resource(name = "profileupdateValidator")
	private Validator profileupdateValidator;
	@Resource(name = "memberupdateValidator")
	private Validator memberupdateValidator;
	@Resource(name = "b2cprofileupdateValidator")
	private Validator b2cprofileupdateValidator;
	@Autowired
	private CartFacade cartFacade;

	@Secured(
	{ "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@SuppressWarnings("squid:S1160")
	@ApiOperation(hidden = true, value = " Registers a customer", notes = "Registers a customer. The following two sets of parameters are available: First set is used to register a customer. In this case the required parameters are: login, password, firstName, lastName, titleCode. Second set is used to convert a guest to a customer. In this case the required parameters are: guid, password.")
	@ApiBaseSiteIdParam
	public UserWsDTO registerUser(
			@ApiParam(value = "Customer's login. Customer login is case insensitive.") @RequestParam(required = false) final String login,
			@ApiParam(value = "Customer's password.") @RequestParam final String password,
			@ApiParam(value = "Customer's title code. For a list of codes, see /{baseSiteId}/titles resource") @RequestParam(required = false) final String titleCode,
			@ApiParam(value = "Customer's first name.") @RequestParam(required = false) final String firstName,
			@ApiParam(value = "Customer's last name.") @RequestParam(required = false) final String lastName,
			@ApiParam(value = "Guest order's guid.") @RequestParam(required = false) final String guid,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
			final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) throws DuplicateUidException,
			RequestParameterException, WebserviceValidationException, UnsupportedEncodingException //NOSONAR
	{
		final UserSignUpWsDTO user = new UserSignUpWsDTO();
		httpRequestUserSignUpDTOPopulator.populate(httpRequest, user);
		CustomerData customer = null;
		String userId = login;
		if (guid != null)
		{
			validate(user, "user", guestConvertingDTOValidator);
			convertToCustomer(password, guid);
			customer = customerFacade.getCurrentCustomer();
			userId = customer.getUid();
		}
		else
		{
			validate(user, "user", userSignUpDTOValidator);
			registerNewUser(login, password, titleCode, firstName, lastName);
			customer = customerFacade.getUserForUID(userId);
		}
		httpResponse.setHeader(YcommercewebservicesConstants.LOCATION, getAbsoluteLocationURL(httpRequest, userId)); //NOSONAR
		return getDataMapper().map(customer, UserWsDTO.class, fields);
	}


	/* B2C and B2B registration */

	@Secured(
	{ "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(method = RequestMethod.POST, consumes =
	{ MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@SuppressWarnings("squid:S1160")
	@ApiOperation(value = " Registers a customer", notes = "Registers a customer. The following two sets of parameters are available: First set is used to register a customer. In this case the required parameters are: login, password, firstName, lastName, titleCode. Second set is used to convert a guest to a customer. In this case the required parameters are: guid, password.")
	public ResponseEntity<Object> registerUser(
			@ApiParam(value = "User's object.", required = true) @RequestBody final UserSignUpWsDTO user,
			final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) throws DuplicateUidException,
			UnknownIdentifierException, //NOSONAR
			IllegalArgumentException, WebserviceValidationException, UnsupportedEncodingException //NOSONAR
			, WooliesB2BUserException
	{

		if (null != user.getMemberId())
		{
			if (null != user.getMemberToken())
			{

							//check validatity, if true then check member user exist in member account
				final List<MemberUnitModel> memberUnit = wooliesMemCustomerFacade.getMemberUnit(user.getMemberId());
				if (null != memberUnit && !memberUnit.isEmpty())
				{
					final String memberToken = user.getMemberToken();
					final String memberKey = memberUnit.get(0).getMemberKey();
					final String timeStamp = wooliesMemCustomerFacade.decrypt(memberToken, memberKey);
					try
					{
						if (isTokenExpired(timeStamp))
						{
							try
							{
								wooliesMemCustomerFacade.getMemberUser(user, memberUnit.get(0));
								return new ResponseEntity<Object>(HttpStatus.CREATED);
							}
							catch (final WooliesFacadeLayerException e)
							{
								if (e.getMessage().equalsIgnoreCase(WooliesgcWebServicesConstants.ERRCODE_USER_ALREADY_AVAILABLE))
								{
									throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_USER_ALREADY_AVAILABLE,
											WooliesgcWebServicesConstants.ERRRSN_USER_ALREADY_AVAILABLE,
											WooliesgcWebServicesConstants.ERRMSG_USER_ALREADY_AVAILABLE);
								}
								else if (e.getMessage().equalsIgnoreCase(WooliesgcWebServicesConstants.ERRCODE_ISNOTUSEREXIST))
								{
									throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ISNOTUSEREXIST,
											WooliesgcWebServicesConstants.ERRRSN_ISNOTUSEREXIST,
											WooliesgcWebServicesConstants.ERRMSG_ISNOTUSEREXIST);
								}

								else if (e.getMessage().equalsIgnoreCase(WooliesgcWebServicesConstants.ERRCODE_MEMEBER_ACCOUNT_NOT_VALID))
								{
									throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_MEMEBER_ACCOUNT_NOT_VALID,
											WooliesgcWebServicesConstants.ERRRSN_MEMEBER_ACCOUNT_NOT_VALID,
											WooliesgcWebServicesConstants.ERRMSG_MEMEBER_ACCOUNT_NOT_VALID);
								}

								else if (e.getMessage().equalsIgnoreCase(WooliesgcWebServicesConstants.ERRCODE_EMAILFORMAT))
								{
									throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_EMAILFORMAT,
											WooliesgcWebServicesConstants.ERRMSG_EMAILFORMAT,
											WooliesgcWebServicesConstants.ERRRSN_EMAILFORMAT);
								}
								else if (e.getMessage().equalsIgnoreCase(WooliesgcWebServicesConstants.ERRCODE_PHONENOTVALID))
								{
									throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_PHONENOTVALID,
											WooliesgcWebServicesConstants.ERRMSG_PHONENOTVALID,
											WooliesgcWebServicesConstants.ERRRSN_PHONENOTVALID);
								}

							}
						}
						else
						{
							throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_MEMBER_TOKEN_TIME_LIMIT_ERROR,
									WooliesgcWebServicesConstants.ERRRSN_MEMBER_TOKEN_TIME_LIMIT_ERROR,
									WooliesgcWebServicesConstants.ERRMSG_MEMBER_TOKEN_TIME_LIMIT_ERROR);
						}
					}
					catch (final ParseException e)
					{
						throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_MEMBER_TOKEN_DATE_NOT_VALID,
								WooliesgcWebServicesConstants.ERRRSN_MEMBER_TOKEN_DATE_NOT_VALID,
								WooliesgcWebServicesConstants.ERRMSG_MEMBER_TOKEN_DATE_NOT_VALID);
					}
				}
				else
				{
					LOG.info("No Member account available");
					throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_MEMBER_ACCOUNT_NOT_AVAILABLE,
							WooliesgcWebServicesConstants.ERRRSN_MEMBER_ACCOUNT_NOT_AVAILABLE,
							WooliesgcWebServicesConstants.ERRMSG_MEMBER_ACCOUNT_NOT_AVAILABLE);
				}


			}
			else
			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_MEMBER_TOKEN_NOT_AVAILABLE,
						WooliesgcWebServicesConstants.ERRRSN_MEMBER_TOKEN_NOT_AVAILABLE,
						WooliesgcWebServicesConstants.ERRMSG_MEMBER_TOKEN_NOT_AVAILABLE);
			}
		}
		if (user.getCorporateName() == null && user.getAdminUid() == null)
		{
			registerValidate(user, "user", userSignUpDTOValidator);

			for (final AddressData addressData : user.getAddresses())
			{
				addressValidate(addressData, ADDRESS_DATA, minimalAddressValidator);
			}
			final RegisterData registration = getDataMapper().map(user, RegisterData.class);
			try
			{
				if (user.getPolicyAgreement().booleanValue())
				{
					customerFacade.register(registration);
				}
				else
				{
					throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_POLICYAGREEMENT,
							WooliesgcWebServicesConstants.ERRMSG_POLICYAGREEMENT, WooliesgcWebServicesConstants.ERRRSN_POLICYAGREEMENT);
				}
			}
			catch (final DuplicateUidException ex)
			{
				if (ex.getMessage().equals("80003"))
				{
					throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_EMAILFORMAT,
							WooliesgcWebServicesConstants.ERRMSG_EMAILFORMAT, WooliesgcWebServicesConstants.ERRRSN_EMAILFORMAT);
				}
				if (ex.getMessage().equals("80007"))
				{
					throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_PHONENOTVALID,
							WooliesgcWebServicesConstants.ERRMSG_PHONENOTVALID, WooliesgcWebServicesConstants.ERRRSN_PHONENOTVALID);
				}
			}
			catch (final ModelSavingException ex)
			{
				if (ex.getMessage().equals("70011"))
				{
					throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_USERALREADYEXIST,
							WooliesgcWebServicesConstants.ERRMSG_USERALREADYEXIST, WooliesgcWebServicesConstants.ERRRSN_USERALREADYEXIST);
				}
			}
		}

		else if (!(user.getAdminUid() == null))
		{
			validate(user, "user", userManagementValidator);

			try
			{
				wooliesB2BUnitFacade.b2bUserManagement(user);
			}
			catch (final WooliesB2BUserException ex)
			{
				if (ex.getMessage().equals("50004"))
				{
					throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ORDERLIMIT,
							WooliesgcWebServicesConstants.ERRMSG_ORDERLIMIT, WooliesgcWebServicesConstants.ERRRSN_ORDERLIMIT);
				}
				else if (ex.getMessage().equals("70008"))
				{
					throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ADDADMINSANDBUYERS,
							WooliesgcWebServicesConstants.ERRMSG_ADDADMINSANDBUYERS,
							WooliesgcWebServicesConstants.ERRRSN_ADDADMINSANDBUYERS);
				}
				if (ex.getMessage().equals("80003"))
				{
					throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_EMAILFORMAT,
							WooliesgcWebServicesConstants.ERRMSG_EMAILFORMAT, WooliesgcWebServicesConstants.ERRRSN_EMAILFORMAT);
				}
				if (ex.getMessage().equals("80007"))
				{
					throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_PHONENOTVALID,
							WooliesgcWebServicesConstants.ERRMSG_PHONENOTVALID, WooliesgcWebServicesConstants.ERRRSN_PHONENOTVALID);
				}
				if (ex.getMessage().equals(WooliesgcFacadesConstants.CURRENCYNOTNULL))
				{
					throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_CURRENCYNOTNULL,
							WooliesgcWebServicesConstants.ERRMSG_CURRENCYNOTNULL, WooliesgcWebServicesConstants.ERRRSN_CURRENCYNOTNULL);
				}
			}
			catch (final IndexOutOfBoundsException ex)
			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ISNOTEXIST,
						WooliesgcWebServicesConstants.ERRMSG_ISNOTEXIST, WooliesgcWebServicesConstants.ERRRSN_ISNOTEXIST);
			}
			catch (final DuplicateUidException ex)
			{
				if (ex.getMessage().equals("70011"))
				{
					throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_USERALREADYEXIST,
							WooliesgcWebServicesConstants.ERRMSG_USERALREADYEXIST, WooliesgcWebServicesConstants.ERRRSN_USERALREADYEXIST);
				}
			}

		}

		else
		{
			registerValidate(user, "user", userSignUpDTOValidator);
			AddressData addressData1 = null;
			for (final AddressData addressData : user.getAddresses())
			{
				addressValidate(addressData, ADDRESS_DATA, minimalAddressValidator);
				addressData1 = addressData;
			}
			corporateValidate(user, "user", corporateNameValidator);
			final B2BUnitData b2bUnitData = getDataMapper().map(user, B2BUnitData.class);

			final CustomerData customerData = getDataMapper().map(user, CustomerData.class);
			CorporateB2BUnitModel corporateb2bUnitModel = new CorporateB2BUnitModel();

			if (user.getPolicyAgreement().booleanValue())

			{
				if (customerData.getCRNnumber() != null && customerData.getPhone() != null && customerData.getPhone().length() <= 21
						&& customerData.getFirstName() != null && customerData.getLastName() != null && customerData.getEmail() != null
						&& addressData1.getAddress1() != null && addressData1.getPostalCode() != null
						&& addressData1.getState() != null && addressData1.getCity() != null)
				{
					try
					{
						corporateb2bUnitModel = wooliesB2BUnitFacade.createB2BUnit(b2bUnitData);
					}
					catch (final WooliesB2BUserException ex)
					{
						if (ex.getMessage().equals("80003"))
						{
							throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_EMAILFORMAT,
									WooliesgcWebServicesConstants.ERRMSG_EMAILFORMAT, WooliesgcWebServicesConstants.ERRRSN_EMAILFORMAT);
						}
					}
					catch (final ModelSavingException ex)
					{
						if (ex.getMessage().equals("70011"))
						{
							throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_USERALREADYEXIST,
									WooliesgcWebServicesConstants.ERRMSG_USERALREADYEXIST,
									WooliesgcWebServicesConstants.ERRRSN_USERALREADYEXIST);
						}
					}
				}
				if (customerData.getIsCorpAdminUser().booleanValue())
				{
					try
					{
						wooliesB2BUnitFacade.register(customerData, corporateb2bUnitModel);
					}
					catch (final DuplicateUidException ex)
					{
						if (ex.getMessage().equals("80007"))
						{
							throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_PHONENOTVALID,
									WooliesgcWebServicesConstants.ERRMSG_PHONENOTVALID, WooliesgcWebServicesConstants.ERRRSN_PHONENOTVALID);
						}
					}
				}
				else
				{
					throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ADMINCANREGISTER,
							WooliesgcWebServicesConstants.ERRMSG_ADMINCANREGISTER, WooliesgcWebServicesConstants.ERRRSN_ADMINCANREGISTER);
				}
			}
			else
			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_POLICYAGREEMENT,
						WooliesgcWebServicesConstants.ERRMSG_POLICYAGREEMENT, WooliesgcWebServicesConstants.ERRRSN_POLICYAGREEMENT);
			}
		}
		return new ResponseEntity<Object>(HttpStatus.CREATED);
	}

	private void registerValidate(final Object object, final String objectName, final Validator validator)
	{
		final Errors errors = new BeanPropertyBindingResult(object, objectName);
		validator.validate(object, errors);
		if (errors.hasErrors())
		{
			final List<FieldError> fieldErrors = errors.getFieldErrors();
			for (final FieldError fieldError : fieldErrors)
			{
				if (fieldError.getField().equalsIgnoreCase("firstName") || fieldError.getField().equalsIgnoreCase("lastName")
						|| fieldError.getField().equalsIgnoreCase("CRNnumber"))
				{
					throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_FIELDMISSING, fieldError.getField()
							+ WooliesgcWebServicesConstants.ERRMSG_FIELDMISSING, fieldError.getField()
							+ WooliesgcWebServicesConstants.ERRRSN_FIELDMISSING);
				}
			}
		}
	}

	private void addressValidate(final Object object, final String objectName, final Validator validator)
	{
		final Errors errors = new BeanPropertyBindingResult(object, objectName);
		validator.validate(object, errors);
		if (errors.hasErrors())
		{
			final List<FieldError> fieldErrors = errors.getFieldErrors();
			for (final FieldError fieldError : fieldErrors)
			{
				if (fieldError.getField().equalsIgnoreCase(WooliesgcWebServicesConstants.ADDRESS1)
						|| fieldError.getField().equalsIgnoreCase(WooliesgcWebServicesConstants.STATE)
						|| fieldError.getField().equalsIgnoreCase(WooliesgcWebServicesConstants.CITY)
						|| fieldError.getField().equalsIgnoreCase(WooliesgcWebServicesConstants.POSTALCODE))
				{
					throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_FIELDMISSING, fieldError.getField()
							+ WooliesgcWebServicesConstants.ERRMSG_FIELDMISSING, fieldError.getField()
							+ WooliesgcWebServicesConstants.ERRRSN_FIELDMISSING);
				}
			}
		}
	}

	private void corporateValidate(final Object object, final String objectName, final Validator validator)
	{
		final Errors errors = new BeanPropertyBindingResult(object, objectName);
		validator.validate(object, errors);
		if (errors.hasErrors())
		{
			final List<FieldError> fieldErrors = errors.getFieldErrors();
			for (final FieldError fieldError : fieldErrors)
			{
				if (fieldError.getField().equalsIgnoreCase("corporateName") || fieldError.getField().equalsIgnoreCase("companyType"))
				{
					throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_FIELDMISSING, fieldError.getField()
							+ WooliesgcWebServicesConstants.ERRMSG_FIELDMISSING, fieldError.getField()
							+ WooliesgcWebServicesConstants.ERRRSN_FIELDMISSING);
				}
			}
		}
	}



	private boolean isTokenExpired(final String timeStamp) throws ParseException
	{

		final SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");
		final Calendar cal1 = Calendar.getInstance();
		cal1.add(Calendar.MINUTE, 20);
		final String aheadTime = formatter.format(cal1.getTime());
		final Calendar cal2 = Calendar.getInstance();
		cal2.add(Calendar.MINUTE, -20);
		final String oldTime = formatter.format(cal2.getTime());

		final Date date = formatter.parse(timeStamp);
		final Date aheadTimes = formatter.parse(aheadTime);
		final Date oldTimes = formatter.parse(oldTime);
		LOG.info("Input date" + date);
		LOG.debug("ahead date" + aheadTime);
		LOG.debug("old date" + oldTimes);
		if (date.getTime() >= oldTimes.getTime() && date.getTime() <= aheadTimes.getTime())
		{
			return true;
		}
		return false;

	}

	protected String getAbsoluteLocationURL(final HttpServletRequest httpRequest, final String uid)
			throws UnsupportedEncodingException
	{
		final String requestURL = httpRequest.getRequestURL().toString();
		final StringBuilder absoluteURLSb = new StringBuilder(requestURL);
		if (!requestURL.endsWith(YcommercewebservicesConstants.SLASH))
		{
			absoluteURLSb.append(YcommercewebservicesConstants.SLASH);
		}
		absoluteURLSb.append(UriUtils.encodePathSegment(uid, StandardCharsets.UTF_8.name()));
		return absoluteURLSb.toString();
	}

	protected void registerNewUser(final String login, final String password, final String titleCode, final String firstName,
			final String lastName) throws RequestParameterException, DuplicateUidException //NOSONAR
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("registerUser: login=" + sanitize(login));
		}

		if (!EmailValidator.getInstance().isValid(login))
		{
			throw new RequestParameterException("Login [" + sanitize(login) + "] is not a valid e-mail address!",
					RequestParameterException.INVALID, "login");
		}

		final RegisterData registration = new RegisterData();
		registration.setFirstName(firstName);
		registration.setLastName(lastName);
		registration.setLogin(login);
		registration.setPassword(password);
		registration.setTitleCode(titleCode);
		customerFacade.register(registration);
	}


	protected void convertToCustomer(final String password, final String guid) throws RequestParameterException,
			DuplicateUidException //NOSONAR
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("registerUser: guid=" + sanitize(guid));
		}

		try
		{
			customerFacade.changeGuestToCustomer(password, guid);
		}
		catch (final UnknownIdentifierException ex)
		{
			throw new RequestParameterException("Order with guid " + sanitize(guid) + " not found in current BaseStore",
					RequestParameterException.UNKNOWN_IDENTIFIER, "guid", ex);
		}
		catch (final IllegalArgumentException ex)
		{
			// Occurs when order does not belong to guest user.
			// For security reasons it's better to treat it as "unknown identifier" error
			throw new RequestParameterException("Order with guid " + sanitize(guid) + " not found in current BaseStore",
					RequestParameterException.UNKNOWN_IDENTIFIER, "guid", ex);
		}
	}


	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get customer profile", notes = "Returns customer profile.")
	@ApiBaseSiteIdAndUserIdParam
	public UserWsDTO getUser(
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL,PROFILESUMMERY") @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws WooliesFacadeLayerException
	{
		final UserProfileData userProfileData = profileSummeryFacade.getCurrentCustomer();
		return getDataMapper().map(userProfileData, UserWsDTO.class, fields);
	}

	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}/guid/{anonymousCartGuid}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get customer profile", notes = "Returns customer profile.")
	@ApiBaseSiteIdAndUserIdParam
	public UserWsDTO getUser(
			@ApiParam(value = "Anonymous cart GUID.", required = false) @PathVariable final String anonymousCartGuid,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL,PROFILESUMMERY") @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws CommerceCartRestorationException, WooliesFacadeLayerException
	{
		if (anonymousCartGuid != null)
		{
			wooliesDefaultCartFacade.restoreWooliesAnonymousCartAndTakeOwnership(anonymousCartGuid);
			getCartFacade().removeStaleCarts();
		}
		final UserProfileData userProfileData = profileSummeryFacade.getCurrentCustomer();
		return getDataMapper().map(userProfileData, UserWsDTO.class, fields);
	}

	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}/guidForCart/{guid}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get customer profile", notes = "Returns customer profile.")
	@ApiBaseSiteIdAndUserIdParam
	public GuestCartProfileWsDTO getGuestCartProfile(
			@ApiParam(value = "Anonymous cart GUID.", required = false) @PathVariable final String guid,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL,PROFILESUMMERY") @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws CommerceCartRestorationException, WooliesFacadeLayerException
	{
		GuestCartProfileData guestCartProfileData = new GuestCartProfileData();
		if (guid != null)
		{
			try
			{
				guestCartProfileData = profileSummeryFacade.getCartDetailsForAnonymousUser(guid);
			}
			catch (final WooliesFacadeLayerException ex)
			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERR_CODE_GUEST_CART,
						WooliesgcWebServicesConstants.ERRMSG_GUEST_CART_PROFILE + ":" + (guid),
						WooliesgcWebServicesConstants.ERRRSN_GUEST_CART_PROFILE + ":" + (guid));
			}
		}
		return getDataMapper().map(guestCartProfileData, GuestCartProfileWsDTO.class, fields);
	}

	@RequestMapping(value = "/{userId}/getCorporateDetails", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get account details", notes = "Returns account details.")
	@ApiBaseSiteIdAndUserIdParam
	public CorporateDetailsDTO viewCorporateDetails(
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws WooliesB2BUserException
	{
		B2BUnitData b2bUnitData = new B2BUnitData();
		try
		{
			b2bUnitData = corporateDetailsFacade.getCorporateDetails();
		}
		catch (final WooliesB2BUserException ex)
		{
			if (ex.getMessage().equals("70003"))
			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ISNOTEXIST,
						WooliesgcWebServicesConstants.ERRMSG_ISNOTEXIST, WooliesgcWebServicesConstants.ERRRSN_ISNOTEXIST);
			}
		}
		return getDataMapper().map(b2bUnitData, CorporateDetailsDTO.class, fields);
	}


	@RequestMapping(value = "/{userId}/updateCorporateDetails", method = RequestMethod.PUT, consumes =
	{ MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Updates customer profile", notes = "Updates customer profile. Only attributes provided in the request body will be changed.")
	@ApiBaseSiteIdAndUserIdParam
	public void updateCorporateDetails(
			@ApiParam(value = "User's object.", required = true) @RequestBody final CorporateDetailsDTO corporateDto,
			@ApiParam("User identifier.") @PathVariable final String userId) throws DuplicateUidException,
			RequestParameterException, WooliesB2BUserException
	{
		validate(corporateDto, "corporateName", corporateNameValidator);
		try
		{
			corporateDetailsFacade.updateCorporateDetails(corporateDto);
		}
		catch (final WooliesB2BUserException ex)
		{
			if (ex.getMessage().equals(WooliesgcWebServicesConstants.ERRCODE_ISNOTADMIN))
			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ISNOTADMIN,
						WooliesgcWebServicesConstants.ERRMSG_ISNOTADMIN, WooliesgcWebServicesConstants.ERRRSN_ISNOTADMIN);
			}
			else if (ex.getMessage().equals(WooliesgcWebServicesConstants.ERRCODE_ISNOTEXIST))
			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ISNOTEXIST,
						WooliesgcWebServicesConstants.ERRMSG_ISNOTEXIST, WooliesgcWebServicesConstants.ERRRSN_ISNOTEXIST);
			}
		}
	}


	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}", method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(hidden = true, value = "Updates customer profile", notes = "Updates customer profile. Attributes not provided in the request body will be defined again (set to null or default).")
	@ApiImplicitParams(
	{
			@ApiImplicitParam(name = "baseSiteId", value = "Base site identifier", required = true, dataType = "String", paramType = "path"),
			@ApiImplicitParam(name = "userId", value = "User identifier.", required = true, dataType = "String", paramType = "path"),
			@ApiImplicitParam(name = "language", value = "Customer's language.", required = false, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "currency", value = "Customer's currency.", required = false, dataType = "String", paramType = "query") })
	public void putUser(
			@ApiParam("Customer's first name.") @RequestParam final String firstName,
			@ApiParam("Customer's last name.") @RequestParam final String lastName,
			@ApiParam(value = "Customer's title code. For a list of codes, see /{baseSiteId}/titles resource", required = true) @RequestParam(required = true) final String titleCode,
			final HttpServletRequest request) throws DuplicateUidException
	{
		final CustomerData customer = customerFacade.getCurrentCustomer();
		if (LOG.isDebugEnabled())
		{
			LOG.debug("putCustomer: userId=" + customer.getUid());
		}
		customer.setFirstName(firstName);
		customer.setLastName(lastName);
		customer.setTitleCode(titleCode);
		customer.setLanguage(null);
		customer.setCurrency(null);
		httpRequestCustomerDataPopulator.populate(request, customer);

		customerFacade.updateFullProfile(customer);
	}


	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}", method = RequestMethod.PUT, consumes =
	{ MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Updates customer profile", notes = "Updates customer profile. Attributes not provided in the request body will be defined again (set to null or default).")
	@ApiBaseSiteIdAndUserIdParam
	public void putUser(@ApiParam(value = "User's object", required = true) @RequestBody final UserWsDTO user)
			throws DuplicateUidException
	{
		validate(user, "user", putUserDTOValidator);

		final CustomerData customer = customerFacade.getCurrentCustomer();
		if (LOG.isDebugEnabled())
		{
			LOG.debug("putCustomer: userId=" + customer.getUid());
		}

		getDataMapper().map(user, customer, "firstName,lastName,titleCode,currency(isocode),language(isocode)", true);
		customerFacade.updateFullProfile(customer);
	}


	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}", method = RequestMethod.PATCH)
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(hidden = true, value = "Updates customer profile", notes = "Updates customer profile. Only attributes provided in the request body will be changed.")
	@ApiImplicitParams(
	{
			@ApiImplicitParam(name = "baseSiteId", value = "Base site identifier", required = true, dataType = "String", paramType = "path"),
			@ApiImplicitParam(name = "userId", value = "User identifier", required = true, dataType = "String", paramType = "path"),
			@ApiImplicitParam(name = "firstName", value = "Customer's first name", required = false, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "lastName", value = "Customer's last name", required = false, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "titleCode", value = "Customer's title code. Customer's title code. For a list of codes, see /{baseSiteId}/titles resource", required = false, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "language", value = "Customer's language", required = false, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "currency", value = "Customer's currency", required = false, dataType = "String", paramType = "query") })
	public void updateUser(final HttpServletRequest request) throws DuplicateUidException
	{
		final CustomerData customer = customerFacade.getCurrentCustomer();
		if (LOG.isDebugEnabled())
		{
			LOG.debug("updateUser: userId=" + customer.getUid());
		}
		httpRequestCustomerDataPopulator.populate(request, customer);
		customerFacade.updateFullProfile(customer);
	}


	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}/CRNnumber/{CRNnumber}", method = RequestMethod.PATCH, consumes =
	{ MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Updates customer profile", notes = "Updates customer profile. Only attributes provided in the request body will be changed.")
	@ApiBaseSiteIdAndUserIdParam
	public void updateUser(@ApiParam("User identifier.") @PathVariable final String userId,
			@ApiParam(value = "crnNumber number from Gigiya.", required = true) @PathVariable final String CRNnumber,
			@ApiParam(value = "User's object.", required = true) @RequestBody final UserWsDTO user) throws DuplicateUidException,
			RequestParameterException
	{
		final CustomerData customer = customerFacade.getCurrentCustomer();
		if (LOG.isDebugEnabled())
		{
			LOG.debug("updateUser: userId=" + customer.getUid());
		}
		if (null != customer.getUid() && CRNnumber.equals(customer.getUid().toString()))
		{
			final List<AddressData> addressList = new ArrayList<AddressData>();
			for (final AddressWsDTO addressWTO : user.getAddresses())
			{
				final AddressData addressdata = new AddressData();
				getDataMapper().map(addressWTO, addressdata, false);
				addressList.add(addressdata);
			}

			getDataMapper().map(user, customer, "firstName,lastName,phone,dob,cardNo", false);
			if (null != user.getUid() && userId.equalsIgnoreCase(user.getUid()))
			{
				customer.setDisplayUid(userId);
				customer.setUid(userId);
			}
			else
			{
				customer.setDisplayUid(user.getUid());
				customer.setUid(user.getUid());
			}

			customer.setAddresses(addressList);
			customerFacade.updateFullProfile(customer);
		}
		else
		{
			throw new RequestParameterException("crnNumber does not exist for user " + customer.getUid());
		}
	}


	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Delete customer profile", notes = "Removes customer profile.")
	@ApiBaseSiteIdAndUserIdParam
	public void deactivateUser()
	{
		final CustomerData customer = customerFacade.closeAccount();
		if (LOG.isDebugEnabled())
		{
			LOG.debug("deactivateUser: userId=" + customer.getUid());
		}
	}

	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}/login", method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Changes customer's login", notes = "Changes customer's login.")
	@ApiBaseSiteIdAndUserIdParam
	public void changeLogin(
			@ApiParam(value = "Customer's new login. Customer login is case insensitive.", required = true) @RequestParam final String newLogin,
			@ApiParam(value = "Customer's current password.", required = true) @RequestParam final String password)
			throws DuplicateUidException, PasswordMismatchException, RequestParameterException //NOSONAR
	{
		if (!EmailValidator.getInstance().isValid(newLogin))
		{
			throw new RequestParameterException("Login [" + newLogin + "] is not a valid e-mail address!",
					RequestParameterException.INVALID, "newLogin");
		}
		customerFacade.changeUid(newLogin, password);
	}


	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}/password", method = RequestMethod.PUT)
	@ResponseStatus(value = HttpStatus.ACCEPTED)
	@ApiOperation(value = "Changes customer's password", notes = "Changes customer's password.")
	@ApiBaseSiteIdAndUserIdParam
	public void changePassword(@ApiParam("User identifier.") @PathVariable final String userId,
			@ApiParam("Old password. Required only for ROLE_CUSTOMERGROUP") @RequestParam(required = false) final String old,
			@ApiParam(value = "New password.", required = true) @RequestParam(value = "new") final String newPassword)
	{
		final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		final UserSignUpWsDTO customer = new UserSignUpWsDTO();
		customer.setPassword(newPassword);
		validate(customer, "password", passwordStrengthValidator);
		if (containsRole(auth, "ROLE_TRUSTED_CLIENT") || containsRole(auth, "ROLE_CUSTOMERMANAGERGROUP"))
		{
			userService.setPassword(userId, newPassword);
		}
		else
		{
			if (StringUtils.isEmpty(old))
			{
				throw new RequestParameterException("Request parameter 'old' is missing.", RequestParameterException.MISSING, "old");
			}
			customerFacade.changePassword(old, newPassword);
		}
	}

	protected boolean containsRole(final Authentication auth, final String role)
	{
		for (final GrantedAuthority ga : auth.getAuthorities())
		{
			if (ga.getAuthority().equals(role))
			{
				return true;
			}
		}
		return false;
	}


	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}/addresses", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get customer's addresses", notes = "Returns customer's addresses.")
	@ApiBaseSiteIdAndUserIdParam
	@ApiResponse(code = 200, message = "List of customer's addresses")
	public AddressListWsDTO getAddresses(
			@ApiParam(value = "User id", required = true) @PathVariable final String userId,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(value = "addressType", required = false) final String addressType,
			@RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) throws DuplicateUidException,
			RequestParameterException
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("GetUserAddresses: userId=" + sanitize(userId));
		}
		List<AddressData> addressList = new ArrayList<>();

		if (null != userId && userId.equalsIgnoreCase(WooliesgcWebServicesConstants.ANONYMOUS))
		{
			final AddressDataList addressDataList = new AddressDataList();
			addressDataList.setAddresses(addressList);
			return getDataMapper().map(addressDataList, AddressListWsDTO.class, fields);
		}
		if (null != userId)
		{
			if (null != addressType && StringUtils.isNotBlank(addressType))
			{
				if (addressType.equalsIgnoreCase(WooliesgcWebServicesConstants.BILLINGTYPE))
				{
					try
					{
						addressList = wooliesUserFacade.getWooliesBillingAddressBook();
					}
					catch (final WooliesWebserviceException e)
					{
						throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ADDRESSTYPE,
								WooliesgcWebServicesConstants.ERRRSN_ADDRESSTYPE, WooliesgcWebServicesConstants.ERRMSG_ADDRESSTYPE);
					}
				}
				else if (addressType.equalsIgnoreCase(WooliesgcWebServicesConstants.SHIPPINGTYPE))
				{
					try
					{
						addressList = wooliesUserFacade.getWooliesShippingAddressBook();
					}
					catch (final WooliesWebserviceException e)
					{
						throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ADDRESSTYPE,
								WooliesgcWebServicesConstants.ERRRSN_ADDRESSTYPE, WooliesgcWebServicesConstants.ERRMSG_ADDRESSTYPE);
					}
				}
				else
				{
					throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ADDRESSTYPE,
							WooliesgcWebServicesConstants.ERRRSN_ADDRESSTYPE, WooliesgcWebServicesConstants.ERRMSG_ADDRESSTYPE);
				}
			}
			else
			{
				addressList = wooliesUserFacade.getWooliesAddressBook();
			}
			final AddressDataList addressDataList = new AddressDataList();
			addressDataList.setAddresses(addressList);
			return getDataMapper().map(addressDataList, AddressListWsDTO.class, fields);
		}

		else
		{
			throw new RequestParameterException("Customer with UserId does not exists");
		}
	}

	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}/addresses", method = RequestMethod.POST)
	@ResponseBody
	@ResponseStatus(value = HttpStatus.CREATED)
	@ApiOperation(hidden = true, value = "Creates a new address.", notes = "Creates a new address.")
	@ApiImplicitParams(
	{
			@ApiImplicitParam(name = "baseSiteId", value = "Base site identifier", required = true, dataType = "String", paramType = "path"),
			@ApiImplicitParam(name = "userId", value = "User identifier", required = true, dataType = "String", paramType = "path"),
			@ApiImplicitParam(name = "firstName", value = "Customer's first name", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "lastName", value = "Customer's last name", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "titleCode", value = "Customer's title code. Customer's title code. For a list of codes, see /{baseSiteId}/titles resource", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "country.isocode", value = "Country isocode. This parameter is required and have influence on how rest of parameters are validated (e.g. if parameters are required : line1,line2,town,postalCode,region.isocode)", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "line1", value = "First part of address. If this parameter is required depends on country (usually it is required).", required = false, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "line2", value = "Second part of address. If this parameter is required depends on country (usually it is not required)", required = false, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "town", value = "Town name. If this parameter is required depends on country (usually it is required)", required = false, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "postalCode", value = "Postal code. Isocode for region. If this parameter is required depends on country.", required = false, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "region.isocode", value = "Second part of address. If this parameter is required depends on country (usually it is not required)", required = false, dataType = "String", paramType = "query") })
	public AddressWsDTO createAddress(
			final HttpServletRequest request,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws WebserviceValidationException //NOSONAR
	{
		final AddressData addressData = super.createAddressInternal(request);
		return getDataMapper().map(addressData, AddressWsDTO.class, fields);
	}


	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}/addresses", method = RequestMethod.POST, consumes =
	{ MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.CREATED)
	@ApiOperation(value = "Created a new address.", notes = "Created a new address.")
	@ApiBaseSiteIdAndUserIdParam
	public AddressWsDTO createAddress(
			@ApiParam(value = "Address object.", required = true) @RequestBody final AddressWsDTO address,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws WebserviceValidationException, DuplicateUidException //NOSONAR
	{
		validate(address, "address", getAddressDTOValidator());
		final AddressData addressData = getDataMapper().map(address, AddressData.class, fields);
		final Boolean shippingFlag = addressData.getIsShipping();
		final Boolean billingFlag = addressData.getIsBilling();
		if (billingFlag != shippingFlag)
		{
			addressData.setVisibleInAddressBook(true);
			getUserFacade().addAddress(addressData);
		}
		else
		{
			throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ADDADDRESS,
					WooliesgcWebServicesConstants.ERRMSG_ADDADDRESS, WooliesgcWebServicesConstants.ERRRSN_ADDADDRESS);
		}
		return getDataMapper().map(addressData, AddressWsDTO.class, fields);
	}


	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}/addresses/{addressId}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get info about address", notes = "Returns detailed information about address with a given id.")
	@ApiBaseSiteIdAndUserIdParam
	public AddressWsDTO getAddress(
			@ApiParam(value = "Address identifier.", required = true) @PathVariable final String addressId,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws WebserviceValidationException, DuplicateUidException //NOSONAR
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("getAddress: id=" + sanitize(addressId));
		}
		final AddressData addressData = wooliesUserFacade.getAddressModelForAddressId(addressId);
		if (addressData == null)
		{
			throw new RequestParameterException("Address with given id: '" + sanitize(addressId)
					+ "' doesn't exist or belong to another user", //NOSONAR
					RequestParameterException.INVALID, "addressId");
		}

		return getDataMapper().map(addressData, AddressWsDTO.class, fields);
	}


	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}/addresses/{addressId}", method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(hidden = true, value = "Updates the address", notes = "Updates the address. Attributes not provided in the request will be defined again (set to null or default).")
	@ApiImplicitParams(
	{
			@ApiImplicitParam(name = "baseSiteId", value = "Base site identifier", required = true, dataType = "String", paramType = "path"),
			@ApiImplicitParam(name = "userId", value = "User identifier", required = true, dataType = "String", paramType = "path"),
			@ApiImplicitParam(name = "firstName", value = "Customer's first name", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "lastName", value = "Customer's last name", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "titleCode", value = "Customer's title code. Customer's title code. For a list of codes, see /{baseSiteId}/titles resource", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "country.isocode", value = "Country isocode. This parameter is required and have influence on how rest of parameters are validated (e.g. if parameters are required : line1,line2,town,postalCode,region.isocode)", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "line1", value = "First part of address. If this parameter is required depends on country (usually it is required).", required = false, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "line2", value = "Second part of address. If this parameter is required depends on country (usually it is not required)", required = false, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "town", value = "Town name. If this parameter is required depends on country (usually it is required)", required = false, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "postalCode", value = "Postal code. Isocode for region. If this parameter is required depends on country.", required = false, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "region.isocode", value = "Isocode for region. If this parameter is required depends on country.", required = false, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "defaultAddress", value = "Parameter specifies if address should be default for customer.", required = false, dataType = "String", paramType = "query") })
	public void putAddress(@ApiParam(value = "Address identifier.", required = true) @PathVariable final String addressId,
			final HttpServletRequest request) throws WebserviceValidationException //NOSONAR
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("editAddress: id=" + sanitize(addressId));
		}
		final AddressData addressData = getUserFacade().getAddressForCode(addressId);
		if (addressData == null)
		{
			throw new RequestParameterException("Address with given id: '" + sanitize(addressId)
					+ "' doesn't exist or belong to another user", RequestParameterException.INVALID, "addressId");
		}
		final boolean isAlreadyDefaultAddress = addressData.isDefaultAddress();
		addressData.setFirstName(null);
		addressData.setLastName(null);
		addressData.setCountry(null);
		addressData.setLine1(null);
		addressData.setLine2(null);
		addressData.setPostalCode(null);
		addressData.setRegion(null);
		addressData.setTitle(null);
		addressData.setTown(null);
		addressData.setDefaultAddress(false);
		addressData.setFormattedAddress(null);

		getHttpRequestAddressDataPopulator().populate(request, addressData);

		final Errors errors = new BeanPropertyBindingResult(addressData, ADDRESS_DATA);
		getAddressValidator().validate(addressData, errors);

		if (errors.hasErrors())
		{
			throw new WebserviceValidationException(errors);
		}
		getUserFacade().editAddress(addressData);

		if (!isAlreadyDefaultAddress && addressData.isDefaultAddress())
		{
			getUserFacade().setDefaultAddress(addressData);
		}
	}


	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}/addresses/{addressId}", method = RequestMethod.PUT, consumes =
	{ MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Updates the address", notes = "Updates the address. Attributes not provided in the request will be defined again (set to null or default).")
	@ApiBaseSiteIdAndUserIdParam
	public void putAddress(@ApiParam(value = "Address identifier.", required = true) @PathVariable final String addressId,
			@ApiParam(value = "Address object.", required = true) @RequestBody final AddressWsDTO address)
			throws WebserviceValidationException //NOSONAR
	{
		validate(address, "address", getAddressDTOValidator());
		final AddressData addressData = getUserFacade().getAddressForCode(addressId);
		if (addressData == null)
		{
			throw new RequestParameterException("Address with given id: '" + sanitize(addressId)
					+ "' doesn't exist or belong to another user", RequestParameterException.INVALID, "addressId");
		}
		final boolean isAlreadyDefaultAddress = addressData.isDefaultAddress();
		addressData.setFormattedAddress(null);
		getDataMapper().map(address, addressData,
				"firstName,lastName,titleCode,line1,line2,town,postalCode,region(isocode),country(isocode),defaultAddress", true);

		getUserFacade().editAddress(addressData);

		if (!isAlreadyDefaultAddress && addressData.isDefaultAddress())
		{
			getUserFacade().setDefaultAddress(addressData);
		}
	}


	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}/addresses/{addressId}", method = RequestMethod.PATCH)
	@ApiOperation(hidden = true, value = "Updates the address", notes = "Updates the address. Only attributes provided in the request body will be changed.")
	@ApiImplicitParams(
	{
			@ApiImplicitParam(name = "baseSiteId", value = "Base site identifier", required = true, dataType = "String", paramType = "path"),
			@ApiImplicitParam(name = "userId", value = "User identifier", required = true, dataType = "String", paramType = "path"),
			@ApiImplicitParam(name = "firstName", value = "Customer's first name", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "lastName", value = "Customer's last name", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "titleCode", value = "Customer's title code. Customer's title code. For a list of codes, see /{baseSiteId}/titles resource", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "country.isocode", value = "Country isocode. This parameter is required and have influence on how rest of parameters are validated (e.g. if parameters are required : line1,line2,town,postalCode,region.isocode)", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "line1", value = "First part of address. If this parameter is required depends on country (usually it is required).", required = false, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "line2", value = "Second part of address. If this parameter is required depends on country (usually it is not required)", required = false, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "town", value = "Town name. If this parameter is required depends on country (usually it is required)", required = false, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "postalCode", value = "Postal code. Isocode for region. If this parameter is required depends on country.", required = false, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "region.isocode", value = "Isocode for region. If this parameter is required depends on country.", required = false, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "defaultAddress", value = "Parameter specifies if address should be default for customer.", required = false, dataType = "String", paramType = "query") })
	@ResponseStatus(HttpStatus.OK)
	public void patchAddress(@ApiParam(value = "Address identifier.", required = true) @PathVariable final String addressId,
			final HttpServletRequest request) throws WebserviceValidationException //NOSONAR
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("editAddress: id=" + sanitize(addressId));
		}
		final AddressData addressData = getUserFacade().getAddressForCode(addressId);
		if (addressData == null)
		{
			throw new RequestParameterException("Address with given id: '" + sanitize(addressId)
					+ "' doesn't exist or belong to another user", RequestParameterException.INVALID, "addressId");
		}
		final boolean isAlreadyDefaultAddress = addressData.isDefaultAddress();
		addressData.setFormattedAddress(null);
		final Errors errors = new BeanPropertyBindingResult(addressData, ADDRESS_DATA);

		getHttpRequestAddressDataPopulator().populate(request, addressData);
		getAddressValidator().validate(addressData, errors);

		if (errors.hasErrors())
		{
			throw new WebserviceValidationException(errors);
		}

		if (addressData.getId().equals(getUserFacade().getDefaultAddress().getId()))
		{
			addressData.setDefaultAddress(true);
			addressData.setVisibleInAddressBook(true);
		}
		if (!isAlreadyDefaultAddress && addressData.isDefaultAddress())
		{
			getUserFacade().setDefaultAddress(addressData);
		}
		getUserFacade().editAddress(addressData);
	}


	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}/addresses/{addressId}", method = RequestMethod.PATCH, consumes =
	{ MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ApiOperation(value = "Updates the address", notes = "Updates the address. Only attributes provided in the request body will be changed.")
	@ApiBaseSiteIdAndUserIdParam
	@ResponseStatus(HttpStatus.OK)
	public void patchAddress(@ApiParam(value = "Address identifier.", required = true) @PathVariable final String addressId,
			@ApiParam(value = "Address object", required = true) @RequestBody final AddressWsDTO address,
			final HttpServletRequest request, @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws WebserviceValidationException, DuplicateUidException //NOSONAR
	{

		if (LOG.isDebugEnabled())
		{
			LOG.debug("editAddress: id=" + sanitize(addressId));
		}
		final AddressData addressData = wooliesUserFacade.getAddressModelForAddressId(addressId);
		if (addressData == null)
		{
			throw new RequestParameterException("Address with given id: '" + sanitize(addressId)
					+ "' doesn't exist or belong to another user", RequestParameterException.INVALID, "addressId");
		}
		addressData.setFormattedAddress(null);
		getDataMapper().map(address, addressData, fields);
		validate(addressData, "address", getAddressValidator());
		wooliesUserFacade.editAddress(addressData);


	}

	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}/addresses/{addressId}", method = RequestMethod.DELETE)
	@ApiOperation(value = "Delete customer's address", notes = "Removes customer's address.")
	@ApiBaseSiteIdAndUserIdParam
	@ResponseStatus(HttpStatus.OK)
	public void deleteAddress(@ApiParam(value = "Address identifier.", required = true) @PathVariable final String addressId)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("deleteAddress: id=" + sanitize(addressId));
		}
		final AddressData address = wooliesUserFacade.getAddressModelForAddressId(addressId);
		if (null != address)
		{
			getUserFacade().removeAddress(address);
		}
		else
		{
			throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_DELETEADDRESS,
					WooliesgcWebServicesConstants.ERRMSG_DELETEADDRESS, WooliesgcWebServicesConstants.ERRRSN_DELETEADDRESS);
		}
	}


	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}/addresses/verification", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(hidden = true, value = "Verifies the address", notes = "Verifies the address.")
	@ApiImplicitParams(
	{
			@ApiImplicitParam(name = "baseSiteId", value = "Base site identifier", required = true, dataType = "String", paramType = "path"),
			@ApiImplicitParam(name = "userId", value = "User identifier", required = true, dataType = "String", paramType = "path"),
			@ApiImplicitParam(name = "country.isocode", value = "Country isocode. This parameter is required and have influence on how rest of parameters are validated (e.g. if parameters are required : line1,line2,town,postalCode,region.isocode)", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "line1", value = "First part of address. If this parameter is required depends on country (usually it is required).", required = false, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "line2", value = "Second part of address. If this parameter is required depends on country (usually it is not required)", required = false, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "town", value = "Town name. If this parameter is required depends on country (usually it is required)", required = false, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "postalCode", value = "Postal code. Isocode for region. If this parameter is required depends on country.", required = false, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "region.isocode", value = "Isocode for region. If this parameter is required depends on country.", required = false, dataType = "String", paramType = "query") })
	public AddressValidationWsDTO verifyAddress(
			final HttpServletRequest request,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		final AddressData addressData = new AddressData();
		final Errors errors = new BeanPropertyBindingResult(addressData, ADDRESS_DATA);
		AddressValidationData validationData = new AddressValidationData();

		getHttpRequestAddressDataPopulator().populate(request, addressData);
		if (isAddressValid(addressData, errors, validationData))
		{
			validationData = verifyAddresByService(addressData, errors, validationData);
		}
		return getDataMapper().map(validationData, AddressValidationWsDTO.class, fields);
	}


	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}/addresses/verification", method = RequestMethod.POST, consumes =
	{ MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ApiOperation(value = "Verifies address", notes = "Verifies address.")
	@ApiBaseSiteIdAndUserIdParam
	@ResponseBody
	public AddressValidationWsDTO verifyAddress(
			@ApiParam(value = "Address object.", required = true) @RequestBody final AddressWsDTO address,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		// validation is a bit different here
		final AddressData addressData = getDataMapper().map(address, AddressData.class,
				"firstName,lastName,titleCode,line1,line2,town,postalCode,country(isocode),region(isocode)");
		final Errors errors = new BeanPropertyBindingResult(addressData, ADDRESS_DATA);
		AddressValidationData validationData = new AddressValidationData();

		if (isAddressValid(addressData, errors, validationData))
		{
			validationData = verifyAddresByService(addressData, errors, validationData);
		}
		return getDataMapper().map(validationData, AddressValidationWsDTO.class, fields);
	}

	/**
	 * Checks if address is valid by a validators
	 *
	 * @formparam addressData
	 * @formparam errors
	 * @formparam validationData
	 * @return true - adress is valid , false - address is invalid
	 */
	protected boolean isAddressValid(final AddressData addressData, final Errors errors, final AddressValidationData validationData)
	{
		getAddressValidator().validate(addressData, errors);

		if (errors.hasErrors())
		{
			validationData.setDecision(AddressVerificationDecision.REJECT.toString());
			validationData.setErrors(createResponseErrors(errors));
			return false;
		}
		return true;
	}

	/**
	 * Verifies address by commerce service
	 *
	 * @formparam addressData
	 * @formparam errors
	 * @formparam validationData
	 * @return object with verification errors and suggested addresses list
	 */
	protected AddressValidationData verifyAddresByService(final AddressData addressData, final Errors errors,
			final AddressValidationData validationData)
	{
		final AddressVerificationResult<AddressVerificationDecision> verificationDecision = addressVerificationFacade
				.verifyAddressData(addressData);
		if (verificationDecision.getErrors() != null && !verificationDecision.getErrors().isEmpty())
		{
			populateErrors(errors, verificationDecision);
			validationData.setErrors(createResponseErrors(errors));
		}

		validationData.setDecision(verificationDecision.getDecision().toString());

		if (verificationDecision.getSuggestedAddresses() != null && !verificationDecision.getSuggestedAddresses().isEmpty())
		{
			final AddressDataList addressDataList = new AddressDataList();
			addressDataList.setAddresses(verificationDecision.getSuggestedAddresses());
			validationData.setSuggestedAddressesList(addressDataList);
		}

		return validationData;
	}

	protected ErrorListWsDTO createResponseErrors(final Errors errors)
	{
		final List<ErrorWsDTO> webserviceErrorDto = new ArrayList<>();
		validationErrorConverter.convert(errors, webserviceErrorDto);
		final ErrorListWsDTO webserviceErrorList = new ErrorListWsDTO();
		webserviceErrorList.setErrors(webserviceErrorDto);
		return webserviceErrorList;
	}

	/**
	 * Populates Errors object
	 *
	 * @param errors
	 * @param addressVerificationResult
	 */
	protected void populateErrors(final Errors errors,
			final AddressVerificationResult<AddressVerificationDecision> addressVerificationResult)
	{
		addressDataErrorsPopulator.populate(addressVerificationResult, errors);
	}


	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}/paymentdetails", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get customer's credit card payment details list.", notes = "Return customer's credit card payment details list.")
	@ApiBaseSiteIdAndUserIdParam
	public PaymentDetailsListWsDTO getPaymentInfos(
			@ApiParam(value = "Type of payment details.", required = true) @RequestParam(required = false, defaultValue = "false") final boolean saved,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("getPaymentInfos");
		}

		final CCPaymentInfoDatas paymentInfoDataList = new CCPaymentInfoDatas();
		paymentInfoDataList.setPaymentInfos(getUserFacade().getCCPaymentInfos(saved));

		return getDataMapper().map(paymentInfoDataList, PaymentDetailsListWsDTO.class, fields);
	}


	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}/paymentdetails/{paymentDetailsId}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get customer's credit card payment details.", notes = "Returns customer's credit card payment details for a given id.")
	@ApiBaseSiteIdAndUserIdParam
	public PaymentDetailsWsDTO getPaymentDetails(
			@ApiParam(value = "Payment details identifier.", required = true) @PathVariable final String paymentDetailsId,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		return getDataMapper().map(getPaymentInfo(paymentDetailsId), PaymentDetailsWsDTO.class, fields);
	}

	public CCPaymentInfoData getPaymentInfo(final String paymentDetailsId)
	{
		LOG.debug("getPaymentInfo : id = " + sanitize(paymentDetailsId));
		try
		{
			final CCPaymentInfoData paymentInfoData = getUserFacade().getCCPaymentInfoForCode(paymentDetailsId);
			if (paymentInfoData == null)
			{
				throw new RequestParameterException("Payment details [" + sanitize(paymentDetailsId) + "] not found.",
						RequestParameterException.UNKNOWN_IDENTIFIER, "paymentDetailsId");
			}
			return paymentInfoData;
		}
		catch (final PKException e)
		{
			throw new RequestParameterException("Payment details [" + sanitize(paymentDetailsId) + "] not found.",
					RequestParameterException.UNKNOWN_IDENTIFIER, "paymentDetailsId", e);
		}
	}


	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}/paymentdetails/{paymentDetailsId}", method = RequestMethod.DELETE)
	@ApiOperation(value = "Delete customer's credit card payment details.", notes = "Removes customer's credit card payment details based on its ID.")
	@ApiBaseSiteIdAndUserIdParam
	@ResponseStatus(HttpStatus.OK)
	public void deletePaymentInfo(
			@ApiParam(value = "Payment details identifier.", required = true) @PathVariable final String paymentDetailsId)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("deletePaymentInfo: id = " + sanitize(paymentDetailsId));
		}
		getPaymentInfo(paymentDetailsId);
		getUserFacade().removeCCPaymentInfo(paymentDetailsId);
	}



	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}/paymentdetails/{paymentDetailsId}", method = RequestMethod.PATCH)
	@ApiOperation(hidden = true, value = "Updates existing customer's credit card payment details. ", notes = "Updates existing customer's credit card payment details based on its ID. Only attributes given in request will be changed.")
	@ApiImplicitParams(
	{
			@ApiImplicitParam(name = "baseSiteId", value = "Base site identifier", required = true, dataType = "String", paramType = "path"),
			@ApiImplicitParam(name = "userId", value = "User identifier", required = true, dataType = "String", paramType = "path"),
			@ApiImplicitParam(name = "accountHolderName", value = "Name on card.", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "cardType", value = "Card type. Call GET /{baseSiteId}/cardtypes beforehand to see what card types are supported.", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "expiryMonth", value = "Month of expiry date.", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "expiryYear", value = "Year of expiry date.", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "issueNumber", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "startMonth", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "startYear", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "subscriptionId", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "saved", value = "Parameter defines if the payment details should be saved for the customer and than could be reused", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "defaultPaymentInfo", value = "Parameter defines if the payment details should be used as default for customer.", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "billingAddress.firstName", value = "Customer's first name.", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "billingAddress.lastName", value = "Customer's last name.", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "billingAddress.titleCode", value = "Customer's title code. For a list of codes, see /{baseSiteId}/titles resource", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "billingAddress.country.isocode", value = "Country isocode. This parameter havs influence on how rest of address parameters are validated (e.g. if parameters are required: line1,line2,town,postalCode,region.isocode)", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "billingAddress.line1", value = "If this parameter is required depends on country (usually it is required).", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "billingAddress.line2", value = "Second part of address. If this parameter is required depends on country (usually it is not required)", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "bbillingAddress.town", value = "If this parameter is required depends on country (usually it is required)", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "billingAddress.postalCode", value = "Postal code. If this parameter is required depends on country (usually it is required)", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "billingAddressregion.isocode", value = "Isocode for region. If this parameter is required depends on country.", required = false, dataType = "String", paramType = "query") })
	@ResponseStatus(HttpStatus.OK)
	public void updatePaymentInfo(
			@ApiParam(value = "Payment details identifier.", required = true) @PathVariable final String paymentDetailsId,
			final HttpServletRequest request) throws RequestParameterException //NOSONAR
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("updatePaymentInfo: id = " + sanitize(paymentDetailsId));
		}

		final CCPaymentInfoData paymentInfoData = getPaymentInfo(paymentDetailsId);

		final boolean isAlreadyDefaultPaymentInfo = paymentInfoData.isDefaultPaymentInfo();
		final Collection<PaymentInfoOption> options = new ArrayList<PaymentInfoOption>();
		options.add(PaymentInfoOption.BASIC);
		options.add(PaymentInfoOption.BILLING_ADDRESS);

		getHttpRequestPaymentInfoPopulator().populate(request, paymentInfoData, options);
		validate(paymentInfoData, "paymentDetails", getCcPaymentInfoValidator());

		getUserFacade().updateCCPaymentInfo(paymentInfoData);
		if (paymentInfoData.isSaved() && !isAlreadyDefaultPaymentInfo && paymentInfoData.isDefaultPaymentInfo())
		{
			getUserFacade().setDefaultPaymentInfo(paymentInfoData);
		}
	}


	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}/paymentdetails/{paymentDetailsId}", method = RequestMethod.PATCH, consumes =
	{ MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ApiOperation(value = "Updates existing customer's credit card payment details.", notes = "Updates existing customer's credit card payment details based on its ID. Only attributes given in request will be changed.")
	@ApiBaseSiteIdAndUserIdParam
	@ResponseStatus(HttpStatus.OK)
	public void updatePaymentInfo(
			@ApiParam(value = "Payment details identifier.", required = true) @PathVariable final String paymentDetailsId,
			@ApiParam(value = "Payment details object", required = true) @RequestBody final PaymentDetailsWsDTO paymentDetails)
			throws RequestParameterException //NOSONAR
	{
		final CCPaymentInfoData paymentInfoData = getPaymentInfo(paymentDetailsId);
		final boolean isAlreadyDefaultPaymentInfo = paymentInfoData.isDefaultPaymentInfo();

		getDataMapper()
				.map(paymentDetails,
						paymentInfoData,
						"accountHolderName,cardNumber,cardType,issueNumber,startMonth,expiryMonth,startYear,expiryYear,subscriptionId,defaultPaymentInfo,saved,billingAddress(firstName,lastName,titleCode,line1,line2,town,postalCode,region(isocode),country(isocode),defaultAddress)",
						false);
		validate(paymentInfoData, "paymentDetails", getCcPaymentInfoValidator());

		getUserFacade().updateCCPaymentInfo(paymentInfoData);
		if (paymentInfoData.isSaved() && !isAlreadyDefaultPaymentInfo && paymentInfoData.isDefaultPaymentInfo())
		{
			getUserFacade().setDefaultPaymentInfo(paymentInfoData);
		}

	}


	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}/paymentdetails/{paymentDetailsId}", method = RequestMethod.PUT)
	@ApiOperation(hidden = true, value = "Updates existing customer's credit card payment details. ", notes = "Updates existing customer's credit card payment info based on the payment info ID. Attributes not given in request will be defined again (set to null or default).")
	@ApiImplicitParams(
	{
			@ApiImplicitParam(name = "baseSiteId", value = "Base site identifier", required = true, dataType = "String", paramType = "path"),
			@ApiImplicitParam(name = "userId", value = "User identifier", required = true, dataType = "String", paramType = "path"),
			@ApiImplicitParam(name = "accountHolderName", value = "Name on card.", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "cardType", value = "Card type. Call GET /{baseSiteId}/cardtypes beforehand to see what card types are supported.", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "expiryMonth", value = "Month of expiry date.", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "expiryYear", value = "Year of expiry date.", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "issueNumber", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "startMonth", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "startYear", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "subscriptionId", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "saved", value = "Parameter defines if the payment details should be saved for the customer and than could be reused", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "defaultPaymentInfo", value = "Parameter defines if the payment details should be used as default for customer.", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "billingAddress.firstName", value = "Customer's first name.", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "billingAddress.lastName", value = "Customer's last name.", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "billingAddress.titleCode", value = "Customer's title code. For a list of codes, see /{baseSiteId}/titles resource", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "billingAddress.country.isocode", value = "Country isocode. This parameter havs influence on how rest of address parameters are validated (e.g. if parameters are required: line1,line2,town,postalCode,region.isocode)", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "billingAddress.line1", value = "If this parameter is required depends on country (usually it is required).", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "billingAddress.line2", value = "Second part of address. If this parameter is required depends on country (usually it is not required)", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "bbillingAddress.town", value = "If this parameter is required depends on country (usually it is required)", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "billingAddress.postalCode", value = "Postal code. If this parameter is required depends on country (usually it is required)", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "billingAddressregion.isocode", value = "Isocode for region. If this parameter is required depends on country.", required = false, dataType = "String", paramType = "query") })
	@ResponseStatus(HttpStatus.OK)
	public void putPaymentInfo(@PathVariable final String paymentDetailsId, final HttpServletRequest request)
			throws RequestParameterException //NOSONAR
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("updatePaymentInfo: id = " + sanitize(paymentDetailsId));
		}

		final CCPaymentInfoData paymentInfoData = getPaymentInfo(paymentDetailsId);

		final boolean isAlreadyDefaultPaymentInfo = paymentInfoData.isDefaultPaymentInfo();
		paymentInfoData.setAccountHolderName(null);
		paymentInfoData.setCardNumber(null);
		paymentInfoData.setCardType(null);
		paymentInfoData.setExpiryMonth(null);
		paymentInfoData.setExpiryYear(null);
		paymentInfoData.setDefaultPaymentInfo(false);
		paymentInfoData.setSaved(false);

		paymentInfoData.setIssueNumber(null);
		paymentInfoData.setStartMonth(null);
		paymentInfoData.setStartYear(null);
		paymentInfoData.setSubscriptionId(null);

		final AddressData address = paymentInfoData.getBillingAddress();
		address.setFirstName(null);
		address.setLastName(null);
		address.setCountry(null);
		address.setLine1(null);
		address.setLine2(null);
		address.setPostalCode(null);
		address.setRegion(null);
		address.setTitle(null);
		address.setTown(null);

		final Collection<PaymentInfoOption> options = new ArrayList<PaymentInfoOption>();
		options.add(PaymentInfoOption.BASIC);
		options.add(PaymentInfoOption.BILLING_ADDRESS);

		getHttpRequestPaymentInfoPopulator().populate(request, paymentInfoData, options);
		validate(paymentInfoData, "paymentDetails", getCcPaymentInfoValidator());

		getUserFacade().updateCCPaymentInfo(paymentInfoData);
		if (paymentInfoData.isSaved() && !isAlreadyDefaultPaymentInfo && paymentInfoData.isDefaultPaymentInfo())
		{
			getUserFacade().setDefaultPaymentInfo(paymentInfoData);
		}
	}


	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}/paymentdetails/{paymentDetailsId}", method = RequestMethod.PUT, consumes =
	{ MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ApiOperation(value = "Updates existing customer's credit card payment info.", notes = "Updates existing customer's credit card payment info based on the payment info ID. Attributes not given in request will be defined again (set to null or default).")
	@ApiBaseSiteIdAndUserIdParam
	@ResponseStatus(HttpStatus.OK)
	public void putPaymentInfo(
			@ApiParam(value = "Payment details identifier.", required = true) @PathVariable final String paymentDetailsId,
			@ApiParam(value = "Payment details object.", required = true) @RequestBody final PaymentDetailsWsDTO paymentDetails)
			throws RequestParameterException //NOSONAR
	{
		final CCPaymentInfoData paymentInfoData = getPaymentInfo(paymentDetailsId);
		final boolean isAlreadyDefaultPaymentInfo = paymentInfoData.isDefaultPaymentInfo();

		validate(paymentDetails, "paymentDetails", getPaymentDetailsDTOValidator());
		getDataMapper()
				.map(paymentDetails,
						paymentInfoData,
						"accountHolderName,cardNumber,cardType,issueNumber,startMonth,expiryMonth,startYear,expiryYear,subscriptionId,defaultPaymentInfo,saved,billingAddress(firstName,lastName,titleCode,line1,line2,town,postalCode,region(isocode),country(isocode),defaultAddress)",
						true);

		getUserFacade().updateCCPaymentInfo(paymentInfoData);
		if (paymentInfoData.isSaved() && !isAlreadyDefaultPaymentInfo && paymentInfoData.isDefaultPaymentInfo())
		{
			getUserFacade().setDefaultPaymentInfo(paymentInfoData);
		}
	}


	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}/customergroups", method = RequestMethod.GET)
	@ApiOperation(value = "Get all customer groups of a customer.", notes = "Returns all customer groups of a customer.")
	@ApiBaseSiteIdAndUserIdParam
	@ResponseBody
	public UserGroupListWsDTO getAllCustomerGroupsForCustomer(
			@ApiParam(value = "User identifier.", required = true) @PathVariable final String userId,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		final UserGroupDataList userGroupDataList = new UserGroupDataList();
		userGroupDataList.setUserGroups(customerGroupFacade.getCustomerGroupsForUser(userId));
		return getDataMapper().map(userGroupDataList, UserGroupListWsDTO.class, fields);
	}

	protected Set<OrderStatus> extractOrderStatuses(final String statuses)
	{
		final String[] statusesStrings = statuses.split(YcommercewebservicesConstants.OPTIONS_SEPARATOR);

		final Set<OrderStatus> statusesEnum = new HashSet<>();
		for (final String status : statusesStrings)
		{
			statusesEnum.add(OrderStatus.valueOf(status));
		}
		return statusesEnum;
	}

	protected OrderHistoriesData createOrderHistoriesData(final SearchPageData<OrderHistoryData> result)
	{
		final OrderHistoriesData orderHistoriesData = new OrderHistoriesData();

		orderHistoriesData.setOrders(result.getResults());
		orderHistoriesData.setSorts(result.getSorts());
		orderHistoriesData.setPagination(result.getPagination());

		return orderHistoriesData;
	}

	@RequestMapping(value = "/{userId}/accountActivate", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	@ResponseStatus(value = HttpStatus.OK)
	public void accountActivate(@ApiParam(value = "User id", required = true) @PathVariable final String userId)
			throws DuplicateUidException
	{
		//customerActivatefacade.activateCustomer(userId);
	}

	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}/CRNNumber/{CRNnumber}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get customer profile", notes = "Returns customer profile.")
	@ApiBaseSiteIdAndUserIdParam
	public UserWsDTO getUserForCRNNumberAndUserID(
			@ApiParam("User identifier.") @PathVariable final String userId,
			@ApiParam("User identifier.") @PathVariable final String CRNnumber,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL, LOGINCHECKOUT") @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		LOG.debug("Start of getUserForCRNNumberAndUserID() method");
		final CustomerData customerData = customerFacade.getCurrentCustomer();
		if (customerData != null && !customerData.getUid().toString().equals(CRNnumber))
		{
			throw new RequestParameterException("CRN number doesn't match with the existing user id details",
					RequestParameterException.UNKNOWN_IDENTIFIER, "CRNnumber");

		}
			LOG.debug("End of getUserForCRNNumberAndUserID() method");
		return getDataMapper().map(customerData, UserWsDTO.class, fields);
	}

	@RequestMapping(value = "/{userId}/media/displayImages", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Object> displayApprovalImages(
			@ApiParam(value = "User id", required = true) @PathVariable final String userId, final HttpServletRequest request)
			throws WoolliesMediaImagesException
	{
		MediaData media = new MediaData();
		final ErrorWsDTO errorDetails = new ErrorWsDTO();
		if (null != userId)
		{
			media = woolliesApprovedImagesMediaFacade.getApprovedImages(userId);

			if (media.getApprovedImages() == null)
			{
				errorDetails.setErrorCode("40001");
				errorDetails.setErrorDescription("No Approved Images Found");
				errorDetails.setErrorMessage("Media Error");
				final List<ErrorWsDTO> errorDetailList = new ArrayList<>();
				errorDetailList.add(errorDetails);
				return new ResponseEntity<Object>(errorDetailList, HttpStatus.INTERNAL_SERVER_ERROR);
			}

		}
		return new ResponseEntity<Object>(media, HttpStatus.OK);

	}

	@RequestMapping(value = "/{userId}/removeAccount", method = RequestMethod.PUT, consumes =
	{ MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	@ResponseStatus(value = HttpStatus.OK)
	@SuppressWarnings("squid:S1160")
	public ResponseDTO removeAccount(@ApiParam(value = "admion uid", required = true) @PathVariable final String userId,
			@ApiParam(value = "User's object.", required = true) @RequestBody final RemoveCustomerDTO user)
			throws DuplicateUidException, WooliesB2BUserException
	{
		ResponseDTO responseDTO = null;
		try
		{
			responseDTO = wooliesB2BUnitFacade.removeAccount(user);
		}
		catch (final WooliesB2BUserException ex)
		{
			if (ex.getMessage().equals("70001"))
			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ISNOTADMIN,
						WooliesgcWebServicesConstants.ERRMSG_ISNOTADMIN, WooliesgcWebServicesConstants.ERRRSN_ISNOTADMIN);
			}
			else if (ex.getMessage().equals("70002"))
			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ISNOTATIVE,
						WooliesgcWebServicesConstants.ERRMSG_ISNOTATIVE, WooliesgcWebServicesConstants.ERRRSN_ISNOTATIVE);
			}
			else if (ex.getMessage().equals("70003"))
			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ISNOTEXIST,
						WooliesgcWebServicesConstants.ERRMSG_ISNOTEXIST, WooliesgcWebServicesConstants.ERRRSN_ISNOTEXIST);
			}
			else if (ex.getMessage().equals("70012"))
			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_REMOVEACCOUNT,
						WooliesgcWebServicesConstants.ERRMSG_REMOVEACCOUNT, WooliesgcWebServicesConstants.ERRRSN_REMOVEACCOUNT);
			}
		}
		catch (final IndexOutOfBoundsException ex)
		{
			throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ISNOTEXIST,
					WooliesgcWebServicesConstants.ERRMSG_ISNOTEXIST, WooliesgcWebServicesConstants.ERRRSN_ISNOTEXIST);
		}

		return responseDTO;
	}

	@RequestMapping(value = "/{adminUid}/{userId}/orderLimit", method = RequestMethod.PATCH, consumes =
	{ MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Updates customer profile", notes = "Updates customer profile. Only attributes provided in the request body will be changed.")
	@ApiBaseSiteIdAndUserIdParam
	public void updateOrderLimit(@ApiParam("User identifier.") @PathVariable final String userId,
			@ApiParam(value = "admion uid", required = true) @PathVariable final String adminUid,
			@ApiParam(value = "orderLimit.", required = true) @RequestBody final PriceWsDTO orderLimit)
			throws DuplicateUidException, RequestParameterException, WooliesB2BUserException
	{
		if (orderLimit != null)
		{
			if (orderLimit.getCurrencyIso() == null || orderLimit.getValue() == null)
			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_CURRENCYCHECK,
						WooliesgcWebServicesConstants.ERRMSG_CURRENCYCHECK, WooliesgcWebServicesConstants.ERRRSN_CURRENCYCHECK);
			}
		}
		try
		{
			wooliesB2BUnitFacade.updateOrderLimit(userId, adminUid, orderLimit);
		}
		catch (final WooliesB2BUserException ex)
		{
			if (ex.getMessage().equals("70001"))
			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ISNOTADMIN,
						WooliesgcWebServicesConstants.ERRMSG_ISNOTADMIN, WooliesgcWebServicesConstants.ERRRSN_ISNOTADMIN);
			}
			else if (ex.getMessage().equals("70002"))
			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ISNOTATIVE,
						WooliesgcWebServicesConstants.ERRMSG_ISNOTATIVE, WooliesgcWebServicesConstants.ERRRSN_ISNOTATIVE);
			}
			else if (ex.getMessage().equals("70003"))
			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ISNOTEXIST,
						WooliesgcWebServicesConstants.ERRMSG_ISNOTEXIST, WooliesgcWebServicesConstants.ERRRSN_ISNOTEXIST);
			}
		}
		catch (final IndexOutOfBoundsException ex)
		{
			throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_ISNOTEXIST,
					WooliesgcWebServicesConstants.ERRMSG_ISNOTEXIST, WooliesgcWebServicesConstants.ERRRSN_ISNOTEXIST);
		}
	}


	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}/getAllUsers", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get customer profile", notes = "Returns customer profile.")
	@ApiBaseSiteIdAndUserIdParam
	public CustomerDataListDTO getb2bUser(
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
			@ApiParam(value = "User id", required = true) @PathVariable final String userId) throws DuplicateUidException,
			WooliesB2BUserException
	{
		final List<CustomerData> customerList = wooliesB2BUnitFacade.getB2BAccountUsers(userId);
		final CustomerDataList customerDataList = new CustomerDataList();
		customerDataList.setUsers(customerList);
		if (CollectionUtils.isEmpty(customerList))
		{
			return getDataMapper().map(customerDataList, CustomerDataListDTO.class, fields);
		}
		return getDataMapper().map(customerDataList, CustomerDataListDTO.class, fields);
	}

	@RequestMapping(value = "/{userId}/getusers", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get customer profile", notes = "Returns customer profile.")
	@ApiBaseSiteIdAndUserIdParam
	public CustomerDetailDTO getUsers(
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		final CustomerData userProfile = profileSummeryFacade.getuser();
		return getDataMapper().map(userProfile, CustomerDetailDTO.class, fields);

	}

	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}/CRNnumber/{CRNnumber}/update", method = RequestMethod.PUT, consumes =
	{ MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Updates customer profile", notes = "Updates customer profile. Only attributes provided in the request body will be changed.")
	@ApiBaseSiteIdAndUserIdParam
	public void updateUsers(@ApiParam("User identifier.") @PathVariable final String userId,
			@ApiParam(value = "crnNumber number from Gigiya.", required = true) @PathVariable final String CRNnumber,
			@ApiParam(value = "User's object.", required = true) @RequestBody final UserWsDTO user) throws DuplicateUidException,
			RequestParameterException
	{
		final UserModel userModel = wooliesDefaultCustomerFacade.getUser();

		if (((CustomerModel) userModel).getCustomerType() == UserDataType.B2C)
		{
			b2cUpdateValidate(user, "user", b2cprofileupdateValidator);
			final CustomerData customer = customerFacade.getCurrentCustomer();
			if (LOG.isDebugEnabled())
			{
				LOG.debug("updateUser: userId=" + customer.getUid());
			}
			if (null != customer.getUid() && CRNnumber.equals(customer.getUid().toString()))
			{

				getDataMapper().map(user, customer, "firstName,lastName,phone,cardNo,email,optInForMarketing", true);

				try
				{
					customerFacade.updateFullProfile(customer);
				}
				catch (final DuplicateUidException ex)
				{
					if (ex.getMessage().equals("80007"))
					{
						throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_PHONENOTVALID,
								WooliesgcWebServicesConstants.ERRMSG_PHONENOTVALID, WooliesgcWebServicesConstants.ERRRSN_PHONENOTVALID);
					}
				}
			}
			else
			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_CRNNOTMACHED,
						WooliesgcWebServicesConstants.ERRMSG_NNNOTMACHED, WooliesgcWebServicesConstants.ERRRSN_NNNOTMACHED);

			}
		}
		else if (((CustomerModel) userModel).getCustomerType() == UserDataType.B2B)
		{
					updateValidate(user, "user", profileupdateValidator);
			final CustomerData customer = customerFacade.getCurrentCustomer();
			if (LOG.isDebugEnabled())
			{
				LOG.debug("updateUser: userId=" + customer.getUid());
						}
			if (null != customer.getUid() && CRNnumber.equals(customer.getUid().toString()))
			{

				getDataMapper().map(user, customer, "firstName,lastName,phone,email,optInForMarketing", true);

				try
				{
					wooliesDefaultCustomerFacade.B2BupdateFullProfiles(customer);
				}
				catch (final DuplicateUidException ex)
				{
					if (ex.getMessage().equals("80007"))
					{
						throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_PHONENOTVALID,
								WooliesgcWebServicesConstants.ERRMSG_PHONENOTVALID, WooliesgcWebServicesConstants.ERRRSN_PHONENOTVALID);
					}
				}
			}
			else
			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_CRNNOTMACHED,
						WooliesgcWebServicesConstants.ERRMSG_NNNOTMACHED, WooliesgcWebServicesConstants.ERRRSN_NNNOTMACHED);
			}
		}
		else if (((CustomerModel) userModel).getCustomerType() == UserDataType.MEM)
		{
			updatesValidate(user, "user", profileupdateValidator);
			final CustomerData customers = customerFacade.getCurrentCustomer();
			if (LOG.isDebugEnabled())
			{
				LOG.debug("updateUser: userId=" + customers.getUid());
						}
			if (null != customers.getUid() && CRNnumber.equals(customers.getUid().toString()))
			{

				getDataMapper().map(user, customers, "firstName,lastName,email,phone,optInForMarketing", true);

				try
				{
					wooliesDefaultCustomerFacade.memberUpdateFullProfiles(customers);
				}
				catch (final DuplicateUidException ex)
				{
					if (ex.getMessage().equals("80007"))
					{
						throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_PHONENOTVALID,
								WooliesgcWebServicesConstants.ERRMSG_PHONENOTVALID, WooliesgcWebServicesConstants.ERRRSN_PHONENOTVALID);
					}
				}
			}
			else
			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_CRNNOTMACHED,
						WooliesgcWebServicesConstants.ERRMSG_NNNOTMACHED, WooliesgcWebServicesConstants.ERRRSN_NNNOTMACHED);
			}
		}
	}

	private void updateValidate(final Object object, final String objectName, final Validator validator)
	{
		final Errors errors = new BeanPropertyBindingResult(object, objectName);
		validator.validate(object, errors);
		if (errors.hasErrors())
		{
			final List<FieldError> fieldErrors = errors.getFieldErrors();
			for (final FieldError fieldError : fieldErrors)
			{
				if (fieldError.getField().equalsIgnoreCase(WooliesgcFacadesConstants.FIRSTNAME)
						|| fieldError.getField().equalsIgnoreCase(WooliesgcFacadesConstants.LASTNAME)
						|| fieldError.getField().equalsIgnoreCase(WooliesgcFacadesConstants.PHONE)
						|| fieldError.getField().equalsIgnoreCase(WooliesgcFacadesConstants.EMAIL)
						|| fieldError.getField().equalsIgnoreCase(WooliesgcFacadesConstants.OPTINFORMARKETING))

				{
					throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_FIELDMISSING, fieldError.getField()
							+ WooliesgcWebServicesConstants.ERRMSG_FIELDMISSING, fieldError.getField()
							+ WooliesgcWebServicesConstants.ERRRSN_FIELDMISSING);
				}
			}
		}
	}



	private void updatesValidate(final Object object, final String objectName, final Validator validator)
	{
		final Errors errors = new BeanPropertyBindingResult(object, objectName);
		validator.validate(object, errors);
		if (errors.hasErrors())
		{
			final List<FieldError> fieldErrors = errors.getFieldErrors();
			for (final FieldError fieldError : fieldErrors)
			{
				if (fieldError.getField().equalsIgnoreCase(WooliesgcFacadesConstants.FIRSTNAME)
						|| fieldError.getField().equalsIgnoreCase(WooliesgcFacadesConstants.LASTNAME)
						|| fieldError.getField().equalsIgnoreCase(WooliesgcFacadesConstants.PHONE)
						|| fieldError.getField().equalsIgnoreCase("emailaddress")
						|| fieldError.getField().equalsIgnoreCase(WooliesgcFacadesConstants.OPTINFORMARKETING))

				{
					throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_FIELDMISSING, fieldError.getField()
							+ WooliesgcWebServicesConstants.ERRMSG_FIELDMISSING, fieldError.getField()
							+ WooliesgcWebServicesConstants.ERRRSN_FIELDMISSING);
				}
			}
		}
	}

	private void b2cUpdateValidate(final Object object, final String objectName, final Validator validator)
	{
		final Errors errors = new BeanPropertyBindingResult(object, objectName);
		validator.validate(object, errors);
		if (errors.hasErrors())
		{
			final List<FieldError> fieldErrors = errors.getFieldErrors();
			for (final FieldError fieldError : fieldErrors)
			{
				if (fieldError.getField().equalsIgnoreCase("firstName") || fieldError.getField().equalsIgnoreCase("lastName")
						|| fieldError.getField().equalsIgnoreCase("phone") || fieldError.getField().equalsIgnoreCase("uid")
						|| fieldError.getField().equalsIgnoreCase("optInForMarketing"))

				{
					throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_FIELDMISSING, fieldError.getField()
							+ WooliesgcWebServicesConstants.ERRMSG_FIELDMISSING, fieldError.getField()
							+ WooliesgcWebServicesConstants.ERRRSN_FIELDMISSING);
				}
			}
		}
	}

	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}/customerImages/{status}", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "Get customer Images", notes = "Returns customer images.")
	@ApiBaseSiteIdAndUserIdParam
	public PersonalisationMediaDataDataListDTO getCustomerImages(
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
			@ApiParam(value = "User id", required = true) @PathVariable final String userId,
			@ApiParam(value = "Status", required = true) @PathVariable final String status,
			@ApiParam(value = "PageableData object.", required = true) @RequestBody final WooliesPaginationWsDTO pageData)
			throws DuplicateUidException, WooliesB2BUserException
	{
		final String statuses = status.replaceAll("\\s", "");
		final PersonalisationMediaDataList mediaDataList = new PersonalisationMediaDataList();

		if (statuses.equalsIgnoreCase(ImageApprovalStatus.APPROVED) || statuses.equalsIgnoreCase(ImageApprovalStatus.PENDING)
				|| statuses.equalsIgnoreCase(WooliesgcFacadesConstants.FULL_STATUS))
		{

			if (pageData.getFirstImageIds() != null && CollectionUtils.isNotEmpty(pageData.getFirstImageIds())
					&& pageData.getFirstImageIds().size() > pageData.getPageSize())
			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_IMAGEIDEXCEEDED,
						WooliesgcWebServicesConstants.ERRMSG_IMAGEIDEXCEEDED, WooliesgcWebServicesConstants.ERRRSN_IMAGEIDEXCEEDED);
			}
			final PersonalisationMediaDataList imageDataList = wooliesMediaFacade.getCustomerImages(userId, statuses,
					pageData.getStartIndex(), pageData.getPageSize(), pageData.getFirstImageIds());

			if (imageDataList != null)
			{
				mediaDataList.setImageList(imageDataList.getImageList());
				mediaDataList.setPageData(imageDataList.getPageData());
				return getDataMapper().map(imageDataList, PersonalisationMediaDataDataListDTO.class, fields);
			}
			else
			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_IMAGEIDNOTEXIST,
						WooliesgcWebServicesConstants.ERRMSG_IMAGEIDNOTEXIST, WooliesgcWebServicesConstants.ERRRSN_IMAGEIDNOTEXIST);
			}
		}
		else
		{
			throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_NOSTATUS,
					WooliesgcWebServicesConstants.ERRMSG_NOSTATUS, WooliesgcWebServicesConstants.ERRRSN_NOSTATUS);
		}
	}

	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}/deleteImages/{imageId}", method = RequestMethod.DELETE)
	@ResponseBody
	@ApiOperation(value = "Delete customer image", notes = "Delete customer image.")
	@ApiBaseSiteIdAndUserIdParam
	public void getRemoveCustomerImages(
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
			@ApiParam(value = "User id", required = true) @PathVariable final String userId,
			@ApiParam(value = "imageId", required = true) @PathVariable final String imageId) throws DuplicateUidException,
			WooliesB2BUserException
	{
		try
		{
			wooliesMediaFacade.deleteImages(userId, imageId);
		}
		catch (final WooliesB2BUserException ex)
		{
			if (ex.getMessage().equals(WooliesgcWebServicesConstants.ERRCODE_IMAGENOTEXIST))
			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_IMAGENOTEXIST,
						WooliesgcWebServicesConstants.ERRMSG_IMAGENOTEXIST, WooliesgcWebServicesConstants.ERRRSN_IMAGENOTEXIST);
			}
		}

	}

	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{userId}/CRNnumber/{CRNnumber}/loginupdate", method = RequestMethod.PATCH, consumes =
	{ MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Updates customer profile", notes = "Updates customer profile. Only attributes provided in the request body will be changed.")
	@ApiBaseSiteIdAndUserIdParam
	public void disableLoginUsers(@ApiParam("User identifier.") @PathVariable final String userId,
			@ApiParam(value = "crnNumber number from Gigiya.", required = true) @PathVariable final String CRNnumber)

	throws RequestParameterException, WooliesFacadeLayerException, DuplicateUidException
	{

		final UserModel userModel = wooliesDefaultCustomerFacades.getUser();
		if (((CustomerModel) userModel).getCustomerType() == UserDataType.B2C)
		{
			final CustomerData customer = customerFacade.getCurrentCustomer();
			if (LOG.isDebugEnabled())
			{
				LOG.debug("updateUser: userId=" + customer.getUid());
						}
			if (null != customer.getUid() && CRNnumber.equals(customer.getUid().toString()))

			{
				wooliesDefaultCustomerFacades.findAllProductsOlderThanSpecifiedDays(userId);
			}

			else
			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_CRNNOTMACHED,
						WooliesgcWebServicesConstants.ERRMSG_NNNOTMACHED, WooliesgcWebServicesConstants.ERRRSN_NNNOTMACHED);
			}
		}
		if (((CustomerModel) userModel).getCustomerType() == UserDataType.B2B)
		{
			final CustomerData customer = customerFacade.getCurrentCustomer();
			if (LOG.isDebugEnabled())
			{
				LOG.debug("updateUser: userId=" + customer.getUid());
				LOG.debug("updateUser: crnNumber=" + customer.getCRNnumber());
			}
			if (null != customer.getUid() && CRNnumber.equals(customer.getUid().toString()))

			{

				wooliesDefaultCustomerFacades.findAllProductsB2BOlderThanSpecifiedDays(userId);
			}

			else
			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_CRNNOTMACHED,
						WooliesgcWebServicesConstants.ERRMSG_NNNOTMACHED, WooliesgcWebServicesConstants.ERRRSN_NNNOTMACHED);
			}
		}
	}
}
