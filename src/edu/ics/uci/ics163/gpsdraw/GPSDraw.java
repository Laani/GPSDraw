package edu.ics.uci.ics163.gpsdraw;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import edu.uci.ics.ics163.gpsdrawupload.Point;
import edu.uci.ics.ics163.gpsdrawupload.StrokeManager;

public class GPSDraw extends FragmentActivity implements GooglePlayServicesClient.ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
	
	static LocationClient mLocationClient;
	static LocationRequest mLocationRequest;
	static StrokeManager mStrokeManager;
	String lastLocation;
	static boolean mRequestUpdates;
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	
	// Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 3;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    
    private static String strokeName;
    private static int currentStroke;
	private static boolean penDown;
	private static int r = 255;
	private static int g = 255;
	private static int b = 0;
	
	public static class ErrorDialogFragment extends DialogFragment {
		private Dialog mDialog;
		
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}
		
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}
		
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gpsdraw);

		mRequestUpdates = true;
		
		mLocationClient = new LocationClient(this, this, this);
		
		mLocationRequest = LocationRequest.create();
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		mLocationRequest.setInterval(UPDATE_INTERVAL);
		mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
		
		mStrokeManager = new StrokeManager();

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}
	
	protected void onStart() {
		super.onStart();
		mLocationClient.connect();
	}
	
	protected void onStop() {
        mLocationClient.disconnect();
        super.onStop();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.gpsdraw, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void updateUI() {
		runOnUiThread(new Runnable() {
			public void run() {
				if (PlaceholderFragment.location != null && lastLocation != null) {
					PlaceholderFragment.location.setText(lastLocation);
				}
				
				if (PlaceholderFragment.dataDisplay != null) {
					PlaceholderFragment.dataDisplay.setText(mStrokeManager.countStrokes() + "/" + mStrokeManager.countPoints());
				}
			}
		});
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment implements OnCheckedChangeListener {
		
		String groupName;
		String drawingName;
		
		EditText groupid;
		EditText drawingid;
		public static TextView location;
		Switch pen;
		RadioGroup color;
		Button colorSubmit;
		public static TextView dataDisplay;
		Button upload;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_gpsdraw,
					container, false);
			
			currentStroke = 0;
			groupName = "jknw";
			drawingName = "testdraw";
			
			groupid = (EditText) rootView.findViewById(R.id.groupid);
			drawingid = (EditText) rootView.findViewById(R.id.drawingid);
			location = (TextView) rootView.findViewById(R.id.location);
			pen = (Switch) rootView.findViewById(R.id.switch1);
			color = (RadioGroup) rootView.findViewById(R.id.radioGroup1);
			colorSubmit = (Button) rootView.findViewById(R.id.colorsubmit);
			dataDisplay = (TextView) rootView.findViewById(R.id.datapoints);
			upload = (Button) rootView.findViewById(R.id.upload);
						
			pen.setOnCheckedChangeListener(this);
			
			colorSubmit.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                	if (!penDown) {
	                	int col = color.getCheckedRadioButtonId();
	                	if (col == R.id.yellow) {
	                		r = 255;
	                		g = 255;
	                		b = 0;
	                	}
	                	else if (col == R.id.black) {
	                		r = 0;
	                		g = 0;
	                		b = 0;
	                	}
	                	else if (col == R.id.red) {
	                		r = 255;
	                		g = 0;
	                		b = 0;
	                	}
                	}
                }
            });
			
			upload.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                	if (groupid.getText().toString() != "" && drawingid.getText().toString() != "") {
                		groupName = groupid.getText().toString();
                		drawingName = drawingid.getText().toString();
                	}
                	mStrokeManager.upload(groupName, drawingName);
            		Toast.makeText(getActivity(), "Strokes uploading...", Toast.LENGTH_SHORT).show();
                }
            });
			
			return rootView;
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (isChecked) {
				penDown = true;
				currentStroke++;
				strokeName = "" + System.currentTimeMillis();
				mStrokeManager.setStrokeColor(strokeName, r, g, b);
				Toast.makeText(getActivity(), strokeName + ": " + r + ", " + g + ", " + b, Toast.LENGTH_SHORT).show();
			} else {
				penDown = false;
			}
		}
	}

	@Override
	public void onConnected(Bundle arg0) {
		Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
		if (mRequestUpdates) {
            mLocationClient.requestLocationUpdates(mLocationRequest, this);
        }
	}

	@Override
	public void onDisconnected() {
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (result.hasResolution()) {
			try {
				result.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
			} catch (IntentSender.SendIntentException e) {
				e.printStackTrace();
			}
		} else {
			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
			if (errorDialog != null) {
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				errorFragment.setDialog(errorDialog);
				errorFragment.show(getFragmentManager(), "Location Updates");
			}
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		lastLocation = "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
		if (penDown) {
			Point dataPoint = new Point(System.currentTimeMillis(), location.getLatitude(), location.getLongitude());
			mStrokeManager.addPoint(strokeName, dataPoint);
		}
		updateUI();
	}
}