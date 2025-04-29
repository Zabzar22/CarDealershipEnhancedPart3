package org.example

import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.chart.BarChart
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.scene.layout.Priority
import javafx.scene.layout.HBox
import java.util.ArrayList
import java.util.Optional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.io.File

class VehicleDataRow(private val vehicle: Vehicle) {
    fun getType(): String = vehicle.javaClass.simpleName
    fun getId(): String = vehicle.vehicleId ?: ""
    fun getManufacturer(): String = vehicle.manufacturer ?: ""
    fun getModel(): String = vehicle.model ?: ""
    fun getPrice(): String = String.format("$%.2f", vehicle.price)
    fun getStatus(): String {
        return when {
            !vehicle.isRentable() -> "SALE ONLY"
            vehicle.isRented -> "RENTED"
            else -> "AVAILABLE"
        }
    }
    fun getDealer(): String = vehicle.dealerId ?: ""
}

class DashboardView(
        private val manager: DealershipManager,
        private val onInventoryCleared: () -> Unit = {} // <-- Default to no-op
) : Dialog<Boolean>() {

    init {
        title = "Dealership Dashboard"
        headerText = "Vehicle Inventory Statistics"
        dialogPane.buttonTypes.add(ButtonType.CLOSE)
        dialogPane.prefWidth = 950.0
        dialogPane.prefHeight = 800.0
        val dashboardLayout = createDashboardLayout()
        val scrollPane = ScrollPane(dashboardLayout)
        scrollPane.isFitToWidth = true
        scrollPane.prefViewportHeight = 700.0
        scrollPane.prefViewportWidth = 900.0
        dialogPane.content = scrollPane
    }

    private fun createDashboardLayout(): VBox {
        val layout = VBox(20.0)
        layout.padding = Insets(20.0)
        layout.prefWidth = 900.0
        layout.prefHeight = 700.0

        val allVehicles = ArrayList(manager.getVehiclesForDisplay())
        val totalVehicles = allVehicles.size
        val rentedVehicles = allVehicles.count { it.isRented }
        val availableVehicles = totalVehicles - rentedVehicles
        val rentableVehicles = allVehicles.count { it.isRentable() }
        val availableForRent = rentableVehicles - rentedVehicles

        val vehiclesByType = mutableMapOf<String, Int>()
        for (vehicle in allVehicles) {
            val type = vehicle.javaClass.simpleName
            vehiclesByType[type] = vehiclesByType.getOrDefault(type, 0) + 1
        }

        val vehiclesByDealer = mutableMapOf<String?, MutableList<Vehicle>>()
        for (vehicle in allVehicles) {
            val dealerId = vehicle.dealerId
            if (!vehiclesByDealer.containsKey(dealerId)) {
                vehiclesByDealer[dealerId] = mutableListOf()
            }
            vehiclesByDealer[dealerId]?.add(vehicle)
        }

        val summaryLabel = Label("Inventory Summary")
        summaryLabel.style = "-fx-font-size: 18px; -fx-font-weight: bold;"
        val totalLabel = Label("Total Vehicles: $totalVehicles")
        totalLabel.style = "-fx-font-size: 14px;"
        val rentedLabel = Label("Currently Rented: $rentedVehicles")
        rentedLabel.style = "-fx-font-size: 14px;"
        val availableLabel = Label("Available for Sale: $availableVehicles")
        availableLabel.style = "-fx-font-size: 14px;"
        val availableForRentLabel = Label("Available for Rent: $availableForRent")
        availableForRentLabel.style = "-fx-font-size: 14px;"

        val clearInventoryButton = Button("Clear Inventory")
        clearInventoryButton.style = "-fx-background-color: #ff9999;"
        clearInventoryButton.setOnAction {
            showClearInventoryConfirmation(allVehicles.size)
        }

        val dealerLabel = Label("Vehicles by Dealer")
        dealerLabel.style = "-fx-font-size: 18px; -fx-font-weight: bold;"
        val dealersContainer = VBox(15.0)
        dealersContainer.prefHeight = 400.0
        dealersContainer.prefWidth = 850.0
        VBox.setVgrow(dealersContainer, Priority.ALWAYS)
        for ((dealerId, vehicles) in vehiclesByDealer) {
            val dealerPane = TitledPane()
            dealerPane.text = "Dealer ID: $dealerId"
            dealerPane.isExpanded = false
            dealerPane.isCollapsible = true
            dealerPane.prefWidth = 830.0
            val dealerInfo = VBox(10.0)
            dealerInfo.padding = Insets(10.0)
            dealerInfo.style = "-fx-background-color: #f0f8ff;"
            val vehicleCount = Label("Total Vehicles: ${vehicles.size}")
            vehicleCount.style = "-fx-font-weight: bold;"
            val rentedCount = Label("Rented Vehicles: ${vehicles.count { it.isRented }}")
            val dealerAvailableForRent = vehicles.count { !it.isRented && it.isRentable() }
            val availableCount = Label("Available Vehicles: ${vehicles.count { !it.isRented }}")
            val availableForRentCount = Label("Available for Rent: $dealerAvailableForRent")
            val vehicleRows = vehicles.map { VehicleDataRow(it) }
            val vehicleListView = createManualTable(vehicleRows)
            dealerInfo.children.addAll(
                    vehicleCount,
                    rentedCount,
                    availableCount,
                    availableForRentCount,
                    vehicleListView
            )
            dealerPane.content = dealerInfo
            dealersContainer.children.add(dealerPane)
        }

        val typeLabel = Label("Vehicles Types")
        typeLabel.style = "-fx-font-size: 18px; -fx-font-weight: bold;"
        val xAxis = CategoryAxis()
        val yAxis = NumberAxis()
        val typeChart = BarChart<String, Number>(xAxis, yAxis)
        typeChart.title = "Vehicles by Type"
        xAxis.label = "Vehicle Type"
        yAxis.label = "Count"
        val typeSeries = XYChart.Series<String, Number>()
        typeSeries.name = "Count"
        for ((type, count) in vehiclesByType) {
            typeSeries.data.add(XYChart.Data(type, count))
        }
        typeChart.data.add(typeSeries)
        typeChart.prefHeight = 250.0

        layout.children.addAll(
                summaryLabel,
                totalLabel,
                rentedLabel,
                availableLabel,
                availableForRentLabel,
                clearInventoryButton,
                Separator(),
                dealerLabel,
                dealersContainer,
                Separator(),
                typeLabel,
                typeChart
        )

        return layout
    }

    private fun showClearInventoryConfirmation(totalVehicles: Int) {
        val dialog = Dialog<ButtonType>()
        dialog.title = "Clear Entire Inventory"
        dialog.headerText = "⚠️ WARNING: You are about to delete ALL vehicles"
        val content = VBox(10.0)
        content.padding = Insets(20.0, 10.0, 10.0, 10.0)
        val warningLabel = Label("This will permanently remove ALL $totalVehicles vehicles from the system.")
        warningLabel.style = "-fx-font-weight: bold; -fx-text-fill: #cc0000;"
        val rentedWarning = Label("Any currently rented vehicles will also be removed, which may affect billing and customer records.")
        rentedWarning.style = "-fx-text-fill: #cc0000;"
        val createBackupCheckbox = CheckBox("Create backup before clearing (Recommended)")
        createBackupCheckbox.isSelected = true
        val confirmLabel = Label("Type \"CONFIRM DELETE ALL\" to proceed:")
        val confirmField = TextField()
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val timestampLabel = Label("Action timestamp: $timestamp")
        timestampLabel.style = "-fx-font-size: 10px;"
        content.children.addAll(
                warningLabel,
                rentedWarning,
                Separator(),
                createBackupCheckbox,
                confirmLabel,
                confirmField,
                timestampLabel
        )
        dialog.dialogPane.content = content
        dialog.dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)
        val okButton = dialog.dialogPane.lookupButton(ButtonType.OK)
        okButton.isDisable = true
        confirmField.textProperty().addListener { _, _, newValue ->
            okButton.isDisable = newValue != "CONFIRM DELETE ALL"
        }
        val result = dialog.showAndWait()
        // FIXED: Use === for ButtonType reference equality, and safe call for result
        if (result.orElse(null) === ButtonType.OK) {
            if (createBackupCheckbox.isSelected) {
                createInventoryBackup()
            }
            clearAllInventory()
            val successAlert = Alert(Alert.AlertType.INFORMATION)
            successAlert.title = "Inventory Cleared"
            successAlert.headerText = "Inventory Successfully Cleared"
            successAlert.contentText = "All $totalVehicles vehicles have been removed from the system."
            successAlert.showAndWait()
            dialogPane.content = ScrollPane(createDashboardLayout())
        }
    }

    private fun createInventoryBackup() {
        try {
            val timestamp = System.currentTimeMillis()
            val backupFile = File("src/main/resources/inventory_backup_$timestamp.json")
            val success = manager.exportInventoryBackup(backupFile)
            if (success) {
                val alert = Alert(Alert.AlertType.INFORMATION)
                alert.title = "Backup Created"
                alert.headerText = "Inventory Backup Created"
                alert.contentText = "A backup of your inventory has been created at: ${backupFile.absolutePath}"
                alert.showAndWait()
            } else {
                throw Exception("Backup operation failed")
            }
        } catch (e: Exception) {
            val alert = Alert(Alert.AlertType.ERROR)
            alert.title = "Backup Failed"
            alert.headerText = "Failed to Create Backup"
            alert.contentText = "Error: ${e.message}\n\nDo you still want to proceed with clearing inventory?"
            alert.buttonTypes.setAll(ButtonType.YES, ButtonType.NO)
            val result = alert.showAndWait()
            if (result.orElse(null) !== ButtonType.YES) {
                throw RuntimeException("Inventory clear canceled due to backup failure")
            }
        }
    }

    private fun clearAllInventory() {
        try {
            val inventoryFile = File("src/main/resources/inventory.json")
            val success = manager.clearAllInventory(inventoryFile)
            if (!success) {
                throw Exception("Failed to clear inventory")
            }
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            println("AUDIT: Inventory cleared at $timestamp")
            onInventoryCleared() // <-- This will refresh the display panel if a callback is passed
        } catch (e: Exception) {
            val alert = Alert(Alert.AlertType.ERROR)
            alert.title = "Clear Failed"
            alert.headerText = "Failed to Clear Inventory"
            alert.contentText = "Error: ${e.message}"
            alert.showAndWait()
            throw e
        }
    }

    private fun createManualTable(vehicleRows: List<VehicleDataRow>): VBox {
        val tableContainer = VBox()
        tableContainer.style = "-fx-border-color: #cccccc; -fx-border-width: 1px;"
        val headerRow = HBox()
        headerRow.style = "-fx-background-color: #e0e0e0; -fx-border-color: #bbbbbb; -fx-border-width: 0 0 1px 0;"
        headerRow.padding = Insets(5.0)
        val typeHeader = Label("Type")
        typeHeader.prefWidth = 120.0
        typeHeader.style = "-fx-font-weight: bold;"
        val idHeader = Label("ID")
        idHeader.prefWidth = 120.0
        idHeader.style = "-fx-font-weight: bold;"
        val manufacturerHeader = Label("Manufacturer")
        manufacturerHeader.prefWidth = 140.0
        manufacturerHeader.style = "-fx-font-weight: bold;"
        val modelHeader = Label("Model")
        modelHeader.prefWidth = 140.0
        modelHeader.style = "-fx-font-weight: bold;"
        val priceHeader = Label("Price")
        priceHeader.prefWidth = 120.0
        priceHeader.style = "-fx-font-weight: bold;"
        val statusHeader = Label("Status")
        statusHeader.prefWidth = 120.0
        statusHeader.style = "-fx-font-weight: bold;"
        headerRow.children.addAll(typeHeader, idHeader, manufacturerHeader, modelHeader, priceHeader, statusHeader)
        tableContainer.children.add(headerRow)
        for ((index, row) in vehicleRows.withIndex()) {
            val dataRow = HBox()
            dataRow.padding = Insets(5.0)
            if (index % 2 == 0) {
                dataRow.style = "-fx-background-color: #f5f5f5;"
            }
            val typeCell = Label(row.getType())
            typeCell.prefWidth = 120.0
            val idCell = Label(row.getId())
            idCell.prefWidth = 120.0
            val manufacturerCell = Label(row.getManufacturer())
            manufacturerCell.prefWidth = 140.0
            val modelCell = Label(row.getModel())
            modelCell.prefWidth = 140.0
            val priceCell = Label(row.getPrice())
            priceCell.prefWidth = 120.0
            val status = row.getStatus()
            val statusCell = Label(status)
            statusCell.prefWidth = 120.0
            when (status) {
                "RENTED" -> statusCell.style = "-fx-background-color: #d88c8c; -fx-text-fill: black; -fx-font-weight: bold;"
                "AVAILABLE" -> statusCell.style = "-fx-background-color: #99dd99; -fx-text-fill: black; -fx-font-weight: bold;"
                "SALE ONLY" -> statusCell.style = "-fx-background-color: #f0e68c; -fx-text-fill: black; -fx-font-weight: bold;"
            }
            dataRow.children.addAll(typeCell, idCell, manufacturerCell, modelCell, priceCell, statusCell)
            tableContainer.children.add(dataRow)
        }
        return tableContainer
    }

    // I added this companion object so my unit test can verify the dashboard's type counting logic.
    companion object {
        /**
         * Counts the number of vehicles by their type (class name).
         * This is used by my unit test to check type counting.
         */
        @JvmStatic
        fun countVehiclesByType(vehicles: List<Vehicle>): Map<String, Int> {
            // This groups the vehicles by their class name and counts them
            return vehicles.groupingBy { it.javaClass.simpleName }.eachCount()
        }
    }
}
