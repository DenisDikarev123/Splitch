package com.example.splitch;

import androidx.lifecycle.SavedStateHandle;

import com.example.splitch.db.MainDao;
import com.example.splitch.features.receipt.AddingReceiptViewModel;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class AddingReceiptViewModelTest {

    private static final String ERROR_MAX_NUMBER = "error_max_number";

    private AddingReceiptViewModel viewModel;

    @Before
    public void setViewModel(){
        MainDao mainDao = Mockito.mock(MainDao.class);
        SavedStateHandle stateHandle = Mockito.mock(SavedStateHandle.class);
        viewModel = new AddingReceiptViewModel(mainDao, stateHandle);
    }

    @Test
    public void verifyPrice_CorrectPrice() {
        assertThat(viewModel.verifyPrice("."), equalTo("00.00"));
        assertThat(viewModel.verifyPrice("12345678"), equalTo(ERROR_MAX_NUMBER));
        assertThat(viewModel.verifyPrice("12345678."), equalTo(ERROR_MAX_NUMBER));
        assertThat(viewModel.verifyPrice("100.00"), equalTo("100.00"));
    }

    @Test
    public void verifyName() {
        assertThat(viewModel.verifyName("Alex"), equalTo("Alex"));
    }

    @Test
    public void parseDouble(){
        assertThat(viewModel.parseToDouble("123.0"), equalTo(123.00));
        assertThat(viewModel.parseToDouble("123.1"), equalTo(123.10));
        assertThat(viewModel.parseToDouble("123.23"), equalTo(123.24));
        assertThat(viewModel.parseToDouble("123"), equalTo(123.00));
        assertThat(viewModel.parseToDouble("123.223"), equalTo(123.23));
        assertThat(viewModel.parseToDouble("123.220"), equalTo(123.22));
        assertThat(viewModel.parseToDouble("123.229"), equalTo(123.23));
        assertThat(viewModel.parseToDouble("123.9999"), equalTo(124.00));
        assertThat(viewModel.parseToDouble("123.834758375"), equalTo(123.84));
        assertThat(viewModel.parseToDouble("123."), equalTo(123.00));
        assertThat(viewModel.parseToDouble("5.57754642"), equalTo(5.58));
    }
}
