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

public class TagsAdapter extends ArrayAdapter<String> {
	public TagsAdapter(Context context, ArrayList<String> tags) {
		super(context, 0, tags);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Get the data item for this position
		String tag = getItem(position);
		// Check if an existing view is being reused, otherwise inflate the view
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(
					R.layout.item_tag, parent, false);
		}

		TextView tvTag = (TextView) convertView.findViewById(R.id.tvTag);
		// Populate the data into the template view using the data object
		tvTag.setText(tag);
		// Return the completed view to render on screen
		return convertView;
	}
}
