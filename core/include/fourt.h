/*
 * fourt.h — public C ABI for the FOURT periodicity analysis core.
 *
 * Extracted from the original CGI program showFourt.c (Pasquier et al.)
 * so that the numerical pipeline can be compiled natively (tests) and to
 * WebAssembly (browser front-end).
 *
 * The internal computation deliberately keeps the original 1-based indexing
 * and operation order to guarantee results identical to the legacy program.
 *
 * License: GPL-3.0-or-later.
 */
#ifndef FOURT_H
#define FOURT_H

#ifdef __cplusplus
extern "C" {
#endif

#define FOURT_MAX_SEQ   4096  /* maximum sequence length (legacy cap)    */
#define FOURT_MAX_FOLD  4096  /* maximum embedment size (power of 2)     */
#define FOURT_MAX_SEL   25    /* maximum selected residues per problem   */
#define FOURT_MAX_PEAKS 4096  /* maximum result rows                     */

/* One harmonic (peak) of the Fourier spectrum. */
typedef struct {
    int   n;         /* harmonic order (k-1 in legacy output)            */
    float a;         /* real part (scaled)                               */
    float b;         /* imaginary part (scaled)                          */
    float r;         /* amplitude (scaled)                               */
    float theta;     /* phase in degrees                                 */
    float origin;    /* phase origin (residue position)                  */
    float period;    /* nfold / n                                        */
    float intensity; /* r*r                                              */
} FourtPeak;

/* Error codes returned by fourt_compute(). */
enum {
    FOURT_OK            =  0,
    FOURT_ERR_ARGS      = -1,  /* invalid arguments (null pointers, sizes)   */
    FOURT_ERR_NO_RESIDUE= -2,  /* none of the selected residues is present   */
    FOURT_ERR_DEGENERATE= -3,  /* zero variance (all weights identical)      */
    FOURT_ERR_NOT_POW2  = -4,  /* nfold is not a power of 2                  */
    FOURT_ERR_OVERFLOW  = -5,  /* output buffer too small                    */
    FOURT_ERR_TOOBIG    = -6   /* nres/nfold exceed FOURT_MAX_SEQ/FOLD       */
};

/*
 * Compute the Fourier periodicity analysis for one "problem"
 * (a residue selection with weights).
 *
 * seq       : sequence (1-letter codes), length nres, 0-based C string
 *             layout; internally copied to 1-based scratch arrays.
 * asel      : selected residue letters, length nsel (e.g. "KRDE").
 * value     : numeric weight for each selected residue.
 * nfold     : embedment size, must be a power of 2.
 * minPeriod/maxPeriod : period search window (2..nfold).
 * fsqmin    : minimum intensity cutoff for a peak to be reported.
 * out       : caller-allocated array of at least outMax peaks.
 * outCount  : receives the number of peaks written.
 *
 * Returns FOURT_OK or a negative error code.
 */
int fourt_compute(const char *seq, int nres,
                  const char *asel, const float *value, int nsel,
                  int nfold, float minPeriod, float maxPeriod, float fsqmin,
                  FourtPeak *out, int outMax, int *outCount);

/* Iterative radix-2 FFT extracted verbatim from the legacy program.
 * Exposed for unit testing. it must be a power of 2; inv==1 => inverse.
 * Arrays tr/ti must hold it+1 elements (1-based usage). */
int fourt_fft(int it, int inv, float *tr, float *ti);

/* Version string of the core. */
const char *fourt_version(void);

#ifdef __cplusplus
}
#endif
#endif /* FOURT_H */
