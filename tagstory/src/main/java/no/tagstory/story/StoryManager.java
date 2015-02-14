package no.tagstory.story;

import android.content.Context;
import android.database.Cursor;
import no.tagstory.utils.Database;
import no.tagstory.utils.StoryParser;
import org.json.JSONException;

import java.io.IOException;

public class StoryManager {

	private Database database;
	private Context context;

	public StoryManager(Context context) {
		this.context = context;
		database = new Database(this.context);
		database.open();
	}

	public void closeDatabase() {
		database.close();
	}

	public Cursor getCursorOverStories() {
		// TODO Convert cursor to a StoryList-object and return a list
		return database.getStoryList();
	}

	public Story getStory(String id) {
		try {
			return StoryParser.parseJsonToStory(context, id);
		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean deleteStory(String id) {
		return database.deleteStory(id);
	}

	public boolean hasStory(String id) {
		return database.hasStory(id);
	}
}
