package com.example.imgsecure;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private final int GALLERY_ACCESS = 100;

    private BottomNavigationView bottomNavigationView;
    private EncryptFragment encryptFragment;
    private DecryptFragment decryptFragment;
    private FrameLayout encryptFrame, decryptFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        initViews();
        requestPermissions();


        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.action_encrypt) {
                    decryptFrame.setVisibility(View.INVISIBLE);
                    encryptFrame.setVisibility(View.VISIBLE);
                    encryptFragment.clean();
                    return true;
                } else if (item.getItemId() == R.id.action_decrypt) {
                    decryptFrame.setVisibility(View.VISIBLE);
                    encryptFrame.setVisibility(View.INVISIBLE);
                    decryptFragment.clean();
                    return true;
                }
                return false;
            }
        });

    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.READ_MEDIA_IMAGES},
                    GALLERY_ACCESS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GALLERY_ACCESS) {
            // Check if the permissions were granted
            if (!(grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {

                requestPermissions();
            }
        }

    }

    private void initViews() {

        encryptFragment = new EncryptFragment(this);
        decryptFragment = new DecryptFragment(this);


        getSupportFragmentManager().beginTransaction().add(R.id.frame_encrypt, encryptFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.frame_decrypt, decryptFragment).commit();

        decryptFrame.setVisibility(View.INVISIBLE);
        encryptFrame.setVisibility(View.VISIBLE);
    }

    private void findViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        encryptFrame = findViewById(R.id.frame_encrypt);
        decryptFrame = findViewById(R.id.frame_decrypt);

    }
}