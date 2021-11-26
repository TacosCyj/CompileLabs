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
    private Stack<Integer> dstblockeach = new Stack<>();
    private Stack<Integer> strblockeach = new Stack<>();
    private Stack<dstANDstr> three = new Stack<>();
    private expression exper;
    public LinkedList<token> expList = new LinkedList<>();
    private int reg_seq = 0;
    private int ans = 0;
    private int firstbolck = 0;
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
                flag = isLbrace(0, 0, 0);
            }
            else flag = false;
        }
        else flag = false;
        return flag;
    }
    //块中出现return，则置mark_return 为1
    public boolean isLbrace(int is_one, int mark_return, int mark_isElseIf){
        boolean flag = true;
        if(this.tokenList.peek() instanceof operator){
            if(Objects.equals(((operator) this.tokenList.peek()).getOperator(), "{")){
                if(this.firstbolck == 0){
                    this.answer.append("{\n");
                    this.firstbolck++;
                }
                else{
                    this.answer.append("\n");
                }
                this.tokenList.poll();
                flag = isStmt(is_one, mark_return, mark_isElseIf);
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
    //mark_isElseIf = 1表示是elseif
    //mark_isElseIf = 0表示是if
    public boolean isCondExp(int mark_isElseIf){
        boolean flag = true;
        token check = null;
        this.expList.clear();
        while((this.tokenList.peek() instanceof operator || this.tokenList.peek() instanceof number || this.tokenList.peek() instanceof ident)){
            check = this.tokenList.peek();
            this.expList.offer(this.tokenList.poll());
            if(check instanceof operator){
                if(((operator) check).getOperator().charAt(0) == '{') break;
            }
        }
        if(check instanceof operator && (Objects.equals(((operator) check).getOperator(), "{"))){
            exper.getExp(expList);
            exper.getMap(varlist, reglist, timelist, constlist, reg_seq, answer);
            flag = exper.dealExp(((operator) check).getOperator().charAt(0), expList);
            if(flag){
                this.reg_seq = exper.passRegSeq();
                this.answer = exper.getAns();
                this.t_judge = exper.forJudge();
                //将'{'加回去
                this.tokenList.addFirst(check);
                if(mark_isElseIf == 0){
                    dstANDstr temp = new dstANDstr(this.reg_seq + 1, this.reg_seq + 2, this.reg_seq + 3);
                    this.three.push(temp);
                    this.answer.append("    br i1 %").append(this.reg_seq).append(",label %").append(++this.reg_seq).append(", label %").append(++this.reg_seq);
                }
                else{
                    int a1 = this.three.peek().getIf_seq();
                    int a2 = this.three.peek().getElse_seq();
                    int a3 = this.three.peek().getDst();
                    this.three.pop();
                    dstANDstr temp_new = new dstANDstr(a2, this.reg_seq + 1, a3);
                    this.three.push(temp_new);
                    this.answer.append("    br i1 %").append(this.reg_seq).append(",label %").append(a2).append(", label %").append(++this.reg_seq);
                }
                exper.clearExp();
            }
        }
        else flag = false;
        return flag;
    }
    public boolean isIf(int mark_isElseIf){
        boolean flag = true;
        //if中表达式处理，包括两个跳转地址压栈、一个块跳转地址压栈
        //设置three
        flag = isCondExp(mark_isElseIf);
        //弹出)
        if(flag){
            this.strblockeach.push(this.three.peek().getIf_seq());
            flag = isLbrace(1, 0, mark_isElseIf);
        }
        return flag;
    }
    public boolean isElseIf(int mark_isElseIf){
        boolean flag = true;
        this.strblockeach.push(this.three.peek().getElse_seq());
        flag = isStmt(1, 0, mark_isElseIf);
        return flag;
    }
    public boolean isElse(int mark_isElseIf){
        boolean flag = true;
        //进入新的块
        this.strblockeach.push(this.three.peek().getElse_seq());
        flag = isLbrace(2, 0, mark_isElseIf);
        return flag;
    }
    public boolean isStmt(int is_one, int mark_return, int mark_isElseIf){
        boolean flag = true;
        if(!this.strblockeach.isEmpty()){
            this.answer.append(this.strblockeach.pop()).append(":").append("\n");
        }
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
                    if(flag) mark_return = 1;
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
            //if else statement
            else if(this.tokenList.peek() instanceof cond){
                cond temp = (cond)this.tokenList.poll();
                if(Objects.equals(temp.getCondid(), "if")){
                    flag = isIf(0);
                }
                else if(Objects.equals(temp.getCondid(), "else if")){
                    flag = isElseIf(1);
                }
                else{
                    flag = isElse(0);
                }
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
                //标志着一个块儿的结束
                else if(Objects.equals(op.getOperator(), "}")){
                    this.tokenList.poll();
                    //main块结束
                    if(is_one == 0){
                        //System.out.println("Here");
                        this.answer.append("}");
                        return isEnd();
                    }
                    //if块结束
                    else if(is_one == 1){
                        if(mark_return == 1){
                            return flag;
                        }
                        else{
                            this.answer.append("    br label %").append(this.three.peek().getDst() + "\n");
                        }
                    }
                    //else 块结束
                    else{
                        if(mark_return == 1){
                            this.answer.append(this.three.pop().getDst()).append(":").append("\n");
                        }
                        else{
                            this.answer.append("    br label %").append(this.three.peek().getDst() + "\n");
                            this.answer.append(this.three.pop().getDst()).append(":").append("\n");
                        }
                    }
                    return flag;
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
}
