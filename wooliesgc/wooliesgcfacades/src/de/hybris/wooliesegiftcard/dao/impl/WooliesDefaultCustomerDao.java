/**
 *
 */
package de.hybris.wooliesegiftcard.dao.impl;

import de.hybris.model.PersonalisationMediaModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.wooliesegiftcard.core.enums.ImageApprovalStatus;
import de.hybris.wooliesegiftcard.dao.WooliesCustomerDao;
import de.hybris.wooliesegiftcard.exception.WooliesB2BUserException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * @author 648156 This class is used to maintain customer details related database activities
 *
 */
public class WooliesDefaultCustomerDao implements WooliesCustomerDao
{
	private static final Logger LOG = Logger.getLogger(WooliesDefaultCustomerDao.class);
	private FlexibleSearchService flexibleSearchService;

	/**
	 * @return the flexibleSearchService
	 */
	public FlexibleSearchService getFlexibleSearchService()
	{
		return flexibleSearchService;
	}

	/**
	 * @param flexibleSearchService
	 *           the flexibleSearchService to set
	 */
	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.wooliesegiftcard.dao.WooliesCustomerDao#getCountriesByName(java.lang.String) Here we are getting
	 * the countries from hybris db based on the country name
	 *
	 * @param user giving the country name
	 *
	 * @return its returns list of country models
	 */
	@Override
	public List<CountryModel> getCountriesByName(final String countryName)
	{
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder query = new StringBuilder("SELECT DISTINCT {").append(ItemModel.PK).append("}");
		query.append(" from {").append(CountryModel._TYPECODE).append("} where {").append(CountryModel.NAME);
		query.append("} like ?countryName");
		params.put("countryName", countryName);

		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		fQuery.addQueryParameters(params);

		fQuery.setResultClassList(Collections.singletonList(CountryModel.class));
		LOG.info("getCountriesByName" + fQuery);
		final SearchResult<CountryModel> searchResult = flexibleSearchService.search(fQuery);
		if (searchResult != null)
		{
			return searchResult.getResult();
		}
		else
		{
			return Collections.emptyList();
		}
	}

	/**
	 * This method is used to get the customer images
	 *
	 * @param currentUser
	 * @param status
	 * @param startIndex
	 * @param pageSize
	 * @return the PersonalisationMediaModel of customer images
	 * @throws WooliesB2BUserException
	 */
	@Override
	public SearchResult<PersonalisationMediaModel> getCustomerImages(final UserModel currentUser,
			final ImageApprovalStatus[] status, final int startIndex, final int pageSize) throws WooliesB2BUserException
	{
		final String query = "SELECT {p:" + PersonalisationMediaModel.PK + "} FROM {" + PersonalisationMediaModel._TYPECODE
				+ " as p}" + " WHERE {p:" + PersonalisationMediaModel.USER + "} = ?user" + " AND {p:"
				+ PersonalisationMediaModel.IMAGEAPPROVALSTATUS + "}  IN (?status) ORDER BY {p:"
				+ PersonalisationMediaModel.CREATIONTIME + "} DESC";
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		fQuery.setStart(startIndex);
		fQuery.setCount(pageSize);
		fQuery.setNeedTotal(true);
		final Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("user", currentUser);
		queryParams.put("status", Arrays.asList(status));
		fQuery.getQueryParameters().putAll(queryParams);
		LOG.info("getCustomerImages" + fQuery);
		return getFlexibleSearchService().search(fQuery);
	}

	/**
	 * @param currentUser
	 * @param firstImageIDs
	 * @return
	 */
	@Override
	public SearchResult<PersonalisationMediaModel> getCustomerImagesForImageIDS(final UserModel currentUser,
			final List<String> firstImageIDs)
	{
		final String query1 = "select {p:pk} from {PersonalisationMedia as p} WHERE {p:user}=?user and {p:code} in (?firstImageIDs)";
		/**
		 * select {p:pk} from {PersonalisationMediaModel as p join media as m on {p:image}={m:pk} and {p:user}=?user and
		 * {m:code} in ?firstImageIDs
		 */
		final StringBuilder query = new StringBuilder("SELECT DISTINCT {p:").append(PersonalisationMediaModel.PK).append("}");
		query.append(" FROM {").append(PersonalisationMediaModel._TYPECODE).append(" AS p join ");
		query.append(MediaModel._TYPECODE).append(" as m on {p:").append(PersonalisationMediaModel.HASORDER).append("} = {m:");
		query.append(MediaModel.PK).append("}} where {p:").append(PersonalisationMediaModel.USER).append("} = ?user" + " AND {m:");
		query.append(MediaModel.CODE).append("} in (?firstImageIDs)");
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query1);
		final Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("user", currentUser);
		queryParams.put("firstImageIDs", firstImageIDs);
		fQuery.getQueryParameters().putAll(queryParams);
		LOG.info("getCustomerImagesForImageIDS" + fQuery);
		return getFlexibleSearchService().search(fQuery);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.wooliesegiftcard.dao.WooliesCustomerDao#getCustomerImageForDeletion(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public List<PersonalisationMediaModel> getCustomerImageForDeletion(final UserModel currentUser, final String imageId)
			throws WooliesB2BUserException
	{
		final String query = "SELECT {p:" + PersonalisationMediaModel.PK + "} FROM {" + PersonalisationMediaModel._TYPECODE
				+ " as p}" + " WHERE {p:" + PersonalisationMediaModel.USER + "} = ?user" + " AND {p:"
				+ PersonalisationMediaModel.IMAGEAPPROVALSTATUS + "} IN (?status) AND {p:" + PersonalisationMediaModel.CODE
				+ "} = ?imageId ";
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		final Map<String, Object> queryParams = new HashMap<String, Object>();
		final ArrayList statusList = new ArrayList<ImageApprovalStatus>();
		statusList.add(ImageApprovalStatus.APPROVED);
		statusList.add(ImageApprovalStatus.PENDING);
		queryParams.put("user", currentUser);
		queryParams.put("status", statusList);
		queryParams.put("imageId", imageId);
		fQuery.getQueryParameters().putAll(queryParams);
		LOG.info("getCustomerImageForDeletion" + fQuery);
		final SearchResult<PersonalisationMediaModel> searchResult = getFlexibleSearchService().search(fQuery);
		return searchResult.getResult();
	}

}
