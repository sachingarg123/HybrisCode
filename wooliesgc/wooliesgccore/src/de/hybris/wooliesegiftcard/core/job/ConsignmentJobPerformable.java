/**
 *
 */
package de.hybris.wooliesegiftcard.core.job;

import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.wooliesegiftcard.core.wex.dao.impl.WexConsignmentDetailsDaoImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author 676313
 *
 */
public class ConsignmentJobPerformable extends AbstractJobPerformable<CronJobModel>
{
	private static final Logger LOG = Logger.getLogger(ConsignmentJobPerformable.class);
	@Autowired
	private ConfigurationService configurationService;

	private WexConsignmentDetailsDaoImpl wexConsignmentDetailsDaoImpl;

	/**
	 * @return the wexConsignmentDetailsDaoImpl
	 */
	public WexConsignmentDetailsDaoImpl getWexConsignmentDetailsDaoImpl()
	{
		return wexConsignmentDetailsDaoImpl;
	}

	/**
	 * @param wexConsignmentDetailsDaoImpl
	 *           the wexConsignmentDetailsDaoImpl to set
	 */
	public void setWexConsignmentDetailsDaoImpl(final WexConsignmentDetailsDaoImpl wexConsignmentDetailsDaoImpl)
	{
		this.wexConsignmentDetailsDaoImpl = wexConsignmentDetailsDaoImpl;
	}

	@Autowired
	private ModelService modelService;

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable#perform(de.hybris.platform.cronjob.model.
	 * CronJobModel)
	 */
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

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable#perform(de.hybris.platform.cronjob.model.
	 * CronJobModel)
	 */
	@Override
	public PerformResult perform(final CronJobModel cronjob)
	{
		try
		{
			final String inboundFileLocation = configurationService.getConfiguration().getString("consignmentFiles.inbound");
			final String errorFileLocation = configurationService.getConfiguration().getString("consignmentFiles.error");
			final String archiveFileLocation = configurationService.getConfiguration().getString("consignmentFiles.archive");
			final File inboundFolder = new File(inboundFileLocation);
			final File errorFolder = new File(errorFileLocation);
			final File archiveFolder = new File(archiveFileLocation);
			final File[] files = inboundFolder.listFiles();
			final String FILENAMEDATEFORMAT = "ddMMMyyyySSSssss";
			final String stamp = new SimpleDateFormat(FILENAMEDATEFORMAT).format(new Date());
			final File errorfile = new File(errorFolder + stamp);
			boolean isOrderUpdated = false;
			for (final File flatFile : files)
			{
				final String cvsSplitBy = "\\|";
				BufferedReader br = null;
				try
				{
					br = new BufferedReader(new FileReader(flatFile));
					isOrderUpdated = movingFiles(errorFolder, archiveFolder, errorfile, isOrderUpdated, flatFile, cvsSplitBy, br);
				}
				finally
				{
					if (null != br)
					{
						br.close();
					}
				}

			}
			return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
		}
		catch (final Exception e)
		{
			LOG.info("exception :  " + e);
			return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
		}
	}

	/**
	 * This Method is used to Move files to the respective folders
	 *
	 * @param errorFolder
	 * @param archiveFolder
	 * @param errorfile
	 * @param isOrderUpdated
	 * @param flatFile
	 * @param cvsSplitBy
	 * @param br
	 * @return
	 * @throws IOException
	 */
	private boolean movingFiles(final File errorFolder, final File archiveFolder, final File errorfile, boolean isOrderUpdated,
			final File flatFile, final String cvsSplitBy, final BufferedReader br) throws IOException
	{
		String line;
		if ((line = br.readLine()) != null)
		{
			isOrderUpdated = setConsignmentNoAndCount(errorfile, isOrderUpdated, line, cvsSplitBy);
		}

		if (!isOrderUpdated)
		{
			moveFiles(flatFile, errorFolder);
		}
		else
		{
			moveFiles(flatFile, archiveFolder);
		}
		return isOrderUpdated;
	}

	/**
	 * This method is used to set consignment Number and Product count
	 *
	 * @param errorfile
	 * @param isOrderUpdated
	 * @param line
	 * @param cvsSplitBy
	 * @param br
	 * @return boolean value
	 * @throws IOException
	 */
	private boolean setConsignmentNoAndCount(final File errorfile, boolean isOrderUpdated, final String line,
			final String cvsSplitBy) throws IOException
	{
		final String[] values = line.split(cvsSplitBy);
		final String invoiceNumber = values[0];
		final String consignmentNumber = values[1];
		final String productCount = values[3];
		if (invoiceNumber != null && consignmentNumber != null && productCount != null)
		{
			final OrderModel orderModel = wexConsignmentDetailsDaoImpl.getconsignmentDetailsForWex(invoiceNumber);
			if (orderModel != null)
			{
				isOrderUpdated = setStatusAndSaveOrder(isOrderUpdated, consignmentNumber, productCount, orderModel);
			}
			else
			{
				FileWriter writer = null;
				try
				{
					writer = new FileWriter(errorfile);
					writer.write(invoiceNumber);
					writer.close();
					LOG.error("No order found for the given taxinvoiceID");
				}
				finally
				{
					if (null != writer)
					{
						writer.close();
					}
				}
			}
		}
		return isOrderUpdated;
	}

	/**
	 * @param isOrderUpdated
	 * @param consignmentNumber
	 * @param productCount
	 * @param orderModel
	 * @return boolean value
	 */
	private boolean setStatusAndSaveOrder(boolean isOrderUpdated, final String consignmentNumber, final String productCount,
			final OrderModel orderModel)
	{
		final List<AbstractOrderEntryModel> entries = orderModel.getEntries();
		for (final AbstractOrderEntryModel abstractOrderEntryModel : entries)
		{
			if (!abstractOrderEntryModel.getProduct().getIsEGiftCard())
			{
				orderModel.setConsignmentNo(consignmentNumber);
				orderModel.setProductCount(productCount);
				orderModel.setStatus(OrderStatus.COMPLETED);
				getModelService().save(orderModel);
				isOrderUpdated = true;
				break;
			}
		}
		return isOrderUpdated;
	}



	/**
	 * This method is used move the files
	 *
	 * @param flatFile
	 *           File
	 * @param destination
	 *           File
	 * @throws IOException
	 *            throws this exception
	 */
	private void moveFiles(final File flatFile, final File destination) throws IOException
	{
		FileUtils.copyFileToDirectory(flatFile, destination);
		flatFile.delete();
	}
}


