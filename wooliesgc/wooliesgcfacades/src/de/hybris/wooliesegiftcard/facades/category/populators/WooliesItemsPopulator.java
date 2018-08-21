/**
 *
 */
package de.hybris.wooliesegiftcard.facades.category.populators;

import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.media.impl.DefaultMediaService;
import de.hybris.platform.wooliesgcfacades.category.data.ItemsData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author 653930 This class is used to populate items
 */
public class WooliesItemsPopulator implements Populator<CategoryModel, ItemsData>
{
	private static final Logger LOG = LoggerFactory.getLogger(WooliesItemsPopulator.class);
	private DefaultMediaService mediaService;
	private ConfigurationService configurationService;

	/**
	 * This method is used to populate items for the give category
	 *
	 * @param source
	 * @param target
	 */
	@Override
	public void populate(final CategoryModel source, final ItemsData target)
	{
		final String absolutePath = getConfigurationService().getConfiguration().getString("website.woolworths.giftcards.https");
		LOG.info("WooliesItemsPopulator==AbsolutePath===" + absolutePath);
		target.setCategoryId(source.getCode());
		target.setName(source.getName());
		target.setDescription(source.getDescription());


		if (source.getPicture() != null && absolutePath != null && !absolutePath.isEmpty())
		{
			target.setImageURL(absolutePath + source.getPicture().getURL());
			LOG.info("WooliesItemsPopulator==ImageURL===" + target.getImageURL());
		}
	}

	/**
	 * @return the mediaService
	 */
	public DefaultMediaService getMediaService()
	{
		return mediaService;
	}

	/**
	 * @param mediaService
	 *           the mediaService to set
	 */
	public void setMediaService(final DefaultMediaService mediaService)
	{
		this.mediaService = mediaService;
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
