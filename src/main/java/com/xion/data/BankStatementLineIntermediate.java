package com.xion.data;

import com.xion.resultObjectModel.resultSummeries.bank.BankStatementLine;
import com.xion.resultObjectModel.resultSummeries.bank.BankStatementLineStatus;
import com.xion.util.Status;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BankStatementLineIntermediate {
    private String account;
    private double balance;
    private String comments;
    private List<String> costCenter;
    private double credit;
    private String currency;
    private String customerComment;
    private Date date;
    private double debit;
    private String description;
    private String fileURL;
    private String matchID;
    private String name;
    private BankStatementLineStatus status;
    private String txnType;
    private Long id;
    private Integer lineNumber;
    private Long parentId;
    private Boolean saved;
    private Boolean split;
    private String statementInternalID;
    private List<Long> accountID;
    private Boolean xiboComments;
    // List<Long> documentReconcilationIds;

//    public List<Long> getDocumentReconcilationIds() {
//        return documentReconcilationIds;
//    }

//    public void setDocumentReconcilationIds(List<Long> documentReconcilationIds) {
//        this.documentReconcilationIds = documentReconcilationIds;
//    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public List<String> getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(List<String> costCenter) {
        this.costCenter = costCenter;
    }

    public double getCredit() {
        return credit;
    }

    public void setCredit(double credit) {
        this.credit = credit;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCustomerComment() {
        return customerComment;
    }

    public void setCustomerComment(String customerComment) {
        this.customerComment = customerComment;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getDebit() {
        return debit;
    }

    public void setDebit(double debit) {
        this.debit = debit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileURL() {
        return fileURL;
    }

    public void setFileURL(String fileURL) {
        this.fileURL = fileURL;
    }

    public String getMatchID() {
        return matchID;
    }

    public void setMatchID(String matchID) {
        this.matchID = matchID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BankStatementLineStatus getStatus() {
        return status;
    }

    public void setStatus(BankStatementLineStatus status) {
        this.status = status;
    }

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Boolean getSaved() {
        return saved;
    }

    public void setSaved(Boolean saved) {
        this.saved = saved;
    }

    public Boolean getSplit() {
        return split;
    }

    public void setSplit(Boolean split) {
        this.split = split;
    }

    public String getStatementInternalID() {
        return statementInternalID;
    }

    public void setStatementInternalID(String statementInternalID) {
        this.statementInternalID = statementInternalID;
    }

    public List<Long> getAccountID() {
        return accountID;
    }

    public void setAccountID(String accountID) {
        if (accountID == null || accountID.isEmpty()) {
            this.accountID = new ArrayList<>();
        }
    }

    public void setAccountID(List<Long> accountID) {
        this.accountID = accountID;
    }

    public Boolean getXiboComments() {
        return xiboComments;
    }

    public void setXiboComments(Boolean xiboComments) {
        this.xiboComments = xiboComments;
    }



}
