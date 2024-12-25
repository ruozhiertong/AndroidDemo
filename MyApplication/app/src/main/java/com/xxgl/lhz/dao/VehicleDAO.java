
package com.xxgl.lhz.dao;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import com.xxgl.lhz.database.DatabaseHelper;
import com.xxgl.lhz.models.Vehicle;

import java.util.ArrayList;
import java.util.List;

public class VehicleDAO {
    private static final String TAG = "VehicleDAO";
    private DatabaseHelper dbHelper;

    public VehicleDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * 创建新车辆
     */
    public long createVehicle(Vehicle vehicle) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long vehicleId = -1;

        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.KEY_PLATE_NUMBER, vehicle.getPlateNumber());
            values.put(DatabaseHelper.KEY_BRAND, vehicle.getBrand());
            values.put(DatabaseHelper.KEY_MODEL, vehicle.getModel());
            values.put(DatabaseHelper.KEY_VEHICLE_TYPE, vehicle.getVehicleType());
            values.put(DatabaseHelper.KEY_COLOR, vehicle.getColor());
            values.put(DatabaseHelper.KEY_SEATS, vehicle.getSeats());
            values.put(DatabaseHelper.KEY_YEAR, vehicle.getYear());
            values.put(DatabaseHelper.KEY_MILEAGE, vehicle.getMileage());
            values.put(DatabaseHelper.KEY_DAILY_RATE, vehicle.getDailyRate());
            values.put(DatabaseHelper.KEY_STATUS, vehicle.getStatus());
            values.put(DatabaseHelper.KEY_PHOTO_URL, vehicle.getPhotoUrl());
            values.put(DatabaseHelper.KEY_CREATED_AT, DatabaseHelper.getDateTime());
            values.put(DatabaseHelper.KEY_UPDATED_AT, DatabaseHelper.getDateTime());
            values.put(DatabaseHelper.KEY_REMARKS, vehicle.getRemarks());

            vehicleId = db.insert(DatabaseHelper.TABLE_VEHICLES, null, values);
            Log.d(TAG, "Created new vehicle with ID: " + vehicleId);
        } catch (Exception e) {
            Log.e(TAG, "Error creating vehicle: " + e.getMessage());
        }

        return vehicleId;
    }

    /**
     * 获取单个车辆信息
     */
    public Vehicle getVehicle(int vehicleId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Vehicle vehicle = null;

        try {
            Cursor cursor = db.query(DatabaseHelper.TABLE_VEHICLES,
                    null,
                    DatabaseHelper.KEY_ID + "=?",
                    new String[]{String.valueOf(vehicleId)},
                    null, null, null);

            if (cursor.moveToFirst()) {
                vehicle = cursorToVehicle(cursor);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting vehicle: " + e.getMessage());
        }

        return vehicle;
    }

    /**
     * 获取所有车辆列表
     */
    public List<Vehicle> getAllVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_VEHICLES;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    Vehicle vehicle = cursorToVehicle(cursor);
                    vehicles.add(vehicle);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting all vehicles: " + e.getMessage());
        }

        return vehicles;
    }

    /**
     * 获取车辆列表
     */
    public List<Vehicle> getVehiclesByStatus(int status) {
        List<Vehicle> vehicles = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            Cursor cursor = db.query(DatabaseHelper.TABLE_VEHICLES,
                    null,
                    DatabaseHelper.KEY_STATUS + "=?",
                    new String[]{String.valueOf(status)}, // 1表示可用状态
                    null, null, null);

            if (cursor.moveToFirst()) {
                do {
                    Vehicle vehicle = cursorToVehicle(cursor);
                    vehicles.add(vehicle);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting available vehicles: " + e.getMessage());
        }

        return vehicles;
    }

    /**
     * 更新车辆信息
     */
    public int updateVehicle(Vehicle vehicle) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = 0;

        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.KEY_PLATE_NUMBER, vehicle.getPlateNumber());
            values.put(DatabaseHelper.KEY_BRAND, vehicle.getBrand());
            values.put(DatabaseHelper.KEY_MODEL, vehicle.getModel());
            values.put(DatabaseHelper.KEY_VEHICLE_TYPE, vehicle.getVehicleType());
            values.put(DatabaseHelper.KEY_COLOR, vehicle.getColor());
            values.put(DatabaseHelper.KEY_SEATS, vehicle.getSeats());
            values.put(DatabaseHelper.KEY_YEAR, vehicle.getYear());
            values.put(DatabaseHelper.KEY_MILEAGE, vehicle.getMileage());
            values.put(DatabaseHelper.KEY_DAILY_RATE, vehicle.getDailyRate());
            values.put(DatabaseHelper.KEY_STATUS, vehicle.getStatus());
            values.put(DatabaseHelper.KEY_PHOTO_URL, vehicle.getPhotoUrl());
            values.put(DatabaseHelper.KEY_UPDATED_AT, DatabaseHelper.getDateTime());
            values.put(DatabaseHelper.KEY_REMARKS, vehicle.getRemarks());

            rowsAffected = db.update(DatabaseHelper.TABLE_VEHICLES,
                    values,
                    DatabaseHelper.KEY_ID + " = ?",
                    new String[]{String.valueOf(vehicle.getId())});

            Log.d(TAG, "Updated vehicle with ID: " + vehicle.getId());
        } catch (Exception e) {
            Log.e(TAG, "Error updating vehicle: " + e.getMessage());
        }

        return rowsAffected;
    }

    /**
     * 删除车辆
     */
    public int deleteVehicle(int vehicleId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = 0;

        try {
            rowsAffected = db.delete(DatabaseHelper.TABLE_VEHICLES,
                    DatabaseHelper.KEY_ID + " = ?",
                    new String[]{String.valueOf(vehicleId)});

            Log.d(TAG, "Deleted vehicle with ID: " + vehicleId);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting vehicle: " + e.getMessage());
        }

        return rowsAffected;
    }

    /**
     * 更新车辆状态
     */
    public boolean updateVehicleStatus(int vehicleId, int status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;

        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.KEY_STATUS, status);
            values.put(DatabaseHelper.KEY_UPDATED_AT, DatabaseHelper.getDateTime());

            int rowsAffected = db.update(DatabaseHelper.TABLE_VEHICLES,
                    values,
                    DatabaseHelper.KEY_ID + " = ?",
                    new String[]{String.valueOf(vehicleId)});

            success = rowsAffected > 0;
            Log.d(TAG, "Updated vehicle status for ID: " + vehicleId);
        } catch (Exception e) {
            Log.e(TAG, "Error updating vehicle status: " + e.getMessage());
        }

        return success;
    }

    /**
     * 检查车牌号是否已存在
     */
    public boolean isPlateNumberExists(String plateNumber) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        boolean exists = false;

        try {
            Cursor cursor = db.query(DatabaseHelper.TABLE_VEHICLES,
                    new String[]{DatabaseHelper.KEY_ID},
                    DatabaseHelper.KEY_PLATE_NUMBER + "=?",
                    new String[]{plateNumber},
                    null, null, null);

            exists = cursor.getCount() > 0;
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error checking plate number existence: " + e.getMessage());
        }

        return exists;
    }

    /**
     * 查找符合条件的车辆
     */
    public List<Vehicle> searchVehicles(String keyword) {
        List<Vehicle> vehicles = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            String searchQuery = "SELECT * FROM " + DatabaseHelper.TABLE_VEHICLES +
                    " WHERE " + DatabaseHelper.KEY_PLATE_NUMBER + " LIKE '%" + keyword + "%'" +
                    " OR " + DatabaseHelper.KEY_BRAND + " LIKE '%" + keyword + "%'" +
                    " OR " + DatabaseHelper.KEY_MODEL + " LIKE '%" + keyword + "%'";

            Cursor cursor = db.rawQuery(searchQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    Vehicle vehicle = cursorToVehicle(cursor);
                    vehicles.add(vehicle);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error searching vehicles: " + e.getMessage());
        }

        return vehicles;
    }

    /**
     * 将Cursor转换为Vehicle对象
     */
    private Vehicle cursorToVehicle(Cursor cursor) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_ID)));
        vehicle.setPlateNumber(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_PLATE_NUMBER)));
        vehicle.setBrand(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_BRAND)));
        vehicle.setModel(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_MODEL)));
        vehicle.setVehicleType(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_VEHICLE_TYPE)));
        vehicle.setColor(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_COLOR)));
        vehicle.setSeats(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_SEATS)));
        vehicle.setYear(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_YEAR)));
        vehicle.setMileage(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.KEY_MILEAGE)));
        vehicle.setDailyRate(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.KEY_DAILY_RATE)));
        vehicle.setStatus(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_STATUS)));
        vehicle.setPhotoUrl(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_PHOTO_URL)));
        vehicle.setCreateTime(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_CREATED_AT)));
        vehicle.setUpdateTime(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_UPDATED_AT)));
        vehicle.setRemarks(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_REMARKS)));
        return vehicle;
    }
}