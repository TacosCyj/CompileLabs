import javax.sound.midi.SysexMessage;
import java.util.*;

public class Grammar {
    private LinkedList<token> tokenList;
    private static Grammar grammar;
    private StringBuilder answer = new StringBuilder();
    private HashMap<String, Integer> varlist = new HashMap<>();
    private HashMap<String, register> reglist = new HashMap<>();
    private HashMap<String, Integer> timelist = new HashMap<>();
    private HashMap<String, Boolean> constlist = new HashMap<>();
    private HashMap<String, Integer> funclist = new HashMap<>();
    private expression exper;
    public LinkedList<token> expList = new LinkedList<>();
    private int reg_seq = 0;
    private int ans = 0;
    private token t_judge;
    private Grammar(){}
    static{
        grammar = new Grammar();
    }
    public static Grammar getInstance(){
        return grammar;
    }
    public void setExper(expression exp){
        this.exper = exp;
    }
    public void setTokenList(LinkedList<token> q){
        this.tokenList = q;
    }
    public void checkForFunc(){
        int i;
        for(i = 0; i < this.tokenList.toArray().length; i++){
            token temp = this.tokenList.get(i);
            if(temp instanceof function){
                if(Objects.equals(((function) temp).getFuncName(), "getint") && !this.funclist.containsKey("getint")){
                    this.answer.append("declare i32 @getint()\n");
                    this.funclist.put("getint", 1);
                }
                else if(Objects.equals(((function) temp).getFuncName(), "getch") && !this.funclist.containsKey("getch")){
                    this.answer.append("declare i32 @getch()\n");
                    this.funclist.put("getch", 2);
                }
                else if(Objects.equals(((function) temp).getFuncName(), "putint") && !this.funclist.containsKey("putint")){
                    this.answer.append("declare void @putint(i32)\n");
                    this.funclist.put("putint", 3);
                }
                else if(Objects.equals(((function) temp).getFuncName(), "putch") && !this.funclist.containsKey("putch")){
                    this.answer.append("declare void @putch(i32)\n");
                    this.funclist.put("putch", 4);
                }
            }
        }
    }
    //递归下降
    public boolean isInt(){
        boolean flag = true;
        if(this.tokenList.peek() instanceof ident){
            if(Objects.equals(((ident) this.tokenList.peek()).getId(), "int")){
                this.answer.append("define dso_local i32 ");
                this.tokenList.poll();
                flag = isMain();
            }
            else flag = false;
        }
        else flag = false;
        return flag;
    }
    public boolean isMain(){
        boolean flag = true;
        if(this.tokenList.peek() instanceof function){
            if(Objects.equals(((function) this.tokenList.peek()).getFuncName(), "main")){
                this.answer.append("@main");
                this.tokenList.poll();
                flag = isLbar();
            }
            else flag = false;
        }
        else flag = false;
        return flag;
    }
    public boolean isLbar(){
        boolean flag = true;
        if(this.tokenList.peek() instanceof operator){
            if(Objects.equals(((operator) this.tokenList.peek()).getOperator(), "(")){
                this.answer.append("(");
                this.tokenList.poll();
                flag = isRbar();
            }
            else flag = false;
        }
        else flag = false;
        return flag;
    }
    public boolean isRbar(){
        boolean flag = true;
        if(this.tokenList.peek() instanceof operator){
            if(Objects.equals(((operator) this.tokenList.peek()).getOperator(), ")")){
                this.answer.append(")");
                this.tokenList.poll();
                flag = isLbrace();
            }
            else flag = false;
        }
        else flag = false;
        return flag;
    }
    public boolean isLbrace(){
        boolean flag = true;
        if(this.tokenList.peek() instanceof operator){
            if(Objects.equals(((operator) this.tokenList.peek()).getOperator(), "{")){
                this.answer.append("{\n");
                this.tokenList.poll();
                flag = isStmt();
            }
            else flag = false;
        }
        else flag = false;
        return flag;
    }
    public boolean isExp(){
        boolean flag = true;
        token check = null;
        this.expList.clear();
        while((this.tokenList.peek() instanceof operator || this.tokenList.peek() instanceof number || this.tokenList.peek() instanceof ident || this.tokenList.peek() instanceof function)){
            check = this.tokenList.peek();
            this.expList.offer(this.tokenList.poll());
            if(check instanceof operator){
                if(((operator) check).getOperator().charAt(0) == ';'|| ((operator) check).getOperator().charAt(0) == ',') break;
            }
        }
        if(check instanceof operator && (Objects.equals(((operator) check).getOperator(), ";") || Objects.equals(((operator) check).getOperator(), ","))){
            exper.getExp(expList);
            exper.getMap(varlist, reglist, timelist, constlist, reg_seq, answer);
            flag = exper.dealExp(((operator) check).getOperator().charAt(0), expList);
            if(flag){
                this.ans = exper.passAns();
                this.reg_seq = exper.passRegSeq();
                this.answer = exper.getAns();
                this.t_judge = exper.forJudge();
                // System.exit(0);
                //将表达式终止符号;或,加回去，方便下面的合法性判断
                this.tokenList.addFirst(check);
                exper.clearExp();
            }
        }
        else flag = false;
        return flag;
    }
    public boolean isGiveValueOld(ident obj){
        boolean flag = true;
        int old_value = this.reg_seq;
        if(!constlist.get(obj.getId()) && isExp()){
            this.varlist.put(obj.getId(), ans);
            register reg = this.reglist.get(obj.getId());
            reg.setValueOfReg(ans);
            obj.setAssigntimes(obj.getAssigntimes() + 1);
            this.timelist.put(obj.getId(),obj.getAssigntimes());
            if(this.reg_seq == old_value){
                if(t_judge instanceof number) this.answer.append("    store i32 ").append(ans).append(", i32* %").append(reg.getSeq()).append("\n");
                else if(t_judge instanceof ident){
                    int s = this.reglist.get(((ident) t_judge).getId()).getSeq();
                    this.reg_seq++;
                    this.answer.append("    %" + this.reg_seq + " = load i32, i32* %" + s + "\n");
                    this.answer.append("    store i32 ").append("%" + this.reg_seq).append(", i32* %").append(reg.getSeq()).append("\n");
                }
            }
            else{
                this.answer.append("    store i32 %").append(this.reg_seq).append(", i32* %").append(reg.getSeq()).append("\n");
            }
        }
        else if(constlist.get(obj.getId()) && timelist.get(obj.getId()) == 0 && isExp()){
            this.varlist.put(obj.getId(), ans);
            register reg = this.reglist.get(obj.getId());
            reg.setValueOfReg(ans);
            obj.setAssigntimes(obj.getAssigntimes() + 1);
            this.timelist.put(obj.getId(),obj.getAssigntimes());
            if(this.reg_seq == old_value){
                if(t_judge instanceof number) this.answer.append("    store i32 ").append(ans).append(", i32* %").append(reg.getSeq()).append("\n");
                else if(t_judge instanceof ident){
                    int s = this.reglist.get(((ident) t_judge).getId()).getSeq();
                    this.reg_seq++;
                    this.answer.append("    %" + this.reg_seq + " = load i32, i32* %" + s + "\n");
                    this.answer.append("    store i32 ").append("%" + this.reg_seq).append(", i32* %").append(reg.getSeq()).append("\n");
                }
            }
            else{
                this.answer.append("    store i32 %").append(this.reg_seq).append(", i32* %").append(reg.getSeq()).append("\n");
            }
        }
        else{
            flag = false;
        }
        return flag;
    }
    public boolean isIndentifyNew(boolean is_const){
        boolean flag = true;
        if(this.tokenList.peek() instanceof ident){
            ident temp_token = (ident) this.tokenList.poll();
            if(this.varlist.isEmpty() || !this.varlist.containsKey(temp_token.getId())){
                if(is_const) constlist.put(temp_token.getId(), true);
                else constlist.put(temp_token.getId(), false);
                //加入变量表
                this.varlist.put(temp_token.getId(), -1);
                this.timelist.put(temp_token.getId(), 0);
                register reg = new register();
                this.reg_seq++;
                reg.setSeq(this.reg_seq);
                reg.setValueOfReg(-1);
                reg.setOwnerofreg(temp_token.getId());
                this.reglist.put(temp_token.getId(), reg);
                this.answer.append("    %").append(this.reg_seq).append(" = alloca i32\n");
                //检查下一个
                if(this.tokenList.peek() instanceof operator){
                    operator temp_op = (operator) this.tokenList.peek();
                    if(Objects.equals(temp_op.getOperator(), ",")){
                        this.tokenList.poll();
                        flag = isIndentifyNew(is_const);
                    }
                    else if(Objects.equals(temp_op.getOperator(), ";")){
                        this.tokenList.poll();
                        flag = true;
                        return flag;
                    }
                    else if(Objects.equals(temp_op.getOperator(), "=")){
                        this.tokenList.poll();
                        token f = this.tokenList.peek();
                        if(f instanceof function){
                            if(Objects.equals(((function) f).getFuncName(), "getint")){
                                this.answer.append("    %").append(++this.reg_seq).append("= call i32 @getint()\n");
                                this.answer.append("    store i32 %" + this.reg_seq + ", i32* %" + this.reglist.get(temp_token.getId()).getSeq() + "\n");
                                temp_token.setAssigntimes(temp_token.getAssigntimes() + 1);
                                this.timelist.put(temp_token.getId(), temp_token.getAssigntimes());
                            }
                            else if(Objects.equals(((function) f).getFuncName(), "getch")){
                                this.answer.append("    %").append(++this.reg_seq).append("= call i32 @getch()\n");
                                this.answer.append("    store i32 %" + this.reg_seq + ", i32* %" + this.reglist.get(temp_token.getId()).getSeq() + "\n");
                                temp_token.setAssigntimes(temp_token.getAssigntimes() + 1);
                                this.timelist.put(temp_token.getId(), temp_token.getAssigntimes());
                            }
                            else{
                                flag = false;
                            }
                            this.tokenList.poll();
                            this.tokenList.poll();
                            this.tokenList.poll();
                            if(this.tokenList.peek() instanceof operator && flag){
                                operator temp_op_2 = (operator) this.tokenList.peek();
                                if(Objects.equals(temp_op_2.getOperator(), ",")){
                                    this.tokenList.poll();
                                    flag = isIndentifyNew(is_const);
                                }
                                else if(Objects.equals(temp_op_2.getOperator(), ";")){
                                    this.tokenList.poll();
                                    //System.exit(0);
                                    flag = true;
                                    return flag;
                                }
                                else flag = false;
                            }
                            else flag = false;
                        }
                        else{
                            flag = isGiveValueOld(temp_token);
                            if(this.tokenList.peek() instanceof operator && flag){
                                operator temp_op_2 = (operator) this.tokenList.peek();
                                if(Objects.equals(temp_op_2.getOperator(), ",")){
                                    this.tokenList.poll();
                                    flag = isIndentifyNew(is_const);
                                }
                                else if(Objects.equals(temp_op_2.getOperator(), ";")){
                                    this.tokenList.poll();
                                    //System.exit(0);
                                    flag = true;
                                    return flag;
                                }
                                else flag = false;
                            }
                            else flag = false;
                        }
                    }
                    else flag = false;
                }
                else flag = false;
            }
            else flag = false;
        }
        else flag = false;
        return flag;
    }
    public boolean isStmt(){
        boolean flag = true;
        while(flag){
            if(this.tokenList.peek() instanceof ident){
                ident temp_ident = (ident)this.tokenList.peek();
                //int
                if(Objects.equals(temp_ident.getId(), "int")){
                    this.tokenList.poll();
                    if(this.tokenList.peek() instanceof ident) flag = isIndentifyNew(false);
                    else flag = false;
                }
                //const int
                else if(Objects.equals(temp_ident.getId(), "const")){
                    this.tokenList.poll();
                    if(this.tokenList.peek() instanceof ident){
                        if(Objects.equals(((ident) this.tokenList.peek()).getId(), "int")){
                            this.tokenList.poll();
                            if(this.tokenList.peek() instanceof ident) flag = isIndentifyNew(true);
                            else flag = false;
                        }
                        else flag= false;
                    }
                    else flag = false;
                }
                else if(Objects.equals(temp_ident.getId(), "return")){
                    flag = isReturn();
                    break;
                }
                else{
                    if(this.varlist.containsKey(temp_ident.getId())){
                        token back = this.tokenList.poll();
                        if(this.tokenList.peek() instanceof operator){
                            if(Objects.equals(((operator) this.tokenList.peek()).getOperator(), "=")){
                                this.tokenList.poll();
                                token f = this.tokenList.peek();
                                if(f instanceof function){
                                    if(Objects.equals(((function) f).getFuncName(), "getint")){
                                        this.answer.append("    %").append(++this.reg_seq).append("= call i32 @getint()\n");
                                        this.answer.append("    store i32 %" + this.reg_seq + ", i32* %" + this.reglist.get(temp_ident.getId()).getSeq() + "\n");
                                        temp_ident.setAssigntimes(temp_ident.getAssigntimes() + 1);
                                        this.timelist.put(temp_ident.getId(), temp_ident.getAssigntimes());
                                    }
                                    else if(Objects.equals(((function) f).getFuncName(), "getch")){
                                        this.answer.append("    %").append(++this.reg_seq).append("= call i32 @getch()\n");
                                        this.answer.append("    store i32 %" + this.reg_seq + ", i32* %" + this.reglist.get(temp_ident.getId()).getSeq() + "\n");
                                        temp_ident.setAssigntimes(temp_ident.getAssigntimes() + 1);
                                        this.timelist.put(temp_ident.getId(), temp_ident.getAssigntimes());
                                    }
                                    else{
                                        flag = false;
                                    }
                                    this.tokenList.poll();
                                    this.tokenList.poll();
                                    this.tokenList.poll();
                                    if(this.tokenList.peek() instanceof operator && flag){
                                        operator temp_op_2 = (operator) this.tokenList.peek();
                                        if(Objects.equals(temp_op_2.getOperator(), ";")){
                                            this.tokenList.poll();
                                        }
                                        else flag = false;
                                    }
                                    else flag = false;
                                }
                                else{
                                    flag = isGiveValueOld(temp_ident);
                                    if(this.tokenList.peek() instanceof operator && flag){
                                        operator temp_op_2 = (operator) this.tokenList.peek();
                                        if(Objects.equals(temp_op_2.getOperator(), ";")){
                                            this.tokenList.poll();
                                        }
                                        else flag = false;
                                    }
                                    else flag = false;
                                }
                            }
                            else{
                                this.tokenList.addFirst(back);
                                flag = isExp();
                                if(this.tokenList.peek() instanceof operator && flag){
                                    operator temp_op_2 = (operator) this.tokenList.peek();
                                    if(Objects.equals(temp_op_2.getOperator(), ";")){
                                        this.tokenList.poll();
                                    }
                                    else flag = false;
                                }
                                else flag = false;
                            }
                        }
                        else flag = false;
                    }
                    else flag = false;
                }
            }
            //putint、putch
            else if(this.tokenList.peek() instanceof function){
                function f = (function)this.tokenList.poll();
                String func_name = f.getFuncName();
                //this.tokenList.poll();
                int old_value = this.reg_seq;
                //removeRbar(0);
                flag = isExp();
                if(flag && this.tokenList.peek() instanceof operator){
                    //System.out.println("HIHI");
                    operator temp_opp = (operator) this.tokenList.peek();
                    if(Objects.equals(temp_opp.getOperator(), ";")){
                        if(this.reg_seq == old_value){
                            if(t_judge instanceof number){
                                if(Objects.equals(func_name, "putint")){
                                    this.answer.append("    call void @putint(i32 " + ans + ")\n");
                                }
                                else{
                                    this.answer.append("    call void @putch(i32 " + ans + ")\n");
                                }
                            }
                            else if(t_judge instanceof ident){
                                int s = this.reglist.get(((ident) t_judge).getId()).getSeq();
                                this.answer.append("    %" + (++this.reg_seq) + " = " + "load i32, i32* %" + s + "\n");
                                if(Objects.equals(func_name, "putint")){
                                    this.answer.append("    call void @putint(i32 %" + this.reg_seq + ")\n");
                                }
                                else{
                                    this.answer.append("    call void @putch(i32 %" + this.reg_seq + ")\n");
                                }
                            }
                        }
                        else{
                            if(Objects.equals(func_name, "putint")){
                                this.answer.append("    call void @putint(i32 %" + this.reg_seq + ")\n");
                            }
                            else{
                                this.answer.append("    call void @putch(i32 %" + this.reg_seq + ")\n");
                            }
                        }
                        this.tokenList.poll();
                    }
                    else flag = false;
                }
                else flag = false;
            }
            //一行只有一个表达式
            else if(this.tokenList.peek() instanceof number){
                flag = isExp();
                if(flag && this.tokenList.peek() instanceof operator){
                    operator temp_opp = (operator) this.tokenList.peek();
                    if(Objects.equals(temp_opp.getOperator(), ";")){
                        this.tokenList.poll();
                    }
                    else flag = false;
                }
                else flag = false;
            }
            else if(this.tokenList.peek() instanceof operator){
                operator op = (operator) this.tokenList.peek();
                if(Objects.equals(op.getOperator(), "+") || Objects.equals(op.getOperator(), "-") || Objects.equals(op.getOperator(), "(")){
                    flag = isExp();
                    if(flag && this.tokenList.peek() instanceof operator){
                        operator temp_opp = (operator) this.tokenList.peek();
                        if(Objects.equals(temp_opp.getOperator(), ";")){
                            this.tokenList.poll();
                        }
                        else flag = false;
                    }
                    else flag = false;
                }
                else flag = false;
            }
            else flag = false;
        }
        return flag;
    }
    public boolean isReturn(){
        boolean flag = true;
        if(this.tokenList.peek() instanceof ident){
            if(Objects.equals(((ident) this.tokenList.peek()).getId(), "return")){
                this.tokenList.poll();
                int old_value = this.reg_seq;
                flag = isExp();
                if(this.tokenList.peek() instanceof operator && flag){
                    operator temp_op_2 = (operator) this.tokenList.peek();
                    //System.out.println(ans);
                    if(Objects.equals(temp_op_2.getOperator(), ";")){
                        this.tokenList.poll();
                        if(this.reg_seq == old_value){
                            if(t_judge instanceof number){
                                this.answer.append("    ret i32 ");
                                this.answer.append(ans).append("\n");
                            }
                            else if(t_judge instanceof ident){
                                int s = this.reglist.get(((ident) t_judge).getId()).getSeq();
                                this.answer.append("    %" + (++this.reg_seq) + " = " + "load i32, i32* %" + s + "\n");
                                this.answer.append("    ret i32 %" + this.reg_seq).append("\n");
                            }
                        }
                        else{
                            this.answer.append("    ret i32 %" + this.reg_seq).append("\n");
                        }
                        flag = isRbrace();
                    }
                    else flag = false;
                }
                else flag = false;
            }
            else flag = false;
        }
        else flag = false;
        return flag;
    }
    public boolean isRbrace(){
        boolean flag = true;
        if(this.tokenList.peek() instanceof operator){
            if(Objects.equals(((operator) this.tokenList.peek()).getOperator(), "}")){
                this.answer.append("}");
                this.tokenList.poll();
                flag = isEnd();
            }
            else flag = false;
        }
        else flag = false;
        return flag;
    }
    public boolean isEnd(){
        boolean flag = true;
        if(this.tokenList.peek() instanceof operator){
            if(Objects.equals(((operator) this.tokenList.peek()).getOperator(), "#")){
                return true;
            }
        }
        return false;
    }
    public StringBuilder getAnswer(){return this.answer;}
    public void removeRbar(int start){
        int i;
        for(i = start; i < this.tokenList.toArray().length; i++){
            if(this.tokenList.get(i) instanceof operator){
                operator t = (operator) this.tokenList.get(i);
                if(Objects.equals(t.getOperator(), ";")){
                    break;
                }
            }
        }
        this.tokenList.remove(i - 1);
    }
}
