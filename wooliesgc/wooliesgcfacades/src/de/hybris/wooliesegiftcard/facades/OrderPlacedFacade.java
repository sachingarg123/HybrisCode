/**
 *
 */
package de.hybris.wooliesegiftcard.facades;

import de.hybris.platform.commercefacades.order.OrderFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.wooliesegiftcard.exceptions.WooliesFacadeLayerException;
import de.hybris.wooliesegiftcard.facades.dto.FraudOrderStatusRequest;


/**
 * @author 264343 This interface is used to maintain placed order
 */
public interface OrderPlacedFacade extends OrderFacade
{
	/**
	 * This method is used to save shipping address
	 *
	 * @param addressData
	 *           the addressdata associated with place order
	 * @param cartModel
	 *           the Cart of the User
	 * @throws WooliesFacadeLayerException
	 *            throws this Exception
	 */
	void saveShippingAddress(AddressData addressData, CartModel cartModel) throws WooliesFacadeLayerException;

	/**
	 * This method is used to get the cart for the given uid
	 *
	 * @param uid
	 *           the Cart Id
	 * @return CartModel for the corresponding UID
	 */
	CartModel getCart(String uid);

	/**
	 * This method is used to get the cart details for anonymous guest uid
	 *
	 * @param guid
	 *           the Id of the Guest User
	 * @return cartModel the Cart Associated with the Guest User
	 */
	CartModel getCartForAnonymous(String guid);

	/**
	 * This method is used to set the Order Status
	 *
	 * @param fraudOrderRequest
	 *           fraudOrderRequest as a parameter
	 * @throws WooliesFacadeLayerException
	 *            throws this exception in case of error
	 */
	public void setOrderStatus(FraudOrderStatusRequest fraudOrderRequest) throws WooliesFacadeLayerException;

	/**
	 * This method is used to get the EGiftCard Details
	 *
	 * @param eToken
	 *           the Token for encryption and decryption
	 * @return the GiftCardResponseData
	 */
	GiftCardResponseData getEgiftCardDetails(String eToken);

}
