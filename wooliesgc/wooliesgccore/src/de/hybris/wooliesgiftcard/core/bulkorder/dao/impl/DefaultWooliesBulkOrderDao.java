/**
 *
 */
package de.hybris.wooliesgiftcard.core.bulkorder.dao.impl;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.wooliesegiftcard.core.model.WWBulkOrderDataModel;
import de.hybris.wooliesegiftcard.core.model.WWBulkOrderItemsDataModel;
import de.hybris.wooliesgiftcard.core.bulkorder.dao.WooliesBulkOrderDao;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author 653930
 *
 */
public class DefaultWooliesBulkOrderDao extends AbstractItemDao implements WooliesBulkOrderDao
{
	static final String VALIDATE_SUCCESS = "VALIDATE_SUCCESS";
	static final String SUCCESS = "SUCCESS";

	@Override
	public Collection<WWBulkOrderItemsDataModel> getBulkOrderSavedData(final String refNo)
	{

		final Map<String, Object> params = new HashMap();
		final StringBuilder query = new StringBuilder(" SELECT {boi." + WWBulkOrderItemsDataModel.PK + "} FROM ");
		query.append(" {" + WWBulkOrderDataModel._TYPECODE + " AS bod ");
		query.append(" JOIN " + WWBulkOrderItemsDataModel._TYPECODE + " AS boi ON {boi.referenceNumber}={bod.referenceNumber} ");
		query.append("	JOIN BulkOrderStatus as bos ON {bod.bulkOrderStatus}={bos.pk}");
		query.append(" AND {bod.referenceNumber} = ?refNo AND {bos.code}=?bulkOrderStatus" + " }");
		params.put("refNo", refNo);
		params.put("bulkOrderStatus", VALIDATE_SUCCESS);
		final SearchResult<WWBulkOrderItemsDataModel> searchRes = search(query.toString(), params);
		return searchRes.getResult();
	}

	@Override
	public Collection<WWBulkOrderDataModel> getStatusOfBulkOrder(final String refNo)
	{
		final Map<String, Object> params = new HashMap();
		final StringBuilder query = new StringBuilder(" SELECT {" + WWBulkOrderDataModel.PK + "} ");
		query.append(" FROM {" + WWBulkOrderDataModel._TYPECODE + "  AS bod ");
		query.append("	JOIN BulkOrderStatus as bos ON {bod.bulkOrderStatus}={bos.pk}");
		query.append(" AND {referenceNumber} = ?referenceNo " + " }");
		params.put("referenceNo", refNo);
		final SearchResult<WWBulkOrderDataModel> searchRes = search(query.toString(), params);
		return searchRes.getResult();
	}


	/**
	 * This method is used to get old bulk order list
	 *
	 * @param uid
	 * @return the old bulk order list
	 */
	@Override
	public List<WWBulkOrderDataModel> getOldBulkOrder(final String uid)
	{
		final Map<String, Object> params = new HashMap();
		final StringBuilder query = new StringBuilder(" SELECT {" + WWBulkOrderDataModel.PK + "} ");
		query.append(" FROM {" + WWBulkOrderDataModel._TYPECODE + "  AS bod ");
		query.append("	JOIN BulkOrderStatus as bos ON {bod.bulkOrderStatus}={bos.pk}");
		query.append(" AND {bos.code}=?bulkOrderStatus" + " }");
		params.put("uid", uid);
		params.put("bulkOrderStatus", VALIDATE_SUCCESS);
		final SearchResult<WWBulkOrderDataModel> searchRes = search(query.toString(), params);
		return searchRes.getResult();
	}

	/**
	 * This method is used to get bulk items for the order
	 *
	 * @param referenceNumber
	 * @return the bulk items
	 */
	@Override
	public List<WWBulkOrderItemsDataModel> getBulkItemsOrder(final String referenceNumber)
	{
		final StringBuilder flexBuf = new StringBuilder();
		final Map<String, Object> queryParams = new HashMap();
		flexBuf.append("SELECT {PK} FROM {WWBulkOrderItemsData} where {referenceNumber} = ?referenceNumber");
		queryParams.put("referenceNumber", referenceNumber);
		final SearchResult<WWBulkOrderItemsDataModel> searchOrderModel = search(flexBuf.toString(), queryParams);
		return searchOrderModel.getResult();
	}

	/**
	 * This method is used to get bulk order for the given reference number
	 *
	 * @param referenceNumber
	 * @return the WWBulkOrderDataModel list
	 */
	@Override
	public List<WWBulkOrderDataModel> getBulkOrder(final String referenceNumber)
	{
		final StringBuilder flexBuf = new StringBuilder();
		final Map<String, Object> queryParams = new HashMap();
		flexBuf.append("SELECT {PK} FROM {WWBulkOrderData} where {referenceNumber} = ?referenceNumber");
		queryParams.put("referenceNumber", referenceNumber);
		final SearchResult<WWBulkOrderDataModel> bulkOrderModel = search(flexBuf.toString(), queryParams);
		return bulkOrderModel.getResult();
	}

	/**
	 * This method is used to get b2b customer infomation for the given id
	 *
	 * @param uid
	 * @return List<CustomerModel>
	 */
	@Override
	public List<CustomerModel> getB2BCustomerInfo(final String uid)
	{
		final StringBuilder flexBuf = new StringBuilder();
		final Map<String, Object> queryParams = new HashMap();
		flexBuf.append("SELECT {PK} FROM {customer} where {uid} = ?uid");
		queryParams.put("uid", uid);
		final SearchResult<CustomerModel> b2bCustomerModel = search(flexBuf.toString(), queryParams);
		return b2bCustomerModel.getResult();
	}


	/**
	 * This method is used to get products for the given code
	 *
	 * @param code
	 * @return the products
	 */
	@Override
	public List<ProductModel> getProducts(final String code)
	{
		final StringBuilder flexBuf = new StringBuilder();
		final Map<String, Object> queryParams = new HashMap();
		flexBuf.append("SELECT {PK} FROM {Product} where {code} = ?code");
		queryParams.put("code", code);
		final SearchResult<ProductModel> productModel = search(flexBuf.toString(), queryParams);
		return productModel.getResult();
	}

	/**
	 * This method is used to get product price for the given product id
	 *
	 * @param productId
	 * @return the product price
	 */
	@Override
	public List<PriceRowModel> getProductsPrice(final String productId)
	{
		final StringBuilder flexBuf = new StringBuilder();
		final Map<String, Object> queryParams = new HashMap();
		flexBuf.append("SELECT {PK} FROM {PriceRow} where {productId} = ?productId");
		queryParams.put("productId", productId);
		final SearchResult<PriceRowModel> priceRowModel = search(flexBuf.toString(), queryParams);
		return priceRowModel.getResult();
	}

}
