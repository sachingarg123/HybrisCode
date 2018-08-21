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


import de.hybris.platform.commerceservices.event.QuoteCancelEvent;
import de.hybris.platform.commerceservices.model.process.QuoteProcessModel;
import de.hybris.platform.core.model.order.QuoteModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.wooliesegiftcard.core.constants.WooliesgcCoreConstants;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * Event Listener for {@link QuoteCancelEvent}. This Event Listener starts the quote post cancellation process.
 */
public class QuoteCancelEventListener extends AbstractEventListener<QuoteCancelEvent>
{
	private ModelService modelService;
	private BusinessProcessService businessProcessService;
	private static final Logger LOG = Logger.getLogger(QuoteCancelEventListener.class);

	/**
	 * To process the quote snapshot cancelled by buyer.
	 * 
	 * @param quoteCancelEvent
	 */
	@Override
	protected void onEvent(final QuoteCancelEvent quoteCancelEvent)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Received QuoteCancelEvent..");
		}

		final Map<String, Object> contextParams = new HashMap<String, Object>();
		contextParams.put(WooliesgcCoreConstants.QUOTE_USER_TYPE, quoteCancelEvent.getQuoteUserType());

		final QuoteProcessModel quotePostCancellationProcessModel = (QuoteProcessModel) getBusinessProcessService().createProcess(
				"quotePostCancellationProcess" + "-" + quoteCancelEvent.getQuote().getCode() + "-"
						+ quoteCancelEvent.getQuote().getStore().getUid() + "-" + System.currentTimeMillis(),
				WooliesgcCoreConstants.QUOTE_POST_CANCELLATION_PROCESS, contextParams);

		if (quotePostCancellationProcessModel != null && LOG.isDebugEnabled())
		{
			LOG.debug(String.format("Created business process for QuoteCancelEvent. Process code : [%s] ...",
					quotePostCancellationProcessModel.getCode()));


			final QuoteModel quoteModel = quoteCancelEvent.getQuote();
			if (quoteModel != null)
			{
				quotePostCancellationProcessModel.setQuoteCode(quoteModel.getCode());
			}
			getModelService().save(quotePostCancellationProcessModel);
			//start the business process
			getBusinessProcessService().startProcess(quotePostCancellationProcessModel);
		}

	}

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
}
