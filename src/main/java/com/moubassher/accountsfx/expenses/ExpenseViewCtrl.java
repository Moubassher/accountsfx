package com.moubassher.accountsfx.expenses;

import com.context.my_javafx_utilities.FxmlHelper;
import com.context.my_javafx_utilities.accounting.AccountingUtils;
import com.moubassher.accountsfx.Account;
import com.moubassher.accountsfx.AccountDao;
import com.moubassher.accountsfx.Feature;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class ExpenseViewCtrl <F extends Enum<F> & Feature<F>> implements Initializable {

    @FXML
    private TableColumn<Expense, String> amountCol;

    @FXML
    private TableColumn<Expense, LocalDate> dateCol;

    @FXML
    private TableColumn<Expense, String>  timeCol;

    @FXML
    private TableColumn<Expense, String> expenseCol, noteCol;

    @FXML
    private TableColumn<Expense, Account<F>> madeByCol;

    @FXML
    private TableColumn<Expense, Boolean>approveCol;

    @FXML
    private ToggleButton unapprovedExpensesViewBtn, approvedExpensesViewBtn, allExpensesViewBtn;

    @FXML
    private Button approveBtn, unapproveBtn, deleteExpenseBtn, selectAllBtn, unselectAll;

    @FXML
    private DatePicker startDate, endDate;

    @FXML
    private ChoiceBox<Currency> currencySelector;

    @FXML
    private TableView<Expense> expensesTable;

    @FXML
    private Tab addExpenseTab, viewExpensesTab;

    @FXML
    private Label unapprovedExpensesCounter;

    private ExpenseDao expenseDao;

    private String appName;

    private AccountDao<F> accountDao;
    private Account<F> loggedInUser;

    private Map<Expense, Boolean> selectedExpensesMap = new HashMap<>();

    public ExpenseViewCtrl(String appName, AccountDao<F> accountDao, Account<F> loggedInUser, ExpenseDao expenseDao) {
        this.appName = appName;
        this.accountDao = accountDao;
        this.loggedInUser = loggedInUser;
        this.expenseDao = expenseDao;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        ExpenseAddCtrl<F> expenseAddCtrl = new ExpenseAddCtrl<>(appName, loggedInUser, expenseDao);
        Parent expenseAddView = FxmlHelper.loadFxml("expenses_add.fxml", expenseAddCtrl);
        addExpenseTab.setContent(expenseAddView);

        Currency currency = Currency.getInstance("SDG");
        expenseCol.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().getExpense()));
        noteCol.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().getNote()));

        amountCol.setCellValueFactory(x ->
                new SimpleStringProperty(AccountingUtils.format(x.getValue().getCost())));
        amountCol.setComparator(Comparator.comparing(string -> Integer.valueOf(string.replace(",",""))));
        dateCol.setCellValueFactory(x -> new SimpleObjectProperty<>(x.getValue().getTimestamp().toLocalDate()));
        timeCol.setCellValueFactory(x ->
                new SimpleStringProperty(formatTime(x.getValue().getTimestamp().toLocalTime())));
        madeByCol.setCellValueFactory(x -> new SimpleObjectProperty<>(accountDao.getAccount(x.getValue().getMadeByUserId())));

        approveCol.setCellFactory(x -> createApprovalCell());

        List<Expense> unapproved = expenseDao.readUnapprovedExpenses();
        int unapprovedCount = unapproved.size();
        unapprovedExpensesCounter.setText(unapprovedCount+"");

        expensesTable.setItems(FXCollections.observableArrayList(unapproved));

        approveBtn.setOnAction(x -> {
            approveExpenses();
        });

        unapproveBtn.setOnAction(x -> {
            unapproveExpenses();
        });

        unapprovedExpensesViewBtn.selectedProperty().addListener((a, b, c) -> {
            if(c) {
                List<Expense> expenses = expenseDao.readUnapprovedExpenses();
                expensesTable.setItems(FXCollections.observableArrayList(expenses));
                approveBtn.setDisable(false);
                unapproveBtn.setDisable(true);
            }
        });

        approvedExpensesViewBtn.selectedProperty().addListener((a, b, c) -> {
            if(c) {
                List<Expense> approvedExpenses = expenseDao.readApprovedExpenses();
                expensesTable.setItems(FXCollections.observableArrayList(approvedExpenses));
                unapproveBtn.setDisable(false);
                approveBtn.setDisable(true);
            }
        });

        allExpensesViewBtn.selectedProperty().addListener((a, b, c) ->{
            if(c) {
                List<Expense> allExpenses = expenseDao.readAllExpenses();
                expensesTable.setItems(FXCollections.observableArrayList(allExpenses));
            }
        });

        selectAllBtn.setOnAction(x -> {
            selectAllExpenses();
        });

        unselectAll.setOnAction(x -> {
            selectedExpensesMap.clear();
            expensesTable.refresh();
        });


        refreshTableOnTabSelection();

    }

    private void selectAllExpenses() {
        for (Expense expense : expensesTable.getItems()) {
            selectedExpensesMap.put(expense, true);
        }

        expensesTable.refresh();
    }

    private TableCell<Expense, Boolean> createApprovalCell() {
        return new TableCell<>() {

            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setOnAction(e -> {
                    Expense expense = getTableView().getItems().get(getIndex());

                    if (checkBox.isSelected()) {
                        selectedExpensesMap.put(expense, true);
                    } else {
                        selectedExpensesMap.remove(expense); // cleaner than storing false
                    }
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    var expense = getTableView().getItems().get(getIndex());
                    checkBox.setSelected(selectedExpensesMap.containsKey(expense));
                    setGraphic(checkBox);
                }
            }
        };
    }

    private void approveExpenses() {
        for (Expense expense : selectedExpensesMap.keySet()) {
            expenseDao.approveExpense(expense);
        }
        selectedExpensesMap.clear();
        expensesTable.setItems(FXCollections.observableArrayList(expenseDao.readUnapprovedExpenses()));

    }

    private void unapproveExpenses() {
        for(Expense expense : selectedExpensesMap.keySet()) {
            expenseDao.unapproveExpense(expense);
        }
        selectedExpensesMap.clear();

        expensesTable.setItems(FXCollections.observableArrayList(expenseDao.readApprovedExpenses()));
    }


    private String formatTime(LocalTime time) {
        var clock = DateTimeFormatter.ofPattern("h:mm");
        var amPm = DateTimeFormatter.ofPattern("a").localizedBy(Locale.forLanguageTag("AR"));
        return clock.format(time) + amPm.format(time);
    }


    private void refreshTableOnTabSelection() {
        viewExpensesTab.selectedProperty().addListener((a, b, selected) -> {
            if(selected) {
                List<Expense> expenses = expenseDao.readUnapprovedExpenses();
                expensesTable.setItems(FXCollections.observableArrayList(expenses));
            }
        });
    }

}
