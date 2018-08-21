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
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.events.OrderPlacedEvent;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import org.springframework.beans.factory.annotation.Required;


/**
 * Listener for order confirmation events.
 */
public class OrderConfirmationEventListener extends AbstractAcceleratorSiteEventListener<OrderPlacedEvent>
{

	private ModelService modelService;
	private BusinessProcessService businessProcessService;

	/**
	 * To get Business process service
	 *
	 * @return the businessProcessService
	 */
	protected BusinessProcessService getBusinessProcessService()
	{
		return businessProcessService;
	}

	/**
	 * To set Business process service
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
	 * @return modelService
	 */
	protected ModelService getModelService()
	{
		return modelService;
	}

	/**
	 * To set model service
	 *
	 * @param modelService
	 */
	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	/**
	 * To process the order confirmation event
	 *
	 * @param orderPlacedEvent
	 */
	@Override
	protected void onSiteEvent(final OrderPlacedEvent orderPlacedEvent)
	{
		OrderProcessModel orderProcessModel = null;

		final OrderModel orderModel = orderPlacedEvent.getProcess().getOrder();
		if (orderModel != null)
		{
			orderProcessModel = (OrderProcessModel) getBusinessProcessService().createProcess(
					"orderConfirmationEmailProcess-" + orderModel.getCode() + "-" + System.currentTimeMillis(),
					"orderConfirmationEmailProcess");
			orderProcessModel.setOrder(orderModel);
		}
		if (orderProcessModel != null)
		{
			getModelService().save(orderProcessModel);
			getBusinessProcessService().startProcess(orderProcessModel);
		}
	}

	/**
	 * To get site Channel details for order confirmation event
	 *
	 * @param orderPlacedEvent
	 * @return siteChannel
	 */
	@Override
	protected SiteChannel getSiteChannelForEvent(final OrderPlacedEvent orderPlacedEvent)
	{
		BaseSiteModel site = null;
		SiteChannel siteChannel = null;
		final OrderModel order = orderPlacedEvent.getProcess().getOrder();
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
