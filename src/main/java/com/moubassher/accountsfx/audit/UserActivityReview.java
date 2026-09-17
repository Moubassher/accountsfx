package com.moubassher.accountsfx.audit;

import com.context.my_javafx_utilities.DateTimeFormats;
import com.moubassher.accountsfx.AccountDao;
import com.moubassher.accountsfx.Feature;
import com.moubassher.search.SearchBar;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.control.MaskerPane;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class UserActivityReview<F extends Enum<F> & Feature<F>> implements Initializable {

    @FXML
    private VBox actionContainer;

    @FXML
    private SearchBar<UserActivity<F>> userActivitySearchbar;
    @FXML
    private MaskerPane maskerPane;

    private AccountDao<F> accountDao;
    private Class<F> featureEnum;
    public UserActivityReview(AccountDao<F> accountDao, Class<F> featureEnum) {
        this.accountDao = accountDao;
        this.featureEnum = featureEnum;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a"));
        
        maskerPane.setMouseTransparent(false);

        Task<List<UserActivity<F>>> activityLoadingTask = new Task<>() {
            @Override
            protected List<UserActivity<F>> call() {
                List<UserActivity<F>> userActivity= accountDao.getAllUserActivity(featureEnum);
                return userActivity;
            }
        };

        //maskerPane.visibleProperty().bind(activityLoadingTask.runningProperty());
        activityLoadingTask.setOnSucceeded(success -> {

            List<UserActivity<F>> userActivity = activityLoadingTask.getValue();
          //  maskerPane.visibleProperty().unbind();

            userActivitySearchbar.initialize(userActivity, results -> {
                showUserActivity(results);
            });
            showUserActivity(userActivity);
        });

        new Thread(activityLoadingTask).start();;

    }

    private void showUserActivity(List<UserActivity<F>> userActivity) {
        actionContainer.getChildren().clear();
        for(int i = 0; i < userActivity.size(); i++) {
            var activity = userActivity.get(i);
            HBox actionRow = createActionRow(activity);
            if(i % 2 ==0) {
                actionRow.getStyleClass().add("even-row");
            } else {
                actionRow.getStyleClass().add("odd-row");
            }
            actionContainer.getChildren().add(actionRow);
        }
    }

 private DateTimeFormatter amPm = DateTimeFormats.am_pmFormat.localizedBy(Locale.forLanguageTag("AR"));
    private DateTimeFormatter hourFormat = DateTimeFormatter.ofPattern("h:mm");
    private HBox createActionRow(UserActivity<F> activity) {
        HBox actionRow = new HBox(10);
        String action = activity.getAction().name();
        String userName = activity.getUserName();
        String accountTypeName = activity.getAccountType().toString();
        String accountClass = activity.getAccountClass();
        LocalDateTime timestamp = activity.getTimestamp();

        actionRow.getChildren().addAll(new Label("المستخدم"),
                new Label(userName),
                new Label("بنوع حساب: " + accountTypeName),
                new Label("من فئة: " + accountClass),
                new Label( "قام بـ"+ action),
                new Label("في الساعة: "),
                new Label(timestamp.format(hourFormat)),
                new Label(timestamp.format(amPm)),
                new Label("يوم: "),
                new Label(timestamp.toLocalDate().toString()));

        return actionRow;
    }
}
