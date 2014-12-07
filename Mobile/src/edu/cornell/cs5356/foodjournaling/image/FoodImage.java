package edu.cornell.cs5356.foodjournaling.image;

import android.graphics.Bitmap;

public class FoodImage {
	private Bitmap bmp;
	private String imageUri;
	private String description;
	private String timestamp;
	private String username;
	
	public FoodImage(){
		
	}

	public Bitmap getBmp() {
		return bmp;
	}

	public void setBmp(Bitmap bmp) {
		this.bmp = bmp;
	}
	
	public String getimageUri() {
		return imageUri;
	}

	public void setimageUri(String imageUri) {
		this.imageUri = imageUri;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
