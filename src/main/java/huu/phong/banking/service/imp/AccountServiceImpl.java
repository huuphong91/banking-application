package huu.phong.banking.service.imp;

import huu.phong.banking.dto.AccountDto;
import huu.phong.banking.dto.TransactionDto;
import huu.phong.banking.dto.TransferFundDto;
import huu.phong.banking.entity.Account;
import huu.phong.banking.entity.Transaction;
import huu.phong.banking.exception.AccountException;
import huu.phong.banking.mapper.AccountMapper;
import huu.phong.banking.repository.AccountRepository;
import huu.phong.banking.repository.TransactionRepository;
import huu.phong.banking.service.AccountService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    private AccountRepository accountRepository;

    private TransactionRepository transactionRepository;

    private static final String TRANSACTION_TYPE_DEPOSIT = "DEPOSIT";
    private static final String TRANSACTION_TYPE_WITHDRAW = "WITHDRAW";
    private static final String TRANSACTION_TYPE_TRANSFER = "TRANSFER";

    public AccountServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public AccountDto createAccount(AccountDto accountDto) {
        Account account = AccountMapper.mapToAccount(accountDto);
        Account savedAccount = accountRepository.save(account);
        return AccountMapper.mapToAccountDto(savedAccount);
    }

    @Override
    public AccountDto getAccountById(Long id) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new AccountException("Account not found"));

        return AccountMapper.mapToAccountDto(account);
    }

    @Override
    public AccountDto deposit(Long id, double amount) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new AccountException("Account not found"));

        account.setBalance(account.getBalance() + amount);
        Account savedAccount = accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setAccountId(id);
        transaction.setAmount(amount);
        transaction.setTransactionType(TRANSACTION_TYPE_DEPOSIT);
        transaction.setTimestamp(LocalDateTime.now());

        transactionRepository.save(transaction);

        return AccountMapper.mapToAccountDto(savedAccount);
    }

    @Override
    public AccountDto withdraw(Long id, double amount) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new AccountException("Account not found"));
        if(account.getBalance() < amount) {
            throw new RuntimeException("Not enough balance");
        }

        double total = account.getBalance() - amount;
        account.setBalance(total);
        Account savedAccount = accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setAccountId(id);
        transaction.setAmount(amount);
        transaction.setTransactionType(TRANSACTION_TYPE_WITHDRAW);
        transaction.setTimestamp(LocalDateTime.now());

        transactionRepository.save(transaction);

        return AccountMapper.mapToAccountDto(savedAccount);
    }

    @Override
    public List<AccountDto> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        return accounts.stream().map(AccountMapper::mapToAccountDto).collect(Collectors.toList());
    }

    @Override
    public void deleteAccountById(Long id) {
        accountRepository.deleteById(id);
    }

    @Override
    public void transferFunds(TransferFundDto transferFundDto) {
        // retrive the account from which we send the amount
        Account fromAccount = accountRepository.findById(transferFundDto.fromAccountId()).orElseThrow(() -> new AccountException("Account not found"));

        // retrive the account to which we send the amount
        Account toAccount = accountRepository.findById(transferFundDto.toAccountId()).orElseThrow(() -> new AccountException("Account not found"));

        // check if the account has enough balance to transfer the amount
        if(fromAccount.getBalance() < transferFundDto.amount()) {
            throw new RuntimeException("Not enough balance");
        }

        // deduct the amount from the account
        fromAccount.setBalance(fromAccount.getBalance() - transferFundDto.amount());

        // add the amount to the account
        toAccount.setBalance(toAccount.getBalance() + transferFundDto.amount());

        // save the changes
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // save the transaction
        Transaction transaction = new Transaction();
        transaction.setAccountId(fromAccount.getId());
        transaction.setAmount(transferFundDto.amount());
        transaction.setTransactionType(TRANSACTION_TYPE_TRANSFER);
        transaction.setTimestamp(LocalDateTime.now());

        transactionRepository.save(transaction);
    }

    @Override
    public List<TransactionDto> getAccountTransactions(Long id) {
        List<Transaction> transactions = transactionRepository.findByAccountIdOrderByTimestampDesc(id);
        return transactions.stream().map(this::convertEntityToDto).collect(Collectors.toList());
    }

    private TransactionDto convertEntityToDto(Transaction transaction) {
        return new TransactionDto(transaction.getId(), transaction.getAccountId(), transaction.getAmount(), transaction.getTransactionType(), transaction.getTimestamp());
    };
}
