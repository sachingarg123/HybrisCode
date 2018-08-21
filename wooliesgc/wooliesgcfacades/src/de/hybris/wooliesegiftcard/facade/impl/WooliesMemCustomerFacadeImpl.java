/**
 *
 */
package de.hybris.wooliesegiftcard.facade.impl;

import de.hybris.platform.commercefacades.customer.impl.DefaultCustomerFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.commercewebservicescommons.dto.user.AddressWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.user.UserSignUpWsDTO;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.wooliesegiftcard.core.WooliesMemberCustomerAccountService;
import de.hybris.wooliesegiftcard.core.enums.UserDataType;
import de.hybris.wooliesegiftcard.core.model.MemberCustomerModel;
import de.hybris.wooliesegiftcard.core.model.MemberUnitModel;
import de.hybris.wooliesegiftcard.exceptions.WooliesFacadeLayerException;
import de.hybris.wooliesegiftcard.facade.ProfileSummeryFacade;
import de.hybris.wooliesegiftcard.facade.WooliesMemCustomerFacade;
import de.hybris.wooliesegiftcard.facades.constants.WooliesgcFacadesConstants;
import de.hybris.wooliesegiftcard.facades.dto.PlaceOrderRequestDTO;

import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;



/**
 * @author 669567 This class is used to maintain customer member user details
 */
public class WooliesMemCustomerFacadeImpl extends DefaultCustomerFacade implements WooliesMemCustomerFacade
{
	private static String ERRCODE_MEMEBER_ACCOUNT_NOT_VALID = "ERR_40017";
	@Autowired
	private FlexibleSearchService flexibleSearchService;

	private ProfileSummeryFacade profileSummeryFacade;

	private UserService userService;
	private WooliesMemberCustomerAccountService wooliesMemberCustomerAccountService;

	private Converter<AddressData, AddressModel> addressReverseConverter;

	private static final String CHARACTER_ENCODING = "UTF-8";
	private static final String CIPHERTRANSFORMATION = "AES/CBC/PKCS5PADDING";
	private static final String AESENCRYPTIONALGORITHEM = "AES";
	final String PHONE_REGEX = "^\\+?[0-9 ()-]{10,20}$";
	final Pattern phone_pattern = Pattern.compile(PHONE_REGEX);
	private static final String ERRORCODE = "80007";
	private static final String ERRCODE_EMAILFORMAT = "80003";
	private static final String ERRCODE_PHONENOTVALID = "80007";
	final Pattern pattern = Pattern.compile("[a-zA-Z0-9\\s]*");
	String EMAIL_REGEX = "^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$";
	final Pattern emailPattern = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);

	@Autowired
	private CommonI18NService commonI18NService;
	private static final Logger LOG = Logger.getLogger(WooliesMemCustomerFacadeImpl.class);

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
	 * @return the wooliesB2BCustomerAccountService
	 */
	public WooliesMemberCustomerAccountService getWooliesB2BCustomerAccountService()
	{
		return wooliesMemberCustomerAccountService;
	}

	/**
	 * @param wooliesB2BCustomerAccountService
	 *           the wooliesB2BCustomerAccountService to set
	 */
	public void setWooliesMemberCustomerAccountService(
			final WooliesMemberCustomerAccountService wooliesMemberCustomerAccountService)
	{
		this.wooliesMemberCustomerAccountService = wooliesMemberCustomerAccountService;
	}

	/**
	 * @return the wooliesB2BCustomerAccountService
	 */
	public ProfileSummeryFacade getProfileSummeryFacade()
	{
		return profileSummeryFacade;
	}

	/**
	 * @param wooliesB2BCustomerAccountService
	 *           the wooliesB2BCustomerAccountService to set
	 */
	public void setProfileSummeryFacade(final ProfileSummeryFacade profileSummeryFacade)
	{
		this.profileSummeryFacade = profileSummeryFacade;
	}

	/**
	 * This method is used to get he member user data
	 *
	 * @param user
	 *           User during signup
	 * @param memberUnit
	 *           the Member Unit for the User
	 * @return userProfileData the profile summary Data for the User
	 * @throws WooliesFacadeLayerException
	 *            throwing WooliesFacadeLayerException exception
	 */
	@Override
	public void getMemberUser(final UserSignUpWsDTO user, final MemberUnitModel memberUnit) throws WooliesFacadeLayerException
	{

		final String usermemberId = user.getUid().toLowerCase();
		final List<MemberCustomerModel> memberUser = getMemberUserModel(usermemberId);
		if (null != memberUser && !memberUser.isEmpty())
		{
			if (null != memberUser.get(0).getDefaultB2BUnit().getUid()
					&& user.getMemberId().equalsIgnoreCase(memberUser.get(0).getDefaultB2BUnit().getUid()))
			{
				updateMemberCustomerModel(user, memberUser.get(0));
			}
			else
			{

				LOG.info("User is not associated with supplied memberID.");
				throw new WooliesFacadeLayerException(ERRCODE_MEMEBER_ACCOUNT_NOT_VALID);

			}
		}
		else
		{
			LOG.info("User is not available in System.");

			try
			{
				if (checkValidMemberId(user.getMemberId(), user.getUid()))
				{
					createMemberUser(user, memberUnit);
				}
				else
				{
					LOG.info("User is not associated with supplied memberID.");
					throw new WooliesFacadeLayerException(ERRCODE_MEMEBER_ACCOUNT_NOT_VALID);
				}
			}
			catch (final DuplicateUidException e)
			{
				LOG.info("User already available in system.", e);
				throw new WooliesFacadeLayerException(WooliesgcFacadesConstants.ERRCODE_USER_ALREADY_AVAILABLE);
			}
		}
	}
	/*
	 * * This method is used to get he member user data
	 *
	 * @param user User during signup
	 *
	 * @param memberCustomerModel the Member Customer
	 */
	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.wooliesegiftcard.facade.WooliesMemCustomerFacade#updateMemberCustomerModel(java.lang.String)
	 */

	private void updateMemberCustomerModel(final UserSignUpWsDTO user, final MemberCustomerModel memberCustomerModel)
			throws WooliesFacadeLayerException
	{
		if (StringUtils.isNotBlank(user.getFirstName()))
		{
			memberCustomerModel.setFirstName(user.getFirstName());
			memberCustomerModel.setName(user.getFirstName());
		}
		else
		{
			memberCustomerModel.setFirstName(WooliesgcFacadesConstants.DUMMY);
			memberCustomerModel.setName(user.getFirstName());
		}
		memberCustomerModel.setLastName(user.getLastName());
		if (null != user.getEmail())
		{
			final Matcher fromEmailMatcher = emailPattern.matcher(user.getEmail());
			if (!fromEmailMatcher.matches())
			{
				try
				{
					throw new WooliesFacadeLayerException(ERRCODE_EMAILFORMAT);
				}
				catch (final WooliesFacadeLayerException e)
				{
					LOG.info("Email format not Correct.", e);
				}
			}
		}
		memberCustomerModel.setEmail(null != user.getEmail() ? user.getEmail() : WooliesgcFacadesConstants.DUMMY);
		memberCustomerModel.setUserEmail(null != user.getEmail() ? user.getEmail() : WooliesgcFacadesConstants.DUMMY);
		if (null != user.getPhone())
		{
			final Matcher matcher = phone_pattern.matcher(user.getPhone());

			if (!matcher.matches())
			{
				throw new WooliesFacadeLayerException(ERRCODE_PHONENOTVALID);
			}
		}
		memberCustomerModel.setPhone(user.getPhone());
		wooliesMemberCustomerAccountService.updateMemberCustomer(memberCustomerModel);

	}

	/**
	 * This method is used check member is valid or not
	 *
	 * @param memberId
	 *           the MemberId
	 * @param uid
	 *           the User ID
	 * @return The valid user or not
	 */
	private boolean checkValidMemberId(final String memberId, final String uid)
	{

		final StringTokenizer stringTokenizer = new StringTokenizer(uid.trim(), "_");

		while (stringTokenizer.hasMoreElements())
		{
			if (memberId.equalsIgnoreCase(stringTokenizer.nextElement().toString()))
			{
				return true;
			}
		}
		return false;

	}

	/**
	 * This method is used to create member user
	 *
	 * @param user
	 *           the Current User
	 * @param memberAccount
	 *           the Member account for the User
	 * @return UserProfileData the UserProfileData of the User
	 * @throws DuplicateUidException
	 *            in case throws DuplicateUidException
	 * @throws WooliesFacadeLayerException
	 *            in case throws WooliesFacadeLayerException
	 */

	private void createMemberUser(final UserSignUpWsDTO user, final MemberUnitModel memberAccount)
			throws DuplicateUidException, WooliesFacadeLayerException
	{
		final MemberCustomerModel newCustomer = getModelService().create(MemberCustomerModel.class);
		if (newCustomer != null && user != null)
		{
			setNamesForTheMemberUser(user, newCustomer);

			newCustomer.setDefaultB2BUnit(memberAccount);
			newCustomer.setCustomerType(UserDataType.MEM);
			checkEmailFormat(newCustomer);
			newCustomer.setEmail(null != user.getEmail() ? user.getEmail() : WooliesgcFacadesConstants.DUMMY);
			newCustomer.setUserEmail(null != user.getEmail() ? user.getEmail() : WooliesgcFacadesConstants.DUMMY);
			final Set<PrincipalGroupModel> groups = new HashSet<>(newCustomer.getGroups());
			groups.add(userService.getUserGroupForUID("b2badmingroup"));
			newCustomer.setGroups(groups);
			newCustomer.setUid(user.getUid());
			newCustomer.setOriginalUid(user.getUid());
			if (null != newCustomer.getPhone())
			{
				final Matcher matcher = phone_pattern.matcher(newCustomer.getPhone());

				if (!matcher.matches())
				{
					throw new WooliesFacadeLayerException(ERRCODE_EMAILFORMAT);
				}
			}
			newCustomer.setPhone(null != user.getPhone() ? user.getPhone() : WooliesgcFacadesConstants.DUMMY);
			wooliesMemberCustomerAccountService.register(newCustomer);
		}
	}

	/**
	 * @param newCustomer
	 *           the new Customer Model created
	 * @throws DuplicateUidException
	 *            throwing this in case of any error
	 */
	private void checkEmailFormat(final MemberCustomerModel newCustomer) throws DuplicateUidException
	{
		if (null != newCustomer.getEmail())
		{
			final Matcher fromEmailMatcher = emailPattern.matcher(newCustomer.getEmail());
			if (!fromEmailMatcher.matches())
			{
				throw new DuplicateUidException(ERRORCODE);
			}
		}
	}

	/**
	 * @param user
	 *           the current User
	 * @param newCustomer
	 *           the new Customer Model created
	 */
	private void setNamesForTheMemberUser(final UserSignUpWsDTO user, final MemberCustomerModel newCustomer)
	{
		if (StringUtils.isNotBlank(user.getFirstName()))
		{
			newCustomer.setName(getCustomerNameStrategy().getName(user.getFirstName(), user.getLastName()));

			newCustomer.setFirstName(user.getFirstName());
			newCustomer.setLastName(user.getLastName());
		}
		else
		{
			newCustomer.setFirstName(WooliesgcFacadesConstants.DUMMY);
			newCustomer.setLastName(WooliesgcFacadesConstants.DUMMY);

			newCustomer.setName(getCustomerNameStrategy().getName(newCustomer.getFirstName(), newCustomer.getLastName()));
		}
	}

	/**
	 * This method is used to get member user model
	 *
	 * @param uid
	 *           the UID for the User
	 * @return memberCustomerModel the MemberCustomerModel returns the List of Member Customer
	 */
	private List<MemberCustomerModel> getMemberUserModel(final String uid)
	{
		final StringBuilder flexBuf = new StringBuilder();
		final Map<String, Object> queryParams = new HashMap<>();
		flexBuf.append("SELECT {PK} FROM {MemberCustomer} where {uid} = ?uid");
		queryParams.put("uid", uid);
		final SearchResult<MemberCustomerModel> memmberUser = flexibleSearchService.search(flexBuf.toString(), queryParams);
		if (memmberUser != null)
		{
			return memmberUser.getResult();
		}
		else
		{
			return Collections.emptyList();

		}
	}

	/**
	 * This method is used to get member unit model for the given member id
	 *
	 * @param memberId
	 *           the MemberId/uid used to fetch the Member Unit MemberUnitModel
	 * @return the list of MemberUnitModel it returns the list of MemberUnit Model
	 */
	@Override
	public List<MemberUnitModel> getMemberUnit(final String uid)
	{
		final StringBuilder flexBuf = new StringBuilder();
		final Map<String, Object> queryParams = new HashMap<>();
		flexBuf.append("SELECT {PK} FROM {MemberUnit} where {uid} = ?uid");

		queryParams.put("uid", uid);
		final SearchResult<MemberUnitModel> memberUnit = flexibleSearchService.search(flexBuf.toString(), queryParams);
		if (memberUnit != null)
		{
			return memberUnit.getResult();
		}
		else
		{
			return Collections.emptyList();
		}
	}
	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.wooliesegiftcard.facade.WooliesMemCustomerFacade#decrypt(java.lang.String, java.lang.String)
	 */

	@Override
	public String decrypt(final String memberToken, final String memberKey)
	{
		String decryptedText = null;
		try
		{
			final Cipher cipher = Cipher.getInstance(CIPHERTRANSFORMATION);
			final byte[] key = memberKey.getBytes(CHARACTER_ENCODING);
			final SecretKeySpec secretKey = new SecretKeySpec(key, AESENCRYPTIONALGORITHEM);
			final IvParameterSpec ivparameterspec = new IvParameterSpec(key);
			cipher.init(Cipher.DECRYPT_MODE, secretKey, ivparameterspec);
			final Base64.Decoder decoder = Base64.getDecoder();
			final byte[] cipherText = decoder.decode(memberToken.getBytes("UTF8"));
			decryptedText = new String(cipher.doFinal(cipherText), CHARACTER_ENCODING);

		}
		catch (final Exception e)
		{
			LOG.error("decrypt Exception : ", e);
		}
		return decryptedText;
	}

	/**
	 * This method is used to save the member profile details
	 *
	 * @param placeOrderRequestDTO
	 *           the Data request during placeOrder request
	 * @param billingAddressData
	 *           the Billing Address Data for the User
	 * @param userId
	 *           the USER ID for the current User
	 * @return the MemberCustomerModel
	 */
	@Override
	public MemberCustomerModel saveMemberProfile(final PlaceOrderRequestDTO placeOrderRequestDTO,
			final AddressData shippingAddressData, final AddressData billingAddressData, final String userId)
	{
		final List<MemberCustomerModel> memberUserList = getMemberUserModel(userId);
		MemberCustomerModel memberUser = null;
		if (null != memberUserList && !memberUserList.isEmpty())
		{
			memberUser = memberUserList.get(0);
			memberUser.getCustomerType();
			if (null == memberUserList.get(0).getFirstName() || memberUserList.get(0).getFirstName().isEmpty()
					|| memberUserList.get(0).getFirstName().equals(WooliesgcFacadesConstants.DUMMY))
			{
				memberUser.setFirstName(placeOrderRequestDTO.getFirstName());
			}

			if (null == memberUserList.get(0).getLastName() || memberUserList.get(0).getLastName().isEmpty()
					|| memberUserList.get(0).getLastName().equals(WooliesgcFacadesConstants.DUMMY))
			{
				memberUser.setLastName(placeOrderRequestDTO.getLastName());
			}

			setEmailForUser(placeOrderRequestDTO, memberUserList, memberUser);
			setPhone(placeOrderRequestDTO, memberUserList, memberUser);
			setBillingAddressforUser(placeOrderRequestDTO, billingAddressData, memberUserList, memberUser);
			setShippingAddressforUser(placeOrderRequestDTO, billingAddressData, memberUser);

			memberUser.setName(getCustomerNameStrategy().getName(memberUser.getFirstName(), memberUser.getLastName()));
			wooliesMemberCustomerAccountService.updateMemberCustomer(memberUser);
		}
		else
		{
			LOG.info("User is not available in System.");
			try
			{
				throw new WooliesFacadeLayerException(ERRCODE_MEMEBER_ACCOUNT_NOT_VALID);
			}
			catch (final WooliesFacadeLayerException e)
			{
				LOG.info(e);
			}

		}
		return memberUser;
	}

	/**
	 * @param placeOrderRequestDTO
	 *           the Data request during placeOrder request
	 * @param memberUserList
	 *           List of Member User Customers
	 * @param memberUser
	 *           the Member User
	 */
	private void setEmailForUser(final PlaceOrderRequestDTO placeOrderRequestDTO, final List<MemberCustomerModel> memberUserList,
			final MemberCustomerModel memberUser)
	{
		if (null == memberUserList.get(0).getEmail() || memberUserList.get(0).getEmail().isEmpty()
				|| memberUserList.get(0).getEmail().equals(WooliesgcFacadesConstants.DUMMY))
		{
			memberUser.setEmail(placeOrderRequestDTO.getEmail());
		}
	}

	/**
	 * @param placeOrderRequestDTO
	 *           the Data request during placeOrder request
	 * @param memberUserList
	 *           List of Member User Customers
	 * @param memberUser
	 *           the Member User
	 */
	/*
	 * This Method is used to set the Phone Number during Place order Request for the User
	 */
	private void setPhone(final PlaceOrderRequestDTO placeOrderRequestDTO, final List<MemberCustomerModel> memberUserList,
			final MemberCustomerModel memberUser)
	{
		if (null == memberUserList.get(0).getPhone() || memberUserList.get(0).getPhone().isEmpty()
				|| memberUserList.get(0).getPhone().equals(WooliesgcFacadesConstants.DUMMY))
		{
			memberUser.setPhone(placeOrderRequestDTO.getPhoneNumber());
		}
	}

	/**
	 * @param placeOrderRequestDTO
	 *           the Data request during placeOrder request
	 * @param billingAddressData
	 *           the Billing Address Data for the User
	 * @param memberUser
	 *           The Current Member User
	 */
	/*
	 * This Method is used to set the Shipping Address during Place order Request for the User
	 */
	private void setShippingAddressforUser(final PlaceOrderRequestDTO placeOrderRequestDTO, final AddressData billingAddressData,
			final MemberCustomerModel memberUser)
	{
		if (placeOrderRequestDTO.getShippingAddress().getSaveToProfile() != null
				&& placeOrderRequestDTO.getShippingAddress().getSaveToProfile().booleanValue())
		{

			final AddressModel shippingAddressModel = getAddressReverseConverter().convert(billingAddressData);
			shippingAddressModel.setOwner(memberUser);
			memberUser.setDefaultShipmentAddress(shippingAddressModel);
		}
	}

	/**
	 *
	 * @param placeOrderRequestDTO
	 *           the Data request during placeOrder request
	 * @param billingAddressData
	 *           the Billing Address Data for the User
	 * @param memberUserList
	 *           List of Member User Customers
	 * @param memberUser
	 *           the Member User
	 */
	/*
	 * This Method is used to set Billing Address during Place order Request for the User
	 */
	private void setBillingAddressforUser(final PlaceOrderRequestDTO placeOrderRequestDTO, final AddressData billingAddressData,
			final List<MemberCustomerModel> memberUserList, final MemberCustomerModel memberUser)
	{
		if (null == memberUserList.get(0).getDefaultPaymentAddress()
				|| (placeOrderRequestDTO.getBillingAddress().getSaveToProfile() != null
						&& placeOrderRequestDTO.getBillingAddress().getSaveToProfile().booleanValue()))
		{
			final AddressModel billingAddressModel = getAddressReverseConverter().convert(billingAddressData);
			billingAddressModel.setOwner(memberUser);
			memberUser.setDefaultPaymentAddress(billingAddressModel);

		}
	}

	/**
	 * This method is used to set shipping address for the give user
	 *
	 * @param placeOrderRequestDTO
	 *           the Data request during placeOrder request
	 * @param shippingAddress
	 *           the Billing Address Data for the User
	 * @param memberUser
	 *           the Member User
	 *
	 * @return the AddressModel
	 */
	public AddressModel setShippingAddress(final PlaceOrderRequestDTO placeOrderRequestDTO, final AddressWsDTO shippingAddress,
			final MemberCustomerModel memberUser)
	{
		final AddressModel shippingAddressModel = new AddressModel();
		shippingAddressModel.setStreetname(shippingAddress.getAddress1());
		shippingAddressModel.setStreetnumber(shippingAddress.getAddress2());
		shippingAddressModel.setDistrict(shippingAddress.getState());
		shippingAddressModel.setPostalcode(shippingAddress.getPostalCode());
		shippingAddressModel.setFirstname(placeOrderRequestDTO.getFirstName());
		shippingAddressModel.setLastname(placeOrderRequestDTO.getLastName());
		shippingAddressModel.setTown(shippingAddress.getCity());
		shippingAddressModel.setShippingAddress(Boolean.TRUE);
		final String isocode = shippingAddress.getCountry().getIsocode();
		try
		{

			final CountryModel countryModel = commonI18NService.getCountry(isocode);
			shippingAddressModel.setCountry(countryModel);
		}
		catch (final UnknownIdentifierException e)
		{
			throw new ConversionException("No country with the code " + isocode + " found.", e);
		}
		catch (final AmbiguousIdentifierException e)
		{
			throw new ConversionException("More than one country with the code " + isocode + " found.", e);
		}
		shippingAddressModel.setPhone1(placeOrderRequestDTO.getPhoneNumber());
		shippingAddressModel.setOwner(memberUser);
		return shippingAddressModel;

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
}
