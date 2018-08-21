/**
 *
 */
package de.hybris.wooliesegiftcard.facade;

import de.hybris.platform.b2bcommercefacades.company.data.B2BUnitData;
import de.hybris.wooliesegiftcard.exception.WooliesB2BUserException;
import de.hybris.wooliesegiftcard.facades.CorporateDetailsDTO;


/**
 * @author 648156 This interface is used for to maintain corporate details
 *
 */
public interface CorporateDetailsFacade
{

	/**
	 *
	 * This method is used to get he corporate details
	 * 
	 * @return the b2b unit data
	 * @throws WooliesB2BUserException exception thrown by below method
	 */
	B2BUnitData getCorporateDetails() throws WooliesB2BUserException;

	/**
	 * This method is used to update corporate details
	 * 
	 * @param corporateWsDTO the parameter for below method
	 * @throws WooliesB2BUserException exception thrown by below method
	 */
	void updateCorporateDetails(CorporateDetailsDTO corporateWsDTO) throws WooliesB2BUserException;
}
