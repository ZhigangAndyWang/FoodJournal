package edu.cornell.cs5356.foodjournaling;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
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
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import edu.cornell.cs5356.foodjournaling.image.FoodImage;
import edu.cornell.cs5356.foodjournaling.user.DatabaseHandler;
import edu.cornell.cs5356.foodjournaling.user.MultipartEntity;
import edu.cornell.cs5356.foodjournaling.user.UserFunctions;

public class MainMenuActivity extends Activity {
	UserFunctions userFunctions;
	Button btnLogout;
	Button btnTakePic;
	ProgressDialog dialog = null;
	private ImageView mImageView;
	private TextView mTextView1, mTextView2;
	
	TextView welcomeText;
	String username = "UNKNOWN";

	private static final String TAG = "MainMenu";
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private Uri picUri;
	private String serverUri = "http://54.172.32.59:8000";
	private String getImageUri = "http://54.172.32.59:8000/getImages/username=andy";

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
		// Check login status in database
		userFunctions = new UserFunctions();
		if (userFunctions.isUserLoggedIn(getApplicationContext())) {
			// user already logged in show databoard
			setContentView(R.layout.main_menu);
			
			SharedPreferences prefs = getSharedPreferences("Letseat", MODE_PRIVATE);
	        username = prefs.getString("username", "UNKNOWN");
	        welcomeText = (TextView) findViewById(R.id.welcome_text);
	        welcomeText.setText("Hi, " + username);

			mImageView = (ImageView) findViewById(R.id.main_menu_imageView1);
			mTextView1 = (TextView) findViewById(R.id.main_menu_textView1);
			mTextView2 = (TextView) findViewById(R.id.main_menu_textView2);

			btnTakePic = (Button) findViewById(R.id.btnTakePic);
			btnTakePic.setOnClickListener(new View.OnClickListener() {

				public void onClick(View arg0) {
					Intent takePictureIntent = new Intent(
							MediaStore.ACTION_IMAGE_CAPTURE);
					// start the image capture Intent
					if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
						picUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
						takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
								picUri);
						startActivityForResult(takePictureIntent,
								CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
					}
				}
			});

			btnLogout = (Button) findViewById(R.id.btnLogout);
			btnLogout.setOnClickListener(new View.OnClickListener() {

				public void onClick(View arg0) {
					userFunctions.logoutUser(getApplicationContext());
					Intent login = new Intent(getApplicationContext(),
							LoginActivity.class);
					login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(login);
					finish();
				}
			});
			
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {				
				Intent uploadPicIntent = new Intent(getApplicationContext(),
						UploadImageActivity.class);				
				uploadPicIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				uploadPicIntent.putExtra("picUri", picUri.toString());
				startActivity(uploadPicIntent);
				finish();
			} else if (resultCode == RESULT_CANCELED) {
				// User cancelled the image capture
				System.out.println("User cancelled impage capture!");
			} else {
				// Image capture failed, advise user
				System.out.println("Image capture failed!");
			}
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
	
	private class GetImagesTask extends AsyncTask<String, Void, FoodImage> {
		@Override
		protected FoodImage doInBackground(String... params) {
			FoodImage fImage = new FoodImage();
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
					
					String imagePath = "";
					String imageDesc = "";
					String createdTime = "";
					JSONObject json = new JSONObject(sb.toString());
					if(json.getString("images") != null) {
						JSONArray json_array = json.getJSONArray("images");
						
						for(int i=0; i<json_array.length(); i++) {
							JSONObject json_object = new JSONObject(json_array.getString(i));
							
							System.out.println(json_object.getString("imageUrl"));
							imagePath = json_object.getString("imageUrl");
							imageDesc = json_object.getString("description");
							createdTime = json_object.getString("created_date");
						}
					}
	
					URL imageUrl = new URL(serverUri + imagePath);
					Bitmap bmp = BitmapFactory.decodeStream(imageUrl.openConnection().getInputStream());
                    fImage.setBmp(bmp);
                    fImage.setDescription(imageDesc);
                    fImage.setTimestamp(createdTime);
					
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return fImage;
		}

		@Override
		protected void onPostExecute(FoodImage result) {
			if (result != null) {
				try {
					mImageView.setImageBitmap(Bitmap.createScaledBitmap(result.getBmp(), 120, 120, false));
					mTextView1.setText(result.getDescription());
					mTextView2.setText(result.getTimestamp());
				} catch (Exception e) {
					e.printStackTrace();
				}	
			} else {
				//loginErrorMsg.setText(result);
			}
		}
	}
}
