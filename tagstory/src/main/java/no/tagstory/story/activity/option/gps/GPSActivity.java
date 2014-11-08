package no.tagstory.story.activity.option.gps;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import no.tagstory.StoryApplication;
import no.tagstory.R;
import no.tagstory.story.activity.StoryTravelActivity;

import static no.tagstory.utils.GooglePlayServiceUtils.ErrorDialogFragment;
import static no.tagstory.utils.GooglePlayServiceUtils.servicesConnected;

public class GPSActivity extends StoryTravelActivity implements
		LocationListener, GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	protected Location goalLocation;

	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private LocationClient mLocationClient;
	// private Location mCurrentLocation;
	private LocationRequest mLocationRequest;
	private boolean mUpdatesRequested;
	private SharedPreferences mPrefs;
	private Editor mEditor;
	private AlertDialog gpsInfoDialog;

	// Milliseconds per second
	private static final int MILLISECONDS_PER_SECOND = 1000;
	// Update frequency in seconds
	public static final int UPDATE_INTERVAL_IN_SECONDS = 2;
	// Update frequency in milliseconds
	private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND
			* UPDATE_INTERVAL_IN_SECONDS;
	// The fastest update frequency, in seconds
	private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
	// A fast frequency ceiling in milliseconds
	private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND
			* FASTEST_INTERVAL_IN_SECONDS;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mPrefs = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
		mEditor = mPrefs.edit();
		mLocationClient = new LocationClient(this, this, this);
		// Start with updates turned on
		mUpdatesRequested = true;
		mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
		mEditor.commit();

		// Create the LocationRequest object
		mLocationRequest = LocationRequest.create();
		// Use high accuracy
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		// Set the update interval to 2 seconds
		mLocationRequest.setInterval(UPDATE_INTERVAL);
		// Set the fastest update interval to 1 second
		mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
		servicesConnected(this);
		// hintText.setText(R.string.gps_about_button);

		// TODO: Provider should be set in the json file
		setTitle(R.string.map_next_tag);
		goalLocation = new Location("goal"); // <-- provider
		goalLocation.setLatitude(option.getLatitude());
		goalLocation.setLongitude(option.getLongitude());
	}

	@Override
	public void scanTag(View v) {
		if (v.getId() == R.id.help_button) {
			showGpsDialog();
		}
	}

	private void showGpsDialog() {
		if (gpsInfoDialog == null) {
			gpsInfoDialog = createGpsDialog();
		}
		gpsInfoDialog.show();
	}

	private AlertDialog createGpsDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.gps_about_title);
		builder.setMessage(R.string.gps_about_content);
		builder.setCancelable(true);
		builder.setNeutralButton(R.string.dialog_cancel,
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
					                    int which) {
						dialog.cancel();
					}
				});
		builder.setNegativeButton(R.string.skip,
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
					                    int which) {
						skipTag();
					}
				});
		return builder.create();
	}

	@Override
	protected void onStart() {
		super.onStart();
		mLocationClient.connect();
	}

	@Override
	protected void onStop() {
		if (mLocationClient.isConnected()) {
			stopPeriodicUpdates();
		}
		mLocationClient.disconnect();
		super.onStop();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
		mEditor.commit();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mPrefs.contains("KEY_UPDATES_ON")) {
			mUpdatesRequested = mPrefs.getBoolean("KEY_UPDATES_ON", false);
		} else {
			mEditor.putBoolean("KEY_UPDATES_ON", true);
			mEditor.commit();
		}
	}

	/**
	 * In response to a request to start updates, send a request to Location
	 * Services
	 */
	private void startPeriodicUpdates() {
		mLocationClient.requestLocationUpdates(mLocationRequest, this);
	}

	/**
	 * In response to a request to stop updates, send a request to Location
	 * Services
	 */
	private void stopPeriodicUpdates() {
		mLocationClient.removeLocationUpdates(this);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case CONNECTION_FAILURE_RESOLUTION_REQUEST:
				switch (resultCode) {
					case Activity.RESULT_OK:
				/*
				 * Try the request again
				 */
						break;
				}
		}
	}

	@Override
	public void onConnected(Bundle dataBundle) {
		Log.d("GPS", "Connected");
		if (mUpdatesRequested) {
			startPeriodicUpdates();
		}
	}

	@Override
	public void onDisconnected() {
		Toast.makeText(this, "Disconnected. Please re-connect GPS to continue.",
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onLocationChanged(Location location) {
		((StoryApplication) getApplication()).addLocation(location);
		if (goalLocation != null) {
			Log.d("GPS",
					"Distance to goal " + location.distanceTo(goalLocation)
							+ " meters");
			if (location.distanceTo(goalLocation) < 20) {
				Log.d("GPS", "Closer then 20 meters to location");
				checkTagData(story.getTag(option.getOptNext())
						.getBelongsToTag());
			}
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if (connectionResult.hasResolution()) {
			try {
				connectionResult.startResolutionForResult(this,
						CONNECTION_FAILURE_RESOLUTION_REQUEST);
			} catch (IntentSender.SendIntentException e) {
				e.printStackTrace();
			}
		} else {
			showErrorDialog(connectionResult.getErrorCode());
		}
	}

	/**
	 * Show a dialog returned by Google Play services for the connection error
	 * code
	 *
	 * @param errorCode An error code returned from onConnectionFailed
	 */
	private void showErrorDialog(int errorCode) {
		Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode,
				this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

		if (errorDialog != null) {
			ErrorDialogFragment errorFragment = new ErrorDialogFragment();
			errorFragment.setDialog(errorDialog);
			errorFragment.show(getSupportFragmentManager(), "LocationTester");
		}
	}
}