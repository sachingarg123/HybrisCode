/**
 *
 */
package com.woolies.webservices.order.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import de.hybris.platform.commerceservices.order.CommerceCartModification;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commerceservices.order.impl.DefaultCommerceUpdateCartEntryStrategy;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.media.impl.DefaultMediaDao;
import de.hybris.platform.servicelayer.media.impl.DefaultMediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.wooliesegiftcard.core.enums.UserDataType;
import de.hybris.wooliesegiftcard.service.impl.WooliesDefaultCartService;
import de.hybris.model.PersonalisationEGiftCardModel;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import com.woolies.webservices.rest.exceptions.WoolliesCartModificationException;


/**
 * @author 668982 This class is to maintain the update cart entry strategies functionalities
 */
public class WooliesDefaultCommerceUpdateCartEntryStrategy extends DefaultCommerceUpdateCartEntryStrategy
{
	private DefaultMediaService mediaService;
	private DefaultMediaDao mediaDao;
	private ModelService modelService;
	private WooliesDefaultCartService cartService;
	private ConfigurationService configurationService;

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
	 * @return the cartService
	 */
	@Override
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
	 * This method is used to update quantity for cartEntry
	 *
	 * @param parameters
	 * @return CommerceCartModification
	 * @throws CommerceCartModificationException
	 */
	@Override
	public CommerceCartModification updateQuantityForCartEntry(final CommerceCartParameter parameters)
			throws CommerceCartModificationException
	{
		beforeUpdateCartEntry(parameters);
		final CartModel cartModel = parameters.getCart();
		final long newQuantity = parameters.getQuantity();
		final long entryNumber = parameters.getEntryNumber();

		validateParameterNotNull(cartModel, "Cart model cannot be null");
		CommerceCartModification modification;

		final AbstractOrderEntryModel entryToUpdate = getEntryForNumber(cartModel, (int) entryNumber);
		validateEntryBeforeModification(newQuantity, entryToUpdate);
		setCustomerPrice(parameters, entryToUpdate);
		final Integer maxOrderQuantity = entryToUpdate.getProduct().getMaxOrderQuantity();
		// Work out how many we want to add (could be negative if we are
		// removing items)
		final long quantityToAdd = newQuantity - entryToUpdate.getQuantity().longValue();

		// So now work out what the maximum allowed to be added is (note that
		// this may be negative!)
		final long actualAllowedQuantityChange = getAllowedCartAdjustmentForProduct(cartModel, entryToUpdate.getProduct(),
				quantityToAdd, entryToUpdate.getDeliveryPointOfService());

		long totalQuantities = 0;
		final List<AbstractOrderEntryModel> entries = cartModel.getEntries();
		for (final AbstractOrderEntryModel abstractOrderEntryModel : entries)
		{
			if (abstractOrderEntryModel.equals(entryToUpdate))
			{
				totalQuantities += abstractOrderEntryModel.getQuantity().longValue() + actualAllowedQuantityChange;
			}
			else
			{
				totalQuantities += abstractOrderEntryModel.getQuantity().longValue();
			}
		}
		final CustomerModel customerModel = (CustomerModel) cartModel.getUser();
		cartLimitCheck(totalQuantities, customerModel);

		//Now do the actual cartModification
		modification = modifyEntry(cartModel, entryToUpdate, actualAllowedQuantityChange, newQuantity, maxOrderQuantity);
		setIsAddtoCartDisable(modification, totalQuantities, customerModel);
		afterUpdateCartEntry(parameters, modification);
		//checking whether product is egit card or not, if yes then update the PID accordingly.
		if (entryToUpdate.getProduct().getIsEGiftCard())
		{
			if (CollectionUtils.isNotEmpty(entryToUpdate.getPersonalisationDetail()) && newQuantity != 0)
			{
				getCartService().generatePIDForEntryModel(entryToUpdate);
			}
			else
			{
				final List<PersonalisationEGiftCardModel> personalisation = entryToUpdate.getPersonalisationDetail();
				if (CollectionUtils.isNotEmpty(personalisation))
				{
					getModelService().removeAll(personalisation);
				}
			}
		}

		modification.setCartTotal(getTotalPrice(parameters.getCart()));
		modification.setCartItemCount(getLineItem(parameters.getCart()));
		modification.setCartGiftCardCount(getCardCount(parameters.getCart()));

		return modification;

	}

	/**
	 * @param parameters
	 * @param entryToUpdate
	 * @throws CommerceCartModificationException
	 */
	private void setCustomerPrice(final CommerceCartParameter parameters, final AbstractOrderEntryModel entryToUpdate)
			throws CommerceCartModificationException
	{
		if (parameters.getCustomerPrice() == null && entryToUpdate.getCustomPrice() != null)
		{
			parameters.setCustomerPrice(entryToUpdate.getCustomPrice());
		}
		else if (parameters.getCustomerPrice() != null && parameters.getCustomerPrice().doubleValue() < 0)
		{
			throw new CommerceCartModificationException("New price must not be less than zero");
		}
		else if (parameters.getCustomerPrice() != entryToUpdate.getBasePrice())
		{
			entryToUpdate.setCustomPrice(parameters.getCustomerPrice());
			entryToUpdate.setBasePrice(parameters.getCustomerPrice());
		}
	}

	/**
	 * @param totalQuantities
	 * @param customerModel
	 * @throws WoolliesCartModificationException
	 */
	private void cartLimitCheck(long totalQuantities, final CustomerModel customerModel) throws WoolliesCartModificationException
	{
		if (null == customerModel.getCustomerType() || (null != customerModel.getCustomerType()
				&& customerModel.getCustomerType().getCode().equals(UserDataType.B2C.getCode())
				|| customerModel.getCustomerType().getCode().equals(UserDataType.MEM.getCode())))
		{
			if (totalQuantities > configurationService.getConfiguration().getLong("cart.limit.for.user", 2))
			{
				throw new WoolliesCartModificationException("40013");
			}

		}
		else
		{
			if (totalQuantities > configurationService.getConfiguration().getLong("cart.limit.for.B2B.user", 9999))
			{
				throw new WoolliesCartModificationException("40023");
			}
		}
	}

	/**
	 * @param modification
	 * @param totalQuantities
	 * @param customerModel
	 */
	private void setIsAddtoCartDisable(CommerceCartModification modification, long totalQuantities,
			final CustomerModel customerModel)
	{
		if (null == customerModel.getCustomerType() || (null != customerModel.getCustomerType()
				&& customerModel.getCustomerType().getCode().equals(UserDataType.B2C.getCode())
				|| customerModel.getCustomerType().getCode().equals(UserDataType.MEM.getCode())))
		{
			if (totalQuantities < configurationService.getConfiguration().getLong("cart.limit.for.user", 2))
			{
				modification.setIsAddtoCartDisable(Boolean.TRUE);
			}
			else
			{
				modification.setIsAddtoCartDisable(Boolean.FALSE);
			}
		}
		else
		{
			if (totalQuantities < configurationService.getConfiguration().getLong("cart.limit.for.B2B.user", 9999))
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
	 * This method is used to get total price
	 * 
	 * @param cartModel
	 * @return totalPrice
	 */
	public Double getTotalPrice(final CartModel cartModel)
	{

		double totalPrice = 0.0;
		Double returnPrice = totalPrice;
		final List<AbstractOrderEntryModel> listEntry = cartModel.getEntries();
		for (final AbstractOrderEntryModel abstractOrderEntryModel : listEntry)
		{
			final double newtotalPrice = abstractOrderEntryModel.getTotalPrice().doubleValue();
			totalPrice = totalPrice + newtotalPrice;
			returnPrice = totalPrice;
		}

		return returnPrice;
	}

	/**
	 * This method is used to get CardCount
	 * 
	 * @param cartModel
	 * @return returnCount
	 */
	public String getCardCount(final CartModel cartModel)
	{
		long sumQty = 0;
		final long returnAmt = 0;
		Long returnCount = returnAmt;
		final List<AbstractOrderEntryModel> listEntry = cartModel.getEntries();
		for (final AbstractOrderEntryModel abstractOrderEntryModel : listEntry)
		{
			final long toQty = abstractOrderEntryModel.getQuantity().longValue();
			sumQty = sumQty + toQty;
			returnCount = sumQty;
		}

		return returnCount.toString();
	}

	/**
	 * This method is used to get line item for the cart
	 * 
	 * @param cartModel
	 * @return
	 */
	public int getLineItem(final CartModel cartModel)
	{
		final List<AbstractOrderEntryModel> listEntry = cartModel.getEntries();
		return listEntry.size();
	}


}
