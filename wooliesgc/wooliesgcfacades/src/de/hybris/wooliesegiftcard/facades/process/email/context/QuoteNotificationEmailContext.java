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
package de.hybris.wooliesegiftcard.facades.process.email.context;

import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel;
import de.hybris.platform.acceleratorservices.process.email.context.AbstractEmailContext;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commercefacades.quote.data.QuoteData;
import de.hybris.platform.commerceservices.model.process.QuoteProcessModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.order.QuoteModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.order.QuoteService;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Required;


/**
 * Velocity context for a quote notification email.
 */
public class QuoteNotificationEmailContext extends AbstractEmailContext<QuoteProcessModel>
{
	private QuoteService quoteService;

	private Converter<QuoteModel, QuoteData> quoteConverter;

	private QuoteData quoteData;

	/**
	 * This method is used to quote notification email
	 * 
	 * @param quoteProcessModel
	 * @param emailPageModel
	 */
	@Override
	public void init(final QuoteProcessModel quoteProcessModel, final EmailPageModel emailPageModel)
	{
		super.init(quoteProcessModel, emailPageModel);

		quoteData = getQuoteConverter().convert(getQuote(quoteProcessModel));
	}

	/**
	 * To get quote
	 * 
	 * @return quoteData
	 */
	public QuoteData getQuote()
	{
		return quoteData;
	}

	/**
	 * To get site details
	 * 
	 * @param quoteProcessModel
	 * @return BaseSiteModel
	 */
	@Override
	protected BaseSiteModel getSite(final QuoteProcessModel quoteProcessModel)
	{
		return getQuote(quoteProcessModel).getSite();
	}

	/**
	 * To get customer
	 * 
	 * @param quoteProcessModel
	 * @return CustomerModel
	 */
	@Override
	protected CustomerModel getCustomer(final QuoteProcessModel quoteProcessModel)
	{
		return (CustomerModel) getQuote(quoteProcessModel).getUser();
	}

	/**
	 * To get email language
	 * 
	 * @param quoteProcessModel
	 * @return LanguageModel
	 */
	@Override
	protected LanguageModel getEmailLanguage(final QuoteProcessModel quoteProcessModel)
	{
		return getCustomer(quoteProcessModel).getSessionLanguage();
	}

	/**
	 * To get quote
	 * 
	 * @param quoteProcessModel
	 * @return
	 */
	protected QuoteModel getQuote(final QuoteProcessModel quoteProcessModel)
	{
		return Optional.of(quoteProcessModel).map(QuoteProcessModel::getQuoteCode).map(getQuoteService()::getCurrentQuoteForCode)
				.get();
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
	 * To get quote service
	 * 
	 * @return quoteService
	 */
	protected QuoteService getQuoteService()
	{
		return quoteService;
	}

	/**
	 * To set quote converter
	 * 
	 * @param quoteConverter
	 */
	@Required
	public void setQuoteConverter(final Converter<QuoteModel, QuoteData> quoteConverter)
	{
		this.quoteConverter = quoteConverter;
	}

	/**
	 * To get quote converter
	 * 
	 * @return quoteConverter
	 */
	protected Converter<QuoteModel, QuoteData> getQuoteConverter()
	{
		return quoteConverter;
	}
}
