/**
 *
 */
package de.hybris.wooliesegiftcard.facade.impl;

import de.hybris.model.PersonalisationMediaModel;
import de.hybris.platform.commercefacades.user.data.WooliesPaginationData;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.wooliesegiftcard.core.enums.ImageApprovalStatus;
import de.hybris.wooliesegiftcard.exception.WooliesB2BUserException;
import de.hybris.wooliesegiftcard.facade.WooliesMediaFacade;
import de.hybris.wooliesegiftcard.facades.PersonalisationMediaDataList;
import de.hybris.wooliesegiftcard.facades.constants.WooliesgcFacadesConstants;
import de.hybris.wooliesegiftcard.facades.product.data.PersonalisationMediaData;
import de.hybris.wooliesegiftcard.service.WooliesCustomerService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;



/**
 * @author 653154 This method is used maintain customer media details
 */
public class DefaultWooliesMediaFacade implements WooliesMediaFacade
{
	@Autowired
	private WooliesCustomerService wooliesCustomerService;
	@Autowired
	private Converter<PersonalisationMediaModel, PersonalisationMediaData> wooliesMediaModelConverter;

	/**
	 * This method is used to get customer image
	 *
	 * @param userId
	 * @param status
	 * @param startIndex
	 * @param pageSize
	 * @return personalisationMediaDataList
	 * @throws WooliesB2BUserException
	 */
	@Override
	public PersonalisationMediaDataList getCustomerImages(final String userId, final String statuses, final int startIndex,
			final int pageSize, final List<String> firstImageIDs) throws WooliesB2BUserException
	{

		final Set<ImageApprovalStatus> statusSet = extractImageStatuses(statuses);
		final SearchResult<PersonalisationMediaModel> personalisations = wooliesCustomerService.getCustomerImages(userId,
				statusSet.toArray(new ImageApprovalStatus[statusSet.size()]), startIndex, pageSize);
		final List<PersonalisationMediaModel> resultSet = new ArrayList(personalisations.getResult());
		List<PersonalisationMediaModel> firstImageList = new ArrayList();
		if (CollectionUtils.isNotEmpty(firstImageIDs))
		{
			final SearchResult<PersonalisationMediaModel> firstImages = wooliesCustomerService.getCustomerImagesForImageIDS(userId,
					firstImageIDs);
			firstImageList = firstImages.getResult();
		}
		final WooliesPaginationData pageData = new WooliesPaginationData();
		pageData.setTotalCount(personalisations.getTotalCount());
		pageData.setCurrentIndex(startIndex + pageSize);

		final PersonalisationMediaDataList dataList = new PersonalisationMediaDataList();
		final List<PersonalisationMediaData> medias = new ArrayList<PersonalisationMediaData>();

		for (final PersonalisationMediaModel personalisationMediaModel : firstImageList)
		{
			PersonalisationMediaData eachData = new PersonalisationMediaData();
			eachData = wooliesMediaModelConverter.convert(personalisationMediaModel);
			medias.add(eachData);
		}
		int imagesAdded = firstImageList.size();
		if (firstImageIDs != null)
		{
			for (final PersonalisationMediaModel personalisationMediaModel : resultSet)
			{
				if (imagesAdded == pageSize)
				{
					break;
				}
				if (!firstImageIDs.contains(personalisationMediaModel.getCode()))
				{
					PersonalisationMediaData eachData = new PersonalisationMediaData();
					eachData = wooliesMediaModelConverter.convert(personalisationMediaModel);
					medias.add(eachData);
					imagesAdded++;
				}
			}
		}
		else
		{
			for (final PersonalisationMediaModel personalisationMediaModel : resultSet)
			{
				PersonalisationMediaData eachData = new PersonalisationMediaData();
				eachData = wooliesMediaModelConverter.convert(personalisationMediaModel);
				medias.add(eachData);
				imagesAdded++;
			}
		}
		dataList.setImageList(medias);
		dataList.setPageData(pageData);
		return dataList;
	}

	/**
	 * This method is used to delete the image of user
	 *
	 * @param userId
	 * @param imageId
	 * @throws WooliesB2BUserException
	 */
	@Override
	public void deleteImages(final String userId, final String imageId) throws WooliesB2BUserException
	{
		wooliesCustomerService.deleteImages(userId, imageId);

	}

	/**
	 * This method is used to extract image status
	 *
	 * @param statuses
	 * @return statusesEnum
	 */
	private Set<ImageApprovalStatus> extractImageStatuses(final String statuses)
	{
		final String[] statusesStrings = statuses.split(WooliesgcFacadesConstants.OPTIONS_SEPARATOR);

		final Set<ImageApprovalStatus> statusesEnum = new HashSet<>();
		for (final String status : statusesStrings)
		{
			statusesEnum.add(ImageApprovalStatus.valueOf(status));
		}
		return statusesEnum;
	}

}
