package com.moubassher.accountsfx.expenses;

import com.context.our_utilities.MyNotifications;
import core.GlobalConfig;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class ExpenseAddCtrl implements Initializable {

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        GlobalConfig.getInstance().applyIntegerFormatting(amountField);

        employeeDisplay.setText(GlobalConfig.getInstance().getLoggedInAccount().toString());

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

        int amount = Integer.valueOf(amountField.getText());
        Expense expense;
        if(noteField.getText().isEmpty()) {
            expense = new Expense(expenseTitle, amount);
        } else {
            String note = noteField.getText();
            expense = new Expense(expenseTitle, amount, note);
        }
        boolean added = ExpenseDao.getInstance().addExpense(expense);
        if(added) {
            MyNotifications.getInstance().showSimpleToast("تم إضافة المنصرف بنجاح", root);
        }
    }
}
