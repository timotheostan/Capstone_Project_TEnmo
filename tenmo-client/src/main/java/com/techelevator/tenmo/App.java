package com.techelevator.tenmo;

import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.ConsoleService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Scanner;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";

    //Create a new client for this class.
    RestTemplate restTemplate = new RestTemplate();

    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);

    private AuthenticatedUser currentUser;

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
            mainMenu();
        }
    }
    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleRegister();
            } else if (menuSelection == 2) {
                handleLogin();
            } else if (menuSelection != 0) {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void handleRegister() {
        System.out.println("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            System.out.println("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser == null) {
            consoleService.printErrorMessage();
        }
    }

    private void mainMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                viewCurrentBalance();
            } else if (menuSelection == 2) {
                viewTransferHistory();
            } else if (menuSelection == 3) {
                viewPendingRequests();
            } else if (menuSelection == 4) {
                sendBucks();
            } else if (menuSelection == 5) {
                requestBucks();
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }

	private void viewCurrentBalance() {
		// TODO Auto-generated method stub
        System.out.println("Your current account balance is: $" + getCurrentBalance());
	}

    private BigDecimal getCurrentBalance() {
        HttpEntity<Object> entity = getEntity();
        BigDecimal balance = restTemplate.exchange(
                API_BASE_URL + "users/" + currentUser.getUser().getId() + "/balance", HttpMethod.GET, entity, BigDecimal.class).getBody();
        return balance;
    }

	private void viewTransferHistory() {
		// TODO Auto-generated method stub
        //Query for an array of all transfers related to current user.
        HttpEntity<Object> entity = getEntity();
        Transfer[] transferArray = restTemplate.exchange(
                API_BASE_URL + "users/" + currentUser.getUser().getId() + "/transfers", HttpMethod.GET, getEntity(), Transfer[].class).getBody();
        assert transferArray != null;

        //Go through each entry of the above array, retrieve details summary, and print out list of past transfers with the prompted option of requesting further details or cancelling.
        System.out.println("-------------------------------------------\n" + "Transfers\n" + "ID          From/To                 Amount\n" + "-------------------------------------------\n");

        for (Transfer transfer : transferArray) {
            int transferId = transfer.getId();

            String transferType = "";
            int transferTypeId = transfer.getTypeId();
            String otherUserName;
            int otherUserAccountId = 0;

            if (transferTypeId == 1 && accountIdUserIdMap().get(transfer.getAccountTo()) == currentUser.getUser().getId().intValue()) {
                transferType = "From: ";
                otherUserAccountId = transfer.getAccountFrom();
            }
            else if (transferTypeId == 1 && accountIdUserIdMap().get(transfer.getAccountFrom()) == currentUser.getUser().getId().intValue()) {
                transferType = "To: ";
                otherUserAccountId = transfer.getAccountTo();
            }
            else if (transferTypeId == 2 && accountIdUserIdMap().get(transfer.getAccountTo()) == currentUser.getUser().getId().intValue()) {
                transferType = "From: ";
                otherUserAccountId = transfer.getAccountFrom();
            }
            else if (transferTypeId == 2 && accountIdUserIdMap().get(transfer.getAccountFrom()) == currentUser.getUser().getId().intValue()) {
                transferType = "To: ";
                otherUserAccountId = transfer.getAccountTo();
            }

            int otherUserId = accountIdUserIdMap().get(otherUserAccountId);
            otherUserName = viewUsers().get(otherUserId);

            BigDecimal amount = transfer.getAmount();

            System.out.println(transferId + "          " + transferType + " " + otherUserName + "          $ " + amount);
        }

        System.out.println("---------\n" + "Please enter transfer ID to view details (0 to cancel):");

        //Prompt user for transaction ID and retrieve details.
        int transferDetailsId;
        String userFrom = "";
        String userTo = "";
        String transactionType = "";
        int transactionTypeId;
        String status = "";
        int statusId;
        BigDecimal amount = BigDecimal.valueOf(0);

        try {
            Scanner transferDetailsIdInput = new Scanner(System.in);
            transferDetailsId = transferDetailsIdInput.nextInt();
            if (transferDetailsId != 0) {
                    for (Transfer transfer : transferArray) {
                        if (transfer.getId() == transferDetailsId) {
                            userFrom = viewUsers().get(accountIdUserIdMap().get(transfer.getAccountFrom()));

                            userTo = viewUsers().get(accountIdUserIdMap().get(transfer.getAccountTo()));

                            transactionTypeId = transfer.getTypeId();
                            if (transactionTypeId == 1) {
                                transactionType = "Request";
                            }
                            else if (transactionTypeId == 2) {
                                transactionType = "Send";
                            }

                            statusId = transfer.getStatusId();
                            if (statusId == 1) {
                                status = "Pending";
                            }
                            else if (statusId == 2) {
                                status = "Approved";
                            }
                            else if (statusId == 3) {
                                status = "Rejected";
                            }

                            amount = transfer.getAmount();
                        }
                    }
                //Print out details for selected transaction.
                System.out.println("--------------------------------------------\n" + "Transfer Details\n" + "--------------------------------------------\n");
                System.out.println("Id: " + transferDetailsId + "\n" + "From: " + userFrom + "\n" + "To: " + userTo + "\n" +
                        "Type: " + transactionType + "\n" + "Status: " + status + "\n" + "Amount: $" + amount);
                }
        }
        catch (NumberFormatException | InputMismatchException e) {
            System.err.println("Invalid input.");
        }

	}

	private void viewPendingRequests() {
		// TODO Auto-generated method stub
		
	}

	private void sendBucks() {
		// TODO Auto-generated method stub
        //Print out list of users with associated user IDs.
        System.out.println("-------------------------------------------\n" + "Users\n" + "ID            Name\n" + "-------------------------------------------");
        viewUsers().forEach((key, value) -> System.out.println(key + "          " + value));
        System.out.println("-------------------------------------------\n");

        //Prompt user for ID of recipient (or 0 to cancel).
        System.out.println("Enter ID of user you are sending to (0 to cancel):");

        int recipientId = 0;

        try {
            Scanner recipientIdInput = new Scanner(System.in);
            recipientId = recipientIdInput.nextInt();
            if (recipientId != 0) {
                //Check if user is attempting to send money to self.
                if (recipientId == currentUser.getUser().getId().intValue()) {
                    System.err.println("You cannot send money to yourself.");
                }
                //Check if recipient is valid choice.
                if (!viewUsers().containsKey(recipientId)) {
                    System.err.println("Invalid ID; no user found.");
                }
            }
            else {
                mainMenu();
            }
        }
        catch (NumberFormatException | InputMismatchException e) {
            System.err.println("Invalid input.");
        }

        //Prompt user to input amount to be transferred.
        System.out.println("Enter amount:");

        BigDecimal sendAmount = null;

        try {
            Scanner sendAmountInput = new Scanner(System.in);
            sendAmount = sendAmountInput.nextBigDecimal();
            //Check if send amount is a positive number.
            if (sendAmount.compareTo(new BigDecimal(0)) > 0) {
                //Check user has sufficient funds for send transaction.
                BigDecimal balance = getCurrentBalance();
                if (balance.compareTo(sendAmount) < 0) {
                    System.out.println("Insufficient funds.");
                }
            }
            else {
                System.err.println("You cannot send a zero or negative amount of money.");
            }
        }
        catch (NumberFormatException | InputMismatchException e) {
            System.err.println("Invalid input.");
        }

        //Initiate transfer transaction.
        Transfer send = new Transfer(2, 2, currentUser.getUser().getId().intValue(), recipientId, sendAmount);

        String token = currentUser.getToken();
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.setBearerAuth(token);
        HttpEntity<Transfer> transferHttpEntity = new HttpEntity<>(send, header);

        try {
            Transfer postSend = restTemplate
                    .exchange(API_BASE_URL + "new-transfer", HttpMethod.POST, transferHttpEntity, Transfer.class).getBody();
            System.out.println("Transfer complete.");
        } catch (Exception e) {
            System.out.println("Cannot process transfer. Please try again.");
        }
	}

	private void requestBucks() {
		// TODO Auto-generated method stub
		
	}

    private HashMap<Integer, String> viewUsers() {
        //Create a HashMap with user IDs as keys, and usernames as values.
        HashMap<Integer, String> usersMap = new HashMap<>();
        int userId;
        String name;

        //Query for array of all Users.
        HttpEntity<Object> entity = getEntity();
        User[] userArray = restTemplate.exchange(API_BASE_URL + "users/", HttpMethod.GET, entity, User[].class).getBody();
        assert userArray != null;

        //Go through each entry of the above array and add them to the Hashmap of IDs/Usernames.
        for (User user : userArray) {
            userId = user.getId().intValue();
            name = user.getUsername();
            usersMap.put(userId, name);
        }
        return usersMap;
    }

    private HashMap<Integer, Integer> accountIdUserIdMap() {
        //Create a HashMap with account IDs as keys, and user IDs as values.
        HashMap<Integer, Integer> accountUserMap = new HashMap<>();
        int accountId;
        int userId;

        //Query for array of all Accounts.
        HttpEntity<Object> entity = getEntity();
        Account[] accountArray = restTemplate.exchange(API_BASE_URL + "accounts/", HttpMethod.GET, entity, Account[].class).getBody();
        assert accountArray != null;

        //Go through each entry of the above array and add them to the Hashmap of IDs/Usernames.
        for (Account account : accountArray) {
            accountId = account.getAccountId();
            userId = account.getUserId();
            accountUserMap.put(accountId, userId);
        }
        return accountUserMap;
    }

    private HttpEntity<Object> getEntity() {
        // Create a general method to return the authorised entity whenever required.
        String token = currentUser.getToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }

}