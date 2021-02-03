package com.down.ui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.x.down.XDownload;
import com.x.down.base.IDownloadRequest;
import com.x.down.core.HttpDownload;
import com.x.down.core.XDownloadRequest;
import com.x.down.listener.OnDownloadConnectListener;
import com.x.down.listener.OnDownloadListener;
import com.x.down.listener.OnProgressListener;
import com.x.down.listener.OnSpeedListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

public class DownloadingUi extends JFrame {
    private HttpDownload httpDownload;
    private JPanel contentPane;
    private JProgressBar progressBar;
    private JLabel downProgress;
    private JLabel downSpeed;
    private JTextArea textLog;

    private JButton buttonPause;
    private JButton buttonCancel;
    private JButton buttonRestart;
    private JButton buttonResume;

    public DownloadingUi(HttpDownload down, XDownload xDownload) {
        this.httpDownload = down;
        setContentPane(contentPane);

//        getRootPane().setDefaultButton(buttonPause);

        buttonPause.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                xDownload.cancleDownload(((XDownloadRequest) httpDownload).getTag());
                buttonPause.setEnabled(false);
            }
        });

        buttonResume.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                httpDownload.start();
                buttonResume.setEnabled(false);
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int confirmDialog = JOptionPane.showConfirmDialog(contentPane, "是否取消当前下载?", "取消提醒!", JOptionPane.YES_NO_OPTION);
                //如果这个整数等于JOptionPane.YES_OPTION，则说明你点击的是“确定”按钮，则允许继续操作，否则结束
                if (confirmDialog == JOptionPane.YES_OPTION) {
                    xDownload.cancleDownload(((XDownloadRequest) httpDownload).getTag());
                    buttonCancel.setEnabled(false);
                    onCancel();
                }
            }
        });
        buttonRestart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int confirmDialog = JOptionPane.showConfirmDialog(contentPane, "是否重新下载?", "提醒!", JOptionPane.YES_NO_OPTION);
                //如果这个整数等于JOptionPane.YES_OPTION，则说明你点击的是“确定”按钮，则允许继续操作，否则结束
                if (confirmDialog == JOptionPane.YES_OPTION) {
                    httpDownload.delete();
                    httpDownload.start();
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
        //最大进度条
        progressBar.setMaximum(100);
        httpDownload
                .setConnectListener(new OnDownloadConnectListener() {
                    @Override
                    public void onPending(IDownloadRequest iDownloadRequest) {
                        printLog("准备下载中");
                        buttonRestart.setEnabled(false);
                        buttonResume.setEnabled(false);
                        buttonPause.setEnabled(true);
                        buttonCancel.setEnabled(true);
                    }

                    @Override
                    public void onStart(IDownloadRequest iDownloadRequest) {
                        printLog("开始下载");
                        buttonRestart.setEnabled(false);
                        buttonResume.setEnabled(false);
                        buttonPause.setEnabled(true);
                        buttonCancel.setEnabled(true);
                    }

                    @Override
                    public void onConnecting(IDownloadRequest iDownloadRequest) {
                        printLog("正在连接下载--下载文件大小为:" + formatFileSize(iDownloadRequest.getTotalLength(), false));
                        buttonRestart.setEnabled(false);
                        buttonResume.setEnabled(false);
                        buttonPause.setEnabled(true);
                        buttonCancel.setEnabled(true);
                    }

                    @Override
                    public void onRequestError(IDownloadRequest iDownloadRequest, int i, String s) {
                        printLog("请求失败:code=" + i + ":" + s);
                    }

                    @Override
                    public void onCancel(IDownloadRequest iDownloadRequest) {
                        printLog("下载已取消");
                        buttonResume.setEnabled(true);
                        buttonPause.setEnabled(false);
                        buttonCancel.setEnabled(false);
                        buttonRestart.setEnabled(true);
                    }

                    @Override
                    public void onRetry(IDownloadRequest iDownloadRequest) {
                        printLog("正在重试--重试次数:" + iDownloadRequest.retryCount());
                    }
                })
                .setDownloadListener(new OnDownloadListener() {
                    @Override
                    public void onComplete(IDownloadRequest iDownloadRequest) {
                        printLog("下载已完成--保存文件为:" + iDownloadRequest.getFilePath());
                        buttonResume.setEnabled(false);
                        buttonPause.setEnabled(false);
                        buttonCancel.setEnabled(false);
                        buttonRestart.setEnabled(true);
                        new Timer("timer").schedule(new TimerTask() {
                            @Override
                            public void run() {
                                onCancel();
                            }
                        }, 5000);
                    }

                    @Override
                    public void onFailure(IDownloadRequest iDownloadRequest) {
                        printLog("下载失败");
                        buttonResume.setEnabled(false);
                        buttonPause.setEnabled(false);
                        buttonCancel.setEnabled(false);
                        buttonRestart.setEnabled(true);
                    }
                })
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(IDownloadRequest iDownloadRequest, float v) {
                        downProgress.setText("进度:" + (int) (v * 100) + "%");
                        progressBar.setValue((int) (v * 100));
                    }
                })
                .setOnSpeedListener(new OnSpeedListener() {
                    @Override
                    public void onSpeed(IDownloadRequest iDownloadRequest, int i, int i1) {
                        downSpeed.setText("速度:" + formatFileSize((long) (i / (i1 / 1000f)), false) + "/秒");
                    }
                }).start();
    }


    /**
     * 格式化文件大小，保留末尾的0，达到长度一致
     *
     * @param len      大小
     * @param keepZero 是否保留小数点
     * @return
     */
    public static String formatFileSize(long len, boolean keepZero) {
        String size;
        DecimalFormat formatKeepTwoZero = new DecimalFormat("#.00");
        DecimalFormat formatKeepOneZero = new DecimalFormat("#.0");
        if (len < 1024) {
            size = len + "B";
        } else if (len < 10 * 1024) {
            // [0, 10KB)，保留两位小数
            size = len * 100 / 1024 / 100f + "KB";
        } else if (len < 100 * 1024) {
            // [10KB, 100KB)，保留一位小数
            size = len * 10 / 1024 / 10f + "KB";
        } else if (len < 1024 * 1024) {
            // [100KB, 1MB)，个位四舍五入
            size = len / 1024 + "KB";
        } else if (len < 10 * 1024 * 1024) {
            // [1MB, 10MB)，保留两位小数
            if (keepZero) {
                size = formatKeepTwoZero.format(len * 100 / 1024 / 1024 / 100f) + "MB";
            } else {
                size = len * 100 / 1024 / 1024 / (float) 100 + "MB";
            }
        } else if (len < 100 * 1024 * 1024) {
            // [10MB, 100MB)，保留一位小数
            if (keepZero) {
                size = formatKeepOneZero.format(len * 10 / 1024 / 1024 / 10f) + "MB";
            } else {
                size = len * 10 / 1024 / 1024 / (float) 10 + "MB";
            }
        } else if (len < 1024 * 1024 * 1024) {
            // [100MB, 1GB)，个位四舍五入
            size = len / 1024 / 1024 + "MB";
        } else {
            // [1GB, ...)，保留两位小数
            size = len * 100 / 1024 / 1024 / 1024 / 100f + "GB";
        }
        return size;
    }

    private void printLog(String value) {
        textLog.append(value + "\r\n");
        int length = textLog.getText().length();
        textLog.setCaretPosition(length);
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
//        setVisible(false);
    }

    private int getContentWidth() {
        return contentPane.getMinimumSize().width;
    }

    private int getContentHeight() {
        return contentPane.getMinimumSize().height;
    }

    public static void show(HttpDownload httpDownload, XDownload xDownload) {
        DownloadingUi dialog = new DownloadingUi(httpDownload, xDownload);
        dialog.setMinimumSize(new Dimension(dialog.getContentWidth(), dialog.getContentHeight()));
        dialog.setBounds(100, 100, dialog.getContentWidth(), dialog.getContentHeight());
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
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        contentPane.setMinimumSize(new Dimension(600, 300));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setActionCommand("");
        buttonCancel.setLabel("取消下载");
        buttonCancel.setText("取消下载");
        panel2.add(buttonCancel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonPause = new JButton();
        buttonPause.setActionCommand("");
        buttonPause.setEnabled(true);
        buttonPause.setFocusPainted(true);
        buttonPause.setFocusable(false);
        buttonPause.setLabel("暂停下载");
        buttonPause.setOpaque(true);
        buttonPause.setRequestFocusEnabled(true);
        buttonPause.setText("暂停下载");
        panel2.add(buttonPause, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonRestart = new JButton();
        buttonRestart.setActionCommand("");
        buttonRestart.setLabel("重新下载");
        buttonRestart.setText("重新下载");
        panel2.add(buttonRestart, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonResume = new JButton();
        buttonResume.setActionCommand("");
        buttonResume.setEnabled(true);
        buttonResume.setFocusPainted(true);
        buttonResume.setFocusable(false);
        buttonResume.setLabel("恢复下载");
        buttonResume.setOpaque(true);
        buttonResume.setRequestFocusEnabled(true);
        buttonResume.setText("恢复下载");
        panel2.add(buttonResume, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, true));
        progressBar = new JProgressBar();
        panel4.add(progressBar, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        downProgress = new JLabel();
        downProgress.setText("下载进度");
        downProgress.putClientProperty("html.disable", Boolean.FALSE);
        panel4.add(downProgress, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, -1), new Dimension(100, -1), new Dimension(100, -1), 0, false));
        downSpeed = new JLabel();
        downSpeed.setHorizontalAlignment(4);
        downSpeed.setText("下载速度");
        panel4.add(downSpeed, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, -1), new Dimension(100, -1), new Dimension(100, -1), 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel3.add(scrollPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        textLog = new JTextArea();
        textLog.setEditable(false);
        scrollPane1.setViewportView(textLog);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
