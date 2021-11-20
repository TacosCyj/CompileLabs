public class operator extends token{
    private String operator;
    public operator(char c, String t, int s){
        super(t, s);
        this.operator = String.valueOf(c);
    }
    public String getOperator(){return this.operator;}
}
