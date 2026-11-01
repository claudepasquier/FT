# FT — Identification of Residue Periodicities in Sequences

FT is a tool for locating residue periodicities in amino-acid or DNA
sequences using a Fast Fourier Transform (FFT). Residue periodicities are a
powerful aid for studying the structure and function of proteins whose 3-D
conformation is unknown: they reveal repeating patterns, help understand the
molecular structure and assembly of fibrous proteins, and characterise
structural/functional features of DNA.

## History

The original FT was a late-1990s web service developed at the **Division of
Cell Biology and Biophysics, Department of Biology, University of Athens**.
The code was written mainly by **Claude Pasquier**, with contributions from
**Nikos Varvayannis** and **Vasilis Promponas**; **Stavros Hamodrakas** is the
author of the original 1985 Fortran program on which it is based. It was built on a
C conversion (by A.D. McLachlan's algorithm) of a 1985 Fortran program
(Hamodrakas et al., 1985), with a Java-applet front-end and C CGI back-end.

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

## Quick start (development)

```bash
# 1. Core tests (native C)
cd core && make test

# 2. WASM build (requires emsdk, see core/Makefile)
make wasm          # outputs web/src/wasm/fourt.js + fourt.wasm

# 3. Front-end — no build step; serve statically, e.g.:
cd web && python3 -m http.server 8000
```

## Deployment

- Primary: CNRS Koda GitLab + GitLab Pages (see `.gitlab-ci.yml`).
- Mirror: GitHub (push mirror) for visibility, Zenodo DOI minting and
  Software Heritage archiving.

## Documentation

See `docs/index.md` and the ported pages: method, input formats
(FASTA/SwissProt/PDB), output interpretation, examples.

## Citation

See `CITATION.cff`. Key references:

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
