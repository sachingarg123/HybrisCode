/**
 *
 */
package de.hybris.wooliesegiftcard.dao;

import de.hybris.wooliesegiftcard.core.model.WWHtmlLookUpModel;

import java.util.List;


/**
 * @author 669567
 *
 */
public interface WooliesPaymentDao
{
	public List<WWHtmlLookUpModel> getPaymentOptions();
}
