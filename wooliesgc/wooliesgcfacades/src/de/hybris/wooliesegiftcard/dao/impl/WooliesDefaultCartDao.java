/**
 *
 */
package de.hybris.wooliesegiftcard.dao.impl;

import de.hybris.model.PersonalisationMediaModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.deliveryzone.model.ZoneDeliveryModeModel;
import de.hybris.platform.deliveryzone.model.ZoneDeliveryModeValueModel;
import de.hybris.platform.jalo.link.Link;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.wooliesegiftcard.core.model.WWHtmlLookUpModel;
import de.hybris.wooliesegiftcard.dao.WooliesCartDao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;


/**
 * @author 668982
 *
 */
public class WooliesDefaultCartDao implements WooliesCartDao
{
	private static final Logger LOG = Logger.getLogger(WooliesDefaultCartDao.class);
	private FlexibleSearchService flexibleSearchService;
	private ConfigurationService configurationService;
	private CommonI18NService commonI18NService;
	private UserService userService;
	private static final String FROM = " FROM {";
	private static final String DELIVERYCOUNTRY = "deliveryCountry";
	private static final String VAL = " ON {val:";
	private static final String ZDM = " AND {zdm:";
	private static final String Z2C = " AND {z2c:";



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
	 * @return the commonI18NService
	 */
	public CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	/**
	 * @param commonI18NService
	 *           the commonI18NService to set
	 */
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	/**
	 * @return the flexibleSearchService
	 */

	public FlexibleSearchService getFlexibleSearchService()
	{
		return flexibleSearchService;
	}

	/**
	 * @return the configurationService
	 */
	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	/**
	 * @param configurationService
	 *           the configurationService to set
	 */
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	/**
	 * @param flexibleSearchService
	 *           the flexibleSearchService to set
	 */

	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}

	/**
	 * This method is used to find the delivery modes for the given order and total price
	 *
	 * @param abstractOrder
	 *           Abstract OrderModel
	 * @param totalPrice
	 *           Long
	 * @return zoneDeliveryModeValueModel Collection<ZoneDeliveryModeValueModel>
	 */
	@Override
	public Collection<ZoneDeliveryModeValueModel> findDeliveryModes(final AbstractOrderModel abstractOrder, final Long totalPrice)
	{
		/*
		 * SELECT DISTINCT * FROM {ZoneDeliveryModeValue AS val JOIN ZoneDeliveryMode AS zdm ON
		 * {val:deliveryMode}={zdm:pk} JOIN ZoneCountryRelation AS z2c ON {val:zone}={z2c:source} JOIN
		 * BaseStore2DeliveryModeRel AS s2d ON {val:deliveryMode}={s2d:target} } WHERE {val:currency}=8796125855777 AND
		 * {s2d:source}=8796125824989 AND {zdm:net}=false AND {zdm:active}=true AND ({val:minimum} = 0 or 5 <=
		 * {val:minimum}) and {zdm:deliveryType}=({{select {pk} from {deliverymodetype} where {code}='Plastic'}})
		 */
		final StringBuilder query = new StringBuilder("SELECT DISTINCT").append(" {val:").append(ItemModel.PK).append("}");

		query.append(" FROM { ").append(ZoneDeliveryModeValueModel._TYPECODE).append(" AS val");
		query.append(" JOIN ").append(ZoneDeliveryModeModel._TYPECODE).append(" AS zdm");
		query.append(VAL).append(ZoneDeliveryModeValueModel.DELIVERYMODE).append("}={zdm:").append(ItemModel.PK).append('}');
		query.append(" JOIN ZoneCountryRelation ").append(" AS z2c");
		query.append(VAL).append(ZoneDeliveryModeValueModel.ZONE).append("}={z2c:").append(Link.SOURCE).append('}');
		query.append(" JOIN BaseStore2DeliveryModeRel ").append(" AS s2d");
		query.append(VAL).append(ZoneDeliveryModeValueModel.DELIVERYMODE).append("}={s2d:").append(Link.TARGET).append('}');
		query.append(" } WHERE {val:").append(ZoneDeliveryModeValueModel.CURRENCY).append("}=?currency");
		query.append(" AND {s2d:").append(Link.SOURCE).append("}=?store");
		query.append(ZDM).append(ZoneDeliveryModeModel.NET).append("}=?net");
		query.append(ZDM).append(ZoneDeliveryModeModel.ACTIVE).append("}=?active");
		query.append(ZDM).append(ZoneDeliveryModeModel.CODE).append("} LIKE 'PGC%'");


		if (totalPrice != null)
		{
			query.append(" AND (?totalPrice <= {val:").append(ZoneDeliveryModeValueModel.MINIMUM).append("} or {val:");
			query.append(ZoneDeliveryModeValueModel.MINIMUM).append("}= 0)");
		}
		final Map<String, Object> params = new HashMap<>();
		boolean hasDeliveryAddress = false;
		if (abstractOrder.getDeliveryAddress() != null)
		{
			hasDeliveryAddress = true;
			query.append(Z2C).append(Link.TARGET).append("}=?deliveryCountry");
			params.put(DELIVERYCOUNTRY, abstractOrder.getDeliveryAddress().getCountry());
		}
		else if (CollectionUtils.isNotEmpty(abstractOrder.getUser().getAddresses()))
		{
			final Collection<AddressModel> addresses = abstractOrder.getUser().getAddresses();
			for (final AddressModel addressModel : addresses)
			{
				if (addressModel.getShippingAddress().booleanValue())
				{
					hasDeliveryAddress = true;
					query.append(Z2C).append(Link.TARGET).append("}=?deliveryCountry");
					params.put(DELIVERYCOUNTRY, addressModel.getCountry());
					break;
				}
			}
		}
		hasDeliveryMode(query, params, hasDeliveryAddress);
		params.put("currency", abstractOrder.getCurrency());
		params.put("net", abstractOrder.getNet());
		params.put("active", Boolean.TRUE);
		params.put("store", abstractOrder.getStore());
		if (totalPrice != null)
		{
			params.put("totalPrice", totalPrice);
		}
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		fQuery.addQueryParameters(params);

		fQuery.setResultClassList(Collections.singletonList(DeliveryModeModel.class));
		LOG.info("findDeliveryModes" + fQuery);
		final SearchResult<ZoneDeliveryModeValueModel> searchResult = flexibleSearchService.search(fQuery);
		if (searchResult != null)
		{
			return searchResult.getResult();
		}
		else
		{
			return Collections.emptyList();
		}
	}

	/**
	 * @param query
	 *           StringBuilder
	 * @param params
	 *           StringBuilder
	 * @param hasDeliveryAddress
	 *           Map<String, Object>
	 */
	private void hasDeliveryMode(final StringBuilder query, final Map<String, Object> params, final boolean hasDeliveryAddress)
	{
		if (!hasDeliveryAddress)
		{
			query.append(Z2C).append(Link.TARGET).append("} in (?deliveryCountry)");
			final List<CountryModel> countries = new ArrayList<>();
			final String deliveryCountries = getConfigurationService().getConfiguration().getString("default.deliveryCountries",
					"AU,NZ");
			for (final String countryIsoCode : deliveryCountries.split(","))
			{
				final CountryModel country = getCommonI18NService().getCountry(countryIsoCode);
				if (country != null)
				{
					countries.add(country);
				}
			}
			params.put(DELIVERYCOUNTRY, countries);
		}
	}

	/**
	 *
	 * This method is used to find delivery mode for eGift product based on the order
	 *
	 * @param abstractOrder
	 *           AbstractOrderModel
	 * @return zoneDeliveryModeValueModel
	 */
	@Override
	public Collection<ZoneDeliveryModeValueModel> findDeliveryModeForeGiftProduct(final AbstractOrderModel abstractOrder)
	{

		final StringBuilder query = new StringBuilder("SELECT DISTINCT {val:").append(ItemModel.PK).append("}");
		query.append(" FROM { ").append(ZoneDeliveryModeValueModel._TYPECODE).append(" AS val");
		query.append(" JOIN ").append(ZoneDeliveryModeModel._TYPECODE).append(" AS zdm");
		query.append(VAL).append(ZoneDeliveryModeValueModel.DELIVERYMODE).append("}={zdm:").append(ItemModel.PK).append('}');

		query.append(" } WHERE {val:").append(ZoneDeliveryModeValueModel.CURRENCY).append("}=?currency");
		query.append(ZDM).append(ZoneDeliveryModeModel.NET).append("}=?net");
		query.append(ZDM).append(ZoneDeliveryModeModel.ACTIVE).append("}=?active");
		query.append(ZDM).append(ZoneDeliveryModeModel.CODE).append("} LIKE 'EGC%'");



		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		final Map<String, Object> params = new HashMap<>();
		fQuery.setResultClassList(Collections.singletonList(ZoneDeliveryModeValueModel.class));
		params.put("currency", abstractOrder.getCurrency());
		params.put("net", abstractOrder.getNet());
		params.put("active", Boolean.TRUE);
		fQuery.addQueryParameters(params);
		LOG.info("findDeliveryModeForeGiftProduct" + fQuery);
		final SearchResult<ZoneDeliveryModeValueModel> searchResult = flexibleSearchService.search(fQuery);
		if (searchResult != null)
		{
			return searchResult.getResult();
		}
		else
		{
			return Collections.emptyList();
		}
	}


	/**
	 * This method is used to get cart details based on uid
	 *
	 * @param uid
	 *           customer uid
	 * @param baseStore
	 *           BaseStoreModel
	 * @return list of cartModels
	 */
	@Override
	public List<CartModel> getCartByUID(final String uid, final BaseStoreModel baseStore)
	{
		final StringBuilder query = new StringBuilder("SELECT DISTINCT {c:").append(ItemModel.PK).append("}");
		query.append(FROM).append(CartModel._TYPECODE).append(" as c join ").append(CustomerModel._TYPECODE);
		query.append(" as u on {u:").append(CustomerModel.PK).append("} = {c:").append(CartModel.USER).append("} and {u:");
		query.append(CustomerModel.UID).append("}=?uid and {c:").append(CartModel.STORE).append("}=?baseStore}");
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		final Map<String, Object> params = new HashMap<>();
		fQuery.setResultClassList(Collections.singletonList(CartModel.class));
		params.put("uid", uid);
		params.put("baseStore", baseStore.getPk());
		fQuery.addQueryParameters(params);
		LOG.info("getCartByUID" + fQuery);
		final SearchResult<CartModel> searchResult = flexibleSearchService.search(fQuery);
		if (searchResult != null)
		{
			return searchResult.getResult();
		}
		else
		{
			return Collections.emptyList();
		}
	}

	/**
	 * This method is used to get image details based on imageid
	 *
	 * @param imageID
	 *           String
	 * @return list of media Models
	 */
	@Override
	public List<PersonalisationMediaModel> getImageByID(final String imageID)
	{
		final String query1 = "Select {pk} from {PersonalisationMedia} Where {code}=?imageId and {user}=?user";
		/**
		 * select distinct {val:pk} from {PersonalisationMediaModel as val join MediaModel as media on
		 * {media:PK}={val.image} join User as user on {user:pk}={val:user} and {media:code}=?imageId and {user:pk}=?user}
		 */
		final UserModel user = getUserService().getCurrentUser();
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query1);
		final Map<String, Object> params = new HashMap<>();
		params.put("imageId", imageID);
		params.put("user", user);
		fQuery.addQueryParameters(params);
		LOG.info("getImageByID" + fQuery);
		final SearchResult<PersonalisationMediaModel> searchResult = flexibleSearchService.search(fQuery);
		return searchResult.getResult();
	}

	/**
	 * This method is used to get cart details based on guid
	 *
	 * @param guid
	 *           String
	 * @return list of cartModels
	 */
	@Override
	public List<CartModel> getCartByGUID(final String guid)
	{
		final StringBuilder query = new StringBuilder("SELECT DISTINCT {").append(ItemModel.PK).append("}");
		query.append(FROM).append(CartModel._TYPECODE).append("} where {").append(CartModel.GUID).append("}=?guid");
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		final Map<String, Object> params = new HashMap();
		fQuery.setResultClassList(Collections.singletonList(CartModel.class));
		params.put("guid", guid);
		fQuery.addQueryParameters(params);
		LOG.info("getCartByGUID" + fQuery);
		final SearchResult<CartModel> searchResult = flexibleSearchService.search(fQuery);
		return searchResult.getResult();
	}

	/**
	 *
	 * @param paymentOption
	 *           paymentOption
	 * @return WWHtmlLookUpModel
	 */
	public WWHtmlLookUpModel getPaymentName(final String paymentOption)
	{
		final String query1 = "select {pk} from {WWHtmlLookUp} where {id}=({{select {pk} from {PaymentOptions} where {code} IN (?paymentOption)}})";
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query1);
		final Map<String, Object> params = new HashMap<>();
		params.put("paymentOption", Arrays.asList(paymentOption));

		fQuery.addQueryParameters(params);
		LOG.info("getPaymentName based on paymentOption " + fQuery);
		final SearchResult<WWHtmlLookUpModel> searchResult = flexibleSearchService.search(fQuery);
		return searchResult.getResult() != null ? searchResult.getResult().get(0) : null;
	}

}
