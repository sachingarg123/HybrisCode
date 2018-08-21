/**
 *
 */
package de.hybris.wooliesegiftcard.facades.populators;

import de.hybris.model.PersonalisationMediaModel;
import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.cmsfacades.uniqueidentifier.UniqueItemIdentifierService;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.wooliesegiftcard.facades.product.data.PersonalisationMediaData;

import org.springframework.beans.factory.annotation.Required;


/**
 * @author 653154 This class is used to populate media details
 */
public class WooliesMediaModelPopulator implements Populator<PersonalisationMediaModel, PersonalisationMediaData>
{
	private UniqueItemIdentifierService uniqueItemIdentifierService;
	private ConfigurationService configurationService;

	/**
	 * This method is used to populate media model
	 *
	 * @param source
	 * @param target
	 * @throws ConversionException
	 */
	@Override
	public void populate(final PersonalisationMediaModel source, final PersonalisationMediaData target) throws ConversionException
	{
		final String absolutePath = getConfigurationService().getConfiguration().getString("website.woolworths.giftcards.https");
		target.setAltText(source.getAltText());
		target.setCode(source.getCode());
		target.setDescription(source.getDescription());
		target.setDownloadUrl(source.getDownloadURL());
		target.setMime(source.getMime());
		target.setUrl(absolutePath + source.getURL());
		target.setPID(source.getPid());
		target.setImageApprovalStatus(source.getImageApprovalStatus().toString());
		getUniqueItemIdentifierService().getItemData(source).ifPresent(itemData -> target.setUuid(itemData.getItemId()));

		final CatalogVersionModel catalogVersion = source.getCatalogVersion();
		if (catalogVersion != null)
		{
			target.setCatalogVersion(catalogVersion.getVersion());

			final CatalogModel catalog = catalogVersion.getCatalog();
			if (catalog != null)
			{
				target.setCatalogId(catalog.getId());
			}
		}
	}

	/**
	 * To set Unique item identifier service
	 *
	 * @param uniqueItemIdentifierService
	 */
	@Required
	public void setUniqueItemIdentifierService(final UniqueItemIdentifierService uniqueItemIdentifierService)
	{
		this.uniqueItemIdentifierService = uniqueItemIdentifierService;
	}

	/**
	 * To get Unique item identifier service
	 *
	 * @return uniqueItemIdentifierService
	 */
	protected UniqueItemIdentifierService getUniqueItemIdentifierService()
	{
		return uniqueItemIdentifierService;
	}

	/**
	 * To get configuration service
	 *
	 * @return configurationService
	 */
	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	/**
	 * To set configuration service
	 *
	 * @param configurationService
	 */
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

}
