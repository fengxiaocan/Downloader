package com.down.huabian;

public class HuaBean {

    /**
     * pin_id : 1607372295
     * user_id : 18691263
     * board_id : 38267508
     * file_id : 188881376
     * file : {"bucket":"hbimg","key":"304342b40efca7c7ca18c14870fbef5c517c529039130-trIptC","type":"image/jpeg","height":975,"width":650,"frames":1}
     * media_type : 0
     * source : bcy.net
     * link : https://bcy.net/image/full?index=5&type=coser&id=2274066&url=https%3A%2F%2Fimg9.bcyimg.com%2Fcoser%2F56442%2Fpost%2Fc0jb6%2Fcqkkt4817pkmtwefu4mmfbjjjfe7abxo.jpg%2Fw650
     * raw_text :
     * text_meta : {"tags":[]}
     * via : 2
     * via_user_id : 0
     * original : null
     * created_at : 1524055806
     * like_count : 3
     * comment_count : 0
     * repin_count : 25
     * is_private : 0
     * extra : null
     * orig_source : https://img9.bcyimg.com/coser/56442/post/c0jb6/cqkkt4817pkmtwefu4mmfbjjjfe7abxo.jpg/w650
     * tags : []
     * trusted : true
     * user : {"user_id":18691263,"username":"旅游摄影精品美图","urlname":"s5gkw8njnjx","created_at":1461570722,"avatar":{"id":150789800,"farm":"farm1","bucket":"hbimg","key":"79f8bef0ab93a52fdfab888737ef2e1e3bd42cb715c1f-sGm5eR","type":"image/jpeg","width":"640","height":"640","frames":"1"},"extra":null}
     * board : {"board_id":38267508,"user_id":18691263,"title":"COS王者荣耀","description":"","category_id":"beauty","seq":48,"pin_count":678,"follow_count":133,"like_count":0,"created_at":1501241561,"updated_at":1574341086,"deleting":0,"is_private":0,"extra":{"cover":{"pin_id":"1250618366"}}}
     */

    private long pin_id;
    private long user_id;
    private long board_id;
    private long file_id;
    private FileBean file;
    private int media_type;
    private String source;
    private String link;
    private String raw_text;
    private long created_at;
    private int like_count;
    private int comment_count;
    private int repin_count;
    private int is_private;
    private String orig_source;
    private boolean trusted;
    private UserBean user;
    private BoardBean board;

    public long getPin_id(){ return pin_id;}

    public void setPin_id(long pin_id){ this.pin_id=pin_id;}

    public long getUser_id(){ return user_id;}

    public void setUser_id(long user_id){ this.user_id=user_id;}

    public long getBoard_id(){ return board_id;}

    public void setBoard_id(long board_id){ this.board_id=board_id;}

    public long getFile_id(){ return file_id;}

    public void setFile_id(long file_id){ this.file_id=file_id;}

    public FileBean getFile(){ return file;}

    public void setFile(FileBean file){ this.file=file;}

    public int getMedia_type(){ return media_type;}

    public void setMedia_type(int media_type){ this.media_type=media_type;}

    public String getSource(){ return source;}

    public void setSource(String source){ this.source=source;}

    public String getLink(){ return link;}

    public void setLink(String link){ this.link=link;}

    public String getRaw_text(){ return raw_text;}

    public void setRaw_text(String raw_text){ this.raw_text=raw_text;}

    public long getCreated_at(){ return created_at;}

    public void setCreated_at(long created_at){ this.created_at=created_at;}

    public int getLike_count(){ return like_count;}

    public void setLike_count(int like_count){ this.like_count=like_count;}

    public int getComment_count(){ return comment_count;}

    public void setComment_count(int comment_count){ this.comment_count=comment_count;}

    public int getRepin_count(){ return repin_count;}

    public void setRepin_count(int repin_count){ this.repin_count=repin_count;}

    public int getIs_private(){ return is_private;}

    public void setIs_private(int is_private){ this.is_private=is_private;}

    public String getOrig_source(){ return orig_source;}

    public void setOrig_source(String orig_source){ this.orig_source=orig_source;}

    public boolean isTrusted(){ return trusted;}

    public void setTrusted(boolean trusted){ this.trusted=trusted;}

    public UserBean getUser(){ return user;}

    public void setUser(UserBean user){ this.user=user;}

    public BoardBean getBoard(){ return board;}

    public void setBoard(BoardBean board){ this.board=board;}

}
