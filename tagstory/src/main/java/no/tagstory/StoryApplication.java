package no.tagstory;

import android.app.Application;
import android.location.Location;
import no.tagstory.statistics.StoryHistory;

import java.util.ArrayList;
import java.util.LinkedList;

public class StoryApplication extends Application {

	private Location startLocation;
	private ArrayList<Location> currentLocationHistoryList;
	private long startTime;

	private StoryHistory storyHistory;

	@Override
	public void onCreate() {
		super.onCreate();
		currentLocationHistoryList = new ArrayList<Location>();
		storyHistory = new StoryHistory();
	}

	public StoryHistory getStoryHistory() {
		return storyHistory;
	}

	@Deprecated
	public long getStartTime() {
		return startTime;
	}

	@Deprecated
	public void startTimer() {
		this.startTime = System.currentTimeMillis();
	}

	@Deprecated
	public void setStartLocation(Location startLocation) {
		this.startLocation = startLocation;
	}

	@Deprecated
	public float distanceFromStartTo(Location dest) {
		return startLocation.distanceTo(dest);
	}

	@Deprecated
	public boolean addLocation(Location location) {
		return currentLocationHistoryList.add(location);
	}

	@Deprecated
	public float distanceWalked() {
		float distance = 0f;
		Location last = null;
		for (Location l : currentLocationHistoryList) {
			if (last != null) {
				distance += last.distanceTo(l);
			}
			last = l;
		}
		return distance;
	}
}
