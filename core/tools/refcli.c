/*
 * refcli.c — command-line reference tool.
 *
 * Prints, in CSV, the peaks computed by fourt_compute for a sequence and
 * parameters given on the command line. Used to generate golden fixtures
 * and to cross-check the WASM build against native results.
 *
 * Usage: refcli <sequence> <asel> <weights-comma> <nfold> <minPeriod>
 *               <maxPeriod> <fsqmin>
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "fourt.h"

int main(int argc, char **argv)
{
    if (argc != 8) {
        fprintf(stderr,
                "usage: %s <seq> <asel> <w1,w2,..> <nfold> <minPeriod> <maxPeriod> <fsqmin>\n",
                argv[0]);
        return 2;
    }
    const char *seq = argv[1];
    const char *asel = argv[2];
    int nres = (int)strlen(seq);
    int nsel = (int)strlen(asel);
    float value[FOURT_MAX_SEL] = {0};
    char *tok = strtok(argv[3], ",");
    for (int i = 0; i < nsel && tok; i++, tok = strtok(NULL, ","))
        value[i] = (float)atof(tok);
    int nfold = atoi(argv[4]);
    float minP = (float)atof(argv[5]);
    float maxP = (float)atof(argv[6]);
    float fsqmin = (float)atof(argv[7]);

    FourtPeak peaks[FOURT_MAX_PEAKS];
    int n = 0;
    int rc = fourt_compute(seq, nres, asel, value, nsel, nfold,
                           minP, maxP, fsqmin, peaks, FOURT_MAX_PEAKS, &n);
    if (rc != FOURT_OK) {
        fprintf(stderr, "error %d\n", rc);
        return 1;
    }
    printf("n,a,b,r,theta,origin,period,intensity\n");
    for (int i = 0; i < n; i++)
        printf("%d,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f\n",
               peaks[i].n, peaks[i].a, peaks[i].b, peaks[i].r,
               peaks[i].theta, peaks[i].origin, peaks[i].period,
               peaks[i].intensity);
    return 0;
}
