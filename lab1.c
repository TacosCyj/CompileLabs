#include<stdio.h>
#include<stdlib.h>
#include<string.h>
#include<ctype.h>
#define LEN 1024

char input_file[LEN];
char output_file[LEN];
char buffer[LEN];
char content[LEN];
char result[LEN];
char token[20];
FILE* fp_r, *fp_w;

int symbol;
char* pmove;

void IdentAnalysis();
void LBarAnalysis();
void RBarAnalysis();
void LBraceAnalysis();
void StmtAnalysis();
void RBraceAnalysis();
void AnalysisBegin();

void error(){
    exit(1);
}

void init(){
    pmove = content;
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
    if((c >= 'A' && c <= 'F')|| (c >= 'a' && c <= 'f')) return 1;
    else return 0;
}

int isdigit_Oct(char c){
    if((c >= '0' && c <= '7')) return 1;
    else return 0;
}
int isLegalHex(){
    for(int i = 2; i < strlen(token); i++){
        if(isdigit(token[i]) == 0 && isalpha_Hex(token[i]) == 0) return 1;
    }
    return 0;
}

int isLegalOct(){
    for(int i = 1; i < strlen(token); i++){
        if(isdigit(token[i]) == 0 || isdigit_Oct(token[i]) == 0) return 1;
    }
    return 0;
}

void CheckNumber(){
    char* pp = token;
    int flag;
    if(*pp == '0'){
        pp++;
        if(*pp == 'X' || *pp == 'x'){
            flag = isLegalHex();
            if(flag == 0) HexToDec();
            else error();
        }
        else{
            flag = isLegalOct();
            if(flag == 0) OctToDec();
            else error();
        }
    }
    else{
        for(int i = 1; i < strlen(token); i++){
            if(isdigit(token[i]) == 0) error();
        }
    }
}

void getsym(){
    int loc = 0;
    symbol = -1;
    memset(token, 0, sizeof(token));
    while(*pmove == ' ' || *pmove == '\t' || *pmove == '\n') pmove++;
    if(*pmove == '#'){
        /*symbol = -2, 表示输入文件结束*/
        symbol = -2;
    }
    else if(*pmove == '/'){
        int flag = 0;
        char* pp = pmove + 1;
        if(*pp == '/'){
            while(*pp != '\n'){
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
                    error();
                }
            }
            pmove = pp + 1;
            getsym();
        }
        else symbol = -1;
    }
    else if(isdigit(*pmove)){
        while(isdigit(*pmove) || (loc == 1 && (*pmove == 'x' || *pmove == 'X')) || (loc > 1 && isalpha(*pmove))){
            token[loc++] = *pmove;
            pmove++;
            if(*pmove == '#') break;
        }
        CheckNumber();
        symbol = 10;
    }
    else if(isalpha(*pmove)){
        while(isalpha(*pmove) || isdigit(*pmove) || *pmove == '_'){
            token[loc++] = *pmove;
            pmove++;
            if(*pmove == '#') break;
        }
        if(strcmp(token, "int") == 0) symbol = 11;
        else if(strcmp(token, "main") == 0) symbol = 12;
        else if(strcmp(token, "return") == 0) symbol = 13;
        else symbol = -1;
    }
    else{
        switch(*pmove){
            case '(': token[loc] = '('; symbol = 14; break;
            case ')': token[loc] = ')'; symbol = 15; break;
            case '{': token[loc] = '{'; symbol = 16; break;
            case '}': token[loc] = '}'; symbol = 17; break;
            case ';': token[loc] = ';'; symbol = 18; break;
            default: symbol = -1; break;
        }
        pmove++;
    }
}

/*检查main*/
void FuncTypeAnalysis(){
    strcat(result, "define dso_local i32 ");
    getsym();
    if(symbol == 12) IdentAnalysis();
    else error();
}

/*检查(*/
void IdentAnalysis(){
    strcat(result, "@main");
    getsym();
    if(symbol == 14) LBarAnalysis();
    else error();
}

/*检查)*/
void LBarAnalysis(){
    strcat(result, "(");
    getsym();
    if(symbol == 15) RBarAnalysis();
    else error();
}

void RBarAnalysis(){
    strcat(result, ")");
    getsym();
    if(symbol == 16) LBraceAnalysis();
    else error();
}

void LBraceAnalysis(){
    strcat(result, "{\n");
    getsym();
    if(symbol == 13) StmtAnalysis();
    else error();
}

void StmtAnalysis(){
    strcat(result, "    ret ");
    getsym();
    if(symbol == 10){
        strcat(result, "i32 ");
        strcat(result, token);
        getsym();
        if(symbol != 18) error();
        else{
            strcat(result, "\n");
            getsym();
            if(symbol == 17) RBraceAnalysis();
            else error();
        }
    }
    else error();
}

void RBraceAnalysis(){
    strcat(result, "}");
    getsym();
    if(symbol == -2) return;
    else error();
}

void AnalysisBegin(){
    FuncTypeAnalysis();
}

int main(int argv, char* argc[]){
    int i;
    if(argv == 3){
        strcpy(input_file, argc[1]);
        strcpy(output_file, argc[2]);
        fp_r = fopen(input_file, "r");
        fp_w = fopen(output_file, "w");
        while(fgets(buffer, LEN, fp_r)){
            strcat(content, buffer);
        }
        strcat(content, "#");
        init();
        getsym();
        if(symbol != 11) error();
        else AnalysisBegin();
        fwrite(result, strlen(result), 1, fp_w);
        fclose(fp_r);
        fclose(fp_w);
    }
    else{
        char c;
        int s = 0;
        while((c = getchar()) != EOF){
            content[s++] = c;
        }
        strcat(content, "#");
        init();
        getsym();
        if(symbol != 11) error();
        else AnalysisBegin();
        printf("%s", result);
    }
    return 0;
}
