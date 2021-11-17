package com.example.finalwindowapp

import javafx.application.Platform
import javafx.embed.swing.SwingFXUtils
import javafx.fxml.FXML
import javafx.geometry.Rectangle2D
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.CheckBox
import javafx.scene.control.ColorPicker
import javafx.scene.control.Slider
import javafx.scene.image.ImageView
import javafx.scene.image.WritableImage
import javafx.scene.image.WritablePixelFormat
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.robot.Robot
import javafx.scene.shape.ArcType
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeLineCap
import javafx.scene.shape.StrokeLineJoin
import javafx.stage.FileChooser
import javafx.stage.Screen
import javafx.stage.Stage
import java.util.*
import javax.imageio.ImageIO
import kotlin.concurrent.schedule
import javafx.event.EventHandler

class HelloController {
    var stage: Stage? = null
    private var minimizeFlag: Boolean = false
    private var robot: Robot = Robot()
    private lateinit var graphContext: GraphicsContext
    @FXML
    private lateinit var hBox: HBox
    @FXML
    private lateinit var window: GridPane
    @FXML
    private lateinit var pane: AnchorPane
    @FXML
    private lateinit var minWindow: CheckBox
    @FXML
    private lateinit var imageView: ImageView
    @FXML
    private lateinit var canvas: Canvas
    @FXML
    private lateinit var colorPicker: ColorPicker
    @FXML
    private lateinit var cursorWidth: Slider
    @FXML
    private lateinit var checkbox: CheckBox


    fun initialize() {
        graphContext = canvas.graphicsContext2D
        graphContext.fill = colorPicker.value
        graphContext.lineJoin = StrokeLineJoin.ROUND
        graphContext.lineCap = StrokeLineCap.ROUND
        eventHandler()
    }

    private var clickAndPressHandler = EventHandler { event: MouseEvent ->
        canvas.graphicsContext2D.fillArc(event.x - cursorWidth.value / 2, event.y - cursorWidth.value / 2,
            cursorWidth.value, cursorWidth.value, 0.0, 360.0, ArcType.OPEN)
        graphContext.moveTo(event.x, event.y)
        graphContext.beginPath()
    }

    private var dragHandler = EventHandler { event: MouseEvent ->
        graphContext.lineTo(event.x, event.y)
        graphContext.lineWidth = cursorWidth.value
        graphContext.stroke = colorPicker.value
        graphContext.stroke()
    }

    private var releaseHandler = EventHandler { _: MouseEvent ->
        graphContext.closePath()
    }

    private var eraseHandler = EventHandler { event: MouseEvent ->
        graphContext.clearRect(event.x - cursorWidth.value / 2, event.y - cursorWidth.value / 2,
            cursorWidth.value, cursorWidth.value)
    }

    fun fitToWindow() {
        window.prefWidthProperty().bind(stage?.widthProperty())
        window.prefHeightProperty().bind(stage?.heightProperty())
        pane.prefWidthProperty().bind(window.prefWidthProperty())
        hBox.prefWidthProperty().bind(pane.prefWidthProperty())
    }

    @FXML
    fun eventHandler() {
        if (checkbox.isSelected) {
            canvas.onMouseClicked = eraseHandler
            canvas.onMousePressed = eraseHandler
            canvas.onMouseDragged = eraseHandler
            canvas.onMouseReleased = eraseHandler
        } else {
            canvas.onMouseClicked = clickAndPressHandler
            canvas.onMousePressed = clickAndPressHandler
            canvas.onMouseDragged = dragHandler
            canvas.onMouseReleased = releaseHandler
        }
    }

    @FXML
    private fun minWindowToggle() {
        minimizeFlag = minWindow.isSelected
    }

    @FXML
    fun screenshotButtonPressed() {
        if (minimizeFlag) {
            stage?.isIconified = true
        }
        Timer().schedule(500) {
            Platform.runLater {
                val screen = Screen.getPrimary().bounds
                val screenImg: WritableImage = robot.getScreenCapture(null, Rectangle2D(0.0, 0.0, screen.width, screen.height))
                val rect: Rectangle2D = ScreenshotWindow().start(screenImg)
                val drawingImg = WritableImage(rect.width.toInt(), rect.height.toInt())
                val buffer = ByteArray(rect.width.toInt() * rect.height.toInt() * 4)
                val format = WritablePixelFormat.getByteBgraInstance()
                screenImg.pixelReader.getPixels(
                    rect.minX.toInt(), rect.minY.toInt(), rect.width.toInt(),
                    rect.height.toInt(), format, buffer, 0, rect.width.toInt() * 4)
                drawingImg.pixelWriter.setPixels(
                    0, 0, rect.width.toInt(), rect.height.toInt(),
                    format, buffer, 0, rect.width.toInt() * 4)

                imageView.image = drawingImg
                imageView.fitWidth = drawingImg.width
                imageView.fitHeight = drawingImg.height
                canvas.widthProperty().bind(imageView.fitWidthProperty())
                canvas.heightProperty().bind(imageView.fitHeightProperty())

                stage?.isIconified = false
            }
        }
    }

    @FXML
    fun colorPicked() {
        canvas.graphicsContext2D.fill = colorPicker.value
    }

    @FXML
    fun openImage() {
        val fileChooser: FileChooser = FileChooser()
        fileChooser.extensionFilters.addAll()
        val file = fileChooser.showOpenDialog(stage)
        val newImg = SwingFXUtils.toFXImage(ImageIO.read(file.inputStream()), null)

        imageView.image = newImg
        imageView.fitWidth = newImg.width
        imageView.fitHeight = newImg.height
        canvas.widthProperty().bind(imageView.fitWidthProperty())
        canvas.heightProperty().bind(imageView.fitHeightProperty())
    }

    @FXML
    fun programExit() {
        Platform.exit()
    }

}

class ScreenshotWindow {
    lateinit var stage: Stage
    lateinit var layout: Pane
    var rectangle: Rectangle = Rectangle(0.0,0.0,0.0,0.0)
    var x = 0.0
    var y = 0.0
    var edgeX = 0.0
    var edgeY = 0.0

    fun start(img: WritableImage): Rectangle2D {
        stage = Stage()
        stage.height= Screen.getPrimary().bounds.height
        stage.width= Screen.getPrimary().bounds.width

        layout = Pane()
        layout.children.add(ImageView(img))
        layout.children.add(rectangle)
        layout.prefWidth = Screen.getPrimary().bounds.width
        layout.prefHeight = Screen.getPrimary().bounds.height
        val scene = Scene(layout)
        stage.scene = scene

        layout.onMousePressed = EventHandler{ event: MouseEvent ->
            x = event.screenX
            y = event.screenY
        }

        layout.onMouseReleased = EventHandler{ event: MouseEvent ->
            edgeX = event.screenX
            edgeY = event.screenY
            stage.hide()
        }

        stage.showAndWait()
        return Rectangle2D(x, y, edgeX - x, edgeY - y)
    }
}