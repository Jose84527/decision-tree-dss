import { useEffect, useState } from "react";

export default function App() {
    const [result, setResult] = useState("Cargando...");
    const [error, setError] = useState("");

    useEffect(() => {
        fetch("http://localhost:8080/ping")
            .then(async (res) => {
                if (!res.ok) {
                    throw new Error(`HTTP ${res.status}`);
                }
                const text = await res.text();
                setResult(text);
            })
            .catch((err) => {
                setError(String(err));
                setResult("");
            });
    }, []);

    return (
        <div style={{ fontFamily: "system-ui, sans-serif", padding: 24 }}>
            <h1>Decision Tree DSS</h1>

            {error ? (
                <>
                    <p style={{ color: "crimson" }}>
                        Error conectando con el backend:
                    </p>
                    <pre>{error}</pre>
                    <p>
                        Asegúrate de que el backend esté corriendo en{" "}
                        <code>http://localhost:8080</code> y que <code>/ping</code> responda.
                    </p>
                </>
            ) : (
                <>
                    <p>Respuesta del backend:</p>
                    <h2>{result}</h2>
                </>
            )}
        </div>
    );
}
