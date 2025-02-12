package fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.simplefitness.R;

import database.DatabaseHelper;
import model.User;

public class ProfileFragment extends Fragment {
    private EditText etWeight, etHeight, etAge;
//    private RadioGroup rgGoal;
    private RadioButton rbLoseWeight, rbMuscleGain, rbKeepFit;
    private Button btnSave;
    private SharedPreferences preferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        preferences = getActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);

        etWeight = view.findViewById(R.id.et_weight);
        etHeight = view.findViewById(R.id.et_height);
        etAge = view.findViewById(R.id.et_age);
//        rgGoal = view.findViewById(R.id.rg_goal);
        rbLoseWeight = view.findViewById(R.id.rb_lose_weight);
        rbMuscleGain = view.findViewById(R.id.rb_muscle_gain);
        rbKeepFit = view.findViewById(R.id.rb_keep_fit);

        btnSave = view.findViewById(R.id.btn_save);

        loadUserData();

        btnSave.setOnClickListener(v -> saveUserData());

        return view;
    }

    private void loadUserData() {
        float weight = preferences.getFloat("weight", 0);
        float height = preferences.getFloat("height", 0);
        int age = preferences.getInt("age", 0);
        String goal = preferences.getString("goal", "lose_weight");


        etWeight.setText(String.valueOf(weight));
        etHeight.setText(String.valueOf(height));
        etAge.setText(String.valueOf(age));

        switch (goal) {
            case "lose_weight":
                rbLoseWeight.setChecked(true);
                break;
            case "muscle_gain":
                rbMuscleGain.setChecked(true);
                break;
            case "keep_fit":
                rbKeepFit.setChecked(true);
                break;
        }
    }

    private void saveUserData() {
        float weight = Float.parseFloat(etWeight.getText().toString());
        float height = Float.parseFloat(etHeight.getText().toString());
        int age = Integer.parseInt(etAge.getText().toString());
        String goal = "";

        if (rbLoseWeight.isChecked()) {
            goal = "lose_weight";
        } else if (rbMuscleGain.isChecked()) {
            goal = "muscle_gain";
        } else if (rbKeepFit.isChecked()) {
            goal = "keep_fit";
        }

        String account = preferences.getString("account", "");
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("weight", weight);
        editor.putFloat("height", height);
        editor.putInt("age", age);
        editor.putString("goal", goal);
        editor.apply();

        User user = new User();
        user.setAccount(account);
        user.setWeight(weight);
        user.setHeight(height);
        user.setAge(age);
        user.setGoal(goal);

        //保存到数据库。
//        new DatabaseHelper(this.getContext()).updateUser(user);
        DatabaseHelper.getInstance(this.getContext()).updateUser(user);


        Toast.makeText(getContext(), "保存成功", Toast.LENGTH_SHORT).show();
    }
}
