#include <stdio.h>
#include <strings.h>
#include "statistics.h"
#include <iostream.h>

//int main(){
//char s[20000] = "KYKAISEELDHALNDMTSI";
//printStat(s, 8434);
//}

void printStat( char seq[4097], int length)
{
char residues[26] = "ACDEFGHIKLMNPQRSTVWXYacgt" ;
int resnb[25] = {0};
    
    //calculate the total appearance of each residue in the sequence
    for ( int i =0; i<length; i++)
    {
        char res = seq[i];
        int Index = index(residues , res) - residues;
        resnb[Index] ++ ;
    }
    float resPercent[25];
    //calculate the percentage appearance of each residue in the sequence
    for (i=0; i<25; i++)
    {
    resPercent[i] = (float)resnb[i]/( (float) length) *100 ;
//cout << resPercent[i] << "\n";
    }

//sorting the frequencies
    for ( i=0 ; i<25; i++){
        for (int j=0; j< 24-i; j++){
            if (resPercent[j]<resPercent[j+1] ) {
                float exchange = resPercent[j+1];
                resPercent[j+1] = resPercent[j];
                resPercent[j] = exchange;
                char change = residues[j+1];
                residues[j+1] = residues[j];
                residues[j] = change; 
               }
            }
    }
	if (resPercent[5] > 0) {
	//calculate sum3 and print the results in the result page of FT
  	  float sum3 = resPercent[0] + resPercent[1] + resPercent[2];
  	  int structPred = 0; //false at the beginning
  	  printf ( " <H3> <U>PREDICTION FOR FIBROUS/STRUCTURAL PROTEINS </U>based on composition analysis:</H3>" );
    
		if (sum3 >= 34  )  
        	structPred = 1;
        	if (structPred == 1) 
        	 printf("<H3>Predicted as a structural protein<BR>sum3= %4.2f%%</H3>\n" ,sum3);
        	else printf("<H3>Not Predicted structural</H3>\n");
	}
}
