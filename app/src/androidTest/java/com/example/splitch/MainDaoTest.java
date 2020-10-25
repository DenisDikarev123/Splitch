package com.example.splitch;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.paging.RxPagedListBuilder;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.splitch.db.MainDao;
import com.example.splitch.db.MainDatabase;
import com.example.splitch.db.entities.Person;
import com.example.splitch.db.entities.Product;
import com.example.splitch.db.entities.ProductPersonCrossRef;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@RunWith(AndroidJUnit4.class)
public class MainDaoTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private static final Product PRODUCT = new Product("сухарик", 24.00);
    private static final Product PRODUCT1 = new Product("сухарик2", 22.00);
    private static final Product PRODUCT2 = new Product("сухарик3", 24.55);
    private static final Person PERSON = new Person("test_id","Valerii", 2, R.color.material_indigo);
    private static final ProductPersonCrossRef CROSS_REF = new ProductPersonCrossRef(PRODUCT.productId, PERSON.personId);

    private MainDao mainDao;
    private MainDatabase mainDatabase;

    @Before
    public void createDb(){
        Context context = ApplicationProvider.getApplicationContext();
        mainDatabase = Room.inMemoryDatabaseBuilder(context, MainDatabase.class)
                //only for testing
                .allowMainThreadQueries()
                .build();
        mainDao = mainDatabase.mainDao();
    }

    @After
    public void closeDb() throws IOException {
        mainDatabase.close();
    }

    @Test
    public void insertUpdateDeleteProduct(){
        mainDao.insertProduct(PRODUCT).blockingAwait();

        mainDao.queryProduct(PRODUCT.productId)
                .test()
                .assertValue(PRODUCT);

        PRODUCT.name = "updatedProductName";

        mainDao.updateProduct(PRODUCT).blockingAwait();

        mainDao.queryProduct(PRODUCT.productId)
                .test()
                .assertValue(product -> product.name.equals("updatedProductName"));

        mainDao.deleteProduct(PRODUCT).blockingAwait();

        mainDao.queryProduct(PRODUCT.productId)
                .test()
                .assertNoValues();
    }

    @Test
    public void insertMultipleProducts(){
        mainDao.insertProduct(PRODUCT).blockingAwait();
        mainDao.insertProduct(PRODUCT1).blockingAwait();
        mainDao.insertProduct(PRODUCT2).blockingAwait();

        new RxPagedListBuilder<>(mainDao.queryAllProducts(), 3)
                .buildObservable()
                .test()
                .assertValue(products -> products.size() == 3);
    }

    @Test
    public void insertUpdateDeletePerson(){

        mainDao.insertPerson(PERSON).blockingAwait();
        mainDao.insertPerson(PERSON).blockingAwait();

        mainDao.queryPersonById(PERSON.personId)
                .test()
                .assertValue(PERSON);

        PERSON.name = "updatedName";

        mainDao.updatePerson(PERSON).blockingAwait();

        mainDao.queryPersonById(PERSON.personId)
                .test()
                .assertValue(PERSON);

        mainDao.deletePerson(PERSON).blockingAwait();

        mainDao.queryPersonById(PERSON.personId)
                .test()
                .assertNoValues();
    }

    @Test
    public void insertThreePerson(){
        Person person1 = new Person("test1", 0, R.color.material_indigo);
        Person person2 = new Person("test2", 0, R.color.material_pink);
        Person person3 = new Person("test3",0, R.color.material_red);
        List<Person> personList = new ArrayList<>();
        personList.add(person1);
        personList.add(person2);
        personList.add(person3);

        mainDao.insertPersons(personList).blockingGet();

        mainDao.queryAllPersonsSingle()
                .test()
                .assertValue(personList);

    }

    @Test
    public void deleteAddCrossRefs(){
        mainDao.addJunction(CROSS_REF).blockingAwait();

        mainDao.queryAllJunctions()
                .test()
                .assertValue(productPersonCrossRefs -> productPersonCrossRefs.get(0).personId.equals(CROSS_REF.personId)
                        && productPersonCrossRefs.get(0).productId.equals(CROSS_REF.productId));

        mainDao.deleteJunction(CROSS_REF).blockingAwait();

        mainDao.queryAllJunctions()
                .test()
                .assertValue(productPersonCrossRefs -> productPersonCrossRefs.size() == 0);
    }

    @Test
    public void deleteAllRefsWithPersonAndWithProduct(){
        ProductPersonCrossRef ref1 = new ProductPersonCrossRef("product1", "person1");
        ProductPersonCrossRef ref2 = new ProductPersonCrossRef("product2", "person2");
        ProductPersonCrossRef ref3 = new ProductPersonCrossRef("product3", "person3");
        ProductPersonCrossRef ref4 = new ProductPersonCrossRef("product1", "person2");

        mainDao.addJunction(ref1).blockingAwait();
        mainDao.addJunction(ref2).blockingAwait();
        mainDao.addJunction(ref3).blockingAwait();
        mainDao.addJunction(ref4).blockingAwait();

        mainDao.queryAllJunctions()
                .test()
                .assertValue(productPersonCrossRefs -> productPersonCrossRefs.size() == 4);

        mainDao.deleteAllRefWithPerson("person2").blockingAwait();

        mainDao.queryAllJunctions()
                .test()
                .assertValue(productPersonCrossRefs -> productPersonCrossRefs.size() == 2);

        mainDao.deleteAllRefWithProduct("product1").blockingAwait();

        mainDao.queryAllJunctions()
                .test()
                .assertValue(productPersonCrossRefs ->
                        productPersonCrossRefs.get(0).productId.equals(ref3.productId));
    }

    @Test
    public void clearAllTables(){
        mainDao.insertPerson(PERSON).blockingAwait();
        mainDao.insertProduct(PRODUCT).blockingAwait();

        mainDao.queryAllPersons()
                .test()
                .assertValue(personList -> personList.size() == 1);

        mainDatabase.clearAllTables();

        mainDao.queryAllPersons()
                .test()
                .assertValue(List::isEmpty);
    }

    @Test
    public void updatePersonTotals(){
        Person person1 = new Person("test1", 2.22, R.color.material_indigo);
        Person person2 = new Person("test2", 3.33, R.color.material_pink);
        Person person3 = new Person("test3",4.44, R.color.material_red);
        List<Person> personList = new ArrayList<>();
        personList.add(person1);
        personList.add(person2);
        personList.add(person3);

        mainDao.insertPersons(personList).blockingGet();

        mainDao.queryAllPersons()
                .test()
                .assertValue(personList1 -> personList1.get(0).equals(person1))
                .assertValue(personList1 -> personList1.get(1).equals(person2))
                .assertValue(personList1 -> personList1.get(2).equals(person3));

        personList.get(0).total = 0.00;
        personList.get(1).total = 0.00;
        personList.get(2).total = 0.00;

        mainDao.updatePerson(personList).blockingAwait();

        mainDao.queryAllPersons()
                .test()
                .assertValue(personList1 -> personList1.get(0).total == 0.00)
                .assertValue(personList1 -> personList1.get(1).total == 0.00)
                .assertValue(personList1 -> personList1.get(2).total == 0.00);
    }

    @Test
    public void InsertQueryProductWithPersons(){
        ProductPersonCrossRef ref1 = new ProductPersonCrossRef(PRODUCT.productId, PERSON.personId);
        mainDao.insertProduct(PRODUCT).blockingAwait();
        mainDao.insertPerson(PERSON).blockingAwait();
        mainDao.addJunction(ref1);

        mainDao.getProductWithPersons()
                .test()
                .assertValue(persons -> persons.size() == 1)
                .assertValue(persons -> persons.get(0).product.productId.equals(PRODUCT.productId));
    }

}
