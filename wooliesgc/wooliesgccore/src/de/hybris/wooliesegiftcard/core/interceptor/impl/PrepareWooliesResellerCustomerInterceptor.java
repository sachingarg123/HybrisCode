/**
 *
 */
package de.hybris.wooliesegiftcard.core.interceptor.impl;

import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.wooliesgccore.reseller.model.ResellerCustomerModel;

import org.apache.log4j.Logger;


/**
 * @author 653930 This intercepter is used to prepare reselling the customer item
 */
public class PrepareWooliesResellerCustomerInterceptor implements PrepareInterceptor
{

	private static final Logger LOG = Logger.getLogger(PrepareWooliesResellerCustomerInterceptor.class);
	private KeyGenerator keyGenerator;

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.platform.servicelayer.interceptor.PrepareInterceptor#onPrepare(java.lang.Object,
	 * de.hybris.platform.servicelayer.interceptor.InterceptorContext)
	 */
	/**
	 * To set reseller id to the customer model
	 * 
	 * @param model
	 * @param ctx
	 *           the InterceptorContext
	 * @throws InterceptorException
	 */
	@Override
	public void onPrepare(final Object model, final InterceptorContext ctx) throws InterceptorException
	{
		if (model instanceof ResellerCustomerModel)
		{
			final ResellerCustomerModel resellerCustomer = (ResellerCustomerModel) model;
			resellerCustomer.setResellerId((String) this.keyGenerator.generate());
			LOG.info("ResellerId==" + resellerCustomer.getResellerId());
			LOG.info("Saved ResellerCustomerModel");
		}
	}

	/**
	 * @param keyGenerator
	 *           the keyGenerator to set
	 */
	public void setKeyGenerator(final KeyGenerator keyGenerator)
	{
		this.keyGenerator = keyGenerator;
	}

}
