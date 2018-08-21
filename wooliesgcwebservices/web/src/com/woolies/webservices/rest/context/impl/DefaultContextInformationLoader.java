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
package com.woolies.webservices.rest.context.impl;

import de.hybris.platform.basecommerce.exceptions.BaseSiteActivationException;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.basecommerce.strategies.ActivateBaseSiteInSessionStrategy;
import de.hybris.platform.commerceservices.i18n.CommerceCommonI18NService;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CalculationService;
import de.hybris.platform.order.CartService;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import de.hybris.platform.webservicescommons.util.YSanitizer;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.woolies.webservices.rest.constants.YcommercewebservicesConstants;
import com.woolies.webservices.rest.context.ContextInformationLoader;
import com.woolies.webservices.rest.exceptions.InvalidResourceException;
import com.woolies.webservices.rest.exceptions.RecalculationException;
import com.woolies.webservices.rest.exceptions.UnsupportedCurrencyException;
import com.woolies.webservices.rest.exceptions.UnsupportedLanguageException;


/**
 * Default context information loader
 */
public class DefaultContextInformationLoader implements ContextInformationLoader
{
	private static final String[] urlSplitters =
	{ "/v1/", "/v2/" };

	private static final Logger LOG = Logger.getLogger(DefaultContextInformationLoader.class);

	private BaseSiteService baseSiteService;
	private ActivateBaseSiteInSessionStrategy activateBaseSiteInSessionStrategy;
	private ConfigurationService configurationService;
	private Set<String> baseSiteResourceExceptions;
	private CommonI18NService commonI18NService;
	private CommerceCommonI18NService commerceCommonI18NService;
	private BaseStoreService baseStoreService;
	private CartService cartService;
	private CalculationService calculationService;

	/**
	 * This method is used to set language which is coming from the request
	 *
	 * @param request
	 */
	@Override
	public LanguageModel setLanguageFromRequest(final HttpServletRequest request) throws UnsupportedLanguageException
	{
		final String languageString = request.getParameter(YcommercewebservicesConstants.HTTP_REQUEST_PARAM_LANGUAGE);
		LanguageModel languageToSet = null;

		if (!StringUtils.isBlank(languageString))
		{
			try
			{
				languageToSet = getCommonI18NService().getLanguage(languageString);
			}
			catch (final UnknownIdentifierException e)
			{
				throw new UnsupportedLanguageException("Language  " + YSanitizer.sanitize(languageString) + " is not supported", e);
			}
		}

		if (languageToSet == null)
		{
			languageToSet = getCommerceCommonI18NService().getDefaultLanguage();
		}

		final BaseStoreModel currentBaseStore = getBaseStoreService().getCurrentBaseStore();

		if (currentBaseStore != null)
		{
			final Collection<LanguageModel> storeLanguages = getStoresLanguages(currentBaseStore);

			if (storeLanguages.isEmpty())
			{
				throw new UnsupportedLanguageException("Current base store supports no languages!");
			}

			if (!storeLanguages.contains(languageToSet))
			{
				throw new UnsupportedLanguageException(languageToSet);
			}
		}


		if (languageToSet != null && !languageToSet.equals(getCommerceCommonI18NService().getCurrentLanguage()))
		{
			getCommerceCommonI18NService().setCurrentLanguage(languageToSet);

			if (LOG.isDebugEnabled())
			{
				LOG.debug(languageToSet + " set as current language");
			}
		}
		return languageToSet;
	}

	/**
	 * Get storesLanguages
	 *
	 * @param currentBaseStore
	 * @return collections of languageModel
	 */
	protected Collection<LanguageModel> getStoresLanguages(final BaseStoreModel currentBaseStore)
	{
		if (currentBaseStore == null)
		{
			throw new IllegalStateException("No current base store was set!");
		}
		return currentBaseStore.getLanguages() == null ? Collections.<LanguageModel> emptySet() : currentBaseStore.getLanguages();
	}

	/**
	 * This method sets currency from the request
	 *
	 * @param request
	 */
	@Override
	public CurrencyModel setCurrencyFromRequest(final HttpServletRequest request)
			throws UnsupportedCurrencyException, RecalculationException
	{
		final String currencyString = request.getParameter(YcommercewebservicesConstants.HTTP_REQUEST_PARAM_CURRENCY);
		CurrencyModel currencyToSet = null;

		if (!StringUtils.isBlank(currencyString))
		{
			try
			{
				currencyToSet = getCommonI18NService().getCurrency(currencyString);
			}
			catch (final UnknownIdentifierException e)
			{
				throw new UnsupportedCurrencyException("Currency " + YSanitizer.sanitize(currencyString) + " is not supported", e);
			}
		}

		if (currencyToSet == null)
		{
			currencyToSet = getCommerceCommonI18NService().getDefaultCurrency();
		}

		final BaseStoreModel currentBaseStore = getBaseStoreService().getCurrentBaseStore();

		if (currentBaseStore != null)
		{
			final List<CurrencyModel> storeCurrencies = getCommerceCommonI18NService().getAllCurrencies();

			if (storeCurrencies.isEmpty())
			{
				throw new UnsupportedCurrencyException("Current base store supports no currencies!");
			}

			if (!storeCurrencies.contains(currencyToSet))
			{
				throw new UnsupportedCurrencyException(currencyToSet);
			}
		}

		if (currencyToSet != null && !currencyToSet.equals(getCommerceCommonI18NService().getCurrentCurrency()))
		{
			getCommerceCommonI18NService().setCurrentCurrency(currencyToSet);
			recalculateCart(currencyString);
			if (LOG.isDebugEnabled())
			{
				LOG.debug(currencyToSet + " set as current currency");
			}
		}

		return currencyToSet;
	}

	/**
	 * Recalculates cart when currency has changed
	 */
	protected void recalculateCart(final String currencyString) throws RecalculationException
	{
		if (getCartService().hasSessionCart())
		{
			final CartModel cart = getCartService().getSessionCart();
			if (cart != null)
			{
				try
				{
					getCalculationService().recalculate(cart);
				}
				catch (final CalculationException e)
				{
					throw new RecalculationException(e, YSanitizer.sanitize(currencyString));
				}
			}
		}
	}

	/**
	 * Method resolves base site uid from request URL and set it as current site i.e<br>
	 * <i>/rest/v1/mysite/cart</i>, or <br>
	 * <i>/rest/v1/mysite/customers/current</i><br>
	 * would try to set base site with uid=mysite as a current site.<br>
	 *
	 * One should define the path which is expected to be before the site resource in the project properties file
	 * (<b>commercewebservices.rootcontext</b>).<br>
	 * Default and fallback value equals to <i>/rest/v1/</i><br>
	 *
	 * Method uses also a comma separated list of url special characters that are used to parse the site id resource. You
	 * can reconfigure it in properties file (<b>commercewebservices.url.special.characters</b>). The default and
	 * fallback value is equal to <i>"?,/</i>".
	 *
	 * Method will throw {@link InvalidResourceException} if it fails to find the site which is in the resource url.<br>
	 * However, you can configure exceptions that doesn't require the site mapping in the resource path. You can
	 * configure them in a spring bean called 'baseFilterResourceExceptions'.<br>
	 *
	 * @param request
	 *           - request from which we should get base site uid
	 *
	 * @return baseSite set as current site or null
	 * @throws InvalidResourceException
	 */
	@Override
	public BaseSiteModel initializeSiteFromRequest(final HttpServletRequest request) throws InvalidResourceException
	{
		final String requestURL = request.getRequestURL().toString();
		final String requestMapping = getRequestMapping(requestURL);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Requested resource : " + YSanitizer.sanitize(requestMapping));
		}
		if (requestMapping == null || isNotBaseSiteResource(requestMapping))
		{
			return null;
		}

		final String baseSiteUid = parseBaseSiteId(requestMapping);

		final BaseSiteModel requestedBaseSite = getBaseSiteService().getBaseSiteForUID(baseSiteUid);

		if (requestedBaseSite != null)
		{
			final BaseSiteModel currentBaseSite = getBaseSiteService().getCurrentBaseSite();

			if (!requestedBaseSite.equals(currentBaseSite))
			{
				setCurrentBaseSite(requestedBaseSite);
			}
		}
		else
		{
			throw new InvalidResourceException(YSanitizer.sanitize(baseSiteUid));
		}

		return requestedBaseSite;
	}

	/**
	 * This method is used to get request mapping
	 *
	 * @param queryString
	 * @return
	 */
	protected String getRequestMapping(final String queryString)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Processing request : " + YSanitizer.sanitize(queryString));
		}

		int rootContextIndex = -1;
		for (final String rootContext : urlSplitters)
		{
			rootContextIndex = queryString.indexOf(rootContext);
			if (rootContextIndex != -1)
			{
				String result = queryString.substring(rootContextIndex);
				result = result.replaceAll(rootContext, "");
				return result;
			}
		}

		return null;
	}

	/**
	 * This method is used to check request is base site resource or not
	 *
	 * @param requestMapping
	 * @return
	 */
	protected boolean isNotBaseSiteResource(final String requestMapping)
	{
		for (final String exception : getBaseSiteResourceExceptions())
		{
			if (requestMapping.startsWith(exception))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * This method is used to parseBase site id
	 *
	 * @param requestMapping
	 * @return result
	 */
	protected String parseBaseSiteId(final String requestMapping)
	{
		String result = requestMapping;

		final String[] specialCharacters = getSpecialUrlCharacters();
		for (final String specialCharacter : specialCharacters)
		{
			final int specialCharacterIndex = result.indexOf(specialCharacter);
			if (specialCharacterIndex != -1)
			{
				result = result.substring(0, specialCharacterIndex);
			}
		}

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Parsed base site uid: " + YSanitizer.sanitize(result));
		}
		return result;
	}

	/**
	 * This method is used to get get special url characters
	 * 
	 * @return
	 */
	protected String[] getSpecialUrlCharacters()
	{
		final String configurationString = getConfigurationService().getConfiguration().getString(
				YcommercewebservicesConstants.URL_SPECIAL_CHARACTERS_PROPERTY,
				YcommercewebservicesConstants.DEFAULT_URL_SPECIAL_CHARACTERS);
		return configurationString.split(",");
	}

	/**
	 * Sets the CurrentBaseSit
	 * 
	 * @param baseSiteModel
	 */
	protected void setCurrentBaseSite(final BaseSiteModel baseSiteModel)
	{
		if (baseSiteModel != null)
		{
			LOG.debug("setCurrentSite : " + baseSiteModel);
			try
			{
				getBaseSiteService().setCurrentBaseSite(baseSiteModel, false);
				getActivateBaseSiteInSessionStrategy().activate(baseSiteModel);
				LOG.debug("Base site " + baseSiteModel + " activated.");
			}
			catch (final BaseSiteActivationException e)
			{
				LOG.error("Could not set current base site to " + baseSiteModel, e);
			}
		}
	}

	/***
	 * Gets configuration service
	 * 
	 * @return configurationService
	 */
	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	/**
	 * sets configuration service
	 * 
	 * @param configurationService
	 */
	@Required
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	/**
	 * Gets Base Site ResourceExceptions
	 * 
	 * @return
	 */
	public Set<String> getBaseSiteResourceExceptions()
	{
		return baseSiteResourceExceptions;
	}

	/**
	 * Sets Base Site ResourceExceptions
	 * 
	 * @param baseSiteResourceExceptions
	 */
	@Required
	public void setBaseSiteResourceExceptions(final Set<String> baseSiteResourceExceptions)
	{
		this.baseSiteResourceExceptions = baseSiteResourceExceptions;
	}

	/**
	 * Get Base site service
	 * 
	 * @return baseSiteService
	 */
	public BaseSiteService getBaseSiteService()
	{
		return baseSiteService;
	}

	/**
	 * Set the Base site service
	 * 
	 * @param baseSiteService
	 */
	@Required
	public void setBaseSiteService(final BaseSiteService baseSiteService)
	{
		this.baseSiteService = baseSiteService;
	}

	/**
	 * Get Activate baseSite in session strategy
	 * 
	 * @return activateBaseSiteInSessionStrategy
	 */
	public ActivateBaseSiteInSessionStrategy getActivateBaseSiteInSessionStrategy()
	{
		return activateBaseSiteInSessionStrategy;
	}

	/**
	 * Set the activate baseSite in session strategy
	 * 
	 * @param activateBaseSiteInSessionStrategy
	 */
	@Required
	public void setActivateBaseSiteInSessionStrategy(final ActivateBaseSiteInSessionStrategy activateBaseSiteInSessionStrategy)
	{
		this.activateBaseSiteInSessionStrategy = activateBaseSiteInSessionStrategy;
	}

	/**
	 * Get CommonI18NService
	 * 
	 * @return commonI18NService
	 */
	public CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	/**
	 * Set the CommonI18NService
	 * 
	 * @param commonI18NService
	 */
	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	/**
	 * Get Commerce CommonI18NService
	 * 
	 * @return commerceCommonI18NService
	 */
	public CommerceCommonI18NService getCommerceCommonI18NService()
	{
		return commerceCommonI18NService;
	}

	/**
	 * Set the Commerce CommonI18NService
	 * 
	 * @param commerceCommonI18NService
	 */
	@Required
	public void setCommerceCommonI18NService(final CommerceCommonI18NService commerceCommonI18NService)
	{
		this.commerceCommonI18NService = commerceCommonI18NService;
	}

	/**
	 * Get the baseStore Service
	 * 
	 * @return
	 */
	public BaseStoreService getBaseStoreService()
	{
		return baseStoreService;
	}

	/**
	 * Set the baseStore Service
	 * 
	 * @param baseStoreService
	 */
	@Required
	public void setBaseStoreService(final BaseStoreService baseStoreService)
	{
		this.baseStoreService = baseStoreService;
	}

	/**
	 * Get Cart Service
	 * 
	 * @return cartService
	 */
	public CartService getCartService()
	{
		return cartService;
	}

	/**
	 * Set the Cart Service
	 * 
	 * @param cartService
	 */
	@Required
	public void setCartService(final CartService cartService)
	{
		this.cartService = cartService;
	}

	/**
	 * Get the CalculationService
	 * 
	 * @return calculationService
	 */
	public CalculationService getCalculationService()
	{
		return calculationService;
	}

	/**
	 * Set the CalculationService
	 * 
	 * @param calculationService
	 */
	@Required
	public void setCalculationService(final CalculationService calculationService)
	{
		this.calculationService = calculationService;
	}
}
