package activity;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;

import com.example.simplefitness.R;

import component.MyCalendarView;
import database.DatabaseHelper;
import model.Workout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddWorkoutActivity extends AppCompatActivity {
    private MyCalendarView myCalendarView;
    private Spinner spinnerType;
    private TextView tvSelectedTime;
    private EditText etDuration;
    private Spinner spinnerDifficulty;
    private Button btnSelectTime, btnSaveWorkout;
    private DatabaseHelper dbHelper;
    private List<String> workoutTypes;
    private List<String> difficultyTypes;

    private String selectedTime;


//    private boolean editMode;

    private Workout currentWorkout = null;
    private int idx = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_workout);


        // 接收传递过来的 Workout 对象
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("workout_item")) {
            currentWorkout = intent.getParcelableExtra("workout_item");
            idx = intent.getIntExtra("position", -1);
            // 如果 currentWorkout 不为空，则设置编辑模式
//            if (currentWorkout != null) {
//                // 设置编辑模式的 UI
//            }
        }

//        dbHelper = new DatabaseHelper(this);
        dbHelper = DatabaseHelper.getInstance(this);
        initView();
    }

    private void initView() {

        myCalendarView = findViewById(R.id.calendarView);

        workoutTypes = new ArrayList<>();
        workoutTypes.add("跑步");
        workoutTypes.add("搏击");
        workoutTypes.add("瑜伽");
        workoutTypes.add("舞蹈");

        difficultyTypes = new ArrayList<>();
        difficultyTypes.add("初级");
        difficultyTypes.add("中级");
        difficultyTypes.add("高级");

        spinnerType = findViewById(R.id.spinner_type);
        btnSelectTime = findViewById(R.id.btn_select_time);
        tvSelectedTime = findViewById(R.id.tv_selected_time);
        etDuration = findViewById(R.id.et_duration);
        spinnerDifficulty = findViewById(R.id.spinner_difficulty);
        btnSaveWorkout = findViewById(R.id.btn_save_workout);

        // 设置 Spinner 的数据
        spinnerType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, workoutTypes));
        spinnerType.setPrompt("选择健身类型");

        spinnerDifficulty.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, difficultyTypes));
        spinnerDifficulty.setPrompt("选择健身难度");

        // 设置选择时间按钮的点击事件
        // 设置选择时间按钮的点击事件
        btnSelectTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Set the selected day to the current day of the month
                myCalendarView.setSelectedDay(myCalendarView.calendar.get(Calendar.DAY_OF_MONTH));


                final Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);
                // 创建一个 TimePickerDialog 对话框
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddWorkoutActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                        // 当时间被设置时的操作
                        selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute);
                        tvSelectedTime.setText(selectedTime);
                    }
                }, hour, minute, android.text.format.DateFormat.is24HourFormat(AddWorkoutActivity.this));
                timePickerDialog.show();
            }
        });

        if (currentWorkout!= null){
            spinnerType.setSelection(workoutTypes.indexOf(currentWorkout.getType()));
            tvSelectedTime.setText(currentWorkout.getStartTime());
            etDuration.setText(String.valueOf(currentWorkout.getDuration()));
            spinnerDifficulty.setSelection(difficultyTypes.indexOf(currentWorkout.getDifficulty()));
        }

        btnSaveWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String type = (String) spinnerType.getSelectedItem();
                int duration = Integer.parseInt(etDuration.getText().toString());
                String difficulty = (String) spinnerDifficulty.getSelectedItem();

                if (currentWorkout == null){
                    System.out.println("add new workout");
                    currentWorkout = new Workout(type, selectedTime, duration, difficulty);
                    int id = dbHelper.addWorkout(currentWorkout);
                    currentWorkout.setId(id);

                    // 在 AddWorkoutActivity 中保存或更新健身项目后
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("newWorkout", currentWorkout);
                    setResult(Activity.RESULT_OK, returnIntent);

                }else{

                    System.out.println("update workout");
                    currentWorkout.setType(type);
                    currentWorkout.setStartTime(selectedTime);
                    currentWorkout.setDifficulty(difficulty);
                    currentWorkout.setDuration(duration);
                    dbHelper.updateWorkout(currentWorkout);

                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("position", idx);
                    returnIntent.putExtra("updateWorkout", currentWorkout);
                    setResult(Activity.RESULT_OK, returnIntent);
                }

                finish(); // 返回到前一个 Activity
            }
        });
    }
}