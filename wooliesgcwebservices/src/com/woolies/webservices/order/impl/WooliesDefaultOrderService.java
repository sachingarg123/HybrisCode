/**
 *
 */
package com.woolies.webservices.order.impl;

import de.hybris.model.PersonalisationEGiftCardModel;
import de.hybris.model.PersonalisationMediaModel;
import de.hybris.platform.b2b.model.B2BBudgetModel;
import de.hybris.platform.b2b.model.B2BCostCenterModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.order.payment.PaymentModeModel;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.order.impl.DefaultOrderService;
import de.hybris.platform.order.strategies.SubmitOrderStrategy;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BCustomerModel;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BUnitModel;
import de.hybris.wooliesegiftcard.facades.constants.WooliesgcFacadesConstants;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;


/**
 * @author 668982
 *
 */
public class WooliesDefaultOrderService extends DefaultOrderService
{
	private static final Logger LOG = Logger.getLogger(WooliesDefaultOrderService.class);

	private BusinessProcessService businessProcessService;
	private KeyGenerator keyGenerator;

	/**
	 * @return the keyGenerator
	 */
	public KeyGenerator getKeyGenerator()
	{
		return keyGenerator;
	}

	/**
	 * @param keyGenerator
	 *           the keyGenerator to set
	 */
	public void setKeyGenerator(final KeyGenerator keyGenerator)
	{
		this.keyGenerator = keyGenerator;
	}

	/**
	 * @return the businessProcessService
	 */
	public BusinessProcessService getBusinessProcessService()
	{
		return businessProcessService;
	}

	/**
	 * @param businessProcessService
	 *           the businessProcessService to set
	 */
	public void setBusinessProcessService(final BusinessProcessService businessProcessService)
	{
		this.businessProcessService = businessProcessService;
	}

	/**
	 * @return the baseStoreService
	 */
	public BaseStoreService getBaseStoreService()
	{
		return baseStoreService;
	}

	/**
	 * @param baseStoreService
	 *           the baseStoreService to set
	 */
	public void setBaseStoreService(final BaseStoreService baseStoreService)
	{
		this.baseStoreService = baseStoreService;
	}

	/**
	 * @return the modelService
	 */
	@Override
	public ModelService getModelService()
	{
		return modelService;
	}

	/**
	 * @param modelService
	 *           the modelService to set
	 */
	@Override
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	private BaseStoreService baseStoreService;
	private ModelService modelService;

	/**
	 * @return the submitOrderStrategies
	 */
	public List<SubmitOrderStrategy> getSubmitOrderStrategies()
	{
		return submitOrderStrategies;
	}

	/**
	 * @param submitOrderStrategies
	 *           the submitOrderStrategies to set
	 */
	@Override
	public void setSubmitOrderStrategies(final List<SubmitOrderStrategy> submitOrderStrategies)
	{
		this.submitOrderStrategies = submitOrderStrategies;
	}

	private List<SubmitOrderStrategy> submitOrderStrategies = Collections.emptyList();

	@Override
	public OrderModel createOrderFromCart(final CartModel cart) throws InvalidCartException
	{
		final OrderModel order = super.createOrderFromCart(cart);
		if (order != null)
		{
			final PaymentInfoModel paymentInfoModel = order.getPaymentInfo();
			if (null != paymentInfoModel)
			{
				LOG.info("paymentInfo==" + paymentInfoModel.getPaymentOption().getCode());
				if (order.getUser() instanceof CorporateB2BCustomerModel)
				{
					updateCreditLimit(order);
				}
			}
			order.setOrderToken((String) this.keyGenerator.generate());
			for (final AbstractOrderEntryModel model : order.getEntries())
			{
				final AbstractOrderEntryModel orderEntry = cart.getEntries().get(model.getEntryNumber());
				final List<PersonalisationEGiftCardModel> personalisationDetails = orderEntry.getPersonalisationDetail();
				if (CollectionUtils.isNotEmpty(personalisationDetails))
				{
					model.setPersonalisationDetail(personalisationDetails);
					getModelService().save(orderEntry);
					getModelService().refresh(orderEntry);
					checkPendingImages(model);
				}
			}
			final OrderProcessModel orderProcessModel = businessProcessService.createProcess(
					"orderConfirmationEmailProcess-" + order.getCode() + "-" + System.currentTimeMillis(),
					"orderConfirmationEmailProcess");
			orderProcessModel.setOrder(order);
			getModelService().save(orderProcessModel);
			businessProcessService.startProcess(orderProcessModel);
		}
		return order;
	}

	/**
	 * @param order
	 */
	private void updateCreditLimit(final OrderModel order)
	{
		final PaymentModeModel paymentMode = order.getPaymentMode();
		if (paymentMode.getName().equals(WooliesgcFacadesConstants.ON_ACCOUNT))
		{
			final CorporateB2BCustomerModel customerModel = (CorporateB2BCustomerModel) order.getUser();
			if (customerModel.getDefaultB2BUnit() != null)
			{
				final CorporateB2BUnitModel b2bUnit = (CorporateB2BUnitModel) customerModel.getDefaultB2BUnit();
				final List<B2BCostCenterModel> b2bCostCenters = b2bUnit.getCostCenters();
				saveBudgetModel(order, b2bCostCenters);
			}
		}
	}

	/**
	 * @param order
	 * @param b2bCostCenters
	 */
	private void saveBudgetModel(final OrderModel order, final List<B2BCostCenterModel> b2bCostCenters)
	{
		for (final B2BCostCenterModel b2bCostCenterModel : b2bCostCenters)
		{
			final Set<B2BBudgetModel> currentBudget = b2bCostCenterModel.getBudgets();
			for (final B2BBudgetModel b2bBudgetMdel : currentBudget)
			{
				b2bBudgetMdel.setBudget(b2bBudgetMdel.getBudget().subtract(BigDecimal.valueOf(order.getTotalPrice())));
				getModelService().save(b2bBudgetMdel);
			}
		}
	}

	/**
	 * @param model
	 */
	private void checkPendingImages(final AbstractOrderEntryModel model)
	{
		for (final PersonalisationEGiftCardModel eachEGift : model.getPersonalisationDetail())
		{
			if (eachEGift.getCustomerImage() != null)
			{
				final PersonalisationMediaModel mediaModel = eachEGift.getCustomerImage();
				if (mediaModel != null)
				{
					mediaModel.setHasOrder(true);
				}
				getModelService().save(mediaModel);
			}
		}

	}


	/**
	 *
	 * @param order
	 */
	@Override
	public void submitOrder(final OrderModel order)
	{
		ServicesUtil.validateParameterNotNullStandardMessage("event.order", order);
		BaseStoreModel store = order.getStore();
		if (store == null)
		{
			store = getBaseStoreService().getCurrentBaseStore();
		}

		if (store == null)
		{
			LOG.warn("Unable to start fulfilment process for order [" + order.getCode()
					+ "]. Store not set on Order and no current base store defined in session.");
		}
		else
		{
			final String fulfilmentProcessDefinitionName = store.getSubmitOrderProcessCode();
			if (fulfilmentProcessDefinitionName == null || fulfilmentProcessDefinitionName.isEmpty())
			{
				LOG.warn("Unable to start fulfilment process for order [" + order.getCode() + "]. Store [" + store.getUid()
						+ "] has missing SubmitOrderProcessCode");
			}
			else
			{
				final String processCode = fulfilmentProcessDefinitionName + "-" + order.getCode() + "-" + System.currentTimeMillis();
				final OrderProcessModel businessProcessModel = getBusinessProcessService().createProcess(processCode,
						fulfilmentProcessDefinitionName);
				businessProcessModel.setOrder(order);
				getModelService().save(businessProcessModel);
				getBusinessProcessService().startProcess(businessProcessModel);
				if (LOG.isInfoEnabled())
				{
					LOG.info(String.format("Started the process %s", processCode));
				}
			}
		}
	}
}