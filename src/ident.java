public class ident extends token{
    private int assigntimes;
    private String id;
    private int is_neg;
    private int create_when_op = 0;
    public ident(String id, String t, int s, int a, int n){
        super(t, s);
        this.id = id;
        this.assigntimes = a;
        this.is_neg = n;
    }
    public String getId(){return this.id;}

    public int getIs_neg(){return this.is_neg;}
    public int getCreate_when_op(){return this.create_when_op;}
}
