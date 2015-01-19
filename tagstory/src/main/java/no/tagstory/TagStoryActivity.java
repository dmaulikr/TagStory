package no.tagstory;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import no.tagstory.adapters.StoryCursorAdapter;
import no.tagstory.honeycomb.StoryDetailActivityHoneycomb;
import no.tagstory.marked.SimpleStoryMarkedActivity;
import no.tagstory.story.StoryManager;
import no.tagstory.utils.ClassVersionFactory;
import no.tagstory.utils.Database;
import no.tagstory.utils.DialogFactory;
import no.tagstory.utils.GooglePlayServiceUtils;

import static no.tagstory.utils.GooglePlayServiceUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST;

public class TagStoryActivity extends FragmentActivity implements OnItemClickListener {

	protected Dialog aboutTagStoryDialog;
	protected Cursor storyCursor;
	protected StoryManager storyManager;
	protected ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_story_list);

		GooglePlayServiceUtils.servicesConnected(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		storyManager = new StoryManager(this);
		storyCursor = storyManager.getCursorOverStories();
		if (storyCursor.getCount() > 0) {
			initializeListView();
		} else {
			initializeNoStoriesView();
		}
	}

	private void initializeNoStoriesView() {
		setContentView(R.layout.activity_no_stories);
	}

	protected void initializeListView() {
		listView = (ListView) findViewById(R.id.story_list);
		listView.setAdapter(new StoryCursorAdapter(this, R.layout.story_list_item,
				storyCursor));
		listView.setOnItemClickListener(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case CONNECTION_FAILURE_RESOLUTION_REQUEST:
				switch (resultCode) {
					case Activity.RESULT_OK:
						// Try the request again
						break;
				}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_about:
				showAboutTagStoryDialog();
				break;
			case R.id.menu_story_marked:
				startMarkedActivity();
				break;
		}

		return true;
	}

	private void startMarkedActivity() {
		startActivity(new Intent(this, SimpleStoryMarkedActivity.class));
	}

	private void showAboutTagStoryDialog() {
		if (aboutTagStoryDialog == null) {
			aboutTagStoryDialog = DialogFactory.createAboutDialog(this, R.string.dialog_about_title,
					R.string.dialog_about_tagstory);
		}
		aboutTagStoryDialog.show();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		storyCursor.moveToPosition(position);
		Intent detailIntent = ClassVersionFactory.createIntent(getApplicationContext(),
				StoryDetailActivityHoneycomb.class, StoryDetailActivity.class);
		detailIntent.putExtra(Database.STORY_ID,
				storyCursor.getString(0));
		startActivity(detailIntent);
	}

	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.visit_marked:
				startMarkedActivity();
				break;
		}
	}
}
