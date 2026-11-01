// presets.js — residue groups ported from legacy/java/InputFourt.java:68-78.
// Each preset is one "problem": a set of selected residues with weights.

export const AA_LETTERS = 'ACDEFGHIKLMNPQRSTVWYatcgX';

// Default weight for presets: 1 for selected residues, 0 otherwise.
function group(letters) {
    return letters.split('').map((l) => ({ residue: l, weight: 1 }));
}

export const PRESETS = {
    charged: { label: 'Charged', residues: group('HRKDE') },
    chargedPos: { label: 'Charged positive', residues: group('HRK') },
    chargedNeg: { label: 'Charged negative', residues: group('DE') },
    aromatic: { label: 'Aromatics', residues: group('HFWY') },
    aliphatic: { label: 'Aliphatics', residues: group('VLIA') },
    bSheet: { label: 'β-Sheet formers', residues: group('VLIFWYTCQM') },
    bTurn: { label: 'β-Turn formers', residues: group('GPDNSCKWYQTRE') },
    polar: { label: 'Polars', residues: group('DEHKRSTNQ') },
    aHelix: { label: 'α-Helix formers', residues: group('AVLIFWDEQMHK') },
    hydrophob: { label: 'Hydrophobic', residues: group('CVILMFYWAP') },
};

export const PRESET_KEYS = Object.keys(PRESETS);
