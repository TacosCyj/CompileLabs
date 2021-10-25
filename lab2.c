#include<stdio.h>
#include<stdlib.h>
#include<string.h>
#include<ctype.h>
#define LEN 1024

char content[LEN];
char token[LEN];
int numstack[LEN];
int isnumstack[LEN];
char OPTRstack[LEN];
int opList[5] = {19, 20, 21, 22, 23};

int top_n = -1;
int top_o = -1;
int top_i = -1;
int num_in_Bar = 0;

char* pmove;
char* pmove_for_semi;
char* pmove_exp;
static int symbol;
static int isLegal;
static int returnValue;
static int Value;
static int numOfLBar;
static int numOfRBar;
static int numOfMinus;
static int numOfPlus;

int PriorityMatrix[9][9] = {
    {1, 1, -1, -1, -1, -1, 1, 1},
    {1, 1, -1, -1, -1, -1, 1, 1},
    {1, 1, 1, 1, 1, -1, 1, 1},
    {1, 1, 1, 1, 1, -1, 1, 1},
    {1, 1, 1, 1, 1, -1, 1, 1},
    {-1, -1, -1, -1, -1, -1, 0, -2},
    {1, 1, 1, 1, 1, -2, 1, 1},
    {-1, -1, -1, -1, -1, -1, -2, -2},
};
void isInt();
void isMain();
void isLBar();
void isRBar();
void isLBrace();
void isRBrace();
void isReturn();
void isLeftExp();
void isRightExp();
void isNumber();
void isSemicolon();
void isFinished();
void initStack();
void isExp();


int numStack_top(){
    return numstack[top_n];
}
int numStack_pop(){
    return numstack[top_n--];
}

void numStack_push(int n){
    numstack[++top_n] = n;
}

char optrStack_pop(){
    return OPTRstack[top_o--];
}

char optrStack_top(){
    return OPTRstack[top_o];
}

void optrStack_push(char c){
    OPTRstack[++top_o] = c;
}

void reverse(){
    int i = 0, j = strlen(token) - 1;
    while(i < j){
        char temp = token[i];
        token[i] = token[j];
        token[j] = temp;
        i++;
        j--;
    }
}

void HexToDec(){
    int sum = 0;
    int len = strlen(token);
    int i, j = 0;
    for(i = 2; i < len; i++){
        if(isdigit(token[i])){
            sum = sum * 16 + (token[i] - '0');
        }
        if(isalpha(token[i])){
            sum = sum * 16 + (toupper(token[i]) - 'A' + 10);
        }
    }
    memset(token, 0, sizeof(token));
    while(sum){
        token[j++] = (sum % 10) + '0';
        sum /= 10;
    }
    reverse();
}

void OctToDec(){
    int sum = 0;
    int len = strlen(token);
    int i, j = 0;
    for(i = 1; i < len; i++){
        sum = sum * 8 + (token[i] - '0');
    }
    memset(token, 0, sizeof(token));
    while(sum){
        token[j++] = (sum % 10) + '0';
        sum /= 10;
    }
    reverse();
}

int isalpha_Hex(char c){
    if((c >= 'A' && c <= 'F')|| (c >= 'a' && c <= 'f')) return 0;
    else return 1;
}

int isdigit_Oct(char c){
    if((c >= '0' && c <= '7')) return 0;
    else return 1;
}
int isLegalHex(){
    for(int i = 2; i < strlen(token); i++){
        if(isdigit(token[i]) == 0 && isalpha_Hex(token[i]) == 1) return 1;
    }
    return 0;
}

int isLegalOct(){
    for(int i = 1; i < strlen(token); i++){
        if(isdigit_Oct(token[i]) == 1) return 1;
    }
    return 0;
}

int isLegalDeci(){
    for(int i = 0; i < strlen(token); i++){
        if(isdigit(token[i]) == 0) return 1;
    }
    return 0;
}

/*!!!!!!*/
int CheckNumber(){
    char* pp = token;
    if(*pp == '0' && strlen(token) > 1){
        pp++;
        if(*pp == 'X' || *pp == 'x'){
            if(isLegalHex() == 0){
                HexToDec();
                return 0;
            }
            else return 1;
        }
        else{
            if(isLegalOct() == 0){
                OctToDec();
                return 0;
            }
            else return 1;
        }
    }
    else{
        if(isLegalDeci() == 0) return 0;
        else return 1;
    }
}

void getsym(){
    symbol = -1;
    int start = 0;
    memset(token, 0, sizeof(token));
    while(*pmove == ' ' || *pmove == '\n' || *pmove == '\t' || *pmove == '\r') pmove++;
    if(*pmove == '#'){
        symbol = -2;
    }
    else if(*pmove == '/'){
        int flag = 0;
        char* pp = pmove + 1;
        if(*pp == '/'){
            while(*pp != '\n' || *pp == '\r'){
                pp++;
                if(*pp == '#') break;
            }
            pmove = pp;
            getsym();
        }
        else if(*pp == '*'){
            char* pmove_temp = pmove;
            while(*pp != '/' || *pmove_temp != '*'){
                pp++;
                pmove_temp++;
                if(*pp == '#'){
                    symbol = -1;
                    isLegal = 1;
                    break;
                }
            }
            if(!isLegal){
                pmove = pp + 1;
                getsym();
            }
        }
        else{
            token[0] = '/';
            pmove_exp = pmove;
            symbol = 22;
            pmove++;
        }
    }
    else if(isdigit(*pmove)){
        while(isdigit(*pmove) || isalpha(*pmove)){
            token[start++] = *pmove;
            pmove++;
        }
        if(CheckNumber() == 0) symbol = 10;
        else symbol = -1;
    }
    else if(isalpha(*pmove)){
        while(isalpha(*pmove)){
            token[start++] = *pmove;
            pmove++;
        }
        if(strcmp(token, "int") == 0) symbol = 11;
        else if(strcmp(token, "main") == 0) symbol = 12;
        else if(strcmp(token, "return") == 0) symbol = 13;
        else symbol = -1;
    }
    else {
        switch(*pmove){
            case '(': token[0] = '('; symbol = 14; break;
            case ')': token[0] = ')'; symbol = 15; break;
            case '{': token[0] = '{'; symbol = 16; break;
            case '}': token[0] = '}'; symbol = 17; break;
            case ';': token[0] = ';'; symbol = 18; pmove_for_semi = pmove; break;
            case '+': token[0] = '+'; symbol = 19; break;
            case '-': token[0] = '-'; symbol = 20; break;
            case '*': token[0] = '*'; symbol = 21; break;
            case '/': token[0] = '/'; symbol = 22; break;
            case '%': token[0] = '%'; symbol = 23; break;
            default: symbol = -1; break;
        }
        pmove_exp = pmove;
        pmove++;
    }
}

void getValue(){
    int i;
    int l = strlen(token);
    Value = 0;
    for(i = 0; i < l; i++){
        Value = Value * 10 + (token[i] - '0');
    }
}

void getReturnValue(){
    int i;
    int l = strlen(token);
    for(i = 0; i < l; i++){
        returnValue = returnValue * 10 + (token[i] - '0');
    }
    if(numOfMinus % 2) returnValue *= -1;
}

void init(){
    pmove = content;
}

void isInt(){
    getsym();
    if(symbol == 11){
        isMain();
    }
    else isLegal = 1;
}

void isMain(){
    getsym();
    if(symbol == 12){
        isLBar();
    }
    else isLegal = 1;
}

void isLBar(){
    getsym();
    if(symbol == 14){
        isRBar();
    }
    else isLegal = 1;
}

void isRBar(){
    getsym();
    if(symbol == 15){
        isLBrace();
    }
    else isLegal = 1;
}

void isLBrace(){
    getsym();
    if(symbol == 16){
        isReturn();
    }
    else isLegal = 1;
}

void initStack(){
    optrStack_push(';');
}

void initExp(){
    char* pp = pmove;
    char* pt, *pe;
    int numofplus = 0, numofminus = 0;
    while(*pp != ';' && *pp != '\n' && *pp != '\r'){
        if(*pp == '+' || *pp == '-'){
            pt = pp;
            while(*pt == '+' || *pt == '-' || *pt == ' '){
                if(*pt == '+') numofplus++;
                else if(*pt == '-') numofminus++;
                pt++;
            }
            pe = pt;
            if(numofminus % 2 == 0) *pp = '+';
            else *pp = '-';
            pt = pp + 1;
            while(pt != pe){
                *pt = ' ';
                pt++;
            }
            numofplus = 0;
            numofminus = 0;
            pp = pe;
        }
        else pp++;
    }
}

void isReturn(){
    getsym();
    int number = 0;
    int bar = 0;
    if(symbol == 13){
        char* pp = pmove;
        while(*pmove != '\n' && *pmove !='\r'){
            getsym();
            if(symbol == 10) number++;
            else if(symbol == 14) bar++;
            else if(symbol == 15) bar--;
        }
        if(bar == 0){
            if(number == 1){
                pmove = pp;
                isLeftExp();
            }
            else{
                pmove = pp;
                initStack();
                initExp();
                isExp();
            }
        }
        else isLegal = 1;
    }
    else isLegal = 1;
}

void isLeftExp(){
    getsym();
    if(symbol == 14){
        numOfLBar++;
        isLeftExp();
    }
    else if(symbol == 19){
        numOfPlus++;
        isLeftExp();
    }
    else if(symbol == 20){
        numOfMinus++;
        isLeftExp();
    }
    else if(symbol == 10){
        isNumber();
    }
    else isLegal = 1;
}

void isRightExp(){
    if(numOfLBar == 0 || numOfLBar == numOfRBar){
        isSemicolon();
    }
    else{
        getsym();
        if(symbol == 15){
            numOfRBar++;
            isRightExp();
        }
        else isLegal = 1;
    }
}

void isNumber(){
    getReturnValue();
    isRightExp();
}


int calculate(){
    if(top_n == 0){
        int a = numStack_pop();
        int b = 0;
        char op = optrStack_top();
        switch(op){
            case '+': numStack_push(b + a); break;
            case '-': numStack_push(b - a); break;
            case '*': numStack_push(b * a); break;
            case '/': numStack_push(b / a); break;
            case '%': numStack_push(b % a); break;
            default: break;
        }
        optrStack_pop();
        return 0;
    }
   else if(top_n > 0){
        int a = numStack_pop();
        int b = numStack_pop();
        char op = optrStack_top();
        switch(op){
            case '+': numStack_push(b + a); break;
            case '-': numStack_push(b - a); break;
            case '*': numStack_push(b * a); break;
            case '/': numStack_push(b / a); break;
            case '%': numStack_push(b % a); break;
            default: break;
        }
        optrStack_pop();
        return 0;
    }
    else{
        return 1;
    }
}

int getPriority(char a, char b){
    char c_x = a;
    char c_y = b;
    int x, y;
    switch(c_x){
        case '+': x = 0; break;
        case '-': x = 1; break;
        case '*': x = 2; break;
        case '/': x = 3; break;
        case '%': x = 4; break;
        case '(': x = 5; break;
        case ')': x = 6; break;
        case ';': x = 7; break;
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
        default: break;
    }
    return PriorityMatrix[y][x];
}

int ScannerFurtherForNum(){
    int num_of_num = 0;
    while(*pmove_exp != ')'){
        if(*pmove_exp == ' ' || *pmove_exp == '\n' || *pmove_exp == '\t' || *pmove_exp == '\r') pmove_exp++;
        if(isdigit(*pmove_exp)){
            while(isdigit(*pmove_exp) || isalpha(*pmove_exp)){
                pmove_exp++;
            }
            num_of_num++;
        }
        else pmove_exp++;
    }
    return num_of_num;
}

int ScannerFurtherForOp(){
    int num_of_op = 0;
    while(*pmove_exp == '*' || *pmove_exp == '/' || *pmove_exp == '%' || *pmove_exp == ' ' || *pmove_exp == '+' || *pmove_exp == '-' || *pmove_exp == ')'){
        if(*pmove_exp == ' '){
            pmove_exp++;
        }
        else{
            num_of_op++;
            pmove_exp++;
        }
    }
    return num_of_op;
}

int ScannerFurtherForMinusNum(){
    pmove_exp++;
    while(*pmove_exp == ' ') pmove_exp++;
    pmove = pmove_exp;
    return 1;
}

int ScannerFutherForLBar(){
    char* pp = pmove;
    while(*pp == ' ')pp++;
    if(*pp == '(') return 1;
    else return 0;
}

void isExp(){
    int flag = 1;
    int is_Minus = 0;
    int p;
    int symbol_last;
    getsym();
    while(symbol != 18){
        if(symbol == 10){
            if(ScannerFutherForLBar() == 1){
                isLegal = 1;
                break;
            }
            else{
                getValue();
                if(is_Minus == 1) Value *= -1;
                numStack_push(Value);
                is_Minus = 0;
            }
        }
        else if((symbol >= 19 && symbol <= 23) || symbol == 14 || symbol == 15){
            if(symbol == 21 || symbol == 22 || symbol == 23){
                int tt = ScannerFurtherForOp();
                if(tt != 1){
                    isLegal = 1;
                    break;
                }
            }
            if(symbol == 20 && (symbol_last == 14 || (optrStack_top() == ';' && top_n == -1))){
                is_Minus = 1;
                getsym();
                continue;
            }
            int j = getPriority(token[0], optrStack_top());
            if(j == -1){
                optrStack_push(token[0]);
            }
            else if(j == 1){
                while((p = getPriority(token[0], optrStack_top())) == 1){
                    int f = calculate();
                    if(f == 1) break;
                }
                if(p == -2 && optrStack_top() != ';'){
                    isLegal = 1;
                    break;
                }
                if(optrStack_top() == '('){
                    optrStack_pop();
                }
                if(token[0] != ')'){
                    optrStack_push(token[0]);
                }
            }
            else if(j == 0){
                optrStack_pop();
            }
            else if(j == -2){
                isLegal = 1;
                break;
            }
        }
        else{
            isLegal = 1;
            break;
        }
        symbol_last = symbol;
        getsym();
    }
    
    if(symbol_last != 10 && symbol_last != 15) isLegal = 1;
    else{
        while(getPriority(token[0], optrStack_top()) == 1){
            calculate();
        }
        if(top_n == 0 && top_o == 0 && optrStack_top() == ';'){
            pmove = pmove_for_semi;
            isSemicolon();   
        }
        else{
            isLegal = 1;
        }
    }
}

void isSemicolon(){
    getsym();
    if(symbol == 18){
        isRBrace();
    }
    else isLegal = 1;
}

void isRBrace(){
    getsym();
    if(symbol == 17){
        isFinished();
    }
    else isLegal = 1;
}

void isFinished(){
    if(*pmove == '\r' || *pmove == '\n' || *pmove == '\n' || *pmove == '\t') return;
    else{
        getsym();
        if(symbol == -2) return;
        else isLegal = 1;
    }
}

void PrintCode(){
    if(top_n != -1) returnValue = numStack_top();
    printf("define dso_local i32 @main(){\n");
    printf("    ret i32 %d\n", returnValue);
    printf("}");
}

int main(){
    int i = 0;
    char c;
    while((c = getchar()) != EOF){
        content[i++] = c;
    }
    strcat(content, "#");
    init();
    isInt();
    if(isLegal == 1) return 1;
    else{
        PrintCode();
        return 0;
    }
}
