import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import models.StudentAccount;

public class StudentRepository {

    private static  StudentRepository instance;
    private final Map<String , StudentAccount> accounts;

    private StudentRepository() {
        accounts = new HashMap<>();
    }

    public static synchronized StudentRepository getInstance() {
        if (instance == null) {
            instance = new StudentRepository();
        }
        return instance;
    }

    
    public boolean addAccount(StudentAccount account) {
        if (account == null || accounts.containsKey(account.getid())) {
            return false;
        }
        accounts.put(account.getid(), account);
        return true;
    }

    public StudentAccount findById(String id) {
        return accounts.get(id);
    }

    public Collection<StudentAccount> getAllAccounts() {
        return accounts.values();
    }

    public int getAccountCount() {
        return accounts.size();
    }
}

