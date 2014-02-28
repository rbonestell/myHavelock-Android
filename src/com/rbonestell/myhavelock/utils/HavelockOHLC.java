package com.rbonestell.myhavelock.utils;

import java.util.LinkedHashMap;

public class HavelockOHLC
{
	public LinkedHashMap<String, HavelockPoint> chartData;
	
	private String lastDate; 
	
	public HavelockOHLC()
	{
		chartData = new LinkedHashMap<String, HavelockPoint>();
	}
	
	public void addData(String date, double value)
	{
		HavelockPoint p = chartData.get(date);
		if (p != null)
		{
			if (value > p.high)
				p.high = value;
			if (value < p.low)
				p.low = value;
			p.close = value;
			p.volume += value;
		}
		else
		{
			p = new HavelockPoint();
			if (lastDate != null)
			{
				HavelockPoint lp = chartData.get(lastDate);
				p.open = lp.close;
			}
			else
			{
				p.open = value;
			}
			lastDate = date;
			p.date = date;
			
			p.high = value;
			p.low = value;
			p.close = value;
			p.volume += value;
			chartData.put(date, p);
		}
	}
	
}
