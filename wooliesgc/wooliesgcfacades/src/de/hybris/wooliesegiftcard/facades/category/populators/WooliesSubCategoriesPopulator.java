/**
 *
 */
package de.hybris.wooliesegiftcard.facades.category.populators;

import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.converters.impl.AbstractPopulatingConverter;
import de.hybris.platform.wooliesgcfacades.category.data.ItemsData;
import de.hybris.platform.wooliesgcfacades.category.data.SubCategoriesData;

import java.util.ArrayList;
import java.util.List;


/**
 * @author 653930 This class is used populate subcategories
 *
 */
public class WooliesSubCategoriesPopulator implements Populator<CategoryModel, SubCategoriesData>
{
	private AbstractPopulatingConverter<CategoryModel, ItemsData> itemsConverter;

	/**
	 * This method is used to populate sub categories for the given category
	 *
	 * @param source
	 * @param target
	 */
	@Override
	public void populate(final CategoryModel source, final SubCategoriesData target)
	{
		final List<CategoryModel> categories = source.getCategories();
		target.setItems(new ArrayList<ItemsData>());

		if (categories != null && !categories.isEmpty())
		{
			target.setClassification(source.getCategories().get(0).getClassification());
			for (final CategoryModel category : categories)
			{
				final ItemsData itemsData = getItemsConverter().convert(category);
				target.getItems().add(itemsData);
			}
		}
	}

	/**
	 * @return the itemsConverter
	 */
	public AbstractPopulatingConverter<CategoryModel, ItemsData> getItemsConverter()
	{
		return itemsConverter;
	}

	/**
	 * @param itemsConverter
	 *           the itemsConverter to set
	 */
	public void setItemsConverter(final AbstractPopulatingConverter<CategoryModel, ItemsData> itemsConverter)
	{
		this.itemsConverter = itemsConverter;
	}



}
