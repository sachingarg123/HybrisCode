/**
 *
 */
package de.hybris.wooliesegiftcard.utility;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;


/**
 * @author 669567
 *
 */
public class WooliesEncoderDecoder
{
	private WooliesEncoderDecoder()
	{
	}

	private static final Logger LOG = Logger.getLogger(WooliesEncoderDecoder.class);


	public static String encode(final String data) throws IOException
	{
		return encode(data.getBytes());
	}

	public static String encode(final byte[] data) throws IOException
	{
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (

				InputStream is = new ByteArrayInputStream(data);
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				OutputStreamWriter writer = new OutputStreamWriter(new GZIPOutputStream(out));)
		{
			final char[] charBuffer = new char[data.length];
			while (br.read(charBuffer) != -1)
			{
				writer.write(charBuffer);
			}
		}
		finally
		{
			close(out);
		}

		return new String(Base64.encodeBase64(out.toByteArray()));
	}

	public static void close(final InputStream stream)
	{
		try
		{
			if (stream != null)
			{
				stream.close();
			}
		}
		catch (final Exception e)
		{
			LOG.info(e);
			LOG.error("Input Stream not closed");
		}
	}

	public static void close(final OutputStream stream)
	{
		try
		{
			if (stream != null)
			{
				stream.close();
			}
		}
		catch (final Exception localException)
		{
			LOG.info(localException);
			LOG.error("Output Stream not closed");
		}
	}

	public static void close(final Reader reader)
	{
		try
		{
			if (reader != null)
			{
				reader.close();
			}
		}
		catch (final Exception localExceptions)
		{
			LOG.info(localExceptions);
			LOG.error("reader not closed");
		}
	}
}
