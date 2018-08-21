/**
 *
 */
package de.hybris.wooliesegiftcard.facades.order.converters.populator;

import de.hybris.platform.commercefacades.product.converters.populator.PromotionsPopulator;
import de.hybris.platform.commercefacades.product.data.PromotionData;
import de.hybris.platform.promotions.model.AbstractPromotionModel;


/**
 * @author 648156
 *
 */
public class WooliesPromotionPopulator extends PromotionsPopulator
{
	@Override
	public void populate(final AbstractPromotionModel source, final PromotionData target)
	{
		if (source.getTitle() != null)
		{
			target.setName(source.getTitle());
		}
	}
}
