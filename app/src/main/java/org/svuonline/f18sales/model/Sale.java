package org.svuonline.f18sales.model;

import android.content.ContentValues;

import org.svuonline.f18sales.data.DatabaseHelper;

public class Sale {
    private int Id;
    private int SalesmanId;
    private int RegionId;
    private String SaleDate;
    private int Amount;
    private String RegionName;

    public Sale(int salesmanId,int regionId, String saleDate,int amount) {
        this.SalesmanId = salesmanId;
        this.RegionId = regionId;
        this.SaleDate = saleDate;
        this.Amount = amount;
    }
    public Sale(String regionName,int amount) {
        this.RegionName = regionName;
        this.Amount = amount;
    }

    public int getId() {
        return Id;
    }
    public int getSalesmanId() {
        return SalesmanId;
    }
    public int getRegionIdId() {
        return RegionId;
    }
    public String getSaleDate() {
        return SaleDate;
    }
    public int getAmount() {
        return Amount;
    }
    public String getRegionName() {
        return RegionName;
    }

    public ContentValues ToContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.SalesEntry.SALESMAN_ID, SalesmanId);
        contentValues.put(DatabaseHelper.SalesEntry.REGION_ID, RegionId);
        contentValues.put(DatabaseHelper.SalesEntry.SALE_DATE, SaleDate);
        contentValues.put(DatabaseHelper.SalesEntry.AMOUNT, Amount);
        return contentValues;
    }
}
