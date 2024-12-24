package com.example.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.models.Customer;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {
    private Context context;
    private List<Customer> customers;
    private OnCustomerClickListener listener;

    public interface OnCustomerClickListener {
        void onCustomerClick(Customer customer);
        void onRentalHistoryClick(Customer customer);
        void onBlacklistClick(Customer customer);
    }

    public CustomerAdapter(Context context, List<Customer> customers) {
        this.context = context;
        this.customers = customers;
    }

    public void setOnCustomerClickListener(OnCustomerClickListener listener) {
        this.listener = listener;
    }

    public void updateCustomers(List<Customer> newCustomers) {
        this.customers = newCustomers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_customer, parent, false);
        return new CustomerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        Customer customer = customers.get(position);
        holder.bind(customer);
    }

    @Override
    public int getItemCount() {
        return customers.size();
    }

    class CustomerViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private TextView tvIdNumber;
        private TextView tvPhone;
        private TextView tvLicenseNumber;
//        private TextView tvStatus;
        private TextView tvAddr;

        private MaterialButton btnRentalHistory;
        private MaterialButton btnBlacklist;

        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvIdNumber = itemView.findViewById(R.id.tv_id_number);
            tvPhone = itemView.findViewById(R.id.tv_phone);
            tvLicenseNumber = itemView.findViewById(R.id.tv_license_number);
//            tvStatus = itemView.findViewById(R.id.tv_status);
            tvAddr = itemView.findViewById(R.id.tv_addr);

            btnRentalHistory = itemView.findViewById(R.id.btn_rental_history);
            btnBlacklist = itemView.findViewById(R.id.btn_blacklist);
        }

        public void bind(Customer customer) {
            tvName.setText(context.getResources().getString(R.string.customer_name) + ": " +  customer.getName());
            tvIdNumber.setText(context.getResources().getString(R.string.id_number) + ": " +  customer.getIdNumber());
            tvPhone.setText(context.getResources().getString(R.string.hint_phone) + ":" + customer.getPhone());
            tvLicenseNumber.setText(context.getResources().getString(R.string.license_number) + ": " + customer.getLicenseNumber());
            tvAddr.setText(context.getResources().getString(R.string.customer_address )+ ": " + customer.getAddress());

            // 设置状态
//            String status;
//            int statusColor;
//            if (customer.getStatus() == 0) {
//                status = context.getString(R.string.status_blacklisted);
//                statusColor = R.color.status_unavailable;
//                btnBlacklist.setText(R.string.remove_from_blacklist);
//            } else {
//                status = context.getString(R.string.status_normal);
//                statusColor = R.color.status_available;
//                btnBlacklist.setText(R.string.add_to_blacklist);
//            }
//            tvStatus.setText(status);
//            tvStatus.setTextColor(context.getColor(statusColor));

            // 设置点击事件
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCustomerClick(customer);
                }
            });

            btnRentalHistory.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRentalHistoryClick(customer);
                }
            });

            btnBlacklist.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBlacklistClick(customer);
                }
            });
        }
    }
}