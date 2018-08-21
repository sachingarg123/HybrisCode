/**
 *
 */
package de.hybris.wooliesegiftcard.facades.order.converters.populator;

import de.hybris.platform.commercefacades.order.converters.populator.CartModificationPopulator;
import de.hybris.platform.commercefacades.order.data.CartModificationData;
import de.hybris.platform.commercefacades.product.PriceDataFactory;
import de.hybris.platform.commercefacades.product.data.PriceDataType;
import de.hybris.platform.commerceservices.order.CommerceCartModification;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.wooliesegiftcard.facades.order.impl.WooliesDefaultCartFacade;

import java.math.BigDecimal;


/**
 * @author 668982 This class is used to populate card modification
 */
public class WooliesDefaultCartModificationPopulator extends CartModificationPopulator
{
	private PriceDataFactory priceDataFactory;

	private WooliesDefaultCartFacade wooliesDefaultCartFacade;

	/**
	 * This method is used to populate commerce card modification
	 * 
	 * @param source
	 * @param target
	 */
	@Override
	public void populate(final CommerceCartModification source, final CartModificationData target)
	{
		if (source.getCustomerPrice() != null)
		{
			target.setCustomerPrice(source.getCustomerPrice());
		}
		target.setIsAddtoCartDisable(source.getIsAddtoCartDisable());

		if (null != source.getCartGiftCardCount())
		{
			target.setCartGiftCardCount(source.getCartGiftCardCount());
		}
		target.setCartItemCount(source.getCartItemCount());
		if (null != source.getCartTotal())
		{
			target.setCartTotal(source.getCartTotal());
		}
		if (getWooliesDefaultCartFacade().getCart() != null)
		{
			final CartModel cart = getWooliesDefaultCartFacade().getCart();
			final String isocode = cart.getCurrency().getIsocode();
			if (cart.getTotalPrice() != null)
			{
				final BigDecimal totalPrice = BigDecimal.valueOf(cart.getTotalPrice().doubleValue());
				target.setTotalPrice(getPriceDataFactory().create(PriceDataType.BUY, totalPrice, isocode));
			}
			if (cart.getSubtotal() != null)
			{
				final BigDecimal subTotal = BigDecimal.valueOf(cart.getSubtotal().doubleValue());
				target.setSubTotal(getPriceDataFactory().create(PriceDataType.BUY, subTotal, isocode));
			}
			if (cart.getTotalDiscounts() != null)
			{
				final BigDecimal totalDiscount = BigDecimal.valueOf(cart.getTotalDiscounts().doubleValue());
				target.setTotalDiscounts(getPriceDataFactory().create(PriceDataType.BUY, totalDiscount, isocode));
			}

		}
		target.setAppliedOrderPromotions(getWooliesDefaultCartFacade().getSessionCart().getAppliedOrderPromotions());
		target.setAppliedProductPromotions(getWooliesDefaultCartFacade().getSessionCart().getAppliedProductPromotions());

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

}
