package com.rbonestell.myhavelock.utils;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/***
 * Havelock Trade
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class HavelockTrade implements Serializable
{
	private static final long serialVersionUID = 8714726763827557262L;
	public String d;
	public double p;
	public int q;
}
