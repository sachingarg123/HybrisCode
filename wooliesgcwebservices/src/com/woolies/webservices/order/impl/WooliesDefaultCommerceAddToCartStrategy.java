/**
 *
 */
package com.woolies.webservices.order.impl;

import de.hybris.platform.commerceservices.order.CommerceCartModification;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commerceservices.order.CommerceCartModificationStatus;
import de.hybris.platform.commerceservices.order.impl.DefaultCommerceAddToCartStrategy;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.order.CalculationService;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.media.impl.DefaultMediaDao;
import de.hybris.platform.servicelayer.media.impl.DefaultMediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.storelocator.model.PointOfServiceModel;
import de.hybris.wooliesegiftcard.core.enums.UserDataType;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BCustomerModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

import org.apache.log4j.Logger;

import com.woolies.webservices.rest.constants.YcommercewebservicesConstants;
import com.woolies.webservices.rest.exceptions.WoolliesCartModificationException;


/**
 * @author 668982. This class provides commerce add to cart strategy functionalities
 *
 */
public class WooliesDefaultCommerceAddToCartStrategy extends DefaultCommerceAddToCartStrategy
{
	private static final String ERRCODE_CARTLIMIT = "40013";
	private DefaultMediaService mediaService;
	private ModelService modelService;
	private DefaultMediaDao mediaDao;
	private ConfigurationService configurationService;
	private CalculationService calculationService;
	private static final Logger LOG = Logger.getLogger(WooliesDefaultCommerceAddToCartStrategy.class);

	/**
	 * @return the configurationService
	 */
	@Override
	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	/**
	 * @param configurationService
	 *           the configurationService to set
	 */
	@Override
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	/**
	 * @return the mediaService
	 */
	public DefaultMediaService getMediaService()
	{
		return mediaService;
	}

	/**
	 * @param mediaService
	 *           the mediaService to set
	 */
	public void setMediaService(final DefaultMediaService mediaService)
	{
		this.mediaService = mediaService;
	}

	/**
	 * @return the modelService
	 */
	@Override
	public ModelService getModelService()
	{
		return modelService;
	}

	/**
	 * @param modelService
	 *           the modelService to set
	 */
	@Override
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	/**
	 * @return the mediaDao
	 */
	public DefaultMediaDao getMediaDao()
	{
		return mediaDao;
	}

	/**
	 * @param mediaDao
	 *           the mediaDao to set
	 */
	public void setMediaDao(final DefaultMediaDao mediaDao)
	{
		this.mediaDao = mediaDao;
	}



	/**
	 * This method is used to create add to cart
	 *
	 * @param parameter
	 * @param status
	 * @param entry
	 * @param quantityAdded
	 * @return CommerceCartModification
	 */
	@Override
	protected CommerceCartModification createAddToCartResp(final CommerceCartParameter parameter, final String status,
			final CartEntryModel entry, final long quantityAdded)
	{
		final long quantityToAdd = parameter.getQuantity();

		final CommerceCartModification modification = new CommerceCartModification();
		modification.setStatusCode(status);
		modification.setQuantityAdded(quantityAdded);
		modification.setQuantity(quantityToAdd);
		modification.setCartTotal(getCartTotalPrice(parameter.getCart()));
		modification.setCartItemCount(getLineItem(parameter.getCart()));
		modification.setCartGiftCardCount(getCartCount(parameter.getCart()));
		if (parameter.getCustomerPrice() != null)
		{
			modification.setCustomerPrice(parameter.getCustomerPrice());
		}
		modification.setEntry(entry);

		return modification;
	}

	/**
	 * This method is used to do add to cart
	 *
	 * @param parameter
	 * @return
	 * @throws CommerceCartModificationException
	 */
	@Override
	protected CommerceCartModification doAddToCart(final CommerceCartParameter parameter) throws CommerceCartModificationException
	{
		CommerceCartModification modification = null;
		if (parameter != null)
		{
			final CartModel cartModel = parameter.getCart();
			final ProductModel productModel = parameter.getProduct();
			final long quantityToAdd = parameter.getQuantity();
			final PointOfServiceModel deliveryPointOfService = parameter.getPointOfService();
			long totalQuantitiesOfCart = 0;
			final List<AbstractOrderEntryModel> entries = cartModel.getEntries();
			for (final AbstractOrderEntryModel abstractOrderEntryModel : entries)
			{
				totalQuantitiesOfCart += abstractOrderEntryModel.getQuantity().longValue();
			}
			this.beforeAddToCart(parameter);
			validateAddToCart(parameter);
			if (isProductForCode(parameter).booleanValue())
			{
				// So now work out what the maximum allowed to be added is (note that this may be negative!)
				final long actualAllowedQuantityChange = getAllowedCartAdjustmentForProduct(cartModel, productModel, quantityToAdd,
						deliveryPointOfService);
				final Integer maxOrderQuantity = productModel.getMaxOrderQuantity();
				final long cartLevel = checkCartLevel(productModel, cartModel, deliveryPointOfService);
				final long cartLevelAfterQuantityChange = actualAllowedQuantityChange + cartLevel;
				totalQuantitiesOfCart += actualAllowedQuantityChange;
				if (actualAllowedQuantityChange > 0)
				{
					final CustomerModel customerModel = (CustomerModel) cartModel.getUser();
					final UserDataType userdataType = customerModel.getCustomerType();

					calculateCartQuanties(totalQuantitiesOfCart, userdataType);
					final CartEntryModel entryModel = addCartEntry(parameter, actualAllowedQuantityChange);
					settingCustomerPrice(parameter, entryModel);
					final String statusCode = getStatusCodeAllowedQuantityChange(actualAllowedQuantityChange, maxOrderQuantity,
							quantityToAdd, cartLevelAfterQuantityChange);

					modification = createAddToCartResp(parameter, statusCode, entryModel, actualAllowedQuantityChange);

					cartStatus(modification, totalQuantitiesOfCart, userdataType);

					getModelService().save(entryModel);
				}

				else
				{ // Not allowed to add any quantity, or maybe even asked to reduce the quantity
				  // Do nothing!
					final String status = getStatusCodeForNotAllowedQuantityChange(maxOrderQuantity, maxOrderQuantity);

					modification = cartStatusChecking(parameter, modification, status);
				}
			}
			else
			{
				modification = createAddToCartResp(parameter, CommerceCartModificationStatus.UNAVAILABLE,
						createEmptyCartEntry(parameter), 0);
			}
		}
		return modification;
	}

	/**
	 * This used to calculateCartQuanties
	 * 
	 * @param totalQuantitiesOfCart
	 * @param userdataType
	 * @throws WoolliesCartModificationException
	 */
	private void calculateCartQuanties(final long totalQuantitiesOfCart, final UserDataType userdataType)
			throws WoolliesCartModificationException
	{
		if (userdataType == null || (userdataType == UserDataType.B2C || userdataType == UserDataType.MEM))
		{
			calculateQuantiesOfCart(totalQuantitiesOfCart);
		}
		else
		{
			if (totalQuantitiesOfCart > configurationService.getConfiguration().getLong(
					YcommercewebservicesConstants.CART_LIMIT_B2B, 9999))
			{
				throw new WoolliesCartModificationException("40023");
			}
		}
	}

	/**
	 * This method is used to set cart status
	 *
	 * @param modification
	 * @param totalQuantitiesOfCart
	 * @param userdataType
	 */
	private void cartStatus(final CommerceCartModification modification, final long totalQuantitiesOfCart,
			final UserDataType userdataType)
	{
		if (userdataType == null || (userdataType == UserDataType.B2C || userdataType == UserDataType.MEM))
		{

			setCartStatus(modification, totalQuantitiesOfCart);
		}
		else
		{
			if (totalQuantitiesOfCart < configurationService.getConfiguration().getLong(
					YcommercewebservicesConstants.CART_LIMIT_B2B, 9999))
			{
				modification.setIsAddtoCartDisable(Boolean.TRUE);
			}
			else
			{
				modification.setIsAddtoCartDisable(Boolean.FALSE);
			}
		}
	}

	/**
	 * this method is used to check cart status
	 *
	 * @param parameter
	 * @param modification
	 * @param status
	 * @return
	 */
	private CommerceCartModification cartStatusChecking(final CommerceCartParameter parameter,
			CommerceCartModification modification, final String status)
	{
		if (status != null)
		{
			modification = createAddToCartResp(parameter, status, createEmptyCartEntry(parameter), 0);
		}
		return modification;
	}

	/**
	 * this method is used to set cart status
	 *
	 * @param modification
	 * @param totalQuantitiesOfCart
	 */
	private void setCartStatus(final CommerceCartModification modification, final long totalQuantitiesOfCart)
	{
		if (totalQuantitiesOfCart < configurationService.getConfiguration().getLong("cart.limit.for.user", 2))
		{
			modification.setIsAddtoCartDisable(Boolean.TRUE);
		}
		else
		{
			modification.setIsAddtoCartDisable(Boolean.FALSE);
		}
	}

	/**
	 * @param parameter
	 * @param entryModel
	 */
	private void settingCustomerPrice(final CommerceCartParameter parameter, final CartEntryModel entryModel)
	{
		if (parameter.getCustomerPrice() != null)//setting customer price to entry
		{
			entryModel.setCustomPrice(parameter.getCustomerPrice());
		}
	}

	/**
	 * @param totalQuantitiesOfCart
	 * @throws WoolliesCartModificationException
	 */
	private void calculateQuantiesOfCart(final long totalQuantitiesOfCart) throws WoolliesCartModificationException
	{
		if (totalQuantitiesOfCart > configurationService.getConfiguration().getLong("cart.limit.for.user", 2))
		{
			throw new WoolliesCartModificationException(ERRCODE_CARTLIMIT);
		}
	}

	/**
	 * This method is used to merge the commerce cart entries
	 *
	 * @param modification
	 * @param parameter
	 * @throws CommerceCartModificationException
	 */
	@Override
	protected void mergeEntry(@Nonnull final CommerceCartModification modification, @Nonnull final CommerceCartParameter parameter)
			throws CommerceCartModificationException
	{

		ServicesUtil.validateParameterNotNullStandardMessage("modification", modification);
		if (modification.getEntry() == null || Objects.equals(modification.getEntry().getQuantity(), Long.valueOf(0L)))
		{
			// nothing to merge
			return;
		}
		ServicesUtil.validateParameterNotNullStandardMessage("parameter", parameter);
		if (parameter.isCreateNewEntry())
		{
			return;
		}
		final AbstractOrderModel cart = modification.getEntry().getOrder();
		if (cart == null)
		{
			// The entry is not in cart (most likely it's a stub)
			return;
		}
		final AbstractOrderEntryModel mergeTarget = getEntryMergeStrategy().getEntryToMerge(cart.getEntries(),
				modification.getEntry());
		if (mergeTarget == null)
		{
			if (parameter.getEntryNumber() != CommerceCartParameter.DEFAULT_ENTRY_NUMBER)
			{
				throw new CommerceCartModificationException("The new entry can not be merged into the entry #"
						+ parameter.getEntryNumber() + ". Give a correct value or " + CommerceCartParameter.DEFAULT_ENTRY_NUMBER
						+ " to accept any suitable entry.");
			}
		}
		else
		{
			if (modification.getEntry().getCustomPrice().doubleValue() == mergeTarget.getCustomPrice().doubleValue())
			{
				b2bUserChecking(modification, cart, mergeTarget);
				// Merge the original entry into the merge target and remove the original entry.
				final Map<Integer, Long> entryQuantities = new HashMap<>(2);
				entryQuantities.put(mergeTarget.getEntryNumber(),
						Long.valueOf(modification.getEntry().getQuantity().longValue() + mergeTarget.getQuantity().longValue()));
				entryQuantities.put(modification.getEntry().getEntryNumber(), Long.valueOf(0L));
				getCartService().updateQuantities(parameter.getCart(), entryQuantities);
				//Commend for reset all values so promotion is resetting.use calculateTotals
				try
				{
					getCalculationService().calculateTotals(cart, true);
				}
				catch (final CalculationException e)
				{
					LOG.error(e.getMessage());
					LOG.info(e.getMessage());
					LOG.info(e);
				}
				modification.setCartItemCount(modification.getCartItemCount() - 1);
				modification.setEntry(mergeTarget);
			}
		}

	}

	/**
	 * This method is used to check user is b2b or not
	 *
	 * @param modification
	 * @param cart
	 * @param mergeTarget
	 * @throws WoolliesCartModificationException
	 */
	private void b2bUserChecking(final CommerceCartModification modification, final AbstractOrderModel cart,
			final AbstractOrderEntryModel mergeTarget) throws WoolliesCartModificationException
	{
		if (cart.getUser() instanceof CorporateB2BCustomerModel)
		{
			final long totalQuantities = modification.getEntry().getQuantity().longValue() + mergeTarget.getQuantity().longValue();
			if (totalQuantities > getConfigurationService().getConfiguration().getLong("cart.limit.for.B2B.user", 9999))
			{
				throw new WoolliesCartModificationException("40023");
			}
		}
	}

	/**
	 * This method is used to get total price
	 *
	 * @param cartModel
	 * @return totalPrice
	 */
	public Double getCartTotalPrice(final CartModel cartModel)
	{

		double totalPrice = 0.0;
		Double returnPrice = null;
		final List<AbstractOrderEntryModel> listEntry = cartModel.getEntries();
		if (listEntry != null && !listEntry.isEmpty())
		{
			for (final AbstractOrderEntryModel abstractOrderEntryModel : listEntry)
			{
				final double newtotalPrice = abstractOrderEntryModel.getTotalPrice().doubleValue();
				totalPrice = totalPrice + newtotalPrice;
				returnPrice = Double.valueOf(totalPrice);
			}
		}

		return returnPrice;
	}

	/**
	 * This method is used to get card count
	 *
	 * @param cartModel
	 * @return returnCount
	 */
	public String getCartCount(final CartModel cartModel)
	{
		long sumQty = 0;
		Long returnCount = null;
		final List<AbstractOrderEntryModel> listEntry = cartModel.getEntries();
		if (listEntry != null)
		{
			for (final AbstractOrderEntryModel abstractOrderEntryModel : listEntry)
			{
				final long toQty = abstractOrderEntryModel.getQuantity().longValue();
				sumQty = sumQty + toQty;
				returnCount = Long.valueOf(sumQty);
			}
		}
		if (returnCount != null)
		{
			return returnCount.toString();
		}
		else
		{
			return null;
		}

	}

	public int getLineItem(final CartModel cartModel)
	{
		final List<AbstractOrderEntryModel> listEntry = cartModel.getEntries();
		return listEntry.size();
	}


	/**
	 * @return the calculationService
	 */
	public CalculationService getCalculationService()
	{
		return calculationService;
	}

	/**
	 * @param calculationService
	 *           the calculationService to set
	 */
	public void setCalculationService(final CalculationService calculationService)
	{
		this.calculationService = calculationService;
	}

}
