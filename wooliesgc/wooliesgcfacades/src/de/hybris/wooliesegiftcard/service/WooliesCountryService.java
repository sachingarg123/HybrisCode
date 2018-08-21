/**
 *
 */
package de.hybris.wooliesegiftcard.service;

import de.hybris.platform.core.model.c2l.CountryModel;

import java.util.List;


/**
 * @author 648156 This interface is used to maintain country service
 */
public interface WooliesCountryService
{
	/**
	 * This method is used to get country for the user
	 *
	 * @param object
	 *           the value to be used
	 */
	void getCountryForUser(final Object object);

	/**
	 * This method is used to get country for the given country name
	 *
	 * @param countryName
	 *           the value to be used
	 * @return CountryModel it returns country model
	 */
	List<CountryModel> getCountry(final String countryName);
}
