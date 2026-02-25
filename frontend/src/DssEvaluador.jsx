import { useState, useRef } from "react";
import { parseDecisionExcel } from "./parseDecisionExcel";

const BACKEND_URL = "http://localhost:8080/api/dss/evaluar";

const CRITERIOS_LABELS = {
  maximax: "Maximax", maximin: "Maximin", laplace: "Laplace",
  hurwicz: "Hurwicz", minimax_regret: "Minimax Regret", valor_esperado: "Valor Esperado",
};
const MODOS_LABELS = {
  riesgo: "Riesgo", incertidumbre: "Incertidumbre", certidumbre: "Certidumbre",
};

export default function DssEvaluador() {
  const [estado, setEstado] = useState("idle");
  const [archivo, setArchivo] = useState(null);
  const [resultado, setResultado] = useState(null);
  const [errorMsg, setErrorMsg] = useState("");
  const [jsonPreview, setJsonPreview] = useState(null);
  const fileInputRef = useRef(null);

  function handleArchivoChange(e) {
    const file = e.target.files[0];
    if (!file) return;
    if (!file.name.endsWith(".xlsx")) { setErrorMsg("Solo se aceptan archivos .xlsx"); setEstado("error"); return; }
    setArchivo(file); setEstado("idle"); setResultado(null); setErrorMsg(""); setJsonPreview(null);
  }

  function handleDrop(e) {
    e.preventDefault();
    const file = e.dataTransfer.files[0];
    if (!file) return;
    if (!file.name.endsWith(".xlsx")) { setErrorMsg("Solo se aceptan archivos .xlsx"); setEstado("error"); return; }
    setArchivo(file); setEstado("idle"); setResultado(null); setErrorMsg(""); setJsonPreview(null);
  }

  async function handleEvaluar() {
    if (!archivo) return;
    try {
      setEstado("parsing");
      const jsonData = await parseDecisionExcel(archivo);
      setJsonPreview(jsonData);
      setEstado("loading");
      const response = await fetch(BACKEND_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(jsonData),
      });
      const data = await response.json();
      if (!response.ok) { setErrorMsg(data.mensaje || data.message || "Error desconocido."); setEstado("error"); return; }
      setResultado(data); setEstado("success");
    } catch (err) {
      setErrorMsg(err.message.includes("Failed to fetch") ? "No se pudo conectar con el backend." : err.message || "Error inesperado.");
      setEstado("error");
    }
  }

  function handleReset() {
    setArchivo(null); setEstado("idle"); setResultado(null); setErrorMsg(""); setJsonPreview(null);
    if (fileInputRef.current) fileInputRef.current.value = "";
  }

  const ocupado = estado === "parsing" || estado === "loading";

  return (
    <div className="min-h-screen bg-slate-50 text-slate-800">

      {/* Header */}
     <header className="bg-white border-b border-slate-200 shadow-sm sticky top-0 z-10">
  <div className="max-w-3xl mx-auto px-4 py-4 flex items-center gap-4 flex-wrap">
          <span className="bg-sky-500 text-white font-mono font-bold text-sm px-3 py-1 rounded">DSS</span>
          <div>
            <h1 className="text-lg font-semibold text-slate-900">Sistema de Apoyo a la Decisión</h1>
            <p className="text-xs text-slate-400">Matriz de pagos · Criterios de decisión</p>
          </div>
        </div>
      </header>

     <main className="max-w-3xl mx-auto px-4 py-8 flex flex-col gap-6">

        {/* Zona de carga */}
        <section className="bg-white rounded-xl border border-slate-200 shadow-sm p-6 md:p-8">
          <p className="text-xs font-semibold text-slate-400 uppercase tracking-widest mb-5">① Cargar archivo Excel</p>

          <div
            onClick={() => fileInputRef.current?.click()}
            onDragOver={(e) => e.preventDefault()}
            onDrop={handleDrop}
            className={`border-2 border-dashed rounded-xl p-10 text-center cursor-pointer transition-all
              ${archivo ? "border-green-400 bg-green-50" : "border-slate-300 bg-slate-50 hover:border-sky-400 hover:bg-sky-50"}`}
          >
            <input ref={fileInputRef} type="file" accept=".xlsx" className="hidden" onChange={handleArchivoChange} />
            {archivo ? (
              <>
                <div className="text-4xl mb-2">📊</div>
                <p className="font-semibold text-green-600 break-all">{archivo.name}</p>
                <p className="text-xs text-slate-400 mt-1">{(archivo.size / 1024).toFixed(1)} KB · listo para evaluar</p>
              </>
            ) : (
              <>
                <div className="text-4xl mb-3 text-slate-300">⬆</div>
                <p className="text-slate-500">Arrastra tu archivo <strong>.xlsx</strong> aquí</p>
                <p className="text-xs text-slate-400 mt-1">o haz clic para seleccionar</p>
              </>
            )}
          </div>

          <div className="flex gap-3 mt-5 flex-wrap">
            <button
              onClick={handleEvaluar}
              disabled={!archivo || ocupado}
              className={`bg-gradient-to-r from-sky-500 to-indigo-500 text-white font-bold px-6 py-3 rounded-lg transition-opacity
                ${!archivo || ocupado ? "opacity-40 cursor-not-allowed" : "hover:opacity-90 cursor-pointer"}`}
            >
              {estado === "parsing" ? "⏳ Interpretando Excel…" : estado === "loading" ? "⏳ Evaluando…" : "▶ Evaluar"}
            </button>
            {(archivo || estado !== "idle") && (
              <button onClick={handleReset} className="border border-slate-200 text-slate-400 px-5 py-3 rounded-lg hover:bg-slate-100 cursor-pointer">
                ↺ Reiniciar
              </button>
            )}
          </div>
        </section>

        {/* Cargando */}
        {ocupado && (
          <section className="bg-white rounded-xl border border-slate-200 shadow-sm p-6 flex items-center gap-4">
            <div className="w-6 h-6 border-4 border-slate-200 border-t-sky-500 rounded-full animate-spin shrink-0" />
            <p className="text-slate-500 text-sm">
              {estado === "parsing" ? "Interpretando el archivo Excel…" : "Enviando al backend y calculando…"}
            </p>
          </section>
        )}

        {/* Error */}
        {estado === "error" && (
          <section className="bg-white rounded-xl border-l-4 border-red-400 border border-slate-200 shadow-sm p-6">
            <p className="text-xs font-semibold text-red-400 uppercase tracking-widest mb-3">⚠ Error</p>
            <p className="text-red-600 text-sm mb-2">{errorMsg}</p>
            <p className="text-xs text-slate-400">Verifica que el archivo tenga las hojas: config, estados y payoffs.</p>
          </section>
        )}

        {/* Resultado exitoso */}
        {estado === "success" && resultado && (
          <>
            <section className="bg-white rounded-xl border-l-4 border-green-400 border border-slate-200 shadow-sm p-6 md:p-8">
              <span className="inline-block text-xs font-bold text-green-600 bg-green-50 border border-green-200 rounded-full px-3 py-1 mb-4">
                ✓ Evaluación completada
              </span>
              <p className="text-xs font-semibold text-slate-400 uppercase tracking-widest mb-4">② Mejor alternativa</p>
              <div className="bg-green-50 border border-green-200 rounded-xl p-5">
                <p className="text-2xl md:text-3xl font-semibold text-slate-900 mb-3 break-all">{resultado.mejorAlternativa}</p>
                <div className="flex gap-2 flex-wrap">
                  <span className="bg-slate-100 text-slate-500 text-xs px-3 py-1 rounded-full">Modo: {MODOS_LABELS[resultado.modo] || resultado.modo}</span>
                  <span className="bg-slate-100 text-slate-500 text-xs px-3 py-1 rounded-full">Criterio: {CRITERIOS_LABELS[resultado.criterio] || resultado.criterio}</span>
                  <span className="bg-green-100 text-green-700 text-xs px-3 py-1 rounded-full">Puntaje: {resultado.puntajeMejor ?? resultado.puntaje ?? "—"}</span>
                </div>
              </div>
            </section>

            {resultado.ranking && resultado.ranking.length > 0 && (
              <section className="bg-white rounded-xl border border-slate-200 shadow-sm p-6 md:p-8">
                <p className="text-xs font-semibold text-slate-400 uppercase tracking-widest mb-5">③ Ranking completo</p>
                <div className="overflow-x-auto">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b border-slate-200">
                        <th className="text-left py-2 px-3 text-xs font-bold text-slate-400 uppercase">#</th>
                        <th className="text-left py-2 px-3 text-xs font-bold text-slate-400 uppercase">Alternativa</th>
                        <th className="text-right py-2 px-3 text-xs font-bold text-slate-400 uppercase">Puntaje</th>
                      </tr>
                    </thead>
                    <tbody>
                      {resultado.ranking.map((item, i) => (
                        <tr key={item.alternativa} className={`border-b border-slate-100 ${i === 0 ? "bg-green-50" : ""}`}>
                          <td className="py-3 px-3 text-slate-500">{i + 1}</td>
                          <td className="py-3 px-3 text-slate-700">
                            {i === 0 && <span className="text-yellow-400 mr-1">★</span>}
                            {item.alternativa}
                          </td>
                          <td className="py-3 px-3 text-right text-slate-700">
                            {typeof item.puntaje === "number" ? item.puntaje.toFixed(4) : item.puntaje}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </section>
            )}
          </>
        )}

        {/* JSON debug */}
        {jsonPreview && (
          <details className="bg-slate-50 border border-slate-200 rounded-xl overflow-hidden">
            <summary className="px-4 py-3 cursor-pointer text-xs text-slate-400">🔍 Ver JSON enviado al backend</summary>
            <pre className="px-4 pb-4 text-xs text-slate-500 overflow-x-auto">{JSON.stringify(jsonPreview, null, 2)}</pre>
          </details>
        )}

      </main>

      <footer className="text-center py-6 text-xs text-slate-300">
        decision-tree-dss  React + Vite + Tailwind
      </footer>
    </div>
  );
}

