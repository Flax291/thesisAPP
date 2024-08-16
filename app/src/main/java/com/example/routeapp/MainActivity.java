package com.example.routeapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private LocationManager locationManager;
    private RouteView routeView;
    private Location lastLocation;
    private Location targetLocation;
    private Location currentLocation;
    private Location preCurrentLocation;
    private ImageView compass;
    private float currentAzimuth = 0;
    private Button startButton;
    private Button openMarkerSelectionButton;
    private boolean isRouteActive = false;
    private int selectedMarkerResource;
    private BottomSheetDialog bottomSheetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        routeView = findViewById(R.id.routeView);
        compass = findViewById(R.id.compass);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        startButton = findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routeView.clearPoints();
                isRouteActive = true;
                if (currentLocation != null) {
                    routeView.addCustomMarker(currentLocation, R.drawable.camping);
                    setTargetLocation(currentLocation);
                }
                startTracking();
            }
        });

        openMarkerSelectionButton = findViewById(R.id.open_marker_selection);
        openMarkerSelectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMarkerSelectionDialog(); // Показать диалоговое окно с выбором маркеров
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startTracking();
        }
    }

    private void startTracking() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5, this);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (!isRouteActive) return; // Если маршрут не активен, ничего не делаем

        currentLocation = location;
        if (lastLocation == null || lastLocation.distanceTo(location) >= 10) {
            routeView.addPoint(location);
            lastLocation = location;
        }
        if (targetLocation != null) {
            updateCompass(currentLocation, targetLocation);
        }
        routeView.setCurrentLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(@NonNull String provider) {}

    @Override
    public void onProviderDisabled(@NonNull String provider) {}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTracking();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateCompass(Location currentLocation, Location targetLocation) {
        if (preCurrentLocation != null && currentLocation != null && targetLocation != null) {

            double dx1 = currentLocation.getLongitude() - preCurrentLocation.getLongitude();
            double dy1 = currentLocation.getLatitude() - preCurrentLocation.getLatitude();

            double dx2 = targetLocation.getLongitude() - currentLocation.getLongitude();
            double dy2 = targetLocation.getLatitude() - currentLocation.getLatitude();

            double angle = Math.toDegrees(Math.atan2(dx1 * dy2 - dy1 * dx2, dx1 * dx2 + dy1 * dy2));
            angle = -angle;

            compass.setRotation((float) angle);
        }
        preCurrentLocation = currentLocation;
    }

    private void showMarkerSelectionDialog() {
        bottomSheetDialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.marker_selection_dialog, null);
        bottomSheetDialog.setContentView(dialogView);

        ImageView mushroomsMarker = dialogView.findViewById(R.id.mushrooms_marker);
        ImageView berriesMarker = dialogView.findViewById(R.id.berries_marker);
        ImageView campingMarker = dialogView.findViewById(R.id.camping_marker);

        mushroomsMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLocation != null) {
                    routeView.addCustomMarker(currentLocation, R.drawable.mushrooms);
                }
                bottomSheetDialog.dismiss();
            }
        });

        berriesMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLocation != null) {
                    routeView.addCustomMarker(currentLocation, R.drawable.berries);
                }
                bottomSheetDialog.dismiss();
            }
        });

        campingMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLocation != null) {
                    routeView.addCustomMarker(currentLocation, R.drawable.camping);
                }
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.show();
    }


    public boolean isRouteActive() {
        return isRouteActive;
    }
    public void setTargetLocation(Location location) {
        targetLocation = location;
        if (isRouteActive) {
            updateCompass(currentLocation, targetLocation);
        }
        Toast.makeText(MainActivity.this, "Установленна целевая точка: Широта: " + targetLocation.getLatitude() + ", Долгота: " + targetLocation.getLongitude(), Toast.LENGTH_SHORT).show();
    }
}
