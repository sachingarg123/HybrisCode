/**
 *
 */
package de.hybris.wooliesegiftcard.facades.dto;

/**
 * @author 264343
 *
 */
public class ResponseDTO
{

	private String responseMessage;
	private String responseCode;
	private String errorResponse;


	/**
	 * @return the responseMessage
	 */
	public String getResponseMessage()
	{
		return responseMessage;
	}

	/**
	 * @param responseMessage
	 *           the responseMessage to set
	 */
	public void setResponseMessage(final String responseMessage)
	{
		this.responseMessage = responseMessage;
	}

	/**
	 * @return the responseCode
	 */
	public String getResponseCode()
	{
		return responseCode;
	}

	/**
	 * @param responseCode
	 *           the responseCode to set
	 */
	public void setResponseCode(final String responseCode)
	{
		this.responseCode = responseCode;
	}

	/**
	 * @return the errorResponse
	 */
	public String getErrorResponse()
	{
		return errorResponse;
	}

	/**
	 * @param errorResponse
	 *           the errorResponse to set
	 */
	public void setErrorResponse(final String errorResponse)
	{
		this.errorResponse = errorResponse;
	}


}
