package com.example.splitch.features.adding_persons;

import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;
import androidx.paging.RxPagedListBuilder;

import com.example.splitch.R;
import com.example.splitch.db.MainDao;
import com.example.splitch.db.entities.Person;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class AddingPersonsViewModel extends ViewModel {

    private static final String TAG = AddingPersonsViewModel.class.getSimpleName();

    private static final String DIALOG_FLAG_KEY = "is_dialog_open";

    private static final String INDEX_COLOR_KEY = "last_index_color";

    private static final String PERSON_ANIMATION = "person_animation";

    private static final String PERSON_KEY = "current_person";

    private static final String MODE_KEY = "person_mode";

    private static final String CONTACTS_KEY = "contacts_info";

    private MainDao mainDao;

    private SavedStateHandle handle;

    private Cursor cursor;

    private ContactsArray contacts;

    private boolean isDialogOpen = false;

    private Observable<PagedList<Person>> personList;

    private Person person;

    private boolean isAdding = true;

    private float animationProgress = 0f;

    private Subject<Boolean> subjectNewPersonSnackbar = PublishSubject.create();

    private Subject<Boolean> subjectUpdatePersonSnackbar = PublishSubject.create();

    private int[] arrayColors = new int[]{
            R.color.material_indigo,
            R.color.material_light_blue,
            R.color.material_pink,
            R.color.material_red,
            R.color.material_purple
    };

    private int lastIndexColor = 0;

    public AddingPersonsViewModel(MainDao mainDao, SavedStateHandle handle) {
        this.mainDao = mainDao;
        this.handle = handle;

        if(handle.contains(DIALOG_FLAG_KEY)){
            isDialogOpen = handle.get(DIALOG_FLAG_KEY);
        }

        if(handle.contains(INDEX_COLOR_KEY)){
            lastIndexColor = handle.get(INDEX_COLOR_KEY);
        }

        if(handle.contains(PERSON_ANIMATION)){
            animationProgress = handle.get(PERSON_ANIMATION);
        }

        if(handle.contains(PERSON_KEY)){
            person = handle.get(PERSON_KEY);
        }

        if(handle.contains(MODE_KEY)){
            isAdding = handle.get(MODE_KEY);
        }

        if(handle.contains(CONTACTS_KEY)){
            contacts = handle.get(CONTACTS_KEY);
        }
        Log.i(TAG, "view model constructor");
    }

    public float getAnimationProgress() {
        return animationProgress;
    }

    public void setAnimationProgress(float animationProgress) {
        this.animationProgress = animationProgress;
        handle.set(PERSON_ANIMATION, animationProgress);
    }

    public boolean isDialogOpen() {
        return isDialogOpen;
    }

    public void setDialogOpen(boolean dialogOpen) {
        isDialogOpen = dialogOpen;
        handle.set(DIALOG_FLAG_KEY, isDialogOpen);
    }

    public ContactsArray getContacts() {
        return contacts;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        if(person != null) {
            //you should use here new operator because otherwise u gonna change primary person object.
            this.person = new Person(person.personId, person.name, person.total, person.colorId);
            Log.i(TAG, "setting person with name " + person.name);
            isAdding = false;
            handle.set(PERSON_KEY, this.person);
        } else {
            this.person = null;
            isAdding = true;
            handle.set(PERSON_KEY, null);
        }
        handle.set(MODE_KEY, isAdding);
    }

    public boolean isAdding() {
        return isAdding;
    }

    Observable<Person> getObservablePerson() {
        return Observable.create(emitter -> emitter.onNext(person));
    }

    public Subject<Boolean> getSubjectNewPersonSnackbar() {
        return subjectNewPersonSnackbar;
    }

    public void setSubjectNewPersonSnackbar(Boolean newPersonSnackbar) {
        subjectNewPersonSnackbar.onNext(newPersonSnackbar);
    }

    public Subject<Boolean> getSubjectUpdatePersonSnackbar() {
        return subjectUpdatePersonSnackbar;
    }

    public void setSubjectUpdatePersonSnackbar(Boolean updatePersonSnackbar) {
        subjectUpdatePersonSnackbar.onNext(updatePersonSnackbar);
    }

    public Observable<PagedList<Person>> getAllPersons(){
        if(personList == null){
            personList = new RxPagedListBuilder<>(mainDao.queryAllPersonsPaging(), 10)
                    .buildObservable();
        }
        return personList;
    }

    public void setLastPersonColor(int colorId){
        switch (colorId){
            case R.color.material_indigo:
                lastIndexColor = 0;
                break;
            case R.color.material_light_blue:
                lastIndexColor = 1;
                break;
            case R.color.material_pink:
                lastIndexColor = 2;
                break;
            case R.color.material_red:
                lastIndexColor = 3;
                break;
            case R.color.material_purple:
                lastIndexColor = 4;
                break;
            default:
                throw new IllegalArgumentException("unknown colorId was passed.");
        }

        if(lastIndexColor == (arrayColors.length - 1)){
            lastIndexColor = 0;
        } else {
            lastIndexColor++;
        }

        Log.i(TAG, "saving last index position with value " + lastIndexColor);

        handle.set(INDEX_COLOR_KEY, lastIndexColor);
    }

    public int getLastPersonColor(){
        //we should suggest the next available color
        Log.i(TAG, "retuning color with position " + lastIndexColor);
        return arrayColors[lastIndexColor];
    }

    public void addSelectedItem(int position){
        contacts.getContacts()[position].setSelected(true);
    }

    public void removeSelectedItem(int position){
        contacts.getContacts()[position].setSelected(false);
    }

    public void clearSelectedList(){
        for(ContactInfo contactInfo: contacts.getContacts()){
            contactInfo.setSelected(false);
        }
    }

    public Maybe<List<Long>> saveSelectedItems(){
        List<Person> personList = new ArrayList<>();
        for(int i = 0; i < contacts.getContacts().length; i++){
            if(contacts.getContacts()[i].isSelected()){
                Person person = new Person(contacts.getContacts()[i].getName(), 0, arrayColors[lastIndexColor]);
                personList.add(person);
                if(lastIndexColor == (arrayColors.length - 1)){
                    lastIndexColor = 0;
                } else {
                    lastIndexColor++;
                }
            }
        }
        handle.set(INDEX_COLOR_KEY, lastIndexColor);
        return mainDao.insertPersons(personList);
    }

    public void updatePersonName(String name){
        if(person != null) {
            person.name = name;
        } else {
            person = new Person(name, 0, R.color.material_indigo);
        }
        handle.set(PERSON_KEY, person);
        Log.i(TAG, "updating person name in viewModel " + name);
    }

    public void updatePersonColor(int colorId){
        if(person != null) {
            person.colorId = colorId;
        } else {
            person = new Person("", 0, colorId);
        }
        handle.set(PERSON_KEY, person);
        Log.i(TAG, "updating person color in viewModel " + colorId);
    }


    public Completable deletePerson(Person person){
        mainDao.deleteAllRefWithPerson(person.personId);
        return mainDao.deletePerson(person);
    }

    public void swapCursor(Cursor newCursor){
        if (newCursor == cursor) {
            return;
        }
        Cursor oldCursor = cursor;
        cursor = newCursor;
        if(oldCursor != null){
            oldCursor.close();
        }
    }

    public Single<ContactInfo[]> makeArrayOfNames() {
            if(cursor.isAfterLast()){
                return Single.just(contacts.getContacts());
            } else {
                List<String> names = new ArrayList<>();
                int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY);
                while (cursor.moveToNext()) {
                    names.add(cursor.getString(nameIndex));
                }
                String[] stringsArray = names.toArray(new String[0]);
                ContactInfo[] contractInfos = new ContactInfo[stringsArray.length];
                contacts = new ContactsArray(contractInfos);
                for(int i = 0; i < stringsArray.length; i++){
                    contacts.getContacts()[i] = new ContactInfo(stringsArray[i], false);
                }

                handle.set(CONTACTS_KEY, contacts);
                return Single.just(contacts.getContacts());
            }
    }

    public Completable addNewPerson(String name, int colorId) {
        Person newPerson = new Person(name, 0, colorId);
        Log.i(TAG, "adding person in db with personId " + newPerson.personId);
        Log.i(TAG, "adding person in db with colorId " + newPerson.colorId);
        Log.i(TAG, "adding person in db with colorId " + newPerson.name);
        return mainDao.insertPerson(newPerson);
    }

    public Completable updatePerson(String name, int colorId){
        Person updatedPerson = new Person(person.personId, name, person.total, colorId);
        Log.i(TAG, "updating person in db with personId " + updatedPerson.personId);
        Log.i(TAG, "updating person in db with colorId " + updatedPerson.colorId);
        Log.i(TAG, "updating person in db with name " + updatedPerson.name);
        return mainDao.updatePerson(updatedPerson);
    }
}
