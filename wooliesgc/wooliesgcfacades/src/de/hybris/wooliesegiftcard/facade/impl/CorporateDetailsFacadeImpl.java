/**
 *
 */
package de.hybris.wooliesegiftcard.facade.impl;

import de.hybris.platform.b2bcommercefacades.company.data.B2BUnitData;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BCustomerModel;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BUnitModel;
import de.hybris.wooliesegiftcard.exception.WooliesB2BUserException;
import de.hybris.wooliesegiftcard.facade.CorporateDetailsFacade;
import de.hybris.wooliesegiftcard.facades.CorporateDetailsDTO;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.wooliesegiftcard.facades.constants.WooliesgcFacadesConstants;
import java.util.Set;

/**
 * @author 648156 This class is used for to maintain corporate details
 */
public class CorporateDetailsFacadeImpl implements CorporateDetailsFacade
{
	private UserService userService;
	private ModelService modelService;

	/**
	 * @return the modelService
	 */
	public ModelService getModelService()
	{
		return modelService;
	}

	/**
	 * @param modelService
	 *           the modelService to set
	 */
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

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
	 * This method is used to get he corporate details
	 *
	 * @return the b2b unit data
	 * @throws WooliesB2BUserException
	 */
	@Override
	public B2BUnitData getCorporateDetails() throws WooliesB2BUserException
	{
		final B2BUnitData b2bUnitData = new B2BUnitData();
		final UserModel userModel = getCurrentUser();
		if (userModel != null && userModel instanceof CorporateB2BCustomerModel)
		{
			final CorporateB2BCustomerModel corporateB2BCustomerModel = (CorporateB2BCustomerModel) userModel;
			b2bUnitData.setCorporateABN(((CorporateB2BUnitModel) corporateB2BCustomerModel.getDefaultB2BUnit()).getCorporateABN());
			b2bUnitData.setCorporateName(((CorporateB2BUnitModel) corporateB2BCustomerModel.getDefaultB2BUnit()).getName());
			b2bUnitData.setCompanyType((((CorporateB2BUnitModel) corporateB2BCustomerModel.getDefaultB2BUnit()).getCompanyType()));
			b2bUnitData.setCharityOrganization(
					((CorporateB2BUnitModel) corporateB2BCustomerModel.getDefaultB2BUnit()).getCharityOrganization());
					b2bUnitData.setTradingName(((CorporateB2BUnitModel) corporateB2BCustomerModel.getDefaultB2BUnit()).getTradingName());
		}
		else
		{
			throw new WooliesB2BUserException(WooliesgcFacadesConstants.ERRCODE_ISNOTEXIST);
		}
		return b2bUnitData;
	}

	/**
	 * This method is used to update corporate details
	 *
	 * @param corporateWsDTO
	 * @throws WooliesB2BUserException
	 */
	@Override
	public void updateCorporateDetails(final CorporateDetailsDTO corporateWsDTO) throws WooliesB2BUserException
	{
		CorporateB2BUnitModel corporateb2bUnitmodel;
		final UserModel userModel = getCurrentUser();
		if (userModel instanceof CorporateB2BCustomerModel)
		{
			final CorporateB2BCustomerModel corporateb2bCustomerModel = (CorporateB2BCustomerModel) userModel;
			corporateb2bUnitmodel = (CorporateB2BUnitModel) corporateb2bCustomerModel.getDefaultB2BUnit();
			final Set<PrincipalGroupModel> allGroups = corporateb2bCustomerModel.getAllGroups();
			if (allGroups.contains(userService.getUserGroupForUID(WooliesgcFacadesConstants.B2BADMINGROUP)))
			{
				corporateb2bUnitmodel.setCorporateABN(corporateWsDTO.getCorporateABN());
				corporateb2bUnitmodel.setName(corporateWsDTO.getCorporateName());
				corporateb2bUnitmodel.setCompanyType(corporateWsDTO.getCompanyType());
				corporateb2bUnitmodel.setCharityOrganization(corporateWsDTO.getCharityOrganization());
				corporateb2bUnitmodel.setTradingName(corporateWsDTO.getTradingName());
				getModelService().save(corporateb2bUnitmodel);
			}
			else
			{
				throw new WooliesB2BUserException(WooliesgcFacadesConstants.ERRCODE_ISNOTADMIN);
			}
		}
		else
		{
			throw new WooliesB2BUserException(WooliesgcFacadesConstants.ERRCODE_ISNOTEXIST);
		}
	}

	/**
	 * To get current user
	 * 
	 * @return the userModel
	 */
	protected UserModel getCurrentUser()
	{
		return getUserService().getCurrentUser();
	}
}



