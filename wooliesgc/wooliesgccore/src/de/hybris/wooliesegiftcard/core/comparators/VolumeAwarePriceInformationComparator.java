/*
 * [y] hybris Platform
 *
 * Copyright (c) 2017 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.wooliesegiftcard.core.comparators;

import de.hybris.platform.europe1.jalo.PriceRow;
import de.hybris.platform.jalo.order.price.PriceInformation;
import de.hybris.platform.util.PriceValue;

import java.util.Comparator;


/**
 * Compares two prices.<br>
 * The prices are compared by minqty and the lowest minqty (or null) is treated as lower. <br>
 * If there are multiple prices then the lowest price is treated as lower. <br>
 * If there are still multiple records then the first is treated as lower.
 */
public class VolumeAwarePriceInformationComparator implements Comparator<PriceInformation>
{
	/**
	 * Compares two prices
	 *
	 * @param priceInfo1
	 *           the price information of first object
	 * @param priceInfo2
	 *           the price information of second object
	 */
	@Override
	public int compare(final PriceInformation priceInfo1, final PriceInformation priceInfo2)
	{
		final Long o1Quantity = (Long) priceInfo1.getQualifierValue(PriceRow.MINQTD);
		final Long o2Quantity = (Long) priceInfo2.getQualifierValue(PriceRow.MINQTD);

		if (o1Quantity == null && o2Quantity == null)
		{
			return 0;
		}

		if (o1Quantity == null)
		{
			return -1;
		}

		if (o2Quantity == null)
		{
			return 1;
		}

		if (o1Quantity.longValue() == o2Quantity.longValue())
		{
			final PriceValue obj1PriceValue = priceInfo1.getPriceValue();
			final PriceValue obj2PriceValue = priceInfo2.getPriceValue();
			return Double.compare(obj1PriceValue.getValue(), obj2PriceValue.getValue());
		}
		return o1Quantity.compareTo(o2Quantity);
	}
}
