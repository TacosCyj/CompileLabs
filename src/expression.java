import java.util.*;

public class expression {
    private static expression exp;
    private LinkedList<token> expList = new LinkedList<>();
    private HashMap<String, Integer> varlist;
    private HashMap<String, register> reglist;
    private HashMap<String, Integer> timelist;
    private HashMap<String, Boolean> constlist;
    private int reg_seq;
    private int id_name = 0;
    private StringBuilder ans;
    private Stack<token> numstack = new Stack<>();
    private Stack<operator> opstack = new Stack<>();
    private int[][] PriorityMatrix = {
        {1, 1, -1, -1, -1, -1, 1, 1, 1},
        {1, 1, -1, -1, -1, -1, 1, 1, 1},
        {1, 1, 1, 1, 1, -1, 1, 1, 1},
        {1, 1, 1, 1, 1, -1, 1, 1, 1},
        {1, 1, 1, 1, 1, -1, 1, 1, 1},
        {-1, -1, -1, -1, -1, -1, 0, -2, -2},
        {1, 1, 1, 1, 1, -2, 1, 1, 1},
        {-1, -1, -1, -1, -1, -1, -2, -2, -2},
        {-1, -1, -1, -1, -1, -1, -2, -2, -2},
    };
    private expression(){}
    static {
        exp = new expression();
    }
    public static expression getInstance(){return exp;}
    public void getExp(LinkedList<token> q){
        this.expList = q;
    }
    public void getMap(HashMap<String, Integer> v,  HashMap<String, register> r, HashMap<String, Integer> t, HashMap<String, Boolean> c, int rs, StringBuilder ans){
        this.varlist = v;
        this.reglist = r;
        this.timelist = t;
        this.constlist = c;
        this.reg_seq = rs;
        this.ans = ans;
    }
    public int isAddOrSub(token t){
        if(t instanceof operator){
            if(Objects.equals(((operator) t).getOperator(), "+")) return 1;
            else if(Objects.equals(((operator) t).getOperator(), "-")) return 2;
        }
        return 3;
    }
    public boolean initExp(char c){
        boolean flag = true;
        int numl = 0, numr = 0;
        int nump = 0, numm = 0;
        int num_op = 0;
        int i = 0, j, k;
        operator temp_op;
        operator begin = new operator(c, "Op", 26);
        token tt, t_op, t_opp;
        //检查括号数目匹配
        this.opstack.push(begin);
        for(token t : expList){
            if(t instanceof operator && Objects.equals(((operator) t).getOperator(), "(")) numl++;
            else if(t instanceof operator && Objects.equals(((operator) t).getOperator(), ")")) numr++;
        }
        if(numl != numr) flag = false;
        //检查表达式是否以数字或变量结尾
        if(!(this.expList.get(this.expList.toArray().length - 2) instanceof number) && !(this.expList.get(this.expList.toArray().length - 2) instanceof ident)){
            if(this.expList.get(this.expList.toArray().length - 2) instanceof operator && ((operator) this.expList.get(this.expList.toArray().length - 2)).getOperator().charAt(0) != ')'){
                return false;
            }
        }
        //检查是否有不会出现在表达式中的符号
        for(token t : expList){
            if(t instanceof operator && (Objects.equals(((operator) t).getOperator(), "=") || Objects.equals(((operator) t).getOperator(), "{") || Objects.equals(((operator) t).getOperator(), "}"))){
                flag = false;
                break;
            }
        }
        //计算符号排布是否合法
        //** */ *%...
        for(i = 0; i < this.expList.toArray().length;){
            t_op = this.expList.get(i);
            num_op = 0;
            if(t_op instanceof operator && (((operator) t_op).getOperator().charAt(0) == '*' || ((operator) t_op).getOperator().charAt(0) == '/' || ((operator) t_op).getOperator().charAt(0) == '%')){
                j = i;
                while(t_op instanceof operator && (((operator) t_op).getOperator().charAt(0) == '*' || ((operator) t_op).getOperator().charAt(0) == '/' || ((operator) t_op).getOperator().charAt(0) == '%'
                        || ((operator) t_op).getOperator().charAt(0) == '+'|| ((operator) t_op).getOperator().charAt(0) == '-'|| ((operator) t_op).getOperator().charAt(0) == ')')){
                    num_op++;
                    t_op = this.expList.get(++j);
                }
                if(num_op != 1){
                    flag = false;
                    break;
                }
                else i = j;
            }
            else i++;
        }
        //数字直接与(相连
        for(i = 0; i < this.expList.toArray().length;i++){
            t_op = this.expList.get(i);
            if(t_op instanceof number || t_op instanceof ident){
                j = i + 1;
                t_opp = this.expList.get(j);
                if(t_opp instanceof operator && ((operator) t_opp).getOperator().charAt(0) == '('){
                    flag = false;
                    break;
                }
            }
        }
        return flag;
    }
    public int getPriority(char a, char b){
        char c_x = a;
        char c_y = b;
        int x = 0, y = 0;
        switch(c_x){
            case '+': x = 0; break;
            case '-': x = 1; break;
            case '*': x = 2; break;
            case '/': x = 3; break;
            case '%': x = 4; break;
            case '(': x = 5; break;
            case ')': x = 6; break;
            case ';': x = 7; break;
            case ',': x = 8; break;
            default: break;
        }
        switch(c_y){
            case '+': y = 0; break;
            case '-': y = 1; break;
            case '*': y = 2; break;
            case '/': y = 3; break;
            case '%': y = 4; break;
            case '(': y = 5; break;
            case ')': y = 6; break;
            case ';': y = 7; break;
            case ',': y = 8; break;
            default: break;
        }
        return this.PriorityMatrix[y][x];
    }
    public int getSign(int a, int b, char c){
        int result = 0;
        switch (c){
            case '+': result = a + b; break;
            case '-': result = a - b; break;
            case '*': result = a * b; break;
            case '/': result = a / b; break;
            case '%': result = a % b; break;
        }
        if(result >= 0) return 1;
        else return -1;
    }
    //0为数字
    //1为ident
    public void setAns(int a, int b, int v1, int v2, String id1, String id2){
        ident n;
        register r;
        char op = opstack.peek().getOperator().charAt(0);
        if(v1 == 0 && v2 == 0){
            this.reg_seq++;
            id_name++;
            r = new register();
            r.setSeq(this.reg_seq);
            r.setOwnerofreg(String.valueOf(id_name));
            switch(op){
                case '+': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b + a); r.setValueOfReg(b + a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "add " + "i32 " + b + ", " + a + "\n");break;
                case '-': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b - a); r.setValueOfReg(b - a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "sub " + "i32 " + b + ", " + a + "\n");break;
                case '*': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b * a); r.setValueOfReg(b * a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "mul " + "i32 " + b + ", " + a + "\n");break;
                case '/': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b / a); r.setValueOfReg(b * a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "sdiv " + "i32 " + b + ", " + a + "\n");break;
                case '%': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b * a); r.setValueOfReg(b % a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "srem " + "i32 " + b + ", " + a + "\n");break;
                default: break;
            }
        }
        else if(v1 == 1 && v2 == 0){
            int old = ++this.reg_seq;
            this.ans.append("    %" + this.reg_seq +  " = " + "load i32, i32* %" + this.reglist.get(id1).getSeq() + "\n");
            this.reg_seq++;
            id_name++;
            r = new register();
            r.setSeq(this.reg_seq);
            r.setOwnerofreg(String.valueOf(id_name));
            switch(op){
                case '+': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b + a); r.setValueOfReg(b + a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "add " + "i32 " + b + ", %" + old + "\n");break;
                case '-': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1);  numstack.push(n); varlist.put(String.valueOf(id_name), b - a); r.setValueOfReg(b - a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "sub " + "i32 " + b + ", %" + old + "\n");break;
                case '*': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b * a); r.setValueOfReg(b * a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "mul " + "i32 " + b + ", %" + old + "\n");break;
                case '/': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b / a); r.setValueOfReg(b * a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "sdiv " + "i32 " + b + ", %" + old + "\n");break;
                case '%': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b * a); r.setValueOfReg(b % a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "srem " + "i32 " + b + ", %" + old + "\n");break;
                default: break;
            }
        }
        else if(v1 == 0 && v2 == 1){
            int old = ++this.reg_seq;
            this.ans.append("    %" + this.reg_seq +  " = " + "load i32, i32* %" + this.reglist.get(id2).getSeq() + "\n");
            this.reg_seq++;
            r = new register();
            r.setSeq(this.reg_seq);
            r.setOwnerofreg(String.valueOf(id_name));
            switch(op){
                case '+': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b + a); r.setValueOfReg(b + a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "add " + "i32 " + "%"  + old + ", " + a + "\n"); break;
                case '-': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b - a); r.setValueOfReg(b - a); this.reglist.put(String.valueOf(id_name), r);  this.ans.append("    %" + this.reg_seq + " = " + "sub " + "i32 " + "%"  + old + ", " + a + "\n"); break;
                case '*': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b * a); r.setValueOfReg(b * a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "mul " + "i32 " + "%"  + old + ", " + a + "\n"); break;
                case '/': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b / a); r.setValueOfReg(b * a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "sdiv " + "i32 " + "%"  + old + ", " + a + "\n"); break;
                case '%': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b * a); r.setValueOfReg(b % a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "srem " + "i32 " + "%"  + old + ", " + a + "\n"); break;
                default: break;
            }
        }
        else{
            int old1 = ++this.reg_seq;
            this.ans.append("    %" + this.reg_seq +  " = " + "load i32, i32* %" + this.reglist.get(id1).getSeq() + "\n");
            int old2 = ++this.reg_seq;
            this.ans.append("    %" + this.reg_seq +  " = " + "load i32, i32* %" + this.reglist.get(id2).getSeq() + "\n");
            this.reg_seq++;
            r = new register();
            r.setSeq(this.reg_seq);
            r.setOwnerofreg(String.valueOf(id_name));
            switch(op){
                case '+': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b + a); r.setValueOfReg(b + a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "add " + "i32 " + "%"  + old2 + ", " + "%"  + old1 + "\n");  break;
                case '-': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b - a); r.setValueOfReg(b - a); this.reglist.put(String.valueOf(id_name), r);  this.ans.append("    %" + this.reg_seq + " = " + "sub " + "i32 " + "%"  + old2 + ", " + "%"  + old1 + "\n");   break;
                case '*': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b * a); r.setValueOfReg(b * a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "mul " + "i32 " + "%"  + old2 + ", " + "%"  + old1 + "\n");  break;
                case '/': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b / a); r.setValueOfReg(b * a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "sdiv " + "i32 " + "%"  + old2 + ", " + "%"  + old1 + "\n");  break;
                case '%': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b * a); r.setValueOfReg(b % a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "srem " + "i32 " + "%"  + old2 + ", " + "%"  + old1 + "\n");  break;
                default: break;
            }
        }
    }
    public void setAns2(int a, int b, int v1, int v2, String id1, String id2){
        ident n;
        register r;
        char op = opstack.peek().getOperator().charAt(0);
        if(v1 == 1 && v2 == 0){
            this.reg_seq++;
            id_name++;
            r = new register();
            r.setSeq(this.reg_seq);
            r.setOwnerofreg(String.valueOf(id_name));
            switch(op){
                case '+': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b + a); r.setValueOfReg(b + a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "add " + "i32 " + b + ", " + "%" + this.reglist.get(id1).getSeq() + "\n");break;
                case '-': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b - a); r.setValueOfReg(b - a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "sub " + "i32 " + b + ", " + "%" + this.reglist.get(id1).getSeq() + "\n");break;
                case '*': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b * a); r.setValueOfReg(b * a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "mul " + "i32 " + b + ", " + "%" + this.reglist.get(id1).getSeq() + "\n");break;
                case '/': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b / a); r.setValueOfReg(b * a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "sdiv " + "i32 " + b + ", " + "%" + this.reglist.get(id1).getSeq() + "\n");break;
                case '%': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b * a); r.setValueOfReg(b % a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "srem " + "i32 " + b + ", " + "%" + this.reglist.get(id1).getSeq() + "\n");break;
                default: break;
            }
        }
        else if(v1 == 0 && v2 == 1){
            this.reg_seq++;
            id_name++;
            r = new register();
            r.setSeq(this.reg_seq);
            r.setOwnerofreg(String.valueOf(id_name));
            switch(op){
                case '+': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b + a); r.setValueOfReg(b + a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "add " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", " + a + "\n");break;
                case '-': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b - a); r.setValueOfReg(b - a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "sub " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", " + a + "\n");break;
                case '*': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b * a); r.setValueOfReg(b * a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "mul " + "i32 " + "%" +  this.reglist.get(id2).getSeq() + ", " + a + "\n");break;
                case '/': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b / a); r.setValueOfReg(b * a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "sdiv " + "i32 " + "%" +  this.reglist.get(id2).getSeq()+ ", " + a + "\n");break;
                case '%': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b * a); r.setValueOfReg(b % a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "srem " + "i32 " + "%" +  this.reglist.get(id2).getSeq() + ", " + a + "\n");break;
                default: break;
            }
        }
    }
    public void setAns3(int a, int b, int v1, int v2, String id1, String id2, int sign1, int sign2){
        ident n;
        register r;
        char op = opstack.peek().getOperator().charAt(0);
        if(sign1 == 0 && sign2 == 1){
            int old = ++this.reg_seq;
            this.ans.append("    %" + this.reg_seq +  " = " + "load i32, i32* %" + this.reglist.get(id1).getSeq() + "\n");
            this.reg_seq++;
            id_name++;
            r = new register();
            r.setSeq(this.reg_seq);
            r.setOwnerofreg(String.valueOf(id_name));
            switch(op){
                case '+': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b + a); r.setValueOfReg(b + a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "add " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", %" + old + "\n");break;
                case '-': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1);  numstack.push(n); varlist.put(String.valueOf(id_name), b - a); r.setValueOfReg(b - a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "sub " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", %" + old + "\n");break;
                case '*': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b * a); r.setValueOfReg(b * a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "mul " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", %" + old + "\n");break;
                case '/': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b / a); r.setValueOfReg(b * a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "sdiv " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", %" + old + "\n");break;
                case '%': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b * a); r.setValueOfReg(b % a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "srem " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", %" + old + "\n");break;
                default: break;
            }
        }
        else if(sign1 == 1 && sign2 == 0){
            int old = ++this.reg_seq;
            this.ans.append("    %" + this.reg_seq +  " = " + "load i32, i32* %" + this.reglist.get(id2).getSeq() + "\n");
            this.reg_seq++;
            id_name++;
            r = new register();
            r.setSeq(this.reg_seq);
            r.setOwnerofreg(String.valueOf(id_name));
            switch(op){
                case '+': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b + a); r.setValueOfReg(b + a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "add " + "i32 " + "%" + old + ", %" + this.reglist.get(id1).getSeq() + "\n");break;
                case '-': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1);  numstack.push(n); varlist.put(String.valueOf(id_name), b - a); r.setValueOfReg(b - a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "sub " + "i32 " + "%" + old + ", %" + this.reglist.get(id1).getSeq() + "\n");break;
                case '*': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b * a); r.setValueOfReg(b * a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "mul " + "i32 " + "%" + old + ", %" + this.reglist.get(id1).getSeq() + "\n");break;
                case '/': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b / a); r.setValueOfReg(b * a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "sdiv " + "i32 " + "%" + old + ", %" + this.reglist.get(id1).getSeq() + "\n");break;
                case '%': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b * a); r.setValueOfReg(b % a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "srem " + "i32 " + "%" + old + ", %" + this.reglist.get(id1).getSeq() + "\n");break;
                default: break;
            }
        }
        else{
            this.reg_seq++;
            id_name++;
            r = new register();
            r.setSeq(this.reg_seq);
            r.setOwnerofreg(String.valueOf(id_name));
            switch(op){
                case '+': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b + a); r.setValueOfReg(b + a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "add " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", %" + this.reglist.get(id1).getSeq() + "\n");break;
                case '-': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1);  numstack.push(n); varlist.put(String.valueOf(id_name), b - a); r.setValueOfReg(b - a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "sub " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", %" + this.reglist.get(id1).getSeq() + "\n");break;
                case '*': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b * a); r.setValueOfReg(b * a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "mul " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", %" + this.reglist.get(id1).getSeq() + "\n");break;
                case '/': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b / a); r.setValueOfReg(b * a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "sdiv " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", %" + this.reglist.get(id1).getSeq() + "\n");break;
                case '%': n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); n.setCreate_when_op(1); numstack.push(n); varlist.put(String.valueOf(id_name), b * a); r.setValueOfReg(b % a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "srem " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", %" + this.reglist.get(id1).getSeq() + "\n");break;
                default: break;
            }
        }
    }
    public void calculate(){
        token temp_t1, temp_t2;
        int a = 0, b = 0;
        int sign, sign2;
        number n;
        if(this.numstack.size() == 1){
            temp_t1 = numstack.pop();
            if(temp_t1 instanceof number){
                a = ((number) temp_t1).getValue();
            }
            else if(temp_t1 instanceof ident){
                a = varlist.get(((ident) temp_t1).getId());
            }
            char op = opstack.peek().getOperator().charAt(0);
            switch(op){
                case '+': n = new number(b + a, "Number", 10); numstack.push(n); break;
                case '-': n = new number(b - a, "Number", 10); numstack.push(n); break;
                case '*': n = new number(b * a, "Number", 10); numstack.push(n);break;
                case '/': n = new number(b / a, "Number", 10); numstack.push(n);break;
                case '%': n = new number(b % a, "Number", 10); numstack.push(n); break;
                default: break;
            }
            opstack.pop();
        }
        else{
            temp_t1 = numstack.pop();
            temp_t2 = numstack.pop();
            if(temp_t1 instanceof number && temp_t2 instanceof number){
                a = ((number) temp_t1).getValue();
                b = ((number) temp_t2).getValue();
                setAns(a, b, 0, 0, null, null);
            }
            else if(temp_t1 instanceof number && temp_t2 instanceof ident){
                a = ((number) temp_t1).getValue();
                b = varlist.get(((ident) temp_t2).getId()) * ((ident) temp_t2).getIs_neg();
                sign = ((ident) temp_t2).getCreate_when_op();
                if(sign == 0){
                    setAns(a, b, 0, 1, null, ((ident) temp_t2).getId());
                }
                else{
                    setAns2(a, b, 0, 1, null, ((ident) temp_t2).getId());
                }
            }
            else if(temp_t1 instanceof ident && temp_t2 instanceof number){
                a = varlist.get(((ident) temp_t1).getId()) * ((ident) temp_t1).getIs_neg();
                b = ((number) temp_t2).getValue();
                sign = ((ident) temp_t1).getCreate_when_op();
                if(sign == 0){
                    setAns(a, b, 1, 0, ((ident) temp_t1).getId(), null);
                }
                else{
                    setAns2(a, b, 1, 0, ((ident) temp_t1).getId(), null);
                }
            }
            else if(temp_t1 instanceof ident && temp_t2 instanceof ident){
                a = varlist.get(((ident) temp_t1).getId()) * ((ident) temp_t1).getIs_neg();
                b = varlist.get(((ident) temp_t2).getId()) * ((ident) temp_t2).getIs_neg();
                sign = ((ident) temp_t1).getCreate_when_op();
                sign2 = ((ident) temp_t2).getAssigntimes();
                if(sign == 0 && sign2 == 0){
                    setAns(a, b, 1, 1, ((ident) temp_t1).getId(),((ident) temp_t2).getId());
                }
                else{
                    setAns3(a, b, 1, 1, ((ident) temp_t1).getId(), ((ident) temp_t2).getId(), sign, sign2);
                }
            }
            opstack.pop();
        }
    }
    //public boolean calExp(){}
    public boolean dealExp(char c, LinkedList<token> exp){
        boolean flag = true;
        int i, j, p;
        flag = initExp(c);
        if(!flag) return flag;
        else{
            for(i = 0; i < exp.toArray().length; i++){
                token temp_t = exp.get(i);
                if(temp_t instanceof number){
                    numstack.push((number) temp_t);
                }
                else if(temp_t instanceof ident){
                    if(!this.varlist.containsKey(((ident) temp_t).getId()) || this.timelist.get(((ident) temp_t).getId()) == 0){
                        flag = false;
                        break;
                    }
                    else if(!constlist.get(((ident) temp_t).getId())){
                        numstack.push(temp_t);
                    }
                    //变量是const
                    else{
                        number n = new number(varlist.get(((ident) temp_t).getId()), "Number", 10);
                        numstack.push(n);
                    }
                }
                else if(temp_t instanceof operator){
                    j = getPriority(((operator) temp_t).getOperator().charAt(0), opstack.peek().getOperator().charAt(0));
                    if(j == -1) opstack.push((operator) temp_t);
                    else if(j == 1){
                        while((p = getPriority(((operator) temp_t).getOperator().charAt(0), opstack.peek().getOperator().charAt(0))) == 1){
                           calculate();
                        }
                        if(p == -2 && opstack.peek().getOperator().charAt(0) != c){
                            flag = false;
                            break;
                        }
                        if(opstack.peek().getOperator().charAt(0) == '('){
                            opstack.pop();
                        }
                        if(((operator) temp_t).getOperator().charAt(0) != ')' && ((operator) temp_t).getOperator().charAt(0) != c){
                            opstack.push((operator) temp_t);
                        }
                    }
                    else if(j == 0){
                        opstack.pop();
                    }
                }
            }
            while(getPriority(c, opstack.peek().getOperator().charAt(0)) == 1){
                calculate();
            }
            if(this.numstack.size() == 1 && this.opstack.size() == 1 && this.opstack.peek().getOperator().charAt(0) == c) flag = true;
            else flag = false;
        }
        return flag;
    }
    public int passAns(){
        token temp_t = this.numstack.peek();
        if(temp_t instanceof number){
            return ((number) temp_t).getValue();
        }
        else if(temp_t instanceof ident){
            return varlist.get(((ident) temp_t).getId());
        }
        return -1;
    }
    public token forJudge(){
        return this.numstack.peek();
    }
    public int passRegSeq(){
        return this.reg_seq;
    }
    public StringBuilder getAns(){
        return this.ans;
    }
    public void clearExp(){
        this.numstack.clear();
        this.opstack.clear();
        this.expList.clear();
    }
}
