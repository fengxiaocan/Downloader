package com.down.ui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.x.down.XDownload;
import com.x.down.config.XConfig;
import com.x.down.core.HttpDownload;
import com.x.down.core.XDownloadRequest;
import com.x.down.data.Headers;
import com.x.down.data.Params;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class Downloader extends JFrame {

    private JPanel contentPane;

    private JTextField inputText;
    private JTextField inputHeader;
    private JTextField inputParams;
//    private JTextField inputUa;

    private JSpinner spinnerRetryTime;
    private JSpinner spinnerRetryInterval;
    private JSpinner spinnerOutTime;

    private JCheckBox cbMulti;
    private JCheckBox cbRetry;
    private JCheckBox cbResume;

    private JSpinner spinnerMulitCore;
    private JSpinner spinnerDownMax;
    private JSpinner spinnerDownMin;

    private JButton btSave;
    private JLabel labelSave;

    private JButton btReset;
    private JButton btDown;

    private JList historyList;
    private JButton btClear;
    private JComboBox comboBoxUa;

    private final String hintUrl = "http://...";
    private final String hintText = "格式为 name1=value1&name2=value2";

    /**
     * 初始化数据
     */
    private void initData() {
        //初始化提示文本
        inputText.addFocusListener(new JTextFieldHintListener(inputText, hintUrl));
        inputHeader.addFocusListener(new JTextFieldHintListener(inputHeader, hintText));
        inputParams.addFocusListener(new JTextFieldHintListener(inputParams, hintText));
        //初始化UA
        comboBoxUa.setEditable(true);
        ComboBoxEditor editor = comboBoxUa.getEditor();
        //默认UA
        comboBoxUa.configureEditor(editor, XConfig.getDefaultUserAgent());
        //可选UA
        for (String item : UserAgent.UserAgentArray) {
            comboBoxUa.addItem(item);
        }
        //重试次数
        spinnerRetryTime.setModel(new SpinnerNumberModel(100, 0, 999999, 1));
        //重试间隔时间
        spinnerRetryInterval.setModel(new SpinnerNumberModel(20, 0, Integer.MAX_VALUE, 1));
        //超时时间
        spinnerOutTime.setModel(new SpinnerNumberModel(60, 5, Integer.MAX_VALUE, 1));
        //多线程核心数
        spinnerMulitCore.setModel(new SpinnerNumberModel(10, 0, 100, 1));
        //下载的最大块
        spinnerDownMax.setModel(new SpinnerNumberModel(5, 1, 999, 1));
        //下载的最小块
        spinnerDownMin.setModel(new SpinnerNumberModel(100, 10, 1000, 1));
        //保存路径
        labelSave.setText(System.getProperty("user.dir"));
        //历史记录选择
        historyList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int firstIndex = e.getFirstIndex();
                String url = (String) historyList.getModel().getElementAt(firstIndex);
                inputText.setText(url);
            }
        });
        addHistoryList();
    }

    /**
     * 添加历史记录
     */
    private void addHistoryList() {
        new Thread(() -> {
            historyList.setListData(DownloadHistory.getHistory());
        }).start();
    }

    public Downloader() {
        setContentPane(contentPane);

//        getRootPane().setDefaultButton(btDown);

        btDown.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startDown();
            }
        });

        btReset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                restartDown();
            }
        });
        btClear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DownloadHistory.clearHistory();
                historyList.setListData(new String[0]);
            }
        });
        //选择保存文件夹
        btSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser jf = new JFileChooser();
                jf.setMultiSelectionEnabled(false);
                jf.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                jf.setApproveButtonText("选择文件夹");
                jf.setApproveButtonToolTipText("选择文件夹");

                int i = jf.showOpenDialog(btSave);//显示打开的文件对话框
                if (i == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = jf.getSelectedFile();//使用文件类获取选择器选择的文件
                    //保存路径
                    labelSave.setText(selectedFile.getAbsolutePath());
                }
            }
        });
        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        initData();
    }

    /**
     * 格式化请求头
     *
     * @return
     */
    private Headers getDownHeaders() {
        if (inputHeader.getText().equals(hintText)) {
            return null;
        }
        Headers headers = new Headers();
        String text = inputHeader.getText();
        String[] split = text.split("&");
        if (split != null && split.length > 0) {
            for (String header : split) {
                String[] strings = header.split("=");
                if (strings != null && strings.length == 2) {
                    headers.addHeader(strings[0], strings[1]);
                }
            }
        }
        return headers;
    }

    /**
     * 格式化下载请求参数
     *
     * @return
     */
    private Params getDownParams() {
        if (inputParams.getText().equals(hintText)) {
            return null;
        }
        Params params = new Params();
        String text = inputParams.getText();
        String[] split = text.split("&");
        if (split != null && split.length > 0) {
            for (String param : split) {
                String[] strings = param.split("=");
                if (strings != null && strings.length == 2) {
                    params.addParams(strings[0], strings[1]);
                }
            }
        }
        return params;
    }

    /**
     * 生成下载地址
     *
     * @return
     */
    private HttpDownload getDown() {
        return XDownload.download(inputText.getText())
                .setHeader(getDownHeaders())
                .setParams(getDownParams())
                .setAutoRetryTimes((Integer) spinnerRetryTime.getModel().getValue())
                .setAutoRetryInterval((Integer) spinnerRetryInterval.getModel().getValue())
                .setConnectTimeOut((Integer) spinnerOutTime.getModel().getValue() * 1000)
                .setDownloadMultiThreadSize((Integer) spinnerMulitCore.getModel().getValue())
                .setMaxDownloadBlockSize((Integer) spinnerDownMax.getModel().getValue() * 1024 * 1024)
                .setMinDownloadBlockSize((Integer) spinnerDownMin.getModel().getValue() * 1024)
                .setUseMultiThread(cbMulti.isSelected())
                .setUseAutoRetry(cbRetry.isSelected())
                .setUseBreakpointResume(cbResume.isSelected())
                .setUserAgent((String) comboBoxUa.getSelectedItem())
                .setCacheDir(labelSave.getText())
                .setUpdateProgressTimes(500)
                .setUpdateSpeedTimes(1000);
    }

    /**
     * 开始下载
     */
    private void startDown() {
        if (inputText.getText().equals(hintUrl) || !inputText.getText().startsWith("http")) {
            JOptionPane.showConfirmDialog(this, "请输入下载地址", "提醒!", JOptionPane.OK_CANCEL_OPTION);
        } else {
            XDownloadRequest httpDownload = (XDownloadRequest) getDown();
            XDownload xDownload = XDownload.get();
            xDownload.cancleDownload(httpDownload.getTag());

            DownloadHistory.addHistory(httpDownload.getConnectUrl());
            addHistoryList();
            DownloadingUi.show(httpDownload, xDownload);
        }
    }

    /**
     * 重新下载
     */
    private void restartDown() {
        if (inputText.getText().equals(hintUrl) || !inputText.getText().startsWith("http")) {
            JOptionPane.showConfirmDialog(this, "请输入下载地址", "提醒!", JOptionPane.OK_CANCEL_OPTION);
        } else {
            XDownloadRequest httpDownload = (XDownloadRequest) getDown().delete();
            DownloadingUi.show(httpDownload, XDownload.get());
        }
    }

    /**
     * 关闭窗口
     */
    private void onCancel() {
        // add your code here if necessary
        int confirmDialog = JOptionPane.showConfirmDialog(contentPane, "退出将会停止当前所有的下载,是否确认退出?", "提示", JOptionPane.YES_NO_OPTION);
        //如果这个整数等于JOptionPane.YES_OPTION，则说明你点击的是“确定”按钮，则允许继续操作，否则结束
        if (confirmDialog == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    private int getContentWidth() {
        return contentPane.getMinimumSize().width;
    }

    private int getContentHeight() {
        return contentPane.getMinimumSize().height;
    }

    public static void main(String[] args) {
        Downloader dialog = new Downloader();
//        dialog.setMinimumSize(new Dimension(window_width, window_height));
        dialog.setBounds(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - dialog.getContentWidth(),
                Toolkit.getDefaultToolkit().getScreenSize().height / 2 - dialog.getContentHeight(),
                dialog.getContentWidth(), dialog.getContentHeight());
        dialog.pack();
        dialog.setVisible(true);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(9, 1, new Insets(10, 10, 10, 10), -1, -1));
        contentPane.setMinimumSize(new Dimension(600, 300));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        btDown = new JButton();
        btDown.setText("下载");
        panel2.add(btDown, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btReset = new JButton();
        btReset.setText("重新下载");
        panel2.add(btReset, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btClear = new JButton();
        btClear.setLabel("清理历史记录");
        btClear.setText("清理历史记录");
        panel2.add(btClear, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, 0));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, true));
        inputText = new JTextField();
        inputText.setMargin(new Insets(2, 6, 2, 6));
        inputText.setName("");
        inputText.setText("");
        inputText.setToolTipText("请输入下载地址");
        panel3.add(inputText, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("下载地址:");
        panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(80, -1), new Dimension(80, -1), new Dimension(80, -1), 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, false, true));
        contentPane.add(panel4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, true));
        inputHeader = new JTextField();
        panel4.add(inputHeader, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("请求头:");
        panel4.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(80, -1), new Dimension(80, -1), new Dimension(80, -1), 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 7, new Insets(0, 0, 0, 0), -1, -1));
        panel5.setAutoscrolls(false);
        contentPane.add(panel5, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, true));
        final JLabel label3 = new JLabel();
        label3.setText("出错重试次数:");
        panel5.add(label3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        spinnerRetryTime = new JSpinner();
        spinnerRetryTime.setAutoscrolls(true);
        spinnerRetryTime.setDoubleBuffered(true);
        spinnerRetryTime.setToolTipText("选择多线程下载核心数");
        panel5.add(spinnerRetryTime, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(80, -1), new Dimension(80, -1), new Dimension(80, -1), 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("出错重试间隔/豪秒:");
        panel5.add(label4, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cbRetry = new JCheckBox();
        cbRetry.setLabel("出错重试");
        cbRetry.setSelected(true);
        cbRetry.setText("出错重试");
        panel5.add(cbRetry, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setAutoscrolls(false);
        label5.setText("连接超时/秒:");
        panel5.add(label5, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        spinnerOutTime = new JSpinner();
        panel5.add(spinnerOutTime, new GridConstraints(0, 6, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, -1), new Dimension(100, -1), new Dimension(100, -1), 0, false));
        spinnerRetryInterval = new JSpinner();
        spinnerRetryInterval.setAlignmentX(0.5f);
        spinnerRetryInterval.setDoubleBuffered(false);
        spinnerRetryInterval.setOpaque(true);
        spinnerRetryInterval.setRequestFocusEnabled(true);
        panel5.add(spinnerRetryInterval, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, -1), new Dimension(100, -1), new Dimension(100, -1), 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel6.setAutoscrolls(false);
        contentPane.add(panel6, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("参数:");
        panel6.add(label6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(80, -1), new Dimension(80, -1), new Dimension(80, -1), 0, false));
        inputParams = new JTextField();
        panel6.add(inputParams, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(1, 8, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel7, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, true));
        cbMulti = new JCheckBox();
        cbMulti.setContentAreaFilled(true);
        cbMulti.setLabel("启用多线程");
        cbMulti.setSelected(true);
        cbMulti.setText("启用多线程");
        panel7.add(cbMulti, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setAutoscrolls(false);
        label7.setText("多线程下载核心数");
        panel7.add(label7, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        spinnerMulitCore = new JSpinner();
        spinnerMulitCore.setAutoscrolls(true);
        panel7.add(spinnerMulitCore, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(60, -1), new Dimension(60, -1), new Dimension(60, -1), 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("下载分块最大值/MB");
        panel7.add(label8, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        spinnerDownMax = new JSpinner();
        panel7.add(spinnerDownMax, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, -1), new Dimension(100, -1), new Dimension(100, -1), 0, false));
        final JLabel label9 = new JLabel();
        label9.setAutoscrolls(true);
        label9.setText("下载分块最小值/KB");
        panel7.add(label9, new GridConstraints(0, 6, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        spinnerDownMin = new JSpinner();
        panel7.add(spinnerDownMin, new GridConstraints(0, 7, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, -1), new Dimension(100, -1), new Dimension(100, -1), 0, false));
        cbResume = new JCheckBox();
        cbResume.setEnabled(true);
        cbResume.setLabel("启用断点续传");
        cbResume.setSelected(true);
        cbResume.setText("启用断点续传");
        panel7.add(cbResume, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel8, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel8.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        historyList = new JList();
        historyList.setEnabled(true);
        historyList.setLayoutOrientation(0);
        historyList.setSelectionMode(1);
        historyList.setValueIsAdjusting(false);
        historyList.putClientProperty("List.isFileList", Boolean.FALSE);
        historyList.putClientProperty("html.disable", Boolean.FALSE);
        scrollPane1.setViewportView(historyList);
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel9.setAutoscrolls(false);
        contentPane.add(panel9, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, true));
        btSave = new JButton();
        btSave.setLabel("选择保存路径");
        btSave.setText("选择保存路径");
        panel9.add(btSave, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelSave = new JLabel();
        labelSave.setText("保存路径地址");
        labelSave.putClientProperty("html.disable", Boolean.FALSE);
        panel9.add(labelSave, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel9.add(spacer1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel10, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, true));
        final JLabel label10 = new JLabel();
        label10.setText("User-Agent");
        panel10.add(label10, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(80, -1), new Dimension(80, -1), new Dimension(80, -1), 0, false));
        comboBoxUa = new JComboBox();
        comboBoxUa.setAutoscrolls(true);
        comboBoxUa.setBackground(new Color(-12828863));
        comboBoxUa.setEditable(true);
        panel10.add(comboBoxUa, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        label1.setLabelFor(inputText);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }


}
