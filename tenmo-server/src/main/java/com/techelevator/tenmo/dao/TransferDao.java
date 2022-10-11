package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;

import java.math.BigDecimal;
import java.util.List;

public interface TransferDao {

    public List<Transfer> transferHistory(int accountId);

    public Transfer viewTransfer(int transferId);

    public Transfer createTransfer(int type, int status, int from, int to, BigDecimal amount);

    void transferFunds(Transfer transfer);
}