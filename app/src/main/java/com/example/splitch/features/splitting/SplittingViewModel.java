package com.example.splitch.features.splitting;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.example.splitch.db.MainDao;
import com.example.splitch.db.entities.Person;
import com.example.splitch.db.entities.Product;
import com.example.splitch.db.entities.ProductPersonCrossRef;
import com.example.splitch.db.entities.ProductWithPersons;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

public class SplittingViewModel extends ViewModel {

    private static final String TAG = SplittingViewModel.class.getSimpleName();

    private MainDao mainDao;

    private List<ProductWithPersons> productWithPersonsList;

    private List<Person> updatedPersonList;

    //save and restore this list to show error states
    private List<ProductWithPersonMap> productWithPersonMapList;

    public SplittingViewModel(MainDao mainDao) {
        this.mainDao = mainDao;

        Log.i(TAG, "SplittingViewModel constructor");
    }

    public List<ProductWithPersons> getProductWithPersonsList() {
        return productWithPersonsList;
    }

    public List<ProductWithPersonMap> getProductWithPersonMapList(){
        return productWithPersonMapList;
    }

    private Single<List<ProductWithPersons>> getProductWithPersonsFromBd() {
        return mainDao.getProductWithPersons()
                .map(persons -> {
                    productWithPersonsList = persons;
                    Log.i(TAG, "getting and saving product with person list with size " + productWithPersonsList.size());
                    return persons;
                });
    }

    private Single<List<Person>> getPersonsList() {
        return mainDao.queryAllPersonsSingle()
                    .map(personList -> {
                        updatedPersonList = personList;
                        Log.i(TAG, "getting and saving persons list with size " + updatedPersonList.size());
                        return personList;
                    });
    }

    public Single<List<ProductWithPersonMap>> getProductWithPersonMapListFromDb(){
        return getPersonsList()
                .zipWith(getProductWithPersonsFromBd(), (personList, persons) -> {
                    List<ProductWithPersonMap> productWithPersonMaps = new ArrayList<>();
                    for (ProductWithPersons productWithPersons : persons) {
                        Product product = productWithPersons.product;
                        List<Person> productPersonList = productWithPersons.persons;
                        HashMap<Person, Boolean> personStatusMap = new HashMap<>();
                        //setting correct person status
                        for (Person person : personList) {
                            if (productPersonList.contains(person)) {
                                personStatusMap.put(person, true);
                            } else {
                                personStatusMap.put(person, false);
                            }
                        }
                        ProductWithPersonMap productWithPersonMap =
                                new ProductWithPersonMap(product, personStatusMap, false);
                        productWithPersonMaps.add(productWithPersonMap);
                    }
                    if (productWithPersonMapList == null)
                        Log.i(TAG, "productWithPersonMapList is null");
                    productWithPersonMapList = productWithPersonMaps;
                    return productWithPersonMaps;
                });
    }

    public Completable addJunction(ProductWithPersonMap productWithPersonMap, Person person){
        int pos = productWithPersonMapList.indexOf(productWithPersonMap);
        ProductWithPersonMap updatedProduct = productWithPersonMapList.get(pos);
        updatedProduct.getPersonStatusMap().remove(person);
        updatedProduct.getPersonStatusMap().put(person, true);
        ProductPersonCrossRef crossRef =
                new ProductPersonCrossRef(productWithPersonMap.getProduct().productId, person.personId);
        return mainDao.addJunction(crossRef);
    }

    public Completable deleteJunction(ProductWithPersonMap productWithPersonMap, Person person){
        int pos = productWithPersonMapList.indexOf(productWithPersonMap);
        ProductWithPersonMap updatedProduct = productWithPersonMapList.get(pos);
        updatedProduct.getPersonStatusMap().remove(person);
        updatedProduct.getPersonStatusMap().put(person, false);
        ProductPersonCrossRef crossRef =
                new ProductPersonCrossRef(productWithPersonMap.getProduct().productId, person.personId);
        return mainDao.deleteJunction(crossRef);
    }

    public Completable calculateTotalPrice() {
        //reset all totals to not doubling previous value.
        for(Person person: updatedPersonList){
            person.total = 0;
            Log.i(TAG, "person with name " + person.name +  "now has total " + person.total);
        }
        Log.i(TAG, "adding products with size " + productWithPersonsList.size());
        for(ProductWithPersonMap productWithPersonMap: productWithPersonMapList){
            List<Person> productWithActivePersons = new ArrayList<>();
            //counting how many players gonna pay for this product
            for (HashMap.Entry entry : productWithPersonMap.getPersonStatusMap().entrySet()) {
                Boolean value = (Boolean) entry.getValue();
                if(value) {
                    Person person = (Person) entry.getKey();
                    productWithActivePersons.add(person);
                }
            }
            //if there is only one payer, he pays all price
            if(productWithActivePersons.size() == 1){
                //take person which gonna pay full price
                Person person = productWithActivePersons.get(0);
                //Since i have two different lists, i have different person examples in them.
                //therefore i can't index of person. I should find it by id or name.
                //NOTE: indexOf find the same examples of class.
                Log.i(TAG, updatedPersonList.toString());
                Log.i(TAG, person.toString());
                //finding this person in common list
                int position = getPosition(person);
                Person updatedPerson = updatedPersonList.get(position);
                //rounding total
                BigDecimal oldBalance = new BigDecimal(updatedPerson.total)
                        .setScale(2, BigDecimal.ROUND_HALF_UP);
                //rounding added price
                BigDecimal productPrice = new BigDecimal(productWithPersonMap.getProduct().price)
                        .setScale(2, BigDecimal.ROUND_HALF_UP);
                //adding product price to person total
                BigDecimal newBalance = oldBalance.add(productPrice);
                //updating person's total
                updatedPerson.total = newBalance.doubleValue();
                Log.i(TAG, "adding price: " + productWithPersonMap.getProduct().price + " total now: " + updatedPerson.total);
            } else {
                //if there is more than one payer, the price is divided between them.
                Log.i(TAG, "adding price for " + productWithActivePersons.size() + " persons");
                addProductPriceToPerson(productWithActivePersons, productWithPersonMap.getProduct().price);
            }
        }

        return mainDao.updatePerson(updatedPersonList);
    }

    private void addProductPriceToPerson(List<Person> personList, double price) {
        //counting price for each person
        double priceForEachPerson = price / personList.size();
        //rounding price for each person
        BigDecimal roundedPrice = new BigDecimal(priceForEachPerson)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
        Log.i(TAG, "rounded price = " + roundedPrice.doubleValue());
        //counting overpayment
        double overpayment = price - roundedPrice.doubleValue() * personList.size();
        //rounding overpayment
        BigDecimal roundedOverpayment = new BigDecimal(overpayment)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
        Log.i(TAG, "price " + price + " - " + roundedPrice.doubleValue() + " * " + personList.size() + " = " + roundedOverpayment);
        //adding price for each person in list
        for(int i = 0; i < personList.size(); i++) {
            //getting person
            Person person = personList.get(i);
            //finding person in common person list
            int position = getPosition(person);
            Person updatedPerson = updatedPersonList.get(position);
            Log.i(TAG, "person.total " +  updatedPerson.total);
            //adding product price to person total
            updatedPerson.total = updatedPerson.total + roundedPrice.doubleValue();
            Log.i(TAG,  "roundedPrice" + roundedPrice.doubleValue() + " = " +  updatedPerson.total);
            //check whether person is last in list. Last person pays overpayment.
            if(i == (personList.size() - 1)){
                //rounding old person total
                BigDecimal oldBalance = new BigDecimal(updatedPerson.total)
                        .setScale(2, BigDecimal.ROUND_HALF_UP);
                //adding overpayment to old total
                BigDecimal newBalance = oldBalance.add(roundedOverpayment);
                //updating person total
                updatedPerson.total = newBalance.doubleValue();
                Log.i(TAG,  "adding overpayment " + roundedOverpayment.doubleValue() + " and total becoming " +  updatedPerson.total);
            }
        }
    }

    private int getPosition(Person person){
        int pos = -1;
        for(int j = 0; j < updatedPersonList.size(); j++){
            if(updatedPersonList.get(j).personId.equals(person.personId)){
                pos = j;
                break;
            }
        }
        return pos;
    }
}
