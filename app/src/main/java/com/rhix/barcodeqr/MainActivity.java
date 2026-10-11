
package com.rhix.barcodeqr;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.rhix.barcodeqr.fragments.ApiBarcodeFragment;
import com.rhix.barcodeqr.fragments.ApiQrFragment;
import com.rhix.barcodeqr.fragments.NativeBarcodeFragment;
import com.rhix.barcodeqr.fragments.NativeQrFragment;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);

        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.nav_api_qr);
            openFragment(new ApiQrFragment(), "API Driven QR");
        }
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        String title = "";

        int itemId = item.getItemId();

        if (itemId == R.id.nav_api_qr) {
            fragment = new ApiQrFragment();
            title = "API Driven QR";

        } else if (itemId == R.id.nav_api_barcode) {
            fragment = new ApiBarcodeFragment();
            title = "API Driven Barcode";

        } else if (itemId == R.id.nav_native_qr) {
            fragment = new NativeQrFragment();
            title = "Native QR";

        } else if (itemId == R.id.nav_native_barcode) {
            fragment = new NativeBarcodeFragment();
            title = "Native Barcode";
        }

        if (fragment != null) {
            openFragment(fragment, title);
        }

        drawerLayout.closeDrawers();
        return true;
    }

    private void openFragment(Fragment fragment, String title) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();

        toolbar.setTitle(title);
    }
}
