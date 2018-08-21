/**
 *
 */
package de.hybris.wooliesgiftcard.core.bulkorder.dao;

import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.servicelayer.internal.dao.Dao;
import de.hybris.wooliesegiftcard.core.model.WWBulkOrderDataModel;
import de.hybris.wooliesegiftcard.core.model.WWBulkOrderItemsDataModel;

import java.util.Collection;
import java.util.List;


/**
 * @author 653930
 *
 */
public interface WooliesBulkOrderDao extends Dao
{
	/**
	 * @param refNo
	 * @return
	 * @throws CommerceCartModificationException
	 */
	public Collection<WWBulkOrderItemsDataModel> getBulkOrderSavedData(final String refNo)
			throws CommerceCartModificationException;

	/**
	 * @param refNo
	 * @return
	 */
	public Collection<WWBulkOrderDataModel> getStatusOfBulkOrder(final String refNo);

	/**
	 * @param uid
	 * @return
	 */
	public List<WWBulkOrderDataModel> getOldBulkOrder(final String uid);

	/**
	 * @param referenceNumber
	 * @return
	 */
	public List<WWBulkOrderItemsDataModel> getBulkItemsOrder(final String referenceNumber);

	/**
	 * @param referenceNumber
	 * @return
	 */
	public List<WWBulkOrderDataModel> getBulkOrder(final String referenceNumber);

	/**
	 * @param uid
	 * @return
	 */
	public List<CustomerModel> getB2BCustomerInfo(final String uid);

	/**
	 * @param code
	 * @return
	 */
	public List<ProductModel> getProducts(final String code);

	/**
	 * @param productId
	 * @return
	 */
	public List<PriceRowModel> getProductsPrice(final String productId);



}
