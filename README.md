

# Decision Tree DSS

Sistema de Apoyo a la Decisión (DSS) basado en criterios matemáticos paraza evaluar alternativas bajo distintos escenarios.

---

## Descripción

**Decision Tree DSS** es una aplicación web que permite analizar y seleccionar la mejor alternativa entre varias opciones utilizando modelos de toma de decisiones como:

- SAW (Simple Additive Weighting)
- Valor Esperado
- Maximin
- Maximax
- Laplace
- Hurwicz
- Minimax Regret

El sistema procesa datos desde un archivo Excel y devuelve un **ranking ordenado de alternativas**, facilitando la toma de decisiones en entornos de incertidumbre o riesgo.

---

## Arquitectura del sistema

El proyecto sigue una arquitectura cliente-servidor:

Frontend (React + Vite) → Backend (Spring Boot) → Motor DSS

- **Frontend:** interfaz gráfica y carga de Excel  
- **Backend:** validación + lógica de negocio  
- **Motor DSS:** cálculo de criterios  

---

##  Tecnologías utilizadas

### Frontend
- React
- Vite
- Tailwind CSS

### Backend
- Java 21
- Spring Boot 3
- Spring Web
- Maven

---


---

## Instalación y ejecución

### 1. Clonar repositorio
git clone https://github.com/Jose84527/decision-tree-dss.git
cd decision-tree-dss


## 2. Ejecutar Backend
cd backend
mvnw spring-boot:run

Disponible en:
http://localhost:8080

## 3. Ejecutar Frontend
cd frontend
npm install
npm run dev

Disponible en:
http://localhost:5173

## Endpoints principales
#### Health Check
GET /api/health
#### Ping
GET /ping
#### Evaluar DSS
POST /api/dss/evaluar


### Ejemplo de request
{
  "modo": "saw",
  "criterios": [
    {"criterio": "costo", "peso": 0.5, "tipo": "costo"},
    {"criterio": "calidad", "peso": 0.5, "tipo": "beneficio"}
  ],
  "alternativas": [
    {
      "nombre": "Opcion A",
      "valores": {"costo": 100, "calidad": 80}
    },
    {
      "nombre": "Opcion B",
      "valores": {"costo": 120, "calidad": 90}
    }
  ]
}

 Ejemplo de respuesta
{
  "mensaje": "Evaluación DSS realizada correctamente",
  "ranking": [
    {"alternativa": "Opcion B", "puntaje": 0.75},
    {"alternativa": "Opcion A", "puntaje": 0.60}
  ]
}

--- 
## Validaciones implementadas

El backend valida:
-Request no nulo
- Modo válido

Criterios:
- Sin duplicados
- Peso válido
- Tipo correcto (beneficio/costo)

Alternativas:
- Nombre obligatorio
- Valores completos
- Parámetro alpha en Hurwicz (0 a 1)



## Notas

El sistema requiere archivos .xlsx con estructura específica

Backend y frontend deben estar corriendo simultáneamente

El puerto del backend es 8080

---
