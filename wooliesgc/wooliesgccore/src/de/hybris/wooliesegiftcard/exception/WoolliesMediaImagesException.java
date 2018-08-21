/**
 *
 */
package de.hybris.wooliesegiftcard.exception;




/**
 * @author 264343 This class is to throw user defined MediaImagesException
 *
 */
public class WoolliesMediaImagesException extends Exception
{

	/**
	 * @param message
	 */

	public WoolliesMediaImagesException()
	{
		super("No Approved Images Found");
	}

}
