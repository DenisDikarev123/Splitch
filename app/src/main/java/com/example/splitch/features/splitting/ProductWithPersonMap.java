package com.example.splitch.features.splitting;

import com.example.splitch.db.entities.Person;
import com.example.splitch.db.entities.Product;

import java.util.HashMap;

public class ProductWithPersonMap {

    private Product product;
    private HashMap<Person, Boolean> personStatusMap;
    private boolean showError;

    public ProductWithPersonMap(Product product, HashMap<Person, Boolean> personStatusMap, boolean showError) {
        this.product = product;
        this.personStatusMap = personStatusMap;
        this.showError = showError;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public HashMap<Person, Boolean> getPersonStatusMap() {
        return personStatusMap;
    }

    public void setPersonStatusMap(HashMap<Person, Boolean> personStatusMap) {
        this.personStatusMap = personStatusMap;
    }

    public boolean isShowError() {
        return showError;
    }

    public void setShowError(boolean showError) {
        this.showError = showError;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProductWithPersonMap that = (ProductWithPersonMap) o;

        if (showError != that.showError) return false;
        if (product != null ? !product.equals(that.product) : that.product != null) return false;
        return personStatusMap != null ? personStatusMap.equals(that.personStatusMap) : that.personStatusMap == null;
    }

    @Override
    public int hashCode() {
        int result = product != null ? product.hashCode() : 0;
        result = 31 * result + (personStatusMap != null ? personStatusMap.hashCode() : 0);
        result = 31 * result + (showError ? 1 : 0);
        return result;
    }
}
