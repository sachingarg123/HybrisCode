
package de.hybris.wooliesegiftcard.facades.user.converters.populator;

/**
 * @author 676313
 *
 */
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import java.text.SimpleDateFormat;
import de.hybris.wooliesegiftcard.facades.constants.WooliesgcFacadesConstants;


/**
 * @author 676313 This class is used to populate B2c view profile
 */

public class WooliesB2CViewPopulator implements Populator<UserModel, CustomerData>
{
	/**
	 * This method is used to populate populate B2B view profile
	 * 
	 * @param source
	 * @param target
	 * @throws ConversionException
	 */
	@Override
	public void populate(final UserModel source, final CustomerData target) throws ConversionException
	{

        target.setFirstName(((CustomerModel) source).getFirstName());
		target.setLastName(((CustomerModel) source).getLastName());
		target.setUid(((CustomerModel) source).getUid());
		if (((CustomerModel) source).getDob() != null)
		{
			final SimpleDateFormat formatter = new SimpleDateFormat(WooliesgcFacadesConstants.DOB);
			target.setDob(formatter.format(((CustomerModel) source).getDob()));
		}
		target.setPhone(((CustomerModel) source).getPhone());
		target.setCardNo(((CustomerModel) source).getCardNo());
			target.setCustomerId(((CustomerModel) source).getCustomerID());
		target.setEmail(((CustomerModel) source).getUserEmail());
		target.setOptInForMarketing(((CustomerModel) source).getOptInForMarketing());
	}
}
