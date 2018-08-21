/**
 *
 */
package de.hybris.wooliesegiftcard.facade;

import de.hybris.platform.b2bcommercefacades.company.data.B2BUnitData;
import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.commercewebservicescommons.dto.product.PriceWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.user.UserSignUpWsDTO;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BUnitModel;
import de.hybris.wooliesegiftcard.exception.WooliesB2BUserException;
import de.hybris.wooliesegiftcard.facades.RemoveCustomerDTO;
import de.hybris.wooliesegiftcard.facades.dto.ResponseDTO;

import java.util.List;


/**
 * @author 648156 This interface is used to maintain the b2b user account details
 *
 */
public interface WooliesB2BUnitFacade extends CustomerFacade
{

	/**
	 * This used to create b2b unit
	 * 
	 * @param b2bUnitData
	 *           the parameter value to be used
	 * @return CorporateB2BUnitModel the parameter used to return
	 * @throws DuplicateUidException
	 *            used to through exception
	 * @throws WooliesB2BUserException
	 *            used to through exception
	 */
	CorporateB2BUnitModel createB2BUnit(final B2BUnitData b2bUnitData) throws DuplicateUidException, WooliesB2BUserException;

	/**
	 * This method is used to register the b2b customer details for the given unit model and customer data
	 *
	 * @param customerData
	 *           the parameter value to be used
	 * @param corporateB2BUnitModel
	 *           the parameter used to return
	 * @throws DuplicateUidException
	 *            used to through exception
	 */

	void register(final CustomerData customerData, final CorporateB2BUnitModel corporateB2BUnitModel) throws DuplicateUidException;

	/**
	 * This method is used to manage b2b user
	 *
	 * @param user
	 *           the parameter value to be used
	 * @throws WooliesB2BUserException
	 *            used to through exception
	 * @throws DuplicateUidException
	 *            used to through exception
	 */

	void b2bUserManagement(final UserSignUpWsDTO user) throws WooliesB2BUserException, DuplicateUidException;

	/**
	 * This method is used to remove the account of the user
	 *
	 * @param user
	 *           the parameter value to be used
	 * @return the responseDTO the parameter used to return
	 * @throws WooliesB2BUserException
	 *            used to through exception
	 */
	ResponseDTO removeAccount(final RemoveCustomerDTO user) throws WooliesB2BUserException;

	/**
	 * This method is used to check whether customer is deactivated or not
	 *
	 * @param user
	 *           the parameter value to be used
	 * @return Boolean the parameter used to return
	 * @throws WooliesB2BUserException
	 *            used to through exception
	 */
	Boolean isCustomerDeactivated(final RemoveCustomerDTO user) throws WooliesB2BUserException;

	/**
	 * This method is used to get b2b account details of the user
	 *
	 * @param email
	 *           the parameter value to be used
	 * @return CustomerData the parameter used to return
	 * @throws WooliesB2BUserException
	 *            used to through exception
	 */
	List<CustomerData> getB2BAccountUsers(final String email) throws WooliesB2BUserException;

	/**
	 * This method is used to update the order limit
	 *
	 * @param userId
	 *           the parameter value to be used
	 * @param adminUid
	 *           the parameter value to be used
	 * @param orderLimit
	 *           the parameter value to be used
	 * @throws WooliesB2BUserException
	 *            used to through exception
	 */
	void updateOrderLimit(String userId, String adminUid, PriceWsDTO orderLimit) throws WooliesB2BUserException;
}
