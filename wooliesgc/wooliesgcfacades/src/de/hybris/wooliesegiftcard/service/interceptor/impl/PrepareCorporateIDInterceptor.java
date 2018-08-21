/**
 *
 */
package de.hybris.wooliesegiftcard.service.interceptor.impl;

import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BUnitModel;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * @author 668982 This class is acts as intercepter to prepare corporate id
 */
public class PrepareCorporateIDInterceptor implements PrepareInterceptor
{
	private static final Logger LOG = Logger.getLogger(PrepareCorporateIDInterceptor.class.getName());
	private KeyGenerator keyGenerator;

	/**
	 * This method is to prepare corporate id
	 *
	 * @param model
	 * @param ctx
	 * @throws InterceptorException
	 */
	@Override
	public void onPrepare(final Object model, final InterceptorContext ctx) throws InterceptorException
	{
		if (model instanceof CorporateB2BUnitModel)
		{
			final CorporateB2BUnitModel corporateB2BUnitModel = (CorporateB2BUnitModel) model;
			if (corporateB2BUnitModel.getUid() == null)
			{
				corporateB2BUnitModel.setUid("CORP_" + (String) this.keyGenerator.generate());
				LOG.isDebugEnabled();
				LOG.debug("CorporateID Generated");
			}
		}

	}

	/**
	 * To set key generator
	 * 
	 * @param keyGenerator
	 */
	@Required
	public void setKeyGenerator(final KeyGenerator keyGenerator)
	{
		this.keyGenerator = keyGenerator;
	}
}