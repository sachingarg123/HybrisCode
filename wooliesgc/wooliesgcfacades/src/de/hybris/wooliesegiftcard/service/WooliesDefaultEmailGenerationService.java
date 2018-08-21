/**
 *
 */
package de.hybris.wooliesegiftcard.service;

import de.hybris.platform.acceleratorservices.email.impl.DefaultEmailGenerationService;
import de.hybris.platform.acceleratorservices.model.email.EmailAddressModel;
import de.hybris.platform.acceleratorservices.model.email.EmailAttachmentModel;
import de.hybris.platform.acceleratorservices.model.email.EmailMessageModel;
import de.hybris.platform.acceleratorservices.process.email.context.AbstractEmailContext;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.wooliesegiftcard.facades.process.email.context.EGiftCartOrderNotificationEmailContext;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author 687679
 *
 */
public class WooliesDefaultEmailGenerationService extends DefaultEmailGenerationService
{

	@Autowired
	private MediaService mediaService;


	@Override
	public EmailMessageModel createEmailMessage(final String emailSubject, final String emailBody,
			final AbstractEmailContext<BusinessProcessModel> wooliesEmailContext)
	{
		final List<EmailAddressModel> toEmails = new ArrayList<>();
		final EmailAddressModel toAddress = getEmailService().getOrCreateEmailAddressForEmail(wooliesEmailContext.getToEmail(),
				wooliesEmailContext.getToDisplayName());
		toEmails.add(toAddress);
		final EmailAddressModel fromAddress = getEmailService().getOrCreateEmailAddressForEmail(wooliesEmailContext.getFromEmail(),
				wooliesEmailContext.getFromDisplayName());
		if (wooliesEmailContext instanceof EGiftCartOrderNotificationEmailContext)
		{
			final EGiftCartOrderNotificationEmailContext wooliesContext = (EGiftCartOrderNotificationEmailContext) wooliesEmailContext;
			final List<EmailAttachmentModel> attachments = getNotificationEmailAttachments(wooliesContext);
			return getEmailService().createEmailMessage(toEmails, new ArrayList<EmailAddressModel>(),
					new ArrayList<EmailAddressModel>(), fromAddress, wooliesEmailContext.getFromEmail(), emailSubject, emailBody,
					attachments);
		}
		else
		{
			return getEmailService().createEmailMessage(toEmails, new ArrayList<EmailAddressModel>(),
					new ArrayList<EmailAddressModel>(), fromAddress, wooliesEmailContext.getFromEmail(), emailSubject, emailBody,
					null);

		}
	}

	/**
	 * This Method is used to add attachment for the Email
	 *
	 * @param wooliesContext
	 *           takes Woolies Context as a Parameter
	 * @return it returns a List of EmailAttachmentModel
	 */
	private List<EmailAttachmentModel> getNotificationEmailAttachments(final EGiftCartOrderNotificationEmailContext wooliesContext)
	{
		final List<EmailAttachmentModel> atts = new ArrayList<>();
		final EmailAttachmentModel media = (EmailAttachmentModel) mediaService.getMedia(wooliesContext.getWooliesattachment());
		atts.add(media);
		return atts;
	}

	/**
	 * @return the mediaService
	 */
	public MediaService getMediaService()
	{
		return mediaService;
	}

	/**
	 * @param mediaService
	 *           the mediaService to set
	 */
	public void setMediaService(final MediaService mediaService)
	{
		this.mediaService = mediaService;
	}

}