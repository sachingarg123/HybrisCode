/**
 *
 */
package de.hybris.wooliesegiftcard.service.interceptor.impl;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.cms2.servicelayer.services.admin.CMSAdminSiteService;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;
import de.hybris.wooliesegiftcard.core.model.CoBrandImageModel;

import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author 653154 This class is intercepter to validate cobrand
 */
public class CoBrandValidateInterceptor implements ValidateInterceptor
{
	private static final Logger LOG = Logger.getLogger(CoBrandValidateInterceptor.class.getName());
	private static final String ERROR_COBRAND_IMAGE = "Please choose an GiftCard Product";
	private static final String ERROR_COBRAND_CV = "Please choose an E-GiftCard Product from Online Version";
	private static final String ERROR_COBRAND_SIZE = "Please choose an Image of size less than 1MB";
	private static final String ERROR_COBRAND_MIME = "Please choose an Image of type JPEG or PNG";
	private static final String ERROR_PNG_MIME = "image/png";
	private static final String ERROR_JPEG_MIME = "image/jpeg";
	private static final long IMAGESIZE = 1048576L;
	private CMSAdminSiteService cmsAdminSiteService;

	/**
	 * This method is to validate the cobrands
	 *
	 * @param model
	 * @param ctx
	 * @throws InterceptorException
	 */
	@Override
	public void onValidate(final Object model, final InterceptorContext ctx) throws InterceptorException
	{
		if (model instanceof CoBrandImageModel)
		{
			final CoBrandImageModel image = (CoBrandImageModel) model;
			final ProductModel product = image.getProduct();
			if (product != null)
			{
				if (product.getIsEGiftCard().booleanValue())
				{
					LOG.error(ERROR_COBRAND_IMAGE);
					throw new InterceptorException(ERROR_COBRAND_IMAGE);
				}
				final CatalogVersionModel cvModel = product.getCatalogVersion();
				if (!("Online").equalsIgnoreCase(cvModel.getVersion()))
				{
					LOG.error(ERROR_COBRAND_CV);
					throw new InterceptorException(ERROR_COBRAND_CV);
				}
				checkMIME(image);
				final Long size = image.getSize();
				if (!Objects.isNull(size) && size.longValue() > IMAGESIZE)
				{
					LOG.error(ERROR_COBRAND_SIZE);
					throw new InterceptorException(ERROR_COBRAND_SIZE);
				}
			}
		}
	}

	/**
	 * @param image
	 * @throws InterceptorException
	 */
	private void checkMIME(final CoBrandImageModel image) throws InterceptorException
	{
		if (StringUtils.isNotEmpty(image.getMime()))
		{
			if (image.getMime().equalsIgnoreCase(ERROR_JPEG_MIME) || image.getMime().equalsIgnoreCase(ERROR_PNG_MIME))
			{
				// Do Nothing
			}
			else
			{
				LOG.error(ERROR_COBRAND_MIME);
				throw new InterceptorException(ERROR_COBRAND_MIME);
			}
		}
	}

	/**
	 * @return the cmsAdminSiteService
	 */
	public CMSAdminSiteService getCmsAdminSiteService()
	{
		return cmsAdminSiteService;
	}

	/**
	 * @param cmsAdminSiteService
	 *           the cmsAdminSiteService to set
	 */
	public void setCmsAdminSiteService(final CMSAdminSiteService cmsAdminSiteService)
	{
		this.cmsAdminSiteService = cmsAdminSiteService;
	}
}
