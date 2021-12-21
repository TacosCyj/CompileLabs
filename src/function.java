import java.util.LinkedList;

public class function extends token{
    String funcName;
    String typeOfRetValue;
    int params_num = 0;
    boolean isSelfDecl = false;
    LinkedList<String> funVarList = new LinkedList<>();
    public function(String fn, String torv, String t, int s){
        super(t, s);
        this.funcName = fn;
        this.typeOfRetValue = torv;
    }
    public String getFuncName(){return this.funcName;}
    public String getTypeOfRetValue(){return this.typeOfRetValue;}
    public void setFuncVar(String t){this.funVarList.offer(t);}
    public LinkedList<String> getFuncVar(){return this.funVarList;}
    public void setSelfDecl(boolean b){this.isSelfDecl = b;}
    public boolean getSelfDecl(){return this.isSelfDecl;}
    public void setParams_num(int pn){this.params_num = pn;}
    public int getParams_num(){return this.params_num;}
}
