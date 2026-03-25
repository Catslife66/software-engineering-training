package javaOop.exercises.bankAccount;

public class StandardWithdrawal implements WithdrawalPolicy{
    @Override
    public double withdraw(double balance, double amount){
        if(amount <= 0){
            System.out.println("Amount must be greater than 0.");
            return balance;
        }
        if(amount > balance){
            System.out.println("Insufficient funds.");
            return balance;
        }
        System.out.println("Withdraw money.");
        return balance -= amount;
    }
}
