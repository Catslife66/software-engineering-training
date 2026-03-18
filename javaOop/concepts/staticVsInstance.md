# Static vs Instance Members

Instance members belong to each object separately.

Static members belong to the class itself, not individual objects.

Example:

```
public class BankAccount {

    private String accountHolder;
    private double balance;

    private static int totalAccounts = 0;

    public BankAccount(String accountHolder){
        this.accountHolder = accountHolder;
        totalAccounts++;
    }

    public static int getTotalAccounts(){
        return totalAccounts;
    }
}

BankAccount a = new BankAccount("Alice");
BankAccount b = new BankAccount("David");

System.out.println(BankAccount.getTotalAccounts());

```

We access it through the class name, not an object.

## Static Methods

Static methods belong to the class.

Example:

```
public class MathUtils {

    public static int add(int a, int b){
        return a + b;
    }
}


int result = MathUtils.add(3,5);

```

**Instance**
belongs to object
each object has its own copy
accessed via object

**Static**
belongs to class
shared across all objects
accessed via class
