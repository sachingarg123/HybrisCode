/**
 *
 */
package de.hybris.wooliesegiftcard.facade;

import de.hybris.wooliesegiftcard.exception.WooliesB2BUserException;
import de.hybris.wooliesegiftcard.facades.PersonalisationMediaDataList;

import java.util.List;


/**
 * @author 653154 This method is used maintain customer media details
 */
public interface WooliesMediaFacade
{
	/**
	 * This method is used to get the image of user
	 *
	 * @param userId
	 *           the parameter value to be used
	 * @param status
	 *           the parameter value to be used
	 * @param startIndex
	 *           the parameter value to be used
	 * @param pageSize
	 *           the parameter value to be used
	 * @param firstImageIDs
	 *           the parameter value to be used
	 * @return the getCustomerImages the parameter used to return
	 * @throws WooliesB2BUserException
	 *            used to throw exception
	 */
	public PersonalisationMediaDataList getCustomerImages(String userId, String status, int startIndex, int pageSize,
			List<String> firstImageIDs) throws WooliesB2BUserException;

	/**
	 * This method is used to delete the image of user
	 *
	 * @param userId
	 *           the parameter value to be used
	 * @param imageId
	 *           the parameter value to be used
	 * @throws WooliesB2BUserException
	 *            used to throw exception
	 */
	void deleteImages(String userId, String imageId) throws WooliesB2BUserException;

}
