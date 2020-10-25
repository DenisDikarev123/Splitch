package com.example.splitch.db.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(primaryKeys = {"productId", "personId"}, tableName = "crossRefs")
public class ProductPersonCrossRef {

    @NonNull
    public String productId;
    @NonNull
    public String personId;

    public ProductPersonCrossRef(@NonNull String productId, @NonNull String personId) {
        this.productId = productId;
        this.personId = personId;
    }
}
