// Adapter del contrato: google.type.Date (protobuf) -> ISO string (REST) y viceversa.
// El gateway es el limite del sistema: la SPA nunca debe ver tipos protobuf.

const pad = (n) => String(n).padStart(2, "0");

// Idempotente: acepta objeto {year,month,day}, string ya ISO, o null.
export function toIsoDate(fechaNacimiento) {
  if (!fechaNacimiento) return null;
  if (typeof fechaNacimiento === "string") return fechaNacimiento;
  const { year, month, day } = fechaNacimiento;
  if (!year || !month || !day) return null;
  return `${year}-${pad(month)}-${pad(day)}`;
}

// Convierte un string ISO ('YYYY-MM-DD') o Date a objeto google.type.Date {year, month, day}
export function toGoogleDate(isoDateString) {
  if (!isoDateString) return null;
  if (typeof isoDateString === 'object' && isoDateString.year) {
    return {
      year: Number(isoDateString.year),
      month: Number(isoDateString.month),
      day: Number(isoDateString.day)
    };
  }
  if (typeof isoDateString === 'string') {
    const parts = isoDateString.trim().split('-');
    if (parts.length === 3) {
      const year = parseInt(parts[0], 10);
      const month = parseInt(parts[1], 10);
      const day = parseInt(parts[2], 10);
      if (!isNaN(year) && !isNaN(month) && !isNaN(day)) {
        return { year, month, day };
      }
    }
  }
  const d = new Date(isoDateString);
  if (!isNaN(d.getTime())) {
    return {
      year: d.getUTCFullYear(),
      month: d.getUTCMonth() + 1,
      day: d.getUTCDate()
    };
  }
  return null;
}

// Normaliza un Customer del proto manteniendo compatibilidad con REST (camelCase)
export function toClientRest(customer) {
  if (!customer) return null;
  const rawDate = customer.fechaNacimiento || customer.fecha_nacimiento;
  const idCliente = customer.idCliente ?? customer.id_cliente;
  const idUsuario = customer.idUsuario ?? customer.id_usuario;
  return {
    ...customer,
    idCliente: idCliente != null ? Number(idCliente) : undefined,
    id_cliente: idCliente != null ? Number(idCliente) : undefined,
    idUsuario: idUsuario != null ? Number(idUsuario) : undefined,
    fechaNacimiento: toIsoDate(rawDate),
  };
}

export function toClientListRest(customers) {
  return (customers || []).map(toClientRest);
}