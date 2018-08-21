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

import static de.hybris.wooliesegiftcard.core.constants.WooliesgcCoreConstants.QUOTE_SELLER_APPROVER_PROCESS;

import de.hybris.platform.commerceservices.event.QuoteSellerApprovalSubmitEvent;
import de.hybris.platform.commerceservices.model.process.QuoteProcessModel;
import de.hybris.platform.core.model.order.QuoteModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import de.hybris.platform.servicelayer.model.ModelService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * Event listener that listens to {@link QuoteSellerApprovalSubmitEvent} which is used to trigger seller approval
 * process.
 */
public class QuoteSellerApprovalSubmitEventListener extends AbstractEventListener<QuoteSellerApprovalSubmitEvent>
{
	private ModelService modelService;
	private BusinessProcessService businessProcessService;
	private static final Logger LOG = Logger.getLogger(QuoteSellerApprovalSubmitEventListener.class);

	/**
	 * To process quote seller approval which has been submitted
	 *
	 * @param quoteSellerApprovalSubmitEvent
	 */
	@Override
	protected void onEvent(final QuoteSellerApprovalSubmitEvent quoteSellerApprovalSubmitEvent)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Received QuoteSellerApprovalSubmitEvent..");
		}

		final QuoteProcessModel quoteSellerApprovalProcess = (QuoteProcessModel) getBusinessProcessService().createProcess(
				"quoteSellerApprovalProcess" + "-" + quoteSellerApprovalSubmitEvent.getQuote().getCode() + "-"
						+ quoteSellerApprovalSubmitEvent.getQuote().getStore().getUid() + "-" + System.currentTimeMillis(),
				QUOTE_SELLER_APPROVER_PROCESS);

		final QuoteModel quoteModel = quoteSellerApprovalSubmitEvent.getQuote();
		if (quoteSellerApprovalProcess != null && quoteModel != null)
		{
			quoteSellerApprovalProcess.setQuoteCode(quoteModel.getCode());
			getModelService().save(quoteSellerApprovalProcess);
			//start the business process
			getBusinessProcessService().startProcess(quoteSellerApprovalProcess);
		}
	}

	/**
	 * To set model service
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
}
