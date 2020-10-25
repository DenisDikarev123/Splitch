package com.example.splitch.features.result;

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.example.splitch.db.MainDao;
import com.example.splitch.db.entities.Person;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ResultViewModel extends ViewModel {

    private static final String DIALOG_APPEAR_KEY = "dialog_frag";

    private Single<List<Person>> persons;

    private MainDao mainDao;

    private SavedStateHandle handle;

    private boolean isDialogOpened;

    private CompositeDisposable mDisposable = new CompositeDisposable();

    public ResultViewModel(MainDao mainDao, SavedStateHandle handle) {

      this.mainDao = mainDao;
      this.handle = handle;

      if(handle.contains(DIALOG_APPEAR_KEY)){
          isDialogOpened = handle.get(DIALOG_APPEAR_KEY);
      }
    }

    public boolean isDialogOpened() {
        return isDialogOpened;
    }

    public void setDialogOpened(boolean dialogOpened) {
        isDialogOpened = dialogOpened;
        handle.set(DIALOG_APPEAR_KEY, dialogOpened);
    }

    public Single<List<Person>> getPersons() {
        if(persons == null){
            persons = mainDao.queryAllPersonsSingle();
        }
        return persons;
    }

    public void resetAllTotals() {
        mDisposable.add(getPersons()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((personList, throwable) -> {
                    for(Person person: personList){
                        person.total = 0.00;
                    }
                    mainDao.updatePerson(personList)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe();
                }));
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        mDisposable.clear();
    }
}
