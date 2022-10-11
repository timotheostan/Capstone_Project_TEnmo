package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.model.Transfer;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("isAuthenticated()")
@RestController
public class TransferController {

    private TransferDao transferDao;

    public TransferController (TransferDao transferDao) {
        this.transferDao = transferDao;
    }

    //Creates a pending send transaction.
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "new-transfer")
    public Transfer newTransfer(@RequestBody Transfer transfer) {
        Transfer sendTransfer = transferDao.createTransfer(transfer.getTypeId(), transfer.getStatusId(), transfer.getAccountFrom(), transfer.getAccountTo(), transfer.getAmount());
        transferDao.transferFunds(sendTransfer);
        return sendTransfer;
    }

    //Change specified transfer's status to "approved" and initiate transaction.
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PutMapping(path = "new-transfer/approved")
    public void transferFunds(@RequestBody Transfer transfer) {
        transferDao.transferFunds(transfer);
    }

    //Return a transfer with specified ID.
    @GetMapping(path = "transfers/{id}")
    public Transfer viewTransferById(@PathVariable("id") int transferId) {
        return transferDao.viewTransfer(transferId);
    }

    //Return a list of transfers related to specified user ID.
    @GetMapping(path = "users/{id}/transfers")
    public List<Transfer> getTransferHistory(@PathVariable int id){
        return transferDao.transferHistory(id);
    }

}