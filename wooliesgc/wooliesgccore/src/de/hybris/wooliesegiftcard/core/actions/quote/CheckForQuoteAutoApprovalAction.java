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
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.task.RetryLaterException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * This action class will be used by business process to determine, whether the quote requires manual approval from
 * seller approver. <br>
 * If manual approval is not required, a new quote snapshot with status SELLERAPPROVER_APPROVED is created. <br>
 * Otherwise, a new quote snapshot with status SELLERAPPROVER_PENDING is created.
 *
 */
public class CheckForQuoteAutoApprovalAction extends AbstractSimpleDecisionAction<QuoteProcessModel>
{
	private QuoteService quoteService;
	private CommerceQuoteService commerceQuoteService;
	private static final Logger LOG = Logger.getLogger(CheckSellerApproverResponseOnQuoteAction.class);

	/**
	 * Executes this <code>Action</code>'s business logic working for the given quote
	 *
	 * @param process
	 * @return
	 * @throws RetryLaterException
	 * @throws Exception
	 */
	@Override
	public Transition executeAction(final QuoteProcessModel process) throws RetryLaterException, Exception
	{
		Transition result = null;
		QuoteModel quoteModel = null;

		if (LOG.isDebugEnabled() && process != null)
		{
			LOG.debug(String.format("In CheckForQuoteAutoApprovalAction for process code : [%s]", process.getCode()));
		}
		if (process != null)
		{
			quoteModel = getQuoteService().getCurrentQuoteForCode(process.getQuoteCode());
		}

		if (quoteModel != null)
		{
			if (getCommerceQuoteService().shouldAutoApproveTheQuoteForSellerApproval(quoteModel))
			{
				getCommerceQuoteService().createQuoteSnapshotWithState(quoteModel, QuoteState.SELLERAPPROVER_APPROVED);
				result = Transition.OK;
			}
			else
			{
				getCommerceQuoteService().createQuoteSnapshotWithState(quoteModel, QuoteState.SELLERAPPROVER_PENDING);
				result = Transition.NOK;
			}
		}

		return result;
	}

	/**
	 * To get the quote service
	 * 
	 * @return quote service.
	 */
	protected QuoteService getQuoteService()
	{
		return quoteService;
	}

	/**
	 *
	 * @param quoteService
	 *           the quote service data
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
