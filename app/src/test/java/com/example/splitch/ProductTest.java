package com.example.splitch;

import com.example.splitch.db.entities.Product;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ProductTest {

    private Product product1 = new Product("test", 12.00);
    private Product product2 = new Product(product1.productId, "test", 12.0);

    @Before
    public void initSecondProduct(){

    }

    @Test
    public void twoStringNotNullAndEquals_AreEquals(){
        assertEquals(product1, product2);
        assertEquals(product2, product1);
    }

    @Test
    public void twoStringNotNullAndNotEquals_AreNotEqual(){
        product2.name = "test23";
        assertNotEquals(product1, product2);
        assertNotEquals(product2, product1);
    }

    @Test
    public void theFirstStringIsNull_AreNotEqual(){
        product1.name = null;
        assertNotEquals(product1, product2);
        assertNotEquals(product2, product1);
    }

    @Test
    public void theSecondStringIsNull_AreNotEqual(){
        product1.name = "test";
        product2.name = null;
        assertNotEquals(product1, product2);
        assertNotEquals(product2, product1);
    }

    @Test
    public void bothStringsAreNull_AreEqual(){
        product1.name = null;
        product2.name = null;
        assertEquals(product1, product2);
        assertEquals(product2, product1);
    }

}
