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
package de.hybris.wooliesegiftcard.core.event;

import de.hybris.platform.acceleratorservices.site.AbstractAcceleratorSiteEventListener;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commerceservices.enums.SiteChannel;
import de.hybris.platform.commerceservices.event.ForgottenPwdEvent;
import de.hybris.platform.commerceservices.model.process.ForgottenPasswordProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import org.springframework.beans.factory.annotation.Required;


/**
 * Listener for "forgotten password" functionality event.
 */
public class ForgottenPasswordEventListener extends AbstractAcceleratorSiteEventListener<ForgottenPwdEvent>
{

	private ModelService modelService;
	private BusinessProcessService businessProcessService;

	/**
	 * Gets business process service
	 *
	 * @return the businessProcessService
	 */
	protected BusinessProcessService getBusinessProcessService()
	{
		return businessProcessService;
	}

	/**
	 * set business process service
	 *
	 * @param businessProcessService
	 *           the businessProcessService to set
	 */
	@Required
	public void setBusinessProcessService(final BusinessProcessService businessProcessService)
	{
		this.businessProcessService = businessProcessService;
	}

	/**
	 * @return the modelService
	 */
	protected ModelService getModelService()
	{
		return modelService;
	}

	/**
	 * @param modelService
	 *           the modelService to set
	 */
	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	/**
	 * To process the forgot password and set the password
	 *
	 * @param event
	 */
	@Override
	protected void onSiteEvent(final ForgottenPwdEvent forgottenPwdEvent)
	{
		final ForgottenPasswordProcessModel forgottenPasswordProcessModel = (ForgottenPasswordProcessModel) getBusinessProcessService()
				.createProcess("forgottenPassword-" + forgottenPwdEvent.getCustomer().getUid() + "-" + System.currentTimeMillis(),
						"forgottenPasswordEmailProcess");
		if (forgottenPasswordProcessModel != null)
		{
			forgottenPasswordProcessModel.setSite(forgottenPwdEvent.getSite());
			forgottenPasswordProcessModel.setCustomer(forgottenPwdEvent.getCustomer());
			forgottenPasswordProcessModel.setToken(forgottenPwdEvent.getToken());
			forgottenPasswordProcessModel.setLanguage(forgottenPwdEvent.getLanguage());
			forgottenPasswordProcessModel.setCurrency(forgottenPwdEvent.getCurrency());
			forgottenPasswordProcessModel.setStore(forgottenPwdEvent.getBaseStore());
			getModelService().save(forgottenPasswordProcessModel);
			getBusinessProcessService().startProcess(forgottenPasswordProcessModel);
		}
	}

	/**
	 *
	 * @param event
	 *           the forgot password event
	 * @return the siteChannel
	 */

	@Override
	protected SiteChannel getSiteChannelForEvent(final ForgottenPwdEvent forgottenPwdEvent)
	{
		SiteChannel siteChannel = null;
		final BaseSiteModel site = forgottenPwdEvent.getSite();
		if (site != null)
		{
			ServicesUtil.validateParameterNotNullStandardMessage("event.site", site);
			siteChannel = site.getChannel();
		}

		if (siteChannel != null)
		{
			return siteChannel;
		}
		else
		{
			return null;
		}
	}
}
