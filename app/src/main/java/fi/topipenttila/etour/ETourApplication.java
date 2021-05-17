package fi.topipenttila.etour;

import android.util.Log;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;

import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.altbeacon.beacon.startup.BootstrapNotifier;

import io.flutter.app.FlutterApplication;

/**
 * Created by Topi Penttilä
 */

public class ETourApplication extends FlutterApplication implements BootstrapNotifier {

    private static final String TAG = "eTour";
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;
    BeaconManager beaconManager = null;
    private static final String BEACONUUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";

    public void onCreate() {
        super.onCreate();

        beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().clear();
        // iBeacon layout
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        beaconManager.setDebug(false);

        // Uncomment the code below to use a foreground service to scan for beacons. This unlocks
        // the ability to continually scan for long periods of time in the background on Andorid 8+
        // in exchange for showing an icon at the top of the screen and a always-on notification to
        // communicate to users that your app is using resources in the background.
        //
        /*Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentTitle("Scanning for Beacons");
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(pendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("My Notification Channel ID",
                    "My Notification Name", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("My Notification Channel Description");
            NotificationManager notificationManager = (NotificationManager) getSystemService(
                    Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channel.getId());
        }
        beaconManager.enableForegroundServiceScanning(builder.build(), 456);*/

        // For the above foreground scanning service to be useful, you need to disable
        // JobScheduler-based scans (used on Android 8+) and set a fast background scan
        // cycle that would otherwise be disallowed by the operating system.
        //
        beaconManager.setEnableScheduledScanJobs(false);
        beaconManager.setBackgroundBetweenScanPeriod(0);
        beaconManager.setBackgroundScanPeriod(2200);
        beaconManager.setForegroundBetweenScanPeriod(2200);


        Log.d(TAG, "setting up background monitoring for beacons and power saving");
        // wake up the app when a beacon is seen
        Region region = new Region(BEACONUUID,
                null, null, null);
        regionBootstrap = new RegionBootstrap(this, region);

        // Constructing this class and holding a reference to it saves battery
        backgroundPowerSaver = new BackgroundPowerSaver(this);
    }

    @Override
    public void didEnterRegion(Region arg0) {
        Log.d(TAG, "did enter region.");
        Log.d(TAG, "Sending notification.");

    }

    @Override
    public void didExitRegion(Region region) {

    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
    }

}
