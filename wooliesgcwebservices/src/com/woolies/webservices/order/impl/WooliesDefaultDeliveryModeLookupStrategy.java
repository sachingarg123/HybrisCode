/**
 *
 */
package com.woolies.webservices.order.impl;

import de.hybris.platform.commerceservices.strategies.impl.DefaultDeliveryModeLookupStrategy;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.deliveryzone.model.ZoneDeliveryModeValueModel;
import de.hybris.wooliesegiftcard.service.impl.WooliesDefaultCartService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * @author 668982
 *
 */
public class WooliesDefaultDeliveryModeLookupStrategy extends DefaultDeliveryModeLookupStrategy
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

	@Override
	public List<DeliveryModeModel> getSelectableDeliveryModesForOrder(final AbstractOrderModel abstractOrderModel)
	{
		if (isPickUpOnlyOrder(abstractOrderModel))
		{
			return new ArrayList<>(getPickupDeliveryModeDao().findPickupDeliveryModesForAbstractOrder(abstractOrderModel));
		}
		else
		{
			final List<DeliveryModeModel> deliveryModes = new ArrayList<>();
			final Collection<ZoneDeliveryModeValueModel> zoneDeliveryModes = new ArrayList<>();
			final CurrencyModel currency = abstractOrderModel.getCurrency();
			if (currency != null)
			{
				zoneDeliveryModes.addAll(getCartService().getdeliveryOptionsForCart(abstractOrderModel));
				for (final ZoneDeliveryModeValueModel zoneDeliveryModeValueModel : zoneDeliveryModes)
				{
					deliveryModes.add(zoneDeliveryModeValueModel.getDeliveryMode());
				}
			}
			return deliveryModes;
		}
	}
}
