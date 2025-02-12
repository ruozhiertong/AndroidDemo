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

//    资源管理：减少资源消耗，避免多次打开数据库连接。
//    线程安全：避免多个实例之间的并发问题，确保数据一致性。
//    易于管理：简化数据库操作的管理，便于维护和调试。
    // 使用单例模式。 在整个应用生命周期之间，只要一个单例，即只要有一个数据库连接， 这样减少资源消耗，避免多次打开数据库连接.
    private static DatabaseHelper instance;

    //关于，何时关闭数据库连接。关键看是否频繁操作数据库。
    //每次操作后关闭，比较适合不那么频繁操作数据库的，如果过于频繁，每次操作都关闭会导致性能低下。
    /**
     * 在使用SQLite数据库时，合理管理数据库连接的打开和关闭非常重要，以避免资源泄漏和性能问题。关闭数据库连接的时机取决于你的应用架构和使用场景。以下是一些常见的策略和建议：
     * 1. 每次操作后关闭连接
     * 在每次数据库操作完成后关闭连接是一种常见的做法。这种方式可以确保每次操作后都释放资源，避免长时间占用数据库连接。
     * 示例代码
     * java复制
     * public class MainActivity extends AppCompatActivity {
     *     private DatabaseHelper dbHelper;
     *
     *     @Override
     *     protected void onCreate(Bundle savedInstanceState) {
     *         super.onCreate(savedInstanceState);
     *         setContentView(R.layout.activity_main);
     *
     *         // 获取数据库实例
     *         dbHelper = DatabaseHelper.getInstance(this);
     *
     *         // 使用数据库
     *         SQLiteDatabase db = dbHelper.getWritableDatabase();
     *         try {
     *             // 执行数据库操作
     *             db.execSQL("INSERT INTO users (name, email) VALUES ('John Doe', 'john@example.com')");
     *         } finally {
     *             // 关闭数据库连接
     *             db.close();
     *         }
     *     }
     * }
     * 2. 使用try-with-resources语句
     * 从Java 7开始，try-with-resources语句可以自动关闭实现了AutoCloseable接口的资源。SQLiteDatabase实现了AutoCloseable接口，因此可以使用try-with-resources语句来自动管理数据库连接。
     * 示例代码
     * java复制
     * public class MainActivity extends AppCompatActivity {
     *     private DatabaseHelper dbHelper;
     *
     *     @Override
     *     protected void onCreate(Bundle savedInstanceState) {
     *         super.onCreate(savedInstanceState);
     *         setContentView(R.layout.activity_main);
     *
     *         // 获取数据库实例
     *         dbHelper = DatabaseHelper.getInstance(this);
     *
     *         // 使用数据库
     *         try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
     *             // 执行数据库操作
     *             db.execSQL("INSERT INTO users (name, email) VALUES ('John Doe', 'john@example.com')");
     *         } catch (Exception e) {
     *             e.printStackTrace();
     *         }
     *     }
     * }
     * 3. 在Activity或Fragment的生命周期中管理连接
     * 如果你在Activity或Fragment中频繁使用数据库，可以在onCreate或onResume方法中打开数据库连接，并在onDestroy或onPause方法中关闭连接。
     * 示例代码
     * java复制
     * public class MainActivity extends AppCompatActivity {
     *     private DatabaseHelper dbHelper;
     *     private SQLiteDatabase db;
     *
     *     @Override
     *     protected void onCreate(Bundle savedInstanceState) {
     *         super.onCreate(savedInstanceState);
     *         setContentView(R.layout.activity_main);
     *
     *         // 获取数据库实例
     *         dbHelper = DatabaseHelper.getInstance(this);
     *
     *         // 打开数据库连接
     *         db = dbHelper.getWritableDatabase();
     *     }
     *
     *     @Override
     *     protected void onDestroy() {
     *         super.onDestroy();
     *         // 关闭数据库连接
     *         if (db != null && db.isOpen()) {
     *             db.close();
     *         }
     *     }
     *
     *     private void performDatabaseOperation() {
     *         try {
     *             // 执行数据库操作
     *             db.execSQL("INSERT INTO users (name, email) VALUES ('John Doe', 'john@example.com')");
     *         } catch (Exception e) {
     *             e.printStackTrace();
     *         }
     *     }
     * }
     * 4. 在Application类中管理全局数据库实例
     * 如果你的应用中有多个组件需要频繁访问数据库，可以在Application类中管理全局数据库实例，并在应用退出时关闭连接。
     * 示例代码
     * MyApplication.java
     * java复制
     * public class MyApplication extends Application {
     *     private static DatabaseHelper dbHelper;
     *
     *     @Override
     *     public void onCreate() {
     *         super.onCreate();
     *         dbHelper = DatabaseHelper.getInstance(this);
     *     }
     *
     *     public static DatabaseHelper getDbHelper() {
     *         return dbHelper;
     *     }
     *
     *     @Override
     *     public void onTerminate() {
     *         super.onTerminate();
     *         if (dbHelper != null) {
     *             dbHelper.close();
     *         }
     *     }
     * }
     * MainActivity.java
     * java复制
     * public class MainActivity extends AppCompatActivity {
     *     @Override
     *     protected void onCreate(Bundle savedInstanceState) {
     *         super.onCreate(savedInstanceState);
     *         setContentView(R.layout.activity_main);
     *
     *         // 获取数据库实例
     *         SQLiteDatabase db = MyApplication.getDbHelper().getWritableDatabase();
     *
     *         try {
     *             // 执行数据库操作
     *             db.execSQL("INSERT INTO users (name, email) VALUES ('John Doe', 'john@example.com')");
     *         } finally {
     *             // 关闭数据库连接
     *             db.close();
     *         }
     *     }
     * }
     * 注意事项
     * 资源释放：确保在每次操作后或在组件销毁时释放数据库资源，避免内存泄漏。
     * 线程安全：在多线程环境中，确保数据库操作是线程安全的。
     * 性能优化：避免频繁打开和关闭数据库连接，尤其是在短时间内多次操作数据库时。
     * 通过合理管理数据库连接的打开和关闭，可以确保应用的性能和稳定性，同时避免资源泄漏。
     */

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




//    public DatabaseHelper(Context context) {
//        super(context, DATABASE_NAME, null, DATABASE_VERSION);
//    }

    // 私有化构造函数。 确保外部不会创建多个实例。
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    /**
     * 确保线程安全。进行同步处理 synchronized。
     * @param context
     * @return
     */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
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
