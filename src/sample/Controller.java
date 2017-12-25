package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private ImageView Hello;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
            /*
            File file = new File("SampleFile1.jpg");
            Image image = new Image(file.toURI().toString());
            Hello.setImage(image);
            */

    }

}
