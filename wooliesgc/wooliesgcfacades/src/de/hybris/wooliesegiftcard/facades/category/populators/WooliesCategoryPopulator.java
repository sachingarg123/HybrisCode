/**
 *
 */
package de.hybris.wooliesegiftcard.facades.category.populators;

import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.commercefacades.product.data.CategoryData;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.converters.impl.AbstractPopulatingConverter;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.media.impl.DefaultMediaService;
import de.hybris.platform.wooliesgcfacades.category.data.SubCategoriesData;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author 653930 This class is used to populate the category
 */
public class WooliesCategoryPopulator implements Populator<CategoryModel, CategoryData>
{
	private static final Logger LOG = LoggerFactory.getLogger(WooliesCategoryPopulator.class);
	private AbstractPopulatingConverter<ProductModel, ProductData> wooliesProductConverter;
	private AbstractPopulatingConverter<CategoryModel, SubCategoriesData> subCategoriesConverter;
	private ConfigurationService configurationService;
	@Resource
	private DefaultMediaService mediaService;

	/**
	 * This method is used to populate the categories
	 *
	 * @param source
	 * @param target
	 */
	@Override
	public void populate(final CategoryModel source, final CategoryData target)
	{
		final List<CategoryModel> categories = source.getCategories();
		final String absolutePath = getConfigurationService().getConfiguration().getString("website.woolworths.giftcards.https");
		LOG.info("WooliesCategoryPopulator===AbsolutePath===" + absolutePath);
		target.setCode(source.getCode());
		target.setName(source.getName());
		target.setDescription(source.getDescription());
		target.setClassification(source.getClassification());
		target.setProducts(new ArrayList<ProductData>());
		target.setCategories(null);
		final List<ProductModel> products = source.getProducts();

		if (null != categories && !categories.isEmpty())
		{
			target.setCategories(getSubCategoriesConverter().convert(source));
		}
		if (products != null && !products.isEmpty())
		{
			for (final ProductModel product : products)
			{
				final ProductData productData = getWooliesProductConverter().convert(product);
				target.getProducts().add(productData);
			}
		}
		else
		{
			target.setProducts(null);
		}

		if (source.getPicture() != null && absolutePath != null && !absolutePath.isEmpty())
		{
			final StringBuilder sb = new StringBuilder();
			sb.append(absolutePath);
			sb.append(source.getPicture().getURL());
			target.setImageURL(sb.toString());
			LOG.info("WooliesCategoryPopulator==ImageURL===" + target.getImageURL());
		}

	}


	/**
	 * @return the subCategoriesConverter
	 */
	public AbstractPopulatingConverter<CategoryModel, SubCategoriesData> getSubCategoriesConverter()
	{
		return subCategoriesConverter;
	}

	/**
	 * @param subCategoriesConverter
	 *           the subCategoriesConverter to set
	 */
	public void setSubCategoriesConverter(
			final AbstractPopulatingConverter<CategoryModel, SubCategoriesData> subCategoriesConverter)
	{
		this.subCategoriesConverter = subCategoriesConverter;
	}


	/**
	 * @return the wooliesProductConverter
	 */
	public AbstractPopulatingConverter<ProductModel, ProductData> getWooliesProductConverter()
	{
		return wooliesProductConverter;
	}


	/**
	 * @param wooliesProductConverter
	 *           the wooliesProductConverter to set
	 */
	public void setWooliesProductConverter(final AbstractPopulatingConverter<ProductModel, ProductData> wooliesProductConverter)
	{
		this.wooliesProductConverter = wooliesProductConverter;
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
