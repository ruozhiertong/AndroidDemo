package com.example.myapplication.models;

public class Rental {
    private int id;
    private int vehicleId;
    private int customerId;
    private String vehicleDes;
    private String customerDes;
    private String startDate;
    private String endDate;
    private String actualReturnDate;
    private double totalAmount;
    private double deposit;
    private int status; // 0, 1,2,3. 0-进行中，1-已完成，2-逾期，3-取消
    private String createTime;
    private String updateTime;
    private int createdBy;
    private String remarks;

    public String getVehicleDes() {
        return vehicleDes;
    }

    public void setVehicleDes(String vehicleDes) {
        this.vehicleDes = vehicleDes;
    }

    public String getCustomerDes() {
        return customerDes;
    }

    public void setCustomerDes(String customerDes) {
        this.customerDes = customerDes;
    }

    // Constructor
    public Rental() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getActualReturnDate() {
        return actualReturnDate;
    }

    public void setActualReturnDate(String actualReturnDate) {
        this.actualReturnDate = actualReturnDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getDeposit() {
        return deposit;
    }

    public void setDeposit(double deposit) {
        this.deposit = deposit;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
