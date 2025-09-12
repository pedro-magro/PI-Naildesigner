const appointments = [
  { id: 1, clientName: "Maria Silva", service: "Alongamento em Gel", dateTime: "2025-05-11T10:00:00", status: "pending" },
  { id: 2, clientName: "Ana Oliveira", service: "Nail Art Floral", dateTime: "2025-05-11T14:30:00", status: "confirmed" },
  { id: 3, clientName: "Carla Mendes", service: "Esmaltação em Gel", dateTime: "2025-05-12T11:00:00", status: "completed" },
  { id: 4, clientName: "Juliana Costa", service: "Francesinha Moderna", dateTime: "2025-05-10T15:00:00", status: "cancelled" },
  { id: 5, clientName: "Patricia Santos", service: "Decoração Moderna", dateTime: "2025-05-12T16:30:00", status: "pending" },
  { id: 6, clientName: "Fernanda Lima", service: "Alongamento em Gel", dateTime: "2025-05-11T13:00:00", status: "confirmed" }
];

const tableBody = document.getElementById("appointment-table");
const searchInput = document.getElementById("search");
const statusFilter = document.getElementById("status-filter");

function formatDate(dateStr) {
  const date = new Date(dateStr);
  return `${date.toLocaleDateString()} às ${date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`;
}

function renderAppointments() {
  const term = searchInput.value.toLowerCase();
  const status = statusFilter.value;

  const filtered = appointments.filter(app => {
    const matchTerm = app.clientName.toLowerCase().includes(term) || app.service.toLowerCase().includes(term);
    const matchStatus = status === "all" || app.status === status;
    return matchTerm && matchStatus;
  });

  tableBody.innerHTML = "";

  if (filtered.length === 0) {
    tableBody.innerHTML = `<tr><td colspan="4" style="text-align:center;">Nenhum agendamento encontrado</td></tr>`;
    return;
  }

  filtered.forEach(app => {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td>${app.clientName}</td>
      <td>${app.service}</td>
      <td>${formatDate(app.dateTime)}</td>
      <td>${app.status}</td>
    `;
    tableBody.appendChild(row);
  });

  updateSummary();
}

function updateSummary() {
  const today = new Date().toISOString().split("T")[0];
  const todayCount = appointments.filter(app => app.dateTime.startsWith(today)).length;
  const pendingCount = appointments.filter(app => app.status === "pending").length;
  const confirmedCount = appointments.filter(app => app.status === "confirmed").length;

  document.getElementById("today-count").textContent = todayCount;
  document.getElementById("pending-count").textContent = pendingCount;
  document.getElementById("confirmed-count").textContent = confirmedCount;

  const future = appointments.filter(app => new Date(app.dateTime) > new Date() && (app.status === "pending" || app.status === "confirmed"))
    .sort((a, b) => new Date(a.dateTime) - new Date(b.dateTime));

  document.getElementById("next-appointment").textContent =
    future.length > 0 ? `${formatDate(future[0].dateTime)} - ${future[0].clientName}` : "Nenhum";
}

searchInput.addEventListener("input", renderAppointments);
statusFilter.addEventListener("change", renderAppointments);

function voltar() {
  alert("Simulação: voltando para o site");
}

renderAppointments();
