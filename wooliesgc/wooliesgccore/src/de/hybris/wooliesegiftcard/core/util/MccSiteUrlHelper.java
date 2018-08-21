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
package de.hybris.wooliesegiftcard.core.util;

import de.hybris.platform.acceleratorservices.site.strategies.SiteChannelValidationStrategy;
import de.hybris.platform.acceleratorservices.urlresolver.SiteBaseUrlResolutionService;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.core.Registry;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Required;


/**
 * Helper bean for generating the MCC site links for the supported websites
 */
public class MccSiteUrlHelper
{
	private CMSSiteService cmsSiteService;
	private SiteBaseUrlResolutionService siteBaseUrlResolutionService;
	private SiteChannelValidationStrategy siteChannelValidationStrategy;

	// Called from BeanShell by MCC
	/**
	 * To get all sites and urls for MCC
	 *
	 * @return sites and urls
	 */
	public static Map<String, String> getAllSitesAndUrls()
	{
		final MccSiteUrlHelper mccSiteUrlHelper = Registry.getApplicationContext().getBean("mccSiteUrlHelper",
				MccSiteUrlHelper.class);
		return mccSiteUrlHelper.getSitesAndUrls();
	}

	/**
	 * To get all sites and urls for MCC
	 * 
	 * @return sites and urls
	 */
	private Map<String, String> getSitesAndUrls()
	{
		final Map<String, String> siteToUrl = new TreeMap<String, String>();
		if (getCmsSiteService().getSites() != null)
		{
			for (final CMSSiteModel cmsSiteModel : getCmsSiteService().getSites())
			{
				final String url = getSiteUrl(cmsSiteModel);
				if (url != null && !url.isEmpty()
						&& getSiteChannelValidationStrategy().validateSiteChannel(cmsSiteModel.getChannel()))
				{
					siteToUrl.put(cmsSiteModel.getName(), url);
				}
			}
		}
		return siteToUrl;
	}

	/**
	 * To get site url
	 * 
	 * @param cmsSiteModel
	 * @return url
	 */
	protected String getSiteUrl(final CMSSiteModel cmsSiteModel)
	{
		return getSiteBaseUrlResolutionService().getWebsiteUrlForSite(cmsSiteModel, false, "/");
	}

	/**
	 * To get cms site service
	 * 
	 * @return cmsSiteService
	 */
	protected CMSSiteService getCmsSiteService()
	{
		return cmsSiteService;
	}

	/**
	 * To set cms site service
	 * 
	 * @param cmsSiteService
	 */
	@Required
	public void setCmsSiteService(final CMSSiteService cmsSiteService)
	{
		this.cmsSiteService = cmsSiteService;
	}

	/**
	 * To get site base url resolution service
	 * 
	 * @return siteBaseUrlResolutionService
	 */
	protected SiteBaseUrlResolutionService getSiteBaseUrlResolutionService()
	{
		return siteBaseUrlResolutionService;
	}

	/**
	 * To set site base url resolution service
	 * 
	 * @param siteBaseUrlResolutionService
	 */
	@Required
	public void setSiteBaseUrlResolutionService(final SiteBaseUrlResolutionService siteBaseUrlResolutionService)
	{
		this.siteBaseUrlResolutionService = siteBaseUrlResolutionService;
	}

	/**
	 * To get site channel validation strategy
	 * 
	 * @return siteChannelValidationStrategy
	 */
	protected SiteChannelValidationStrategy getSiteChannelValidationStrategy()
	{
		return siteChannelValidationStrategy;
	}

	/**
	 * To set site channel validation strategy
	 * 
	 * @param siteChannelValidationStrategy
	 */
	@Required
	public void setSiteChannelValidationStrategy(final SiteChannelValidationStrategy siteChannelValidationStrategy)
	{
		this.siteChannelValidationStrategy = siteChannelValidationStrategy;
	}
}
