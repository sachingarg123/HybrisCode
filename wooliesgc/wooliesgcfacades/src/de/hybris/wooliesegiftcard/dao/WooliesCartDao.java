/**
 *
 */
package de.hybris.wooliesegiftcard.dao;

import de.hybris.model.PersonalisationMediaModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.deliveryzone.model.ZoneDeliveryModeValueModel;
import de.hybris.platform.store.BaseStoreModel;

import java.util.Collection;
import java.util.List;


/**
 * @author 668982 This interface is used to maintain cart related database activities
 *
 */
public interface WooliesCartDao
{
	/**
	 * This method is used to find the delivery modes for the given order and total price
	 *
	 * @param abstractOrder
	 * @param totalPrice
	 * @return zoneDeliveryModeValueModel
	 */
	Collection<ZoneDeliveryModeValueModel> findDeliveryModes(final AbstractOrderModel abstractOrder, final Long totalPrice);

	/**
	 * This method is used to find delivery mode for eGift product based on the order
	 *
	 * @param abstractOrder
	 * @return zoneDeliveryModeValueModel
	 */
	Collection<ZoneDeliveryModeValueModel> findDeliveryModeForeGiftProduct(final AbstractOrderModel abstractOrder);

	/**
	 * This method is used to get cart details based on uid
	 *
	 * @param uid
	 * @param baseStore
	 * @return list of cartModels
	 */
	List<CartModel> getCartByUID(final String uid, final BaseStoreModel baseStore);

	/**
	 * This method is used to get image details based on imageid
	 *
	 * @param imageID
	 * @return list of media Models
	 */
	List<PersonalisationMediaModel> getImageByID(String imageID);

	/**
	 * This method is used to get cart details based on guid
	 *
	 * @param guid
	 * @return list of cartModels
	 */
	List<CartModel> getCartByGUID(String guid);
}
