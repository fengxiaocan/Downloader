package com.down.huabian;

public class UserBean {
    /**
     * user_id : 18691263
     * username : 旅游摄影精品美图
     * urlname : s5gkw8njnjx
     * created_at : 1461570722
     * avatar : {"id":150789800,"farm":"farm1","bucket":"hbimg","key":"79f8bef0ab93a52fdfab888737ef2e1e3bd42cb715c1f-sGm5eR","type":"image/jpeg","width":"640","height":"640","frames":"1"}
     * extra : null
     */

    private int user_id;
    private String username;
    private String urlname;
    private long created_at;
    private AvatarBean avatar;

    public int getUser_id(){ return user_id;}

    public void setUser_id(int user_id){ this.user_id=user_id;}

    public String getUsername(){ return username;}

    public void setUsername(String username){ this.username=username;}

    public String getUrlname(){ return urlname;}

    public void setUrlname(String urlname){ this.urlname=urlname;}

    public long getCreated_at(){ return created_at;}

    public void setCreated_at(long created_at){ this.created_at=created_at;}

    public AvatarBean getAvatar(){ return avatar;}

    public void setAvatar(AvatarBean avatar){ this.avatar=avatar;}
}
