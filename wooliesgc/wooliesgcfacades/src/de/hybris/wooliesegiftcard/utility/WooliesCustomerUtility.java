/**
 *
 */
package de.hybris.wooliesegiftcard.utility;


import de.hybris.wooliesegiftcard.facades.constants.WooliesgcFacadesConstants;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.SocketException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;


/**
 * @author 668982
 *
 *         This is Utility class for customer details
 */
public class WooliesCustomerUtility
{

	private WooliesCustomerUtility()
	{

	}

	private static final Logger LOG = Logger.getLogger(WooliesCustomerUtility.class);





	/**
	 * @param dateFormat
	 * @param zone
	 * @param localDate
	 * @return String
	 */
	public static String getTimeWithZone(final String dateFormat, final String zone, final Date localDate)
	{

		final SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
		final TimeZone timeZone = TimeZone.getTimeZone(zone);
		formatter.setTimeZone(timeZone);
		return formatter.format(localDate);
	}

	/**
	 * @param targetURL
	 * @param xApiKey
	 * @param env
	 * @param connTimeOut
	 * @param readTimeOut
	 * @param payLoad
	 * @param accessToken
	 * @return HttpsURLConnection
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws SocketException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	public static HttpsURLConnection postApigee(final String targetURL, final String xApiKey, final String env,
			final int connTimeOut, final int readTimeOut, final Object payLoad, final String accessToken)
			throws IOException, NoSuchAlgorithmException, KeyManagementException
	{
		HttpsURLConnection httpConnection = null;


		final URL targetUrl = new URL(targetURL);
		final ObjectMapper mapper = new ObjectMapper();



		if (env.equalsIgnoreCase(WooliesgcFacadesConstants.ENV_LOCAL))
		{
			final InetSocketAddress proxyInet = new InetSocketAddress(WooliesgcFacadesConstants.PROXY_URL, 6050);
			final Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyInet);
			httpConnection = (HttpsURLConnection) targetUrl.openConnection(proxy);
		}
		else
		{
			httpConnection = (HttpsURLConnection) targetUrl.openConnection();
		}
		httpConnection.setConnectTimeout(connTimeOut);
		httpConnection.setReadTimeout(readTimeOut);
		httpConnection.setDoOutput(true);
		httpConnection.setRequestMethod(WooliesgcFacadesConstants.METHOD_POST);
		httpConnection.setRequestProperty(WooliesgcFacadesConstants.CONTENT_TYPE, WooliesgcFacadesConstants.APPLICATION_JSON);

		httpConnection.setRequestProperty(WooliesgcFacadesConstants.API_KEY, xApiKey);

		httpConnection.setRequestProperty(WooliesgcFacadesConstants.AUTHORIZATION,
				WooliesgcFacadesConstants.BEARER + " " + accessToken);
		if (env.equalsIgnoreCase(WooliesgcFacadesConstants.ENV_LOCAL))
		{
			// Set up a Trust all manager
			final TrustManager[] trustAllCerts = new TrustManager[]
			{ new X509TrustManager()
			{

				public java.security.cert.X509Certificate[] getAcceptedIssuers()
				{
					return new java.security.cert.X509Certificate[0];
				}

				@Override
				public void checkClientTrusted(final java.security.cert.X509Certificate[] certs, final String authType)
				{
					//
				}

				@Override
				public void checkServerTrusted(final java.security.cert.X509Certificate[] certs, final String authType)
				{
					//
				}
			} };
			final SSLContext sc = SSLContext.getInstance(WooliesgcFacadesConstants.TLS);
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			httpConnection.setSSLSocketFactory(sc.getSocketFactory());
			final HostnameVerifier allHostsValid = new HostnameVerifier()
			{

				public boolean verify(final String hostname, final SSLSession session)
				{
					return true;
				}
			};
			httpConnection.setHostnameVerifier(allHostsValid);

		}

		final String input = mapper.writeValueAsString(payLoad);
		LOG.debug("Request payload " + input);
		OutputStream outputStream = null;
		try
		{
			outputStream = httpConnection.getOutputStream();
			outputStream.write(input.getBytes());
			outputStream.flush();
		}
		finally
		{
			if (null != outputStream)
			{
				outputStream.close();
			}
		}
		return httpConnection;

	}

}