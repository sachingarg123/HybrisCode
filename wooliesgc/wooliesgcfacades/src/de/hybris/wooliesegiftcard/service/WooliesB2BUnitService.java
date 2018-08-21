/**
 *
 */
package de.hybris.wooliesegiftcard.service;

import de.hybris.platform.b2b.model.B2BPermissionModel;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BCustomerModel;

import java.util.List;


/**
 * @author 648156 This interface is used for B2b unit service
 *
 */
public interface WooliesB2BUnitService
{
	/**
	 * This method is used to get B2B admin details
	 *
	 * @param adminUid
	 *           the value to be used
	 * @return the b2b admin list
	 */
	List<CorporateB2BCustomerModel> getB2BAdmin(final String adminUid);

	/**
	 * This method is used to get b2b permission model list
	 * 
	 * @param userId
	 *           the value to be used
	 * @return the b2b permission model list
	 */
	public List<B2BPermissionModel> orderLimitUpdate(final String userId);
}
