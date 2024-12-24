package com.example.myapplication.activities;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.dao.CustomerDAO;
import com.example.myapplication.dao.RentalDAO;
import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.models.Customer;
import com.example.myapplication.models.Rental;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public class AddCustomerActivity extends AppCompatActivity {

    private CustomerDAO customerDAO;
    private RentalDAO rentalDAO;
    private Customer customer;
    private boolean isEditMode = false;

    private TextInputLayout tilName;
    private TextInputLayout tilIdNumber;
    private TextInputLayout tilPhone;
    private TextInputLayout tilEmail;
    private TextInputLayout tilAddress;
    private TextInputLayout tilLicenseNumber;
    private TextInputEditText etName;
    private TextInputEditText etIdNumber;
    private TextInputEditText etPhone;
    private TextInputEditText etEmail;
    private TextInputEditText etAddress;
    private TextInputEditText etLicenseNumber;
    private MaterialButton btnSave;
    private MaterialButton btnDelete;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        System.out.println("AddCustomerActivity onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_customer);

        // Initialize DAO
//        DatabaseHelper dbHelper = new DatabaseHelper(this);
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        customerDAO = new CustomerDAO(dbHelper);
        rentalDAO = new RentalDAO(dbHelper);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Check if we're in edit mode
        int customerId = getIntent().getIntExtra("customer_id", -1);
        if (customerId != -1){
            isEditMode = true;
        }

        // Set title
        getSupportActionBar().setTitle(isEditMode ? R.string.edit_customer : R.string.add_customer);

        // Initialize views
        initializeViews();

        // Load customer data if in edit mode
        if (isEditMode) {
            loadCustomerData(customerId);

            //如果有在借车辆，无法进行删除
            List<Rental> rentals= rentalDAO.getCustomerRentals(customerId);
            boolean inProcessd = false;
            for (Rental r : rentals){
                if (r.getStatus() == 0 || r.getStatus() == 2){
                    inProcessd = true;
                    break;
                }
            }
            if (inProcessd) {
                btnDelete.setEnabled(false);
            }
        }else{
            btnDelete.setVisibility(View.GONE);
        }

        // Setup save button click
        btnSave.setOnClickListener(v -> saveCustomer());

        btnDelete.setOnClickListener(v -> btnDeleteCustomer() );
    }

    private void initializeViews() {
        tilName = findViewById(R.id.til_name);
        tilIdNumber = findViewById(R.id.til_id_number);
        tilPhone = findViewById(R.id.til_phone);
        tilEmail = findViewById(R.id.til_email);
        tilAddress = findViewById(R.id.til_address);
        tilLicenseNumber = findViewById(R.id.til_license_number);
        etName = findViewById(R.id.et_name);
        etIdNumber = findViewById(R.id.et_id_number);
        etPhone = findViewById(R.id.et_phone);
        etEmail = findViewById(R.id.et_email);
        etAddress = findViewById(R.id.et_address);
        etLicenseNumber = findViewById(R.id.et_license_number);
        btnSave = findViewById(R.id.btn_save);
        btnDelete = findViewById(R.id.btn_delete);
    }

    private void loadCustomerData(int customerId) {
        customer = customerDAO.getCustomer(customerId);
        if (customer != null) {
            etName.setText(customer.getName());
            etIdNumber.setText(customer.getIdNumber());
            etPhone.setText(customer.getPhone());
            etEmail.setText(customer.getEmail());
            etAddress.setText(customer.getAddress());
            etLicenseNumber.setText(customer.getLicenseNumber());

            // Disable ID number editing in edit mode
            etIdNumber.setEnabled(false);
            tilIdNumber.setEnabled(false);
        }
    }



    private void deleteCustomer(){
        int ret = customerDAO.deleteCustomer(customer.getId());
        if (ret > 0) {
            setResult(RESULT_OK);
            finish();
        }
    }
    private void btnDeleteCustomer() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete)
                .setMessage(R.string.delete_confirmation)
                .setPositiveButton(R.string.yes, (dialog, which) -> deleteCustomer())
                .setNegativeButton(R.string.no, null)
                .show();
    }
    private void saveCustomer() {
        if (!validateInput()) {
            return;
        }

        // Create or get customer object
        if (customer == null) {
            customer = new Customer();
        }

        // Set customer properties
        customer.setName(etName.getText().toString().trim());
        customer.setIdNumber(etIdNumber.getText().toString().trim());
        customer.setPhone(etPhone.getText().toString().trim());
        customer.setEmail(etEmail.getText().toString().trim());
        customer.setAddress(etAddress.getText().toString().trim());
        customer.setLicenseNumber(etLicenseNumber.getText().toString().trim());
        customer.setStatus(1); // Set as active by default

        // Save to database
        int result;
        if (isEditMode) {
            result = customerDAO.updateCustomer(customer);
        } else {
            result = customerDAO.createCustomer(customer);
        }

        if (result > 0) {
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, R.string.error_saving_customer, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInput() {
        boolean isValid = true;

        // Reset all errors
        tilName.setError(null);
        tilIdNumber.setError(null);
        tilPhone.setError(null);
        tilEmail.setError(null);
        tilLicenseNumber.setError(null);

        // Name validation
        String name = etName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            tilName.setError(getString(R.string.error_empty_name));
            isValid = false;
        }

        // ID Number validation
        String idNumber = etIdNumber.getText().toString().trim();
        if (TextUtils.isEmpty(idNumber)) {
            tilIdNumber.setError(getString(R.string.error_empty_id_number));
            isValid = false;
        } else if (!isEditMode && customerDAO.getCustomerByIdNumber(idNumber) != null) {
            tilIdNumber.setError(getString(R.string.error_id_number_exists));
            isValid = false;
        }

        // Phone validation
        String phone = etPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            tilPhone.setError(getString(R.string.error_empty_phone));
            isValid = false;
        } else if (!android.util.Patterns.PHONE.matcher(phone).matches()) {
            tilPhone.setError(getString(R.string.error_invalid_phone));
            isValid = false;
        }

        // Email validation
        String email = etEmail.getText().toString().trim();
        if (!TextUtils.isEmpty(email) && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            isValid = false;
        }

        // License Number validation
        String licenseNumber = etLicenseNumber.getText().toString().trim();
        if (TextUtils.isEmpty(licenseNumber)) {
            tilLicenseNumber.setError(getString(R.string.error_empty_license_number));
            isValid = false;
        }

        return isValid;


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStart() {
        System.out.println("AddCustomerActivity onStart");

        super.onStart();
    }

    @Override
    protected void onRestart() {
        System.out.println("AddCustomerActivity onRestart");

        super.onRestart();
    }

    @Override
    protected void onResume() {
        System.out.println("AddCustomerActivity onResume");

        super.onResume();
    }

    @Override
    protected void onDestroy() {
        System.out.println("AddCustomerActivity onDestroy");

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        System.out.println("AddCustomerActivity onPause");

        super.onPause();
    }

    @Override
    protected void onStop() {
        System.out.println("AddCustomerActivity onStop");
        super.onStop();
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        System.out.println("AddCustomerActivity onSaveInstanceState");
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onStateNotSaved() {
        System.out.println("AddCustomerActivity onStateNotSaved");
        super.onStateNotSaved();
    }
}