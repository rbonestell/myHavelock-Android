package com.rbonestell.myhavelock.utils;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/***
 * Havelock Response
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class HavelockPortfolioResponse implements Serializable
{
	private static final long serialVersionUID = 5413941472295620918L;
	public String status;
	public String message;
	public String apirate;
	public HavelockFund[] portfolio;
}
