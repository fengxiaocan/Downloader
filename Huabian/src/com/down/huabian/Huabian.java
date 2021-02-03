package com.down.huabian;

import com.google.gson.Gson;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.x.down.XDownload;
import com.x.down.base.IDownloadRequest;
import com.x.down.base.IRequest;
import com.x.down.data.Headers;
import com.x.down.data.Response;
import com.x.down.listener.OnDownloadListener;
import com.x.down.listener.OnResponseListener;
import com.x.down.tool.XDownUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Huabian extends JDialog {
    private JPanel contentPane;
    private JTextField inputText;
    private JButton bt_down;
    private JTextArea logText;
    private JTextArea textList;
    private Headers headers;
    private String baseUrl = "https://hbimg.huabanimg.com/";

    public Huabian() {
        $$$setupUI$$$();
        setContentPane(contentPane);
        setModal(true);
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
        initHeader();
        XDownload.get().config().ignoredSpeed(true).ignoredProgress(true).isUseMultiThread(false).downloadMultiThreadSize(20);
        bt_down.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!inputText.getText().startsWith("http")) {
                    JOptionPane.showConfirmDialog(Huabian.this, "请输入下载地址", "提醒!", JOptionPane.OK_CANCEL_OPTION);
                } else {
                    reptile(inputText.getText(), XDownUtils.getMd5(inputText.getText()));
                }
            }
        });
    }

    private void addDownloads(String name, SimplePins array) {
        File file = new File(System.getProperty("user.dir"), name);
        String absolutePath = file.getAbsolutePath();
        for (SimpleHuaBean bean : array) {
            addDownload(absolutePath, baseUrl + bean.getFile().getKey());
        }
    }

    private void addDownload(String file, String url) {
        XDownload.download(url).setCacheDir(file).setSaveFile(new File(file, XDownUtils.getMd5(url) + ".jpg").getAbsolutePath()).setUseMultiThread(false).setDownloadListener(new OnDownloadListener() {
            @Override
            public void onComplete(IDownloadRequest iDownloadRequest) {
                printList(url);
            }

            @Override
            public void onFailure(IDownloadRequest iDownloadRequest) {
                printLog("下载失败:" + url);
            }
        }).start();
    }

    private static class UrlListModel extends AbstractListModel<String> {
        private List<String> data;

        public UrlListModel(List<String> data) {
            this.data = data;
        }

        @Override
        public int getSize() {
            if (data != null) {
                return data.size();
            }
            return 0;
        }

        @Override
        public String getElementAt(int index) {
            return data.get(index);
        }
    }

    private void reptile(String url, String name) {
        printLog("正在抓取:" + url);
        XDownload.request(url).setHeader(headers).setOnResponseListener(new OnResponseListener() {
            @Override
            public void onResponse(IRequest iRequest, Response response) {
                if (response.isSuccess()) {
                    String baseUrl = url.contains("?") ? url.substring(0, url.indexOf("?")) : url;

                    if (url.contains("explore")) {
                        SimplePins array = getExploreGson(response.result());
                        if (array != null && array.size() > 0) {
                            SimpleHuaBean huaBean = array.get(array.size() - 1);
                            reptile(baseUrl + "?max=" + huaBean.getPin_id() + "&limit=20&wfl=1", name);
                            addDownloads(name, array);
                        } else {
                            printLog("抓取完成!!");
                        }
                    } else if (url.contains("search")) {
                        SimplePins array = getExploreGson(response.result());
                        if (array != null && array.size() > 0) {
                            reptile(replacePage(url), name);
                            addDownloads(name, array);
                        } else {
                            printLog("抓取完成!!");
                        }
                    } else if (url.contains("boards")) {
                        SimpleBoardBean bean = getBoardsGson(response.result());
                        if (bean != null) {
                            SimplePins array = bean.getPins();
                            if (array != null && array.size() > 0) {
                                SimpleHuaBean huaBean = array.get(array.size() - 1);
                                reptile(baseUrl + "?max=" + huaBean.getPin_id() + "&limit=20&wfl=1", name);
                                addDownloads(name, array);
                            } else {
                                printLog("抓取完成!!");
                            }
                        } else {
                            printLog("抓取完成!!");
                        }
                    } else if (url.contains("pins")) {
                        SimpleHuaBean bean = getPinsGson(response.result());
                        if (bean != null) {
                            String url1 = "https://huaban.com/boards/" + bean.getBoard_id();
                            reptile(url1, XDownUtils.getMd5(url1));
                        } else {
                            printLog("抓取完成!!");
                        }
                    } else {
                        printLog("抓取失败!!暂未收录该页面");
                    }
                } else {
                    printLog(response.error());
                }
            }

            @Override
            public void onError(IRequest iRequest, Exception e) {
                printLog("Exception=" + e.getMessage());
            }
        }).start();
    }

    private String replacePage(String response) {
        Pattern pattern = Pattern.compile("(?:page=)(\\d*)");
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            String group = matcher.group(1);
            int page = 1;
            try {
                page = Integer.valueOf(group);
            } catch (NumberFormatException e) {
                page = 1;
            }
            page++;
            return matcher.replaceFirst("page=" + page);
        }
        return response + "&page=2&per_page=20&wfl=1";
    }

    private SimplePins getExploreGson(String response) {
        Pattern pattern = Pattern.compile("(?:app\\.page\\[\"pins\"\\])\\s*=\\s*(.*)(?:;\\s*app)");
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            String group = matcher.group(1);
            return new Gson().fromJson(group, SimplePins.class);
        }
        return null;
    }

    private SimpleBoardBean getBoardsGson(String response) {
        Pattern pattern = Pattern.compile("(?:app\\.page\\[\"board\"\\])\\s*=\\s*(.*)(?:;\\s*app)");
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            String group = matcher.group(1);
            return new Gson().fromJson(group, SimpleBoardBean.class);
        }
        return null;
    }


    private SimpleHuaBean getPinsGson(String response) {
        Pattern pattern = Pattern.compile("(?:app\\.page\\[\"pin\"\\])\\s*=\\s*(.*)(?:;\\s*app)");
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            String group = matcher.group(1);
            return new Gson().fromJson(group, SimpleHuaBean.class);
        }
        return null;
    }


    private void printLog(String value) {
        logText.append(value + "\r\n");
        int length = logText.getText().length();
        logText.setCaretPosition(length);
    }


    private void printList(String value) {
        textList.append(value + "\r\n");
        int length = textList.getText().length();
        textList.setCaretPosition(length);
    }

    private void initHeader() {
        headers = new Headers();
        headers.addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.97 Mobile Safari/537.36");
//        headers.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
        headers.addHeader("Accept-Encoding", "gzip, deflate, br");
        headers.addHeader("Accept-Language", "zh-CN,zh;q=0.9");
        headers.addHeader("Connection", "keep-alive");
        headers.addHeader("Accept", "application/json");
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    private int getContentWidth() {
        return contentPane.getMinimumSize().width;
    }

    private int getContentHeight() {
        return contentPane.getMinimumSize().height;
    }

    public static void main(String[] args) {
        Huabian dialog = new Huabian();
        dialog.setTitle("花瓣网抓取工具");
        dialog.setMinimumSize(new Dimension(500, 300));
        dialog.setBounds(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - dialog.getContentWidth(),
                Toolkit.getDefaultToolkit().getScreenSize().height / 2 - dialog.getContentHeight(),
                dialog.getContentWidth(), dialog.getContentHeight());
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
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
        contentPane.setLayout(new GridLayoutManager(1, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, true));
        inputText = new JTextField();
        inputText.setToolTipText("请输入花瓣的画板网址");
        panel2.add(inputText, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        bt_down = new JButton();
        bt_down.setLabel("下载");
        bt_down.setText("下载");
        panel2.add(bt_down, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, new Dimension(-1, 100), null, new Dimension(-1, 500), 0, false));
        logText = new JTextArea();
        logText.setEditable(false);
        logText.setLineWrap(true);
        scrollPane1.setViewportView(logText);
        final JScrollPane scrollPane2 = new JScrollPane();
        panel1.add(scrollPane2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, new Dimension(-1, 100), null, null, 0, false));
        textList = new JTextArea();
        textList.setEditable(false);
        textList.setLineWrap(true);
        scrollPane2.setViewportView(textList);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
