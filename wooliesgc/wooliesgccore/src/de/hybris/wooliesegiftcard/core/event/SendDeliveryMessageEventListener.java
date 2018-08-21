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
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.orderprocessing.events.SendDeliveryMessageEvent;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import org.springframework.beans.factory.annotation.Required;


/**
 * Listener for SendDeliveryMessageEvent events.
 */
public class SendDeliveryMessageEventListener extends AbstractAcceleratorSiteEventListener<SendDeliveryMessageEvent>
{

	private ModelService modelService;
	private BusinessProcessService businessProcessService;

	/**
	 * @return the businessProcessService
	 */
	protected BusinessProcessService getBusinessProcessService()
	{
		return businessProcessService;
	}

	/**
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
	 * To process the delivery message
	 * 
	 * @param sendDeliveryMessageEvent
	 */
	@Override
	protected void onSiteEvent(final SendDeliveryMessageEvent sendDeliveryMessageEvent)
	{
		ConsignmentProcessModel consignmentProcessModel = null;
		final ConsignmentModel consignmentModel = sendDeliveryMessageEvent.getProcess().getConsignment();
		if (consignmentModel != null)
		{
			consignmentProcessModel = getBusinessProcessService().createProcess(
					"sendDeliveryEmailProcess-" + consignmentModel.getCode() + "-" + System.currentTimeMillis(),
					"sendDeliveryEmailProcess");
		}
		if (consignmentProcessModel != null)
		{
			consignmentProcessModel.setConsignment(consignmentModel);
			getModelService().save(consignmentProcessModel);
			getBusinessProcessService().startProcess(consignmentProcessModel);
		}
	}

	/**
	 * To get site Channel details to send delivery message
	 * 
	 * @param sendDeliveryMessageEvent
	 * @return siteChannel
	 */
	@Override
	protected SiteChannel getSiteChannelForEvent(final SendDeliveryMessageEvent sendDeliveryMessageEvent)
	{
		BaseSiteModel site = null;
		SiteChannel siteChannel = null;
		final AbstractOrderModel order = sendDeliveryMessageEvent.getProcess().getConsignment().getOrder();
		if (order != null)
		{
			ServicesUtil.validateParameterNotNullStandardMessage("event.order", order);
			site = order.getSite();
		}
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
