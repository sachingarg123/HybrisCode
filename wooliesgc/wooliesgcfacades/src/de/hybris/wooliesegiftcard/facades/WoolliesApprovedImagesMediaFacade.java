/**
 *
 */
package de.hybris.wooliesegiftcard.facades;

import de.hybris.platform.cmsfacades.data.MediaData;
import de.hybris.platform.cmsfacades.media.MediaFacade;
import de.hybris.wooliesegiftcard.exception.WoolliesMediaImagesException;


/**
 * @author 264343 This interface is used to maintain approve media images
 */
public interface WoolliesApprovedImagesMediaFacade extends MediaFacade
{
	/**
	 * This method is used to get the approved images
	 *
	 * @param userId
	 *           the parameter value to be used
	 * @return the mediaData the parameter used to return
	 * @throws WoolliesMediaImagesException
	 *            used to throw exception
	 */
	MediaData getApprovedImages(String userId) throws WoolliesMediaImagesException;

	/**
	 * This method is used to encode the image
	 *
	 * @param url
	 *           the parameter value to be used
	 * @return Image the parameter used to return
	 * @throws WoolliesMediaImagesException
	 *            used to throw exception
	 */
	String imageEncoding(String url) throws WoolliesMediaImagesException;

}
