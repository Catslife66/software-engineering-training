package javaOop.exercises.bankAccount;


public abstract class Account{
    private final String accountHolder;
    private double balance;
    private DepositPolicy depositPolicy;
    private WithdrawalPolicy withdrawalPolicy;

    private static int totalAccounts = 0;

    public Account(
        String accountHolder, 
        double balance, 
        DepositPolicy depositPolicy, 
        WithdrawalPolicy withdrawalPolicy
    ){
        this.accountHolder = accountHolder;
        this.balance = balance;
        this.depositPolicy = depositPolicy;
        this.withdrawalPolicy = withdrawalPolicy;
        totalAccounts ++;
    }

    public static int getTotalAccounts(){
        return totalAccounts;
    }

    public void displayBalance(){
        System.out.println(accountHolder + " balance: £ " + balance);
    }

    public void setBalance(double balance){
        this.balance = balance;
    }

    public double getBalance(){
        return balance;
    }

    public void deposit(double amount){
        setBalance(depositPolicy.deposit(getBalance(), amount));
    }

    public void withdraw(double amount){
        setBalance(withdrawalPolicy.withdraw(getBalance(), amount));
    }


}