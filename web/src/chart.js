// chart.js — canvas plot of period vs intensity, replacing legacy Chart.java.
// Hand-rolled (no dependency) to keep the site build-free.

export function drawChart(canvas, peaks, fsqmin) {
    const ctx = canvas.getContext('2d');
    const W = canvas.width, H = canvas.height;
    const pad = { left: 70, right: 20, top: 20, bottom: 50 };
    ctx.clearRect(0, 0, W, H);

    if (!peaks.length) {
        ctx.fillStyle = '#666';
        ctx.font = '14px sans-serif';
        ctx.fillText('No peaks above the intensity cutoff.', pad.left, H / 2);
        return;
    }

    const maxI = Math.max(...peaks.map((p) => p.intensity), fsqmin || 0) * 1.05;
    const minP = Math.min(...peaks.map((p) => p.period));
    const maxP = Math.max(...peaks.map((p) => p.period));

    const x = (p) => pad.left + (p.period - minP) / (maxP - minP || 1) * (W - pad.left - pad.right);
    const y = (i) => H - pad.bottom - (i / (maxI || 1)) * (H - pad.top - pad.bottom);

    // axes
    ctx.strokeStyle = '#333';
    ctx.beginPath();
    ctx.moveTo(pad.left, pad.top);
    ctx.lineTo(pad.left, H - pad.bottom);
    ctx.lineTo(W - pad.right, H - pad.bottom);
    ctx.stroke();

    // cutoff line
    if (fsqmin > 0) {
        ctx.strokeStyle = '#c33';
        ctx.setLineDash([4, 4]);
        ctx.beginPath();
        ctx.moveTo(pad.left, y(fsqmin));
        ctx.lineTo(W - pad.right, y(fsqmin));
        ctx.stroke();
        ctx.setLineDash([]);
        ctx.fillStyle = '#c33';
        ctx.font = '11px sans-serif';
        ctx.fillText('cutoff', pad.left + 4, y(fsqmin) - 4);
    }

    // peaks
    ctx.fillStyle = '#1a5fb4';
    for (const p of peaks) {
        ctx.beginPath();
        ctx.arc(x(p), y(p.intensity), 4, 0, 2 * Math.PI);
        ctx.fill();
    }

    // labels
    ctx.fillStyle = '#333';
    ctx.font = '12px sans-serif';
    ctx.textAlign = 'center';
    ctx.fillText('Period (residues)', (pad.left + W - pad.right) / 2, H - 10);
    ctx.save();
    ctx.translate(15, (pad.top + H - pad.bottom) / 2);
    ctx.rotate(-Math.PI / 2);
    ctx.fillText('Intensity', 0, 0);
    ctx.restore();
    ctx.textAlign = 'left';

    // tick labels for x
    const ticks = 6;
    for (let t = 0; t <= ticks; t++) {
        const pv = minP + (t / ticks) * (maxP - minP);
        ctx.fillText(pv.toFixed(1), x({ period: pv }), H - pad.bottom + 15);
    }
}
