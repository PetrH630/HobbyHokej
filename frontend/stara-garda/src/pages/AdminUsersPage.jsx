// src/pages/admin/AdminUsersPage.jsx
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { userApi } from '../api/userApi';

function AdminUsersPage() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    userApi.getAll().then(setUsers).finally(() => setLoading(false));
  }, []);

  return (
    <div className="container mt-4">
      <h1>Správa uživatelů</h1>

      {loading && <p>Načítám…</p>}

      {!loading && (
        <table className="table table-hover mt-3">
          <thead>
            <tr>
              <th>ID</th>
              <th>Jméno</th>
              <th>Email</th>
              <th>Role</th>
              <th>Stav</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id}>
                <td>{u.id}</td>
                <td>{u.name} {u.surname}</td>
                <td>{u.email}</td>
                <td>{u.role}</td>
                <td>
                  {u.enabled ? 'Aktivní' : 'Neaktivní'}
                </td>
                <td className="text-end">
                  <Link
                    to={`/admin/users/${u.id}`}
                    className="btn btn-sm btn-primary"
                  >
                    Detail
                  </Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

export default AdminUsersPage;
