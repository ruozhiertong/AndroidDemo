package com.xxgl.lhz.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.xxgl.lhz.activities.AddVehicleActivity;
import com.xxgl.lhz.adapters.VehicleAdapter;
import com.xxgl.lhz.dao.VehicleDAO;
import com.xxgl.lhz.database.DatabaseHelper;
import com.xxgl.lhz.dialogs.FilterDialog;
import com.xxgl.lhz.models.Vehicle;
import com.example.myapplication.R;
//import com.example.myapplication.activities.VehicleDetailActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class VehicleManagementFragment extends Fragment implements
        VehicleAdapter.OnVehicleClickListener,
        FilterDialog.OnFilterAppliedListener {

    private RecyclerView recyclerView;
    private VehicleAdapter vehicleAdapter;
    private FloatingActionButton fabAddCar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View emptyView;

    private VehicleDAO vehicleDAO;
    private String currentQuery = "";
    private int currentFilter = 0; // 0: All, 1: Available, 2: Rented, 3: Maintenance

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        System.out.println("VehicleManagementFragment onCreate");

        // Initialize Database Helper
//        DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(getActivity());
        vehicleDAO = new VehicleDAO(dbHelper);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vehicle_management, container, false);

        // Set up toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle(R.string.vehicle_management);

        // Initialize views
        initializeViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        setupFab();

        // Load vehicles
        loadVehicles();

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        fabAddCar = view.findViewById(R.id.fab_add_car);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        emptyView = view.findViewById(R.id.empty_view);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        vehicleAdapter = new VehicleAdapter(getContext(), new ArrayList<>());
        vehicleAdapter.setOnVehicleClickListener(this);
        recyclerView.setAdapter(vehicleAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadVehicles);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.primary,
                R.color.primary_dark,
                R.color.accent
        );
    }

    private void setupFab() {
        fabAddCar.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddVehicleActivity.class);
            startActivity(intent);
        });
    }

    private void loadVehicles() {
        List<Vehicle> vehicles;

        if (!currentQuery.isEmpty()) {
            // Search with query
            vehicles = vehicleDAO.searchVehicles(currentQuery);
        } else {
            // Filter by status
            switch (currentFilter) {
                case 1: // Available
//                    vehicles = vehicleDAO.getAvailableVehicles();
//                    break;
                case 2: // Rented
//                    vehicles = filterByStatus(2);
//                    break;
                case 3: // Maintenance
//                    vehicles = filterByStatus(3);
//                    break;
//                    vehicles = filterByStatus(currentFilter);
                    vehicles = vehicleDAO.getVehiclesByStatus(currentFilter);
                    break;
                default: // All
                    vehicles = vehicleDAO.getAllVehicles();
            }
        }

        vehicleAdapter.updateVehicles(vehicles);
        swipeRefreshLayout.setRefreshing(false);
        updateEmptyView(vehicles.isEmpty());
    }

    private List<Vehicle> filterByStatus(int status) {
//        List<Vehicle> allVehicles = vehicleDAO.getAllVehicles();
//        List<Vehicle> filteredVehicles = new ArrayList<>();
//        for (Vehicle vehicle : allVehicles) {
//            if (vehicle.getStatus() == status) {
//                filteredVehicles.add(vehicle);
//            }
//        }
//        return filteredVehicles;
        return vehicleDAO.getVehiclesByStatus(status);
    }

    private void updateEmptyView(boolean isEmpty) {
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.management_menu, menu);

        // Setup search
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        setupSearchView(searchView);

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setupSearchView(SearchView searchView) {
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentQuery = query;
                loadVehicles();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                loadVehicles();
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_filter) {
            showFilterDialog();
            return true;
        }
//        } else if (itemId == R.id.action_sort) {
//            // TODO: Implement sorting
//            return true;
        else if (itemId == R.id.action_refresh) {
            loadVehicles();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFilterDialog() {
        FilterDialog dialog = FilterDialog.newInstance(R.string.filter_vehicles,R.array.filter_options, currentFilter);
        dialog.setOnFilterAppliedListener(this);
        dialog.show(getChildFragmentManager(), "FilterDialog");
    }

    @Override
    public void onFilterApplied(int filter) {
        currentFilter = filter;
        loadVehicles();
    }

    @Override
    public void onVehicleClick(Vehicle vehicle) {
        Intent intent = new Intent(getContext(), AddVehicleActivity.class);
        System.out.println("onVehicleClick" + vehicle.getId());
        intent.putExtra("vehicle_id", vehicle.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Vehicle vehicle) {
        // TODO: Implement rent functionality
        int ret = vehicleDAO.deleteVehicle(vehicle.getId());

        if (ret == 0)
            Snackbar.make(requireView(), R.string.delete_failed, Snackbar.LENGTH_SHORT).show();
        else
            loadVehicles();
    }

    @Override
    public void onMaintenanceClick(Vehicle vehicle, int status) {

        boolean success = false;
        if (status == 3) {
             success = vehicleDAO.updateVehicleStatus(vehicle.getId(), 1);
        }
        else {
             success = vehicleDAO.updateVehicleStatus(vehicle.getId(), 3);
        }
        if (success) {
            loadVehicles();
            Snackbar.make(requireView(), R.string.vehicle_maintenance_status_updated,
                            Snackbar.LENGTH_LONG)
//                    .setAction(R.string.undo, v -> {
//                        vehicleDAO.updateVehicleStatus(vehicle.getId(), 1);
//                        loadVehicles();
//                    })
                    .show();
        } else {
            Toast.makeText(getContext(), R.string.error_updating_status,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadVehicles();
    }
}