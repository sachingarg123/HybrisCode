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
package de.hybris.wooliesegiftcard.core.actions.quote;

import de.hybris.platform.commerceservices.model.process.QuoteProcessModel;
import de.hybris.platform.commerceservices.order.CommerceQuoteService;
import de.hybris.platform.core.enums.QuoteState;
import de.hybris.platform.core.model.order.QuoteModel;
import de.hybris.platform.order.QuoteService;
import de.hybris.platform.processengine.action.AbstractProceduralAction;
import de.hybris.platform.task.RetryLaterException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * This class creates a new quote snapshot with status SELLER_REQUEST, if seller approver has rejected the quote.
 */
public class QuoteApprovalRejectedAction extends AbstractProceduralAction<QuoteProcessModel>
{
	private QuoteService quoteService;
	private CommerceQuoteService commerceQuoteService;
	private static final Logger LOG = Logger.getLogger(CheckSellerApproverResponseOnQuoteAction.class);

	/**
	 * Executes this <code>Action</code>'s business logic working for the given quote
	 *
	 * @param process
	 *           the quote process data
	 * @throws RetryLaterException
	 * @throws Exception
	 */
	@Override
	public void executeAction(final QuoteProcessModel process) throws RetryLaterException, Exception
	{
		QuoteModel quoteModel = null;
		if (LOG.isDebugEnabled() && process != null)
		{
			LOG.debug(String.format("In QuoteApprovalRejectedAction for process code : [%s]", process.getCode()));
		}
		if (process != null)
		{
			quoteModel = getQuoteService().getCurrentQuoteForCode(process.getQuoteCode());
		}

		if (quoteModel != null && QuoteState.SELLERAPPROVER_REJECTED.equals(quoteModel.getState()))
		{
			getCommerceQuoteService().createQuoteSnapshotWithState(quoteModel, QuoteState.SELLER_REQUEST);
		}
	}

	/**
	 *
	 * Gets quote service
	 *
	 * @return quoteService
	 */
	protected QuoteService getQuoteService()
	{
		return quoteService;
	}

	/**
	 * To set quote service
	 * 
	 * @param quoteService
	 */
	@Required
	public void setQuoteService(final QuoteService quoteService)
	{
		this.quoteService = quoteService;
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
