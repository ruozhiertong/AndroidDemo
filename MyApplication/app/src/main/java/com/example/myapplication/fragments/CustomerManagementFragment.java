package com.example.myapplication.fragments;

import android.content.Context;
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

import com.example.myapplication.R;
//import com.example.myapplication.activities.AddCustomerActivity;
//import com.example.myapplication.activities.CustomerDetailActivity;
//import com.example.myapplication.activities.AddCustomerActivity;
import com.example.myapplication.activities.AddCustomerActivity;
import com.example.myapplication.adapters.CustomerAdapter;
import com.example.myapplication.dao.CustomerDAO;
import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.models.Customer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class CustomerManagementFragment extends Fragment implements CustomerAdapter.OnCustomerClickListener {

    private RecyclerView recyclerView;
    private CustomerAdapter customerAdapter;
    private FloatingActionButton fabAddCustomer;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View emptyView;
    private CustomerDAO customerDAO;
    private String currentQuery = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        System.out.println("CustomerManagementFragment onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // Initialize Database Helper
//        DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(getActivity());
        customerDAO = new CustomerDAO(dbHelper);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        System.out.println("CustomerManagementFragment onCreateView");

        View view = inflater.inflate(R.layout.fragment_customer_management, container, false);

        // Set up toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle(R.string.customer_management);

        // Initialize views
        initializeViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        setupFab();

        // Load customers
        loadCustomers();

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        fabAddCustomer = view.findViewById(R.id.fab_add_customer);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        emptyView = view.findViewById(R.id.empty_view);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        customerAdapter = new CustomerAdapter(getContext(), new ArrayList<>());
        customerAdapter.setOnCustomerClickListener(this);
        recyclerView.setAdapter(customerAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadCustomers);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.primary,
                R.color.primary_dark,
                R.color.accent
        );
    }

    private void setupFab() {
        fabAddCustomer.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddCustomerActivity.class);
            startActivity(intent);
        });
    }

    private void loadCustomers() {
        List<Customer> customers;
        if (!currentQuery.isEmpty()) {
            customers = customerDAO.searchCustomers(currentQuery);
        } else {
            customers = customerDAO.getAllCustomers();
        }

        customerAdapter.updateCustomers(customers);
        swipeRefreshLayout.setRefreshing(false);
        updateEmptyView(customers.isEmpty());
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

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        setupSearchView(searchView);

        MenuItem filterItem = menu.findItem(R.id.action_filter);
        filterItem.setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }


    private void setupSearchView(SearchView searchView) {
        searchView.setQueryHint(getString(R.string.search_customer_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentQuery = query;
                loadCustomers();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                loadCustomers();
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            loadCustomers();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCustomerClick(Customer customer) {
        Intent intent = new Intent(getContext(), AddCustomerActivity.class);
        intent.putExtra("customer_id", customer.getId());
        startActivity(intent);
    }

    @Override
    public void onRentalHistoryClick(Customer customer) {
        // TODO: Implement rental history view
        Snackbar.make(requireView(), R.string.rental_history_coming_soon, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onBlacklistClick(Customer customer) {
        int ret;
        if (customer.getStatus() == 0) {
            // Remove from blacklist
            ret = customerDAO.updateCustomer(customer);
            if (ret > 0) {
                Snackbar.make(requireView(), R.string.customer_removed_from_blacklist,
                        Snackbar.LENGTH_LONG).show();
            }
        } else {
            // Add to blacklist
            ret = customerDAO.addToBlacklist(customer.getId());
            if (ret > 0) {
                Snackbar.make(requireView(), R.string.customer_added_to_blacklist,
                        Snackbar.LENGTH_LONG).show();
            }
        }

        if (ret == 0) {
            Toast.makeText(getContext(), R.string.error_updating_customer,
                    Toast.LENGTH_SHORT).show();
        }
        loadCustomers();
    }

    @Override
    public void onResume() {
        System.out.println("CustomerManagementFragment onResume");
        super.onResume();
        loadCustomers();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        System.out.println("CustomerManagementFragment onViewCreated");
        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    public void onStart() {
        System.out.println("CustomerManagementFragment onStart");

        super.onStart();
    }


    @Override
    public void onPause() {
        System.out.println("CustomerManagementFragment onPause");

        super.onPause();
    }

    @Override
    public void onStop() {
        System.out.println("CustomerManagementFragment onStop");

        super.onStop();
    }

    @Override
    public void onDestroyView() {
        System.out.println("CustomerManagementFragment onDestroyView");

        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        System.out.println("CustomerManagementFragment onDestroy");

        super.onDestroy();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        System.out.println("CustomerManagementFragment onAttach");

        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        System.out.println("CustomerManagementFragment onDetach");

        super.onDetach();
    }
}