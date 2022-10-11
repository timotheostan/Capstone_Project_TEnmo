package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@PreAuthorize("isAuthenticated()")
@RestController
public class AccountController {

    private AccountDao accountDao;
    private UserDao userDao;

    public AccountController (AccountDao accountDao, UserDao userDao) {
        this.accountDao = accountDao;
        this.userDao = userDao;
    }

    //Return a list of all users.
    @GetMapping(path = "users")
    public List<User> list() {
        return userDao.findAll();
    }

    //Return a user with specified user ID.
    @GetMapping(path = "user-ids")
    public User findUserById(@RequestParam int id) {
        User user = userDao.findUserById(id);
        return user;
    }

    //Return a user with specified username.
    @GetMapping(path = "user-names")
    public User findUserByUsername(@RequestParam String username) {
        User user = userDao.findByUsername(username);
        return user;
    }

    //Return the account balance of authorised user.
    @GetMapping(path = "users/{id}/balance")
    public BigDecimal viewBalance(@PathVariable int id) {
        return accountDao.viewBalance(id);
    }

    @GetMapping(path = "accounts")
    public List<Account> accountList() {
        return accountDao.findAllAccounts();
    }

}
