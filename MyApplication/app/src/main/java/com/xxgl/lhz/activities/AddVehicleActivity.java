package com.xxgl.lhz.activities;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.xxgl.lhz.dao.VehicleDAO;
import com.xxgl.lhz.database.DatabaseHelper;
import com.xxgl.lhz.models.Vehicle;
import com.example.myapplication.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;

import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.OptionalInt;
import java.util.stream.IntStream;

public class AddVehicleActivity extends AppCompatActivity {
    private TextInputEditText etPlateNumber;
    private TextInputEditText etBrand;
    private TextInputEditText etModel;
    private TextInputEditText etType;
    private TextInputEditText etColor;
    private TextInputEditText etSeats;
    private TextInputEditText etYear;
    private TextInputEditText etMileage;
    private TextInputEditText etDailyRate;
    private AutoCompleteTextView autoVehicleStatus;
    private String[] vehicleStatusStr;


    private MaterialButton btnImg;
    private MaterialButton btnSave;
    private MaterialButton btnDelete;
    // 请求码常量
    //    private static final int PERMISSIONS_REQUEST_CODE = 2; //权限相关
    private static final int REQUEST_CODE_PICK_IMAGE = 1; // 可以是任何唯一的整数
    private static final int REQUEST_MEDIA_PERMISSIONS = 3;

    private ImageView ivCarImage;
    private String imgURI;


    private VehicleDAO vehicleDAO;
    private Vehicle existingVehicle;

    private int vehicleId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vehicle);

        // Initialize Database Helper
//        DatabaseHelper dbHelper = new DatabaseHelper(this);
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);

        vehicleDAO = new VehicleDAO(dbHelper);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setTitle(R.string.add_vehicle);


        // Check if we're in edit mode
        vehicleId = getIntent().getIntExtra("vehicle_id", -1);

        // Set title
        getSupportActionBar().setTitle(vehicleId != -1 ? R.string.edit_vehicle : R.string.add_vehicle);

        // Initialize Views
        initializeViews();

        // Check if editing existing vehicle
        if (vehicleId != -1) {
//            int vehicleId = getIntent().getIntExtra("vehicle_id", -1);
            loadExistingVehicle(vehicleId);
        }else{
            btnDelete.setVisibility(MaterialButton.GONE);
        }

        // Setup Save Button
        btnSave.setOnClickListener(v -> saveVehicle());
        btnDelete.setOnClickListener(v -> btnDeleteVehicle());
        btnImg.setOnClickListener(v -> btnImageClicked());
    }


//    //动态权限请求。
//    private boolean ifNeedRequestPermissions() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
//            return true;
//        }
//        return false;
//    }
    
    private void btnImageClicked() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
//            // 没有权限，需要请求
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_CODE);
//        } else {
//            // 已有权限，可以直接使用相关功能
//            Intent intent = new Intent(Intent.ACTION_PICK);
//            intent.setType("image/*");
//            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
//        }

//        if (!ifNeedRequestPermissions()){
//            pickImage();
//        }

        pickImage();
    }

    private void saveImageToInternalStorage(Intent data, String path) {
        ContentResolver contentResolver = getContentResolver();
        Uri selectedImageUri = data.getData();
        if (selectedImageUri != null) {
            try (InputStream in = contentResolver.openInputStream(selectedImageUri);
                 OutputStream out = new FileOutputStream(new File(path))) {
                // 将输入流复制到输出流
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
                // 刷新输出流，确保所有数据都写入
                out.flush();
            } catch (Exception e) {
                Log.e("SaveImage", "Error saving the image", e);
            }
        } else {
            Log.e("SaveImage", "Image Uri is null");
        }
    }


    private void saveImageToInternalStorage(ImageView imageView, String filename) {
        Drawable drawable = imageView.getDrawable();
        Bitmap bitmap = null;
        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
            // 保存Bitmap到内部存储
            if (bitmap != null) {
                // 定义保存图片的路径和文件名
                File storageDir = getApplicationContext().getFilesDir();
                File imageFile = new File(storageDir, filename);

                try {
                    // 使用FileOutputStream写入图片
                    FileOutputStream out = new FileOutputStream(imageFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // 压缩格式和质量
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void pickImage(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == PERMISSIONS_REQUEST_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // 用户同意了权限请求，可以执行相关操作
//                pickImage();
//            } else {
//                // 用户拒绝了权限请求，可以给出提示或禁用相关功能
//                // 权限被拒绝，提示用户
//                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
//            }
//        }else if (requestCode == REQUEST_MEDIA_PERMISSIONS) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                loadExistingVehicle(vehicleId);
//            } else {
//                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            imgURI = selectedImageUri.toString();
            imgURI = getApplicationContext().getFilesDir()  + imgURI.substring(imgURI.lastIndexOf("/") + 1);
            System.out.println("onActivityResult imgURI:" + imgURI);
            saveImageToInternalStorage(data, imgURI);
            try {
                // 获取持久化的URI权限

//                final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
//                getContentResolver().takePersistableUriPermission(selectedImageUri, takeFlags);
//                int takeFlags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
//                getContentResolver().takePersistableUriPermission(selectedImageUri, takeFlags);

                System.out.println("onActivityResult: " + selectedImageUri.toString());
//            imgURI = selectedImageUri.getPath().replace("/raw","");
//            imgURI = selectedImageUri.toString();
            System.out.println("onActivityResult: " + imgURI);
//            ivCarImage.setImageURI(selectedImageUri);
                System.out.println("AddVehicleActivity Selected image URI: " + selectedImageUri);
                System.out.println("AddVehicleActivity URI scheme: " + selectedImageUri.getScheme());
                // 使用Glide加载新选择的图片
                Glide.with(getApplicationContext())
                        .load(imgURI)
                        .placeholder(R.drawable.ic_car_placeholder)
                        .error(R.drawable.ic_car_placeholder)
                        .into(ivCarImage);

//                saveImageToInternalStorage(ivCarImage,imgURI.substring(imgURI.lastIndexOf("/") + 1));

            }catch (SecurityException e) {
                System.out.println("AddVehicle ailed to take persistable permission: " + e.getMessage());
                Toast.makeText(this, "Failed to save image permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initializeViews() {
        etPlateNumber = findViewById(R.id.et_plate_number);
        etBrand = findViewById(R.id.et_brand);
        etModel = findViewById(R.id.et_model);
        etType = findViewById(R.id.et_type);
        etColor = findViewById(R.id.et_color);
        etSeats = findViewById(R.id.et_seats);
        etYear = findViewById(R.id.et_year);
        etMileage = findViewById(R.id.et_mileage);
        etDailyRate = findViewById(R.id.et_daily_rate);
        btnSave = findViewById(R.id.btn_save);
        btnDelete = findViewById(R.id.btn_delete);
        btnImg = findViewById(R.id.btn_upload_image);
        ivCarImage = findViewById(R.id.iv_car_image);

        // 初始化控件
        autoVehicleStatus = findViewById(R.id.spinner_vehicle_status);

        // 获取数据
        vehicleStatusStr = getResources().getStringArray(R.array.vehicle_status);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,  // 使用系统内置的下拉项布局
                vehicleStatusStr
        );

        // 设置适配器
        autoVehicleStatus.setAdapter(adapter);

        // 设置默认值（可选）
        autoVehicleStatus.setText(vehicleStatusStr[0], false);

        // 监听选择事件
        autoVehicleStatus.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = (String) parent.getItemAtPosition(position);
//            Toast.makeText(this, "选中: " + selectedItem + ", 位置: " + position, Toast.LENGTH_SHORT).show();
        });
    }

    private void loadExistingVehicle(int vehicleId) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
//                    != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
//                        REQUEST_MEDIA_PERMISSIONS);
//                return;
//            }
//        } else {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
//                    != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                        REQUEST_MEDIA_PERMISSIONS);
//                return;
//            }
//        }

        existingVehicle = vehicleDAO.getVehicle(vehicleId);
        if (existingVehicle != null) {
            etPlateNumber.setText(existingVehicle.getPlateNumber());
            etBrand.setText(existingVehicle.getBrand());
            etModel.setText(existingVehicle.getModel());
            etType.setText(existingVehicle.getVehicleType());
            etColor.setText(existingVehicle.getColor());
            etSeats.setText(String.valueOf(existingVehicle.getSeats()));
            etYear.setText(String.valueOf(existingVehicle.getYear()));
            etMileage.setText(String.valueOf(existingVehicle.getMileage()));
            etDailyRate.setText(String.valueOf(existingVehicle.getDailyRate()));

            autoVehicleStatus.setText(vehicleStatusStr[existingVehicle.getStatus() - 1], false);


            System.out.println("loadExistingVehicle:" + existingVehicle.getPhotoUrl());
//            Uri imageUri = Uri.parse("content:" + existingVehicle.getPhotoUrl());
//            File file = new File(existingVehicle.getPhotoUrl());
//            Uri imageUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);
//            Uri imageUri = Uri.parse(existingVehicle.getPhotoUrl());
//            ivCarImage.setImageURI(imageUri);

            if (existingVehicle.getPhotoUrl() != null && !existingVehicle.getPhotoUrl().isEmpty()) {
                System.out.println("loadExistingVehicle:   ===" + existingVehicle.getPhotoUrl());
                Glide.with(this)
                        .load(existingVehicle.getPhotoUrl())
                        .placeholder(R.drawable.ic_car_placeholder)
                        .error(R.drawable.ic_car_placeholder)
                        .into(ivCarImage);
            } else {
                ivCarImage.setImageResource(R.drawable.ic_car_placeholder);
            }

            //出租状态不允许编辑。
            if (existingVehicle.getStatus() == 2){
                btnSave.setEnabled(false);
                btnDelete.setEnabled(false);
                btnImg.setEnabled(false);
            }
//            getSupportActionBar().setTitle(R.string.edit_vehicle);
        }
    }


    private void deleteVehicle() {

        int ret = vehicleDAO.deleteVehicle(existingVehicle.getId());
        if (ret > 0) {
            setResult(RESULT_OK);
            finish();
        }
    }

    private void btnDeleteVehicle() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete)
                .setMessage(R.string.delete_confirmation)
                .setPositiveButton(R.string.yes, (dialog, which) -> deleteVehicle())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void saveVehicle() {
        // Validate Input
        if (!validateInput()) {
            return;
        }

        // Create Vehicle Object
        Vehicle vehicle = new Vehicle();
        if (existingVehicle != null) {
            vehicle.setId(existingVehicle.getId());
        }

        vehicle.setPlateNumber(etPlateNumber.getText().toString().trim());
        vehicle.setBrand(etBrand.getText().toString().trim());
        vehicle.setModel(etModel.getText().toString().trim());
        vehicle.setVehicleType(etType.getText().toString().trim());
        vehicle.setColor(etColor.getText().toString().trim());
        vehicle.setSeats(Integer.parseInt(etSeats.getText().toString().trim()));
        vehicle.setYear(Integer.parseInt(etYear.getText().toString().trim()));
        vehicle.setMileage(Double.parseDouble(etMileage.getText().toString().trim()));
        vehicle.setDailyRate(Double.parseDouble(etDailyRate.getText().toString().trim()));
        vehicle.setPhotoUrl(imgURI);
//        vehicle.setStatus(1); // Default status: Available

            System.out.println("saveVehicle : " + autoVehicleStatus.getText().toString());
        OptionalInt index = IntStream.range(0, vehicleStatusStr.length)
                .filter(i -> vehicleStatusStr[i].equals(autoVehicleStatus.getText().toString()))
                .findFirst();
        vehicle.setStatus(index.getAsInt() + 1);
        // Save to Database
        long result;
        if (existingVehicle != null) {
            result = vehicleDAO.updateVehicle(vehicle);
        } else {
            result = vehicleDAO.createVehicle(vehicle);
        }

        if (result > 0) {
            Toast.makeText(this, R.string.vehicle_saved, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, R.string.error_saving_vehicle, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInput() {
        boolean isValid = true;

        // Validate Plate Number
        if (TextUtils.isEmpty(etPlateNumber.getText())) {
            etPlateNumber.setError(getString(R.string.required_field));
            isValid = false;
        } else if (existingVehicle == null &&
                vehicleDAO.isPlateNumberExists(etPlateNumber.getText().toString().trim())) {
            etPlateNumber.setError(getString(R.string.plate_number_exists));
            isValid = false;
        }

        // Validate Other Required Fields
        if (TextUtils.isEmpty(etBrand.getText())) {
            etBrand.setError(getString(R.string.required_field));
            isValid = false;
        }
        if (TextUtils.isEmpty(etModel.getText())) {
            etModel.setError(getString(R.string.required_field));
            isValid = false;
        }
        if (TextUtils.isEmpty(etType.getText())) {
            etType.setError(getString(R.string.required_field));
            isValid = false;
        }
        if (TextUtils.isEmpty(etSeats.getText())) {
            etSeats.setError(getString(R.string.required_field));
            isValid = false;
        }
        if (TextUtils.isEmpty(etYear.getText())) {
            etYear.setError(getString(R.string.required_field));
            isValid = false;
        }
        if (TextUtils.isEmpty(etDailyRate.getText())) {
            etDailyRate.setError(getString(R.string.required_field));
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
}