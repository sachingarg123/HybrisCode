/**
 *
 */
package de.hybris.wooliesegiftcard.facades.order.converters.populator;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import de.hybris.platform.commercefacades.order.converters.populator.OrderPopulator;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.order.data.DeliveryModeData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.order.data.ZoneDeliveryModeData;
import de.hybris.platform.commercefacades.product.data.PriceData;
import de.hybris.platform.commercefacades.product.data.PriceDataType;
import de.hybris.platform.commercefacades.product.data.PromotionData;
import de.hybris.platform.commercefacades.product.data.PromotionResultData;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.deliveryzone.model.ZoneDeliveryModeModel;
import de.hybris.platform.deliveryzone.model.ZoneDeliveryModeValueModel;
import de.hybris.platform.promotionengineservices.model.RuleBasedOrderAdjustTotalActionModel;
import de.hybris.platform.promotionengineservices.model.RuleBasedOrderChangeDeliveryModeActionModel;
import de.hybris.platform.promotionengineservices.model.RuleBasedOrderEntryAdjustActionModel;
import de.hybris.platform.promotions.model.AbstractPromotionActionModel;
import de.hybris.platform.promotions.model.PromotionResultModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.util.DiscountValue;
import de.hybris.platform.wooliesgcfacades.order.data.CardDeliveryDateData;
import de.hybris.wooliesegiftcard.core.enums.UserDataType;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BCustomerModel;
import de.hybris.wooliesegiftcard.core.model.MemberCustomerModel;
import de.hybris.wooliesegiftcard.core.model.WWHtmlLookUpModel;
import de.hybris.wooliesegiftcard.dao.impl.WooliesDefaultCartDao;
import de.hybris.wooliesegiftcard.facades.customersdata;
import de.hybris.wooliesegiftcard.facades.paymentData;
import de.hybris.wooliesegiftcard.facades.constants.WooliesgcFacadesConstants;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;


/**
 * @author 653930 This class is used to populate order
 */
public class WooliesOrderPopulator extends OrderPopulator
{
	private static final Logger LOG = Logger.getLogger(WooliesOrderPopulator.class);
	private ConfigurationService configurationService;
	private WooliesDefaultCartDao wooliesDefaultCartDao;

	private Converter<PaymentInfoModel, CCPaymentInfoData> paymentInfoConverter;

	private static final String DELIVERYCOST = "tax.deliveryCost";
	private static final String PLASTIC = "Plastic";


	protected Converter<PaymentInfoModel, CCPaymentInfoData> getPaymentInfoConverter()
	{
		return paymentInfoConverter;
	}


	public void setPaymentInfoConverter(final Converter<PaymentInfoModel, CCPaymentInfoData> paymentInfoConverter)
	{
		this.paymentInfoConverter = paymentInfoConverter;
	}


	/**
	 * @return the wooliesDefaultCartDao
	 */
	public WooliesDefaultCartDao getWooliesDefaultCartDao()
	{
		return wooliesDefaultCartDao;
	}

	/**
	 * @param wooliesDefaultCartDao
	 *           the wooliesDefaultCartDao to set
	 */
	public void setWooliesDefaultCartDao(final WooliesDefaultCartDao wooliesDefaultCartDao)
	{
		this.wooliesDefaultCartDao = wooliesDefaultCartDao;
	}

	/**
	 * This method is used to populate order
	 *
	 * @param source
	 *           OrderModel
	 * @param target
	 *           OrderData
	 */

	@Override
	public void populate(final OrderModel source, final OrderData target)
	{
		target.setSubTotal(createPrice(source, source.getSubtotal()));
		target.setIsMinimalPackage(source.getIsMinimalPackage());
		final PaymentInfoModel credits = source.getPaymentInfo();
		final String eGiftCardLabel = getConfigurationService().getConfiguration().getString(
				"wooliesgcwebservices.Order.orderConfirmation.eGiftCard.cardLabel", "eGiftCard");

		final String plasticCardLabel = getConfigurationService().getConfiguration().getString(
				"wooliesgcwebservices.Order.orderConfirmation.plastic.cardLabel", PLASTIC);


		final List<CardDeliveryDateData> cardDeliveryDt = new ArrayList();
		for (final AbstractOrderEntryModel entries : source.getEntries())
		{
			setDeliveryModeForEntry(eGiftCardLabel, plasticCardLabel, cardDeliveryDt, entries);
		}
		final Set<String> deptSet = new HashSet();
		cardDeliveryDt.removeIf(p -> !deptSet.add(p.getCardType()));
		target.setCardDeliveryDate(cardDeliveryDt);
		LOG.info(cardDeliveryDt);
		final boolean plastiCardExist = cardDeliveryDt.parallelStream().anyMatch(
				item -> WooliesgcFacadesConstants.PLASTICCARD.equals(item.getCardType()));
		if (plastiCardExist)
		{
			target.setDeliveryCost(createPrice(source, source.getDeliveryCost()));
		}
		else
		{
			target.setDeliveryCost(null);
		}
		if (source.getDeliveryCost() != null)
		{
			setDeliveryCosts(source, target);
		}
		final Date date = source.getDate();
		final long dob = date.getTime();
		target.setDate(dob);
		final UserModel credit = source.getUser();
		final customersdata customer = new customersdata();

		setNameForCustomer(credit, customer);
		target.setCustomer(customer);

		final paymentData customersa = new paymentData();
		if (null != credits)
		{
			customersa.setSchema(credits.getSchema());
			if (null != credits.getPaymentOption())
			{
				customersa.setPaymentOption(credits.getPaymentOption().toString());
				final WWHtmlLookUpModel paymentName = getWooliesDefaultCartDao()
						.getPaymentName(credits.getPaymentOption().toString());
				if (paymentName != null)
				{
					customersa.setPaymentName(paymentName.getDetails());
				}
			}
			customersa.setSuffix(credits.getCcNumber());
		}
		target.setPayment(customersa);
		final PaymentInfoModel paymentInfo = source.getPaymentInfo();
		final CCPaymentInfoData paymentInfoData = getPaymentInfoConverter().convert(paymentInfo);
		target.setPaymentInfo(paymentInfoData);

		final Set<PromotionResultModel> promotionResults = source.getAllPromotionResults();
		final List<DiscountValue> globalDiscounts = source.getGlobalDiscountValues();
		final List<PromotionResultData> allPromotions = new ArrayList();
		allPromotions.addAll(target.getAppliedProductPromotions());
		allPromotions.addAll(target.getAppliedOrderPromotions());
		boolean isTotalDiscountSet = false;
		for (final PromotionResultModel promotionResultModel : promotionResults)
		{
			isTotalDiscountSet = setPromotions(source, target, globalDiscounts, allPromotions, isTotalDiscountSet,
					promotionResultModel);
		}

		target.setDeliveryModes(getDeliveryMode(source));
		target.setOrderNo(source.getCode());
		if (null != source.getDate())
		{
			target.setOrderDate(String.valueOf(source.getDate().getTime()));
		}
		final PriceData priceData = new PriceData();
		priceData.setCurrencyIso("AUD");
		priceData.setFormattedValue(source.getTotalPrice().toString());
		target.setAmount(priceData);
		if (null != source.getStatus())
		{
			target.setOrderStatus(source.getStatus().toString());
		}
		target.setTaxInvoiceId(source.getInvoiceNumber());
		target.setIsBulkOrder(source.getIsBulkOrder());
		final long totalItems = getTotalItems(source);
		final Long totalQuantity = Long.valueOf(totalItems);
		target.setQuantity(totalQuantity.toString());
	}


	/**
	 * set promotion to cart
	 *
	 * @param source
	 *           OrderModel
	 * @param target
	 *           OrderData
	 * @param globalDiscounts
	 *           DiscountValue
	 * @param allPromotions
	 *           promotion model
	 * @param isTotalDiscountSet
	 *           boolean
	 * @param promotionResultModel
	 *           PromotionResultModel
	 * @return boolean
	 */
	private boolean setPromotions(final OrderModel source, final OrderData target, final List<DiscountValue> globalDiscounts,
			final List<PromotionResultData> allPromotions, boolean isTotalDiscountSet,
			final PromotionResultModel promotionResultModel)
	{
		final Collection<AbstractPromotionActionModel> actions = promotionResultModel.getActions();
		for (final AbstractPromotionActionModel abstractPromotionActionModel : actions)
		{
			if (abstractPromotionActionModel instanceof RuleBasedOrderChangeDeliveryModeActionModel)
			{
				final RuleBasedOrderChangeDeliveryModeActionModel deliveryChangeModel = (RuleBasedOrderChangeDeliveryModeActionModel) abstractPromotionActionModel;

				isTotalDiscountSet = setTotalDiscount(source, target, allPromotions, isTotalDiscountSet, promotionResultModel,
						deliveryChangeModel);
				if (isTotalDiscountSet)
				{
					break;
				}
			}
			else if (abstractPromotionActionModel instanceof RuleBasedOrderAdjustTotalActionModel)
			{
				final RuleBasedOrderAdjustTotalActionModel actionModel = (RuleBasedOrderAdjustTotalActionModel) abstractPromotionActionModel;
				for (final DiscountValue discountModel : globalDiscounts)
				{
					isTotalDiscountSet = setCouponCodeInPromotionResultData(source, allPromotions, isTotalDiscountSet,
							promotionResultModel, actionModel, discountModel);
					if (isTotalDiscountSet)
					{
						break;
					}
				}
				if (isTotalDiscountSet)
				{
					break;
				}
			}
			else if (abstractPromotionActionModel instanceof RuleBasedOrderEntryAdjustActionModel)
			{

				final RuleBasedOrderEntryAdjustActionModel actionModel = (RuleBasedOrderEntryAdjustActionModel) abstractPromotionActionModel;
				if (CollectionUtils.isNotEmpty(actionModel.getUsedCouponCodes()))
				{
					final Collection<String> couponCodes = actionModel.getUsedCouponCodes();
					setCouponInPromotionResultData(allPromotions, promotionResultModel, couponCodes);
				}

			}
		}
		return isTotalDiscountSet;
	}


	/**
	 * This method is used to setCouponInPromotionResultData
	 *
	 * @param allPromotions
	 * @param promotionResultModel
	 * @param couponCodes
	 */
	private void setCouponInPromotionResultData(final List<PromotionResultData> allPromotions,
			final PromotionResultModel promotionResultModel, final Collection<String> couponCodes)
	{
		for (final String coupon : couponCodes)
		{
			for (final PromotionResultData promotionResultData : allPromotions)
			{
				final PromotionData promotionData = promotionResultData.getPromotionData();
				if (promotionData.getCode().equals(promotionResultModel.getPromotion().getCode()))
				{
					promotionResultData.setCouponCode(coupon);
				}
			}
		}
	}


	/**
	 * This method is used to setCouponCodeInPromotionResultData
	 *
	 * @param source
	 * @param allPromotions
	 * @param isTotalDiscountSet
	 * @param promotionResultModel
	 * @param actionModel
	 * @param discountModel
	 * @return
	 */
	private boolean setCouponCodeInPromotionResultData(final OrderModel source, final List<PromotionResultData> allPromotions,
			boolean isTotalDiscountSet, final PromotionResultModel promotionResultModel,
			final RuleBasedOrderAdjustTotalActionModel actionModel, final DiscountValue discountModel)
	{
		if (discountModel.getCode().equals(actionModel.getGuid()))
		{
			for (final PromotionResultData promotionResultData : allPromotions)
			{
				final PromotionData promotionData = promotionResultData.getPromotionData();
				if (promotionData.getCode().equals(promotionResultModel.getPromotion().getCode()))
				{
					promotionResultData.setTotalDiscount(getPriceDataFactory().create(PriceDataType.BUY,
							BigDecimal.valueOf(discountModel.getAppliedValue()), source.getCurrency().getIsocode()));
					isTotalDiscountSet = true;
					final Collection<String> couponCodes = actionModel.getUsedCouponCodes();
					checkingCouponNullOrNot(promotionResultData, couponCodes);
					break;
				}
			}
		}
		return isTotalDiscountSet;
	}


	/**
	 * This method is used to checkingCouponNullOrNot
	 * 
	 * @param promotionResultData
	 * @param couponCodes
	 */
	private void checkingCouponNullOrNot(final PromotionResultData promotionResultData, final Collection<String> couponCodes)
	{
		if (couponCodes != null)
		{
			for (final String coupon : couponCodes)
			{
				promotionResultData.setCouponCode(coupon);
			}
		}
	}


	/**
	 * set delivery mode to entry
	 *
	 * @param eGiftCardLabel
	 *           String
	 * @param plasticCardLabel
	 *           String
	 * @param cardDeliveryDt
	 *           CardDeliveryDateData
	 * @param entries
	 *           AbstractOrderEntryModel
	 */
	private void setDeliveryModeForEntry(final String eGiftCardLabel, final String plasticCardLabel,
			final List<CardDeliveryDateData> cardDeliveryDt, final AbstractOrderEntryModel entries)
	{
		final CardDeliveryDateData cdd = new CardDeliveryDateData();
		if (null != entries.getDeliveryMode())
		{
			if (entries.getDeliveryMode().getCode().startsWith("EGC"))
			{
				cdd.setCardType("eGift");
				cdd.setCardTypeLabel(eGiftCardLabel);
			}
			else
			{
				cdd.setCardType(PLASTIC);
				cdd.setCardTypeLabel(plasticCardLabel);
			}
			cdd.setDeliveryModeType(entries.getDeliveryMode().getCode());
			cdd.setDeliveryDate(getDeliveryDates(entries));
		}
		cardDeliveryDt.add(cdd);
	}


	/**
	 * set name to customer data
	 *
	 * @param credit
	 *           UserModel
	 * @param customer
	 *           customersdata
	 */
	private void setNameForCustomer(final UserModel credit, final customersdata customer)
	{
		if (((CustomerModel) credit).getCustomerType() == UserDataType.B2C)
		{
			final String[] nameSplit = credit.getName().split(" ");
			if (nameSplit[0] != null)
			{
				customer.setFirstName(nameSplit[0]);

			}
			if (nameSplit[1] != null)
			{
				customer.setLastName(nameSplit[1]);
			}
			customer.setEmailaddress(((CustomerModel) credit).getContactEmail());
			customer.setCustomerType(((CustomerModel) credit).getCustomerType().getCode());
		}
		else if (((CustomerModel) credit).getCustomerType() == UserDataType.B2B)
		{
			final String[] nameSplit = credit.getName().split(" ");
			if (nameSplit[0] != null)
			{
				customer.setFirstName(nameSplit[0]);

			}
			if (nameSplit[1] != null)
			{
				customer.setLastName(nameSplit[1]);
			}
			customer.setCoorporateid(((CorporateB2BCustomerModel) credit).getDefaultB2BUnit().getUid());
			customer.setEmailaddress(((CorporateB2BCustomerModel) credit).getContactEmail());
			customer.setCustomerType(((CorporateB2BCustomerModel) credit).getCustomerType().getCode());
		}
		else if (((CustomerModel) credit).getCustomerType() == UserDataType.MEM)
		{
			customer.setMembername((((MemberCustomerModel) credit).getName()));
			customer.setEmailaddress(((MemberCustomerModel) credit).getEmail());
			customer.setCustomerType(((MemberCustomerModel) credit).getCustomerType().getCode());
		}
	}


	/**
	 * set total discount
	 *
	 * @param source
	 *           OrderModel
	 * @param target
	 *           OrderData
	 * @param allPromotions
	 *           applied promotions
	 * @param isTotalDiscountSet
	 *           total discount on the cart
	 * @param promotionResultModel
	 *           promo result
	 * @param deliveryChangeModel
	 *           applied delivery promotion
	 * @return
	 */
	private boolean setTotalDiscount(final OrderModel source, final OrderData target,
			final List<PromotionResultData> allPromotions, boolean isTotalDiscountSet,
			final PromotionResultModel promotionResultModel, final RuleBasedOrderChangeDeliveryModeActionModel deliveryChangeModel)
	{
		for (final PromotionResultData promotionResultData : allPromotions)
		{
			final PromotionData promotionData = promotionResultData.getPromotionData();
			if (promotionData.getCode().equals(promotionResultModel.getPromotion().getCode()))
			{

				promotionResultData.setTotalDiscount(getPriceDataFactory().create(PriceDataType.BUY,
						deliveryChangeModel.getReplacedDeliveryCost(), source.getCurrency().getIsocode()));
				BigDecimal totalDiscounts = target.getTotalDiscounts().getValue();
				totalDiscounts = totalDiscounts.add(deliveryChangeModel.getReplacedDeliveryCost());
				target.setTotalDiscounts(getPriceDataFactory().create(PriceDataType.BUY, totalDiscounts,
						source.getCurrency().getIsocode()));
				isTotalDiscountSet = true;
				break;
			}
		}
		return isTotalDiscountSet;
	}

	/**
	 * This method is used to get delivery dates for the given order
	 *
	 * @param orderEntry
	 *           AbstractOrderEntryModel
	 * @return orderModel
	 */
	public final String getDeliveryDates(final AbstractOrderEntryModel orderEntry)
	{

		validateParameterNotNullStandardMessage("orderEntry", orderEntry);

		String deliveryDate = "";
		if (orderEntry instanceof OrderEntryModel && orderEntry.getDeliveryMode() != null)
		{
			final Date date = new Date();
			final GregorianCalendar cal = new GregorianCalendar();
			final Integer daysInt = orderEntry.getDeliveryMode().getDeliveryDays();
			if (daysInt != null)
			{
				final int days = daysInt.intValue();
				cal.setTime(date);
				cal.add(Calendar.DATE, days);
				final DateFormat dateFormat = new SimpleDateFormat(WooliesgcFacadesConstants.DATEFORMAT);
				deliveryDate = dateFormat.format(cal.getTime());
			}
			else
			{
				throw new ConversionException("NO Delivery Days founds for " + orderEntry.getDeliveryMode());
			}

		}
		return deliveryDate;

	}



	/**
	 * @return the configurationService
	 */
	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}


	/**
	 * @param configurationService
	 *           the configurationService to set
	 */
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	/**
	 * This method is used to get deliver mode
	 *
	 * @param orderModel
	 *           orderModel
	 * @return appliedDeliveryModes
	 */
	private List<DeliveryModeData> getDeliveryMode(final OrderModel orderModel)
	{
		final List<AbstractOrderEntryModel> entries = orderModel.getEntries();
		final List<DeliveryModeData> appliedDeliveryModes = new ArrayList();
		for (final AbstractOrderEntryModel abstractOrderEntryModel : entries)
		{
			if (abstractOrderEntryModel.getProduct().getIsEGiftCard().booleanValue())
			{
				final Collection<ZoneDeliveryModeValueModel> deliveryModes = wooliesDefaultCartDao
						.findDeliveryModeForeGiftProduct(orderModel);
				for (final ZoneDeliveryModeValueModel zoneDeliveryModeValueModel : deliveryModes)
				{
					final ZoneDeliveryModeData zoneDeliveryModeData = getZoneDeliveryModeConverter().convert(
							zoneDeliveryModeValueModel.getDeliveryMode());
					deliveryCostCalculation(zoneDeliveryModeValueModel, zoneDeliveryModeData);
					final Date dates = orderModel.getDate();
					final long dobo = dates.getTime()
							+ (getConfigurationService().getConfiguration().getLong("delivery.startDates", 0));
					zoneDeliveryModeData.setEstStart(dobo);
					final long dobu = dates.getTime()
							+ (getConfigurationService().getConfiguration().getLong("delivery.endDates", 86400000));
					zoneDeliveryModeData.setEstEnd(dobu);

					setDeliveryType(zoneDeliveryModeData);
					appliedDeliveryModes.add(zoneDeliveryModeData);

				}
				break;
			}
		}
		if (orderModel.getDeliveryMode() != null)
		{
			final DeliveryModeModel deliveryMode = orderModel.getDeliveryMode();
			ZoneDeliveryModeData zoneDeliveryModeData = new ZoneDeliveryModeData();
			if (deliveryMode instanceof ZoneDeliveryModeModel)
			{
				zoneDeliveryModeData = getZoneDeliveryModeConverter().convert((ZoneDeliveryModeModel) deliveryMode);
			}
			final CurrencyModel currency = orderModel.getCurrency();
			if (orderModel.getDeliveryCost() != null && orderModel.getDeliveryCost().doubleValue() > 0)
			{
				final double taxCost = getCommonI18NService().roundCurrency(
						orderModel.getDeliveryCost().doubleValue()
								/ ((getConfigurationService().getConfiguration().getDouble(DELIVERYCOST, 10)) + 1),
						currency.getDigits().intValue());
				zoneDeliveryModeData.setTaxCost(getPriceDataFactory().create(PriceDataType.BUY, BigDecimal.valueOf(taxCost),
						currency.getIsocode()));
			}
			zoneDeliveryModeData.setDeliveryCost(getPriceDataFactory().create(PriceDataType.BUY,
					BigDecimal.valueOf(orderModel.getDeliveryCost().doubleValue()), currency.getIsocode()));
			if (null != orderModel.getDeliveryMode())

			{
				setDeliveryType(zoneDeliveryModeData);
			}
			final Date dates = orderModel.getDate();
			final long dobg = dates.getTime()
					+ (getConfigurationService().getConfiguration().getLong("delivery.startDate", 172800000));
			zoneDeliveryModeData.setEstStart(dobg);
			final long dobl = dates.getTime()
					+ (getConfigurationService().getConfiguration().getLong("delivery.endDate", 345600000));
			zoneDeliveryModeData.setEstEnd(dobl);

			appliedDeliveryModes.add(zoneDeliveryModeData);
		}


		return appliedDeliveryModes;
	}


	/**
	 * @param zoneDeliveryModeData
	 */
	private void setDeliveryType(final ZoneDeliveryModeData zoneDeliveryModeData)
	{
		if (zoneDeliveryModeData.getCode().startsWith("EGC"))
		{
			zoneDeliveryModeData.setDeliveryType("eGift");
		}
		else
		{
			zoneDeliveryModeData.setDeliveryType(PLASTIC);
		}
	}

	/**
	 * This method is used to calculate delivery cost
	 *
	 * @param deliveryModeModel
	 * @param zoneDeliveryModeData
	 */
	private void deliveryCostCalculation(final ZoneDeliveryModeValueModel deliveryModeModel,
			final ZoneDeliveryModeData zoneDeliveryModeData)
	{
		final CurrencyModel currency = deliveryModeModel.getCurrency();
		if (deliveryModeModel.getValue() != null && deliveryModeModel.getValue().doubleValue() > 0)
		{
			final double taxCost = getCommonI18NService().roundCurrency(
					deliveryModeModel.getValue().doubleValue()
							/ ((getConfigurationService().getConfiguration().getDouble(DELIVERYCOST, 10)) + 1),
					currency.getDigits().intValue());
			zoneDeliveryModeData.setTaxCost(getPriceDataFactory().create(PriceDataType.BUY, BigDecimal.valueOf(taxCost),
					currency.getIsocode()));
		}
		zoneDeliveryModeData.setDeliveryCost(getPriceDataFactory().create(PriceDataType.BUY,
				BigDecimal.valueOf(deliveryModeModel.getValue().doubleValue()), currency.getIsocode()));

	}

	/**
	 * set delivery cost
	 *
	 * @param source
	 *           OrderModel
	 * @param target
	 *           OrderData
	 */
	private void setDeliveryCosts(final OrderModel source, final OrderData target)
	{
		final CurrencyModel currency = source.getCurrency();
		target.setDeliveryCost(getPriceDataFactory().create(PriceDataType.BUY,
				BigDecimal.valueOf(source.getDeliveryCost().doubleValue()), currency.getIsocode()));
		if (source.getDeliveryCost().doubleValue() > 0)
		{

			final double taxCost = getCommonI18NService().roundCurrency(
					source.getDeliveryCost().doubleValue()
							/ ((getConfigurationService().getConfiguration().getDouble(DELIVERYCOST, 10)) + 1),
					currency.getDigits().intValue());
			target.setTotalTax(getPriceDataFactory().create(PriceDataType.BUY, BigDecimal.valueOf(taxCost), currency.getIsocode()));
		}
	}

	/**
	 * This method is used to get total items for order
	 *
	 * @param source
	 *           OrderModel
	 * @return long quantity
	 */
	private final long getTotalItems(final OrderModel source)
	{
		long quantity = 0;
		final List<AbstractOrderEntryModel> listEntry = source.getEntries();
		if (null != listEntry && !listEntry.isEmpty())
		{
			for (final AbstractOrderEntryModel abstractOrderEntryModel : listEntry)
			{
				quantity = quantity + abstractOrderEntryModel.getQuantity().longValue();
			}
		}
		return quantity;
	}
}
