/*
 * fourt.c — numerical core of FOURT, extracted verbatim from the legacy
 * CGI program legacy/c/showFourt.c (Pasquier et al.).
 *
 * The pipeline (windowed averaging -> RMS normalisation -> FFT -> peak
 * extraction) preserves the original 1-based indexing and operation order
 * so results are numerically identical to the 1990s program.
 *
 * All printf/HTML/CGI concerns were removed; this file is pure computation.
 *
 * License: GPL-3.0-or-later.
 */
#include <math.h>
#include <string.h>
#include "fourt.h"

/* Legacy code used double-precision math (M_PI, sqrt, atan2) on float
 * operands; we keep the same promotions for bit-identical results. */
#define PI 3.14159265358979323846  /* matches legacy M_PI */

/* 1-based scratch buffers, as in the original program. */
static float tr[FOURT_MAX_SEQ + 1];   /* real part of the data            */
static float ti[FOURT_MAX_SEQ + 1];   /* imaginary part of the data       */
static char  seq1[FOURT_MAX_SEQ + 2]; /* sequence, 1-based                */
static float fseq[FOURT_MAX_SEQ + 1]; /* weighted signal                  */
static float vseq[FOURT_MAX_SEQ + 1]; /* window sums                      */
static float anseq[FOURT_MAX_SEQ + 1];/* window counts                    */

const char *fourt_version(void)
{
    return "1.0.0";
}

/*
 * Iterative radix-2 FFT — copied unchanged from legacy showFourt.c.
 * (Variable names kept for provenance; i/j/k counters, io/ii loop bounds.)
 */
int fourt_fft(int it, int inv, float *tr, float *ti)
{
    float ur[16];
    float ui[16];
    float nbr = PI;
    int i, j, k;
    int to;
    float um;
    int io;
    int ii;
    int i1;
    int i3;

    /* twiddle factors: cos/sin of halved angles */
    for (i = 1; i <= 15; i++) {
        nbr = nbr / 2;
        ur[i] = cos(nbr);
        ui[i] = sin(nbr);
    }

    /* check that it is a power of 2 */
    to = 2;
    for (i = 2; i <= 16; i++) {
        to = to * 2;
        if (to > it) return -1;
        if (to == it) break;
    }
    if (to != it) return -1;

    if (inv == 1) um = -1;
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
            while (kk != 0) {
                float ws;
                do {
                    jo = jo - 1;
                    kk1 = kk;
                    kk = kk / 2;
                } while (kk1 == kk * 2);
                ws = wr * ur[jo] - wi * ui[jo];
                wi = wr * ui[jo] + wi * ur[jo];
                wr = ws;
            }

            wi = wi * um;
            j = 0;

            do {
                int l = j * i2 + k;
                int l1 = l + i1;
                float zr = tr[l + 1] + tr[l1 + 1];
                float zi = ti[l + 1] + ti[l1 + 1];
                float z = wr * (tr[l + 1] - tr[l1 + 1]) - wi * (ti[l + 1] - ti[l1 + 1]);
                ti[l1 + 1] = wr * (ti[l + 1] - ti[l1 + 1]) + wi * (tr[l + 1] - tr[l1 + 1]);
                tr[l + 1] = zr;
                tr[l1 + 1] = z;
                ti[l + 1] = zi;
                j += 1;
            } while (j < i3);

            k += 1;
        } while (k < i1);

        i3 = i3 * 2;
        io -= 1;
        i1 = i1 / 2;
    } while (i1 > 0);

    /* bit-reversal permutation (and scaling for the inverse transform) */
    j = 1;

    if (inv == 1) um = 1 / (float)it;
    else um = 1;

    k = 0;
    do {
        int j1 = j;
        k = 0;
        for (i = 1; i <= ii; i++) {
            int j2 = j1 / 2;
            k = 2 * (k - j2) + j1;
            j1 = j2;
        }

        if (k == j) {
            tr[j + 1] = tr[j + 1] * um;
            ti[j + 1] = ti[j + 1] * um;
        } else if (k > j) {
            float zr = tr[j + 1];
            float zi = ti[j + 1];
            tr[j + 1] = tr[k + 1] * um;
            ti[j + 1] = ti[k + 1] * um;
            tr[k + 1] = zr * um;
            ti[k + 1] = zi * um;
        }

        j += 1;
    } while (j - it + 1 < 0);

    tr[1] = tr[1] * um;
    ti[1] = ti[1] * um;
    tr[it] = tr[it] * um;
    ti[it] = ti[it] * um;

    return 1;
}

int fourt_compute(const char *seq, int nres,
                  const char *asel, const float *value, int nsel,
                  int nfold, float minPeriod, float maxPeriod, float fsqmin,
                  FourtPeak *out, int outMax, int *outCount)
{
    int i, j;
    int kf1, kf2;         /* harmonic window from period range */
    float fac;
    int nnorm, annorm;
    float v1, v2, vav1, vav2, vrms;
    float frms, scale;
    int nbResidue = 0;
    float theta;
    int ix2;
    float a, b, r;

    if (outCount) *outCount = 0;
    if (!seq || !asel || !value || !out || !outCount) return FOURT_ERR_ARGS;
    if (nres <= 0 || nsel <= 0 || nsel > FOURT_MAX_SEL) return FOURT_ERR_ARGS;
    if (nres > FOURT_MAX_SEQ || nfold > FOURT_MAX_FOLD || nfold <= 0)
        return FOURT_ERR_TOOBIG;

    /* --- legacy setup: copy sequence into 1-based scratch ---------------- */
    strncpy(seq1 + 1, seq, (size_t)nres);
    seq1[nres + 1] = '\0';

    fac = (float)nfold / 360;

    for (j = 1; j <= nres; j++) {
        fseq[j] = 0;
    }
    for (j = 1; j <= nfold; j++) {
        vseq[j] = 0;
        anseq[j] = 0;
    }

    /* build the weighted signal (legacy lines 357-369) */
    for (j = 1; j <= nres; j++) {
        char as = seq1[j];
        for (i = 0; i < nsel; i++) {
            if (as == asel[i]) {
                fseq[j] = value[i];
                break;
            }
        }
    }
    /* count appearances of selected residues (legacy display block) */
    for (i = 1; i <= nres; i++) {
        for (j = 0; j < nsel; j++) {
            if (seq1[i] == asel[j]) { nbResidue++; break; }
        }
    }
    if (nbResidue == 0) return FOURT_ERR_NO_RESIDUE;

    nnorm = (nfold < nres) ? nfold : nres;

    /* windowed averaging over embedments of size nfold (legacy 377-396) */
    {
        int kb1 = 1;
        int kb2 = 0;
        do {
            int kbx = kb1;
            int kby = ((kb2 + nfold) < nres ? kb2 + nfold : nres) - 1;
            for (j = kbx; j <= kby; j++) {
                int j1 = j - kb2;
                vseq[j1] = vseq[j1] + fseq[j];
                anseq[j1] = anseq[j1] + 1;
            }
            kb1 = kb1 + nfold;
            kb2 = kb2 + nfold;
        } while (kb1 < nres);

        if (nfold >= nres) {
            for (j = 1; j <= nfold; j++) {
                anseq[j] = 1;
            }
        }
    }

    /* RMS normalisation over nnorm points (legacy 407-423) */
    annorm = nnorm;
    v1 = 0;
    v2 = 0;
    for (i = 1; i <= nnorm; i++) {
        float vs = vseq[i] / anseq[i];
        fseq[i] = vs;
        v1 = v1 + vs;
        v2 = v2 + vs * vs;
    }

    vav1 = v1 / annorm;
    vav2 = v2 / annorm;
    vrms = (float)sqrt((double)(vav2 - vav1 * vav1));
    /* Guard: vrms==0, or NaN (which happens in the legacy pipeline when
     * nres == nfold and the last point is never accumulated: 0/0).
     * The legacy code silently produced NaN rows; we reject instead. */
    if (!(vrms > 0)) return FOURT_ERR_DEGENERATE;

    for (j = 1; j <= nnorm; j++) {
        fseq[j] = (fseq[j] - vav1) / vrms;
    }

    /* zero-pad to nfold and transform (legacy 425-436) */
    for (j = 1; j <= nfold; j++) {
        tr[j] = 0;
        ti[j] = 0;
    }
    for (j = 1; j <= nnorm; j++) {
        tr[j] = fseq[j];
    }

    if (fourt_fft(nfold, 2, tr, ti) != 1) return FOURT_ERR_NOT_POW2;

    frms = (float)sqrt((double)annorm / (double)(nfold - 1));
    scale = 1 / (frms * (float)sqrt((double)nfold));

    kf1 = (int)ceil((double)nfold / (double)maxPeriod) + 1;  /* legacy 128 + 441 */
    kf2 = nfold / (int)minPeriod + 1;

    /* peak extraction (legacy 472-521) */
    ix2 = 0;
    for (int k = kf1; k <= kf2 && k <= nfold; k++) {
        float ak1;
        float orign;
        float per;
        float fint;
        a = tr[k];
        b = ti[k];
        r = (float)sqrt((double)(a * a + b * b));
        theta = (r != 0) ? (float)(180 * atan2((double)b, (double)a) / PI) : 0;

        ak1 = (float)(k - 1);
        orign = -theta * fac / ak1 + 1;
        per = (float)nfold / ak1;
        a = a * scale;
        b = b * scale;
        r = r * scale;
        fint = r * r;

        if (fint >= fsqmin) {
            if (ix2 >= outMax) return FOURT_ERR_OVERFLOW;
            out[ix2].n = k - 1;
            out[ix2].a = a;
            out[ix2].b = b;
            out[ix2].r = r;
            out[ix2].theta = theta;
            out[ix2].origin = orign;
            out[ix2].period = per;
            out[ix2].intensity = fint;
            ix2++;
        }
    }
    *outCount = ix2;
    return FOURT_OK;
}
