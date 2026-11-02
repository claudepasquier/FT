# Changelog

## 1.0.0 — 2026-11-02

First release of the modernised FT (WebAssembly edition).

### Added
- `core/`: pure C library extracted verbatim from the legacy CGI
  (`legacy/c/showFourt.c`): weighted residue signal → windowed averaging →
  RMS normalisation → radix-2 FFT (`fourt_fft`) → peak extraction
  (`fourt_compute`). Results are numerically identical to the 1998 program.
- 334 unit checks (impulse/flat spectrum, sine peak position, DFT
  cross-check, Parseval, error paths) — all passing.
- CLI reference tool `refcli` + golden fixture (`ccc4_ggy_512.csv`).
- Emscripten build (`make wasm`) producing `web/src/wasm/fourt.js`
  + `fourt.wasm` (committed, Emscripten 6.0.13).
- `web/`: build-free single-page app replacing the Java applets —
  sequence input (FASTA/SwissProt/PDB/plain), 10 residue-group presets with
  editable weights, visual active-state on presets, results table, CSV
  export, hand-rolled canvas chart of period vs intensity.
- Ported documentation (`docs/`), migration recipe for other CGI apps.
- GitHub Pages deployment workflow (`.github/workflows/deploy.yml`).
- Zenodo metadata, CITATION.cff with the original 1998 Bioinformatics paper,
  GPL-3.0 license, AUTHORS.md.

### Changed
- All computation now runs client-side (WebAssembly); no server, no CGI,
  no user data stored — sequences never leave the browser.

### Removed
- The CGI round-trip (`initFourt` job files, `log/` directory) and the Java
  applet interface (preserved untouched in `legacy/`).
