package com.rbonestell.myhavelock;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Map.Entry;

import org.stockchart.StockChartActivity;
import org.stockchart.StockChartView.HitTestInfo;
import org.stockchart.StockChartView.ITouchEventListener;
import org.stockchart.core.Area;
import org.stockchart.core.Axis.Side;
import org.stockchart.points.BarPoint;
import org.stockchart.points.StockPoint;
import org.stockchart.series.BarSeries;
import org.stockchart.series.StockSeries;

import com.rbonestell.myhavelock.utils.HavelockFund;
import com.rbonestell.myhavelock.utils.HavelockOHLC;
import com.rbonestell.myhavelock.utils.HavelockPoint;
import com.rbonestell.myhavelock.utils.HavelockRequest;
import com.rbonestell.myhavelock.utils.HavelockTrade;
import com.rbonestell.myhavelock.utils.HavelockTradesResponse;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ChartActivity extends StockChartActivity
{
	
	private StockSeries fPriceSeries;
	private BarSeries fVolumeSeries;
	private HavelockOHLC ohlcData;
	
	// OHLC Dialog Controls
	private AlertDialog dlgOHLC;
	private TextView tvDate, tvOpen, tvHigh, tvLow, tvClose, tvVolume;
	
    private TextView tvMessage, tvMessageHeading, tvChartHeader;
	private ImageView imgAlert;
	private LinearLayout llChartHost;
	
	private DecimalFormat btcdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        Intent myIntent = getIntent();
		HavelockFund selectedFund = (HavelockFund)myIntent.getSerializableExtra("selectedFund");
		String symbol = (selectedFund != null) ? selectedFund.symbol : null;
		String name = (selectedFund != null) ? selectedFund.name : null;
		this.setupActionBar(symbol);
		setContentView(R.layout.activity_chart);
		this.initializeControls(name, symbol);

		new HavelockTask().execute(symbol);
        
    }
    
    @Override
	public void finish()
	{
		super.finish();
		overridePendingTransition( R.anim.slide_in_right, R.anim.slide_out_right );
	}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
	{
        switch (item.getItemId()) {
        case android.R.id.home:
            this.finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private void initializeControls(String fundName, String fundSymbol)
	{
    	btcdf = new DecimalFormat("#.########");
    	btcdf.setMinimumFractionDigits(8);
    	
    	llChartHost = (LinearLayout)findViewById(R.id.llChartLayout);
    	tvChartHeader = (TextView)findViewById(R.id.tvChartHeader);
    	this.getStockChartView().setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    	this.getStockChartView().setTouchEventUpListener(new ChartTouchListener());
    	this.getStockChartView().invalidate();
    	llChartHost.addView(this.getStockChartView());

    	tvMessage = (TextView)findViewById(R.id.tvMessage);
		tvMessageHeading = (TextView)findViewById(R.id.tvMessageHeading);
		imgAlert = (ImageView)findViewById(R.id.imgAlert);
		
		this.initializeOHLCDialog(fundSymbol);
		
		if (fundName != null)
			tvChartHeader.setText(fundName + "\n" + tvChartHeader.getText());
		
	}
    
    private void initializeOHLCDialog(String fundSymbol)
	{
		LayoutInflater inflater = LayoutInflater.from(this);
		View aboutView = inflater.inflate(R.layout.dialog_ohlc, null);
		
		tvDate = (TextView)aboutView.findViewById(R.id.tvDate);
		tvOpen = (TextView)aboutView.findViewById(R.id.tvOpen);
		tvHigh = (TextView)aboutView.findViewById(R.id.tvHigh);
		tvLow = (TextView)aboutView.findViewById(R.id.tvLow);
		tvClose = (TextView)aboutView.findViewById(R.id.tvClose);
		tvVolume = (TextView)aboutView.findViewById(R.id.tvVolume);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
			.setTitle(fundSymbol)
			.setView(aboutView)
	        .setPositiveButton("OK", null);
		
		dlgOHLC = builder.create();
	}
    
    private void setupActionBar(String symbol)
	{
		if (symbol != null)
			this.setTitle(this.getTitle().toString().replace("Fund", symbol));
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
    
    private void displayAlertMessage(String message)
	{
    	llChartHost.setVisibility(View.GONE);
		imgAlert.setImageResource(R.drawable.alert);
		imgAlert.setVisibility(ImageView.VISIBLE);
		tvMessageHeading.setVisibility(TextView.VISIBLE);
		tvMessageHeading.setText(R.string.alert);
		tvMessage.setVisibility(TextView.VISIBLE);
		tvMessage.setText(message);
	}
	
	private void displayLoadingMessage()
	{
		llChartHost.setVisibility(View.GONE);
		imgAlert.setVisibility(ImageView.GONE);
		tvMessageHeading.setVisibility(TextView.VISIBLE);
		tvMessageHeading.setText("Loading");
		tvMessage.setVisibility(TextView.VISIBLE);
		tvMessage.setText(R.string.loading_more_trades);
		tvMessage.setOnClickListener(null);
	}
	
	private void hideMessage()
	{
		llChartHost.setVisibility(View.VISIBLE);
		imgAlert.setVisibility(ImageView.GONE);
		tvMessageHeading.setVisibility(TextView.GONE);
		tvMessage.setVisibility(TextView.GONE);
		Toast.makeText(this, "Pinch to Zoom\nSwipe to Scroll\nTap for Details", Toast.LENGTH_LONG).show();
	}
    
    private void populateChart(HavelockOHLC ohlc)
    {
    	for(Entry<String, HavelockPoint> e : ohlc.chartData.entrySet())
        {
			HavelockPoint p = e.getValue();
            StockPoint sp = new StockPoint();
            sp.setID(p.date);
            sp.setValues(p.open, p.high, p.low, p.close);
            fPriceSeries.getPoints().add(sp);
            BarPoint bp = new BarPoint();
            bp.setID(p.date);
            bp.setValues(new double[] {0.0, p.volume});
            fVolumeSeries.getPoints().add(bp);
        }
    	ohlcData = ohlc;
    	this.hideMessage();
    }
    
    @Override
    protected void initChart()
    {
        fPriceSeries = new StockSeries();
        fPriceSeries.setName("Price");
        fPriceSeries.setYAxisSide(Side.LEFT);
        	        
        fVolumeSeries = new BarSeries();
        fVolumeSeries.setName("volume");
        fVolumeSeries.setYAxisSide(Side.LEFT);
        
        Area a = this.getStockChartView().addArea();
        a.getLeftAxis().setDefaultLabelFormat("#.#####");
        a.getAppearance().setOutlineWidth(0);
        a.getAppearance().setOutlineColor(Color.TRANSPARENT);
        a.setTitle("Price History");
        a.setAutoHeight(false);
        a.setHeightInPercents(.75f);
        a.getSeries().add(fPriceSeries);
        a.getBottomAxis().getAxisRange().setMaxMinViewLength(30, 7);
        a.getBottomAxis().getAxisRange().expandAutoValues(30, 7);
        a.getBottomAxis().getAxisRange().setViewValues(30, 21);
        a.getBottomAxis().getAxisRange().setZoomable(true);
        a.getBottomAxis().getAxisRange().setMovable(true);
					
        Area b = this.getStockChartView().addArea();
        b.getAppearance().setOutlineWidth(0);
        b.getAppearance().setOutlineColor(Color.TRANSPARENT);
        b.setTitle("Volume History");
        b.setAutoHeight(false);
        b.setHeightInPercents(.25f);
        b.getSeries().add(fVolumeSeries);
        b.getBottomAxis().getAxisRange().setMaxMinViewLength(30, 7);
        b.getBottomAxis().getAxisRange().expandAutoValues(30, 7);
        b.getBottomAxis().getAxisRange().setViewValues(30, 21);
        b.getBottomAxis().getAxisRange().setZoomable(true);
        b.getBottomAxis().getAxisRange().setMovable(true);

    }

    @Override
    protected void restoreChart() 
    {
    	fPriceSeries = (StockSeries) this.getStockChartView().findSeriesByName("Price");
    	fVolumeSeries = (BarSeries) this.getStockChartView().findSeriesByName("Volume");
    }
    
	private String getTodayString()
	{
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("GMT-5"));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00", getResources().getConfiguration().locale);
		String today = sdf.format(cal.getTime());
		return today;
	}
	
	private String getLastMonthString()
	{
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT-5"));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00", getResources().getConfiguration().locale);
		cal.add(Calendar.DAY_OF_YEAR, -30);
		String delorean = sdf.format(cal.getTime());
		cal.add(Calendar.DAY_OF_YEAR, 30);
		return delorean;
	}
    
	private void handleHavelockResponse(HavelockOHLC ohlc)
	{
		if (ohlc != null)
		{
			this.populateChart(ohlc);
		}
		else
		{
			this.displayAlertMessage(getString(R.string.trades_connection_error));
		}
	}
	
	// StockChartView Touch Listener
	private final class ChartTouchListener implements ITouchEventListener
	{
		@Override
		public void onTouchEvent(MotionEvent arg0)
		{
			HitTestInfo h = getStockChartView().getHitTestInfo(arg0.getX(), arg0.getY());
			if ((h.points != null) && (!h.points.isEmpty()))
			{
				String id = (String)h.points.firstEntry().getValue().getID();
				HavelockPoint poi = ohlcData.chartData.get(id);
				ChartActivity.this.tvDate.setText(id);
				ChartActivity.this.tvOpen.setText("\u0E3F" + btcdf.format(poi.open));
				ChartActivity.this.tvHigh.setText("\u0E3F" + btcdf.format(poi.high));
				ChartActivity.this.tvLow.setText("\u0E3F" + btcdf.format(poi.low));
				ChartActivity.this.tvClose.setText("\u0E3F" + btcdf.format(poi.close));
				ChartActivity.this.tvVolume.setText("\u0E3F" + btcdf.format(poi.volume));
				dlgOHLC.show();
			}
		}
	}

	// Havelock Trades Loading Task
	private class HavelockTask extends AsyncTask<String, Integer, HavelockOHLC>
	{
		@Override
		protected void onPreExecute()
		{
			ChartActivity.this.displayLoadingMessage();
			super.onPreExecute();
		}

		@Override
		protected HavelockOHLC doInBackground(String... params)
		{
			try
			{
				String back = ChartActivity.this.getLastMonthString();
				String now = ChartActivity.this.getTodayString();
				HavelockTradesResponse resp = HavelockRequest.getTrades(params[0], back, now);
				return this.mapDataToOHLC(resp);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return null;
		}
		
		private HavelockOHLC mapDataToOHLC(HavelockTradesResponse resp)
		{
			HavelockOHLC ohlc = new HavelockOHLC();
			for (HavelockTrade t : resp.trades)
				ohlc.addData(t.d.substring(0, 10), t.p);
			return ohlc;
		}

		@Override
		protected void onProgressUpdate(Integer... values)
		{
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(HavelockOHLC result)
		{
			super.onPostExecute(result);
			ChartActivity.this.handleHavelockResponse(result);
		}

	}

}