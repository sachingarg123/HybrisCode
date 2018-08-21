/**
 *
 */
package de.hybris.wooliesegiftcard.service.impl;

import de.hybris.wooliesegiftcard.core.model.CorporateB2BCustomerModel;
import de.hybris.wooliesegiftcard.dao.impl.WooliesB2BUnitDaoImpl;
import de.hybris.wooliesegiftcard.service.WooliesB2BUnitService;
import de.hybris.platform.b2b.model.B2BPermissionModel;

import java.util.List;


/**
 * @author 648156 This class is used for B2b unit service
 */
public class WooliesB2BUnitServiceImpl implements WooliesB2BUnitService
{
	private WooliesB2BUnitDaoImpl wooliesB2BUnitDaoImpl;


	/**
	 * This method is used to get B2B admin details
	 *
	 * @param adminUid
	 * @return the b2b admin list
	 */
	@Override
	public List<CorporateB2BCustomerModel> getB2BAdmin(final String adminUid)
	{

		return wooliesB2BUnitDaoImpl.getB2BAdmin(adminUid);
	}


	/**
	 * @return the wooliesB2BUnitDaoImpl
	 */
	public WooliesB2BUnitDaoImpl getWooliesB2BUnitDaoImpl()
	{
		return wooliesB2BUnitDaoImpl;
	}


	/**
	 * @param wooliesB2BUnitDaoImpl
	 *           the wooliesB2BUnitDaoImpl to set
	 */
	public void setWooliesB2BUnitDaoImpl(final WooliesB2BUnitDaoImpl wooliesB2BUnitDaoImpl)
	{
		this.wooliesB2BUnitDaoImpl = wooliesB2BUnitDaoImpl;
	}

		/**
	 * @param userId
	 * @return B2BPermissionModel
	 */
	@Override
	public List<B2BPermissionModel> orderLimitUpdate(final String userId)
	{
		return wooliesB2BUnitDaoImpl.getOrderLimit(userId);
	}

}
