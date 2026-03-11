package com.moubassher.accountsfx;

import com.context.our_utilities.MyNotifications;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;

public class AccountOverviewCtrl<F extends Enum<F> & Feature<F>> implements Initializable{

    @FXML
    private TabPane rootTabPane;

    @FXML TableView<Account<F>> accountApprovalTable;

    @FXML TableColumn<Account<F>, String> userNameCol, accountTypeCol;

    @FXML
    TableColumn<Account<F>, AccountType.Class> accountClassCol;

    @FXML
    private TableColumn<Account<F>, Boolean> approveCol;
    @FXML
    private ListView<Account<F>> accountsListView;

    @FXML Button approveAllBtn, approveSelectedBtn, createAccountBtn, denyUnselectedBtn, refreshPendingBtn,
            refreshAccountsBtn, resetPasswordBtn, deleteSelectedAccountBtn;

    @FXML
    private Tab activeUsersTab, pendingUsersTab;

    private List<Account<F>> pendingAccounts;

    private Map<Account<F>, Boolean> approvedAccountsMap = new HashMap<>();

    private AccountDao<F> accountDao;


    public AccountOverviewCtrl(AccountDao<F> accountDao) {
        this.accountDao = accountDao;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        loadAccounts();

        pendingUsersTab.selectedProperty().addListener((a, b, selected) -> {
            if(selected) {
                loadPendingAccounts();
            }
        });

        activeUsersTab.selectedProperty().addListener((a, b, selected) -> {
            if(selected) {
                loadAccounts();
            }
        });

        createAccountBtn.setOnAction(x -> {
            if(!accountDao.hasNonMaintainerAccountType()) {
                promptAccountTypesCreation();
            } else {
                boolean isAdminRequired = accountDao.isAdminRequired();
                AccountCreateCtrl<F> accountCreateCtrl = new AccountCreateCtrl<>(accountDao, isAdminRequired);
                accountCreateCtrl.showCreateAdminAccountDialog();
            }
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


        refreshAccountsBtn.setOnAction(x -> {
            loadAccounts();
        });


        userNameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        accountTypeCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getAccountType().getTypeName()));
        accountClassCol.setCellValueFactory(cell -> new SimpleObjectProperty(cell.getValue().getAccountType().getAccountClass()));
        approveCol.setCellValueFactory(cell -> new SimpleBooleanProperty(approvedAccountsMap.getOrDefault(cell.getValue(), false)));

        approveCol.setCellFactory(col -> createApprovalCell());

        loadPendingAccounts();


        approveAllBtn.setOnAction(x -> {
            loadPendingAccounts();
            pendingAccounts.forEach(account -> {
                accountDao.approveNewAccount(account);
            });
        });

        approveSelectedBtn.setOnAction(x -> {
            approvedSelectedAccounts();
        });

        denyUnselectedBtn.setOnAction(x -> {
            boolean confirmed = confirmDeletePending();
            if(confirmed) {
                denyUnselectedAccounts();
            }
        });

        deleteSelectedAccountBtn.setOnAction(x -> {
            deleteSelectedAccount();
        });

        refreshPendingBtn.setOnAction(x -> {
            loadPendingAccounts();
        });

    }

    private boolean promptAccountTypesCreation() {
        String noAccountTypesMsg = "لا توجد أي فئات حسابات في النظام، يجب إنشاء فئة حساب إدارية واحدة على الأقل";
        ButtonType ok = new ButtonType("موافق", ButtonBar.ButtonData.OK_DONE);
        Optional<ButtonType> result =  MyNotifications.getInstance()
                .showConfirmAlert("لا يوجد فئات حسابات", noAccountTypesMsg, List.of(ok), NodeOrientation.RIGHT_TO_LEFT);
        return result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE;
    }

    private boolean confirmDeletePending() {
        List<ButtonType> options = new ArrayList<>();
        ButtonType ok = new ButtonType("موافق", ButtonBar.ButtonData.OK_DONE);
        ButtonType back = new ButtonType("رجوع", ButtonBar.ButtonData.CANCEL_CLOSE);
        options.addAll(List.of(ok, back));
        Optional<ButtonType> result = MyNotifications.getInstance().showConfirmAlert("هل أنت متأكد", "", options);
        return (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE);

    }


    private void approvedSelectedAccounts() {
        Iterator<Map.Entry<Account<F>, Boolean>> iterator =
                approvedAccountsMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Account<F>, Boolean> entry = iterator.next();

            Account<F> account = entry.getKey();
            boolean selected = entry.getValue();

            if (selected) {
                boolean approved = accountDao.approveNewAccount(account);

                if (approved) {
                    iterator.remove(); // SAFE removal
                }
            }
        }
        loadPendingAccounts();
    }

    private void denyUnselectedAccounts() {

        for (Account<F> account : pendingAccounts) {

            boolean selected = approvedAccountsMap.containsKey(account);

            if (!selected) {
                accountDao.deletePendingUser(account.getUserId());
            }
        }

        approvedAccountsMap.clear();
        loadPendingAccounts();
    }

    private void deleteSelectedAccount() {
        boolean confirmed = MyNotifications.getInstance().showConfirmAlert("هل أنت متأكد؟", "عند حذف الحساب، سيتم إضافته للأرشيف حيث يمكنك إسترجاعه من جديد");
        if(confirmed) {
           Account<F> accountToDelete= accountsListView.getSelectionModel().getSelectedItem();
           boolean deleted = accountDao.deleteAccount(accountToDelete);
           if(deleted) {
               MyNotifications.getInstance().showSimpleToast( "تم حذف الحساب وإضافته للأرشيف", (Pane) rootTabPane.getParent());
            loadAccounts();
           }
        }
    }


    private void loadPendingAccounts() {
        pendingAccounts = accountDao.readPendingAccounts();

        Label noPendingAccountsLbl = new Label("ليس هنالك أي حسابات قيد الإنشاء/للتصديق عليها في النظام حالياً");
        noPendingAccountsLbl.setStyle("-fx-text-fill: black;");
        accountApprovalTable.setPlaceholder(noPendingAccountsLbl);
        accountApprovalTable.setItems(FXCollections.observableArrayList(pendingAccounts));
    }

    private void loadAccounts() {
        Label noActiveAccountsLbl = new Label("ليس هنالك أي حسابات نشطة على النظام حالياً");
        noActiveAccountsLbl.setStyle("-fx-text-fill: black;");
        accountsListView.setPlaceholder(noActiveAccountsLbl);

        List<Account<F>> accounts = accountDao.readAccounts();
        accountsListView.setItems(FXCollections.observableArrayList(accounts));
    }


    private TableCell<Account<F>, Boolean> createApprovalCell() {
        return new TableCell<>() {

            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setOnAction(e -> {
                    Account<F> account = getTableView().getItems().get(getIndex());

                    if (checkBox.isSelected()) {
                        approvedAccountsMap.put(account, true);
                    } else {
                        approvedAccountsMap.remove(account); // cleaner than storing false
                    }
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    Account<F> account = getTableView().getItems().get(getIndex());
                    checkBox.setSelected(approvedAccountsMap.containsKey(account));
                    setGraphic(checkBox);
                }
            }
        };
    }

    void goToPendingUsersTab() {
        rootTabPane.getSelectionModel().select(pendingUsersTab);
    }
}