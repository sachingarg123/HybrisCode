/**
 *
 */
package de.hybris.wooliesegiftcard.dao.impl;

import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.wooliesegiftcard.core.constants.GeneratedWooliesgcCoreConstants.Enumerations.LookUpType;
import de.hybris.wooliesegiftcard.core.model.WWHtmlLookUpModel;
import de.hybris.wooliesegiftcard.dao.WooliesPaymentDao;
import de.hybris.wooliesegiftcard.facade.impl.WooliesPaymentFacadeImpl;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;


/**
 * @author 669567
 *
 */
public class WooliesPaymentDaoImpl implements WooliesPaymentDao
{
	private static final Logger LOG = Logger.getLogger(WooliesPaymentFacadeImpl.class);

	

	/**
	 * @return the flexibleSearchService
	 */
	public FlexibleSearchService getFlexibleSearchService()
	{
		return flexibleSearchService;
	}

	/**
	 * @param flexibleSearchService
	 *           the flexibleSearchService to set
	 */
	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}

	private FlexibleSearchService flexibleSearchService;

	@Override
	public List<WWHtmlLookUpModel> getPaymentOptions()
	{

		LOG.debug("getPayment Options Images");
		final StringBuilder flexBuf = new StringBuilder();
		final HashMap queryParams = new HashMap();
		flexBuf.append(
				"SELECT {PK} FROM {WWHtmlLookUp} where {lookUpType} = ({{Select {PK} from {LookUpType} where {code} = ?PaymentType}})");
		queryParams.put("PaymentType", LookUpType.PAYMENT_TYPE);
		final SearchResult<WWHtmlLookUpModel> paymentOptions = this.flexibleSearchService.search(flexBuf.toString(), queryParams);
		return paymentOptions.getResult();
	}
}
