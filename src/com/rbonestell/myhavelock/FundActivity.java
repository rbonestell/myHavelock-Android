package com.rbonestell.myhavelock;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import com.rbonestell.myhavelock.utils.HavelockFund;
import com.rbonestell.myhavelock.utils.HavelockRequest;
import com.rbonestell.myhavelock.utils.HavelockTrade;
import com.rbonestell.myhavelock.utils.HavelockTradesResponse;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FundActivity extends Activity
{
	
	private static int MYHAVELOCK_CHART_INTENT = 3;
	
	private HavelockFund _currentFund;
	private TextView tvFundHeader, tvBookVal, tvMarketVal, tvQuantity, tvFundMessage, tvFundMessageHeading, tvFundSymbol;
	private ListView lvTrades;
	private RelativeLayout rlChartButton, rlAlert;
	private ImageView imgFundAlert, imgFundUpDown;
	private DecimalFormat btcdf;

	@Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        Intent myIntent = getIntent();
		_currentFund = (HavelockFund)myIntent.getSerializableExtra("selectedFund");
		this.setupActionBar();
		setContentView(R.layout.activity_fund);
		this.initializeControls();
		
		new HavelockTask().execute(_currentFund.symbol);

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
    
    private void setupActionBar()
	{
		if ((_currentFund != null) && (_currentFund.symbol != null))
			this.setTitle(this.getTitle().toString().replace("Fund", _currentFund.symbol));
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
    
    private void initializeControls()
	{
    	btcdf = new DecimalFormat("#.########");
    	btcdf.setMinimumFractionDigits(8);
    	
    	tvFundHeader = (TextView)findViewById(R.id.tvFundHeader);
    	tvFundHeader.setText(_currentFund.name);
    	
    	tvFundSymbol = (TextView)findViewById(R.id.tvFundSymbol);
    	tvFundSymbol.setText(_currentFund.symbol);
    	
    	tvQuantity = (TextView)findViewById(R.id.tvQuantity);
    	tvQuantity.setText(_currentFund.quantity + " x " + _currentFund.symbol);
    	
    	tvBookVal = (TextView)findViewById(R.id.tvBookVal);
    	tvBookVal.setText("\u0E3F" + btcdf.format(_currentFund.bookvalue));
    	
    	tvMarketVal = (TextView)findViewById(R.id.tvMarketVal);
    	tvMarketVal.setText("\u0E3F" + btcdf.format(_currentFund.marketvalue));
    	
    	tvFundMessage = (TextView)findViewById(R.id.tvFundMessage);
		tvFundMessageHeading = (TextView)findViewById(R.id.tvFundMessageHeading);
		imgFundAlert = (ImageView)findViewById(R.id.imgFundAlert);
		
		imgFundUpDown = (ImageView)findViewById(R.id.imgFundUpDown);
		int arrow = (_currentFund.marketvalue > _currentFund.bookvalue) ? R.drawable.uparrow : (_currentFund.marketvalue == _currentFund.bookvalue) ? android.R.color.transparent : R.drawable.downarrow;
		imgFundUpDown.setImageResource(arrow);
		
    	lvTrades = (ListView)findViewById(R.id.lvTrades);
    	
    	rlAlert = (RelativeLayout)findViewById(R.id.rlAlert);
    	rlChartButton = (RelativeLayout)findViewById(R.id.rlChartButton);
    	
    	rlChartButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				Intent chartIntent = new Intent(FundActivity.this, ChartActivity.class);
				chartIntent.putExtra("selectedFund", _currentFund);
				startActivityForResult(chartIntent, MYHAVELOCK_CHART_INTENT, null);
		        overridePendingTransition( R.anim.slide_in_left, R.anim.slide_out_left );
			}
		});
		
	}
    
    private void displayAlertMessage(String message)
	{
    	lvTrades.setVisibility(View.GONE);
    	rlAlert.setVisibility(View.VISIBLE);
		imgFundAlert.setImageResource(R.drawable.alert);
		imgFundAlert.setVisibility(View.VISIBLE);
		tvFundMessageHeading.setVisibility(View.VISIBLE);
		tvFundMessageHeading.setText(R.string.alert);
		tvFundMessage.setVisibility(View.VISIBLE);
		tvFundMessage.setText(message);
	}
	
	private void displayLoadingMessage()
	{
		lvTrades.setVisibility(View.GONE);
		imgFundAlert.setVisibility(View.GONE);
		rlAlert.setVisibility(View.VISIBLE);
		tvFundMessageHeading.setVisibility(View.VISIBLE);
		tvFundMessageHeading.setText("Loading");
		tvFundMessage.setVisibility(View.VISIBLE);
		tvFundMessage.setText(R.string.loading_trades);
		tvFundMessage.setOnClickListener(null);
	}
	
	private void displayNoTradesMessage()
	{
		lvTrades.setVisibility(View.GONE);
		rlAlert.setVisibility(View.VISIBLE);
		imgFundAlert.setImageResource(R.drawable.alert);
		imgFundAlert.setVisibility(View.VISIBLE);
		tvFundMessageHeading.setVisibility(View.VISIBLE);
		tvFundMessageHeading.setText(R.string.no_trades);
		tvFundMessage.setVisibility(View.VISIBLE);
		tvFundMessage.setText(R.string.no_trades_found_for_last_24hrs);
	}
	
	private void hideMessage()
	{
		lvTrades.setVisibility(View.VISIBLE);
		imgFundAlert.setVisibility(ImageView.GONE);
		tvFundMessageHeading.setVisibility(TextView.GONE);
		tvFundMessage.setVisibility(TextView.GONE);
		rlAlert.setVisibility(TextView.GONE);
	}
	
	private String getYesterdayString()
	{
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT-5"));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", getResources().getConfiguration().locale);
		cal.add(Calendar.DAY_OF_YEAR, -1);
		String delorean = sdf.format(cal.getTime());
		cal.add(Calendar.DAY_OF_YEAR, 1);
		return delorean;
	}
	
	private void handleHavelockResponse(HavelockTradesResponse trades)
	{
		if (trades != null)
		{
			this.populateTrades(trades);
		}
		else
		{
			this.displayAlertMessage(getString(R.string.trades_connection_error));
		}
	}
	
	private void populateTrades(HavelockTradesResponse trades)
    {
    	//array list 'em
		if ((trades.trades != null) && (trades.trades.length > 0))
		{
			String[] tradeStrings = new String[trades.trades.length];
	
			int i = tradeStrings.length-1;
			for (HavelockTrade t : trades.trades)
			{
				tradeStrings[i] = t.q + " x " + _currentFund.symbol + " @ \u0E3F" + btcdf.format(t.p);
				i--;
			}
			
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.listitem_trade, tradeStrings);
			lvTrades.setAdapter(adapter);
			this.hideMessage();
		}
		else
		{
			displayNoTradesMessage();
		}
    }

	// Havelock Trades Loading Task
	private class HavelockTask extends AsyncTask<String, Integer, HavelockTradesResponse>
	{
		@Override
		protected void onPreExecute()
		{
			FundActivity.this.displayLoadingMessage();
			super.onPreExecute();
		}

		@Override
		protected HavelockTradesResponse doInBackground(String... params)
		{
			try
			{
				String back = FundActivity.this.getYesterdayString();
				return HavelockRequest.getTrades(params[0], back, null);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values)
		{
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(HavelockTradesResponse result)
		{
			super.onPostExecute(result);
			FundActivity.this.handleHavelockResponse(result);
		}

	}

}
