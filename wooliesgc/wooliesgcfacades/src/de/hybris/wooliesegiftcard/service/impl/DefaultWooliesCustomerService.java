/**
 *
 */
package de.hybris.wooliesegiftcard.service.impl;

import de.hybris.model.PersonalisationMediaModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.user.impl.DefaultUserService;
import de.hybris.wooliesegiftcard.core.enums.ImageApprovalStatus;
import de.hybris.wooliesegiftcard.dao.impl.WooliesDefaultCustomerDao;
import de.hybris.wooliesegiftcard.exception.WooliesB2BUserException;
import de.hybris.wooliesegiftcard.facades.constants.WooliesgcFacadesConstants;
import de.hybris.wooliesegiftcard.service.WooliesCustomerService;

import java.util.List;
import java.util.Set;


import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author 653154 This class is used to maintain customer service
 *
 */
public class DefaultWooliesCustomerService implements WooliesCustomerService
{
	@Autowired
	private DefaultUserService userService;
	@Autowired
	private ModelService modelService;
	@Autowired
	private WooliesDefaultCustomerDao wooliesCustomerDao;

	/**
	 * This method is used to get customer images
	 *
	 * @param userId
	 * @param status
	 * @param startIndex
	 * @param pageSize
	 * @return
	 * @throws WooliesB2BUserException
	 */
	@Override
	public SearchResult<PersonalisationMediaModel> getCustomerImages(final String userId, final ImageApprovalStatus[] status,
			final int startIndex, final int pageSize) throws WooliesB2BUserException
	{
		SearchResult<PersonalisationMediaModel> personalisationMediaModel = null;
		final UserModel currentUser = userService.getUserForUID(userId);
		if (currentUser != null)
		{
			personalisationMediaModel = wooliesCustomerDao.getCustomerImages(currentUser, status, startIndex, pageSize);
			if (personalisationMediaModel != null)
			{
				return personalisationMediaModel;
			}
		}
		return null;
	}

	@Override
	public SearchResult<PersonalisationMediaModel> getCustomerImagesForImageIDS(final String userId,
			final List<String> firstImageIDs)
	{
		final UserModel currentUser = userService.getUserForUID(userId);
		return wooliesCustomerDao.getCustomerImagesForImageIDS(currentUser, firstImageIDs);
	}


	/**
	 * This method is used to delete images
	 *
	 * @param userId
	 * @param imageId
	 * @throws WooliesB2BUserException
	 */
	@Override
	public void deleteImages(final String userId, final String imageId) throws WooliesB2BUserException
	{
		final UserModel currentUser = userService.getUserForUID(userId);
		final Set<PersonalisationMediaModel> images = currentUser.getGiftCardMedias();
		boolean prodFound = true;
		if (images != null && !images.isEmpty())
		{
			for (final PersonalisationMediaModel eachMedia : images)
			{
				if (eachMedia.getCode().equalsIgnoreCase(imageId))
				{
					eachMedia.setImageApprovalStatus(ImageApprovalStatus.DELETE);
					modelService.save(eachMedia);
					prodFound = false;
					break;
				}
			}
		}
		if (prodFound)
		{
			throw new WooliesB2BUserException(WooliesgcFacadesConstants.ERRCODE_IMAGENOTEXIST);
		}

	}
}
