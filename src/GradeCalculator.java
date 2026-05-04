import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Arrays;
import java.util.prefs.Preferences;

public class GradeCalculator extends JFrame {

    private final String[] COURSES = {"CA", "OS", "Management", "Networks", "DB II", "SE"};
    private final String[] LETTERS = {"A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D"};
    private final double[] THRESHOLDS = {94, 90, 86, 82, 78, 74, 70, 65, 60, 55, 50};

    private final Preferences prefs = Preferences.userNodeForPackage(GradeCalculator.class);

    private JTable summaryTable;
    private DefaultTableModel tableModel;
    private JButton currentSaveButton;
    private JLabel notificationLabel;
    private Timer notificationTimer;

    private JTextField[][] caQuizzes = new JTextField[1][3];
    private JTextField[][] caLabs = new JTextField[1][6];
    private JTextField caProject = new JTextField(), caMidterm = new JTextField();

    private JTextField[][] osQuizzes = new JTextField[1][3];
    private JTextField osProject = new JTextField(), osMidterm = new JTextField();

    private JTextField mgtMidterm = new JTextField();

    private JTextField[][] netLabs = new JTextField[1][8];
    private JTextField[][] netLabTests = new JTextField[1][3];
    private JTextField[][] netQuizzes = new JTextField[1][4];

    private JTextField[][] dbQuizzes = new JTextField[1][3];
    private JTextField dbM1 = new JTextField(), dbM2 = new JTextField(), dbMidterm = new JTextField();

    private JTextField seM1 = new JTextField(), seM2 = new JTextField(), seAssignment = new JTextField(), seMidterm = new JTextField();

    private final Color THEME_BG = new Color(24, 26, 32);
    private final Color PANEL_BG = new Color(30, 33, 41);
    private final Color PANEL_ALT = new Color(36, 40, 50);
    private final Color CARD_BG = new Color(40, 44, 54);
    private final Color TEXT_FG = new Color(236, 239, 244);
    private final Color MUTED_FG = new Color(161, 170, 185);
    private final Color ACCENT = new Color(88, 166, 255);
    private final Color ACCENT_HOVER = new Color(110, 185, 255);
    private final Color BORDER = new Color(72, 78, 92);
    private final Font UI_FONT = new Font("Segoe UI Variable", Font.PLAIN, 13);
    private final Font UI_FONT_BOLD = new Font("Segoe UI Variable", Font.BOLD, 13);

    public GradeCalculator() {
        setTitle("Grades Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(THEME_BG);

        applyDarkTheme();

        for (int i = 0; i < caQuizzes[0].length; i++) caQuizzes[0][i] = new JTextField();
        for (int i = 0; i < caLabs[0].length; i++) caLabs[0][i] = new JTextField();
        for (int i = 0; i < osQuizzes[0].length; i++) osQuizzes[0][i] = new JTextField();
        for (int i = 0; i < netLabs[0].length; i++) netLabs[0][i] = new JTextField();
        for (int i = 0; i < netLabTests[0].length; i++) netLabTests[0][i] = new JTextField();
        for (int i = 0; i < netQuizzes[0].length; i++) netQuizzes[0][i] = new JTextField();
        for (int i = 0; i < dbQuizzes[0].length; i++) dbQuizzes[0][i] = new JTextField();

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBackground(PANEL_BG);
        tabbedPane.setForeground(TEXT_FG);
        tabbedPane.addTab("CA", new JScrollPane(createCAPanel()));
        tabbedPane.addTab("OS", new JScrollPane(createOSPanel()));
        tabbedPane.addTab("Management", new JScrollPane(createMgtPanel()));
        tabbedPane.addTab("Networks", new JScrollPane(createNetPanel()));
        tabbedPane.addTab("DB II", new JScrollPane(createDBPanel()));
        tabbedPane.addTab("SE", new JScrollPane(createSEPanel()));
        add(tabbedPane, BorderLayout.CENTER);

        setupTable();
        add(summaryTable.getTableHeader(), BorderLayout.SOUTH);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(summaryTable, BorderLayout.CENTER);
        tablePanel.setPreferredSize(new Dimension(0, 320));

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(summaryTable.getTableHeader(), BorderLayout.NORTH);
        southPanel.add(summaryTable, BorderLayout.CENTER);
        southPanel.setPreferredSize(new Dimension(0, 320));

        add(southPanel, BorderLayout.SOUTH);

        notificationLabel = new JLabel("Saved!");
        notificationLabel.setOpaque(true);
        notificationLabel.setBackground(new Color(56, 61, 74));
        notificationLabel.setForeground(TEXT_FG);
        notificationLabel.setFont(UI_FONT_BOLD);
        notificationLabel.setHorizontalAlignment(SwingConstants.CENTER);
        notificationLabel.setVisible(false);
        notificationLabel.setPreferredSize(new Dimension(128, 36));
        notificationLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));

        JPanel notifContainer = new JPanel(new BorderLayout());
        notifContainer.setOpaque(false);
        notifContainer.add(notificationLabel, BorderLayout.CENTER);
        notifContainer.setSize(notificationLabel.getPreferredSize());

        JLayeredPane lp = getLayeredPane();
        lp.add(notifContainer, JLayeredPane.POPUP_LAYER);
        notifContainer.setVisible(false);

        notificationTimer = null;

        SwingUtilities.invokeLater(() -> positionNotificationContainer(notifContainer));

        notificationLabel.putClientProperty("container", notifContainer);

        updateAllCalculations();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                positionNotificationContainer(notifContainer);
            }
        });
    }


    private JPanel createCAPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        addCompactSection(p, "Quizzes (Best 2 of 3)", caQuizzes[0], 3, "CA_Q", 17.0);
        p.add(Box.createHorizontalStrut(15));
        addCompactSection(p, "Lab Tasks (Best 5 of 6)", caLabs[0], 6, "CA_L", 10.0);
        p.add(Box.createHorizontalStrut(15));
        addCompactSingleSection(p, "Project", caProject, "CA_Proj", 15.0);
        p.add(Box.createHorizontalStrut(15));
        addCompactSingleSection(p, "Midterm", caMidterm, "CA_Mid", 100.0);
        p.add(Box.createHorizontalGlue());

        return p;
    }

    private JPanel createOSPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        addCompactSection(p, "Quizzes (Best 2 of 3)", osQuizzes[0], 3, "OS_Q", 17.0);
        p.add(Box.createHorizontalStrut(15));
        addCompactSingleSection(p, "Project", osProject, "OS_Proj", 20.0);
        p.add(Box.createHorizontalStrut(15));
        addCompactSingleSection(p, "Midterm", osMidterm, "OS_Mid", 100.0);
        p.add(Box.createHorizontalGlue());

        return p;
    }

    private JPanel createMgtPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        addCompactSingleSection(p, "Midterm", mgtMidterm, "MGT_Mid", 100.0);
        p.add(Box.createHorizontalGlue());

        return p;
    }

    private JPanel createNetPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        addCompactSection(p, "Lab Exp. (Best 7 of 8)", netLabs[0], 8, "NET_L", 10.0);
        p.add(Box.createHorizontalStrut(15));
        addCompactSection(p, "Lab Tests (Best 2 of 3)", netLabTests[0], 3, "NET_LT", 10.0);
        p.add(Box.createHorizontalStrut(15));
        addNetworkQuizzes(p);
        p.add(Box.createHorizontalGlue());

        return p;
    }

    private JPanel createDBPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        addCompactSection(p, "Quizzes (Best 2 of 3)", dbQuizzes[0], 3, "DB_Q", 10.0);
        p.add(Box.createHorizontalStrut(15));
        addCompactSingleSection(p, "Milestone 1", dbM1, "DB_M1", 10.0);
        p.add(Box.createHorizontalStrut(15));
        addCompactSingleSection(p, "Milestone 2", dbM2, "DB_M2", 10.0);
        p.add(Box.createHorizontalStrut(15));
        addCompactSingleSection(p, "Midterm", dbMidterm, "DB_Mid", 100.0);
        p.add(Box.createHorizontalGlue());

        return p;
    }

    private JPanel createSEPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        addCompactSingleSection(p, "Milestone 1", seM1, "SE_M1", 15.0);
        p.add(Box.createHorizontalStrut(15));
        addCompactSingleSection(p, "Milestone 2", seM2, "SE_M2", 15.0);
        p.add(Box.createHorizontalStrut(15));
        addCompactSingleSection(p, "Assignment", seAssignment, "SE_Ass", 10.0);
        p.add(Box.createHorizontalStrut(15));
        addCompactSingleSection(p, "Midterm", seMidterm, "SE_Mid", 100.0);
        p.add(Box.createHorizontalGlue());

        return p;
    }

    private void applyDarkTheme() {
        UIManager.put("control", THEME_BG);
        UIManager.put("info", PANEL_BG);
        UIManager.put("nimbusBase", PANEL_BG);
        UIManager.put("nimbusBlueGrey", PANEL_ALT);
        UIManager.put("nimbusLightBackground", PANEL_BG);
        UIManager.put("TabbedPane.background", PANEL_BG);
        UIManager.put("TabbedPane.foreground", TEXT_FG);
        UIManager.put("TabbedPane.selected", PANEL_ALT);
        UIManager.put("Panel.background", THEME_BG);
        UIManager.put("ScrollPane.background", THEME_BG);
        UIManager.put("Viewport.background", THEME_BG);
        UIManager.put("Table.background", PANEL_BG);
        UIManager.put("Table.foreground", TEXT_FG);
        UIManager.put("Table.gridColor", BORDER);
        UIManager.put("Table.selectionBackground", new Color(58, 64, 80));
        UIManager.put("Table.selectionForeground", TEXT_FG);
        UIManager.put("TableHeader.background", PANEL_ALT);
        UIManager.put("TableHeader.foreground", TEXT_FG);
        UIManager.put("TableHeader.cellBorder", BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        UIManager.put("Label.foreground", TEXT_FG);
        UIManager.put("Button.background", CARD_BG);
        UIManager.put("Button.foreground", TEXT_FG);
        UIManager.put("Button.border", BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        UIManager.put("TextField.background", CARD_BG);
        UIManager.put("TextField.foreground", TEXT_FG);
        UIManager.put("TextField.caretForeground", ACCENT);
        UIManager.put("TextField.selectionBackground", new Color(70, 90, 120));
        UIManager.put("TextField.selectionForeground", TEXT_FG);
        UIManager.put("TitledBorder.titleColor", TEXT_FG);
        UIManager.put("TitledBorder.border", BorderFactory.createLineBorder(BORDER, 1));
        UIManager.put("ToolTip.background", PANEL_ALT);
        UIManager.put("ToolTip.foreground", TEXT_FG);
        UIManager.put("ToolTip.border", BorderFactory.createLineBorder(BORDER, 1));
        UIManager.put("defaultFont", UI_FONT);
        UIManager.put("Label.font", UI_FONT);
        UIManager.put("Button.font", UI_FONT);
        UIManager.put("TextField.font", UI_FONT);
        UIManager.put("Table.font", UI_FONT);
        UIManager.put("TableHeader.font", UI_FONT_BOLD);
    }

    private void addCompactSection(JPanel p, String title, JTextField[] array, int count, String prefKey, double maxPerItem) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBorder(BorderFactory.createTitledBorder(title));
        section.setBackground(PANEL_BG);

        Dimension fieldDim = new Dimension(80, 26);

        for (int i = 0; i < count; i++) {
            JPanel itemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
            itemPanel.setBackground(PANEL_BG);
            JLabel itemLabel = new JLabel(String.format("%s %d", title.split("\\(")[0].trim(), i + 1));
            itemLabel.setPreferredSize(new Dimension(110, 20));
            itemLabel.setForeground(TEXT_FG);
            JTextField field = new JTextField();
            field.setPreferredSize(fieldDim);
            field.setMinimumSize(fieldDim);
            field.setMaximumSize(fieldDim);
            field.setBackground(CARD_BG);
            field.setForeground(TEXT_FG);
            field.setCaretColor(ACCENT);
            field.setSelectionColor(new Color(70, 90, 120));
            field.setSelectedTextColor(TEXT_FG);
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER, 1),
                    BorderFactory.createEmptyBorder(3, 6, 3, 6)));
            array[i] = field;
            String key = prefKey + "_" + i;
            array[i].setText(prefs.get(key, ""));

            JPanel buttonContainer = new JPanel();
            buttonContainer.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
            buttonContainer.setPreferredSize(new Dimension(70, 26));
            buttonContainer.setBackground(PANEL_BG);
            JButton saveBtn = new JButton("Save");
            saveBtn.setPreferredSize(new Dimension(64, 24));
            saveBtn.setVisible(false);
            styleActionButton(saveBtn);
            buttonContainer.add(saveBtn);

            GradeInputListener listener = new GradeInputListener(key, array[i], maxPerItem, saveBtn);
            array[i].addKeyListener(listener);
            array[i].addFocusListener(listener);
            saveBtn.addActionListener(e -> listener.saveGrade());

            JLabel maxLabel = new JLabel("/ " + (int)maxPerItem);
            maxLabel.setForeground(MUTED_FG);
            itemPanel.add(itemLabel);
            itemPanel.add(array[i]);
            itemPanel.add(buttonContainer);
            itemPanel.add(maxLabel);
            section.add(itemPanel);
        }

        p.add(section);
    }

    private void addCompactSingleSection(JPanel p, String title, JTextField tf, String prefKey, double maxValue) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBorder(BorderFactory.createTitledBorder(title));
        section.setBackground(PANEL_BG);

        JPanel itemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        itemPanel.setBackground(PANEL_BG);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setPreferredSize(new Dimension(110, 20));
        titleLabel.setForeground(TEXT_FG);

        tf.setText(prefs.get(prefKey, ""));
        Dimension fieldDim = new Dimension(80, 26);
        tf.setPreferredSize(fieldDim);
        tf.setMinimumSize(fieldDim);
        tf.setMaximumSize(fieldDim);
        tf.setBackground(CARD_BG);
        tf.setForeground(TEXT_FG);
        tf.setCaretColor(ACCENT);
        tf.setSelectionColor(new Color(70, 90, 120));
        tf.setSelectedTextColor(TEXT_FG);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(3, 6, 3, 6)));

        JPanel buttonContainer = new JPanel();
        buttonContainer.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonContainer.setPreferredSize(new Dimension(70, 26));
        buttonContainer.setBackground(PANEL_BG);
        JButton saveBtn = new JButton("Save");
        saveBtn.setPreferredSize(new Dimension(64, 24));
        saveBtn.setVisible(false);
        styleActionButton(saveBtn);
        buttonContainer.add(saveBtn);

        GradeInputListener listener = new GradeInputListener(prefKey, tf, maxValue, saveBtn);
        tf.addKeyListener(listener);
        tf.addFocusListener(listener);
        saveBtn.addActionListener(e -> listener.saveGrade());

        JLabel maxLabel = new JLabel("/ " + (int)maxValue);
        maxLabel.setForeground(MUTED_FG);
        itemPanel.add(titleLabel);
        itemPanel.add(tf);
        itemPanel.add(buttonContainer);
        itemPanel.add(maxLabel);
        section.add(itemPanel);

        p.add(section);
    }

    private void addNetworkQuizzes(JPanel p) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBorder(BorderFactory.createTitledBorder("Quizzes (Best 3 of 4)"));
        section.setBackground(PANEL_BG);

        double[] netQuizMaxes = {15.0, 21.0, 15.0, 15.0};
        Dimension fieldDim = new Dimension(80, 26);
        for (int i = 0; i < 4; i++) {
            JPanel itemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
            itemPanel.setBackground(PANEL_BG);
            JLabel itemLabel = new JLabel("Quiz " + (i + 1));
            itemLabel.setPreferredSize(new Dimension(110, 20));
            itemLabel.setForeground(TEXT_FG);
            JTextField field = new JTextField();
            field.setPreferredSize(fieldDim);
            field.setMinimumSize(fieldDim);
            field.setMaximumSize(fieldDim);
            field.setBackground(CARD_BG);
            field.setForeground(TEXT_FG);
            field.setCaretColor(ACCENT);
            field.setSelectionColor(new Color(70, 90, 120));
            field.setSelectedTextColor(TEXT_FG);
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER, 1),
                    BorderFactory.createEmptyBorder(3, 6, 3, 6)));
            netQuizzes[0][i] = field;
            String key = "NET_Q_" + i;
            netQuizzes[0][i].setText(prefs.get(key, ""));

            JPanel buttonContainer = new JPanel();
            buttonContainer.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
            buttonContainer.setPreferredSize(new Dimension(70, 26));
            buttonContainer.setBackground(PANEL_BG);
            JButton saveBtn = new JButton("Save");
            saveBtn.setPreferredSize(new Dimension(64, 24));
            saveBtn.setVisible(false);
            styleActionButton(saveBtn);
            buttonContainer.add(saveBtn);

            GradeInputListener listener = new GradeInputListener(key, netQuizzes[0][i], netQuizMaxes[i], saveBtn);
            netQuizzes[0][i].addKeyListener(listener);
            netQuizzes[0][i].addFocusListener(listener);
            saveBtn.addActionListener(e -> listener.saveGrade());

            JLabel maxLabel = new JLabel("/ " + (int)netQuizMaxes[i]);
            maxLabel.setForeground(MUTED_FG);
            itemPanel.add(itemLabel);
            itemPanel.add(netQuizzes[0][i]);
            itemPanel.add(buttonContainer);
            itemPanel.add(maxLabel);
            section.add(itemPanel);
        }

        p.add(section);
    }

    private void styleActionButton(AbstractButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setBackground(ACCENT);
        button.setForeground(Color.WHITE);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFont(UI_FONT_BOLD);
        button.addChangeListener(e -> {
            ButtonModel m = button.getModel();
            if (m.isPressed()) {
                button.setBackground(ACCENT_HOVER.darker());
            } else if (m.isRollover()) {
                button.setBackground(ACCENT_HOVER);
            } else {
                button.setBackground(ACCENT);
            }
        });
    }

    private void setupTable() {
        String[] cols = new String[COURSES.length + 1];
        cols[0] = "Grade";
        System.arraycopy(COURSES, 0, cols, 1, COURSES.length);

        tableModel = new DefaultTableModel(cols, 0);
        for (String letter : LETTERS) {
            Object[] row = new Object[COURSES.length + 1];
            row[0] = letter;
            tableModel.addRow(row);
        }

        summaryTable = new JTable(tableModel);
        summaryTable.setEnabled(false);
        summaryTable.setRowHeight(28);
        summaryTable.setShowGrid(true);
        summaryTable.setIntercellSpacing(new Dimension(0, 0));
        summaryTable.setFillsViewportHeight(true);
        summaryTable.setFont(UI_FONT);
        summaryTable.setBackground(PANEL_BG);
        summaryTable.setForeground(TEXT_FG);
        summaryTable.setSelectionBackground(new Color(58, 64, 80));
        summaryTable.setSelectionForeground(TEXT_FG);
        summaryTable.setGridColor(BORDER);
        summaryTable.getTableHeader().setFont(UI_FONT_BOLD);
        summaryTable.getTableHeader().setBackground(PANEL_ALT);
        summaryTable.getTableHeader().setForeground(TEXT_FG);
        summaryTable.getTableHeader().setReorderingAllowed(false);
        summaryTable.setRowSelectionAllowed(false);
        summaryTable.setColumnSelectionAllowed(false);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground((row % 2 == 0) ? PANEL_BG : PANEL_ALT);
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, BORDER));
                setHorizontalAlignment(JLabel.CENTER);
                setForeground(TEXT_FG);
                return c;
            }
        };

        for (int i = 0; i < summaryTable.getColumnCount(); i++) {
            summaryTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    private void updateAllCalculations() {
        double[] courseScores = {calculateCA(), calculateOS(), calculateMgt(), calculateNet(), calculateDB(), calculateSE()};
        double[] courseWeights = {40.0, 40.0, 50.0, 0.0, 40.0, 40.0};

        for (int gradeIdx = 0; gradeIdx < LETTERS.length; gradeIdx++) {
            double target = THRESHOLDS[gradeIdx];
            tableModel.setValueAt(LETTERS[gradeIdx], gradeIdx, 0);

            for (int courseIdx = 0; courseIdx < COURSES.length; courseIdx++) {
                double currentPercentage = courseScores[courseIdx];
                double finalWeight = courseWeights[courseIdx];

                String required;
                if (finalWeight == 0) {
                    required = (currentPercentage >= target) ? "Achieved" : "X";
                } else {
                    double neededInFinal = ((target - currentPercentage) / finalWeight) * 100;
                    if (neededInFinal > 100 || neededInFinal < 30) required = "X";
                    else required = String.format("%.2f%%", neededInFinal);
                }
                tableModel.setValueAt(required, gradeIdx, courseIdx + 1);
            }
        }
    }

    private double parse(JTextField tf) {
        try {
            if (tf.getText().trim().isEmpty()) return 0;
            return Double.parseDouble(tf.getText().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double getBestN(JTextField[] tfs, int take, double maxPerItem, double weightPct) {
        double[] vals = new double[tfs.length];
        for (int i = 0; i < tfs.length; i++) vals[i] = parse(tfs[i]);
        Arrays.sort(vals);
        double sum = 0;
        for (int i = vals.length - 1; i >= vals.length - take; i--) sum += vals[i];
        return (sum / (take * maxPerItem)) * weightPct;
    }

    private double calculateCA() {
        double q = getBestN(caQuizzes[0], 2, 17, 10.0);
        double l = getBestN(caLabs[0], 5, 10, 10.0);
        double p = (parse(caProject) / 15.0) * 15.0;
        double m = (parse(caMidterm) / 100.0) * 25.0;
        return q + l + p + m;
    }

    private double calculateOS() {
        double q = getBestN(osQuizzes[0], 2, 17, 15.0);
        double p = (parse(osProject) / 20.0) * 20.0;
        double m = (parse(osMidterm) / 100.0) * 25.0;
        return q + p + m;
    }

    private double calculateMgt() {
        return (parse(mgtMidterm) / 100.0) * 50.0;
    }

    private double calculateNet() {
        double l = getBestN(netLabs[0], 7, 10, 34.0);
        double lt = getBestN(netLabTests[0], 2, 10, 33.0);

        double[] maxes = {15.0, 21.0, 15.0, 15.0};
        double[] pcts = new double[4];
        for (int i = 0; i < 4; i++) {
            pcts[i] = (parse(netQuizzes[0][i]) / maxes[i]);
        }
        Arrays.sort(pcts);
        double qSum = pcts[3] + pcts[2] + pcts[1];
        double qWeight = (qSum / 3.0) * 33.0;

        return l + lt + qWeight;
    }

    private double calculateDB() {
        double q = getBestN(dbQuizzes[0], 2, 10, 15.0);
        double m1 = (parse(dbM1) / 10.0) * 10.0;
        double m2 = (parse(dbM2) / 10.0) * 10.0;
        double m = (parse(dbMidterm) / 100.0) * 25.0;
        return q + m1 + m2 + m;
    }

    private double calculateSE() {
        double m1 = (parse(seM1) / 15.0) * 15.0;
        double m2 = (parse(seM2) / 15.0) * 15.0;
        double a = (parse(seAssignment) / 10.0) * 10.0;
        double m = (parse(seMidterm) / 100.0) * 20.0;
        return m1 + m2 + a + m;
    }

    private class GradeInputListener implements KeyListener, FocusListener {
        private final String key;
        private final JTextField tf;
        private final double maxValue;
        private final JButton saveBtn;

        public GradeInputListener(String key, JTextField tf, double maxValue) {
            this.key = key;
            this.tf = tf;
            this.maxValue = maxValue;
            this.saveBtn = null;
        }

        public GradeInputListener(String key, JTextField tf, double maxValue, JButton saveBtn) {
            this.key = key;
            this.tf = tf;
            this.maxValue = maxValue;
            this.saveBtn = saveBtn;
        }

        @Override
        public void focusGained(FocusEvent e) {
            if (saveBtn != null) {
                if (currentSaveButton != null && currentSaveButton != saveBtn) {
                    currentSaveButton.setVisible(false);
                }
                saveBtn.setVisible(true);
                currentSaveButton = saveBtn;
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            Component opposite = e.getOppositeComponent();
            if (saveBtn != null && opposite != null) {
                if (opposite == saveBtn) return;
                Component parent = saveBtn.getParent();
                if (parent != null) {
                    if (opposite == parent) return;
                    try {
                        if (SwingUtilities.isDescendingFrom(opposite, parent)) return;
                    } catch (Exception ignored) {}
                }
            }

            String stored = prefs.get(key, "");
            tf.setText(stored);
            if (saveBtn != null) {
                saveBtn.setVisible(false);
                if (currentSaveButton == saveBtn) currentSaveButton = null;
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                saveGrade();
                e.consume();
            }
        }

        public void saveGrade() {
            String text = tf.getText().trim();

            if (!text.isEmpty()) {
                try {
                    double value = Double.parseDouble(text);
                    if (value > maxValue) {
                        JOptionPane.showMessageDialog(GradeCalculator.this,
                                "Grade cannot exceed " + (int)maxValue + ". Please enter a valid grade.",
                                "Invalid Grade",
                                JOptionPane.ERROR_MESSAGE);
                        tf.setText("");
                        tf.requestFocus();
                        return;
                    }
                    if (value < 0) {
                        JOptionPane.showMessageDialog(GradeCalculator.this,
                                "Grade cannot be negative. Please enter a valid grade.",
                                "Invalid Grade",
                                JOptionPane.ERROR_MESSAGE);
                        tf.setText("");
                        tf.requestFocus();
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(GradeCalculator.this,
                            "Please enter a valid number.",
                            "Invalid Input",
                            JOptionPane.ERROR_MESSAGE);
                    tf.setText("");
                    tf.requestFocus();
                    return;
                }
            }

            prefs.put(key, tf.getText().trim());
            updateAllCalculations();

            showNotification();
        }

        @Override public void keyReleased(KeyEvent e) { }
        @Override public void keyTyped(KeyEvent e) { }
    }

    private void showNotification() {
        Object cobj = notificationLabel.getClientProperty("container");
        if (!(cobj instanceof JPanel)) return;
        JPanel notifContainer = (JPanel) cobj;

        if (notificationTimer != null && notificationTimer.isRunning()) {
            notificationTimer.stop();
        }

        Dimension pref = notificationLabel.getPreferredSize();
        notifContainer.setSize(pref.width + 10, pref.height);

        JLayeredPane lp = getLayeredPane();
        int paneWidth = lp.getWidth();
        int targetX = Math.max(10, paneWidth - notifContainer.getWidth() - 20);
        int startX = paneWidth;
        int y = 10;

        notifContainer.setLocation(startX, y);
        notifContainer.setVisible(true);
        notificationLabel.setVisible(true);
        notifContainer.revalidate();
        notifContainer.repaint();

        int animationDuration = 200;
        int delay = 15;
        int frames = Math.max(1, animationDuration / delay);
        final int dx = startX - targetX;
        final int[] currentFrame = {0};

        notificationTimer = new Timer(delay, null);
        notificationTimer.addActionListener(e -> {
            currentFrame[0]++;
            double t = (double) currentFrame[0] / frames;
            if (t > 1) t = 1;
            double ease = 1 - Math.pow(1 - t, 3);
            int x = startX - (int) Math.round(dx * ease);
            notifContainer.setLocation(x, y);
            notifContainer.repaint();
            if (t >= 1) {
                notificationTimer.stop();
                Timer pause = new Timer(1000, ev -> {
                    ((Timer) ev.getSource()).stop();
                    animateNotificationOut(notifContainer);
                });
                pause.setRepeats(false);
                pause.start();
            }
        });
        notificationTimer.start();
    }

    private void animateNotificationOut(JPanel notifContainer) {
        if (notificationTimer != null && notificationTimer.isRunning()) notificationTimer.stop();

        JLayeredPane lp = getLayeredPane();
        int paneWidth = lp.getWidth();
        int startX = notifContainer.getX();
        int endX = paneWidth;
        int y = notifContainer.getY();

        int animationDuration = 200;
        int delay = 15;
        int frames = Math.max(1, animationDuration / delay);
        final int dx = endX - startX;
        final int[] currentFrame = {0};

        notificationTimer = new Timer(delay, null);
        notificationTimer.addActionListener(e -> {
            currentFrame[0]++;
            double t = (double) currentFrame[0] / frames;
            if (t > 1) t = 1;
            double ease = Math.pow(t, 3);
            int x = startX + (int) Math.round(dx * ease);
            notifContainer.setLocation(x, y);
            notifContainer.repaint();
            if (t >= 1) {
                notificationTimer.stop();
                notifContainer.setVisible(false);
            }
        });
        notificationTimer.start();
    }

    private void positionNotificationContainer(JPanel notifContainer) {
        int x = getWidth() - notifContainer.getWidth() - 10;
        int y = 10;
        notifContainer.setLocation(x, y);
        notifContainer.revalidate();
        notifContainer.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GradeCalculator().setVisible(true));
    }
}
