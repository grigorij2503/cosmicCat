# CosmicCat

Polska gra 2D dla dzieci. Stwórz koto-astronautę, wpisz cel podróży i odkrywaj kosmos. Za każdą misję CatX wypłaca 12 monet, które można wydać na budowę i ulepszanie portu. Gra działa offline, bez reklam i mikropłatności.

W wersji 1.1 wyprawy mają trzy obszary, zagadkę, nasionko i sekretną naklejkę. Na mapie można wpisać nazwę wcześniej odwiedzonej planety i wrócić do niej bez zmiany głównej misji. Karty budynków opisują aktywne bonusy. Wejścia do szklarni i domków przyjaciół są w ekranie budowy portu.

## Uruchomienie

Otwórz ten katalog w Android Studio z obsługą Android Gradle Plugin 9.4 i zainstalowanym Android SDK 37. Uruchom moduł `app` na urządzeniu z Androidem 9 lub nowszym. Pierwsza synchronizacja Gradle może pobrać zależności kompilacji.

Na Windowsie ustaw `JAVA_HOME` i `ANDROID_HOME`, a następnie uruchom poniższe polecenie.

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
```

APK debug znajduje się w `app/build/outputs/apk/debug/app-debug.apk`.

Identyfikator aplikacji w obu wariantach kompilacji to `pl.pointblank.cosmiccat`. Przełączenie między wariantami podpisanymi różnymi kluczami wymaga odinstalowania wcześniejszego wariantu. Zapisy instalacji o wcześniejszych identyfikatorach pozostają w ich prywatnej pamięci.

## Wydanie

```powershell
.\gradlew.bat :app:bundleRelease
```

Wariant `release` używa R8 do optymalizacji kodu i usuwania zbędnych zasobów. Plik `app/src/main/res/raw/pl_pointblank_cosmiccat_keep.xml` chroni grafiki ładowane po nazwie.

Bundle trafia do `app/build/outputs/bundle/release/app-release.aab`. To polecenie tworzy plik bez podpisu. Do publikacji użyj klucza przesyłania przypisanego do aplikacji, np. przez **Build → Generate Signed Bundle/APK** w Android Studio. Zachowaj `app/build/outputs/mapping/release/mapping.txt` z tego samego wydania, aby móc odczytać raporty błędów.

Polityka prywatności znajduje się w [docs/privacy-policy.html](docs/privacy-policy.html). W ustawieniach GitHub Pages wybierz **Deploy from a branch → main → /docs**. Po udostępnieniu strony wpisz [jej publiczny adres](https://grigorij2503.github.io/cosmicCat/privacy-policy.html) w polu **Privacy policy URL** w Play Console. Ten sam adres otwiera link w kreatorze kotka i w Porcie Kosmicznym.

## Struktura

- `app/src/main/java/pl/pointblank/cosmiccat` zawiera modele i zapis gry, `GameViewModel`, platformówkę, ekrany Compose oraz system bitmap.
- `app/src/main/res/drawable-nodpi` zawiera bitmapy używane w grze. `ProceduralBitmaps.kt` odtwarza je w pamięci, jeśli brakuje plików w tym katalogu.
- `scripts/generate_assets.py` odtwarza bitmapy i ikonę aplikacji. Aby uruchomić generator, zainstaluj Pillow i wykonaj `python scripts/generate_assets.py`. Zasoby są dołączone do projektu, więc generator jest opcjonalny.
- `app/src/test` zawiera testy walidacji celu, wypłat, cen, odmiany komunikatów i fizyki.

Postępy i kopie wybranych obrazków trafiają do prywatnej pamięci aplikacji. Po ponownym uruchomieniu gra przywraca ostatnie lądowanie, podsumowanie odkrycia lub oczekującą nagrodę. W razie uszkodzenia zapisu można odzyskać poprzednią wersję razem z przypisanymi do niej obrazkami albo rozpocząć nową grę, gdy kopia jest niedostępna.

## Ciekawostki naukowe

Źródła NASA wykorzystane w grze. [Księżyc](https://science.nasa.gov/solar-system/moon/10-cool-things-nasas-lunar-reconnaissance-orbiter-is-teaching-us-about-the-moon/), [Mars](https://science.nasa.gov/mars/facts/), [Pluton](https://science.nasa.gov/dwarf-planets/pluto/facts/), [Wenus](https://science.nasa.gov/venus/venus-facts/), [Jowisz](https://science.nasa.gov/jupiter/jupiter-facts/), [Saturn](https://science.nasa.gov/solar-system/planets/), [Neptun](https://science.nasa.gov/neptune/neptune-facts/).
