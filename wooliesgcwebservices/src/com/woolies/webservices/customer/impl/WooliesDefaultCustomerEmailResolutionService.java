/**
 *
 */
package com.woolies.webservices.customer.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import de.hybris.platform.commerceservices.customer.impl.DefaultCustomerEmailResolutionService;
import de.hybris.platform.core.model.user.CustomerModel;


/**
 * @author 668982
 *
 */
public class WooliesDefaultCustomerEmailResolutionService extends DefaultCustomerEmailResolutionService
{
	@Override
	protected String validateAndProcessEmailForCustomer(final CustomerModel customerModel)
	{
		validateParameterNotNullStandardMessage("customerModel", customerModel);

		return customerModel.getUserEmail();
	}

}
