import { OFFICIAL_LANGUAGE } from './configs/environment.config'

export function l<T extends keyof typeof friendlyLanguageData>(key: T, fields?: (typeof parameters)[T]) {
  let message = OFFICIAL_LANGUAGE ? officialLanguageData[key] ?? friendlyLanguageData[key] : friendlyLanguageData[key]
  if (parameters[key]) {
    Object.entries(parameters[key] as Record<string, string>).forEach(([templateKey, value]) => {
      const field = fields?.[templateKey]
      message = message.replace(`{{${templateKey}}}`, field ?? value)
    })
  }
  return message
}

const officialLanguageData: Partial<Record<keyof typeof friendlyLanguageData, string>> = {
  'error-boundary-title': 'Hiba történt!',
  'error-boundary-message': 'Sajnos ilyennel még nem találkoztunk. Legyen szíves jelezni ezt a fejlesztőknek!',
  'not-found-message': 'Hoppá, úgy tűnik egy olyan oldalra került, amely nem létezik többé!',
  'no-permission': 'Nincs jogosultsága megtekinteni!',
  'login-consent': 'Válasszon bejelentkezési módot!',
  'login-helmet': 'Bejelentkezés',
  'logout-title': 'Kijelentkezés',
  'logout-description': 'Sikeres kijelentkezés!',
  'error-page-helmet': 'Hiba',
  'error-page-title': 'Hiba történt',
  'error-service-unavailable-title': 'Próbálkozzon egy kicsit később',
  'error-connection-unsuccessful': 'Kapcsolódás sikertelen',
  'error-service-unavailable': 'Az alkalmazás jelenleg nem elérhető!',
  'unauthorized-page-helmet': 'Nem vagy bejelentkezve',
  'unauthorized-page-title': 'Bejelentkezés szükséges',
  'unauthorized-page-description': 'Az oldal eléréséhez be kell jelentkeznie!',
  'toast-title-success': 'Siker',
  'toast-title-error': 'Hiba',
  'toast-title-warning': 'Figyelmeztetés',
  'toast-title-info': 'Információ',
  'riddle-incorrect-title': 'Helytelen válasz!',
  'riddle-incorrect-description': 'Próbálja meg újra, sikerülni fog!',
  'riddle-correct-title': 'Helyes válasz!',
  'riddle-correct-description': 'Csak így tovább!',
  'riddle-skipped-title': 'Riddle átugorva',
  'riddle-skipped-description': 'A következő menni fog!',
  'riddle-completed-title': 'Minden megvan!',
  'riddle-completed-description': 'Igazán szép munka, kolléga!',
  'riddle-completed-category-title': 'Minden megvan!',
  'riddle-completed-category-description': 'Igazán szép munka, kolléga!',
  'riddle-submission-failed': 'Nem sikerült beadni a Riddle-t.',
  'riddle-skipping-failed': 'Nem sikerült átugrani a Riddle-t.',
  'riddle-history-query-failed': 'Nem sikerült lekérni a megoldott riddleöket',
  'task-empty-title': 'Üres megoldás',
  'task-empty-description': 'Üres megoldást nem küldhet be.',
  'task-too-large-title': 'Túl nagy a fájl',
  'task-too-large-description': 'A feltöltött fájl túllépte a 30 MB-os feltöltési korlátot!',
  'task-not-found-title': 'Feladat nem található',
  'task-not-found-description': 'Ilyen feladat nem létezik vagy nincs jogosultsága hozzá.',
  'task-category-failed': 'Nem sikerült lekérni ezt a feladat kategóriát',
  'token-completed': 'Ahol eddig járt',
  'token-empty': 'Még nem szerzett pecsétet',
  'token-scan-network-error': 'Hálózati hiba a token érvényesítésénél',
  'token-scan-read-error': 'Beolvasási hiba.',
  'location-query-failed': 'A pozíciók nem érhetőek el.',
  'location-sensor-denied': 'Helymeghatározás nem elérhető',
  'location-show-own': 'Magam pozíciójának mutatása',
  'location-description': 'Csak annak a helyzete látható, akinél a helymegosztás engedélyezve (használatban) van.',
  'location-privacy': 'Az Ön pozícióját csak Ön láthatja, nem kerül megosztásra mással.',
  'users-location-title': 'Az Ön pozíciója',
  'component-unavailable': 'Ez a komponens nem elérhető.',
  'result-query-failed': 'Nem sikerült lekérni az eredményeket.',
  'alias-change-successful': 'Becenév sikeresen módosítva',
  'alias-change-failure': 'Nem sikerült megváltoztatni a becenevet',
  'alias-change-not-allowed': 'A becenév szerkesztése nem engedélyezett!',
  'organization-title': 'Reszortok',
  'organization-description': 'Az egyes reszortok a hasonló jellegű köröket összefogó szervezetek.',
  'community-title': 'Körök',
  'community-description': 'A körök fogják össze az azonos érdeklődésű körű hallgatókat. A körök a Schönherz Kollégiumban működnek.',
  'page-load-failed': '{{title}} betöltése sikertelen!',
  'page-load-failed-contact-developers': '{{title}} betöltése sikertelen!\n Keresse az oldal fejlesztőit.',
  'access-token-failed': 'Nem sikerült az azonosítás!',
  'access-token-success': 'Sikeres azonosítás!',
  'access-token-missing': 'Adja meg a kódot!',
  'access-token-not-available': 'Jelenleg nem lehet kódot beváltani.',
  'form-not-available': 'Űrlap nem található, vagy nincs joga hozzá.',
  'form-disabled': 'Űrlap nincs engedélyezve.'
}

const friendlyLanguageData = {
  'error-boundary-title': 'Hiba történt!',
  'error-boundary-message': 'Sajnos ilyennel még nem találkoztunk. Légy szíves ezt jelezd a fejlesztőknek!',
  'not-found-message': 'Hoppá, úgy tűnik egy olyan oldalra kerültél, amely nem létezik többé!',
  'no-permission': 'Nincs jogosultságod megtekinteni!',
  'login-consent': 'Válassz bejelentkezési módot!',
  'login-helmet': 'Bejelentkezés',
  'logout-title': 'Kijelentkezés',
  'logout-description': 'Sikeres kijelentkezés!',
  'error-page-helmet': 'Hiba',
  'error-page-title': 'Kapcsolódás sikertelen',
  'error-service-unavailable-title': 'Próbálkozz egy kicsit később',
  'error-connection-unsuccessful': 'Nem sikerült csatlakozni (egyébként az is lehetséges, hogy épp nem fut a szolgáltatás 🤓)',
  'error-service-unavailable': 'Az alkalmazás jelenleg nem elérhető, próbálkozz egy kicsit később!',
  'unauthorized-page-helmet': 'Nem vagy bejelentkezve',
  'unauthorized-page-title': 'Bejelentkezés szükséges',
  'unauthorized-page-description': 'Az oldal eléréséhez be kell jelentkezned!',
  'toast-title-success': 'Siker',
  'toast-title-error': 'Hiba',
  'toast-title-warning': 'Figyelmeztetés',
  'toast-title-info': 'Információ',
  'riddle-incorrect-title': 'Helytelen válasz!',
  'riddle-incorrect-description': 'Próbáld meg újra, sikerülni fog!',
  'riddle-submitter-banned-title': 'Ki vagy tiltva a riddleökből!',
  'riddle-submitter-banned-description': 'Jár a virgács!',
  'riddle-correct-title': 'Helyes válasz!',
  'riddle-correct-description': 'Csak így tovább!',
  'riddle-skipped-title': 'Riddle átugorva',
  'riddle-skipped-description': 'A következő menni fog!',
  'riddle-completed-title': 'Minden megvan!',
  'riddle-completed-description': 'Igazán szép munka, kolléga!',
  'riddle-completed-category-title': 'Minden megvan!',
  'riddle-completed-category-description': 'Igazán szép munka, kolléga!',
  'riddle-submission-failed': 'Nem sikerült beadni a Riddle-t.',
  'riddle-skipping-failed': 'Nem sikerült átugrani a Riddle-t.',
  'riddle-history-query-failed': 'Nem sikerült lekérni a megoldott riddleöket',
  'task-empty-title': 'Üres megoldás',
  'task-empty-description': 'Üres megoldást nem küldhetsz be.',
  'task-too-large-title': 'Túl nagy a fájl',
  'task-too-large-description': 'A feltöltött fájl túllépte a 30 MB-os feltöltési korlátot!',
  'task-not-found-title': 'Feladat nem található',
  'task-not-found-description': 'Ilyen feladat nem létezik vagy nincs jogosultságod hozzá.',
  'task-category-failed': 'Nem sikerült lekérni ezt a feladat kategóriát',
  'token-completed': 'Ahol eddig jártál',
  'token-empty': 'Még nem szereztél pecsétet',
  'token-scan-network-error': 'Hálózati hiba a token érvényesítésénél',
  'token-scan-read-error': 'Beolvasási hiba.',
  'location-query-failed': 'A pozíciók nem érhetőek el.',
  'location-show-own': 'Saját pozícióm mutatása',
  'location-description': 'Csak annak a helyzete látható, akinél a helymegosztás engedélyezve (használatban) van.',
  'location-privacy': 'A saját pozíciódat csak te látod, nem kerül megosztásra mással.',
  'location-sensor-denied': 'Helymeghatározás nem elérhető',
  'users-location-title': 'A te pozíciód',
  'component-unavailable': 'Ez a komponens nem elérhető.',
  'result-query-failed': 'Nem sikerült lekérni az eredményeket.',
  'alias-change-successful': 'Becenév sikeresen módosítva',
  'alias-change-failure': 'Nem sikerült megváltoztatni a becenevet',
  'alias-change-not-allowed': 'A becenév szerkesztése nem engedélyezett!',
  'organization-title': 'Reszortok',
  'organization-description': 'Az egyes reszortok a hasonló jellegű köröket összefogó szervezetek.',
  'community-title': 'Körök',
  'community-description': 'A körök fogják össze az azonos érdeklődésű körű hallgatókat. A körök a Schönherz Kollégiumban működnek.',
  'page-load-failed': '{{title}} betöltése sikertelen!',
  'page-load-failed-contact-developers': '{{title}} betöltése sikertelen!\n Keresd az oldal fejlesztőit.',
  'access-token-failed': 'Nem sikerült az azonosítás!',
  'access-token-success': 'Sikeres azonosítás!',
  'access-token-missing': 'Add meg a kódot!',
  'access-token-not-available': 'Jelenleg nem lehet kódot beváltani.',
  'form-not-available': 'Űrlap nem található, vagy nincs jogod hozzá.',
  'form-disabled': 'Űrlap nincs engedélyezve.'
}

const parameters: Partial<Record<keyof typeof friendlyLanguageData, Record<string, string | undefined>>> = {
  'page-load-failed': { title: 'Oldal' },
  'page-load-failed-contact-developers': { title: 'Oldal' }
}
