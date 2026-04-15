package com.moubassher.accountsfx.expenses;

import com.moubassher.accountsfx.Account;
import core.RetailFeature;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.ResourceBundle;

public class ExpenseViewCtrl implements Initializable {

    @FXML
    private TableColumn<Expense, Integer> amountCol;

    @FXML
    private TableColumn<Expense, LocalDateTime> dateCol;

    @FXML
    private TableColumn<Expense, String> expenseCol, noteCol;

    @FXML
    private TableColumn<Expense, Account<RetailFeature>> madeByCol;

    @FXML
    private TableColumn<Expense, Boolean>approveCol;

    @FXML
    private Button approveBtn, deleteExpenseBtn, selectAllBtn, unselectAll;

    @FXML
    private DatePicker startDate, endDate;

    @FXML
    private ChoiceBox<accounting.Currency> currencySelector;

    @FXML
    private TableView<Expense> expensesTable;

    @FXML
    private Tab viewExpensesTab;

    private ExpenseDao expenseDao = ExpenseDao.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Currency currency = Currency.getInstance("SDG");
        System.out.println("currency:    + " + currency);


        expenseCol.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().getExpense()));
        noteCol.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().getNote()));

        amountCol.setCellValueFactory(x -> new SimpleIntegerProperty(x.getValue().getCost()).asObject());
        dateCol.setCellValueFactory(x -> new SimpleObjectProperty<>(x.getValue().getDate()));
        madeByCol.setCellValueFactory(x -> new SimpleObjectProperty<>(x.getValue().getMadeByUserId()));

        expensesTable.setItems(FXCollections.observableArrayList(expenseDao.readExpenses()));

        refreshTableOnTabSelection();


    }



    private void refreshTableOnTabSelection() {
        viewExpensesTab.selectedProperty().addListener((a, b, selected) -> {
            if(selected) {
                List<Expense> expenses = expenseDao.readExpenses();
                expensesTable.setItems(FXCollections.observableArrayList(expenses));
            }
        });
    }
}
