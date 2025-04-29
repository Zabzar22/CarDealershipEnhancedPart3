package org.example

import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import javafx.scene.layout.HBox
import java.util.Date
import java.util.LinkedHashSet

// VehicleAddWizard.kt
// This is my vehicle addition wizard. I added a companion object with isValidPrice so I can test price validation in my unit test.

class VehicleAddWizard(private val controller: DealershipControllerAdapter) : Dialog<Boolean>() {
    // Current step tracker
    private var currentStep = 1
    private val totalSteps = 3
    // Input fields
    private val dealerIdField = TextField()
    private val dealerIdComboBox = ComboBox<String>()
    private val vehicleTypeComboBox = ComboBox<String>()
    private val vehicleIdField = TextField()
    private val manufacturerField = TextField()
    private val modelField = TextField()
    private val priceField = TextField()
    // Content panes for each step
    private val step1Content = createStep1Content()
    private val step2Content = createStep2Content()
    private val step3Content = createStep3Content()
    // Navigation buttons
    private val backButton = Button("Back")
    private val nextButton = Button("Next")
    private val finishButton = Button("Finish")

    init {
        title = "Add New Vehicle"
        headerText = "Step 1: Select Dealership"
        dialogPane.buttonTypes.addAll(ButtonType.CANCEL)
        dialogPane.content = step1Content
        updateNavigationButtons()
        val buttonBox = HBox(10.0)
        buttonBox.alignment = Pos.CENTER_RIGHT
        buttonBox.padding = Insets(10.0)
        buttonBox.children.addAll(backButton, nextButton, finishButton)
        val contentVBox = VBox()
        contentVBox.children.add(dialogPane.content)
        contentVBox.children.add(buttonBox)
        dialogPane.content = contentVBox

        backButton.setOnAction { goToPreviousStep() }
        nextButton.setOnAction { goToNextStep() }
        finishButton.setOnAction { finishWizard() }

        // NEW: Refresh dealer ComboBox every time the wizard is shown
        setOnShowing {
            refreshDealerComboBox()
        }

        setResultConverter { buttonType ->
            buttonType == ButtonType.OK || buttonType == ButtonType.FINISH
        }
    }

    // NEW: Method to refresh the dealer ComboBox from the controller's current dealer list
    private fun refreshDealerComboBox() {
        val dealerList = FXCollections.observableArrayList<String>()
        dealerList.add("-- New Dealer --")
        val existingDealers = controller.getDealerList()
        if (existingDealers != null) {
            for (dealer in existingDealers) {
                if (dealer != "-- New Dealer --") {
                    dealerList.add(dealer.toString())
                }
            }
        }
        dealerIdComboBox.items = dealerList
        // If there are no dealers, ensure only "-- New Dealer --" is selected
        if (dealerList.size == 1) {
            dealerIdComboBox.value = "-- New Dealer --"
            dealerIdField.isDisable = false
        } else {
            dealerIdComboBox.value = dealerList[1] // Select first actual dealer by default
            dealerIdField.isDisable = true
        }
    }

    private fun createStep1Content(): GridPane {
        val grid = GridPane()
        grid.padding = Insets(20.0)
        grid.hgap = 10.0
        grid.vgap = 10.0
        val dealerSelectionBox = HBox(10.0)
        dealerSelectionBox.children.addAll(dealerIdComboBox, dealerIdField)
        dealerIdComboBox.setOnAction {
            if (dealerIdComboBox.value != null) {
                if (dealerIdComboBox.value == "-- New Dealer --") {
                    dealerIdField.clear()
                    dealerIdField.isDisable = false
                } else {
                    dealerIdField.clear()
                    dealerIdField.isDisable = true
                }
            } else {
                dealerIdField.isDisable = false
            }
        }
        dealerIdField.textProperty().addListener { _, _, newValue ->
            if (newValue != null && newValue.isNotEmpty()) {
                dealerIdComboBox.value = null
            }
        }
        dealerIdField.prefWidth = 120.0
        dealerIdComboBox.prefWidth = 120.0
        grid.add(Label("Dealer ID:"), 0, 0)
        grid.add(dealerSelectionBox, 1, 0)
        return grid
    }

    private fun createStep2Content(): GridPane {
        val grid = GridPane()
        grid.padding = Insets(20.0)
        grid.hgap = 10.0
        grid.vgap = 10.0
        vehicleTypeComboBox.items = FXCollections.observableArrayList(
                "SUV", "Sedan", "Pickup", "Sports Car"
        )
        vehicleTypeComboBox.value = "SUV"
        vehicleTypeComboBox.prefWidth = 250.0
        vehicleIdField.prefWidth = 250.0
        grid.add(Label("Vehicle Type:"), 0, 0)
        grid.add(vehicleTypeComboBox, 1, 0)
        grid.add(Label("Vehicle ID:"), 0, 1)
        grid.add(vehicleIdField, 1, 1)
        return grid
    }

    private fun createStep3Content(): GridPane {
        val grid = GridPane()
        grid.padding = Insets(20.0)
        grid.hgap = 10.0
        grid.vgap = 10.0
        manufacturerField.prefWidth = 250.0
        modelField.prefWidth = 250.0
        priceField.prefWidth = 250.0
        grid.add(Label("Manufacturer:"), 0, 0)
        grid.add(manufacturerField, 1, 0)
        grid.add(Label("Model:"), 0, 1)
        grid.add(modelField, 1, 1)
        grid.add(Label("Price:"), 0, 2)
        grid.add(priceField, 1, 2)
        return grid
    }

    private fun goToPreviousStep() {
        if (currentStep > 1) {
            currentStep--
            updateStepContent()
        }
    }

    private fun goToNextStep() {
        if (validateCurrentStep()) {
            if (currentStep < totalSteps) {
                currentStep++
                updateStepContent()
            }
        }
    }

    private fun updateStepContent() {
        val contentContainer = dialogPane.content as VBox
        val currentContent = when (currentStep) {
            1 -> {
                headerText = "Step 1: Select Dealership"
                step1Content
            }
            2 -> {
                headerText = "Step 2: Choose Vehicle Type"
                step2Content
            }
            3 -> {
                headerText = "Step 3: Enter Vehicle Details"
                step3Content
            }
            else -> step1Content
        }
        contentContainer.children[0] = currentContent
        updateNavigationButtons()
    }

    private fun updateNavigationButtons() {
        backButton.isDisable = currentStep == 1
        nextButton.isVisible = currentStep < totalSteps
        finishButton.isVisible = currentStep == totalSteps
    }

    private fun validateCurrentStep(): Boolean {
        when (currentStep) {
            1 -> {
                val dealerId = getDealerId()
                if (dealerId.isEmpty()) {
                    showError("Dealer ID is required")
                    return false
                }
                if (!controller.isAcquisitionEnabled(dealerId)) {
                    showError("Cannot add vehicle: Acquisition is disabled for dealer $dealerId")
                    return false
                }
            }
            2 -> {
                if (vehicleTypeComboBox.value == null) {
                    showError("Vehicle type is required")
                    return false
                }
                if (vehicleIdField.text.isNullOrBlank()) {
                    showError("Vehicle ID is required")
                    return false
                }
            }
            3 -> {
                if (manufacturerField.text.isNullOrBlank()) {
                    showError("Manufacturer is required")
                    return false
                }
                if (modelField.text.isNullOrBlank()) {
                    showError("Model is required")
                    return false
                }
                if (priceField.text.isNullOrBlank()) {
                    showError("Price is required")
                    return false
                }
                try {
                    val price = priceField.text.toDouble()
                    if (price <= 0) {
                        showError("Price must be greater than 0")
                        return false
                    }
                } catch (e: NumberFormatException) {
                    showError("Invalid price format")
                    return false
                }
            }
        }
        return true
    }

    private fun showError(message: String) {
        val alert = Alert(Alert.AlertType.ERROR)
        alert.title = "Validation Error"
        alert.headerText = null
        alert.contentText = message
        alert.showAndWait()
    }

    private fun getDealerId(): String {
        return if (dealerIdComboBox.value != null &&
                dealerIdComboBox.value.isNotEmpty() &&
                dealerIdComboBox.value != "-- New Dealer --") {
            dealerIdComboBox.value
        } else {
            dealerIdField.text.trim()
        }
    }

    private fun createVehicleFromType(): Vehicle {
        return when (vehicleTypeComboBox.value) {
            "SUV" -> SUV()
            "Sedan" -> Sedan()
            "Pickup" -> Pickup()
            "Sports Car" -> SportsCar()
            else -> SUV()
        }
    }

    private fun finishWizard() {
        if (validateCurrentStep()) {
            try {
                val dealerId = getDealerId()
                val vehicle = createVehicleFromType()
                vehicle.setVehicleId(vehicleIdField.text.trim())
                vehicle.setManufacturer(manufacturerField.text.trim())
                vehicle.setModel(modelField.text.trim())
                vehicle.setPrice(priceField.text.trim().toDouble())
                vehicle.setAcquisitionDate(Date())
                vehicle.setDealerId(dealerId)
                val success = controller.addVehicle(vehicle, dealerId)
                if (success) {
                    setResult(true)
                    close()
                } else {
                    setResult(false)
                }
            } catch (ex: Exception) {
                showError("Error adding vehicle: ${ex.message}")
                setResult(false)
            }
        }
    }

    companion object {
        /**
         * Checks if the input string is a valid, non-negative price.
         * I added this so my unit test can verify the price validation logic.
         */
        @JvmStatic
        fun isValidPrice(input: String?): Boolean {
            // Checks if the input is a non-negative number
            return try {
                val price = input?.toDoubleOrNull()
                price != null && price >= 0
            } catch (e: Exception) {
                false
            }
        }
    }
}
