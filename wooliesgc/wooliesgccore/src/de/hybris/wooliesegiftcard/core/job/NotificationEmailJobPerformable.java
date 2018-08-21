/**
 *
 */
package de.hybris.wooliesegiftcard.core.job;

import de.hybris.model.EGiftCardModel;
import de.hybris.platform.acceleratorservices.email.EmailService;
import de.hybris.platform.acceleratorservices.model.email.EmailAttachmentModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.wooliesegiftcard.core.EncryptionUtils;
import de.hybris.wooliesegiftcard.core.enums.UserDataType;
import de.hybris.wooliesegiftcard.core.genric.dao.impl.DefaultWooliesGenericDao;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author 687679
 *
 */
public class NotificationEmailJobPerformable extends AbstractJobPerformable<CronJobModel>
{
	@Autowired
	private DefaultWooliesGenericDao defaultWooliesGenericDao;
	@Resource(name = "configurationService")
	private ConfigurationService configurationService;
	@Autowired
	BaseSiteService baseSiteService;
	@Autowired
	private BusinessProcessService businessProcessService;
	@Autowired
	private EmailService emailService;

	private static final String CARDNAME = "Card Name";
	private static final String CARDVALUE = "Card Value";
	private static final String RECIPIENTNAME = "Recipient Name";
	private static final String RECIPIENTEMAIL = "Recepient-Email";
	private static final String SENDERNAME = "Sender-Name";
	private static final String MESSAGE = "Message";
	private static final String GIFTCARDLINK = "eGift Card Link";
	private static final String CHARACTERENCODING = "UTF-8";
	private static final Logger LOG = Logger.getLogger(NotificationEmailJobPerformable.class);
	final String FILENAMEDATEFORMAT = "ddMMMyyyySSSssss";
	final String extension = ".xls";
	private static final String IO_EXCEPTION_IN_ECARDJOB = "IOException in EGiftCardJobPerformable: ";
	private static final String FILE_NOTFOUND_IN_ECARDJOB = "FileNotFoundException in EGiftCardJobPerformable: ";
	private static final String ERROR_LOADIND_DATA = "Error in loading data";
	private static final String VALUED_CUSTOMER = "Valued Customer";

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable#perform(de.hybris.platform.cronjob.model.
	 * CronJobModel)
	 */
	@Override
	public PerformResult perform(final CronJobModel arg0)
	{
		final Collection<OrderModel> orders = defaultWooliesGenericDao.getOrdersForEmailNotification();
		int count = 0;
		if (null != orders && !orders.isEmpty())
		{
			for (final OrderModel order : orders)
			{
				final List<AbstractOrderEntryModel> entries = order.getEntries();
				for (final AbstractOrderEntryModel orderEntry : entries)
				{
					count = numberOfEGiftProducts(count, orderEntry);
				}
				if (count >= 1)
				{
					getCardDetailsAndSaveOrder(order);
				}
			}
		}
		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}


	/**
	 * @param order
	 *           Order Card details
	 */
	private void getCardDetailsAndSaveOrder(final OrderModel order)
	{
		try
		{
			eGiftCardDetails(order);

			modelService.save(order);
			modelService.refresh(order);
			LOG.info("Cronjob Notification passed");
		}

		catch (final IOException e)
		{
			LOG.info("No File Available available ", e);

		}
	}


	/**
	 * @param count
	 *           number of Count
	 * @param orderEntry
	 *           Order Entry
	 * @return count
	 */
	private int numberOfEGiftProducts(int count, final AbstractOrderEntryModel orderEntry)
	{
		if (orderEntry.getProduct().getIsEGiftCard().booleanValue())
		{
			count = count + 1;
		}
		return count;
	}


	/**
	 * @param order
	 * @throws IOException
	 */
	void eGiftCardDetails(final OrderModel order) throws IOException
	{

		final List<EGiftCardModel> eGiftCardList = defaultWooliesGenericDao.getEgiftCardDetails(order.getCode());
		final UserModel customer = order.getUser();
		if (customer instanceof CustomerModel)
		{
			final CustomerModel customerModel = (CustomerModel) customer;

			if (customerModel.getCustomerType().getCode() == UserDataType.B2B.getCode())
			{
				getEmailList(order, eGiftCardList, customerModel);
			}
			else
			{
				forOtherUsers(order, eGiftCardList, customerModel);
			}
		}
	}


	/**
	 * This Method handles all Users except B2B
	 *
	 * @param order
	 *           the Order Associated
	 * @param eGiftCardList
	 *           the List of E-giftCard Model
	 * @param customerModel
	 *           the Customer Associated
	 * @throws FileNotFoundException
	 *            throws an exception if file is not found
	 * @throws IOException
	 *            throws exception if any error occurs
	 */
	private void forOtherUsers(final OrderModel order, final List<EGiftCardModel> eGiftCardList, final CustomerModel customerModel)
			throws FileNotFoundException, IOException
	{
		String toName = null;
		String message = null;
		final EGiftCardModel firstGiftcard = eGiftCardList.get(0);
		if (null != firstGiftcard.getPersonalisationGiftCard().getToName())
		{
			toName = firstGiftcard.getPersonalisationGiftCard().getToName();
		}
		if (null != firstGiftcard.getPersonalisationGiftCard().getMessage())
		{
			message = firstGiftcard.getPersonalisationGiftCard().getMessage();
		}
		final String toEmail = customerModel.getUserEmail();

		if (!eGiftCardList.isEmpty())
		{
			final String stamp = new SimpleDateFormat(FILENAMEDATEFORMAT).format(new Date());
			final String filename = order.getCode() + stamp + extension;
			final File eGiftCardFile = new File(filename);
			final FileOutputStream fileOutputStream;
			fileOutputStream = new FileOutputStream(eGiftCardFile);
			final HSSFWorkbook workbook = new HSSFWorkbook();
			final HSSFSheet sheet = workbook.createSheet(order.getCode());
			prepareHeaderRow(sheet);
			final int rowCount = 1;
			final EmailAttachmentModel attachmentModel = loadingCSVAndCreatingAttachment(order, eGiftCardList, filename,
					eGiftCardFile, fileOutputStream, workbook, sheet, rowCount);

			final OrderProcessModel orderProcessModel = createOrderProcessModel();
			if (orderProcessModel != null)
			{


				orderProcessModel.setAttachment(attachmentModel.getCode());
				orderProcessModel.setToEmail(toEmail);
				orderProcessModel.setMessage(null != message ? message : "");
				orderProcessModel.setToName(null != toName ? toName : VALUED_CUSTOMER);
				orderProcessModel.setOrder(order);
				modelService.save(orderProcessModel);
				businessProcessService.startProcess(orderProcessModel);
			}
			order.setStatus(OrderStatus.MAIL_NOTIFICATION_COMPLETED);
		}
	}


	/**
	 * @return OrderProcessModel
	 */
	private OrderProcessModel createOrderProcessModel()
	{
		final OrderProcessModel orderProcessModel = businessProcessService
				.createProcess("eGiftCardOrderEmailProcess-" + "-" + System.currentTimeMillis(), "eGiftCardOrderEmailProcess");
		return orderProcessModel;
	}



	/**
	 * * @param order the Order Associated
	 *
	 * @param eGiftCardList
	 *           the List of E-giftCard Model
	 * @param customerModel
	 *           the Customer Associated
	 * @throws FileNotFoundException
	 *            throws an exception if file is not found
	 * @throws IOException
	 *            throws exception if any error occurs
	 */
	private void getEmailList(final OrderModel order, final List<EGiftCardModel> eGiftCardList, final CustomerModel customerModel)
			throws IOException
	{
		final List<String> listOfEmails = new ArrayList();
		final Map<String, List<EGiftCardModel>> personalisationEmailMap = new HashMap();
		final Map<String, List<EGiftCardModel>> orderEmailMap = new HashMap<>();

		final List<EGiftCardModel> orderEmailGiftCardList = new ArrayList<>();
		for (final EGiftCardModel eGiftCard : eGiftCardList)
		{
			if (null != eGiftCard.getPersonalisationGiftCard().getToEmail())
			{

				if (personalisationEmailMap.containsKey(eGiftCard.getPersonalisationGiftCard().getToEmail()))
				{
					final List<EGiftCardModel> list1 = personalisationEmailMap
							.get(eGiftCard.getPersonalisationGiftCard().getToEmail());
					list1.add(eGiftCard);
					LOG.info("list1 added");
				}
				else
				{
					final List<EGiftCardModel> list2 = new ArrayList<>();
					list2.add(eGiftCard);
					personalisationEmailMap.put(eGiftCard.getPersonalisationGiftCard().getToEmail(), list2);
				}
				listOfEmails.add(eGiftCard.getPersonalisationGiftCard().getToEmail());
				LOG.info("listOfEmails added");
			}

			else
			{
				orderEmailGiftCardList.add(eGiftCard);
				orderEmailMap.put(customerModel.getUserEmail(), orderEmailGiftCardList);

			}
		}

		if (!personalisationEmailMap.isEmpty())
		{
			for (final String toEmail : personalisationEmailMap.keySet())
			{
				personalisedEmails(order, personalisationEmailMap, toEmail);
				LOG.info("personalisedEmails called");
			}
		}


		if (!orderEmailMap.isEmpty())
		{
			for (final String eachUser : orderEmailMap.keySet())
			{
				orderEmailList(order, orderEmailMap, eachUser);
				LOG.info("orderEmailList called");
			}
		}
	}

	/**
	 * @param orderEmailMap
	 *           Order Email Map
	 * @param eachUser
	 *           the Email Id
	 * @throws IOException
	 *            throws this exception
	 */
	private void orderEmailList(final OrderModel order, final Map<String, List<EGiftCardModel>> orderEmailMap,
			final String eachUser) throws IOException
	{
		final List<EGiftCardModel> list1 = orderEmailMap.get(eachUser);

		String toName = null;
		String message = null;
		String fromName = null;
		final EGiftCardModel firstGiftcard = list1.get(0);
		if (null != firstGiftcard.getPersonalisationGiftCard().getToName())
		{
			toName = firstGiftcard.getPersonalisationGiftCard().getToName();
		}
		if (null != firstGiftcard.getPersonalisationGiftCard().getMessage())
		{
			message = firstGiftcard.getPersonalisationGiftCard().getMessage();
		}
		if (null != firstGiftcard.getPersonalisationGiftCard().getFromName())
		{
			fromName = firstGiftcard.getPersonalisationGiftCard().getFromName();
		}
		if (!list1.isEmpty())
		{

			final String stamp = new SimpleDateFormat(FILENAMEDATEFORMAT).format(new Date());
			final String filename = order.getCode() + stamp + extension;
			final File eGiftCardFile = new File(filename);
			final FileOutputStream fileOutputStream;
			fileOutputStream = new FileOutputStream(eGiftCardFile);
			final HSSFWorkbook workbook = new HSSFWorkbook();
			final HSSFSheet sheet = workbook.createSheet(order.getCode());
			prepareHeaderRow(sheet);
			final int rowCount = 1;
			final EmailAttachmentModel attachmentModel = loadingCSVAndCreatingAttachment(order, list1, filename, eGiftCardFile,
					fileOutputStream, workbook, sheet, rowCount);

			final OrderProcessModel orderProcessModel = createOrderProcessModel();
			if (orderProcessModel != null)
			{


				final UserModel user = order.getUser();
				final CustomerModel customer = (CustomerModel) user;
				orderProcessModel.setAttachment(attachmentModel.getCode());
				orderProcessModel.setToEmail(eachUser);
				orderProcessModel.setFromEmail(null != fromName ? fromName : customer.getFirstName());
				orderProcessModel.setMessage(null != message ? message : "");
				orderProcessModel.setToName(null != toName ? toName : VALUED_CUSTOMER);
				orderProcessModel.setOrder(order);
				modelService.save(orderProcessModel);
				businessProcessService.startProcess(orderProcessModel);
			}
			order.setStatus(OrderStatus.MAIL_NOTIFICATION_COMPLETED);
		}

	}


	/**
	 * @param order
	 * @param list1
	 * @param filename
	 * @param eGiftCardFile
	 * @param fileOutputStream
	 * @param workbook
	 * @param sheet
	 * @param rowCount
	 * @return EmailAttachmentModel
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private EmailAttachmentModel loadingCSVAndCreatingAttachment(final OrderModel order, final List<EGiftCardModel> list1,
			final String filename, final File eGiftCardFile, final FileOutputStream fileOutputStream, final HSSFWorkbook workbook,
			final HSSFSheet sheet, int rowCount) throws IOException, FileNotFoundException
	{
		for (final EGiftCardModel eGiftCardModel : list1)
		{
			try
			{
				loadOrderDatatoCSV(eGiftCardModel, sheet, rowCount, order);
			}
			catch (final UnsupportedEncodingException e)
			{
				LOG.error(ERROR_LOADIND_DATA, e);
			}
			rowCount++;
		}
		try
		{
			workbook.write(fileOutputStream);

		}
		catch (final FileNotFoundException e1)
		{
			LOG.error(FILE_NOTFOUND_IN_ECARDJOB, e1);
		}
		catch (final IOException e1)
		{
			LOG.error(IO_EXCEPTION_IN_ECARDJOB, e1);
		}
		finally
		{
			workbook.close();
			fileOutputStream.close();
		}

		final DataInputStream dataIn = new DataInputStream(new FileInputStream(eGiftCardFile));
		final EmailAttachmentModel attachmentModel = emailService.createEmailAttachment(dataIn, filename, ".xls");
		return attachmentModel;
	}

	/**
	 * @param order
	 * @param personalisationEmailMap
	 * @param toEmail
	 * @throws FileNotFoundException
	 */
	private void personalisedEmails(final OrderModel order, final Map<String, List<EGiftCardModel>> personalisationEmailMap,
			final String toEmail) throws FileNotFoundException
	{
		final List<EGiftCardModel> list3 = personalisationEmailMap.get(toEmail);

		String toName = null;
		String message = null;
		String fromName = null;
		final EGiftCardModel firstGiftcard = list3.get(0);
		if (null != firstGiftcard.getPersonalisationGiftCard().getToName())
		{
			toName = firstGiftcard.getPersonalisationGiftCard().getToName();
		}
		if (null != firstGiftcard.getPersonalisationGiftCard().getMessage())
		{
			message = firstGiftcard.getPersonalisationGiftCard().getMessage();
		}
		if (null != firstGiftcard.getPersonalisationGiftCard().getFromName())
		{
			fromName = firstGiftcard.getPersonalisationGiftCard().getFromName();
		}
		if (!list3.isEmpty())
		{
			final String stamp = new SimpleDateFormat(FILENAMEDATEFORMAT).format(new Date());
			final String filename = order.getCode() + stamp + extension;
			final File eGiftCardFile = new File(filename);
			final FileOutputStream fileOutputStream;
			fileOutputStream = new FileOutputStream(eGiftCardFile);
			final HSSFWorkbook workbook = new HSSFWorkbook();
			final HSSFSheet sheet = workbook.createSheet(order.getCode());
			prepareHeaderRow(sheet);
			int rowCount = 1;
			for (final EGiftCardModel eGiftCardModel : list3)
			{
				try
				{
					loadOrderDatatoCSV(eGiftCardModel, sheet, rowCount, order);
				}
				catch (final UnsupportedEncodingException e)
				{
					LOG.error(ERROR_LOADIND_DATA, e);
				}
				rowCount++;
			}
			try
			{
				workbook.write(fileOutputStream);
				workbook.close();
				fileOutputStream.close();
			}
			catch (final FileNotFoundException e1)
			{
				LOG.error(FILE_NOTFOUND_IN_ECARDJOB, e1);
			}
			catch (final IOException e1)
			{
				LOG.error(IO_EXCEPTION_IN_ECARDJOB, e1);
			}

			final DataInputStream dataIn = new DataInputStream(new FileInputStream(eGiftCardFile));
			final EmailAttachmentModel attachmentModel = emailService.createEmailAttachment(dataIn, filename, ".xls");

			final OrderProcessModel orderProcessModel = createOrderProcessModel();
			if (orderProcessModel != null)
			{
				final UserModel user = order.getUser();
				final CustomerModel customer = (CustomerModel) user;
				orderProcessModel.setAttachment(attachmentModel.getCode());
				orderProcessModel.setToEmail(toEmail);
				orderProcessModel.setFromEmail(null != fromName ? fromName : customer.getFirstName());
				orderProcessModel.setMessage(null != message ? message : "");
				orderProcessModel.setToName(null != toName ? toName : VALUED_CUSTOMER);
				orderProcessModel.setOrder(order);
				modelService.save(orderProcessModel);
				businessProcessService.startProcess(orderProcessModel);
			}
			order.setStatus(OrderStatus.MAIL_NOTIFICATION_COMPLETED);
		}

	}


	/**
	 * @param sheet
	 */
	private void prepareHeaderRow(final HSSFSheet sheet)
	{
		final HSSFRow rowHead = sheet.createRow((short) 0);
		rowHead.createCell(0).setCellValue(CARDNAME);
		rowHead.createCell(1).setCellValue(CARDVALUE);
		rowHead.createCell(2).setCellValue(RECIPIENTNAME);
		rowHead.createCell(3).setCellValue(RECIPIENTEMAIL);
		rowHead.createCell(4).setCellValue(SENDERNAME);
		rowHead.createCell(5).setCellValue(MESSAGE);
		rowHead.createCell(6).setCellValue(GIFTCARDLINK);
	}

	private void loadOrderDatatoCSV(final EGiftCardModel eGiftCardModel, final HSSFSheet sheet, final int rowCount,
			final OrderModel orderModel) throws UnsupportedEncodingException
	{
		final String url = configurationService.getConfiguration().getString("tokenURL");
		final String encypKey = configurationService.getConfiguration().getString("encryption.key");
		final byte[] encypKeyByte = encypKey.getBytes(CHARACTERENCODING);
		final HSSFRow row = sheet.createRow((short) rowCount);
		final CustomerModel customer = (CustomerModel) orderModel.getUser();
		final AbstractOrderEntryModel eachEntry = orderModel.getEntries().get(Integer.valueOf(eGiftCardModel.getOrdeLineID()));
		if (null != eachEntry)
		{
			row.createCell(0).setCellValue(eachEntry.getProduct().getName());
		}
		row.createCell(1).setCellValue(eGiftCardModel.getGiftCardValue());
		if (eGiftCardModel.getPersonalisationGiftCard() != null && eGiftCardModel.getPersonalisationGiftCard().getToName() != null)
		{
			row.createCell(2).setCellValue(eGiftCardModel.getPersonalisationGiftCard().getToName());
		}
		else
		{
			row.createCell(2).setCellValue(VALUED_CUSTOMER);
		}
		if (eGiftCardModel.getPersonalisationGiftCard() != null && eGiftCardModel.getPersonalisationGiftCard().getToEmail() != null)
		{
			row.createCell(3).setCellValue(eGiftCardModel.getPersonalisationGiftCard().getToEmail());
		}
		else
		{
			row.createCell(3).setCellValue(customer.getUserEmail());
		}
		if (eGiftCardModel.getPersonalisationGiftCard() != null && eGiftCardModel.getPersonalisationGiftCard().getToName() != null)
		{
			row.createCell(4).setCellValue(eGiftCardModel.getPersonalisationGiftCard().getToName());
		}
		else
		{
			row.createCell(4).setCellValue(customer.getName());
		}
		if (eGiftCardModel.getPersonalisationGiftCard() != null && eGiftCardModel.getPersonalisationGiftCard().getMessage() != null)
		{
			row.createCell(5).setCellValue(eGiftCardModel.getPersonalisationGiftCard().getMessage());
		}
		row.createCell(6).setCellValue(url + EncryptionUtils.encrypt(eGiftCardModel.getGiftCardToken(), encypKeyByte));
	}
}
