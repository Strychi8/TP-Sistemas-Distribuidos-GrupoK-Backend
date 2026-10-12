// Adapter del contrato: google.type.Date (protobuf) -> ISO string (REST).
// El gateway es el limite del sistema: la SPA nunca debe ver tipos protobuf.

const pad = (n) => String(n).padStart(2, "0");

// Idempotente: acepta objeto {year,month,day}, string ya ISO, o null.
export function toIsoDate(fechaNacimiento) {
  if (!fechaNacimiento) return null;
  if (typeof fechaNacimiento === "string") return fechaNacimiento;
  const { year, month, day } = fechaNacimiento;
  return `${year}-${pad(month)}-${pad(day)}`;
}

// Normaliza un Customer del proto manteniendo todos los campos (spread)
// y solo corrigiendo la fecha, que es el campo que se filtraba.
export function toClientRest(customer) {
  if (!customer) return null;
  return {
    ...customer,
    fechaNacimiento: toIsoDate(customer.fechaNacimiento),
  };
}

export function toClientListRest(customers) {
  return (customers || []).map(toClientRest);
}