package activity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.simplefitness.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import fragment.ProfileFragment;
import fragment.WorkoutFragment;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // 设置默认选中的菜单项
        bottomNavigationView.setSelectedItemId(R.id.navigation_workout);

        // 设置默认显示的Fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new WorkoutFragment())
                    .commit();
        }


        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int item_id = item.getItemId();
            if (item_id == R.id.navigation_workout) {
                selectedFragment = new WorkoutFragment();
            } else if (item_id == R.id.navigation_profile) {
                selectedFragment = new ProfileFragment();
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
            return true;
        });
    }
}
