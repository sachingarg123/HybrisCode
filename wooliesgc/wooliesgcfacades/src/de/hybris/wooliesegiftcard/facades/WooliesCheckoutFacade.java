/**
 *
 */
package de.hybris.wooliesegiftcard.facades;

/**
 * @author 668982 This interface is used to maintain checkout details
 */
public interface WooliesCheckoutFacade
{
	/**
	 * This method is used to create cart from the given order code
	 *
	 * @param orderCode
	 *           the parameter value to be used
	 */
	void createCartFromOrder(final String orderCode);
}
