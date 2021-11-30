public class register {
    private int seq = 0;
    private String ownerofreg = "";
    private int valueofreg;
    private Boolean hasValue = false;
    private Boolean isConst;
    private int CreatedWhenOp = 0;
    private int is_neg;
    public void setSeq(int s){this.seq = s;}
    public void setHasValue(){this.hasValue = true;}
    public Boolean getHasValue(){return this.hasValue;}
    public void setValueOfReg(int v){this.valueofreg = v;}
    public void setOwnerofreg(String s){this.ownerofreg = s;}
    public int getSeq(){return this.seq;}
    public void setIsConst(Boolean c){this.isConst = c;}
    public Boolean getIsConst(){return this.isConst;}
    public int getValueofreg(){return this.valueofreg;}
    public void setCreatedWhenOp(int c){this.CreatedWhenOp = c;}
    public int getCreatedWhenOp(){return this.CreatedWhenOp;}
    public void setIs_neg(int n){this.is_neg = n;}
    public int getIs_neg(){return this.is_neg;}
}
