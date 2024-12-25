package com.xxgl.lhz.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.xxgl.lhz.database.DatabaseHelper;
import com.xxgl.lhz.models.Maintenance;

import java.util.ArrayList;
import java.util.List;

public class MaintenanceDAO {
    private static final String TAG = "MaintenanceDAO";
    private DatabaseHelper dbHelper;
    private VehicleDAO vehicleDAO;

    public MaintenanceDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.vehicleDAO = new VehicleDAO(dbHelper);
    }

    /**
     * 创建新维护记录
     */
    public long createMaintenance(Maintenance maintenance) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long maintenanceId = -1;

        db.beginTransaction();
        try {
            // 更新车辆状态为维护中
            boolean vehicleUpdated = vehicleDAO.updateVehicleStatus(maintenance.getVehicleId(), 3); // 3表示维护中

            if (vehicleUpdated) {
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.KEY_VEHICLE_ID, maintenance.getVehicleId());
                values.put(DatabaseHelper.KEY_MAINTENANCE_TYPE, maintenance.getMaintenanceType());
                values.put(DatabaseHelper.KEY_MAINTENANCE_DATE, maintenance.getMaintenanceDate());
                values.put(DatabaseHelper.KEY_COST, maintenance.getCost());
                values.put(DatabaseHelper.KEY_DESCRIPTION, maintenance.getDescription());
                values.put(DatabaseHelper.KEY_STATUS, maintenance.getStatus());
                values.put(DatabaseHelper.KEY_CREATED_AT, DatabaseHelper.getDateTime());
                values.put(DatabaseHelper.KEY_CREATED_BY, maintenance.getCreatedBy());

                maintenanceId = db.insert(DatabaseHelper.TABLE_MAINTENANCE, null, values);
                db.setTransactionSuccessful();
                Log.d(TAG, "Created new maintenance record with ID: " + maintenanceId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating maintenance record: " + e.getMessage());
        } finally {
            db.endTransaction();
        }

        return maintenanceId;
    }

    /**
     * 获取单个维护记录
     */
    public Maintenance getMaintenance(long maintenanceId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Maintenance maintenance = null;

        try {
            Cursor cursor = db.query(DatabaseHelper.TABLE_MAINTENANCE,
                    null,
                    DatabaseHelper.KEY_ID + "=?",
                    new String[]{String.valueOf(maintenanceId)},
                    null, null, null);

            if (cursor.moveToFirst()) {
                maintenance = cursorToMaintenance(cursor);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting maintenance record: " + e.getMessage());
        }

        return maintenance;
    }

    /**
     * 获取所有维护记录
     */
    public List<Maintenance> getAllMaintenance() {
        List<Maintenance> maintenanceList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_MAINTENANCE;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    Maintenance maintenance = cursorToMaintenance(cursor);
                    maintenanceList.add(maintenance);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting all maintenance records: " + e.getMessage());
        }

        return maintenanceList;
    }

    /**
     * 获取车辆的维护记录
     */
    public List<Maintenance> getVehicleMaintenance(long vehicleId) {
        List<Maintenance> maintenanceList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            Cursor cursor = db.query(DatabaseHelper.TABLE_MAINTENANCE,
                    null,
                    DatabaseHelper.KEY_VEHICLE_ID + "=?",
                    new String[]{String.valueOf(vehicleId)},
                    null, null, DatabaseHelper.KEY_MAINTENANCE_DATE + " DESC");

            if (cursor.moveToFirst()) {
                do {
                    Maintenance maintenance = cursorToMaintenance(cursor);
                    maintenanceList.add(maintenance);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting vehicle maintenance records: " + e.getMessage());
        }

        return maintenanceList;
    }

    /**
     * 更新维护记录
     */
    public int updateMaintenance(Maintenance maintenance) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = 0;

        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.KEY_MAINTENANCE_TYPE, maintenance.getMaintenanceType());
            values.put(DatabaseHelper.KEY_MAINTENANCE_DATE, maintenance.getMaintenanceDate());
            values.put(DatabaseHelper.KEY_COST, maintenance.getCost());
            values.put(DatabaseHelper.KEY_DESCRIPTION, maintenance.getDescription());
            values.put(DatabaseHelper.KEY_STATUS, maintenance.getStatus());

            rowsAffected = db.update(DatabaseHelper.TABLE_MAINTENANCE,
                    values,
                    DatabaseHelper.KEY_ID + " = ?",
                    new String[]{String.valueOf(maintenance.getId())});

            Log.d(TAG, "Updated maintenance record with ID: " + maintenance.getId());
        } catch (Exception e) {
            Log.e(TAG, "Error updating maintenance record: " + e.getMessage());
        }

        return rowsAffected;
    }

    /**
     * 完成维护
     */
    public boolean completeMaintenance(long maintenanceId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;

        db.beginTransaction();
        try {
            // 获取维护记录
            Maintenance maintenance = getMaintenance(maintenanceId);
            if (maintenance != null) {
                // 更新维护记录状态
                ContentValues maintenanceValues = new ContentValues();
                maintenanceValues.put(DatabaseHelper.KEY_STATUS, 1); // 1表示已完成

                int maintenanceUpdated = db.update(DatabaseHelper.TABLE_MAINTENANCE,
                        maintenanceValues,
                        DatabaseHelper.KEY_ID + " = ?",
                        new String[]{String.valueOf(maintenanceId)});

                // 更新车辆状态为可用
                if (maintenanceUpdated > 0) {
                    boolean vehicleUpdated = vehicleDAO.updateVehicleStatus(maintenance.getVehicleId(), 1); // 1表示可用
                    if (vehicleUpdated) {
                        success = true;
                        db.setTransactionSuccessful();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error completing maintenance: " + e.getMessage());
        } finally {
            db.endTransaction();
        }

        return success;
    }

    /**
     * 获取进行中的维护记录
     */
    public List<Maintenance> getActiveMaintenance() {
        List<Maintenance> maintenanceList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            Cursor cursor = db.query(DatabaseHelper.TABLE_MAINTENANCE,
                    null,
                    DatabaseHelper.KEY_STATUS + "=?",
                    new String[]{"0"}, // 0表示进行中
                    null, null, DatabaseHelper.KEY_MAINTENANCE_DATE + " ASC");

            if (cursor.moveToFirst()) {
                do {
                    Maintenance maintenance = cursorToMaintenance(cursor);
                    maintenanceList.add(maintenance);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting active maintenance records: " + e.getMessage());
        }

        return maintenanceList;
    }

    /**
     * 统计维护成本
     */
    public double calculateMaintenanceCost(long vehicleId, String startDate, String endDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        double totalCost = 0;

        try {
            String query = "SELECT SUM(" + DatabaseHelper.KEY_COST + ") FROM " + DatabaseHelper.TABLE_MAINTENANCE +
                    " WHERE " + DatabaseHelper.KEY_VEHICLE_ID + " = ?" +
                    " AND " + DatabaseHelper.KEY_MAINTENANCE_DATE + " BETWEEN ? AND ?" +
                    " AND " + DatabaseHelper.KEY_STATUS + " = 1"; // 只计算已完成的维护记录

            Cursor cursor = db.rawQuery(query, new String[]{
                    String.valueOf(vehicleId),
                    startDate,
                    endDate
            });

            if (cursor.moveToFirst()) {
                totalCost = cursor.getDouble(0);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error calculating maintenance cost: " + e.getMessage());
        }

        return totalCost;
    }

    /**
     * 将Cursor转换为Maintenance对象
     */
    private Maintenance cursorToMaintenance(Cursor cursor) {
        Maintenance maintenance = new Maintenance();
        maintenance.setId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_ID)));
        maintenance.setVehicleId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_VEHICLE_ID)));
        maintenance.setMaintenanceType(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_MAINTENANCE_TYPE)));
        maintenance.setMaintenanceDate(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_MAINTENANCE_DATE)));
        maintenance.setCost(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.KEY_COST)));
        maintenance.setDescription(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_DESCRIPTION)));
        maintenance.setStatus(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_STATUS)));
        maintenance.setCreateTime(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_CREATED_AT)));
        maintenance.setCreatedBy(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_CREATED_BY)));
        return maintenance;
    }
}