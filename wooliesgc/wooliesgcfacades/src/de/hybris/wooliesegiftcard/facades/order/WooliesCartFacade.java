/**
 *
 */
package de.hybris.wooliesegiftcard.facades.order;

import de.hybris.platform.commercefacades.order.data.CartModificationData;
import de.hybris.platform.commercefacades.order.data.CartRestorationData;
import de.hybris.platform.commercefacades.order.data.DeliveryModeData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commerceservices.order.CommerceCartRestorationException;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.wooliesegiftcard.core.model.WWBulkOrderDataModel;
import de.hybris.wooliesegiftcard.core.model.WWBulkOrderItemsDataModel;
import de.hybris.wooliesegiftcard.exceptions.WooliesFacadeLayerException;
import de.hybris.wooliesegiftcard.facades.product.data.PersonalisationMediaData;
import de.hybris.wooliesgiftcard.core.bulkorder.service.WooliesBulkOrderService;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.woolies.webservices.dto.eGiftCardWsDTO;


/**
 * @author 668982 This interface is used to maintain cart details
 */
public interface WooliesCartFacade
{
	/**
	 *
	 * This method is used add cart
	 *
	 * @param code
	 *           the parameter value to be used
	 * @param quantity
	 *           the parameter value to be used
	 * @param priceGivenByCustomer
	 *           the parameter value to be used
	 * @return CartModificationData the parameter used to return
	 * @throws CommerceCartModificationException
	 *            used to throw exception
	 */
	CartModificationData addToCart(final String code, final long quantity, final Double priceGivenByCustomer)
			throws CommerceCartModificationException;

	/**
	 * This method is used add cart
	 *
	 * @param code
	 *           the parameter value to be used
	 * @param qty
	 *           the parameter value to be used
	 * @param pickupStore
	 *           the parameter value to be used
	 * @param priceGivenByCustomer
	 *           the parameter value to be used
	 * @return CartModificationData the parameter used to return
	 * @throws CommerceCartModificationException
	 *            used to throw exception
	 */
	CartModificationData addToCart(String code, long qty, String pickupStore, Double priceGivenByCustomer)
			throws CommerceCartModificationException;

	/**
	 * This method is used to add cart
	 * 
	 * @param entryNumber
	 *           the parameter value to be used
	 * @param quantity
	 *           the parameter value to be used
	 * @param customerPrice
	 *           customerPrice
	 * @return CartModificationData the parameter used to return
	 * @throws CommerceCartModificationException
	 *            used to throw exception
	 */
	CartModificationData updateCartEntry(long entryNumber, long quantity, Double customerPrice)
			throws CommerceCartModificationException;

	/**
	 * This method is used to get delivery mode
	 *
	 * @return the delivermode data
	 */
	List<DeliveryModeData> getDeliveryMode();

	/**
	 * This method is used to get the cart details
	 *
	 * @return the cartModel
	 */
	CartModel getCart();

	/**
	 * This method is used to get the pid for gift card for the given entry number
	 *
	 * @param entryNumber
	 *           entryNumber
	 * @return OrderEntryData
	 */
	OrderEntryData generatePIDForeGiftCard(final AbstractOrderEntryModel entryNumber);

	/**
	 * This method is used to apply personalization for gift card
	 *
	 * @param cartModel
	 *           the parameter used to be used
	 * @param abstractOrderEntryModel
	 *           abstractOrderEntryModel
	 * @param PIDNo
	 *           PIDNo
	 * @param eGiftCardCustomization
	 *           eGiftCardCustomization
	 * @return OrderEntryData
	 */
	OrderEntryData applyPersnalisationForeGiftCard(final CartModel cartModel,
			final AbstractOrderEntryModel abstractOrderEntryModel, final int PIDNo, final eGiftCardWsDTO eGiftCardCustomization);

	/**
	 * This method is used to remove PID's
	 *
	 * @param cartModel
	 *           user cart
	 * @param entryNumber
	 *           order entry number
	 * @throws WooliesFacadeLayerException
	 *            used to throw exception
	 */
	public void removePersonalisationForEgiftCard(final CartModel cartModel, final int entryNumber)
			throws WooliesFacadeLayerException;

	/**
	 * To save image for customer
	 *
	 * @param pIDNo
	 *           pid number
	 * @param url
	 *           url
	 * @return MediaData PersonalisationMediaData
	 */
	PersonalisationMediaData saveImageForCustomer(int pIDNo, String url);

	//cart merge functionality
	/**
	 * This method is used to restore anonymous user
	 *
	 * @param guid
	 *           guid
	 * @return cartRestorationData
	 * @throws CommerceCartRestorationException
	 *            used to throw exception
	 */
	CartRestorationData restoreWooliesAnonymousCartAndTakeOwnership(final String guid) throws CommerceCartRestorationException;

	/**
	 * This method is used to update cob brand data
	 *
	 * @param cartModel
	 *           user cart
	 * @param entryNumber
	 *           order entry
	 * @param coBrandID
	 *           cobrand id
	 * @return boolean
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

	/**
	 * This method is used to add items in cart for bulk order
	 *
	 * @param wooliesBulkOrderService
	 *           WooliesBulkOrderService
	 * @param bulkOrderDataCollection
	 *           List of WWBulkOrderItemsDataModel
	 * @param wwStatusOfBulkOrderData
	 *           WWBulkOrderDataModel
	 * @param qnty
	 *           quantity
	 * @throws CommerceCartModificationException
	 *            used to throw exception
	 */
	public void addToCartforBulkOrder(final WooliesBulkOrderService wooliesBulkOrderService,
			final Collection<WWBulkOrderItemsDataModel> bulkOrderDataCollection, final WWBulkOrderDataModel wwStatusOfBulkOrderData,
			final int qnty) throws CommerceCartModificationException;

	/**
	 * This method is used to set Personalization data for Bulk Order
	 *
	 * @param coreItems
	 *           Map<Integer, WWBulkOrderItemsDataModel>
	 * @param cartModel
	 *           user cart
	 * @param entryNumber
	 *           integer number
	 */
	public void setPersonalisationData(final Map<Integer, WWBulkOrderItemsDataModel> coreItems, final CartModel cartModel,
			final int entryNumber);
}
