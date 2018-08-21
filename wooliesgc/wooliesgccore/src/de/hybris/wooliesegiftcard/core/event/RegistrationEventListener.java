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
import de.hybris.platform.commerceservices.event.RegisterEvent;
import de.hybris.platform.commerceservices.model.process.StoreFrontCustomerProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import org.springframework.beans.factory.annotation.Required;


/**
 * Listener for customer registration events.
 */
public class RegistrationEventListener extends AbstractAcceleratorSiteEventListener<RegisterEvent>
{

	private ModelService modelService;
	private BusinessProcessService businessProcessService;

	/**
	 * To get business process service
	 *
	 * @return businessProcessService
	 */
	protected BusinessProcessService getBusinessProcessService()
	{
		return businessProcessService;
	}

	/**
	 * To set business process service
	 * 
	 * @param businessProcessService
	 */
	@Required
	public void setBusinessProcessService(final BusinessProcessService businessProcessService)
	{
		this.businessProcessService = businessProcessService;
	}

	/**
	 * To get model service
	 * 
	 * @return the modelService
	 */
	protected ModelService getModelService()
	{
		return modelService;
	}

	/**
	 * To set model service
	 * 
	 * @param modelService
	 *           the modelService to set
	 */
	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	/**
	 * To process customer registration
	 * 
	 * @param registerEvent
	 */
	@Override
	protected void onSiteEvent(final RegisterEvent registerEvent)
	{
		final StoreFrontCustomerProcessModel storeFrontCustomerProcessModel = (StoreFrontCustomerProcessModel) getBusinessProcessService()
				.createProcess(
						"customerRegistrationEmailProcess-" + registerEvent.getCustomer().getUid() + "-" + System.currentTimeMillis(),
						"customerRegistrationEmailProcess");
		if (storeFrontCustomerProcessModel != null)
		{
			storeFrontCustomerProcessModel.setSite(registerEvent.getSite());
			storeFrontCustomerProcessModel.setCustomer(registerEvent.getCustomer());
			storeFrontCustomerProcessModel.setLanguage(registerEvent.getLanguage());
			storeFrontCustomerProcessModel.setCurrency(registerEvent.getCurrency());
			storeFrontCustomerProcessModel.setStore(registerEvent.getBaseStore());
			getModelService().save(storeFrontCustomerProcessModel);
			getBusinessProcessService().startProcess(storeFrontCustomerProcessModel);
		}
	}

	/**
	 * To get site Channel details for customer registration
	 * 
	 * @param event
	 * @return siteChannel
	 */
	@Override
	protected SiteChannel getSiteChannelForEvent(final RegisterEvent registerEvent)
	{

		SiteChannel siteChannel = null;
		final BaseSiteModel site = registerEvent.getSite();
		if (site != null)
		{
			ServicesUtil.validateParameterNotNullStandardMessage("event.order.site", site);
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
