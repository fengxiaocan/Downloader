package com.down.huabian;


public class BoardBean {
    /**
     * board_id : 38267508
     * user_id : 18691263
     * title : COS王者荣耀
     * description :
     * category_id : beauty
     * seq : 48
     * pin_count : 678
     * follow_count : 133
     * like_count : 0
     * created_at : 1501241561
     * updated_at : 1574341086
     * deleting : 0
     * is_private : 0
     * extra : {"cover":{"pin_id":"1250618366"}}
     */

    protected int board_id;
    protected int user_id;
    protected String title;
    protected String description;
    protected String category_id;
    protected int pin_count;
    protected int follow_count;
    protected int like_count;
    protected long created_at;
    protected long updated_at;
    protected int is_private;

    public int getBoard_id(){ return board_id;}

    public void setBoard_id(int board_id){ this.board_id=board_id;}

    public int getUser_id(){ return user_id;}

    public void setUser_id(int user_id){ this.user_id=user_id;}

    public String getTitle(){ return title;}

    public void setTitle(String title){ this.title=title;}

    public String getDescription(){ return description;}

    public void setDescription(String description){ this.description=description;}

    public String getCategory_id(){ return category_id;}

    public void setCategory_id(String category_id){ this.category_id=category_id;}

    public int getPin_count(){ return pin_count;}

    public void setPin_count(int pin_count){ this.pin_count=pin_count;}

    public int getFollow_count(){ return follow_count;}

    public void setFollow_count(int follow_count){ this.follow_count=follow_count;}

    public int getLike_count(){ return like_count;}

    public void setLike_count(int like_count){ this.like_count=like_count;}

    public long getCreated_at(){ return created_at;}

    public void setCreated_at(long created_at){ this.created_at=created_at;}

    public long getUpdated_at(){ return updated_at;}

    public void setUpdated_at(long updated_at){ this.updated_at=updated_at;}

    public int getIs_private(){ return is_private;}

    public void setIs_private(int is_private){ this.is_private=is_private;}
}
