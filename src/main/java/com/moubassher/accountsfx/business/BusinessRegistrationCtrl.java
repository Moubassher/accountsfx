package com.moubassher.accountsfx.business;

import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;

import static java.nio.file.Files.writeString;

public class BusinessRegistrationCtrl implements Initializable {

	private BusinessAddress businessAddress;

	private BusinessType businessType;
	private String appName;

	public BusinessRegistrationCtrl(String appName, BusinessType businessType) {
		this.appName = appName;
		this.businessType = businessType;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}

	public void showEstablishmentInfo() {
		Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);

		VBox box = establishmentInfoPane();
		Scene scene = new Scene(box, 600, 600);
		stage.setScene(scene);
		stage.show();
	}


	private void readEstablishmentType() {

		JSONObject establishmentInfo = new JSONObject();
/*

		File pharmacyFile = new File("C:pharmaNetResources//pharmacyDetails.json");
		try {
			String pharmacyInfoString= Files.readString(pharmacyFile.toPath());
			establishmentInfo = new JSONObject(pharmacyInfoString);
		} catch (IOException e) {
			e.printStackTrace();
		}
*/
	}

	public VBox establishmentInfoPane() {

		String id = "pharmacyInfo";

		Label pharmacyNamelbl = new Label("اسم الصيدلية");
		pharmacyNamelbl.setId(id);
		TextField pharmacyNameTF = new TextField();
		pharmacyNameTF.setMaxWidth(170);

		Label addresslbl = new Label("العنوان");
		addresslbl.setId(id);
		TextField addressTF = new TextField();
		addressTF.setMaxWidth(300);

		Label citylbl = new Label("المدينة");
		citylbl.setId(id);

		ChoiceBox<String> cityCB = new ChoiceBox<>();
		cityCB.getItems().addAll("الخرطوم", "بحري", "أمدرمان");

		Label phoneNolbl = new Label("أرقام الهواتف");
		phoneNolbl.setId(id);
		String phonePrompt = "رقم الهاتف";
		TextField phone1TF = new TextField();
		phone1TF.setPromptText(phonePrompt);
		TextField phone2TF = new TextField();
		phone2TF.setPromptText(phonePrompt);

		HBox phoneBox = new HBox(10);
		phoneBox.getChildren().addAll(phone1TF, phone2TF);

		Button register = new Button("تسجيل");
		register.setOnAction(x -> {
			registerBusiness(pharmacyNameTF, addressTF, cityCB, phone1TF, phone2TF);
		});

		VBox box = new VBox(20);
		box.setPadding(new Insets(30));
		box.getChildren().addAll(pharmacyNamelbl, pharmacyNameTF, addresslbl, addressTF, citylbl, cityCB
				,phoneNolbl, phoneBox, register);
		return box;
	}

	private void registerBusiness(TextField businessNameField, TextField addressTF, ChoiceBox<String> cityCB,
								  TextField phone1TF, TextField phone2TF) {
		String pharmacy = businessNameField.getText();
		String address = addressTF.getText();
		String city = cityCB.getSelectionModel().getSelectedItem();
		String phone1 = phone1TF.getText();
		String phone2 = phone2TF.getText();

		JSONObject pharmacyJson =  new JSONObject();

		pharmacyJson.put("businessName", pharmacy);
		pharmacyJson.put("address", address);
		pharmacyJson.put("city", city);
		pharmacyJson.put("phone1", phone1);
		pharmacyJson.put("phone2", phone2);

		File file = new File("C:pharmaNetResources//pharmacyDetails.json");

		boolean exportSuccess = false;
		try {
			Path writePath = writeString(file.toPath(), pharmacyJson.toString(2));
			exportSuccess = (writePath != null);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if(exportSuccess) {
			String notification = "تم تسجيل بيانات الصيدلية بنجاح." + "\n";
			notification += "يمكنك الآن اجراء طلبات من المورّدين المسجلين، كما يمكن استقبال العروض منهم." + "\n";
			//UP add "know more" button showcasing what it means for a retailer to be registered.
			//TODO: show a toast for successfully registering a retailer.
			//MyNotifications.getInstance().showSimpleToast("نجاح!", notification);
		}
	}

	private static BusinessRegistrationCtrl instance = null;

	private BusinessRegistrationCtrl() {
		readEstablishmentType();
	}

	public static BusinessRegistrationCtrl getInstance() {
		if (instance == null) {
			instance = new BusinessRegistrationCtrl();
		}
		return instance;
	}

}