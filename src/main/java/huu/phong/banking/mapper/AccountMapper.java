package huu.phong.banking.mapper;

import huu.phong.banking.dto.AccountDto;
import huu.phong.banking.entity.Account;

public class AccountMapper {
    public static Account mapToAccount(AccountDto accountDto) {
        Account account = new Account();
        account.setId(accountDto.id());
        account.setAccountHolderName(accountDto.accountHolderName());
        account.setBalance(accountDto.balance());
        return account;
    }

    public static AccountDto mapToAccountDto(Account account) {
        return new AccountDto(account.getId(),account.getAccountHolderName(),account.getBalance());
    }
}
