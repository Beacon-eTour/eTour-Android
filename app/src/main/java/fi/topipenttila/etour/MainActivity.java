package fi.topipenttila.etour;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

/**
 *
 * @author Topi Penttilä
 */
public class MainActivity extends FlutterActivity implements BeaconConsumer, EventChannel.StreamHandler {
    protected static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;
    private BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
    private static final String BEACONUUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    private EventChannel.EventSink eventSink = null;
    // This has to be exaclty this, otherwise Flutter doesn't understand
    private static final String ETOUR_CHANNEL_NAME = "etour_event_channel";
    private EventChannel etourChannel;

    // MARK: EventChannel.StreamHandler
    private void setupEventChannels(BinaryMessenger messenger) {
        etourChannel = new EventChannel(messenger, ETOUR_CHANNEL_NAME);
        etourChannel.setStreamHandler(this);
    }

    private void teardownEventChannels() {
        etourChannel.setStreamHandler(null);
    }

    void sendEventToFlutter(String event) {
        if (eventSink != null) {
            System.out.println(event);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    eventSink.success(event);
                }
            });

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        // Don't kill the app, only put it in the BG when back button tapped
        final MethodChannel channel = new MethodChannel(getFlutterEngine().getDartExecutor().getBinaryMessenger(), "android_app_retain");
        channel.setMethodCallHandler(new MethodChannel.MethodCallHandler() {
            @Override
            public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
                if (call.method.equals("sendToBackground")) {
                    moveTaskToBack(true);
                }
            }
        });
        setupEventChannels(getFlutterEngine().getDartExecutor().getBinaryMessenger());
        beaconManager.bind(this);
        verifyBluetooth();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (this.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        if (!this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setTitle("This app needs background location access");
                            builder.setMessage("Please grant location access so this app can detect beacons in the background.");
                            builder.setPositiveButton(android.R.string.ok, null);
                            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                                @TargetApi(23)
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                            PERMISSION_REQUEST_BACKGROUND_LOCATION);
                                }

                            });
                            builder.show();
                        }
                        else {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setTitle("Functionality limited");
                            builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons in the background.  Please go to Settings -> Applications -> Permissions and grant background location access to this app.");
                            builder.setPositiveButton(android.R.string.ok, null);
                            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                }

                            });
                            builder.show();
                        }
                    }
                }
            } else {
                if (!this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                            PERMISSION_REQUEST_FINE_LOCATION);
                }
                else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.  Please go to Settings -> Applications -> Permissions and grant location access to this app.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_FINE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "fine location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
            case PERMISSION_REQUEST_BACKGROUND_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "background location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void verifyBluetooth() {

        try {
            if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bluetooth not enabled");
                builder.setMessage("Please enable bluetooth in settings and restart this application.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        //finish();
                        //System.exit(0);
                    }
                });
                builder.show();
            }
        }
        catch (RuntimeException e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    //finish();
                    //System.exit(0);
                }

            });
            builder.show();

        }

    }

    @Override
    public void onBeaconServiceConnect() {

        RangeNotifier rangeNotifier = new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    ArrayList<Beacon> sortedBeaconList = new ArrayList<>(beacons);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        sortedBeaconList.removeIf(n -> (!n.getId1().equals(Identifier.fromUuid(UUID.fromString(BEACONUUID)))) || n.getDistance() > 3);
                    }
                    if (sortedBeaconList.size() > 0) {
                        Collections.sort(sortedBeaconList, new Comparator<Beacon>() {
                            @Override
                            public int compare(Beacon c1, Beacon c2) {
                                return Double.compare(c1.getDistance(), c2.getDistance());
                            }
                        });
                        Beacon firstBeacon = sortedBeaconList.iterator().next();
                        sendEventToFlutter(firstBeacon.getId2().toString());
                    }
                }
            }

        };
        try {
            beaconManager.startRangingBeaconsInRegion(new Region(BEACONUUID, null, null, null));
            beaconManager.addRangeNotifier(rangeNotifier);
        } catch (RemoteException e) {
            Log.d(TAG, "exception " + e);
        }
    }

    @Override
    public void onListen(Object arguments, EventChannel.EventSink events) {
        eventSink = events;
    }

    @Override
    public void onCancel(Object arguments) {
        eventSink = null;
    }
}