/**
 *
 */
package com.woolies.webservices.order.impl;

import de.hybris.platform.b2b.model.B2BOrderThresholdPermissionModel;
import de.hybris.platform.b2b.model.B2BPermissionModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.price.DiscountModel;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.deliveryzone.model.ZoneDeliveryModeValueModel;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.order.impl.DefaultCalculationService;
import de.hybris.platform.order.strategies.calculation.FindDeliveryCostStrategy;
import de.hybris.platform.order.strategies.calculation.FindPaymentCostStrategy;
import de.hybris.platform.order.strategies.calculation.OrderRequiresCalculationStrategy;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.DiscountValue;
import de.hybris.platform.util.PriceValue;
import de.hybris.platform.util.TaxValue;
import de.hybris.wooliesegiftcard.core.enums.UserDataType;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BCustomerModel;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BUnitModel;
import de.hybris.wooliesegiftcard.core.model.MemberCustomerModel;
import de.hybris.wooliesegiftcard.facades.constants.WooliesgcFacadesConstants;
import de.hybris.wooliesegiftcard.service.impl.WooliesDefaultCartService;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;





/**
 * @author 668982 This class is used as calculation service
 *
 */
public class WooliesDefaultCalculationService extends DefaultCalculationService
{
	private static final String ERRCODE_ORDERCONFIRMATION_ORDER_NOT_FOUND = "50002";
	private static final String ERRCODE_CUSTOMERPRICE = "40002";
	private FindDeliveryCostStrategy findDeliveryCostStrategy;
	private FindPaymentCostStrategy findPaymentCostStrategy;
	private static final String ERRCODE_MULTIPRICE = "50012";


	private UserService userService;

	/**
	 * @return the userService
	 */
	public UserService getUserService()
	{
		return userService;
	}

	/**
	 * @param userService
	 *           the userService to set
	 */
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	@Autowired
	private OrderRequiresCalculationStrategy orderRequiresCalculationStrategy;
	@Autowired
	private CommonI18NService commonI18NService;

	/**
	 * @return the findDeliveryCostStrategy
	 */
	public FindDeliveryCostStrategy getFindDeliveryCostStrategy()
	{
		return findDeliveryCostStrategy;
	}

	/**
	 * @param findDeliveryCostStrategy
	 *           the findDeliveryCostStrategy to set
	 */
	@Override
	public void setFindDeliveryCostStrategy(final FindDeliveryCostStrategy findDeliveryCostStrategy)
	{
		this.findDeliveryCostStrategy = findDeliveryCostStrategy;
	}

	/**
	 * @return the findPaymentCostStrategy
	 */
	public FindPaymentCostStrategy getFindPaymentCostStrategy()
	{
		return findPaymentCostStrategy;
	}

	/**
	 * @param findPaymentCostStrategy
	 *           the findPaymentCostStrategy to set
	 */
	@Override
	public void setFindPaymentCostStrategy(final FindPaymentCostStrategy findPaymentCostStrategy)
	{
		this.findPaymentCostStrategy = findPaymentCostStrategy;
	}

	private WooliesDefaultCartService cartService;

	public WooliesDefaultCartService getCartService()
	{
		return cartService;
	}

	/**
	 * @param cartService
	 *           the cartService to set
	 */
	public void setCartService(final WooliesDefaultCartService cartService)
	{
		this.cartService = cartService;
	}

	/**
	 * This method is used to reset all the values for the given order entry model
	 *
	 * @param entry
	 * @throws CalculationException
	 */
	@Override
	protected void resetAllValues(final AbstractOrderEntryModel entry) throws CalculationException
	{
		final Collection<TaxValue> entryTaxes = findTaxValues(entry);
		entry.setTaxValues(entryTaxes);
		PriceValue priceValue = null;
		try
		{
			final UserModel user = entry.getOrder().getUser();
			final Collection<PriceRowModel> europeanPrices = entry.getProduct().getEurope1Prices();
			if (CollectionUtils.isNotEmpty(europeanPrices) && europeanPrices.size() > 1)
			{
				priceValue = multiPriceForProduct(entry, entryTaxes, priceValue, user, europeanPrices);
			}
			else
			{
				priceValue = findBasePrice(entry);
				final AbstractOrderModel order = entry.getOrder();
				final PriceValue basePrice = convertPriceIfNecessary(priceValue, order.getNet().booleanValue(), order.getCurrency(),
						entryTaxes);
				setBasePrice(entry, basePrice);
			}
			if (((CustomerModel) entry.getOrder().getUser()).getCustomerType() == UserDataType.B2B)
			{
				final CorporateB2BCustomerModel corporateb2bCustomer = (CorporateB2BCustomerModel) entry.getOrder().getUser();
				final Set<PrincipalGroupModel> allGroups = corporateb2bCustomer.getAllGroups();
				Double thresholdValue = null;
				final CorporateB2BUnitModel corporateb2BUnit = (CorporateB2BUnitModel) corporateb2bCustomer.getDefaultB2BUnit();
				final Set<B2BPermissionModel> b2bPermissionModel = corporateb2BUnit.getPermissions();
				if (!b2bPermissionModel.isEmpty())
				{
					thresholdValue = getthresholdValue(corporateb2bCustomer, thresholdValue, b2bPermissionModel);
				}
				if (!allGroups.contains(userService.getUserGroupForUID(WooliesgcFacadesConstants.B2BADMINGROUP))
						&& null != thresholdValue)
				{
					final List<AbstractOrderEntryModel> orderEntries = entry.getOrder().getEntries();
					double totalPrice = 0;
					totalPrice = calculateTotalPrice(entry, orderEntries, totalPrice);
					caompareTotalPrice(thresholdValue, totalPrice);
				}
			}
			final List<DiscountValue> entryDiscounts = findDiscountValues(entry);
			entry.setDiscountValues(entryDiscounts);
		}
		catch (final CalculationException e)
		{
			catchBlock(entry, priceValue, e);
		}
	}

	/**
	 * This method is used to compare the total price
	 *
	 * @param thresholdValue
	 * @param totalPrice
	 * @throws CalculationException
	 */
	private void caompareTotalPrice(final Double thresholdValue, final double totalPrice) throws CalculationException
	{
		if (thresholdValue != null && totalPrice > thresholdValue.doubleValue())
		{

			throw new CalculationException(ERRCODE_ORDERCONFIRMATION_ORDER_NOT_FOUND);

		}
	}

	/**
	 * @param corporateb2bCustomer
	 * @param thresholdValue
	 * @param b2bPermissionModel
	 * @return
	 */
	private Double getthresholdValue(final CorporateB2BCustomerModel corporateb2bCustomer, Double thresholdValue,
			final Set<B2BPermissionModel> b2bPermissionModel)
	{
		for (final B2BPermissionModel b2bPermission : b2bPermissionModel)
		{
			if (b2bPermission.getCode().equalsIgnoreCase(corporateb2bCustomer.getUid()))
			{
				final B2BOrderThresholdPermissionModel b2bOrderPermissionModel = (B2BOrderThresholdPermissionModel) b2bPermission;
				thresholdValue = b2bOrderPermissionModel.getThreshold();
			}
		}
		return thresholdValue;
	}

	/**
	 * @param entry
	 * @param entryTaxes
	 * @param priceValue
	 * @param user
	 * @param europeanPrices
	 * @return
	 * @throws CalculationException
	 */
	private PriceValue multiPriceForProduct(final AbstractOrderEntryModel entry, final Collection<TaxValue> entryTaxes,
			PriceValue priceValue, final UserModel user, final Collection<PriceRowModel> europeanPrices) throws CalculationException
	{
		if (user instanceof MemberCustomerModel)
		{
			memberUserEntry(entry, europeanPrices);
		}
		else if (entry.getCustomPrice().doubleValue() > 0)
		{
			boolean hasZeroPrice = false;
			for (final PriceRowModel priceRowModel : europeanPrices)
			{
				final double dbPrice = priceRowModel.getPrice().doubleValue();
				if (Double.compare(dbPrice, 0.0) == 0)
				{
					hasZeroPrice = true;
					entry.setBasePrice(entry.getCustomPrice());
					break;
				}
			}
			if (!hasZeroPrice)
			{
				priceValue = findBasePrice(entry);
				final AbstractOrderModel order = entry.getOrder();
				final PriceValue basePrice = convertPriceIfNecessary(priceValue, order.getNet().booleanValue(), order.getCurrency(),
						entryTaxes);
				setBasePrice(entry, basePrice);
			}
		}
		else
		{
			throw new CalculationException(ERRCODE_CUSTOMERPRICE);
		}
		return priceValue;
	}

	/**
	 * @param entry
	 * @param europeanPrices
	 * @throws CalculationException
	 */
	private void memberUserEntry(final AbstractOrderEntryModel entry, final Collection<PriceRowModel> europeanPrices)
			throws CalculationException
	{
		boolean isPriceAvailable = false;
		for (final PriceRowModel priceRowModel : europeanPrices)
		{
			final double dbPrice = priceRowModel.getPrice().doubleValue();
			if (Double.compare(entry.getCustomPrice().doubleValue(), dbPrice) == 0)
			{
				isPriceAvailable = true;
				entry.setBasePrice(entry.getCustomPrice());
				break;
			}
		}
		if (!isPriceAvailable)
		{
			throw new CalculationException(ERRCODE_MULTIPRICE);
		}
	}

	/**
	 * @param entry
	 * @param orderEntries
	 * @param totalPrice
	 * @return
	 */
	private double calculateTotalPrice(final AbstractOrderEntryModel entry, final List<AbstractOrderEntryModel> orderEntries,
			double totalPrice)
	{
		for (final AbstractOrderEntryModel abstractOrderEntryModel : orderEntries)
		{
			if (abstractOrderEntryModel != entry)
			{
				totalPrice += abstractOrderEntryModel.getTotalPrice();
			}
			else
			{
				final List<DiscountValue> discountApplied = abstractOrderEntryModel.getDiscountValues();
				double discount = 0;
				for (final DiscountValue discountValue : discountApplied)
				{
					discount += discountValue.getValue();
				}
				totalPrice += (entry.getBasePrice().doubleValue() - discount) * entry.getQuantity().longValue();
			}
		}
		final List<DiscountModel> globalDiscounts = entry.getOrder().getDiscounts();
		for (final DiscountModel discountModel : globalDiscounts)
		{
			totalPrice -= discountModel.getValue().doubleValue();
		}
		return totalPrice;
	}

	/**
	 * @param entry
	 * @param basePrice
	 * @throws CalculationException
	 */
	private void setBasePrice(final AbstractOrderEntryModel entry, final PriceValue basePrice) throws CalculationException
	{
		if (basePrice != null && basePrice.getValue() > 0) //setting base price to entry if product price is greater than 0 and not null
		{
			entry.setBasePrice(Double.valueOf(basePrice.getValue()));
		}
		//if customer price is not null and greater than 0, then will set that price to base price to the entry
		else if (entry.getCustomPrice() != null && entry.getCustomPrice().doubleValue() > 0)
		{
			entry.setBasePrice(entry.getCustomPrice());
		}
		else
		{
			//will come to this condition when there is product price with 0 and customer price is also 0
			//deleting the entry since model is created before checking the prices.
			throw new CalculationException("Please provide customer price for entry");//catch will continue next execution part.
		}
	}

	/**
	 * @param entry
	 * @param priceValue
	 * @param e
	 * @throws CalculationException
	 */
	private void catchBlock(final AbstractOrderEntryModel entry, final PriceValue priceValue, final CalculationException e)
			throws CalculationException
	{
		if (e.getMessage().equalsIgnoreCase(ERRCODE_MULTIPRICE) || e.getMessage().equalsIgnoreCase(ERRCODE_CUSTOMERPRICE))
		{
			final AbstractOrderModel order = entry.getOrder();
			getModelService().remove(entry);
			getModelService().refresh(order);
			throw new CalculationException(e.getMessage());
		}
		if (e.getMessage().equalsIgnoreCase(ERRCODE_ORDERCONFIRMATION_ORDER_NOT_FOUND))
		{
			final AbstractOrderModel order = entry.getOrder();
			getModelService().remove(entry);
			getModelService().refresh(order);
			throw new CalculationException(ERRCODE_ORDERCONFIRMATION_ORDER_NOT_FOUND);
		}
		//will come to catch block when there is no price row created for the product.
		if (firstCondition(entry, priceValue) || secondCondition(entry, priceValue))
		{
			//will come to this condition when there customer price is less than 0
			//deleting the entry since model is created before checking the prices.
			final AbstractOrderModel order = entry.getOrder();
			getModelService().remove(entry);
			getModelService().refresh(order);
			throw new CalculationException(ERRCODE_CUSTOMERPRICE);
		}
		else if (entry.getCustomPrice() != null && entry.getCustomPrice().doubleValue() > 0)
		{
			entry.setBasePrice(entry.getCustomPrice());
			final List<DiscountValue> entryDiscounts = findDiscountValues(entry);
			entry.setDiscountValues(entryDiscounts);
		}
	}

	/**
	 * This method is used to check conditions
	 *
	 * @param entry
	 * @param priceValue
	 * @return
	 */
	private boolean secondCondition(final AbstractOrderEntryModel entry, final PriceValue priceValue)
	{
		return priceValue != null && entry.getCustomPrice() != null && entry.getCustomPrice().doubleValue() <= 0;
	}

	/**
	 * This method is used to check conditions
	 *
	 * @param entry
	 * @param priceValue
	 * @return
	 */
	private boolean firstCondition(final AbstractOrderEntryModel entry, final PriceValue priceValue)
	{
		return priceValue == null && entry.getCustomPrice() != null && entry.getCustomPrice().doubleValue() <= 0;
	}

	/**
	 * This method is used to reset additional cost for the given abstractOrderModel
	 *
	 * @param order
	 * @param relativeTaxValues
	 */
	@Override
	protected void resetAdditionalCosts(final AbstractOrderModel order, final Collection<TaxValue> relativeTaxValues)
	{
		final Collection<ZoneDeliveryModeValueModel> deliveryModeModel = getCartService().getdeliveryOptionsForCart(order);
		if (CollectionUtils.isNotEmpty(deliveryModeModel))
		{
			boolean hasDeliveryMode = false;
			for (final ZoneDeliveryModeValueModel zoneDeliveryModeValueModel : deliveryModeModel)
			{
				if (zoneDeliveryModeValueModel.getDeliveryMode().equals(order.getDeliveryMode()))
				{
					hasDeliveryMode = true;
					double deliveryCostValue = 0;
					deliveryCostValue = zoneDeliveryCostValue(order, relativeTaxValues, zoneDeliveryModeValueModel, deliveryCostValue);
					order.setDeliveryCost(Double.valueOf(deliveryCostValue));
					break;
				}
			}
			if (!hasDeliveryMode)
			{
				order.setDeliveryCost(0.0);
				order.setDeliveryMode(null);
			}
		}
		else
		{
			order.setDeliveryCost(0.0);
		}
		// set payment cost - convert if net or currency is different
		final PriceValue payCost = findPaymentCostStrategy.getPaymentCost(order);
		double paymentCostValue = 0.0;
		if (payCost != null)
		{
			paymentCostValue = convertPriceIfNecessary(payCost, order.getNet().booleanValue(), order.getCurrency(),
					relativeTaxValues).getValue();
		}
		order.setPaymentCost(Double.valueOf(paymentCostValue));
	}

	/**
	 * This is used to get zoneDeliveryCostValue
	 *
	 * @param order
	 * @param relativeTaxValues
	 * @param zoneDeliveryModeValueModel
	 * @param deliveryCostValue
	 * @return
	 */
	private double zoneDeliveryCostValue(final AbstractOrderModel order, final Collection<TaxValue> relativeTaxValues,
			final ZoneDeliveryModeValueModel zoneDeliveryModeValueModel, double deliveryCostValue)
	{
		if (zoneDeliveryModeValueModel.getValue() != null)
		{
			final PriceValue deliCost = new PriceValue(order.getCurrency().getIsocode(), zoneDeliveryModeValueModel.getValue(),
					order.getNet().booleanValue());
			deliveryCostValue = convertPriceIfNecessary(deliCost, order.getNet().booleanValue(), order.getCurrency(),
					relativeTaxValues).getValue();
		}
		return deliveryCostValue;
	}

	@Override
	protected Map<TaxValue, Map<Set<TaxValue>, Double>> calculateSubtotal(final AbstractOrderModel order,
			final boolean recalculate)
	{
		if (recalculate || orderRequiresCalculationStrategy.requiresCalculation(order))
		{
			double subtotal = 0.0;
			final List<AbstractOrderEntryModel> entries = order.getEntries();
			final Map<TaxValue, Map<Set<TaxValue>, Double>> taxValueMap = new LinkedHashMap<>(entries.size() * 2);

			for (final AbstractOrderEntryModel entry : entries)
			{
				calculateTotals(entry, recalculate);
				subtotal += entry.getBasePrice().doubleValue() * entry.getQuantity().longValue();
			}
			subtotal = commonI18NService.roundCurrency(subtotal, order.getCurrency().getDigits().intValue());
			order.setSubtotal(Double.valueOf(subtotal));
			return taxValueMap;
		}
		return Collections.emptyMap();
	}

}
