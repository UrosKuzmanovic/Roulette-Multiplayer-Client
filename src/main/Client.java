package main;

import com.jfoenix.controls.JFXButton;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.json.*;
import services.FieldInfo;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.*;

public class Client extends Application implements Initializable, Serializable {

    private static BufferedReader serverInput;
    private static PrintStream serverOutput;

    private static Thread thread;
    private double balance = 1000;
    private List<Integer> previousNumbers = new ArrayList<>();
    private static String username;

    @FXML
    private Label betValueLabel;
    @FXML
    private Label balanceValue;
    @FXML
    private Label timer;
    @FXML
    private Label number;
    @FXML
    private TextArea prNums;
    @FXML
    private TextArea onlineUsers;
    @FXML
    private Label label0;
    @FXML
    private Label label1;
    @FXML
    private Label label2;
    @FXML
    private Label label3;
    @FXML
    private Label label4;
    @FXML
    private Label label5;
    @FXML
    private Label label6;
    @FXML
    private Label label7;
    @FXML
    private Label label8;
    @FXML
    private Label label9;
    @FXML
    private Label label10;
    @FXML
    private Label label11;
    @FXML
    private Label label12;
    @FXML
    private Label label13;
    @FXML
    private Label label14;
    @FXML
    private Label label15;
    @FXML
    private Label label16;
    @FXML
    private Label label17;
    @FXML
    private Label label18;
    @FXML
    private Label label19;
    @FXML
    private Label label20;
    @FXML
    private Label label21;
    @FXML
    private Label label22;
    @FXML
    private Label label23;
    @FXML
    private Label label24;
    @FXML
    private Label label25;
    @FXML
    private Label label26;
    @FXML
    private Label label27;
    @FXML
    private Label label28;
    @FXML
    private Label label29;
    @FXML
    private Label label30;
    @FXML
    private Label label31;
    @FXML
    private Label label32;
    @FXML
    private Label label33;
    @FXML
    private Label label34;
    @FXML
    private Label label35;
    @FXML
    private Label label36;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("../resources/view/sample.fxml"));
        primaryStage.setTitle("Roulette");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(new Image("resources/images/roulette.png"));
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Kraj");
        JSONObject json = new JSONObject();
        json.put("type", 6);
        serverOutput.println(createJSONString(json));
        serverOutput.close();
        serverInput.close();
        System.exit(0);
    }

    public static void main(String[] args) {
        Socket socket;
        try {
            //socket = new Socket("192.168.0.55", 8000);
            socket = new Socket("localhost", 8000);
            System.out.println("Connected!");

            serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            serverOutput = new PrintStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Connection error");
        }
        launch(args);
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        thread = new Thread(this::threadTest);
        thread.start();
    }

    private void threadTest() {
        Platform.runLater(() -> {
            usernameDialog();
        });

        boolean testBoolean = true;
        while (true) {
            String jsonString = null;
            try {
                jsonString = serverInput.readLine();
            } catch (IOException e) {
            }
            if (jsonString != null) {
                JSONObject json = toJSON(jsonString);
                int type = json.getInt("type");

                if (type == 0) { // vreme
                    String value = json.getString("value");
                    String[] time = value.split(":");
                    int min = Integer.parseInt(time[0]);
                    int sec = Integer.parseInt(time[1]);
                    if (min != 0 || sec != 0) {
                        Platform.runLater(() -> {
                            number.getStyleClass().clear();
                            number.getStyleClass().add("selected-number");
                            number.setText("Place your bets!");
                            if (sec < 10)
                                timer.setText("0" + min + ":0" + sec);
                            else timer.setText("0" + min + ":" + sec);
                        });
                        testBoolean = true;
                    }
                    if (min == 0 && sec == 0 && testBoolean) {
                        Platform.runLater(() -> {
                            timer.setText("0" + min + ":0" + sec);
                            number.setText("Selecting number...");
                        });
                        testBoolean = false;
                    }
                } else if (type == 1) { // broj
                    int num = Integer.parseInt(json.getString("value"));
                    previousNumbers.add(num);
                    if (previousNumbers.size() > 24)
                        previousNumbers.remove(0);
                    updateList();
                    Platform.runLater(() -> {
                        number.setText(num + "");
                    });
                    if (isRed(num)) {
                        number.getStyleClass().clear();
                        number.getStyleClass().add("selected-number-red");
                    } else if (!isRed(num) && num != 0) {
                        number.getStyleClass().clear();
                        number.getStyleClass().add("selected-number-black");
                    } else {
                        number.getStyleClass().clear();
                        number.getStyleClass().add("selected-number-green");
                    }
                } else if (type == 2) { // profit
                    double profit = Double.parseDouble(json.getString("value"));
                    System.out.println("Profit: " + profit);
                    balance += profit;
                    double balanceRound = (Math.round(balance) * 100.0) / 100.0;
                    Platform.runLater(() -> {
                        balanceValue.setText("Balance: " + balanceRound + " RSD");
                        resetBets();
                    });
                } else if (type == 3) { // users
                    String users = json.getString("value");
                    onlineUsers.setText(users);
                }
            }
        }
    }

    public void increaseBet() {
        int betValue = Integer.parseInt(betValueLabel.getText()) + 100;
        if (betValue > 10000)
            betValue = 10000;
        betValueLabel.setText(betValue + "");
    }

    public void decreaseBet() {
        int betValue = Integer.parseInt(betValueLabel.getText()) - 100;
        if (betValue < 100)
            betValue = 100;
        betValueLabel.setText(betValue + "");
    }

    public void clickBetListener(ActionEvent e) {
        JFXButton button = (JFXButton) e.getSource();
        int number = Integer.parseInt(button.getId());
        Label[] labels = betLabelList();
        int bet = Integer.parseInt(betValueLabel.getText());
        balance = balance - bet;

        FieldInfo field = new FieldInfo(number, bet);
        List<FieldInfo> list = new ArrayList<>();
        list.add(field);

        if (balance >= 0) {
            if (number >= 0 && number <= 36) { // brojevi 0-36
                placeBet(labels, bet, number, 1);
            } else if (number == 211) { // prva kolona brojeva
                for (int i = 1; i < 37; i++) {
                    if (i == 1 || (i - 1) % 3 == 0) {
                        placeBet(labels, bet, i, 12);
                    }
                }
            } else if (number == 212) { // druga kolona brojeva
                for (int i = 1; i < 37; i++) {
                    if (i == 2 || (i - 2) % 3 == 0) {
                        placeBet(labels, bet, i, 12);
                    }
                }
            } else if (number == 213) { // treca kolona brojeva
                for (int i = 1; i < 37; i++) {
                    if (i == 3 || (i - 3) % 3 == 0) {
                        placeBet(labels, bet, i, 12);
                    }
                }
            } else if (number == 121) { // brojevi 1-12
                for (int i = 1; i < 13; i++) {
                    placeBet(labels, bet, i, 12);
                }
            } else if (number == 122) { // brojevi 13-24
                for (int i = 13; i < 25; i++) {
                    placeBet(labels, bet, i, 12);
                }
            } else if (number == 123) { // brojevi 25-36
                for (int i = 25; i < 37; i++) {
                    placeBet(labels, bet, i, 12);
                }
            } else if (number == 118) { // brojevi 1-18
                for (int i = 1; i < 19; i++) {
                    placeBet(labels, bet, i, 18);
                }
            } else if (number == 1936) { // brojevi 19-36
                for (int i = 19; i < 37; i++) {
                    placeBet(labels, bet, i, 18);
                }
            } else if (number == 40) { // parni brojevi
                for (int i = 1; i < 37; i++) {
                    if (i % 2 == 0) {
                        placeBet(labels, bet, i, 18);
                    }
                }
            } else if (number == 41) { // neparni brojevi
                for (int i = 1; i < 37; i++) {
                    if (i % 2 == 1) {
                        placeBet(labels, bet, i, 18);
                    }
                }
            } else if (number == 42) { // crveno
                for (int i = 0; i < 37; i++) {
                    if (isRed(i)) {
                        placeBet(labels, bet, i, 18);
                    }
                }
            } else if (number == 43) { // crno
                for (int i = 0; i < 37; i++) {
                    if (!isRed(i) && i != 0) {
                        placeBet(labels, bet, i, 18);
                    }
                }
            } else if (number == 50) { // mala
                for (int i = 0; i < 37; i++) {
                    if (isThird(i)) {
                        placeBet(labels, bet, i, 12);
                    }
                }
            } else if (number == 51) { // nula
                for (int i = 0; i < 37; i++) {
                    if (isZero(i)) {
                        placeBet(labels, bet, i, 17);
                    }
                }
            } else if (number == 52) { // orfa
                for (int i = 0; i < 37; i++) {
                    if (isOrph(i)) {
                        placeBet(labels, bet, i, 8);
                    }
                }
            }
        } else {
            balance = 0;
            alertBox("You don't have enough credits.");
        }
    }

    private static void alertBox(String poruka) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Roulette info");
        alert.setHeaderText(null);
        alert.setContentText(poruka);
        alert.showAndWait();
    }

    private static void usernameDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Roulette");
        dialog.setHeaderText("");
        dialog.setContentText("Please enter your nickname:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            username = result.get();
        } else {
            username = "*****";
        }
        System.out.println(username);
        JSONObject json = new JSONObject();
        json.put("type", 5);
        json.put("value", username);
        serverOutput.println(createJSONString(json));
    }

    public Label[] betLabelList() {
        Label[] list = new Label[37];
        list[0] = label0;
        list[1] = label1;
        list[2] = label2;
        list[3] = label3;
        list[4] = label4;
        list[5] = label5;
        list[6] = label6;
        list[7] = label7;
        list[8] = label8;
        list[9] = label9;
        list[10] = label10;
        list[11] = label11;
        list[12] = label12;
        list[13] = label13;
        list[14] = label14;
        list[15] = label15;
        list[16] = label16;
        list[17] = label17;
        list[18] = label18;
        list[19] = label19;
        list[20] = label20;
        list[21] = label21;
        list[22] = label22;
        list[23] = label23;
        list[24] = label24;
        list[25] = label25;
        list[26] = label26;
        list[27] = label27;
        list[28] = label28;
        list[29] = label29;
        list[30] = label30;
        list[31] = label31;
        list[32] = label32;
        list[33] = label33;
        list[34] = label34;
        list[35] = label35;
        list[36] = label36;
        return list;
    }

    public double valueOfBet(String labelText) {
        String number = "";
        int i;
        for (i = 0; i < labelText.length(); i++) {
            if (labelText.charAt(i) == ':') {
                i += 2;
                break;
            }
        }
        for (int j = i; j < labelText.length(); j++) {
            while (labelText.charAt(i) != ' ') {
                number = number + labelText.charAt(i);
                i++;
            }
        }
        return Double.parseDouble(number);
    }

    private void placeBet(Label[] labels, double bet, int i, int nums) {
        double left = Math.round((valueOfBet(labels[i].getText()) + (bet / nums)) * 100.0) / 100.0;
        double balanceRound = Math.round(balance * 100.0) / 100.0;
        labels[i].setText(i + ": " + left + " RSD");
        balanceValue.setText("Balance: " + balanceRound + " RSD");
        JSONObject json = new JSONObject();
        json.put("type", 4);
        json.put("value", i + ":" + (bet / nums));
        String jsonString = createJSONString(json);
        serverOutput.println(jsonString);
    }

    private boolean isRed(int i) {
        if (i == 1 || i == 3 || i == 5 || i == 7 || i == 9 || i == 12 || i == 14 || i == 16 || i == 18
                || i == 19 || i == 21 || i == 23 || i == 25 || i == 27 || i == 30 || i == 32 || i == 34 || i == 36)
            return true;
        return false;
    }

    private boolean isOrph(int i) {
        if (i == 1 || i == 20 || i == 14 || i == 31 || i == 9 || i == 6 || i == 34 || i == 17)
            return true;
        return false;
    }

    private boolean isThird(int i) {
        if (i == 27 || i == 13 || i == 36 || i == 11 || i == 30 || i == 8 || i == 23 || i == 10 || i == 5
                || i == 24 || i == 16 || i == 33)
            return true;
        return false;
    }

    private boolean isZero(int i) {
        if (i == 22 || i == 18 || i == 29 || i == 7 || i == 28 || i == 12 || i == 35 || i == 3 || i == 26
                || i == 0 || i == 32 || i == 15 || i == 19 || i == 4 || i == 21 || i == 2 || i == 25)
            return true;
        return false;
    }

    private void resetBets() {
        Label[] list = betLabelList();
        for (int i = 0; i < list.length; i++) {
            list[i].setText(i + ": 0 RSD");
        }
    }

    private void updateList() {
        String nums = "";
        for (int i = 0; i < previousNumbers.size(); i++) {
            if (i == 0)
                nums += previousNumbers.get(i);
            else
                nums = nums + "   " + previousNumbers.get(i);
        }
        prNums.setText(nums);
    }

    private static String createJSONString(JSONObject json) {
        return json.toString();
    }

    private JSONObject toJSON(String string) {
        JSONObject json = new JSONObject(string);
        return json;
    }
}
