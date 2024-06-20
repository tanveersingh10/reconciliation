package com.xion.views2.list;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
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

import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
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
import org.apache.poi.ss.formula.functions.T;

import java.beans.VetoableChangeListener;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@JavaScript("./bankGrid.js")
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
    private String companyId = "xion_ai_pte_ltd";
    private String bankName = "DBS SGD";
    FetchDataService fetchDataService;

    public MainView(FetchDataService fetchDataService) throws Exception {
        setSpacing(false);
        this.fetchDataService = fetchDataService;

        startDatePickerBank = new DatePicker("Start Date");
        endDatePickerBank = new DatePicker("End Date");

        startDatePickerInvoice = new DatePicker("Start Date");
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

        Button splitButton = new Button("Split Amount", e -> openSplitAmountDialog());
        splitButton.setVisible(false);

        Button createInvoiceButton = new Button("Create Invoice", e -> openCreateInvoiceDialog());
        createInvoiceButton.setVisible(false);

        Button aiSuggestion = new Button("AI Suggestion", e -> openAISuggestionDialog());
        //FetchDataService.getAISuggestion(companyId, bankName,
        //startDatePickerBank, endDatePickerBank, startDatePickerInvoice, endDatePickerInvoice))
        aiSuggestion.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        invoiceGrid.addSelectionListener(event -> updateButtonVisibility(matchButton, splitButton, createInvoiceButton, aiSuggestion));
        bankGrid.addSelectionListener(event -> updateButtonVisibility(matchButton, splitButton, createInvoiceButton, aiSuggestion));
        VerticalLayout matchLayout = new VerticalLayout(matchButton, splitButton, createInvoiceButton, aiSuggestion);
        matchLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        matchLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        matchLayout.setHeightFull();
        matchLayout.setWidth("8%");
        return matchLayout;
    }

    private void updateButtonVisibility(Button matchButton, Button splitButton,
                                        Button createInvoiceButton, Button aiSuggestion) {
        Set<InvoiceResultSummeryIntermediate> selectedInvoices = invoiceGrid.getSelectedItems();
        Set<BankStatementLineIntermediate> selectedBankStatements = bankGrid.getSelectedItems();

        if (selectedInvoices.isEmpty() && selectedBankStatements.isEmpty()) {
            matchButton.setVisible(false);
            createInvoiceButton.setVisible(false);
            splitButton.setVisible(false);
            aiSuggestion.setVisible(true);
            return;
        } else if (!selectedBankStatements.isEmpty() && selectedInvoices.isEmpty()) {
            createInvoiceButton.setVisible(true);
            matchButton.setVisible(false);
            splitButton.setVisible(false);
            return;
        }


        double invoiceAmount = selectedInvoices.stream().mapToDouble(InvoiceResultSummeryIntermediate::getTotalAmount).sum();
        double bankAmount = selectedBankStatements.stream()
                .mapToDouble(bankStatement -> bankStatement.getCredit() + bankStatement.getDebit()).sum();

        if (invoiceAmount == bankAmount) {
            matchButton.setVisible(true);
            splitButton.setVisible(false);
            createInvoiceButton.setVisible(false);
        } else {
            matchButton.setVisible(false);
            splitButton.setVisible(true);
            createInvoiceButton.setVisible(false);
        }
    }

    private void openCreateInvoiceDialog() {
        BankStatementLineIntermediate selectedBankStatement = null;
        if (bankGrid.getSelectedItems().size() == 1) {
            selectedBankStatement = bankGrid.getSelectedItems().stream().collect(Collectors.toList()).get(0);
        }
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        TextField supplierNameField = new TextField("Supplier Name");
        TextField customerField = new TextField("Customer Name");
        NumberField totalAmountField = new NumberField("Total Amount");
        TextField currencyField = new TextField("Currency");
        DatePicker dateField = new DatePicker("Date");

        if (selectedBankStatement != null) {
            if (selectedBankStatement.getDebit() == 0) {
                totalAmountField.setValue(selectedBankStatement.getCredit());
            } else {
                totalAmountField.setValue(selectedBankStatement.getDebit());
            }

            currencyField.setValue(selectedBankStatement.getCurrency());
            dateField.setValue(selectedBankStatement.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }

        Button createButton = new Button("Create", event -> {
            if (supplierNameField.getValue() == null || customerField.getValue() == null || dateField.getValue() == null
                || totalAmountField.getValue() == null || currencyField.getValue() == null) {
                Notification.show("Please fill in all fields").setPosition(Notification.Position.MIDDLE);
                return;
            }
            InvoiceResultSummeryIntermediate newInvoice = new InvoiceResultSummeryIntermediate();
            newInvoice.setSupplierName(supplierNameField.getValue());
            LocalDate date = dateField.getValue();
            newInvoice.setDate(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            newInvoice.setTotalAmount(totalAmountField.getValue());
            newInvoice.setCurrency(currencyField.getValue());
            if (this.invoiceData != null) {
                this.invoiceData.add(newInvoice);
                this.invoiceGrid.setItems(invoiceData);
            }
            List<Long> bankIds = new ArrayList<>();
            for (BankStatementLineIntermediate bankStatement : bankGrid.getSelectedItems()) {
                bankIds.add(bankStatement.getId());
            }
            newInvoice.setBankReconciliationIds(bankIds);

            //add code to save to database
            dialog.close();
        });

        VerticalLayout dialogLayout = new VerticalLayout(supplierNameField, customerField, totalAmountField, currencyField, dateField, createButton);
        dialog.add(dialogLayout);
        dialog.open();
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

    private void openSplitAmountDialog() {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        VerticalLayout dialogLayout = new VerticalLayout();
        Set<BankStatementLineIntermediate> selectedBankStatements = bankGrid.getSelectedItems();
        Set<InvoiceResultSummeryIntermediate> selectedInvoices = invoiceGrid.getSelectedItems();

        double bankTotal = selectedBankStatements.stream()
                .mapToDouble(bank -> bank.getCredit() + bank.getDebit()).sum();
        double invoiceTotal = selectedInvoices.stream()
                .mapToDouble(InvoiceResultSummeryIntermediate::getTotalAmount).sum();

        NumberField bankAmountField = new NumberField("Bank Total Amount");
        bankAmountField.setValue(bankTotal);
        bankAmountField.setReadOnly(true);

        NumberField invoiceAmountField = new NumberField("Invoice Total Amount");
        invoiceAmountField.setValue(invoiceTotal);
        invoiceAmountField.setReadOnly(true);

        dialogLayout.add(new H2("Split Amounts"), bankAmountField, invoiceAmountField);

        for (InvoiceResultSummeryIntermediate invoice : selectedInvoices) {
            NumberField splitField = new NumberField("Invoice Amount for " + invoice.getSupplierName());
            splitField.setValue(invoice.getTotalAmount());
            dialogLayout.add(splitField);
        }

        Button splitButton = new Button("Confirm Split", e -> {
            double totalSplitAmount = dialogLayout.getChildren()
                    .filter(component -> component instanceof NumberField)
                    .mapToDouble(component -> ((NumberField) component).getValue())
                    .sum();

            if (totalSplitAmount != bankTotal) {
                Notification.show("Split amounts do not match bank total amount. Please adjust.");
                return;
            }

            // Save split logic here...

            dialog.close();
        });

        dialogLayout.add(splitButton);
        dialog.add(dialogLayout);
        dialog.open();
    }

    private void openAISuggestionDialog() {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        VerticalLayout dialogLayout = new VerticalLayout();

        TextArea hintTextArea = new TextArea("Hint");
        hintTextArea.setPlaceholder("What should our AI look out for? \n \n \n \n \n");
        hintTextArea.setWidthFull();

        Button suggestButton = new Button("Get Suggestion", e -> {
            String hint = hintTextArea.getValue();
//            FetchDataService.getAISuggestion(companyId, bankName, startDatePickerBank,
//                    endDatePickerBank, startDatePickerInvoice, endDatePickerInvoice, hint);
            dialog.close();
        });
//        Icon icon = new Icon(VaadinIcon.MICROPHONE);
//        VerticalLayout recordingLayout = new VerticalLayout();
        Button voiceRecording = new Button ("", VaadinIcon.MICROPHONE.create());
//        recordingLayout.add(voiceRecording);
//        recordingLayout.setHorizontalComponentAlignment(Alignment.CENTER);

        dialogLayout.add(new H2("AI Suggestion"), hintTextArea, voiceRecording, suggestButton);
        dialogLayout.setHorizontalComponentAlignment(Alignment.CENTER, voiceRecording);
        dialogLayout.setHorizontalComponentAlignment(Alignment.CENTER, suggestButton);

        dialog.add(dialogLayout);
        dialog.open();
    }


    public class AudioRecorderView extends VerticalLayout {

        public AudioRecorderView() {
            Button voiceRecordingButton = new Button("", VaadinIcon.MICROPHONE.create());
            voiceRecordingButton.addClickListener(e -> {
                getUI().ifPresent(ui -> ui.getPage().executeJs("startRecording();"));
            });

            Button stopRecordingButton = new Button("Stop Recording", VaadinIcon.STOP.create());
            stopRecordingButton.addClickListener(e -> {
                getUI().ifPresent(ui -> ui.getPage().executeJs("stopRecording();"));
            });

            add(voiceRecordingButton, stopRecordingButton);
        }

        @ClientCallable
        public void serverSideCallback(String base64AudioMessage) {
            // Handle the audio data (base64 string) here
            byte[] audioBytes = java.util.Base64.getDecoder().decode(base64AudioMessage);
            // You can now save the audioBytes to a file or process them as needed
            System.out.println(audioBytes);
        }
    }



}