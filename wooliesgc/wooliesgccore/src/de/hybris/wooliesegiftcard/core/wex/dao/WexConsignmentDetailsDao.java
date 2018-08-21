/**
 *
 */
package de.hybris.wooliesegiftcard.core.wex.dao;

import de.hybris.platform.core.model.order.OrderModel;


/**
 * @author 676313
 *
 */
public interface WexConsignmentDetailsDao
{
	public OrderModel getconsignmentDetailsForWex(final String invoiceNumber);
}