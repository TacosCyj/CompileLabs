#include<stdio.h>
#include<stdlib.h>
#include<string.h>
#include<ctype.h>
#define LEN 1024

char content[LEN];
char token[LEN];

char* pmove;
static int symbol;
static int isLegal;
static int returnValue;
static int numOfLBar;
static int numOfRBar;
static int numOfMinus;
static int numOfPlus;

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
        else symbol = -1;
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
            case ';': token[0] = ';'; symbol = 18; break;
            case '+': token[0] = '+'; symbol = 19; break;
            case '-': token[0] = '-'; symbol = 20; break;
            default: symbol = -1; break;
        }
        pmove++;
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

void isReturn(){
    getsym();
    if(symbol == 13){
        isLeftExp();
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
    //getsym();
    getReturnValue();
       //isSemicolon();
    isRightExp();
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