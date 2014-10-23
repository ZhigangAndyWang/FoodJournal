package edu.cornell.cs5356.foodjournaling.image;

import android.graphics.Bitmap;

public class FoodImage {
	private Bitmap bmp;
	private String description;
	private String timestamp;
	
	public FoodImage(){
		
	}

	public Bitmap getBmp() {
		return bmp;
	}

	public void setBmp(Bitmap bmp) {
		this.bmp = bmp;
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

}
