# ---- Stage 1: dependencias (solo prod) ----
FROM node:24-alpine AS deps
WORKDIR /app/gateway
COPY gateway/package.json gateway/package-lock.json ./
RUN --mount=type=cache,target=/root/.npm \
    npm ci --omit=dev

# ---- Stage 2: runtime ----
FROM node:24-alpine AS runtime
ENV NODE_ENV=production
WORKDIR /app/gateway

# App source
COPY gateway/ ./
# Instala las dependencias de producción desde la stage 1
COPY --from=deps /app/gateway/node_modules ./node_modules

# Contratos gRPC: clients/grpc.js resuelve '../../proto' -> hermano de gateway/.
# Layout /app/gateway + /app/proto hace que /app/gateway/clients/../../proto = /app/proto
COPY proto/ /app/proto/

USER node
EXPOSE 3000
CMD ["node", "index.js"]