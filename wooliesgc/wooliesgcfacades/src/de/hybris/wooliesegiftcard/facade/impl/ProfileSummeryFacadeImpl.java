/**
 *
 */
package de.hybris.wooliesegiftcard.facade.impl;

import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commerceservices.order.CommerceCartService;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.wooliesegiftcard.core.constants.GeneratedWooliesgcCoreConstants.Enumerations.UserDataType;
import de.hybris.wooliesegiftcard.exceptions.WooliesFacadeLayerException;
import de.hybris.wooliesegiftcard.facade.ProfileSummeryFacade;
import de.hybris.wooliesegiftcard.facades.GuestCartProfileData;
import de.hybris.wooliesegiftcard.facades.UserProfileData;
import de.hybris.wooliesegiftcard.facades.constants.WooliesgcFacadesConstants;
import de.hybris.wooliesegiftcard.facades.user.converters.populator.WooliesB2BCustomerProfilePopulator;
import de.hybris.wooliesegiftcard.facades.user.converters.populator.WooliesB2BViewPopulator;
import de.hybris.wooliesegiftcard.facades.user.converters.populator.WooliesB2CViewPopulator;
import de.hybris.wooliesegiftcard.facades.user.converters.populator.WooliesCustomerProfilePopulator;
import de.hybris.wooliesegiftcard.facades.user.converters.populator.WooliesMemberCustomerProfilePopulator;
import de.hybris.wooliesegiftcard.facades.user.converters.populator.WooliesMemberViewPopulator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;


/**
 * @author 648156 This class is used to profile details of customer
 */
public class ProfileSummeryFacadeImpl implements ProfileSummeryFacade
{
	private UserService userService;
	private CartService cartService;
	private CartFacade cartFacade;
	private WooliesCustomerProfilePopulator wooliesCustomerProfilePopulator;
	private WooliesB2BCustomerProfilePopulator wooliesB2BCustomerProfilePopulator;
	private Converter<UserModel, CustomerData> customerConverter;
	private WooliesB2CViewPopulator viewProfilePopulator;
	private WooliesB2BViewPopulator viewsProfilePopulator;
	private WooliesMemberCustomerProfilePopulator wooliesMemberCustomerProfilePopulator;
	private Converter<CartModel, CartData> cartConverter;
	private WooliesMemberViewPopulator wooliesMemberViewPopulator;
	@Autowired
	private BaseSiteService baseSiteService;
	@Autowired
	private CommerceCartService commerceCartService;

	private static final Logger LOG = Logger.getLogger(ProfileSummeryFacadeImpl.class);

	/**
	 * @return the wooliesMemberViewPopulator
	 */
	public WooliesMemberViewPopulator getWooliesMemberViewPopulator()
	{
		return wooliesMemberViewPopulator;
	}

	/**
	 * @param wooliesMemberViewPopulator
	 *           the wooliesMemberViewPopulator to set
	 */
	public void setWooliesMemberViewPopulator(final WooliesMemberViewPopulator wooliesMemberViewPopulator)
	{
		this.wooliesMemberViewPopulator = wooliesMemberViewPopulator;
	}

	/**
	 * @return the viewsProfilePopulator
	 */
	public WooliesB2BViewPopulator getViewsProfilePopulator()
	{
		return viewsProfilePopulator;
	}

	/**
	 * @param viewsProfilePopulator
	 *           the viewsProfilePopulator to set
	 */
	public void setViewsProfilePopulator(final WooliesB2BViewPopulator viewsProfilePopulator)
	{
		this.viewsProfilePopulator = viewsProfilePopulator;
	}

	public WooliesB2CViewPopulator getViewProfilePopulator()
	{
		return viewProfilePopulator;
	}

	/**
	 * @param viewProfilePopulator
	 *           the viewProfilePopulator to set
	 */
	public void setViewProfilePopulator(final WooliesB2CViewPopulator viewProfilePopulator)
	{
		this.viewProfilePopulator = viewProfilePopulator;
	}

	/**
	 * @return the customerConverter
	 */
	public Converter<UserModel, CustomerData> getCustomerConverter()
	{
		return customerConverter;
	}

	/**
	 * @param customerConverter
	 *           the customerConverter to set
	 */
	public void setCustomerConverter(final Converter<UserModel, CustomerData> customerConverter)
	{
		this.customerConverter = customerConverter;
	}

	/**
	 * @return the wooliesB2BCustomerProfilePopulator
	 */
	public WooliesB2BCustomerProfilePopulator getWooliesB2BCustomerProfilePopulator()
	{
		return wooliesB2BCustomerProfilePopulator;
	}

	/**
	 * @param wooliesB2BCustomerProfilePopulator
	 *           the wooliesB2BCustomerProfilePopulator to set
	 */
	public void setWooliesB2BCustomerProfilePopulator(final WooliesB2BCustomerProfilePopulator wooliesB2BCustomerProfilePopulator)
	{
		this.wooliesB2BCustomerProfilePopulator = wooliesB2BCustomerProfilePopulator;
	}

	/**
	 * @return the wooliesMemberCustomerProfilePopulator
	 */
	public WooliesMemberCustomerProfilePopulator getWooliesMemberCustomerProfilePopulator()
	{
		return wooliesMemberCustomerProfilePopulator;
	}

	/**
	 * @param wooliesMemberCustomerProfilePopulator
	 *           the wooliesMemberCustomerProfilePopulator to set
	 */
	public void setWooliesMemberCustomerProfilePopulator(
			final WooliesMemberCustomerProfilePopulator wooliesMemberCustomerProfilePopulator)
	{
		this.wooliesMemberCustomerProfilePopulator = wooliesMemberCustomerProfilePopulator;
	}

	/**
	 * @return the cartService
	 */
	public CartService getCartService()
	{
		return cartService;
	}

	/**
	 * @param cartService
	 *           the cartService to set
	 */
	public void setCartService(final CartService cartService)
	{
		this.cartService = cartService;
	}

	/**
	 * @return the userService
	 */
	public UserService getUserService()
	{
		return userService;
	}

	/**
	 * @return the wooliesCustomerProfilePopulator
	 */
	public WooliesCustomerProfilePopulator getWooliesCustomerProfilePopulator()
	{
		return wooliesCustomerProfilePopulator;
	}

	/**
	 * @param wooliesCustomerProfilePopulator
	 *           the wooliesCustomerProfilePopulator to set
	 */
	public void setWooliesCustomerProfilePopulator(final WooliesCustomerProfilePopulator wooliesCustomerProfilePopulator)
	{
		this.wooliesCustomerProfilePopulator = wooliesCustomerProfilePopulator;
	}

	/**
	 * @param userService
	 *           the userService to set
	 */
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	protected Converter<CartModel, CartData> getCartConverter()
	{
		return cartConverter;
	}

	/**
	 * To set the cart converter
	 *
	 * @param cartConverter
	 */
	@Required
	public void setCartConverter(final Converter<CartModel, CartData> cartConverter)
	{
		this.cartConverter = cartConverter;
	}

	/**
	 * This method is used to get the current customer details
	 *
	 * @return the UserProfileData
	 * @throws WooliesFacadeLayerException
	 */
	@SuppressWarnings("deprecation")
	@Override
	public UserProfileData getCurrentCustomer() throws WooliesFacadeLayerException
	{
		final UserProfileData userProfileData = new UserProfileData();
		final UserModel userModel = getCurrentUser();
		final List<CartData> allCarts = new ArrayList<>();
		final Collection<CartModel> cartModels = userModel.getCarts();
		if (null != cartModels && !cartModels.isEmpty())
		{
			for (final CartModel cartmodel : cartModels)
			{
				CartData cartData = null;
				cartData = getCartConverter().convert(cartmodel);
				allCarts.add(cartData);
			}
		}

		populateCartDetails(allCarts, userProfileData);

		if (getUserService().isAnonymousUser(userModel))
		{
			return userProfileData;
		}
		else if (((CustomerModel) userModel).getCustomerType().getCode() == UserDataType.B2C)
		{
			wooliesCustomerProfilePopulator.populate(userModel, userProfileData);
		}
		else if (((CustomerModel) userModel).getCustomerType().getCode() == UserDataType.B2B)
		{
			wooliesB2BCustomerProfilePopulator.populate(userModel, userProfileData);
		}
		else if (((CustomerModel) userModel).getCustomerType().getCode() == UserDataType.MEM)
		{
			wooliesMemberCustomerProfilePopulator.populate(userModel, userProfileData);
		}
		LOG.info("Profile summary of the Customer Data returned");
		return userProfileData;
	}

	/**
	 * @param allCarts
	 * @param userProfileData
	 */
	private void populateCartDetails(final List<CartData> allCarts, final UserProfileData userProfileData)
	{
		double cartTotal = 0.0;
		int cartLineItemCount = 0;
		long cartGiftCardCount = 0;

		if (!allCarts.isEmpty())
		{
			for (final CartData cartData : allCarts)
			{
				if (null != cartData.getTotalPrice())
				{
					final BigDecimal newtotalPrice = cartData.getTotalPrice().getValue();
					cartTotal = cartTotal + newtotalPrice.doubleValue();
				}
				if (null != cartData.getEntries())
				{
					final int newcartLineItemCount = cartData.getEntries().size();
					cartLineItemCount = cartLineItemCount + newcartLineItemCount;
				}
				if (null != cartData.getDeliveryItemsQuantity())
				{

					final long newcartGiftCardCount = cartData.getDeliveryItemsQuantity().longValue();
					cartGiftCardCount = cartGiftCardCount + newcartGiftCardCount;
				}
			}
		}
		userProfileData.setCartTotal(Double.valueOf(cartTotal));
		userProfileData.setCartLineItemCount(cartLineItemCount);
		userProfileData.setCartGiftCardCount(cartGiftCardCount);
		LOG.info("Cart Details of the Customer updated");
	}

	/**
	 * This method is used to get the user details
	 *
	 * @return the customer data
	 */
	@Override
	public CustomerData getuser()
	{
		final CustomerData customerdatas = new CustomerData();
		final UserModel userModel = getCurrentUser();
		if (((CustomerModel) userModel).getCustomerType().getCode() == UserDataType.B2C)
		{
			viewProfilePopulator.populate(userModel, customerdatas);
		}

		else if (((CustomerModel) userModel).getCustomerType().getCode() == UserDataType.B2B)
		{
			viewsProfilePopulator.populate(userModel, customerdatas);

		}
		else if (((CustomerModel) userModel).getCustomerType().getCode() == UserDataType.MEM)
		{
			wooliesMemberViewPopulator.populate(userModel, customerdatas);
		}
		LOG.info("Customer Data returned");
		return customerdatas;
	}


	/**
	 * @return the cartFacade
	 */
	public CartFacade getCartFacade()
	{
		return cartFacade;
	}

	public void setCartFacade(final CartFacade cartFacade)
	{
		this.cartFacade = cartFacade;
	}

	@Override
	public CustomerData getCurrentB2BCustomer()
	{
		return getCustomerConverter().convert(getCurrentUser());
	}

	protected UserModel getCurrentUser()
	{
		return getUserService().getCurrentUser();
	}

	/**
	 * This method is used to get the current customer details for the given code
	 *
	 * @param code
	 * @return the UserProfileData
	 * @throws WooliesFacadeLayerException
	 */
	@Override
	public UserProfileData getCurrentCustomer(final String code) throws WooliesFacadeLayerException
	{
		final UserProfileData userProfileData = new UserProfileData();
		final UserModel userModel = userService.getUserForUID(code);
		userService.setCurrentUser(userModel);
		final List<CartData> allCarts = new ArrayList<>();
		final Collection<CartModel> cartModels = userModel.getCarts();
		if (null != cartModels && !cartModels.isEmpty())
		{
			for (final CartModel cartmodel : cartModels)
			{
				CartData cartData = null;
				cartData = getCartConverter().convert(cartmodel);
				allCarts.add(cartData);
			}
		}
		populateCartDetails(allCarts, userProfileData);

		if (((CustomerModel) userModel).getCustomerType().getCode() == UserDataType.MEM)
		{
			wooliesMemberCustomerProfilePopulator.populate(userModel, userProfileData);
		}
		LOG.info("Profile summary of the Customer Data returned");
		return userProfileData;
	}

	@Override
	public GuestCartProfileData getCartDetailsForAnonymousUser(final String guid) throws WooliesFacadeLayerException
	{
		final CartModel cartModel = commerceCartService.getCartForGuidAndSiteAndUser(guid, baseSiteService.getCurrentBaseSite(),
				userService.getAnonymousUser());

		if (cartModel == null)
		{
			throw new WooliesFacadeLayerException(WooliesgcFacadesConstants.ERR_CODE_GUEST_CART);
		}

		final CartData cartData = cartConverter.convert(cartModel);
		final GuestCartProfileData guestCartProfileData = new GuestCartProfileData();
		double cartTotal = 0.0;
		int cartLineItemCount = 0;
		long cartGiftCardCount = 0;
		if (null != cartData)
		{

			if (null != cartData.getTotalPrice())
			{
				cartTotal = cartData.getTotalPrice().getValue().doubleValue();
			}
			if (null != cartData.getEntries())
			{
				cartLineItemCount = cartData.getEntries().size();
			}
			if (null != cartData.getDeliveryItemsQuantity())
			{
				cartGiftCardCount = cartData.getDeliveryItemsQuantity().longValue();
			}
		}
		guestCartProfileData.setGuid(guid);
		guestCartProfileData.setCartLineItemCount(cartLineItemCount);
		guestCartProfileData.setCartTotal(Double.valueOf(cartTotal));
		guestCartProfileData.setCartGiftCardCount(cartGiftCardCount);
		return guestCartProfileData;
	}
}
