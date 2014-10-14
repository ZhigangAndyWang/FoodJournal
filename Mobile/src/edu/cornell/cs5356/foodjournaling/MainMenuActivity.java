package edu.cornell.cs5356.foodjournaling;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

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
import edu.cornell.cs5356.foodjournaling.user.MultipartEntity;
import edu.cornell.cs5356.foodjournaling.user.UserFunctions;

public class MainMenuActivity extends Activity {
	UserFunctions userFunctions;
	Button btnLogout;
	Button btnTakePic;
	ProgressDialog dialog = null;
	private ImageView mImageView;
	
	TextView welcomeText;
	String username = "UNKOWN";

	private static final String TAG = "MainMenu";
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private Uri picUri;

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

			mImageView = (ImageView) findViewById(R.id.imageView1);

			btnTakePic = (Button) findViewById(R.id.btnTakePic);
			btnTakePic.setOnClickListener(new View.OnClickListener() {

				public void onClick(View arg0) {
					Intent takePictureIntent = new Intent(
							MediaStore.ACTION_IMAGE_CAPTURE);
					// start the image capture Intent
					if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
						picUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
						System.out.println("picUri: " + picUri);
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
					// TODO Auto-generated method stub
					userFunctions.logoutUser(getApplicationContext());
					Intent login = new Intent(getApplicationContext(),
							LoginActivity.class);
					login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(login);
					// Closing dashboard screen
					finish();
				}
			});

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
				// Image captured and saved to fileUri specified in the Intent
				// Toast.makeText(this, "Image saved to:\n" +
				// data.getData(), Toast.LENGTH_LONG).show();
				// Toast.makeText(this, "Uploading image " + data.getData() +
				// " to server", Toast.LENGTH_LONG).show();
				// dialog = ProgressDialog.show(DashboardActivity.this, "",
				// "Uploading file...", true);
				/*
				 * System.out.println("data: " + data); Bundle extras =
				 * data.getExtras(); Bitmap imageBitmap = (Bitmap)
				 * extras.get("data"); mImageView.setImageBitmap(imageBitmap);
				 * System.out.println("3");
				 */
				
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

	private class UploadTask extends AsyncTask<Bitmap, Void, Void> {

		protected Void doInBackground(Bitmap... bitmaps) {
			if (bitmaps[0] == null)
				return null;
			setProgress(0);

			Bitmap bitmap = bitmaps[0];
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream); // convert
																		// Bitmap
																		// to
																		// ByteArrayOutputStream
			InputStream in = new ByteArrayInputStream(stream.toByteArray()); // convert
																				// ByteArrayOutputStream
																				// to
																				// ByteArrayInputStream

			DefaultHttpClient httpclient = new DefaultHttpClient();
			try {
				HttpPost httppost = new HttpPost(
						"http://192.168.8.84:8003/savetofile.php"); // server

				MultipartEntity reqEntity = new MultipartEntity();
				reqEntity.addPart("myFile",
						System.currentTimeMillis() + ".jpg", in);
				httppost.setEntity(reqEntity);

				Log.i(TAG, "request " + httppost.getRequestLine());
				HttpResponse response = null;
				try {
					response = httpclient.execute(httppost);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					if (response != null)
						Log.i(TAG, "response "
								+ response.getStatusLine().toString());
				} finally {

				}
			} finally {

			}

			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			Toast.makeText(MainMenuActivity.this, "uploaded", Toast.LENGTH_LONG)
					.show();
		}
	}

}
