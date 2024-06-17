package com.xion.views2.list;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@JavaScript("./bankGrid.js")
@PageTitle("Reconciliation")
@Route(value = "")
@RouteAlias(value = "")
@StyleSheet("https://cdn.jsdelivr.net/npm/bootstrap@4.3.1/dist/css/bootstrap.min.css")
public class MainView extends VerticalLayout {
    private final Grid<InvoiceResultSummeryIntermediate> invoiceGrid;
    private final Grid<BankStatementLineIntermediate> bankGrid;
    private final DatePicker startDatePickerBank;
    private final DatePicker endDatePickerBank;
    private final DatePicker startDatePickerInvoice;
    private final DatePicker endDatePickerInvoice;
    private TextField filter;
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

        HorizontalLayout mainLayout = new HorizontalLayout(invoiceLayout, bankLayout, matchLayout);
        mainLayout.setWidthFull();
        add(mainLayout);

        Page page = UI.getCurrent().getPage();

//        bankGrid.getElement().executeJs(
//                "this.addEventListener('dom-change', () => {" +
//                        "  const rows = this.shadowRoot.querySelectorAll('tr.unreconciled-row');" +
//                        "  console.log(rows);" +
//                        "  rows.forEach(row => {" +
//                        "    row.style.backgroundColor = 'red';" +
//                        "  });" +
//                        "});"
//        );

//        page.executeJs(
//                "console.log('inline');" +
//                        "const bankGrid = document.querySelector('#bankGridDiv #bankGrid');" +
//                        "const bankGridDiv = document.querySelector('#bankGridDiv');" +
//                        "const totalAmountSpan = document.querySelector('#totalAmountSpan');" +
//                        "console.log(bankGrid.selectedItems);" +
//                        "console.log(bankGridDiv);" +
//                        "bankGridDiv.addEventListener('click', (event) => {" +
//                        "const selectedItems = bankGrid.selectedItems;" +
//                        "let totalAmount = 0;" +
//                        "selectedItems.forEach(item => {" +
//                        "console.log(item);" +
//                        "const credit = parseFloat(item.credit) || 0;" +  // Default to 0 if parseFloat fails
//                        "const debit = parseFloat(item.debit) || 0;" +   // Default to 0 if parseFloat fails
//                        "totalAmount += (credit - debit);" +
//                        "});" +
//                        "totalAmountSpan.textContent = `Total Amount: ${totalAmount}`;" +
//                        "});"
//        );








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

        Page page = UI.getCurrent().getPage();


        page.executeJs(
                "console.log('inline');" +
                        "const totalAmountSpan = document.querySelector('#totalAmountSpan');" +
                        "console.log('totalAmountSpan:', totalAmountSpan);" +
                        "let totalAmount = 0;" +
                        "console.log('Initial totalAmount:', totalAmount);" +
                        "const checkboxes = document.querySelectorAll('#bankGridDiv vaadin-checkbox');" +
                        "console.log('checkboxes:', checkboxes);" +
                        "checkboxes.forEach((checkbox, index) => {" +
                        "   console.log('Adding event listener to checkbox ' + index + ':', checkbox);" +
                        "   checkbox.addEventListener('click', (event) => {" +
                        "       console.log('Click event triggered for checkbox', index);" +
                        "       const grid = document.querySelector('#bankGridDiv #bankGrid');" +
                        "       console.log('Grid:', grid);" +
                        "       if (grid) {" +
                        "           const activeRowGroup = grid._activeRowGroup;" +
                        "           console.log('activeRowGroup:', activeRowGroup);" +
                        "           if (activeRowGroup) {" +
//                        "               const key = parseInt(event.currentTarget.__item.key);" +
                                        "const key = index;" +
                        "               console.log('key:', key);" +
                        "               const x = 27 + ((key - 1) * 6);" +
                        "               console.log('x:', x);" +
                        "               const amountCell = activeRowGroup.childNodes[key].children['vaadin-grid-cell-' + x]._content.firstChild.firstChild.textContent;" +
                        "               console.log('amountCell:', amountCell);" +
                        "               const amount = parseFloat(amountCell) || 0;" +  // Default to 0 if parseFloat fails
                        "               console.log('Parsed amount:', amount);" +
                        "               totalAmount += amount;" +
                        "               console.log('Updated totalAmount:', totalAmount);" +
                        "               totalAmountSpan.textContent = 'Total Amount: ' + totalAmount;" +
                        "               console.log('Updated totalAmountSpan:', totalAmountSpan.textContent);" +
                        "           } else {" +
                        "               console.log('activeRowGroup not found');" +
                        "           }" +
                        "       } else {" +
                        "           console.log('Grid not found');" +
                        "       }" +
                        "   });" +
                        "});"
        );

    }

    private void filterInvoiceDataByDate() throws JsonProcessingException {
        LocalDate startDate = startDatePickerInvoice.getValue();
        LocalDate endDate = endDatePickerInvoice.getValue();

        if (startDate != null && endDate != null) {
            this.invoiceData = fetchDataService.fetchInvoiceData("", companyId, startDate, endDate);
            this.invoiceGrid.setItems(invoiceData);
        }
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

        Button onlyUnreconciled = new Button("Unreconciled Invoices", event -> {
            unreconciledInvoiceData = invoiceData.stream()
                    .filter(x -> !isInvoiceReconciled(x)).collect(Collectors.toList());
            invoiceGrid.setItems(unreconciledInvoiceData);
        });

        Button allInvoices = new Button("All Invoices", event -> {
            invoiceGrid.setItems(invoiceData);
        });

        HorizontalLayout filterLayout = new HorizontalLayout(onlyUnreconciled, allInvoices);
        filterLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);


        invoiceGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        invoiceGrid.setColumns("supplierName", "totalAmount", "currency", "date");
        //invoiceGrid.addColumn(invoice -> isInvoiceReconciled(invoice)).setHeader("Reconciled");
        invoiceGrid.addColumn(new ComponentRenderer<>(invoice -> {
            boolean reconciled = isInvoiceReconciled(invoice);
            Span reconciledSpan = new Span(reconciled ? "Yes" : "No");
            return reconciledSpan;
        })).setHeader("Reconciled");

        H2 invoiceTitle = new H2("Invoices");




        VerticalLayout invoiceLayout = new VerticalLayout(invoiceTitle, datePickersLayout, filterLayout, invoiceGrid);
        invoiceLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        invoiceLayout.setWidth("45%");
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

        Span totalAmountSpan = new Span("Total Amount: 0");
        totalAmountSpan.setId("totalAmountSpan");
        totalAmountSpan.getStyle().set("font-weight", "bold");
        totalAmountSpan.getStyle().set("font-size", "1.2em");

//        bankGrid.addSelectionListener(event -> {
//            double totalAmount = event.getAllSelectedItems().stream()
//                    .mapToDouble(item -> (item.getCredit() - item.getDebit()))
//                    .sum();
//            totalAmountSpan.setText("Total Amount: " + totalAmount);
//            totalAmountSpan.setVisible(!event.getAllSelectedItems().isEmpty());
//        });


        Button filterButton = new Button("Search", e -> filterBankDataByDate());

        filter = new TextField();
        filter.setPlaceholder("Filter");
        filter.setValueChangeMode(ValueChangeMode.LAZY);
        filter.addValueChangeListener(e -> applyFilters());

        HorizontalLayout searchFields = new HorizontalLayout(filter);

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
            amountDiv.setText(String.valueOf(Math.abs(amount)));

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

//        bankGrid.setRowStyleGenerator(bankStatement -> {
//            boolean reconciled = isBankStatementReconciled(bankStatement);
//            if (reconciled) {
//                return "reconciled-row"; // CSS class for reconciled rows
//            } else {
//                return "unreconciled-row"; // CSS class for unreconciled rows
//            }
//        });
//        bankGrid.setClassNameGenerator(bankStatement -> {
//            boolean reconciled = isBankStatementReconciled(bankStatement);
//                if (reconciled) {
//                    return "reconciled-row"; // CSS class for reconciled rows
//                } else {
//                    return "unreconciled-row"; // CSS class for unreconciled rows
//                }
//        });

        bankGrid.setClassNameGenerator(bankStatement -> {
            boolean reconciled = isBankStatementReconciled(bankStatement);
            return reconciled ? "table-success" : "table-danger";
        });

//        bankGrid.setClassName("table-danger");

        HorizontalLayout datePickersLayout = new HorizontalLayout(startDatePickerBank, endDatePickerBank, filterButton);
        datePickersLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        H2 bankTitle = new H2("Bank Statements");
        FlexLayout bankLayout = new FlexLayout(bankTitle, datePickersLayout, searchFields, filterButtons, totalAmountSpan, bankGridDiv);
        bankLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        bankLayout.setWidth("45%");

        return bankLayout;
    }

    private Component createMatchLayout() {
        Button matchButton = new Button("Create Match", e -> checkAmounts());
        Button splitButton = new Button("Split Amount");
        Button aiSuggestion = new Button("AI Suggestion", e -> FetchDataService.getAISuggestion(companyId, bankName,
                startDatePickerBank, endDatePickerBank, startDatePickerInvoice, endDatePickerInvoice));

        VerticalLayout matchLayout = new VerticalLayout(matchButton, splitButton, aiSuggestion);
        matchLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        matchLayout.setAlignItems(Alignment.CENTER);
        matchLayout.setHeightFull();
        matchLayout.setWidth("10%");
        return matchLayout;
    }



    private void applyFilters() {
        List<BankStatementLineIntermediate> filteredData = bankData;

        String nameFilterValue = filter.getValue().toLowerCase();
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
//            Dialog confirmationDialog = new Dialog();
//            confirmationDialog.add(new Span("The total bank amount (" + bankAmount + ") does not equal the total invoice amount (" + invoiceAmount + "). Do you want to continue?"));
//
//            Button confirmButton = new Button("Yes", event -> {
//                createMatch(selectedInvoices, selectedBankStatements);
//                confirmationDialog.close();
//            });
//            Button cancelButton = new Button("No", event -> confirmationDialog.close());
//
//            confirmationDialog.add(confirmButton, cancelButton);
//            System.out.println("before open");
//            getUI().ifPresent(ui -> ui.access(() -> confirmationDialog.open()));
//            //confirmationDialog.open();
//            System.out.println("after open");
        }
        createMatch(selectedInvoices, selectedBankStatements);
    }

}