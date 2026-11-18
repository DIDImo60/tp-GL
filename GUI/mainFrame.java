package GUI;

import logger.AuditLogger;
import models.StudentAccount;
import repository.StudentRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Arrays;

// Pattern: View / UI Layer interacting with Builder & Singleton
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

    public mainFrame() {
        setTitle("نظام إدارة حسابات الطلبة - UniAccounts (TP1)");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // توسيط النافذة
        setLayout(new BorderLayout(10, 10));

        // 1. بناء لوحة الإدخال
        add(createFormPanel(), BorderLayout.EAST);

        // 2. بناء جدول عرض الحسابات (وسط)
        add(createTablePanel(), BorderLayout.CENTER);

        // 3. بناء لوحة سجل التدقيق (أسفل)
        add(createLogPanel(), BorderLayout.SOUTH);

        logToUI("تم تشغيل الواجهة الرسومية بنجاح.");
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridLayout(9, 2, 5, 8));
        panel.setBorder(BorderFactory.createTitledBorder("إنشاء حساب جديد (Builder)"));
        panel.setPreferredSize(new Dimension(340, 0));

        panel.add(new JLabel("رقم التسجيل (ID) *:"));
        txtId = new JTextField();
        panel.add(txtId);

        panel.add(new JLabel("الاسم الكامل *:"));
        txtName = new JTextField();
        panel.add(txtName);

        panel.add(new JLabel("البريد الإلكتروني *:"));
        txtEmail = new JTextField();
        panel.add(txtEmail);

        panel.add(new JLabel("رقم الهاتف:"));
        txtPhone = new JTextField();
        panel.add(txtPhone);

        panel.add(new JLabel("المستوى الدراسي:"));
        cmbLevel = new JComboBox<>(new String[]{"L1", "L2", "L3", "M1", "M2"});
        panel.add(cmbLevel);

        panel.add(new JLabel("التخصص:"));
        txtMajor = new JTextField();
        panel.add(txtMajor);

        panel.add(new JLabel("مستفيد من منحة؟:"));
        chkScholarship = new JCheckBox("نعم");
        panel.add(chkScholarship);

        panel.add(new JLabel("المقررات (مفصولة بفاصلة):"));
        txtCourses = new JTextField();
        panel.add(txtCourses);

        JButton btnCreate = new JButton("إضافة الحساب");
        btnCreate.addActionListener(e -> handleCreateAccount());
        
        JButton btnSearch = new JButton("بحث بـ ID");
        btnSearch.addActionListener(e -> handleSearchAccount());

        panel.add(btnCreate);
        panel.add(btnSearch);

        return panel;
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

    // منطق إنشاء الحساب عند الضغط على الزر
    private void handleCreateAccount() {
        try {
            if (txtId.getText() == null || txtId.getText().trim().isEmpty()) {
                throw new IllegalArgumentException("رقم التسجيل (id) لا يجب أن يكون فارغاً");
            }
            if (txtName.getText() == null || txtName.getText().trim().isEmpty()) {
                throw new IllegalArgumentException("الاسم الكامل (fullName) لا يجب أن يكون فارغاً");
            }
            if (txtEmail.getText() == null || !txtEmail.getText().contains("@")) {
                throw new IllegalArgumentException("البريد الإلكتروني (email) يجب أن يحتوي على الرمز @");
            }
            // استخدام نمط Builder
            StudentAccount.StudentAccountBuilder builder = new StudentAccount.StudentAccountBuilder(
                    txtId.getText().trim(),
                    txtName.getText().trim(),
                    txtEmail.getText().trim()
            );

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

            StudentAccount account = builder.build();

            // الحفظ في Singleton Repository
            if (repository.addAccount(account)) {
                refreshTable();
                clearFields();
                logToUI("تم إنشاء وحفظ الحساب: " + account.getid() + " - " + account.getfullName());
            } else {
                JOptionPane.showMessageDialog(this, "رقم التسجيل موجود مسبقاً!", "خطأ تكرار", JOptionPane.ERROR_MESSAGE);
                logToUI("فشل الإضافة: رقم التسجيل مكرر (" + account.getid() + ")");
            }

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "خطأ في البيانات", JOptionPane.WARNING_MESSAGE);
            logToUI("خطأ إدخال: " + ex.getMessage());
        }
    }

    // منطق البحث عن طالب
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

    // تحديث الجدول من المستودع
    private void refreshTable() {
        tableModel.setRowCount(0); // مسح الصفوف القديمة
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

    // كتابة الحدث في سجل التدقيق وفي الواجهة معاً
    private void logToUI(String message) {
        logger.log(message); // الطباعة في الكونسول عبر Singleton
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