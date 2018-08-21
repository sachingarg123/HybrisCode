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
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.model.BusinessProcessModel;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * Velocity context for a order notification email.
 */
public class EGiftCartOrderNotificationEmailContext extends AbstractEmailContext<BusinessProcessModel>
{
	public static final String ORDER = "order";
	public static final String REDIRECTLINK = "redirectlink";
	public static final String DELIVERYCOST = "deliverycost";
	public static final String CONTACTUS = "contactus";
	public static final String FAQ = "faq";
	public static final String PRIVACYPOLICY = "privacypolicy";
	public static final String FOOTERIMAGE = "emailFooter";
	public static final String LOGO = "woolworthsEmailLogo";
	public static final Logger LOG = Logger.getLogger(EGiftCartOrderNotificationEmailContext.class);
	public static final String WOOLIESTOEMAIL = "wooliesToEmail";
	public static final String WOOLIESFROMEMAIL = "wooliesFromEmail";
	public static final String WOOLIESATTACHMENT = "wooliesAttachment";
	public static final String MESSAGE = "message";
	public static final String TONAME = "toName";


	@Override
	public void init(final BusinessProcessModel orderProcessModel, final EmailPageModel emailPageModel)
	{
		super.init(orderProcessModel, emailPageModel);

		if (StringUtils.isNotEmpty(((OrderProcessModel) orderProcessModel).getToEmail()))
		{
			put(WOOLIESTOEMAIL, ((OrderProcessModel) orderProcessModel).getToEmail());
		}
		if (StringUtils.isNotEmpty(((OrderProcessModel) orderProcessModel).getFromEmail()))
		{
			put(WOOLIESFROMEMAIL, ((OrderProcessModel) orderProcessModel).getFromEmail());
		}

		if (StringUtils.isNotEmpty(((OrderProcessModel) orderProcessModel).getMessage()))
		{
			put(MESSAGE, ((OrderProcessModel) orderProcessModel).getMessage());
		}

		if (StringUtils.isNotEmpty(((OrderProcessModel) orderProcessModel).getAttachment()))
		{
			put(WOOLIESATTACHMENT, ((OrderProcessModel) orderProcessModel).getAttachment());
		}

		if (StringUtils.isNotEmpty(((OrderProcessModel) orderProcessModel).getToEmail()))
		{
			put(EMAIL, ((OrderProcessModel) orderProcessModel).getToEmail());
		}

		if (StringUtils.isNotEmpty(((OrderProcessModel) orderProcessModel).getToName()))
		{
			put(TONAME, ((OrderProcessModel) orderProcessModel).getToName());
		}

		if (StringUtils.isNotEmpty(((OrderProcessModel) orderProcessModel).getToName()))
		{
			put(DISPLAY_NAME, ((OrderProcessModel) orderProcessModel).getToName());
		}

		put(CONTACTUS, getConfigurationService().getConfiguration().getString("orderConfirmation.contactUs.link",
				"https://dev-giftcards.woolworths.com.au/about-woolworths-gift-cards/contact-us.html"));
		put(FAQ, getConfigurationService().getConfiguration().getString("orderConfirmation.FAQ.link",
				"https://dev-giftcards.woolworths.com.au/about-woolworths-gift-cards/faqs.html"));
		put(PRIVACYPOLICY, getConfigurationService().getConfiguration().getString("orderConfirmation.privacyPolicy.link",
				"https://dev-giftcards.woolworths.com.au/about-woolworths-gift-cards/privacy-policy.html"));
	}


	/**
	 * @return the order
	 */
	public static String getOrder()
	{
		return ORDER;
	}


	/**
	 * @return the redirectlink
	 */
	public static String getRedirectlink()
	{
		return REDIRECTLINK;
	}


	/**
	 * @return the deliverycost
	 */
	public static String getDeliverycost()
	{
		return DELIVERYCOST;
	}


	/**
	 * @return the contactus
	 */
	public static String getContactus()
	{
		return CONTACTUS;
	}


	/**
	 * @return the faq
	 */
	public static String getFaq()
	{
		return FAQ;
	}


	/**
	 * @return the privacypolicy
	 */
	public static String getPrivacypolicy()
	{
		return PRIVACYPOLICY;
	}


	/**
	 * @return the footerimage
	 */
	public static String getFooterimage()
	{
		return FOOTERIMAGE;
	}


	/**
	 * @return the logo
	 */
	public static String getLogo()
	{
		return LOGO;
	}


	/**
	 * @return the log
	 */
	public static Logger getLog()
	{
		return LOG;
	}




	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.platform.acceleratorservices.process.email.context.AbstractEmailContext#getSite(de.hybris.platform.
	 * processengine.model.BusinessProcessModel)
	 */
	@Override
	protected BaseSiteModel getSite(final BusinessProcessModel businessProcessModel)
	{
		// YTODO Auto-generated method stub
		return null;
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.hybris.platform.acceleratorservices.process.email.context.AbstractEmailContext#getCustomer(de.hybris.platform.
	 * processengine.model.BusinessProcessModel)
	 */
	@Override
	protected CustomerModel getCustomer(final BusinessProcessModel businessProcessModel)
	{
		// YTODO Auto-generated method stub
		return null;
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.platform.acceleratorservices.process.email.context.AbstractEmailContext#getEmailLanguage(de.hybris.
	 * platform.processengine.model.BusinessProcessModel)
	 */
	@Override
	protected LanguageModel getEmailLanguage(final BusinessProcessModel businessProcessModel)
	{
		// YTODO Auto-generated method stub
		return null;
	}


	/**
	 * @return the wooliestoemail
	 */
	public String getWooliestoemail()
	{
		return (String) get(EMAIL);
	}


	/**
	 * @return the wooliesfromemail
	 */
	public String getWooliesfromemail()
	{
		return (String) get(WOOLIESFROMEMAIL);
	}


	/**
	 * @return the WOOLIESATTACHMENT
	 */
	public String getWooliesattachment()
	{
		return (String) get(WOOLIESATTACHMENT);
	}


	@Override
	public String getDisplayName()
	{
		return (String) get(DISPLAY_NAME);
	}

	@Override
	public String getEmail()
	{
		return (String) get(EMAIL);
	}

	@Override
	public String getToEmail()
	{
		return getEmail();
	}

	@Override
	public String getToDisplayName()
	{
		return getDisplayName();
	}

	@Override
	public String getFromEmail()
	{
		return (String) get(FROM_EMAIL);
	}

	@Override
	public String getFromDisplayName()
	{
		return (String) get(FROM_DISPLAY_NAME);
	}


	/**
	 * @return the message
	 */
	public String getMessage()
	{
		return (String) get(MESSAGE);
	}

}
