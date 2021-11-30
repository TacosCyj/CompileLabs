public class register {
    private int seq = 0;
    private int valueofreg;
    private int CreatedWhenOp = 0;
    private String globalname;

    private Boolean hasValue = false;
    private Boolean isConst;
    private Boolean isGlobal = false;

    public void setSeq(int s){this.seq = s;}
    public void setHasValue(){this.hasValue = true;}
    public Boolean getHasValue(){return this.hasValue;}
    public void setValueOfReg(int v){this.valueofreg = v;}
    public int getSeq(){return this.seq;}
    public void setIsConst(Boolean c){this.isConst = c;}
    public Boolean getIsConst(){return this.isConst;}
    public int getValueofreg(){return this.valueofreg;}
    public void setCreatedWhenOp(int c){this.CreatedWhenOp = c;}
    public int getCreatedWhenOp(){return this.CreatedWhenOp;}
    public void setIsGlobal(Boolean f){this.isGlobal = f;}
    public Boolean getIsGlobal(){return this.isGlobal;}
    public void setGlobalname(String g){this.globalname = g;}
    public String getGlobalname(){return this.globalname;}
}
