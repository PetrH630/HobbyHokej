// src/pages/RegisterPage.jsx
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { registerUser } from "../api/authApi";
import { tryGetDemoNotifications, tryClearDemoNotifications } from "../api/demoApi";
import DemoNotificationsModal from "../components/demo/DemoNotificationsModal";

const hasAnyDemoItems = (demo) => {
  if (!demo) return false;
  return (demo.emails?.length ?? 0) > 0 || (demo.sms?.length ?? 0) > 0;
};

/**
 * RegisterPage
 *
 * registrace – Bootstrap modal.
 *
 * Vedlejší efekty:
 * - může provádět navigaci v aplikaci
 * - načítá nebo odesílá data přes API
 *
 * @param {Object} props vstupní hodnoty komponenty
 */
const RegisterPage = () => {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    name: "",
    surname: "",
    email: "",
    password: "",
    passwordConfirm: "",
  });

  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);

  // DEMO modal
  const [demoNotifications, setDemoNotifications] = useState(null);
  const [showDemoModal, setShowDemoModal] = useState(false);

  const handleChange = (e) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  };

  const handleCloseDemoModal = () => {
    setShowDemoModal(false);
    setDemoNotifications(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    setSuccess(false);

    if (form.password !== form.passwordConfirm) {
      setError("Hesla se neshodují.");
      setLoading(false);
      return;
    }

    try {
      await tryClearDemoNotifications();
      await registerUser(form);

      // Success hláška (platí vždy)
      setSuccess(true);

      // DEMO: načti zachycené notifikace a ukaž modal (např. USER_CREATED s aktivačním odkazem)
      const demo = await tryGetDemoNotifications();
      console.log("[RegisterPage] demo:", demo);

      if (hasAnyDemoItems(demo)) {
        setDemoNotifications(demo);
        setShowDemoModal(true);
      }
    } catch (err) {
      console.error(err);
      setError(
        err?.response?.data?.message ||
        err?.message ||
        "Registrace se nezdařila."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container mt-4">
      <div className="row justify-content-center">
        <div className="col-md-6 col-lg-5">
          <div className="card shadow p-4">
            <h3 className="text-center mb-4">Registrace</h3>

            {error && <div className="alert alert-danger">{error}</div>}

            {success && (
              <div className="alert alert-success">
                <strong>Registrace proběhla úspěšně.</strong>
                <br />
                Byl Vám zaslán e-mail s odkazem pro aktivaci účtu. Prosím
                zkontrolujte svou schránku (i složku Spam).
              </div>
            )}

            {/* 🔹 FORMULÁŘ SE SKRYJE PO ÚSPĚCHU */}
            {!success && (
              <form onSubmit={handleSubmit}>
                <div className="mb-3">
                  <label className="form-label">Křestní jméno</label>
                  <input
                    type="text"
                    className="form-control"
                    name="name"
                    value={form.name}
                    onChange={handleChange}
                    required
                  />
                </div>

                <div className="mb-3">
                  <label className="form-label">Příjmení</label>
                  <input
                    type="text"
                    className="form-control"
                    name="surname"
                    value={form.surname}
                    onChange={handleChange}
                    required
                  />
                </div>

                <div className="mb-3">
                  <label className="form-label">E-mail</label>
                  <input
                    type="email"
                    className="form-control"
                    name="email"
                    value={form.email}
                    onChange={handleChange}
                    required
                  />
                </div>

                <div className="mb-3">
                  <label className="form-label">Heslo</label>
                  <input
                    type="password"
                    className="form-control"
                    name="password"
                    value={form.password}
                    onChange={handleChange}
                    required
                  />
                </div>

                <div className="mb-3">
                  <label className="form-label">Potvrzení hesla</label>
                  <input
                    type="password"
                    className="form-control"
                    name="passwordConfirm"
                    value={form.passwordConfirm}
                    onChange={handleChange}
                    required
                  />
                </div>

                <button
                  type="submit"
                  className="btn btn-primary w-100 mb-2"
                  disabled={loading}
                >
                  {loading ? "Registruji…" : "Registrovat"}
                </button>
              </form>
            )}

            {/* 🔹 TLAČÍTKO ZPĚT NA LOGIN – vždy viditelné */}
            <button
              type="button"
              className="btn btn-outline-secondary w-100 mt-2"
              onClick={() => navigate("/login")}
            >
              Zpět na přihlášení
            </button>
          </div>
        </div>
      </div>

      {showDemoModal && demoNotifications && (
        <DemoNotificationsModal
          show={showDemoModal}
          notifications={demoNotifications}
          loading={false}
          error={null}
          onClose={handleCloseDemoModal}
        />
      )}
    </div>
  );
};

export default RegisterPage;
