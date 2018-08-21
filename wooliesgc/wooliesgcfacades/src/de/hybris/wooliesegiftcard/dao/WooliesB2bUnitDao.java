/**
 *
 */
package de.hybris.wooliesegiftcard.dao;

import de.hybris.wooliesegiftcard.core.model.CorporateB2BCustomerModel;
import de.hybris.platform.b2b.model.B2BPermissionModel;
import java.util.List;


/**
 * @author 648156 This interface is used for B2B DAO
 *
 */
public interface WooliesB2bUnitDao
{

	/**
	 * This method is used to get B2B Admin details for the given admin uid
	 *
	 * @param adminUid
	 * @return corporateB2BCustomerModel
	 */
	List<CorporateB2BCustomerModel> getB2BAdmin(String adminUid);
/**
	 * @param userId
	 * @return
	 */
	public List<B2BPermissionModel> getOrderLimit(final String userId);

}
