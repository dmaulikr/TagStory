package no.tagstory.honeycomb;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import no.tagstory.StoryDetailActivity;

@SuppressLint("NewApi")
public class StoryDetailActivityHoneycomb extends StoryDetailActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onMenuItemSelected(int featuredId, MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onMenuItemSelected(featuredId, item);
	}
}
