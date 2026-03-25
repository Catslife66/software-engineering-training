package javaOop.exercises.bankAccount;

public class SavingsAccount extends Account{

    public SavingsAccount(
        String accountHolder, 
        double balance,
        DepositPolicy depositPolicy, 
        WithdrawalPolicy withdrawalPolicy
    ){
        super(
            accountHolder, 
            balance,
            depositPolicy,
            withdrawalPolicy
        );
    }
    
}
