package javaOop.exercises.bankAccount;

public class StandardDeposit implements DepositPolicy{
    @Override
    public double deposit(double balance, double amount){
        if(amount <= 0){
            System.out.println("Deposit must be greater than 0.");
            return balance;
        }
        return balance += amount;
    }
}
