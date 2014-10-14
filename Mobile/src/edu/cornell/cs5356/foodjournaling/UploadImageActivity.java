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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;


import android.app.Activity;
import android.content.Intent;
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

	private class uploadImageTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			
			setProgress(0);

			DefaultHttpClient httpclient = new DefaultHttpClient();
			try {
				HttpPost httpPost = new HttpPost(uploadUrl); // server

				//String imageUri = "/storage/emulated/0/Pictures/LetsEatPics/IMG_20141013_171657.jpg";
				String imageUri = "/storage/emulated/0/Pictures/Screenshots/Screenshot_2013-12-09-11-25-34.png";
				
				File file = new File(imageUri);
				FileBody fileBody = new FileBody(file);
				
				MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();
				multipartEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				
				multipartEntity.addPart("file", fileBody);
				//multipartEntity.addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName());
				multipartEntity.addPart("description", new StringBody("test description", ContentType.MULTIPART_FORM_DATA));
				
				httpPost.setEntity(multipartEntity.build());
				
				//set header for anth
				//request.setHeader("Authorization", "Bearer 83ebeabdec09f6670863766f792ead24d61fe3f9");
				
				/*
				MultipartEntity reqEntity = new MultipartEntity();
				//reqEntity.addPart("myFile",
						//System.currentTimeMillis() + ".jpg", in);
				reqEntity.addPart("file",
						"file:///storage/emulated/0/Pictures/LetsEatPics/IMG_2014013_171657.jpg", in);
				reqEntity.addPart("description", "This is lunch");
				httppost.setEntity(reqEntity);
				*/

				Log.i(TAG, "request " + httpPost.getRequestLine());
				HttpResponse response = null;
				try {
					response = httpclient.execute(httpPost);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (response != null) {
					Log.i(TAG, "response " + response.getStatusLine().toString());
					HttpEntity httpEntity = response.getEntity();
					InputStream is = httpEntity.getContent();
					BufferedReader reader = new BufferedReader(new InputStreamReader(
							is, "iso-8859-1"), 8);
					StringBuilder sb = new StringBuilder();
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
			Toast.makeText(UploadImageActivity.this, "uploaded",
					Toast.LENGTH_LONG).show();
		}

	}

	/*
	 * @Override protected void onPostExecute(String result) {
	 * 
	 * }
	 */
	
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
