package com.moubassher.accountsfx;

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
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.Files.writeString;

public class Pharmacy  {

	private String name, city, address;
	private int phone1, phone2;

	public String getName() {
		return name;
	}

	public void showPharmacyInfo() {
		Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);

		VBox box = pharmacyInfoPane();
		Scene scene = new Scene(box, 600, 600);
		stage.setScene(scene);
		stage.show();
	}

	private JSONObject pharmacyInfo = null;

	private void readPharmacyInfo() {

		pharmacyInfo = new JSONObject();

		File pharmacyFile = new File("C:pharmaNetResources//pharmacyDetails.json");
		try {
			String pharmacyInfoString= Files.readString(pharmacyFile.toPath());
			pharmacyInfo = new JSONObject(pharmacyInfoString);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public VBox pharmacyInfoPane() {

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
			registerPharmacy(pharmacyNameTF, addressTF, cityCB, phone1TF, phone2TF);
		});


		VBox box = new VBox(20);
		box.setPadding(new Insets(30));
		box.getChildren().addAll(pharmacyNamelbl, pharmacyNameTF, addresslbl, addressTF, citylbl, cityCB
				,phoneNolbl, phoneBox, register);
		return box;
	}

	private void registerPharmacy(TextField pharmacyNameTF, TextField addressTF, ChoiceBox<String> cityCB,
								  TextField phone1TF, TextField phone2TF) {
		String pharmacy = pharmacyNameTF.getText();
		String address = addressTF.getText();
		String city = cityCB.getSelectionModel().getSelectedItem();
		String phone1 = phone1TF.getText();
		String phone2 = phone2TF.getText();

		JSONObject pharmacyJson =  new JSONObject();

		pharmacyJson.put("pharmacyName", pharmacy);
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

	private static Pharmacy instance = null;

	private Pharmacy() {
		readPharmacyInfo();
	}

	public static Pharmacy getInstance() {
		if (instance == null) {
			instance = new Pharmacy();
		}
		return instance;
	}
}
