/**
 *
 */
package de.hybris.wooliesegiftcard.core.event;

import static de.hybris.wooliesegiftcard.core.constants.WooliesgcCoreConstants.QUOTE_EXPIRED_EMAIL_PROCESS;

import de.hybris.platform.commerceservices.event.QuoteExpiredEvent;
import de.hybris.platform.commerceservices.model.process.QuoteProcessModel;
import de.hybris.platform.core.model.order.QuoteModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import de.hybris.platform.servicelayer.model.ModelService;

import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * Event Listener for {@link QuoteExpiredEvent}. This Event Listener starts the quote expired business process.
 */
public class QuoteExpiredEventListener extends AbstractEventListener<QuoteExpiredEvent>
{
	private ModelService modelService;
	private BusinessProcessService businessProcessService;
	private static final Logger LOG = Logger.getLogger(QuoteExpiredEventListener.class);

	/**
	 * To process the quote expired event.
	 *
	 * @param quoteExpiredEvent
	 */
	@Override
	protected void onEvent(final QuoteExpiredEvent quoteExpiredEvent)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Received QuoteExpiredEvent..");
		}

		final QuoteProcessModel quoteProcessModel = createQuoteProcessModel(quoteExpiredEvent);
		if (quoteProcessModel != null)
		{
			getModelService().save(quoteProcessModel);
			getBusinessProcessService().startProcess(quoteProcessModel);
		}
	}

	/**
	 * To create the quote quote process model
	 *
	 * @param quoteExpiredEvent
	 * @return quoteProcessModel
	 */
	private QuoteProcessModel createQuoteProcessModel(final QuoteExpiredEvent quoteExpiredEvent)
	{
		QuoteProcessModel quoteProcessModel = null;
		final QuoteModel quote = quoteExpiredEvent.getQuote();
		if (quote != null)
		{
			quoteProcessModel = (QuoteProcessModel) getBusinessProcessService().createProcess(String.format("quoteExpired-%s-%s-%s",
					quote.getCode(), quote.getStore().getUid(), Long.valueOf(System.currentTimeMillis())), QUOTE_EXPIRED_EMAIL_PROCESS,
					MapUtils.EMPTY_MAP);

			if (quoteProcessModel != null && LOG.isDebugEnabled())
			{
				LOG.debug(String.format("Created business process for QuoteExpiredEvent. Process code : [%s] ...",
						quoteProcessModel.getCode()));

				quoteProcessModel.setQuoteCode(quote.getCode());
			}
		}
		return quoteProcessModel;
	}

	/**
	 * To get business process service
	 *
	 * @return businessProcessService
	 */
	protected BusinessProcessService getBusinessProcessService()
	{
		return businessProcessService;
	}

	/**
	 * To set business process service
	 * 
	 * @param businessProcessService
	 */
	@Required
	public void setBusinessProcessService(final BusinessProcessService businessProcessService)
	{
		this.businessProcessService = businessProcessService;
	}

	/**
	 * To get model service
	 * 
	 * @return modelService
	 */
	protected ModelService getModelService()
	{
		return modelService;
	}

	/**
	 * To set model service
	 * 
	 * @param modelService
	 */
	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}
}
