/**
 *
 */
package de.hybris.wooliesegiftcard.facades.populators;

import de.hybris.platform.commercefacades.order.converters.populator.PromotionResultPopulator;
import de.hybris.platform.commercefacades.product.PriceDataFactory;
import de.hybris.platform.commercefacades.product.data.PriceDataType;
import de.hybris.platform.commercefacades.product.data.PromotionResultData;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.promotions.model.AbstractPromotionActionModel;
import de.hybris.platform.promotions.model.PromotionOrderEntryConsumedModel;
import de.hybris.platform.promotions.model.PromotionResultModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.util.DiscountValue;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.fest.util.Collections;


/**
 * @author 668982 This class is used to populate promotion result of the product
 */
public class WooliesPromotionResultPopulator extends PromotionResultPopulator
{
	private PriceDataFactory priceDataFactory;
	private CommonI18NService commonI18NService;

	/**
	 * @return the commonI18NService
	 */
	public CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	/**
	 * @param commonI18NService
	 *           the commonI18NService to set
	 */
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	/**
	 * @return the priceDataFactory
	 */
	public PriceDataFactory getPriceDataFactory()
	{
		return priceDataFactory;
	}

	/**
	 * @param priceDataFactory
	 *           the priceDataFactory to set
	 */
	public void setPriceDataFactory(final PriceDataFactory priceDataFactory)
	{
		this.priceDataFactory = priceDataFactory;
	}

	/**
	 * populate promotion result of the product
	 *
	 * @param source
	 *           PromotionResultModel
	 * @param target
	 */
	@Override
	public void populate(final PromotionResultModel source, final PromotionResultData target)
	{
		final CurrencyModel currency = source.getOrder().getCurrency();
		if (!Collections.isEmpty(source.getConsumedEntries()))
		{
			setTotalDiscount(source, target, currency);
		}
		else
		{
			final List<DiscountValue> discountList = source.getOrder().getGlobalDiscountValues();

			if (discountList != null && !discountList.isEmpty())
			{
				setTotalDisc(source, target, currency, discountList);

			}
		}

	}

	/**
	 * set total discount for the promotion
	 *
	 * @param source
	 *           PromotionResultModel
	 * @param target
	 *           PromotionResultData
	 * @param currency
	 *           CurrencyModel
	 * @param discountList
	 *           List<DiscountValue>
	 */
	private void setTotalDisc(final PromotionResultModel source, final PromotionResultData target, final CurrencyModel currency,
			final List<DiscountValue> discountList)
	{
		boolean isDiscountFound = false;
		for (final DiscountValue discount : discountList)
		{
			final Collection<AbstractPromotionActionModel> actions = source.getActions();
			if (!Collections.isEmpty(actions))
			{
				isDiscountFound = setTotalDiscount(target, currency, isDiscountFound, discount, actions);
				if (isDiscountFound)
				{
					break;
				}
			}
		}
	}

	/**
	 * set total discount for the promotion
	 *
	 * @param target
	 *           PromotionResultData
	 * @param currency
	 *           CurrencyModel
	 * @param isDiscountFound
	 *           boolean
	 * @param discount
	 *           DiscountValue
	 * @param actions
	 *           Collection<AbstractPromotionActionModel>
	 * @return boolean
	 */
	private boolean setTotalDiscount(final PromotionResultData target, final CurrencyModel currency, boolean isDiscountFound,
			final DiscountValue discount, final Collection<AbstractPromotionActionModel> actions)
	{
		for (final AbstractPromotionActionModel abstractPromotionActionModel : actions)
		{
			if (abstractPromotionActionModel.getGuid().equals(discount.getCode()))
			{
				isDiscountFound = true;
				target.setTotalDiscount(getPriceDataFactory().create(PriceDataType.BUY, BigDecimal.valueOf(discount.getValue()),
						currency.getIsocode()));
				break;
			}
		}
		return isDiscountFound;
	}

	/**
	 * set total discount for the promotion
	 *
	 * @param source
	 *           PromotionResultModel
	 * @param target
	 *           PromotionResultData
	 * @param currency
	 *           CurrencyModel
	 */
	private void setTotalDiscount(final PromotionResultModel source, final PromotionResultData target,
			final CurrencyModel currency)
	{
		final Collection<PromotionOrderEntryConsumedModel> consumedEntries = source.getConsumedEntries();
		double totalDiscount = 0;
		for (final PromotionOrderEntryConsumedModel promotionOrderEntryConsumedModel : consumedEntries)
		{
			final Double adjustedUnitPrice = promotionOrderEntryConsumedModel.getAdjustedUnitPrice();
			final Double actualPrice = promotionOrderEntryConsumedModel.getOrderEntry().getBasePrice();
			final double discountPerUnit = actualPrice.doubleValue() - adjustedUnitPrice.doubleValue();
			totalDiscount += discountPerUnit * promotionOrderEntryConsumedModel.getOrderEntry().getQuantity().longValue();
		}
		final double roundedTotalDiscount = getCommonI18NService().roundCurrency(totalDiscount, currency.getDigits().intValue());
		target.setTotalDiscount(
				getPriceDataFactory().create(PriceDataType.BUY, BigDecimal.valueOf(roundedTotalDiscount), currency.getIsocode()));
	}
}
