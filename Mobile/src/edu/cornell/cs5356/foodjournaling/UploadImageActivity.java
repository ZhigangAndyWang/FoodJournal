package edu.cornell.cs5356.foodjournaling;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


public class UploadImageActivity extends Activity {
	private static final String TAG = "UploadImageActivity";
	private static final String uploadUrl = "http://54.172.32.59:8000/uploadImage/";
	Button btnSumbitPic;
	Button btnCancelPic;
	private ImageView mImageView;
	private Uri picUri;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.upload_image);
		
		mImageView = (ImageView) findViewById(R.id.imageViewUpload1);
		System.out.println("mimageView: " + mImageView);
		System.out.println("HW: " + mImageView.getHeight() + ":" + mImageView.getWidth());
		
		Bundle bundle = getIntent().getExtras();
		if(bundle != null) {
			picUri = Uri.parse(bundle.getString("picUri"));
		}
		
		btnSumbitPic = (Button) findViewById(R.id.btnSumbitPic);
		btnSumbitPic.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				new uploadImageTask().execute();
			}
		});
		
		btnCancelPic = (Button) findViewById(R.id.btnCancelPic);
		btnCancelPic.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				Intent mainMenuActivity = new Intent(getApplicationContext(),
						MainMenuActivity.class);
				mainMenuActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(mainMenuActivity);
				finish();
			}
		});
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus){
		this.setPic(picUri);
	}

	private class uploadImageTask extends AsyncTask<Void, Void, String> {
		@Override
		protected String doInBackground(Void... params) {
			StringBuilder sb = new StringBuilder();
			
			setProgress(0);

			DefaultHttpClient httpclient = new DefaultHttpClient();
			
			// Create a local instance of cookie store
			CookieStore cookieStore = new BasicCookieStore();
			// Create local HTTP context
            HttpContext localContext= new BasicHttpContext();
            // Bind custom cookie store to the local context
            localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
			
			try {
				HttpPost httpPost = new HttpPost(uploadUrl); // server

				File file = new File(picUri.getPath());

				SharedPreferences prefs = getSharedPreferences("Letseat", MODE_PRIVATE);
		        String username = prefs.getString("username", "UNKNOWN");
		        System.out.println("username: " + username);
				
				MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();
				multipartEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				
				//FileBody fileBody = new FileBody(file);
				//multipartEntity.addPart("file", fileBody);
				multipartEntity.addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName());
				multipartEntity.addPart("description", new StringBody("test description", ContentType.MULTIPART_FORM_DATA));
				multipartEntity.addPart("username", new StringBody(username, ContentType.MULTIPART_FORM_DATA));
				
				httpPost.setEntity(multipartEntity.build());
				
				//set header for anth
				//request.setHeader("Authorization", "Bearer 83ebeabdec09f6670863766f792ead24d61fe3f9");

				Log.i(TAG, "request " + httpPost.getRequestLine());
				HttpResponse response = httpclient.execute(httpPost, localContext);
				
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
					
					System.out.println("!!!!!!! " + sb);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}

			return sb.toString();
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				JSONObject jObj = new JSONObject(result); 
				
				if(jObj.getString("success") != null) {
					int success_result = Integer.parseInt(jObj.getString("success"));
					
					if (success_result == 1) {
						// super.onPostExecute(result);
						Toast.makeText(UploadImageActivity.this, "uploaded",
								Toast.LENGTH_LONG).show();

						Intent dashboard = new Intent(getApplicationContext(),
								MainMenuActivity.class);
						// Close all views before launching Dashboard
						dashboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(dashboard);
						finish();
					} else if (success_result == 0) {
						Toast.makeText(UploadImageActivity.this, "upload failed",
								Toast.LENGTH_LONG).show();
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	
	private void setPic(Uri picUri) {
		String mCurrentPhotoPath = picUri.getPath();
		System.out.println("mCurrentPhotoPath: " + mCurrentPhotoPath);

		// Get the dimensions of the View
		int targetW = mImageView.getWidth();
		int targetH = mImageView.getHeight();
		// Get the dimensions of the bitmap
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;
		// Determine how much to scale down the image
		int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
		// Decode the image file into a Bitmap sized to fill the View
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;
		Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		mImageView.setImageBitmap(bitmap);
	}

}
