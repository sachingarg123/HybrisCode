/**
 *
 */
package de.hybris.wooliesegiftcard.facades.impl;

import de.hybris.platform.b2b.model.B2BOrderThresholdPermissionModel;
import de.hybris.platform.b2b.model.B2BPermissionModel;
import de.hybris.platform.commercefacades.order.impl.DefaultCheckoutFacade;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.wooliesegiftcard.core.enums.ActiveStatus;
import de.hybris.wooliesegiftcard.core.enums.BulkOrderStatus;
import de.hybris.wooliesegiftcard.core.enums.UserDataType;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BCustomerModel;
import de.hybris.wooliesegiftcard.core.model.CorporateB2BUnitModel;
import de.hybris.wooliesegiftcard.core.model.WWBulkOrderDataModel;
import de.hybris.wooliesegiftcard.core.model.WWBulkOrderItemsDataModel;
import de.hybris.wooliesegiftcard.exceptions.WooliesFacadeLayerException;
import de.hybris.wooliesegiftcard.facades.WooliesBulkOrderFacade;
import de.hybris.wooliesegiftcard.facades.constants.WooliesgcFacadesConstants;
import de.hybris.wooliesegiftcard.facades.dto.BulkOrderRequestDTO;
import de.hybris.wooliesegiftcard.facades.dto.BulkOrderRequestItem;
import de.hybris.wooliesegiftcard.facades.dto.BulkOrderResponseDTO;
import de.hybris.wooliesegiftcard.facades.dto.BulkOrderResponseError;
import de.hybris.wooliesgiftcard.core.bulkorder.service.WooliesBulkOrderService;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author 669567 This class is used to maintain bulk order
 */
public class WooliesBulkOrderFacadeImpl extends DefaultCheckoutFacade implements WooliesBulkOrderFacade
{

	@Autowired
	private FlexibleSearchService flexibleSearchService;

	private ModelService modelService;

	@Autowired
	private KeyGenerator bulkOrderKeyGenerator;
	private UserService userService;
	private WooliesBulkOrderService wooliesBulkOrderService;

	/**
	 * @return the userService
	 */
	@Override
	public UserService getUserService()
	{
		return userService;
	}

	/**
	 * @param userService
	 *           the userService to set
	 */
	@Override
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	/**
	 * @return the modelService
	 */
	@Override
	public ModelService getModelService()
	{
		return modelService;
	}

	/**
	 * @param modelService
	 *           the modelService to set
	 */
	@Override
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	/**
	 * @return the modelService
	 */

	public KeyGenerator getBulkOrderKeyGenerator()
	{
		return bulkOrderKeyGenerator;
	}



	/**
	 * @param bulkOrderKeyGenerator
	 *           the modelService to set
	 */

	public void setBulkOrderKeyGenerator(final KeyGenerator bulkOrderKeyGenerator)
	{
		this.bulkOrderKeyGenerator = bulkOrderKeyGenerator;
	}

	/**
	 * @return the flexibleSearchService
	 */

	public FlexibleSearchService getFlexibleSearchService()
	{
		return flexibleSearchService;
	}

	/**
	 * @param flexibleSearchService
	 *           the modelService to set
	 */

	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}


	/**
	 * @return the wooliesBulkOrderService
	 */
	public WooliesBulkOrderService getWooliesBulkOrderService()
	{
		return wooliesBulkOrderService;
	}

	/**
	 * @param wooliesBulkOrderService
	 *           the wooliesBulkOrderService to set
	 */
	public void setWooliesBulkOrderService(final WooliesBulkOrderService wooliesBulkOrderService)
	{
		this.wooliesBulkOrderService = wooliesBulkOrderService;
	}

	/**
	 * This method is used to save the bulk order and returns order object
	 *
	 * @param bulkData
	 * @return bulkOrderResponseDTO
	 */
	@Override
	public BulkOrderResponseDTO saveBulkOrder(final BulkOrderRequestDTO bulkData)
	{
		final String randomNumber = bulkOrderKeyGenerator.generate().toString();
		double totalPrice = 0d;
		final BulkOrderResponseDTO bulkOrderResponseDTO = new BulkOrderResponseDTO();
		final List<BulkOrderResponseError> errorList = new LinkedList<>();
		final List<WWBulkOrderItemsDataModel> bulkOrderItemList = new LinkedList();
		final List<CustomerModel> customerModel = wooliesBulkOrderService.getB2BCustomerInfo(bulkData.getUid());
		if (null != customerModel && !customerModel.isEmpty() && customerModel.get(0).getCustomerType() == UserDataType.B2B)
		{
			wooliesBulkOrderService.updateOldBulkOrderStatus(bulkData.getUid());
			final WWBulkOrderDataModel bulkOrderModel = modelService.create(WWBulkOrderDataModel.class);
			for (final BulkOrderRequestItem bulkOrderItems : bulkData.getItems())
			{
				final WWBulkOrderItemsDataModel bulkOrderItemsModel = modelService.create(WWBulkOrderItemsDataModel.class);
				bulkOrderItemsModel.setSkuCode(bulkOrderItems.getSkuCode());
				final Double unitPrice = (null == bulkOrderItems.getUnitPrice()) ? new Double(0.0d) : bulkOrderItems.getUnitPrice();
				bulkOrderItemsModel.setUnitPrice(unitPrice);
				final double price = (null == bulkOrderItems.getUnitPrice()) ? 0 : bulkOrderItems.getUnitPrice().doubleValue();
				totalPrice = totalPrice + price;
				bulkOrderItemsModel.setToName(bulkOrderItems.getToName());
				bulkOrderItemsModel.setFromName(bulkOrderItems.getFromName());
				bulkOrderItemsModel.setToEmail(bulkOrderItems.getToEmail());
				bulkOrderItemsModel.setFromEmail(bulkOrderItems.getFromEmail());
				bulkOrderItemsModel.setMessage(bulkOrderItems.getMessage());
				bulkOrderItemsModel.setRowNumber(bulkOrderItems.getRowNumber());
				bulkOrderItemsModel.setReferenceNumber(randomNumber);
				bulkOrderItemList.add(bulkOrderItemsModel);
			}
			bulkOrderModel.setReferenceNumber(randomNumber);
			bulkOrderModel.setUid(bulkData.getUid());
			bulkOrderModel.setBulkOrderStatus(BulkOrderStatus.IN_PROCESS);
			bulkOrderModel.setTotalPrice(new Double(totalPrice));
			modelService.saveAll(bulkOrderItemList);
			modelService.save(bulkOrderModel);
			bulkOrderResponseDTO.setReferenceNumber(randomNumber);

		}
		else
		{
			final BulkOrderResponseError error = new BulkOrderResponseError();
			error.setErrorMessage("Customer doesn't exist");
			error.setErrorCode(WooliesgcFacadesConstants.BULK_ORDER_VALIDATION_ERROR);
			error.setValue(bulkData.getUid());
			error.setProperty("uid");
			errorList.add(error);
			bulkOrderResponseDTO.setErrors(errorList);
		}
		return bulkOrderResponseDTO;
	}

	/**
	 * This method is used to validates the bulk order details and return the valid bulk order
	 *
	 * @param referenceNumber
	 * @return BulkOrderResponseDTO
	 * @throws WooliesFacadeLayerException
	 */
	@Override
	public BulkOrderResponseDTO validateBulkOrder(final String referenceNumber) throws WooliesFacadeLayerException
	{
		String userId = null;
		double totalPrice = 0d;
		final List<WWBulkOrderDataModel> bulkOrderData = wooliesBulkOrderService.getBulkOrder(referenceNumber.trim());
		final List<WWBulkOrderItemsDataModel> bulkOrderItemData = wooliesBulkOrderService.getBulkItemsOrder(referenceNumber.trim());
		if (null != bulkOrderData && !bulkOrderData.isEmpty())
		{
			userId = bulkOrderData.get(0).getUid();
			totalPrice = bulkOrderData.get(0).getTotalPrice().doubleValue();
		}
		else
		{
			throw new WooliesFacadeLayerException("No Order available in Hybris");

		}
		BulkOrderResponseDTO bulkOrderResponseDTO = new BulkOrderResponseDTO();
		if (null != bulkOrderItemData && !bulkOrderItemData.isEmpty())
		{
			bulkOrderResponseDTO.setReferenceNumber(referenceNumber);
			bulkOrderResponseDTO = validateOrders(bulkOrderItemData, bulkOrderResponseDTO, userId, totalPrice);
			final List<BulkOrderResponseError> bulkOrderResponseError = bulkOrderResponseDTO.getErrors();
			if (null != bulkOrderResponseError && !bulkOrderResponseError.isEmpty())
			{
				for (final WWBulkOrderDataModel wwBulkOrderModel : bulkOrderData)
				{
					wwBulkOrderModel.setBulkOrderStatus(BulkOrderStatus.VALIDATE_FAILED);
					modelService.save(wwBulkOrderModel);
				}

			}
			else
			{
				for (final WWBulkOrderDataModel wwBulkOrderModel : bulkOrderData)
				{
					wwBulkOrderModel.setBulkOrderStatus(BulkOrderStatus.VALIDATE_SUCCESS);
					modelService.save(wwBulkOrderModel);
				}
				bulkOrderResponseDTO.setMessage("you are placing " + bulkOrderItemData.size() + " gift cards in your order");
			}
		}
		else
		{
			throw new WooliesFacadeLayerException("No Order available in Hybris");
		}


		return bulkOrderResponseDTO;
	}



	/**
	 * This method is used to validate orders
	 *
	 * @param bulkOrderModelItem
	 * @param bulkOrderResponseDTO
	 * @param uid
	 * @param totalPrice
	 * @return BulkOrderResponseDTO
	 */
	private BulkOrderResponseDTO validateOrders(final List<WWBulkOrderItemsDataModel> bulkOrderModelItem,
			final BulkOrderResponseDTO bulkOrderResponseDTO, final String uid, final double totalPrice)
	{
		final Pattern pattern = Pattern.compile("[a-zA-Z0-9\\s]*");
		final String EMAIL_REGEX = "^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$";
		final Pattern emailPattern = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);
		final List<BulkOrderResponseError> errorList = new LinkedList<>();
		for (final WWBulkOrderItemsDataModel wwBulkOrderItem : bulkOrderModelItem)
		{
			//check null parameters
			validateSku(errorList, wwBulkOrderItem);
			validateUnitPrice(errorList, wwBulkOrderItem);
			validateRowNumber(errorList, wwBulkOrderItem);
			validateMsg(pattern, errorList, wwBulkOrderItem);
			validateFromEmail(emailPattern, errorList, wwBulkOrderItem);
			validateToEmail(emailPattern, errorList, wwBulkOrderItem);
			List<ProductModel> productModelInfo = null;
			List<PriceRowModel> priceRowModel = null;
			if (null != wwBulkOrderItem.getSkuCode())
			{
				productModelInfo = wooliesBulkOrderService.getProducts(wwBulkOrderItem.getSkuCode());
				priceRowModel = wooliesBulkOrderService.getProductsPrice(wwBulkOrderItem.getSkuCode());
			}
			if (null != productModelInfo && CollectionUtils.isNotEmpty(productModelInfo))
			{
				validateProducts(errorList, wwBulkOrderItem, productModelInfo);
			}
			else
			{
				validateProductError(errorList, wwBulkOrderItem);
			}

			verifyProductPrice(errorList, wwBulkOrderItem);

			if (null != priceRowModel && CollectionUtils.isNotEmpty(priceRowModel))
			{
				final Double price = new Double(priceRowModel.get(0).getPrice().doubleValue());
				final Double dObj2 = new Double("0.0d");
				final int i2 = price.compareTo(dObj2);
				if (i2 != 0)
				{
					validateProductPrice(errorList, wwBulkOrderItem, priceRowModel);
				}

			}
		}
		final List<CustomerModel> customerModel = wooliesBulkOrderService.getB2BCustomerInfo(uid);
		validateCustomer(uid, totalPrice, errorList, customerModel);
		bulkOrderResponseDTO.setErrors(errorList);
		return bulkOrderResponseDTO;

	}

	/**
	 * @param uid
	 * @param totalPrice
	 * @param errorList
	 * @param customerModel
	 */
	private void validateCustomer(final String uid, final double totalPrice, final List<BulkOrderResponseError> errorList,
			final List<CustomerModel> customerModel)
	{
		if (CollectionUtils.isNotEmpty(customerModel) && customerModel.get(0).getCustomerType() == UserDataType.B2B)
		{
			final CorporateB2BCustomerModel corporateb2bCustomerModel = (CorporateB2BCustomerModel) customerModel.get(0);
			Double thresholdValue = null;
			final CorporateB2BUnitModel corporateb2BUnit = (CorporateB2BUnitModel) corporateb2bCustomerModel.getDefaultB2BUnit();
			final Set<B2BPermissionModel> b2bPermissionModel = corporateb2BUnit.getPermissions();
			if (!b2bPermissionModel.isEmpty())
			{
				for (final B2BPermissionModel b2bPermission : b2bPermissionModel)
				{
					thresholdValue = getThreshHoldValue(corporateb2bCustomerModel, thresholdValue, b2bPermission);
				}
			}
			final Set<PrincipalGroupModel> allGroups = corporateb2bCustomerModel.getAllGroups();
			if (!allGroups.contains(userService.getUserGroupForUID(WooliesgcFacadesConstants.B2BADMINGROUP)))
			{
				final Double totalPriceD = new Double(totalPrice);
				if (thresholdValue != null && totalPriceD.compareTo(thresholdValue) > 0)
				{

					final BulkOrderResponseError error = new BulkOrderResponseError();
					error.setErrorMessage("Current Order limit of $" + thresholdValue + "exceeded");
					error.setErrorCode("ValidationError");
					error.setValue(totalPriceD.toString());
					error.setProperty("unitPrice");
					errorList.add(error);

				}
			}
		}
		else
		{
			final BulkOrderResponseError error = new BulkOrderResponseError();
			error.setErrorMessage("Customer doesn't exist");
			error.setErrorCode(WooliesgcFacadesConstants.BULK_ORDER_VALIDATION_ERROR);
			error.setValue(uid);
			error.setProperty("uid");
			errorList.add(error);
		}
	}

	/**
	 * @param errorList
	 * @param wwBulkOrderItem
	 */
	private void validateProductError(final List<BulkOrderResponseError> errorList,
			final WWBulkOrderItemsDataModel wwBulkOrderItem)
	{
		final BulkOrderResponseError error = new BulkOrderResponseError();
		error.setErrorMessage("Product is not available in System");
		error.setErrorCode(WooliesgcFacadesConstants.BULK_ORDER_VALIDATION_ERROR);
		error.setValue(wwBulkOrderItem.getSkuCode());
		error.setProperty(WooliesgcFacadesConstants.BULK_ORDER_SKU_CODE);
		error.setRowNumber(wwBulkOrderItem.getRowNumber());
		errorList.add(error);
	}

	/**
	 * @param errorList
	 * @param wwBulkOrderItem
	 * @param productModelInfo
	 */
	private void validateProducts(final List<BulkOrderResponseError> errorList, final WWBulkOrderItemsDataModel wwBulkOrderItem,
			final List<ProductModel> productModelInfo)
	{
		if (!productModelInfo.get(0).getIsEGiftCard().booleanValue())
		{
			final BulkOrderResponseError error = new BulkOrderResponseError();
			error.setErrorMessage("Product is not eGift");
			error.setErrorCode(WooliesgcFacadesConstants.BULK_ORDER_VALIDATION_ERROR);
			error.setValue(wwBulkOrderItem.getSkuCode());
			error.setProperty(WooliesgcFacadesConstants.BULK_ORDER_SKU_CODE);
			error.setRowNumber(wwBulkOrderItem.getRowNumber());
			errorList.add(error);
		}
		if (productModelInfo.get(0).getActiveStatus() != ActiveStatus.A)
		{
			final BulkOrderResponseError error = new BulkOrderResponseError();
			error.setErrorMessage("Product is not active");
			error.setErrorCode(WooliesgcFacadesConstants.BULK_ORDER_VALIDATION_ERROR);
			error.setValue(wwBulkOrderItem.getSkuCode());
			error.setProperty(WooliesgcFacadesConstants.BULK_ORDER_SKU_CODE);
			error.setRowNumber(wwBulkOrderItem.getRowNumber());
			errorList.add(error);
		}
	}

	/**
	 * @param pattern
	 * @param errorList
	 * @param wwBulkOrderItem
	 */
	private void validateMsg(final Pattern pattern, final List<BulkOrderResponseError> errorList,
			final WWBulkOrderItemsDataModel wwBulkOrderItem)
	{
		if (null != wwBulkOrderItem.getMessage() && wwBulkOrderItem.getMessage().trim().length() > 250)
		{
			//check msg values
			final BulkOrderResponseError error = new BulkOrderResponseError();
			final Matcher matcher = pattern.matcher(wwBulkOrderItem.getMessage().trim());
			if (!matcher.matches())
			{
				error.setErrorMessage("Message Length is more than 250 and contains special characters");
			}
			else
			{
				error.setErrorMessage("Message Length is more than 250");
			}

			error.setErrorCode(WooliesgcFacadesConstants.BULK_ORDER_VALIDATION_ERROR);
			error.setValue(wwBulkOrderItem.getMessage());
			error.setProperty(WooliesgcFacadesConstants.BULK_ORDER_MESSAGE);
			error.setRowNumber(wwBulkOrderItem.getRowNumber());
			errorList.add(error);

		}
		if (null != wwBulkOrderItem.getMessage() && wwBulkOrderItem.getMessage().trim().length() <= 250)
		{

			final Matcher matcher = pattern.matcher(wwBulkOrderItem.getMessage().trim());
			if (!matcher.matches())
			{
				final BulkOrderResponseError error = new BulkOrderResponseError();
				error.setErrorMessage("Message contains special characters");
				error.setErrorCode(WooliesgcFacadesConstants.BULK_ORDER_VALIDATION_ERROR);
				error.setValue(wwBulkOrderItem.getMessage());
				error.setProperty(WooliesgcFacadesConstants.BULK_ORDER_MESSAGE);
				error.setRowNumber(wwBulkOrderItem.getRowNumber());
				errorList.add(error);
			}
		}
	}

	/**
	 * @param errorList
	 * @param wwBulkOrderItem
	 */
	private void verifyProductPrice(final List<BulkOrderResponseError> errorList, final WWBulkOrderItemsDataModel wwBulkOrderItem)
	{
		if (null != wwBulkOrderItem.getUnitPrice())
		{
			final double price1 = 5d;
			final double price2 = 500d;

			if (Double.compare(wwBulkOrderItem.getUnitPrice().doubleValue(), price1) < 0)
			{
				final BulkOrderResponseError error = new BulkOrderResponseError();
				error.setErrorMessage("Product Price is less than 5$");
				error.setErrorCode(WooliesgcFacadesConstants.BULK_ORDER_VALIDATION_ERROR);
				error.setValue(wwBulkOrderItem.getUnitPrice().toString());
				error.setProperty(WooliesgcFacadesConstants.BULK_ORDER_UNIT_PRICE);
				error.setRowNumber(wwBulkOrderItem.getRowNumber());
				errorList.add(error);
			}

			if (Double.compare(wwBulkOrderItem.getUnitPrice().doubleValue(), price2) > 0)
			{
				final BulkOrderResponseError error = new BulkOrderResponseError();
				error.setErrorMessage("Product Price is greater than 500$");
				error.setErrorCode(WooliesgcFacadesConstants.BULK_ORDER_VALIDATION_ERROR);
				error.setValue(wwBulkOrderItem.getUnitPrice().toString());
				error.setProperty(WooliesgcFacadesConstants.BULK_ORDER_UNIT_PRICE);
				error.setRowNumber(wwBulkOrderItem.getRowNumber());
				errorList.add(error);
			}
		}
	}

	/**
	 * @param emailPattern
	 * @param errorList
	 * @param wwBulkOrderItem
	 */
	private void validateFromEmail(final Pattern emailPattern, final List<BulkOrderResponseError> errorList,
			final WWBulkOrderItemsDataModel wwBulkOrderItem)
	{
		if (null != wwBulkOrderItem.getFromEmail())
		{
			final Matcher fromEmailMatcher = emailPattern.matcher(wwBulkOrderItem.getFromEmail());
			if (!fromEmailMatcher.matches())
			{
				final BulkOrderResponseError error = new BulkOrderResponseError();
				error.setErrorMessage(WooliesgcFacadesConstants.BULK_ORDER_EMAIL_NOT_VALID);
				error.setErrorCode(WooliesgcFacadesConstants.BULK_ORDER_VALIDATION_ERROR);
				error.setValue(wwBulkOrderItem.getFromEmail());
				error.setProperty("fromEmail");
				error.setRowNumber(wwBulkOrderItem.getRowNumber());
				errorList.add(error);

			}
		}
	}

	/**
	 * @param emailPattern
	 * @param errorList
	 * @param wwBulkOrderItem
	 */
	private void validateToEmail(final Pattern emailPattern, final List<BulkOrderResponseError> errorList,
			final WWBulkOrderItemsDataModel wwBulkOrderItem)
	{
		if (null != wwBulkOrderItem.getToEmail())
		{
			final Matcher toEmailMatcher = emailPattern.matcher(wwBulkOrderItem.getToEmail());
			if (!toEmailMatcher.matches())
			{
				final BulkOrderResponseError error = new BulkOrderResponseError();
				error.setErrorMessage(WooliesgcFacadesConstants.BULK_ORDER_EMAIL_NOT_VALID);
				error.setErrorCode(WooliesgcFacadesConstants.BULK_ORDER_VALIDATION_ERROR);
				error.setValue(wwBulkOrderItem.getToEmail());
				error.setProperty("toEmail");
				error.setRowNumber(wwBulkOrderItem.getRowNumber());
				errorList.add(error);

			}
		}
	}

	/**
	 * @param errorList
	 * @param wwBulkOrderItem
	 */
	private void validateRowNumber(final List<BulkOrderResponseError> errorList, final WWBulkOrderItemsDataModel wwBulkOrderItem)
	{
		if (null == wwBulkOrderItem.getRowNumber())
		{
			final BulkOrderResponseError error = new BulkOrderResponseError();
			error.setErrorMessage(WooliesgcFacadesConstants.BULK_ORDER_NO_VALUE_ERROR);
			error.setErrorCode(WooliesgcFacadesConstants.BULK_ORDER_MISSING_VALUE_ERROR);
			error.setValue(wwBulkOrderItem.getRowNumber().toString());
			error.setProperty("rowNumber");
			error.setRowNumber(wwBulkOrderItem.getRowNumber());
			errorList.add(error);
		}
	}

	/**
	 * @param errorList
	 * @param wwBulkOrderItem
	 */
	private void validateUnitPrice(final List<BulkOrderResponseError> errorList, final WWBulkOrderItemsDataModel wwBulkOrderItem)
	{
		if (null == wwBulkOrderItem.getUnitPrice())
		{
			final BulkOrderResponseError error = new BulkOrderResponseError();
			error.setErrorMessage(WooliesgcFacadesConstants.BULK_ORDER_NO_VALUE_ERROR);
			error.setErrorCode(WooliesgcFacadesConstants.BULK_ORDER_MISSING_VALUE_ERROR);
			error.setValue(wwBulkOrderItem.getUnitPrice().toString());
			error.setProperty(WooliesgcFacadesConstants.BULK_ORDER_UNIT_PRICE);
			error.setRowNumber(wwBulkOrderItem.getRowNumber());
			errorList.add(error);
		}
	}

	/**
	 * @param errorList
	 * @param wwBulkOrderItem
	 */
	private void validateSku(final List<BulkOrderResponseError> errorList, final WWBulkOrderItemsDataModel wwBulkOrderItem)
	{
		if (null == wwBulkOrderItem.getSkuCode())
		{
			final BulkOrderResponseError error = new BulkOrderResponseError();
			error.setErrorMessage(WooliesgcFacadesConstants.BULK_ORDER_NO_VALUE_ERROR);
			error.setErrorCode(WooliesgcFacadesConstants.BULK_ORDER_MISSING_VALUE_ERROR);
			error.setValue(wwBulkOrderItem.getSkuCode());
			error.setProperty(WooliesgcFacadesConstants.BULK_ORDER_SKU_CODE);
			error.setRowNumber(wwBulkOrderItem.getRowNumber());
			errorList.add(error);

		}
	}

	/**
	 * @param corporateb2bCustomerModel
	 * @param thresholdValue
	 * @param b2bPermission
	 * @return
	 */
	private Double getThreshHoldValue(final CorporateB2BCustomerModel corporateb2bCustomerModel, Double thresholdValue,
			final B2BPermissionModel b2bPermission)
	{
		if (b2bPermission.getCode().equalsIgnoreCase(corporateb2bCustomerModel.getUid()))
		{
			final B2BOrderThresholdPermissionModel b2bOrderPermissionModel = (B2BOrderThresholdPermissionModel) b2bPermission;
			thresholdValue = b2bOrderPermissionModel.getThreshold();
		}
		return thresholdValue;
	}

	/**
	 * @param errorList
	 * @param wwBulkOrderItem
	 * @param priceRowModel
	 */
	private void validateProductPrice(final List<BulkOrderResponseError> errorList,
			final WWBulkOrderItemsDataModel wwBulkOrderItem, final List<PriceRowModel> priceRowModel)
	{
		if (wwBulkOrderItem.getUnitPrice().compareTo(priceRowModel.get(0).getPrice()) != 0)
		{
			final BulkOrderResponseError error = new BulkOrderResponseError();
			error.setErrorMessage("Product Price is not correct");
			error.setErrorCode(WooliesgcFacadesConstants.BULK_ORDER_VALIDATION_ERROR);
			error.setValue(wwBulkOrderItem.getUnitPrice().toString());
			error.setProperty(WooliesgcFacadesConstants.BULK_ORDER_UNIT_PRICE);
			error.setRowNumber(wwBulkOrderItem.getRowNumber());
			errorList.add(error);
		}
	}





}
