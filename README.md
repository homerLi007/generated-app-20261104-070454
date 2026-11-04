# Safe File Deleter

A small full-stack demo that safely deletes files inside a sandboxed root directory. The Spring Boot backend validates every requested path against a configurable allowed root, refusing symlinks, non-regular files, and any path that escapes the sandbox. The React frontend provides a simple UI for submitting paths and viewing deleted/refused results.

## Stack

Spring Boot 3.2 / Java 17 / React 18 / Vite 5

## Layout

```
.
├── backend/
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/com/example/demo/
│           │   ├── DemoApplication.java
│           │   └── controller/ApiController.java
│           └── resources/
│               ├── application.properties
│               └── static/          # populated by frontend build
└── frontend/
    ├── index.html
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── App.jsx
        ├── App.css
        └── main.jsx
```

## How to run

**Backend** (port 8080):

```bash
cd backend
mvn spring-boot:run
```

**Frontend** (port 5173, proxies `/api` to the backend):

```bash
cd frontend
npm install
npm run dev
```

For a production build, run `npm run build` in `frontend/` — Vite outputs to `backend/src/main/resources/static/`, so the Spring Boot app can serve the SPA directly.

## API

| Method | Path          | Description                                                                 |
|--------|---------------|-----------------------------------------------------------------------------|
| POST   | `/api/delete` | Accepts `{ "paths": ["relative/path", ...] }` and deletes each file inside the allowed root. Returns `{ "deleted": [...], "refused": [...] }`. |

A path is **refused** if it is blank, resolves outside the allowed root, is a symbolic link, is not a regular file, or cannot be resolved.

## Notes

- The allowed root defaults to `./sandbox` (relative to the backend working directory) and is configurable via `app.allowed-root` in `application.properties`. The directory must exist and be resolvable at request time, otherwise the endpoint returns HTTP 500.
- Paths are interpreted relative to the allowed root; absolute paths and `..` traversal are rejected by the `startsWith(root)` check after normalization and `toRealPath()` resolution.
- Symlinks are refused even if they point inside the root, to avoid TOCTOU issues.
- There is no authentication or authorization — this is a demo and should not be exposed publicly.
- Deletion is permanent; there is no trash/undo.
- The frontend splits input on newlines and trims whitespace; empty lines are ignored.