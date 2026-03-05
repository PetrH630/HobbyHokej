import React, { useMemo, useRef } from "react";
import Flatpickr from "react-flatpickr";
import "flatpickr/dist/flatpickr.min.css";
import "flatpickr/dist/themes/material_blue.css";
import { Czech } from "flatpickr/dist/l10n/cs.js";

/**
 * DateTimePicker
 *
 * Formulářová komponenta pro výběr data a času s validací a normalizací hodnot.
 *
 * Props:
 * @param {number} props.id vstupní hodnota komponenty. [default: "dateTime"]
 * @param {string} props.name vstupní hodnota komponenty.
 * @param {string} props.value Aktuální hodnota ovládacího prvku.
 * @param {Function} props.onChange callback pro předání akce do nadřazené vrstvy.
 * @param {Function} props.onBlur vstupní hodnota komponenty.
 * @param {string} props.placeholder vstupní hodnota komponenty. [default: "Vyber datum a čas…"]
 * @param {Object} props.required vstupní hodnota komponenty. [default: false]
 * @param {boolean} props.disabled Příznak, zda jsou ovládací prvky dočasně zakázány.
 * @param {string} props.minDate vstupní hodnota komponenty.
 * @param {string} props.maxDate vstupní hodnota komponenty.
 * @param {string} props.className vstupní hodnota komponenty. [default: "form-control"]
 * @param {Object} props.minuteIncrement vstupní hodnota komponenty. [default: 5]
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


    const parsedValue = useMemo(() => {
        if (!value) return null;


        const d = new Date(value);
        return Number.isNaN(d.getTime()) ? null : d;
    }, [value]);

    const options = useMemo(
        () => ({
            locale: Czech,
            enableTime: true,
            time_24hr: true,


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
