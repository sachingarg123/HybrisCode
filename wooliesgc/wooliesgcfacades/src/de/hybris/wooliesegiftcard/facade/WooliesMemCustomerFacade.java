/**
 *
 */
package de.hybris.wooliesegiftcard.facade;

import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercewebservicescommons.dto.user.UserSignUpWsDTO;
import de.hybris.wooliesegiftcard.core.model.MemberCustomerModel;
import de.hybris.wooliesegiftcard.core.model.MemberUnitModel;
import de.hybris.wooliesegiftcard.exceptions.WooliesFacadeLayerException;
import de.hybris.wooliesegiftcard.facades.dto.PlaceOrderRequestDTO;

import java.util.List;


/**
 * @author 669567 This interface is used to maintain customer member user details
 */
public interface WooliesMemCustomerFacade extends CustomerFacade
{
	/**
	 * This method is used to geet he member user data
	 *
	 * @param user
	 *           the User during Signup as a Member User
	 * @param memberUnit
	 *           the Member Unit
	 * @return userProfileData the Profile summary data for the User
	 * @throws WooliesFacadeLayerException
	 *            throws this exception
	 */
	public void getMemberUser(UserSignUpWsDTO user, MemberUnitModel memberUnit) throws WooliesFacadeLayerException;

	/**
	 * This method is used to get member unit model for the given member id
	 *
	 * @param memberId
	 *           MemberUnitModel for this MemberId
	 * @return the list of MemberUnitModel
	 */
	List<MemberUnitModel> getMemberUnit(String memberId);

	/**
	 * This method is used to decrypt the given token
	 *
	 * @param memberToken
	 *           The token used to decrypt
	 * @param memberKey
	 *           the Key used for decryption
	 * @return the decrypt token as a String value
	 */
	String decrypt(final String memberToken, final String memberKey);

	/**
	 * This Method is used to set the Shipping Address during Place order Request for the User
	 *
	 * @param placeOrderRequestDTO
	 *           the Data request during placeOrder request
	 * @param billingAddressData
	 *           the Billing Address Data for the User\
	 *
	 * @param shippingAddressData
	 *           the Shipping Address Data for the User
	 * @param userId
	 *           The Current Member User
	 * @return the MemberCustomerModel
	 */

	public MemberCustomerModel saveMemberProfile(final PlaceOrderRequestDTO placeOrderRequestDTO,
			final AddressData shippingAddressData, final AddressData billingAddressData, final String userId);



}
