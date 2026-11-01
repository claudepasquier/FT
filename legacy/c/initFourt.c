#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>
#include "initFourt.h"
#include "common.h"

/*
   This program is part of the application "FOURT"
  
   This simple fonction read the parameters sended by the java program,
   write them in a file (the name of the file is randomly selected),
   and return its name to the java program
*/

int main(int argc, char *argv[])
{
    char name[9];
    char dataPath[100];
    char logPath[100];
    int c;
    FILE *logFile; /* logfile */
    FILE* fileOut; /* outfile */

    srand( time(0) ); /* initialisation of the random number generator */
    /* open the logFile and write the user info */

    strcpy(logPath, logDirectory);
    strcat(logPath, logFileName);
    logFile = fopen(logPath, "a");
    /* if logfile == NULL, we don't print any information in the log file */
    c = getchar();
    if (c != '*') {
        if (logFile) fclose(logFile);
        return -1;
    }
    do
    {
        c = getchar();
        if (c != '\n' && logFile) fputc(c, logFile);
    }
    while( c != '\n');
    if (logFile) fclose(logFile);

    /* try to create a new empty file */
    do
    {
        randomName( name, 8);
        name[0]='D';
        name[8]='\0';
        strcpy(dataPath, logDirectory);
        strcat(dataPath, name);
	fileOut = fopen(dataPath, "a");
    }
    while ( fileOut == NULL );

    /* read all the parameters and write them in the file */
    while( ( c = getchar()) != EOF ) fputc(c, fileOut);
    fclose(fileOut);

    /* write the name of the file in the log file */
    logFile = fopen(logPath, "a");
    fprintf(logFile, " (%s)\n", name);
    fclose(logFile);

    /* return the name of the data file to the java program */

    /* standard header needed by the browser */
    printf("Content-type: text/html\n");
    printf("Pragma: no-cache\n\n");
    printf("%s\n", name);
}

void randomName( char* name, int length )
/* return a random name */
{
    int i; /* counter */
    for( i=0; i < length ; i++ )
    {
        name[i]=rand()%26+'A';
    }
}
