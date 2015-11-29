package com.rbonestell.myhavelock.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/***
 * HTTP web service client class
 * @author Robert Bonestell
 */
public class WSClient
{
	
	public enum RequestType { GET, POST, UPDATE, DELETE };
	
	/***
	 * Send web request
	 * @param targetURL Target web service URL including query parameters
	 * @return Raw JSON response
	 */
	public static String sendRequest(String targetURL, RequestType reqType, String urlParameters)
	{
        String line;
        String response = "";
        try
        {
        	URL url = new URL(targetURL);
        	HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
        	conn.setHostnameVerifier(DO_NOT_VERIFY);
			conn.setRequestMethod(reqType.toString());
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("charset", "utf-8");
			conn.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(15000);
			conn.setDoInput(true);
			
			if (reqType == RequestType.POST)
			{
				conn.setDoOutput(true);
				OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
				writer.write(urlParameters);
				writer.close();
			}
			   
			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK)
			{
				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				
				while ((line = rd.readLine()) != null)
					response += line;
				
				rd.close();	
			}
			else
			{
				response = "Error: " + conn.getResponseCode() + " " + conn.getResponseMessage();
			}
			
			conn.disconnect();
		}
        catch (Exception e)
        {
			response = "Exception: " + e.getMessage();
		}
        return response;
	}
	
	final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};

}
