package com.xion.views2.list;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.xion.data.BankStatementLineIntermediate;
import com.xion.data.FetchDataService;
import com.xion.data.InvoiceResultSummeryIntermediate;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReconciliationDialogues {

    private List<BankStatementLineIntermediate> bankData;
    private static SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

    public static void openCreateInvoiceDialog(Grid<BankStatementLineIntermediate> bankGrid,
                                               Grid<InvoiceResultSummeryIntermediate> invoiceGrid) {
        BankStatementLineIntermediate selectedBankStatement = null;
        if (bankGrid.getSelectedItems().size() == 1) {
            selectedBankStatement = bankGrid.getSelectedItems().stream().collect(Collectors.toList()).get(0);
        }
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);
        dialog.setHeight(80, Unit.VH);
        dialog.setWidth(80, Unit.VW);
        VerticalLayout mainView = new VerticalLayout();
        H2 mainViewTitle = new H2("Create Invoice");

        VerticalLayout gridView = new VerticalLayout();

        Grid<InvoiceResultSummeryIntermediate> selectedInvoiceGrid = new Grid<>();
        Grid<BankStatementLineIntermediate> selectedBankGrid = new Grid<>();

        H2 bankTitle = new H2("Bank Statements");
        H2 invoiceTitle = new H2("Created Invoices");

        selectedBankGrid.removeAllColumns();
        selectedBankGrid.addColumn(x -> formatter.format(x.getDate())).setHeader("Date");
        selectedBankGrid.addColumn(x -> x.getAccount()).setHeader("Account");
        selectedBankGrid.addColumn(x -> x.getCredit()).setHeader("Credit");
        selectedBankGrid.addColumn(x -> x.getDebit()).setHeader("Debit");
        selectedBankGrid.addColumn(x -> x.getCurrency()).setHeader("Currency");
        selectedBankGrid.addColumn(x -> isBankStatementReconciled(x) ? "Yes" : "No").setHeader("Reconciled");
        selectedBankGrid.setItems(bankGrid.getSelectedItems());
        selectedBankGrid.setHeightByRows(true);

        selectedInvoiceGrid.removeAllColumns();
        selectedInvoiceGrid.addColumn(x -> formatter.format(x.getDate())).setHeader("Date");
        selectedInvoiceGrid.addColumn(x -> x.getSupplierName()).setHeader("Supplier");
        selectedInvoiceGrid.addColumn(x -> x.getTotalAmount()).setHeader("Amount");
        selectedInvoiceGrid.addColumn(x -> x.getCurrency()).setHeader("Currency");
        selectedInvoiceGrid.addColumn(x -> isInvoiceReconciled(x) ? "Yes" : "No" ).setHeader("Reconciled");
        selectedInvoiceGrid.setHeightByRows(true);

        List<InvoiceResultSummeryIntermediate> createdInvoices = new ArrayList<>();

        gridView.add(bankTitle, selectedBankGrid, invoiceTitle, selectedInvoiceGrid);

        TextField supplierNameField = new TextField("Supplier Name");
        TextField customerField = new TextField("Customer Name");
        HorizontalLayout textFields = new HorizontalLayout(supplierNameField, customerField);

        NumberField totalAmountField = new NumberField("Total Amount");
        TextField currencyField = new TextField("Currency");
        HorizontalLayout amountLayout = new HorizontalLayout(totalAmountField, currencyField);
        DatePicker dateField = new DatePicker("Date");

        Button undoButton = new Button("Undo", e -> {
            createdInvoices.clear();
            selectedInvoiceGrid.setItems(createdInvoices);
            for (BankStatementLineIntermediate bankStatement : bankGrid.getSelectedItems()) {
                bankStatement.setAccountID(new ArrayList<>());
            }
            selectedBankGrid.setItems(bankGrid.getSelectedItems());
        });


        Button confirmButton = new Button("Confirm Creation", e -> {
            double invoiceAmount = createdInvoices.stream().mapToDouble(x -> x.getTotalAmount()).sum();
            double bankAmount = bankGrid.getSelectedItems().stream().mapToDouble(x -> x.getCredit() + x.getDebit()).sum();
            if (invoiceAmount != bankAmount) {
                Notification.show("Created Invoice amounts don't match up to bank total").setPosition(Notification.Position.MIDDLE);
            } else {
                dialog.close();
            }
        });

        HorizontalLayout buttons = new HorizontalLayout(undoButton, confirmButton);
        buttons.setVisible(false);

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

            List<Long> bankIds = new ArrayList<>();
            for (BankStatementLineIntermediate bankStatement : bankGrid.getSelectedItems()) {
                bankIds.add(bankStatement.getId());
            }
            newInvoice.setBankReconciliationIds(bankIds);
            createdInvoices.add(newInvoice);

            double invoiceAmount = createdInvoices.stream().mapToDouble(x -> x.getTotalAmount()).sum();
            double bankAmount = bankGrid.getSelectedItems().stream().mapToDouble(x -> x.getCredit() + x.getDebit()).sum();
            if (invoiceAmount == bankAmount) {
                for (BankStatementLineIntermediate bankStatement : bankGrid.getSelectedItems()) {
                    bankStatement.setAccountID(createdInvoices.stream().map(x -> x.getId()).collect(Collectors.toList()));
                }
            }

            selectedInvoiceGrid.setItems(createdInvoices);
            selectedBankGrid.setItems(bankGrid.getSelectedItems());
        });

        createButton.addClickListener(e -> buttons.setVisible(true));
        undoButton.addClickListener(e -> buttons.setVisible(false));

        mainView.add(mainViewTitle, textFields, amountLayout, dateField, createButton, buttons);
        HorizontalLayout dialogLayout = new HorizontalLayout(gridView, mainView);
        dialog.add(dialogLayout);
        dialog.open();
    }

    public static void openSplitAmountDialog(List<InvoiceResultSummeryIntermediate> invoiceData, Grid<BankStatementLineIntermediate> bankGrid,
                                      Grid<InvoiceResultSummeryIntermediate> invoiceGrid) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);
        dialog.setWidth(80, Unit.VW);
        dialog.setHeight(80, Unit.VH);

        HorizontalLayout dialogLayout = new HorizontalLayout();
        VerticalLayout centerLayout = new VerticalLayout();
        dialogLayout.setWidthFull();

        Set<BankStatementLineIntermediate> selectedBankStatements = bankGrid.getSelectedItems();

        //split amount button should only be visible if 1 invoice is selected
        assert invoiceGrid.getSelectedItems().size() == 1;
        InvoiceResultSummeryIntermediate selectedInvoice = invoiceGrid.getSelectedItems().stream().collect(Collectors.toList()).get(0);

        double bankTotal = selectedBankStatements.stream()
                .mapToDouble(bank -> bank.getCredit() + bank.getDebit()).sum();
        double invoiceTotal = selectedInvoice.getTotalAmount();

        NumberField bankAmountField = new NumberField("Bank Total Amount");
        bankAmountField.setValue(bankTotal);
        bankAmountField.setReadOnly(true);

        NumberField invoiceAmountField = new NumberField("Invoice Amount");
        invoiceAmountField.setValue(invoiceTotal);
        invoiceAmountField.setReadOnly(true);

        NumberField remainingAmountField = new NumberField("Unpaid Invoice Amount");
        remainingAmountField.setValue(invoiceTotal - bankTotal);



        VerticalLayout invoiceLayout = new VerticalLayout();
        H2 invoiceTitle = new H2("Selected Invoice");
        Grid<InvoiceResultSummeryIntermediate> selectedInvoiceGrid = new Grid<>(InvoiceResultSummeryIntermediate.class);
        selectedInvoiceGrid.setItems(selectedInvoice);

        selectedInvoiceGrid.removeAllColumns();
        selectedInvoiceGrid.addColumn(new ComponentRenderer<>(invoice -> {
            Date date = invoice.getDate();
            Span dateSpan = new Span(formatter.format(date));
            return dateSpan;
        }
        )).setHeader("Date");

        selectedInvoiceGrid.addColumn(x -> x.getSupplierName()).setHeader("Supplier");
        selectedInvoiceGrid.addColumn(x -> x.getTotalAmount()).setHeader("Amount");
        selectedInvoiceGrid.addColumn(x -> x.getCurrency()).setHeader("Currency");

        selectedInvoiceGrid.addColumn(new ComponentRenderer<>(invoice -> {
            boolean reconciled = isInvoiceReconciled(invoice);
            Span reconciledSpan = new Span(reconciled ? "Yes" : "No");
            return reconciledSpan;
        })).setHeader("Reconciled");

        selectedInvoiceGrid.setHeightByRows(true);
        invoiceLayout.add(invoiceTitle, selectedInvoiceGrid);

        VerticalLayout bankLayout = new VerticalLayout();
        H2 bankTitle = new H2("Selected Bank Statements");
        Grid<BankStatementLineIntermediate> selectedBankGrid = new Grid<>(BankStatementLineIntermediate.class);
        selectedBankGrid.setItems(selectedBankStatements);

        selectedBankGrid.removeAllColumns();
        selectedBankGrid.addColumn(new ComponentRenderer<>(bankStatement -> {
            Date date = bankStatement.getDate();
            Span dateSpan = new Span(formatter.format(date));
            return dateSpan;
        }
        )).setHeader("Date");
        selectedBankGrid.addColumn(x -> x.getAccount()).setHeader("Account");
        selectedBankGrid.addColumn(x -> x.getCredit()).setHeader("Credit");
        selectedBankGrid.addColumn(x -> x.getDebit()).setHeader("Debit");
        selectedBankGrid.addColumn(x -> x.getCurrency()).setHeader("Currency");
        selectedBankGrid.addColumn(new ComponentRenderer<>(x -> {
            boolean reconciled = isBankStatementReconciled(x);
            Span reconciledSpan = new Span(reconciled ? "Yes" : "No");
            return reconciledSpan;
        })).setHeader("Reconciled");

        selectedBankGrid.setHeightByRows(true);


        bankLayout.add(bankTitle, selectedBankGrid);

        VerticalLayout grids = new VerticalLayout(invoiceLayout, bankLayout);

        Double originalInvoiceAmount = selectedInvoice.getTotalAmount();

        Button undoButton = new Button("Undo", e -> {
            selectedInvoice.setTotalAmount(originalInvoiceAmount);
            selectedInvoice.setBankReconciliationIds(new ArrayList<>());
            selectedBankStatements.forEach(x -> x.setAccountID(new ArrayList<>()));
            selectedInvoiceGrid.setItems(selectedInvoice);
            selectedBankGrid.setItems(selectedBankStatements);
        });

        Button confirmButton = new Button("Confirm Split", e -> {
            //save to database
            dialog.close();
        });

        HorizontalLayout buttons = new HorizontalLayout(undoButton, confirmButton);
        buttons.setVisible(false);

        Button splitButton = new Button("Split", e -> {
            if (remainingAmountField.getValue() == null) {
                Notification.show("Please enter remaining amount unpaid").setPosition(Notification.Position.MIDDLE);
                return;
            }

            InvoiceResultSummeryIntermediate newInvoice = selectedInvoice.clone();
            selectedInvoice.setTotalAmount(bankTotal);
            newInvoice.setTotalAmount(remainingAmountField.getValue());

            List<Long> bankIds = new ArrayList<>();

            for (BankStatementLineIntermediate bankStatement : selectedBankStatements) {
                bankIds.add(bankStatement.getId());
                bankStatement.setAccountID(List.of(selectedInvoice.getId()));
            }

            //set selectedInvoice as reconciled
            selectedInvoice.setBankReconciliationIds(bankIds);

            selectedInvoiceGrid.setItems(List.of(selectedInvoice, newInvoice));
            selectedBankGrid.setItems(selectedBankStatements);

            buttons.setVisible(true);
        });

        splitButton.addClickListener(e -> splitButton.setVisible(false));
        undoButton.addClickListener(e -> splitButton.setVisible(true));
        undoButton.addClickListener(e -> buttons.setVisible(false));

        centerLayout.add(new H2("Split Amounts"), invoiceAmountField, bankAmountField,
                remainingAmountField, splitButton, buttons);
        centerLayout.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        centerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        centerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        dialogLayout.add(grids, centerLayout);
        dialog.add(dialogLayout);
        dialog.open();
    }

    public static void openAISuggestionDialog(String companyId, String bankName, DatePicker startDatePickerBank,
                                              DatePicker endDatePickerBank, DatePicker startDatePickerInvoice,
                                              DatePicker endDatePickerInvoice) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        VerticalLayout dialogLayout = new VerticalLayout();

        TextArea hintTextArea = new TextArea("Rules");
    //        hintTextArea.setPlaceholder("What should our AI look out for? \n \n \n \n \n");
        String currentHints = FetchDataService.getCurrentHints();
        hintTextArea.setValue(currentHints + "\n\n\n\n\n");
        hintTextArea.setValueChangeMode(ValueChangeMode.EAGER);

        ProgressBar progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false); // Initially hidden

        hintTextArea.setWidthFull();

        Button suggestButton = new Button("Get Suggestion", e -> {
            progressBar.setVisible(true);
            String hint = hintTextArea.getValue() != null ? hintTextArea.getValue() : "";

            CompletableFuture.runAsync(() -> {
                try {
                     FetchDataService.getAISuggestion(companyId, bankName, startDatePickerBank,
                            endDatePickerBank, startDatePickerInvoice, endDatePickerInvoice, hint);


                } catch (Exception ex) {
                    // Handle any exceptions that occur during the fetching process
                    UI.getCurrent().access(() -> {
                        progressBar.setVisible(false);
                        // Optionally, show an error message to the user
                        Notification.show("Failed to fetch AI suggestion: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
                    });
                }
            });
        });


        Component audioRecorderView = new AudioRecorderView();

        Button voiceRecording = new Button ("", VaadinIcon.MICROPHONE.create());

        dialogLayout.add(new H2("AI Suggestion"), hintTextArea, progressBar, audioRecorderView, suggestButton);
        dialogLayout.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, audioRecorderView);
        dialogLayout.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, suggestButton);

        dialog.add(dialogLayout);
        dialog.open();
    }

    @Tag("audio-recorder-view")
    public static class AudioRecorderView extends HorizontalLayout {

        public AudioRecorderView() {
            Button voiceRecordingButton = new Button("", VaadinIcon.MICROPHONE.create());
            voiceRecordingButton.addClickListener(e -> {
                getUI().ifPresent(ui -> ui.getPage().executeJs("startRecording();"));
            });

            Button stopRecordingButton = new Button( VaadinIcon.STOP.create());
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

    public static void openCreateAdjustmentsDialog(Grid<BankStatementLineIntermediate> bankGrid,
                                                   Grid<InvoiceResultSummeryIntermediate> invoiceGrid) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setWidthFull();

        Set<BankStatementLineIntermediate> selectedBankStatements = bankGrid.getSelectedItems();
        Set<InvoiceResultSummeryIntermediate> selectedInvoices = invoiceGrid.getSelectedItems();

        assert selectedBankStatements.size() == 1;

        BankStatementLineIntermediate selectedBankStatement = selectedBankStatements.stream().findFirst().get();

        NumberField bankAmountField = new NumberField("Bank Total Amount");
        bankAmountField.setValue(selectedBankStatement.getCredit() + selectedBankStatement.getDebit());
        bankAmountField.setReadOnly(true);

        Double totalInvoiceAmount = selectedInvoices.stream().map(x -> x.getTotalAmount()).mapToDouble(Double::doubleValue).sum();

        NumberField differenceField = new NumberField("Difference");
        differenceField.setValue(bankAmountField.getValue() - totalInvoiceAmount);
        differenceField.setReadOnly(true);

        NumberField invoiceAmountField = new NumberField("Invoice Amount");
        invoiceAmountField.setValue(totalInvoiceAmount);
        invoiceAmountField.setReadOnly(true);

        NumberField bankFeeField = new NumberField("Bank Fee");
        bankFeeField.setValue(0.0);
        NumberField minorAdjustmentField = new NumberField("Minor Adjustment");
        minorAdjustmentField.setValue(0.0);

        bankFeeField.addValueChangeListener(e -> differenceField.setValue(bankAmountField.getValue() - totalInvoiceAmount
                - bankFeeField.getValue() - minorAdjustmentField.getValue()));

        minorAdjustmentField.addValueChangeListener(e -> differenceField.setValue(bankAmountField.getValue() - totalInvoiceAmount
                - bankFeeField.getValue() - minorAdjustmentField.getValue()));

        bankFeeField.setValueChangeMode(ValueChangeMode.EAGER);
        minorAdjustmentField.setValueChangeMode(ValueChangeMode.EAGER);

        Button adjustButton = new Button("Adjust and Create Match", e -> {
            if (differenceField.getValue() != 0) {
                Notification.show("Must match bank statement: " + bankAmountField.getValue()).setPosition(Notification.Position.MIDDLE);
                return;
            }

            List<Long> invoiceIds = new ArrayList<>();
            for (InvoiceResultSummeryIntermediate invoice : selectedInvoices) {
                invoiceIds.add(invoice.getId());
                List<Long> bankId = new ArrayList<>();
                bankId.add(selectedBankStatement.getId());
                invoice.setBankReconciliationIds(bankId);
            }
            selectedBankStatement.setAccountID(invoiceIds);

            dialog.close();
        });

        dialogLayout.add(new H2("Create Adjustments"), bankAmountField, invoiceAmountField, differenceField,
                bankFeeField, minorAdjustmentField, adjustButton);
        dialog.add(dialogLayout);
        dialog.open();
    }

    public static boolean isInvoiceReconciled(InvoiceResultSummeryIntermediate invoice) {
        return invoice.getBankReconciliationIds() != null && !invoice.getBankReconciliationIds().isEmpty();
    }

    public static boolean isBankStatementReconciled(BankStatementLineIntermediate bankStatement) {
        return bankStatement.getAccountID() != null && !bankStatement.getAccountID().isEmpty();
    }


}
