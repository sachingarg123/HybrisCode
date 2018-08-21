/**
 *
 */
package de.hybris.wooliesegiftcard.facades.order.converters.populator;

import de.hybris.platform.commercefacades.order.converters.populator.CartPopulator;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.DeliveryModeData;
import de.hybris.platform.commercefacades.product.data.PriceData;
import de.hybris.platform.commercefacades.product.data.PriceDataType;
import de.hybris.platform.commercefacades.product.data.PromotionData;
import de.hybris.platform.commercefacades.product.data.PromotionResultData;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.promotionengineservices.model.RuleBasedOrderAdjustTotalActionModel;
import de.hybris.platform.promotionengineservices.model.RuleBasedOrderChangeDeliveryModeActionModel;
import de.hybris.platform.promotionengineservices.model.RuleBasedOrderEntryAdjustActionModel;
import de.hybris.platform.promotions.model.AbstractPromotionActionModel;
import de.hybris.platform.promotions.model.PromotionResultModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.util.DiscountValue;
import de.hybris.wooliesegiftcard.core.enums.UserDataType;
import de.hybris.wooliesegiftcard.facades.order.impl.WooliesDefaultCartFacade;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;


/**
 * @author 668982 This class is used to populate cart details
 */
public class WooliesCartPopulator extends CartPopulator
{
	private ConfigurationService configurationService;
	private CommonI18NService commonI18NService;
	private WooliesDefaultCartFacade wooliesDefaultCartFacade;

	/**
	 * @return the wooliesDefaultCartFacade
	 */
	public WooliesDefaultCartFacade getWooliesDefaultCartFacade()
	{
		return wooliesDefaultCartFacade;
	}

	/**
	 * @param wooliesDefaultCartFacade
	 *           the wooliesDefaultCartFacade to set
	 */
	public void setWooliesDefaultCartFacade(final WooliesDefaultCartFacade wooliesDefaultCartFacade)
	{
		this.wooliesDefaultCartFacade = wooliesDefaultCartFacade;
	}

	/**
	 * @return the commonI18NService
	 */
	@Override
	public CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	/**
	 * @param commonI18NService
	 *           the commonI18NService to set
	 */
	@Override
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

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
	 * This method is used to populate cart details
	 *
	 * @param source
	 *           user cart model
	 * @param target
	 *           user cart data
	 */
	@Override
	public void populate(final CartModel source, final CartData target)
	{
		target.setIsMinimalPackage(source.getIsMinimalPackage());
		final List<DeliveryModeData> deliveryModes = new ArrayList<>();
		getWooliesDefaultCartFacade().getDeliveryModeData(deliveryModes, source);
		target.setDeliveryModes(deliveryModes);
		if (source.getDeliveryCost() != null)
		{
			setDeliveryCost(source, target);
		}
		final CustomerModel customerModel = (CustomerModel) source.getUser();

		final UserDataType userdataType = customerModel.getCustomerType();
		setMinMaxQty(target, userdataType);

		target.setIsBulkOrder(source.getIsBulkOrder());
		target.setCartGiftCardCount(getCardCount(source));
		target.setCartTotal(source.getTotalPrice());
		target.setCartItemCount(getLineItem(source));
		final Set<PromotionResultModel> promotionResults = source.getAllPromotionResults();
		final List<DiscountValue> globalDiscounts = source.getGlobalDiscountValues();
		final List<PromotionResultData> allPromotions = new ArrayList<>();
		allPromotions.addAll(target.getAppliedProductPromotions());
		allPromotions.addAll(target.getAppliedOrderPromotions());
		boolean isTotalDiscountSets = false;
		for (final PromotionResultModel promotionResultModel : promotionResults)
		{
			isTotalDiscountSets = setPromotions(source, target, globalDiscounts, allPromotions, isTotalDiscountSets,
					promotionResultModel);
		}
		if (source.getSubtotal() != null)
		{
			final PriceData subTotalPriceData = createPrice(source, source.getSubtotal());
			target.setSubTotal(subTotalPriceData);
		}
		if (target.getTotalDiscounts().getValue().doubleValue() > 0)
		{
			target.setTotalPrice(getPriceDataFactory().create(PriceDataType.BUY, target.getTotalPrice().getValue(),
					source.getCurrency().getIsocode()));
		}
	}

	/**
	 * applied promotions for the cart
	 *
	 * @param source
	 *           user cart
	 * @param target
	 *           user cart
	 * @param globalDiscounts
	 *           global Discounts
	 * @param allPromotions
	 *           cart promotions
	 * @param isTotalDiscountSets
	 *           isTotalDiscountSets
	 * @param promotionResultModel
	 *           promotionResultModel
	 * @return boolean
	 */
	private boolean setPromotions(final CartModel source, final CartData target, final List<DiscountValue> globalDiscounts,
			final List<PromotionResultData> allPromotions, boolean isTotalDiscountSets,
			final PromotionResultModel promotionResultModel)
	{
		final Collection<AbstractPromotionActionModel> actions = promotionResultModel.getActions();
		for (final AbstractPromotionActionModel abstractPromotionActionModel : actions)
		{
			if (abstractPromotionActionModel instanceof RuleBasedOrderChangeDeliveryModeActionModel)
			{
				final RuleBasedOrderChangeDeliveryModeActionModel deliveryChangeModel = (RuleBasedOrderChangeDeliveryModeActionModel) abstractPromotionActionModel;
				isTotalDiscountSets = ruleBasedOrderChangeDeliveryModeActionModel(source, target, allPromotions, isTotalDiscountSets,
						promotionResultModel, deliveryChangeModel);
				if (isTotalDiscountSets)
				{
					break;
				}
			}
			else if (abstractPromotionActionModel instanceof RuleBasedOrderAdjustTotalActionModel)
			{
				isTotalDiscountSets = ruleBasedOrderAdjustTotalActionModel(source, globalDiscounts, allPromotions,
						isTotalDiscountSets, promotionResultModel, abstractPromotionActionModel);
				if (isTotalDiscountSets)
				{
					break;
				}
			}
			else if (abstractPromotionActionModel instanceof RuleBasedOrderEntryAdjustActionModel)
			{
				ruleBasedOrderEntryAdjustActionModel(allPromotions, promotionResultModel, abstractPromotionActionModel);
			}
		}
		return isTotalDiscountSets;
	}

	/**
	 * set min max qty to cart
	 *
	 * @param target
	 *           user cart
	 * @param userdataType
	 *           userdataType
	 */
	private void setMinMaxQty(final CartData target, final UserDataType userdataType)
	{
		if (null != userdataType)
		{
			if (userdataType == UserDataType.B2C)
			{
				target.setMaxQty(getConfigurationService().getConfiguration().getString("max.qty.B2C", "2"));
				target.setMinQty(getConfigurationService().getConfiguration().getString("min.qty.B2C", "1"));
			}
			else if (userdataType == UserDataType.B2B)
			{
				target.setMaxQty(getConfigurationService().getConfiguration().getString("max.qty.B2B", "0"));
				target.setMinQty(getConfigurationService().getConfiguration().getString("min.qty.B2B", "1"));
			}
			else if (userdataType == UserDataType.MEM)
			{
				target.setMaxQty(getConfigurationService().getConfiguration().getString("max.qty.member", "2"));
				target.setMinQty(getConfigurationService().getConfiguration().getString("min.qty.member", "1"));
			}
		}
		else
		{
			target.setMaxQty(getConfigurationService().getConfiguration().getString("max.qty.B2C", "2"));
			target.setMinQty(getConfigurationService().getConfiguration().getString("min.qty.B2C", "1"));

		}
	}

	/**
	 * set delivery cost to cart
	 *
	 * @param source
	 *           user cart
	 * @param target
	 *           user cart
	 */
	private void setDeliveryCost(final CartModel source, final CartData target)
	{
		final CurrencyModel currency = source.getCurrency();
		target.setDeliveryCost(getPriceDataFactory().create(PriceDataType.BUY,
				BigDecimal.valueOf(source.getDeliveryCost().doubleValue()), currency.getIsocode()));
		if (source.getDeliveryCost().doubleValue() > 0)
		{

			final double taxCost = getCommonI18NService().roundCurrency(
					source.getDeliveryCost().doubleValue()
							/ ((getConfigurationService().getConfiguration().getDouble("tax.deliveryCost", 10)) + 1),
					currency.getDigits().intValue());
			target.setTotalTax(getPriceDataFactory().create(PriceDataType.BUY, BigDecimal.valueOf(taxCost), currency.getIsocode()));
		}
	}

	/**
	 * set coupon code to cart data
	 *
	 * @param allPromotions
	 *           applied promotions
	 * @param promotionResultModel
	 *           promo result
	 * @param abstractPromotionActionModel
	 *           abstractPromotionActionModel
	 */
	private void ruleBasedOrderEntryAdjustActionModel(final List<PromotionResultData> allPromotions,
			final PromotionResultModel promotionResultModel, final AbstractPromotionActionModel abstractPromotionActionModel)
	{
		final RuleBasedOrderEntryAdjustActionModel actionModel = (RuleBasedOrderEntryAdjustActionModel) abstractPromotionActionModel;
		if (CollectionUtils.isNotEmpty(actionModel.getUsedCouponCodes()))
		{
			final Collection<String> couponCodes = actionModel.getUsedCouponCodes();
			for (final String coupon : couponCodes)
			{
				for (final PromotionResultData promotionResultData : allPromotions)
				{
					setCouponCodes(promotionResultModel, coupon, promotionResultData);
				}
			}
		}
	}

	/**
	 * This Method sets the Coupon codes
	 *
	 * @param promotionResultModel
	 * @param coupon
	 * @param promotionResultData
	 */
	private void setCouponCodes(final PromotionResultModel promotionResultModel, final String coupon,
			final PromotionResultData promotionResultData)
	{
		final PromotionData promotionData = promotionResultData.getPromotionData();
		if (promotionData.getCode().equals(promotionResultModel.getPromotion().getCode()))
		{
			promotionResultData.setCouponCode(coupon);
		}
	}

	/**
	 * set promotions to cart
	 *
	 * @param source
	 *           source
	 * @param globalDiscounts
	 *           globalDiscounts
	 * @param allPromotions
	 *           allPromotions
	 * @param isTotalDiscountSets
	 *           isTotalDiscountSet
	 * @param promotionResultModel
	 *           promotionResultModel
	 * @param abstractPromotionActionModel
	 *           abstractPromotionActionModel
	 * @return boolean
	 */
	private boolean ruleBasedOrderAdjustTotalActionModel(final CartModel source, final List<DiscountValue> globalDiscounts,
			final List<PromotionResultData> allPromotions, boolean isTotalDiscountSets,
			final PromotionResultModel promotionResultModel, final AbstractPromotionActionModel abstractPromotionActionModel)
	{
		final RuleBasedOrderAdjustTotalActionModel actionModel = (RuleBasedOrderAdjustTotalActionModel) abstractPromotionActionModel;
		for (final DiscountValue discountModel : globalDiscounts)
		{
			if (discountModel.getCode().equals(actionModel.getGuid()))
			{
				isTotalDiscountSets = promotionResult(source, allPromotions, isTotalDiscountSets, promotionResultModel, actionModel,
						discountModel);
			}
			if (isTotalDiscountSets)
			{
				break;
			}
		}
		return isTotalDiscountSets;
	}

	/**
	 * set promotions to cart
	 *
	 * @param source
	 *           source
	 * @param allPromotions
	 *           allPromotions
	 * @param isTotalDiscountSets
	 *           isTotalDiscountSet
	 * @param promotionResultModelk
	 *           promotionResultModel
	 * @param actionModel
	 *           actionModel
	 * @param discountModel
	 *           discountModel
	 * @return boolean
	 */
	private boolean promotionResult(final CartModel source, final List<PromotionResultData> allPromotions,
			boolean isTotalDiscountSets, final PromotionResultModel promotionResultModelk,
			final RuleBasedOrderAdjustTotalActionModel actionModel, final DiscountValue discountModel)
	{
		for (final PromotionResultData promotionResultData1 : allPromotions)
		{
			final PromotionData promotionData2 = promotionResultData1.getPromotionData();
			if (promotionData2.getCode().equals(promotionResultModelk.getPromotion().getCode()))
			{
				promotionResultData1.setTotalDiscount(getPriceDataFactory().create(PriceDataType.BUY,
						BigDecimal.valueOf(discountModel.getAppliedValue()), source.getCurrency().getIsocode()));
				isTotalDiscountSets = true;
				final Collection<String> couponCodes = actionModel.getUsedCouponCodes();
				if (couponCodes != null)
				{
					setCouponCodesInPromotion(promotionResultData1, couponCodes);
				}
				break;
			}
		}
		return isTotalDiscountSets;
	}

	/**
	 * This method is used to setCouponCodesInPromotion
	 *
	 * @param promotionResultData1
	 * @param couponCodes
	 */
	private void setCouponCodesInPromotion(final PromotionResultData promotionResultData1, final Collection<String> couponCodes)
	{
		for (final String coupon : couponCodes)
		{
			promotionResultData1.setCouponCode(coupon);
		}
	}


	/**
	 * change delivery mode promotion
	 *
	 * @param source
	 *           source
	 * @param target
	 *           target
	 * @param allPromotions
	 *           allPromotions
	 * @param isTotalDiscountSets
	 *           isTotalDiscountSet
	 * @param promotionResultModela
	 *           promotionResultModel
	 * @param deliveryChangeModel
	 *           deliveryChangeModel
	 * @return boolean
	 */
	private boolean ruleBasedOrderChangeDeliveryModeActionModel(final CartModel source, final CartData target,
			final List<PromotionResultData> allPromotions, boolean isTotalDiscountSets,
			final PromotionResultModel promotionResultModela, final RuleBasedOrderChangeDeliveryModeActionModel deliveryChangeModel)
	{
		for (final PromotionResultData promotionResultData3 : allPromotions)
		{
			final PromotionData promotionData4 = promotionResultData3.getPromotionData();
			if (promotionData4.getCode().equals(promotionResultModela.getPromotion().getCode()))
			{

				promotionResultData3.setTotalDiscount(getPriceDataFactory().create(PriceDataType.BUY,
						deliveryChangeModel.getReplacedDeliveryCost(), source.getCurrency().getIsocode()));
				BigDecimal totalDiscountsa = target.getTotalDiscounts().getValue();
				totalDiscountsa = totalDiscountsa.add(deliveryChangeModel.getReplacedDeliveryCost());
				target.setTotalDiscounts(
						getPriceDataFactory().create(PriceDataType.BUY, totalDiscountsa, source.getCurrency().getIsocode()));
				isTotalDiscountSets = true;
				break;
			}
		}
		return isTotalDiscountSets;
	}

	/**
	 * This method is used to get total price for the cart
	 *
	 * @param cartModel
	 *           cartModel
	 * @return returnPrice the total price
	 */
	public Double getTotalPrice(final CartModel cartModel)
	{

		double totalPrice = 0.0;
		Double returnPrice = Double.valueOf(totalPrice);
		final List<AbstractOrderEntryModel> listEntry = cartModel.getEntries();
		for (final AbstractOrderEntryModel abstractOrderEntryModel : listEntry)
		{
			final double newtotalPrice = abstractOrderEntryModel.getTotalPrice().doubleValue();
			totalPrice = totalPrice + newtotalPrice;
			returnPrice = Double.valueOf(totalPrice);
		}

		return returnPrice;
	}

	/**
	 * This method is used to get card count
	 *
	 * @param cartModel
	 *           cartModel
	 * @return the number of items in the cart
	 */
	public String getCardCount(final CartModel cartModel)
	{
		long sumQty = 0;
		final long returnAmt = 0;
		Long returnCount = Long.valueOf(returnAmt);
		final List<AbstractOrderEntryModel> listEntry = cartModel.getEntries();
		for (final AbstractOrderEntryModel abstractOrderEntryModel : listEntry)
		{
			final long toQty = abstractOrderEntryModel.getQuantity().longValue();
			sumQty = sumQty + toQty;
			returnCount = Long.valueOf(sumQty);
		}

		return returnCount.toString();
	}

	/**
	 * To get line item for the cart
	 *
	 * @param cartModel
	 *           cartModel
	 * @return the cart entries
	 */
	public int getLineItem(final CartModel cartModel)
	{
		return cartModel.getEntries().size();
	}

}
