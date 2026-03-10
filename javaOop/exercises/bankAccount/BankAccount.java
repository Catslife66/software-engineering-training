package javaOop.exercises.bankAccount;


public class BankAccount{
    private final String accountHolder;
    private double balance;

    private static int totalAccounts = 0;

    public BankAccount(String accountHolder){
        this.accountHolder = accountHolder;
        this.balance = 0.00;
        totalAccounts ++;
    }

    public static int getTotalAccounts(){
        return totalAccounts;
    }

    public String getAccountHolder(){
        return accountHolder;
    }
    public double getBalance(){
        return balance;
    }

    public void deposit(double amount){
        if(amount <= 0){
            System.out.println("Deposit must be greater than 0.");
            return;
        } 
        balance += amount;
        
    }
    public void withdraw(double amount){
        if(amount <= 0){
            System.out.println("You cannot withdraw negative amounts.");
            return;
        }
        if(amount > balance){
            System.out.println("You cannot withdraw more than balance.");
            return;
        }
        balance -= amount;
        
    }

    public void displayBalance(){
        System.out.println(accountHolder + " balance: £ " + balance);
    }
}