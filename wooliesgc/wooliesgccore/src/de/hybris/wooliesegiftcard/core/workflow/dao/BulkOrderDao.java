/**
 *
 */
package de.hybris.wooliesegiftcard.core.workflow.dao;

/**
 * @author 676313
 *
 */

import de.hybris.wooliesegiftcard.core.model.WWBulkOrderDataModel;
import de.hybris.wooliesegiftcard.core.model.WWBulkOrderItemsDataModel;

import java.util.Collection;
import java.util.List;
import java.util.Date;

public interface BulkOrderDao
{



	public Collection<WWBulkOrderDataModel> successAndvalidationfailure();


	public Collection<WWBulkOrderDataModel> inProcessandvalidationsuccess(final Date oldTimes);

	public Collection<WWBulkOrderItemsDataModel> itemsProcessandvalidationsuccess(List<String> refnumbers, final Date oldTimes);

	/**
	 * @param refnumbers
	 * @return
	 */
	public Collection<WWBulkOrderItemsDataModel> itemsSuccessandvalidationfailure(List<String> refnumbers);
}
