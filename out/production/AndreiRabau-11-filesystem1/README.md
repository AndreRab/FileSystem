# zad-11-filesystem

## Definicja
Należy stworzyć własny system plików (MFS, My File System), który będzie działał w jednym katalogu. To oznacza, że ignoruje standardowe podkatalogi systemu operacyjnego.

System rozróżnia wielkośc liter. Dopuszcza nazwy plików i katalogów z zakresu ASCII:
* 0x30 - 0x39 (liczby)
* 0x46 (kropka: `.`)
* 0x5F (podkreślnik: `_`)
* 0x65 - 0x90 (wielkie litery)
* 0x97 - 0x7A (małe litery)

Punktem wejścia do systemu plików ma być plik `root.mfs` (`mfs` - my file system). Plik ten zawiera nawy katalogów oraz plików. Nazwy katalogów są poprzedzone literą `D`, natomiast nazwy plików - literą `F`.

Przykładowa zawartość pliku `root.mfs`:
```
Dusr
Dbin
DDIRECTORY
F123.txt
Ddir110
Fala_ma_kota_kot_ma_ale
```
Znajdują się tu 4 katalogi (usr, bin, DIRECTORY oraz dir110) oraz 2 pliki (123.txt oraz ala_ma_kota_kot_ma_ale).

Każdy podkatalog powinien zawierać spis swojej zawartości, w pliku nazwanym zgodnie z konwencją `rodzice-nazwa_katalogu.mfs`. Część nazwy "rodzice" to lista wszystkich katalogów zawierających ten katalog oddzielonych znakiem `-`. Na przykład: `root-usr-bin-java.mfs` to katalog o nazwie `java` w katalogu `bin`, który jest w katalogu `usr`. Katalog `usr` znajduje się już w głównym katalogu systemu plików (czyli `root.mfs`).

Możliwe jest zagnieżdżanie katalogów o takiej samej nazwie, np. `root-root-root.mfs` to dopuszczalny katalog `root` w katalogu `root` w głównym katalogu.

Pliki powinny w swojej nazwie również zawierać ścieżkę aż do root-a. Aby odzwierciedlić plik z przykładu powyżej `123.txt` powinien on się nazywać `root-123.txt`.

MFS nie obsługuje uprawnień ani dat - można zignorować.

## Program
Sam program powinien obsługiwać ten system plików przez parametry programu. Kolejne operacje poniżej są jednocześnie nazwą parametru. Część komend przyjmuje dodatkowe arguemnty - są one opisane przy tych komendach.

Ponieważ MFS nie posiada pojęcia katalogu bieżącego, nazwy katalogów oraz plików powinny zawsze być w pełni kwalifikowane - czyli np `root-123.txt`, a nie `123.txt`.

* `ls [dir_name]` - wyświetla zawartość katalogu o podanej nazwie. Wyświetlanie musi jasno rozróżniać pliki od katalogów. Sortowanie po nazwach malejąco, w kolejności kodów ASCII. Jeśli taki katalog nie istnieje, wyświetla błąd.
* `mkdir [dir_name]` - tworzy katalog o podanej nazwie. Jeśli taki katalog już istnieje, wyświetla błąd.
* `rmdir [dir_name]` - usuwa katalog o podanej nazwie z całą zawartością. Jeśli taki katalog nie istnieje, wyświetla błąd.
* `mvdir [src_dir] [target_dir]` - przesuwa katalog `src_dir` do katalogu `target_dir`. Jeśli któryś z katalogów nie istnieje, wyświetla błąd. Jeśli wprowadziłoby to cykl w drzewie katalogów, wyświetla błąd.
* `touch [file_name]` - tworzy pusty plik o podanej nazwie. Jeśli taki plik już istnieje, nie dzieje się nic.
* `echo [file_name] [content]` - dopisuje do pliku zawartość parametru `content`.
* `cat [file_name]` - wyświetla zawartość pliku. Jeśli taki plik nie istnieje, wyświetla błąd.
* `delete [file_name]` - usuwa plik o podanej nazwie. Jeśli taki plik nie istnieje, nic się nie dzieje.
* `copy [src_file] [target_dir]` - kopiuje plik `src_file` do katalogu `target_dir`. Jeśli któreś z nich nie istnieje, wyświetla błąd. Jeśli w katalogu docelowym plik o takiej nazwie już istnieje, wyświetla błąd.
* `mv [src_file] [target_dir]` - przesuwa plik `src_file` do katalogu `target_dir`. Jeśli któreś z nich nie istnieje, wyświetla błąd. Jeśli w katalogu docelowym plik o takiej nazwie już istnieje, wyświetla błąd.
* `help` - wyświetla możliwe komendy z krótkim opisem sposobu ich użycia.

## Przykład
Należy zaimplementować również przykład pokazujący działanie wszytkich opcji, łącznie z obsługą sytuacji błędnych. Przykład może być w formie testów JUnit albo skryptu sh. Dla uproszczenia, wszystkie pliki mogą być tekstowe.
