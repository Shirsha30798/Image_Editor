package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class Controller {

    @FXML
    private BorderPane root;

    @FXML
    private Group imageLayer;


    @FXML
    private StackPane stackPane;

    @FXML
    private ImageView imageView;

    @FXML
    private Slider scaleSlider;

    @FXML
    private Slider brightnessSlider;

    @FXML
    private Slider saturationSlider;

    @FXML
    private Slider contrastSlider;

    @FXML
    private Slider hueSlider;

    @FXML
    private Button undoButton;

    @FXML
    private Button redoButton;

    @FXML
    private ToggleButton toggleCrop;

    private final Deque<ColorAdjust> undo = new ArrayDeque<>();
    private final Deque<ColorAdjust> redo = new ArrayDeque<>();

    RubberBandSelection rubberBandSelection;

    private final double defaultFitWidth = 780.0 ;
    private final double defaultFitHeight = 620.0;

    private final ColorAdjust colorAdjust = new ColorAdjust();

    Image currentImage;

    public void initialize() {

        imageView.setFitWidth(defaultFitWidth);
        imageView.setFitHeight(defaultFitHeight);

        scaleSlider.setDisable(true);
        brightnessSlider.setDisable(true);
        saturationSlider.setDisable(true);
        contrastSlider.setDisable(true);
        hueSlider.setDisable(true);
        undoButton.setDisable(true);
        redoButton.setDisable(true);
        toggleCrop.setDisable(true);

        rubberBandSelection = new RubberBandSelection(imageLayer);

        ContextMenu contextMenu = new ContextMenu();

        MenuItem cropMenuItem = new MenuItem("Crop");
        cropMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {

                // get bounds for image crop
                Bounds selectionBounds = rubberBandSelection.getBounds();

                // show bounds info
                System.out.println( "Selected area: " + selectionBounds);

                // crop the image
                crop( selectionBounds);

            }
        });
        contextMenu.getItems().add( cropMenuItem);


        imageLayer.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isSecondaryButtonDown()) {
                    contextMenu.show(imageLayer, event.getScreenX(), event.getScreenY());
                }
            }
        });

        toggleCrop.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                if (toggleCrop.isSelected()) {
                    imageView.setDisable(false);
                } else {
                    imageView.setDisable(true);
                }
            }
        });

        imageView.setDisable(true);

        scaleSlider.valueChangingProperty().addListener((observableValue, aBoolean, t1) -> zoom());

        brightnessSlider.valueChangingProperty().addListener((observableValue, aBoolean, t1) -> adjustBrightness());

        saturationSlider.valueChangingProperty().addListener((observableValue, aBoolean, t1) -> adjustSaturation());

        contrastSlider.valueChangingProperty().addListener((observableValue, aBoolean, t1) -> adjustContrast());

        hueSlider.valueChangingProperty().addListener((observableValue, aBoolean, t1) -> adjustHue());

        stackPane.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");


    }


    @FXML
    protected void openImage() {

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Image");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image", "*.jpg")
        );
        File file = chooser.showOpenDialog(root.getScene().getWindow());


        if (file != null) {
            String path;
            path = file.toURI().toString();
            Image image = new Image(path);
            this.currentImage = image;
            imageView.setImage(image);
            initializeImageViewDefaultProperties();
        } else {
            System.out.println("File chooser closed");
        }
    }

    @FXML
    protected void saveImage() throws IOException {

     imageView.setFitWidth(this.currentImage.getWidth());
     imageView.setFitHeight(this.currentImage.getHeight());

//     File output = new File("New.png");
//     ImageIO.write(SwingFXUtils.fromFXImage(imageView.snapshot(null, null), null), "png", output);

        WritableImage wi = new WritableImage( (int) currentImage.getWidth(),(int)currentImage.getHeight());



     imageView.setFitWidth(defaultFitWidth + scaleSlider.getValue());
     imageView.setFitHeight(defaultFitHeight + scaleSlider.getValue());

    }

    protected void initializeImageViewDefaultProperties() {
        scaleSlider.setDisable(false);
        brightnessSlider.setDisable(false);
        saturationSlider.setDisable(false);
        contrastSlider.setDisable(false);
        hueSlider.setDisable(false);
        toggleCrop.setDisable(false);
        scaleSlider.setValue(0);
        brightnessSlider.setValue(0);
        saturationSlider.setValue(0);
        contrastSlider.setValue(0);
        hueSlider.setValue(0);
        toggleCrop.setSelected(false);
    }

    protected void zoom() {
        imageView.setFitWidth(defaultFitWidth + (scaleSlider.getValue() * 5));
        imageView.setFitHeight(defaultFitHeight + (scaleSlider.getValue() * 5));
    }

    protected void adjustBrightness() {
        colorAdjust.setBrightness(brightnessSlider.getValue());
        imageView.setEffect(colorAdjust);
        saveColorAdjustment(colorAdjust);
    }

    protected void adjustSaturation() {
        colorAdjust.setSaturation(saturationSlider.getValue());
        imageView.setEffect(colorAdjust);
        saveColorAdjustment(colorAdjust);
    }

    protected void adjustContrast() {
        colorAdjust.setContrast(contrastSlider.getValue());
        imageView.setEffect(colorAdjust);
        saveColorAdjustment(colorAdjust);
    }

    protected void adjustHue() {
        colorAdjust.setHue(hueSlider.getValue());
        imageView.setEffect(colorAdjust);
        saveColorAdjustment(colorAdjust);
    }

    protected void saveColorAdjustment(ColorAdjust save) {
        ColorAdjust copy = new ColorAdjust(save.getHue(), save.getSaturation(), save.getBrightness(), save.getContrast());
        undo.addLast(copy);
        // clear any redo since we added a new undo
        redo.clear();
        undoButton.setDisable(false);
        redoButton.setDisable(true);
    }

    @FXML
    protected void undoColorAdjustment() {

        if (undo.size() > 1) {

            // move last undo (which is current applied adjustment) to redo
            redo.addFirst(undo.removeLast());
            // apply last color effect
            imageView.setEffect(undo.getLast());
            brightnessSlider.setValue(undo.getLast().getBrightness());
            saturationSlider.setValue(undo.getLast().getSaturation());
            contrastSlider.setValue(undo.getLast().getContrast());
            hueSlider.setValue(undo.getLast().getHue());
            redoButton.setDisable(false);
        } else {
            undoButton.setDisable(true);
        }
    }

    @FXML
    protected void redoColorAdjustment() {
        if (redo.size() > 0 ) {
            // move first redo to undo
            undo.addLast(redo.removeFirst());
            // apply last color effect
            imageView.setEffect(undo.getLast());
            brightnessSlider.setValue(undo.getLast().getBrightness());
            saturationSlider.setValue(undo.getLast().getSaturation());
            contrastSlider.setValue(undo.getLast().getContrast());
            hueSlider.setValue(undo.getLast().getHue());
            undoButton.setDisable(false);
        } else {
            redoButton.setDisable(true);
        }
    }

    protected void crop( Bounds bounds) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");

        File file = fileChooser.showSaveDialog(root.getScene().getWindow());
        if (file == null)
            return;

        double imageToImageViewWidthRatio =  (currentImage.getWidth()/imageView.getFitWidth());
        double imageToImageViewHeightRatio =  (currentImage.getHeight()/imageView.getFitHeight());

        int width = (int) (bounds.getWidth() * imageToImageViewWidthRatio) ;
        int height = (int)(bounds.getHeight() * imageToImageViewHeightRatio);

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        parameters.setViewport(new Rectangle2D( bounds.getMinX() * imageToImageViewWidthRatio , bounds.getMinY() * imageToImageViewHeightRatio , width, height));

        WritableImage wi = new WritableImage( width, height);
        imageView.setFitWidth(currentImage.getWidth());
        imageView.setFitHeight(currentImage.getHeight());
        imageView.snapshot(parameters, wi);
        imageView.setFitWidth(defaultFitWidth + scaleSlider.getValue());
        imageView.setFitHeight(defaultFitHeight + scaleSlider.getValue());


        // save image (without alpha)
        // --------------------------------
        BufferedImage bufImageARGB = SwingFXUtils.fromFXImage(wi, null);
        BufferedImage bufImageRGB = new BufferedImage(bufImageARGB.getWidth(), bufImageARGB.getHeight(), BufferedImage.OPAQUE);

        Graphics2D graphics = bufImageRGB.createGraphics();
        graphics.drawImage(bufImageARGB, 0, 0, null);

        try {

            ImageIO.write(bufImageRGB, "jpg", file);

            System.out.println( "Image saved to " + file.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
        }

        graphics.dispose();

    }

}
