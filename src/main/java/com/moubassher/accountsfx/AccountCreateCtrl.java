package com.moubassher.accountsfx;

import com.context.our_utilities.MyNotifications;
import com.jfoenix.controls.JFXComboBox;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.ImageCursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Duration;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import static com.moubassher.accountsfx.PasswordStrength.STRONG;


public class AccountCreateCtrl<F extends Enum<F> & Feature<F>> implements Initializable {

    @FXML
    private VBox root;

    @FXML
    private TextField userNameField, visiblePasswordField, visibleConfirmPasswordField;

    @FXML
    private PasswordField passwordField, confirmPasswordField;

    @FXML
    private JFXComboBox<AccountType<F>> accountTypeSelector;

    @FXML
    private ProgressBar strengthBar;

    @FXML
    private Label strengthLabel;

    @FXML
    private Button showPasswordBtn1, showPasswordBtn2, createAccountBtn;

    @FXML
    private ProgressIndicator createAccountProgress;

    private Tooltip passwordMismatch;

    private AccountDao<F> accountDao;

    private AccountValidator accountValidator;

    private boolean isAdminAccount, isAccountCreated;

    private List<F> privileges;

    private Window thisWindow;

    public AccountCreateCtrl(AccountDao<F> accountDao, List<F> privileges, boolean isAdminAccount) {
        this.accountDao = accountDao;
        this.privileges = privileges;
        this.isAdminAccount = isAdminAccount;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        getWindowReference();

        accountValidator = new AccountValidator();

        if(accountTypeSelector.getItems().isEmpty()) {
            AccountType<F> accountType = AccountType.of("مشرف",privileges, true);
            accountTypeSelector.getItems().add(accountType);
        }

        visiblePasswordField.managedProperty().bind(visiblePasswordField.visibleProperty());

        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());

        visibleConfirmPasswordField.textProperty().bindBidirectional(confirmPasswordField.textProperty());

        onPasswordTyped(this::showPasswordStrength);

        onConfirmedPasswordTyped(confirmedPass -> {
            validatePasswordMatch(passwordField.getText(), confirmedPass);
        });

        passwordMismatch = new Tooltip();
        passwordMismatch.setText("يجب أن تتطابق كلمة المرور");
        passwordMismatch.setShowDelay(Duration.ZERO);
        passwordMismatch.setHideDelay(Duration.INDEFINITE);

        showPasswordBtn1.setOnMousePressed(e -> {
            visiblePasswordField.setVisible(true);
            passwordField.setVisible(false);
        });


        showPasswordBtn1.setOnMouseReleased(e -> {
            visiblePasswordField.setVisible(false);
            passwordField.setVisible(true);
        });

        showPasswordBtn2.setOnMousePressed(x -> {
            visibleConfirmPasswordField.setVisible(true);
            confirmPasswordField.setVisible(false);
        });

        showPasswordBtn2.setOnMouseReleased(x -> {
            visibleConfirmPasswordField.setVisible(false);
            confirmPasswordField.setVisible(true);
        });

        createAccountProgress.setVisible(false);

        createAccountBtn.setOnAction(x -> {
            boolean isValid = validateAccountDetails();
            if(isValid) {
                isAccountCreated = createAccount();
            }
        });

        if(isAdminAccount) {
            enforceAdminAccountCreation();
        } else {
            accountTypeSelector.getItems().setAll(accountDao.readAccountTypes());
        }

        Image notAllowedImage = new Image(this.getClass().getResource("not_allowed.png").toExternalForm());

        createAccountBtn.hoverProperty().addListener((a, b, hovered) -> {
            if(hovered) {
                String password = passwordField.getText();
                String confirmedPassword = confirmPasswordField.getText();
                if(!accountValidator.validatePassword(password, confirmedPassword)) {
                    createAccountBtn.setCursor(new ImageCursor(notAllowedImage));
                } else {
                    createAccountBtn.setCursor(null);
                }
            }
        });

    }

    private void getWindowReference() {
        root.sceneProperty().addListener((a, b, scene) -> {
            if(scene != null) {
                root.getScene().windowProperty().addListener((obs, old, window) -> {
                    thisWindow = window;
                });
            }
        });
    }

    private boolean validateAccountDetails() {
        boolean isValid;
        String userName = userNameField.getText();
        if(userName.isBlank()) {
            userNameField.getTooltip().show(thisWindow);
            userNameField.requestFocus();
            return false;
        }

        AccountType accountType = accountTypeSelector.getSelectionModel().getSelectedItem();
        if(accountType == null) {
            accountTypeSelector.getTooltip().show(thisWindow);
            accountTypeSelector.requestFocus();
            return false;
        }
        isValid = validatePassword();

        return isValid;
    }


    private boolean validatePassword() {
        String password = passwordField.getText();
        String confirmed = confirmPasswordField.getText();

        return validatePasswordStrength(password)
                && validatePasswordMatch(password, confirmed);
    }


    private boolean validatePasswordStrength(String password) {
        PasswordStrength strength = accountValidator.evaluateStrength(password);
        if (strength != STRONG) {
            MyNotifications.getInstance().showErrorAlert(
                    "كلمة المرور ليست قوية ما يكفي",
                    "يجب إضافة كلمة مرور قوية حسب الإرشادات لرفع مستوى الحماية"
            );
            return false;
        }
        return true;
    }


    private boolean validatePasswordMatch(String password, String confirmed) {
        boolean match = accountValidator.validatePassword(password, confirmed);
        if (!match) {
            passwordMismatch.show(confirmPasswordField.getScene().getWindow());
        } else {
            passwordMismatch.hide();
        }
        return match;
    }


    public void enforceAdminAccountCreation() {
        accountTypeSelector.getItems().clear();
        List<AccountType<F>> adminAccountTypes = accountDao.readAccountTypes()
                .stream()
                .filter( accountType -> accountType.isAdmin()).toList();

        accountTypeSelector.setItems(FXCollections.observableArrayList(adminAccountTypes));
    }

    public boolean isAccountCreated() {
        return isAccountCreated;
    }

    private void onPasswordTyped(Consumer<String> passwordFunc) {
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            passwordFunc.accept(newVal);
        });
    }

    private void onConfirmedPasswordTyped(Consumer<String> func) {
        confirmPasswordField.textProperty().addListener((a, b, newVal) -> {
            func.accept(newVal);
        });
    }


    private  void showPasswordStrength(String password) {
        PasswordStrength strength = accountValidator.evaluateStrength(password);
        if (strength == PasswordStrength.WEAK) {
            strengthLabel.setText("كلمة المرور ضعيفة");
            strengthBar.setStyle("-fx-accent: red;");
        } else if (strength == PasswordStrength.MEDIUM) {
            strengthLabel.setText("كلمة المرور متوسطة الجودة");
            strengthBar.setStyle("-fx-accent: orange;");
        } else {
            strengthLabel.setText("كلمة المرور قوية ✔");
            strengthBar.setStyle("-fx-accent: green;");
        }

        double score = strength.getScore();
        strengthBar.setProgress(score);

    }


    private boolean createAccount() {

        String userName = userNameField.getText();
        String password = passwordField.getText();
        AccountType userType = accountTypeSelector.getSelectionModel().getSelectedItem();

        createAccountBtn.setDisable(true);
        createAccountProgress.setVisible(true);

        Task<String> hashingTask = new Task<>() {
            @Override
            protected String call() {
                return accountValidator.hashPassword(password);
            }
        };


        hashingTask.setOnSucceeded(e -> {
            String hashedPassword = hashingTask.getValue();
            Account<F> account = new Account<>(userName, hashedPassword, userType);

            isAccountCreated = accountDao.addNewAccount(account);

            createAccountBtn.setDisable(false);
            createAccountProgress.setVisible(false);
        });

        String hashedPassword = hashingTask.getValue();
        Account<F> account = new Account(userName, hashedPassword, userType);
        isAccountCreated = accountDao.addNewAccount(account);

        createAccountProgress.setVisible(false);
        createAccountBtn.setDisable(false);

        return isAccountCreated;
    }



}
