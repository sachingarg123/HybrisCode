/**
 *
 */
package de.hybris.wooliesgiftcard.core.bulkorder.service;

import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.wooliesegiftcard.core.model.WWBulkOrderDataModel;
import de.hybris.wooliesegiftcard.core.model.WWBulkOrderItemsDataModel;

import java.util.Collection;
import java.util.List;


/**
 * @author 653930
 *
 */

public interface WooliesBulkOrderService
{
	/**
	 * This method is used to get the getBulkOrderSavedData For refNo
	 *
	 * @param refNo
	 *           the parameter value to be used
	 * @return WWBulkOrderItemsDataModel
	 * @throws CommerceCartModificationException
	 *            used to throw exception
	 */
	public Collection<WWBulkOrderItemsDataModel> getBulkOrderSavedData(final String refNo)
			throws CommerceCartModificationException;

	/**
	 * This method is used to get the getStatusOfBulkOrder For refNo
	 *
	 * @param refNo
	 *           the parameter value to be used
	 * @return WWBulkOrderDataModel
	 * @throws CommerceCartModificationException
	 *            used to throw exception
	 */
	public WWBulkOrderDataModel getStatusOfBulkOrder(final String refNo) throws CommerceCartModificationException;

	/**
	 * This method is used to updateStatusOfBulkOrder.
	 *
	 * @param uid
	 *           the parameter value to be used
	 */
	public void updateOldBulkOrderStatus(final String uid);

	/**
	 * This method is used to getB2BCustomerInfo
	 *
	 * @param uid
	 *           the parameter value to be used
	 * @return CustomerModel
	 */
	public List<CustomerModel> getB2BCustomerInfo(final String uid);

	/**
	 * This method is used to getBulkItemsOrder
	 *
	 * @param referenceNumber
	 *           the parameter value to be used
	 *
	 * @return getBulkItemsOrder
	 */
	public List<WWBulkOrderItemsDataModel> getBulkItemsOrder(final String referenceNumber);

	/**
	 * This method is used to getBulkOrder
	 *
	 * @param referenceNumber
	 *           the parameter value to be used
	 * @return getBulkOrder
	 */
	public List<WWBulkOrderDataModel> getBulkOrder(final String referenceNumber);

	/**
	 * This method is used to getProducts
	 *
	 * @param code
	 *           the parameter value to be used
	 * @return ProductModel
	 */
	public List<ProductModel> getProducts(final String code);

	/**
	 * This method is used to getProductsPrice
	 *
	 * @param productId
	 *           the parameter value to be used
	 * @return PriceRowModel
	 */
	public List<PriceRowModel> getProductsPrice(final String productId);

	/**
	 * This method is used to saveModel
	 *
	 * @param cart
	 *           the parameter value to be used
	 */
	public void saveModel(Object cart);

}
