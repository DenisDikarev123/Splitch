package com.example.splitch.db.entities;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.List;

public class ProductWithPersons {

    @Embedded
    public Product product;
    @Relation(
            parentColumn = "productId",
            entityColumn = "personId",
            associateBy = @Junction(ProductPersonCrossRef.class)
    )
    public List<Person> persons;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProductWithPersons that = (ProductWithPersons) o;

        if (product != null ? !product.equals(that.product) : that.product != null) return false;
        return persons != null ? persons.equals(that.persons) : that.persons == null;
    }

    @Override
    public int hashCode() {
        int result = product != null ? product.hashCode() : 0;
        result = 31 * result + (persons != null ? persons.hashCode() : 0);
        return result;
    }
}
