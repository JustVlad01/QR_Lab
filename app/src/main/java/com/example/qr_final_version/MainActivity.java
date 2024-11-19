package com.example.qr_final_version;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends AppCompatActivity {

    private EditText etName, etAddress;
    private Button buttonScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializing UI elements
        etName = findViewById(R.id.etName);
        etAddress = findViewById(R.id.etAddress);
        buttonScan = findViewById(R.id.buttonScan);

        // Set up the scan button
        buttonScan.setOnClickListener(view -> {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
            integrator.setPrompt("Scan a QR Code"); // Display a message
            integrator.setCameraId(1); // Use the back camera (0 is front)
            integrator.setBeepEnabled(true); // Play sound on scan
            integrator.setBarcodeImageEnabled(false); // Disable image saving
            integrator.initiateScan(); // Start scanning
        });

        // Handle clicking on the address field (try opening a browser)
        etAddress.setOnClickListener(view -> {
            String url = etAddress.getText().toString();
            if (isValidUrl(url)) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent); // Open the URL in a browser
            } else {
                Toast.makeText(this, "Invalid URL!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle scan result
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
            } else {
                parseQRCode(result.getContents()); // Process QR code content
            }
        }
    }

    private void parseQRCode(String content) {
        try {
            // Assume the QR code content is JSON
            org.json.JSONObject jsonObject = new org.json.JSONObject(content);
            String title = jsonObject.optString("title", "Unknown"); // Default to "Unknown" if no title
            String website = jsonObject.optString("website", ""); // Default to an empty string

            // Populate the fields
            etName.setText(title);
            etAddress.setText(website);

        } catch (org.json.JSONException e) {
            // If not JSON, check if it's a URL
            if (content.startsWith("http://") || content.startsWith("https://")) {
                etName.setText(getDomainFromUrl(content)); // Show domain name
                etAddress.setText(content); // Show full URL
            } else {
                // For unknown or unsupported content
                etName.setText("Unknown Content");
                etAddress.setText("");
                Toast.makeText(this, "Invalid QR Code content", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Helper function to extract domain name from a URL
    private String getDomainFromUrl(String url) {
        try {
            Uri uri = Uri.parse(url);
            return uri.getHost(); // Extract the domain (e.g., "www.example.com")
        } catch (Exception e) {
            return "Invalid URL"; // Return an error if parsing fails
        }
    }

    // Helper function to check if a string is a valid URL
    private boolean isValidUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }
}
