package com.moubassher.accountsfx.expenses;

import com.context.our_utilities.FxmlHelper;
import com.moubassher.accountsfx.Account;
import com.moubassher.accountsfx.AccountDao;
import com.moubassher.accountsfx.Feature;
import javafx.beans.property.SimpleIntegerProperty;
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
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class ExpenseViewCtrl <F extends Enum<F> & Feature<F>> implements Initializable {

    @FXML
    private TableColumn<Expense, Integer> amountCol;

    @FXML
    private TableColumn<Expense, LocalDateTime> dateCol;

    @FXML
    private TableColumn<Expense, String> expenseCol, noteCol;

    @FXML
    private TableColumn<Expense, Account<F>> madeByCol;

    @FXML
    private TableColumn<Expense, Boolean>approveCol;

    @FXML
    private Button approveBtn, deleteExpenseBtn, selectAllBtn, unselectAll;

    @FXML
    private DatePicker startDate, endDate;

    @FXML
    private ChoiceBox<Currency> currencySelector;

    @FXML
    private TableView<Expense> expensesTable;

    @FXML
    private Tab addExpenseTab, viewExpensesTab;

    private ExpenseDao expenseDao;

    private String appName;

    private AccountDao<F> accountDao;
    private Account<F> loggedInUser;

    private Map<Expense, Boolean> approvedExpensesMap = new HashMap<>();

    public ExpenseViewCtrl(String appName, AccountDao<F> accountDao, Account<F> loggedInUser, ExpenseDao expenseDao) {
        this.appName = appName;
        this.accountDao = accountDao;
        this.loggedInUser = loggedInUser;
        this.expenseDao = expenseDao;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        ExpenseAddCtrl<F> expenseAddCtrl = new ExpenseAddCtrl<>(appName, loggedInUser, expenseDao);
        Parent expenseAddView = FxmlHelper.loadFxml("expenses_add.fxml", expenseAddCtrl).getKey();
        addExpenseTab.setContent(expenseAddView);

        Currency currency = Currency.getInstance("SDG");
        String symbol = currency.getSymbol(Locale.forLanguageTag("AR"));
        System.out.println("symbol: " + symbol);

        expenseCol.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().getExpense()));
        noteCol.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().getNote()));

        amountCol.setCellValueFactory(x -> new SimpleIntegerProperty(x.getValue().getCost()).asObject());
        dateCol.setCellValueFactory(x -> new SimpleObjectProperty<>(x.getValue().getDate()));
        madeByCol.setCellValueFactory(x -> new SimpleObjectProperty<>(accountDao.getAccount(x.getValue().getMadeByUserId())));

        approveCol.setCellFactory(x -> createApprovalCell());

        expensesTable.setItems(FXCollections.observableArrayList(expenseDao.readExpenses()));

        refreshTableOnTabSelection();

    }

    private TableCell<Expense, Boolean> createApprovalCell() {
        return new TableCell<>() {

            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setOnAction(e -> {
                    Expense expense = getTableView().getItems().get(getIndex());

                    if (checkBox.isSelected()) {
                        approvedExpensesMap.put(expense, true);
                    } else {
                        approvedExpensesMap.remove(expense); // cleaner than storing false
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
                    checkBox.setSelected(approvedExpensesMap.containsKey(expense));
                    setGraphic(checkBox);
                }
            }
        };
    }


    private void refreshTableOnTabSelection() {
        viewExpensesTab.selectedProperty().addListener((a, b, selected) -> {
            if(selected) {
                List<Expense> expenses = expenseDao.readExpenses();
                expensesTable.setItems(FXCollections.observableArrayList(expenses));
            }
        });
    }

    public Parent load() {

        Parent expensesView = FxmlHelper.loadFxml("expenses_view.fxml", this).getKey();
        return expensesView;
    }
}
