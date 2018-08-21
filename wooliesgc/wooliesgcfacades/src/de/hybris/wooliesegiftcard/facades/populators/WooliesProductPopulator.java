/**
 *
 */
package de.hybris.wooliesegiftcard.facades.populators;

import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.media.impl.DefaultMediaService;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author 653930 This class is used to populate product
 */
public class WooliesProductPopulator implements Populator<ProductModel, ProductData>
{
	private static final Logger LOG = LoggerFactory.getLogger(WooliesProductPopulator.class);
	private Populator<ProductModel, ProductData> productDescriptionPopulator;
	@Resource
	private WooliesProductPricePopulator wooliesProductPricePopulator;
	@Resource
	private DefaultMediaService mediaService;
	private ConfigurationService configurationService;


	/**
	 * This method is used to populate product details
	 *
	 * @param source
	 * @param target
	 */
	@Override
	public void populate(final ProductModel source, final ProductData target)
	{
		final String absolutePath = getConfigurationService().getConfiguration().getString("website.woolworths.giftcards.https");
		LOG.info("WooliesProductPopulator==absolutePath===" + absolutePath);

		target.setSkuCode(source.getCode());
		target.setName(source.getName());
		getProductDescriptionPopulator().populate(source, target);
		getWooliesProductPricePopulator().populate(source, target);
		if (source.getPicture() != null)
		{
			final StringBuilder sb = new StringBuilder();
			sb.append(absolutePath);
			sb.append(source.getPicture().getURL());
			target.setImageURL(sb.toString());
			LOG.info("WooliesProductPopulator==ImageURL===" + target.getImageURL());
		}
		if (source.getIsEGiftCard().booleanValue())
		{
			target.setProductType("EGC");
		}
		else
		{
			target.setProductType("PGC");
		}
	}

	/**
	 * @return the wooliesProductPricePopulator
	 */
	public WooliesProductPricePopulator getWooliesProductPricePopulator()
	{
		return wooliesProductPricePopulator;
	}

	/**
	 * @param wooliesProductPricePopulator
	 *           the wooliesProductPricePopulator to set
	 */
	public void setWooliesProductPricePopulator(final WooliesProductPricePopulator wooliesProductPricePopulator)
	{
		this.wooliesProductPricePopulator = wooliesProductPricePopulator;
	}

	/**
	 * @return the productDescriptionPopulator
	 */
	public Populator<ProductModel, ProductData> getProductDescriptionPopulator()
	{
		return productDescriptionPopulator;
	}

	/**
	 * @param productDescriptionPopulator
	 *           the productDescriptionPopulator to set
	 */
	public void setProductDescriptionPopulator(final Populator<ProductModel, ProductData> productDescriptionPopulator)
	{
		this.productDescriptionPopulator = productDescriptionPopulator;
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
}
