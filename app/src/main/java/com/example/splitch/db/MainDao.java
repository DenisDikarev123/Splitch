package com.example.splitch.db;

import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.splitch.db.entities.Person;
import com.example.splitch.db.entities.Product;
import com.example.splitch.db.entities.ProductPersonCrossRef;
import com.example.splitch.db.entities.ProductWithPersons;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;

@Dao
public interface MainDao {
    @Query("SELECT * FROM products")
    DataSource.Factory<Integer, Product> queryAllProducts();

    @Query("SELECT * FROM products WHERE productId = (:productId)")
    Flowable<Product> queryProduct(String productId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertProduct(Product product);

    @Update
    Completable updateProduct(Product product);

    @Delete
    Completable deleteProduct(Product product);

    @Query("SELECT * FROM persons")
    Flowable<List<Person>> queryAllPersons();

    @Query("SELECT * FROM persons")
    Single<List<Person>> queryAllPersonsSingle();

    @Query("SELECT * FROM persons")
    DataSource.Factory<Integer, Person> queryAllPersonsPaging();

    @Query("SELECT * FROM persons WHERE personId = (:personId)")
    Flowable<Person> queryPersonById(String personId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Maybe<List<Long>> insertPersons(List<Person> personList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertPerson(Person person);

    @Update
    Completable updatePerson(Person person);

    @Update
    Completable updatePerson(List<Person> personList);

    @Delete
    Completable deletePerson(Person person);

    @Transaction
    @Query("SELECT * FROM products")
    Single<List<ProductWithPersons>> getProductWithPersons();

    @Query("SELECT * FROM crossRefs")
    Flowable<List<ProductPersonCrossRef>> queryAllJunctions();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable addJunction(ProductPersonCrossRef crossRef);

    @Delete
    Completable deleteJunction(ProductPersonCrossRef crossRef);

    @Query("DELETE FROM crossRefs WHERE productId = (:productId)")
    Completable deleteAllRefWithProduct(String productId);

    @Query("DELETE FROM crossRefs WHERE personId = (:personId)")
    Completable deleteAllRefWithPerson(String personId);
}
