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
package com.woolies.webservices.rest.auth;

import de.hybris.platform.commerceservices.enums.CustomerType;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.user.UserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;


/**
 * This filter should be used after spring security filters and it is responsible for setting current authentication as
 * guest when user decided to do the checkout as a guest. During the guest checkout the userService gets current user as
 * 'anonymous', but cartService returns dedicated user.
 */
public class GuestRoleFilter extends OncePerRequestFilter
{
	private UserService userService;

	private CartService cartService;

	private AuthenticationEventPublisher authenticationEventPublisher;

	private String guestRole;

	/**
	 *
	 * This method is used to filter all the http request and takes care of authentication of the user
	 *
	 * @param httpservletrequest
	 * @param httpservletresponse
	 * @param filterchain
	 * @throws ServletException
	 * @throws IOException
	 */
	@Override
	protected void doFilterInternal(final HttpServletRequest httpservletrequest, final HttpServletResponse httpservletresponse,
			final FilterChain filterchain) throws ServletException, IOException
	{
		final Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();

		if (userService.isAnonymousUser(userService.getCurrentUser()) && cartService.hasSessionCart())
		{
			final UserModel um = cartService.getSessionCart().getUser();
			if (um != null && CustomerModel.class.isAssignableFrom(um.getClass()))
			{
				final CustomerModel cm = (CustomerModel) um;

				if (isGuest(cm))
				{
					if (currentAuth == null)
					{
						processAuthentication(cm.getUid());
					}
					else if (!currentAuth.getClass().equals(GuestAuthenticationToken.class))
					{
						processAuthentication(cm.getUid());
					}
					else if (!cm.getUid().equals(currentAuth.getName()))
					{
						processAuthentication(cm.getUid());
					}
				}
			}
		}
		filterchain.doFilter(httpservletrequest, httpservletresponse);
	}

	/**
	 * This method is used to Process Authentication
	 *
	 * @param uid
	 */
	protected void processAuthentication(final String uid)
	{
		final Authentication authentication = createGuestAuthentication(uid);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		authenticationEventPublisher.publishAuthenticationSuccess(authentication);
	}

	/**
	 * This method is used to create GuestAuthenticatio
	 *
	 * @param uid
	 * @return authentication
	 */
	protected Authentication createGuestAuthentication(final String uid)
	{
		final Collection<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
		grantedAuthorities.add(new SimpleGrantedAuthority(this.guestRole));
		return new GuestAuthenticationToken(uid, grantedAuthorities);
	}

	/**
	 * To check customer is guest or not
	 * 
	 * @param cm
	 * @return
	 */
	protected boolean isGuest(final CustomerModel cm)
	{
		if (cm == null || cm.getType() == null)
		{
			return false;
		}
		if (cm.getType().toString().equals(CustomerType.GUEST.getCode()))
		{
			return true;
		}
		return false;
	}

	/**
	 * Get authenticationEventPublisher
	 * 
	 * @return authenticationEventPublisher
	 */
	public AuthenticationEventPublisher getAuthenticationEventPublisher()
	{
		return authenticationEventPublisher;
	}

	/**
	 * To set authenticationEventPublisher
	 * 
	 * @param authenticationEventPublisher
	 */
	@Required
	public void setAuthenticationEventPublisher(final AuthenticationEventPublisher authenticationEventPublisher)
	{
		this.authenticationEventPublisher = authenticationEventPublisher;
	}

	/**
	 * Get Guest role
	 * 
	 * @return guestRole
	 */
	public String getGuestRole()
	{
		return guestRole;
	}

	/**
	 * Set Guest role
	 * 
	 * @param guestRole
	 */
	@Required
	public void setGuestRole(final String guestRole)
	{
		this.guestRole = guestRole;
	}

	/**
	 * Get UserService
	 * 
	 * @return userService
	 */
	public UserService getUserService()
	{
		return userService;
	}

	/**
	 * Set UserService
	 * 
	 * @param userService
	 */
	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	/**
	 * Gets CartService
	 * 
	 * @return cartService
	 */
	public CartService getCartService()
	{
		return cartService;
	}

	/**
	 * Sets CartService
	 * 
	 * @param cartService
	 */
	@Required
	public void setCartService(final CartService cartService)
	{
		this.cartService = cartService;
	}

}
