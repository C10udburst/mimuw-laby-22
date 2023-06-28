# Lambda wyrażenia

*Osoby rozumiejące motywacje i mechanizmy za lambda wyrażeniami, mogą przejrzeć część omawiającą, 
na pewno warto obejrzeć [Przyklad7](src/p48/cotomoze/przyklad7/Przyklad7.java)
i [Przyklad8](src/p48/cotomoze/przyklad8/Przyklad8.java) i przejść od razu do zadania (na końcu).*


## 0-1. Wstęp

Pamiętacie assertThrows z JUnit?
```java
    assertThrows( () -> testowanaMetoda(1,0) );
```

to w środku to właśnie lambda wyrażenie (ang. *lambda expression*).  
BTW: Lambda λ to symbol anonimowej funkcji (na pewno pamiętacie z Podstaw matematyki, np. λx.x+2 :)  
Symbol pochodzi z rachunku lambda (https://pl.wikipedia.org/wiki/Rachunek_lambda).

Ale po co to w ogóle tu jest? Nie mogłoby być tak?
```java
    assertThrows( testowanaMetoda(1,0) ); 
```
No właśnie nie mogłoby... patrz [Przyklad0](src/p01/ocochodzi/przyklad0/Przyklad0.java)

### Z jakich dokładnie części to się wszystko składa?
- interfejs funkcyjny (czyli mający dokładnie **jedną** metodą abstrakcyjną)
- najlepiej oznaczony adnotacją @FunctionalInterface
- argument jakiejś metody (albo inne miejsce w kodzie) typu "ten intefejs"
- lambda wyrażenie: zamiast obiektu (pod)typu "ten interfejs"

patrz [Przyklad1](src/p01/ocochodzi/przyklad1/Przyklad1.java)

BTW: interfejs funkcyjny z funkcją bezparametrową używany jest nie tylko w JUnit.
Również w wątkach pojawia się interfejs Runnable z jedną metodą void run()
```java
    new Thread( () -> System.out.println("biegnę!") ).start();
```
<Ogólnie wszędzie tam gdzie chcemy wykonać jakiś kod być może wielokrotnie ale w innej sytuacji>

Lambda wyrażenie oczywiście może przyjmować argumenty i obliczać jakiś wynik, ale o tym za chwilę.

## 2-3. Jak to się ma do klas i obiektów?
Lambda wyrażenie formalnie jest obiektem nowej (anonimowej) klasy implementującej
(tą jedną metodą) swój interfejs.  
Patrz [Przyklad2](src/p23/cotojest/przyklad2/Przyklad2.java) i [Przyklad3](src/p23/cotojest/przyklad3/Przyklad3.java).  
Ale tak naprawdę w skompilowanym kodzie z lambda-wyrażeniem nie pojawia się żadna klasa...
Porównaj (po skompilowaniu całego projektu):
```shell
   ls -l out/production/*/p01/ocochodzi/przyklad1/*
   ls -l out/production/*/p23/cotojest/przyklad3/*
```

Dla ciekawskich:
```shell
   javap -p -v out/production/*/p01/ocochodzi/przyklad1/Przyklad1.class | less
```
Widać metodę prywatną *lambda$main$0* (na dole),  
w main:
```
   0: invokedynamic #7,  0              // InvokeDynamic #0:rob:()Lp01/ocochodzi/przyklad1/JakisKod;
```
i na samym dole
```
BootstrapMethods:
  0: #53 REF_invokeStatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;
    Method arguments:
      #60 ()V
      #61 REF_invokeStatic p01/ocochodzi/przyklad1/Przyklad1.lambda$main$0:()V
      #60 ()V
InnerClasses:
  public static final #69= #65 of #67;    // Lookup=class java/lang/invoke/MethodHandles$Lookup of class java/lang/invoke/MethodHandles
```
Czyli ten obiekt i ta anonimowa klasa są robione *magicznie* (czyli wywołaniem wewnętrznej metody *LambdaMetafactory.metafactory*) z dodatkowej prywatnej metody.

## 4-8. Co z tym można zrobić?

- Lambda wyrażenia mogą używać zmiennych z kontekstu (ale bez zmieniania!)
- mogą zawierać więcej kodu, nie tylko jedną linijkę
- można ich używać jak normalnych obiektów (jakiejś klasy dziedziczącej po ich interfejsie), 
  przypisywać na zmienną, przekazywać do metod, przetwarzać itp.  

patrz [Przyklad4](src/p48/cotomoze/przyklad4/Przyklad4.java)

- mogą przyjmować parametry

patrz [Przyklad5](src/p48/cotomoze/przyklad5/Przyklad5.java)

- mogą też dawać wynik

patrz [Przyklad6](src/p48/cotomoze/przyklad6/Przyklad6.java)

- w samym lambda wyrażeniu można (czasem trzeba) podać typ argumentu
- można też podać typ interfejsu (ale to w zasadzie przed wyrażeniem) - cast
- mogą być typu generycznego
- mogą mieć dowolną liczbę parametrów

patrz [Przyklad7](src/p48/cotomoze/przyklad7/Przyklad7.java)

Ogólna składnia lambda wyrażenia to jedno z:
```
parametry -> wynik
parametry -> { instrukcje }
```
gdzie parametry to jedno z:
```
()
nazwa
(nazwa₁, ..., nazwaₙ)
(Typ₁ nazwa₁, ..., Typₙ nazwaₙ)
```
a wynik to wyrażenie (może być typu void)  

W bibliotece standardowej przydają się do:
- porządkowania - interface Comparator< E > { int compare(E e1, E e2); }  
  sort, binarySearch, SortedSet / TreeSet, SortedMap / TreeMap
- robienia czegoś z elementami kolekcji - interface Consumer< E > { void consume(E e); }  
  metoda forEach z interfejsu Iterable
- filtrowania - interface Predicate< E > { boolean test(E e); }  
  metoda removeIf z interfejsu Collection
- przerabiania - interface UnaryOperator< E > { E apply (E e); }   
  metoda replaceAll z interfejsu List
- robienia wielu rzeczy ze strumieniami  
  (ale o tym przy innej okazji :)
- definiowania różnych akcji UI (np. po naciśnięciu Button-a)

patrz [Przyklad8](src/p48/cotomoze/przyklad8/Przyklad8.java)

BTW: Trzy odsłony odwołania do metody
- obiekt::nazwa  
  (x₁, ..., xₙ) -> obiekt.nazwa(x₁, ..., xₙ)
- Klasa::nazwa, gdzie nazwa to metoda statyczna  
  (x₁, ..., xₙ) -> Klasa.nazwa(x₁, ..., xₙ)
- Klasa::nazwa, gdzie nazwa to metoda obiektowa  
  (x₁, ..., xₙ) -> x₁.nazwa(x₂, ..., xₙ)

Większość interfejsów funkcyjnych z biblioteki standardowej opisana jest w pakiecie  
[java.util.function](https://docs.oracle.com/en/java/javase/20/docs/api/java.base/java/util/function/package-summary.html)

Tam jest strasznie dużo interfejsów, ale to przez typy bazowe (i dążenie do efektywności).  
Tak naprawdę interfejsy są kilku rodzajów:
* Function - coś bierze i coś zwraca
* Consumer - coś bierze i nic nie zwraca (np. zmienia to coś, albo zapisuje do pliku) 
* Supplier - nic nie bierze i coś zwraca (np. czyta z wcześniej otwartego pliku)
* Predicate - coś bierze i zwraca boolean (mówi czy to coś jest *dobre*)

Dodatkowo
* BiFunction, BiConsumer, BiPredicate - bierze dwie rzeczy i...
* UnaryOperator - coś bierze i zwraca coś tego samego typu
* BinaryOperator - bierze dwie rzeczy tego samego typu i zwraca coś też tego typu

Część z tych interfejsów ma dodatkowe metody statyczne i domyślne, wspomagające tworzenie odp. funkcji.

Warto też obejrzeć [oficjalny tutorial Javy na temat lambda wyrażeń](https://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html). Jest tam jeden spójny przykład: wyciągnij niektóre (Predicate) elementy z kolekcji, przerób je (Function) i obsłuż (Consumer), w wersji stopniowo coraz bardziej abstrakcyjnej.


# Zadanie

Zadanie będzie o interfejsie Porównywacz (wszelkie podobieństwo nieprzypadkowe :)  
Trzeba samemu przez to przejść, żeby potem świadomie z tych wszystkich funkcji korzystać.
Dla zaawansowanych proponuję "szybką ścieżkę": punkty 1, 4,5, 7,8,9 (ale przeczytać trzeba wszystko).

1. Przerób dany algorytm sortujący (wraz z porównywaczem) na wersję generyczną.

2. Posortuj, używając załączonej metody SortowaniePrzezWybieranie oraz lambda wyrażenia, podane samochody wg:
   * ceny
   * liczby miejsc
   * alfabetycznie po nazwie marki i modelu (tym razem możesz po prostu użyć łączenia napisów)

   (chodzi o wypisanie wszystkich samochodów trzykrotnie, za każdym razem w innej kolejności)

   Możesz oczywiście używać metod *Integer::compare* i *String::compareTo*.

To trochę nudne, bo trzeba wyciągać dane z jednego obiektu, wyciągać dane z drugiego obiektu i potem je porównywać. 
Pomóżmy sobie trochę:
3. Zrób metodę, która połączy wyciąganie z porównywawaniem...
   czyli robimy nowy interfejs funkcyjny "wyciągnij int z samochodu" (np. *IntWyciagacz*)
   oraz metodę *porownywaczZIntWyciagacza*, która zamieni wyciągacz w porównywacz samochodów :)
   Czy to powinna być metoda statyczna czy obiektowa?
   Użyj jej do (wygodniejszego) sortowania samochodów wg ceny, wg liczby osób.  

4. Uogólnij Wyciągacz na typ O i W (wyciągacz wyciąga typ wyciągnięty W z obiektu O)
   oraz zrób generyczną metodę *zamieniacz*, która z Wyciągacza\<O,W> i Porownywacza\<W> utworzy Porownywacz\<O>.

5. Wykorzystaj utworzoną metodę *zamieniacz* do posortowania samochodów wg ceny oraz wg marki i modelu

Teraz posortuj samochody wg pary marka, cena, ale bez używania łączenia napis+liczba! To byłoby oszukiwanie :)  

6. Napisz odpowiedni porównywacz ręcznie. Możesz to zrobić jako lambda wyrażenie,
   albo jako osobna metoda. W tym drugim przypadku użyj odwołania do metody (method reference) w wywołaniu sortowania.

7. Napisz metodę *kolejno*, która bierze Porownywacz\<O> p1 i Porównywacz\<O> p2, i produkuje lepszy Porównywacz\<O>, 
   który próbuje porównywać elementy za pomocą p1, a jak to porównanie nie rozstrzyga, który element jest większy, 
   to porównuje za pomocą p2.
   Użyj jej (oraz metod z wyciągaczem) do porównania wg pary marka + cena.

8. Umieść odpowiednie wersje metod *zamieniacz* i *kolejno* w interfejsie *Porownywacz* jako metody
   default i/lub static.
   Skorzystaj z nich do sortowania wg ceny oraz wg pary marka + cena.

A teraz zobacz, jak to się robi naprawdę:
9. Wykonaj oba sortowania z poprzedniego punktu, korzystając z metody sort w klasie List, 
   używając odpowiednich metod z interfejsu Comparator.  
   Zauważ, że są tam metody podobne do naszej porownywaczZIntWyciagacza,  
   inne z kolei bazują na porównaniu właściwym dla danej klasy (interfejs Comparable).  
   Zadanie można rozwiązać na parę sposobów. Nie krępuj się :)

Na koniec przejrzyj pozostałe metody z interfejsu Comparator i zastanów się jak mogą być zaimplementowane... 
