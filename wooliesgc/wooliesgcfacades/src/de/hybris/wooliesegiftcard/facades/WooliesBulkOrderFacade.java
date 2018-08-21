/**
 *
 */
package de.hybris.wooliesegiftcard.facades;

import de.hybris.wooliesegiftcard.exceptions.WooliesFacadeLayerException;
import de.hybris.wooliesegiftcard.facades.dto.BulkOrderRequestDTO;
import de.hybris.wooliesegiftcard.facades.dto.BulkOrderResponseDTO;


/**
 * @author 669567 This interface is used to maintain bulk order details
 */
public interface WooliesBulkOrderFacade
{
	/**
	 * This method is used to save the bulk order and returns order object
	 *
	 * @param bulkOrder
	 *           the Buld Order Request DTO for the User
	 * @return bulkOrderResponseDTO the bulk Order Response DTO for the User
	 */
	BulkOrderResponseDTO saveBulkOrder(BulkOrderRequestDTO bulkOrder);

	/**
	 * This method is used to validates the bulk order details and return the valid bulk order
	 *
	 * @param referenceNumber
	 *           the REF Number for the Validation of Bulk Order
	 * @return BulkOrderResponseDTO as the Response
	 * @throws WooliesFacadeLayerException
	 *            throws this Exception in case of any Error
	 */
	BulkOrderResponseDTO validateBulkOrder(String referenceNumber) throws WooliesFacadeLayerException;
}
