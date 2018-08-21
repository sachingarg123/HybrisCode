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

import static de.hybris.wooliesegiftcard.core.constants.WooliesgcCoreConstants.QUOTE_USER_TYPE;

import de.hybris.platform.commerceservices.enums.QuoteUserType;
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
 * This action class creates a sales representative snapshot.
 */
public class QuoteBuyerSubmitAction extends AbstractSimpleDecisionAction<QuoteProcessModel>
{
	private static final Logger LOG = Logger.getLogger(QuoteBuyerSubmitAction.class);
	private CommerceQuoteService commerceQuoteService;
	private QuoteService quoteService;

	/**
	 * Executes this <code>Action</code>'s business logic working for the given quote
	 *
	 * @param process
	 *           the quote process data
	 * @throws RetryLaterException
	 * @throws Exception
	 */
	@Override
	public Transition executeAction(final QuoteProcessModel process) throws RetryLaterException, Exception
	{
		Transition result = null;
		QuoteModel quoteModel = null;
		QuoteUserType quoteUserType = null;

		if (LOG.isDebugEnabled() && process != null)
		{
			LOG.debug(String.format("In QuoteBuyerSubmitAction for process code : [%s]", process.getCode()));
		}
		if (process != null)
		{
			quoteUserType = (QuoteUserType) processParameterHelper.getProcessParameterByName(process, QUOTE_USER_TYPE).getValue();

			quoteModel = getQuoteService().getCurrentQuoteForCode(process.getQuoteCode());
		}
		if (quoteUserType != null && quoteModel != null && QuoteUserType.BUYER.equals(quoteUserType))
		{
			getCommerceQuoteService().createQuoteSnapshotWithState(quoteModel, QuoteState.SELLER_REQUEST);
			result = Transition.OK;
		}
		else
		{
			result = Transition.NOK;
		}

		return result;
	}

	/**
	 * To get quote service
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
