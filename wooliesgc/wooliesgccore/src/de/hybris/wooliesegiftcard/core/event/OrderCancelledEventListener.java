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
import de.hybris.platform.commerceservices.event.OrderCancelledEvent;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import org.springframework.beans.factory.annotation.Required;


/**
 *
 * @author cognizant This class is used to process the cancelled order
 *
 */
public class OrderCancelledEventListener extends AbstractAcceleratorSiteEventListener<OrderCancelledEvent>
{

	private ModelService modelService;
	private BusinessProcessService businessProcessService;

	/**
	 * To process the orderCancelled event
	 * 
	 * @param event
	 */
	@Override
	protected void onSiteEvent(final OrderCancelledEvent orderCancelledEvent)
	{
		OrderProcessModel orderProcessModel = null;
		final OrderModel orderModel = orderCancelledEvent.getProcess().getOrder();
		if (orderModel != null)
		{
			orderProcessModel = (OrderProcessModel) getBusinessProcessService().createProcess(
					"sendOrderCancelledEmailProcess-" + orderModel.getCode() + "-" + System.currentTimeMillis(),
					"sendOrderCancelledEmailProcess");
		}
		if (orderProcessModel != null)
		{
			orderProcessModel.setOrder(orderModel);
			getModelService().save(orderProcessModel);
			getBusinessProcessService().startProcess(orderProcessModel);
		}

	}

	/**
	 *
	 * @return the modelService
	 */
	public ModelService getModelService()
	{
		return modelService;
	}

	/**
	 *
	 * @param modelService
	 */
	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	/**
	 * To get business process service
	 * 
	 * @return businessProcessService
	 */
	public BusinessProcessService getBusinessProcessService()
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
	 * To get site channelFor order cancelled event
	 * 
	 * @param orderCancelledEvent
	 * @return
	 */
	@Override
	protected SiteChannel getSiteChannelForEvent(final OrderCancelledEvent orderCancelledEvent)
	{
		BaseSiteModel site = null;
		SiteChannel siteChannel = null;
		final OrderModel order = orderCancelledEvent.getProcess().getOrder();
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
