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

import de.hybris.platform.commercefacades.voucher.VoucherFacade;
import de.hybris.platform.commercefacades.voucher.exceptions.VoucherOperationException;
import de.hybris.platform.commercewebservicescommons.dto.voucher.VoucherWsDTO;

import javax.annotation.Resource;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.woolies.webservices.rest.swagger.ApiBaseSiteIdParam;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;


/**
 * This class is used to as vochers controller
 *
 */
@Controller
@RequestMapping(value = "/{baseSiteId}/vouchers")
@Api(tags = "Vouchers")
public class VouchersController extends BaseController
{
	@Resource(name = "voucherFacade")
	private VoucherFacade voucherFacade;

	/**
	 * This method is used to get voucher for the given code
	 *
	 * @param code
	 * @param fields
	 * @return VoucherWsDTO
	 * @throws VoucherOperationException
	 */
	@Secured("ROLE_TRUSTED_CLIENT")
	@RequestMapping(value = "/{code}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get a voucher based on code", notes = "Returns details of a single voucher according to a voucher code.", authorizations =
	{ @Authorization(value = "oauth2_client_credentials") })
	@ApiBaseSiteIdParam
	public VoucherWsDTO getVoucherByCode(
			@ApiParam(value = "Voucher identifier (code)", required = true) @PathVariable final String code,
			@ApiParam(value = "Response configuration (list of fields, which should be returned in response)", allowableValues = "BASIC, DEFAULT, FULL") @RequestParam(defaultValue = "BASIC") final String fields)
			throws VoucherOperationException
	{
		return getDataMapper().map(voucherFacade.getVoucher(code), VoucherWsDTO.class, fields);
	}
}
