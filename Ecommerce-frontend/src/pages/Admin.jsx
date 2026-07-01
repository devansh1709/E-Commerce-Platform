import "../styles/admin.css";

export default function Admin() {
  return (
    <div className="admin-page">
      <h1>Admin Dashboard</h1>

      <div className="admin-card">
        <h2>Manage Products</h2>
        <p>Add, edit and delete products.</p>
      </div>

      <div className="admin-card">
        <h2>Manage Orders</h2>
        <p>View all customer orders.</p>
      </div>
    </div>
  );
}