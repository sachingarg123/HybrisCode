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
package de.hybris.wooliesegiftcard.core.jalo;

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;
import de.hybris.wooliesegiftcard.core.constants.WooliesgcCoreConstants;


/**
 *
 * @author cognizant
 *
 *         This class is core manager program to to get extension
 *
 */
public class WooliesgcCoreManager extends GeneratedWooliesgcCoreManager
{
	/**
	 * To create instance for WooliesgcCoreManager
	 * 
	 * @return WooliesgcCoreManager
	 */
	public static final WooliesgcCoreManager getInstance()
	{
		final ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (WooliesgcCoreManager) em.getExtension(WooliesgcCoreConstants.EXTENSIONNAME);
	}
}
