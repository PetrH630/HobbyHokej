import React, { useEffect, useState } from "react";

/**
 * App
 *
 * React komponenta používaná ve frontend aplikaci.
 *
 * @param {Object} props vstupní hodnoty komponenty.
 */
const App = () => {
  const [message, setMessage] = useState("");

  useEffect(() => {
    fetch("/api/test")
      .then((res) => res.text())
      .then((data) => setMessage(data))
      .catch((err) => setMessage("Chyba: " + err));
  }, []);

  return (
    <div>
      <h1>Test připojení k backendu</h1>
      <p>{message}</p>
    </div>
  );
};

export default App;
