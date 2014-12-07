package edu.cornell.cs5356.foodjournaling;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import edu.cornell.cs5356.foodjournaling.image.FoodImage;
import edu.cornell.cs5356.foodjournaling.image.FoodImageAdapter;
import edu.cornell.cs5356.foodjournaling.image.FoodImageRecommendAdapter;
import edu.cornell.cs5356.foodjournaling.image.TagsAdapter;
import edu.cornell.cs5356.foodjournaling.user.UserFunctions;

public class TagsActivity extends Activity {
	UserFunctions userFunctions;
	Button btnLogout;
	Button btnTakePic;
	ProgressDialog dialog = null;
	
	private ProgressBar progressBar;
	private int progressStatus = 0;
	private Handler handler = new Handler();
	
	String username = "UNKNOWN";

	private static final String TAG = "TagsActivity";
	public static final String SERVER_URI = "http://54.172.32.59:8000";

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Check login status in database
		userFunctions = new UserFunctions();
		if (userFunctions.isUserLoggedIn(getApplicationContext())) {
			// user already logged in show databoard
			setContentView(R.layout.tag);
			
			SharedPreferences prefs = getSharedPreferences("Letseat", MODE_PRIVATE);
	        //username = prefs.getString("username", "UNKNOWN");
			username = userFunctions.getUsername(getApplicationContext());
			
			ArrayList<String> result = new ArrayList<String>();
			result.add("#Breakfast");
			result.add("#Lunch");
			result.add("#Supper");
			result.add("#American");
			result.add("#Chinese");
			result.add("#French");
			result.add("#Japanese");
			result.add("#Mexican");
			result.add("#Vegetarian");
			TagsAdapter adapter = new TagsAdapter(TagsActivity.this, result);
			ListView listView = (ListView) findViewById(R.id.listView1);
			listView.setAdapter(adapter);

		} else {
			// user is not logged in show login screen
			Intent login = new Intent(getApplicationContext(),
					LoginActivity.class);
			login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(login);
			// Closing dashboard screen
			finish();
		}
	}
}
