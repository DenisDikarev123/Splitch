package com.example.splitch.features.receipt;

import android.text.TextUtils;
import android.util.Log;

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;
import androidx.paging.RxPagedListBuilder;

import com.example.splitch.db.MainDao;
import com.example.splitch.db.entities.Product;

import java.math.BigDecimal;
import java.math.RoundingMode;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class AddingReceiptViewModel extends ViewModel {

    private static final String TAG = AddingReceiptViewModel.class.getSimpleName();

    private static final String ERROR_MAX_NUMBER = "error_max_number";
    private static final String ERROR_EMPTY_PRICE = "error_empty_price";
    private static final String ERROR_EMPTY_NAME = "error_empty_name";

    private static final String RECEIPT_ANIMATION = "receipt_animation";

    private static final String CURRENT_PRODUCT = "current_product";

    private static final String MODE_KEY = "bottom_sheet_mode";

    private SavedStateHandle handle;

    private float animationProgress = 0f;

    private CurrentProduct product;

    private Observable<PagedList<Product>> productList;

    private boolean isAdding = true;

    private Subject<Boolean> subjectNewProductSnackbar = PublishSubject.create();

    private Subject<Boolean> subjectUpdateProductSnackbar = PublishSubject.create();

    private MainDao mainDao;

    public AddingReceiptViewModel(MainDao mainDao, SavedStateHandle handle) {
        this.mainDao = mainDao;
        this.handle = handle;

        if(handle.contains(RECEIPT_ANIMATION)){
            animationProgress = handle.get(RECEIPT_ANIMATION);
            Log.i(TAG, "getting animation flag from saved state " + animationProgress);
        }
        if(handle.contains(CURRENT_PRODUCT)){
            product = handle.get(CURRENT_PRODUCT);
        }
        if(handle.contains(MODE_KEY)){
            isAdding = handle.get(MODE_KEY);
        }
    }

    public float getAnimationProgress() {
        return animationProgress;
    }

    public void setAnimationProgress(float animationProgress) {
        this.animationProgress = animationProgress;
        handle.set(RECEIPT_ANIMATION, animationProgress);
    }

    public void setProduct(Product product) {
        if(product != null) {
            String productPrice = String.valueOf(product.price);
            this.product = new CurrentProduct(product.productId, product.name, productPrice);
            isAdding = false;
            Log.i(TAG, productPrice);
            handle.set(CURRENT_PRODUCT, this.product);
            handle.set(MODE_KEY, isAdding);
        } else {
           this.product = null;
           isAdding = true;
           handle.set(CURRENT_PRODUCT, null);
           handle.set(MODE_KEY, isAdding);
        }
    }

    public CurrentProduct getProduct(){
        return product;
    }

    public Subject<Boolean> getSubjectNewProductSnackbar() {
        return subjectNewProductSnackbar;
    }

    public void setSubjectNewProductSnackbar(boolean newProductSnackbar) {
        subjectNewProductSnackbar.onNext(newProductSnackbar);
    }

    public Subject<Boolean> getSubjectUpdateProductSnackbar() {
        return subjectUpdateProductSnackbar;
    }

    public void setSubjectUpdateProductSnackbar(boolean updateProductSnackbar) {
        subjectUpdateProductSnackbar.onNext(updateProductSnackbar);
    }

    public boolean isAdding() {
        return isAdding;
    }

    Observable<CurrentProduct> getObservableProduct() {
        return Observable.create(emitter -> emitter.onNext(product));
    }

    Observable<PagedList<Product>> getAllProducts(){
        if(productList == null){
            productList = new RxPagedListBuilder<>(mainDao.queryAllProducts(), 10)
                    .buildObservable();
        }
        return productList;
    }

    public Completable addNewItem(String name, String price) {
        double priceDoubleFormat = parseToDouble(price);
        Product newProduct = new Product(name, priceDoubleFormat);
        Log.i(TAG, name + price);
        return mainDao.insertProduct(newProduct);
    }

    public Completable updateItem(String name, String price){
        double priceDouble = parseToDouble(price);
        Log.i(TAG, "updating item with id" + product.productId);
        Product updatedProduct = new Product(product.productId, name, priceDouble);
        return mainDao.updateProduct(updatedProduct);
    }


    Completable deleteItem(Product product){
        mainDao.deleteAllRefWithProduct(product.productId);
        return mainDao.deleteProduct(product);
    }

    public String verifyName(String name){
        if(TextUtils.isEmpty(name)){
            return ERROR_EMPTY_NAME;
        }
        return name;
    }

    public String verifyPrice(String price){
        //price validation
        // [1-7][.][0-2]
        // max value 9999999.99

        //check for empty string
        if(TextUtils.isEmpty(price)){
            return ERROR_EMPTY_PRICE;
        }
        //check for only . string.
        if(price.equals(".")) {
            return "00" + price + "00";
        }

        if(price.contains(".")){
            int dotPosition = price.indexOf(".");
            if (dotPosition > 7) {
                //if string has dot and more that 7 chars = error.
                return ERROR_MAX_NUMBER;
            }
        } else {
            if (price.length() > 7) {
                //if string has more that 7 chars - error
                return ERROR_MAX_NUMBER;
            }
        }
        return price;
    }

    public double parseToDouble(String number){
        Log.i(TAG, "before parsing = " + number);
        BigDecimal bigDecimal = new BigDecimal(number)
                                    .setScale(2, RoundingMode.CEILING);
        Log.i(TAG, "parsed number = " + bigDecimal.doubleValue());
        return bigDecimal.doubleValue();
    }

    public void updateProductName(String name){
        if(product != null) {
            product.name = name;
        } else {
            product = new CurrentProduct(name, "");
        }
        handle.set(CURRENT_PRODUCT, product);
    }

    public void updateProductPrice(String price){
        if(product != null){
            product.price = price;
        } else {
            product = new CurrentProduct("", price);
        }
        handle.set(CURRENT_PRODUCT, product);
    }
}
