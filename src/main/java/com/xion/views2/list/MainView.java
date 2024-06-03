package com.xion.views2.list;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
//import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.xion.components.BankStatementLineService;
import com.xion.components.InvoiceResultSummeryService;
import com.xion.resultObjectModel.resultSummeries.InvoiceResultSummery;
import com.xion.resultObjectModel.resultSummeries.bank.BankStatementLine;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Set;

@PageTitle("Reconciliation")
@Route(value = "")
@RouteAlias(value = "")
public class MainView extends VerticalLayout {
    private final Grid<InvoiceResultSummery> invoiceGrid;
    private final Grid<BankStatementLine> bankGrid;
    private final DatePicker startDatePicker;
    private final DatePicker endDatePicker;
    private TextField nameFilter;
    private TextField searchFilter;
    private TextField amountFilter;
    private List<BankStatementLine> bankData;
    private List<InvoiceResultSummery> invoiceData;

    InvoiceResultSummeryService invoiceResultSummeryService;
    BankStatementLineService bankStatementLineService;
    public MainView() {
        setSpacing(false);

//        this.invoiceResultSummeryService = invoiceResultSummeryService;
//        this.bankStatementLineService = bankStatementLineService;

        invoiceGrid = new Grid<>(InvoiceResultSummery.class);
        bankGrid = new Grid<>(BankStatementLine.class);
        startDatePicker = new DatePicker("Start Date");
        endDatePicker = new DatePicker("End Date");

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
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate != null && endDate != null) {
//            bankData = bankStatementLineService.
            Notification notification = Notification.show("filtered data");
        }
    }

    private Component createInvoiceLayout() {
        invoiceGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        invoiceGrid.setColumns("invoiceNumber", "supplierName", "totalAmount", "currency", "dueOn");
        H2 invoiceTitle = new H2("Invoices");
        VerticalLayout invoiceLayout = new VerticalLayout(invoiceTitle, invoiceGrid);
        invoiceLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        invoiceLayout.setWidth("40%");
        return invoiceLayout;
    }

    private Component createBankLayout() {
        fetchBankData();
        bankGrid.setSelectionMode(Grid.SelectionMode.MULTI);

        Button filterButton = new Button("Search", e -> filterBankDataByDate());

        nameFilter = new TextField();
        nameFilter.setPlaceholder("Filter by name");
        nameFilter.setValueChangeMode(ValueChangeMode.LAZY);
        //nameFilter.addValueChangeListener(e -> updateList());

        searchFilter = new TextField();
        searchFilter.setPlaceholder("Filter by search");
        searchFilter.setValueChangeMode(ValueChangeMode.LAZY);

        amountFilter = new TextField();
        amountFilter.setPlaceholder("Filter by amount");
        amountFilter.setValueChangeMode(ValueChangeMode.LAZY);


        HorizontalLayout searchFields = new HorizontalLayout(nameFilter, searchFilter, amountFilter);

        bankGrid.setColumns("date", "statementInternalID", "debit", "credit", "balance", "currency");

        HorizontalLayout datePickersLayout = new HorizontalLayout(startDatePicker, endDatePicker, filterButton);
        datePickersLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        H2 bankTitle = new H2("Bank Statements");
        FlexLayout bankLayout = new FlexLayout(bankTitle, datePickersLayout, searchFields, bankGrid);
        bankLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        bankLayout.setWidth("40%");

        return bankLayout;
    }

    private Component createMatchLayout() {
        Button matchButton = new Button("Create Match");
        Button splitButton = new Button("Split Amount");

        VerticalLayout matchLayout = new VerticalLayout(matchButton, splitButton);
        matchLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        matchLayout.setAlignItems(Alignment.CENTER);
        matchLayout.setHeightFull();
        matchLayout.setWidth("20%");
        return matchLayout;
    }

    private void createMatch() {
        Set<InvoiceResultSummery> selectedInvoices = invoiceGrid.getSelectedItems();
        Set<BankStatementLine> selectedBankStatements = bankGrid.getSelectedItems();

        double invoiceTotal = selectedInvoices.stream().mapToDouble(InvoiceResultSummery::getTotalAmount).sum();
        double bankTotal = selectedBankStatements.stream().mapToDouble(bank -> bank.getDebit() - bank.getCredit()).sum();

        if (invoiceTotal == bankTotal) {
            Notification.show("Match created successfully!");
        } else {
            Notification.show("Amounts do not match, please split the amount.");
        }
    }

    private void fetchBankData() {
        String baseUrl = "https://store-service.xion.ai/operations/getBankRecordsInDateRange";
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("company_id", "18087d5f-4405-4890-a30d-79aa7534c56d")
                .queryParam("bank_name", "DBS")
                .queryParam("start_date", "2024")
                .queryParam("end_date", "2024");
        String url = uriBuilder.toUriString();

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);
        System.out.println(response);

    }


    private void createSplit() {
        Notification.show("Not implemented yet");

    }


}
