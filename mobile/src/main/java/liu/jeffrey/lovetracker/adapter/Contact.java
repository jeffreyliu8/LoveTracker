package liu.jeffrey.lovetracker.adapter;

public class Contact {
    private int _id;
    private String name;
    private String rid;
    private byte[] picture;

    public Contact(int id,String name, String rid, byte[] picture) {
        this._id = id;
        this.name = name;
        this.rid = rid;
        this.picture = picture;
    }

    public int getID() {
        return _id;
    }

    public String getName() {
        return name;
    }
    public String getRid() {
        return rid;
    }
    public byte[] getPicture() {
        return picture;
    }
}
