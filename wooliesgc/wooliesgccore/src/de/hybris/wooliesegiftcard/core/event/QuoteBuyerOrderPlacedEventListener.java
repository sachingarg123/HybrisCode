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

import de.hybris.platform.commerceservices.event.QuoteBuyerOrderPlacedEvent;
import de.hybris.platform.commerceservices.order.CommerceQuoteService;
import de.hybris.platform.core.enums.QuoteState;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.QuoteModel;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import de.hybris.platform.servicelayer.model.ModelService;

import org.springframework.beans.factory.annotation.Required;


/**
 * Event Listener for {@link QuoteBuyerOrderPlacedEvent} which updates the quote state and attaches the latest quote
 * snapshot to the order placed.
 */
public class QuoteBuyerOrderPlacedEventListener extends AbstractEventListener<QuoteBuyerOrderPlacedEvent>
{
	private ModelService modelService;
	private CommerceQuoteService commerceQuoteService;

	/**
	 * To process the quote state and attaches the latest quote snapshot to the order placed.
	 *
	 * @param quoteBuyerOrderPlacedEvent
	 */
	@Override
	protected void onEvent(final QuoteBuyerOrderPlacedEvent quoteBuyerOrderPlacedEvent)
	{
		final OrderModel orderModel = quoteBuyerOrderPlacedEvent.getOrder();
		if (orderModel != null)
		{
			final QuoteModel quoteModel = getCommerceQuoteService()
					.createQuoteSnapshotWithState(quoteBuyerOrderPlacedEvent.getQuote(), QuoteState.BUYER_ORDERED);
			getModelService().refresh(orderModel);
			if (quoteModel != null)
			{
				orderModel.setQuoteReference(quoteModel);
			}
			getModelService().save(orderModel);

		}
	}

	/**
	 * To get Model service
	 *
	 * @return modelService
	 */
	protected ModelService getModelService()
	{
		return modelService;
	}

	/**
	 * To set Model service
	 *
	 * @param modelService
	 */
	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	/**
	 * To get commerce quote service
	 *
	 * @return commerceQuoteService
	 */
	protected CommerceQuoteService getCommerceQuoteService()
	{
		return commerceQuoteService;
	}

	/**
	 * To set commerce quote service
	 *
	 * @param commerceQuoteService
	 */
	@Required
	public void setCommerceQuoteService(final CommerceQuoteService commerceQuoteService)
	{
		this.commerceQuoteService = commerceQuoteService;
	}

}
