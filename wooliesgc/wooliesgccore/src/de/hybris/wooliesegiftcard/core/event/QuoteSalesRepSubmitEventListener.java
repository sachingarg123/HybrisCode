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

import de.hybris.platform.commerceservices.event.QuoteSalesRepSubmitEvent;
import de.hybris.platform.commerceservices.model.process.QuoteProcessModel;
import de.hybris.platform.core.model.order.QuoteModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.wooliesegiftcard.core.constants.WooliesgcCoreConstants;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * Event Listener for {@link QuoteSalesRepSubmitEvent}, which is used to trigger quote sales representative business
 * process.
 */
public class QuoteSalesRepSubmitEventListener extends AbstractEventListener<QuoteSalesRepSubmitEvent>
{
	private ModelService modelService;
	private BusinessProcessService businessProcessService;
	private static final Logger LOG = Logger.getLogger(QuoteSalesRepSubmitEventListener.class);

	/**
	 * To process quote sales representative business
	 * 
	 * @param quoteSalesRepSubmitEvent
	 */
	@Override
	protected void onEvent(final QuoteSalesRepSubmitEvent quoteSalesRepSubmitEvent)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Received QuoteSalesRepSubmitEvent..");
		}

		final QuoteProcessModel quoteSalesRepProcessModel = (QuoteProcessModel) getBusinessProcessService().createProcess(
				"quoteSalesRepProcess" + "-" + quoteSalesRepSubmitEvent.getQuote().getCode() + "-"
						+ quoteSalesRepSubmitEvent.getQuote().getStore().getUid() + "-" + System.currentTimeMillis(),
				WooliesgcCoreConstants.QUOTE_SALES_REP_PROCESS);

		if (quoteSalesRepProcessModel != null && LOG.isDebugEnabled())
		{
			LOG.debug(String.format("Created business process for QuoteSalesRepSubmitEvent. Process code : [%s] ...",
					quoteSalesRepProcessModel.getCode()));


			final QuoteModel quoteModel = quoteSalesRepSubmitEvent.getQuote();
			if (quoteModel != null)
			{
				quoteSalesRepProcessModel.setQuoteCode(quoteModel.getCode());
			}
			getModelService().save(quoteSalesRepProcessModel);
			//start the business process
			getBusinessProcessService().startProcess(quoteSalesRepProcessModel);
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
