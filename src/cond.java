import java.util.LinkedList;

public class cond extends token{
    private String condid;
    private int tabnums;
    //if 或 else if中的表达式内容
    public cond(String c, String type, int symbol, int n){
        super(type, symbol);
        this.condid = c;
        this.tabnums = n;
    }
    public void setCondid(String c){this.condid = c;}
    public String getCondid(){return this.condid;}
    public int getTabnums(){return this.tabnums;}
}
