/**
*
*/
package de.hybris.wooliesegiftcard.facades.impl;

import de.hybris.model.PersonalisationEGiftCardModel;
import de.hybris.model.PersonalisationMediaModel;
import de.hybris.platform.b2b.model.B2BOrderThresholdPermissionModel;
import de.hybris.platform.b2b.model.B2BPermissionModel;
import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.commerceservices.enums.CustomerType;
import de.hybris.platform.commerceservices.order.CommerceCartService;
import de.hybris.platform.commerceservices.strategies.CheckoutCustomerStrategy;
import de.hybris.platform.commerceservices.strategies.CustomerNameStrategy;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.CartEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import de.hybris.wooliesegiftcard.core.enums.UserDataType;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BCustomerModel;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BUnitModel;
import de.hybris.wooliesegiftcard.dao.impl.WooliesDefaultCartDao;
import de.hybris.wooliesegiftcard.facades.OrderPlacedFacade;
import de.hybris.wooliesegiftcard.facades.WooliesCheckoutFacade;
import de.hybris.wooliesegiftcard.facades.constants.WooliesgcFacadesConstants;
import de.hybris.wooliesegiftcard.facades.dto.PlaceOrderRequestDTO;
import de.hybris.wooliesegiftcard.service.impl.WooliesDefaultCartService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author 668982 This class is default woolies used to checkout
 */
public class DefaultWooliesCheckoutFacade implements WooliesCheckoutFacade
{
	protected static final int APPEND_AS_LAST = -1;
	private CustomerAccountService customerAccountService;
	private BaseStoreService baseStoreService;
	private CheckoutCustomerStrategy checkoutCustomerStrategy;
	private UserService userService;
	private WooliesDefaultCartService cartService;
	private ModelService modelService;
	private CommerceCartService commerceCartService;
	private WooliesDefaultCartDao wooliesDefaultCartDao;
	private ConfigurationService configurationService;
	private KeyGenerator keyGenerator;
	@Autowired
	private CustomerNameStrategy customerNameStrategy;
	@Autowired
	private CustomerFacade customerFacade;

	@Autowired
	private CommonI18NService commonI18NService;

	@Autowired
	private OrderPlacedFacade orderPlacedFacade;


	@Autowired
	private Converter<UserModel, CustomerData> customerConverter;

	/**
	 * @return the configurationService
	 */
	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	/**
	 * @param configurationService
	 *           the configurationService to set
	 */
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}



	/**
	 * @return the modelService
	 */
	public ModelService getModelService()
	{
		return modelService;
	}

	/**
	 * @param modelService
	 *           the modelService to set
	 */
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	/**
	 * @return the wooliesDefaultCartDao
	 */
	public WooliesDefaultCartDao getWooliesDefaultCartDao()
	{
		return wooliesDefaultCartDao;
	}

	/**
	 * @param wooliesDefaultCartDao
	 *           the wooliesDefaultCartDao to set
	 */
	public void setWooliesDefaultCartDao(final WooliesDefaultCartDao wooliesDefaultCartDao)
	{
		this.wooliesDefaultCartDao = wooliesDefaultCartDao;
	}

	/**
	 * @return the commerceCartService
	 */
	public CommerceCartService getCommerceCartService()
	{
		return commerceCartService;
	}

	/**
	 * @param commerceCartService
	 *           the commerceCartService to set
	 */
	public void setCommerceCartService(final CommerceCartService commerceCartService)
	{
		this.commerceCartService = commerceCartService;
	}

	/**
	 * @return the cartService
	 */
	public WooliesDefaultCartService getCartService()
	{
		return cartService;
	}

	/**
	 * @param cartService
	 *           the cartService to set
	 */
	public void setCartService(final WooliesDefaultCartService cartService)
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
	 * @param userService
	 *           the userService to set
	 */
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	/**
	 * @return the checkoutCustomerStrategy
	 */
	public CheckoutCustomerStrategy getCheckoutCustomerStrategy()
	{
		return checkoutCustomerStrategy;
	}

	/**
	 * @param checkoutCustomerStrategy
	 *           the checkoutCustomerStrategy to set
	 */
	public void setCheckoutCustomerStrategy(final CheckoutCustomerStrategy checkoutCustomerStrategy)
	{
		this.checkoutCustomerStrategy = checkoutCustomerStrategy;
	}

	/**
	 * @return the customerAccountService
	 */
	public CustomerAccountService getCustomerAccountService()
	{
		return customerAccountService;
	}

	/**
	 * @param customerAccountService
	 *           the customerAccountService to set
	 */
	public void setCustomerAccountService(final CustomerAccountService customerAccountService)
	{
		this.customerAccountService = customerAccountService;
	}

	/**
	 * @return the baseStoreService
	 */
	public BaseStoreService getBaseStoreService()
	{
		return baseStoreService;
	}

	/**
	 * @param baseStoreService
	 *           the baseStoreService to set
	 */
	public void setBaseStoreService(final BaseStoreService baseStoreService)
	{
		this.baseStoreService = baseStoreService;
	}

	/**
	 * This method is used to create cart from order
	 *
	 * @param order
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void createCartFromOrder(final String orderCode)
	{
		if (!getCheckoutCustomerStrategy().isAnonymousCheckout())
		{
			final BaseStoreModel baseStoreModel = getBaseStoreService().getCurrentBaseStore();
			final OrderModel orderModel;
			try
			{
				orderModel = getCustomerAccountService().getOrderForCode(orderCode, baseStoreModel);
			}
			catch (final ModelNotFoundException ex)
			{
				throw new IllegalArgumentException(WooliesgcFacadesConstants.MODEL_NOT_FOUND);
			}
			final UserModel currentUser = getUserService().getCurrentUser();
			if (orderModel == null || !orderModel.getUser().equals(currentUser))
			{
				throw new IllegalArgumentException(WooliesgcFacadesConstants.MODEL_NOT_FOUND);
			}
			else
			{
				final CartModel cart = getCart(baseStoreModel, currentUser);
				final List<AbstractOrderEntryModel> cartEntries = new ArrayList();
				final List<AbstractOrderEntryModel> orderEntries = orderModel.getEntries();
				double totalPrice = 0;
				long totalQuantity = 0;
				for (final AbstractOrderEntryModel abstractOrderEntryModel : orderEntries)
				{
					totalQuantity += abstractOrderEntryModel.getQuantity().longValue();
					totalPrice += abstractOrderEntryModel.getBasePrice().doubleValue()
							* abstractOrderEntryModel.getQuantity().longValue();
				}
				checkB2BCustomerValue(currentUser, totalPrice, totalQuantity);
				for (final AbstractOrderEntryModel entry : orderEntries)
				{
					createOrdeEntry(cart, cartEntries, entry);
				}
				cart.setEntries(cartEntries);
				getModelService().save(cart);
				setCartToSession(cart);
				getCommerceCartService().calculateCart(cart);
				getModelService().refresh(cart);
			}
		}
	}

	/**
	 * @param currentUser currentUser
	 * @param totalPrice totalPrice
	 * @param totalQuantity totalPrice
	 */
	private void checkB2BCustomerValue(final UserModel currentUser, final double totalPrice, final long totalQuantity)
	{
		final CustomerModel customerModel = (CustomerModel) currentUser;
		if (customerModel.getCustomerType().getCode().equals(UserDataType.B2B.toString()))
		{
			Double thresholdValue = null;
			if (customerModel instanceof CorporateB2BCustomerModel)
			{
				final CorporateB2BCustomerModel b2bCustomer = (CorporateB2BCustomerModel) customerModel;

				final CorporateB2BUnitModel corporateb2BUnit = (CorporateB2BUnitModel) b2bCustomer.getDefaultB2BUnit();
				final Set<B2BPermissionModel> b2bPermissionModel = corporateb2BUnit.getPermissions();
				thresholdValue = checkThreshold(customerModel, thresholdValue, b2bPermissionModel);

				if (thresholdValue != null && totalPrice > thresholdValue.doubleValue())
				{
					throw new IllegalArgumentException("50002");
				}
			}
		}
		else
		{
			if (totalQuantity > configurationService.getConfiguration().getLong("cart.limit.for.user", 2))
			{
				throw new IllegalArgumentException("40013");
			}
		}
	}

	/**
	 * @param customerModel customerModel
	 * @param thresholdValue thresholdValue
	 * @param b2bPermissionModel b2bPermissionModel
	 * @return
	 */
	private Double checkThreshold(final CustomerModel customerModel, Double thresholdValue,
			final Set<B2BPermissionModel> b2bPermissionModel)
	{
		if (!b2bPermissionModel.isEmpty())
		{
			for (final B2BPermissionModel b2bPermission : b2bPermissionModel)
			{
				if (b2bPermission.getCode().equalsIgnoreCase(customerModel.getUid()))
				{
					final B2BOrderThresholdPermissionModel b2bOrderPermissionModel = (B2BOrderThresholdPermissionModel) b2bPermission;
					thresholdValue = b2bOrderPermissionModel.getThreshold();
				}
			}
		}
		return thresholdValue;
	}

	/**
	 * @param baseStoreModel baseStoreModel
	 * @param currentUser baseStoreModel
	 * @return
	 */
	private CartModel getCart(final BaseStoreModel baseStoreModel, final UserModel currentUser)
	{
		final CartModel cart;
		if (wooliesDefaultCartDao.getCartByUID(currentUser.getUid(), baseStoreModel).isEmpty())
		{
			cart = getCartService().getSessionCart();
		}
		else
		{
			cart = wooliesDefaultCartDao.getCartByUID(currentUser.getUid(), baseStoreModel).get(0);
			cart.setEntries(new ArrayList<AbstractOrderEntryModel>());
			modelService.save(cart);
		}
		return cart;
	}

	/**
	 * @param cart cart
	 * @param cartEntries cartEntries
	 * @param entry entry
	 */
	private void createOrdeEntry(final CartModel cart, final List<AbstractOrderEntryModel> cartEntries,
			final AbstractOrderEntryModel entry)
	{
		if (entry.getCustomPrice() != null && entry.getCustomPrice().doubleValue() > 0)
		{
			if (entry.getCustomPrice().doubleValue() >= configurationService.getConfiguration().getLong("minimum.card.value", 5)
					&& entry.getCustomPrice().doubleValue() <= configurationService.getConfiguration().getLong("maximum.card.value",
							500))
			{
				final CartEntryModel cartEntryModel = getCartService().addNewEntry(cart, entry.getProduct(),
						entry.getQuantity().longValue(), entry.getUnit(), APPEND_AS_LAST, false);
				cartEntryModel.setCustomPrice(entry.getCustomPrice());
				cartEntryModel.setBasePrice(entry.getCustomPrice());
				modelService.save(cartEntryModel);
				cartEntries.add(cartEntryModel);
			}
		}
		else
		{
			final CartEntryModel cartEntryModel = getCartService().addNewEntry(cart, entry.getProduct(),
					entry.getQuantity().longValue(), entry.getUnit(), APPEND_AS_LAST, false);
			modelService.save(cartEntryModel);
			cartEntries.add(cartEntryModel);
		}
	}

	/**
	 * During checkout anonymous user is created
	 *
	 * @param placeOrderRequestDTO
	 *           placeOrderRequestDTO
	 * @return CartModel
	 */
	public CartModel createAnonymousUser(final PlaceOrderRequestDTO placeOrderRequestDTO)
	{
		CartModel cartModel;
		final CustomerModel guestCustomer = modelService.create(CustomerModel.class);
		try
		{

			final String guid = customerFacade.generateGUID();
			//takes care of localizing the name based on the site language
			if (placeOrderRequestDTO.getEmail() != null && placeOrderRequestDTO.getFirstName() != null
					&& placeOrderRequestDTO.getLastName() != null && placeOrderRequestDTO.getPhoneNumber() != null)
			{
				guestCustomer.setUid(guid + "|" + placeOrderRequestDTO.getEmail());
				guestCustomer
						.setName(customerNameStrategy.getName(placeOrderRequestDTO.getFirstName(), placeOrderRequestDTO.getLastName()));
				guestCustomer.setFirstName(placeOrderRequestDTO.getFirstName());
				guestCustomer.setLastName(placeOrderRequestDTO.getLastName());
				guestCustomer.setCustomerType(UserDataType.B2C);
				guestCustomer.setType(CustomerType.valueOf(CustomerType.GUEST.getCode()));
				guestCustomer.setSessionLanguage(commonI18NService.getCurrentLanguage());
				guestCustomer.setSessionCurrency(commonI18NService.getCurrentCurrency());
				guestCustomer.setPhone(placeOrderRequestDTO.getPhoneNumber());
				guestCustomer.setUserEmail(placeOrderRequestDTO.getEmail());
				guestCustomer.setCustomerID((String) this.keyGenerator.generate());
			}
			else
			{
				modelService.remove(guestCustomer);
				throw new IllegalArgumentException("103");
			}
			customerAccountService.registerGuestForAnonymousCheckout(guestCustomer, guid);
		}
		catch (final DuplicateUidException e)
		{
			throw new IllegalArgumentException("104");
		}
		cartModel = orderPlacedFacade.getCartForAnonymous(placeOrderRequestDTO.getGuid());
		setCartToSession(cartModel);
		customerFacade.updateCartWithGuestForAnonymousCheckout(customerConverter.convert(guestCustomer));
		setMediaToCustomer(cartModel, guestCustomer);
		return cartModel;
	}

	/**
	 * @param cartModel cartModel
	 * @param guestCustomer cartModel
	 */
	private void setMediaToCustomer(final CartModel cartModel, final CustomerModel guestCustomer)
	{
		final List<AbstractOrderEntryModel> entries = cartModel.getEntries();
		final Set<PersonalisationMediaModel> mediaImages = new HashSet<>();
		for (final AbstractOrderEntryModel abstractOrderEntryModel : entries)
		{
			if (abstractOrderEntryModel.getProduct().getIsEGiftCard().booleanValue()
					&& CollectionUtils.isNotEmpty(abstractOrderEntryModel.getPersonalisationDetail()))
			{
				saveCustomImage(guestCustomer, mediaImages, abstractOrderEntryModel);
			}
		}
		if (CollectionUtils.isNotEmpty(mediaImages))
		{
			guestCustomer.setGiftCardMedias(mediaImages);
			modelService.save(guestCustomer);
		}
	}

	/**
	 * @param guestCustomer cartModel
	 * @param mediaImages mediaImages
	 * @param abstractOrderEntryModel abstractOrderEntryModel
	 */
	private void saveCustomImage(final CustomerModel guestCustomer, final Set<PersonalisationMediaModel> mediaImages,
			final AbstractOrderEntryModel abstractOrderEntryModel)
	{
		final List<PersonalisationEGiftCardModel> personalisationDetail = abstractOrderEntryModel.getPersonalisationDetail();
		for (final PersonalisationEGiftCardModel personalisationEGiftCardModel : personalisationDetail)
		{
			if (personalisationEGiftCardModel.getCustomerImage() != null)
			{
				final PersonalisationMediaModel customImage = personalisationEGiftCardModel.getCustomerImage();
				customImage.setUser(guestCustomer);
				modelService.save(customImage);
				mediaImages.add(customImage);
			}
		}
	}

	/**
	 * @param cart
	 */
	public void setCartToSession(final CartModel cart)
	{
		getCartService().setSessionCart(cart);
	}

	/**
	 * @return the keyGenerator
	 */
	public KeyGenerator getKeyGenerator()
	{
		return keyGenerator;
	}

	/**
	 * @param keyGenerator
	 *           the keyGenerator to set
	 */
	public void setKeyGenerator(final KeyGenerator keyGenerator)
	{
		this.keyGenerator = keyGenerator;
	}


}
