/**
 *
 */
package de.hybris.wooliesgiftcard.core.bulkorder.service.impl;

import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.wooliesegiftcard.core.enums.BulkOrderStatus;
import de.hybris.wooliesegiftcard.core.model.WWBulkOrderDataModel;
import de.hybris.wooliesegiftcard.core.model.WWBulkOrderItemsDataModel;
import de.hybris.wooliesgiftcard.core.bulkorder.dao.WooliesBulkOrderDao;
import de.hybris.wooliesgiftcard.core.bulkorder.service.WooliesBulkOrderService;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * @author 653930
 *
 */
public class DefaultWooliesBulkOrderService implements WooliesBulkOrderService
{
	WooliesBulkOrderDao wooliesBulkOrderDao;
	private ModelService modelService;

	@Override
	public Collection<WWBulkOrderItemsDataModel> getBulkOrderSavedData(final String refNo) throws CommerceCartModificationException
	{

		return getWooliesBulkOrderDao().getBulkOrderSavedData(refNo);
	}

	@Override
	public WWBulkOrderDataModel getStatusOfBulkOrder(final String refNo) throws CommerceCartModificationException
	{


		final Collection<WWBulkOrderDataModel> bulkOrderDataCollection = getWooliesBulkOrderDao().getStatusOfBulkOrder(refNo);

		if (null != bulkOrderDataCollection && !bulkOrderDataCollection.isEmpty())
		{

			return bulkOrderDataCollection.iterator().next();
		}
		else
		{
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.wooliesgiftcard.core.bulkorder.service.WooliesBulkOrderService#removeBulkOrder(java.lang.String)
	 */
	@Override
	public void updateOldBulkOrderStatus(final String uid)
	{
		final List<WWBulkOrderDataModel> bulkOrderModelData = wooliesBulkOrderDao.getOldBulkOrder(uid);
		if (CollectionUtils.isNotEmpty(bulkOrderModelData))
		{
			for (final WWBulkOrderDataModel wwBulkOrderData : bulkOrderModelData)
			{
				wwBulkOrderData.setBulkOrderStatus(BulkOrderStatus.VALIDATE_FAILED);
				modelService.save(wwBulkOrderData);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.wooliesgiftcard.core.bulkorder.service.WooliesBulkOrderService#getB2BCustomerInfo(java.lang.String)
	 */
	@Override
	public List<CustomerModel> getB2BCustomerInfo(final String uid)
	{
		return wooliesBulkOrderDao.getB2BCustomerInfo(uid);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.wooliesgiftcard.core.bulkorder.service.WooliesBulkOrderService#getBulkItemsOrder(java.lang.String)
	 */
	@Override
	public List<WWBulkOrderItemsDataModel> getBulkItemsOrder(final String referenceNumber)
	{
		return wooliesBulkOrderDao.getBulkItemsOrder(referenceNumber);
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.wooliesgiftcard.core.bulkorder.service.WooliesBulkOrderService#getBulkOrder(java.lang.String)
	 */
	@Override
	public List<WWBulkOrderDataModel> getBulkOrder(final String referenceNumber)
	{
		return wooliesBulkOrderDao.getBulkOrder(referenceNumber);
	}


	@Override
	public List<ProductModel> getProducts(final String code)
	{
		return wooliesBulkOrderDao.getProducts(code);
	}

	@Override
	public List<PriceRowModel> getProductsPrice(final String productId)
	{
		return wooliesBulkOrderDao.getProductsPrice(productId);
	}

	@Override
	public void saveModel(final Object obj)
	{

		getModelService().save(obj);
	}


	/**
	 * @return the wooliesBulkOrderDao
	 */
	public WooliesBulkOrderDao getWooliesBulkOrderDao()
	{
		return wooliesBulkOrderDao;
	}

	/**
	 * @param wooliesBulkOrderDao
	 *           the wooliesBulkOrderDao to set
	 */
	public void setWooliesBulkOrderDao(final WooliesBulkOrderDao wooliesBulkOrderDao)
	{
		this.wooliesBulkOrderDao = wooliesBulkOrderDao;
	}

	public ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}









}
