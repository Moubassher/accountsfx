package com.moubassher.accountsfx.expenses;

import com.context.my_javafx_utilities.MyNotifications;
import com.context.my_javafx_utilities.accounting.AccountingUtils;
import com.moubassher.accountsfx.Account;
import com.moubassher.accountsfx.Feature;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class ExpenseAddCtrl <F extends Enum<F> & Feature<F>> implements Initializable {

    @FXML
    private Button addExpenseBtn;

    @FXML
    private TextField amountField;

    @FXML
    private Label employeeDisplay;

    @FXML
    private TextField expenseField;

    @FXML
    private TextArea noteField;

    @FXML
    private VBox root;


    private ExpenseDao expenseDao;

    private String appName;
    private Account<F> loggedInUser;

    public ExpenseAddCtrl(String appName, Account<F> loggedInUser, ExpenseDao expenseDao) {
        this.appName = appName;
        this.loggedInUser = loggedInUser;
        this.expenseDao = expenseDao;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        employeeDisplay.setText(loggedInUser.toString());

        AccountingUtils.applyIntegerFormatting(amountField);

        addExpenseBtn.setOnAction(x -> {
            addExpense();
        });

    }

    private void addExpense() {
        String expenseTitle = expenseField.getText();
        if(expenseTitle == null || expenseTitle.isEmpty()) {
            MyNotifications.getInstance().showFieldError(expenseField, "قم بإضافة اسم المنصرف");
            return;
        }

        int amount = Integer.valueOf(amountField.getText().replace(",", ""));

        int madeByUserId = loggedInUser.getUserId();

        Expense expense;
        if(noteField.getText().isEmpty()) {
            expense = new Expense(expenseTitle, amount, madeByUserId);
        } else {
            String note = noteField.getText();
            expense = new Expense(expenseTitle, amount, madeByUserId, note);
        }
        boolean added = expenseDao.addExpense(expense);
        if(added) {
            clearExpenseFields();
            MyNotifications.getInstance().showSimpleToast("تم إضافة المنصرف بنجاح", root);
        }
    }

    private void clearExpenseFields() {
        expenseField.clear();
        amountField.clear();
        noteField.clear();
    }
}


