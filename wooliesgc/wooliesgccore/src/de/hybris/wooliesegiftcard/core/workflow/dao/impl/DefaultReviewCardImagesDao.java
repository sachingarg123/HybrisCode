/**
 *
 */
/*
 * package de.hybris.wooliesegiftcard.core.workflow.dao.impl;
 * 
 * import de.hybris.platform.core.model.order.OrderEntryModel; import
 * de.hybris.platform.servicelayer.internal.dao.AbstractItemDao; import
 * de.hybris.platform.servicelayer.search.FlexibleSearchQuery; import
 * de.hybris.platform.servicelayer.search.SearchResult; import
 * de.hybris.wooliesegiftcard.core.enums.ImageApprovalStatus; import
 * de.hybris.wooliesegiftcard.core.model.WWCardImagesReviewModel; import
 * de.hybris.wooliesegiftcard.core.workflow.dao.ReviewCardImagesDao;
 * 
 * import java.util.List;
 * 
 * import org.apache.log4j.Logger;
 * 
 * 
 *//**
   * @author 653930
   *
   *//*
	  * public class DefaultReviewCardImagesDao extends AbstractItemDao implements ReviewCardImagesDao { private static
	  * final Logger LOG = Logger.getLogger(DefaultReviewCardImagesDao.class);
	  * 
	  * private static final String SELECT_QUERY_PENDING_IMAGES =
	  * " SELECT {PK}  FROM {OrderEntry} WHERE {cardImageApprovalStatus}=?imageStatus "; private static final String
	  * SELECT_QUERY_IMAGES = " SELECT {PK} FROM {WWCardImagesReview} ";
	  * 
	  * @Override public List<OrderEntryModel> findCardImagesForApproval() { LOG.debug("findCardImagesForApproval");
	  * final FlexibleSearchQuery query = new FlexibleSearchQuery(SELECT_QUERY_PENDING_IMAGES);
	  * query.addQueryParameter("imageStatus", ImageApprovalStatus.PENDING); final SearchResult<OrderEntryModel> result =
	  * getFlexibleSearchService().search(query); return result.getResult(); }
	  * 
	  * @Override public List<WWCardImagesReviewModel> findCardImages() { LOG.debug("findCardImages"); final
	  * FlexibleSearchQuery query = new FlexibleSearchQuery(SELECT_QUERY_IMAGES); final
	  * SearchResult<WWCardImagesReviewModel> result = getFlexibleSearchService().search(query); return
	  * result.getResult(); } }
	  */