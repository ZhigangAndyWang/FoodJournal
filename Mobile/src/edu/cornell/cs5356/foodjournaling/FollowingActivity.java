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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import edu.cornell.cs5356.foodjournaling.image.FollowingAdapter;
import edu.cornell.cs5356.foodjournaling.image.FoodImage;
import edu.cornell.cs5356.foodjournaling.image.FoodImageAdapter;
import edu.cornell.cs5356.foodjournaling.image.FoodImageRecommendAdapter;
import edu.cornell.cs5356.foodjournaling.user.UserFunctions;

public class FollowingActivity extends Activity {
	UserFunctions userFunctions;
	Button btnLogout;
	Button btnTakePic;
	ProgressDialog dialog = null;
	
	private ProgressBar progressBar;
	private int progressStatus = 0;
	private Handler handler = new Handler();
	
	String username = "UNKNOWN";

	private static final String TAG = "FollowingActivity";
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private Uri picUri;
	public static final String SERVER_URI = "http://54.172.32.59:8000";
	private String getImageUri = "http://54.172.32.59:8000/getFriends/username=";

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Check login status in database
		userFunctions = new UserFunctions();
		if (userFunctions.isUserLoggedIn(getApplicationContext())) {
			// user already logged in show databoard
			setContentView(R.layout.tags_list);
			
			SharedPreferences prefs = getSharedPreferences("Letseat", MODE_PRIVATE);
	        //username = prefs.getString("username", "UNKNOWN");
			username = userFunctions.getUsername(getApplicationContext());
			
			progressBar = (ProgressBar) findViewById(R.id.progressBar1);
			progressBar.setIndeterminate(true);
			progressBar.setMax(100);
			progressBar.setProgress(0);
			
			/*
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
			    String value = extras.getString("TAG_NAME");
			    System.out.println("!!!!!!!TAG_NAME: " + value);
			    getImageUri = getImageUri + value;
			}*/
			
			getImageUri = getImageUri + username;
			new GetImagesTask().execute(getImageUri);

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

	/** Create a file Uri for saving an image or video */
	private static Uri getOutputMediaFileUri(int type) {
		return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"LetsEatPics");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("LetsEatPics", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "VID_" + timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}
	
	private class GetImagesTask extends AsyncTask<String, Void, ArrayList<String>> {
		@Override
		protected ArrayList<String> doInBackground(String... params) {
			ArrayList<String> followingList = new ArrayList<String>();
			
			String loadingImagesUrl = params[0];
			
			StringBuilder sb = new StringBuilder();
			try {
				DefaultHttpClient httpclient = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(loadingImagesUrl);
				
				HttpResponse response = httpclient.execute(httpGet);
				
				if (response != null) {
					Log.i(TAG, "response " + response.getStatusLine().toString());
					HttpEntity httpEntity = response.getEntity();
					InputStream is = httpEntity.getContent();
					BufferedReader reader = new BufferedReader(new InputStreamReader(
							is, "iso-8859-1"), 8);
					
					String line = null;
					while ((line = reader.readLine()) != null) {
						sb.append(line + "\n");
					}
					is.close();
					
					String username = "";
					System.out.println("sb: " + sb.toString());
					JSONObject json = new JSONObject(sb.toString());
					if(json.getString("friends") != null) {
						JSONArray json_array = json.getJSONArray("friends");
						
						for(int i=0; i<json_array.length(); i++) {
							JSONObject json_object = new JSONObject(json_array.getString(i));
							
							username = json_object.getString("username");
							
		                    followingList.add(username);
						}
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return followingList;
		}

		@Override
		protected void onPostExecute(ArrayList<String> result) {
			if (result != null) {
				try {
					
					FollowingActivity.this.progressBar.setProgress(100);
					FollowingActivity.this.progressBar.setVisibility(View.GONE);
					
					FollowingAdapter adapter = new FollowingAdapter(FollowingActivity.this, result);
					ListView listView = (ListView) findViewById(R.id.listView1);
					listView.setAdapter(adapter);
					
					listView.setOnItemClickListener(new OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
							final String item = (String) parent.getItemAtPosition(position);
							Intent friend = new Intent(getApplicationContext(), FriendsActivity.class);
							friend.putExtra("USERNAME", item);
							friend.putExtra("FOLLOWED", "TRUE");
							startActivity(friend);
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}	
			} else {
				//loginErrorMsg.setText(result);
			}
		}
	}
}
