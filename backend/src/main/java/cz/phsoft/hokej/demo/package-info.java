/**
 * Balíček obsahující podporu pro DEMO režim aplikace.
 *
 * Tento balíček sdružuje komponenty, které umožňují provoz aplikace
 * v demonstračním režimu bez reálného provádění vybraných operací,
 * zejména odesílání externí komunikace nebo provádění nevratných změn.
 *
 * Odpovědnosti modulu zahrnují zejména:
 * - detekci a řízení běhu aplikace v DEMO režimu,
 * - náhradu reálných externích integrací jejich demonstračními verzemi,
 * - ukládání simulovaných e-mailů a SMS zpráv do dočasného úložiště,
 * - omezení nebo blokování vybraných operací nevhodných pro demo prostředí.
 *
 * DEMO režim je navržen tak, aby bylo možné prezentovat funkčnost systému
 * bez rizika odesílání skutečných zpráv nebo zásahu do produkčních dat.
 * Jednotlivé služby kontrolují stav DEMO režimu a podle něj upravují
 * své chování.
 */
package cz.phsoft.hokej.demo;