package com.down.huabian;

public class SimpleHuaBean {

    private long pin_id;
    private long user_id;
    private long board_id;
    private long file_id;
    private FileBean file;
    private String raw_text;
    private String orig_source;

    public long getPin_id() {
        return pin_id;
    }

    public void setPin_id(long pin_id) {
        this.pin_id = pin_id;
    }

    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    public long getBoard_id() {
        return board_id;
    }

    public void setBoard_id(long board_id) {
        this.board_id = board_id;
    }

    public long getFile_id() {
        return file_id;
    }

    public void setFile_id(long file_id) {
        this.file_id = file_id;
    }

    public FileBean getFile() {
        return file;
    }

    public void setFile(FileBean file) {
        this.file = file;
    }

    public String getRaw_text() {
        return raw_text;
    }

    public void setRaw_text(String raw_text) {
        this.raw_text = raw_text;
    }

    public String getOrig_source() {
        return orig_source;
    }

    public void setOrig_source(String orig_source) {
        this.orig_source = orig_source;
    }
}
