import * as XLSX from "xlsx";

export async function parseDecisionExcel(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();

    reader.onload = (e) => {
      try {
        const data = new Uint8Array(e.target.result);
        const workbook = XLSX.read(data, { type: "array" });

        const hojasRequeridas = ["config", "criterios", "alternativas"];
        for (const hoja of hojasRequeridas) {
          if (!workbook.SheetNames.includes(hoja)) {
            throw new Error(`Falta la hoja "${hoja}". Se requieren: config, criterios, alternativas.`);
          }
        }

        // ── Hoja config ──────────────────────────────
        const rawConfig = XLSX.utils.sheet_to_json(
          workbook.Sheets["config"], { header: 1 }
        );
        if (rawConfig.length < 2) throw new Error('La hoja "config" esta vacia.');

        const fila = rawConfig[1];
        const modo = String(fila[0] || "").trim().toLowerCase();
        const alpha = fila[1] !== undefined && fila[1] !== "" ? parseFloat(fila[1]) : null;
        if (!modo) throw new Error('El campo "modo" esta vacio en la hoja config.');

        // ── Hoja criterios ───────────────────────────
        const rawCriterios = XLSX.utils.sheet_to_json(
          workbook.Sheets["criterios"], { header: 1 }
        );
        if (rawCriterios.length < 2) throw new Error('La hoja "criterios" esta vacia.');

        const criterios = [];
        for (let i = 1; i < rawCriterios.length; i++) {
          const f = rawCriterios[i];
          if (!f || !f[0]) continue;
          const criterio = String(f[0]).trim();
          const peso = f[1] !== undefined && f[1] !== "" ? parseFloat(f[1]) : 0;
          const tipo = String(f[2] || "beneficio").trim().toLowerCase();
          if (!criterio) continue;
          criterios.push({ criterio, peso, tipo });
        }
        if (criterios.length === 0) throw new Error("No se encontraron criterios validos.");

        // ── Hoja alternativas ────────────────────────
        const rawAlt = XLSX.utils.sheet_to_json(
          workbook.Sheets["alternativas"], { header: 1 }
        );
        if (rawAlt.length < 2) throw new Error('La hoja "alternativas" esta vacia.');

        const headers = rawAlt[0].map((h) => String(h).trim());
        const nombresCriterios = headers.slice(1);

        const alternativas = [];
        for (let i = 1; i < rawAlt.length; i++) {
          const f = rawAlt[i];
          if (!f || !f[0]) continue;
          const nombre = String(f[0]).trim();
          if (!nombre) continue;
          const valores = {};
          for (let j = 0; j < nombresCriterios.length; j++) {
            const crit = nombresCriterios[j];
            const val = f[j + 1];
            if (crit && val !== undefined && val !== "") {
              valores[crit] = parseFloat(val);
            }
          }
          alternativas.push({ nombre, valores });
        }
        if (alternativas.length === 0) throw new Error("No se encontraron alternativas validas.");

        resolve({ modo, alpha, criterios, alternativas });

      } catch (err) {
        reject(err);
      }
    };

    reader.onerror = () => reject(new Error("No se pudo leer el archivo."));
    reader.readAsArrayBuffer(file);
  });
}