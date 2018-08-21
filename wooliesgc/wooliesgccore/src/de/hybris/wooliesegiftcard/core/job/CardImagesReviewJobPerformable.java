/**
 *
 */
/*
 * package de.hybris.wooliesegiftcard.core.job;
 * 
 * import de.hybris.platform.core.model.order.OrderEntryModel; import de.hybris.platform.cronjob.enums.CronJobResult;
 * import de.hybris.platform.cronjob.enums.CronJobStatus; import de.hybris.platform.cronjob.model.CronJobModel; import
 * de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable; import
 * de.hybris.platform.servicelayer.cronjob.PerformResult; import de.hybris.platform.servicelayer.model.ModelService;
 * import de.hybris.wooliesegiftcard.core.model.WWCardImagesReviewModel; import
 * de.hybris.wooliesegiftcard.core.workflow.dao.impl.DefaultReviewCardImagesDao;
 * 
 * import java.util.List;
 * 
 * import org.apache.log4j.Logger;
 * 
 * 
 *//**
   * @author 653930
   *
   */
/*
 * public class CardImagesReviewJobPerformable extends AbstractJobPerformable<CronJobModel> { private
 * DefaultReviewCardImagesDao reviewCardImagesDao; private ModelService modelService;
 * 
 * @Override public PerformResult perform(final CronJobModel cronJob) { final Logger LOG =
 * Logger.getLogger(CardImagesReviewJobPerformable.class);
 * 
 * // start the Images workflow for the Pending Images final List<WWCardImagesReviewModel> cardImages =
 * getReviewCardImagesDao().findCardImages(); LOG.debug("cardImages.isEmpty()==" + cardImages.isEmpty()); final
 * List<OrderEntryModel> orderEntryList = getReviewCardImagesDao().findCardImagesForApproval();
 * LOG.debug("orderEntryList.isEmpty()==" + orderEntryList.isEmpty()); if (!cardImages.isEmpty()) {
 * getModelService().removeAll(cardImages); LOG.debug("Remove Old Entry Successfully"); } if (!orderEntryList.isEmpty())
 * { for (final OrderEntryModel model : orderEntryList) { final WWCardImagesReviewModel cardImagesReview =
 * getModelService().create(WWCardImagesReviewModel.class); cardImagesReview.setCardImagesApproval(model);
 * getModelService().save(cardImagesReview); getModelService().refresh(cardImagesReview);
 * LOG.debug("Save  Entry Successfully"); } } return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED); }
 * 
 *//**
   * @return the reviewCardImagesDao
   */
/*
 * public DefaultReviewCardImagesDao getReviewCardImagesDao() { return reviewCardImagesDao; }
 * 
 * 
 *//**
   * @param reviewCardImagesDao
   *           the reviewCardImagesDao to set
   */
/*
 * public void setReviewCardImagesDao(final DefaultReviewCardImagesDao reviewCardImagesDao) { this.reviewCardImagesDao =
 * reviewCardImagesDao; }
 * 
 * 
 *//**
   * @return the modelService
   */
/*
 * public ModelService getModelService() { return modelService; }
 * 
 * 
 *//**
   * @param modelService
   *           the modelService to set
   *//*
	  * @Override public void setModelService(final ModelService modelService) { this.modelService = modelService; }
	  * 
	  * 
	  * }
	  */