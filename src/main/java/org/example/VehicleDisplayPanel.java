package org.example.view;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.example.SportsCar;
import org.example.Vehicle;
import org.example.controller.DealershipController;
import java.util.Comparator;
import java.util.List;
import javafx.application.Platform;

public class VehicleDisplayPanel extends VBox {

    private DealershipController controller;
    private TableView<Vehicle> vehicleTable;
    private ObservableList<Vehicle> vehicleData = FXCollections.observableArrayList();
    private String currentDealerId = null; // Track current dealer for separator styling

    public VehicleDisplayPanel(DealershipController controller) {
        this.controller = controller;
        createComponents();

        // Register as inventory change listener so we always refresh when inventory changes
        controller.addInventoryChangeListener(() -> refreshDisplay());

        // This will be called after the component is added to the scene
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                setupSceneClickHandler(newScene);
            }
        });
    }

    private void setupSceneClickHandler(Scene scene) {
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            Node source = (Node) event.getTarget();
            boolean isButton = false;
            boolean isTable = false;
            Node current = source;
            while (current != null) {
                if (current instanceof Button) {
                    isButton = true;
                    break;
                }
                if (current == vehicleTable) {
                    isTable = true;
                    break;
                }
                current = current.getParent();
            }
            if (!isTable && !isButton) {
                clearSelection();
            }
        });
    }

    private void createComponents() {
        // Create table view
        vehicleTable = new TableView<>();
        vehicleTable.setEditable(false);
        vehicleTable.setPrefHeight(300);
        vehicleTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        vehicleTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        // Add specific class for CSS targeting
        vehicleTable.getStyleClass().add("vehicle-table");

        vehicleTable.setRowFactory(tv -> {
            TableRow<Vehicle> row = new TableRow<>() {
                @Override
                protected void updateItem(Vehicle vehicle, boolean empty) {
                    super.updateItem(vehicle, empty);
                    if (empty || vehicle == null) {
                        setStyle("");
                        return;
                    }
                    int index = getIndex();
                    if (index > 0) {
                        Vehicle prevVehicle = getTableView().getItems().get(index - 1);
                        String prevDealerId = prevVehicle.getDealerId();
                        String currentDealerId = vehicle.getDealerId();
                        if (!currentDealerId.equals(prevDealerId)) {
                            setStyle("-fx-border-color: #666666; -fx-border-width: 2 0 0 0;");
                        } else {
                            setStyle("");
                        }
                    } else {
                        setStyle("");
                    }
                }
            };
            return row;
        });

        TableColumn<Vehicle, String> dealerCol = new TableColumn<>("Dealer");
        dealerCol.setPrefWidth(120);
        dealerCol.setCellValueFactory(cellData -> {
            Vehicle vehicle = cellData.getValue();
            String dealerInfo = vehicle.getDealerId();
            if (vehicle.getMetadata().containsKey("dealer_name")) {
                dealerInfo += " (" + vehicle.getMetadata().get("dealer_name") + ")";
            }
            return new SimpleStringProperty(dealerInfo);
        });

        TableColumn<Vehicle, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getClass().getSimpleName()));

        TableColumn<Vehicle, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getVehicleId()));

        TableColumn<Vehicle, String> manufacturerCol = new TableColumn<>("Manufacturer");
        manufacturerCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getManufacturer()));

        TableColumn<Vehicle, String> modelCol = new TableColumn<>("Model");
        modelCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getModel()));

        TableColumn<Vehicle, Number> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getPrice()));
        priceCol.setCellFactory(col -> new TableCell<Vehicle, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item.doubleValue()));
                }
            }
        });

        TableColumn<Vehicle, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> {
            Vehicle vehicle = cellData.getValue();
            return new SimpleStringProperty(vehicle.getStatusDisplay());
        });
        statusCol.setCellFactory(col -> new TableCell<Vehicle, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("AVAILABLE")) {
                        setStyle("-fx-background-color: #79C97C; -fx-text-fill: black; -fx-font-weight: bold; -fx-padding: 2px 5px;");
                    } else if (item.equals("RENTED")) {
                        setStyle("-fx-background-color: #E08080; -fx-text-fill: black; -fx-font-weight: bold; -fx-padding: 2px 5px;");
                    } else {
                        setStyle("-fx-background-color: #E0B060; -fx-text-fill: black; -fx-font-weight: bold; -fx-padding: 2px 5px;");
                    }
                }
            }
        });

        vehicleTable.getColumns().addAll(
                dealerCol, typeCol, idCol, manufacturerCol, modelCol, priceCol, statusCol);

        vehicleTable.setItems(vehicleData);

        getChildren().add(vehicleTable);
    }

    public void refreshDisplay() {
        List<Vehicle> vehicles = controller.getVehiclesForDisplay();
        vehicles.sort(
                Comparator.comparing(Vehicle::getDealerId)
                        .thenComparing(v -> v.getClass().getSimpleName())
        );
        Platform.runLater(() -> {
            vehicleData.clear();
            vehicleData.addAll(vehicles);
            vehicleTable.refresh();
        });
    }

    public void search(String query, String searchType) {
        if (query == null || query.trim().isEmpty()) {
            refreshDisplay();
            return;
        }
        List<Vehicle> filteredVehicles = controller.searchVehicles(query, searchType);
        filteredVehicles.sort(
                Comparator.comparing(Vehicle::getDealerId)
                        .thenComparing(v -> v.getClass().getSimpleName())
        );
        Platform.runLater(() -> {
            vehicleData.clear();
            vehicleData.addAll(filteredVehicles);
            vehicleTable.refresh();
        });
    }

    public void appendMessage(String message) {
        DialogUtils.showInfo(message);
    }

    public Vehicle getSelectedVehicle() {
        return vehicleTable.getSelectionModel().getSelectedItem();
    }

    public void clearSelection() {
        vehicleTable.getSelectionModel().clearSelection();
    }
}
