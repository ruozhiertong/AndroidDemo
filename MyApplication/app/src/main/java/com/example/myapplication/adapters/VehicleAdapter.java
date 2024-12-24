package com.example.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.models.Vehicle;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {
    private Context context;
    private List<Vehicle> vehicles;
    private OnVehicleClickListener listener;

    public interface OnVehicleClickListener {
        void onVehicleClick(Vehicle vehicle);
        void onDeleteClick(Vehicle vehicle);
        void onMaintenanceClick(Vehicle vehicle, int status);
    }

    public VehicleAdapter(Context context, List<Vehicle> vehicles) {
        this.context = context;
        this.vehicles = vehicles;
    }

    public void setOnVehicleClickListener(OnVehicleClickListener listener) {
        this.listener = listener;
    }

    public void updateVehicles(List<Vehicle> newVehicles) {
        this.vehicles = newVehicles;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vehicle, parent, false);
        return new VehicleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        Vehicle vehicle = vehicles.get(position);
        holder.bind(vehicle);
    }

    @Override
    public int getItemCount() {
        return vehicles.size();
    }

    class VehicleViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivVehicle;
        private TextView tvPlateNumber;
        private TextView tvBrandModel;
        private TextView tvVehicleType;
        private TextView tvDailyRate;
        private TextView tvStatus;
        private TextView tvYear;
        private TextView tvMile;
        private View btnDelete;
        private View btnMaintenance;

        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);
            ivVehicle = itemView.findViewById(R.id.iv_vehicle);
            tvPlateNumber = itemView.findViewById(R.id.tv_plate_number);
            tvBrandModel = itemView.findViewById(R.id.tv_brand_model);
            tvVehicleType = itemView.findViewById(R.id.tv_vehicle_type);
            tvDailyRate = itemView.findViewById(R.id.tv_daily_rate);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvYear = itemView.findViewById(R.id.tv_year);
            tvMile = itemView.findViewById(R.id.tv_mileage);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnMaintenance = itemView.findViewById(R.id.btn_maintenance);

            btnDelete.setVisibility(View.GONE);
        }

        public void bind(Vehicle vehicle) {
            System.out.println("bind:" + vehicle.getPhotoUrl());
            // 加载车辆图片
            if (vehicle.getPhotoUrl() != null && !vehicle.getPhotoUrl().isEmpty()) {
                Glide.with(context)
                        .load(vehicle.getPhotoUrl())
                        .placeholder(R.drawable.ic_car_placeholder)
                        .error(R.drawable.ic_car_placeholder)
                        .into(ivVehicle);
            } else {
                ivVehicle.setImageResource(R.drawable.ic_car_placeholder);
            }

            // 设置车辆信息
            tvPlateNumber.setText(vehicle.getPlateNumber());
            tvBrandModel.setText(String.format("%s %s", vehicle.getBrand(), vehicle.getModel()));
            tvVehicleType.setText(vehicle.getVehicleType());
            tvDailyRate.setText(String.format(context.getString(R.string.daily_rate_format),
                    vehicle.getDailyRate()));

            tvYear.setText(Integer.toString(vehicle.getYear()));

            tvMile.setText(Double.toString(vehicle.getMileage()));

            // 设置状态
            String status;
            int statusColor;
            boolean canRent = false;
            boolean canMaintenance = true;

            switch (vehicle.getStatus()) {
                case 1: // 可用
                    status = context.getString(R.string.status_available);
                    statusColor = R.color.status_available;
                    canRent = true;
                    break;
                case 2: // 已租出
                    status = context.getString(R.string.status_rented);
                    statusColor = R.color.status_rented;
                    canMaintenance = false;
                    break;
                case 3: // 维护中
                    status = context.getString(R.string.status_maintenance);
                    statusColor = R.color.status_maintenance;
                    break;
                default:
                    status = context.getString(R.string.status_unavailable);
                    statusColor = R.color.status_unavailable;
            }

            tvStatus.setText(status);
            tvStatus.setTextColor(context.getColor(statusColor));

            // 设置按钮状态和点击事件
            btnDelete.setEnabled(canRent);
            btnDelete.setAlpha(canRent ? 1.0f : 0.5f);
            btnMaintenance.setEnabled(canMaintenance);
            if (vehicle.getStatus() == 3){
                ((MaterialButton) btnMaintenance).setText(R.string.complete_maintenance);
            }else {
                ((MaterialButton) btnMaintenance).setText(R.string.maintenance);
            }
            btnMaintenance.setAlpha(canMaintenance ? 1.0f : 0.5f);

            // 设置点击事件
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVehicleClick(vehicle);
                }
            });

            boolean finalCanRent = canRent;
            btnDelete.setOnClickListener(v -> {
                if (listener != null && finalCanRent) {
                    listener.onDeleteClick(vehicle);
                }
            });

            boolean finalCanMaintenance = canMaintenance;
            int finalStatus = vehicle.getStatus();
            btnMaintenance.setOnClickListener(v -> {
                if (listener != null && finalCanMaintenance) {
                    listener.onMaintenanceClick(vehicle, finalStatus);
                }
            });
        }
    }
}