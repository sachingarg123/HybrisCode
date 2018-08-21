/**
 *
 */
package de.hybris.wooliesegiftcard.facades.user.converters.populator;

import de.hybris.platform.cmsfacades.data.MediaData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.wooliesegiftcard.core.model.MemberCustomerModel;
import de.hybris.wooliesegiftcard.core.model.MemberUnitModel;
import de.hybris.wooliesegiftcard.facades.MemberData;
import de.hybris.wooliesegiftcard.facades.UserProfileData;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author 687679 This class is used to populate member customer profile
 */
public class WooliesMemberCustomerProfilePopulator implements Populator<UserModel, UserProfileData>
{
	@Autowired
	private Converter<MediaModel, MediaData> mediaModelConverter;
	@Autowired
	private ConfigurationService configurationService;


	/**
	 * This method populates member customer profile for the give user model
	 *
	 * @param memberModel
	 * @param userProfileData
	 */
	@Override
	public void populate(final UserModel source, final UserProfileData target) throws ConversionException
	{
		final CustomerModel customerModel = (CustomerModel) source;
		target.setFirstName(customerModel.getFirstName());
		if (StringUtils.isNotBlank(customerModel.getLastName()))
		{
			target.setLastName(customerModel.getLastName());
		}
		if (StringUtils.isNotBlank(customerModel.getPhone()))
		{
			target.setPhone(customerModel.getPhone());
		}
		final String absolutePath = configurationService.getConfiguration().getString("website.woolworths.giftcards.https");
		final MemberUnitModel memberUnitModel = (MemberUnitModel) ((MemberCustomerModel) source).getDefaultB2BUnit();
		final MemberData member = new MemberData();
		member.setHeading1(memberUnitModel.getHeading1());
		member.setFontColor1(memberUnitModel.getFontColor1());
		member.setHeading2(memberUnitModel.getHeading2());
		member.setFontColor2(memberUnitModel.getFontColor2());
		member.setHeading3(memberUnitModel.getHeading3());
		member.setFontColor3(memberUnitModel.getFontColor3());

		if (memberUnitModel.getMemberBackgroundImage() != null)
		{
			final String backgroundImageUrl = memberUnitModel.getMemberBackgroundImage().getURL();
			final MediaModel mediaModel1 = memberUnitModel.getMemberBackgroundImage();
			mediaModel1.setURL(absolutePath + backgroundImageUrl);
			member.setMemberBackgroundImage(mediaModelConverter.convert(mediaModel1));
		}
		if (memberUnitModel.getMemberLogoImage() != null)
		{
			final String logoImageUrl = memberUnitModel.getMemberLogoImage().getURL();
			final MediaModel mediaModel2 = memberUnitModel.getMemberLogoImage();
			mediaModel2.setURL(absolutePath + logoImageUrl);
			member.setMemberLogoImage(mediaModelConverter.convert(mediaModel2));
		}
		member.setMemberName(memberUnitModel.getLocName());
		target.setMember(member);
		target.setAccountId(memberUnitModel.getUid());
		target.setAccountType(memberUnitModel.getAccountType());
		target.setUid(customerModel.getUid());
		target.setCustomerId(customerModel.getCustomerID());
		target.setEmail(customerModel.getUserEmail());
	}


}