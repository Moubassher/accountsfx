package com.moubassher.accountsfx;

import com.context.our_utilities.FxmlHelper;
import com.context.our_utilities.MyNotifications;
import com.context.themes.vibrant_blue.MyThemes;
import com.jfoenix.controls.JFXComboBox;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.ImageCursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import org.controlsfx.control.MaskerPane;


import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import static com.moubassher.accountsfx.PasswordStrength.STRONG;


public class AccountCreateCtrl<F extends Enum<F> & Feature<F>> implements Initializable {

    @FXML
    private StackPane root;

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
    private MaskerPane masker;

    private Tooltip passwordMismatch;

    private AccountDao<F> accountDao;

    private PasswordValidator passwordValidator;

    private boolean isAdminRequired, isAccountCreated;

    private Account<F> createdAccount;
    public AccountCreateCtrl(AccountDao<F> accountDao, boolean isAdminRequired) {
        this.accountDao = accountDao;
        this.isAdminRequired = isAdminRequired;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        passwordValidator = new PasswordValidator();

        visiblePasswordField.managedProperty().bind(visiblePasswordField.visibleProperty());

        onPasswordTyped(this::showPasswordStrength);

        onConfirmedPasswordTyped(confirmedPass -> {
            validatePasswordMatch(passwordField.getText(), confirmedPass);
        });

        passwordMismatch = new Tooltip();
        passwordMismatch.setText("يجب أن تتطابق كلمة المرور");
        passwordMismatch.setShowDelay(Duration.ZERO);
        passwordMismatch.setHideDelay(Duration.INDEFINITE);

/*
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
*/

        showPasswordBtn1.setOnMousePressed(e -> {
            // Sync hidden to visible before showing
            visiblePasswordField.setText(passwordField.getText());
            visiblePasswordField.setVisible(true);
            passwordField.setVisible(false);
        });

        showPasswordBtn1.setOnMouseReleased(e -> {
            // Sync visible back to hidden before hiding
            passwordField.setText(visiblePasswordField.getText());
            visiblePasswordField.setVisible(false);
            passwordField.setVisible(true);
        });

        showPasswordBtn2.setOnMousePressed(x -> {
            visibleConfirmPasswordField.setText(confirmPasswordField.getText());
            visibleConfirmPasswordField.setVisible(true);
            confirmPasswordField.setVisible(false);
            visibleConfirmPasswordField.requestFocus();
            visibleConfirmPasswordField.positionCaret(visibleConfirmPasswordField.getText().length());
        });

        showPasswordBtn2.setOnMouseReleased(x -> {
            confirmPasswordField.setText(visibleConfirmPasswordField.getText());
            visibleConfirmPasswordField.setVisible(false);
            confirmPasswordField.setVisible(true);
            confirmPasswordField.requestFocus();
            confirmPasswordField.positionCaret(confirmPasswordField.getText().length());
        });

        masker.setMouseTransparent(false);

        createAccountBtn.setOnAction(x -> {
            boolean isValid = validateAccountDetails();
            if(isValid) {
                createAccount();
            }
        });

        if(isAdminRequired) {
            enforceAdminAccountCreation();
        } else {
            accountTypeSelector.getItems().setAll(accountDao.readAccountTypes());
        }

        Image notAllowedImage = new Image(this.getClass().getResource("not_allowed.png").toExternalForm());

        createAccountBtn.hoverProperty().addListener((a, b, hovered) -> {
            if(hovered) {
                String password = passwordField.getText();
                String confirmedPassword = confirmPasswordField.getText();
                if(!passwordValidator.validatePassword(password, confirmedPassword)) {
                    createAccountBtn.setCursor(new ImageCursor(notAllowedImage));
                } else {
                    createAccountBtn.setCursor(null);
                }
            }
        });

    }


    private boolean validateAccountDetails() {
        boolean isValid;
        Window window = root.getScene().getWindow();

        String userName = userNameField.getText();
        if(userName.isBlank()) {
            userNameField.getTooltip().show(window);
            userNameField.requestFocus();
            return false;
        }

        AccountType<F> accountType = accountTypeSelector.getSelectionModel().getSelectedItem();
        if(accountType == null) {
            accountTypeSelector.getTooltip().show(window);
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
        PasswordStrength strength = passwordValidator.evaluateStrength(password);
        if (strength != STRONG) {
            MyNotifications.getInstance().showErrorAlert(
                    "كلمة المرور ليست قوية ما يكفي",
                    "يجب إضافة كلمة مرور قوية حسب الإرشادات لرفع مستوى الحماية");
            return false;
        }
        return true;
    }


    private boolean validatePasswordMatch(String password, String confirmed) {
        boolean match = passwordValidator.validatePassword(password, confirmed);
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
                .filter( accountType -> accountType.getAccountClass() == AccountType.Class.ADMIN).toList();

        accountTypeSelector.setItems(FXCollections.observableArrayList(adminAccountTypes));
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


    private void showPasswordStrength(String password) {
        PasswordStrength strength = passwordValidator.evaluateStrength(password);

        if (strength == null) {
            strengthLabel.setText("");
            strengthBar.setProgress(0);
            strengthBar.setStyle("");
            return;
        }

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

        strengthBar.setProgress(strength.getScore());
    }


    private void createAccount() {

        String userName = userNameField.getText();
        String password = passwordField.getText();
        AccountType userType = accountTypeSelector.getSelectionModel().getSelectedItem();

        createAccountBtn.setDisable(true);

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                String hashed = passwordValidator.hashPassword(password);
                Account<F> account = new Account<>(userName, hashed, userType);
                return accountDao.requestAccountApproval(account);
            }
        };

        masker.visibleProperty().bind(task.runningProperty());
        masker.setMouseTransparent(false);
        masker.toFront();

        task.setOnSucceeded(e -> {
            isAccountCreated = task.getValue();

            createAccountBtn.setDisable(false);
            masker.visibleProperty().unbind();

            if (isAccountCreated) {
                boolean oked = MyNotifications.getInstance().showConfirmAlert("نجاح", "تم إرسال طلب إنشاء الحساب إلى المشرفين. يمكنك تسجيل الدخول فور الموافقة عليه");
                if(oked) {
                    Stage theStage = (Stage) root.getScene().getWindow();
                    theStage.close();
                } else {
                    System.exit(0);
                }
            }
        });

        task.setOnFailed(e -> {
            createAccountBtn.setDisable(false);
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }


    void showCreateAdminAccountDialog() {
        Parent accountCreateRoot = FxmlHelper.loadFxml("create_account.fxml", this).getKey();

        Scene scene = new Scene(accountCreateRoot, 540, 440);
        scene.getStylesheets().add(MyThemes.vibrantBlue);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.showAndWait();

    }

}