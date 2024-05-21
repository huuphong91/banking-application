package huu.phong.banking.dto;

public record TransferFundDto(Long fromAccountId, Long toAccountId, double amount) {

}
