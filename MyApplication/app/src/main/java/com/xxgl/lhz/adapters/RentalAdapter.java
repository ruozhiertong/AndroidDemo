package com.xxgl.lhz.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xxgl.lhz.database.DatabaseHelper;
import com.example.myapplication.R;
import com.xxgl.lhz.models.Rental;
import com.xxgl.lhz.dao.VehicleDAO;
import com.xxgl.lhz.dao.CustomerDAO;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class RentalAdapter extends RecyclerView.Adapter<RentalAdapter.RentalViewHolder> {
    private Context context;
    private List<Rental> rentals;
    private OnRentalClickListener listener;
    private VehicleDAO vehicleDAO;
    private CustomerDAO customerDAO;

    public interface OnRentalClickListener {
        void onRentalClick(Rental rental);
        void onCompleteClick(Rental rental);
        void onCancelClick(Rental rental);
        void onOverdueClick(Rental rental);
    }

    public RentalAdapter(Context context, List<Rental> rentals) {
        this.context = context;
        this.rentals = rentals;
//        DatabaseHelper dbHelper = new DatabaseHelper(context);
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        this.vehicleDAO = new VehicleDAO(dbHelper);
        this.customerDAO = new CustomerDAO(dbHelper);
    }

    public void setOnRentalClickListener(OnRentalClickListener listener) {
        this.listener = listener;
    }

    public void updateRentals(List<Rental> newRentals) {
        this.rentals = newRentals;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RentalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rental, parent, false);
        return new RentalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RentalViewHolder holder, int position) {
        Rental rental = rentals.get(position);
        holder.bind(rental);
    }

    @Override
    public int getItemCount() {
        return rentals.size();
    }

    class RentalViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCustomerName;
        private TextView tvVehicleInfo;
        private TextView tvRentalDates;
        private TextView tvStatus;
        private TextView tvAmount;
        private TextView tvDeposit;
        private MaterialButton btnComplete;
        private MaterialButton btnCancel;
        private MaterialButton btnOverdue;


        public RentalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
            tvVehicleInfo = itemView.findViewById(R.id.tv_vehicle_info);
            tvRentalDates = itemView.findViewById(R.id.tv_rental_dates);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvDeposit = itemView.findViewById(R.id.tv_deposite);
            btnComplete = itemView.findViewById(R.id.btn_complete);
            btnCancel = itemView.findViewById(R.id.btn_cancel);
            btnOverdue = itemView.findViewById(R.id.btn_overdue);
        }

        public void bind(Rental rental) {
            // Get customer and vehicle info
//            Customer customer = customerDAO.getCustomer(rental.getCustomerId());
//            Vehicle vehicle = vehicleDAO.getVehicle(rental.getVehicleId());

            // Set customer name
//            if (customer != null){
//                tvCustomerName.setText(customer.getName());
//            }else {
//                tvCustomerName.setText(rental.getCustomerDes());
//            }
//            if (vehicle != null){
//                // Set vehicle info
//                String vehicleInfo = String.format("%s %s (%s)",
//                        vehicle.getBrand(),
//                        vehicle.getModel(),
//                        vehicle.getPlateNumber());
//                tvVehicleInfo.setText(vehicleInfo);
//            } else{
//                tvVehicleInfo.setText(rental.getVehicleDes());
//            }

            tvCustomerName.setText(rental.getCustomerDes());
            tvVehicleInfo.setText(rental.getVehicleDes());

            // Set rental dates
            String rentalDates = String.format("%s - %s",
                    rental.getStartDate(),
                    rental.getEndDate());
            tvRentalDates.setText(rentalDates);


            // Set amount
//            String amount = String.format("%.2f", rental.getTotalAmount());
            String amount = String.format(context.getString(R.string.amount)+":%.2f", rental.getTotalAmount()); //TODO
            tvAmount.setText(amount);

            String deposit = String.format(context.getString(R.string.deposit) + ":%.2f", rental.getDeposit()); //TODO
            tvDeposit.setText(deposit);

            // Set status
            String status;
            int statusColor;
            boolean canComplete = false;
            boolean canCancel = false;
            boolean canOverdue = false;

            switch (rental.getStatus()) {
                case 0: // Active
                    status = context.getString(R.string.status_active);
                    statusColor = R.color.status_active;
                    canComplete = true;
                    canCancel = true;
                    canOverdue = true;
                    break;
                case 1: // Completed
                    status = context.getString(R.string.status_completed);
                    statusColor = R.color.status_completed;
                    break;
                case 2: // Overdued
                    status = context.getString(R.string.status_Overdued);
                    statusColor = R.color.status_overdued;
                    canComplete = true;
                    break;
                case 3: // Cancelled
                    status = context.getString(R.string.status_cancelled);
                    statusColor = R.color.status_cancelled;
                    break;
                default:
                    status = context.getString(R.string.status_unknown);
                    statusColor = R.color.status_unknown;
            }

            tvStatus.setText(status);
            tvStatus.setTextColor(context.getColor(statusColor));

            // Set button visibility and click listeners
            btnComplete.setVisibility(canComplete ? View.VISIBLE : View.GONE);
            btnCancel.setVisibility(canCancel ? View.VISIBLE : View.GONE);
            btnOverdue.setVisibility(canOverdue ? View.VISIBLE : View.GONE);


            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRentalClick(rental);
                }
            });

            boolean finalCanComplete = canComplete;
            btnComplete.setOnClickListener(v -> {
                if (listener != null && finalCanComplete) {
                    listener.onCompleteClick(rental);
                }
            });

            boolean finalCanCancel = canCancel;
            btnCancel.setOnClickListener(v -> {
                if (listener != null && finalCanCancel) {
                    listener.onCancelClick(rental);
                }
            });

            boolean finalcanOverdue = canOverdue;
            btnOverdue.setOnClickListener(v -> {
                if (listener != null && finalcanOverdue) {
                    listener.onOverdueClick(rental);
                }
            });
        }
    }
}