package com.rbonestell.myhavelock;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import com.rbonestell.myhavelock.utils.BitcoinIntegration;
import com.rbonestell.myhavelock.utils.HavelockFund;
import com.rbonestell.myhavelock.utils.HavelockPortfolioResponse;
import com.rbonestell.myhavelock.utils.HavelockRequest;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MainActivity extends ListActivity
{
	
	private static int MYHAVELOCK_CONFIGURATION_INTENT = 1;
	private static int MYHAVELOCK_DETAILS_INTENT = 2;

	private Menu optionsMenu;
	private SimpleAdapter listAdapter;
	private HavelockPortfolioResponse havelockResp;
	private double portfolioValue = 0;
	private TextView tvPortfolioValue, tvMessage, tvMessageHeading;
	private PullToRefreshListView lstFunds;
	private ImageView imgAlert;
	private String apiKey;
	private AlertDialog dlgAbout;
	private DecimalFormat btcdf = new DecimalFormat("#.########");

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initializeControls();
		loadSavedAPIKey();
		displayLoadingMessage();
		refreshPortfolio();
	}

	private void refreshPortfolio()
	{
		lstFunds.setRefreshing();
		new HavelockTask().execute(apiKey);
	}

	private void loadSavedAPIKey()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		apiKey = settings.getString("apikey", "");
	}

	private void initializeControls()
	{
		tvMessage = (TextView)findViewById(R.id.tvMessage);
		tvMessageHeading = (TextView)findViewById(R.id.tvMessageHeading);
		tvPortfolioValue = (TextView)findViewById(R.id.tvPortfolioValue);
		tvPortfolioValue.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				showPortfolioSummary();
			}
		});
		lstFunds = (PullToRefreshListView)findViewById(R.id.list);
		imgAlert = (ImageView)findViewById(R.id.imgAlert);	
		lstFunds.getLoadingLayoutProxy(true, true).setRefreshingLabel(getString(R.string.refreshing_portfolio));
		lstFunds.setOnRefreshListener(new OnRefreshListener<ListView>() {
		    @Override
		    public void onRefresh(PullToRefreshBase<ListView> refreshView)
		    {
		        refreshPortfolio();
		    }
		});
	}
	
	private void showPortfolioSummary()
	{
		if ((havelockResp != null) && (havelockResp.portfolio != null))
		{
			double book = 0;
			double market = 0;
			
			for (HavelockFund f : havelockResp.portfolio)
			{
				book += f.bookvalue;
				market += f.marketvalue;
			}
			
			double diff = Math.abs(book - market);
			double delta = diff/book * 100;
			DecimalFormat df = new DecimalFormat("#.####");
			String sdelta = ((market < book) ? "Loss: " : "Profit: ") + df.format(delta) + "%";
			
			btcdf.setMinimumFractionDigits(8);
			
			String message = "Book: \u0E3F" + btcdf.format(book) + "\n" +
					"Market: \u0E3F" + btcdf.format(market) + "\n" +
					sdelta;
	
			new AlertDialog.Builder(this)
			   .setTitle("Portfolio Summary")
			   .setMessage(message)
			   .setPositiveButton("OK", null)
			   .show();
		}
	}

	private void initializeAboutDialog()
	{
		LayoutInflater inflater = LayoutInflater.from(this);
		View aboutView = inflater.inflate(R.layout.dialog_about, null);
		
		ImageView imgDonate = (ImageView)aboutView.findViewById(R.id.imgDonate);
		imgDonate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				BitcoinIntegration.request(MainActivity.this, "1JXgV2fAGyMRfNoLH5U6LL4xLCbXBWn3fF");
			}
		});
		
		String version;
		try 
		{
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			version = "v" + pInfo.versionName;
			
		} catch (NameNotFoundException e) {
			version = "for Android";
		}
		
		TextView tvVersion = (TextView)aboutView.findViewById(R.id.tvVersion);
		tvVersion.setText("myHavelock " + version);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
		   .setTitle("About myHavelock")
		   .setView(aboutView)
		   .setPositiveButton("OK", null);
		
		dlgAbout = builder.create();
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
    	this.optionsMenu = menu;
	    return super.onCreateOptionsMenu(menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
	    switch (item.getItemId())
	    {
		    case R.id.menu_refresh:
		    	refreshPortfolio();
		    	return true;
		    case R.id.menu_configure:
		    	openConfigActivity();
	            return true;
		    case R.id.menu_about:
		    	showAboutDialog();
		    	return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	private void showAboutDialog()
	{
		if (dlgAbout == null)
			initializeAboutDialog();
		dlgAbout.show();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		position--;
		
		HavelockFund selectedFund = havelockResp.portfolio[position];		
		Intent detIntent = new Intent(this, FundActivity.class);
		detIntent.putExtra("selectedFund", selectedFund);
		startActivityForResult(detIntent, MYHAVELOCK_DETAILS_INTENT, null);
        overridePendingTransition( R.anim.slide_in_left, R.anim.slide_out_left );
	}

	private void openConfigActivity()
	{
		Intent confIntent = new Intent(this, ConfigActivity.class);
		startActivityForResult(confIntent, MYHAVELOCK_CONFIGURATION_INTENT, null);
        overridePendingTransition( R.anim.slide_in_left, R.anim.slide_out_left );
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
	    // Check which request we're responding to
	    if (requestCode == 1)
	    {
	        String oldAPIKey = apiKey;
	        loadSavedAPIKey();
	        if (!oldAPIKey.equals(apiKey))
	        {
	        	tvMessage.setOnClickListener(null);
	        	displayLoadingMessage();
	        	refreshPortfolio();
	        }
	    }
	}
	
	public void setRefreshActionButtonState(final boolean refreshing)
	{
	    if (optionsMenu != null)
	    {
	        final MenuItem refreshItem = optionsMenu.findItem(R.id.menu_refresh);
	        if (refreshItem != null) {
	            if (refreshing)
	                refreshItem.setActionView(R.layout.layout_refresh_progress);
	            else
	                refreshItem.setActionView(null);
	        }
	    }
	}
	
	private void populateListView(HavelockFund[] funds)
	{
		ArrayList<Map<String, String>> fundList = buildListData(funds);
		String[] from = { "symbol", "price", "book", "market", "updown", "delta" };
		int[] to = { R.id.tvSymbol, R.id.tvSharePrice, R.id.tvBookPrice, R.id.tvMarketPrice, R.id.imgUpDown, R.id.tvDelta };
		listAdapter = new SimpleAdapter(getBaseContext(), fundList, R.layout.listitem_fund, from, to);
		
        // Setting the adapter to the listView
		this.setListAdapter(listAdapter);
        setPortfolioValue();
        hideMessages();
        lstFunds.setVisibility(ListView.VISIBLE);
	}

	private void hideMessages()
	{
		tvMessage.setVisibility(TextView.GONE);
        tvMessageHeading.setVisibility(TextView.GONE);
        imgAlert.setVisibility(ImageView.GONE);
	}

	private void setPortfolioValue()
	{
		DecimalFormat df = new DecimalFormat("#,###,###.########");
        tvPortfolioValue.setText("Portfolio Value \u0E3F" + df.format(portfolioValue));
        tvPortfolioValue.setVisibility(TextView.VISIBLE);
	}
		
	private ArrayList<Map<String, String>> buildListData(HavelockFund[] funds)
	{
		ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
		portfolioValue = 0;
		
		for (HavelockFund f : funds)
		{
			int arrow = (f.marketvalue > f.bookvalue) ? R.drawable.uparrow : (f.marketvalue == f.bookvalue) ? android.R.color.transparent : R.drawable.downarrow;
			double diff = Math.abs(f.bookvalue - f.marketvalue);
			double delta = diff/f.bookvalue * 100;
			DecimalFormat df = new DecimalFormat("#,###.####");
			btcdf.setMinimumFractionDigits(8);
			String sdelta = "Δ " + ((f.marketvalue < f.bookvalue) ? "-" : "+") + df.format(delta) + "%";
			list.add(putData(f.symbol, "\u0E3F" + f.lastprice + " x " + f.quantity, "Book \u0E3F" + btcdf.format(f.bookvalue), "Market \u0E3F" + btcdf.format(f.marketvalue), arrow, sdelta));
			portfolioValue += f.marketvalue;
		}
		
		return list;
	}
	
	private HashMap<String, String> putData(String symbol, String price, String book, String market, int updown, String sdelta)
	{
		HashMap<String, String> item = new HashMap<String, String>();
		item.put("symbol", symbol);
		item.put("price", price);
		item.put("book", book);
		item.put("market", market);
		item.put("updown", Integer.toString(updown));
		item.put("delta", sdelta);
		return item;
	}
	
	private void handleHavelockResponse(HavelockPortfolioResponse resp)
	{
		havelockResp = resp;
		if (resp != null)
		{
			if (resp.status.toLowerCase().equals("ok"))
			{
				if ((resp.portfolio != null) && (resp.portfolio.length > 0))
				{
					populateListView(resp.portfolio);
				}
				else
				{
					displayEmptyPortfolioMessage();
				}
			}
			else
			{
				String error = "The following error was received from HavelockInvestments.com: " + resp.message;
				
				if (resp.message.toLowerCase().equals("invalid api key"))
				{
					error = getString(R.string.apikey_error);
				}
				else if (resp.message.toLowerCase().equals("key does not have requested permission"))
				{
					error = getString(R.string.no_api_portfolio_access);
				}
				
				displayAlertMessage(error, true);
				
			}
		}
		else
		{
			displayAlertMessage(getString(R.string.portfolio_connection_error), false);
		}
		
	}

	private void displayAlertMessage(String message, boolean tapForConfig)
	{
		lstFunds.setVisibility(ListView.GONE);
		tvPortfolioValue.setVisibility(TextView.INVISIBLE);
		imgAlert.setImageResource(R.drawable.alert);
		imgAlert.setVisibility(ImageView.VISIBLE);
		tvMessageHeading.setVisibility(TextView.VISIBLE);
		tvMessageHeading.setText(R.string.alert);
		tvMessage.setVisibility(TextView.VISIBLE);
		tvMessage.setText(message);
		if (tapForConfig)
		{
			tvMessage.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					openConfigActivity();
				}
			});
		}
	}
	
	private void displayEmptyPortfolioMessage()
	{
		lstFunds.setVisibility(ListView.GONE);
		tvPortfolioValue.setVisibility(TextView.INVISIBLE);
		imgAlert.setImageResource(R.drawable.empty);
		imgAlert.setVisibility(ImageView.VISIBLE);
		tvMessageHeading.setVisibility(TextView.VISIBLE);
		tvMessageHeading.setText(R.string.no_funds);
		tvMessage.setVisibility(TextView.VISIBLE);
		tvMessage.setText(R.string.empty_portfolio_alert);
		tvMessage.setOnClickListener(null);
	}
	
	private void displayWelcomeMessage()
	{
		lstFunds.setVisibility(ListView.GONE);
		tvPortfolioValue.setVisibility(TextView.INVISIBLE);
		imgAlert.setImageResource(R.drawable.welcome);
		imgAlert.setVisibility(ImageView.VISIBLE);
		tvMessageHeading.setVisibility(TextView.VISIBLE);
		tvMessageHeading.setText(R.string.welcome);
		tvMessage.setVisibility(TextView.VISIBLE);
		tvMessage.setText(getString(R.string.welcome_message));
		tvMessage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openConfigActivity();
			}
		});
	}
	
	private void displayLoadingMessage()
	{
		lstFunds.setVisibility(ListView.GONE);
		tvPortfolioValue.setVisibility(TextView.INVISIBLE);
		imgAlert.setVisibility(ImageView.GONE);
		tvMessageHeading.setVisibility(TextView.VISIBLE);
		tvMessageHeading.setText("Loading");
		tvMessage.setVisibility(TextView.VISIBLE);
		tvMessage.setText(R.string.loading_portfolio);
		tvMessage.setOnClickListener(null);
	}
	
	// Havelock Portfolio Loading Task
	private class HavelockTask extends AsyncTask<String, Integer, HavelockPortfolioResponse>
	{
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			
			if (apiKey.equals(""))
			{
				displayWelcomeMessage();
				this.cancel(true);
			}
			else
			{
				//displayLoadingMessage();
				setRefreshActionButtonState(true);
			}
		}

		@Override
		protected HavelockPortfolioResponse doInBackground(String... params)
		{
			try
			{
				return HavelockRequest.getPortfolio(params[0]);
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
		protected void onPostExecute(HavelockPortfolioResponse result)
		{
			super.onPostExecute(result);
			lstFunds.onRefreshComplete();
			setRefreshActionButtonState(false);
			handleHavelockResponse(result);
		}

	}
}
