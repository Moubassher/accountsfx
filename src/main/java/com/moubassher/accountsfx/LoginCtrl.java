package com.moubassher.accountsfx;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.context.our_utilities.MyNotifications;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.MaskerPane;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * A controller for the login window. The user
 */
public class LoginCtrl<F extends Enum<F> & Feature<F>>  implements Initializable {

	TaskDatabase taskDatabase = TaskDatabase.getInstance();

	@FXML
	private Label signInDisplay, credsInfoDisplay, accountTypeDisplay, userNameDisplay;

	@FXML
	private ChoiceBox<AccountType<F>> accountTypeSelector;

	@FXML
	private ChoiceBox<Account<F>> registeredUsersSelector;

	@FXML
	private PasswordField passwordField;

	@FXML
	private Button loginBtn, createAccountBtn;

	@FXML
	private Hyperlink forgotPasswordLink;

	@FXML
	private StackPane loginRootPane;

	@FXML
	private MaskerPane masker;

	private boolean isUserManager;

	private ValidationSupport validationSupport;

	private AccountDao<F> accountDao;
	private Account<F> loggedInUser;

	private AccountsFX<F> accountsFX;
	public LoginCtrl(AccountDao<F> accountDao, AccountsFX<F> accountsFX) {
		this.accountDao = accountDao;
		this.accountsFX = accountsFX;
	}


	@Override
	public void initialize(URL location, ResourceBundle resources) {

		String title;
		if(isUserManager) {
			title = "تسجيل الدخول (الإدارة)";
		} else {
			title = "تسجيل الدخول (حساب)";
		}
		signInDisplay.setText(title);

		accountTypeSelector.getItems().setAll(accountDao.readAccountTypes());

		onAccountTypeSelected(accountType -> getAccountsForType(accountType.getTypeName()));

		credsInfoDisplay.setId("error");
		if(isUserManager) {
			credsInfoDisplay.setText("تسجيل دخول للإدارة");
		}

		validationSupport = new ValidationSupport();
		validationSupport.registerValidator(passwordField,
				Validator.createEmptyValidator("يجب إدخال كلمة السر"));


		loginBtn.setOnAction(x -> login());

		forgotPasswordLink.setOnAction(x -> {
			sendPasswordResetRequest();
		});

		createAccountBtn.setOnAction(x -> {
			String description = "تم إرسال الطلب للإدارة";
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setContentText(description);
			alert.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
			ButtonType okBtn = new ButtonType("موافق");
			alert.getButtonTypes().setAll(List.of(okBtn));
			alert.showAndWait().ifPresent(result -> {
				if( result == okBtn) {
					//navigator.toLoginScreen(this);
				}
			});
		});

	}


	private void onAccountTypeSelected(Consumer<AccountType> accountTypeSelectedFunc) {
		accountTypeSelector.getSelectionModel().selectedItemProperty()
				.addListener((a, b, selectedDept) -> {
					accountTypeSelectedFunc.accept(selectedDept);
				});
	}

	private void getAccountsForType(String accountType) {
		List<Account<F>> allAccounts = accountDao.readAccounts();
		List<Account<F>> filteredAccounts = allAccounts.stream()
				.filter(account ->
						account.getAccountType().getTypeName().equals(accountType))
				.toList();
		if(filteredAccounts.isEmpty()) {
			MyNotifications.getInstance().showConfirmAlert("لا يوجد حساب لهذه الفئة", "اختر فئة حسابات أخرى أو قم بمراجعة الإدراة لإنشاء حساب لهذه الفئة");
			accountTypeSelector.getSelectionModel().clearSelection();
			return;
		}
		registeredUsersSelector.setItems(FXCollections.observableList(filteredAccounts));
	}


	public void enforceAccountClass(AccountType.Class accountClass) {

		AccountType<F> maintainerType = accountTypeSelector.getItems()
				.stream()
				.filter(type -> type.getAccountClass().equals(accountClass))
				.findFirst()
				.orElse(null);

		if (maintainerType != null) {
			accountTypeSelector.getSelectionModel().select(maintainerType);
			accountTypeSelector.setDisable(true);

			// ensure accounts list is populated immediately
			getAccountsForType(maintainerType.getTypeName());
		}
	}

	private void login() {
		Account<F> account = registeredUsersSelector.getSelectionModel().getSelectedItem();
		if (account == null) {
			return;
		}

		String password = passwordField.getText();

		Validator<String> emptyValidator =
				Validator.createEmptyValidator("يجب إدخال كلمة السر");

		validationSupport.registerValidator(passwordField, emptyValidator);

		if (validationSupport.isInvalid()) {
			return; // Stop if field is empty
		}

		masker.setMouseTransparent(false);

		Task<Boolean> task = new Task<>() {
			@Override
			protected Boolean call() {
				boolean passwordMatches =
						BCrypt.verifyer().verify(password.toCharArray(), account.getHashedPassword()).verified;

				if (!passwordMatches) {
					return false; // Invalid password
				}

				return accountDao.loginAccount(account);
			}
		};

		loginBtn.disableProperty().bind(task.runningProperty());
		masker.visibleProperty().bind(task.runningProperty());

		task.setOnSucceeded(e -> {
			boolean success = task.getValue();

			loginBtn.disableProperty().unbind();
			masker.visibleProperty().unbind();
			if (success) {
				loggedInUser = account;
				accountsFX.showMainWindow();
			} else {
				MyNotifications.getInstance().showErrorAlert("كلمة السر غير صحيحة", "حاول مرة أخرى أو أرسل طلب تغيير كلمة السر من أحد مستخدمي الحسابات الإدارية");
			}
		});

		task.setOnFailed(e -> {
			task.getException().printStackTrace();
			loginBtn.disableProperty().unbind();
		});

		new Thread(task).start();
	}

	public Account<F> getLoggedInUser() {
		return loggedInUser;
	}


	public static void trialVersionEnded() {

		String msg =""" 
		عزيزنا المشترك،
		لقد انتهت الفترة التجريبية لاستخدام برنامج المحاسب الذهبي. 
		نرجو ان تكون قد حظيت بتجربة سلسة في استخدامك لمزايا البرنامج
		للمتابعة، يرجى تجديد الاشتراك
		""";
		Alert alert = new Alert(Alert.AlertType.INFORMATION);

		alert.setTitle("المحاسب الذهبي");
		alert.setHeaderText("لقد انتهت الفترة التجريبية");
		alert.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
		alert.setContentText(msg);

		alert.showAndWait().ifPresent(result -> {
			if(result == ButtonType.OK) {
				System.exit(0);
			}
		});

	}


	@FXML
	private void sendPasswordResetRequest() {
		if(loggedInUser == null) {
			MyNotifications.getInstance()
					.showConfirmAlert("اختر اسم المستخدم الذي تود إرسال طلب تغيير كلمة السر من أجله", "");
		} else {
			boolean done = taskDatabase.addTask("طلب إعادة ضبط كلمة سر", "for " + loggedInUser.getName());
			if (done) {
				//TODO implement all possible ways to reset a password.
			/* 1. Send the reset request to the server (to be received by an admin).
				2. Send reset request to local admin, requiring a login with an admin account.
			 */
				MyNotifications.getInstance().showMessage("تم", "تم إرسال طلب للإدارة لإعادة ضبط كلمة السر إلى 123. وبعدما تتم إعادة الضبط. ادخل إلى حسابك باستخدام 123 (مرة واحدة)، ومن ثم قم بتغييرها من القائمة الرئيسية.");
			}
		}
	}
}
//original line count: 276