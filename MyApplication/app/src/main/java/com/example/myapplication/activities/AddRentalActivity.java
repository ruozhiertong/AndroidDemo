package com.example.myapplication.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.R;
import com.example.myapplication.dao.RentalDAO;
import com.example.myapplication.dao.VehicleDAO;
import com.example.myapplication.dao.CustomerDAO;
import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.models.Rental;
import com.example.myapplication.models.Vehicle;
import com.example.myapplication.models.Customer;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class AddRentalActivity extends AppCompatActivity {
    private AutoCompleteTextView spinnerCustomer;
    private AutoCompleteTextView spinnerVehicle;
    private TextInputEditText etStartDate;
    private TextInputEditText etEndDate;
    private TextInputEditText etDeposit;
    private TextInputEditText etAmount;
    private TextInputEditText etRemarks;
    private MaterialButton btnSave;

    private RentalDAO rentalDAO;
    private VehicleDAO vehicleDAO;
    private CustomerDAO customerDAO;
    private Rental existingRental;

    private List<Customer> customers;
    private List<Vehicle> vehicles;
    private Calendar startDateCalendar = Calendar.getInstance();
    private Calendar endDateCalendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rental);

        // Initialize Database Helpers
//        DatabaseHelper dbHelper = new DatabaseHelper(this);
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);

        rentalDAO = new RentalDAO(dbHelper);
        vehicleDAO = new VehicleDAO(dbHelper);
        customerDAO = new CustomerDAO(dbHelper);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Check if we're in edit mode
        int rentalId = getIntent().getIntExtra("rental_id", -1);
        getSupportActionBar().setTitle(rentalId != -1 ? R.string.edit_rental : R.string.add_rental);

        // Initialize Views
        initializeViews();
        setupDatePickers();
        loadCustomersAndVehicles(rentalId != -1);

        // Check if editing existing rental
        if (rentalId != -1) {
            loadExistingRental(rentalId);
        }

        // Setup Save Button
        btnSave.setOnClickListener(v -> saveRental());
    }

    private void initializeViews() {
        spinnerCustomer = findViewById(R.id.spinner_customer);
        spinnerVehicle = findViewById(R.id.spinner_vehicle);
        etStartDate = findViewById(R.id.et_start_date);
        etEndDate = findViewById(R.id.et_end_date);
        etDeposit = findViewById(R.id.et_deposit);
        etAmount = findViewById(R.id.et_amount);
        etRemarks = findViewById(R.id.et_remarks);
        btnSave = findViewById(R.id.btn_save);
    }

    private void setupDatePickers() {
        // Start Date Picker
        etStartDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        startDateCalendar.set(year, month, dayOfMonth);
                        etStartDate.setText(dateFormat.format(startDateCalendar.getTime()));
                        calculateTotalAmount();
                    },
                    startDateCalendar.get(Calendar.YEAR),
                    startDateCalendar.get(Calendar.MONTH),
                    startDateCalendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            datePickerDialog.show();
        });

        // End Date Picker
        etEndDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        endDateCalendar.set(year, month, dayOfMonth);
                        etEndDate.setText(dateFormat.format(endDateCalendar.getTime()));
                        calculateTotalAmount();
                    },
                    endDateCalendar.get(Calendar.YEAR),
                    endDateCalendar.get(Calendar.MONTH),
                    endDateCalendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.getDatePicker().setMinDate(startDateCalendar.getTimeInMillis());
            datePickerDialog.show();
        });
    }

    private void loadCustomersAndVehicles(boolean editMode) {
        // Load Customers
        List<String> customerNames = new ArrayList<String>();
        if (!editMode){
            customers = customerDAO.getAllCustomers();
            customerNames = customers.stream()
                    .map(customer -> customer.getName() + " (" + customer.getIdNumber() + ")")
                    .collect(Collectors.toList());
        }


        ArrayAdapter<String> customerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                customerNames
        );
        spinnerCustomer.setAdapter(customerAdapter);

        // Load Available Vehicles
        List<String> vehicleNames = new ArrayList<String>();
        if (!editMode){
            vehicles = vehicleDAO.getVehiclesByStatus(1);
            vehicleNames = vehicles.stream()
                    .map(vehicle -> vehicle.getBrand() + " " + vehicle.getModel() +
                            " (" + vehicle.getPlateNumber() + ")")
                    .collect(Collectors.toList());
        }

        ArrayAdapter<String> vehicleAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                vehicleNames
        );
        spinnerVehicle.setAdapter(vehicleAdapter);

        // Setup Selection Listeners
        spinnerVehicle.setOnItemClickListener((parent, view, position, id) -> {
            calculateTotalAmount();
        });
    }

    private void loadExistingRental(int rentalId) {
        existingRental = rentalDAO.getRental(rentalId);
        if (existingRental != null) {
            // Find and set customer
//            Customer customer = customerDAO.getCustomer(existingRental.getCustomerId());
//            String customerStr = customer.getName() + " (" + customer.getIdNumber() + ")";
//            spinnerCustomer.setText(customerStr, false);
            spinnerCustomer.setText(existingRental.getCustomerDes());
            spinnerCustomer.setEnabled(false);

            // Find and set vehicle
//            Vehicle vehicle = vehicleDAO.getVehicle(existingRental.getVehicleId());
//            String vehicleStr = vehicle.getBrand() + " " + vehicle.getModel() +
//                    " (" + vehicle.getPlateNumber() + ")";
//            spinnerVehicle.setText(vehicleStr, false);
            spinnerVehicle.setText(existingRental.getVehicleDes());
            spinnerVehicle.setEnabled(false);

            // Set other fields
            etStartDate.setText(existingRental.getStartDate());
            etEndDate.setText(existingRental.getEndDate());
            etDeposit.setText(String.valueOf(existingRental.getDeposit()));
            etAmount.setText(String.valueOf(existingRental.getTotalAmount()));

            etRemarks.setText(existingRental.getRemarks());

            // Disable editing if rental is completed or cancelled
            if (existingRental.getStatus() != 0) {
                disableAllFields();
            }
        }
    }

    private void calculateTotalAmount() {

        if (existingRental == null){
            String selectedVehicle = spinnerVehicle.getText().toString();
            if (!selectedVehicle.isEmpty() && !TextUtils.isEmpty(etStartDate.getText()) &&
                    !TextUtils.isEmpty(etEndDate.getText())) {
                // Find selected vehicle
                Vehicle vehicle = findVehicleBySpinnerText(selectedVehicle);
                if (vehicle != null) {
                    // Calculate days between start and end date
                    long days = (endDateCalendar.getTimeInMillis() - startDateCalendar.getTimeInMillis()) /
                            (24 * 60 * 60 * 1000) + 1;
                    double totalAmount = vehicle.getDailyRate() * days;
                    etAmount.setHint(String.format(Locale.getDefault(),
                            getString(R.string.suggested_amount), totalAmount));
                    etDeposit.setHint(String.format(Locale.getDefault(),
                            getString(R.string.suggested_deposit), totalAmount));
                }
            }
        }

    }

    private Vehicle findVehicleBySpinnerText(String spinnerText) {
        String plateNumber = spinnerText.substring(spinnerText.lastIndexOf("(") + 1,
                spinnerText.lastIndexOf(")"));
        return vehicles.stream()
                .filter(v -> v.getPlateNumber().equals(plateNumber))
                .findFirst()
                .orElse(null);
    }

    private Customer findCustomerBySpinnerText(String spinnerText) {
        String idNumber = spinnerText.substring(spinnerText.lastIndexOf("(") + 1,
                spinnerText.lastIndexOf(")"));
        return customers.stream()
                .filter(c -> c.getIdNumber().equals(idNumber))
                .findFirst()
                .orElse(null);
    }

    private void disableAllFields() {
        spinnerCustomer.setEnabled(false);
        spinnerVehicle.setEnabled(false);
        etStartDate.setEnabled(false);
        etEndDate.setEnabled(false);
        etDeposit.setEnabled(false);
        etAmount.setEnabled(false);
        etRemarks.setEnabled(false);
        btnSave.setEnabled(false);
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(spinnerCustomer.getText())) {
            spinnerCustomer.setError(getString(R.string.required_field));
            return false;
        }

        if (TextUtils.isEmpty(spinnerVehicle.getText())) {
            spinnerVehicle.setError(getString(R.string.required_field));
            return false;
        }

        if (TextUtils.isEmpty(etStartDate.getText())) {
            etStartDate.setError(getString(R.string.required_field));
            return false;
        }

        if (TextUtils.isEmpty(etEndDate.getText())) {
            etEndDate.setError(getString(R.string.required_field));
            return false;
        }

        if (TextUtils.isEmpty(etDeposit.getText())) {
            etDeposit.setError(getString(R.string.required_field));
            return false;
        }
        if (TextUtils.isEmpty(etAmount.getText())) {
            etAmount.setError(getString(R.string.required_field));
            return false;
        }

        return true;
    }

    private void saveRental() {
        if (!validateInput()) {
            return;
        }

        // Create Rental Object
        Rental rental = new Rental();
        if (existingRental != null) {
            rental = existingRental;
//            rental.setId(existingRental.getId());
            rental.setStartDate(etStartDate.getText().toString());
            rental.setDeposit(Double.parseDouble(etDeposit.getText().toString()));
            rental.setTotalAmount(Double.parseDouble(etAmount.getText().toString()));
            rental.setRemarks(etRemarks.getText().toString());

        }else {
            // Set Customer
            Customer customer = findCustomerBySpinnerText(spinnerCustomer.getText().toString());
            rental.setCustomerId(customer.getId());

            // Set Vehicle
            Vehicle vehicle = findVehicleBySpinnerText(spinnerVehicle.getText().toString());
            rental.setVehicleId(vehicle.getId());

            rental.setStartDate(etStartDate.getText().toString());
            rental.setEndDate(etEndDate.getText().toString());
            rental.setDeposit(Double.parseDouble(etDeposit.getText().toString()));
            rental.setTotalAmount(Double.parseDouble(etAmount.getText().toString()));

            rental.setRemarks(etRemarks.getText().toString());

            // Calculate total amount
            long days = (endDateCalendar.getTimeInMillis() - startDateCalendar.getTimeInMillis()) /
                    (24 * 60 * 60 * 1000) + 1;
            rental.setTotalAmount(vehicle.getDailyRate() * days);

            // Set status as active
            rental.setStatus(0);

        }



        // Save to Database
        long result;
        if (existingRental != null) {
            result = rentalDAO.updateRental(rental);
        } else {
            result = rentalDAO.createRental(rental);
        }

        if (result > 0) {
            Toast.makeText(this, R.string.rental_saved, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, R.string.error_saving_rental, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}