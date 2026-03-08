package com.moubassher.accountsfx;

import com.context.our_utilities.FxmlHelper;
import com.context.our_utilities.MyNotifications;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;


public class MaintainerAccountCtrl <F extends Enum<F> & Feature<F>> implements Initializable {

    @FXML
    private VBox root;

    @FXML
    private JFXTextField accountTypeField;

    @FXML
    private Button addAccountTypeBtn, deleteAccountTypeBtn, doneBtn;

    @FXML
    private FlowPane featurePane;

    @FXML
    private ScrollPane accordionContainer;

    @FXML
    private Accordion accountTypeAccordion;

    @FXML
    private ChoiceBox<AccountType.Class> accountTypeClassSelector;

    @FXML
    private ToggleButton viewAccountTypesBtn, viewAccountsBtn, currentAccountTypeToggleBtn, archivedAccountTypeToggleBtn;

    @FXML
    private HBox accountTypesPane;

    @FXML
    private VBox accountsPane;

    private final Class<F> featureEnumClass;

    private final AccountDao<F> accountDao;
    public MaintainerAccountCtrl(AccountDao<F> accountDao, Class<F> featureEnumClass) {
        this.accountDao = accountDao;
        this.featureEnumClass = featureEnumClass;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        accountTypeAccordion.getPanes().clear();

        accountTypeClassSelector.setItems(FXCollections.observableArrayList(AccountType.Class.values()));

        List<AccountType<F>> accountTypes = accountDao.readAccountTypes();
        displayCreatedAccountTypes(accountTypes);

        for (F feature : featureEnumClass.getEnumConstants()) {
            CheckBox checkBox = new CheckBox(feature.name());
            checkBox.setUserData(feature);
            featurePane.getChildren().add(checkBox);
        }

        addAccountTypeBtn.setOnAction(_ -> {
            addAccountType();
        });

        deleteAccountTypeBtn.setOnAction( _ -> {
            deleteAccountType();
        });

        currentAccountTypeToggleBtn.setOnAction(x -> {
            displayCreatedAccountTypes(accountDao.readAccountTypes());
        });

        archivedAccountTypeToggleBtn.setOnAction(x -> {
            List<AccountType<F>> archivedAccountTypes = accountDao.readArchivedAccountTypes();
            displayCreatedAccountTypes(archivedAccountTypes);
        });

        accountTypeAccordion.expandedPaneProperty().addListener((a, b, titledPane) -> {
            showSelectedAccountTypeDetails((AccountTitledPane) titledPane);
        });

        viewAccountTypesBtn.setOnAction(x -> {
            accountTypesPane.setVisible(true);
            accountTypesPane.setManaged(true);

            accountsPane.setVisible(false);
            accountsPane.setManaged(false);

            accountTypesPane.toFront();
        });


        Label notificationLbl = new Label();
        notificationLbl.setStyle("-fx-background-color: red; -fx-text-fill: white;");

        viewAccountsBtn.setGraphic(notificationLbl);
        viewAccountsBtn.setContentDisplay(ContentDisplay.RIGHT);

        viewAccountsBtn.setOnAction(x -> {
            accountsPane.setVisible(true);
            accountsPane.setManaged(true);

            accountTypesPane.setVisible(false);
            accountTypesPane.setManaged(false);
            accountsPane.toFront();
        });


        AccountOverviewCtrl<F> accountOverviewCtrl = new AccountOverviewCtrl<>(accountDao);
        Pane tabPane = FxmlHelper.loadFxml("account_overview.fxml", accountOverviewCtrl).getKey();
        accountsPane.getChildren().set(0, tabPane);

        List<Account<F>> pendingAccounts = accountDao.readPendingAccounts();
        if(!pendingAccounts.isEmpty()) {
            boolean ok = MyNotifications.getInstance().showConfirmAlert("", "هنالك حسابات مستخدمين قيد الإنشاء. الرجاء الموافقة عليها أو رفضها");
            if(ok) {
                accountOverviewCtrl.goToPendingUsersTab();
            }
        }

        doneBtn.setOnAction(x -> {
            if(accountDao.isAdminRequired()) {
                promptForAdminCreation();
            } else {
                Stage stage = (Stage) root.getScene().getWindow();
                stage.close();
            }
        });

    }

    private void promptForAdminCreation() {
        String closeMsg = "هل تريد إغلاق البرنامج؟";
        String adminRequiredMsg = "لم تتم إضافة أي حساب إداري. لا يمكن استخدام النظام بدون حساب إداري. متابعة؟";
        ButtonType createAdmin = new ButtonType("إنشاء حساب إداري");
        ButtonType closeProgram = new ButtonType("إغلاق البرنامج", ButtonBar.ButtonData.CANCEL_CLOSE);

        Optional<ButtonType> result = MyNotifications.getInstance().showConfirmAlert(closeMsg, adminRequiredMsg, List.of(createAdmin, closeProgram));
        result.ifPresent(button -> {
            if(button == createAdmin) {
                AccountCreateCtrl<F> accountCreateCtrl = new AccountCreateCtrl<>(accountDao,
                        List.of(), true);
                accountCreateCtrl.showCreateAdminAccountDialog();
            } else if(button.getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE) {
                System.exit(0);
            }
        });
    }

    private void deleteAccountType() {
        AccountTitledPane<F> selectedAccountPane = (AccountTitledPane<F>) accountTypeAccordion.getExpandedPane();
        if(selectedAccountPane != null) {
            AccountType<F> accountType = selectedAccountPane.getAccountType();

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Delete account type(" + accountType.getTypeName() +")?");
            confirmAlert.showAndWait().ifPresent(action -> {
                if(action == ButtonType.OK) {
                    boolean deleted = accountDao.deleteAccountType(accountType.getAccountTypeId());
                    if(deleted) {
                        accountTypeAccordion.getPanes().remove(selectedAccountPane);
                        confirmAlert.setContentText("Done!");
                        //MyNotifications.getInstance().showMessage("Account type deleted","");
                    }
                }
            });
        }
    }


    private void showSelectedAccountTypeDetails(AccountTitledPane<F> titledPane) {
        if(titledPane == null) return;
        AccountType<F> accountType = titledPane.getAccountType();
        String name = accountType.getTypeName();
        accountTypeField.setText(name);
        AccountType.Class accountClass = accountType.getAccountClass();
        accountTypeClassSelector.getSelectionModel().select(accountClass);

        List<String> privileges = accountType.getPrivileges().stream().map(feature -> feature.name()).toList();
        for (Node node : featurePane.getChildren()) {
            if (node instanceof CheckBox checkBox) {
                checkBox.setSelected(privileges.contains(checkBox.getText()));
            }
        }
    }

    private void addAccountType() {
        String accountTypeName = accountTypeField.getText();
        List<F> selectedPrivileges = readSelectedPrivileges();
        AccountType.Class accountClass = accountTypeClassSelector.getSelectionModel().getSelectedItem();

        AccountType<F> accountType = new AccountType<>(accountTypeName, selectedPrivileges, accountClass);

        boolean isValid = validateAccountType(accountType);
        if(!isValid) return;

        displayCreatedAccountType(accountType);

        boolean added = accountDao.addAccountType(accountType);
        if(added) {
            accountTypeField.clear();
            accountTypeClassSelector.getSelectionModel().clearSelection();
            clearSelectedPrivileges();
        }
    }

    private boolean validateAccountType(AccountType<F> accountType) {

        String accountTypeName = accountType.getTypeName();
        List<F> selectedPrivileges = accountType.getPrivileges();

        if(accountTypeName.isBlank()) {
            MyNotifications.getInstance().showErrorAlert("Error!", "Cannot add a nameless account type.");
            return false;
        }

        List<String> accountTypeNames = accountDao.readAccountTypes().stream().map(AccountType::getTypeName).toList();
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


    private void displayCreatedAccountTypes(List<AccountType<F>> accountTypes) {
        accountTypeAccordion.getPanes().clear();
        accountTypes.forEach(accountType -> {
            displayCreatedAccountType(accountType);
        });
    }

    private void displayCreatedAccountType(AccountType<F> accountType) {
        AccountTitledPane<F> accountTypePane = new AccountTitledPane<>();
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
                .forEach(feature -> selectedPrivileges.add(F.valueOf(featureEnumClass, feature)));
        return selectedPrivileges;
    }

    private void clearSelectedPrivileges() {
        featurePane.getChildren().stream()
                .filter(node -> node instanceof CheckBox cb && cb.isSelected())
                .forEach(feature -> ((CheckBox) feature).setSelected(false));
    }
}
