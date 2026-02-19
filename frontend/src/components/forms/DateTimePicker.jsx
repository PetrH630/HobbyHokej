// src/components/forms/DateTimePicker.jsx
import React, { useMemo, useRef } from "react";
import Flatpickr from "react-flatpickr";
import "flatpickr/dist/flatpickr.min.css";
import "flatpickr/dist/themes/material_blue.css";
import { Czech } from "flatpickr/dist/l10n/cs.js";

/**
 * DateTime picker pro Bootstrap formuláře.
 *
 * value: string ve formátu "YYYY-MM-DDTHH:mm" (stejně jako datetime-local)
 * onChange: (valueString) => void
 */
const DateTimePicker = ({
    id = "dateTime",
    name,
    value,
    onChange,
    onBlur,
    placeholder = "Vyber datum a čas…",
    required = false,
    disabled = false,
    minDate,
    maxDate,
    className = "form-control",
    minuteIncrement = 5,
}) => {
    const fpRef = useRef(null);

    // ✅ string -> Date pro zobrazení ve flatpickru
    const parsedValue = useMemo(() => {
        if (!value) return null;

        // value je "YYYY-MM-DDTHH:mm" -> JS to bere jako "local"
        const d = new Date(value);
        return Number.isNaN(d.getTime()) ? null : d;
    }, [value]);

    const options = useMemo(
        () => ({
            locale: Czech,
            enableTime: true,
            time_24hr: true,

            // ✅ zobrazujeme česky přímo v inputu (bez altInput => žádný druhý input)
            dateFormat: "d.m.Y H:i",

            minuteIncrement,
            allowInput: true,
            disableMobile: true,

            minDate: minDate || null,
            maxDate: maxDate || null,
        }),
        [minuteIncrement, minDate, maxDate]
    );

    return (
        <Flatpickr
            options={options}
            value={parsedValue}
            onReady={(_, __, fp) => {
                fpRef.current = fp;
            }}
            onChange={(dates) => {
                const d = dates && dates.length ? dates[0] : null;
                if (!d) {
                    onChange?.("");
                    return;
                }

                const fp = fpRef.current;

                // ✅ do stavu posíláme stabilní ISO-local string (datetime-local)
                const formatted = fp ? fp.formatDate(d, "Y-m-d\\TH:i") : "";
                onChange?.(formatted);
            }}
            render={(_, ref) => (
                <input
                    id={id}
                    name={name}
                    ref={ref}
                    className={className}
                    placeholder={placeholder}
                    disabled={disabled}
                    required={required}
                    onBlur={onBlur}
                    autoComplete="off"
                />
            )}
        />
    );
};

export default DateTimePicker;
