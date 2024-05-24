package main.lab6a;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.sql.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BirthDateController {
    private final List<BirthDay> birthDays = new ArrayList<>();
    private Connection connection;
    @FXML
    public TextField lastNameField;
    @FXML
    public TextField firstNameField;
    @FXML
    public TextField phoneField;
    @FXML
    public DatePicker datePicker;
    @FXML
    public ScrollPane scrollPane;
    @FXML
    public VBox vbox;

    @FXML
    private void postBirthDate() {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String phone = phoneField.getText();

        LocalDate date = datePicker.getValue();

        if (isValidText("Lastname", lastName)
                && isValidText("Firstname", firstName)
                && isValidPhone(phone)
                && isValidDate(date)) {
            BirthDay birthDay = new BirthDay(firstName, lastName, phone, date);

            birthDays.add(birthDay);
            reloadScrollPane();
        }
    }

    @FXML
    protected void filterByFirstName() {
        showTextInputDialog("firstname");
    }

    @FXML
    protected void filterByLastName() {
        showTextInputDialog("lastname");
    }

    @FXML
    protected void filterByMonth() {
        showTextInputDialog("month");
    }

    public void insertPaymentsIntoDB() {
        connectToDatabase();

        String query = "INSERT INTO BirthDates (firstName, lastName, phone, date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement params = connection.prepareStatement(query)) {
            for (BirthDay birthDay : birthDays) {
                params.setString(1, birthDay.getFirstName());
                params.setString(2, birthDay.getLastName());
                params.setString(3, birthDay.getPhone());
                params.setDate(4, Date.valueOf(LocalDate.of(birthDay.getYear(), birthDay.getMonth(), birthDay.getDay())));
                params.executeUpdate();
            }

            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadPaymentsFromDB() {
        connectToDatabase();
        vbox.getChildren().clear();
        String query = "SELECT * FROM BirthDates";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                birthDays.add(new BirthDay(rs.getString("firstName"), rs.getString("lastName"), rs.getString("phone"), rs.getDate("date").toLocalDate()));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        reloadScrollPane();
    }

    public void deleteBirthDatesFromDB(){
        connectToDatabase();
        vbox.getChildren().clear();
        String query = "DELETE FROM BirthDates";
        try {
            Statement stmt = connection.createStatement();
            stmt.execute(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        reloadScrollPane();
    }

    private void connectToDatabase() {
        String connectionUrl = "jdbc:sqlserver://DESKTOP-FNGUF28\\MSSQLSERVER_2022:50099;database=BirthDates;user=sa;password=1;trustServerCertificate=true";
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            connection = DriverManager.getConnection(connectionUrl);
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void reloadScrollPane() {
        vbox.getChildren().clear();
        for (BirthDay birthDay : birthDays) {
            vbox.getChildren().add(new Label(birthDay.toString()));
        }
        scrollPane.setContent(vbox);
    }

    private void showWarningDialog(String text) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Incorrect input");
        alert.setHeaderText(null);
        alert.setContentText(text);

        alert.showAndWait();
    }

    private boolean isValidText(String Field, String input) {
        if (input.isEmpty()) {
            showWarningDialog(STR."\{Field} cannot be empty");
            return false;
        } else if (!input.matches("[a-zA-Z]+")) {
            showWarningDialog(STR."\{Field} is in wrong format");
            return false;
        } else {
            return true;
        }
    }

    private boolean isValidPhone(String input) {
        if (input.isEmpty()) {
            showWarningDialog("Phone number cannot be empty");
            return false;
        } else if (!input.matches("^(\\d{3}[- .]?){2}\\d{4}$")) {
            showWarningDialog("Incorrect phone number format");
            return false;
        } else {
            return true;
        }
    }

    private boolean isValidDate(LocalDate date) {
        try {
            Period diff = Period.between(date, LocalDate.now());
            if (diff.getDays() >= 0 && diff.getYears() < 100) {
                return true;
            } else {
                showWarningDialog("Date should be not older that 100 years and not in future");
                return false;
            }
        } catch (Exception ex) {
            showWarningDialog("Date in wrong format");
            return false;
        }
    }

    private void showTextInputDialog(String filed) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(STR."Input \{filed}");
        dialog.setHeaderText("Enter value");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(input -> {

            if(input.isEmpty()){
                reloadScrollPane();
                return;
            }

            switch (filed) {
                case "month":
                    try {
                        int month = Integer.parseInt(input);

                        if (month >= 1 && month <= 12) {
                            vbox.getChildren().clear();
                            for (BirthDay birthDay : birthDays) {
                                if (birthDay.getMonth() == month)
                                    vbox.getChildren().add(new Label(birthDay.toString()));
                            }
                            scrollPane.setContent(vbox);
                        } else {
                            showWarningDialog("Month should be in diapason 1-12 and not empty");
                        }

                    } catch (Exception _) {
                        reloadScrollPane();
                    }
                    break;
                case "firstname":
                    try {
                        vbox.getChildren().clear();
                        for (BirthDay birthDay : birthDays) {
                            if (birthDay.getFirstName().equals(input))
                                vbox.getChildren().add(new Label(birthDay.toString()));
                        }
                        scrollPane.setContent(vbox);
                    } catch (Exception _) {
                        reloadScrollPane();
                    }
                    break;
                case "lastname":
                    try {
                        vbox.getChildren().clear();
                        for (BirthDay birthDay : birthDays) {
                            if (birthDay.getLastName().equals(input))
                                vbox.getChildren().add(new Label(birthDay.toString()));
                        }
                        scrollPane.setContent(vbox);
                    } catch (Exception _) {
                        reloadScrollPane();
                    }
                    break;
            }
        });
    }
}
