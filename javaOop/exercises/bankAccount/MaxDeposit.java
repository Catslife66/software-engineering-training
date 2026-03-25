package javaOop.exercises.bankAccount;

public class MaxDeposit implements DepositPolicy{
    private double maxAmount = 250;

    @Override
    public double deposit(double balance, double amount){
        if(amount <= 0){
            System.out.println("Deposit must be greater than 0.");
            return balance;
        } 
        if(amount > maxAmount){
            System.out.println("The amount is over the deposit maximum limit.");
            return balance;
        }
        return balance += amount;
    }
}
