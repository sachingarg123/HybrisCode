/**
 *
 */
package de.hybris.wooliesegiftcard.service;

import de.hybris.wooliesegiftcard.core.model.WWHtmlLookUpModel;

import java.util.List;


/**
 * @author 669567
 *
 */
public interface WooliesPaymentService
{
	public List<WWHtmlLookUpModel> getPaymentOptions();
}
