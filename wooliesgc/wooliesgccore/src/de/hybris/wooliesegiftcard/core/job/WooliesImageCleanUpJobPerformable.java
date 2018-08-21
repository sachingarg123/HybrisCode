/**
 *
 */
package de.hybris.wooliesegiftcard.core.job;

import de.hybris.model.PersonalisationMediaModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.wooliesegiftcard.core.genric.dao.impl.DefaultWooliesGenericDao;

import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author 653154
 *
 */
public class WooliesImageCleanUpJobPerformable extends AbstractJobPerformable<CronJobModel>
{
	private static final Logger LOG = Logger.getLogger(WooliesImageCleanUpJobPerformable.class.getName());
	@Autowired
	private DefaultWooliesGenericDao defaultWooliesGenericDao;

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable#perform(de.hybris.platform.cronjob.model.
	 * CronJobModel)
	 */
	@Override
	public PerformResult perform(final CronJobModel job)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("WooliesImageCleanUpJobPerformable start : " + new Date());
		}
		final List<PersonalisationMediaModel> pendingImages = defaultWooliesGenericDao.getImagesforDelete();
		if (clearAbortRequestedIfNeeded(job))
		{
			return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
		}
		if (CollectionUtils.isNotEmpty(pendingImages))
		{
			modelService.removeAll(pendingImages);
		}
		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}

	@Override
	public boolean isAbortable()
	{
		return true;
	}
}
