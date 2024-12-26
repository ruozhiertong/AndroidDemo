package database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import model.User;
import model.Workout;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "FitnessApp.db";
    private static final int DATABASE_VERSION = 2;

    // 用户表
    private static final String TABLE_USER = "users";
    private static final String COLUMN_NICKNAME = "nickname";
    private static final String COLUMN_ACCOUNT = "account"; //主键
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_WEIGHT = "weight";
    private static final String COLUMN_HEIGHT = "height";
    private static final String COLUMN_AGE = "age";
    private static final String COLUMN_GOAL = "goal";

    // 健身表
    private static final String TABLE_WORKOUT = "workouts";
    private static final String COLUMN_WORKOUT_ID = "id"; // 主键
    private static final String COLUMN_WORKOUT_TYPE = "type"; // 健身类型
    private static final String COLUMN_WORKOUT_START_TIME = "start_time"; // 开始时间
    private static final String COLUMN_WORKOUT_DURATION = "duration"; // 健身时间
    private static final String COLUMN_WORKOUT_DIFFICULTY = "difficulty"; // 健身难度




    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + COLUMN_NICKNAME + " TEXT,"
                + COLUMN_ACCOUNT + " TEXT PRIMARY KEY,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_WEIGHT + " REAL,"
                + COLUMN_HEIGHT + " REAL,"
                + COLUMN_AGE + " INTEGER,"
                + COLUMN_GOAL +  " TEXT)";
        db.execSQL(CREATE_USER_TABLE);

        // 创建健身表
        String CREATE_WORKOUT_TABLE = "CREATE TABLE " + TABLE_WORKOUT + "("
                + COLUMN_WORKOUT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_WORKOUT_TYPE + " TEXT,"
                + COLUMN_WORKOUT_START_TIME + " TEXT,"
                + COLUMN_WORKOUT_DURATION + " INTEGER,"
                + COLUMN_WORKOUT_DIFFICULTY + " TEXT)";
        db.execSQL(CREATE_WORKOUT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORKOUT); // 删除健身表
        onCreate(db);
    }

    public void addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NICKNAME, user.getNickname());
        values.put(COLUMN_ACCOUNT, user.getAccount());
        values.put(COLUMN_PASSWORD, user.getPassword());
        values.put(COLUMN_WEIGHT, user.getWeight());
        values.put(COLUMN_HEIGHT, user.getHeight());
        values.put(COLUMN_AGE, user.getAge());
        values.put(COLUMN_GOAL, user.getGoal());
        db.insert(TABLE_USER, null, values);
        db.close();
    }


    @SuppressLint("Range")
    public User getUser(String account, String password) {
        // 获取当前登录用户的详细信息
        // 此处简化实现，实际应用中需要更复杂的登录状态管理
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER,
                null,
                COLUMN_ACCOUNT + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{account, password}, null, null, null);

        User user = null;
        if (cursor.moveToFirst()) {
            user = new User();
            user.setNickname(cursor.getString(cursor.getColumnIndex(COLUMN_NICKNAME)));
            user.setAccount(cursor.getString(cursor.getColumnIndex(COLUMN_ACCOUNT)));
            user.setPassword(cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD)));
            user.setWeight(cursor.getDouble(cursor.getColumnIndex(COLUMN_WEIGHT)));
            user.setHeight(cursor.getDouble(cursor.getColumnIndex(COLUMN_HEIGHT)));
            user.setAge(cursor.getInt(cursor.getColumnIndex(COLUMN_AGE)));
            user.setGoal(cursor.getString(cursor.getColumnIndex(COLUMN_GOAL)));
        }
        cursor.close();
        return user;
    }

    public void updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_WEIGHT, user.getWeight());
        values.put(COLUMN_HEIGHT, user.getHeight());
        values.put(COLUMN_AGE, user.getAge());
        values.put(COLUMN_GOAL, user.getGoal());

        db.update(TABLE_USER, values, COLUMN_ACCOUNT + "=?",
                new String[]{user.getAccount()});
        db.close();
    }


    // 添加健身记录
    public int addWorkout(Workout workout) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_WORKOUT_TYPE, workout.getType());
        values.put(COLUMN_WORKOUT_START_TIME, workout.getStartTime());
        values.put(COLUMN_WORKOUT_DURATION, workout.getDuration());
        values.put(COLUMN_WORKOUT_DIFFICULTY, workout.getDifficulty());

        int ret =(int)db.insert(TABLE_WORKOUT, null, values);
        db.close();
        return ret;
    }

    public void updateWorkout(Workout workout) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_WORKOUT_TYPE, workout.getType());
        values.put(COLUMN_WORKOUT_START_TIME,workout.getStartTime());
        values.put(COLUMN_WORKOUT_DURATION, workout.getDuration());
        values.put(COLUMN_WORKOUT_DIFFICULTY, workout.getDifficulty());
        db.update(TABLE_WORKOUT, values, COLUMN_WORKOUT_ID + "=?",
                new String[]{String.valueOf(workout.getId())});
        db.close();
    }

    @SuppressLint("Range")
    public List<Workout> getAllWorkoutItems() {
        List<Workout> workoutItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_WORKOUT, null);
        if (cursor.moveToFirst()) {
            do {
                int id  = cursor.getInt(cursor.getColumnIndex(COLUMN_WORKOUT_ID));
                String type = cursor.getString(cursor.getColumnIndex(COLUMN_WORKOUT_TYPE));
                String startTime = cursor.getString(cursor.getColumnIndex(COLUMN_WORKOUT_START_TIME));
                int duration = cursor.getInt(cursor.getColumnIndex(COLUMN_WORKOUT_DURATION));
                String difficulty = cursor.getString(cursor.getColumnIndex(COLUMN_WORKOUT_DIFFICULTY));

                Workout wo = new Workout(id, type, startTime, duration, difficulty);
                workoutItems.add(wo);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return workoutItems;
    }

    public void deleteWorkout(Workout workout) {
        SQLiteDatabase db = this.getWritableDatabase();
        System.out.println(workout);
        db.delete(TABLE_WORKOUT, COLUMN_WORKOUT_ID + " = ?", new String[]{String.valueOf(workout.getId())});
        db.close();
    }
}
