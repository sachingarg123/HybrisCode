/**
 *
 */
package de.hybris.wooliesegiftcard.core;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;


/**
 * @author 653154
 *
 */
public class EncryptionUtils
{

	private static final Logger LOG = Logger.getLogger(EncryptionUtils.class);
	private static final String cipherTransformation = "AES/CBC/PKCS5PADDING";
	private static final String aesEncryptionAlgorithem = "AES";

	private EncryptionUtils()
	{

	}

	/**
	 * Method for Encrypt Plain String Data
	 *
	 * @param plainText
	 * @param encryptionKey
	 *           (TODO: should be type byte[])
	 * @return encryptedText (includes IV and is base 64 encoded)
	 */
	public static String encrypt(final String plainText, final byte[] encryptionKey)
	{
		String encryptedText = "";
		try
		{

			final Cipher cipher = Cipher.getInstance(cipherTransformation);
			final SecretKeySpec secretKey = new SecretKeySpec(encryptionKey, aesEncryptionAlgorithem);

			final byte[] iv = new byte[16]; // AES always has 16 byte IV (regardless of key size)
			final SecureRandom random = new SecureRandom();
			random.nextBytes(iv); // IVs *must* be generated by SecureRandom()
			final IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

			// encrypt
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
			final byte[] cipherText = cipher.doFinal(plainText.getBytes("UTF8"));

			// full ciphertext consists of IV followed by cipherText above
			final byte[] IVAndCiphertext = new byte[16 + cipherText.length];
			System.arraycopy(iv, 0, IVAndCiphertext, 0, 16);
			System.arraycopy(cipherText, 0, IVAndCiphertext, 16, cipherText.length);

			// base64 encode result
			final Base64.Encoder encoder = Base64.getEncoder();
			encryptedText = encoder.encodeToString(IVAndCiphertext);

		}
		catch (final Exception E)
		{
			LOG.info(E);
			LOG.info("Encrypt Exception : " + E.getMessage());
		}
		return encryptedText;
	}

	/**
	 * Method For Get encryptedText and Decrypted provided String
	 *
	 * @param encryptedText
	 *           (IV followed by ciphertext)
	 * @param encryptionKey
	 *           (TODO: should be type byte[])
	 * @return decryptedText
	 */
	public static String decrypt(final String encryptedText, final byte[] encryptionKey)
	{
		String decryptedText = "";
		try
		{
			final Cipher cipher = Cipher.getInstance(cipherTransformation);
			final SecretKeySpec secretKey = new SecretKeySpec(encryptionKey, aesEncryptionAlgorithem);

			// Decode encryptedText
			final Base64.Decoder decoder = Base64.getDecoder();
			final byte[] IVAndCiphertext = decoder.decode(encryptedText.getBytes("UTF8"));

			// TODO: Should check that the IVAndCiphertext is a multiple of 16 bytes and length is at least 2*16,
			// Otherwise throw exception (invalid encryptedText length)

			// Extract IV
			final byte[] iv = new byte[16];
			System.arraycopy(IVAndCiphertext, 0, iv, 0, 16);
			final IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

			// Extract ciphertext
			final int encryptedSize = IVAndCiphertext.length - 16; // exclude 16 byte IV
			final byte[] encryptedBytes = new byte[encryptedSize];
			System.arraycopy(IVAndCiphertext, 16, encryptedBytes, 0, encryptedSize);

			cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
			decryptedText = new String(cipher.doFinal(encryptedBytes), "UTF-8");

		}
		catch (final Exception E)
		{
			LOG.info(E);
			LOG.info("decrypt Exception : " + E.getMessage());
			try
			{
				throw E;
			}
			catch (final Exception e)
			{
				LOG.info(e);
			}
		}
		return decryptedText;
	}

}