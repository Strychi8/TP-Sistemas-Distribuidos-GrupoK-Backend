import { graphqlHTTP } from 'express-graphql';
import { parse } from 'graphql';
import { schema } from './schema.js';
import { resolvers } from './resolvers.js';
import { authMiddleware } from '../middleware/auth.middleware.js';
import express from 'express';

const router = express.Router();

// Detecta si es una consulta de introspección pura (usada por GraphiQL para explorar el esquema y documentar)
const isIntrospection = (req) => {
  const query = req.body?.query || req.query?.query || '';
  if (!query || typeof query !== 'string') return false;
  try {
    const doc = parse(query);
    if (!doc.definitions || doc.definitions.length === 0) return false;

    const operations = doc.definitions.filter(def => def.kind === 'OperationDefinition');
    if (operations.length === 0) return false;

    // Todas las operaciones deben ser de tipo 'query' y seleccionar ÚNICAMENTE __schema o __type
    return operations.every((op) => {
      if (op.operation !== 'query') return false;
      return op.selectionSet?.selections?.every(
        (sel) => sel.kind === 'Field' && (sel.name.value === '__schema' || sel.name.value === '__type')
      );
    });
  } catch {
    return false;
  }
};

// Plantilla HTML de GraphiQL moderno con el plugin Explorer
const modernGraphiQLHTML = `<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>GraphiQL - API Gateway</title>
  <style>
    body { height: 100vh; margin: 0; overflow: hidden; background: #0b1015; }
    #graphiql { height: 100vh; }
    .loading {
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.5rem;
      color: #9ca3af;
      font-family: system-ui, -apple-system, sans-serif;
    }
  </style>
  <link rel="stylesheet" href="https://esm.sh/graphiql@5.4.0/dist/style.css" crossorigin="anonymous" />
  <link rel="stylesheet" href="https://esm.sh/@graphiql/plugin-explorer@5.1.5/dist/style.css" crossorigin="anonymous" />
  <script type="importmap">
    {
      "imports": {
        "react": "https://esm.sh/react@19.3.0",
        "react/": "https://esm.sh/react@19.3.0/",
        "react-dom": "https://esm.sh/react-dom@19.3.0",
        "react-dom/": "https://esm.sh/react-dom@19.3.0/",
        "graphiql": "https://esm.sh/graphiql@5.4.0?standalone&external=react,react-dom,@graphiql/react,graphql",
        "graphiql/": "https://esm.sh/graphiql@5.4.0/",
        "@graphiql/plugin-explorer": "https://esm.sh/@graphiql/plugin-explorer@5.1.5?standalone&external=react,@graphiql/react,graphql",
        "@graphiql/react": "https://esm.sh/@graphiql/react@0.39.1?standalone&external=react,react-dom,graphql,@graphiql/toolkit,@emotion/is-prop-valid",
        "@graphiql/toolkit": "https://esm.sh/@graphiql/toolkit@0.12.1?standalone&external=graphql",
        "graphql": "https://esm.sh/graphql@17.0.2",
        "@emotion/is-prop-valid": "data:text/javascript,"
      }
    }
  </script>
</head>
<body>
  <div id="graphiql">
    <div class="loading">Cargando GraphiQL Explorer...</div>
  </div>

  <script type="module">
    import React from 'react';
    import ReactDOM from 'react-dom/client';
    import { GraphiQL, HISTORY_PLUGIN } from 'graphiql';
    import { createGraphiQLFetcher } from '@graphiql/toolkit';
    import { explorerPlugin } from '@graphiql/plugin-explorer';
    import 'graphiql/setup-workers/esm.sh';

    const fetcher = createGraphiQLFetcher({ url: '/graphql' });
    const explorer = explorerPlugin({ showAttribution: false });
    const plugins = [HISTORY_PLUGIN, explorer];

    function App() {
      return React.createElement(GraphiQL, {
        fetcher,
        plugins,
        visiblePlugin: explorer,
        defaultEditorToolsVisibility: 'headers',
        shouldPersistHeaders: true
      });
    }

    ReactDOM.createRoot(document.getElementById('graphiql')).render(
      React.createElement(App)
    );
  </script>
</body>
</html>`;

// Servir la interfaz visual moderna si se accede desde el navegador
router.get('/', (req, res, next) => {
  if (req.headers.accept && req.headers.accept.includes('text/html')) {
    return res.type('html').send(modernGraphiQLHTML);
  }
  next();
});

// Middleware de autenticación:
// 1. Deja pasar introspección para que la documentación y autocompletado carguen sin trabas.
// 2. Exige JWT para todas las consultas y mutaciones reales de negocio.
router.use('/', (req, res, next) => {
  if (isIntrospection(req)) {
    return next();
  }
  return authMiddleware(req, res, next);
}, graphqlHTTP((req) => ({
  schema: schema,
  rootValue: resolvers,
  graphiql: false,
  context: { req },
  customFormatErrorFn: (err) => {
    const code = err.originalError?.code || 500;
    const details = err.originalError?.details || null;
    return {
      message: err.message,
      locations: err.locations,
      path: err.path,
      code,
      details,
      extensions: {
        code,
        details
      }
    };
  }
})));

export default router;
