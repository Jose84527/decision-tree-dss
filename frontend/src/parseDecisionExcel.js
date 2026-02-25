import * as XLSX from "xlsx";

export async function parseDecisionExcel(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();

    reader.onload = (e) => {
      try {
        const data = new Uint8Array(e.target.result);
        const workbook = XLSX.read(data, { type: "array" });

        const hojasRequeridas = ["config", "estados", "payoffs"];
        for (const hoja of hojasRequeridas) {
          if (!workbook.SheetNames.includes(hoja)) {
            throw new Error(`Falta la hoja "${hoja}" en el Excel. Se requieren: config, estados, payoffs.`);
          }
        }

        const sheetConfig = workbook.Sheets["config"];
        const rawConfig = XLSX.utils.sheet_to_json(sheetConfig, { header: 1 });
        if (rawConfig.length < 2) throw new Error('La hoja "config" está vacía o mal estructurada.');

        const configRow = rawConfig[1];
        const config = {
          modo: String(configRow[0] || "").trim().toLowerCase(),
          criterio: String(configRow[1] || "").trim().toLowerCase(),
        };
        if (configRow[2] !== undefined && configRow[2] !== "") config.alfaHurwicz = parseFloat(configRow[2]);
        if (configRow[3] !== undefined && configRow[3] !== "") config.estadoReal = String(configRow[3]).trim();
        if (!config.modo) throw new Error('El campo "modo" está vacío en la hoja config.');
        if (!config.criterio) throw new Error('El campo "criterio" está vacío en la hoja config.');

        const sheetEstados = workbook.Sheets["estados"];
        const rawEstados = XLSX.utils.sheet_to_json(sheetEstados, { header: 1 });
        if (rawEstados.length < 2) throw new Error('La hoja "estados" está vacía.');

        const estados = [];
        for (let i = 1; i < rawEstados.length; i++) {
          const fila = rawEstados[i];
          if (!fila || !fila[0]) continue;
          const nombre = String(fila[0]).trim();
          const probabilidad = fila[1] !== undefined && fila[1] !== "" ? parseFloat(fila[1]) : null;
          if (!nombre) continue;
          estados.push({ nombre, probabilidad });
        }
        if (estados.length === 0) throw new Error('No se encontraron estados válidos.');

        const sheetPayoffs = workbook.Sheets["payoffs"];
        const rawPayoffs = XLSX.utils.sheet_to_json(sheetPayoffs, { header: 1 });
        if (rawPayoffs.length < 2) throw new Error('La hoja "payoffs" está vacía.');

        const headers = rawPayoffs[0].map((h) => String(h).trim());
        const nombresEstadosEnPayoffs = headers.slice(1);

        const alternativas = [];
        for (let i = 1; i < rawPayoffs.length; i++) {
          const fila = rawPayoffs[i];
          if (!fila || !fila[0]) continue;
          const nombre = String(fila[0]).trim();
          if (!nombre) continue;
          const payoffs = {};
          for (let j = 0; j < nombresEstadosEnPayoffs.length; j++) {
            const estado = nombresEstadosEnPayoffs[j];
            const valor = fila[j + 1];
            if (estado && valor !== undefined && valor !== "") payoffs[estado] = parseFloat(valor);
          }
          alternativas.push({ nombre, payoffs });
        }
        if (alternativas.length === 0) throw new Error('No se encontraron alternativas válidas.');

        resolve({ config, estados, alternativas });
      } catch (err) {
        reject(err);
      }
    };

    reader.onerror = () => reject(new Error("No se pudo leer el archivo."));
    reader.readAsArrayBuffer(file);
  });
}
