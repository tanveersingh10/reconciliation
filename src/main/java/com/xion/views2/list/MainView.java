package com.xion.views2.list;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import com.xion.components.BankStatementResultSummaryService;
import com.xion.components.InvoiceResultSummeryService;
import com.xion.data.BankStatementLineDeserializer;
import com.xion.data.InvoiceResultSummeryIntermediate;
import com.xion.resultObjectModel.resultSummeries.InvoiceResultSummery;
import com.xion.resultObjectModel.resultSummeries.bank.BankStatementLine;
import com.xion.resultObjectModel.resultSummeries.bank.BankStatementResultSummary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    public MainView(BankStatementLineService bankStatementLineService, InvoiceResultSummeryService invoiceResultSummeryService) throws Exception {
        setSpacing(false);
        fetchBankData();


        this.bankStatementLineService = bankStatementLineService;
        this.invoiceResultSummeryService = invoiceResultSummeryService;


        LocalDate startDate = LocalDate.of(2022, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 4, 30);
        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

//        boolean x = bankStatementLineService.existsInRangeForCompany("18087d5f-4405-4890-a30d-79aa7534c56d",
//                start, end);
//        System.out.println(x);

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
            Notification.show("filtered data");
        }
    }

    private Component createInvoiceLayout() throws JsonProcessingException {
        invoiceData = pullInvoiceData("", "xion_ai_pte_ltd", "01-01-2024", "31-01-2024");
        invoiceGrid.setItems(invoiceData);
        invoiceGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        invoiceGrid.setColumns("invoiceNumber", "supplierName", "totalAmount", "currency", "dueOn");
        H2 invoiceTitle = new H2("Invoices");
        VerticalLayout invoiceLayout = new VerticalLayout(invoiceTitle, invoiceGrid);
        invoiceLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        invoiceLayout.setWidth("40%");
        return invoiceLayout;
    }

    private Component createBankLayout() {
        //fetchBankData();
        bankGrid.setSelectionMode(Grid.SelectionMode.MULTI);

        Button filterButton = new Button("Search", e -> filterBankDataByDate());

        nameFilter = new TextField();
        nameFilter.setPlaceholder("Filter by name");
        nameFilter.setValueChangeMode(ValueChangeMode.LAZY);

        searchFilter = new TextField();
        searchFilter.setPlaceholder("Filter by search");
        searchFilter.setValueChangeMode(ValueChangeMode.LAZY);

        amountFilter = new TextField();
        amountFilter.setPlaceholder("Filter by amount");
        amountFilter.setValueChangeMode(ValueChangeMode.LAZY);


        HorizontalLayout searchFields = new HorizontalLayout(nameFilter, searchFilter, amountFilter);

        bankGrid.setColumns("date", "account", "debit", "credit", "currency");

        HorizontalLayout datePickersLayout = new HorizontalLayout(startDatePicker, endDatePicker, filterButton);
        datePickersLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        H2 bankTitle = new H2("Bank Statements");
        FlexLayout bankLayout = new FlexLayout(bankTitle, datePickersLayout, searchFields, bankGrid);
        bankLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        bankLayout.setWidth("50%");
        bankGrid.setItems(bankData);

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


    private void fetchBankData() {

        String baseUrl = "http://ops-bg-flask.xion.ai/get_bank_data";
        RestTemplate restTemplate = new RestTemplate();
        String uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("company_id", "xion_ai_pte_ltd")
                .queryParam("bank_name", "DBS SGD")
                .queryParam("start_date", "01-01-2024")
                .queryParam("end_date", "31-01-2024")
                .build()
                .toString();

        ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            ObjectMapper objectMapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addDeserializer(BankStatementLine.class, new BankStatementLineDeserializer());
            objectMapper.registerModule(module);
            String responseBody = response.getBody();
            try {
                JsonNode rootNode = objectMapper.readTree(responseBody);
                // Extract the array node for "DBS SGD"
                JsonNode dbsSgdNode = rootNode.path("DBS SGD");
                bankData = objectMapper.readValue(dbsSgdNode.toString(), new TypeReference<List<BankStatementLine>>() {});
            } catch (Exception e){
                e.printStackTrace();
                System.out.println("Error parsing JSON response.");
            }
        } else {
            System.out.println("Error: " + response.getStatusCode());
        }
    }




    public String getToken(String scope) {
        RestTemplate restTemplate = new RestTemplate();

        String clientId = "0oa119mjqmhhpR1dv357";
        String clientSecret = "XzRM4hQc2CR8gCgKOPX2E1mf2hAi9Xdh4NCpHNXi";
        String tokenUrl = "https://dev-754943.okta.com/oauth2/default/v1/token";

        String auth = clientId + ":" + clientSecret;
        byte[] encodedAuth = Base64.encode(auth.getBytes(StandardCharsets.US_ASCII));
        String authHeader = "Basic " + new String(encodedAuth);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", authHeader);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("scope", scope);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                tokenUrl, HttpMethod.POST, entity, Map.class);


        Map<String, Object> responseBody = response.getBody();
        assert responseBody != null;
        return (String) responseBody.get("access_token");
    }

    public ArrayList<InvoiceResultSummery> pullInvoiceData(String pipelineState, String companyId, String startDate, String endDate) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        String token = getToken("vault_store");
        String baseUrl = "http://store-service.xion.ai";
        String GENERAL_PULL_DQM = baseUrl + "/dqm/generalPull";

        if (pipelineState.isEmpty()) pipelineState = "ACCOUNTANT_REVIEW";

        Map<String, Object> jsonRequest = new HashMap<>();
        jsonRequest.put("pipelineState", pipelineState);
        jsonRequest.put("companyId", companyId);
        jsonRequest.put("startDate", startDate);
        jsonRequest.put("endDate", endDate);
        jsonRequest.put("mergedReview", true);
        jsonRequest.put("docDate", true);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "bearer " + token);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(jsonRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange(GENERAL_PULL_DQM, HttpMethod.POST, entity, String.class);
        String jsonString = response.getBody();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JsonNode rootNode = objectMapper.readTree(jsonString);
        JsonNode dataNode = rootNode.path("payload").path("data");
        ArrayList<InvoiceResultSummery> invoices = new ArrayList<>();

        if (dataNode.isArray()) {
            for (JsonNode node : dataNode) {
                JsonNode resultSummeryNode = node.path("resultSummery");
                InvoiceResultSummeryIntermediate dto = objectMapper.treeToValue(resultSummeryNode, InvoiceResultSummeryIntermediate.class);

                InvoiceResultSummery invoice = new InvoiceResultSummery();

                invoice.setType(dto.getType());
                invoice.setName(dto.getName());
                invoice.setUniversalDocumentId(dto.getUniversalDocumentId());
                invoice.setCompanyId(dto.getCompanyId());
                invoice.setId(dto.getId());
                invoice.setLabels(dto.getLabels());
                invoice.setStatus(dto.getStatus());
                invoice.setNotes(dto.getNotes());
                invoice.setDate(dto.getDate());
                invoice.setCreationDate(dto.getCreationDate());
                invoice.setFlag(dto.getFlag());
                invoice.setReviewed(dto.getReviewed());
                invoice.setGl(dto.getGl());
                invoice.setMatchId(dto.getMatchId().toString());
                invoice.setDocumentClassification(dto.getDocumentClassification());

                invoice.setPreTaxAmount(dto.getPreTaxAmount());
                invoice.setTaxAmount(dto.getTaxAmount());
                invoice.setSupplierName(dto.getSupplierName());
                invoice.setCustomerName(dto.getCustomerName());
                invoice.setSupplierAddress(dto.getSupplierAddress());
                invoice.setCustomerAddress(dto.getCustomerAddress());
                invoice.setInvoiceNumber(dto.getInvoiceNumber());
                invoice.setDueOn(dto.getDueOn());
                invoice.setPoNumber(dto.getPoNumber());
                invoice.setGstNumber(dto.getGstNumber());
                invoice.setTotalAmount(dto.getTotalAmount());
                invoice.setCurrency(dto.getCurrency());

                invoices.add(invoice);
            }


        }

        return invoices;

    }






}
