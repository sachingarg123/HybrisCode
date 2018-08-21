/**
 *
 */
package de.hybris.wooliesegiftcard.core.fraud.service;

import de.hybris.wooliesegiftcard.facades.dto.FraudRequestCron;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;



/**
 * @author 669567
 *
 */
public interface FraudOrderStatusService
{

	/**
	 * Method is utilized to fetch the current date in EPOCH format.It will calculate the current time and send the same
	 * to APIGEE in EPOCH format
	 *
	 * @param dateFormat
	 *           dateFormat
	 * @param zone
	 *           zone
	 * @param localDate
	 *           localDate
	 * @return String
	 */
	public String getTimeWithZone(final String dateFormat, final String zone, final Date localDate);

	/**
	 * This Method will create the JSON structure for sending <Fraud> details to Cronjob, which will be triggered at
	 * specific time interval to send these data to DIGIPAY
	 *
	 * @param startTimeInString
	 *           startTimeInString
	 * @param currentTime
	 *           currentTime
	 * @return FraudRequestCron FraudRequestCron
	 */
	public FraudRequestCron createFraudCronRequest(final String startTimeInString, final String currentTime);

	/**
	 * This method is responsible to create the request pay load thats needs to be sent to APIGEE
	 *
	 * @param fraudRequestCron
	 *           fraudRequestCron
	 * @return boolean boolean
	 * @throws KeyManagementException
	 *            KeyManagementException
	 * @throws NoSuchAlgorithmException
	 *            NoSuchAlgorithmException
	 */
	public boolean sendFraudRequestApigee(final FraudRequestCron fraudRequestCron)
			throws KeyManagementException, NoSuchAlgorithmException;

}
