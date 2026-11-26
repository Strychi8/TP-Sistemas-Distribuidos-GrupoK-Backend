#!/usr/bin/env bash
set -euo pipefail

# Usa las direcciones configuradas o los valores predeterminados.
BOOTSTRAP_SERVER="${KAFKA_BOOTSTRAP_SERVERS:-kafka:9092}"
KAFKA_TOPICS_BIN="${KAFKA_TOPICS_BIN:-/opt/kafka/bin/kafka-topics.sh}"

# Busca la herramienta en PATH si no está en la ruta configurada.
if [ ! -x "${KAFKA_TOPICS_BIN}" ]; then
  KAFKA_TOPICS_BIN="$(command -v kafka-topics.sh || echo "")"
fi

# Falla si kafka-topics.sh no está disponible.
if [ -z "${KAFKA_TOPICS_BIN}" ]; then
  echo "Error: kafka-topics.sh no encontrado."
  exit 1
fi

echo "Waiting for Kafka broker at ${BOOTSTRAP_SERVER}..."
# Espera hasta 25 intentos, con tres segundos entre cada uno.
MAX_ATTEMPTS=25
COUNT=0
until "${KAFKA_TOPICS_BIN}" --bootstrap-server "${BOOTSTRAP_SERVER}" --list > /dev/null 2>&1; do
  COUNT=$((COUNT + 1))
  if [ "${COUNT}" -ge "${MAX_ATTEMPTS}" ]; then
    echo "Error: Kafka no respondio despues de $((MAX_ATTEMPTS * 3)) segundos."
    exit 1
  fi
  echo "Kafka not fully ready yet, retrying in 3s (${COUNT}/${MAX_ATTEMPTS})..."
  sleep 3
done

# Topics que usan los servicios de verificación y mantenimiento.
TOPICS=(
  "vehiculos.verificacion.solicitada"
  "vehiculos.verificacion.resultado"
  "vehiculos.externos.registrados"
  "vehiculos.externos.validacion.patente"
  "vehiculos.externos.validacion.titularidad"
  "vehiculos.externos.validacion.completada"
  "reserva.confirmada"
  "mantenimiento.resultado"
)

# Crea cada topic si todavía no existe.
for topic in "${TOPICS[@]}"; do
  echo "Creating topic if missing: ${topic}"
  CREATED=false
  for attempt in 1 2 3; do
    if "${KAFKA_TOPICS_BIN}" \
      --bootstrap-server "${BOOTSTRAP_SERVER}" \
      --create \
      --if-not-exists \
      --topic "${topic}" \
      --partitions 1 \
      --replication-factor 1; then
      CREATED=true
      break
    fi
    echo "Warning: Error creating ${topic}. Retrying (attempt ${attempt}/3)..."
    sleep 2
  done

  if [ "${CREATED}" = false ]; then
    echo "Error critico: No se pudo crear el topic ${topic} tras 3 intentos."
    exit 1
  fi
done

echo "Available topics:"
"${KAFKA_TOPICS_BIN}" --bootstrap-server "${BOOTSTRAP_SERVER}" --list
