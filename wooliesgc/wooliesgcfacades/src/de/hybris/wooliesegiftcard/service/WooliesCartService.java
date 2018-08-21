/**
 *
 */
package de.hybris.wooliesegiftcard.service;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.deliveryzone.model.ZoneDeliveryModeValueModel;
import de.hybris.wooliesegiftcard.facades.product.data.PersonalisationMediaData;

import java.util.Collection;


/**
 * @author 668982 This interface is to maintain cart service details
 */
public interface WooliesCartService
{
	/**
	 * This method is used to get delivery options for cart
	 *
	 * @param cart2
	 *           order model
	 * @return the ZoneDeliveryModeValueModel
	 */
	Collection<ZoneDeliveryModeValueModel> getdeliveryOptionsForCart(final AbstractOrderModel cart2);

	/**
	 * This method is used to generate pid
	 *
	 * @return CartModel user cart
	 */
	CartModel generatePID();

	/**
	 * This method is used save image for customer
	 *
	 * @param pIDNo
	 *           PID number
	 * @param url
	 *           url
	 * @return MediaData PersonalisationMediaData
	 */
	public PersonalisationMediaData saveImageForCustomer(int pIDNo, String url);

	/**
	 * This method is used to create card from absract order
	 *
	 * @param order
	 *           order
	 * @return CartModel
	 */
	CartModel createCartFromAbstractOrder(final AbstractOrderModel order);

	/**
	 * This method is used to update co brand data
	 *
	 * @param cartModel
	 *           user cart
	 * @param entryNumber
	 *           entry number
	 * @param coBrandID
	 *           coBrandID
	 * @return boolean value
	 */
	boolean updateCoBrandData(CartModel cartModel, int entryNumber, String coBrandID);

	/**
	 * This method is used to remove co brand data
	 *
	 * @param cartModel
	 *           user cart
	 * @param entryNumber
	 *           entry number
	 */
	void removeCoBrandData(CartModel cartModel, int entryNumber);

}
