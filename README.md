# FT — Identification of Residue Periodicities in Sequences

[![DOI](https://zenodo.org/badge/1442042344.svg)](https://doi.org/10.5281/zenodo.23878676)
[![CI pipeline (Koda)](https://src.koda.cnrs.fr/uoa-biol/FT/badges/main/pipeline.svg?v=2)](https://src.koda.cnrs.fr/uoa-biol/FT/-/pipelines)
[![CI (GitHub Actions)](https://github.com/claudepasquier/FT/actions/workflows/ci.yml/badge.svg)](https://github.com/claudepasquier/FT/actions/workflows/ci.yml)
[![Deploy (GitHub Actions)](https://github.com/claudepasquier/FT/actions/workflows/deploy.yml/badge.svg)](https://github.com/claudepasquier/FT/actions/workflows/deploy.yml)
[![Pages deployment](https://img.shields.io/website?url=https%3A%2F%2Fclaudepasquier.github.io%2FFT%2F&label=online%20app)](https://claudepasquier.github.io/FT/)

**▶ Run the app online: <https://claudepasquier.github.io/FT/>**

FT is a tool for locating residue periodicities in amino-acid or DNA
sequences using a Fast Fourier Transform (FFT). Residue periodicities are a
powerful aid for studying the structure and function of proteins whose 3-D
conformation is unknown: they reveal repeating patterns, help understand the
molecular structure and assembly of fibrous proteins, and characterise
structural/functional features of DNA.

## History

- **[original: 1998]** The original FT was a late-1990s web service developed
  at the **Division of Cell Biology and Biophysics, Department of Biology,
  University of Athens, Panepistimiopolis, Athens 15701, Greece**.
  The code was written mainly by **Claude Pasquier**, with contributions from
  **Nikos Varvayannis** and **Vasilis Promponas**; **Stavros Hamodrakas** is the
  author of the original 1985 Fortran program on which it is based. It was built on a
  C conversion (by A.D. McLachlan's algorithm) of a 1985 Fortran program
  (Hamodrakas et al., 1985), with a Java-applet front-end and C CGI back-end.
- **[modernised: 2026]** The modernised edition (WebAssembly port, this
  repository) was produced at **Université Côte d'Azur, CNRS, I3S
  (Sophia Antipolis), France** by **Claude Pasquier**.

This repository is the **modernised edition** (CNRS):

- `core/` — the original numerical pipeline (residue weighting → windowed
  averaging → RMS normalisation → radix-2 FFT) extracted verbatim into a pure
  C library and compiled to **WebAssembly** with Emscripten. Numerics match
  the legacy program (1-based scratch arrays, identical operation order).
- `web/` — a build-free single-page application (plain ES modules) replacing
  the Java applets. **All computation runs in the browser** — the sequence
  never leaves the user's machine; there is no server and no user tracking.
- `docs/` — the original documentation, ported.
- `legacy/` — the untouched 1990s sources (provenance).

## Run the program

The application runs entirely in the browser — no installation needed.

- **Online (hosted)**: <https://claudepasquier.github.io/FT/> — open the page,
  paste a sequence (FASTA/SwissProt/PDB) and run the analysis. The executable
  itself is the WebAssembly bundle served from
  [`web/src/wasm/`](web/src/wasm/) at that address.
- **Locally**: serve the `web/` directory statically and open it in a browser:

  ```bash
  cd web && python3 -m http.server 8000
  # then open http://localhost:8000
  ```

- **Command line (development/testing)**: build and run the native CLI:

  ```bash
  cd core && make refcli && ./build/refcli
  ```

## Quick start (development)

```bash
# 1. Core tests (native C)
cd core && make test

# 2. WASM build (requires emsdk, see core/Makefile)
make wasm          # outputs web/src/wasm/fourt.js + fourt.wasm

# 3. Front-end — no build step; serve statically, e.g.:
cd web && python3 -m http.server 8000
```

## Executable

The runnable artefact is the WebAssembly module of the numerical core:

- Browser bundle: [`web/src/wasm/fourt.js`](web/src/wasm/fourt.js) +
  [`web/src/wasm/fourt.wasm`](web/src/wasm/fourt.wasm), loaded by
  [`web/src/fourt-wasm.js`](web/src/fourt-wasm.js).
- Native CLI (development/testing): [`core/tools/refcli.c`](core/tools/refcli.c),
  built with `make` in [`core/`](core/Makefile).
- The WASM module is **committed to the repository** (Koda runners cannot pull
  the Emscripten toolchain); rebuild it locally with `cd core && make wasm`.

## Continuous Integration

CI is embedded in the project via [`.gitlab-ci.yml`](.gitlab-ci.yml) (CNRS Koda
GitLab):

- `pages` — deploys the app and docs to GitLab Pages on the default branch.
- `mirror-github` — optional push mirror to GitHub (requires the
  `GITHUB_MIRROR_URL` / `GITHUB_MIRROR_KEY` CI/CD variables; skipped otherwise).

## Deployment

- Primary: CNRS Koda GitLab + GitLab Pages (see `.gitlab-ci.yml`).
- Mirror: GitHub (push mirror) for visibility, Zenodo DOI minting and
  Software Heritage archiving.

## Historical access

The original 1998 service was hosted at the University of Athens:

- Legacy version accessible (but no longer executable):
  `http://athina.biol.uoa.gr/FT/`

## Documentation

See `docs/index.md` and the ported pages: method, input formats
(FASTA/SwissProt/PDB), output interpretation, examples.

## Authors

See [`AUTHORS.md`](AUTHORS.md) for the full list of contributors, their
affiliations and roles (CRediT taxonomy).

## Citation

See [`CITATION.cff`](CITATION.cff). In plain text (APA):

> Pasquier, C. M., Promponas, V. I., Varvayannis, N. J., & Hamodrakas, S. J.
> (1998). A web server to locate periodicities in a sequence.
> *Bioinformatics*, 14(8), 749–750.
> https://doi.org/10.1093/oxfordjournals.bioinformatics.a011054

Key references:

1. Hamodrakas S.J., Ekmetzoglou T. & Kafatos F.C. (1985) *Amino acid
   periodicities and their structural implications for the evolutionarily
   conservative central domain of some silkmoth chorion proteins.*
   J. Mol. Biol. **186**, 583–589.
2. McLachlan A.D. & Steward M. (1976) *The 14-fold periodicity in
   α-tropomyosin and the interaction with actin.* J. Mol. Biol. **103**,
   271–298.
3. McLachlan A.D. (1977) *Analysis of periodic patterns in amino acid
   sequences: collagen.* Biopolymers **16**, 1271–1297.

The FT service publication:

0. Pasquier C.M., Promponas V.I., Varvayannis N.J. & Hamodrakas S.J. (1998)
   *A web server to locate periodicities in a sequence.* Bioinformatics
   **14**(8), 749–750. [doi:10.1093/oxfordjournals.bioinformatics.a011054](https://doi.org/10.1093/oxfordjournals.bioinformatics.a011054)

## License

GPL-3.0-or-later (see `LICENSE`).
