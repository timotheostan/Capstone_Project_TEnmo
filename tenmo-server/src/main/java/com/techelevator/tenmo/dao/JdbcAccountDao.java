package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcAccountDao implements AccountDao {

    private JdbcTemplate jdbcTemplate;

    public JdbcAccountDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public BigDecimal viewBalance(int id) {

        BigDecimal balance = BigDecimal.valueOf(0);

        //Query for balance value of specified single account user from SQL table.
        String sqlSelectBalance = "SELECT balance FROM account WHERE user_id = ?";
        SqlRowSet resultSelectBalance = jdbcTemplate.queryForRowSet(sqlSelectBalance, id);
        while (resultSelectBalance.next()) {
            balance = resultSelectBalance.getBigDecimal("balance");
        }
        return balance;
    }

    @Override
    public List<Account> findAllAccounts() {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM tenmo_user JOIN account ON tenmo_user.user_id = account.user_id;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
        while(results.next()) {
            Account account = mapRowToAccount(results);
            accounts.add(account);
        }
        return accounts;
    }

    private Account mapRowToAccount(SqlRowSet rs) {
        Account account = new Account();
        account.setAccountId(rs.getInt("account_id"));
        account.setUserId(rs.getInt("user_id"));
        account.setUserName(rs.getString("username"));
        account.setBalance(rs.getBigDecimal("balance"));
        return account;
    }
}