import java.util.LinkedList;

public class function extends token{
    String funcName;
    String typeOfRetValue;
    LinkedList<token> funVarList = new LinkedList<>();
    public function(String fn, String torv, String t, int s){
        super(t, s);
        this.funcName = fn;
        this.typeOfRetValue = torv;
    }
    public String getFuncName(){return this.funcName;}
    public String getTypeOfRetValue(){return this.typeOfRetValue;}
    public void setFuncVar(token t){this.funVarList.offer(t);}
    public LinkedList getFuncVar(){return this.funVarList;}
}
