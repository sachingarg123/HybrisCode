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

import de.hybris.platform.acceleratorservices.orderprocessing.model.OrderModificationProcessModel;
import de.hybris.platform.acceleratorservices.site.AbstractAcceleratorSiteEventListener;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commerceservices.enums.SiteChannel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.ordermodify.model.OrderModificationRecordEntryModel;
import de.hybris.platform.orderprocessing.events.SendOrderPartiallyCanceledMessageEvent;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import org.springframework.beans.factory.annotation.Required;


/**
 * Listener for SendOrderPartiallyCanceledMessageEvent events.
 */
public class SendOrderPartiallyCanceledMessageEventListener
		extends AbstractAcceleratorSiteEventListener<SendOrderPartiallyCanceledMessageEvent>
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
	 * To process Send Order Partially Canceled Message
	 *
	 * @param sendOrderPartiallyCanceledMessageEvent
	 */
	@Override
	protected void onSiteEvent(final SendOrderPartiallyCanceledMessageEvent sendOrderPartiallyCanceledMessageEvent)
	{
		OrderModificationProcessModel orderModificationProcessModel = null;
		final OrderModel order = sendOrderPartiallyCanceledMessageEvent.getProcess().getOrder();
		final OrderModificationRecordEntryModel modificationRecordEntry = sendOrderPartiallyCanceledMessageEvent.getProcess()
				.getOrderModificationRecordEntry();
		if (modificationRecordEntry != null)
		{
			orderModificationProcessModel = getBusinessProcessService().createProcess(
					"sendOrderPartiallyCanceledEmailProcess-" + modificationRecordEntry.getCode() + "-" + System.currentTimeMillis(),
					"sendOrderPartiallyCanceledEmailProcess");
		}
		if (orderModificationProcessModel != null)
		{
			if (order != null)
			{
				orderModificationProcessModel.setOrder(order);
			}
			orderModificationProcessModel.setOrderModificationRecordEntry(modificationRecordEntry);
			getModelService().save(orderModificationProcessModel);
			getBusinessProcessService().startProcess(orderModificationProcessModel);
		}
	}

	/**
	 * To get site Channel details for Send Order Partially Canceled Message
	 *
	 * @param event
	 *           the Send Order Partially Canceled Message event
	 * @return siteChannel
	 */
	@Override
	protected SiteChannel getSiteChannelForEvent(final SendOrderPartiallyCanceledMessageEvent event)
	{
		BaseSiteModel site = null;
		SiteChannel siteChannel = null;
		final AbstractOrderModel order = event.getProcess().getOrder();
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
