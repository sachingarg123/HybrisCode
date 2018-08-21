/**
 *
 */
package de.hybris.wooliesegiftcard.facades;

import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.order.data.OrderHistoryData;
import de.hybris.platform.commercefacades.order.impl.DefaultOrderFacade;
import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.commerceservices.strategies.CheckoutCustomerStrategy;
import de.hybris.platform.converters.Converters;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import de.hybris.wooliesegiftcard.service.impl.WooliesOrderServiceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;


/**
 * @author 676313 This class is used to maintain default order details
 */
public class WooliesDefaultOrderFacade extends DefaultOrderFacade
{

	private static final String ORDER_NOT_FOUND_FOR_USER_AND_BASE_STORE = "Order with guid %s not found for current user in current BaseStore";

	private UserService userService;
	private CustomerAccountService customerAccountService;
	private BaseStoreService baseStoreService;

	private Converter<OrderModel, OrderData> orderConverter;

	private Converter<OrderModel, OrderHistoryData> orderHistoryConverter;
	private CheckoutCustomerStrategy checkoutCustomerStrategy;


	private WooliesOrderServiceImpl wooliesOrderServiceImpl;


	/**
	 * @return the wooliesOrderServiceImpl
	 */
	public WooliesOrderServiceImpl getWooliesOrderServiceImpl()
	{
		return wooliesOrderServiceImpl;
	}

	/**
	 * @param wooliesOrderServiceImpl
	 *           the wooliesOrderServiceImpl to set
	 */
	public void setWooliesOrderServiceImpl(final WooliesOrderServiceImpl wooliesOrderServiceImpl)
	{
		this.wooliesOrderServiceImpl = wooliesOrderServiceImpl;
	}

	/**
	 * This method is used to get order details
	 *
	 * @param code code
	 * @return orderData
	 */
	@Override
	public OrderData getOrderDetailsForCode(final String code)
	{
		final BaseStoreModel baseStoreModel = getBaseStoreService().getCurrentBaseStore();

		OrderModel orderModel = null;
		if (getCheckoutCustomerStrategy().isAnonymousCheckout())
		{
			orderModel = getCustomerAccountService().getOrderDetailsForGUID(code, baseStoreModel);
		}
		else
		{
			try
			{
				orderModel = getCustomerAccountService().getOrderForCode((CustomerModel) getUserService().getCurrentUser(), code,
						baseStoreModel);
			}
			catch (final ModelNotFoundException e)
			{
				throw new UnknownIdentifierException(String.format(ORDER_NOT_FOUND_FOR_USER_AND_BASE_STORE, code));
			}
		}

		if (orderModel == null)
		{
			throw new UnknownIdentifierException(String.format(ORDER_NOT_FOUND_FOR_USER_AND_BASE_STORE, code));
		}
		return getOrderConverter().convert(orderModel);
	}

	/**
	 * This method is used to get order details for guid
	 *
	 * @param guid guid
	 * @return orderData
	 */
	@Override
	public OrderData getOrderDetailsForGUID(final String guid)
	{
		final OrderModel orderModel = getCustomerAccountService().getGuestOrderForGUID(guid,
				getBaseStoreService().getCurrentBaseStore());
		if (orderModel == null)
		{
			throw new UnknownIdentifierException(String.format(ORDER_NOT_FOUND_FOR_USER_AND_BASE_STORE, guid));
		}
		return getOrderConverter().convert(orderModel);
	}

	/**
	 * This method is used to get order history for status
	 *
	 * @param statuses statuses
	 * @return the OrderHistoryData list
	 */
	@Override
	public List<OrderHistoryData> getOrderHistoryForStatuses(final OrderStatus... statuses)
	{
		final CustomerModel currentCustomer = (CustomerModel) getUserService().getCurrentUser();
		final BaseStoreModel currentBaseStore = getBaseStoreService().getCurrentBaseStore();
		final List<OrderModel> orderList = getCustomerAccountService().getOrderList(currentCustomer, currentBaseStore, statuses);
		return Converters.convertAll(orderList, getOrderHistoryConverter());
	}

	/**
	 * This method is used to get paged order histor the the status
	 *
	 * @param pageableData pageableData
	 * @param statuses statuses
	 * @return the OrderHistoryData
	 */
	@Override
	public SearchPageData<OrderHistoryData> getPagedOrderHistoryForStatuses(final PageableData pageableData,
			final OrderStatus... statuses)
	{
		final CustomerModel currentCustomer = (CustomerModel) getUserService().getCurrentUser();
		final BaseStoreModel currentBaseStore = getBaseStoreService().getCurrentBaseStore();
		final SearchPageData<OrderModel> orderResults = getCustomerAccountService().getOrderList(currentCustomer, currentBaseStore,
				statuses, pageableData);

		return convertPageData(orderResults, getOrderHistoryConverter());
	}

	/**
	 * This method is used to get details for code without user for the given code
	 *
	 * @param code code
	 * @return the orderData
	 */
	@Override
	public OrderData getOrderDetailsForCodeWithoutUser(final String code)
	{
		final BaseStoreModel baseStoreModel = getBaseStoreService().getCurrentBaseStore();
		final OrderModel orderModel = getCustomerAccountService().getOrderForCode(code, baseStoreModel);
		if (orderModel == null)
		{
			throw new UnknownIdentifierException("Order with orderGUID " + code + " not found in current BaseStore");
		}
		return getOrderConverter().convert(orderModel);
	}

	/**
	 * This method is used to convert page data
	 *
	 * @param source source
	 * @param converter converter
	 * @return sarchPageData
	 */
	@Override
	protected <S, T> SearchPageData<T> convertPageData(final SearchPageData<S> source, final Converter<S, T> converter)
	{
		final SearchPageData<T> result = new SearchPageData<T>();
		result.setPagination(source.getPagination());
		result.setSorts(source.getSorts());
		result.setResults(Converters.convertAll(source.getResults(), converter));
		return result;
	}

	/**
	 *
	 * @return userService
	 */
	@Override
	protected UserService getUserService()
	{
		return userService;
	}

	/**
	 * To set user service
	 *
	 * @param userService
	 */
	@Override
	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	/**
	 * To get customer account service
	 *
	 * @return customerAccountService
	 */
	@Override
	protected <T extends CustomerAccountService> T getCustomerAccountService()
	{
		return (T) customerAccountService;
	}

	/**
	 * To set customer account service
	 *
	 * @param customerAccountService
	 */
	@Override
	@Required
	public void setCustomerAccountService(final CustomerAccountService customerAccountService)
	{
		this.customerAccountService = customerAccountService;
	}

	/**
	 * To get base store service
	 *
	 * @return baseStoreService
	 */
	@Override
	protected BaseStoreService getBaseStoreService()
	{
		return baseStoreService;
	}

	/**
	 * To set base store service
	 *
	 * @param service
	 */
	@Override
	@Required
	public void setBaseStoreService(final BaseStoreService service)
	{
		this.baseStoreService = service;
	}

	/**
	 * order converter
	 *
	 * @return orderConverter
	 */
	@Override
	protected Converter<OrderModel, OrderData> getOrderConverter()
	{
		return orderConverter;
	}

	/**
	 * To set order converter
	 *
	 * @param orderConverter
	 */
	@Override
	@Required
	public void setOrderConverter(final Converter<OrderModel, OrderData> orderConverter)
	{
		this.orderConverter = orderConverter;
	}

	/**
	 * The history converter
	 *
	 * @return orderHistoryConverter
	 */
	@Override
	protected Converter<OrderModel, OrderHistoryData> getOrderHistoryConverter()
	{
		return orderHistoryConverter;
	}

	/**
	 * To set history coverter
	 *
	 * @param orderHistoryConverter
	 */
	@Override
	@Required
	public void setOrderHistoryConverter(final Converter<OrderModel, OrderHistoryData> orderHistoryConverter)
	{
		this.orderHistoryConverter = orderHistoryConverter;
	}

	/**
	 * To get checkout customer stragery
	 *
	 * @return checkoutCustomerStrategy
	 */
	@Override
	protected CheckoutCustomerStrategy getCheckoutCustomerStrategy()
	{
		return checkoutCustomerStrategy;
	}

	/**
	 * To set checkout customer stragery
	 *
	 * @param checkoutCustomerStrategy
	 */
	@Override
	@Required
	public void setCheckoutCustomerStrategy(final CheckoutCustomerStrategy checkoutCustomerStrategy)
	{
		this.checkoutCustomerStrategy = checkoutCustomerStrategy;
	}

	/**
	 * This api used for get the orderdetails
	 *
	 * @param orderToken
	 *           orderToken
	 * @return OrderData
	 */
	public OrderData getOrderDetailsWithDecryptKey(final String orderToken)
	{
		OrderModel orderModel1 = new OrderModel();

		final List<OrderModel> orderModel = wooliesOrderServiceImpl.getOrderDetailsWithDecryptKey(orderToken);
		for (final OrderModel orderModel2 : orderModel)
		{
			orderModel1 = orderModel2;
		}

		return getOrderConverter().convert(orderModel1);
	}
}
