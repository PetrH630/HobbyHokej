// src/pages/AdminUsersPage.jsx
import { useCallback, useMemo, useState } from "react";
import { useAllUsersAdmin } from "../hooks/useAllUsersAdmin";
import AdminUsersTable from "../components/admin/AdminUsersTable";
import AdminUserEditModal from "../components/admin/AdminUserEditModal";
import { useNotification } from "../context/NotificationContext";
import { userApi } from "../api/userApi";
import BackButton from "../components/BackButton";
/**
 * Stránka pro globální správu uživatelů (ADMIN).
 *
 * - načítá všechny uživatele přes useAllUsersAdmin
 * - zobrazuje je jako karty (uživatel + jeho hráči)
 * - řeší akce EDIT / RESET PASSWORD / ACTIVATE / DEACTIVATE
 */
const AdminUsersPage = () => {
  const { users, loading, error, reload } = useAllUsersAdmin();
  const { showNotification } = useNotification();

  // === STAV PRO EDITACÍ MODAL ===
  const [editingUser, setEditingUser] = useState(null);
  const [editSaving, setEditSaving] = useState(false);

  // Reset hesla
  const handleResetPassword = useCallback(
    async (userId) => {
      const confirmReset = window.confirm(
        `Opravdu chceš resetovat heslo uživatele s ID ${userId}?`
      );
      if (!confirmReset) return;

      try {
        await userApi.resetPassword(userId);
        showNotification(
          "Heslo bylo resetováno na 'Player123'.",
          "success"
        );
        await reload();
      } catch (err) {
        const message =
          err?.response?.data?.message ||
          err?.message ||
          "Reset hesla se nezdařil.";
        showNotification(message, "danger");
      }
    },
    [reload, showNotification]
  );

  // Aktivace uživatele
  const handleActivate = useCallback(
    async (userId) => {
      try {
          await userApi.activateUser(userId);
        showNotification("Uživatel byl aktivován.", "success");
        await reload();
      } catch (err) {
        const message =
          err?.response?.data?.message ||
          err?.message ||
          "Aktivace uživatele se nezdařila.";
        showNotification(message, "danger");
      }
    },
    [reload, showNotification]
  );

  // Deaktivace uživatele
  const handleDeactivate = useCallback(
    async (userId) => {
      try {
        await userApi.deactivateUser(userId);
        showNotification("Uživatel byl deaktivován.", "success");
        await reload();
      } catch (err) {
        const message =
          err?.response?.data?.message ||
          err?.message ||
          "Deaktivace uživatele se nezdařila.";
        showNotification(message, "danger");
      }
    },
    [reload, showNotification]
  );

  // Otevření modalu pro editaci
  const handleEdit = useCallback((user) => {
    setEditingUser(user);
  }, []);

  // Zavření modalu
  const handleCloseEdit = () => {
    if (!editSaving) {
      setEditingUser(null);
    }
  };

  // Uložení změn uživatele z modalu
  const handleSaveEdit = async (payload) => {
    try {
      setEditSaving(true);

      await userApi.updateAdmin(payload.id, payload);

      showNotification("Uživatel byl úspěšně upraven.", "success");
      setEditingUser(null);
      await reload();
    } catch (err) {
      const message =
        err?.response?.data?.message ||
        err?.message ||
        "Úprava uživatele se nezdařila.";
      showNotification(message, "danger");
    } finally {
      setEditSaving(false);
    }
  };

  // (volitelně) seřadit uživatele podle příjmení
  const sortedUsers = useMemo(() => {
    if (!users) return [];
    return [...users].sort((a, b) => {
      const aSurname = (a.surname || "").toLocaleLowerCase("cs");
      const bSurname = (b.surname || "").toLocaleLowerCase("cs");
      if (aSurname < bSurname) return -1;
      if (aSurname > bSurname) return 1;
      return 0;
    });
  }, [users]);

  return (
    <>
            <BackButton />
    <div className="container mt-4">
 
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h1 className="h3 mb-0">Správa uživatelů</h1>
      </div>

      <p className="text-muted mb-3">
        Zde může administrátor spravovat všechny uživatele v systému.
        Lze upravovat základní údaje, resetovat heslo a aktivovat /
        deaktivovat uživatelské účty. V dolní části každé karty je
        přehled hráčů přiřazených k danému uživateli.
      </p>

      <AdminUsersTable
        users={sortedUsers}
        loading={loading}
        error={error}
        onEdit={handleEdit}
        onResetPassword={handleResetPassword}
        onActivate={handleActivate}
        onDeactivate={handleDeactivate}
      />

      {/* Modal pro úpravu uživatele */}
      <AdminUserEditModal
        user={editingUser}
        show={!!editingUser}
        onClose={handleCloseEdit}
        onSave={handleSaveEdit}
        saving={editSaving}
      />
    
    </div>
    </>
  );
};

export default AdminUsersPage;
