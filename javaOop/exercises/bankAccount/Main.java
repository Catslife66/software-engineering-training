package javaOop.exercises.bankAccount;

public class Main {
    public static void main(String[] args){
        BankAccount account1 = new BankAccount("Alice Brown");
        BankAccount account2 = new BankAccount("David Dickens");

        account1.deposit(20);
        account1.withdraw(50);
        account1.displayBalance();


        account2.deposit(10);
        account2.withdraw(5);
        account2.displayBalance();

        System.out.println("Total accounts: " + BankAccount.getTotalAccounts());
    }
}
