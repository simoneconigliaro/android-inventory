package com.project.simoneconigliaro.inventorymanager;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface ProductDao {

    @Insert
    void insert(Product product);

    @Update
    void update(Product product);

    @Delete
    void delete(Product product);

    @Query("DELETE FROM product_table")
    void deleteAllProducts();

    @Query("SELECT * FROM product_table ORDER BY id ASC")
    LiveData<List<Product>> getAllProducts();

    @Query("SELECT * FROM PRODUCT_TABLE WHERE id = :id ")
    LiveData<Product> getProductById(int id);
}
