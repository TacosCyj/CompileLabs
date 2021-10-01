#include<stdio.h>
#include<string.h>
#include<stdlib.h>
#include<ctype.h>
#define LEN 1024

char input_file[LEN];
char readfile[LEN];
char ans[LEN];
char* reserved[6] = {"If", "Else", "While", "Break", "Continue", "Return"};
FILE* fp_r;


int judgeIsReserve(char s[]){
	int len, i, j;
	char temp[15] = {0}, temp_reserve[15] = {0};
	len = strlen(s);
	for(i = 6, j = 0; i < len; i++, j++){
		temp[j] = s[i];
	}
	for(i = 0; i < 6; i++){
		strcpy(temp_reserve, reserved[i]);
		temp_reserve[0] = 'a' + (temp_reserve[0] - 'A');
		if(strcmp(temp_reserve, temp) == 0){
			return i;
		}
		memset(temp_reserve, 0, sizeof(temp_reserve));
	}
	return 6;
}

void lexicalAnalysis(){
	char* pmove = readfile, *pp;
	char ans_each[LEN], temp_judge[LEN];
	int len_each = 0, len_temp, flag;
	while(pmove != NULL){
		if(*pmove == '\n') break;
		if(*pmove == ' ' || *pmove == '\t') pmove++;
		else if(isdigit(*pmove)){
			strcpy(ans_each, "Number(");
			len_each = strlen(ans_each);
			while(pmove != NULL && isdigit(*pmove)){
				ans_each[len_each++] = *pmove;
				pmove++;
			}
			if(pmove == NULL){
				len_temp = strlen(ans_each);
				ans_each[len_temp++] = ')';
				ans_each[len_temp] = '\n';
				strcat(ans, ans_each);
				break;
			}
			else if(*pmove != ' ' && *pmove != '\n' && *pmove != '\t' && *pmove != ')' && *pmove != ';'){
				printf("Err");
				exit(0);
			}
			else{
				len_temp = strlen(ans_each);
				ans_each[len_temp++] = ')';
				ans_each[len_temp] = '\n';
				strcat(ans, ans_each);
				memset(ans_each, 0, sizeof(ans_each));
			}
		}
		else if(isalpha(*pmove) || *pmove == '_'){
			strcpy(ans_each, "Ident(");
			len_each = strlen(ans_each);
			while(pmove != NULL && (isalpha(*pmove) || *pmove == '_' || isdigit(*pmove))){
				ans_each[len_each++] = *pmove;
				pmove++;
			}
			if(pmove == NULL){
				flag = judgeIsReserve(ans_each);
				if(flag == 6){
					strcat(ans_each, ")");
					len_temp = strlen(ans_each);
					ans_each[len_temp] = '\n';
					strcat(ans, ans_each);
				}
				else{
					strcpy(temp_judge, reserved[flag]);
					temp_judge[strlen(temp_judge)] = '\n';
					strcat(ans, temp_judge);
				}	
				break;
			}
			else if(*pmove != ' ' && *pmove != '\n' && *pmove != '\t' && *pmove != ')' && *pmove != ';'){
				printf("Err");
				exit(0);
			}
			else{
				flag = judgeIsReserve(ans_each);
				if(flag == 6){
					strcat(ans_each, ")");
					len_temp = strlen(ans_each);
					ans_each[len_temp] = '\n';
					strcat(ans, ans_each);
				}
				else{
					strcpy(temp_judge, reserved[flag]);
					temp_judge[strlen(temp_judge)] = '\n';
					strcat(ans, temp_judge);
				}	
				memset(ans_each, 0, sizeof(ans_each));
				memset(temp_judge, 0, sizeof(temp_judge));
			}
		}
		else{
			if(*pmove == '='){
				char* pback = pmove;
				int n = 0;
				while(pmove != NULL && *pmove == '='){
					pback = pmove;
					pmove++;
					n++;
				}
				pmove = pback;
				if(n % 2 == 0){
					n /= 2;
					while(n--){
						strcat(ans_each, "Eq");
					}
				}
				else{
					n -= 1;
					n /= 2;
					while(n--){
						strcat(ans_each, "Eq");
					}
					strcat(ans_each, "Assign");
				}
			}
			else{
				switch(*pmove){
					case ';': strcpy(ans_each, "Semicolon"); break;
					case '(': strcpy(ans_each, "LPar"); break;
					case ')': strcpy(ans_each, "RPar"); break;
					case '{': strcpy(ans_each, "LBrace"); break;
					case '}': strcpy(ans_each, "RBrace"); break;
					case '+': strcpy(ans_each, "Plus"); break;
					case '*': strcpy(ans_each, "Mult"); break;
					case '/': strcpy(ans_each, "div"); break;
					case '<': strcpy(ans_each, "Lt"); break;
					case '>': strcpy(ans_each, "Gt"); break;
					default: printf("Err"); exit(0);
				}
			}
			if((pmove + 1) != NULL && strcmp(ans_each, "LPar") != 0){
				char *pcheck = pmove + 1;
				if(*pcheck != ' ' && *pcheck != '\t' && *pcheck != '\n'){
					printf("Err");
					exit(0);
				}
			}
			len_temp = strlen(ans_each);
			ans_each[len_temp] = '\n';
			strcat(ans, ans_each);
			memset(ans_each, 0, sizeof(ans_each));
			pmove++;
		}
	}
}

int main(int argc, char* argv[]){
	int i, j;
	if(argc > 1){
		strcpy(input_file, argv[1]);
	}
	else{
		gets(input_file);
	}
	fp_r = fopen(input_file, "r");
	while(fgets(readfile, LEN, fp_r)){
		lexicalAnalysis();
		printf("%s", ans);
		memset(ans, 0, sizeof(ans));
	}
	return 0;
} 
