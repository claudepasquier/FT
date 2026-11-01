#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "showFourt.h"
#include "common.h"
//#include "statistics.h"
//#include "statistics.c"

  float tr[4097]={0}; /* the real part of the data */
  float ti[4097]={0}; /* the imaginary part of the data */

    char dataPath[100];
    char* cl = getenv("QUERY_STRING");
    char progName[9];
    char displayMode[1];
    char residueCode[1];
    char seqTitle[1000];
    int c;
//    char code3[4];
    int HTMLDisplay;
    int use3LetterCode;
    int nbLine;
    int  prob_nb;
    FILE* fileIn;
    char sfix[4097] = {'\32'}; /* sfix will contains the sequence */
    int ns = 0; /* ns contains lenth of the sequence */
    int nres1, nres2; /* nres1 & nres2 identifie the part of the sequence */
    int nfold;        /* nfold contains the power of 2 of the sequence */
    float fsqmin;     /* fsqmin is the cutoff */
    float minPeriod, maxPeriod;
    int kf1, kf2; /* kf1 & kf2 are the orders of the fourier transform */
    int nres;         /* nres is the length of string we are interested in */
    char asel[26];    /* asel is the array of selected letters */
    float value[26];  /* value is the array of the weight of the letters */
    char probsel[20][26];  //memorisation of the selecfion for several problems
    float probval[20][26];
    int nbAA; /* the number of selected Amino Acids */
    int lineLength = 0;
    int nbResidue = 0;
    int anin;
    float anin1;
    float fac;
    int nnorm; /* nnorm is the min */
    char seq[4097];
    float fseq[4097];
    char selseq[4097];
    float scale;
    int nsel;
    float vseq[4097];
    float anseq[4097];
    int kb1;
    int kb2;
    int annorm;
    float v1;
    float v2;
    float vav1;
    float vav2;
    float vrms;
    int inv;
    float frms;
    int ilast;
    int iwrite;
    int k;
    float theta;
    int ix2;
    float a,b,r;
    float fint2[2048];
    float pper2[2048];
    int nbData;
    char probName[20][12]; // maximum of 10 problem with a name less than 12 characters
    int nbProb;

int main(int argc, char *argv[])
{
  int result;
  result = init();
  if ( result == -1 ) return -1;
}

int init()
{
int i,j;
    for ( i = 0 ; i < 8 ; i++) {
        progName[i] = cl[i];
/*        progName[i]=arg[i]; */
    }
    progName[8] = '\0';
    prob_nb = (cl[8] - '0')*100 + (cl[9] - '0')*10 + (cl[10] - '0');
//    cout << "before opening the file" << flush;  
    /* open the file */
    strcpy(dataPath, logDirectory);
    strcat(dataPath, progName);
    fileIn = fopen(dataPath, "r");
        
    i = 0;
    do
    {
        c = fgetc(fileIn);
        seqTitle[i++] = c;       
    }
    while( c != 0);

        
    /*  read the sequence on the file */

    
    fscanf( fileIn, "%s", sfix+1);

    /* reqd the other parameters */
    fscanf( fileIn, "%d %d %d", &nres1, &nres2, &nfold);

    fscanf( fileIn, "%f %f %f", &fsqmin, &minPeriod, &maxPeriod);


    /* shift the elements in the array */
    /* in order that the first element of the selected sequence */
    /* be stored at the index 1. */

    if (nres1 > 1) {
        for (i = nres1 ; i <= nres2+1 ; i++ ) {
            sfix[i-nres1+1] = sfix[i];
        }
    }
    while ( sfix[ns] != '\x0' && ns < 4097 ) ns++; 


    kf1 = ceil(nfold / maxPeriod);
    kf2 = nfold / minPeriod;
    nres = nres2 - nres1 + 1;

    /* read the display mode */
    /* 'H' = HTML, T = plain text */
    fscanf( fileIn, "%s", &displayMode);
    if (displayMode[0] == 'H' )
        HTMLDisplay = 1;
    else HTMLDisplay = 0;

    /* read the code to use to print the residues */
    /* '1' = 1-letter code, 3 = 3-letter code */
    fscanf( fileIn, "%s", &residueCode);
    if (residueCode[0] == '3' )
        use3LetterCode = 1;
    else use3LetterCode = 0;

    // read the different problems
    fscanf( fileIn, "%d", &nbProb );
    for (j=0 ; j < nbProb ; j++ )
    {
        fscanf( fileIn, "%s", probName[j] );
        /* read the number of selected Amino-Acids */
        fscanf( fileIn, "%d", &nbAA );
        for ( i=1 ; i <= nbAA ; i++ ) {
        	char c[10];
        	float f;
            fscanf( fileIn, "%s %f", c, &f);
            probsel[j][i] = c[0];
            probval[j][i] = f;
            if (j == prob_nb)
            {
            	asel[i] = c[0];
            	value[i] = f;
            }
        }
    }
    fclose(fileIn);

    /* print the header of the HTML file */
    /* standard header needed by the browser */
    printf("Content-type: text/html\n");
    printf("Pragma: no-cache\n\n");
    printf("<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 3.2//EN'>\n");
    printf("<HTML>\n");
    printf("  <HEAD>\n");
    printf("    <TITLE>Results</TITLE>\n");
    printf("  </HEAD>\n");
    if (HTMLDisplay) {
        printf("  <BODY BACKGROUND='http://o2.db.uoa.gr/FT/bkg.gif'>\n");
        printf("    <TABLE WIDTH='100%%'>\n");
        printf("      <TR>\n");
        printf("        <TD ALIGN = 'LEFT'>\n");
        printf("          <I><BR>Study of Residue Periodicities in Sequences</I>\n");
        printf("        </TD>\n");
        printf("        <TD ALIGN='RIGHT'>\n");
        printf("          <I>Pasquier, C.M., Promponas, V.I.,<BR>Varvayannis, N.J., and Hamodrakas S.J.</I>\n");
        printf("        </TD>\n");
        printf("      </TR>\n");
        printf("    </TABLE>\n");
        printf("    <HR>\n");
        // display the buttons that link on the page
        printf("<TABLE BORDER=0>\n");
        printf("<TR>\n");
        printf("<TD><A HREF='#table'><IMG BORDER=0 SRC='http://o2.db.uoa.gr/FT/bt_table.gif'></A></TD>\n");
        printf("<TD>View table of results</TD><TD WIDTH=15></TD>\n");
        printf("<TD><A HREF='#graph'><IMG BORDER=0 SRC='http://o2.db.uoa.gr/FT/bt_graph.gif'></A></TD>\n");
        printf("<TD>View graph</TD>\n");
        printf("</TR>\n");
        printf("</TABLE>\n");
        printf("<HR>\n");

        // display the buttons that link on other problems
        if (nbProb > 1 )
        {
            printf("<P>Search the periodicities for the residues:\n");
            printf("<FONT SIZE=+2>");
            for (i=0 ; i < nbProb ; i++)
            {
                if (i == prob_nb)
                {
                    printf("&nbsp;&nbsp;%s",probName[i]);
                }
                else
                {
                    printf("&nbsp;&nbsp;<A HREF='showFourt?%s%03d'>%s</A>",progName, i,probName[i]);
                }

            }
            printf("<FONT SIZE=+0>");
            printf("</P>\n");
        }

        printf("<BR>\n");
    }
    else {
        printf("  <BODY>\n");
        printf("<PRE>\n");
        printf("Result of the program\n");
        printf("'Identification of Residue Periodicities in Sequences'\n");
        printf("\n\n");
        printf("------------------------------------------------------\n");
        printf("Pasquier, C.M., Promponas, V.I.,\n");
        printf("Varvayannis, N.J. and Hamodrakas\n");
        printf("------------------------------------------------------\n");
    }
    if (HTMLDisplay)
    {
        if (seqTitle[0] != '\0') {
             printf("<H2>Query data for:</H2>\n");
             printf("<H2><PRE>%s</PRE></H2>", seqTitle);
        }
        else printf("<H2>Query data </H2>\n");
        displayOneProblem();
    }
    else
    {
        for (i=0 ; i < nbProb ; i++ )
        {
            printf("\n\n");
            if (seqTitle[0] != '\0') {
                 printf("QUERY DATA FOR: \n");
                 printf("%s\n\n", seqTitle);
            }
            else printf("QUERY DATA\n\n");
            for ( j=1 ; j <= nbAA ; j++ )
            {
               	asel[j] = probsel[i][j];
            	value[j] = probval[i][j];
            }
            displayOneProblem();
        }
        printf("</PRE>\n");
    }
    printf("</BODY>\n</HTML>\n");
    return 1;
}


void displayOneProblem()
{
    int i,j;
    if (HTMLDisplay)
    {
        printf("<P><B>Selected residues=");
        for( i=1 ; i <= nbAA ; i++ ) printf("(%c %.1f) ", asel[i], value[i]);
        printf("<P><B>Appearance of the selected residues:</B></P>\n");
        printf("<PRE>\n");
    }
    else
    {
        printf("Selected residues=");
        for( i=1 ; i <= nbAA ; i++ ) printf("(%c %.1f) ", asel[i], value[i]);
        printf("\n\nAppearance of the selected residues:\n");
    }
    if (use3LetterCode) {
        printf("      1   2   3   4   5   6   7   8   9   0\n");
    }
    else {
        printf("              1         2         3         4\n");
        printf("     1234567890123456789012345678901234567890\n");
    }
    printf("0000 ");

    nbLine = 0;
    for ( i = 1 ; i <= nres ; i++ ) {
        int residueSelected = 0;
        for ( j = 1 ; j <= nbAA ; j++ ) {
            if (sfix[i] == asel[j] ) {
                residueSelected = 1;
                break;
            }
        }
        if ( residueSelected ) {
            if (use3LetterCode) {
                 printf("%s ", convert1To3(sfix[i]));
            }
            else printf("%c", sfix[i]);
            nbResidue++;
        }
        else {
            if (use3LetterCode) {
                 printf("... ");
            }
            else printf(".");
        }
        lineLength++;
        if ( ( use3LetterCode && (lineLength == 10) ) || (lineLength == 40) ) {
            printf("\n");
            nbLine+=1;
            printf("%04d ", nbLine*lineLength);
            lineLength = 0;
        }
    }
    if (lineLength != 0) printf("\n");
    if (HTMLDisplay) {
        printf("</PRE>\n");
        printf("<P><B>Total appearance of the requested residues: %d</B></P>\n", nbResidue);
        printf("<P><B>Start residue=%d,  End Residue=%d</B></P>\n", nres1, nres2);
        printf("<P><B>Embedment size=%d</B></P>\n", nfold);
        printf("<P><B>Minimum intensity=%.1f</B></P>\n", fsqmin);
        printf("<P><B>Min period=%.1f, Max Period=%.1f</B></P>\n", minPeriod, maxPeriod);
//        printf("<P><B>Selected residues=");
//        for( i=1 ; i <= nbAA ; i++ ) printf("(%c %.1f) ", asel[i], value[i]);
        printf("</B></P>\n");
    }
    else {
        printf("\nTotal appearance of the requested residues: %d\n\n", nbResidue);
        printf("Start residue=%d,  End Residue=%d\n\n", nres1, nres2);
        printf("Embedment size=%d\n\n", nfold);
        printf("Minimum intensity=%.1f\n\n", fsqmin);
        printf("Min period=%.1f, Max Period=%.1f\n\n", minPeriod, maxPeriod);
//        printf("Selected residues=");
//        for( i=1 ; i <= nbAA ; i++ ) printf("(%c %.1f) ", asel[i], value[i]);
        printf("\n\n");
    }
    /* transformation of the parameters */
//    printStat(sfix+1,ns);
    anin = nfold;
    anin1 = anin - 1;
    fac = anin / 360;
  
    for ( i = 1 ; i <= ns ; i++ ) seq[i] = sfix[i];
    for ( i = 1 ; i <= nres ; i++ ) {
        fseq[i] = 0;
        selseq[i]='.';
    }

    scale = 0;
    nsel = 1;
    while ( asel[nsel] != '\x0' ) nsel++;  
    for ( j = 1 ; j <= nres ; j++ ) {
        char as = seq[j];
        for ( i=1 ; i <= nsel ; i++ ) {
            if ( as == asel[i] ) {
                fseq[j] = value[i];
                selseq[j] = as;
                break;
            }
        }
    }
    nnorm = ( nfold < nres ? nfold : nres );

    for ( j=1 ; j <= nfold ; j++ ) {
        vseq[j] = 0;
        anseq[j] = 0;
    }

    kb1 = 1;
    kb2 = 0;
    do {
        int kbx = kb1;
        int kby = ( (kb2+nfold) < ns ? kb2+nfold : ns ) - 1;
        for ( j = kbx ; j <= kby ; j++ ) {
            int j1 = j - kb2;
            vseq[j1] = vseq[j1] + fseq[j];
            anseq[j1] = anseq[j1] + 1;
        }

        kb1 = kb1 + nfold;
        kb2 = kb2 + nfold;
    } while ( kb1 < nres );

    if ( nfold >= nres ) {
        for ( j = 1 ; j <= nfold ; j++ ) {
            anseq[j] = 1;
        }
    }


//     // as nfold is always greater than nres,
//     // we can simplify the lines above with :
//     for (j = 1 ; i <= nfold ; i++ ) {
//       vseq[j] = fseq[j];
//       anseq[j] = 1;
//     }


    annorm = nnorm;
    v1 = 0;
    v2 = 0;
    for ( i = 1 ; i <= nnorm ; i++ ) {
        float vs = vseq[i] / anseq[i];
        fseq[i] = vs;
        v1 = v1 + vs;
        v2 = v2 + vs*vs;
    }
  
    vav1 = v1 / annorm;
    vav2 = v2 / annorm;
    vrms = sqrt(vav2-vav1*vav1);
  
    for ( j = 1 ; j <= nnorm ; j++ ) {
        fseq[j] = ( fseq[j] - vav1 ) / vrms;
    }

    for ( j = 1 ; j <= nfold ; j++ ) {
        tr[j] = 0;
        ti[j] = 0;
    }

    for ( j = 1 ; j <= nnorm ; j++ ) {
        tr[j] = fseq[j];
    }

    inv = 2;

    four_trans( nfold, inv, tr, ti );

    frms = sqrt( annorm / anin1 );
    scale = 1 / ( frms * sqrt( anin ) );
  
    kf1 = kf1 + 1;
    kf2 = kf2 + 1;
 
    /* here is the initialisation of SINGUL, NUMSIM, CUT, ISHORT */

    ilast = 0;
    iwrite = 1;
  
    theta = 0;
    ix2 = 0;

    if (HTMLDisplay) {
        printf("<HR><H2><A NAME='table'>Results</A></H2>");
        printf("<TABLE BORDER=1>");
        printf("<TR ALIGN=CENTER><TD>n</TD>\n");
        printf("<TD>a</TD>\n");
        printf("<TD>b</TD>\n");
        printf("<TD>r</TD>\n");
        printf("<TD>theta</TD>\n");
        printf("<TD>origin</TD>\n");
        printf("<TD>period</TD>\n");
        printf("<TD>intensity</TD></TR>\n");
    }
    else {
        printf("RESULTS\n\n");
        printf("----------------------------------------------------------------------\n");
        printf("  n      a        b        r       theta    origin   period  intensity\n");
        printf("----------------------------------------------------------------------\n");
    }

/*    printf("*****(%d, %d)\n", kf1, kf2);*/
    for ( k=kf1 ; k <= kf2 ; k++ ) {
	int k1;
        int ak;
        float ak1;
        float orign;
        float per;
        float fint;
        a = tr[k];
        b = ti[k];
        r = sqrt( a*a + b*b );
        if ( r != 0 ) theta = 180 * atan2( b, a ) / M_PI;
        else theta = 0;

        ak = k - 1;
        ak1 = ak;
        orign = -theta * fac / ak1 + 1;
        per = anin / ak1;
        a = a * scale;
        b = b * scale;
        r = r * scale;
        fint = r*r;
        k1 = k - 1;
        ilast = iwrite;
        ix2 = ix2 + 1;
        fint2[ix2] = fint;
        pper2[ix2] = per;
        if ( fint >= fsqmin ) {
            iwrite = 1;
            if (HTMLDisplay) {
                printf("<TR ALIGN=RIGHT><TD>%d</TD>", k1);
                printf("<TD>%.3f</TD>", a);
                printf("<TD>%.3f</TD>", b);
                printf("<TD>%.3f</TD>", r);
                printf("<TD>%.3f</TD>", theta);
                printf("<TD>%.3f</TD>", orign);
                printf("<TD>%.3f</TD>", per);
                printf("<TD>%.3f</TD></TR>\n", fint);
            }
            else {
                printf(" %.3d ", k1);
                printf(" %7.3f ", a);
                printf(" %7.3f ", b);
                printf(" %7.3f ", r);
                printf(" %8.3f ", theta);
                printf(" %7.3f ", orign);
                printf(" %7.3f ", per);
                printf(" %7.3f\n", fint);
            }
        }
    }
    if (HTMLDisplay) printf("</TABLE>");
//    else printf("</PRE>\n");

    if (HTMLDisplay) {
        /* write the parameters for the Chart Applet */
        printf("<HR><H2><A NAME='graph'>Graphical representation</A></H2>\n");
        printf("<APPLET CODE=Chart.class CODEBASE='http://o2.db.uoa.gr/FT' WIDTH=100%% HEIGHT=550>\n");
        printf("<PARAM NAME=cutoff VALUE=%.3f>\n", fsqmin);
    
        nbData = 0;
        for (i=1 ; i <= ix2 ; i++ ) {
            if (fint2[i] >=fsqmin) {
                nbData++;
                printf("<PARAM NAME=p%d VALUE=%.2f>\n", nbData, pper2[i]);
                printf("<PARAM NAME=i%d VALUE=%.2f>\n", nbData, fint2[i]);
            }
        }

        printf("<PARAM NAME=nbdata VALUE=%d>\n", nbData);
        printf("</APPLET>\n");
    }
}

char* convert1To3(char c) {
        if (c == 'F') return "PHE";
        if (c == 'L') return "LEU";
        if (c == 'I') return "ILE";
        if (c == 'M') return "MET";
        if (c == 'V') return "VAL";
        if (c == 'S') return "SER";
        if (c == 'P') return "PRO";
        if (c == 'T') return "THR";
        if (c == 'A') return "ALA";
        if (c == 'Y') return "TYR";
        if (c == 'H') return "HIS";
        if (c == 'Q') return "GLN";
        if (c == 'N') return "ASN";
        if (c == 'K') return "LYS";
        if (c == 'D') return "ASP";
        if (c == 'E') return "GLU";
        if (c == 'C') return "CYS";
        if (c == 'W') return "TRP";
        if (c == 'R') return "ARG";
        if (c == 'G') return "GLY";
        if (c == 'a') return "  a";
        if (c == 't') return "  t";
        if (c == 'c') return "  c";
        if (c == 'g') return "  g";
        return "UNK"; /* Unknown */
}

int four_trans(int it, int inv, float tr[20], float ti[20])
{
    float ur[16];
    float ui[16];
    float nbr = M_PI;
    int i, j, k;
    int to;
    float um;
    int io;
    int ii;
    int i1;
    int i3;

    /* initialization of the ur and ui arrays */
    for( i=1 ; i<=15 ; i++ ) {
        nbr = nbr / 2;
        ur[i] = cos( nbr );
        ui[i] = sin( nbr );
    }

    /* check that it is a power of 2 */
    to = 2;
    for ( i=2 ; i <= 16 ; i++) {
        to = to*2;
        if ( to > it ) return -1;
        if ( to == it ) break;
    }
    if ( to != it ) return -1;

    if ( inv == 1 ) um = -1;
    else um = 1;

    io = i;
    ii = io;
    i1 = it / 2;
    i3 = 1;

    do {
        int k = 0;
        int i2 = i1 * 2;
        int j = 0;
        do {
            float wr = 1;
            float wi = 0;
            int kk = k;
            int jo = io;
            int kk1 = 0;
            while ( kk != 0 ) {
                float ws;
                do {
                    jo = jo - 1;
                    kk1 = kk;
                    kk = kk / 2;
                } while ( kk1 == kk * 2 );
                ws = wr * ur[jo] - wi*ui[jo];
                wi = wr * ui[jo] + wi*ur[jo];
                wr = ws;
            }

            wi = wi * um;
            j = 0;
  
            do {
                int l = j * i2 + k;
                int l1 = l + i1;
                float zr = tr[l+1] + tr[l1+1];
                float zi = ti[l+1] + ti[l1+1];
                float z = wr*(tr[l+1] - tr[l1+1]) - wi*(ti[l+1] - ti[l1+1]);
                ti[l1+1] = wr*(ti[l+1] - ti[l1+1]) + wi*(tr[l+1] - tr[l1+1]);
                tr[l+1]  = zr;
                tr[l1+1] = z;
                ti[l+1] = zi;
                j += 1;
            } while ( j < i3 );
  
            k += 1;
        } while ( k < i1 );

        i3 = i3 * 2;
        io -= 1;
        i1 = i1 / 2;
    } while ( i1 > 0 );

    j = 1;
  
    if ( inv == 1 ) um = 1 / it;
    else um = 1;


    k = 0;
    do {
        int j1 = j;
        k = 0;
        for ( i = 1 ; i <= ii ; i++ ) {
            int j2 = j1 / 2;
            k = 2 * ( k - j2 ) + j1;
            j1 = j2;
        }

        if ( k == j ) {
            tr[j+1] = tr[j+1]*um;
            ti[j+1] = ti[j+1]*um;  
        }
        else if ( k > j ) {
            float zr = tr[j+1];
            float zi = ti[j+1];
            tr[j+1] = tr[k+1]*um;
            ti[j+1] = ti[k+1]*um;
            tr[k+1] = zr*um;
            ti[k+1] = zi*um;
        }

        j += 1;
    } while ( j - it + 1 < 0 );

    tr[1] = tr[1]*um;
    ti[1] = ti[1]*um;
    tr[it] = tr[it]*um;
    ti[it] = ti[it]*um;

    return 1;
}
