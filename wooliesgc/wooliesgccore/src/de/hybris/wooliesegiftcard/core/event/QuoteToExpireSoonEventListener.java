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


import static de.hybris.wooliesegiftcard.core.constants.WooliesgcCoreConstants.QUOTE_TO_EXPIRE_SOON_EMAIL_PROCESS;

import de.hybris.platform.commerceservices.event.QuoteToExpireSoonEvent;
import de.hybris.platform.commerceservices.model.process.QuoteProcessModel;
import de.hybris.platform.core.model.order.QuoteModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import de.hybris.platform.servicelayer.model.ModelService;

import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * Event Listener for {@link QuoteToExpireSoonEvent}. This Event Listener starts the quote to expire soon business
 * process.
 */
public class QuoteToExpireSoonEventListener extends AbstractEventListener<QuoteToExpireSoonEvent>
{
	private ModelService modelService;
	private BusinessProcessService businessProcessService;
	private static final Logger LOG = Logger.getLogger(QuoteToExpireSoonEventListener.class);

	/**
	 * To process quote to expire soon business
	 *
	 * @param quoteToExpireSoonEvent
	 */
	@Override
	protected void onEvent(final QuoteToExpireSoonEvent quoteToExpireSoonEvent)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Received QuoteToExpireSoonEvent..");
		}

		final QuoteProcessModel quoteProcessModel = createQuoteProcessModel(quoteToExpireSoonEvent);
		if (quoteProcessModel != null)
		{
			getModelService().save(quoteProcessModel);

			getBusinessProcessService().startProcess(quoteProcessModel);
		}
	}

	/**
	 * To create exiry quote process.
	 *
	 * @param quoteToExpireSoonEvent
	 * @return quoteProcessModel
	 */
	private QuoteProcessModel createQuoteProcessModel(final QuoteToExpireSoonEvent quoteToExpireSoonEvent)
	{
		QuoteProcessModel quoteProcessModel = null;
		final QuoteModel quote = quoteToExpireSoonEvent.getQuote();
		if (quote != null)
		{
			quoteProcessModel = (QuoteProcessModel) getBusinessProcessService()
					.createProcess(String.format("quoteToExpireSoon-%s-%s-%s", quote.getCode(), quote.getStore().getUid(),
							Long.valueOf(System.currentTimeMillis())), QUOTE_TO_EXPIRE_SOON_EMAIL_PROCESS, MapUtils.EMPTY_MAP);
		}
		if (quoteProcessModel != null && LOG.isDebugEnabled())
		{
			LOG.debug(String.format("Created business process for QuoteToExpireSoonEvent. Process code : [%s] ...",
					quoteProcessModel.getCode()));
			quoteProcessModel.setQuoteCode(quote.getCode());
		}

		return quoteProcessModel;
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
