import java.util.*;

public class expression {
    private static expression exp;
    private LinkedList<token> expList = new LinkedList<>();

    private HashMap<String, Integer> varlist;
    private HashMap<String, register> reglist;

    private int final_layer = 0;

    private int reg_seq;
    private int id_name = 0;
    private StringBuilder ans;
    private Stack<token> numstack = new Stack<>();
    private Stack<operator> opstack = new Stack<>();
    private int[][] PriorityMatrix = {
            {1, 1, -1, -1, -1, 1 ,1 ,1, 1, 1, 1, 1, 1, -1, 1, 1, 1, 1},
            {1, 1, -1, -1, -1, 1 ,1 ,1, 1, 1, 1, 1, 1, -1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, 1, 1, 1, 1},
            {-1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, -1, 1, 1, 1, 1},
            {-1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, -1, 1, 1, 1, 1},
            {-1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, -1, 1, 1, 1, 1},
            {-1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, -1, 1, 1, 1, 1},
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, -1, 1, 1, 1, 1},
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, -1, 1, 1, 1, 1},
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 1, -1, 1, 1, 1, 1},
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, 1, 1, 1, 1},
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, -2, -2, -2},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -2, 1, 1, 1, 1},
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -2, -2, -2, -2},
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -2, -2, -2, -2},
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -2, -2, -2, -2},
    };
    private expression(){}
    static {
        exp = new expression();
    }
    public static expression getInstance(){return exp;}
    public void getExp(LinkedList<token> q){
        this.expList = q;
    }
    public void getMap(HashMap<String, Integer> v,  HashMap<String, register> r, int rs, StringBuilder ans){
        this.varlist = v;
        this.reglist = r;
        this.reg_seq = rs;
        this.ans = ans;
    }
    public boolean initExp(char c){
        boolean flag = true;
        int numl = 0, numr = 0;
        int nump = 0, numm = 0;
        int num_op = 0;
        int i = 0, j, k;
        operator temp_op;
        operator begin = new operator(String.valueOf(c), "Op", 26);
        token tt, t_op, t_opp;
        //检查括号数目匹配
        this.opstack.push(begin);
        //token temp = expList.removeLast();
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
            if(t instanceof operator && (Objects.equals(((operator) t).getOperator(), "=") ||  Objects.equals(((operator) t).getOperator(), "}"))){
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
        //expList.addLast(temp);
        return flag;
    }
    public int getPriority(String a, String b){
        String c_x = a;
        String c_y = b;
        int x = 0, y = 0;
        switch(c_x){
            case "+": x = 0; break;
            case "-": x = 1; break;
            case "*": x = 2; break;
            case "/": x = 3; break;
            case "%": x = 4; break;
            case "<": x = 5; break;
            case ">": x = 6; break;
            case "<=": x = 7; break;
            case ">=": x = 8; break;
            case "==": x = 9; break;
            case "!=": x = 10; break;
            case "&&": x = 11; break;
            case "||": x = 12; break;
            case "(": x = 13; break;
            case ")": x = 14; break;
            case ";": x = 15; break;
            case ",": x = 16; break;
            case "{": x = 17; break;
            default: break;
        }
        switch(c_y){
            case "+": y = 0; break;
            case "-": y = 1; break;
            case "*": y = 2; break;
            case "/": y = 3; break;
            case "%": y = 4; break;
            case "<": y = 5; break;
            case ">": y = 6; break;
            case "<=": y = 7; break;
            case ">=": y = 8; break;
            case "==": y = 9; break;
            case "!=": y = 10; break;
            case "&&": y = 11; break;
            case "||": y = 12; break;
            case "(": y = 13; break;
            case ")": y = 14; break;
            case ";": y = 15; break;
            case ",": y = 16; break;
            case "{": y = 17; break;
            default: break;
        }
        return this.PriorityMatrix[y][x];
    }
    public int getRegTail(ident id){
        int i;
        for(i = final_layer; i >= 0; i--){
            if(reglist.containsKey(id.getId() + i)){
                return i;
            }
        }
        return -1;
    }
    public Boolean getIdentlegal(ident id){
        int tail;
        tail = getRegTail(id);
        if(reglist.containsKey(id.getId() + (tail == -1 ? "" : tail)) && this.reglist.get(id.getId() + (tail == -1 ? "" : tail)).getHasValue()){
            return true;
        }
        return false;
    }
    //0为数字
    //1为ident
    public void setAns(int a, int b, int v1, int v2, String id1, String id2){
        ident n;
        int r1, r2;
        register r;
        String op = opstack.peek().getOperator();
        if(v1 == 0 && v2 == 0){
            this.reg_seq++;
            id_name++;
            r = new register();
            r.setSeq(this.reg_seq);
            switch (op) {
                case "+" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b + a);
                    r.setHasValue();
                    r.setValueOfReg(b + a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "add " + "i32 " + b + ", " + a + "\n");
                }
                case "-" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b - a);
                    r.setHasValue();
                    r.setValueOfReg(b - a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "sub " + "i32 " + b + ", " + a + "\n");
                }
                case "*" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b * a);
                    r.setHasValue();
                    r.setValueOfReg(b * a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "mul " + "i32 " + b + ", " + a + "\n");
                }
                case "/" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b / a);
                    r.setHasValue();
                    r.setValueOfReg(b * a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "sdiv " + "i32 " + b + ", " + a + "\n");
                }
                case "%" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b % a);
                    r.setHasValue();
                    r.setValueOfReg(b % a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "srem " + "i32 " + b + ", " + a + "\n");
                }
                case ">" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b > a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b > a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp sgt " + "i32 " + b + ", " + a + "\n");
                }
                case "<" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b < a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b < a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp slt " + "i32 " + b + ", " + a + "\n");
                }
                case ">=" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b >= a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b >= a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp sge " + "i32 " + b + ", " + a + "\n");
                }
                case "<=" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b <= a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b <= a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp sle " + "i32 " + b + ", " + a + "\n");
                }
                case "==" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b == a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b == a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp eq " + "i32 " + b + ", " + a + "\n");
                }
                case "!=" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b != a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b != a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp ne " + "i32 " + b + ", " + a + "\n");
                }
                case "&&" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b != 0 && a != 0 ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b != 0 && a != 0 ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp ne " + "i32 " + b + ", " + 0 + "\n");
                    r1 = this.reg_seq;
                    this.ans.append("    %" + (++this.reg_seq) + " = " + "icmp ne " + "i32 " + a + ", " + 0 + "\n");
                    r2 = this.reg_seq;
                    this.ans.append("    %" + (++this.reg_seq) + " = " + "and i1 " + "%" + r1 + ", %" + r2 + "\n");
                    this.reglist.get(String.valueOf(id_name)).setSeq(this.reg_seq);
                }
                case "||" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b != 0 || a != 0 ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b != 0 || a != 0 ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp ne " + "i32 " + b + ", " + 0 + "\n");
                    r1 = this.reg_seq;
                    this.ans.append("    %" + (++this.reg_seq) + " = " + "icmp ne " + "i32 " + a + ", " + 0 + "\n");
                    r2 = this.reg_seq;
                    this.ans.append("    %" + (++this.reg_seq) + " = " + "or i1 " + "%" + r1 + ", %" + r2 + "\n");
                    this.reglist.get(String.valueOf(id_name)).setSeq(this.reg_seq);
                }
                default -> {
                }
            }
        }
        else if(v1 == 1 && v2 == 0){
            int old = ++this.reg_seq;
            if(!this.reglist.get(id1).getIsGlobal()) {
                this.ans.append("    %" + this.reg_seq + " = " + "load i32, i32* %" + this.reglist.get(id1).getSeq() + "\n");
            }
            else{
                this.ans.append("    %" + this.reg_seq + " = " + "load i32, i32* " + this.reglist.get(id1).getGlobalname() + "\n");
            }
            this.reg_seq++;
            id_name++;
            r = new register();
            r.setSeq(this.reg_seq);
            switch (op) {
                case "+" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b + a);
                    r.setHasValue();
                    r.setValueOfReg(b + a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "add " + "i32 " + b + ", %" + old + "\n");
                }
                case "-" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b - a);
                    r.setHasValue();
                    r.setValueOfReg(b - a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "sub " + "i32 " + b + ", %" + old + "\n");
                }
                case "*" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b * a);
                    r.setHasValue();
                    r.setValueOfReg(b * a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "mul " + "i32 " + b + ", %" + old + "\n");
                }
                case "/" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b / a);
                    r.setHasValue();
                    r.setValueOfReg(b * a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "sdiv " + "i32 " + b + ", %" + old + "\n");
                }
                case "%" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b * a);
                    r.setHasValue();
                    r.setValueOfReg(b % a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "srem " + "i32 " + b + ", %" + old + "\n");
                }
                case ">" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b > a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b > a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp sgt " + "i32 " + b + ", %" + old + "\n");
                }
                case "<" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b < a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b < a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp slt " + "i32 " + b + ", %" + old + "\n");
                }
                case ">=" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b >= a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b >= a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp sge " + "i32 " + b + ", %" + old + "\n");
                }
                case "<=" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b <= a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b <= a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp sle " + "i32 " + b + ", %" + old + "\n");
                }
                case "==" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b == a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b == a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp eq " + "i32 " + b + ", %" + old + "\n");
                }
                case "!=" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b != a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b != a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp ne " + "i32 " + b + ", %" + old + "\n");
                }
                case "&&" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b != 0 && a != 0 ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b != 0 && a != 0 ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp ne " + "i32 " + b + ", " + 0 + "\n");
                    r1 = this.reg_seq;
                    this.ans.append("    %" + (++this.reg_seq) + " = " + "icmp ne " + "i32 %" + old + ", " + 0 + "\n");
                    r2 = this.reg_seq;
                    this.ans.append("    %" + (++this.reg_seq) + " = " + "and i1 " + "%" + r1 + ", %" + r2 + "\n");
                    this.reglist.get(String.valueOf(id_name)).setSeq(this.reg_seq);
                }
                case "||" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b != 0 || a != 0 ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b != 0 || a != 0 ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp ne " + "i32 " + b + ", " + 0 + "\n");
                    r1 = this.reg_seq;
                    this.ans.append("    %" + (++this.reg_seq) + " = " + "icmp ne " + "i32 %" + old + ", " + 0 + "\n");
                    r2 = this.reg_seq;
                    this.ans.append("    %" + (++this.reg_seq) + " = " + "or i1 " + "%" + r1 + ", %" + r2 + "\n");
                    this.reglist.get(String.valueOf(id_name)).setSeq(this.reg_seq);
                }
                default -> {
                }
            }
        }
        else if(v1 == 0 && v2 == 1){
            int old = ++this.reg_seq;
            if(!this.reglist.get(id2).getIsGlobal()) {
                this.ans.append("    %" + this.reg_seq + " = " + "load i32, i32* %" + this.reglist.get(id2).getSeq() + "\n");
            }
            else{
                this.ans.append("    %" + this.reg_seq + " = " + "load i32, i32* " + this.reglist.get(id2).getGlobalname() + "\n");
            }
            this.reg_seq++;
            id_name++;
            r = new register();
            r.setSeq(this.reg_seq);
            switch (op) {
                case "+" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b + a);
                    r.setHasValue();
                    r.setValueOfReg(b + a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "add " + "i32 " + "%" + old + ", " + a + "\n");
                }
                case "-" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b - a);
                    r.setHasValue();
                    r.setValueOfReg(b - a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "sub " + "i32 " + "%" + old + ", " + a + "\n");
                }
                case "*" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b * a);
                    r.setHasValue();
                    r.setValueOfReg(b * a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "mul " + "i32 " + "%" + old + ", " + a + "\n");
                }
                case "/" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b / a);
                    r.setHasValue();
                    r.setValueOfReg(b * a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "sdiv " + "i32 " + "%" + old + ", " + a + "\n");
                }
                case "%" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b * a);
                    r.setHasValue();
                    r.setValueOfReg(b % a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "srem " + "i32 " + "%" + old + ", " + a + "\n");
                }
                case ">" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b > a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b > a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp sgt " + "i32 " + "%" + old + ", " + a + "\n");
                }
                case "<" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b < a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b < a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp slt " + "i32 " + "%" + old + ", " + a + "\n");
                }
                case ">=" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b >= a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b >= a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp sge " + "i32 " + "%" + old + ", " + a + "\n");
                }
                case "<=" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b <= a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b <= a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp sle " + "i32 " + "%" + old + ", " + a + "\n");
                }
                case "==" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b == a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b == a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp eq " + "i32 " + "%" + old + ", " + a + "\n");
                }
                case "!=" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b != a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b != a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp ne " + "i32 " + "%" + old + ", " + a + "\n");
                }
                case "&&" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b != 0 && a != 0 ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b != 0 && a != 0 ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp ne " + "i32 %" + old + ", " + 0 + "\n");
                    r1 = this.reg_seq;
                    this.ans.append("    %" + (++this.reg_seq) + " = " + "icmp ne " + "i32 " + a + ", " + 0 + "\n");
                    r2 = this.reg_seq;
                    this.ans.append("    %" + (++this.reg_seq) + " = " + "and i1 " + "%" + r1 + ", %" + r2 + "\n");
                    this.reglist.get(String.valueOf(id_name)).setSeq(this.reg_seq);
                }
                case "||" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b != 0 || a != 0 ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b != 0 || a != 0 ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp ne " + "i32 %" + old + ", " + 0 + "\n");
                    r1 = this.reg_seq;
                    this.ans.append("    %" + (++this.reg_seq) + " = " + "icmp ne " + "i32 " + a + ", " + 0 + "\n");
                    r2 = this.reg_seq;
                    this.ans.append("    %" + (++this.reg_seq) + " = " + "or i1 " + "%" + r1 + ", %" + r2 + "\n");
                    this.reglist.get(String.valueOf(id_name)).setSeq(this.reg_seq);
                }
                default -> {
                }
            }
        }
        else{
            int old1 = ++this.reg_seq;
            if(!this.reglist.get(id1).getIsGlobal()) {
                this.ans.append("    %" + this.reg_seq + " = " + "load i32, i32* %" + this.reglist.get(id1).getSeq() + "\n");
            }
            else{
                this.ans.append("    %" + this.reg_seq + " = " + "load i32, i32* " + this.reglist.get(id1).getGlobalname() + "\n");
            }
            int old2 = ++this.reg_seq;
            if(!this.reglist.get(id2).getIsGlobal()) {
                this.ans.append("    %" + this.reg_seq + " = " + "load i32, i32* %" + this.reglist.get(id2).getSeq() + "\n");
            }
            else{
                this.ans.append("    %" + this.reg_seq + " = " + "load i32, i32* " + this.reglist.get(id2).getGlobalname() + "\n");
            }
            this.reg_seq++;
            id_name++;
            r = new register();
            r.setSeq(this.reg_seq);
            switch (op) {
                case "+" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b + a);
                    r.setHasValue();
                    r.setValueOfReg(b + a);
                    r.setCreatedWhenOp(1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "add " + "i32 " + "%" + old2 + ", " + "%" + old1 + "\n");
                }
                case "-" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b - a);
                    r.setHasValue();
                    r.setValueOfReg(b - a);
                    r.setCreatedWhenOp(1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "sub " + "i32 " + "%" + old2 + ", " + "%" + old1 + "\n");
                }
                case "*" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b * a);
                    r.setHasValue();
                    r.setValueOfReg(b * a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "mul " + "i32 " + "%" + old2 + ", " + "%" + old1 + "\n");
                }
                case "/" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b / a);
                    r.setHasValue();
                    r.setValueOfReg(b * a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "sdiv " + "i32 " + "%" + old2 + ", " + "%" + old1 + "\n");
                }
                case "%" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b * a);
                    r.setHasValue();
                    r.setValueOfReg(b % a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "srem " + "i32 " + "%" + old2 + ", " + "%" + old1 + "\n");
                }
                case ">" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b > a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b > a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp sgt " + "i32 " + "%" + old2 + ", " + "%" + old1 + "\n");
                }
                case "<" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b < a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b < a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp slt " + "i32 " + "%" + old2 + ", " + "%" + old1 + "\n");
                }
                case ">=" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b >= a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b >= a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp sge " + "i32 " + "%" + old2 + ", " + "%" + old1 + "\n");
                }
                case "<=" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b <= a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b <= a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp sle " + "i32 " + "%" + old2 + ", " + "%" + old1 + "\n");
                }
                case "==" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b == a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b == a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp eq " + "i32 " + "%" + old2 + ", " + "%" + old1 + "\n");
                }
                case "!=" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b != a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b != a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp ne " + "i32 " + "%" + old2 + ", " + "%" + old1 + "\n");
                }
                case "&&" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b != 0 && a != 0 ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b != 0 && a != 0 ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp ne " + "i32 %" + old2 + ", " + 0 + "\n");
                    r1 = this.reg_seq;
                    this.ans.append("    %" + (++this.reg_seq) + " = " + "icmp ne " + "i32 %" + old1 + ", " + 0 + "\n");
                    r2 = this.reg_seq;
                    this.ans.append("    %" + (++this.reg_seq) + " = " + "and i1 " + "%" + r1 + ", %" + r2 + "\n");
                    this.reglist.get(String.valueOf(id_name)).setSeq(this.reg_seq);
                }
                case "||" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b != 0 || a != 0 ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b != 0 || a != 0 ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp ne " + "i32 %" + old2 + ", " + 0 + "\n");
                    r1 = this.reg_seq;
                    this.ans.append("    %" + (++this.reg_seq) + " = " + "icmp ne " + "i32 %" + old1 + ", " + 0 + "\n");
                    r2 = this.reg_seq;
                    this.ans.append("    %" + (++this.reg_seq) + " = " + "or i1 " + "%" + r1 + ", %" + r2 + "\n");
                    this.reglist.get(String.valueOf(id_name)).setSeq(this.reg_seq);
                }
                default -> {
                }
            }
        }
    }
    public void setAns2(int a, int b, int v1, int v2, String id1, String id2){
        ident n;
        register r;
        int r1, r2;
        String op = opstack.peek().getOperator();
        if(v1 == 1 && v2 == 0){
            this.reg_seq++;
            id_name++;
            r = new register();
            r.setSeq(this.reg_seq);
            switch (op) {
                case "+" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b + a);
                    r.setHasValue();
                    r.setValueOfReg(b + a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "add " + "i32 " + b + ", " + "%" + this.reglist.get(id1).getSeq() + "\n");
                }
                case "-" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b - a);
                    r.setHasValue();
                    r.setValueOfReg(b - a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "sub " + "i32 " + b + ", " + "%" + this.reglist.get(id1).getSeq() + "\n");
                }
                case "*" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b * a);
                    r.setHasValue();
                    r.setValueOfReg(b * a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "mul " + "i32 " + b + ", " + "%" + this.reglist.get(id1).getSeq() + "\n");
                }
                case "/" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b / a);
                    r.setHasValue();
                    r.setValueOfReg(b * a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "sdiv " + "i32 " + b + ", " + "%" + this.reglist.get(id1).getSeq() + "\n");
                }
                case "%" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b * a);
                    r.setHasValue();
                    r.setValueOfReg(b % a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "srem " + "i32 " + b + ", " + "%" + this.reglist.get(id1).getSeq() + "\n");
                }
                case ">" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b > a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b > a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp sgt " + "i32 " + b + ", " + "%" + this.reglist.get(id1).getSeq() + "\n");
                }
                case "<" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b < a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b < a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp slt " + "i32 " + b + ", " + "%" + this.reglist.get(id1).getSeq() + "\n");
                }
                case ">=" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b >= a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b >= a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp sge " + "i32 " + b + ", " + "%" + this.reglist.get(id1).getSeq() + "\n");
                }
                case "<=" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b <= a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b <= a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp sle " + "i32 " + b + ", " + "%" + this.reglist.get(id1).getSeq() + "\n");
                }
                case "==" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b == a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b == a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp eq " + "i32 " + b + ", " + "%" + this.reglist.get(id1).getSeq() + "\n");
                }
                case "!=" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b != a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b != a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp ne " + "i32 " + b + ", " + "%" + this.reglist.get(id1).getSeq() + "\n");
                }
                case "&&" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b != 0 && a != 0 ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b != 0 && a != 0 ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp ne " + "i32 " + b + ", " + 0 + "\n");
                    r1 = this.reg_seq;
                    this.ans.append("    %" + (++this.reg_seq) + " = zext i1 %" + this.reglist.get(id1).getSeq() + " to i32\n");
                    r2 = this.reg_seq;
                    this.ans.append("    %" + (++this.reg_seq) + " = " + "and i1 " + "%" + r1 + ", %" + r2 + "\n");
                    this.reglist.get(String.valueOf(id_name)).setSeq(this.reg_seq);
                }
                case "||" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b != 0 || a != 0 ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b != 0 || a != 0 ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp ne " + "i32 " + b + ", " + 0 + "\n");
                    r1 = this.reg_seq;
                    this.ans.append("    %" + (++this.reg_seq) + " = zext i1 %" + this.reglist.get(id1).getSeq() + " to i32\n");
                    r2 = this.reg_seq;
                    this.ans.append("    %" + (++this.reg_seq) + " = " + "or i1 " + "%" + r1 + ", %" + r2 + "\n");
                    this.reglist.get(String.valueOf(id_name)).setSeq(this.reg_seq);
                }
                default -> {
                }
            }
        }
        else if(v1 == 0 && v2 == 1){
            this.reg_seq++;
            id_name++;
            r = new register();
            r.setSeq(this.reg_seq);
            switch (op) {
                case "+" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b + a);
                    r.setHasValue();
                    r.setValueOfReg(b + a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "add " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", " + a + "\n");
                }
                case "-" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b - a);
                    r.setHasValue();
                    r.setValueOfReg(b - a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "sub " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", " + a + "\n");
                }
                case "*" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b * a);
                    r.setHasValue();
                    r.setValueOfReg(b * a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "mul " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", " + a + "\n");
                }
                case "/" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b / a);
                    r.setHasValue();
                    r.setValueOfReg(b * a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "sdiv " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", " + a + "\n");
                }
                case "%" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b * a);
                    r.setHasValue();
                    r.setValueOfReg(b % a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "srem " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", " + a + "\n");
                }
                case ">" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b > a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b > a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp sgt " + "i32 %" + this.reglist.get(id2).getSeq() + ", " + a + "\n");
                }
                case "<" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b < a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b < a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp slt " + "i32 %" + this.reglist.get(id2).getSeq() + ", " + a + "\n");
                }
                case ">=" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b >= a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b >= a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp sge " + "i32 %" + this.reglist.get(id2).getSeq() + ", " + a + "\n");
                }
                case "<=" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b <= a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b <= a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp sle " + "i32 %" + this.reglist.get(id2).getSeq() + ", " + a + "\n");
                }
                case "==" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b == a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b == a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp eq " + "i32 %" + this.reglist.get(id2).getSeq() + ", " + a + "\n");
                }
                case "!=" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b != a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b != a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp ne " + "i32 %" + this.reglist.get(id2).getSeq() + ", " + a + "\n");
                }
                case "&&" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b != 0 && a != 0 ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b != 0 && a != 0 ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + (++this.reg_seq) + " = " + "icmp ne " + "i32 " + a + ", " + 0 + "\n");
                    r2 = this.reg_seq;
                    this.ans.append("    %" + (++this.reg_seq) + " = " + "and i1 " + "%" + this.reglist.get(id2).getSeq() + ", %" + r2 + "\n");
                    this.reglist.get(String.valueOf(id_name)).setSeq(this.reg_seq);
                }
                case "||" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b != 0 || a != 0 ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b != 0 || a != 0 ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + (++this.reg_seq) + " = " + "icmp ne " + "i32 " + a + ", " + 0 + "\n");
                    r2 = this.reg_seq;
                    this.ans.append("    %" + (++this.reg_seq) + " = " + "or i1 " + "%" + this.reglist.get(id2).getSeq() + ", %" + r2 + "\n");
                    this.reglist.get(String.valueOf(id_name)).setSeq(this.reg_seq);
                }
                default -> {
                }
            }
        }
    }
    public void setAns3(int a, int b, int v1, int v2, String id1, String id2, int sign1, int sign2){
        ident n;
        register r;
        int r1, r2;
        String op = opstack.peek().getOperator();
        if(sign1 == 0 && sign2 == 1){
            int old = ++this.reg_seq;
            if(!this.reglist.get(id1).getIsGlobal()) {
                this.ans.append("    %" + this.reg_seq + " = " + "load i32, i32* %" + this.reglist.get(id1).getSeq() + "\n");
            }
            else{
                this.ans.append("    %" + this.reg_seq + " = " + "load i32, i32* " + this.reglist.get(id1).getGlobalname() + "\n");
            }
            this.reg_seq++;
            id_name++;
            r = new register();
            r.setSeq(this.reg_seq);
            switch (op) {
                case "+" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b + a);
                    r.setHasValue();
                    r.setValueOfReg(b + a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "add " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", %" + old + "\n");
                }
                case "-" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b - a);
                    r.setHasValue();
                    r.setValueOfReg(b - a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "sub " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", %" + old + "\n");
                }
                case "*" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b * a);
                    r.setHasValue();
                    r.setValueOfReg(b * a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "mul " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", %" + old + "\n");
                }
                case "/" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b / a);
                    r.setHasValue();
                    r.setValueOfReg(b * a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "sdiv " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", %" + old + "\n");
                }
                case "%" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b * a);
                    r.setHasValue();
                    r.setValueOfReg(b % a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "srem " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", %" + old + "\n");
                }
                case ">" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b > a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b > a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp sgt " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", %" + old + "\n");
                }
                case "<" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b < a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b < a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp slt " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", %" + old + "\n");
                }
                case ">=" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b >= a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b >= a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp sge " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", %" + old + "\n");
                }
                case "<=" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b <= a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b <= a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp sle " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", %" + old + "\n");
                }
                case "==" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b == a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b == a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp eq " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", %" + old + "\n");
                }
                case "!=" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b != a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b != a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp ne " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", %" + old + "\n");
                }
                case "&&" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b != 0 && a != 0 ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b != 0 && a != 0 ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + (this.reg_seq) + " = " + "icmp ne " + "i32 " + "%" + old + ", " + 0 + "\n");
                    r2 = this.reg_seq;
                    this.ans.append("    %" + (++this.reg_seq) + " = " + "and i1 " + "%" + this.reglist.get(id2).getSeq() + ", %" + r2 + "\n");
                    this.reglist.get(String.valueOf(id_name)).setSeq(this.reg_seq);
                }
                case "||" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b != 0 || a != 0 ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b != 0 || a != 0 ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + (this.reg_seq) + " = " + "icmp ne " + "i32 " + "%" + old + ", " + 0 + "\n");
                    r2 = this.reg_seq;
                    this.ans.append("    %" + (++this.reg_seq) + " = " + "or i1 " + "%" + this.reglist.get(id2).getSeq() + ", %" + r2 + "\n");
                    this.reglist.get(String.valueOf(id_name)).setSeq(this.reg_seq);
                }
                default -> {
                }
            }
        }
        else if(sign1 == 1 && sign2 == 0){
            int old = ++this.reg_seq;
            if(!this.reglist.get(id2).getIsGlobal()) {
                this.ans.append("    %" + this.reg_seq + " = " + "load i32, i32* %" + this.reglist.get(id2).getSeq() + "\n");
            }
            else{
                this.ans.append("    %" + this.reg_seq + " = " + "load i32, i32* " + this.reglist.get(id2).getGlobalname() + "\n");
            }
            this.reg_seq++;
            id_name++;
            r = new register();
            r.setSeq(this.reg_seq);
            switch (op) {
                case "+" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b + a);
                    r.setHasValue();
                    r.setValueOfReg(b + a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "add " + "i32 " + "%" + old + ", %" + this.reglist.get(id1).getSeq() + "\n");
                }
                case "-" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b - a);
                    r.setHasValue();
                    r.setValueOfReg(b - a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "sub " + "i32 " + "%" + old + ", %" + this.reglist.get(id1).getSeq() + "\n");
                }
                case "*" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b * a);
                    r.setHasValue();
                    r.setValueOfReg(b * a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "mul " + "i32 " + "%" + old + ", %" + this.reglist.get(id1).getSeq() + "\n");
                }
                case "/" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b / a);
                    r.setHasValue();
                    r.setValueOfReg(b * a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "sdiv " + "i32 " + "%" + old + ", %" + this.reglist.get(id1).getSeq() + "\n");
                }
                case "%" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b * a);
                    r.setHasValue();
                    r.setValueOfReg(b % a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "srem " + "i32 " + "%" + old + ", %" + this.reglist.get(id1).getSeq() + "\n");
                }
                case ">" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b > a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b > a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp sgt " + "i32 " + "%" + old + ", %" + this.reglist.get(id1).getSeq() + "\n");
                }
                case "<" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b < a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b < a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp slt " + "i32 " + "%" + old + ", %" + this.reglist.get(id1).getSeq() + "\n");
                }
                case ">=" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b >= a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b >= a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp sge " + "i32 " + "%" + old + ", %" + this.reglist.get(id1).getSeq() + "\n");
                }
                case "<=" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b <= a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b <= a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp sle " + "i32 " + "%" + old + ", %" + this.reglist.get(id1).getSeq() + "\n");
                }
                case "==" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b == a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b == a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp eq " + "i32 " + "%" + old + ", %" + this.reglist.get(id1).getSeq() + "\n");
                }
                case "!=" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b != a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b != a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp ne " + "i32 " + "%" + old + ", %" + this.reglist.get(id1).getSeq() + "\n");
                }
                case "&&" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b != 0 && a != 0 ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b != 0 && a != 0 ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + (this.reg_seq) + " = " + "icmp ne " + "i32 " + "%" + old + ", " + 0 + "\n");
                    r1 = this.reg_seq;
                    this.ans.append("    %" + (++this.reg_seq) + " = " + "and i1 " + "%" + r1 + ", %" + this.reglist.get(id1).getSeq() + "\n");
                    this.reglist.get(String.valueOf(id_name)).setSeq(this.reg_seq);
                }
                case "||" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b != 0 || a != 0 ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b != 0 || a != 0 ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + (this.reg_seq) + " = " + "icmp ne " + "i32 " + "%" + old + ", " + 0 + "\n");
                    r1 = this.reg_seq;
                    this.ans.append("    %" + (++this.reg_seq) + " = " + "or i1 " + "%" + r1 + ", %" + this.reglist.get(id1).getSeq() + "\n");
                    this.reglist.get(String.valueOf(id_name)).setSeq(this.reg_seq);
                }
                default -> {
                }
            }
        }
        else{
            this.reg_seq++;
            id_name++;
            r = new register();
            r.setSeq(this.reg_seq);
            switch (op) {
                case "+" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b + a);
                    r.setHasValue();
                    r.setValueOfReg(b + a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "add " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", %" + this.reglist.get(id1).getSeq() + "\n");
                }
                case "-" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b - a);
                    r.setHasValue();
                    r.setValueOfReg(b - a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "sub " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", %" + this.reglist.get(id1).getSeq() + "\n");
                }
                case "*" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b * a);
                    r.setHasValue();
                    r.setValueOfReg(b * a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "mul " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", %" + this.reglist.get(id1).getSeq() + "\n");
                }
                case "/" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b / a);
                    r.setHasValue();
                    r.setValueOfReg(b * a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "sdiv " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", %" + this.reglist.get(id1).getSeq() + "\n");
                }
                case "%" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b * a);
                    r.setHasValue();
                    r.setValueOfReg(b % a);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "srem " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", %" + this.reglist.get(id1).getSeq() + "\n");
                }
                case ">" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b > a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b > a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp sgt " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", %" + this.reglist.get(id1).getSeq() + "\n");
                }
                case "<" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b < a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b < a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp slt " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", %" + this.reglist.get(id1).getSeq() + "\n");
                }
                case ">=" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b >= a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b >= a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp sge " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", %" + this.reglist.get(id1).getSeq() + "\n");
                }
                case "<=" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b <= a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b <= a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp sle " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", %" + this.reglist.get(id1).getSeq() + "\n");
                }
                case "==" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b == a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b == a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp eq " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", %" + this.reglist.get(id1).getSeq() + "\n");
                }
                case "!=" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b != a ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b != a ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + this.reg_seq + " = " + "icmp ne " + "i32 " + "%" + this.reglist.get(id2).getSeq() + ", %" + this.reglist.get(id1).getSeq() + "\n");
                }
                case "&&" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b != 0 && a != 0 ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b != 0 && a != 0 ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + (this.reg_seq) + " = " + "and i1 " + "%" + this.reglist.get(id2).getSeq() + ", %" + this.reglist.get(id1).getSeq() + "\n");
                    this.reglist.get(String.valueOf(id_name)).setSeq(this.reg_seq);
                }
                case "||" -> {
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), b != 0 || a != 0 ? 1 : -1);
                    r.setHasValue();
                    r.setValueOfReg(b != 0 || a != 0 ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                    this.ans.append("    %" + (this.reg_seq) + " = " + "or i1 " + "%" + this.reglist.get(id2).getSeq() + ", %" + this.reglist.get(id1).getSeq() + "\n");
                    this.reglist.get(String.valueOf(id_name)).setSeq(this.reg_seq);
                }
                default -> {
                }
            }
        }
    }
    //除0检验
    public boolean isDivZero(int b, String op){
        if(b == 0 && op.charAt(0) == '/') return false;
        else return true;
    }
    public void calculate(){
        token temp_t1, temp_t2;
        int a, b = 0;
        int sign, sign2;
        if(this.numstack.size() == 1 && !Objects.equals(this.opstack.peek().getOperator(), "!")){
            temp_t1 = numstack.pop();
            String op = opstack.peek().getOperator();
            if(temp_t1 instanceof number && !Objects.equals(op, "(")){
                number n;
                a = ((number) temp_t1).getValue();
                if(!isDivZero(a, op)) System.exit(3);
                switch(op){
                    case "+": n = new number(b + a, "Number", 10); numstack.push(n); break;
                    case "-": n = new number(b - a, "Number", 10); numstack.push(n); break;
                    case "*": n = new number(b * a, "Number", 10); numstack.push(n);break;
                    case "/": n = new number(b / a, "Number", 10); numstack.push(n);break;
                    case "%": n = new number(b % a, "Number", 10); numstack.push(n); break;
                    default: break;
                }
            }
            else if(temp_t1 instanceof ident t1){
                int tail = getRegTail(t1);
                if(this.reglist.get(t1.getId() + (tail == -1 ? "" : tail)).getHasValue()){
                    if(((ident) temp_t1).getCreate_when_op() != 0){
                        a = this.reglist.get(t1.getId() + (tail == -1 ? "" : tail)).getValueofreg();
                        if(!isDivZero(a, op)) System.exit(3);
                        int old = this.reglist.get(((ident) temp_t1).getId()+ (tail == -1 ? "" : tail)).getSeq();
                        ident n;
                        register r;
                        this.reg_seq++;
                        id_name++;
                        r = new register();
                        r.setSeq(this.reg_seq);
                        switch(op){
                            case "+": n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); r.setCreatedWhenOp(1); numstack.push(n); varlist.put(String.valueOf(id_name), b + a); r.setHasValue(); r.setValueOfReg(b + a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "add " + "i32 "  + 0 + ", %" + old + "\n");break;
                            case "-": n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); r.setCreatedWhenOp(1); numstack.push(n); varlist.put(String.valueOf(id_name), b - a); r.setHasValue(); r.setValueOfReg(b - a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "sub " + "i32 "  + 0 + ", %" + old + "\n");break;
                            case "*": n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); r.setCreatedWhenOp(1); numstack.push(n); varlist.put(String.valueOf(id_name), b * a); r.setHasValue(); r.setValueOfReg(b * a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "mul " + "i32 " + 0 + ", %" + old + "\n");break;
                            case "/": n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); r.setCreatedWhenOp(1); numstack.push(n); varlist.put(String.valueOf(id_name), b / a); r.setHasValue(); r.setValueOfReg(b * a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "sdiv " + "i32 "  + 0+ ", %" + old + "\n");break;
                            case "%": n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); r.setCreatedWhenOp(1); numstack.push(n); varlist.put(String.valueOf(id_name), b * a); r.setHasValue(); r.setValueOfReg(b % a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "srem " + "i32 "  + 0 + ", %" + old + "\n");break;
                        }
                        int for_not_eq_zero = this.reg_seq;
                        this.ans.append("    %" + (++this.reg_seq) + " = icmp ne i32 %" + for_not_eq_zero  +", 0\n");
                    }
                    else if(!this.reglist.get(t1.getId() + (tail == -1 ? "" : tail)).getIsGlobal()){
                        a = this.reglist.get(t1.getId() + (tail == -1 ? "" : tail)).getValueofreg();
                        if(!isDivZero(a, op)) System.exit(3);
                        tail = getRegTail((ident)temp_t1);
                        this.ans.append("    %" + (++this.reg_seq) +  " = " + "load i32, i32* %" + this.reglist.get(((ident) temp_t1).getId() + (tail == -1 ? "" : tail)).getSeq() + "\n");
                        int old = this.reg_seq;
                        ident n;
                        register r;
                        this.reg_seq++;
                        id_name++;
                        r = new register();
                        r.setSeq(this.reg_seq);
                        switch(op){
                            case "+": n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); r.setCreatedWhenOp(1); numstack.push(n); varlist.put(String.valueOf(id_name), b + a); r.setHasValue(); r.setValueOfReg(b + a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "add " + "i32 "  + 0 + ", %" + old + "\n");break;
                            case "-": n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); r.setCreatedWhenOp(1); numstack.push(n); varlist.put(String.valueOf(id_name), b - a); r.setHasValue(); r.setValueOfReg(b - a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "sub " + "i32 "  + 0 + ", %" + old + "\n");break;
                            case "*": n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); r.setCreatedWhenOp(1); numstack.push(n); varlist.put(String.valueOf(id_name), b * a); r.setHasValue(); r.setValueOfReg(b * a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "mul " + "i32 " + 0 + ", %" + old + "\n");break;
                            case "/": n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); r.setCreatedWhenOp(1); numstack.push(n); varlist.put(String.valueOf(id_name), b / a); r.setHasValue(); r.setValueOfReg(b * a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "sdiv " + "i32 "  + 0+ ", %" + old + "\n");break;
                            case "%": n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); r.setCreatedWhenOp(1); numstack.push(n); varlist.put(String.valueOf(id_name), b * a); r.setHasValue(); r.setValueOfReg(b % a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "srem " + "i32 "  + 0 + ", %" + old + "\n");break;
                        }
                    }
                    else{
                        a = this.reglist.get(t1.getId() + (tail == -1 ? "" : tail)).getValueofreg();
                        if(!isDivZero(a, op)) System.exit(3);
                        tail = getRegTail((ident)temp_t1);
                        this.ans.append("    %" + (++this.reg_seq) +  " = " + "load i32, i32* " + this.reglist.get(((ident) temp_t1).getId() + (tail == -1 ? "" : tail)).getGlobalname() + "\n");
                        int old = this.reg_seq;
                        ident n;
                        register r;
                        this.reg_seq++;
                        id_name++;
                        r = new register();
                        r.setSeq(this.reg_seq);
                        switch(op){
                            case "+": n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); r.setCreatedWhenOp(1); numstack.push(n); varlist.put(String.valueOf(id_name), b + a); r.setHasValue(); r.setValueOfReg(b + a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "add " + "i32 "  + 0 + ", %" + old + "\n");break;
                            case "-": n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); r.setCreatedWhenOp(1); numstack.push(n); varlist.put(String.valueOf(id_name), b - a); r.setHasValue(); r.setValueOfReg(b - a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "sub " + "i32 "  + 0 + ", %" + old + "\n");break;
                            case "*": n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); r.setCreatedWhenOp(1); numstack.push(n); varlist.put(String.valueOf(id_name), b * a); r.setHasValue(); r.setValueOfReg(b * a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "mul " + "i32 " + 0 + ", %" + old + "\n");break;
                            case "/": n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); r.setCreatedWhenOp(1); numstack.push(n); varlist.put(String.valueOf(id_name), b / a); r.setHasValue(); r.setValueOfReg(b * a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "sdiv " + "i32 "  + 0+ ", %" + old + "\n");break;
                            case "%": n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1); r.setCreatedWhenOp(1); numstack.push(n); varlist.put(String.valueOf(id_name), b * a); r.setHasValue(); r.setValueOfReg(b % a); this.reglist.put(String.valueOf(id_name), r); this.ans.append("    %" + this.reg_seq + " = " + "srem " + "i32 "  + 0 + ", %" + old + "\n");break;
                        }
                    }
                }
                else{
                    System.exit(4);
                }
            }
            opstack.pop();
        }
        else{
            int tail;
            temp_t1 = numstack.pop();
            temp_t2 = numstack.pop();
            if(temp_t1 instanceof number && temp_t2 instanceof number){
                a = ((number) temp_t1).getValue();
                b = ((number) temp_t2).getValue();
                if(isDivZero(a, opstack.peek().getOperator())){
                    setAns(a, b, 0, 0, null, null);
                }
                else{
                    System.exit(4);
                }
            }
            else if(temp_t1 instanceof number && temp_t2 instanceof ident t2){
                tail = getRegTail(t2);
                a = ((number) temp_t1).getValue();
                b = this.reglist.get(t2.getId() + (tail == -1 ? "" : tail)).getValueofreg() * ((ident) temp_t2).getIs_neg();
                if(isDivZero(a, this.opstack.peek().getOperator()) && this.reglist.get(t2.getId() + (tail == -1 ? "" : tail)).getHasValue()){
                    sign = this.reglist.get(t2.getId() + (tail == -1 ? "" : tail)).getCreatedWhenOp();
                    tail = getRegTail(t2);
                    if(sign == 0){
                        setAns(a, b, 0, 1, null, ((ident) temp_t2).getId() + (tail == -1 ? "" : tail));
                    }
                    else{
                        setAns2(a, b, 0, 1, null, ((ident) temp_t2).getId() + (tail == -1 ? "" : tail));
                    }
                }
                else {
                    System.exit(4);
                }
            }
            else if(temp_t1 instanceof ident t1 && temp_t2 instanceof number){
                tail = getRegTail(t1);
                a = this.reglist.get(t1.getId() + (tail == -1 ? "" : tail)).getValueofreg() * ((ident) temp_t1).getIs_neg();
                b = ((number) temp_t2).getValue();
                if(isDivZero(a, this.opstack.peek().getOperator()) && this.reglist.get(t1.getId() + (tail == -1 ? "" : tail)).getHasValue()){
                    sign = this.reglist.get(t1.getId() + (tail == -1 ? "" : tail)).getCreatedWhenOp();
                    tail = getRegTail(t1);
                    if(sign == 0){
                        setAns(a, b, 1, 0, ((ident) temp_t1).getId()+ (tail == -1 ? "" : tail), null);
                    }
                    else{
                        setAns2(a, b, 1, 0, ((ident) temp_t1).getId()+ (tail == -1 ? "" : tail), null);
                    }
                }
                else {
                    System.exit(4);
                }
            }
            else if(temp_t1 instanceof ident t1 && temp_t2 instanceof ident t2){
                int tail1 = getRegTail(t1);
                int tail2 = getRegTail(t2);
                a = this.reglist.get(t1.getId() + (tail1 == -1 ? "" : tail1)).getValueofreg() * ((ident) temp_t1).getIs_neg();
                b = this.reglist.get(t2.getId() + (tail2 == -1 ? "" : tail2)).getValueofreg() * ((ident) temp_t2).getIs_neg();
                if(isDivZero(a, this.opstack.peek().getOperator()) && this.reglist.get(t1.getId() + (tail1 == -1 ? "" : tail1)).getHasValue() && this.reglist.get(t1.getId() + (tail1 == -1 ? "" : tail1)).getHasValue()){
                    sign = this.reglist.get(t1.getId() + (tail1 == -1 ? "" : tail1)).getCreatedWhenOp();
                    sign2 = this.reglist.get(t2.getId() + (tail2 == -1 ? "" : tail2)).getCreatedWhenOp();
                    tail1= getRegTail((ident) temp_t1);
                    tail2 = getRegTail((ident) temp_t2);
                    if(sign == 0 && sign2 == 0){
                        setAns(a, b, 1, 1, ((ident) temp_t1).getId()+ (tail1 == -1 ? "" : tail1),((ident) temp_t2).getId()+ (tail2 == -1 ? "" : tail2));
                    }
                    else{
                        setAns3(a, b, 1, 1, ((ident) temp_t1).getId()+ (tail1 == -1 ? "" : tail1),((ident) temp_t2).getId()+ (tail2 == -1 ? "" : tail2), sign, sign2);
                    }
                }
                else {
                    System.exit(4);
                }
            }
            opstack.pop();
        }
    }
    public void calculate_not_eqzero(){
        token temp_t1;
        int a = 0;
        if(Objects.equals(this.opstack.peek().getOperator(), "!")){
            temp_t1 = this.numstack.pop();
            ident n;
            register r = new register();
            if(temp_t1 instanceof number){
                a = ((number) temp_t1).getValue();
                this.ans.append("    %" + (this.reg_seq) + " = icmp eq i32 %" + a + ", 0\n");
                int old = this.reg_seq;
                this.ans.append("    %" + (++this.reg_seq) + " = zext i1 %" + old +" to i32\n");
                id_name++;
                r.setSeq(this.reg_seq);
                r.setValueOfReg(a == 0 ? 1 : -1);
                n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                r.setHasValue();
                r.setCreatedWhenOp(1);
                numstack.push(n);
                varlist.put(String.valueOf(id_name), a == 0 ? 1 : -1);
                this.reglist.put(String.valueOf(id_name), r);
            }
            else if(temp_t1 instanceof ident t1){
                int tail = getRegTail(t1);
                if(this.reglist.get(t1.getId() + (tail == -1 ? "" : tail)).getCreatedWhenOp() != 0){
                    a = this.reglist.get(t1.getId() + (tail == -1 ? "" : tail)).getSeq();
                    this.ans.append("    %" + (++this.reg_seq) + " = icmp eq i32 %" + a + ", 0\n");
                    int old = this.reg_seq;
                    this.ans.append("    %" + (++this.reg_seq) + " = zext i1 %" + old +" to i32\n");
                    id_name++;
                    r.setSeq(this.reg_seq);
                    r.setValueOfReg(a == 0 ? 1 : -1);
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setHasValue();
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), a == 0 ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                }
                else if(!this.reglist.get(t1.getId() + (tail == -1 ? "" : tail)).getIsGlobal()){
                    a = this.reglist.get(t1.getId() + (tail == -1 ? "" : tail)).getSeq();
                    this.ans.append("    %" + (++this.reg_seq) + " = load i32, i32* %" + a + "\n");
                    this.ans.append("    %" + (++this.reg_seq) + " = icmp eq i32 %" + (this.reg_seq - 1) + ", 0\n");
                    int old = this.reg_seq;
                    this.ans.append("    %" + (++this.reg_seq) + " = zext i1 %" + old +" to i32\n");
                    id_name++;
                    r.setSeq(this.reg_seq);
                    r.setValueOfReg(a == 0 ? 1 : -1);
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setHasValue();
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), a == 0 ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                }
                else{
                    String aa = this.reglist.get(t1.getId() + (tail == -1 ? "" : tail)).getGlobalname();
                    this.ans.append("    %" + (++this.reg_seq) + " = load i32, i32* %" + aa + "\n");
                    this.ans.append("    %" + (++this.reg_seq) + " = icmp eq i32 %" + (this.reg_seq - 1) + ", 0\n");
                    int old = this.reg_seq;
                    this.ans.append("    %" + (++this.reg_seq) + " = zext i1 %" + old +" to i32\n");
                    id_name++;
                    r.setSeq(this.reg_seq);
                    r.setValueOfReg(a == 0 ? 1 : -1);
                    n = new ident(String.valueOf(id_name), "Ident", 15, 1, 1);
                    r.setHasValue();
                    r.setCreatedWhenOp(1);
                    numstack.push(n);
                    varlist.put(String.valueOf(id_name), a == 0 ? 1 : -1);
                    this.reglist.put(String.valueOf(id_name), r);
                }

            }
            this.opstack.pop();
        }
    }
    //public boolean calExp(){}
    public boolean dealExp(char c, LinkedList<token> exp, boolean is_cond){
        boolean flag = true;
        int i, j, p;
        flag = initExp(c);
        if(!flag){
            return flag;
        }
        else{
            for(i = 0; i < exp.toArray().length; i++){
                token temp_t = exp.get(i);
                if(temp_t instanceof number){
                    numstack.push((number) temp_t);
                }
                else if(temp_t instanceof ident){
                    if(!getIdentlegal((ident)temp_t)){
                        System.exit(3);
                    }
                    else if(!this.reglist.get(((ident) temp_t).getId() + getRegTail((ident)temp_t)).getIsConst()){
                        numstack.push(temp_t);
                    }
                    //变量是const
                    else{
                        number n = new number(this.reglist.get(((ident) temp_t).getId() + getRegTail((ident)temp_t)).getValueofreg(), "Number", 10);
                        numstack.push(n);
                    }
                }
                else if(temp_t instanceof operator){
                    if(Objects.equals(((operator) temp_t).getOperator(), "!")){
                        this.opstack.push((operator) temp_t);
                    }
                    else{
                        if(!this.numstack.isEmpty()){
                            while(Objects.equals(this.opstack.peek().getOperator(), "!"))
                                calculate_not_eqzero();
                        }
                        j = getPriority(((operator) temp_t).getOperator(), opstack.peek().getOperator());
                        if(j == -1) opstack.push((operator) temp_t);
                        else if(j == 1){
                            while((p = getPriority(((operator) temp_t).getOperator(), opstack.peek().getOperator())) == 1 && !numstack.isEmpty()){
                                calculate();
                            }
                            if(p == -2 && opstack.peek().getOperator().charAt(0) != c){
                                flag = false;
                                break;
                            }
                            if(p == 0 && opstack.peek().getOperator().charAt(0) == '('){
                                opstack.pop();
                            }
                            if(((operator) temp_t).getOperator().charAt(0) != ')' && ((operator) temp_t).getOperator().charAt(0) != c){
                                opstack.push((operator) temp_t);
                            }
                        }
                        else if(j == 0){
                            if(numstack.size() == 1 && numstack.peek() instanceof number n && is_cond){
                                int a = n.getValue();
                                this.ans.append("    %").append(++this.reg_seq).append(" = icmp ne i32 ").append(a).append(", 0\n");
                                opstack.pop();
                            }
                            else opstack.pop();
                        }
                    }
                }
            }
            while(getPriority(String.valueOf(c), opstack.peek().getOperator()) == 1){
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
        else if(temp_t instanceof ident i){
            int tail = getRegTail((ident) temp_t);
            return this.reglist.get(i.getId() + (tail == -1 ? "" : tail)).getValueofreg();
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
    public void setFinal_layer(int f){this.final_layer = f;}
    public void clearExp(){
        this.numstack.clear();
        this.opstack.clear();
        this.expList.clear();
    }
}
