/**
 *
 */
package de.hybris.wooliesegiftcard.core.interceptor.impl;

import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.wooliesegiftcard.core.model.MemberUnitModel;

import org.apache.log4j.Logger;


/**
 * @author 687679
 *
 *         this Interceptor is created for backoffice updation to validate member unit attributes
 */
public class ValidateMemberUnitAttributesInterceptor implements ValidateInterceptor<MemberUnitModel>
{
	private ModelService modelService;
	private static final Logger LOG = Logger.getLogger(ValidateMemberUnitAttributesInterceptor.class);


	/**
	 * This method is used to validate member unit attributes
	 *
	 * @param memberUnitModel
	 *           The Member Unit for the Member User
	 * @param interceptorContext
	 *           interceptorContextfor the Member Unit Model
	 * @throws InterceptorException
	 *            throwing this Exception in case of any error
	 */
	@Override
	public void onValidate(final MemberUnitModel memberUnitModel, final InterceptorContext interceptorContext)
			throws InterceptorException
	{
		final String heading1 = memberUnitModel.getHeading1();
		final String heading2 = memberUnitModel.getHeading2();
		final String heading3 = memberUnitModel.getHeading3();
		final String fontcolor1 = memberUnitModel.getFontColor1();
		final String fontcolor2 = memberUnitModel.getFontColor2();
		final String fontcolor3 = memberUnitModel.getFontColor3();

		checkHeadingLength(heading1, heading2, heading3);
		checkFontColorLength(fontcolor1, fontcolor2, fontcolor3);

		LOG.info("Member Unit fields validated");
	}

	/**
	 * @param fontcolor1
	 *           the Color for fontcolor1 field
	 * @param fontcolor2
	 *           the Color for fontcolor3 field
	 * @param fontcolor3
	 *           the Color for fontcolor3 field
	 * @throws InterceptorException
	 *            throwing this Exception in case of any error
	 */
	/*
	 * This method checks the length of Font Color 1,2,3.
	 */
	private void checkFontColorLength(final String fontcolor1, final String fontcolor2, final String fontcolor3)
			throws InterceptorException
	{
		if (null != fontcolor1 && fontcolor1.length() > 100)
		{
			throw new InterceptorException("More than 100 characters are not allowed in FontColor1");
		}

		if (null != fontcolor2 && fontcolor2.length() > 100)
		{
			throw new InterceptorException("More than 100 characters are not allowed in FontColor2");
		}

		if (null != fontcolor3 && fontcolor3.length() > 100)
		{
			throw new InterceptorException("More than 100 characters are not allowed in FontColor3");
		}
	}

	/**
	 * @param heading1
	 *           the Color for heading1 field
	 * @param heading2
	 *           the Color for heading2 field
	 * @param heading3
	 *           the Color for heading3 field
	 * @throws InterceptorException
	 *            throwing this Exception in case of any error
	 */
	/*
	 * This method checks the length of Heading 1,2,3.
	 */
	private void checkHeadingLength(final String heading1, final String heading2, final String heading3)
			throws InterceptorException
	{
		if (null != heading1 && heading1.length() > 250)
		{
			throw new InterceptorException("More than 250 characters are not allowed in Heading1");
		}

		if (null != heading2 && heading2.length() > 250)
		{
			throw new InterceptorException("More than 250 characters are not allowed in Heading2");
		}
		if (null != heading2 && heading3.length() > 250)
		{
			throw new InterceptorException("More than 250 characters are not allowed in Heading3");
		}
	}

	/**
	 * @return the modelService
	 */
	public final ModelService getModelService()
	{
		return modelService;
	}

	/**
	 * @param modelService
	 *           the modelService to set
	 */
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}
}
