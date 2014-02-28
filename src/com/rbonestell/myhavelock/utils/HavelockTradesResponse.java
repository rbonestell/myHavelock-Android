package com.rbonestell.myhavelock.utils;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/***
 * Havelock Response
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class HavelockTradesResponse implements Serializable
{
	private static final long serialVersionUID = -5667008493910991559L;
	public String status;
	public String message;
	public String apirate;
	public HavelockTrade[] trades;
}
