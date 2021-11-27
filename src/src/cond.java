import java.util.LinkedList;

public class cond extends token{
    private String condid;
    //if 或 else if中的表达式内容
    public cond(String c, String type, int symbol){
        super(type, symbol);
        this.condid = c;
    }
    public void setCondid(String c){this.condid = c;}
    public String getCondid(){return this.condid;}
}
