package com.example.splitch.db.entities;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.List;

public class PersonWithProducts {
    @Embedded
    public Person person;
    @Relation(
            parentColumn = "personId",
            entityColumn = "productId",
            associateBy = @Junction(ProductPersonCrossRef.class)
    )
    public List<Product> products;
}
