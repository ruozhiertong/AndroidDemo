package com.xxgl.lhz.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.xxgl.lhz.database.DatabaseHelper;
import com.xxgl.lhz.models.User;

import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private static final String TAG = "UserDAO";
    private DatabaseHelper dbHelper;

    public UserDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * 创建新用户
     */
    public long createUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long userId = -1;

        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.KEY_USERNAME, user.getUsername());
            values.put(DatabaseHelper.KEY_PASSWORD, user.getPassword());
            values.put(DatabaseHelper.KEY_REAL_NAME, user.getRealName());
            values.put(DatabaseHelper.KEY_PHONE, user.getPhone());
            values.put(DatabaseHelper.KEY_EMAIL, user.getEmail());
            values.put(DatabaseHelper.KEY_ROLE, user.getRole());
            values.put(DatabaseHelper.KEY_CREATED_AT, DatabaseHelper.getDateTime());
            values.put(DatabaseHelper.KEY_STATUS, user.getStatus());
            values.put(DatabaseHelper.KEY_REMARKS, user.getRemarks());

            userId = db.insert(DatabaseHelper.TABLE_USERS, null, values);
            Log.d(TAG, "Created new user with ID: " + userId);
        } catch (Exception e) {
            Log.e(TAG, "Error creating user: " + e.getMessage());
        }

        return userId;
    }

    /**
     * 获取单个用户信息
     */
    public User getUser(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        User user = null;

        String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_USERS +
                " WHERE " + DatabaseHelper.KEY_ID + " = '" + userId + "'";

        try {
            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                user = new User();
                user.setId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_ID)));
                user.setUsername(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_USERNAME)));
                user.setPassword(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_PASSWORD)));
                user.setRealName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_REAL_NAME)));
                user.setPhone(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_PHONE)));
                user.setEmail(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_EMAIL)));
                user.setRole(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_ROLE)));
                user.setCreateTime(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_CREATED_AT)));
                user.setLastLogin(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_LAST_LOGIN)));
                user.setStatus(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_STATUS)));
                user.setRemarks(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_REMARKS)));
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting user: " + e.getMessage());
        }

        return user;
    }


    public User getUser(String username){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        User user = null;

        String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_USERS +
                " WHERE " + DatabaseHelper.KEY_USERNAME + " = '" + username + "'";

        System.out.println(selectQuery);
        try {
            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                user = new User();
                user.setId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_ID)));
                user.setUsername(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_USERNAME)));
                user.setPassword(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_PASSWORD)));
                user.setRealName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_REAL_NAME)));
                user.setPhone(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_PHONE)));
                user.setEmail(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_EMAIL)));
                user.setRole(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_ROLE)));
                user.setCreateTime(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_CREATED_AT)));
                user.setLastLogin(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_LAST_LOGIN)));
                user.setStatus(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_STATUS)));
                user.setRemarks(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_REMARKS)));
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting user: " + e.getMessage());
        }

        return user;
    }

    /**
     * 获取所有用户列表
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_USERS;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    User user = new User();
                    user.setId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_ID)));
                    user.setUsername(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_USERNAME)));
                    user.setPassword(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_PASSWORD)));
                    user.setRealName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_REAL_NAME)));
                    user.setPhone(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_PHONE)));
                    user.setEmail(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_EMAIL)));
                    user.setRole(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_ROLE)));
                    user.setCreateTime(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_CREATED_AT)));
                    user.setLastLogin(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_LAST_LOGIN)));
                    user.setStatus(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_STATUS)));
                    user.setRemarks(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_REMARKS)));

                    users.add(user);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting all users: " + e.getMessage());
        }

        return users;
    }

    /**
     * 更新用户信息
     */
    public int updateUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = 0;

        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.KEY_USERNAME, user.getUsername());
            values.put(DatabaseHelper.KEY_PASSWORD, user.getPassword());
            values.put(DatabaseHelper.KEY_REAL_NAME, user.getRealName());
            values.put(DatabaseHelper.KEY_PHONE, user.getPhone());
            values.put(DatabaseHelper.KEY_EMAIL, user.getEmail());
            values.put(DatabaseHelper.KEY_ROLE, user.getRole());
            values.put(DatabaseHelper.KEY_STATUS, user.getStatus());
            values.put(DatabaseHelper.KEY_REMARKS, user.getRemarks());

            rowsAffected = db.update(DatabaseHelper.TABLE_USERS, values,
                    DatabaseHelper.KEY_ID + " = ?",
                    new String[]{String.valueOf(user.getId())});

            Log.d(TAG, "Updated user with ID: " + user.getId());
        } catch (Exception e) {
            Log.e(TAG, "Error updating user: " + e.getMessage());
        }

        return rowsAffected;
    }

    /**
     * 删除用户
     */
    public int deleteUser(long userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = 0;

        try {
            rowsAffected = db.delete(DatabaseHelper.TABLE_USERS,
                    DatabaseHelper.KEY_ID + " = ?",
                    new String[]{String.valueOf(userId)});

            Log.d(TAG, "Deleted user with ID: " + userId);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting user: " + e.getMessage());
        }

        return rowsAffected;
    }

    /**
     * 用户登录验证
     */
    public User authenticateUser(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        User user = null;

        try {
            String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_USERS +
                    " WHERE " + DatabaseHelper.KEY_USERNAME + " = ? AND " +
                    DatabaseHelper.KEY_PASSWORD + " = ?";

            Cursor cursor = db.rawQuery(selectQuery, new String[]{username, password});

            if (cursor.moveToFirst()) {
                user = new User();
                user.setId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_ID)));
                user.setUsername(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_USERNAME)));
                user.setRealName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_REAL_NAME)));
                user.setRole(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_ROLE)));
                user.setStatus(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_STATUS)));

                // 更新最后登录时间
                updateLastLogin(user.getId());
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error authenticating user: " + e.getMessage());
        }

        return user;
    }

    /**
     * 更新最后登录时间
     */
    private void updateLastLogin(long userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.KEY_LAST_LOGIN, DatabaseHelper.getDateTime());

            db.update(DatabaseHelper.TABLE_USERS, values,
                    DatabaseHelper.KEY_ID + " = ?",
                    new String[]{String.valueOf(userId)});
        } catch (Exception e) {
            Log.e(TAG, "Error updating last login time: " + e.getMessage());
        }
    }

    /**
     * 检查用户名是否已存在
     */
    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        boolean exists = false;

        try {
            Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                    new String[]{DatabaseHelper.KEY_ID},
                    DatabaseHelper.KEY_USERNAME + "=?",
                    new String[]{username}, null, null, null);

            exists = cursor.getCount() > 0;
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error checking username existence: " + e.getMessage());
        }

        return exists;
    }
}
