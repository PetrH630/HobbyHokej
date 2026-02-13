// src/components/settings/ChangePasswordForm.jsx

const ChangePasswordForm = ({
    values,
    errors = {},
    saving,
    success,
    error,
    onChange,
    onSubmit,
}) => {
    const handleInputChange = (e) => {
        const { name, value } = e.target;
        onChange({ [name]: value });
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        onSubmit();
    };

    return (
        <form onSubmit={handleSubmit}>
            {error && (
                <div className="alert alert-danger">
                    {error}
                </div>
            )}

            {success && (
                <div className="alert alert-success">
                    {success}
                </div>
            )}

            <div className="mb-3">
                <label htmlFor="oldPassword" className="form-label">
                    Stávající heslo
                </label>
                <input
                    type="password"
                    id="oldPassword"
                    name="oldPassword"
                    className={
                        "form-control" +
                        (errors.oldPassword ? " is-invalid" : "")
                    }
                    value={values.oldPassword}
                    onChange={handleInputChange}
                    autoComplete="current-password"
                />
                {errors.oldPassword && (
                    <div className="invalid-feedback">
                        {errors.oldPassword}
                    </div>
                )}
            </div>

            <div className="mb-3">
                <label htmlFor="newPassword" className="form-label">
                    Nové heslo
                </label>
                <input
                    type="password"
                    id="newPassword"
                    name="newPassword"
                    className={
                        "form-control" +
                        (errors.newPassword ? " is-invalid" : "")
                    }
                    value={values.newPassword}
                    onChange={handleInputChange}
                    autoComplete="new-password"
                />
                {errors.newPassword && (
                    <div className="invalid-feedback">
                        {errors.newPassword}
                    </div>
                )}
                <div className="form-text">
                    Doporučeno alespoň 8 znaků, kombinace písmen a číslic.
                </div>
            </div>

            <div className="mb-3">
                <label htmlFor="newPasswordConfirm" className="form-label">
                    Potvrzení nového hesla
                </label>
                <input
                    type="password"
                    id="newPasswordConfirm"
                    name="newPasswordConfirm"
                    className={
                        "form-control" +
                        (errors.newPasswordConfirm ? " is-invalid" : "")
                    }
                    value={values.newPasswordConfirm}
                    onChange={handleInputChange}
                    autoComplete="new-password"
                />
                {errors.newPasswordConfirm && (
                    <div className="invalid-feedback">
                        {errors.newPasswordConfirm}
                    </div>
                )}
            </div>

            <div className="d-flex justify-content-end mt-3">
                <button
                    type="submit"
                    className="btn btn-primary"
                    disabled={saving}
                >
                    {saving ? "Měním heslo…" : "Změnit heslo"}
                </button>
            </div>
        </form>
    );
};

export default ChangePasswordForm;
