#!/usr/bin/env bash
set -euo pipefail

PORT="${PORT:-8080}"
APP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

find_port_pids() {
  ss -ltnp 2>/dev/null \
    | awk -v port=":${PORT}" '$4 ~ port {print $NF}' \
    | sed -nE 's/.*pid=([0-9]+).*/\1/p' \
    | sort -u
}

mapfile -t pids < <(find_port_pids)

if ((${#pids[@]} > 0)); then
  echo "Puerto ${PORT} en uso. Revisando procesos..."
  for pid in "${pids[@]}"; do
    cmd="$(ps -p "${pid}" -o comm= 2>/dev/null || true)"
    if [[ "${cmd}" == "java" ]]; then
      echo "Cerrando proceso Java PID ${pid} en puerto ${PORT}"
      kill -9 "${pid}"
    else
      echo "Proceso PID ${pid} (${cmd}) no es Java. No se mata automaticamente."
    fi
  done
fi

if ss -ltnp 2>/dev/null | awk -v port=":${PORT}" '$4 ~ port {found=1} END {exit !found}'; then
  echo "No se pudo liberar el puerto ${PORT}. Libera ese proceso manualmente."
  exit 1
fi

cd "${APP_DIR}"
echo "Iniciando Spring Boot en puerto ${PORT}..."
exec ./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=${PORT}"
