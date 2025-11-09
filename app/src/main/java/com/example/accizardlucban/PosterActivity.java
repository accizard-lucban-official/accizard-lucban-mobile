package com.example.accizardlucban;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class PosterActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;
    private static final int LOCATION_PERMISSION_REQUEST = 100;
    private static final int CAMERA_PERMISSION_REQUEST = 101;

    private EditText incidentDescriptionEdit;
    private Spinner incidentTypeSpinner;
    private TextView locationText;
    private ImageView incidentImageView;
    private Button takePhotoButton;
    private Button selectImageButton;
    private Button getLocationButton;
    private Button submitReportButton;
    private ImageView backButton;

    private FusedLocationProviderClient fusedLocationClient;
    private Bitmap incidentImage;
    private String currentLocation = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poster);

        initializeViews();
        setupClickListeners();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void initializeViews() {
        incidentDescriptionEdit = findViewById(R.id.incidentDescriptionEdit);
        incidentTypeSpinner = findViewById(R.id.incidentTypeSpinner);
        locationText = findViewById(R.id.locationText);
        incidentImageView = findViewById(R.id.incidentImageView);
        takePhotoButton = findViewById(R.id.takePhotoButton);
        selectImageButton = findViewById(R.id.selectImageButton);
        getLocationButton = findViewById(R.id.getLocationButton);
        submitReportButton = findViewById(R.id.submitReportButton);
        backButton = findViewById(R.id.backButton);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCameraPermissionAndTakePhoto();
            }
        });

        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImageFromGallery();
            }
        });

        getLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLocation();
            }
        });

        submitReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitIncidentReport();
            }
        });
    }

    private void checkCameraPermissionAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        } else {
            takePhoto();
        }
    }

    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            currentLocation = "Lat: " + String.format("%.6f", location.getLatitude()) +
                                    ", Lng: " + String.format("%.6f", location.getLongitude());
                            locationText.setText(currentLocation);
                            Toast.makeText(PosterActivity.this, "Location obtained successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(PosterActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void submitIncidentReport() {
        String description = incidentDescriptionEdit.getText().toString().trim();
        String incidentType = incidentTypeSpinner.getSelectedItem().toString();

        if (description.isEmpty()) {
            Toast.makeText(this, "Please enter incident description", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentLocation.isEmpty()) {
            Toast.makeText(this, "Please get your current location", Toast.LENGTH_SHORT).show();
            return;
        }

        // Simulate report submission
        Toast.makeText(this, "Incident report submitted successfully!", Toast.LENGTH_LONG).show();

        // Create confirmation dialog or send to server
        showSubmissionConfirmation(description, incidentType);
    }

    private void showSubmissionConfirmation(String description, String incidentType) {
        // You can implement a custom dialog here or navigate back
        Toast.makeText(this, "Report Type: " + incidentType + "\nLocation: " + currentLocation, Toast.LENGTH_LONG).show();

        // Optional: Navigate back to main activity
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    Bundle extras = data.getExtras();
                    incidentImage = (Bitmap) extras.get("data");
                    incidentImageView.setImageBitmap(incidentImage);
                    incidentImageView.setVisibility(View.VISIBLE);
                    break;

                case REQUEST_IMAGE_GALLERY:
                    Uri selectedImage = data.getData();
                    incidentImageView.setImageURI(selectedImage);
                    incidentImageView.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation();
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                }
                break;

            case CAMERA_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto();
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}