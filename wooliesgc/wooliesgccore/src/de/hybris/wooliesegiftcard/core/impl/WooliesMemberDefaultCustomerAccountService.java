/**
 *
 */
package de.hybris.wooliesegiftcard.core.impl;

import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.commerceservices.customer.impl.DefaultCustomerAccountService;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.impl.UniqueAttributesInterceptor;
import de.hybris.wooliesegiftcard.core.WooliesMemberCustomerAccountService;
import de.hybris.wooliesegiftcard.core.model.MemberCustomerModel;


/**
 * @author 669567 This class is used for maintaining default customer Account service details
 *
 */
public class WooliesMemberDefaultCustomerAccountService extends DefaultCustomerAccountService
		implements WooliesMemberCustomerAccountService
{

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.hybris.wooliesegiftcard.core.WooliesMemberCustomerAccountService#register(de.hybris.wooliesegiftcard.core.model
	 * .MemberCustomerModel)
	 */
	@Override
	public void register(final MemberCustomerModel memberCustomer) throws DuplicateUidException
	{
		registerCustomer(memberCustomer);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.hybris.wooliesegiftcard.core.WooliesMemberCustomerAccountService#registerCustomer(de.hybris.wooliesegiftcard.
	 * core.model.MemberCustomerModel)
	 */
	@Override
	public void registerCustomer(final MemberCustomerModel memberCustomer) throws DuplicateUidException
	{
		internalSaveCustomer(memberCustomer);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.wooliesegiftcard.core.WooliesMemberCustomerAccountService#internalSaveCustomer(de.hybris.
	 * wooliesegiftcard.core.model.MemberCustomerModel)
	 */
	@Override
	public void internalSaveCustomer(final MemberCustomerModel memberCustomer) throws DuplicateUidException
	{
		try
		{
			getModelService().save(memberCustomer);
		}
		catch (final ModelSavingException modelSavingException)
		{
			if (modelSavingException.getCause() instanceof InterceptorException
					&& ((InterceptorException) modelSavingException.getCause()).getInterceptor().getClass()
							.equals(UniqueAttributesInterceptor.class))
			{
				throw new DuplicateUidException(memberCustomer.getUid(), modelSavingException);
			}
			else
			{
				throw modelSavingException;
			}
		}
		catch (final AmbiguousIdentifierException ambigIdentifyException)
		{
			throw new DuplicateUidException(memberCustomer.getUid(), ambigIdentifyException);
		}
	}

	/**
	 * To update the member details
	 *
	 * @param memberCustomer
	 *           the member data to update
	 */
	@Override
	public void updateMemberCustomer(final MemberCustomerModel memberCustomer)
	{
		getModelService().save(memberCustomer);

	}

}
