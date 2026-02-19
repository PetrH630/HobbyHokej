// src/components/forms/DatePicker.jsx
import React, { useMemo, useRef } from "react";
import Flatpickr from "react-flatpickr";
import "flatpickr/dist/flatpickr.min.css";
import "flatpickr/dist/themes/material_blue.css";
import { Czech } from "flatpickr/dist/l10n/cs.js";

/**
 * Date picker pro Bootstrap formuláře.
 *
 * value: string ve formátu "YYYY-MM-DD"
 * onChange: (valueString) => void
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

                // jistota zavření po výběru (eliminace "2 kliků" u některých kombinací)
                fp?.close?.();
            }}
            // ✅ originální input schováme (viditelný je jen altInput)
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
