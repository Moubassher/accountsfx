package com.moubassher.accountsfx;

import com.context.our_utilities.MyNotifications;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


public class MaintainerAccountCtrl <F extends Enum<F> & Feature<F>> implements Initializable {

    @FXML
    private HBox root;

    @FXML
    private JFXTextField accountTypeField;

    @FXML
    private Button addAccountTypeBtn, deleteAccountTypeBtn, resetPasswordBtn, refreshAccountsBtn;

    @FXML
    private FlowPane featurePane;

    @FXML
    private ScrollPane accordionContainer;

    @FXML
    private Accordion accountTypeAccordion;

    @FXML
    private CheckBox isAdminBox;

    @FXML
    private ToggleButton viewAccountTypesBtn, viewAccountsBtn;

    @FXML
    private HBox accountTypesPane;

    @FXML
    private VBox accountsPane;

    @FXML
    private ListView<Account<F>> accountsListView;

    private Class<F> featureEnumClass;

    private AccountDao<F> accountDao;
    public MaintainerAccountCtrl(AccountDao<F> accountDao, Class<F> featureEnumClass) {
        this.accountDao = accountDao;
        this.featureEnumClass = featureEnumClass;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        accountTypeAccordion.getPanes().clear();

        List<AccountType<F>> accountTypes = accountDao.readAccountTypes();
        accountTypes.forEach(accountType -> {
            displayCreatedAccountType(accountType);
        });

        for (F feature : featureEnumClass.getEnumConstants()) {
            CheckBox checkBox = new CheckBox(feature.name());
            checkBox.setUserData(feature);
            featurePane.getChildren().add(checkBox);
        }

        addAccountTypeBtn.setOnAction(x -> {
            addAccountType();
        });

        deleteAccountTypeBtn.setOnAction( x -> {
            AccountTitledPane<F> selectedAccountPane = (AccountTitledPane<F>) accountTypeAccordion.getExpandedPane();
            if(selectedAccountPane != null) {
                AccountType<F> accountType = selectedAccountPane.getAccountType();

                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Delete account type(" + accountType.getType() +")?");
                confirmAlert.showAndWait().ifPresent(action -> {
                    if(action == ButtonType.OK) {
                        boolean deleted = accountDao.deleteAccountType(accountType.getAccountTypeId());
                        if(deleted) {
                            MyNotifications.getInstance().showMessage("Account type deleted","");
                        }
                    }
                });

            }
        });

        accountTypeAccordion.expandedPaneProperty().addListener((a, b, titledPane) -> {
            showSelectedAccountTypeDetails((AccountTitledPane) titledPane);
        });


        viewAccountTypesBtn.setOnAction(x -> {
            accountTypesPane.setVisible(true);
            accountsPane.setVisible(false);
            accountTypesPane.toFront();
        });

        viewAccountsBtn.setOnAction(x -> {
            accountsPane.setVisible(true);
            accountTypesPane.setVisible(false);
            accountsPane.toFront();
        });

        resetPasswordBtn.setOnAction(x -> {
            Account<F> account = accountsListView.getSelectionModel().getSelectedItem();
            if(account != null) {
                Alert resetAlert = new Alert(Alert.AlertType.CONFIRMATION);
                resetAlert.setTitle("Reset password?");
                resetAlert.showAndWait().ifPresent(result -> {
                    if(result == ButtonType.OK) {
                        accountDao.resetPassword(account, "123");
                    }
                });
            }
        });

        loadAccounts();

        refreshAccountsBtn.setOnAction(x -> {
            loadAccounts();
        });

    }

    private void loadAccounts() {
        ObservableList<Account<F>> accounts = accountDao.readAccounts();
        accountsListView.setItems(accounts);
    }

    private void showSelectedAccountTypeDetails(AccountTitledPane titledPane) {
        AccountTitledPane accountTitledPane = titledPane;
        if(accountTitledPane == null) return;
        AccountType<F> accountType = accountTitledPane.getAccountType();
        String name = accountType.getType();
        accountTypeField.setText(name);
        boolean isAdmin = accountType.isAdmin();
        isAdminBox.setSelected(isAdmin);
        List<String> privileges = accountType.getPrivileges().stream().map(feature -> feature.name()).toList();
        for (Node node : featurePane.getChildren()) {
            if (node instanceof CheckBox checkBox) {
                if (privileges.contains(checkBox.getText())) {
                    checkBox.setSelected(true);
                } else {
                    checkBox.setSelected(false);
                }
            }
        }
    }

    private void addAccountType() {
        String accountTypeName = accountTypeField.getText();
        List<F> selectedPrivileges = readSelectedPrivileges();
        boolean isAdmin = isAdminBox.isSelected();

        AccountType<F> accountType = new AccountType<>(accountTypeName, selectedPrivileges, isAdmin);

        boolean isValid = validateAccountType(accountType);
        if(!isValid) return;

        displayCreatedAccountType(accountType);

        boolean added = accountDao.addAccountType(accountType);
        if(added) {
            accountTypeField.clear();
            isAdminBox.setSelected(false);
            clearSelectedPrivileges();
        }
    }

    private boolean validateAccountType(AccountType<F> accountType) {

        String accountTypeName = accountType.getType();
        List<F> selectedPrivileges = accountType.getPrivileges();

        if(accountTypeName.isBlank()) {
            MyNotifications.getInstance().showErrorAlert("Error!", "Cannot add a nameless account type.");
            return false;
        }

        List<String> accountTypeNames = accountDao.readAccountTypes().stream().map(AccountType::getType).toList();
        if(accountTypeNames.contains(accountTypeName)) {
            MyNotifications.getInstance().showErrorAlert("Account type already exists.","Cannot add 2 account types with the same name");
            return false;
        }

        if(selectedPrivileges.isEmpty()) {
            MyNotifications.getInstance().showErrorAlert("Error!", "Cannot add an account type with zero privileges!");
            return false;
        }

        return true;
    }

    private void displayCreatedAccountType(AccountType<F> accountType) {
        AccountTitledPane accountTypePane = new AccountTitledPane();
        accountTypePane.setAccountType(accountType);

        accountTypeAccordion.getPanes().add(accountTypePane);
        accountTypeAccordion.setExpandedPane(accountTypePane);
        accordionContainer.setVvalue(1.0);
        accountTypePane.requestFocus();
    }

    private List<F> readSelectedPrivileges() {
        List<F> selectedPrivileges = new ArrayList<>();

        featurePane.getChildren().stream()
                .filter(node -> node instanceof CheckBox cb && cb.isSelected())
                .map(featureBox -> ((CheckBox) featureBox).getText())
                .forEach(feature -> {
                    selectedPrivileges.add(F.valueOf(featureEnumClass, feature));
                });
        return selectedPrivileges;
    }

    private void clearSelectedPrivileges() {
        featurePane.getChildren().stream()
                .filter(node -> node instanceof CheckBox cb && cb.isSelected())
                .forEach(feature -> ((CheckBox) feature).setSelected(false));
    }
}
