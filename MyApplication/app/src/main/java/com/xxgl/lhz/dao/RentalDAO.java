package com.xxgl.lhz.dao;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import com.xxgl.lhz.database.DatabaseHelper;
import com.xxgl.lhz.models.Customer;
import com.xxgl.lhz.models.Rental;
import com.xxgl.lhz.models.Vehicle;

import java.util.ArrayList;
import java.util.List;

public class RentalDAO {
    private static final String TAG = "RentalDAO";
    private DatabaseHelper dbHelper;
    private VehicleDAO vehicleDAO;
    private CustomerDAO customerDAO;

    public RentalDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.vehicleDAO = new VehicleDAO(dbHelper);
        this.customerDAO = new CustomerDAO(dbHelper);
    }

    /**
     * 创建新租赁记录
     */
    public long createRental(Rental rental) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rentalId = -1;

        Vehicle vh = vehicleDAO.getVehicle(rental.getVehicleId());
        Customer cus = customerDAO.getCustomer(rental.getCustomerId());

        db.beginTransaction();
        try {
            // 首先更新车辆状态为已租出
            boolean vehicleUpdated = vehicleDAO.updateVehicleStatus(rental.getVehicleId(), 2); // 2表示已租出

            if (vehicleUpdated) {
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.KEY_VEHICLE_ID, rental.getVehicleId());
                values.put(DatabaseHelper.KEY_CUSTOMER_ID, rental.getCustomerId());
                values.put(DatabaseHelper.KEY_VEHICLE_DES, vh.getBrand() + " " + vh.getModel() + " ("+ vh.getPlateNumber() + ")");
                values.put(DatabaseHelper.KEY_CUSTOMER_DES, cus.getName() + " (" + cus.getIdNumber() + ")");
                values.put(DatabaseHelper.KEY_START_DATE, rental.getStartDate());
                values.put(DatabaseHelper.KEY_END_DATE, rental.getEndDate());
                values.put(DatabaseHelper.KEY_TOTAL_AMOUNT, rental.getTotalAmount());
                values.put(DatabaseHelper.KEY_DEPOSIT, rental.getDeposit());
                values.put(DatabaseHelper.KEY_STATUS, rental.getStatus());
                values.put(DatabaseHelper.KEY_CREATED_AT, DatabaseHelper.getDateTime());
                values.put(DatabaseHelper.KEY_UPDATED_AT, DatabaseHelper.getDateTime());
                values.put(DatabaseHelper.KEY_CREATED_BY, rental.getCreatedBy());
                values.put(DatabaseHelper.KEY_REMARKS, rental.getRemarks());

                rentalId = db.insert(DatabaseHelper.TABLE_RENTALS, null, values);
                db.setTransactionSuccessful();
                Log.d(TAG, "Created new rental with ID: " + rentalId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating rental: " + e.getMessage());
        } finally {
            db.endTransaction();
        }

        return rentalId;
    }

    /**
     * 获取单个租赁记录
     */
    public Rental getRental(int rentalId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Rental rental = null;

        try {
            Cursor cursor = db.query(DatabaseHelper.TABLE_RENTALS,
                    null,
                    DatabaseHelper.KEY_ID + "=?",
                    new String[]{String.valueOf(rentalId)},
                    null, null, null);

            if (cursor.moveToFirst()) {
                rental = cursorToRental(cursor);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting rental: " + e.getMessage());
        }

        return rental;
    }

    /**
     * 获取所有租赁记录
     */
    public List<Rental> getAllRentals() {
        List<Rental> rentals = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_RENTALS;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    Rental rental = cursorToRental(cursor);
                    rentals.add(rental);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting all rentals: " + e.getMessage());
        }

        return rentals;
    }

    /**
     * 更新租赁记录
     */
    public int updateRental(Rental rental) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = 0;

        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.KEY_START_DATE, rental.getStartDate());
            values.put(DatabaseHelper.KEY_END_DATE, rental.getEndDate());
            values.put(DatabaseHelper.KEY_ACTUAL_RETURN_DATE, rental.getActualReturnDate());
            values.put(DatabaseHelper.KEY_TOTAL_AMOUNT, rental.getTotalAmount());
            values.put(DatabaseHelper.KEY_DEPOSIT, rental.getDeposit());
            values.put(DatabaseHelper.KEY_STATUS, rental.getStatus());
            values.put(DatabaseHelper.KEY_UPDATED_AT, DatabaseHelper.getDateTime());
            values.put(DatabaseHelper.KEY_REMARKS, rental.getRemarks());

            rowsAffected = db.update(DatabaseHelper.TABLE_RENTALS,
                    values,
                    DatabaseHelper.KEY_ID + " = ?",
                    new String[]{String.valueOf(rental.getId())});

            Log.d(TAG, "Updated rental with ID: " + rental.getId());
        } catch (Exception e) {
            Log.e(TAG, "Error updating rental: " + e.getMessage());
        }

        return rowsAffected;
    }

    /**
     * 完成租赁（还车）
     */
    public boolean completeRental(int rentalId, String actualReturnDate) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;

        db.beginTransaction();
        try {
            // 获取租赁记录
            Rental rental = getRental(rentalId);
            if (rental != null) {
                // 更新租赁状态
                ContentValues rentalValues = new ContentValues();
                rentalValues.put(DatabaseHelper.KEY_ACTUAL_RETURN_DATE, actualReturnDate);
                rentalValues.put(DatabaseHelper.KEY_STATUS, 1); // 1表示已完成
                rentalValues.put(DatabaseHelper.KEY_UPDATED_AT, DatabaseHelper.getDateTime());

                int rentalUpdated = db.update(DatabaseHelper.TABLE_RENTALS,
                        rentalValues,
                        DatabaseHelper.KEY_ID + " = ?",
                        new String[]{String.valueOf(rentalId)});

                // 更新车辆状态为可用
                if (rentalUpdated > 0) {
                    boolean vehicleUpdated = vehicleDAO.updateVehicleStatus(rental.getVehicleId(), 1); // 1表示可用
                    if (vehicleUpdated) {
                        success = true;
                        db.setTransactionSuccessful();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error completing rental: " + e.getMessage());
        } finally {
            db.endTransaction();
        }

        return success;
    }

    /**
     * 获取客户的租赁历史
     */
    public List<Rental> getCustomerRentals(int customerId) {
        List<Rental> rentals = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            Cursor cursor = db.query(DatabaseHelper.TABLE_RENTALS,
                    null,
                    DatabaseHelper.KEY_CUSTOMER_ID + "=?",
                    new String[]{String.valueOf(customerId)},
                    null, null, DatabaseHelper.KEY_CREATED_AT + " DESC");

            if (cursor.moveToFirst()) {
                do {
                    Rental rental = cursorToRental(cursor);
                    rentals.add(rental);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting customer rentals: " + e.getMessage());
        }

        return rentals;
    }

    /**
     * 获取车辆的租赁历史
     */
    public List<Rental> getVehicleRentals(int vehicleId) {
        List<Rental> rentals = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            Cursor cursor = db.query(DatabaseHelper.TABLE_RENTALS,
                    null,
                    DatabaseHelper.KEY_VEHICLE_ID + "=?",
                    new String[]{String.valueOf(vehicleId)},
                    null, null, DatabaseHelper.KEY_CREATED_AT + " DESC");

            if (cursor.moveToFirst()) {
                do {
                    Rental rental = cursorToRental(cursor);
                    rentals.add(rental);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting vehicle rentals: " + e.getMessage());
        }

        return rentals;
    }

    /**
     * 获取当前进行中的租赁
     */
    public List<Rental> getRentalsByStatus(int status) {
        List<Rental> rentals = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            Cursor cursor = db.query(DatabaseHelper.TABLE_RENTALS,
                    null,
                    DatabaseHelper.KEY_STATUS + "=?",
                    new String[]{String.valueOf(status)}, // 0表示进行中
                    null, null, DatabaseHelper.KEY_END_DATE + " ASC");

            if (cursor.moveToFirst()) {
                do {
                    Rental rental = cursorToRental(cursor);
                    rentals.add(rental);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting active rentals: " + e.getMessage());
        }

        return rentals;
    }

    /**
     * 取消租赁
     */
    public boolean cancelRental(int rentalId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;

        db.beginTransaction();
        try {
            // 获取租赁记录
            Rental rental = getRental(rentalId);
            if (rental != null) {
                // 更新租赁状态
                ContentValues rentalValues = new ContentValues();
                rentalValues.put(DatabaseHelper.KEY_STATUS, 3); // 3表示已取消
                rentalValues.put(DatabaseHelper.KEY_UPDATED_AT, DatabaseHelper.getDateTime());

                int rentalUpdated = db.update(DatabaseHelper.TABLE_RENTALS,
                        rentalValues,
                        DatabaseHelper.KEY_ID + " = ?",
                        new String[]{String.valueOf(rentalId)});

                // 更新车辆状态为可用
                if (rentalUpdated > 0) {
                    boolean vehicleUpdated = vehicleDAO.updateVehicleStatus(rental.getVehicleId(), 1);
                    if (vehicleUpdated) {
                        success = true;
                        db.setTransactionSuccessful();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error canceling rental: " + e.getMessage());
        } finally {
            db.endTransaction();
        }

        return success;
    }

    public List<Rental> searchRentals(String keyword) {
        List<Rental> rentals = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            String searchQuery = "SELECT * FROM " + DatabaseHelper.TABLE_RENTALS +
                    " WHERE " + DatabaseHelper.KEY_CUSTOMER_ID + " LIKE '%" + keyword + "%'" +
                    " OR " + DatabaseHelper.KEY_VEHICLE_ID + " LIKE '%" + keyword + "%'" +
                    " OR " + DatabaseHelper.KEY_CUSTOMER_DES + " LIKE '%" + keyword + "%'" +
                    " OR " + DatabaseHelper.KEY_VEHICLE_DES + " LIKE '%" + keyword + "%'" +
                    " OR " + DatabaseHelper.KEY_START_DATE + " LIKE '%" + keyword + "%'" +
                    " OR " + DatabaseHelper.KEY_END_DATE + " LIKE '%" + keyword + "%'";

            Cursor cursor = db.rawQuery(searchQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    Rental r = cursorToRental(cursor);
                    rentals.add(r);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error searching vehicles: " + e.getMessage());
        }

        return rentals;
    }

    /**
     * 将Cursor转换为Rental对象
     */
    private Rental cursorToRental(Cursor cursor) {
        Rental rental = new Rental();
        rental.setId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_ID)));
        rental.setVehicleId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_VEHICLE_ID)));
        rental.setCustomerId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_CUSTOMER_ID)));
        rental.setCustomerDes(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_CUSTOMER_DES)));
        rental.setVehicleDes(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_VEHICLE_DES)));
        rental.setStartDate(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_START_DATE)));
        rental.setEndDate(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_END_DATE)));
        rental.setActualReturnDate(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_ACTUAL_RETURN_DATE)));
        rental.setTotalAmount(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.KEY_TOTAL_AMOUNT)));
        rental.setDeposit(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.KEY_DEPOSIT)));
        rental.setStatus(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_STATUS)));
        rental.setCreateTime(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_CREATED_AT)));
        rental.setUpdateTime(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_UPDATED_AT)));
        rental.setCreatedBy(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_CREATED_BY)));
        rental.setRemarks(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_REMARKS)));
        return rental;
    }
}