package edu.cornell.cs5356.foodjournaling.image;

import java.net.URL;
import java.util.ArrayList;

import edu.cornell.cs5356.foodjournaling.MainMenuActivity;
import edu.cornell.cs5356.foodjournaling.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FoodImageAdapter extends ArrayAdapter<FoodImage> {
	public FoodImageAdapter(Context context, ArrayList<FoodImage> foodImages) {
		super(context, 0, foodImages);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Get the data item for this position
		FoodImage foodImage = getItem(position);
		// Check if an existing view is being reused, otherwise inflate the view
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(
					R.layout.item_food_image, parent, false);
		}

		try {
			// Lookup view for data population
			ImageView mImageView = (ImageView) convertView
					.findViewById(R.id.item_food_imageView1);
			
			mImageView.setImageBitmap(Bitmap.createScaledBitmap(foodImage.getBmp(), 120, 120, false));
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		TextView tvDesc = (TextView) convertView.findViewById(R.id.tvDesc);
		TextView tvDate = (TextView) convertView.findViewById(R.id.tvDate);
		// Populate the data into the template view using the data object
		tvDesc.setText(foodImage.getDescription());
		tvDate.setText(foodImage.getTimestamp());
		// Return the completed view to render on screen
		return convertView;
	}
}
