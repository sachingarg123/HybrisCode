/**
 *
 */
package de.hybris.wooliesegiftcard.facades.populators;

import de.hybris.platform.commercefacades.order.converters.populator.DeliveryModePopulator;
import de.hybris.platform.commercefacades.order.data.DeliveryModeData;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;


/**
 * @author 653930 This class is used to populate deliver mode
 */
public class WooliesDeliveryModePopulator extends DeliveryModePopulator
{
	/**
	 * This method is used to populate deliver mode
	 * 
	 * @param source
	 * @param target
	 */
	@Override
	public void populate(final DeliveryModeModel source, final DeliveryModeData target)
	{
		if (source.getCode().startsWith("EGC"))
		{
			target.setDeliveryType("eGift");
		}
		else
		{
			target.setDeliveryType("Plastic");
		}
		super.populate(source, target);
	}
}
