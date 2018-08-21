/**
 *
 */
package de.hybris.wooliesegiftcard.service.delivery;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import de.hybris.platform.commerceservices.delivery.impl.DefaultDeliveryService;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;

import java.util.List;


/**
 * @author 668982 This class is used to maintain default delivery service
 */
public class WooliesDefaultDeliveryService extends DefaultDeliveryService
{
	/**
	 * This method is used toget supported delivery mode list for order
	 * 
	 * @param abstractOrder
	 * @return
	 */
	@Override
	public List<DeliveryModeModel> getSupportedDeliveryModeListForOrder(final AbstractOrderModel abstractOrder)
	{
		validateParameterNotNull(abstractOrder, "abstractOrder model cannot be null");
		return getDeliveryModeLookupStrategy().getSelectableDeliveryModesForOrder(abstractOrder);
	}

}
