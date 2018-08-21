/**
 *
 */
package de.hybris.wooliesegiftcard.core.job;

import de.hybris.model.EGiftCardModel;
import de.hybris.model.PersonalisationEGiftCardModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.wooliesegiftcard.core.EncryptionUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.solr.common.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @author 653154
 *
 */
public class EGiftCardJobPerformable extends AbstractJobPerformable<CronJobModel>
{
	private static final Logger LOG = Logger.getLogger(EGiftCardJobPerformable.class);
	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private ModelService modelService;
	@Autowired
	private FlexibleSearchService flexibleSearchService;
	private KeyGenerator keyGenerator;
	private static final String CHARACTERENCODING = "UTF-8";

	@Override
	public PerformResult perform(final CronJobModel job)
	{
		try
		{
			final String inboundFileLocation = configurationService.getConfiguration().getString("giftCardFiles.inbound");
			final String errorFileLocation = configurationService.getConfiguration().getString("giftCardFiles.error");
			final String archiveFileLocation = configurationService.getConfiguration().getString("giftCardFiles.archive");
			final String encypKey = configurationService.getConfiguration().getString("encryption.key");
			final byte[] encypKeyByte = encypKey.getBytes(CHARACTERENCODING);
			final File inboundFolder = new File(inboundFileLocation);
			final File errorFolder = new File(errorFileLocation);
			final File archiveFolder = new File(archiveFileLocation);
			final File[] files = inboundFolder.listFiles();
			for (final File fXmlFile : files)
			{
				boolean isGiftCardCreated = false;
				LOG.info("started execution : " + files.length);
				final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				final Document doc = dBuilder.parse(fXmlFile);
				doc.getDocumentElement().normalize();
				OrderModel orderModel = null;
				String emailID = null;
				final File file = new File(errorFolder + fXmlFile.getName());
				boolean hasNoOrderCode = false;
				final NodeList rootList = doc.getElementsByTagName("WoolworthsOrderFTPResponseMessage");
				for (int temp = 0; temp < rootList.getLength(); temp++)
				{
					final Node nNode = rootList.item(temp);
					if (nNode.getNodeType() == Node.ELEMENT_NODE)
					{
						final Element eElement = (Element) nNode;
						final String orderID = eElement.getElementsByTagName("RequestMessageId").item(0).getTextContent();
						emailID = eElement.getElementsByTagName("UserName").item(0).getTextContent();
						if (StringUtils.isEmpty(orderID) || StringUtils.isEmpty(emailID))
						{
							LOG.info("OrderID or EmailID is null, skipping further processing");
							hasNoOrderCode = true;
							break;
						}
						else
						{
							orderModel = findOrderByCode(orderID);
							if (orderModel == null)
							{
								LOG.info("No Order found, skipping further processing");
								hasNoOrderCode = true;
								break;
							}
						}
					}
				}
				if (hasNoOrderCode)
				{
					LOG.info("Skipping the complete file");
					isGiftCardCreated = false;
				}
				else
				{
					final NodeList nList = doc.getElementsByTagName("Order");
					for (int temp = 0; temp < nList.getLength(); temp++)
					{
						final Node nNode = nList.item(temp);
						if (nNode.getNodeType() == Node.ELEMENT_NODE)
						{
							isGiftCardCreated = saveEgiftCardModel(encypKeyByte, isGiftCardCreated, doc, orderModel, emailID, file, temp,
									nNode);
						}
					}
				}

				moveFiles(isGiftCardCreated, fXmlFile, archiveFolder, errorFolder);
			}
			return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
		}
		catch (final Exception e)
		{
			LOG.info("CronJob failed, please check the files");
			try
			{
				throw e;
			}
			catch (final Exception e1)
			{
				LOG.info(e1);
			}
			return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
		}


	}

	/**
	 * @param encypKeyByte
	 * @param isGiftCardCreated
	 * @param doc
	 * @param orderModel
	 * @param emailID
	 * @param file
	 * @param temp
	 * @param nNode
	 * @return boolean
	 * @throws ParseException
	 * @throws IOException
	 */
	private boolean saveEgiftCardModel(final byte[] encypKeyByte, boolean isGiftCardCreated, final Document doc,
			final OrderModel orderModel, final String emailID, final File file, final int temp, final Node nNode)
			throws ParseException, IOException
	{
		final Element eElement = (Element) nNode;
		boolean isElementMissing = false;
		boolean isDateMissing = false;
		final String orderLineID = eElement.getElementsByTagName("OrderReferenceId").item(0).getTextContent();
		final String cardNo = eElement.getElementsByTagName("CardNo").item(0).getTextContent();
		final String giftCardValue = eElement.getElementsByTagName("Amount").item(0).getTextContent();
		final String stringToEncrypt = eElement.getElementsByTagName("Pin").item(0).getTextContent();
		isElementMissing = validateElements(orderLineID, cardNo, giftCardValue, stringToEncrypt, doc);
		String validFrom = null, validTo = null;
		final NodeList rootCardList = doc.getElementsByTagName("Cards");
		final Node cardNode = rootCardList.item(temp);
		if (nNode.getNodeType() == Node.ELEMENT_NODE)
		{
			final Element dateElement = (Element) cardNode;
			validFrom = dateElement.getElementsByTagName("ValidFrom").item(0).getTextContent();
			validTo = dateElement.getElementsByTagName("ValidTo").item(0).getTextContent();
		}
		isDateMissing = validateDates(validFrom, validTo);
		if (isElementMissing || isDateMissing)
		{
			FileWriter writer = null;
			try
			{
				writer = new FileWriter(file);
				writer.write(eElement.getElementsByTagName("CardNo").item(0).getTextContent() + "\n");
			}
			finally
			{
				if (null != writer)
				{
					writer.close();
				}
			}
		}
		else
		{
			final EGiftCardModel eGiftCardModel = modelService.create(EGiftCardModel.class);
			eGiftCardModel.setCode((String) this.keyGenerator.generate());
			eGiftCardModel.setIsEgiftCard(Boolean.TRUE);
			eGiftCardModel.setEmailId(null != emailID ? emailID : "");
			eGiftCardModel.setGiftCardToken(UUID.randomUUID().toString());
			eGiftCardModel.setOrdeLineID(orderLineID);
			eGiftCardModel.setGiftCardNumber(Long.valueOf(cardNo));
			eGiftCardModel.setGiftCardValue(Double.valueOf(giftCardValue));
			final String pin = EncryptionUtils.encrypt(stringToEncrypt, encypKeyByte);
			eGiftCardModel.setPin(pin);
			eGiftCardModel.setActivationDate(new SimpleDateFormat("dd/MM/yyyy").parse(validFrom));
			eGiftCardModel.setExpiryDate(new SimpleDateFormat("dd/MM/yyyy").parse(validTo));
			if (null != orderModel)
			{
				eGiftCardModel.setOrderID(orderModel.getCode());
				assignGiftCard(orderModel, eElement, eGiftCardModel);
				isGiftCardCreated = true;
				orderModel.setStatus(OrderStatus.GENERATEGIFTCARD);
			}
		}
		return isGiftCardCreated;
	}

	/**
	 * @param validFrom
	 * @param validTo
	 * @throws ParseException
	 */
	private boolean validateDates(final String validFrom, final String validTo) throws ParseException
	{
		boolean dateMissingValue = false;
		if (StringUtils.isEmpty(validFrom) || StringUtils.isEmpty(validTo))
		{
			dateMissingValue = true;
		}
		else
		{
			dateMissingValue = false;
		}
		return dateMissingValue;
	}

	/**
	 * @param orderModel
	 * @param eElement
	 * @param eGiftCardModel
	 */
	private void assignGiftCard(final OrderModel orderModel, final Element eElement, final EGiftCardModel eGiftCardModel)
	{
		final AbstractOrderEntryModel abstractOrderEntry = orderModel.getEntries()
				.get(Integer.valueOf(eElement.getElementsByTagName("OrderReferenceId").item(0).getTextContent()));
		final List<PersonalisationEGiftCardModel> personalisationDetails = abstractOrderEntry.getPersonalisationDetail();
		for (final PersonalisationEGiftCardModel personalisationEGiftCardModel : personalisationDetails)
		{
			if (personalisationEGiftCardModel.getEGiftCard() == null)
			{
				personalisationEGiftCardModel.setEGiftCard(eGiftCardModel);
				eGiftCardModel.setPersonalisationGiftCard(personalisationEGiftCardModel);
				modelService.save(personalisationEGiftCardModel);
				break;
			}
		}
		modelService.save(eGiftCardModel);
	}

	/**
	 * @param orderLineID
	 * @param doc
	 * @param giftCardValue
	 */
	private boolean validateElements(final String orderLineID, final String cardNo, final String giftCardValue,
			final String stringToEncrypt, final Document doc)
	{
		final boolean result;
		if (StringUtils.isEmpty(orderLineID) || StringUtils.isEmpty(cardNo) || StringUtils.isEmpty(giftCardValue)
				|| StringUtils.isEmpty(stringToEncrypt))
		{
			result = true;
		}

		else if (doc.getElementsByTagName("Cards") == null)
		{
			result = true;
		}
		else
		{
			result = false;
		}
		return result;
	}

	/**
	 * @param isGiftCardCreated
	 * @param fXmlFile
	 * @param archiveDestination
	 * @param errorDestination
	 * @throws IOException
	 */
	private void moveFiles(final boolean isGiftCardCreated, final File fXmlFile, final File archiveDestination,
			final File errorDestination) throws IOException
	{
		if (isGiftCardCreated)
		{
			LOG.info("File processed, Moving file to archive folder");
			FileUtils.copyFileToDirectory(fXmlFile, archiveDestination);
		}
		else
		{
			LOG.info("File processed with errors, Moving file to error folder");
			FileUtils.copyFileToDirectory(fXmlFile, errorDestination);
		}
		fXmlFile.delete();
	}

	/**
	 * @param orderID
	 * @return OrderModel
	 */
	private OrderModel findOrderByCode(final String orderID)
	{
		final Map<String, Object> params = new HashMap<String, Object>();
		final String query = " SELECT {pk} FROM {order} WHERE {code}=?orderID ";
		params.put("orderID", orderID);
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		fQuery.addQueryParameters(params);
		fQuery.setResultClassList(Collections.singletonList(OrderModel.class));
		LOG.info("findOrderByCode" + fQuery);
		final SearchResult<OrderModel> searchResult = flexibleSearchService.search(fQuery);
		final List<OrderModel> results = searchResult.getResult();
		if (results != null && results.iterator().hasNext())
		{
			return results.iterator().next();
		}
		return null;
	}

	/**
	 * @param keyGenerator
	 *           the keyGenerator to set
	 */
	public void setKeyGenerator(final KeyGenerator keyGenerator)
	{
		this.keyGenerator = keyGenerator;
	}

}
