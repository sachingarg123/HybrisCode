/**
 *
 */
package de.hybris.wooliesegiftcard.facades.action.impl;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.promotionengineservices.action.impl.DefaultShippingActionStrategy;
import de.hybris.platform.promotionengineservices.model.RuleBasedOrderChangeDeliveryModeActionModel;
import de.hybris.platform.promotions.model.PromotionResultModel;
import de.hybris.platform.ruleengineservices.rao.AbstractRuleActionRAO;
import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.ShipmentRAO;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author 668982 This class is used to maintain default shipping action strategy details
 */
public class WooliesDefaultShippingActionStrategy extends DefaultShippingActionStrategy
{
	private static final Logger LOG = LoggerFactory.getLogger(WooliesDefaultShippingActionStrategy.class);

	/**
	 * This method is used to apply the promotion
	 *
	 * @param action
	 *           the AbstractRuleAction for the Promotion
	 * @return promotionResultModel the PromotionResultModel list
	 */
	@Override
	public List<PromotionResultModel> apply(final AbstractRuleActionRAO action)
	{
		if (!(action instanceof ShipmentRAO))
		{
			LOG.error("cannot apply {}, action is not of type ShipmentRAO, but {}", this.getClass().getSimpleName(), action);
			return Collections.emptyList();
		}
		else
		{
			return applyPromotion(action);
		}
	}


	/**
	 * This Method applies Promotion
	 *
	 * @param action
	 *           the AbstractRuleAction for the Promotion
	 * @return it returns List<PromotionResultModel>
	 */
	private List<PromotionResultModel> applyPromotion(final AbstractRuleActionRAO action)
	{
		final ShipmentRAO changeDeliveryMethodAction = (ShipmentRAO) action;
		if (!(changeDeliveryMethodAction.getAppliedToObject() instanceof CartRAO))
		{
			LOG.error("cannot apply {}, appliedToObject is not of type CartRAO, but {}", this.getClass().getSimpleName(),
					action.getAppliedToObject());
			return Collections.emptyList();
		}
		else
		{
			final PromotionResultModel promoResult = this.getPromotionActionService().createPromotionResult(action);
			if (promoResult == null)
			{
				LOG.error("cannot apply {}, promotionResult could not be created.", this.getClass().getSimpleName());
				return Collections.emptyList();
			}
			else
			{
				final AbstractOrderModel order = this.getPromotionResultUtils().getOrder(promoResult);
				if (Objects.isNull(order))
				{
					LOG.error("cannot apply {}, order or cart not found: {}", this.getClass().getSimpleName(), order);
					setPromoResult(promoResult);

					return Collections.emptyList();
				}
				else
				{
					return shippingPromotion(action, promoResult, order);
				}
			}
		}
	}


	/**
	 * This is used to set promo result
	 * 
	 * @param promoResult
	 */
	private void setPromoResult(final PromotionResultModel promoResult)
	{
		if (this.getModelService().isNew(promoResult))
		{
			this.getModelService().detach(promoResult);
		}
	}


	/**
	 * @param action
	 *           the AbstractRuleAction for the Promotion
	 * @param promoResult
	 *           the PromotionResultModel as a parameter
	 * @param order
	 *           the AbstractOrderModel
	 * @return List<PromotionResultModel> under this promotion
	 */
	private List<PromotionResultModel> shippingPromotion(final AbstractRuleActionRAO action,
			final PromotionResultModel promoResult, final AbstractOrderModel order)
	{
		final ShipmentRAO shipmentRAO = (ShipmentRAO) action;
		final DeliveryModeModel shipmentModel = this.getDeliveryModeForCode(shipmentRAO.getMode().getCode());
		if (shipmentModel == null)
		{
			LOG.error("Delivery Mode for code {} not found!", shipmentRAO.getMode());
			return Collections.emptyList();
		}
		else
		{
			if (order.getDeliveryMode() == null)
			{
				return Collections.emptyList();
			}
			else if (!order.getDeliveryMode().getCode().equals(shipmentModel.getCode()))
			{
				return Collections.emptyList();
			}
			else
			{
				final DeliveryModeModel shipmentModelToReplace = order.getDeliveryMode();
				order.setDeliveryMode(shipmentModel);
				final Double deliveryCostToReplace = order.getDeliveryCost();
				order.setDeliveryCost(Double.valueOf(0.0));
				final RuleBasedOrderChangeDeliveryModeActionModel actionModel = this.createPromotionAction(promoResult, action);
				this.handleActionMetadata(action, actionModel);
				actionModel.setDeliveryMode(shipmentModel);
				actionModel.setDeliveryCost(BigDecimal.valueOf(0.0));
				actionModel.setReplacedDeliveryMode(shipmentModelToReplace);
				actionModel.setReplacedDeliveryCost(BigDecimal.valueOf(deliveryCostToReplace.doubleValue()));
				this.getModelService().saveAll(new Object[]
				{ promoResult, actionModel, order });
				return Collections.singletonList(promoResult);
			}

		}
	}
}
