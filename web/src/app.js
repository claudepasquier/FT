// app.js — SPA entry point. Wires the UI to the WASM core.
import { PRESETS, PRESET_KEYS } from './presets.js';
import { parseSequence } from './sequence.js';
import { computePeriodicities, fourtVersion } from './fourt-wasm.js';
import { drawChart } from './chart.js';

const $ = (id) => document.getElementById(id);

const AA_LETTERS = 'ACDEFGHIKLMNPQRSTVWYatcgX';

let lastPeaks = [];
let selection = PRESETS.aromatic.residues.map((r) => ({ ...r }));

// ---- init -----------------------------------------------------------------

function initPower2() {
    const sel = $('power2');
    for (let p = 16; p <= 4096; p *= 2) {
        const opt = document.createElement('option');
        opt.value = p;
        opt.textContent = p;
        sel.appendChild(opt);
    }
    sel.value = '512';
}

function markActivePreset() {
    // A preset button is "active" when the current selection matches its
    // residue set with the preset weights. Custom edits deselect all.
    for (const btn of document.querySelectorAll('#preset-buttons .preset')) {
        const preset = PRESETS[btn.dataset.preset];
        const matches =
            preset.residues.length === selection.length &&
            preset.residues.every((r) =>
                selection.some((s) => s.residue === r.residue && s.weight === r.weight));
        btn.classList.toggle('active', matches);
    }
}

function initPresetButtons() {
    const box = $('preset-buttons');
    for (const key of PRESET_KEYS) {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'preset';
        btn.dataset.preset = key;
        btn.textContent = PRESETS[key].label;
        btn.addEventListener('click', () => {
            selection = PRESETS[key].residues.map((r) => ({ ...r }));
            renderWeights();
            markActivePreset();
        });
        box.appendChild(btn);
    }
}

function renderWeights() {
    const grid = $('weights-grid');
    grid.innerHTML = '';
    for (const letter of AA_LETTERS) {
        const sel = selection.find((s) => s.residue === letter);
        const label = document.createElement('label');
        label.className = 'weight';
        label.innerHTML = `${letter} <input type="number" step="0.1" value="${sel ? sel.weight : 0}">`;
        const input = label.querySelector('input');
        input.addEventListener('change', () => {
            const w = parseFloat(input.value) || 0;
            const i = selection.findIndex((s) => s.residue === letter);
            if (i >= 0) { if (w === 0) selection.splice(i, 1); else selection[i].weight = w; }
            else if (w !== 0) selection.push({ residue: letter, weight: w });
            markActivePreset();   // custom edit -> no preset highlighted
        });
        grid.appendChild(label);
    }
}

function updateSeqLength() {
    const { seq } = getSequence();
    $('seq-length').textContent = seq.length;
}

function getSequence() {
    const format = document.querySelector('input[name="format"]:checked').value;
    return parseSequence($('seq-input').value, format);
}

function showError(msg) {
    const el = $('error-msg');
    el.textContent = msg;
    el.hidden = false;
}

function clearError() {
    $('error-msg').hidden = true;
}

// ---- appearance block (legacy showFourt display) ---------------------------

const AA3 = {
    A: 'ALA', R: 'ARG', N: 'ASN', D: 'ASP', C: 'CYS', Q: 'GLN', E: 'GLU',
    G: 'GLY', H: 'HIS', I: 'ILE', L: 'LEU', K: 'LYS', M: 'MET', F: 'PHE',
    P: 'PRO', S: 'SER', T: 'THR', W: 'TRP', Y: 'TYR', V: 'VAL',
    a: '  a', t: '  t', c: '  c', g: '  g',
};

function renderAppearance(seqSlice, start) {
    const use3 = $('use-3letter').checked;
    const perLine = use3 ? 10 : 40;
    const lines = [];
    let line = '';
    let count = 0;
    for (let i = 0; i < seqSlice.length; i++) {
        const ch = seqSlice[i];
        const selected = selection.some((s) => s.residue === ch);
        if (selected) { line += use3 ? `${AA3[ch] || 'UNK'} ` : ch; count++; }
        else line += use3 ? '... ' : '.';
        if ((i + 1) % perLine === 0) {
            lines.push(String(start + i - perLine + 1).padStart(4, '0') + ' ' + line);
            line = '';
        }
    }
    if (line) lines.push(line);
    $('appearance').textContent = lines.join('\n');
    return count;
}

// ---- run -------------------------------------------------------------------

async function run() {
    clearError();
    const { title, seq } = getSequence();
    if (!seq.length) return showError('Please enter a sequence.');

    const start = parseInt($('start-res').value, 10) || 1;
    let end = parseInt($('end-res').value, 10) || seq.length;
    end = Math.min(end, seq.length);
    if (start < 1 || start > end) return showError('Invalid residue range.');

    const nres = end - start + 1;
    if (nres > 4096) return showError('Sequence slice exceeds the 4096-residue limit.');

    const nfold = parseInt($('power2').value, 10);
    const minPeriod = parseFloat($('min-period').value);
    const maxPeriod = parseFloat($('max-period').value);
    const fsqmin = parseFloat($('min-intensity').value) || 0;

    if (maxPeriod > nfold) return showError(`Max period must be ≤ embedment size (${nfold}).`);

    const slice = seq.slice(start - 1, end);
    try {
        const { peaks, totalAppearance } = await computePeriodicities(
            slice, selection, nfold, minPeriod, maxPeriod, fsqmin,
        );
        lastPeaks = peaks;
        renderResults({ title, slice, start, nres, nfold, fsqmin, minPeriod, maxPeriod, totalAppearance, peaks });
    } catch (e) {
        showError(e.message);
    }
}

function renderResults(ctx) {
    $('summary').innerHTML = ctx.title ? `Query data for: <b>${escapeHtml(ctx.title)}</b>` : 'Query data';
    const count = renderAppearance(ctx.slice, ctx.start);
    const selText = selection.map((s) => `(${s.residue} ${s.weight})`).join(' ');

    const tbody = $('peaks-table').querySelector('tbody');
    tbody.innerHTML = '';
    for (const p of ctx.peaks) {
        const row = document.createElement('tr');
        row.innerHTML = [
            p.n, p.a.toFixed(3), p.b.toFixed(3), p.r.toFixed(3),
            p.theta.toFixed(3), p.origin.toFixed(3),
            p.period.toFixed(3), p.intensity.toFixed(3),
        ].map((v) => `<td>${v}</td>`).join('');
        tbody.appendChild(row);
    }

    $('results-panel').hidden = false;
    drawChart($('chart'), ctx.peaks, ctx.fsqmin);
}

function escapeHtml(s) {
    return s.replace(/[&<>"]/g, (c) => ({ '&': '&', '<': '<', '>': '>', '"': '"' }[c]));
}

// ---- CSV export -------------------------------------------------------------

function downloadCsv() {
    const rows = [['n', 'a', 'b', 'r', 'theta', 'origin', 'period', 'intensity']];
    for (const p of lastPeaks) {
        rows.push([p.n, p.a, p.b, p.r, p.theta, p.origin, p.period, p.intensity]);
    }
    const csv = rows.map((r) => r.join(',')).join('\n');
    const blob = new Blob([csv], { type: 'text/csv' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = 'fourt-peaks.csv';
    a.click();
    URL.revokeObjectURL(a.href);
}

// ---- boot -------------------------------------------------------------------

initPower2();
initPresetButtons();
renderWeights();
$('seq-input').addEventListener('input', updateSeqLength);
$('btn-run').addEventListener('click', run);
$('btn-csv').addEventListener('click', downloadCsv);
$('use-3letter').addEventListener('change', updateSeqLength);

fourtVersion().then((v) => console.info(`FOURT core v${v}`)).catch(() => {
    console.warn('WASM core not built yet — run `make -C core wasm`.');
});
