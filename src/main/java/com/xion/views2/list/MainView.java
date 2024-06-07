package com.xion.views2.list;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;

import com.vaadin.flow.component.html.H2;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.xion.components.BankStatementLineService;
import com.xion.components.InvoiceResultSummeryService;
import com.xion.data.FetchDataService;
import com.xion.resultObjectModel.resultSummeries.InvoiceResultSummery;
import com.xion.resultObjectModel.resultSummeries.bank.BankStatementLine;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@PageTitle("Reconciliation")
@Route(value = "")
@RouteAlias(value = "")
public class MainView extends VerticalLayout {
    private final Grid<InvoiceResultSummery> invoiceGrid;
    private final Grid<BankStatementLine> bankGrid;
    private final DatePicker startDatePickerBank;
    private final DatePicker endDatePickerBank;
    private final DatePicker startDatePickerInvoice;
    private final DatePicker endDatePickerInvoice;
    private TextField nameFilter;
    private TextField searchFilter;
    private TextField amountFilter;
    private List<BankStatementLine> bankData;
    private List<InvoiceResultSummery> invoiceData;
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

        LocalDate startDate = LocalDate.of(2022, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 4, 30);
        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        invoiceGrid = new Grid<>(InvoiceResultSummery.class);
        bankGrid = new Grid<>(BankStatementLine.class);

        Component invoiceLayout = createInvoiceLayout();
        Component bankLayout = createBankLayout();
        Component matchLayout = createMatchLayout();

        HorizontalLayout mainLayout = new HorizontalLayout(invoiceLayout, bankLayout, matchLayout);
        mainLayout.setWidthFull();
        add(mainLayout);

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

        invoiceGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        invoiceGrid.setColumns("invoiceNumber", "supplierName", "totalAmount", "currency", "date", "matchId");
        H2 invoiceTitle = new H2("Invoices");

        VerticalLayout invoiceLayout = new VerticalLayout(invoiceTitle, datePickersLayout, invoiceGrid);
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



        bankGrid.setSelectionMode(Grid.SelectionMode.MULTI);

        Button filterButton = new Button("Search", e -> filterBankDataByDate());

        nameFilter = new TextField();
        nameFilter.setPlaceholder("Filter by name");
        nameFilter.setValueChangeMode(ValueChangeMode.LAZY);
        nameFilter.addValueChangeListener(e -> applyFilters());

        searchFilter = new TextField();
        searchFilter.setPlaceholder("Filter by search");
        searchFilter.setValueChangeMode(ValueChangeMode.LAZY);

        amountFilter = new TextField();
        amountFilter.setPlaceholder("Filter by amount");
        amountFilter.setValueChangeMode(ValueChangeMode.LAZY);
        amountFilter.addValueChangeListener(e -> applyFilters());

        HorizontalLayout searchFields = new HorizontalLayout(nameFilter, searchFilter, amountFilter);

        bankGrid.setColumns("date", "account", "debit", "credit", "currency");

        HorizontalLayout datePickersLayout = new HorizontalLayout(startDatePickerBank, endDatePickerBank, filterButton);
        datePickersLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        H2 bankTitle = new H2("Bank Statements");
        FlexLayout bankLayout = new FlexLayout(bankTitle, datePickersLayout, searchFields, bankGrid);
        bankLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        bankLayout.setWidth("45%");


        return bankLayout;
    }

    private Component createMatchLayout() {
        Button matchButton = new Button("Create Match");
        Button splitButton = new Button("Split Amount");

        VerticalLayout matchLayout = new VerticalLayout(matchButton, splitButton);
        matchLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        matchLayout.setAlignItems(Alignment.CENTER);
        matchLayout.setHeightFull();
        matchLayout.setWidth("10%");
        return matchLayout;
    }

    private void applyFilters() {
        List<BankStatementLine> filteredData = bankData;

        String nameFilterValue = nameFilter.getValue().toLowerCase();
        if (!nameFilterValue.isEmpty()) {
            filteredData = filteredData.stream()
                    .filter(bankStatementLine -> bankStatementLine.getAccount().toLowerCase().contains(nameFilterValue))
                    .collect(Collectors.toList());
        }

        String amountFilterValue = amountFilter.getValue();
        if (!amountFilterValue.isEmpty()) {
            try {
                double amount = Double.parseDouble(amountFilterValue);
                filteredData = filteredData.stream()
                        .filter(bankStatementLine -> bankStatementLine.getDebit() == amount || bankStatementLine.getCredit() == amount)
                        .collect(Collectors.toList());
            } catch (NumberFormatException e) {
                Notification.show("Invalid amount format");
            }
        }

        bankGrid.setItems(filteredData);
    }

}
