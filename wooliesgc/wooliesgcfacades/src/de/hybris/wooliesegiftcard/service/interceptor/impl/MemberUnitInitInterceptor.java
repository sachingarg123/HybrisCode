/**
 *
 */
package de.hybris.wooliesegiftcard.service.interceptor.impl;

import de.hybris.platform.servicelayer.interceptor.InitDefaultsInterceptor;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.wooliesegiftcard.core.model.MemberUnitModel;

import org.apache.log4j.Logger;


/**
 * @author 687679
 *
 */
public class MemberUnitInitInterceptor implements InitDefaultsInterceptor<MemberUnitModel>
{

	private static final Logger LOG = Logger.getLogger(MemberUnitInitInterceptor.class);


	@Override
	public void onInitDefaults(final MemberUnitModel memberUnitModel, final InterceptorContext interceptorContext)
			throws InterceptorException
	{

		memberUnitModel.setAccountType("MEM");
		LOG.info("memberUnitModel AccountType==" + memberUnitModel.getAccountType());
		LOG.info("Save MemberUnitModel with AccountType as MEM ");

	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hybris.platform.servicelayer.interceptor.InitDefaultsInterceptor#onInitDefaults(java.lang.Object,
	 * de.hybris.platform.servicelayer.interceptor.InterceptorContext)
	 */
}



