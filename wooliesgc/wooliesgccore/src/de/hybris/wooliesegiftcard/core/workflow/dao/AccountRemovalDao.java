/**
 *
 */
package de.hybris.wooliesegiftcard.core.workflow.dao;

import de.hybris.platform.core.model.user.CustomerModel;

import java.util.Date;
import java.util.List;


/**
 * @author 676313
 *
 */

public interface AccountRemovalDao
{
	/**
	 * This method used to findAllB2COlderThanSpecifiedDays
	 * 
	 * @param oldDate
	 * @return CustomerModel
	 */
	public List<CustomerModel> findAllB2COlderThanSpecifiedDays(final Date oldDate);
}
