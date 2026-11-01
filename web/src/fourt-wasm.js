// fourt-wasm.js — loader/wrapper around the Emscripten ES6 module built by
// core/Makefile (target `wasm`) into src/wasm/fourt.js + fourt.wasm.
//
// Provides a friendly async API:  await loadFourt() -> { compute(...) }

let modulePromise = null;

export function loadFourt() {
    if (!modulePromise) {
        modulePromise = import('./wasm/fourt.js').then((factory) => factory.default());
    }
    return modulePromise;
}

const FOURT_MAX_PEAKS = 4096;

const ERROR_MESSAGES = {
    [-1]: 'Invalid parameters.',
    [-2]: 'None of the selected residues appears in the sequence.',
    [-3]: 'Degenerate signal (zero variance) — check the selected weights.',
    [-4]: 'Embedment size must be a power of 2.',
    [-5]: 'Internal buffer overflow.',
    [-6]: 'Sequence or embedment size exceeds the 4096-residue limit.',
};

/**
 * Run the periodicity analysis for one "problem".
 *
 * @param {string} seq      sequence (1-letter codes)
 * @param {Array<{residue:string,weight:number}>} selection
 * @param {number} nfold    embedment size (power of 2)
 * @param {number} minPeriod
 * @param {number} maxPeriod
 * @param {number} fsqmin   intensity cutoff
 * @returns {{peaks:Array, totalAppearance:number}}
 */
export async function computePeriodicities(seq, selection, nfold,
    minPeriod, maxPeriod, fsqmin) {
    const mod = await loadFourt();

    const asel = selection.map((s) => s.residue).join('');
    const values = selection.map((s) => s.weight);

    const seqBuf = mod._malloc(seq.length + 1);
    const aselBuf = mod._malloc(asel.length + 1);
    const valBuf = mod._malloc(4 * values.length);
    const outBuf = mod._malloc(4 * 8 * FOURT_MAX_PEAKS); // 8 floats per peak
    const countBuf = mod._malloc(4);
    try {
        mod.stringToUTF8(seq, seqBuf, seq.length + 1);
        mod.stringToUTF8(asel, aselBuf, asel.length + 1);
        mod.HEAPF32.set(values, valBuf / 4);

        const rc = mod._fourt_compute(
            seqBuf, seq.length,
            aselBuf, valBuf, selection.length,
            nfold, minPeriod, maxPeriod, fsqmin,
            outBuf, FOURT_MAX_PEAKS, countBuf,
        );
        if (rc !== 0) {
            throw new Error(ERROR_MESSAGES[rc] || `Core error ${rc}`);
        }
        const count = mod.getValue(countBuf, 'i32');
        // C struct FourtPeak is { int n; float a,b,r,theta,origin,period,intensity }
        // — default alignment: 32 bytes = int (4) + padding (4) + 7 floats.
        const peaks = [];
        for (let i = 0; i < count; i++) {
            const byteOff = outBuf + i * 32;
            const intView = new Int32Array(mod.HEAP8.buffer, byteOff, 1);
            const fOff = byteOff / 4 + 1;
            const h = mod.HEAPF32;
            peaks.push({
                n: intView[0],
                a: h[fOff], b: h[fOff + 1], r: h[fOff + 2],
                theta: h[fOff + 3], origin: h[fOff + 4],
                period: h[fOff + 5], intensity: h[fOff + 6],
            });
        }
        // total appearance: count residues of the selection in the sequence
        const selSet = new Set(asel.split(''));
        let totalAppearance = 0;
        for (const ch of seq) if (selSet.has(ch)) totalAppearance++;
        return { peaks, totalAppearance };
    } finally {
        mod._free(seqBuf);
        mod._free(aselBuf);
        mod._free(valBuf);
        mod._free(outBuf);
        mod._free(countBuf);
    }
}

export function fourtVersion() {
    return loadFourt().then((mod) => mod.UTF8ToString(mod._fourt_version()));
}
