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
package com.woolies.webservices.rest.v2.controller;

import de.hybris.platform.commercewebservicescommons.dto.order.GiftCardRequestWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.queues.OrderStatusUpdateElementListWsDTO;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.wooliegcwebservices.dto.order.GiftCardResponseWsDTO;
import de.hybris.wooliesegiftcard.core.EncryptionUtils;
import de.hybris.wooliesegiftcard.facades.GiftCardResponseData;
import de.hybris.wooliesegiftcard.facades.OrderPlacedFacade;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.woolies.webservices.constants.WooliesgcWebServicesConstants;
import com.woolies.webservices.rest.exceptions.WooliesWebserviceException;
import com.woolies.webservices.rest.formatters.WsDateFormatter;
import com.woolies.webservices.rest.queues.data.OrderStatusUpdateElementData;
import com.woolies.webservices.rest.queues.data.OrderStatusUpdateElementDataList;
import com.woolies.webservices.rest.queues.impl.OrderStatusUpdateQueue;
import com.woolies.webservices.rest.swagger.ApiBaseSiteIdParam;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;


/**
 *
 * This class is feeds controller
 *
 */
@Controller
@RequestMapping(value = "/{baseSiteId}/feeds")
@Api(tags = "Feeds")
public class FeedsController extends BaseController
{
	@Resource(name = "wsDateFormatter")
	private WsDateFormatter wsDateFormatter;
	@Resource(name = "orderStatusUpdateQueue")
	private OrderStatusUpdateQueue orderStatusUpdateQueue;
	@Resource(name = "orderPlacedFacade")
	private OrderPlacedFacade orderPlacedFacade;
	@Resource(name = "configurationService")
	private ConfigurationService configurationService;
	private static final String characterEncoding = "UTF-8";


	/**
	 * This method is used to get list of orders with status updates
	 * 
	 * @param timestamp
	 * @param baseSiteId
	 * @param fields
	 * @return OrderStatusUpdateElementListWsDTO
	 */
	@Secured("ROLE_TRUSTED_CLIENT")
	@RequestMapping(value = "/orders/statusfeed", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get a list of orders with status updates", notes = "Returns the orders the status has changed for. Returns only the elements from the current baseSite, updated after the provided timestamp.", authorizations =
	{ @Authorization(value = "oauth2_client_credentials") })
	public OrderStatusUpdateElementListWsDTO orderStatusFeed(
			@ApiParam(value = "Only items newer than the given parameter are retrieved. This parameter should be in ISO-8601 format.", required = true) @RequestParam final String timestamp,
			@ApiParam(value = "Base site identifier", required = true) @PathVariable final String baseSiteId,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		final Date timestampDate = wsDateFormatter.toDate(timestamp);
		final List<OrderStatusUpdateElementData> orderStatusUpdateElements = orderStatusUpdateQueue.getItems(timestampDate);
		filterOrderStatusQueue(orderStatusUpdateElements, baseSiteId);
		final OrderStatusUpdateElementDataList dataList = new OrderStatusUpdateElementDataList();
		dataList.setOrderStatusUpdateElements(orderStatusUpdateElements);
		return getDataMapper().map(dataList, OrderStatusUpdateElementListWsDTO.class, fields);
	}

	/**
	 * This method is used to filter OrderStatus queue
	 * 
	 * @param orders
	 * @param baseSiteId
	 */
	protected void filterOrderStatusQueue(final List<OrderStatusUpdateElementData> orders, final String baseSiteId)
	{
		final Iterator<OrderStatusUpdateElementData> dataIterator = orders.iterator();
		while (dataIterator.hasNext())
		{
			final OrderStatusUpdateElementData orderStatusUpdateData = dataIterator.next();
			if (!baseSiteId.equals(orderStatusUpdateData.getBaseSiteId()))
			{
				dataIterator.remove();
			}
		}
	}

	@RequestMapping(value = "/giftcard", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "Get giftCard details", notes = "Get Giftcard details")
	@ApiBaseSiteIdParam
	public GiftCardResponseWsDTO getGiftCardDetails(@RequestBody final GiftCardRequestWsDTO giftCardRequest,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC") @RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws UnsupportedEncodingException
	{
		final String encryptedToken = giftCardRequest.getGiftCardToken();

		if (StringUtils.isNotEmpty(encryptedToken))
		{
			final String encypKey = configurationService.getConfiguration().getString("encryption.key");
			final byte[] encryptionKey = encypKey.getBytes(characterEncoding);
			final String token = EncryptionUtils.decrypt(encryptedToken, encryptionKey);
			final GiftCardResponseData giftCardData = orderPlacedFacade.getEgiftCardDetails(token);
			if (giftCardData != null)
			{
				return getDataMapper().map(giftCardData, GiftCardResponseWsDTO.class, fields);
			}
			else
			{
				throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_NOTEGIFTCARD,
						WooliesgcWebServicesConstants.ERRMSG_NOTEGIFTCARD, WooliesgcWebServicesConstants.ERRRSN_NOTEGIFTCARD);
			}
		}
		else
		{
			throw new WooliesWebserviceException(WooliesgcWebServicesConstants.ERRCODE_EGIFTCARDEMPTYTOKEN,
					WooliesgcWebServicesConstants.ERRMSG_EGIFTCARDEMPTYTOKEN,
					WooliesgcWebServicesConstants.ERRRSN_EGIFTCARDEMPTYTOKEN);
		}
	}
}
