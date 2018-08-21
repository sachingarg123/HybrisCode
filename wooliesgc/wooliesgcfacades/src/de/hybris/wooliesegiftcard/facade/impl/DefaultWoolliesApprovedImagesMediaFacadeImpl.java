/**
 *
 */
package de.hybris.wooliesegiftcard.facade.impl;

import de.hybris.platform.cmsfacades.data.MediaData;
import de.hybris.platform.cmsfacades.media.impl.DefaultMediaFacade;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.wooliesegiftcard.exception.WoolliesMediaImagesException;
import de.hybris.wooliesegiftcard.facades.WoolliesApprovedImagesMediaFacade;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author 264343 This class is used to maintain the details of approved images
 */
public class DefaultWoolliesApprovedImagesMediaFacadeImpl extends DefaultMediaFacade implements WoolliesApprovedImagesMediaFacade
{

	private static final Logger LOG = Logger.getLogger(DefaultWoolliesApprovedImagesMediaFacadeImpl.class);
	private ModelService modelService;

	@Autowired
	private FlexibleSearchService flexibleSearchService;

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;

	MediaData mediaData = null;

	/**
	 * This method is used to get the apporvedi images for the given user id
	 *
	 * @param userId
	 */
	@Override
	public MediaData getApprovedImages(final String userId) throws WoolliesMediaImagesException
	{

		return mediaData;
	}

	/**
	 * This method is used th encode the image for the give image url
	 *
	 * @param url
	 */
	@Override
	public String imageEncoding(final String url) throws WoolliesMediaImagesException
	{
		String base64Image = "";
		final File file = new File(url);
		try (FileInputStream imageInFile = new FileInputStream(file))
		{
			// Reading a Image file from file system
			final byte[] imageData = new byte[(int) file.length()];
			if (imageInFile.read(imageData) > 0)
			{
				base64Image = Base64.getEncoder().encodeToString(imageData);
			}


		}
		catch (final FileNotFoundException e)
		{
			LOG.error(e);
		}
		catch (final IOException e1)
		{
			LOG.error(e1);
		}
		return base64Image;
	}


	/**
	 * @return the modelService
	 */
	@Override
	public ModelService getModelService()
	{
		return modelService;
	}

	/**
	 * @param modelService
	 *           the modelService to set
	 */
	@Override
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	/**
	 * @return the flexibleSearchService
	 */
	public FlexibleSearchService getFlexibleSearchService()
	{
		return flexibleSearchService;
	}

	/**
	 * @param flexibleSearchService
	 *           the flexibleSearchService to set
	 */
	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
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
