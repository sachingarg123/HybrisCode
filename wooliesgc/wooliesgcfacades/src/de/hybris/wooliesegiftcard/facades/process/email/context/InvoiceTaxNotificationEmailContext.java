/**
 *
 */
package de.hybris.wooliesegiftcard.facades.process.email.context;

import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel;
import de.hybris.platform.acceleratorservices.process.email.context.AbstractEmailContext;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.product.PriceDataFactory;
import de.hybris.platform.commercefacades.product.data.PriceData;
import de.hybris.platform.commercefacades.product.data.PriceDataType;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BCustomerModel;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BUnitModel;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;


/**
 * @author 668982
 *
 */
public class InvoiceTaxNotificationEmailContext extends AbstractEmailContext<OrderProcessModel>
{
	private Converter<OrderModel, OrderData> orderConverter;
	private OrderData orderData;

	public static final String ORDER = "order";
	public static final String ABN = "abn";
	public static final String Date = "date";
	public static final String BLLINGADDRESS = "billingaddress";
	public static final String SHIPPINGADDRESS = "shippingaddress";
	public static final String REDIRECTLINK = "redirectlink";
	public static final String DELIVERYCOST = "deliverycost";
	@Autowired
	private PriceDataFactory priceDataFactory;

	@Override
	public void init(final OrderProcessModel orderProcessModel, final EmailPageModel emailPageModel)
	{
		super.init(orderProcessModel, emailPageModel);
		orderData = getOrderConverter().convert(orderProcessModel.getOrder());
		put(ORDER, orderData);
		final CustomerModel customer = getCustomer(orderProcessModel);
		if (customer instanceof CorporateB2BCustomerModel)
		{
			final CorporateB2BCustomerModel corporateCustomer = (CorporateB2BCustomerModel) customer;
			final B2BUnitModel b2bUnit = corporateCustomer.getDefaultB2BUnit();
			if (b2bUnit instanceof CorporateB2BUnitModel)
			{
				final CorporateB2BUnitModel corporateB2BUnitModel = (CorporateB2BUnitModel) b2bUnit;
				if (corporateB2BUnitModel.getCorporateABN() != null)
				{
					put(ABN, corporateB2BUnitModel.getCorporateABN());
				}
			}
		}
		final PriceData deliveryCost = priceDataFactory.create(PriceDataType.BUY,
				BigDecimal.valueOf(orderProcessModel.getOrder().getDeliveryCost().doubleValue()),
				orderProcessModel.getOrder().getCurrency().getIsocode());
		final Date placedOrderDate = orderData.getCreated();
		final SimpleDateFormat dt1 = new SimpleDateFormat("dd-MM-yyyy HH:MM:SS");
		put(Date, dt1.format(placedOrderDate));
		put(BLLINGADDRESS, orderData.getPaymentInfo().getBillingAddress());
		final String linkRedirect = getConfigurationService().getConfiguration().getString("orderConfirmation.redirect.link",
				"https://uat-giftcards.woolworths.com.au/index.html#/gift-cards/order-details/");
		put(REDIRECTLINK, linkRedirect + orderData.getCode());
		put(DELIVERYCOST, deliveryCost.getFormattedValue());

		final List<AbstractOrderEntryModel> entries = orderProcessModel.getOrder().getEntries();
		for (final AbstractOrderEntryModel abstractOrderEntryModel : entries)
		{
			if (abstractOrderEntryModel.getProduct().getIsEGiftCard().booleanValue())
			{
				put(SHIPPINGADDRESS, orderData.getDeliveryAddress());
				break;
			}
		}
	}

	@Override
	protected BaseSiteModel getSite(final OrderProcessModel orderProcessModel)
	{
		return orderProcessModel.getOrder().getSite();
	}

	@Override
	protected CustomerModel getCustomer(final OrderProcessModel orderProcessModel)
	{
		return (CustomerModel) orderProcessModel.getOrder().getUser();
	}

	protected Converter<OrderModel, OrderData> getOrderConverter()
	{
		return orderConverter;
	}

	@Required
	public void setOrderConverter(final Converter<OrderModel, OrderData> orderConverter)
	{
		this.orderConverter = orderConverter;
	}

	public OrderData getOrder()
	{
		return orderData;
	}

	@Override
	protected LanguageModel getEmailLanguage(final OrderProcessModel orderProcessModel)
	{
		return orderProcessModel.getOrder().getLanguage();
	}


}
