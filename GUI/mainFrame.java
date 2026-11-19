package GUI;

import logger.AuditLogger;
import models.StudentAccount;
import repository.StudentRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

// Pattern: View / UI Layer interacting with Builder, Singleton & Prototype
public class mainFrame extends JFrame {

    // حقول الإدخال
    private JTextField txtId, txtName, txtEmail, txtPhone, txtMajor, txtCourses;
    private JComboBox<String> cmbLevel;
    private JCheckBox chkScholarship;

    // جدول العرض ومنطقة سجل التدقيق
    private DefaultTableModel tableModel;
    private JTextArea txtAuditLog;

    // استدعاء النسخ الوحيدة (Singleton)
    private final StudentRepository repository = StudentRepository.getInstance();
    private final AuditLogger logger = AuditLogger.getInstance();

    // سجل قوالب الدفعات (Prototype Registry)
    private final Map<String, StudentAccount> prototypes = new HashMap<>();

    public mainFrame() {
        setTitle("نظام إدارة حسابات الطلبة - UniAccounts (TP1)");
        setSize(950, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));


        // 1. بناء لوحة الإدخال
        add(createFormPanel(), BorderLayout.EAST);

        // 2. بناء جدول عرض الحسابات (وسط)
        add(createTablePanel(), BorderLayout.CENTER);

        // 3. بناء لوحة سجل التدقيق (أسفل)
        add(createLogPanel(), BorderLayout.SOUTH);

        logToUI("تم تشغيل الواجهة الرسومية وتجهيز قوالب Prototype بنجاح.");
    }


    private JPanel createFormPanel() {
        JPanel mainSidePanel = new JPanel(new BorderLayout(5, 10));
        mainSidePanel.setPreferredSize(new Dimension(360, 0));

        // لوحة الحقول
        JPanel fieldsPanel = new JPanel(new GridLayout(8, 2, 5, 8));
        fieldsPanel.setBorder(BorderFactory.createTitledBorder("بيانات الحساب"));

        fieldsPanel.add(new JLabel("رقم التسجيل (ID) *:"));
        txtId = new JTextField();
        fieldsPanel.add(txtId);

        fieldsPanel.add(new JLabel("الاسم الكامل *:"));
        txtName = new JTextField();
        fieldsPanel.add(txtName);

        fieldsPanel.add(new JLabel("البريد الإلكتروني *:"));
        txtEmail = new JTextField();
        fieldsPanel.add(txtEmail);

        fieldsPanel.add(new JLabel("رقم الهاتف:"));
        txtPhone = new JTextField();
        fieldsPanel.add(txtPhone);

        fieldsPanel.add(new JLabel("المستوى الدراسي:"));
        cmbLevel = new JComboBox<>(new String[]{"L1", "L2", "L3", "M1", "M2"});
        fieldsPanel.add(cmbLevel);

        fieldsPanel.add(new JLabel("التخصص:"));
        txtMajor = new JTextField();
        fieldsPanel.add(txtMajor);

        fieldsPanel.add(new JLabel("مستفيد من منحة؟:"));
        chkScholarship = new JCheckBox("نعم");
        fieldsPanel.add(chkScholarship);

        fieldsPanel.add(new JLabel("المقررات (مفصولة بفاصلة):"));
        txtCourses = new JTextField();
        fieldsPanel.add(txtCourses);

        // لوحة الأزرار (Builder + Singleton + Prototype)
        JPanel buttonsPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        buttonsPanel.setBorder(BorderFactory.createTitledBorder("العمليات والأنماط"));

        JButton btnCreate = new JButton("إضافة");
        btnCreate.addActionListener(e -> handleCreateAccount());

        JButton btnSearch = new JButton("بحث بـ ID");
        btnSearch.addActionListener(e -> handleSearchAccount());

        JButton btnSaveProto = new JButton("حفظ كقالب");
        btnSaveProto.addActionListener(e -> handleSavePrototype());

        JButton btnCloneProto = new JButton("استنساخ قالب");
        btnCloneProto.addActionListener(e -> handleClonePrototype());

        buttonsPanel.add(btnCreate);
        buttonsPanel.add(btnSearch);
        buttonsPanel.add(btnSaveProto);
        buttonsPanel.add(btnCloneProto);

        mainSidePanel.add(fieldsPanel, BorderLayout.CENTER);
        mainSidePanel.add(buttonsPanel, BorderLayout.SOUTH);

        return mainSidePanel;
    }

    private JScrollPane createTablePanel() {
        String[] columns = {"ID", "الاسم الكامل", "البريد", "المستوى", "التخصص", "منحة", "المقررات"};
        tableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("مستودع الحسابات (Singleton Repository)"));
        return scrollPane;
    }

    private JScrollPane createLogPanel() {
        txtAuditLog = new JTextArea(6, 50);
        txtAuditLog.setEditable(false);
        txtAuditLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(txtAuditLog);
        scrollPane.setBorder(BorderFactory.createTitledBorder("سجل التدقيق المباشر (Audit Logger)"));
        return scrollPane;
    }

    // 1. منطق إنشاء الحساب عبر Builder
    private void handleCreateAccount() {
        try {
            StudentAccount account = buildAccountFromFields(
                    txtId.getText().trim(),
                    txtName.getText().trim(),
                    txtEmail.getText().trim()
            );

            if (repository.addAccount(account)) {
                refreshTable();
                clearFields();
                logToUI("تم إنشاء وحفظ الحساب (Builder): " + account.getid() + " - " + account.getfullName());
            } else {
                JOptionPane.showMessageDialog(this, "رقم التسجيل موجود مسبقاً!", "خطأ تكرار", JOptionPane.ERROR_MESSAGE);
                logToUI("فشل الإضافة: رقم التسجيل مكرر (" + account.getid() + ")");
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "خطأ في البيانات", JOptionPane.WARNING_MESSAGE);
            logToUI("خطأ إدخال: " + ex.getMessage());
        }
    }

    // 2. منطق حفظ الإعدادات الحالية كقالب جديد (Prototype)
    private void handleSavePrototype() {
        String protoName = JOptionPane.showInputDialog(this, "أدخل اسماً للقالب (مثلاً: دفعة M1 شبكات):");
        if (protoName != null && !protoName.trim().isEmpty()) {
            // نبني القالب ببيانات افتراضية للهوية ونأخذ المستوى والتخصص والمقررات من الحقول
            StudentAccount template = buildAccountFromFields("TEMPLATE", "قالب", "template@univ.dz");
            prototypes.put(protoName.trim(), template);
            logToUI("تم حفظ قالب جديد في Prototype Registry باسم: " + protoName.trim());
            JOptionPane.showMessageDialog(this, "تم حفظ القالب بنجاح! يمكنك الآن استنساخه للطلبة.");
        }
    }

    // 3. منطق استنساخ حساب جديد من قالب موجود (Prototype Clone)
    private void handleClonePrototype() {
        if (prototypes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "لا توجد قوالب محفوظة حالياً.");
            return;
        }

        // اختيار القالب من قائمة منسدلة
        Object[] protoNames = prototypes.keySet().toArray();
        String selectedProto = (String) JOptionPane.showInputDialog(
                this,
                "اختر قالب الدفعة المراد استنساخه:",
                "استنساخ من قالب (Prototype)",
                JOptionPane.QUESTION_MESSAGE,
                null,
                protoNames,
                protoNames[0]
        );

        if (selectedProto == null) return;

        // طلب البيانات الفريدة للطالب الجديد فقط
        JTextField newIdField = new JTextField();
        JTextField newNameField = new JTextField();
        JTextField newEmailField = new JTextField();
        Object[] message = {
                "رقم التسجيل (ID) *:", newIdField,
                "الاسم الكامل *:", newNameField,
                "البريد الإلكتروني *:", newEmailField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "بيانات الطالب الجديد", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String newId = newIdField.getText().trim();
            String newName = newNameField.getText().trim();
            String newEmail = newEmailField.getText().trim();

            if (newId.isEmpty() || newName.isEmpty() || !newEmail.contains("@")) {
                JOptionPane.showMessageDialog(this, "يرجى إدخال ID واسم وبريد إلكتروني صحيح يحتوي على @", "خطأ", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // استنساخ الكائن وتعديل هويته فقط!
            StudentAccount clonedAccount = prototypes.get(selectedProto).clone();
            clonedAccount.setid(newId);
            clonedAccount.setfullName(newName);
            clonedAccount.setemail(newEmail);

            if (repository.addAccount(clonedAccount)) {
                refreshTable();
                logToUI("تم استنساخ حساب جديد من القالب [" + selectedProto + "] برقم: " + newId);
            } else {
                JOptionPane.showMessageDialog(this, "رقم التسجيل موجود مسبقاً!", "خطأ تكرار", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // دالة مساعدة لتجميع البيانات من الحقول عبر Builder
    private StudentAccount buildAccountFromFields(String id, String name, String email) {
        if (id.isEmpty()) throw new IllegalArgumentException("رقم التسجيل (id) لا يجب أن يكون فارغاً");
        if (name.isEmpty()) throw new IllegalArgumentException("الاسم الكامل (fullName) لا يجب أن يكون فارغاً");
        if (!email.contains("@")) throw new IllegalArgumentException("البريد الإلكتروني (email) يجب أن يحتوي على الرمز @");

        StudentAccount.StudentAccountBuilder builder = new StudentAccount.StudentAccountBuilder(id, name, email);

        if (!txtPhone.getText().trim().isEmpty()) {
            builder.phone(txtPhone.getText().trim());
        }
        builder.level((String) cmbLevel.getSelectedItem());

        if (!txtMajor.getText().trim().isEmpty()) {
            builder.major(txtMajor.getText().trim());
        }
        builder.scholship(chkScholarship.isSelected());

        if (!txtCourses.getText().trim().isEmpty()) {
            builder.courses(Arrays.asList(txtCourses.getText().split(",")));
        }

        return builder.build();
    }

    private void handleSearchAccount() {
        String id = JOptionPane.showInputDialog(this, "أدخل رقم التسجيل (ID) للبحث:");
        if (id != null && !id.trim().isEmpty()) {
            StudentAccount found = repository.findById(id.trim());
            if (found != null) {
                JOptionPane.showMessageDialog(this, found.toString(), "تم العثور على الطالب", JOptionPane.INFORMATION_MESSAGE);
                logToUI("تم البحث بنجاح عن الحساب: " + id);
            } else {
                JOptionPane.showMessageDialog(this, "لا يوجد طالب بهذا الرقم.", "غير موجود", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (StudentAccount acc : repository.getAllAccounts()) {
            tableModel.addRow(new Object[]{
                    acc.getid(),
                    acc.getfullName(),
                    acc.getemail(),
                    acc.getlevel(),
                    acc.getmajor(),
                    acc.getscholship() ? "نعم" : "لا",
                    acc.getcourses()
            });
        }
    }

    private void logToUI(String message) {
        logger.log(message);
        txtAuditLog.append(">> " + message + "\n");
    }

    private void clearFields() {
        txtId.setText("");
        txtName.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        txtMajor.setText("");
        txtCourses.setText("");
        chkScholarship.setSelected(false);
        cmbLevel.setSelectedIndex(0);
    }
}