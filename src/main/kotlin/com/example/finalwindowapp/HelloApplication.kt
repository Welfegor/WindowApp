package com.example.finalwindowapp

import javafx.application.Application
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.stage.Stage

class HelloApplication : Application() {
    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(HelloApplication::class.java.getResource("hello-view.fxml"))
        val scene = Scene(fxmlLoader.load(), 1280.0, 720.0)
        fxmlLoader.getController<HelloController>().stage = stage
        fxmlLoader.getController<HelloController>().fitToWindow()
        stage.title = "WindowScreenApp"
        stage.scene = scene

        stage.scene.accelerators[KeyCodeCombination(KeyCode.Q)] = Runnable{
            Platform.exit()
        }
        stage.scene.accelerators[KeyCodeCombination(KeyCode.C)] = Runnable{
            fxmlLoader.getController<HelloController>().screenshotButtonPressed()
        }
        stage.scene.accelerators[KeyCodeCombination(KeyCode.O)] = Runnable{
            fxmlLoader.getController<HelloController>().openImage()
        }
        stage.show()
    }
}

fun main() {
    Application.launch(HelloApplication::class.java)
}