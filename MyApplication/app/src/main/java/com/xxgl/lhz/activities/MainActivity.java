package com.xxgl.lhz.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.xxgl.lhz.fragments.ProfileFragment;
import com.xxgl.lhz.fragments.RentalManagementFragment;
import com.example.myapplication.R;
import com.xxgl.lhz.fragments.CustomerManagementFragment;
import com.xxgl.lhz.fragments.VehicleManagementFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String PREF_NAME = "CarRentalPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private BottomNavigationView bottomNav;
    private FragmentManager fragmentManager;
    private VehicleManagementFragment vf;
    private CustomerManagementFragment cf;
    private RentalManagementFragment rf;
    private ProfileFragment pf;
    private Fragment currentFragment;
    private int currentItemId;
//    private int currentFragmentId = -1;

    public static int count = 0;

    public MainActivity(){
        System.out.println("MainActivity Constructor");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("MainActivity onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        bottomNav = findViewById(R.id.bottom_navigation);
        fragmentManager = getSupportFragmentManager();

        System.out.println("MainActivity onCreate fragmentManager :" + fragmentManager);
        // Set up bottom navigation
        setupBottomNavigation();

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(R.id.nav_cars);
        } else {

            System.out.println("MainActivity onCreate currentFragment:"+ fragmentManager.getFragment(savedInstanceState, "currentFragment"));
            currentFragment = fragmentManager.getFragment(savedInstanceState, "currentFragment");
            // Restore current fragment from saved state
            loadFragment(-1);
 //            if (currentFragment == null) {
//                currentFragment = new VehicleManagementFragment();
//            }
//            loadFragment(currentFragment);
        }

        // Check if user is logged in
        checkLoginStatus();
    }

    private void checkLoginStatus() {
        // Check if user is logged in
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        if (!isLoggedIn) {
            // User not logged in, redirect to login screen
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                //避免重复点击。
                if (currentItemId == itemId)
                    return true;

                currentItemId = itemId;
                loadFragment(itemId);
                return true;

//                if (fragment != null) {
//                    loadFragment(fragment);
//                    return true;
//                }
//                return false;
            }
        });
    }

    private void loadFragment(int itemId) {

        if (itemId == -1){
            System.out.println("loadFragment currentFragment:" + currentFragment);
            fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, currentFragment, "lxr" + count++)
                .commit();
            return;
        }

        List<Fragment> lfs = fragmentManager.getFragments();

        // 按照维持四个fragment的话，fragmentManager最多会有5个，即包含currentFragment的。 每次撤销后，fragmentManager会重置，新的。
        for (Fragment f : lfs){
            System.out.println("in fragmentManager:" + f);

        }
        for (int i = 0; i < count; i++){
            System.out.println("count:" + fragmentManager.findFragmentByTag("lxr"+i));
        }
//        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
        System.out.println("loadFragment currentFragment:" + currentFragment);

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        Fragment nowFg = null;
        if (itemId == R.id.nav_cars) {
            if (vf == null){
                System.out.println("loadFragment new vf");
                System.out.println("loadFragment new vf id:" + itemId);
                vf = new VehicleManagementFragment();
                System.out.println("loadFragment new vf id:" + vf.getId());
                transaction.add(R.id.fragment_container, vf);
                transaction.hide(vf);
            }
            nowFg = vf;
        } else if (itemId == R.id.nav_customers) {
            if (cf == null){
                System.out.println("loadFragment new cf");
                cf = new CustomerManagementFragment();
                transaction.add(R.id.fragment_container, cf);
                transaction.hide(cf);
            }
            nowFg = cf;
        } else if (itemId == R.id.nav_rental) {
            if (rf == null){
                System.out.println("loadFragment new rf");
                rf = new RentalManagementFragment();
                transaction.add(R.id.fragment_container, rf);
                transaction.hide(rf);
            }
            nowFg = rf;
        }else if (itemId == R.id.nav_profile) {
            if (pf == null) {
                System.out.println("loadFragment new pf");
                pf = new ProfileFragment();
                transaction.add(R.id.fragment_container, pf);
                transaction.hide(pf);
            }
            nowFg = pf;
        }

        System.out.println("loadFragment nowFg:" + nowFg);

        if (currentFragment != null){
            transaction.hide(currentFragment);
        }
        transaction.show(nowFg).commit();
        currentFragment = nowFg;


        //使用replace 可以清楚旧有的fragment。 虽然牺牲了性能，每次点击切换fragment都要删除、重建，但是好处在于处理简单，而且维护方便。即每次fragmentmanager只维护一个fragment。
        //如果使用不同的fragment切换，虽然有切换时效率搞。 但是在onSaveInstanceState有点复杂，而且fragmentmanager都要维护全部四个的fragment。。

//        currentFragment = fragment;
//        fragmentManager.beginTransaction()
//                .replace(R.id.fragment_container, fragment)
//                .commit();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        System.out.println("MainActivity onSaveInstanceState");
        super.onSaveInstanceState(outState);
        if (currentFragment != null) {
            fragmentManager.putFragment(outState, "currentFragment", currentFragment);
            System.out.println("MainActivity onSaveInstanceState currentFragment id: " +  currentFragment.getId());
            System.out.println("MainActivity onSaveInstanceState currentFragment:"+ currentFragment);
        }
//        fragmentManager.putFragment(outState, "currentFragment", fragmentManager.findFragmentById(R.id.fragment_container));
    }
}