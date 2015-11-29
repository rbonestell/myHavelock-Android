package com.rbonestell.myhavelock.utils;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/***
 * Havelock Fund
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class HavelockFund implements Serializable
{
	private static final long serialVersionUID = 6008384877541998343L;
	public String symbol;
	public String name;
	public String quantity;
	public String quantityescrow;
	public String lastprice;
	public double bookvalue;
	public double marketvalue;
}
