package com.xion.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xion.resultObjectModel.resultSummeries.DocumentType;
import com.xion.util.Status;

import javax.print.Doc;
import java.util.Date;
import java.util.List;

public class InvoiceResultSummeryIntermediate {
    private DocumentType type;
    private String name;
    private Long universalDocumentId;
    private String companyId;
    private Long id;
    private List<String> labels;
    private Status status;
    private String notes;
    @JsonFormat(pattern = "MM-dd-yyyy@HH:mm:ss")
    private Date creationDate;
    private Boolean flag;
    private Boolean reviewed;
    private Boolean gl;
    private List<String> matchId;
    private String documentClassification;

    private Double preTaxAmount;
    private Double taxAmount;
    private String supplierName;
    private String customerName;
    private String supplierAddress;
    private String customerAddress;
    private String invoiceNumber;
    private String dueOn;
    private String poNumber;
    private String gstNumber;
    private Double totalAmount;
    private String currency;

    @JsonFormat(pattern = "MM-dd-yyyy")
    private Date date;

    public DocumentType getType() {
        return type;
    }

    public void setType(String type) {
        if (type == null || type.isEmpty()) {
            this.type = null;
        } else {
            this.type = DocumentType.valueOf(type);
        }

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getUniversalDocumentId() {
        return universalDocumentId;
    }

    public void setUniversalDocumentId(Long universalDocumentId) {
        this.universalDocumentId = universalDocumentId;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (status == null || status.isEmpty()) {
            this.status = null;
        } else {
            this.status = Status.valueOf(status);
        }

    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Boolean getFlag() {
        return flag;
    }

    public void setFlag(Boolean flag) {
        this.flag = flag;
    }

    public Boolean getReviewed() {
        return reviewed;
    }

    public void setReviewed(Boolean reviewed) {
        this.reviewed = reviewed;
    }

    public Boolean getGl() {
        return gl;
    }

    public void setGl(Boolean gl) {
        this.gl = gl;
    }

    public List<String> getMatchId() {
        return matchId;
    }

    public void setMatchId(List<String> matchId) {
        this.matchId = matchId;
    }

    public String getDocumentClassification() {
        return documentClassification;
    }

    public void setDocumentClassification(String documentClassification) {
        this.documentClassification = documentClassification;
    }

    public Double getPreTaxAmount() {
        return preTaxAmount;
    }

    public void setPreTaxAmount(Double preTaxAmount) {
        this.preTaxAmount = preTaxAmount;
    }

    public Double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(Double taxAmount) {
        this.taxAmount = taxAmount;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getSupplierAddress() {
        return supplierAddress;
    }

    public void setSupplierAddress(String supplierAddress) {
        this.supplierAddress = supplierAddress;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getDueOn() {
        return dueOn;
    }

    public void setDueOn(String dueOn) {
        this.dueOn = dueOn;
    }

    public String getPoNumber() {
        return poNumber;
    }

    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
    }

    public String getGstNumber() {
        return gstNumber;
    }

    public void setGstNumber(String gstNumber) {
        this.gstNumber = gstNumber;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
