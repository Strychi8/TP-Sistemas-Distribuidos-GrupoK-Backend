#!/usr/bin/env bash
set -euo pipefail

BOOTSTRAP_SERVER="${KAFKA_BOOTSTRAP_SERVERS:-kafka:9092}"
KAFKA_TOPICS_BIN="/opt/kafka/bin/kafka-topics.sh"

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

for topic in "${TOPICS[@]}"; do
  echo "Creating topic if missing: ${topic}"
  "${KAFKA_TOPICS_BIN}" \
    --bootstrap-server "${BOOTSTRAP_SERVER}" \
    --create \
    --if-not-exists \
    --topic "${topic}" \
    --partitions 1 \
    --replication-factor 1
done

echo "Available topics:"
"${KAFKA_TOPICS_BIN}" --bootstrap-server "${BOOTSTRAP_SERVER}" --list
