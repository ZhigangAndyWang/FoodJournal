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
import android.app.Dialog;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import edu.cornell.cs5356.foodjournaling.image.FoodImage;
import edu.cornell.cs5356.foodjournaling.image.FoodImageAdapter;
import edu.cornell.cs5356.foodjournaling.user.UserFunctions;

public class MainMenuActivity extends Activity {
	UserFunctions userFunctions;
	Button btnLogout;
	Button btnTakePic;
	ProgressDialog dialog = null;
	private ImageView mImageView;

	private ProgressBar progressBar;
	private int progressStatus = 0;
	private Handler handler = new Handler();

	TextView welcomeText;
	String username = "UNKNOWN";

	private static final String TAG = "MainMenu";
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private Uri picUri;
	public static final String SERVER_URI = "http://54.172.32.59:8000";
	private String getImageUri = "http://54.172.32.59:8000/getImages/username=";

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

			SharedPreferences prefs = getSharedPreferences("Letseat",
					MODE_PRIVATE);
			// username = prefs.getString("username", "UNKNOWN");
			username = userFunctions.getUsername(getApplicationContext());
			welcomeText = (TextView) findViewById(R.id.welcome_text);
			welcomeText.setText("Hi, " + username);

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

			progressBar = (ProgressBar) findViewById(R.id.progressBar1);
			progressBar.setIndeterminate(true);
			progressBar.setMax(100);
			progressBar.setProgress(0);

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

	private class GetImagesTask extends
			AsyncTask<String, Void, ArrayList<FoodImage>> {
		@Override
		protected ArrayList<FoodImage> doInBackground(String... params) {
			ArrayList<FoodImage> foodList = new ArrayList<FoodImage>();

			String loadingImagesUrl = params[0];

			StringBuilder sb = new StringBuilder();
			try {
				DefaultHttpClient httpclient = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(loadingImagesUrl);

				HttpResponse response = httpclient.execute(httpGet);

				if (response != null) {
					Log.i(TAG, "response "
							+ response.getStatusLine().toString());
					HttpEntity httpEntity = response.getEntity();
					InputStream is = httpEntity.getContent();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(is, "iso-8859-1"), 8);

					String line = null;
					while ((line = reader.readLine()) != null) {
						sb.append(line + "\n");
					}
					is.close();

					String imagePath = "";
					String imageDesc = "";
					String createdTime = "";
					JSONObject json = new JSONObject(sb.toString());
					if (json.getString("images") != null) {
						JSONArray json_array = json.getJSONArray("images");

						for (int i = 0; i < json_array.length(); i++) {
							JSONObject json_object = new JSONObject(
									json_array.getString(i));

							System.out.println(json_object
									.getString("imageUrl"));
							imagePath = json_object.getString("imageUrl");
							imageDesc = json_object.getString("description");
							createdTime = json_object.getString("created_date");

							URL imageUrl = new URL(MainMenuActivity.SERVER_URI
									+ imagePath);
							Bitmap bmp = BitmapFactory.decodeStream(imageUrl
									.openConnection().getInputStream());

							FoodImage fImage = new FoodImage();
							fImage.setBmp(bmp);
							fImage.setimageUri(imagePath);
							fImage.setDescription(imageDesc);
							fImage.setTimestamp(createdTime);

							foodList.add(fImage);
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return foodList;
		}

		@Override
		protected void onPostExecute(ArrayList<FoodImage> result) {
			if (result != null) {
				try {
					
					MainMenuActivity.this.progressBar.setProgress(100);
					MainMenuActivity.this.progressBar.setVisibility(View.GONE);

					FoodImageAdapter adapter = new FoodImageAdapter(
							MainMenuActivity.this, result);
					ListView listView = (ListView) findViewById(R.id.listView1);
					listView.setAdapter(adapter);

					listView.setOnItemClickListener(new OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
							final FoodImage item = (FoodImage) parent.getItemAtPosition(position);
							
							final Dialog dialog = new Dialog(MainMenuActivity.this);
							dialog.setContentView(R.layout.dialog_image);
							dialog.setTitle(item.getDescription());
							
							//TextView text = (TextView) dialog.findViewById(R.id.tv_dialog_image1);
							//text.setText(item.getDescription());
							ImageView image = (ImageView) dialog.findViewById(R.id.dialog_image1);
							//image.setImageBitmap(Bitmap.createScaledBitmap(item.getBmp(), 640, 560, false));
							image.setImageBitmap(item.getBmp());
							
							Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
							// if button is clicked, close the custom dialog
							dialogButton.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									dialog.dismiss();
								}
							});
				 
							dialog.show();
						}
					});
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				// loginErrorMsg.setText(result);
			}
		}

	}
	
	public void setValue(int progress) {
		this.progressBar.setProgress(progress);		
	}
}
