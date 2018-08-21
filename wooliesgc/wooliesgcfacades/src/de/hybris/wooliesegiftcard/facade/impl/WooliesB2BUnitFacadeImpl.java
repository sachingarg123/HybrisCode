/**
 *
 */
package de.hybris.wooliesegiftcard.facade.impl;


import de.hybris.platform.b2b.model.B2BOrderThresholdPermissionModel;
import de.hybris.platform.b2b.model.B2BPermissionModel;
import de.hybris.platform.b2bcommercefacades.company.data.B2BUnitData;
import de.hybris.platform.commercefacades.customer.impl.DefaultCustomerFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.commercewebservicescommons.dto.product.PriceWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.user.UserSignUpWsDTO;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.wooliesegiftcard.core.enums.UserDataType;
import de.hybris.wooliesegiftcard.core.impl.WooliesB2BDefaultCustomerAccountService;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BCustomerModel;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BUnitModel;
import de.hybris.wooliesegiftcard.exception.WooliesB2BUserException;
import de.hybris.wooliesegiftcard.facade.WooliesB2BUnitFacade;
import de.hybris.wooliesegiftcard.facades.RemoveCustomerDTO;
import de.hybris.wooliesegiftcard.facades.constants.WooliesgcFacadesConstants;
import de.hybris.wooliesegiftcard.facades.dto.ResponseDTO;
import de.hybris.wooliesegiftcard.service.impl.WooliesB2BUnitServiceImpl;
import de.hybris.wooliesegiftcard.service.impl.WooliesCountryServiceImpl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author 648156 This class is used to maintain the b2b user account details
 **
 */
public class WooliesB2BUnitFacadeImpl extends DefaultCustomerFacade implements WooliesB2BUnitFacade
{
	private UserService userService;
	private Converter<AddressData, AddressModel> addressReverseConverter;
	private WooliesB2BDefaultCustomerAccountService wooliesB2BCustomerAccountService;
	private WooliesCountryServiceImpl wooliesCountryServiceImpl;
	private static final Logger LOG = Logger.getLogger(WooliesB2BUnitFacadeImpl.class);
	private WooliesB2BUnitServiceImpl wooliesB2BUnitServiceImpl;
	private Converter<UserModel, CustomerData> customerConverter;
	@Autowired
	private FlexibleSearchService flexibleSearchService;

	private final boolean activateFlag = false;
	private Boolean activeBoolFlag = new Boolean(activateFlag);
	private Boolean flagcheck = new Boolean(activateFlag);

	final Pattern pattern = Pattern.compile("[a-zA-Z0-9\\s]*");
	final String EMAIL_REGEX = "^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$";
	final Pattern emailPattern = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);
	final String PHONE_REGEX = "^\\+?[0-9 ()-]{10,20}$";
	final Pattern phone_pattern = Pattern.compile(PHONE_REGEX);
	private static final String QUERY = "select {o.pk} from {CorporateB2BCustomer AS o} where {o.uid} = ?email ";
	private static final String EMAIL = "email";
	private static final String DELETE = "deleted_";

	/**
	 * @return the userService
	 */
	@Override
	public UserService getUserService()
	{
		return userService;
	}

	/**
	 * @param userService
	 *           the userService to set
	 */
	@Override
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
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
	 * @return the wooliesB2BCustomerAccountService
	 */
	public WooliesB2BDefaultCustomerAccountService getWooliesB2BCustomerAccountService()
	{
		return wooliesB2BCustomerAccountService;
	}

	/**
	 * @param wooliesB2BCustomerAccountService
	 *           the wooliesB2BCustomerAccountService to set
	 */
	public void setWooliesB2BCustomerAccountService(final WooliesB2BDefaultCustomerAccountService wooliesB2BCustomerAccountService)
	{
		this.wooliesB2BCustomerAccountService = wooliesB2BCustomerAccountService;
	}

	/**
	 * This method is used to create b2b unit
	 *
	 * @param b2bUnitData
	 * @return the corporateB2BUnitModel
	 * @throws DuplicateUidException
	 * @throws WooliesB2BUserException
	 */
	@Override
	public CorporateB2BUnitModel createB2BUnit(final B2BUnitData b2bUnitData) throws DuplicateUidException,
			WooliesB2BUserException
	{
		UserModel userModel = null;
		final CorporateB2BUnitModel corporateB2BUnitModel = getModelService().create(CorporateB2BUnitModel.class);
		if (userService != null)
		{
			userModel = userService.getAdminUser();
			if (userModel != null)
			{
				userService.setCurrentUser(userModel);
			}
		}

		if (null != b2bUnitData.getUid())
		{
			final Matcher fromEmailMatcher = emailPattern.matcher(b2bUnitData.getUid());
			if (!fromEmailMatcher.matches())
			{
				throw new WooliesB2BUserException(WooliesgcFacadesConstants.ERRCODE_EMAILFORMAT);

			}

		}
		corporateB2BUnitModel.setCorporateABN(b2bUnitData.getCorporateABN());
		corporateB2BUnitModel.setName(b2bUnitData.getCorporateName());
		corporateB2BUnitModel.setLocName(b2bUnitData.getCorporateName());
		corporateB2BUnitModel.setCompanyType(b2bUnitData.getCompanyType());
		corporateB2BUnitModel.setCharityOrganization(b2bUnitData.getCharityOrganization());
		corporateB2BUnitModel.setTradingName(b2bUnitData.getTradingName());
		try
		{
			getModelService().save(corporateB2BUnitModel);
		}
		catch (final ModelSavingException ex)
		{
			LOG.info(ex);
			throw new ModelSavingException("70011");
		}

		return corporateB2BUnitModel;
	}

	/**
	 * This method is used to register the b2b customer details for the given unit model and customer data
	 *
	 * @param customerData
	 * @param corporateB2BUnitModel
	 * @throws DuplicateUidException
	 */
	@Override
	public void register(final CustomerData customerData, final CorporateB2BUnitModel corporateB2BUnitModel)
			throws DuplicateUidException
	{
		final CorporateB2BCustomerModel newCustomer = getModelService().create(CorporateB2BCustomerModel.class);
		if (StringUtils.isNotBlank(customerData.getFirstName()) && StringUtils.isNotBlank(customerData.getLastName()))
		{
			newCustomer.setName(getCustomerNameStrategy().getName(customerData.getFirstName(), customerData.getLastName()));
			newCustomer.setFirstName(customerData.getFirstName());
			newCustomer.setLastName(customerData.getLastName());
		}
		if (customerData.getDob() != null)
		{
			final SimpleDateFormat formatter = new SimpleDateFormat(WooliesgcFacadesConstants.DOB);
			try
			{
				newCustomer.setDob(formatter.parse(customerData.getDob()));
			}
			catch (final ParseException e)
			{
				LOG.error("Exception occurred", e);
			}
		}
		newCustomer.setDefaultB2BUnit(corporateB2BUnitModel);
		newCustomer.setCustomerType(UserDataType.B2B);
		if (null != customerData.getPhone())
		{
			final Matcher matcher = phone_pattern.matcher(customerData.getPhone());

			if (!matcher.matches())
			{
				throw new DuplicateUidException("80007");
			}
		}
		newCustomer.setPhone(customerData.getPhone());
		newCustomer.setCardNo(customerData.getCardNo());
		newCustomer.setOptInForMarketing(customerData.getOptInForMarketing());
		newCustomer.setPolicyAgreement(customerData.getPolicyAgreement());
		newCustomer.setUid(customerData.getCRNnumber());
		newCustomer.setOriginalUid(customerData.getCRNnumber());
		newCustomer.setEmail(customerData.getEmail());
		newCustomer.setUserEmail(customerData.getEmail());
		if (customerData.getIsCorpAdminUser().booleanValue())
		{
			final Set<PrincipalGroupModel> groups = new HashSet<>(newCustomer.getGroups());
			groups.add(userService.getUserGroupForUID(WooliesgcFacadesConstants.B2BADMINGROUP));
			newCustomer.setGroups(groups);
		}
		if (corporateB2BUnitModel.getCharityOrganization().booleanValue())
		{
			final Set<PrincipalGroupModel> groups = new HashSet<>(newCustomer.getGroups());
			groups.add(userService.getUserGroupForUID("charity_user_group"));
			newCustomer.setGroups(groups);
		}
		newCustomer.setSessionLanguage(getCommonI18NService().getCurrentLanguage());
		newCustomer.setSessionCurrency(getCommonI18NService().getCurrentCurrency());
		wooliesCountryServiceImpl.getCountryForUser(customerData);
		final List<AddressModel> addresses = getAddressReverseConverter().convertAll(customerData.getAddresses());
		saveAddress(customerData, newCustomer, addresses);
		wooliesB2BCustomerAccountService.register(newCustomer);
	}

	/**
	 * This method is used to save address against customer model
	 *
	 * @param customerData
	 * @param newCustomer
	 * @param addresses
	 */
	private void saveAddress(final CustomerData customerData, final CorporateB2BCustomerModel newCustomer,
			final List<AddressModel> addresses)
	{
		if (addresses != null && !addresses.isEmpty())
		{
			for (final AddressModel addressModel : addresses)
			{
				addressModel.setFirstname(customerData.getFirstName());
				addressModel.setLastname(customerData.getLastName());
				addressModel.setPhone1(customerData.getPhone());
				addressModel.setOwner(newCustomer);
				newCustomer.setDefaultPaymentAddress(addressModel);
				wooliesB2BCustomerAccountService.saveAddressesEntry(newCustomer, addressModel);
			}
		}
	}

	/**
	 * @return the wooliesCountryServiceImpl
	 */
	public WooliesCountryServiceImpl getWooliesCountryServiceImpl()
	{
		return wooliesCountryServiceImpl;
	}

	/**
	 * @param wooliesCountryServiceImpl
	 *           the wooliesCountryServiceImpl to set
	 */
	public void setWooliesCountryServiceImpl(final WooliesCountryServiceImpl wooliesCountryServiceImpl)
	{
		this.wooliesCountryServiceImpl = wooliesCountryServiceImpl;
	}

	/**
	 * This method is used to manage b2b user
	 *
	 * @param user
	 * @throws WooliesB2BUserException
	 * @throws DuplicateUidException
	 */
	@Override
	public void b2bUserManagement(final UserSignUpWsDTO user) throws WooliesB2BUserException, DuplicateUidException
	{
		final List<CorporateB2BCustomerModel> corporateB2BCustomer1;
		CorporateB2BUnitModel corporateB2BUnitModel = null;

		final String adminUid = user.getAdminUid();
		corporateB2BCustomer1 = wooliesB2BUnitServiceImpl.getB2BAdmin(adminUid);
		for (final CorporateB2BCustomerModel corporateb2bCustomerModel : corporateB2BCustomer1)
		{
			corporateB2BUnitModel = (CorporateB2BUnitModel) corporateb2bCustomerModel.getDefaultB2BUnit();
		}
		final Set<PrincipalGroupModel> allGroups = corporateB2BCustomer1.get(0).getAllGroups();
		if (allGroups.contains(userService.getUserGroupForUID(WooliesgcFacadesConstants.B2BADMINGROUP)))
		{
			final CorporateB2BCustomerModel corporateB2BCustomer = getModelService().create(CorporateB2BCustomerModel.class);
			b2bUserManagementRegister(user, corporateB2BCustomer, corporateB2BUnitModel);
			corporateB2BCustomer.setCustomerType(UserDataType.B2B);
			if (user.getIsCorpAdminUser().booleanValue())
			{
				corporateB2BCustomer.setCustomerType(UserDataType.B2B);
				final Set<PrincipalGroupModel> groups = new HashSet<>(corporateB2BCustomer.getGroups());
				groups.add(userService.getUserGroupForUID(WooliesgcFacadesConstants.B2BADMINGROUP));
				corporateB2BCustomer.setGroups(groups);
			}
			else
			{
				corporateB2BCustomer.setCustomerType(UserDataType.B2B);
				if (user.getOrderLimit() == null)
				{
					throw new WooliesB2BUserException(WooliesgcFacadesConstants.ERRCODE_ORDERLIMIT);
				}
				else
				{
					final B2BOrderThresholdPermissionModel b2bOrderPermissionModel = getModelService().create(
							B2BOrderThresholdPermissionModel.class);
					b2bOrderPermissionModel.setThreshold(Double.valueOf(user.getOrderLimit().getValue().doubleValue()));
					b2bOrderPermissionModel.setUnit(null != corporateB2BUnitModel ? corporateB2BUnitModel : null);
					b2bOrderPermissionModel.setActive(Boolean.TRUE);
					b2bOrderPermissionModel.setCode(user.getCRNnumber());
					setCurrencyInOrderPermissionModel(user, b2bOrderPermissionModel);
					getModelService().save(b2bOrderPermissionModel);

				}
			}

			if (corporateB2BUnitModel != null && corporateB2BUnitModel.getCharityOrganization().booleanValue())
			{
				final Set<PrincipalGroupModel> groups = new HashSet<>(corporateB2BCustomer.getGroups());
				groups.add(userService.getUserGroupForUID("charity_user_group"));
				corporateB2BCustomer.setGroups(groups);
			}
			corporateB2BCustomer.setUid(user.getCRNnumber());
			corporateB2BCustomer.setOriginalUid(user.getCRNnumber());
			try
			{
				wooliesB2BCustomerAccountService.register(corporateB2BCustomer);
			}
			catch (final DuplicateUidException ex)
			{
				throw new DuplicateUidException("70011");
			}
		}
		else
		{
			throw new WooliesB2BUserException(WooliesgcFacadesConstants.ERRCODE_ADDADMINSANDBUYERS);
		}
	}

	/**
	 * This method is used to setCurrencyInOrderPermissionModel
	 *
	 * @param user
	 * @param b2bOrderPermissionModel
	 * @throws WooliesB2BUserException
	 */
	private void setCurrencyInOrderPermissionModel(final UserSignUpWsDTO user,
			final B2BOrderThresholdPermissionModel b2bOrderPermissionModel) throws WooliesB2BUserException
	{
		if (getCommonI18NService().getCurrency(user.getOrderLimit().getCurrencyIso()) != null)
		{
			b2bOrderPermissionModel.setCurrency(getCommonI18NService().getCurrency(user.getOrderLimit().getCurrencyIso()));
		}
		else
		{
			throw new WooliesB2BUserException(WooliesgcFacadesConstants.CURRENCYNOTNULL);
		}
	}

	/**
	 * This method is used to register b2b user
	 *
	 * @param user
	 * @param corporateB2BCustomer
	 * @param corporateB2BUnitModel
	 * @throws WooliesB2BUserException
	 */
	private void b2bUserManagementRegister(final UserSignUpWsDTO user, final CorporateB2BCustomerModel corporateB2BCustomer,
			final CorporateB2BUnitModel corporateB2BUnitModel) throws WooliesB2BUserException
	{
		if (StringUtils.isNotBlank(user.getFirstName()) && StringUtils.isNotBlank(user.getLastName()))
		{
			corporateB2BCustomer.setName(getCustomerNameStrategy().getName(user.getFirstName(), user.getLastName()));

			corporateB2BCustomer.setFirstName(user.getFirstName());
			corporateB2BCustomer.setLastName(user.getLastName());
		}
		corporateB2BCustomer.setDefaultB2BUnit(corporateB2BUnitModel);
		if (null != user.getUid())
		{
			final Matcher fromEmailMatcher = emailPattern.matcher(user.getUid());
			if (!fromEmailMatcher.matches())
			{
				throw new WooliesB2BUserException("80003");

			}

		}
		corporateB2BCustomer.setEmail(user.getEmail());
		corporateB2BCustomer.setUserEmail(user.getEmail());
		if (null != user.getPhone())
		{
			final Matcher matcher = phone_pattern.matcher(user.getPhone());

			if (!matcher.matches())
			{
				throw new WooliesB2BUserException("80007");
			}
		}
		corporateB2BCustomer.setPhone(user.getPhone());
	}

	/**
	 * @return the wooliesB2BUnitServiceImpl
	 */
	public WooliesB2BUnitServiceImpl getWooliesB2BUnitServiceImpl()
	{
		return wooliesB2BUnitServiceImpl;
	}

	/**
	 * @param wooliesB2BUnitServiceImpl
	 *           the wooliesB2BUnitServiceImpl to set
	 */
	public void setWooliesB2BUnitServiceImpl(final WooliesB2BUnitServiceImpl wooliesB2BUnitServiceImpl)
	{
		this.wooliesB2BUnitServiceImpl = wooliesB2BUnitServiceImpl;
	}

	/**
	 * This method is used to remove the account of the user
	 *
	 * @param user
	 * @return the responseDTO
	 * @throws WooliesB2BUserException
	 */
	@Override
	public ResponseDTO removeAccount(final RemoveCustomerDTO user) throws WooliesB2BUserException
	{
		final ResponseDTO responseDTO = new ResponseDTO();
		final List<CorporateB2BCustomerModel> corporateb2bCustomer = wooliesB2BUnitServiceImpl.getB2BAdmin(user.getUid());
		final Set<PrincipalGroupModel> allGroups = corporateb2bCustomer.get(0).getAllGroups();
		if (allGroups.contains(userService.getUserGroupForUID(WooliesgcFacadesConstants.B2BADMINGROUP)))
		{
			flagcheck = isCustomerDeactivated(user);
		}
		else
		{
			throw new WooliesB2BUserException(WooliesgcFacadesConstants.ERRCODE_ISNOTADMIN);
		}
		if (flagcheck.booleanValue())
		{
			responseDTO.setResponseCode("Account Successfully Deactivated");
			responseDTO.setResponseCode("200");
			LOG.info("user removed Successfully");
		}
		else
		{
			responseDTO.setErrorResponse("Either UID not present in the DB or Account not Activated");
			responseDTO.setResponseCode("201");
		}
		return responseDTO;
	}

	/**
	 * This method is used to check whether customer is deactivated or not
	 *
	 * @param user
	 * @return the customer deactivated or not
	 * @throws WooliesB2BUserException
	 */
	@Override
	public Boolean isCustomerDeactivated(final RemoveCustomerDTO user) throws WooliesB2BUserException
	{
		CorporateB2BCustomerModel corporateb2bCustomer = null;

		final StringBuilder flexBuf = new StringBuilder();
		final Map<String, Object> queryParams = new HashMap<>();
		flexBuf.append(QUERY);
		queryParams.put(EMAIL, user.getUsers().get(0).getUid());
		LOG.info("get corporate b2b user" + queryParams);
		final SearchResult<CorporateB2BCustomerModel> queryResultCustomer = flexibleSearchService.search(flexBuf.toString(),
				queryParams);
		if (queryResultCustomer != null && !queryResultCustomer.getResult().isEmpty())
		{
			corporateb2bCustomer = queryResultCustomer.getResult().get(0);
			if (!user.getUid().equalsIgnoreCase(corporateb2bCustomer.getUid()))
			{
				if (corporateb2bCustomer.getActive() == Boolean.TRUE)
				{
					corporateb2bCustomer.setActive(Boolean.FALSE);
					corporateb2bCustomer.setUid(DELETE + corporateb2bCustomer.getUid());
					corporateb2bCustomer.setEmail(DELETE + corporateb2bCustomer.getEmail());
					getModelService().save(corporateb2bCustomer);
					removePermissionModel(user);
					activeBoolFlag = Boolean.TRUE;
				}
				else
				{
					throw new WooliesB2BUserException("70002");
				}
			}
			else
			{
				throw new WooliesB2BUserException(WooliesgcFacadesConstants.ERRCODE_ISNOTATIVE);
			}
		}
		else
		{
			throw new WooliesB2BUserException(WooliesgcFacadesConstants.ERRCODE_ISNOTEXIST);
		}

		return activeBoolFlag;
	}

	/**
	 * This method used to delete the permission model UID
	 *
	 * @param user
	 */
	public void removePermissionModel(final RemoveCustomerDTO user)
	{
		B2BOrderThresholdPermissionModel b2bOrderThresholdPermissionModel = null;
		final StringBuilder flexBuf = new StringBuilder();
		final Map<String, Object> queryParams = new HashMap<>();
		flexBuf.append("select {o.pk} from {B2BOrderThresholdPermission AS o} where {o.code} = ?email");
		queryParams.put(EMAIL, user.getUsers().get(0).getUid());
		LOG.info("get B2BOrderThresholdPermissionModel" + queryParams);
		final SearchResult<B2BOrderThresholdPermissionModel> queryResultCustomer = flexibleSearchService.search(flexBuf.toString(),
				queryParams);
		if (queryResultCustomer != null && !queryResultCustomer.getResult().isEmpty())
		{
			b2bOrderThresholdPermissionModel = queryResultCustomer.getResult().get(0);
			if (!user.getUid().equalsIgnoreCase(b2bOrderThresholdPermissionModel.getCode())
					&& b2bOrderThresholdPermissionModel.getActive() == Boolean.TRUE)
			{
				b2bOrderThresholdPermissionModel.setCode(DELETE + b2bOrderThresholdPermissionModel.getCode());
				getModelService().save(b2bOrderThresholdPermissionModel);
			}
		}
	}

	/**
	 * This method is used to get b2b account details of the user
	 *
	 * @param email
	 * @return
	 * @throws WooliesB2BUserException
	 */
	@Override
	public List<CustomerData> getB2BAccountUsers(final String email) throws WooliesB2BUserException
	{
		Boolean status = null;
		CorporateB2BCustomerModel corporateb2bCustomer = null;
		CorporateB2BUnitModel corporateb2bUnit = null;

		final StringBuilder flexBuf = new StringBuilder();
		final Map<String, Object> queryParams = new HashMap<>();
		flexBuf.append(QUERY);
		queryParams.put(EMAIL, email);
		LOG.info("get B2B user" + queryParams);
		final SearchResult<CorporateB2BCustomerModel> queryResultCustomer = flexibleSearchService.search(flexBuf.toString(),
				queryParams);
		if (queryResultCustomer != null && !queryResultCustomer.getResult().isEmpty())
		{
			corporateb2bCustomer = queryResultCustomer.getResult().get(0);
			corporateb2bUnit = (CorporateB2BUnitModel) corporateb2bCustomer.getDefaultB2BUnit();
			status = corporateb2bUnit.getActive();
			final Set<PrincipalGroupModel> allGroups = corporateb2bCustomer.getAllGroups();
			if (allGroups.contains(userService.getUserGroupForUID(WooliesgcFacadesConstants.B2BADMINGROUP)) && status.booleanValue())
			{
				final StringBuilder flexBuf1 = new StringBuilder();
				final Map<String, Object> queryParams1 = new HashMap<>();
				flexBuf1
						.append("select {o.pk} from {CorporateB2BCustomer AS o} where {o.defaultB2BUnit} = ?pk and {o.active}= ?active ");
				queryParams1.put("pk", corporateb2bUnit.getPk());
				queryParams1.put("active", status);
				LOG.info("get B2B user status" + queryParams);
				final SearchResult<CorporateB2BCustomerModel> queryResultCustomer1 = flexibleSearchService.search(
						flexBuf1.toString(), queryParams1);
				final List<CustomerData> result = new ArrayList<>();
				if (queryResultCustomer1 != null && !queryResultCustomer1.getResult().isEmpty())
				{
					returnCustomerData(email, queryResultCustomer1, result);
					return result;
				}
			}
		}
		return Collections.emptyList();
	}

	/**
	 * This method is used to return customer data
	 *
	 * @param email
	 * @param queryResultCustomer1
	 * @param result
	 */
	private void returnCustomerData(final String email, final SearchResult<CorporateB2BCustomerModel> queryResultCustomer1,
			final List<CustomerData> result)
	{
		for (final UserModel customer : queryResultCustomer1.getResult())
		{
			if (!((CorporateB2BCustomerModel) customer).getUid().equalsIgnoreCase(email))
			{
				final CustomerData customerData = getCustomerConverter().convert(customer);
				result.add(customerData);
			}
		}
	}

	/**
	 * @return the customerConverter
	 */
	@Override
	public Converter<UserModel, CustomerData> getCustomerConverter()
	{
		return customerConverter;
	}

	/**
	 * @param customerConverter
	 *           the customerConverter to set
	 */
	@Override
	public void setCustomerConverter(final Converter<UserModel, CustomerData> customerConverter)
	{
		this.customerConverter = customerConverter;
	}

	/**
	 * This method is used to update the order limit
	 *
	 * @param userId
	 * @param adminUid
	 * @param orderLimit
	 * @throws WooliesB2BUserException
	 */
	@Override
	public void updateOrderLimit(final String userId, final String adminUid, final PriceWsDTO orderLimit)
			throws WooliesB2BUserException
	{
		final List<CorporateB2BCustomerModel> corporateb2bCustomer = wooliesB2BUnitServiceImpl.getB2BAdmin(adminUid);
		final Set<PrincipalGroupModel> allGroups = corporateb2bCustomer.get(0).getAllGroups();
		if (allGroups.contains(userService.getUserGroupForUID(WooliesgcFacadesConstants.B2BADMINGROUP)))
		{
			orderLimitUpdate(userId, orderLimit);
		}
		else
		{
			throw new WooliesB2BUserException(WooliesgcFacadesConstants.ERRCODE_ISNOTADMIN);
		}

	}

	/**
	 * This method is used to update the order limit
	 *
	 * @param userId
	 * @param adminUid
	 * @param orderLimit
	 * @throws WooliesB2BUserException
	 */
	public void orderLimitUpdate(final String userId, final PriceWsDTO orderLimit) throws WooliesB2BUserException
	{
		CorporateB2BCustomerModel corporateb2bCustomer = null;

		final StringBuilder flexBuf = new StringBuilder();
		final Map<String, Object> queryParams = new HashMap<>();
		flexBuf.append(QUERY);
		queryParams.put(EMAIL, userId);
		LOG.info("get B2B user" + queryParams);
		final SearchResult<CorporateB2BCustomerModel> queryResultCustomer = flexibleSearchService.search(flexBuf.toString(),
				queryParams);
		if (queryResultCustomer != null && !queryResultCustomer.getResult().isEmpty())
		{
			corporateb2bCustomer = queryResultCustomer.getResult().get(0);
			final Set<PrincipalGroupModel> allGroups = corporateb2bCustomer.getAllGroups();
			if (corporateb2bCustomer.getActive() == Boolean.TRUE
					&& !allGroups.contains(userService.getUserGroupForUID(WooliesgcFacadesConstants.B2BADMINGROUP)))
			{
				final List<B2BPermissionModel> b2bPermissionModel = wooliesB2BUnitServiceImpl.orderLimitUpdate(userId);
				final B2BOrderThresholdPermissionModel b2bOrderPermissionModel = (B2BOrderThresholdPermissionModel) b2bPermissionModel
						.get(0);
				b2bOrderPermissionModel.setThreshold(Double.valueOf(orderLimit.getValue().doubleValue()));

				if (getCommonI18NService().getCurrency(orderLimit.getCurrencyIso()) != null)
				{
					b2bOrderPermissionModel.setCurrency(getCommonI18NService().getCurrency(orderLimit.getCurrencyIso()));
				}
				else
				{
					throw new WooliesB2BUserException("50004");
				}
				getModelService().save(b2bOrderPermissionModel);
			}
			else
			{
				throw new WooliesB2BUserException(WooliesgcFacadesConstants.ERRCODE_ISNOTATIVE);
			}
		}
		else
		{
			throw new WooliesB2BUserException(WooliesgcFacadesConstants.ERRCODE_ISNOTEXIST);
		}

	}
}
