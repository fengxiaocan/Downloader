package com.down.huabian;

public class FileBean {
    /**
     * bucket : hbimg
     * key : 304342b40efca7c7ca18c14870fbef5c517c529039130-trIptC
     * type : image/jpeg
     * height : 975
     * width : 650
     * frames : 1
     */

    private String bucket;
    private String key;
    private String type;
    private int height;
    private int width;
    private int frames;

    public String getBucket(){ return bucket;}

    public void setBucket(String bucket){ this.bucket=bucket;}

    public String getKey(){ return key;}

    public void setKey(String key){ this.key=key;}

    public String getType(){ return type;}

    public void setType(String type){ this.type=type;}

    public int getHeight(){ return height;}

    public void setHeight(int height){ this.height=height;}

    public int getWidth(){ return width;}

    public void setWidth(int width){ this.width=width;}

    public int getFrames(){ return frames;}

    public void setFrames(int frames){ this.frames=frames;}
}
