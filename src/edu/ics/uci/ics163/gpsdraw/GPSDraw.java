package edu.ics.uci.ics163.gpsdraw;

import java.util.ArrayList;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

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

public class GPSDraw extends FragmentActivity implements GooglePlayServicesClient.ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
	
	static LocationClient mLocationClient;
	static LocationRequest mLocationRequest;
	String lastLocation;
	static boolean mRequestUpdates;
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	
	// Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    
    private static int currentStroke;
	private static ArrayList<Pair> strokes;
	private static int dataPoints;
	private static boolean penDown;
	
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
					PlaceholderFragment.dataDisplay.setText(String.valueOf(dataPoints));
				}
			}
		});
	}
	
	public class Pair<L,R> {

		  private final L left;
		  private final R right;

		  public Pair(L left, R right) {
		    this.left = left;
		    this.right = right;
		  }

		  public L getLeft() { return left; }
		  public R getRight() { return right; }

	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment implements OnCheckedChangeListener {
		
		int currentColor;
		String groupName;
		String drawingName;
		
		EditText groupid;
		EditText drawingid;
		public static TextView location;
		Switch pen;
		RadioGroup color;
		public static TextView dataDisplay;
		Button upload;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_gpsdraw,
					container, false);
			
			strokes = new ArrayList<Pair>();
			currentStroke = 0;
			dataPoints = 0;
			groupName = "JKNW";
			drawingName = "testDraw";
			currentColor = 0;
			
			groupid = (EditText) rootView.findViewById(R.id.groupid);
			drawingid = (EditText) rootView.findViewById(R.id.drawingid);
			location = (TextView) rootView.findViewById(R.id.location);
			pen = (Switch) rootView.findViewById(R.id.switch1);
			color = (RadioGroup) rootView.findViewById(R.id.radioGroup1);
			dataDisplay = (TextView) rootView.findViewById(R.id.datapoints);
			upload = (Button) rootView.findViewById(R.id.upload);
						
			pen.setOnCheckedChangeListener(this);
			
			color.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                	int col = color.getCheckedRadioButtonId();
                	if (col == R.id.red)
                		currentColor = 0;
                	else if (col == R.id.green)
                		currentColor = 1;
                	else if (col == R.id.blue)
                		currentColor = 2;
                }
            });
			
			upload.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                	if (groupid.getText().toString() != "" && drawingid.getText().toString() != "") {
                		groupName = groupid.getText().toString();
                		drawingName = drawingid.getText().toString();
                	}
                	// upload shit.. something like:
                	//	for (int i = 0; i < strokes.size(); i++) {
                	//		upload(groupName, drawingName, i, timestamp, currentColor, strokes.get(i).getLeft(), strokes.get(i).getRight());
                	//	}
                }
            });
			
			return rootView;
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (isChecked) {
				penDown = true;
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
		lastLocation = "(" + location.getLatitude() + "," + location.getLongitude() + ")";
		if (penDown) {
			Pair<Double, Double> tuple = new Pair<Double, Double>(location.getLatitude(), location.getLongitude());
			strokes.add(tuple);
			currentStroke++;
			dataPoints++;
		}
		updateUI();
	}
}
