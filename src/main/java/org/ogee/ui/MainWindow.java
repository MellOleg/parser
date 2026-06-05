package org.ogee.ui;

import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.ogee.application.service.ParserService;
import org.ogee.infrastructure.http.dto.AuctionPriceDto;
import org.springframework.stereotype.Component;

@Component
public class MainWindow {

    private final ParserService parserService;

    private final TableView<AuctionPriceDto> tableView = new TableView<>();

    private final TextField regionField = new TextField("RU");
    private final Label itemNameLabel = new Label("Предмет: -");
    private final TextField itemIdField = new TextField("y1q9");
    private final TextField limitField = new TextField("20");
    private final TextField offsetField = new TextField("0");

    private final Label statusLabel = new Label("Готово");

    public MainWindow(ParserService parserService) {
        this.parserService = parserService;
    }

    public Parent createContent() {
        setupTable();

        Button loadButton = new Button("Загрузить историю");
        loadButton.setOnAction(event -> loadAuctionHistory());

        HBox inputPanel = new HBox(10);
        inputPanel.getChildren().addAll(
                new Label("Region:"), regionField,
                new Label("Item ID:"), itemIdField,
                new Label("Limit:"), limitField,
                new Label("Offset:"), offsetField,
                loadButton
        );

        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 15;");
        root.getChildren().addAll(
                inputPanel,
                itemNameLabel,
                tableView,
                statusLabel
        );

        return root;
    }

    private void setupTable() {
        TableColumn<AuctionPriceDto, Integer> amountColumn = new TableColumn<>("Количество");
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));

        TableColumn<AuctionPriceDto, Long> priceColumn = new TableColumn<>("Цена лота");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<AuctionPriceDto, Long> pricePerUnitColumn = new TableColumn<>("Цена за штуку");
        pricePerUnitColumn.setCellValueFactory(new PropertyValueFactory<>("pricePerUnit"));

        TableColumn<AuctionPriceDto, String> timeColumn = new TableColumn<>("Время");
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));

        tableView.getColumns().setAll(
                amountColumn,
                priceColumn,
                pricePerUnitColumn,
                timeColumn
        );

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    private void loadAuctionHistory() {
        try {
            String region = regionField.getText().trim();
            String itemId = itemIdField.getText().trim();
            String itemName = parserService.getItemName(itemId);
            itemNameLabel.setText("Предмет: " + itemName + " (" + itemId + ")");
            int limit = Integer.parseInt(limitField.getText().trim());
            int offset = Integer.parseInt(offsetField.getText().trim());

            var prices = parserService.getAuctionHistoryPrices(
                    region,
                    itemId,
                    limit,
                    offset
            );

            tableView.setItems(FXCollections.observableArrayList(prices));

            statusLabel.setText("Загружено записей: " + prices.size());

        } catch (NumberFormatException e) {
            statusLabel.setText("Ошибка: limit и offset должны быть числами");
        } catch (Exception e) {
            statusLabel.setText("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}