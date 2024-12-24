package com.example.myapplication.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";


    private static DatabaseHelper sInstance;

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "rental_management.db";

    // Table Names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_VEHICLES = "vehicles";
    public static final String TABLE_CUSTOMERS = "customers";
    public static final String TABLE_RENTALS = "rentals";
    public static final String TABLE_MAINTENANCE = "maintenance";

    // Common column names
    public static final String KEY_ID = "id";
    public static final String KEY_CREATED_AT = "create_time";
    public static final String KEY_UPDATED_AT = "update_time";
    public static final String KEY_STATUS = "status";
    public static final String KEY_REMARKS = "remarks";

    // USERS Table - column names
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_REAL_NAME = "real_name";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_ROLE = "role";
    public static final String KEY_LAST_LOGIN = "last_login";

    // VEHICLES Table - column names
    public static final String KEY_PLATE_NUMBER = "plate_number";
    public static final String KEY_BRAND = "brand";
    public static final String KEY_MODEL = "model";
    public static final String KEY_VEHICLE_TYPE = "vehicle_type";
    public static final String KEY_COLOR = "color";
    public static final String KEY_SEATS = "seats";
    public static final String KEY_YEAR = "year";
    public static final String KEY_MILEAGE = "mileage";
    public static final String KEY_DAILY_RATE = "daily_rate";
    public static final String KEY_PHOTO_URL = "photo_url";

    // CUSTOMERS Table - column names
    public static final String KEY_NAME = "name";
    public static final String KEY_ID_NUMBER = "id_number";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_LICENSE_NUMBER = "license_number";

    // RENTALS Table - column names
    public static final String KEY_VEHICLE_ID = "vehicle_id";
    public static final String KEY_CUSTOMER_ID = "customer_id";
    public static final String KEY_CUSTOMER_DES = "customer_des";
    public static final String KEY_VEHICLE_DES = "vehicle_des";

    public static final String KEY_START_DATE = "start_date";
    public static final String KEY_END_DATE = "end_date";
    public static final String KEY_ACTUAL_RETURN_DATE = "actual_return_date";
    public static final String KEY_TOTAL_AMOUNT = "total_amount";
    public static final String KEY_DEPOSIT = "deposit";
    public static final String KEY_CREATED_BY = "created_by";

    // MAINTENANCE Table - column names
    public static final String KEY_MAINTENANCE_TYPE = "maintenance_type";
    public static final String KEY_MAINTENANCE_DATE = "maintenance_date";
    public static final String KEY_COST = "cost";
    public static final String KEY_DESCRIPTION = "description";

    // Create Users Table
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS +
            "(" +
            KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            KEY_USERNAME + " TEXT NOT NULL UNIQUE," +
            KEY_PASSWORD + " TEXT NOT NULL," +
            KEY_REAL_NAME + " TEXT," +
            KEY_PHONE + " TEXT," +
            KEY_EMAIL + " TEXT," +
            KEY_ROLE + " TEXT," +
            KEY_CREATED_AT + " DATETIME," +
            KEY_LAST_LOGIN + " DATETIME," +
            KEY_STATUS + " INTEGER," +
            KEY_REMARKS + " TEXT" +
            ")";

    // Create Vehicles Table
    private static final String CREATE_TABLE_VEHICLES = "CREATE TABLE " + TABLE_VEHICLES +
            "(" +
            KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            KEY_PLATE_NUMBER + " TEXT NOT NULL UNIQUE," +
            KEY_BRAND + " TEXT NOT NULL," +
            KEY_MODEL + " TEXT NOT NULL," +
            KEY_VEHICLE_TYPE + " TEXT," +
            KEY_COLOR + " TEXT," +
            KEY_SEATS + " INTEGER," +
            KEY_YEAR + " INTEGER," +
            KEY_MILEAGE + " REAL," +
            KEY_DAILY_RATE + " DECIMAL," +
            KEY_STATUS + " INTEGER," +
            KEY_PHOTO_URL + " TEXT," +
            KEY_CREATED_AT + " DATETIME," +
            KEY_UPDATED_AT + " DATETIME," +
            KEY_REMARKS + " TEXT" +
            ")";

    // Create Customers Table
    private static final String CREATE_TABLE_CUSTOMERS = "CREATE TABLE " + TABLE_CUSTOMERS +
            "(" +
            KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            KEY_NAME + " TEXT NOT NULL," +
            KEY_ID_NUMBER + " TEXT NOT NULL UNIQUE," +
            KEY_PHONE + " TEXT NOT NULL," +
            KEY_EMAIL + " TEXT," +
            KEY_ADDRESS + " TEXT," +
            KEY_LICENSE_NUMBER + " TEXT," +
            KEY_CREATED_AT + " DATETIME," +
            KEY_UPDATED_AT + " DATETIME," +
            KEY_STATUS + " INTEGER," +
            KEY_REMARKS + " TEXT" +
            ")";

    // Create Rentals Table
    private static final String CREATE_TABLE_RENTALS = "CREATE TABLE " + TABLE_RENTALS +
            "(" +
            KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            KEY_VEHICLE_ID + " INTEGER," +
            KEY_CUSTOMER_ID + " INTEGER," +
            KEY_CUSTOMER_DES + " TEXT," +
            KEY_VEHICLE_DES + " TEXT," +
            KEY_START_DATE + " DATE NOT NULL," +
            KEY_END_DATE + " DATE NOT NULL," +
            KEY_ACTUAL_RETURN_DATE + " DATE," +
            KEY_TOTAL_AMOUNT + " DECIMAL," +
            KEY_DEPOSIT + " DECIMAL," +
            KEY_STATUS + " INTEGER," +
            KEY_CREATED_AT + " DATETIME," +
            KEY_UPDATED_AT + " DATETIME," +
            KEY_CREATED_BY + " INTEGER," +
            KEY_REMARKS + " TEXT," +
//            "FOREIGN KEY(" + KEY_VEHICLE_ID + ") REFERENCES " + TABLE_VEHICLES + "(" + KEY_ID + ")," +
//            "FOREIGN KEY(" + KEY_CUSTOMER_ID + ") REFERENCES " + TABLE_CUSTOMERS + "(" + KEY_ID + ")," +
            "FOREIGN KEY(" + KEY_CREATED_BY + ") REFERENCES " + TABLE_USERS + "(" + KEY_ID + ")" +
            ")";

    // Create Maintenance Table
    private static final String CREATE_TABLE_MAINTENANCE = "CREATE TABLE " + TABLE_MAINTENANCE +
            "(" +
            KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            KEY_VEHICLE_ID + " INTEGER," +
            KEY_MAINTENANCE_TYPE + " TEXT," +
            KEY_MAINTENANCE_DATE + " DATE," +
            KEY_COST + " DECIMAL," +
            KEY_DESCRIPTION + " TEXT," +
            KEY_STATUS + " INTEGER," +
            KEY_CREATED_AT + " DATETIME," +
            KEY_CREATED_BY + " INTEGER," +
            "FOREIGN KEY(" + KEY_VEHICLE_ID + ") REFERENCES " + TABLE_VEHICLES + "(" + KEY_ID + ")," +
            "FOREIGN KEY(" + KEY_CREATED_BY + ") REFERENCES " + TABLE_USERS + "(" + KEY_ID + ")" +
            ")";

    // Table Create Statements
    private static final String[] SQL_CREATE_TABLES = {
            CREATE_TABLE_USERS,
            CREATE_TABLE_VEHICLES,
            CREATE_TABLE_CUSTOMERS,
            CREATE_TABLE_RENTALS,
            CREATE_TABLE_MAINTENANCE
    };

    // Table Drop Statements
    private static final String[] SQL_DROP_TABLES = {
            "DROP TABLE IF EXISTS " + TABLE_MAINTENANCE,
            "DROP TABLE IF EXISTS " + TABLE_RENTALS,
            "DROP TABLE IF EXISTS " + TABLE_CUSTOMERS,
            "DROP TABLE IF EXISTS " + TABLE_VEHICLES,
            "DROP TABLE IF EXISTS " + TABLE_USERS
    };

//    public DatabaseHelper(Context context) {
//        super(context, DATABASE_NAME, null, DATABASE_VERSION);
//    }

    //使用单例，只要有一个单个数据库连接就行了。
    // 私有构造函数，防止外部直接实例化
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // 公有的静态方法，提供全局访问点
    public static synchronized DatabaseHelper getInstance(Context context) {
        // 使用局部上下文来避免泄漏外部上下文
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating required tables
        for (String sql : SQL_CREATE_TABLES) {
            db.execSQL(sql);
            Log.d(TAG, "Created table with SQL: " + sql);
        }

        createIndexes(db); // 调用创建索引的方法
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ". All data will be destroyed");

        // drop all tables
        for (String sql : SQL_DROP_TABLES) {
            db.execSQL(sql);
        }

        // create new tables
        onCreate(db);
    }

    // Create indexes after table creation
    public void createIndexes(SQLiteDatabase db) {
        Log.w(TAG, "createIndexes " );
        // Vehicles indexes
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_vehicles_status ON " + TABLE_VEHICLES + "(" + KEY_STATUS + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_vehicles_plate ON " + TABLE_VEHICLES + "(" + KEY_PLATE_NUMBER + ")");

        // Customers indexes
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_customers_phone ON " + TABLE_CUSTOMERS + "(" + KEY_PHONE + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_customers_id_number ON " + TABLE_CUSTOMERS + "(" + KEY_ID_NUMBER + ")");

        // Rentals indexes
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_rentals_vehicle ON " + TABLE_RENTALS + "(" + KEY_VEHICLE_ID + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_rentals_customer ON " + TABLE_RENTALS + "(" + KEY_CUSTOMER_ID + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_rentals_dates ON " + TABLE_RENTALS + "(" + KEY_START_DATE + "," + KEY_END_DATE + ")");
    }

    // Helper method to get current datetime
    public static String getDateTime() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                .format(new java.util.Date());
    }
}

/*
在Android开发中，SQLiteOpenHelper 是一个抽象类，用于管理数据库的创建和版本管理。当你继承这个类时，你需要实现两个抽象方法：onCreate(SQLiteDatabase db) 和 onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)。onCreate 方法在数据库第一次创建时被调用，而 onUpgrade 方法在数据库需要升级时被调用。

如果你想在数据库创建时创建索引，你应该在 onCreate(SQLiteDatabase db) 方法中执行这个操作


在Android中，`SQLiteOpenHelper`及其管理的`SQLiteDatabase`对象不需要显式关闭，因为它们被设计为与应用程序的生命周期紧密集成。以下是关于何时以及如何管理`SQLiteOpenHelper`和`SQLiteDatabase`的一些指导：

        1. **SQLiteOpenHelper的生命周期**：
        - `SQLiteOpenHelper`的实例应该在整个应用程序的生命周期内保持活跃。它通常被设计为单例，这意味着它应该在第一次创建后一直存在，直到应用程序被销毁。
        - 你不应该在应用程序的任何地方显式关闭`SQLiteOpenHelper`的实例。

        2. **SQLiteDatabase的生命周期**：
        - 当你通过`SQLiteOpenHelper`获取`SQLiteDatabase`对象进行数据库操作时，完成操作后应该调用`SQLiteDatabase`对象的`close()`方法来释放系统资源。
        - 通常，这会在数据库操作的`try-finally`块中完成，以确保即使在发生异常时也能关闭数据库连接。

        3. **在Activity或Service中管理数据库连接**：
        - 在`Activity`或`Service`中，你应该在`onDestroy()`方法中关闭数据库连接，以确保在组件销毁时释放资源。
        - 例如：
        ```java
@Override
protected void onDestroy() {
    super.onDestroy();
    if (db != null && db.isOpen()) {
        db.close(); // db是SQLiteDatabase的实例
    }
}
     ```

             4. **应用程序退出时**：
        - 当应用程序完全退出时，所有的`SQLiteDatabase`对象将自动关闭。这是因为Android系统会在应用程序退出时清理资源。

        5. **避免内存泄漏**：
        - 如果你的`SQLiteOpenHelper`持有`Context`的引用，确保在`Context`不再需要时释放这些引用，以避免内存泄漏。

        6. **使用ApplicationContext**：
        - 在创建`SQLiteOpenHelper`实例时，建议使用`Context.getApplicationContext()`来获取上下文，这样可以避免因持有`Activity`的引用而导致的内存泄漏。

总结来说，`SQLiteOpenHelper`应该保持活跃直到应用程序结束，而`SQLiteDatabase`对象应该在每次数据库操作完成后关闭。这样可以确保资源得到有效管理，同时避免内存泄漏和性能问题。



在Android中，`SQLiteOpenHelper`的实例并不直接对应于数据库连接。`SQLiteOpenHelper`是一个辅助类，用于管理数据库的创建和版本管理，但它本身并不持有数据库连接。当你通过`SQLiteOpenHelper`的实例获取数据库连接时，实际上是通过调用`getWritableDatabase()`或`getReadableDatabase()`方法来获取`SQLiteDatabase`对象，这些对象代表数据库连接。

如果你创建了多个`SQLiteOpenHelper`的实例，并且它们指向同一个数据库文件（即数据库名称相同），那么：

1. **相同的数据库文件**：这些实例将共享同一个数据库文件，但每次调用`getWritableDatabase()`或`getReadableDatabase()`时，都可能获得不同的`SQLiteDatabase`对象（即不同的连接）。

2. **数据库连接池**：在内部，SQLite使用了一种连接池机制。当你获取一个数据库连接时，SQLite会尝试重用现有的连接，而不是每次都打开一个新的连接。这意味着，即使你创建了多个`SQLiteOpenHelper`实例，SQLite也可能会重用相同的数据库连接。

3. **线程安全**：`SQLiteOpenHelper`类是线程安全的，这意味着你可以在不同的线程中使用同一个`SQLiteOpenHelper`实例来获取数据库连接。

4. **资源管理**：尽管`SQLiteOpenHelper`和`SQLiteDatabase`处理了很多资源管理的工作，但你仍然需要确保在不再需要数据库连接时关闭它们。这通常是通过调用`SQLiteDatabase`对象的`close()`方法来完成的。

5. **单例模式**：在实际应用中，通常推荐将`SQLiteOpenHelper`作为单例使用，这样可以确保整个应用中只有一个数据库文件被管理，并且可以避免多个实例之间的竞争条件。

总结来说，创建多个`SQLiteOpenHelper`实例并不直接意味着会有多个数据库连接，因为SQLite内部会管理连接的重用。但是，为了资源管理和一致性，最好的做法是在整个应用中使用单一的`SQLiteOpenHelper`实例。



*/