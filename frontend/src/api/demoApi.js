// api/demoApi.js
import api from "./axios"; // tvůj axios instance

export async function tryGetDemoNotifications() {
  try {
    const res = await api.get("/demo/notifications", { withCredentials: true });
      console.log("[DEMO] notifications OK:", res.data);
    return res.data; // DemoNotificationsDTO

  } catch (e) {
    const status = e?.response?.status;
      console.log("[DEMO] notifications FAIL:", {
          status,
          url: e?.config?.url,
          baseURL: e?.config?.baseURL,
          message: e?.message,
          data: e?.response?.data,
      });
    // 404 = endpoint v produkci neexistuje (pojistka), 403 = security blokuje
    if (status === 404 || status === 403) return null;
    return null; // ať to neláme flow ani při jiných chybách
  }
}

export async function tryClearDemoNotifications() {
    try {
        await api.delete("/demo/notifications", { withCredentials: true });
        return true;
    } catch (e) {
        const status = e?.response?.status;
        if (status === 404 || status === 403) return false;
        return false;
    }
}

