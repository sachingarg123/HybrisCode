/**
 *
 */
package de.hybris.wooliesegiftcard.core.job;

import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.AccountsRemovalCronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.wooliesegiftcard.core.workflow.dao.impl.AccountRemovalDaoImpl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;


/**
 * @author 676313
 *
 */
public class AccountRemovalJobPerformable extends AbstractJobPerformable<AccountsRemovalCronJobModel>
{



	/**
	 * @author 676313
	 *
	 */

	private AccountRemovalDaoImpl wooliesAccountRemoval;

	/**
	 * @return the modelService
	 */
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

	public AccountRemovalDaoImpl getWooliesAccountRemoval()
	{
		return wooliesAccountRemoval;
	}

	/**
	 * @param wooliesAccountRemoval
	 *           the wooliesAccountRemoval to set
	 */
	public void setWooliesAccountRemoval(final AccountRemovalDaoImpl wooliesAccountRemoval)
	{
		this.wooliesAccountRemoval = wooliesAccountRemoval;
	}

	@Override
	public PerformResult perform(final AccountsRemovalCronJobModel cronjob)
	{
		final int noOfDaysOldToRemove = cronjob.getXDaysOld();
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -noOfDaysOldToRemove);
		final Date oldDate = cal.getTime();
		final List<CustomerModel> accountRemovalListToBeDeleted = new ArrayList();
		final List<CustomerModel> userList = wooliesAccountRemoval.findAllB2COlderThanSpecifiedDays(oldDate);
		if (CollectionUtils.isNotEmpty(userList))
		{
			for (final CustomerModel customerModel : userList)
			{
				if (CollectionUtils.isEmpty(customerModel.getOrders()))
				{
					accountRemovalListToBeDeleted.add(customerModel);

				}
			}
		}
		if (CollectionUtils.isNotEmpty(accountRemovalListToBeDeleted))
		{
			getModelService().removeAll(accountRemovalListToBeDeleted);
		}

		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}

}

