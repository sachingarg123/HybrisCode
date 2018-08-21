/**
 *
 */
package de.hybris.wooliesegiftcard.dao;

import de.hybris.model.PersonalisationMediaModel;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.wooliesegiftcard.core.enums.ImageApprovalStatus;
import de.hybris.wooliesegiftcard.exception.WooliesB2BUserException;

import java.util.List;


/**
 * @author 648156 This interface is used to maintain customer details related database activities
 */
public interface WooliesCustomerDao
{

	public List<CountryModel> getCountriesByName(String countryName);

	public SearchResult<PersonalisationMediaModel> getCustomerImages(UserModel currentUser, ImageApprovalStatus[] status,
			int startIndex, int pageSize) throws WooliesB2BUserException;

	public SearchResult<PersonalisationMediaModel> getCustomerImagesForImageIDS(final UserModel currentUser,
			final List<String> firstImageIDs);

	public List<PersonalisationMediaModel> getCustomerImageForDeletion(UserModel currentUser, String imageId)
			throws WooliesB2BUserException;
}
