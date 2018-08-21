/**
 *
 */
package com.woolies.webservices.order.impl;

import de.hybris.platform.commerceservices.order.strategies.impl.DefaultEntryMergeStrategy;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;


/**
 * @author 668982 This class provides the functionalities to merge the entries
 *
 */
public class WooliesDefaultEntryMergeStrategy extends DefaultEntryMergeStrategy
{
	/**
	 * This method is used to get entry to merge cart orders
	 *
	 * @param entries
	 * @param newEntry
	 * @return get entries after merging the order entries
	 */
	@Override
	public AbstractOrderEntryModel getEntryToMerge(final List<AbstractOrderEntryModel> entries,
			@Nonnull final AbstractOrderEntryModel newEntry)
	{
		ServicesUtil.validateParameterNotNullStandardMessage("newEntry", newEntry);

		if (entries == null)
		{
			return null;
		}
		if (newEntry.getCustomPrice().doubleValue() > 0)
		{
			return entries.stream().filter(Objects::nonNull).filter(e -> !newEntry.equals(e))
					.filter(entry -> canMerge(entry, newEntry).booleanValue())
					//comparing customer price of old entry with the new entry, will return true if both are same otherwise false
					.filter(entryPrice -> entryPrice.getCustomPrice().equals(newEntry.getCustomPrice()))
					.sorted(getEntryModelComparator()).findFirst().orElse(null);
		}
		else
		{
			return entries.stream().filter(Objects::nonNull).filter(e -> !newEntry.equals(e))
					.filter(entry -> canMerge(entry, newEntry).booleanValue()).sorted(getEntryModelComparator()).findFirst()
					.orElse(null);
		}
	}

}
