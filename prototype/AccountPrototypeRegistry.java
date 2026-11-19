package prototype;

import models.StudentAccount;
import java.util.HashMap;
import java.util.Map;

// Pattern: Prototype - Role: Prototype Registry / Manager
public class AccountPrototypeRegistry {

    private final Map<String, StudentAccount> prototypes = new HashMap<>();

    // إضافة قالب جديد
    public void addPrototype(String key, StudentAccount prototype) {
        prototypes.put(key, prototype);
    }

    // إرجاع نسخة جديدة من القالب 
    public StudentAccount createFromPrototype(String key, String newId, String newFullName, String newEmail) {
        StudentAccount prototype = prototypes.get(key);
        if (prototype == null) {
            throw new IllegalArgumentException("القالب المطلوب غير موجود: " + key);
        }

        // 1. نسخ القالب
        StudentAccount clone = prototype.clone();

        // 2. تغيير بيانات هوية الطالب الجديد
        clone.setid(newId);
        clone.setfullName(newFullName);
        clone.setemail(newEmail);

        return clone;
    }
}