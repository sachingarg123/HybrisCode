/*
 * [y] hybris Platform
 *
 * Copyright (c) 2017 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.wooliesegiftcard.facades.process.email.context;

import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel;
import de.hybris.platform.acceleratorservices.process.email.context.AbstractEmailContext;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.cmsfacades.data.MediaData;
import de.hybris.platform.commercefacades.coupon.data.CouponData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.product.PriceDataFactory;
import de.hybris.platform.commercefacades.product.data.PriceData;
import de.hybris.platform.commercefacades.product.data.PriceDataType;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.media.impl.DefaultMediaDao;
import de.hybris.wooliesegiftcard.core.EncryptionUtils;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BCustomerModel;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BUnitModel;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;


/**
 * Velocity context for a order notification email.
 */
public class OrderNotificationEmailContext extends AbstractEmailContext<OrderProcessModel>
{
	private Converter<OrderModel, OrderData> orderConverter;
	private OrderData orderData;
	@Autowired
	private PriceDataFactory priceDataFactory;
	private List<CouponData> giftCoupons;
	public static final String ORDER = "order";
	public static final String CREATEDDATE = "date";
	public static final String BLLINGADDRESS = "billingaddress";
	public static final String REDIRECTLINK = "redirectlink";
	public static final String DELIVERYCOST = "deliverycost";
	public static final String CONTACTUS = "contactus";
	public static final String FAQ = "faq";
	public static final String PRIVACYPOLICY = "privacypolicy";
	public static final String ABN = "abn";
	public static final String FOOTERIMAGE = "emailFooter";
	public static final String LOGO = "woolworthsEmailLogo";
	@Autowired
	private DefaultMediaDao mediaDao;
	@Autowired
	private Converter<MediaModel, MediaData> mediaModelConverter;
	private static final String CHARACTERENCODING = "UTF-8";
	private static final Logger LOG = Logger.getLogger(OrderNotificationEmailContext.class);


	@Override
	public void init(final OrderProcessModel orderProcessModel, final EmailPageModel emailPageModel)
	{
		super.init(orderProcessModel, emailPageModel);
		final OrderModel orderModel = orderProcessModel.getOrder();
		orderData = getOrderConverter().convert(orderModel);
		final PriceData deliveryCost = priceDataFactory.create(PriceDataType.BUY,
				BigDecimal.valueOf(orderModel.getDeliveryCost().doubleValue()), orderModel.getCurrency().getIsocode());
		giftCoupons = orderData.getAppliedOrderPromotions().stream()
				.filter(x -> CollectionUtils.isNotEmpty(x.getGiveAwayCouponCodes())).flatMap(p -> p.getGiveAwayCouponCodes().stream())
				.collect(Collectors.toList());
		put(ORDER, orderData);
		final Date placedOrderDate = orderData.getCreated();
		final SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd");
		put(CREATEDDATE, dt1.format(placedOrderDate));
		put(BLLINGADDRESS, orderData.getPaymentInfo().getBillingAddress());
		final String linkRedirect = getConfigurationService().getConfiguration().getString("orderConfirmation.redirect.link",
				"https://uat-giftcards.woolworths.com.au/index.html#/gift-cards/order-details/");
		final String encypKey = getConfigurationService().getConfiguration().getString("encryption.key");
		try
		{
			final byte[] encypKeyByte = encypKey.getBytes(CHARACTERENCODING);
			final String stringToEncrypt = orderModel.getOrderToken();
			put(REDIRECTLINK, linkRedirect + orderData.getCode() + "/" + EncryptionUtils.encrypt(stringToEncrypt, encypKeyByte));
		}
		catch (final UnsupportedEncodingException e)
		{
			LOG.error(e.getMessage());
		}
		put(DELIVERYCOST, deliveryCost.getFormattedValue());
		put(CONTACTUS, getConfigurationService().getConfiguration().getString("orderConfirmation.contactUs.link",
				"https://dev-giftcards.woolworths.com.au/about-woolworths-gift-cards/contact-us.html"));
		put(FAQ, getConfigurationService().getConfiguration().getString("orderConfirmation.FAQ.link",
				"https://dev-giftcards.woolworths.com.au/about-woolworths-gift-cards/faqs.html"));
		put(PRIVACYPOLICY, getConfigurationService().getConfiguration().getString("orderConfirmation.privacyPolicy.link",
				"https://dev-giftcards.woolworths.com.au/about-woolworths-gift-cards/privacy-policy.html"));
		final List<MediaModel> logoMediaModel = mediaDao
				.findMediaByCode(orderModel.getEntries().get(0).getProduct().getCatalogVersion(), LOGO);
		final String absolutePath = getConfigurationService().getConfiguration().getString("website.ycomdev-giftcards.https");
		if (CollectionUtils.isNotEmpty(logoMediaModel))
		{
			final MediaData defaultImage1 = mediaModelConverter.convert(logoMediaModel.get(0));
			defaultImage1.setUrl(absolutePath + defaultImage1.getUrl());
			put(LOGO, defaultImage1.getUrl());
		}
		final List<MediaModel> footerMediaModel = mediaDao
				.findMediaByCode(orderModel.getEntries().get(0).getProduct().getCatalogVersion(), FOOTERIMAGE);
		if (CollectionUtils.isNotEmpty(footerMediaModel))
		{
			final MediaData defaultImage1 = mediaModelConverter.convert(footerMediaModel.get(0));
			defaultImage1.setUrl(absolutePath + defaultImage1.getUrl());
			put(FOOTERIMAGE, defaultImage1.getUrl());
		}
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

	public List<CouponData> getCoupons()
	{
		return giftCoupons;
	}
}
