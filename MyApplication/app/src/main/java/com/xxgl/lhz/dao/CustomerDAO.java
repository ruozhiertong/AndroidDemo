package com.xxgl.lhz.dao;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import com.xxgl.lhz.database.DatabaseHelper;
import com.xxgl.lhz.models.Customer;

import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {
    private static final String TAG = "CustomerDAO";
    private DatabaseHelper dbHelper;

    public CustomerDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * 创建新客户
     */
    public int createCustomer(Customer customer) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int customerId = -1;

        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.KEY_NAME, customer.getName());
            values.put(DatabaseHelper.KEY_ID_NUMBER, customer.getIdNumber());
            values.put(DatabaseHelper.KEY_PHONE, customer.getPhone());
            values.put(DatabaseHelper.KEY_EMAIL, customer.getEmail());
            values.put(DatabaseHelper.KEY_ADDRESS, customer.getAddress());
            values.put(DatabaseHelper.KEY_LICENSE_NUMBER, customer.getLicenseNumber());
            values.put(DatabaseHelper.KEY_CREATED_AT, DatabaseHelper.getDateTime());
            values.put(DatabaseHelper.KEY_UPDATED_AT, DatabaseHelper.getDateTime());
            values.put(DatabaseHelper.KEY_STATUS, customer.getStatus());
            values.put(DatabaseHelper.KEY_REMARKS, customer.getRemarks());

            customerId = (int)db.insert(DatabaseHelper.TABLE_CUSTOMERS, null, values);
            Log.d(TAG, "Created new customer with ID: " + customerId);
        } catch (Exception e) {
            Log.e(TAG, "Error creating customer: " + e.getMessage());
        }

        return customerId;
    }

    /**
     * 获取单个客户信息
     */
    public Customer getCustomer(int customerId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Customer customer = null;

        try {
            Cursor cursor = db.query(DatabaseHelper.TABLE_CUSTOMERS,
                    null,
                    DatabaseHelper.KEY_ID + "=?",
                    new String[]{String.valueOf(customerId)},
                    null, null, null);

            if (cursor.moveToFirst()) {
                customer = cursorToCustomer(cursor);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting customer: " + e.getMessage());
        }

        return customer;
    }

    /**
     * 获取所有客户列表
     */
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_CUSTOMERS;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    Customer customer = cursorToCustomer(cursor);
                    customers.add(customer);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting all customers: " + e.getMessage());
        }

        return customers;
    }

    /**
     * 更新客户信息
     */
    public int updateCustomer(Customer customer) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = 0;

        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.KEY_NAME, customer.getName());
            values.put(DatabaseHelper.KEY_PHONE, customer.getPhone());
            values.put(DatabaseHelper.KEY_EMAIL, customer.getEmail());
            values.put(DatabaseHelper.KEY_ADDRESS, customer.getAddress());
            values.put(DatabaseHelper.KEY_LICENSE_NUMBER, customer.getLicenseNumber());
            values.put(DatabaseHelper.KEY_UPDATED_AT, DatabaseHelper.getDateTime());
            values.put(DatabaseHelper.KEY_STATUS, customer.getStatus());
            values.put(DatabaseHelper.KEY_REMARKS, customer.getRemarks());

            rowsAffected = db.update(DatabaseHelper.TABLE_CUSTOMERS,
                    values,
                    DatabaseHelper.KEY_ID + " = ?",
                    new String[]{String.valueOf(customer.getId())});

            Log.d(TAG, "Updated customer with ID: " + customer.getId());
        } catch (Exception e) {
            Log.e(TAG, "Error updating customer: " + e.getMessage());
        }

        return rowsAffected;
    }

    /**
     * 删除客户
     */
    public int deleteCustomer(long customerId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = 0;

        try {
            rowsAffected = db.delete(DatabaseHelper.TABLE_CUSTOMERS,
                    DatabaseHelper.KEY_ID + " = ?",
                    new String[]{String.valueOf(customerId)});

            Log.d(TAG, "Deleted customer with ID: " + customerId);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting customer: " + e.getMessage());
        }

        return rowsAffected;
    }

    /**
     * 根据身份证号查找客户
     */
    public Customer getCustomerByIdNumber(String idNumber) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Customer customer = null;

        try {
            Cursor cursor = db.query(DatabaseHelper.TABLE_CUSTOMERS,
                    null,
                    DatabaseHelper.KEY_ID_NUMBER + "=?",
                    new String[]{idNumber},
                    null, null, null);

            if (cursor.moveToFirst()) {
                customer = cursorToCustomer(cursor);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting customer by ID number: " + e.getMessage());
        }

        return customer;
    }

    /**
     * 查找符合条件的客户
     */
    public List<Customer> searchCustomers(String keyword) {
        List<Customer> customers = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            String searchQuery = "SELECT * FROM " + DatabaseHelper.TABLE_CUSTOMERS +
                    " WHERE " + DatabaseHelper.KEY_NAME + " LIKE '%" + keyword + "%'" +
                    " OR " + DatabaseHelper.KEY_PHONE + " LIKE '%" + keyword + "%'" +
                    " OR " + DatabaseHelper.KEY_ID_NUMBER + " LIKE '%" + keyword + "%'";

            Cursor cursor = db.rawQuery(searchQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    Customer customer = cursorToCustomer(cursor);
                    customers.add(customer);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error searching customers: " + e.getMessage());
        }

        return customers;
    }

    /**
     * 将客户加入黑名单
     */
    public int addToBlacklist(long customerId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = 0;

        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.KEY_STATUS, 0); // 0表示黑名单状态
            values.put(DatabaseHelper.KEY_UPDATED_AT, DatabaseHelper.getDateTime());

            rowsAffected = db.update(DatabaseHelper.TABLE_CUSTOMERS,
                    values,
                    DatabaseHelper.KEY_ID + " = ?",
                    new String[]{String.valueOf(customerId)});

//            success = rowsAffected > 0;
            Log.d(TAG, "Added customer to blacklist: " + customerId);
        } catch (Exception e) {
            Log.e(TAG, "Error adding customer to blacklist: " + e.getMessage());
        }

        return rowsAffected;
    }

    /**
     * 将Cursor转换为Customer对象
     */
    private Customer cursorToCustomer(Cursor cursor) {
        Customer customer = new Customer();
        customer.setId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_ID)));
        customer.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_NAME)));
        customer.setIdNumber(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_ID_NUMBER)));
        customer.setPhone(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_PHONE)));
        customer.setEmail(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_EMAIL)));
        customer.setAddress(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_ADDRESS)));
        customer.setLicenseNumber(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_LICENSE_NUMBER)));
        customer.setCreateTime(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_CREATED_AT)));
        customer.setUpdateTime(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_UPDATED_AT)));
        customer.setStatus(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_STATUS)));
        customer.setRemarks(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_REMARKS)));
        return customer;
    }
}