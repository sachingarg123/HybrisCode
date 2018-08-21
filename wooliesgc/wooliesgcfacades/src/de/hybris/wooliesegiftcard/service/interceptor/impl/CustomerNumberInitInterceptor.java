/**
 *
 */
package de.hybris.wooliesegiftcard.service.interceptor.impl;

import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.interceptor.InitDefaultsInterceptor;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BCustomerModel;
import de.hybris.wooliesegiftcard.core.model.MemberCustomerModel;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * @author 648156
 *
 */
public class CustomerNumberInitInterceptor implements InitDefaultsInterceptor
{
	private static final Logger LOG = Logger.getLogger(CustomerNumberInitInterceptor.class);
	private KeyGenerator keyGenerator;

	@Override
	public void onInitDefaults(final Object model, final InterceptorContext ctx) throws InterceptorException
	{
		if (model instanceof CustomerModel)
		{
			final CustomerModel customerModel = (CustomerModel) model;
			if (customerModel.getCustomerID() == null)
			{
				customerModel.setCustomerID((String) this.keyGenerator.generate());
				LOG.isDebugEnabled();
				LOG.debug("CustomerNumber Generated");
			}
		}

		if (model instanceof CorporateB2BCustomerModel)
		{
			final CorporateB2BCustomerModel corporateB2BCustomerModel = (CorporateB2BCustomerModel) model;
			if (corporateB2BCustomerModel.getCustomerID() == null)
			{
				corporateB2BCustomerModel.setCustomerID((String) this.keyGenerator.generate());
				LOG.isDebugEnabled();
				LOG.debug("CustomerNumber Generated");
			}
		}
		if (model instanceof MemberCustomerModel)
		{
			final MemberCustomerModel memberCustomerModel = (MemberCustomerModel) model;
			if (memberCustomerModel.getCustomerID() == null)
			{
				memberCustomerModel.setCustomerID((String) this.keyGenerator.generate());
				LOG.isDebugEnabled();
				LOG.debug("CustomerNumber Generated");
			}
		}
	}

	@Required
	public void setKeyGenerator(final KeyGenerator keyGenerator)
	{
		this.keyGenerator = keyGenerator;
	}
}
