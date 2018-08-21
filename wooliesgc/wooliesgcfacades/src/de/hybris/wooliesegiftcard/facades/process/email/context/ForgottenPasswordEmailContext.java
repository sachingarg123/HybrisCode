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
package de.hybris.wooliesegiftcard.facades.process.email.context;

import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel;
import de.hybris.platform.commerceservices.model.process.ForgottenPasswordProcessModel;
import de.hybris.platform.commerceservices.model.process.StoreFrontCustomerProcessModel;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/**
 * Velocity context for a forgotten password email.
 */

public class ForgottenPasswordEmailContext extends CustomerEmailContext
{
	private int expiresInMinutes = 30;
	private String token;

	/**
	 * To get expires in minutes
	 *
	 * @return expiresInMinutes
	 */
	public int getExpiresInMinutes()
	{
		return expiresInMinutes;
	}

	/**
	 * To set expires in minutes
	 *
	 * @param expiresInMinutes
	 */
	public void setExpiresInMinutes(final int expiresInMinutes)
	{
		this.expiresInMinutes = expiresInMinutes;
	}

	/**
	 * To get token
	 *
	 * @return token
	 */
	public String getToken()
	{
		return token;
	}

	/**
	 * To set token
	 *
	 * @param token
	 */
	public void setToken(final String token)
	{
		this.token = token;
	}

	/**
	 * To get URL EncodedToken
	 *
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public String getURLEncodedToken() throws UnsupportedEncodingException
	{
		return URLEncoder.encode(token, "UTF-8");
	}

	/**
	 * To get RequestResetPasswordUrl
	 *
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public String getRequestResetPasswordUrl() throws UnsupportedEncodingException
	{
		return getSiteBaseUrlResolutionService().getWebsiteUrlForSite(getBaseSite(), getUrlEncodingAttributes(), false,
				"/login/pw/request/external");
	}

	/**
	 * To get SecureRequestResetPasswordUrl
	 *
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public String getSecureRequestResetPasswordUrl() throws UnsupportedEncodingException
	{
		return getSiteBaseUrlResolutionService().getWebsiteUrlForSite(getBaseSite(), getUrlEncodingAttributes(), true,
				"/login/pw/request/external");
	}

	/**
	 * To get ResetPasswordUrl
	 *
	 * @return password url to reset
	 * @throws UnsupportedEncodingException
	 */
	public String getResetPasswordUrl() throws UnsupportedEncodingException
	{
		return getSiteBaseUrlResolutionService().getWebsiteUrlForSite(getBaseSite(), getUrlEncodingAttributes(), false,
				"/login/pw/change", "token=" + getURLEncodedToken());
	}

	/**
	 * To get secure reset password url
	 * 
	 * @return secured passowrd url
	 * @throws UnsupportedEncodingException
	 */
	public String getSecureResetPasswordUrl() throws UnsupportedEncodingException
	{
		return getSiteBaseUrlResolutionService().getWebsiteUrlForSite(getBaseSite(), getUrlEncodingAttributes(), true,
				"/login/pw/change", "token=" + getURLEncodedToken());
	}

	/**
	 * To get DisplayResetPasswordUrl
	 * 
	 * @return ResetPasswordUrl
	 * @throws UnsupportedEncodingException
	 */
	public String getDisplayResetPasswordUrl() throws UnsupportedEncodingException
	{
		return getSiteBaseUrlResolutionService().getWebsiteUrlForSite(getBaseSite(), getUrlEncodingAttributes(), false,
				"/my-account/update-password");
	}

	/**
	 * To get Display Secur eResetPasswordUrl
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public String getDisplaySecureResetPasswordUrl() throws UnsupportedEncodingException
	{
		return getSiteBaseUrlResolutionService().getWebsiteUrlForSite(getBaseSite(), getUrlEncodingAttributes(), true,
				"/my-account/update-password");
	}

	/**
	 * To initiate password email
	 */
	@Override
	public void init(final StoreFrontCustomerProcessModel storeFrontCustomerProcessModel, final EmailPageModel emailPageModel)
	{
		super.init(storeFrontCustomerProcessModel, emailPageModel);
		if (storeFrontCustomerProcessModel instanceof ForgottenPasswordProcessModel)
		{
			setToken(((ForgottenPasswordProcessModel) storeFrontCustomerProcessModel).getToken());
		}
	}
}
