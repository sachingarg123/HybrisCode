/**
 *
 */
package de.hybris.wooliesegiftcard.facades;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.commercefacades.customer.impl.DefaultCustomerFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commercefacades.user.data.RegisterData;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.wooliesegiftcard.core.enums.UserDataType;
import de.hybris.wooliesegiftcard.core.impl.WooliesDefaultCustomerAccountService;
import de.hybris.wooliesegiftcard.core.model.MemberCustomerModel;
import de.hybris.wooliesegiftcard.facades.constants.WooliesgcFacadesConstants;
import de.hybris.wooliesegiftcard.service.impl.WooliesCountryServiceImpl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;



/**
 * @author 648156 This class is used to maintain default customer details
 */
public class WooliesDefaultCustomerFacade extends DefaultCustomerFacade
{

	private Converter<AddressData, AddressModel> addressReverseConverter;
	private WooliesDefaultCustomerAccountService wooliesCustomerAccountService;
	private WooliesCountryServiceImpl wooliesCountryServiceImpl;
	private static final Logger LOG = Logger.getLogger(WooliesDefaultCustomerFacade.class);
	final Pattern pattern = Pattern.compile("[a-zA-Z0-9\\s]*");
	private static final String EMAIL_REGEX = "^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$";
	final Pattern emailPattern = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);

	private static final String PHONE_REGEX = "^\\+?[0-9 ()-]{10,20}$";
	private static final Pattern PHONEPATTERN = Pattern.compile(PHONE_REGEX);

	private static final String ERRORCODE = "80007";
	private static final String DUP_ERRORCODE = "80003";
	@Autowired
	private FlexibleSearchService flexibleSearchService;

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
	 * @return the wooliesCustomerAccountService
	 */
	public WooliesDefaultCustomerAccountService getWooliesCustomerAccountService()
	{
		return wooliesCustomerAccountService;
	}

	/**
	 * @param wooliesCustomerAccountService
	 *           the wooliesCustomerAccountService to set
	 */
	public void setWooliesCustomerAccountService(final WooliesDefaultCustomerAccountService wooliesCustomerAccountService)
	{
		this.wooliesCustomerAccountService = wooliesCustomerAccountService;
	}

	/**
	 * This method is used to register the customer data
	 *
	 * @param registerData
	 * @throws DuplicateUidException
	 */
	@Override
	public void register(final RegisterData registerData) throws DuplicateUidException
	{
		final CustomerModel newCustomer = getModelService().create(CustomerModel.class);


		if (StringUtils.isNotBlank(registerData.getFirstName()) && StringUtils.isNotBlank(registerData.getLastName()))
		{
			newCustomer.setName(getCustomerNameStrategy().getName(registerData.getFirstName(), registerData.getLastName()));

			newCustomer.setFirstName(registerData.getFirstName());
			newCustomer.setLastName(registerData.getLastName());
		}
		if (registerData.getDob() != null)
		{
			final SimpleDateFormat formatter = new SimpleDateFormat(WooliesgcFacadesConstants.DOB);

			try
			{
				newCustomer.setDob(formatter.parse(registerData.getDob()));

			}
			catch (final ParseException e)
			{
				LOG.error("Exception occurred", e);
			}
		}
		if (null != registerData.getPhone())
		{
			final Matcher matcher = PHONEPATTERN.matcher(registerData.getPhone());

			if (!matcher.matches())
			{
				throw new DuplicateUidException(ERRORCODE);
			}
		}
		newCustomer.setUserEmail(registerData.getEmail());

		newCustomer.setCustomerType(UserDataType.B2C);
		newCustomer.setPhone(registerData.getPhone());
		newCustomer.setCardNo(registerData.getCardNo());
		newCustomer.setOptInForMarketing(registerData.getOptInForMarketing());
		newCustomer.setPolicyAgreement(registerData.getPolicyAgreement());
		wooliesCountryServiceImpl.getCountryForUser(registerData);
		final List<AddressModel> addresses = getAddressReverseConverter().convertAll(registerData.getAddresses());
		if (null != registerData.getLogin())
		{
			final Matcher fromEmailMatcher = emailPattern.matcher(registerData.getLogin());
			if (!fromEmailMatcher.matches())
			{
				throw new DuplicateUidException(DUP_ERRORCODE);

			}
		}
		newCustomer.setUid(registerData.getCRNnumber());
		newCustomer.setOriginalUid(registerData.getCRNnumber());
		newCustomer.setSessionLanguage(getCommonI18NService().getCurrentLanguage());
		newCustomer.setSessionCurrency(getCommonI18NService().getCurrentCurrency());
		for (final AddressModel addressModel : addresses)
		{
			addressModel.setFirstname(registerData.getFirstName());
			addressModel.setLastname(registerData.getLastName());
			addressModel.setPhone1(registerData.getPhone());
			addressModel.setOwner(newCustomer);
			newCustomer.setDefaultPaymentAddress(addressModel);
			try
			{
				wooliesCustomerAccountService.saveAddressesEntry(newCustomer, addressModel);
			}
			catch (final ModelSavingException ex)
			{
				LOG.error(ex);
				throw new ModelSavingException("70011");
			}
		}

		wooliesCustomerAccountService.register(newCustomer);
	}

	/**
	 * This method is used to update full profile
	 *
	 * @param customerData
	 * @throws DuplicateUidException
	 */
	@Override
	public void updateFullProfile(final CustomerData customerData) throws DuplicateUidException
	{
		final CustomerModel customer = (CustomerModel) getCurrentUser();
		if (StringUtils.isNotBlank(customerData.getFirstName()) && StringUtils.isNotBlank(customerData.getLastName()))
		{
			customer.setName(getCustomerNameStrategy().getName(customerData.getFirstName(), customerData.getLastName()));
			customer.setFirstName(customerData.getFirstName());
			customer.setLastName(customerData.getLastName());
		}
		customer.setCardNo(customerData.getCardNo());
		if (null != customerData.getPhone())
		{
			final Matcher matcher = PHONEPATTERN.matcher(customerData.getPhone());

			if (!matcher.matches())
			{
				throw new DuplicateUidException(ERRORCODE);
			}
		}
		customer.setPhone(customerData.getPhone());
		customer.setUserEmail(customerData.getEmail());

		customer.setOptInForMarketing(customerData.getOptInForMarketing());
		getModelService().save(customer);
	}


	/**
	 * This method is used to update b2b full profiles
	 *
	 * @param customerDataw
	 *           the Customer Data of the User
	 * @throws DuplicateUidException
	 *            throws this Exception
	 */
	public void B2BupdateFullProfiles(final CustomerData customerDataw) throws DuplicateUidException
	{

		final B2BCustomerModel customers = (B2BCustomerModel) getCurrentUser();
		if (StringUtils.isNotBlank(customerDataw.getFirstName()) && StringUtils.isNotBlank(customerDataw.getLastName()))
		{
			customers.setName(getCustomerNameStrategy().getName(customerDataw.getFirstName(), customerDataw.getLastName()));
			customers.setFirstName(customerDataw.getFirstName());
			customers.setLastName(customerDataw.getLastName());
		}
		if (null != customerDataw.getPhone())
		{
			final Matcher matcher = PHONEPATTERN.matcher(customerDataw.getPhone());

			if (!matcher.matches())
			{
				throw new DuplicateUidException(ERRORCODE);
			}
		}
		customers.setPhone(customerDataw.getPhone());
		customers.setEmail(customerDataw.getEmail());

		customers.setOptInForMarketing(customerDataw.getOptInForMarketing());
		getModelService().save(customers);
	}

	/**
	 * This method is used to update full profiles
	 *
	 * @param customerDatas
	 *           the Customer Data of the User
	 * @throws DuplicateUidException
	 *            throws this Exception
	 */
	public void memberUpdateFullProfiles(final CustomerData customerDatas) throws DuplicateUidException
	{
		final MemberCustomerModel members = (MemberCustomerModel) getCurrentUser();
		if (StringUtils.isNotBlank(customerDatas.getFirstName()) && StringUtils.isNotBlank(customerDatas.getLastName()))
		{
			members.setName(getCustomerNameStrategy().getName(customerDatas.getFirstName(), customerDatas.getLastName()));
			members.setFirstName(customerDatas.getFirstName());
			members.setLastName(customerDatas.getLastName());
		}
		if (null != customerDatas.getPhone())
		{
			final Matcher matcher = PHONEPATTERN.matcher(customerDatas.getPhone());

			if (!matcher.matches())
			{
				throw new DuplicateUidException(ERRORCODE);
			}
		}
		members.setPhone(customerDatas.getPhone());
		members.setEmail(customerDatas.getEmail());

		members.setOptInForMarketing(customerDatas.getOptInForMarketing());
		getModelService().save(members);
	}

	/**
	 * This Method is used to find Products older than Specified days
	 *
	 * @param userId
	 *           the ID associated with the User
	 */
	public void findAllProductsOlderThanSpecifiedDays(final String userId)
	{

		final CustomerModel customer = (CustomerModel) getUserService().getCurrentUser();
		final StringBuilder flexBuf = new StringBuilder();
		final Map<String, Object> queryParams = new HashMap<>();

		flexBuf.append("select {o.pk} from {Customer AS o} where {o.uid} = ?userId");
		queryParams.put("userId", userId);
		final SearchResult<CustomerModel> queryResultCustomer = flexibleSearchService.search(flexBuf.toString(), queryParams);
		if (CollectionUtils.isNotEmpty(queryResultCustomer.getResult()))
		{
			customer.setLoginDisabled(true);
			getModelService().save(customer);
		}
	}

	/**
	 * * This Method is used to find Products older than Specified days for B2B users
	 *
	 * @param userId
	 *           the ID associated with the User
	 */
	public void findAllProductsB2BOlderThanSpecifiedDays(final String userId)
	{

		final B2BCustomerModel customers = (B2BCustomerModel) getUserService().getCurrentUser();
		final StringBuilder flexBuf = new StringBuilder();
		final Map<String, Object> queryParams = new HashMap<>();

		flexBuf.append("select {o.pk} from {B2BCustomer AS o} where {o.uid} = ?userId");
		queryParams.put("userId", userId);
		final SearchResult<B2BCustomerModel> queryResultCustomer = flexibleSearchService.search(flexBuf.toString(), queryParams);
		if (CollectionUtils.isNotEmpty(queryResultCustomer.getResult()))
		{
			customers.setLoginDisabled(true);
			getModelService().save(customers);
		}
	}

	/**
	 * To get current customer
	 *
	 * @return the current customer
	 */
	@Override
	public CustomerData getCurrentCustomer()
	{
		return getCustomerConverter().convert(getCurrentUser());
	}

	/**
	 * To get user
	 *
	 * @return the current user
	 */
	public UserModel getUser()
	{
		return getUserService().getCurrentUser();
	}

	/**
	 * To check anonymous user
	 *
	 * @param customerModel
	 *           the Customer Model Associated with the Guest User
	 * @return true or false
	 */
	public boolean isAnonymousUser(final UserModel customerModel)
	{
		return getUserService().isAnonymousUser(customerModel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.hybris.platform.commercefacades.customer.impl.DefaultCustomerFacade#updateCartWithGuestForAnonymousCheckout(de.
	 * hybris.platform.commercefacades.user.data.CustomerData)
	 */
	@Override
	public void updateCartWithGuestForAnonymousCheckout(final CustomerData guestCustomerData)
	{
		// First thing to do is to try to change the user on the session cart
		if (getCartService().hasSessionCart())
		{
			getCartService().changeCurrentCartUser(getUserService().getUserForUID(guestCustomerData.getUid()));
		}

		// Update the session currency (which might change the cart currency)
		if (!updateSessionCurrency(guestCustomerData.getCurrency(), getStoreSessionFacade().getDefaultCurrency()))
		{
			// Update the user
			getUserFacade().syncSessionCurrency();
		}

		if (!updateSessionLanguage(guestCustomerData.getLanguage(), getStoreSessionFacade().getDefaultLanguage()))
		{
			// Update the user
			getUserFacade().syncSessionLanguage();
		}

		// Calculate the cart after setting everything up
		if (getCartService().hasSessionCart())
		{
			final CartModel sessionCart = getCartService().getSessionCart();

			// Clear the delivery address, delivery mode, payment info before starting the guest checkout.
			sessionCart.setDeliveryAddress(null);
			sessionCart.setPaymentInfo(null);
			getCartService().saveOrder(sessionCart);

			try
			{
				final CommerceCartParameter parameter = new CommerceCartParameter();
				parameter.setEnableHooks(true);
				parameter.setCart(sessionCart);
				getCommerceCartService().recalculateCart(parameter);
			}
			catch (final CalculationException ex)
			{
				LOG.error("Failed to recalculate order [" + sessionCart.getCode() + "]", ex);
			}
		}
	}
}
