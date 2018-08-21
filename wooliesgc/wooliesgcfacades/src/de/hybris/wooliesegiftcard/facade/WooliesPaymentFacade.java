/**
 *
 */
package de.hybris.wooliesegiftcard.facade;

import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.wooliesegiftcard.exceptions.WooliesFacadeLayerException;
import de.hybris.wooliesegiftcard.facades.dto.PaymentAuthError;
import de.hybris.wooliesegiftcard.facades.dto.PlaceOrderRequestDTO;

import java.math.BigDecimal;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import com.woolies.webservices.dto.PaymentInfoDetails;




/**
 * @author 669567 This interface is used to maintain payment details
 *
 */
public interface WooliesPaymentFacade
{
	/**
	 * This method is used to get the payment details
	 *
	 * @param userModel
	 *           the User model as a parameter
	 * @param isAnonymousUser
	 *           bollean value if it is Anonymous user or not
	 * @param cartTotalPrice
	 *           the Cart Total Price as a parameter
	 * @return the paymentInfoDetails it returns the Payment details of the user
	 * @throws WooliesFacadeLayerException
	 *            throws this exception in case of any error
	 */
	PaymentInfoDetails getPaymentDetails(UserModel userModel, boolean isAnonymousUser, final BigDecimal cartTotalPrice)
			throws WooliesFacadeLayerException;

	/**
	 * This method is used to save payment information
	 *
	 * @param placeOrderRequestDTO
	 *           the Request while place Order as a parameter
	 * @param addressData
	 *           the address of the user
	 * @param userModel
	 *           the User model as a parameter
	 * @return payment details saved or not
	 * @throws CalculationException
	 *            throws this exception in case of any error
	 * @throws WooliesFacadeLayerException
	 *            throws this exception in case of any error
	 */
	boolean savePaymentInfo(PlaceOrderRequestDTO placeOrderRequestDTO, AddressData addressData, CartModel userModel)
			throws CalculationException, WooliesFacadeLayerException;

	/**
	 * This method is used to deAuthorize
	 *
	 * @param placeOrderRequestDTO
	 *           he Request while place Order as a parameter
	 * @param cartModel
	 *           the Cart of the User
	 * @param authErrors
	 *           the Authorization errors
	 * @param orderDate
	 *           orderDate
	 * @return doAuthorized or not boolean value
	 * @throws WooliesFacadeLayerException
	 *            throws this exception in case of any error
	 * @throws KeyManagementException
	 *            throws this exception in case of any error
	 * @throws NoSuchAlgorithmException
	 *            throws this exception in case of any error
	 * @throws CalculationException
	 *            throws this exception in case of any error
	 * @throws ParseException
	 *            throws this exception in case of any error
	 */
	public boolean doAuthorize(PlaceOrderRequestDTO placeOrderRequestDTO, CartModel cartModel, List<PaymentAuthError> authErrors,
			Date orderDate) throws WooliesFacadeLayerException, KeyManagementException, NoSuchAlgorithmException,
			CalculationException, ParseException;



}