/**
 *
 */
package de.hybris.wooliesegiftcard.core.utility;

import de.hybris.wooliesegiftcard.core.constants.WooliesgcCoreConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;


/**
 * @author 653930
 *
 */
public class WooliesCoreCustomerUtility
{
	private WooliesCoreCustomerUtility()
	{

	}

	private static final Logger LOG = Logger.getLogger(WooliesCoreCustomerUtility.class);

	/**
	 * Payment related method
	 *
	 * @param targetURL
	 *           targetURL
	 * @param xApiKey
	 *           xApiKey
	 * @param env
	 *           xApiKey
	 * @param connTimeOut
	 *           connTimeOut
	 * @param readTimeOut
	 *           readTimeOut
	 * @param payLoad
	 *           payLoad
	 * @return HttpsURLConnection
	 * @throws IOException
	 *            IOException
	 * @throws NoSuchAlgorithmException
	 *            NoSuchAlgorithmException
	 * @throws KeyManagementException
	 *            KeyManagementException
	 */
	public static HttpsURLConnection postApigee(final String targetURL, final String xApiKey, final String env,
			final int connTimeOut, final int readTimeOut, final Object payLoad)
			throws IOException, NoSuchAlgorithmException, KeyManagementException
	{
		HttpsURLConnection httpConnection = null;
		final URL targetUrl = new URL(targetURL);
		final ObjectMapper mapper = new ObjectMapper();



		if (env.equalsIgnoreCase(WooliesgcCoreConstants.ENV_LOCAL))
		{
			final InetSocketAddress proxyInet = new InetSocketAddress(WooliesgcCoreConstants.PROXY_URL, 6050);
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
		httpConnection.setRequestMethod(WooliesgcCoreConstants.METHOD_POST);
		httpConnection.setRequestProperty(WooliesgcCoreConstants.CONTENT_TYPE, WooliesgcCoreConstants.APPLICATION_JSON);

		httpConnection.setRequestProperty(WooliesgcCoreConstants.API_KEY, xApiKey);
		if (env.equalsIgnoreCase(WooliesgcCoreConstants.ENV_LOCAL))
		{
			// Set up a Trust all manager
			final TrustManager[] trustAllCerts = new TrustManager[]
			{ new X509TrustManager()
			{
				@Override
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
			final SSLContext sc = SSLContext.getInstance(WooliesgcCoreConstants.TLS);
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			httpConnection.setSSLSocketFactory(sc.getSocketFactory());
			final HostnameVerifier allHostsValid = new HostnameVerifier()
			{

				@Override
				public boolean verify(final String hostname, final SSLSession session)
				{
					return true;
				}
			};
			httpConnection.setHostnameVerifier(allHostsValid);

		}

		final String input = mapper.writeValueAsString(payLoad);
		LOG.debug("Payment Completion payload " + input);
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

	/**
	 * @param httpConnection
	 *           opens a https connection
	 * @return returns a StringBuilder
	 * @throws IOException
	 *            throws this exception
	 */
	public static StringBuilder successfulResponse(final HttpsURLConnection httpConnection) throws IOException
	{
		final BufferedReader responseBuffer = new BufferedReader(new InputStreamReader((httpConnection.getInputStream())));
		final StringBuilder builder = new StringBuilder();
		String output;
		LOG.debug("Output from Server(APIGEE):\n");
		while ((output = responseBuffer.readLine()) != null)
		{
			builder.append(output);
		}
		LOG.debug("Sucess response for the Request" + builder);
		responseBuffer.close();
		httpConnection.disconnect();
		return builder;
	}
}
