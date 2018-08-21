/**
 *
 */
package de.hybris.wooliesegiftcard.facades.user.converters.populator;

import de.hybris.platform.b2b.model.B2BOrderThresholdPermissionModel;
import de.hybris.platform.b2b.model.B2BPermissionModel;
import de.hybris.platform.commercefacades.product.PriceDataFactory;
import de.hybris.platform.commercefacades.product.data.PriceDataType;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BCustomerModel;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BUnitModel;
import de.hybris.wooliesegiftcard.facades.constants.WooliesgcFacadesConstants;

import java.math.BigDecimal;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author 648156 This class is used to populate customer data
 */
public class WooliesB2BCustomerPopulator implements Populator<UserModel, CustomerData>
{
	@Autowired
	private CommonI18NService commonI18NService;
	private PriceDataFactory priceDataFactory;
	private UserService userService;

	/**
	 * @return the userService
	 */
	public UserService getUserService()
	{
		return userService;
	}

	/**
	 * @param userService
	 *           the userService to set
	 */
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	/**
	 * @return the priceDataFactory
	 */
	public PriceDataFactory getPriceDataFactory()
	{
		return priceDataFactory;
	}

	/**
	 * @param priceDataFactory
	 *           the priceDataFactory to set
	 */
	public void setPriceDataFactory(final PriceDataFactory priceDataFactory)
	{
		this.priceDataFactory = priceDataFactory;
	}

	/**
	 * This method is used to populate customer data
	 *
	 * @param source
	 * @param target
	 * @throws ConversionException
	 */
	@Override
	public void populate(final UserModel source, final CustomerData target) throws ConversionException
	{
		if (source instanceof CorporateB2BCustomerModel)
		{
			final CorporateB2BCustomerModel corporateb2bCustomerModel = (CorporateB2BCustomerModel) source;
			Double thresholdValue = null;
			final CorporateB2BUnitModel corporateb2BUnit = (CorporateB2BUnitModel) corporateb2bCustomerModel.getDefaultB2BUnit();
			final Set<B2BPermissionModel> b2bPermissionModel = corporateb2BUnit.getPermissions();
			if (!b2bPermissionModel.isEmpty())
			{
				for (final B2BPermissionModel b2bPermission : b2bPermissionModel)
				{
					thresholdValue = getThresholdValue(corporateb2bCustomerModel, thresholdValue, b2bPermission);
				}
			}
			final Set<PrincipalGroupModel> allGroups = corporateb2bCustomerModel.getAllGroups();
			if (allGroups.contains(userService.getUserGroupForUID(WooliesgcFacadesConstants.B2BADMINGROUP)))
			{
				target.setIsCorpAdminUser(Boolean.TRUE);
			}
			else
			{
				target.setIsCorpAdminUser(Boolean.FALSE);
			}
			if (thresholdValue != null)
			{
				target.setOrderLimit(getPriceDataFactory().create(PriceDataType.BUY,
						BigDecimal.valueOf(thresholdValue.doubleValue()), commonI18NService.getCurrentCurrency().getIsocode()));

			}

		}
	}

	/**
	 * This method is used for get threshold value
	 *
	 * @param corporateb2bCustomerModel
	 * @param thresholdValue
	 * @param b2bPermission
	 * @return thresholdValue
	 */
	private Double getThresholdValue(final CorporateB2BCustomerModel corporateb2bCustomerModel, Double thresholdValue,
			final B2BPermissionModel b2bPermission)
	{
		if (b2bPermission.getCode().equalsIgnoreCase(corporateb2bCustomerModel.getUid()))
		{
			final B2BOrderThresholdPermissionModel b2bOrderPermissionModel = (B2BOrderThresholdPermissionModel) b2bPermission;
			thresholdValue = b2bOrderPermissionModel.getThreshold();
		}
		return thresholdValue;
	}

}
