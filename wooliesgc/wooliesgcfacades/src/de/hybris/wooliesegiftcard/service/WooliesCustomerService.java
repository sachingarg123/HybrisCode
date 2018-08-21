/**
 *
 */
package de.hybris.wooliesegiftcard.service;

import de.hybris.model.PersonalisationMediaModel;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.wooliesegiftcard.core.enums.ImageApprovalStatus;
import de.hybris.wooliesegiftcard.exception.WooliesB2BUserException;

import java.util.List;



/**
 * @author 653154 This interface is used to maintain customer service
 */
public interface WooliesCustomerService
{
	/**
	 * This method is used to get customer images
	 *
	 * @param userId
	 *           the value to be used
	 * @param status
	 *           the value to be used
	 * @param startIndex
	 *           the value to be used
	 * @param pageSize
	 *           the value to be used
	 * @return its returns PersonalisationMediaModel
	 * @throws WooliesB2BUserException
	 *            used to throw exception
	 */
	SearchResult<PersonalisationMediaModel> getCustomerImages(String userId, ImageApprovalStatus[] status, int startIndex,
			int pageSize) throws WooliesB2BUserException;

	/**
	 * This method is used to delete images
	 *
	 * @param userId
	 *           the value to be used
	 * @param imageId
	 *           the value to be used
	 * @throws WooliesB2BUserException
	 *            used to throw exception
	 */

	void deleteImages(String userId, String imageId) throws WooliesB2BUserException;

	/**
	 * This method is used to get getCustomerImagesForImageIDS
	 * 
	 * @param userId
	 *           the value to be used
	 * @param firstImageIDs
	 *           the value to be used
	 * @return used to return PersonalisationMediaModel
	 */
	public SearchResult<PersonalisationMediaModel> getCustomerImagesForImageIDS(String userId, List<String> firstImageIDs);
}
