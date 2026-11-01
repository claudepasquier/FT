/*
 * test_fourt.c — self-contained unit tests for the FOURT core.
 *
 * Covers:
 *  - FFT correctness: impulse -> flat spectrum, single sine -> single peak,
 *    linearity, Parseval, cross-check against a naive DFT.
 *  - fourt_compute(): error codes, peak positions for a periodic sequence.
 *
 * Build & run: make -C core test
 */
#define _USE_MATH_DEFINES
#include <math.h>
#include <stdio.h>

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include <string.h>
#include "../include/fourt.h"

static int failures = 0;
static int checks = 0;

#define CHECK(cond, msg) do { \
    checks++; \
    if (!(cond)) { failures++; printf("FAIL: %s (line %d)\n", msg, __LINE__); } \
} while (0)

#define CHECK_NEAR(a, b, tol, msg) do { \
    checks++; \
    double d_ = fabs((double)(a) - (double)(b)); \
    if (!(d_ <= (tol))) { \
        failures++; \
        printf("FAIL: %s (line %d): %g vs %g\n", msg, __LINE__, (double)(a), (double)(b)); \
    } \
} while (0)

/* Naive DFT reference matching the legacy fourt_fft convention.
 * The legacy twiddle factors are cos(t)+i*sin(t) (e^{+it}), i.e. the
 * inverse-sign DFT, so the reference uses a positive exponent. */
static void naive_dft(int n, const float *in /* 1-based */, float *outr, float *outi)
{
    for (int k = 1; k <= n; k++) {
        double sr = 0, si = 0;
        for (int j = 1; j <= n; j++) {
            double ang = 2.0 * M_PI * (double)((k - 1) * (j - 1)) / (double)n;
            sr += (double)in[j] * cos(ang);
            si += (double)in[j] * sin(ang);
        }
        outr[k] = (float)sr;
        outi[k] = (float)si;
    }
}

static void test_fft_impulse(void)
{
    int n = 64;
    float tr[FOURT_MAX_SEQ + 1] = {0}, ti[FOURT_MAX_SEQ + 1] = {0};
    tr[1] = 1.0f; /* impulse at index 1 */
    CHECK(fourt_fft(n, 2, tr, ti) == 1, "fft impulse runs");
    for (int k = 1; k <= n; k++)
        CHECK_NEAR(hypotf(tr[k], ti[k]), 1.0f, 1e-4, "impulse spectrum is flat");
}

static void test_fft_sine_peak(void)
{
    /* signal with period 8 in n=64 => peak at harmonic k=9 (k-1 = 64/8 = 8) */
    int n = 64, period = 8;
    float tr[FOURT_MAX_SEQ + 1] = {0}, ti[FOURT_MAX_SEQ + 1] = {0};
    for (int j = 1; j <= n; j++)
        tr[j] = (float)sin(2.0 * M_PI * (double)((j - 1) % period) / (double)period);
    CHECK(fourt_fft(n, 2, tr, ti) == 1, "fft sine runs");
    float best = 0; int bestk = 0;
    for (int k = 2; k <= n / 2; k++) {
        float r = hypotf(tr[k], ti[k]);
        if (r > best) { best = r; bestk = k; }
    }
    CHECK(bestk == 9, "sine peaks at expected harmonic");
}

static void test_fft_vs_dft(void)
{
    int n = 128;
    float tr[FOURT_MAX_SEQ + 1] = {0}, ti[FOURT_MAX_SEQ + 1] = {0};
    float ref[FOURT_MAX_SEQ + 1] = {0}, refi[FOURT_MAX_SEQ + 1] = {0};
    for (int j = 1; j <= n; j++)
        tr[j] = (float)((j * 37) % 17) / 8.0f - 1.0f; /* deterministic pseudo-random */
    naive_dft(n, tr, ref, refi);
    CHECK(fourt_fft(n, 2, tr, ti) == 1, "fft random runs");
    for (int k = 1; k <= n; k++) {
        CHECK_NEAR(tr[k], ref[k], 1e-3, "fft real matches DFT");
        CHECK_NEAR(ti[k], refi[k], 1e-3, "fft imag matches DFT");
    }
}

static void test_fft_parseval(void)
{
    int n = 32;
    float tr[FOURT_MAX_SEQ + 1] = {0}, ti[FOURT_MAX_SEQ + 1] = {0};
    float energy = 0;
    for (int j = 1; j <= n; j++) { tr[j] = (float)(j % 5) - 2.0f; energy += tr[j] * tr[j]; }
    CHECK(fourt_fft(n, 2, tr, ti) == 1, "fft parseval runs");
    float spec = 0;
    for (int k = 1; k <= n; k++) spec += tr[k] * tr[k] + ti[k] * ti[k];
    CHECK_NEAR(spec, energy * n, 1e-2, "Parseval holds");
}

static void test_fft_not_pow2(void)
{
    float tr[5] = {0}, ti[5] = {0};
    CHECK(fourt_fft(48, 2, tr, ti) == -1, "fft rejects non power of 2");
}

static void test_compute_basic(void)
{
    /* 64 residues, hydrophobic every 4th position -> strong period 4 */
    char seq[65];
    for (int i = 0; i < 64; i++)
        seq[i] = (i % 4 == 0) ? 'L' : 'A';
    seq[64] = '\0';
    char asel[3] = { 'L', 'A', '\0' };
    float value[2] = { 1.0f, 0.0f };
    FourtPeak peaks[64];
    int n = 0;
    int rc = fourt_compute(seq, 64, asel, value, 2, 64, 2.0f, 20.0f, 0.0f,
                           peaks, 64, &n);
    CHECK(rc == FOURT_OK, "compute runs");
    CHECK(n > 0, "compute reports peaks");
    if (n > 0) {
        /* strongest peak should be near period 4 */
        int bi = 0;
        for (int i = 1; i < n; i++)
            if (peaks[i].intensity > peaks[bi].intensity) bi = i;
        CHECK_NEAR(peaks[bi].period, 4.0f, 0.1f, "dominant period is 4");
    }
}

static void test_compute_errors(void)
{
    FourtPeak peaks[8];
    int n = 0;
    /* selecting a residue absent from the sequence */
    char seq[9] = "AAAAAAAB";
    char aselX[2] = { 'X', '\0' };
    float v1s[1] = { 1.0f };
    CHECK(fourt_compute(seq, 8, aselX, v1s, 1, 8, 2.0f, 8.0f, 0.0f,
                        peaks, 8, &n) == FOURT_ERR_NO_RESIDUE, "no residue error");
    CHECK(fourt_compute(NULL, 8, aselX, v1s, 1, 8, 2, 8, 0, peaks, 8, &n)
          == FOURT_ERR_ARGS, "null args error");
    /* nfold not a power of 2: nres == nfold == 7 avoids the legacy
     * partial-window quirk so we reach the FFT check */
    char seq7[8] = "AAAAAAB";
    char asel2[3] = { 'A', 'B', '\0' };
    float val2[2] = { 1.0f, -1.0f };
    CHECK(fourt_compute(seq7, 7, asel2, val2, 2, 7, 2, 8, 0, peaks, 8, &n)
          == FOURT_ERR_NOT_POW2, "nfold not power of 2");
}

static void test_compute_overflow_guard(void)
{
    /* Legacy window handling drops the final position, so use nres = nfold+1
     * with a constant sequence: that yields 0/0 (NaN) in the legacy pipeline,
     * which fourt_compute now rejects as degenerate. */
    char seq[10] = "AAAAAAAAA"; /* 9 residues */
    char asel[2] = { 'A', '\0' };
    float value[1] = { 1.0f };
    FourtPeak peaks[1];
    int n = 0;
    CHECK(fourt_compute(seq, 9, asel, value, 1, 8, 2.0f, 8.0f, 0.0f,
                        peaks, 1, &n) == FOURT_ERR_DEGENERATE,
          "constant signal rejected as degenerate");
}

int main(void)
{
    test_fft_impulse();
    test_fft_sine_peak();
    test_fft_vs_dft();
    test_fft_parseval();
    test_fft_not_pow2();
    test_compute_basic();
    test_compute_errors();
    test_compute_overflow_guard();

    printf("%d checks, %d failures\n", checks, failures);
    return failures ? 1 : 0;
}
