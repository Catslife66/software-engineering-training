package javaOop.exercises.bankAccount;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args){
        SavingsAccount normalAccount = new SavingsAccount("David Dickens", 100, new MaxDeposit(), new StandardWithdrawal());
        FixedAccount fixedAccount = new FixedAccount("Lily Brown", 150, LocalDate.of(2026, 12, 31)); 
        
        normalAccount.deposit(500);
        normalAccount.withdraw(50);
        normalAccount.displayBalance();

        fixedAccount.deposit(500);
        fixedAccount.withdraw(50);
        fixedAccount.displayBalance();
       
        System.out.println("Total accounts: " + Account.getTotalAccounts());
    }
}
