/**
 *
 */
package de.hybris.wooliesegiftcard.service.interceptor.impl;

import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.wooliesegiftcard.core.model.CoBrandImageModel;




/**
 * @author 653154 This class is acts as intercepter to prepare co brand
 */
public class PrepareCoBrandInterceptor implements PrepareInterceptor
{
	private KeyGenerator keyGenerator;

	/**
	 * to prepare co brand
	 * 
	 * @param model
	 * @param ctx
	 * @throws InterceptorException
	 */
	@Override
	public void onPrepare(final Object model, final InterceptorContext ctx) throws InterceptorException
	{

		if (model instanceof CoBrandImageModel)
		{
			final CoBrandImageModel image = (CoBrandImageModel) model;
			if (image.getImageID() == null)
			{
				image.setImageID((String) getKeyGenerator().generate());
			}
		}
	}

	/**
	 * @return the keyGenerator
	 */
	public KeyGenerator getKeyGenerator()
	{
		return keyGenerator;
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
