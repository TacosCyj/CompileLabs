public class operator extends token{
    private String operator;
    public operator(String c, String t, int s){
        super(t, s);
        this.operator = c;
    }
    public String getOperator(){return this.operator;}
    public void setOperator(String op){this.operator = op;}
}
