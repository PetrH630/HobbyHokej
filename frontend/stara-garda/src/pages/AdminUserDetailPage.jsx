// src/pages/admin/AdminUserDetailPage.jsx
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { userApi } from '../api/userApi';

function AdminUserDetailPage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const [user, setUser] = useState(null);

    useEffect(() => {
        userApi.getById(Number(id)).then(setUser);
    }, [id]);

    if (!user) return <p className="m-4">Načítám…</p>;

    return (
        <div className="container mt-4">
            <button className="btn btn-link" onClick={() => navigate(-1)}>
                ← Zpět
            </button>

            <h2>{user.name} {user.surname}</h2>

            <p><strong>Email:</strong> {user.email}</p>
            <p><strong>Role:</strong> {user.role}</p>
            <p>
                <strong>Stav:</strong>{' '}
                {user.enabled ? 'Aktivní' : 'Neaktivní'}
            </p>

            <div className="d-flex gap-2 mt-3">
                <button
                    className="btn btn-warning"
                    onClick={() => userApi.resetPassword(user.id)}
                >
                    Reset hesla
                </button>

                {user.enabled ? (
                    <button
                        className="btn btn-outline-danger"
                        onClick={() => userApi.deactivate(user.id)}
                    >
                        Deaktivovat
                    </button>
                ) : (
                    <button
                        className="btn btn-outline-success"
                        onClick={() => userApi.activate(user.id)}
                    >
                        Aktivovat
                    </button>
                )}
            </div>
        </div>
    );
}

export default AdminUserDetailPage;
