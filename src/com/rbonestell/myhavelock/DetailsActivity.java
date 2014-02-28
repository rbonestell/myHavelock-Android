//package com.rbonestell.myhavelock;
//
//import java.sql.Timestamp;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//
//import org.stockchart.*;
//
//import com.jjoe64.graphview.CustomLabelFormatter;
//import com.jjoe64.graphview.GraphView;
//import com.jjoe64.graphview.GraphView.GraphViewData;
//import com.jjoe64.graphview.GraphViewSeries;
//import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
//import com.jjoe64.graphview.LineGraphView;
//import com.rbonestell.myhavelock.utils.HavelockFund;
//import com.rbonestell.myhavelock.utils.HavelockRequest;
//import com.rbonestell.myhavelock.utils.HavelockTrade;
//import com.rbonestell.myhavelock.utils.HavelockTradesResponse;
//
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.app.Activity;
//import android.content.Intent;
//import android.graphics.Color;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//public class DetailsActivity extends Activity
//{
//	
//	private TextView tvMessage, tvMessageHeading;
//	private ImageView imgAlert;
//	private LinearLayout llGraph;
//
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState)
//	{
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_chart);
//		initializeControls();
//		Intent myIntent = getIntent(); // gets the previously created intent
//		HavelockFund selectedFund = (HavelockFund)myIntent.getSerializableExtra("selectedFund"); // will return "FirstKeyValue"
//		String symbol = (selectedFund != null) ? selectedFund.symbol : null;
//		setupActionBar(symbol);
//		this.displayLoadingMessage();
//		new HavelockTask().execute(selectedFund.symbol);
//	}
//
//	private  String now()
//	{
//		Calendar cal = Calendar.getInstance();
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		String today = sdf.format(cal.getTime());
//		return today;
//	}
//	
//	private String back30Days()
//	{
//		Calendar cal = Calendar.getInstance();
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		cal.add(Calendar.DAY_OF_YEAR, -30);
//		String delorean = sdf.format(cal.getTime());
//		cal.add(Calendar.DAY_OF_YEAR, 30);
//		return delorean;
//	}
//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu)
//	{
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.details, menu);
//		return true;
//	}
//	
//	/**
//	 * Set up the {@link android.app.ActionBar}.
//	 */
//	private void setupActionBar(String symbol)
//	{
//		if (symbol != null)
//			this.setTitle(this.getTitle().toString().replace("Fund", symbol));
//		getActionBar().setDisplayHomeAsUpEnabled(true);
//	}
//
//	@Override
//    public boolean onOptionsItemSelected(MenuItem item)
//	{
//        switch (item.getItemId()) {
//	        case android.R.id.home:
//	            this.finish();
//	            return true;
//	        default:
//	            return super.onOptionsItemSelected(item);
//        }
//    }
//	
//	private void initializeControls()
//	{
//		tvMessage = (TextView)findViewById(R.id.tvMessage);
//		tvMessageHeading = (TextView)findViewById(R.id.tvMessageHeading);
//		imgAlert = (ImageView)findViewById(R.id.imgAlert);
//		llGraph = (LinearLayout) findViewById(R.id.graph);
//	}
//	
//	private void displayAlertMessage(String message)
//	{
//		// Hide graph view
//		imgAlert.setImageResource(R.drawable.alert);
//		imgAlert.setVisibility(ImageView.VISIBLE);
//		tvMessageHeading.setVisibility(TextView.VISIBLE);
//		tvMessageHeading.setText(R.string.alert);
//		tvMessage.setVisibility(TextView.VISIBLE);
//		tvMessage.setText(message);
//	}
//	
//	private void displayLoadingMessage()
//	{
//		// Hide graph view
//		imgAlert.setVisibility(ImageView.GONE);
//		tvMessageHeading.setVisibility(TextView.VISIBLE);
//		tvMessageHeading.setText("Loading");
//		tvMessage.setVisibility(TextView.VISIBLE);
//		tvMessage.setText(R.string.loading_trades);
//		tvMessage.setOnClickListener(null);
//	}
//	
//	private void hideMessage()
//	{
//		// Hide graph view
//		imgAlert.setVisibility(ImageView.GONE);
//		tvMessageHeading.setVisibility(TextView.GONE);
//		tvMessage.setVisibility(TextView.GONE);
//	}
//	
//	private long dateDiff(String date, String now)
//	{
//		long tradeStamp = Timestamp.valueOf(date).getTime();
//		long nowStamp = Timestamp.valueOf(now).getTime();
//		return nowStamp - tradeStamp;
//	}
//	
//	private void doGraph(HavelockTrade[] trades)
//	{
//		this.hideMessage();
//		final long msDay = 86400000;	
//		GraphViewData[] gData = new GraphViewData[trades.length];
//		double high = trades[0].p;
//		double low = trades[0].p;
//		for (int i = 0; i < trades.length; i++)
//		{
//			gData[i] = new GraphViewData(i, trades[i].p);
//			if (trades[i].p > high)
//				high = trades[i].p;
//			if (trades[i].p < low)
//				low = trades[i].p;
//		}
//		
//		double diff = (high - low)/10;
//		
//		GraphViewSeriesStyle style = new GraphViewSeriesStyle();
//        style.color = Color.BLUE;
//        
//		// init series data
//        GraphViewSeries exampleSeries = new GraphViewSeries(null, style, gData);
//        
//        // graph with dynamically genereated horizontal and vertical labels
//        GraphView graphView;
//        graphView = new LineGraphView(this, "");
//        ((LineGraphView) graphView).setDrawDataPoints(true);
//        ((LineGraphView) graphView).setDataPointsRadius(5f);
//        graphView.setManualYAxisBounds(high + diff, low - diff);
//        graphView.setScrollable(true);
//        graphView.setScalable(true);
//        graphView.setViewPort(2, 7);
//        // 86400000ms in 1 day
//        
//        graphView.addSeries(exampleSeries); // data
//
//        llGraph.setVisibility(LinearLayout.VISIBLE);
//        llGraph.addView(graphView);
//	}
//	
//	private void stockChart()
//	{
//		
//	}
//	
//	
//
//}
