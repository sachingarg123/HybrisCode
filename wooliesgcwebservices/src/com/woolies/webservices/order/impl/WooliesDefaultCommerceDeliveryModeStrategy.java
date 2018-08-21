/**
 *
 */
package com.woolies.webservices.order.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import de.hybris.platform.commerceservices.order.impl.DefaultCommerceDeliveryModeStrategy;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.deliveryzone.model.ZoneDeliveryModeValueModel;
import de.hybris.platform.promotionengineservices.model.RuleBasedOrderChangeDeliveryModeActionModel;
import de.hybris.platform.promotions.model.AbstractPromotionActionModel;
import de.hybris.platform.promotions.model.PromotionResultModel;
import de.hybris.wooliesegiftcard.service.impl.WooliesDefaultCartService;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;


/**
 * @author 668982 This class is used maintain commerce deliver mode strategy or the cart
 */
public class WooliesDefaultCommerceDeliveryModeStrategy extends DefaultCommerceDeliveryModeStrategy
{
	private WooliesDefaultCartService cartService;

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
	 * This method is used to set delivery mode for the cart
	 *
	 * @param parameter
	 * @return
	 */
	@Override
	public boolean setDeliveryMode(final CommerceCheckoutParameter parameter)
	{
		final DeliveryModeModel deliveryModeModel = parameter.getDeliveryMode();
		final CartModel cartModel = parameter.getCart();

		validateParameterNotNull(cartModel, "Cart model cannot be null");
		validateParameterNotNull(deliveryModeModel, "Delivery mode model cannot be null");

		cartModel.setDeliveryMode(deliveryModeModel);
		getModelService().save(cartModel);
		final CommerceCartParameter commerceCartParameter = new CommerceCartParameter();
		commerceCartParameter.setEnableHooks(true);
		commerceCartParameter.setCart(cartModel);
		getCommerceCartCalculationStrategy().calculateCart(commerceCartParameter);
		final DeliveryModeModel presentDeliveryMode = cartModel.getDeliveryMode();
		if (presentDeliveryMode != null && !deliveryModeModel.getCode().equals(presentDeliveryMode.getCode()))
		{
			cartModel.setDeliveryMode(deliveryModeModel);

			final Collection<ZoneDeliveryModeValueModel> deliveryModesModel = getCartService().getdeliveryOptionsForCart(cartModel);
			if (CollectionUtils.isNotEmpty(deliveryModesModel))
			{
				setDeliveryDetails(cartModel, deliveryModesModel);
			}


			final Set<PromotionResultModel> promotionResult = cartModel.getAllPromotionResults();
			final Set<PromotionResultModel> promotionResult1 = new HashSet(promotionResult);
			for (final PromotionResultModel promotionResultModel : promotionResult)
			{
				final Collection<AbstractPromotionActionModel> actions = promotionResultModel.getActions();
				removePromotionDetails(cartModel, presentDeliveryMode, promotionResult1, promotionResultModel, actions);
			}
			getModelService().save(cartModel);
		}
		return true;
	}

	/**
	 * This method is used to remove promotion details
	 *
	 * @param cartModel
	 * @param presentDeliveryMode
	 * @param promotionResult1
	 * @param promotionResultModel
	 * @param actions
	 */
	private void removePromotionDetails(final CartModel cartModel, final DeliveryModeModel presentDeliveryMode,
			final Set<PromotionResultModel> promotionResult1, final PromotionResultModel promotionResultModel,
			final Collection<AbstractPromotionActionModel> actions)
	{
		for (final AbstractPromotionActionModel abstractPromotionActionModel : actions)
		{
			if (abstractPromotionActionModel instanceof RuleBasedOrderChangeDeliveryModeActionModel)
			{
				final RuleBasedOrderChangeDeliveryModeActionModel deliveryModeAction = (RuleBasedOrderChangeDeliveryModeActionModel) abstractPromotionActionModel;
				if (deliveryModeAction.getDeliveryMode().getCode().equals(presentDeliveryMode.getCode()))
				{
					promotionResult1.remove(promotionResultModel);
					cartModel.setAllPromotionResults(promotionResult1);
					break;
				}
			}
		}
	}

	/**
	 * This method is used to set delevry details
	 *
	 * @param cartModel
	 * @param deliveryModesModel
	 */
	private void setDeliveryDetails(final CartModel cartModel, final Collection<ZoneDeliveryModeValueModel> deliveryModesModel)
	{
		for (final ZoneDeliveryModeValueModel zoneDeliveryModeValueModel : deliveryModesModel)
		{
			if (zoneDeliveryModeValueModel.getDeliveryMode().equals(cartModel.getDeliveryMode()))
			{
				cartModel.setTotalPrice(cartModel.getTotalPrice() + zoneDeliveryModeValueModel.getValue());
				cartModel.setDeliveryCost(zoneDeliveryModeValueModel.getValue());
				cartModel.setSubtotal(cartModel.getSubtotal() + zoneDeliveryModeValueModel.getValue());
				break;
			}
		}
	}


}
