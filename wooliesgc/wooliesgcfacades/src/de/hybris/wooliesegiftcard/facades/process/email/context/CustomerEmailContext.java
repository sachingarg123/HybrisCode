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
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commerceservices.model.process.StoreFrontCustomerProcessModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import org.springframework.beans.factory.annotation.Required;


/**
 * Velocity context for a customer email.
 */
public class CustomerEmailContext extends AbstractEmailContext<StoreFrontCustomerProcessModel>
{
	private Converter<UserModel, CustomerData> customerConverter;
	private CustomerData customerData;

	/**
	 * This method is to initiate customer email
	 * 
	 * @param storeFrontCustomerProcessModel
	 * @param emailPageModel
	 */
	@Override
	public void init(final StoreFrontCustomerProcessModel storeFrontCustomerProcessModel, final EmailPageModel emailPageModel)
	{
		super.init(storeFrontCustomerProcessModel, emailPageModel);
		customerData = getCustomerConverter().convert(getCustomer(storeFrontCustomerProcessModel));
	}

	/**
	 * This method is used to get site details
	 * 
	 * @param storeFrontCustomerProcessModel
	 * @return
	 */
	@Override
	protected BaseSiteModel getSite(final StoreFrontCustomerProcessModel storeFrontCustomerProcessModel)
	{
		return storeFrontCustomerProcessModel.getSite();
	}

	/**
	 * To get customer details
	 * 
	 * @param storeFrontCustomerProcessModel
	 * @return
	 */
	@Override
	protected CustomerModel getCustomer(final StoreFrontCustomerProcessModel storeFrontCustomerProcessModel)
	{
		return storeFrontCustomerProcessModel.getCustomer();
	}

	/**
	 * To get customer converter
	 * 
	 * @return customerConverter
	 */
	protected Converter<UserModel, CustomerData> getCustomerConverter()
	{
		return customerConverter;
	}

	/**
	 * To set customer converter
	 * 
	 * @param customerConverter
	 */
	@Required
	public void setCustomerConverter(final Converter<UserModel, CustomerData> customerConverter)
	{
		this.customerConverter = customerConverter;
	}

	/**
	 * To get customer
	 * 
	 * @return customerData
	 */
	public CustomerData getCustomer()
	{
		return customerData;
	}

	/**
	 * To get EmailLanguage
	 * 
	 * @param businessProcessModel
	 * @return
	 */
	@Override
	protected LanguageModel getEmailLanguage(final StoreFrontCustomerProcessModel businessProcessModel)
	{
		return businessProcessModel.getLanguage();
	}
}
