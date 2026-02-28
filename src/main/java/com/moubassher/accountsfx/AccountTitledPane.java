package com.moubassher.accountsfx;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.FlowPane;

import java.util.List;

public class AccountTitledPane <F extends Enum<F> & Feature<F>>  extends TitledPane {


    private ObjectProperty<AccountType<F>> accountType = new SimpleObjectProperty<>();

    /**
     * Creates a new TitledPane with no title or content.
     */
    public AccountTitledPane() {
        ChoiceBox<AccountType.Class> accountClassSelector = new ChoiceBox<>(FXCollections.observableArrayList(AccountType.Class.values()));
        this.setGraphic(accountClassSelector);

        FlowPane featurePane = new FlowPane(10, 10);
        featurePane.getStyleClass().add("root");
        this.setContent(featurePane);

        accountType.addListener((a, b, accountType) -> {
            String name = accountType.getTypeName();
            this.setText(name);
            AccountType.Class accountClass = accountType.getAccountClass();
            accountClassSelector.getSelectionModel().select(accountClass);

            List<F> selectedPrivileges = accountType.getPrivileges();

            selectedPrivileges.forEach(selectedFeature -> {
                CheckBox featureBox = new CheckBox(selectedFeature.name());
                featureBox.setSelected(true);
                featurePane.getChildren().add(featureBox);
            });


        });
    }


    public AccountType<F> getAccountType() {
        return accountType.get();
    }

    public void setAccountType(AccountType<F> accountType) {
        this.accountType.set(accountType);
    }
}
