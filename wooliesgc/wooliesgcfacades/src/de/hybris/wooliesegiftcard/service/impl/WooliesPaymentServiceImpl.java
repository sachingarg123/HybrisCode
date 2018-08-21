/**
 *
 */
package de.hybris.wooliesegiftcard.service.impl;

import de.hybris.wooliesegiftcard.core.model.WWHtmlLookUpModel;
import de.hybris.wooliesegiftcard.dao.impl.WooliesPaymentDaoImpl;
import de.hybris.wooliesegiftcard.service.WooliesPaymentService;

import java.util.List;


/**
 * @author 669567
 *
 */
public class WooliesPaymentServiceImpl implements WooliesPaymentService
{
	private WooliesPaymentDaoImpl wooliesPaymentDaoImpl;

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.wooliesegiftcard.service.WooliesPaymentService#getPaymentOptions()
	 */
	@Override
	public List<WWHtmlLookUpModel> getPaymentOptions()
	{

		return wooliesPaymentDaoImpl.getPaymentOptions();
	}

	/**
	 * @return the wooliesPaymentDaoImpl
	 */
	public WooliesPaymentDaoImpl getWooliesPaymentDaoImpl()
	{
		return wooliesPaymentDaoImpl;
	}

	/**
	 * @param wooliesPaymentDaoImpl
	 *           the wooliesPaymentDaoImpl to set
	 */
	public void setWooliesPaymentDaoImpl(final WooliesPaymentDaoImpl wooliesPaymentDaoImpl)
	{
		this.wooliesPaymentDaoImpl = wooliesPaymentDaoImpl;
	}

	//
}
