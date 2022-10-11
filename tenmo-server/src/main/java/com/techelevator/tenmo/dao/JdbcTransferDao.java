package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao{

    private JdbcTemplate jdbcTemplate;

    public JdbcTransferDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Transfer> transferHistory(int accountId) {
        List<Transfer> transferList = new ArrayList<Transfer>();

        //Query for relevant details from SQL table and create new Transfer object to add to history List.
        String sqlSelectTransferHistory = "SELECT * FROM transfer WHERE account_from = (SELECT account_id FROM account WHERE user_id = ?) OR account_to = (SELECT account_id FROM account WHERE user_id = ?)";
        SqlRowSet resultSelectTransferHistory = jdbcTemplate.queryForRowSet(sqlSelectTransferHistory, accountId, accountId);
        while (resultSelectTransferHistory.next()) {
            int id = resultSelectTransferHistory.getInt("transfer_id");
            int typeId = resultSelectTransferHistory.getInt("transfer_type_id");
            int statusId = resultSelectTransferHistory.getInt("transfer_status_id");
            int accountFrom = resultSelectTransferHistory.getInt("account_from");
            int accountTo = resultSelectTransferHistory.getInt("account_to");
            BigDecimal amount = resultSelectTransferHistory.getBigDecimal("amount");

            Transfer transfer = new Transfer(id, typeId, statusId, accountFrom, accountTo, amount);
            transferList.add(transfer);
        }
        return transferList;
    }

    @Override
    public Transfer viewTransfer(int transferId) {
        Transfer transfer = null;

        //Query for relevant details from SQL table to create and return new Transfer object.
        String sqlSelectViewTransfer = "SELECT * FROM transfer WHERE transfer_id = ?";
        SqlRowSet resultSelectViewTransfer = jdbcTemplate.queryForRowSet(sqlSelectViewTransfer, transferId);
        while (resultSelectViewTransfer.next()) {
            int id = resultSelectViewTransfer.getInt("transfer_id");
            int typeId = resultSelectViewTransfer.getInt("transfer_type_id");
            int statusId = resultSelectViewTransfer.getInt("transfer_status_id");
            int accountFrom = resultSelectViewTransfer.getInt("account_from");
            int accountTo = resultSelectViewTransfer.getInt("account_to");
            BigDecimal amount = resultSelectViewTransfer.getBigDecimal("amount");

            transfer = new Transfer(id, typeId, statusId, accountFrom, accountTo, amount);
        }
        return transfer;
    }

    @Override
    public Transfer createTransfer(int type, int status, int from, int to, BigDecimal amount) {
        int id = 0;

        //Update SQL table to insert new Transfer object.
        String sqlInsertNewTransfer = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) VALUES (?, ?, (SELECT account_id FROM account WHERE user_id = ?), (SELECT account_id FROM account WHERE user_id = ?), ?)";
        jdbcTemplate.update(sqlInsertNewTransfer, type, status, from, to, amount);

        //Query for ID of the latest transfer transaction and assign to the new Transfer object.
        String sqlSelectLatestTransfer = "SELECT transfer_id FROM transfer ORDER BY transfer_id DESC LIMIT 1";
        SqlRowSet result = jdbcTemplate.queryForRowSet(sqlSelectLatestTransfer);
        while (result.next()) {
            id = result.getInt("transfer_id");
        }

        Transfer transfer = new Transfer(id, type, status, from, to, amount);
        return transfer;
    }

    @Override
    public void transferFunds(Transfer transfer) {
        BigDecimal fromBalance = new BigDecimal(0);
        BigDecimal toBalance = new BigDecimal(0);
        BigDecimal transferAmount = transfer.getAmount();

        //Query for balance value of origin account from SQL table.
        String sqlSelectTransferFrom = "SELECT balance FROM account WHERE account_id = (SELECT account_id FROM account WHERE user_id = ?)";
        SqlRowSet resultSelectTransferFrom = jdbcTemplate.queryForRowSet(sqlSelectTransferFrom, transfer.getAccountFrom());
        while (resultSelectTransferFrom.next()) {
            fromBalance = resultSelectTransferFrom.getBigDecimal("balance");
        }

        //Query for balance value of destination account from SQL table.
        String sqlSelectTransferTo = "SELECT balance FROM account WHERE account_id = (SELECT account_id FROM account WHERE user_id = ?)";
        SqlRowSet resultSelectTransferTo = jdbcTemplate.queryForRowSet(sqlSelectTransferTo, transfer.getAccountTo());
        while (resultSelectTransferTo.next()) {
            toBalance = resultSelectTransferTo.getBigDecimal("balance");
        }

        //Reassign respective balance values for origin and destination accounts following transfer transaction and update SQL table.
        assert fromBalance != null;
        BigDecimal fromBalanceResult = fromBalance.subtract(transferAmount);
        assert toBalance != null;
        BigDecimal toBalanceResult = toBalance.add(transferAmount);

        String sqlTransferFrom = "UPDATE account SET balance = ? WHERE account_id = (SELECT account_id FROM account WHERE user_id = ?)";
        jdbcTemplate.update(sqlTransferFrom, fromBalanceResult, transfer.getAccountFrom());

        String sqlTransferTo = "UPDATE account SET balance = ? WHERE account_id = (SELECT account_id FROM account WHERE user_id = ?)";
        jdbcTemplate.update(sqlTransferTo, toBalanceResult, transfer.getAccountTo());

        //Update transfer status for transaction to "approved".
        String sqlUpdateTransferStatus = "UPDATE transfer SET transfer_status_id = 2 WHERE transfer_id = ?";
        jdbcTemplate.update(sqlUpdateTransferStatus, transfer.getId());
    }

}