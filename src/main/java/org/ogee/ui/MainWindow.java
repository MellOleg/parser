package org.ogee.ui;

import javafx.application.Platform;
import org.ogee.application.service.MarketScanResult;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.ogee.application.service.MarketScannerService;
import org.ogee.ui.model.MarketOpportunityRow;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Component
public class MainWindow {

    private final MarketScannerService marketScannerService;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> monitoringTask;
    private volatile boolean scanInProgress = false;

    private final TextField regionField = new TextField("RU");
    private final TextField lotsLimitField = new TextField("5");
    private final TextField historyDepthField = new TextField("20");
    private final TextField minProfitPercentField = new TextField("10");
    private final TextField minTotalProfitField = new TextField("10000");
    private final TextField scanIntervalField = new TextField("10");

    private final Button scanButton = new Button("Сканировать");
    private final Button startMonitoringButton = new Button("Старт мониторинга");
    private final Button stopMonitoringButton = new Button("Стоп");

    private final Label watchlistStatusLabel = new Label("Watchlist: -");
    private final Label lastUpdateLabel = new Label("Last update: -");
    private final Label statusLabel = new Label("Status: готово");

    private final TableView<MarketOpportunityRow> tableView = new TableView<>();

    public MainWindow(MarketScannerService marketScannerService) {
        this.marketScannerService = marketScannerService;
    }

    public Parent createContent() {
        configureInputFields();
        configureActions();
        configureTable();

        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 15;");
        root.getChildren().addAll(
                createTitle(),
                createSettingsPanel(),
                createButtonsPanel(),
                createStatusPanel(),
                tableView
        );

        return root;
    }

    private Label createTitle() {
        Label title = new Label("STALCRAFT Market Scanner");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        return title;
    }

    private void configureInputFields() {
        regionField.setPrefWidth(70);
        lotsLimitField.setPrefWidth(70);
        historyDepthField.setPrefWidth(80);
        minProfitPercentField.setPrefWidth(90);
        minTotalProfitField.setPrefWidth(110);
        scanIntervalField.setPrefWidth(90);
    }

    private HBox createSettingsPanel() {
        HBox settingsPanel = new HBox(10);

        settingsPanel.getChildren().addAll(
                new Label("Region:"), regionField,
                new Label("Lots limit:"), lotsLimitField,
                new Label("History depth:"), historyDepthField,
                new Label("Min profit %:"), minProfitPercentField,
                new Label("Min total profit:"), minTotalProfitField,
                new Label("Interval sec:"), scanIntervalField
        );

        return settingsPanel;
    }

    private HBox createButtonsPanel() {
        HBox buttonsPanel = new HBox(10);

        stopMonitoringButton.setDisable(true);

        buttonsPanel.getChildren().addAll(
                scanButton,
                startMonitoringButton,
                stopMonitoringButton
        );

        return buttonsPanel;
    }

    private HBox createStatusPanel() {
        HBox statusPanel = new HBox(20);

        statusPanel.getChildren().addAll(
                watchlistStatusLabel,
                lastUpdateLabel,
                statusLabel
        );

        return statusPanel;
    }
    private String formatLong(long value) {
        return String.format("%,d", value).replace(",", " ");
    }

    private void configureActions() {
        scanButton.setOnAction(event -> scanMarketOnce());

        startMonitoringButton.setOnAction(event -> startMonitoring());

        stopMonitoringButton.setOnAction(event -> stopMonitoring());
    }
    private void startMonitoring() {
        try {
            int intervalSeconds = Integer.parseInt(scanIntervalField.getText().trim());

            if (intervalSeconds <= 0) {
                statusLabel.setText("Status: interval должен быть больше 0");
                return;
            }

            if (monitoringTask != null && !monitoringTask.isCancelled()) {
                statusLabel.setText("Status: мониторинг уже запущен");
                return;
            }

            scanButton.setDisable(true);
            startMonitoringButton.setDisable(true);
            stopMonitoringButton.setDisable(false);

            statusLabel.setText("Status: мониторинг запущен, interval=" + intervalSeconds + " sec");

            monitoringTask = scheduler.scheduleAtFixedRate(
                    this::runMonitoringScanSafely,
                    0,
                    intervalSeconds,
                    TimeUnit.SECONDS
            );

        } catch (NumberFormatException e) {
            statusLabel.setText("Status: interval должен быть числом");
        }
    }

    private void stopMonitoring() {
        if (monitoringTask != null) {
            monitoringTask.cancel(false);
            monitoringTask = null;
        }

        scanButton.setDisable(false);
        startMonitoringButton.setDisable(false);
        stopMonitoringButton.setDisable(true);

        statusLabel.setText("Status: мониторинг остановлен");
    }
    private void runMonitoringScanSafely() {
        if (scanInProgress) {
            Platform.runLater(() ->
                    statusLabel.setText("Status: предыдущий скан ещё выполняется, пропуск цикла")
            );
            return;
        }

        scanInProgress = true;

        try {
            var result = runScan();

            Platform.runLater(() -> applyScanResult(result));

        } catch (Exception e) {
            Platform.runLater(() -> {
                statusLabel.setText("Status: ошибка мониторинга — " + e.getMessage());
                e.printStackTrace();
            });
        } finally {
            scanInProgress = false;
        }
    }

    private void configureTable() {
        TableColumn<MarketOpportunityRow, String> itemNameColumn = new TableColumn<>("Предмет");
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));

        TableColumn<MarketOpportunityRow, String> itemIdColumn = new TableColumn<>("ID");
        itemIdColumn.setCellValueFactory(new PropertyValueFactory<>("itemId"));

        TableColumn<MarketOpportunityRow, Integer> amountColumn = new TableColumn<>("Кол-во");
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));

        TableColumn<MarketOpportunityRow, Long> lotPriceColumn = new TableColumn<>("Цена лота");
        lotPriceColumn.setCellValueFactory(new PropertyValueFactory<>("lotPrice"));

        TableColumn<MarketOpportunityRow, Long> pricePerUnitColumn = new TableColumn<>("Цена/шт");
        pricePerUnitColumn.setCellValueFactory(new PropertyValueFactory<>("pricePerUnit"));

        TableColumn<MarketOpportunityRow, Long> marketPriceColumn = new TableColumn<>("Рынок/шт");
        marketPriceColumn.setCellValueFactory(new PropertyValueFactory<>("marketPricePerUnit"));

        TableColumn<MarketOpportunityRow, Double> profitPercentColumn = new TableColumn<>("Профит %");
        profitPercentColumn.setCellValueFactory(new PropertyValueFactory<>("profitPercent"));

        TableColumn<MarketOpportunityRow, Long> estimatedProfitColumn = new TableColumn<>("Профит общий");
        estimatedProfitColumn.setCellValueFactory(new PropertyValueFactory<>("estimatedProfit"));

        TableColumn<MarketOpportunityRow, String> timeColumn = new TableColumn<>("Время");
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));

        lotPriceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Long value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(formatLong(value));
                }
            }
        });

        pricePerUnitColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Long value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(formatLong(value));
                }
            }
        });

        marketPriceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Long value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(formatLong(value));
                }
            }
        });

        estimatedProfitColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Long value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(formatLong(value));
                }
            }
        });
        profitPercentColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f%%", value));
                }
            }
        });

        tableView.getColumns().setAll(
                itemNameColumn,
                itemIdColumn,
                amountColumn,
                lotPriceColumn,
                pricePerUnitColumn,
                marketPriceColumn,
                profitPercentColumn,
                estimatedProfitColumn,
                timeColumn
        );

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tableView.setPrefHeight(520);
    }
    private void applyScanResult(MarketScanResult result) {
        tableView.setItems(FXCollections.observableArrayList(result.getRows()));

        watchlistStatusLabel.setText(
                "Watchlist: " + result.getWatchlistItems()
                        + " | Blacklist: " + result.getBlacklistedItems()
                        + " | Scanned: " + result.getScannedItems()
                        + " | Lots: " + result.getFoundLots()
        );

        lastUpdateLabel.setText(
                "Last update: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        );

        statusLabel.setText(
                "Status: shown=" + result.getShownRows()
                        + " | filtered=" + result.getFilteredLots()
        );
    }
    private MarketScanResult runScan() {
        String region = regionField.getText().trim();
        int lotsLimit = Integer.parseInt(lotsLimitField.getText().trim());
        int historyDepth = Integer.parseInt(historyDepthField.getText().trim());
        double minProfitPercent = Double.parseDouble(minProfitPercentField.getText().trim());
        long minTotalProfit = Long.parseLong(minTotalProfitField.getText().trim());

        return marketScannerService.scanMarket(
                region,
                lotsLimit,
                historyDepth,
                minProfitPercent,
                minTotalProfit
        );
    }
    private void scanMarketOnce() {
        try {
            MarketScanResult result = runScan();
            applyScanResult(result);

        } catch (NumberFormatException e) {
            statusLabel.setText("Status: ошибка — проверь числовые поля");
        } catch (Exception e) {
            statusLabel.setText("Status: ошибка — " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void shutdown() {
        if (monitoringTask != null) {
            monitoringTask.cancel(false);
        }

        scheduler.shutdownNow();
    }

}