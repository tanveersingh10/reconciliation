package com.xion.views2.list;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.xion.data.BankStatementLineIntermediate;
import com.xion.data.FetchDataService;
import com.xion.data.InvoiceResultSummeryIntermediate;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@JavaScript("./bankGrid.js")
@JavaScript("./voiceRecorder.js")
@JavaScript("https://cdn.webrtc-experiment.com/RecordRTC.js")
@PageTitle("Reconciliation")
@Route(value = "")
@RouteAlias(value = "")
@StyleSheet("https://cdn.jsdelivr.net/npm/bootstrap@4.3.1/dist/css/bootstrap.min.css")
@CssImport("./styles/styles.css")
public class MainView extends VerticalLayout {
    private final Grid<InvoiceResultSummeryIntermediate> invoiceGrid;
    private final Grid<BankStatementLineIntermediate> bankGrid;
    private final DatePicker startDatePickerBank;
    private final DatePicker endDatePickerBank;
    private final DatePicker startDatePickerInvoice;
    private final DatePicker endDatePickerInvoice;
    private TextField bankFilter;
    private TextField invoiceFilter;
    private List<BankStatementLineIntermediate> bankData;
    private List<BankStatementLineIntermediate> unreconciledBankData;
    private List<InvoiceResultSummeryIntermediate> invoiceData;
    private List<InvoiceResultSummeryIntermediate> unreconciledInvoiceData;
    private ReconciliationDialogues reconciliationDialogues;
    private String companyId = "xion_ai_pte_ltd";
    private String bankName = "DBS SGD";
    FetchDataService fetchDataService;

    public MainView(FetchDataService fetchDataService) throws Exception {
        setSpacing(false);
        this.fetchDataService = fetchDataService;

        startDatePickerBank = new DatePicker("Start Date");
        endDatePickerBank = new DatePicker("End Date");

        startDatePickerInvoice  = new DatePicker("Start Date");
        endDatePickerInvoice = new DatePicker("End Date");

        invoiceGrid = new Grid<>(InvoiceResultSummeryIntermediate.class);
        bankGrid = new Grid<>(BankStatementLineIntermediate.class);

        Component invoiceLayout = createInvoiceLayout();
        Component bankLayout = createBankLayout();
        Component matchLayout = createMatchLayout();

        HorizontalLayout mainLayout = new HorizontalLayout(invoiceLayout, matchLayout, bankLayout);
        mainLayout.setWidthFull();
        add(mainLayout);

        Page page = UI.getCurrent().getPage();

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("text-align", "center");
    }

    private void filterBankDataByDate() {
        LocalDate startDate = startDatePickerBank.getValue();
        LocalDate endDate = endDatePickerBank.getValue();

        if (startDate != null && endDate != null) {
            this.bankData = fetchDataService.fetchBankData(companyId, bankName, startDate, endDate);
            this.bankGrid.setItems(bankData);
        }

        UI ui = UI.getCurrent();
        ui.access(() -> {
            //function defined in bankGrid.js
            ui.getPage().executeJs("setTimeout(function() { updateListeners(); }, 250);");
            ui.getPage().executeJs("setTimeout(function() { updateStyles(); }, 250);");
        });
    }

    private void filterInvoiceDataByDate() throws JsonProcessingException {
        LocalDate startDate = startDatePickerInvoice.getValue();
        LocalDate endDate = endDatePickerInvoice.getValue();

        if (startDate != null && endDate != null) {
            this.invoiceData = fetchDataService.fetchInvoiceData("", companyId, startDate, endDate);
            this.invoiceGrid.setItems(invoiceData);
        }

        UI ui = UI.getCurrent();
        ui.access(() -> {
            //function defined in bankGrid.js
            ui.getPage().executeJs("setTimeout(function() { updateListenersInvoice(); }, 300);");
        });
    }

    private Component createInvoiceLayout() throws JsonProcessingException {
        if (startDatePickerInvoice.getValue() != null && endDatePickerInvoice.getValue() != null) {
            this.invoiceData = this.fetchDataService.fetchInvoiceData("", companyId,
                    startDatePickerInvoice.getValue(), endDatePickerInvoice.getValue());
            invoiceGrid.setItems(invoiceData);

        }
        Button filterButton = new Button("Search", event -> {
            try {
                filterInvoiceDataByDate();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });

        HorizontalLayout datePickersLayout = new HorizontalLayout(startDatePickerInvoice, endDatePickerInvoice, filterButton);
        datePickersLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);

        Div invoiceGridDiv = new Div(invoiceGrid);
        invoiceGridDiv.setId("invoiceGridDiv");
        invoiceGrid.setId("invoiceGrid");


        Button onlyUnreconciled = new Button("Unreconciled Invoices", event -> {
            unreconciledInvoiceData = invoiceData.stream()
                    .filter(x -> !isInvoiceReconciled(x)).collect(Collectors.toList());
            invoiceGrid.setItems(unreconciledInvoiceData);
        });

        Button allInvoices = new Button("All Invoices", event -> {
            invoiceGrid.setItems(invoiceData);
        });

        Span totalAmountSpan = new Span("Invoice Amount: 0");
        totalAmountSpan.setId("totalAmountSpan");
        totalAmountSpan.getStyle().set("font-weight", "bold");
        totalAmountSpan.getStyle().set("font-size", "1.2em");

        //HorizontalLayout filterLayout = new HorizontalLayout(onlyUnreconciled, allInvoices);
        //filterLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);

        invoiceGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        invoiceGrid.removeAllColumns();
        invoiceGrid.addColumn(new ComponentRenderer<>(invoice -> {
            Date date = invoice.getDate();
            Span dateSpan = new Span(formatter.format(date));
            return dateSpan;
        }
        )).setHeader("Date");

        invoiceGrid.addColumns("supplierName", "totalAmount", "currency");

        invoiceGrid.addColumn(new ComponentRenderer<>(invoice -> {
            boolean reconciled = isInvoiceReconciled(invoice);
            Span reconciledSpan = new Span(reconciled ? "Yes" : "No");
            return reconciledSpan;
        })).setHeader("Reconciled");


        invoiceFilter = new TextField();
        invoiceFilter.setPlaceholder("Filter");
        invoiceFilter.setValueChangeMode(ValueChangeMode.LAZY);
        invoiceFilter.addValueChangeListener(e -> applyFiltersInvoice());

        HorizontalLayout searchField = new HorizontalLayout(invoiceFilter);
        searchField.setDefaultVerticalComponentAlignment(Alignment.BASELINE);


        H2 invoiceTitle = new H2("Invoices");
        FlexLayout centeredComponentsLayout = new FlexLayout(invoiceTitle, datePickersLayout, searchField, totalAmountSpan);
        centeredComponentsLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        centeredComponentsLayout.setAlignItems(Alignment.CENTER);

        FlexLayout invoiceLayout = new FlexLayout(centeredComponentsLayout, invoiceGridDiv);
//        invoiceLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        invoiceLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        //invoiceLayout.setAlignItems(Alignment.CENTER);

        invoiceLayout.setWidth("46%");
        invoiceLayout.setId("invoiceLayout");
        return invoiceLayout;
    }

    private Component createBankLayout() {
        if (startDatePickerBank.getValue() != null && endDatePickerBank.getValue() != null) {
            this.bankData = this.fetchDataService.fetchBankData(companyId, bankName,
                    startDatePickerBank.getValue(), endDatePickerBank.getValue());
            bankGrid.setItems(bankData);
        }
        Div bankGridDiv = new Div(bankGrid);
        bankGridDiv.setId("bankGridDiv");

        bankGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        bankGrid.setId("bankGrid");

        Span totalAmountSpan = new Span("Bank Amount: 0");
        totalAmountSpan.setId("totalAmountSpan");
        totalAmountSpan.getStyle().set("font-weight", "bold");
        totalAmountSpan.getStyle().set("font-size", "1.2em");

        Button filterButton = new Button("Search", e -> filterBankDataByDate());

        bankFilter = new TextField();
        bankFilter.setPlaceholder("Filter");
        bankFilter.setValueChangeMode(ValueChangeMode.LAZY);
        bankFilter.addValueChangeListener(e -> applyFiltersBank());

        HorizontalLayout searchField = new HorizontalLayout(bankFilter);
        searchField.setDefaultVerticalComponentAlignment(Alignment.BASELINE);

        Button unreconciledButton = new Button("Unreconciled Statements", event -> {
            unreconciledBankData = bankData.stream()
                    .filter(x -> !isBankStatementReconciled(x)).collect(Collectors.toList());
            bankGrid.setItems(unreconciledBankData);
        });

        Button allDataButton = new Button("All Statements", event -> {
            bankGrid.setItems(bankData);
        });

        HorizontalLayout filterButtons = new HorizontalLayout(unreconciledButton, allDataButton);
        filterButtons.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        bankGrid.removeAllColumns();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        bankGrid.addColumn(new ComponentRenderer<>(bankStatement -> {
            String date = simpleDateFormat.format(bankStatement.getDate()).toString();
            Span dateSpan = new Span(date);
            return dateSpan;
        })).setHeader("Date");

        bankGrid.addColumn(new ComponentRenderer<>(bankStatement -> {
            Span span = new Span(bankStatement.getAccount());
            return span;
        })).setHeader("Account");

        bankGrid.addColumn(new ComponentRenderer<>(bankStatement -> {
            double amount = bankStatement.getCredit() - bankStatement.getDebit();
            Div amountDiv = new Div();
            amountDiv.setText(String.valueOf(amount));

            if (amount > 0) {
                amountDiv.getStyle().set("background-color", "rgba(144, 238, 144, 0.6)"); // Lighter green with 0.6 opacity
            } else if (amount < 0) {
                amountDiv.getStyle().set("background-color", "rgba(255, 182, 193, 0.6)"); // Lighter red with 0.6 opacity
            }

            return amountDiv;
        })).setHeader("Amount");

        bankGrid.addColumn(new ComponentRenderer<>(bankStatement -> {
            Span span = new Span(bankStatement.getCurrency());
            return span;
        })).setHeader("Currency");

        bankGrid.addColumn(new ComponentRenderer<>(bankStatement -> {
            boolean reconciled = isBankStatementReconciled(bankStatement);
            Span reconciledSpan = new Span(reconciled ? "Yes" : "No");
            return reconciledSpan;
        })).setHeader("Reconciled");


        bankGrid.setClassNameGenerator(bankStatement -> {
            boolean reconciled = isBankStatementReconciled(bankStatement);
            if (reconciled) {
                return "reconciled-row"; // CSS class for reconciled rows
            } else {
                return "unreconciled-row"; // CSS class for unreconciled rows
            }
        });

        HorizontalLayout datePickersLayout = new HorizontalLayout(startDatePickerBank, endDatePickerBank, filterButton);
        datePickersLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        H2 bankTitle = new H2("Bank Statements");

        FlexLayout centeredComponentsLayout = new FlexLayout(bankTitle, datePickersLayout, searchField, totalAmountSpan);
        centeredComponentsLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        centeredComponentsLayout.setAlignItems(Alignment.CENTER);

        //bankGrid.addItemClickListener(event -> highlightCorrespondingInvoices(event.getItem()));

        FlexLayout bankLayout = new FlexLayout(centeredComponentsLayout, bankGridDiv);
        bankLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        bankLayout.setWidth("46%");
        bankLayout.setId("bankLayout");

        return bankLayout;
    }

    private Component createMatchLayout() {
        Button matchButton = new Button("Create Match", e -> checkAmounts());
        matchButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        matchButton.setVisible(false);

        Button splitButton = new Button("Split Amount", e ->
                ReconciliationDialogues.openSplitAmountDialog(invoiceData, bankGrid, invoiceGrid));
        splitButton.setVisible(false);

        Button createAdjustmentsButton = new Button("Add Adjustment", e -> {
            ReconciliationDialogues.openCreateAdjustmentsDialog(bankGrid, invoiceGrid);
        });
        createAdjustmentsButton.setVisible(false);


        Button createInvoiceButton = new Button("Create Invoice", e ->
                ReconciliationDialogues.openCreateInvoiceDialog(bankGrid, invoiceGrid));
        createInvoiceButton.setVisible(false);


        Button aiSuggestion = new Button("AI Suggestion", e -> ReconciliationDialogues.openAISuggestionDialog(
                companyId, bankName, startDatePickerBank, endDatePickerBank, startDatePickerInvoice, endDatePickerInvoice
        ));

        aiSuggestion.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        invoiceGrid.addSelectionListener(event -> updateButtonVisibility(matchButton, splitButton, createInvoiceButton, aiSuggestion, createAdjustmentsButton));
        bankGrid.addSelectionListener(event -> updateButtonVisibility(matchButton, splitButton, createInvoiceButton, aiSuggestion, createAdjustmentsButton));
        VerticalLayout matchLayout = new VerticalLayout(matchButton, splitButton, createInvoiceButton, createAdjustmentsButton, aiSuggestion);
        matchLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        matchLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        matchLayout.setHeightFull();
        matchLayout.setWidth("8%");
        return matchLayout;
    }

    private void updateButtonVisibility(Button matchButton, Button splitButton, Button createInvoiceButton,
                                        Button aiSuggestion, Button createAdjustmentsButton) {
        Set<InvoiceResultSummeryIntermediate> selectedInvoices = invoiceGrid.getSelectedItems();
        Set<BankStatementLineIntermediate> selectedBankStatements = bankGrid.getSelectedItems();


        if (selectedInvoices.isEmpty() && selectedBankStatements.isEmpty()) {
            matchButton.setVisible(false);
            createInvoiceButton.setVisible(false);
            splitButton.setVisible(false);
            aiSuggestion.setVisible(true);
            createAdjustmentsButton.setVisible(false);
            return;
        } else if (selectedInvoices.isEmpty() && !selectedBankStatements.isEmpty()) {
            matchButton.setVisible(false);
            createInvoiceButton.setVisible(true);
            splitButton.setVisible(false);
            createAdjustmentsButton.setVisible(false);
            return;
        }

        double invoiceAmount = selectedInvoices.stream().mapToDouble(x -> x.getTotalAmount()).sum();

        double bankAmount = selectedBankStatements.stream()
                .mapToDouble(bankStatement -> bankStatement.getCredit() + bankStatement.getDebit()).sum();

        if (invoiceAmount == bankAmount) {
            matchButton.setVisible(true);
            splitButton.setVisible(false);
            createInvoiceButton.setVisible(false);
            createAdjustmentsButton.setVisible(false);
        } else if (invoiceAmount > bankAmount && selectedInvoices.size() == 1) {
            matchButton.setVisible(false);
            splitButton.setVisible(true);
            createInvoiceButton.setVisible(false);
            createAdjustmentsButton.setVisible(true);
        } else if (invoiceAmount != bankAmount && selectedBankStatements.size() == 1) {
            matchButton.setVisible(false);
            splitButton.setVisible(false);
            createInvoiceButton.setVisible(false);
            createAdjustmentsButton.setVisible(true);
        } else {
            matchButton.setVisible(false);
            splitButton.setVisible(false);
            createInvoiceButton.setVisible(false);
            createAdjustmentsButton.setVisible(false);
        }
    }



    private void applyFiltersBank() {
        List<BankStatementLineIntermediate> filteredData = bankData;
        if (bankFilter.getValue() == null || bankFilter.getValue().isEmpty()) {
            bankGrid.setItems(bankData);
            return;
        }

        String nameFilterValue = bankFilter.getValue().toLowerCase();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        if (!nameFilterValue.isEmpty()) {
            filteredData = filteredData.stream()
                    .filter(bankStatementLine -> bankStatementLine.getAccount().toLowerCase().contains(nameFilterValue)
                            || bankStatementLine.getCurrency().contains(nameFilterValue)
                            || formatter.format(bankStatementLine.getDate()).toString().contains(nameFilterValue)
                            || String.valueOf(bankStatementLine.getCredit()).contains(nameFilterValue)
                            || String.valueOf(bankStatementLine.getDebit()).contains(nameFilterValue)
                            || bankStatementLine.getDescription().toLowerCase().contains(nameFilterValue)
                    )
                    .collect(Collectors.toList());
        }
        bankGrid.setItems(filteredData);

    }

    private void applyFiltersInvoice() {
        if (invoiceFilter.getValue() == null || invoiceFilter.getValue().isEmpty()) {
            invoiceGrid.setItems(invoiceData);
            return;
        }

        List<InvoiceResultSummeryIntermediate> filteredData = invoiceData;
        String filterValue = invoiceFilter.getValue().toLowerCase();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        filteredData = filteredData.stream().filter(invoice ->
                simpleDateFormat.format(invoice.getDate()).contains(filterValue)
                        || invoice.getSupplierName().toLowerCase().contains(filterValue)
                        || invoice.getTotalAmount().toString().contains(filterValue)
                        || invoice.getCurrency().toLowerCase().contains(filterValue)
        ).collect(Collectors.toList());
        invoiceGrid.setItems(filteredData);
    }

    public boolean isInvoiceReconciled(InvoiceResultSummeryIntermediate invoice) {
        return invoice.getBankReconciliationIds() != null && !invoice.getBankReconciliationIds().isEmpty();
    }

    public boolean isBankStatementReconciled(BankStatementLineIntermediate bankStatement) {
        return bankStatement.getAccountID() != null && !bankStatement.getAccountID().isEmpty();
    }

    public void createMatch(Set<InvoiceResultSummeryIntermediate> selectedInvoices, Set<BankStatementLineIntermediate> selectedBankStatements) {
        for (InvoiceResultSummeryIntermediate invoice : selectedInvoices) {
            ArrayList<Long> matches = new ArrayList<>();
            for (BankStatementLineIntermediate bankStatement : selectedBankStatements) {
                matches.add(bankStatement.getId());
            }
            invoice.setBankReconciliationIds(matches);
        }

        for (BankStatementLineIntermediate bankStatement : selectedBankStatements) {
            ArrayList<Long> matches = new ArrayList<>();
            for (InvoiceResultSummeryIntermediate invoice : selectedInvoices) {
                matches.add(invoice.getId());
            }
            bankStatement.setAccountID(matches);
        }

        invoiceGrid.getDataProvider().refreshAll();
        bankGrid.getDataProvider().refreshAll();
    }

    public void checkAmounts() {
        Set<InvoiceResultSummeryIntermediate> selectedInvoices = invoiceGrid.getSelectedItems();
        Set<BankStatementLineIntermediate> selectedBankStatements = bankGrid.getSelectedItems();

        if (selectedInvoices.isEmpty()) {
            Notification.show("Please select at least 1 invoice");
            return;
        }

        if (selectedBankStatements.isEmpty()) {
            Notification.show("Please select at least 1 bank statement");
            return;
        }

        double invoiceAmount = 0;
        double bankAmount = 0;

        for (InvoiceResultSummeryIntermediate invoice : selectedInvoices) {
            invoiceAmount += invoice.getTotalAmount();
        }
        for (BankStatementLineIntermediate bankStatement : selectedBankStatements) {
            bankAmount += bankStatement.getDebit();
            bankAmount += bankStatement.getCredit();
        }
        System.out.println(invoiceAmount);
        System.out.println(bankAmount);

        if (invoiceAmount != bankAmount) {
            Notification.show("Invoices don't add up to bank statements");
            createMatch(selectedInvoices, selectedBankStatements);
        }
    }







}