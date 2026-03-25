package javaOop.exercises.bankAccount;

import java.time.LocalDate;

public class FixedAccount extends Account {
    private LocalDate maturityDate;

    public FixedAccount(
        String accountHolder, 
        double balance, 
        LocalDate maturityDate
    ){
        super(
            accountHolder,
            balance,
            new MaxDeposit(),
            new NoWithdrawal()
        );
        this.maturityDate = maturityDate;
    }

    public LocalDate getMaturityDate() {
        return maturityDate;
    }
    
}
