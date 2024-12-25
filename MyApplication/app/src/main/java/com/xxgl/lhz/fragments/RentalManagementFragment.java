package com.xxgl.lhz.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.myapplication.R;
import com.xxgl.lhz.activities.AddRentalActivity;
import com.xxgl.lhz.adapters.RentalAdapter;
import com.xxgl.lhz.dao.RentalDAO;
import com.xxgl.lhz.database.DatabaseHelper;
import com.xxgl.lhz.dialogs.FilterDialog;
import com.xxgl.lhz.models.Rental;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class RentalManagementFragment extends Fragment implements
        RentalAdapter.OnRentalClickListener,
        FilterDialog.OnFilterAppliedListener {

    private RecyclerView recyclerView;
    private RentalAdapter rentalAdapter;
    private FloatingActionButton fabAddRental;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View emptyView;

    private RentalDAO rentalDAO;
    private String currentQuery = "";
    private int currentFilter = 4; // 4: All, 0: Active, 1: Completed, 2. Overdue 3: Cancelled

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // Initialize Database Helper
//        DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(getActivity());
        rentalDAO = new RentalDAO(dbHelper);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rental_management, container, false);

        // Set up toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle(R.string.rental_management);

        // Initialize views
        initializeViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        setupFab();

        // Load rentals
        loadRentals();

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        fabAddRental = view.findViewById(R.id.fab_add_rental);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        emptyView = view.findViewById(R.id.empty_view);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        rentalAdapter = new RentalAdapter(getContext(), new ArrayList<>());
        rentalAdapter.setOnRentalClickListener(this);
        recyclerView.setAdapter(rentalAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadRentals);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.primary,
                R.color.primary_dark,
                R.color.accent
        );
    }

    private void setupFab() {
        fabAddRental.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddRentalActivity.class);
            startActivity(intent);
        });
    }

    private void loadRentals() {
        List<Rental> rentals;

        if (!currentQuery.isEmpty()) {
            // Search with query
            rentals = rentalDAO.searchRentals(currentQuery);
        } else {
            // Filter by status
            switch (currentFilter) {
                case 0: // Active
//                    rentals = rentalDAO.getActiveRentals();
//                    break;
                case 1: // Completed
//                    rentals = filterByStatus(1);
//                    break;
                case 2: // overdue
//                    rentals = filterByStatus(2);
//                    break;
                case 3: //canceled
                    rentals = rentalDAO.getRentalsByStatus(currentFilter);
                    break;
                default: // 4 All
                    rentals = rentalDAO.getAllRentals();
            }
        }

        rentalAdapter.updateRentals(rentals);
        swipeRefreshLayout.setRefreshing(false);
        updateEmptyView(rentals.isEmpty());
    }

    private List<Rental> filterByStatus(int status) {
        List<Rental> allRentals = rentalDAO.getAllRentals();
        List<Rental> filteredRentals = new ArrayList<>();
        for (Rental rental : allRentals) {
            if (rental.getStatus() == status) {
                filteredRentals.add(rental);
            }
        }
        return filteredRentals;
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
//        searchItem.setVisible(false);

//        MenuItem filterItem = menu.findItem(R.id.action_filter);
//        filterItem.setVisible(false);

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setupSearchView(SearchView searchView) {
        searchView.setQueryHint(getString(R.string.search_rental_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentQuery = query;
                loadRentals();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                loadRentals();
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
        } else if (itemId == R.id.action_refresh) {
            loadRentals();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFilterDialog() {
        FilterDialog dialog = FilterDialog.newInstance(R.string.filter_rentals,R.array.rental_options,currentFilter);
        dialog.setOnFilterAppliedListener(this);
        dialog.show(getChildFragmentManager(), "FilterDialog");
    }

    @Override
    public void onFilterApplied(int filter) {
        currentFilter = filter;
        loadRentals();
    }

    @Override
    public void onRentalClick(Rental rental) {
        Intent intent = new Intent(getContext(), AddRentalActivity.class);
        intent.putExtra("rental_id", rental.getId());
        startActivity(intent);
    }


    private void completeState(Rental rental){

        boolean success = rentalDAO.completeRental(rental.getId(), DatabaseHelper.getDateTime());
        if (success) {
            loadRentals();
            Snackbar.make(requireView(), R.string.rental_completed, Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(requireView(), R.string.error_completing_rental, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCompleteClick(Rental rental) {

        new AlertDialog.Builder(getContext())
                .setTitle(R.string.complete)
                .setMessage(R.string.operate_confirmation)
                .setPositiveButton(R.string.yes, (dialog, which) -> completeState(rental))
                .setNegativeButton(R.string.no, null)
                .show();


    }

    private void cancleState(Rental rental){

        boolean success = rentalDAO.cancelRental(rental.getId());
        if (success) {
            loadRentals();
            Snackbar.make(requireView(), R.string.rental_cancelled, Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(requireView(), R.string.error_cancelling_rental, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCancelClick(Rental rental) {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.cancel)
                .setMessage(R.string.operate_confirmation)
                .setPositiveButton(R.string.yes, (dialog, which) -> cancleState(rental))
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void overdueState(Rental rental){

        rental.setStatus(2);
        int ret = rentalDAO.updateRental(rental);
        if (ret > 0) {
            loadRentals();
            Snackbar.make(requireView(), R.string.rental_overdued, Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(requireView(), R.string.error_cancelling_rental, Snackbar.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onOverdueClick(Rental rental) {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.overdue)
                .setMessage(R.string.operate_confirmation)
                .setPositiveButton(R.string.yes, (dialog, which) -> overdueState(rental))
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRentals();
    }
}