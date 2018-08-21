/**
 *
 */
package de.hybris.wooliesegiftcard.facades.order.converters.populator;

import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import org.springframework.beans.factory.annotation.Required;


/**
 * @author 669567
 *
 */
public class PaymentInfoPopulator implements Populator<PaymentInfoModel, CCPaymentInfoData>
{
	private Converter<AddressModel, AddressData> addressConverter;


	protected Converter<AddressModel, AddressData> getAddressConverter()
	{
		return addressConverter;
	}

	@Required
	public void setAddressConverter(final Converter<AddressModel, AddressData> addressConverter)
	{
		this.addressConverter = addressConverter;
	}



	@Override
	public void populate(final PaymentInfoModel source, final CCPaymentInfoData target)
	{
		target.setId(source.getPk().toString());
		target.setCardNumber(source.getCcNumber());
		target.setSaved(source.isSaved());

		if (source.getBillingAddress() != null)
		{
			target.setBillingAddress(getAddressConverter().convert(source.getBillingAddress()));
		}

	}
}
