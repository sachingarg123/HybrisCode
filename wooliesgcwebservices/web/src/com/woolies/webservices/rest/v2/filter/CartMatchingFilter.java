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
package com.woolies.webservices.rest.v2.filter;

import de.hybris.platform.commercewebservicescommons.strategies.CartLoaderStrategy;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.store.services.BaseStoreService;
import de.hybris.wooliesegiftcard.dao.impl.WooliesDefaultCartDao;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Filter that puts cart from the requested url into the session.
 */
public class CartMatchingFilter extends AbstractUrlMatchingFilter
{
	public static final String REFRESH_CART_PARAM = "refreshCart";
	private String regexp;
	private CartLoaderStrategy cartLoaderStrategy;
	private boolean cartRefreshedByDefault = true;
	private UserService userService;
	private CartService cartService;
	private BaseStoreService baseStoreService;
	private WooliesDefaultCartDao wooliesDefaultCartDao;



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
	 * @return the cartService
	 */
	public CartService getCartService()
	{
		return cartService;
	}

	/**
	 * @param cartService
	 *           the cartService to set
	 */
	public void setCartService(final CartService cartService)
	{
		this.cartService = cartService;
	}

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


	/**
	 * This method is used to load the cart
	 *
	 * @param request
	 * @param response
	 * @param filterChain
	 * @throws ServletException
	 * @throws IOException
	 */
	@Override
	protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
			final FilterChain filterChain) throws ServletException, IOException
	{
		if (matchesUrl(request, regexp))
		{
			String cartId = "";
			final UserModel user = getUserService().getCurrentUser();
			if (user != null)
			{
				if (getUserService().isAnonymousUser(user))
				{
					cartId = getValue(request, regexp);
				}
				else
				{
					final List<CartModel> carts = wooliesDefaultCartDao.getCartByUID(user.getUid(), getBaseStoreService()
							.getCurrentBaseStore());
					cartId = getCartId(carts);
				}
			}
			cartLoaderStrategy.loadCart(cartId, shouldCartBeRefreshed(request));
		}


		filterChain.doFilter(request, response);
	}

	/**
	 * @param carts
	 * @return
	 */
	private String getCartId(final List<CartModel> carts)
	{
		String cartId;
		if (CollectionUtils.isEmpty(carts))
		{
			cartId = getCartService().getSessionCart().getCode();
		}
		else
		{
			cartId = carts.get(0).getCode();
		}
		return cartId;
	}

	protected boolean shouldCartBeRefreshed(final HttpServletRequest request)
	{
		final String refreshParam = request.getParameter(REFRESH_CART_PARAM);
		return refreshParam == null ? isCartRefreshedByDefault() : Boolean.parseBoolean(refreshParam);
	}

	protected String getRegexp()
	{
		return regexp;
	}

	@Required
	public void setRegexp(final String regexp)
	{
		this.regexp = regexp;
	}

	public CartLoaderStrategy getCartLoaderStrategy()
	{
		return cartLoaderStrategy;
	}

	@Required
	public void setCartLoaderStrategy(final CartLoaderStrategy cartLoaderStrategy)
	{
		this.cartLoaderStrategy = cartLoaderStrategy;
	}

	public boolean isCartRefreshedByDefault()
	{
		return cartRefreshedByDefault;
	}

	public void setCartRefreshedByDefault(final boolean cartRefreshedByDefault)
	{
		this.cartRefreshedByDefault = cartRefreshedByDefault;
	}
}
