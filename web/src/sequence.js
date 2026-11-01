// sequence.js — sequence format parsers, ported from legacy initFourt.c and
// the format descriptions in legacy/html (FASTA, SwissProt, PDB).

const ONE_LETTER = 'ACDEFGHIKLMNPQRSTVWY';

// 3-letter -> 1-letter map (from legacy showFourt.c convert1To3)
const AA3 = {
    ALA: 'A', ARG: 'R', ASN: 'N', ASP: 'D', CYS: 'C', GLN: 'Q', GLU: 'E',
    GLY: 'G', HIS: 'H', ILE: 'I', LEU: 'L', LYS: 'K', MET: 'M', PHE: 'F',
    PRO: 'P', SER: 'S', THR: 'T', TRP: 'W', TYR: 'Y', VAL: 'V',
    // nucleotides used by the original tool
    '  a': 'a', '  t': 't', '  c': 'c', '  g': 'g',
};

export function parseSequence(text, format) {
    switch (format) {
        case 'fasta': return parseFasta(text);
        case 'swissprot': return parseSwissProt(text);
        case 'pdb': return parsePdb(text);
        default: return parsePlain(text);
    }
}

export function parsePlain(text) {
    const lines = text.split(/\r?\n/);
    let title = '';
    const residues = [];
    for (const line of lines) {
        if (/^>/.test(line)) { title = line.slice(1).trim(); continue; }
        residues.push(...line.replace(/[^A-Za-z]/g, ''));
    }
    return { title, seq: residues.join('') };
}

export function parseFasta(text) {
    // FASTA: header line starting with '>', sequence on following lines.
    const result = parsePlain(text);
    if (!result.seq && !result.title) {
        // maybe a raw sequence mislabelled as FASTA — fall back
        return parsePlain(text);
    }
    return result;
}

export function parseSwissProt(text) {
    // SwissProt: 'ID   NAME ...' header, 'SQ' block, sequence until '//'.
    const lines = text.split(/\r?\n/);
    let title = '';
    let inSeq = false;
    const residues = [];
    for (const line of lines) {
        if (line.startsWith('ID')) { title = line.split(/\s+/)[1] || ''; continue; }
        if (line.startsWith('SQ')) { inSeq = true; continue; }
        if (line.startsWith('//')) break;
        if (inSeq) residues.push(...line.replace(/[^A-Za-z]/g, ''));
    }
    return { title, seq: residues.join('') };
}

export function parsePdb(text, chainId = '') {
    // PDB: SEQRES records; optional ATOM extraction is out of scope here —
    // same behaviour as the legacy applet which used SEQRES + chain ID.
    const lines = text.split(/\r?\n/);
    let title = '';
    const residues = [];
    for (const line of lines) {
        if (line.startsWith('SEQRES')) {
            const chain = line[11] || ' ';
            if (chainId && chain !== chainId) continue;
            // SEQRES residues are in 3-letter code, columns 20-70, groups of 3+space
            const chunk = line.slice(19, 70);
            const tokens = chunk.trim().split(/\s+/);
            for (const t of tokens) {
                const one = AA3[t.toUpperCase()];
                if (one) residues.push(one);
                else if (t.length === 3) residues.push('X');
            }
        } else if (line.startsWith('HEADER') || line.startsWith('TITLE')) {
            if (!title) title = line.slice(10).trim();
        } else if (line.startsWith('ATOM') && !residues.length) {
            // fall back to ATOM records if no SEQRES
            const resName = line.slice(17, 20).trim();
            const one = AA3[resName.toUpperCase()];
            if (one) residues.push(one);
        }
    }
    return { title, seq: residues.join('') };
}

// Validate a sequence against the letters accepted by the legacy tool.
export function isValidResidue(ch) {
    return ONE_LETTER.includes(ch) || 'atcgX'.includes(ch);
}
