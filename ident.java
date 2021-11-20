public class ident extends token{
    private String id;
    private boolean is_const = false;
    private int assigntimes;
    private int is_neg;
    private int create_when_op = 0;
    public ident(String id, String t, int s, int a, int n){
        super(t, s);
        this.id = id;
        this.assigntimes = a;
        this.is_neg = n;
    }
    public String getId(){return this.id;}
    public void setIs_const(){this.is_const = true;}
    public void setAssigntimes(int t){this.assigntimes = t;}
    public int getAssigntimes(){return this.assigntimes;}
    public boolean getIs_const(){return this.is_const;}
    public int getIs_neg(){return this.is_neg;}
    public void setCreate_when_op(int v){this.create_when_op = v;}
    public int getCreate_when_op(){return this.create_when_op;}
}
