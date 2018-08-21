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

import static com.google.common.collect.Sets.newHashSet;

import de.hybris.platform.commerceservices.enums.QuoteNotificationType;
import de.hybris.platform.commerceservices.model.process.QuoteProcessModel;
import de.hybris.platform.core.model.order.QuoteModel;
import de.hybris.platform.order.QuoteService;
import de.hybris.platform.processengine.action.AbstractProceduralAction;

import java.util.Set;

import org.springframework.beans.factory.annotation.Required;


/**
 * A generic quote process action used to mark a specific notification type {set in quoteNotificationType} as generated
 * for the quote.
 */
public class MarkQuoteNotificationAsSentAction extends AbstractProceduralAction<QuoteProcessModel>
{
	private QuoteService quoteService;
	private QuoteNotificationType quoteNotificationType;

	/**
	 * Executes this <code>Action</code>'s business logic working for the given quote
	 *
	 * @param quoteProcessModel
	 *           the quote process model
	 * @throws RetryLaterException
	 * @throws Exception
	 */
	@Override
	public void executeAction(final QuoteProcessModel quoteProcessModel) throws Exception
	{
		final QuoteModel quoteModel = getQuoteService().getCurrentQuoteForCode(quoteProcessModel.getQuoteCode());
		if (quoteModel != null)
		{
			final Set<QuoteNotificationType> generatedQuoteNotifications = newHashSet(quoteModel.getGeneratedNotifications());
			generatedQuoteNotifications.add(getQuoteNotificationType());
			quoteModel.setGeneratedNotifications(generatedQuoteNotifications);

			getModelService().save(quoteModel);
		}
	}

	/**
	 * To get quote Service
	 *
	 * @return quoteService
	 */
	protected QuoteService getQuoteService()
	{
		return quoteService;
	}

	/**
	 * To set quote Service
	 * 
	 * @param quoteService
	 */
	@Required
	public void setQuoteService(final QuoteService quoteService)
	{
		this.quoteService = quoteService;
	}

	/**
	 * gets quote notification type object
	 * 
	 * @return quoteNotificationType
	 */
	protected QuoteNotificationType getQuoteNotificationType()
	{
		return quoteNotificationType;
	}

	/**
	 * sets quote notification type object
	 * 
	 * @param quoteNotificationType
	 */
	@Required
	public void setQuoteNotificationType(final QuoteNotificationType quoteNotificationType)
	{
		this.quoteNotificationType = quoteNotificationType;
	}
}
