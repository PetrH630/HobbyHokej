import React, { useMemo, useRef } from "react";
import Flatpickr from "react-flatpickr";
import "flatpickr/dist/flatpickr.min.css";
import "flatpickr/dist/themes/material_blue.css";
import { Czech } from "flatpickr/dist/l10n/cs.js";

/**
 * DatePicker
 *
 * Formulářová komponenta pro výběr data a času s validací a normalizací hodnot.
 *
 * Props:
 * @param {number} props.id vstupní hodnota komponenty. [default: "date"]
 * @param {string} props.name vstupní hodnota komponenty.
 * @param {string} props.value Aktuální hodnota ovládacího prvku.
 * @param {Function} props.onChange callback pro předání akce do nadřazené vrstvy.
 * @param {Function} props.onBlur vstupní hodnota komponenty.
 * @param {string} props.placeholder vstupní hodnota komponenty. [default: "Vyber datum…"]
 * @param {Object} props.required vstupní hodnota komponenty. [default: false]
 * @param {boolean} props.disabled Příznak, zda jsou ovládací prvky dočasně zakázány.
 * @param {string} props.minDate vstupní hodnota komponenty.
 * @param {string} props.maxDate vstupní hodnota komponenty.
 * @param {string} props.className vstupní hodnota komponenty. [default: "form-control"]
 */
const DatePicker = ({
    id = "date",
    name,
    value,
    onChange,
    onBlur,
    placeholder = "Vyber datum…",
    required = false,
    disabled = false,
    minDate,
    maxDate,
    className = "form-control",
}) => {
    const fpRef = useRef(null);

    const options = useMemo(
        () => ({
            locale: Czech,
            enableTime: false,
            dateFormat: "Y-m-d",

            altInput: true,
            altFormat: "d.m.Y",
            altInputClass: className,

            allowInput: true,
            clickOpens: true,
            closeOnSelect: true,
            disableMobile: true,

            minDate: minDate || null,
            maxDate: maxDate || null,
        }),
        [minDate, maxDate, className]
    );

    return (
        <Flatpickr
            options={options}
            value={value || ""}
            onReady={(_, __, fp) => {
                fpRef.current = fp;

                if (fp?.altInput) {
                    fp.altInput.id = id;
                    if (name) fp.altInput.name = name;
                    fp.altInput.placeholder = placeholder;
                    fp.altInput.disabled = disabled;
                    fp.altInput.required = required;

                    if (onBlur) {
                        fp.altInput.onblur = onBlur;
                    }
                }
            }}
            onChange={(dates) => {
                const d = dates && dates.length ? dates[0] : null;
                if (!d) return onChange?.("");

                const fp = fpRef.current;
                const formatted = fp ? fp.formatDate(d, "Y-m-d") : "";
                onChange?.(formatted);


                fp?.close?.();
            }}

            render={({ defaultValue }, ref) => (
                <input
                    ref={ref}
                    defaultValue={defaultValue}
                    type="hidden"
                    aria-hidden="true"
                    tabIndex={-1}
                />
            )}
        />
    );
};

export default DatePicker;
