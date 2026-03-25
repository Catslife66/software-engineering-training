package javaOop.exercises.bankAccount;

public class NoWithdrawal implements WithdrawalPolicy {
    @Override
    public double withdraw(double balance, double amount){
        System.out.println("Cannot withdraw before maturity");
        return balance;
    }
}
