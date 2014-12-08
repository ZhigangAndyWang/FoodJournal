package edu.cornell.cs5356.foodjournaling;

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class MainTabActivity extends TabActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab_host);

		// create the TabHost that will contain the Tabs
		TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);

		TabSpec tab1 = tabHost.newTabSpec("First Tab");
		TabSpec tab2 = tabHost.newTabSpec("Second Tab");
		TabSpec tab3 = tabHost.newTabSpec("Third tab");
		TabSpec tab4 = tabHost.newTabSpec("Forth tab");

		// Set the Tab name and Activity
		// that will be opened when particular Tab will be selected
		tab1.setIndicator("My Page");
		tab1.setContent(new Intent(this, MainMenuActivity.class));

		tab2.setIndicator("For You");
		tab2.setContent(new Intent(this, RecommendActivity.class));

		tab3.setIndicator("Tags");
		tab3.setContent(new Intent(this, TagsActivity.class));
		//tab3.setContent(new Intent(this, TagsListActivity.class));
		
		tab4.setIndicator("Friends");
		tab4.setContent(new Intent(this, FollowingActivity.class));

		/** Add the tabs to the TabHost to display. */
		tabHost.addTab(tab1);
		tabHost.addTab(tab2);
		tabHost.addTab(tab3);
		tabHost.addTab(tab4);
		
		/*
		for(int i=0;i<tabHost.getTabWidget().getChildCount();i++) 
	    {
	        TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
	        tv.setTextColor(Color.parseColor("#FFFFFF"));
	    }
	    */

	}
}
