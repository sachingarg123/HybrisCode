/**
 *
 */
package de.hybris.wooliesegiftcard.core.genric.dao;

import de.hybris.model.EGiftCardModel;
import de.hybris.model.PersonalisationMediaModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.AddressModel;

import java.util.List;


/**
 * @author 687679 Dao to maintain address
 */
public interface WooliesGenericDao
{

	public List<AddressModel> getAddressModelByAddressId(String addressId);

	List<PersonalisationMediaModel> getImagesforDelete();

	/**
	 * @return
	 */
	List<OrderModel> getOrdersForPaymentCancellation();

	/**
	 * @return
	 */
	List<OrderModel> getOrdersForEmailNotification();

	/**
	 * @return
	 */
	List<EGiftCardModel> getEgiftCardDetails(String orderID);
}
