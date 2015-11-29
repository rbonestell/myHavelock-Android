package com.rbonestell.myhavelock.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HavelockRequest
{

	private static String havelockURL = "https://www.havelockinvestments.com/api/index.php";
	
	public static HavelockPortfolioResponse getPortfolio(String apiKey)
	{
		String params = "cmd=portfolio&key=" + apiKey;
		String rawResponse = WSClient.sendRequest(havelockURL, WSClient.RequestType.POST, params);
		HavelockPortfolioResponse resp = null;
		try
		{
			ObjectMapper mapper = new ObjectMapper();
			resp = (HavelockPortfolioResponse)mapper.readValue(rawResponse, HavelockPortfolioResponse.class);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return resp;
	}
	
	public static HavelockTradesResponse getTrades(String symbol, String startDate, String endDate)
	{
		String params = "cmd=trades&symbol=" + symbol;
		if (startDate != null)
			params += "&dtstart=" + startDate;
		if (endDate != null)
			params += "&dtend=" + endDate;
		String rawResponse = WSClient.sendRequest(havelockURL, WSClient.RequestType.POST, params);
		HavelockTradesResponse resp = null;
		try
		{
			ObjectMapper mapper = new ObjectMapper();
			resp = (HavelockTradesResponse)mapper.readValue(rawResponse, HavelockTradesResponse.class);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return resp;
	}
	
}
